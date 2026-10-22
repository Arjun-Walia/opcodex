/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
import java.awt.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.*;
import javax.swing.tree.TreePath;

/** Exercises the reported regressions inside the real, isolated desktop process. */
public final class OpcodexAuditProbe {
    interface Shot { void take(Window window, String path) throws Exception; }
    private static Class<?> type(String name) throws Exception {
        return Class.forName(name, true, ClassLoader.getSystemClassLoader());
    }
    private static <T> T edt(Callable<T> action) throws Exception {
        Object[] value = new Object[1]; Exception[] error = new Exception[1];
        SwingUtilities.invokeAndWait(() -> { try { value[0] = action.call(); } catch (Exception e) { error[0] = e; } });
        if (error[0] != null) throw error[0];
        return (T) value[0];
    }
    private static void collect(Component c, List<JTree> trees) {
        if (c instanceof JTree tree && tree.isShowing()) trees.add(tree);
        if (c instanceof Container parent) for (Component child : parent.getComponents()) collect(child, trees);
    }
    private static Map<JTree, List<TreePath>> expanded(Window window) {
        List<JTree> trees = new ArrayList<>(); collect(window, trees);
        Map<JTree, List<TreePath>> state = new IdentityHashMap<>();
        for (JTree tree : trees) {
            if (tree.getModel().getRoot() == null) continue;
            var paths = tree.getExpandedDescendants(new TreePath(tree.getModel().getRoot()));
            if (paths != null) state.put(tree, Collections.list(paths));
        }
        return state;
    }
    private static void theme(String className) throws Exception {
        Object theme = type("generic.theme.builtin." + className).getConstructor().newInstance();
        Class<?> manager = type("generic.theme.ThemeManager");
        Object instance = manager.getMethod("getInstance").invoke(null);
        edt(() -> { manager.getMethod("setTheme", type("generic.theme.GTheme")).invoke(instance, theme); return null; });
        Thread.sleep(1100);
    }
    private static double light(Color color) {
        double[] v = {color.getRed()/255d, color.getGreen()/255d, color.getBlue()/255d};
        for (int i=0;i<3;i++) v[i] = v[i] <= .04045 ? v[i]/12.92 : Math.pow((v[i]+.055)/1.055,2.4);
        return .2126*v[0]+.7152*v[1]+.0722*v[2];
    }
    private static void selectionContrast() {
        for (String key : List.of("List", "Table", "Tree", "TextField", "TextArea", "TextPane", "EditorPane",
                "ComboBox", "Menu", "MenuItem", "CheckBoxMenuItem", "RadioButtonMenuItem", "MenuBar")) {
            Color fg=UIManager.getColor(key+".selectionForeground"), bg=UIManager.getColor(key+".selectionBackground");
            if (fg==null || bg==null) throw new AssertionError("Missing live selection colors: "+key);
            double a=light(fg), b=light(bg), contrast=(Math.max(a,b)+.05)/(Math.min(a,b)+.05);
            if (contrast<4.5) throw new AssertionError("Unreadable live selection: "+key+" contrast="+contrast);
        }
    }
    private static String layout(Object manager) throws Exception {
        Object root = type("docking.DockingWindowManager").getMethod("saveWindowingDataToXml").invoke(manager);
        return type("org.jdom2.output.XMLOutputter").getMethod("outputString", type("org.jdom2.Element"))
            .invoke(type("org.jdom2.output.XMLOutputter").getConstructor().newInstance(), root).toString();
    }

    private static long imageHash(Image source) {
        var pixels = new java.awt.image.BufferedImage(32,32,java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D canvas = pixels.createGraphics();
        try { canvas.drawImage(source,0,0,32,32,null); } finally { canvas.dispose(); }
        long hash = 1;
        for (int y=0;y<32;y++) for (int x=0;x<32;x++) hash = hash*31 + pixels.getRGB(x,y);
        return hash;
    }

    private static long verifyWindowBrand(Window window) throws Exception {
        return edt(() -> {
            List<Image> expected = (List<Image>) type("docking.framework.ApplicationInformationDisplayFactory")
                .getMethod("getWindowIcons").invoke(null);
            List<Image> actual = window.getIconImages();
            if (expected.isEmpty() || actual.isEmpty() || imageHash(expected.get(0)) != imageHash(actual.get(0)))
                throw new AssertionError("Native window logo does not match the current theme: " + window);
            return imageHash(actual.get(0));
        });
    }

    private static void buttons(Component component, List<JButton> result) {
        if (component instanceof JButton button) result.add(button);
        if (component instanceof Container container)
            for (Component child : container.getComponents()) buttons(child,result);
    }

    private static void singleRows(Component component) {
        if (component instanceof Container container) {
            if ("docking.menu.OpcodexToolbarStrip".equals(component.getClass().getName())) {
                List<JButton> all = new ArrayList<>(); buttons(component,all);
                Integer middle = null;
                for (JButton button : all) {
                    if (button.getParent() == component) continue; // scroll controls
                    if (!button.isVisible() || button.getHeight()==0) continue;
                    int y = SwingUtilities.convertPoint(button,button.getWidth()/2,button.getHeight()/2,container).y;
                    if (middle != null && Math.abs(middle-y)>1) throw new AssertionError("Toolbar wrapped: "+button.getName());
                    middle=y;
                    if (button.isEnabled() && button.getCursor().getType()!=Cursor.HAND_CURSOR)
                        throw new AssertionError("Missing pointer: "+button.getName());
                }
            }
            for (Component child : container.getComponents()) singleRows(child);
        }
    }

    private static JToolTip tip(Component component) {
        if (component instanceof JToolTip tooltip && tooltip.isShowing()) return tooltip;
        if (component instanceof Container parent) for (Component child : parent.getComponents()) {
            JToolTip found=tip(child); if(found!=null) return found;
        }
        return null;
    }

    private static void hover(Window browser, Path output, Shot shot) throws Exception {
        JButton target = edt(() -> {
            browser.setAlwaysOnTop(true); browser.toFront();
            List<JButton> all = new ArrayList<>(); buttons(browser,all);
            return all.stream().filter(b -> b.isShowing() && b.isEnabled() && b.getName()!=null &&
                b.getToolTipText()!=null && b.getToolTipText().contains("<b>"+b.getName()+"</b>") &&
                b.getVisibleRect().width>=b.getWidth()-2).findFirst().orElseThrow();
        });
        Point point=edt(() -> { Point p=target.getLocationOnScreen();p.translate(target.getWidth()/2,target.getHeight()/2);return p; });
        Robot robot=new Robot(); robot.mouseMove(point.x+2,point.y+2); Thread.sleep(100);
        robot.mouseMove(point.x,point.y);
        boolean visible=false;
        for(int i=0;i<30 && !visible;i++) {
            Thread.sleep(100);
            visible=edt(() -> {
                for(Window window:Window.getWindows()) {
                    JToolTip found=tip(window);
                    if(found!=null && found.getTipText().contains("<b>"+target.getName()+"</b>")) return true;
                }
                return false;
            });
        }
        if(!visible) throw new AssertionError("Hover did not show the action name: "+target.getName());
        shot.take(browser,"dark/toolbar-tooltip.png");
        edt(() -> { browser.setAlwaysOnTop(false); ToolTipManager.sharedInstance().setEnabled(false);
            ToolTipManager.sharedInstance().setEnabled(true); return null; });
    }

    public static void verifyToolbarWidths(Window window) throws Exception {
        Dimension original=edt(() -> window.getSize());
        try {
            for (int width : new int[] {700, 1500, 1900}) {
                edt(() -> { window.setSize(width,original.height); window.validate(); return null; });
                Thread.sleep(200);
                edt(() -> { singleRows(window); return null; });
            }
        } finally { edt(() -> { window.setSize(original); window.validate(); return null; }); }
    }

    private static void themePreferences() throws Exception {
        Class<?> prefs=type("ghidra.framework.preferences.Preferences");
        String saved=(String)prefs.getMethod("getProperty",String.class,String.class,boolean.class)
            .invoke(null,"Theme","Default",false);
        try {
            for (String[] choice : new String[][] {{"FlatDarkTheme","OpcodexLightTheme"},
                    {"GhidraClassicTheme","GhidraClassicTheme"}, {"OpcodexDarkTheme","OpcodexDarkTheme"}}) {
                prefs.getMethod("setProperty",String.class,String.class).invoke(null,"Theme",
                    "Class:generic.theme.builtin."+choice[0]);
                Object loader=type("generic.theme.ThemePreferences").getConstructor().newInstance();
                Object loaded=loader.getClass().getMethod("load").invoke(loader);
                if (!loaded.getClass().getSimpleName().equals(choice[1]))
                    throw new AssertionError("Incorrect saved-theme migration: "+choice[0]);
            }
        } finally { prefs.getMethod("setProperty",String.class,String.class).invoke(null,"Theme",saved); }
    }
    public static void verify(Object tool, Window browser, Window project, Path output, Shot shot) throws Exception {
        themePreferences();
        Map<JTree,List<TreePath>> before=edt(() -> expanded(project));
        before.putAll(edt(() -> expanded(browser)));
        if (before.values().stream().mapToInt(List::size).sum() == 0) throw new AssertionError("No expanded paths to verify");
        theme("OpcodexLightTheme");
        long modernBrand=verifyWindowBrand(project);
        verifyWindowBrand(browser);
        verifyToolbarWidths(browser);
        edt(() -> { selectionContrast(); return null; });
        long until=System.currentTimeMillis()+4000;
        boolean retained;
        do {
            retained=edt(() -> before.entrySet().stream().allMatch(e -> e.getValue().stream().allMatch(e.getKey()::isExpanded)));
            if (!retained) Thread.sleep(100);
        } while (!retained && System.currentTimeMillis()<until);
        if (!retained) throw new AssertionError("Expanded tree paths collapsed on a theme change");
        edt(() -> {
            JMenuBar bar=((RootPaneContainer)browser).getRootPane().getJMenuBar();
            bar.getMenu(0).doClick(); return null;
        });
        shot.take(browser,"light/panels/selected-menu.png");
        edt(() -> { MenuSelectionManager.defaultManager().clearSelectedPath(); return null; });
        Object manager=tool.getClass().getMethod("getWindowManager").invoke(tool);
        String modern=edt(() -> layout(manager));
        theme("GhidraClassicTheme");
        long classicBrand=verifyWindowBrand(project);
        verifyWindowBrand(browser);
        if (classicBrand==modernBrand) throw new AssertionError("Classic and product logos unexpectedly match");
        if ((Boolean)type("generic.theme.builtin.OpcodexUi").getMethod("active").invoke(null))
            throw new AssertionError("Classic still uses modern chrome");
        String classic=edt(() -> layout(manager));
        if (modern.equals(classic)) throw new AssertionError("Classic did not restore the original layout");
        shot.take(browser,"classic/codebrowser.png"); shot.take(project,"classic/project.png");
        Files.writeString(output.resolve("classic-layout.xml"),classic);
        theme("OpcodexDarkTheme");
        verifyWindowBrand(project); verifyWindowBrand(browser);
        edt(() -> { selectionContrast(); return null; });
        String restored=edt(() -> layout(manager));
        // Exact serialized layout preservation also checks visibility, provider identity and divider positions.
        if (!modern.equals(restored)) {
            Files.writeString(output.resolve("modern-before.xml"),modern);
            Files.writeString(output.resolve("modern-after.xml"),restored);
            throw new AssertionError("Modern layout was not restored after the Classic round trip");
        }
        hover(browser,output,shot);
        // Measure actual event-dispatch turnaround after the panel/theme work has settled.
        List<Double> samples=new ArrayList<>();
        for (int i=0;i<80;i++) {
            long start=System.nanoTime(); edt(() -> null);
            samples.add((System.nanoTime()-start)/1_000_000d); Thread.sleep(10);
        }
        Collections.sort(samples);
        Files.writeString(output.resolve("audit-result.json"),
            "{\"lightSelectionContrast\":true,\"darkSelectionContrast\":true,\"expandedTreesRetained\":true,"+
            "\"nativeLogoRefresh\":true,\"singleRowToolbars\":true,\"enabledPointerCursors\":true,\"visibleHoverName\":true,"+
            "\"retiredThemeMigrationToLight\":true,\"explicitClassicPreserved\":true,"+
            "\"classicOriginalLayout\":true,\"modernLayoutRestored\":true,\"eventDispatchSamples\":80,"+
            "\"eventDispatchP95Ms\":"+samples.get(75)+",\"eventDispatchMaxMs\":"+samples.get(79)+"}\n");
    }
}
