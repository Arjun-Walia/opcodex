/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.*;
import javax.swing.tree.TreePath;

/** Exercises the reported regressions inside the real, isolated desktop process. */
public final class OpcodexAuditProbe {
    private static Object testedHelpLocation;
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
        for (String key:List.of("MenuItem","CheckBoxMenuItem","RadioButtonMenuItem")) {
            Color fg=UIManager.getColor(key+".acceleratorSelectionForeground"),bg=UIManager.getColor(key+".selectionBackground");
            if (fg==null || bg==null || (Math.max(light(fg),light(bg))+.05)/(Math.min(light(fg),light(bg))+.05)<4.5)
                throw new AssertionError("Unreadable selected shortcut: "+key);
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
                if (all.stream().noneMatch(button -> button.getParent() != component &&
                        !"More workspace actions".equals(button.getName()) &&
                        !"More panel actions".equals(button.getName()))) {
                    throw new AssertionError("Empty toolbar reserved a row");
                }
                Integer middle = null;
                for (JButton button : all) {
                    if (button.getParent() == component) continue; // scroll controls
                    if (!button.isVisible() || button.getHeight()==0) continue;
                    int y = SwingUtilities.convertPoint(button,button.getWidth()/2,button.getHeight()/2,container).y;
                    if (middle != null && Math.abs(middle-y)>1) throw new AssertionError("Toolbar wrapped: "+button.getName());
                    middle=y;
                    if (button.isEnabled() && (!button.isCursorSet() || button.getCursor().getType()!=Cursor.HAND_CURSOR))
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
            JButton command=all.stream().filter(b -> "Script Manager".equals(b.getName()) && b.isShowing() && b.isEnabled() &&
                b.getToolTipText()!=null && b.getToolTipText().contains("<b>"+b.getName()+"</b>") &&
                b.getVisibleRect().width>=b.getWidth()-2).findFirst().orElseThrow();
            command.scrollRectToVisible(new Rectangle(0,0,command.getWidth(),command.getHeight()));
            ToolTipManager.sharedInstance().setEnabled(false);ToolTipManager.sharedInstance().setEnabled(true);
            return command;
        });
        Point point=edt(() -> { Point p=target.getLocationOnScreen();p.translate(target.getWidth()/2,target.getHeight()/2);return p; });
        Robot robot=new Robot(); robot.mouseMove(point.x,Math.max(0,point.y-60)); Thread.sleep(200);
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
        if(!visible) {
            Files.writeString(output.resolve("hover-diagnostic.txt"),"Target="+point+"\nPointer="+MouseInfo.getPointerInfo().getLocation()+"\nScale="+browser.getGraphicsConfiguration().getDefaultTransform()+"\n");
            shot.take(browser,"hover-failure.png");
            throw new AssertionError("Hover did not show the action name: "+target.getName());
        }
        shot.take(browser,"dark/toolbar-tooltip.png");
        edt(() -> { browser.setAlwaysOnTop(false); ToolTipManager.sharedInstance().setEnabled(false);
            ToolTipManager.sharedInstance().setEnabled(true); return null; });
    }

    public static void verifyPanelToolbarRows(Window window) throws Exception {
        edt(() -> { singleRows(window); return null; });
    }

    public static void verifyToolbarWidths(Window window) throws Exception {
        Dimension original=edt(() -> window.getSize());
        try {
            for (int width : new int[] {700, 1500, 1900}) {
                edt(() -> { window.setSize(width,original.height); window.validate(); return null; });
                Thread.sleep(200);
                edt(() -> {
                    singleRows(window);
                    Component search = named(window,"Find a command");
                    if (search != null && (!(search instanceof JTextField field) || !field.isShowing() || field.getVisibleRect().width<120))
                        throw new AssertionError("Command search disappeared at width "+width);
                    return null;
                });
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

    private static Component named(Component component, String name) {
        if (name.equals(component.getName())) return component;
        if (component instanceof Container parent) for (Component child : parent.getComponents()) {
            Component found = named(child,name); if (found != null) return found;
        }
        return null;
    }

    private static void key(Robot robot, int code) {
        robot.keyPress(code);
        robot.keyRelease(code);
        robot.waitForIdle();
    }

    private static void typeIn(Window window, JTextField field, String value) throws Exception {
        edt(() -> { window.toFront(); return null; });
        Robot robot = new Robot();
        robot.setAutoDelay(35);
        robot.waitForIdle();
        Point center = edt(() -> {
            Point p = field.getLocationOnScreen();
            p.translate(field.getWidth() / 2, field.getHeight() / 2);
            return p;
        });
        robot.mouseMove(center.x, center.y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.waitForIdle();
        for (int attempt = 0; attempt < 20 && !edt(field::isFocusOwner); attempt++) {
            Thread.sleep(50);
        }
        for (char c : value.toCharArray()) {
            assertFocus(field);
            key(robot, KeyEvent.getExtendedKeyCodeForChar(c));
        }
        assertFocus(field);
    }

    private static void assertFocus(JTextField field) throws Exception {
        edt(() -> {
            if (!field.isFocusOwner()) {
                Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                throw new AssertionError(field.getName() + " lost focus to " + owner);
            }
            return null;
        });
    }

    public static void verifyProjectDirectory(Window wizard) throws Exception {
        JTextField directory = edt(() -> (JTextField) named(wizard, "Project Directory"));
        JTextField name = edt(() -> (JTextField) named(wizard, "Project Name"));
        String initialName = edt(name::getText);
        edt(() -> { directory.setText(""); return null; });
        typeIn(wizard, directory, "focusprobe");
        if (!"focusprobe".equals(edt(directory::getText)) || !initialName.equals(edt(name::getText))) {
            throw new AssertionError("Project directory keystrokes were redirected");
        }
    }

    public static void verifyFrontEndSearch(Window window) throws Exception {
        JTextField search = edt(() -> (JTextField) named(window, "Find a command"));
        typeIn(window, search, "help");
        if (!"help".equals(edt(search::getText))) {
            throw new AssertionError("Project window search did not receive the typed query");
        }
        edt(() -> { search.setText(""); return null; });
    }

    private static void ribbonSearch(Object tool, Window browser, Path output, Shot shot) throws Exception {
        JTextField search=edt(() -> (JTextField)named(browser,"Find a command"));
        if(search==null)throw new AssertionError("No visible command search");
        Object manager=tool.getClass().getMethod("getWindowManager").invoke(tool);
        Object graph=type("docking.DockingWindowManager").getMethod("getComponentProvider",String.class).invoke(manager,"Function Graph");
        boolean original=(Boolean)edt(() -> type("docking.ComponentProvider").getMethod("isVisible").invoke(graph));
        typeIn(browser, search, "function graph");
        if (!"function graph".equals(edt(search::getText))) {
            throw new AssertionError("Command search did not receive the typed query");
        }
        Robot robot = new Robot();
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
        Window dialog=null;
        for(int i=0;i<100 && dialog==null;i++) {
            dialog=edt(() -> Arrays.stream(Window.getWindows()).filter(w -> w instanceof Dialog d && d.isShowing() && d.getTitle().startsWith("Find a command")).findFirst().orElse(null));
            if(dialog==null)Thread.sleep(100);
        }
        if(dialog==null)throw new AssertionError("Command search did not open");
        Window finder=dialog;
        Object results=edt(() -> named(finder,"Opcodex command results"));
        Object model=results.getClass().getMethod("getModel").invoke(results);
        int size=(Integer)model.getClass().getMethod("getSize").invoke(model);
        Object chosen=null;
        for(int i=0;i<size;i++) {
            Object entry=model.getClass().getMethod("getElementAt",int.class).invoke(model,i);
            Object action=entry.getClass().getMethod("value").invoke(entry);
            if(type("docking.action.DockingActionIf").getMethod("getName").invoke(action).equals("Function Graph") &&
                    type("docking.action.DockingActionIf").getMethod("getOwner").invoke(action).equals("DockingWindows"))chosen=action;
        }
        if(chosen==null)throw new AssertionError("Search did not find the original Function Graph command");
        Object command=chosen;
        edt(() -> { results.getClass().getMethod("setSelectedItem",Object.class).invoke(results,command); return null; });
        shot.take(finder,"light/command-search.png");
        Object provider=finder.getClass().getMethod("getDialogComponent").invoke(finder);
        java.lang.reflect.Method ok=type("docking.actions.dialog.ActionChooserDialog").getDeclaredMethod("okCallback");ok.setAccessible(true);
        edt(() -> { ok.invoke(provider); return null; });
        boolean visible=false;
        for(int i=0;i<50 && !visible;i++) {
            visible=(Boolean)edt(() -> type("docking.ComponentProvider").getMethod("isVisible").invoke(graph));
            if(!visible)Thread.sleep(100);
        }
        if(!visible)throw new AssertionError("Search did not execute the original command");
        edt(() -> { type("docking.ComponentProvider").getMethod("setVisible",boolean.class).invoke(graph,original); search.setText(""); return null; });
        Files.writeString(output.resolve("ribbon-result.json"),"{\"visibleSearch\":true,\"originalCommandFound\":true,\"originalCommandExecuted\":true}\n");
    }
    public static void verifyDebugger(Object tool, Window window, Path output, Shot shot) throws Exception {
        Object manager=tool.getClass().getMethod("getWindowManager").invoke(tool);
        String modern=edt(() -> layout(manager));
        if(edt(() -> named(window,"Opcodex debugger workflow"))==null)throw new AssertionError("Missing debugger orientation");
        Component empty=edt(() -> named(window,"Opcodex debugger empty session"));
        if(empty==null || !edt(empty::isShowing))throw new AssertionError("No-trace debugger state was not explained");
        for(String name:List.of("Connections","Registers")) {
            Object provider=type("docking.DockingWindowManager").getMethod("getComponentProvider",String.class).invoke(manager,name);
            JComponent component=(JComponent)edt(() -> type("docking.ComponentProvider").getMethod("getComponent").invoke(provider));
            if(!edt(component::isShowing))throw new AssertionError("Debugger default did not expose "+name);
        }
        for(String name:List.of("Connections","Threads","Registers","Stack","Memory","Breakpoints")) {
            Component shortcut=edt(() -> named(window,"Workspace: "+name));
            if(shortcut==null)throw new AssertionError("Missing debugger shortcut: "+name);
        }
        for(String name:List.of("2  Choose a thread","3  Inspect state")) {
            AbstractButton step=edt(() -> (AbstractButton)named(window,"Debugger step: "+name));
            edt(() -> { step.doClick(0); return null; });
        }
        shot.take(window,"dark/debugger-workspace.png");
        String afterFocus=edt(() -> layout(manager));
        verifyTraceEmptyState(tool,window,empty);
        theme("GhidraClassicTheme");
        Component classic=edt(() -> named(window,"Opcodex debugger workflow"));
        if(classic!=null && edt(classic::isShowing))throw new AssertionError("Classic retained modern debugger guide");
        if(edt(empty::isShowing))throw new AssertionError("Classic retained the modern no-trace overlay");
        shot.take(window,"classic/debugger-workspace.png");
        theme("OpcodexDarkTheme");
        String restored=edt(() -> layout(manager));
        if(!sameDebuggerLayout(afterFocus,restored)) {
            Files.writeString(output.resolve("debugger-modern-before.xml"),afterFocus);
            Files.writeString(output.resolve("debugger-modern-after.xml"),restored);
            throw new AssertionError("Debugger modern layout was not restored");
        }
        Files.writeString(output.resolve("debugger-workspace-result.json"),"{\"workflowGuide\":true,\"debuggerShortcuts\":true,\"classicSeparate\":true,\"modernRestored\":true,\"traceEmptyStateTransitions\":true}\n");
    }
    private static void verifyTraceEmptyState(Object tool,Window window,Component empty) throws Exception {
        Object languages=type("ghidra.program.util.DefaultLanguageService").getMethod("getLanguageService").invoke(null);
        Object languageId=type("ghidra.program.model.lang.LanguageID").getConstructor(String.class).newInstance("x86:LE:64:default");
        Object language=type("ghidra.program.model.lang.LanguageService").getMethod("getLanguage",type("ghidra.program.model.lang.LanguageID")).invoke(languages,languageId);
        Object compiler=type("ghidra.program.model.lang.Language").getMethod("getDefaultCompilerSpec").invoke(language);
        Object consumer=new Object();
        Object trace=type("ghidra.trace.database.DBTrace").getConstructor(String.class,type("ghidra.program.model.lang.CompilerSpec"),Object.class).newInstance("Opcodex UI trace",compiler,consumer);
        Class<?> serviceType=type("ghidra.app.services.DebuggerTraceManagerService"),traceType=type("ghidra.trace.model.Trace");
        Object service=tool.getClass().getMethod("getService",Class.class).invoke(tool,serviceType);
        try {
            edt(() -> {serviceType.getMethod("openTrace",traceType).invoke(service,trace);serviceType.getMethod("activateTrace",traceType).invoke(service,trace);return null;});
            for(int i=0;i<60 && edt(empty::isShowing);i++)Thread.sleep(100);
            if(edt(empty::isShowing))throw new AssertionError("No-trace overlay obscured an active trace");
        } finally {
            edt(() -> {serviceType.getMethod("closeTraceNoConfirm",traceType).invoke(service,trace);return null;});
            type("ghidra.framework.model.DomainObject").getMethod("release",Object.class).invoke(trace,consumer);
        }
        for(int i=0;i<60 && !edt(empty::isShowing);i++)Thread.sleep(100);
        if(!edt(empty::isShowing))throw new AssertionError("No-trace state did not return after closing the owned trace");
    }
    private static boolean sameDebuggerLayout(String before,String after) throws Exception {
        var factory=javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
        var builder=factory.newDocumentBuilder();
        var a=builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(before))).getDocumentElement();
        var b=builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(after))).getDocumentElement();
        return sameLayoutNode(a,b);
    }
    private static boolean sameLayoutNode(org.w3c.dom.Element a,org.w3c.dom.Element b) {
        if(!a.getTagName().equals(b.getTagName()) || a.getAttributes().getLength()!=b.getAttributes().getLength())return false;
        for(int i=0;i<a.getAttributes().getLength();i++) {
            var attribute=a.getAttributes().item(i);String key=attribute.getNodeName(),x=attribute.getNodeValue(),y=b.getAttribute(key);
            if(x.equals(y))continue;
            // Native layout may round saved divider fractions (per 1000) and pane sizes by 1–2 units.
            if(!List.of("WIDTH","HEIGHT","DIVIDER_LOCATION").contains(key) || Math.abs(Integer.parseInt(x)-Integer.parseInt(y))>2)return false;
        }
        var left=a.getChildNodes();var right=b.getChildNodes();if(left.getLength()!=right.getLength())return false;
        for(int i=0;i<left.getLength();i++)if(left.item(i) instanceof org.w3c.dom.Element x) {
            if(!(right.item(i) instanceof org.w3c.dom.Element y) || !sameLayoutNode(x,y))return false;
        }
        return true;
    }
    private static Window waitWindow(String title) throws Exception {
        for (int i=0;i<100;i++) {
            Window found=edt(() -> Arrays.stream(Window.getWindows()).filter(w -> w.isShowing() &&
                ((w instanceof Dialog d && d.getTitle().equals(title)) ||
                 (w instanceof Frame f && f.getTitle().equals(title)))).findFirst().orElse(null));
            if (found!=null) return found;
            Thread.sleep(100);
        }
        throw new AssertionError("Missing dialog: "+title);
    }
    private static String text(Component component) {
        StringBuilder result=new StringBuilder();
        if (component instanceof javax.swing.text.JTextComponent field) result.append(field.getText());
        if (component instanceof Container parent) for (Component child : parent.getComponents()) result.append(text(child));
        return result.toString();
    }
    private static void pointerProperties(Component component) throws Exception {
        if (component instanceof AbstractButton button && button.isShowing() &&
            (type("docking.widgets.EmptyBorderButton").isInstance(button) ||
             type("docking.widgets.button.GButton").isInstance(button))) {
            int expected=button.isEnabled()?Cursor.HAND_CURSOR:Cursor.DEFAULT_CURSOR;
            if (!button.isCursorSet() || button.getCursor().getType()!=expected)
                throw new AssertionError("Native cursor property missing: "+button.getName());
        }
        if (component instanceof Container parent) for (Component child : parent.getComponents()) pointerProperties(child);
    }
    private static int helpVectors(Component component) throws Exception {
        int count=0;
        if(component instanceof AbstractButton button && button.getParent() instanceof JToolBar && button.getIcon()!=null) {
            Icon icon=button.getIcon();
            // The navigation aid is a live state-dependent wrapper, not a bitmap.
            if(icon.getClass().getName().equals("docking.help.NavigationAidToggleAction$SelfPaintingIcon")) {
                var current=icon.getClass().getDeclaredMethod("getIcon");current.setAccessible(true);
                icon=(Icon)current.invoke(icon);
            }
            if(!(Boolean)type("generic.theme.builtin.OpcodexUi").getMethod("isVector",Icon.class).invoke(null,icon))
                throw new AssertionError("Legacy Help toolbar icon: "+button.getToolTipText()+" ("+button.getIcon().getClass().getName()+")");
            if(button.isEnabled() && (!button.isCursorSet() || button.getCursor().getType()!=Cursor.HAND_CURSOR))
                throw new AssertionError("Missing Help pointer: "+button.getToolTipText());
            count++;
        }
        if(component instanceof Container parent)for(Component child:parent.getComponents())count+=helpVectors(child);
        return count;
    }
    private static JEditorPane htmlPane(Component component) {
        if(component instanceof JEditorPane editor && editor.isShowing())return editor;
        if(component instanceof Container parent)for(Component child:parent.getComponents()) {
            JEditorPane found=htmlPane(child);if(found!=null)return found;
        }
        return null;
    }
    private static void followup(Object tool, Window browser, Window project, Path output, Shot shot) throws Exception {
        edt(() -> { pointerProperties(browser); pointerProperties(project); return null; });
        Object manager=tool.getClass().getMethod("getWindowManager").invoke(tool);
        Object properties=type("ghidra.framework.Application").getMethod("getApplicationLayout").invoke(null);
        properties=properties.getClass().getMethod("getApplicationProperties").invoke(properties);
        String revision=(String)type("ghidra.framework.ApplicationProperties").getMethod("getSourceRevision").invoke(properties);
        if (!revision.matches("[0-9a-f]{40}")) throw new AssertionError("Packaged source identity missing: "+revision);
        Object front=type("ghidra.framework.main.AppInfo").getMethod("getFrontEndTool").invoke(null);
        Object infoManager=front.getClass().getMethod("getWindowManager").invoke(front);
        Collection<?> actions=(Collection<?>)front.getClass().getMethod("getAllActions").invoke(front);
        Object runtime=null;
        for (Object action:actions) if ("Runtime Information".equals(type("docking.action.DockingActionIf").getMethod("getName").invoke(action))) { runtime=action; break; }
        if (runtime==null) throw new AssertionError("Runtime Information action missing");
        final Object runtimeAction=runtime;
        edt(() -> { type("docking.DockingActionPerformer").getMethod("perform",type("docking.action.DockingActionIf"),
            java.awt.event.ActionEvent.class,type("docking.DockingWindowManager")).invoke(null,runtimeAction,
            new java.awt.event.ActionEvent(project,1001,"Runtime Information"),infoManager);return null; });
        Window version=waitWindow("Runtime Information");
        String versionText=edt(() -> text(version));
        if (!versionText.contains("Source Revision: "+revision) || !versionText.contains("Opcodex Version: 1.0.0-alpha.2") ||
            !versionText.contains("Engine Compatibility: 12.1.4")) throw new AssertionError("Incorrect runtime identity: "+versionText);
        shot.take(version,"light/runtime-information.png");
        edt(() -> { version.dispatchEvent(new java.awt.event.WindowEvent(version,java.awt.event.WindowEvent.WINDOW_CLOSING));return null; });

        Object error=edt(() -> type("docking.ErrLogDialog").getMethod("createExceptionDialog",String.class,String.class,Throwable.class)
            .invoke(null,"Opcodex verification","Controlled error-report verification",new IllegalArgumentException("Controlled verification")));
        SwingUtilities.invokeLater(() -> { try { type("docking.DockingWindowManager").getMethod("showDialog",Component.class,
            type("docking.DialogComponentProvider")).invoke(null,browser,error); } catch (Exception e) { throw new RuntimeException(e); } });
        Window errorWindow=waitWindow("Opcodex verification");
        edt(() -> { List<JButton> all=new ArrayList<>();buttons(errorWindow,all);all.stream().filter(b -> "Details >>>".equals(b.getText())).findFirst().ifPresent(b -> b.doClick(0));return null; });
        String report=edt(() -> text(errorWindow));
        if (!report.contains("Opcodex Version: 1.0.0-alpha.2") || !report.contains("Engine Compatibility: 12.1.4") ||
            !report.contains("Source Revision: "+revision)) throw new AssertionError("Incorrect error-report identity: "+report);
        shot.take(errorWindow,"light/error-details.png");
        edt(() -> { type("docking.DialogComponentProvider").getMethod("close").invoke(error);return null; });

        Component rail=edt(() -> named(browser,"Opcodex workspace navigation"));
        if (rail==null || !rail.isShowing()) throw new AssertionError("Workspace navigation is absent");
        for (String name:List.of("Functions","Symbols","Types","Scripts","Console","Listing")) {
            AbstractButton shortcut=edt(() -> (AbstractButton)named(rail,"Workspace: "+name));
            if (shortcut==null) throw new AssertionError("Missing workspace shortcut: "+name);
            edt(() -> { shortcut.doClick(0);return null; }); Thread.sleep(180);
        }
        shot.take(browser,"light/workspace-navigation.png");
        edt(() -> { ((AbstractButton)named(rail,"Workspace: All panels")).doClick(0); return null; });
        Thread.sleep(200);
        JTextField search=edt(() -> {
            for(Window window:Window.getWindows()) {
                Component found=named(window,"Find workspace panel");
                if(found instanceof JTextField field && field.isShowing()) return field;
            }
            return null;
        });
        if(search==null)throw new AssertionError("Searchable panel picker did not open");
        edt(() -> { search.setText("Symbol Tree");return null; });
        shot.take(browser,"light/panel-picker.png");
        edt(() -> { search.postActionEvent();return null; });

        Object chooser=edt(() -> type("docking.widgets.filechooser.GhidraFileChooser").getConstructor(Component.class).newInstance(browser));
        edt(() -> { type("docking.DialogComponentProvider").getMethod("setTitle",String.class).invoke(chooser,"Opcodex chooser verification");
            type("docking.widgets.filechooser.GhidraFileChooser").getMethod("setCurrentDirectory",java.io.File.class).invoke(chooser,output.toFile());return null; });
        SwingUtilities.invokeLater(() -> { try { type("docking.DockingWindowManager").getMethod("showDialog",Component.class,
            type("docking.DialogComponentProvider")).invoke(null,browser,chooser); } catch (Exception e) { throw new RuntimeException(e); } });
        Window chooserWindow=waitWindow("Opcodex chooser verification");
        AbstractButton home=edt(() -> (AbstractButton)named(chooserWindow,"HOME_BUTTON"));
        if(home==null || !home.isShowing() || home.getHorizontalAlignment()!=SwingConstants.LEFT)
            throw new AssertionError("Modern places sidebar layout missing");
        Color sidebar=home.getParent().getParent().getBackground();
        Color expected=UIManager.getColor("Panel.background");
        if(sidebar.getRGB()!=expected.getRGB())throw new AssertionError("Chooser sidebar does not follow theme");
        shot.take(chooserWindow,"light/file-chooser.png");
        edt(() -> { type("docking.DialogComponentProvider").getMethod("close").invoke(chooser);return null; });

        Collection<?> providers=(Collection<?>)type("docking.DockingWindowManager").getMethod("getComponentProviders",Class.class)
            .invoke(manager,type("docking.ComponentProvider"));
        Object script=null;
        for(Object provider:providers)if("Script Manager".equals(type("docking.ComponentProvider").getMethod("getName").invoke(provider))) {script=provider;break;}
        if(script==null)throw new AssertionError("Script Manager provider missing");
        Object location=type("docking.ComponentProvider").getMethod("getHelpLocation").invoke(script);
        testedHelpLocation=location;
        Object service=type("help.Help").getMethod("getHelpService").invoke(null);
        edt(() -> { type("help.HelpService").getMethod("showHelp",type("ghidra.util.HelpLocation")).invoke(service,location);return null; });
        Window help=waitWindow("Opcodex Help");
        Thread.sleep(700);
        if(help.getWidth()<Math.min(1000,help.getGraphicsConfiguration().getBounds().width))
            throw new AssertionError("Help reading width too small: "+help.getWidth());
        if(edt(() -> helpVectors(help))<7)throw new AssertionError("Help toolbar commands missing");
        shot.take(help,"light/help.png");
        edt(() -> { help.setVisible(false); return null; });
        Files.writeString(output.resolve("pass2-result.json"),"{\"runtimeRevision\":true,\"errorReportIdentity\":true,\"nativeCursorProperties\":true,\"workspaceRailCommands\":true,\"lightChooserSidebar\":true,\"helpReadingWidth\":true}\n");
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
        followup(tool,browser,project,output,shot);
        ribbonSearch(tool,browser,output,shot);
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
        Object helpService=type("help.Help").getMethod("getHelpService").invoke(null);
        edt(() -> { type("help.HelpService").getMethod("showHelp",type("ghidra.util.HelpLocation")).invoke(helpService,testedHelpLocation);return null; });
        Window darkHelp=waitWindow("Opcodex Help");Thread.sleep(700);
        edt(() -> {
            if(helpVectors(darkHelp)<7)throw new AssertionError("Help lost commands after theme switching");
            JEditorPane editor=htmlPane(darkHelp);
            var sheet=((javax.swing.text.html.HTMLDocument)editor.getDocument()).getStyleSheet();
            Color heading=sheet.getForeground(sheet.getRule("h1"));
            Color expected=(Color)type("generic.theme.GColor").getConstructor(String.class).newInstance("color.fg.help.selector.h1");
            if(heading.getRGB()!=expected.getRGB())throw new AssertionError("Help heading retained its previous theme color");
            return null;
        });
        shot.take(darkHelp,"dark/help.png");
        edt(() -> {darkHelp.setVisible(false);return null;});
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
