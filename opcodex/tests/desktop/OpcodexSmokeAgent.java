/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Callable;
import javax.imageio.ImageIO;
import javax.swing.*;

// In-process smoke harness for a disposable application profile. Not shipped in the app.
public class OpcodexSmokeAgent {
    private static final Path OUTPUT = Path.of(System.getProperty("opcodex.smoke.output"));
    private static final long DEADLINE = System.currentTimeMillis() + 240000;
    private static Class<?> type(String name) throws Exception {
        return Class.forName(name, true, ClassLoader.getSystemClassLoader());
    }
    private static <T> T edt(Callable<T> action) throws Exception {
        final Object[] result = new Object[1];
        final Exception[] failure = new Exception[1];
        SwingUtilities.invokeAndWait(() -> {
            try { result[0] = action.call(); } catch (Exception e) { failure[0] = e; }
        });
        if (failure[0] != null) throw failure[0];
        return (T) result[0];
    }
    private static AbstractButton button(Component c, String label) {
        if (c instanceof AbstractButton b && label.equals(b.getText())) return b;
        if (c instanceof Container parent) {
            for (Component child : parent.getComponents()) {
                AbstractButton found = button(child, label);
                if (found != null) return found;
            }
        }
        return null;
    }
    private static AbstractButton named(Component c, String name) {
        if (c instanceof AbstractButton b && name.equals(b.getName())) return b;
        if (c instanceof Container parent) for (Component child : parent.getComponents()) {
            AbstractButton found = named(child, name); if (found != null) return found;
        }
        return null;
    }
    private static JMenuItem menuItem(JPopupMenu popup, String name) {
        for (Component child : popup.getComponents()) {
            if (child instanceof JMenu menu) {
                JMenuItem found = menuItem(menu.getPopupMenu(), name); if (found != null) return found;
            }
            if (child instanceof JMenuItem item && name.equals(item.getText())) return item;
        }
        return null;
    }
    private static void expandFunctions(Component c) {
        if (c instanceof JTree tree && tree.isShowing()) {
            Object root = tree.getModel().getRoot();
            for (int i = 0; i < tree.getModel().getChildCount(root); i++) {
                Object child = tree.getModel().getChild(root, i);
                if (child.toString().equals("Functions"))
                    tree.expandPath(new javax.swing.tree.TreePath(new Object[]{root, child}));
            }
        }
        if (c instanceof Container parent) for (Component child : parent.getComponents()) expandFunctions(child);
    }
    private static boolean hasCleanProgress(Component c) {
        if (c.getClass().getName().equals("ghidra.util.task.ChompingBitsAnimationPanel")) {
            Container panel = (Container) c;
            return panel.getComponentCount() == 1 && panel.getComponent(0) instanceof JProgressBar progress && progress.isIndeterminate();
        }
        if (c instanceof Container parent) for (Component child : parent.getComponents()) if (hasCleanProgress(child)) return true;
        return false;
    }
    private static void dumpTableState(Component c, StringBuilder output) throws Exception {
        if (c instanceof JTable table && table.isShowing()) {
            Class<?> base = type("docking.widgets.table.GTableColumnModel");
            if (base.isInstance(table.getColumnModel())) {
                Field field = base.getDeclaredField("columnModelState"); field.setAccessible(true);
                Object state = field.get(table.getColumnModel());
                Class<?> stateClass = type("docking.widgets.table.TableColumnModelState");
                Method key = stateClass.getDeclaredMethod("getPreferenceKey"); key.setAccessible(true);
                Method save = stateClass.getDeclaredMethod("saveToXML"); save.setAccessible(true);
                output.append("KEY=").append(key.invoke(state)).append("\n");
                if (table.getRowCount() > 0) for (int col=0; col<table.getColumnCount(); col++) {
                    Component renderer=table.prepareRenderer(table.getCellRenderer(0,col),0,col);
                    output.append("CELL value=").append(table.getValueAt(0,col)).append(" text=").append(renderer instanceof JLabel label ? label.getText() : "?").append(" fg=").append(renderer.getForeground()).append(" bg=").append(renderer.getBackground()).append("\n");
                }

                Object xml = save.invoke(state);
                Class<?> element = type("org.jdom2.Element");
                for (Object column : (java.util.List<?>) element.getMethod("getChildren").invoke(xml)) {
                    output.append("COLUMN ");
                    for (String attr : new String[]{"NAME", "WIDTH", "VISIBLE"}) output.append(attr).append("=").append(element.getMethod("getAttributeValue", String.class).invoke(column, attr)).append(" | ");
                    output.append("\n");
                }
            }
        }
        if (c instanceof Container parent) for (Component child : parent.getComponents()) dumpTableState(child, output);
    }
    private static String title(Window w) {
        if (w instanceof Frame f) return f.getTitle();
        if (w instanceof Dialog d) return d.getTitle();
        return "";
    }
    private static Window mainWindow() {
        for (Window w : Window.getWindows()) if (w.isShowing() && title(w).startsWith("Opcodex:")) return w;
        return null;
    }
    private static Window window(String contains) throws Exception {
        return edt(() -> {
            for (Window w : Window.getWindows()) if (w.isShowing() && title(w).contains(contains)) return w;
            return null;
        });
    }
    private static void capture(Window w, String path) throws Exception {
        Rectangle bounds = edt(() -> { for (Window other : Window.getWindows()) if (other.isShowing() && title(other).contains("Tip of the Day")) other.dispose(); Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds(); if (w instanceof Frame) w.setBounds(screen.x+20, screen.y+20, Math.min(w.getWidth(), screen.width-40), Math.min(w.getHeight(), screen.height-40)); w.setAlwaysOnTop(true); w.toFront(); w.requestFocus(); return w.getBounds(); });
        edt(() -> { if (w instanceof RootPaneContainer frame) { JRootPane root=frame.getRootPane(); root.revalidate(); root.paintImmediately(0,0,root.getWidth(),root.getHeight()); } Toolkit.getDefaultToolkit().sync(); return null; });
        Thread.sleep(700);
        bounds.grow(-8, -8); // Capture inside the native frame, excluding transparent OS borders.
        Path file = OUTPUT.resolve(path);
        Files.createDirectories(file.getParent());
        java.awt.image.MultiResolutionImage capture = new Robot().createMultiResolutionScreenCapture(bounds);
        BufferedImage image = (BufferedImage) capture.getResolutionVariants().get(capture.getResolutionVariants().size() - 1);
        ImageIO.write(image, "png", file.toFile());
        edt(() -> { w.setAlwaysOnTop(false); return null; });
    }
    private static Window waitFor(String contains) throws Exception {
        Window w;
        while ((w = window(contains)) == null && System.currentTimeMillis() < DEADLINE) Thread.sleep(250);
        if (w == null) throw new AssertionError("Missing window: " + contains);
        return w;
    }
    private static void verifySurfaceTokens() throws Exception {
        edt(() -> {
            Color expected = (Color) type("generic.theme.GColor").getConstructor(String.class)
                .newInstance("system.color.bg.control");
            for (String key : new String[]{"Panel.background", "ToolBar.background",
                    "MenuBar.background", "TitlePane.background", "TabbedPane.background"}) {
                Color actual = UIManager.getColor(key);
                if (actual == null || actual.getRGB() != expected.getRGB())
                    throw new AssertionError("Incorrect live surface token: " + key);
            }
            return null;
        });
    }
    private static void dumpButtons(Component component, StringBuilder output) {
        if (component instanceof AbstractButton b && b.isShowing()) {
            Icon icon = b.getIcon();
            output.append(b.getName()).append(" | ").append(b.getText()).append(" | ")
                .append(b.getToolTipText()).append(" | ").append(icon == null ? "none" : icon.getClass().getName() + ":" + icon.toString()).append("\n");
        }
        if (component instanceof Container c) for (Component child : c.getComponents()) dumpButtons(child, output);
    }
    private static void run() {
        try {
            Files.createDirectories(OUTPUT);
            boolean accepted = false;
            Window main = null;
            while (System.currentTimeMillis() < DEADLINE && main == null) {
                Window agreement = edt(() -> {
                    for (Window w : Window.getWindows()) if (w.isShowing() && button(w, "I Agree") != null) return w;
                    return null;
                });
                if (agreement != null && !accepted) {
                    capture(agreement, "agreement.png");
                    edt(() -> { button(agreement, "I Agree").doClick(); return null; });
                    accepted = true;
                }
                main = window("Opcodex:");
                Thread.sleep(250);
            }
            if (main == null) throw new AssertionError("Opcodex project window did not start");
            StringBuilder defaults = new StringBuilder();
            edt(() -> {
                UIManager.getDefaults().entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().toString()))
                    .forEach(e -> { if (e.getValue() instanceof Color c) defaults.append(e.getKey()).append("=").append(String.format("#%06x", c.getRGB() & 0xffffff)).append("\n"); });
                return null;
            });
            Files.writeString(OUTPUT.resolve("uidefaults.txt"), defaults.toString());
            if (edt(() -> button(mainWindow(), "Create Project")) == null ||
                edt(() -> button(mainWindow(), "Open Project")) == null) {
                throw new AssertionError("Missing welcome project actions");
            }
            verifySurfaceTokens();
            capture(main, "dark/welcome.png");
            Class<?> themeManagerClass = type("generic.theme.ThemeManager");
            Object themeManager = themeManagerClass.getMethod("getInstance").invoke(null);
            Object active = themeManagerClass.getMethod("getActiveTheme").invoke(themeManager);
            if (!"Opcodex Dark".equals(type("generic.theme.GTheme").getMethod("getName").invoke(active)))
                throw new AssertionError("Unexpected first-run theme");
            Collection<?> themes = (Collection<?>) type("generic.theme.ApplicationThemeManager")
                .getMethod("getAllThemes").invoke(themeManager);
            Set<String> names = new HashSet<>();
            for (Object entry : themes) names.add((String) type("generic.theme.GTheme").getMethod("getName").invoke(entry));
            if (!names.containsAll(Arrays.asList("Opcodex Light", "Opcodex Dark")))
                throw new AssertionError("Product themes are not discoverable");
            SwingUtilities.invokeLater(() -> button(mainWindow(), "Create Project").doClick());
            Window wizard = waitFor("New Project");
            capture(wizard, "dark/create-project.png");
            edt(() -> { button(wizard, "Cancel").doClick(); return null; });
            type("ghidra.framework.GenericRunInfo").getMethod("setProjectsDirPath", String.class)
                .invoke(null, OUTPUT.toString());
            SwingUtilities.invokeLater(() -> button(mainWindow(), "Open Project").doClick());
            OpcodexNativePickerProbe.finish("Open an Opcodex Project", OUTPUT.resolve("dark/open-project.png"), false);
            Thread.sleep(300);
            OpcodexNativePickerProbe.verify(mainWindow(), OUTPUT);
            Object front = type("ghidra.framework.main.AppInfo").getMethod("getFrontEndTool").invoke(null);
            Object pm = front.getClass().getMethod("getProjectManager").invoke(front);
            boolean preview = System.getProperty("opcodex.smoke.project") != null;
            Path projects = preview ? Path.of(System.getProperty("opcodex.smoke.project")) : OUTPUT.resolve("temporary-projects");
            Files.createDirectories(projects);
            Object locator = type("ghidra.framework.model.ProjectLocator")
                .getConstructor(String.class, String.class).newInstance(projects.toString(), preview ? "OpcodexPreview" : "OpcodexSmoke");
            Class<?> projectType = type("ghidra.framework.model.Project");
            Object project;
            if (preview) project = type("ghidra.framework.model.ProjectManager").getMethod("openProject",
                type("ghidra.framework.model.ProjectLocator"), boolean.class, boolean.class).invoke(pm, locator, true, false);
            else project = type("ghidra.framework.model.ProjectManager").getMethod("createProject",
                type("ghidra.framework.model.ProjectLocator"), type("ghidra.framework.client.RepositoryAdapter"), boolean.class)
                .invoke(pm, locator, null, false);
            Collection<Object> files = new ArrayList<>();
            if (preview) {
                Object data = projectType.getMethod("getProjectData").invoke(project);
                Object file = type("ghidra.framework.model.ProjectData").getMethod("getFile", String.class).invoke(data, "/opcodex-demo.exe");
                if (file == null) throw new AssertionError("Missing controlled preview program");
                files.add(file);
            }
            edt(() -> { front.getClass().getMethod("setActiveProject", projectType).invoke(front, project); return null; });
            Object services = projectType.getMethod("getToolServices").invoke(project);
            Object browser = edt(() -> type("ghidra.framework.model.ToolServices").getMethod("launchTool", String.class, Collection.class)
                .invoke(services, "CodeBrowser", files));
            if (browser == null) throw new AssertionError("CodeBrowser failed to launch");
            Window browserWindow = (Window) browser.getClass().getMethod("getToolFrame").invoke(browser);
            if (Boolean.getBoolean("opcodex.smoke.inventoryOnly")) {
                Thread.sleep(1000);
                StringBuilder tables = new StringBuilder();
                edt(() -> { dumpTableState(browserWindow, tables); return null; });
                Files.writeString(OUTPUT.resolve("table-state.txt"), tables.toString());
                capture(browserWindow, "dark/column-preview.png");
                System.exit(0);
            }
            AbstractButton more = edt(() -> named(browserWindow, "More workspace actions"));
            AbstractButton go = edt(() -> named(browserWindow, "Opcodex Go To"));
            if (more == null || go == null) throw new AssertionError("Focused toolbar missing");
            if (edt(() -> named(browserWindow, "Next Matching Byte Values")) != null)
                throw new AssertionError("Secondary navigation still crowds toolbar");
            edt(() -> { more.doClick(0); return null; });
            Thread.sleep(300);
            JPopupMenu overflow = edt(() -> {
                for (MenuElement element : MenuSelectionManager.defaultManager().getSelectedPath())
                    if (element instanceof JPopupMenu popup) return popup;
                return null;
            });
            if (overflow == null || edt(() -> menuItem(overflow, "Show complete toolbar")) == null)
                throw new AssertionError("Complete toolbar access missing");
            capture(browserWindow, "dark/overflow.png");
            JMenuItem symbols = edt(() -> menuItem(overflow, "Symbol Tree"));
            if (symbols == null || !symbols.isEnabled()) throw new AssertionError("Symbol command missing from overflow");
            edt(() -> { symbols.doClick(0); MenuSelectionManager.defaultManager().clearSelectedPath(); return null; });
            Thread.sleep(300);
            edt(() -> { named(browserWindow, "More workspace actions").doClick(0); return null; });
            Thread.sleep(150);
            JPopupMenu layoutMenu = edt(() -> {
                for (MenuElement element : MenuSelectionManager.defaultManager().getSelectedPath())
                    if (element instanceof JPopupMenu popup) return popup;
                return null;
            });
            JMenuItem focused = edt(() -> menuItem(layoutMenu, "Restore focused workspace"));
            if (focused == null) throw new AssertionError("Focused workspace command missing");
            edt(() -> { focused.doClick(0); MenuSelectionManager.defaultManager().clearSelectedPath(); return null; });
            Thread.sleep(600);
            edt(() -> { named(browserWindow, "More workspace actions").doClick(0); return null; });
            Thread.sleep(150);
            JPopupMenu restoredMenu = edt(() -> {
                for (MenuElement element : MenuSelectionManager.defaultManager().getSelectedPath())
                    if (element instanceof JPopupMenu popup) return popup;
                return null;
            });
            JMenuItem previous = edt(() -> menuItem(restoredMenu, "Restore previous workspace"));
            if (previous == null || !previous.isEnabled()) throw new AssertionError("Previous workspace not retained");
            edt(() -> { previous.doClick(0); MenuSelectionManager.defaultManager().clearSelectedPath(); return null; });
            Thread.sleep(500);
            edt(() -> { named(browserWindow, "More workspace actions").doClick(0); return null; });
            Thread.sleep(150);
            JPopupMenu finalMenu = edt(() -> {
                for (MenuElement element : MenuSelectionManager.defaultManager().getSelectedPath())
                    if (element instanceof JPopupMenu popup) return popup;
                return null;
            });
            edt(() -> { menuItem(finalMenu, "Restore focused workspace").doClick(0); MenuSelectionManager.defaultManager().clearSelectedPath(); return null; });
            Thread.sleep(500);
            edt(() -> { named(browserWindow, "Opcodex Go To").doClick(0); return null; });
            Window goDialog = waitFor("Go To");
            capture(goDialog, "dark/go-to.png");
            edt(() -> { AbstractButton cancel = button(goDialog, "Cancel"); if (cancel == null) throw new AssertionError("Go To cancel missing"); cancel.doClick(0); return null; });
            StringBuilder buttons = new StringBuilder();
            edt(() -> { dumpButtons(browserWindow, buttons); return null; });
            Files.writeString(OUTPUT.resolve("visible-buttons.txt"), buttons.toString());
            if (preview) {
                Class<?> programManagerType = type("ghidra.app.services.ProgramManager");
                Object programManager = browser.getClass().getMethod("getService", Class.class).invoke(browser, programManagerType);
                Object program = programManagerType.getMethod("getCurrentProgram").invoke(programManager);
                if (program == null) throw new AssertionError("Preview program did not open");
                Object functions = type("ghidra.program.model.listing.Program").getMethod("getFunctionManager").invoke(program);
                Iterator<?> iterator = (Iterator<?>) type("ghidra.program.model.listing.FunctionManager").getMethod("getFunctions", boolean.class).invoke(functions, true);
                Class<?> functionType = type("ghidra.program.model.listing.Function");
                Object entryPoint = null;
                while (iterator.hasNext()) {
                    Object function = iterator.next();
                    if ("mix_bytes".equals(functionType.getMethod("getName").invoke(function))) {
                        entryPoint = functionType.getMethod("getEntryPoint").invoke(function); break;
                    }
                }
                if (entryPoint == null) throw new AssertionError("Controlled function not discovered");
                Class<?> goToType = type("ghidra.app.services.GoToService");
                Object goTo = browser.getClass().getMethod("getService", Class.class).invoke(browser, goToType);
                final Object address = type("ghidra.program.model.address.Address").getMethod("add", long.class).invoke(entryPoint, 0x30L);
                edt(() -> goToType.getMethod("goTo", type("ghidra.program.model.address.Address")).invoke(goTo, address));
                Thread.sleep(2500);
                edt(() -> { expandFunctions(browserWindow); return null; });
                Thread.sleep(400);
            }

            Class<?> managerClass = type("generic.theme.ThemeManager");
            Object manager = managerClass.getMethod("getInstance").invoke(null);
            for (String theme : new String[]{"light", "dark"}) {
                Object selected = type("generic.theme.builtin.Opcodex" + (theme.equals("dark") ? "Dark" : "Light") + "Theme")
                    .getConstructor().newInstance();
                edt(() -> { managerClass.getMethod("setTheme", type("generic.theme.GTheme")).invoke(manager, selected); return null; });
                verifySurfaceTokens();
                TreeSet<String> ids = new TreeSet<>();
                for (String kind : new String[]{"Colors", "Fonts", "Icons"}) {
                    for (Object value : (Collection<?>) selected.getClass().getMethod("get" + kind).invoke(selected))
                        ids.add(kind + ":" + value.getClass().getMethod("getId").invoke(value));
                }
                Files.write(OUTPUT.resolve(theme + "-theme-ids.txt"), ids);
                StringBuilder fontEvidence = new StringBuilder();
                for (String id : new String[]{"font.monospaced", "font.listing.base", "font.decompiler", "system.font.control", "laf.font.Label.font"})
                    fontEvidence.append(id).append("=").append(managerClass.getMethod("getFont", String.class).invoke(manager, id)).append("\n");
                Files.writeString(OUTPUT.resolve(theme + "-fonts.txt"), fontEvidence.toString());
                capture(main, theme + "/project.png");
                capture(browserWindow, theme + "/codebrowser.png");
                Object progressDialog = edt(() -> type("ghidra.util.task.TaskDialog")
                    .getConstructor(String.class, boolean.class, boolean.class, boolean.class)
                    .newInstance("Opening sample program", true, false, false));
                edt(() -> { progressDialog.getClass().getMethod("setMessage", String.class).invoke(progressDialog, "Reading program data…"); return null; });
                SwingUtilities.invokeLater(() -> {
                    try { type("docking.DockingWindowManager").getMethod("showDialog", type("docking.DialogComponentProvider")).invoke(null, progressDialog); }
                    catch (Exception error) { throw new RuntimeException(error); }
                });
                Window progressWindow = waitFor("Opening sample program");
                if (!edt(() -> hasCleanProgress(progressWindow))) throw new AssertionError("Legacy task animation still present");
                capture(progressWindow, theme + "/task-progress.png");
                progressDialog.getClass().getMethod("taskProcessed").invoke(progressDialog);
                Thread.sleep(200);

                Object about = edt(() -> type("docking.framework.AboutDialog").getConstructor().newInstance());
                SwingUtilities.invokeLater(() -> {
                    try {
                        type("docking.DockingWindowManager").getMethod("showDialog", type("docking.DialogComponentProvider"))
                            .invoke(null, about);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });
                Window aboutWindow = waitFor("About Opcodex");
                capture(aboutWindow, theme + "/about.png");
                edt(() -> { about.getClass().getMethod("close").invoke(about); return null; });
            }
            Files.writeString(OUTPUT.resolve("result.json"), "{\"startup\":true,\"isolatedProfile\":true,\"agreement\":" + accepted + ",\"codeBrowser\":true,\"welcome\":true,\"focusedToolbar\":true,\"workspaceResetAndUndo\":true,\"overflowCommand\":true,\"goTo\":true,\"themeDiscovery\":true,\"themes\":[\"Opcodex Light\",\"Opcodex Dark\"],\"about\":true,\"cleanTaskProgress\":true}\n");
            Files.writeString(OUTPUT.resolve("display.txt"), "Java=" + System.getProperty("java.version") + "\nOS=" + System.getProperty("os.name") + "\nNativeLauncher=" + System.getProperty("jpackage.app-path") + "\nScale=" + GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform().getScaleX() + "\n");
            System.out.println("OPCODEX_SMOKE_PASS");
            System.exit(0);
        } catch (Throwable e) {
            e.printStackTrace();
            try { Files.writeString(OUTPUT.resolve("failure.txt"), e.toString()); } catch (Exception ignored) {}
            System.exit(2);
        }
    }
    public static void premain(String args, Instrumentation instrumentation) {
        Thread worker = new Thread(OpcodexSmokeAgent::run, "Opcodex-smoke");
        worker.setDaemon(true); worker.start();
    }
}

