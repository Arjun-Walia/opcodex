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
    public static void verify(Object tool, Window browser, Window project, Path output, Shot shot) throws Exception {
        Map<JTree,List<TreePath>> before=edt(() -> expanded(project));
        before.putAll(edt(() -> expanded(browser)));
        if (before.values().stream().mapToInt(List::size).sum() == 0) throw new AssertionError("No expanded paths to verify");
        theme("OpcodexLightTheme");
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
        if ((Boolean)type("generic.theme.builtin.OpcodexUi").getMethod("active").invoke(null))
            throw new AssertionError("Classic still uses modern chrome");
        String classic=edt(() -> layout(manager));
        if (modern.equals(classic)) throw new AssertionError("Classic did not restore the original layout");
        shot.take(browser,"classic/codebrowser.png"); shot.take(project,"classic/project.png");
        Files.writeString(output.resolve("classic-layout.xml"),classic);
        theme("OpcodexDarkTheme");
        edt(() -> { selectionContrast(); return null; });
        String restored=edt(() -> layout(manager));
        // Exact serialized layout preservation also checks visibility, provider identity and divider positions.
        if (!modern.equals(restored)) {
            Files.writeString(output.resolve("modern-before.xml"),modern);
            Files.writeString(output.resolve("modern-after.xml"),restored);
            throw new AssertionError("Modern layout was not restored after the Classic round trip");
        }
        // Measure actual event-dispatch turnaround after the panel/theme work has settled.
        List<Double> samples=new ArrayList<>();
        for (int i=0;i<80;i++) {
            long start=System.nanoTime(); edt(() -> null);
            samples.add((System.nanoTime()-start)/1_000_000d); Thread.sleep(10);
        }
        Collections.sort(samples);
        Files.writeString(output.resolve("audit-result.json"),
            "{\"lightSelectionContrast\":true,\"darkSelectionContrast\":true,\"expandedTreesRetained\":true,"+
            "\"classicOriginalLayout\":true,\"modernLayoutRestored\":true,\"eventDispatchSamples\":80,"+
            "\"eventDispatchP95Ms\":"+samples.get(75)+",\"eventDispatchMaxMs\":"+samples.get(79)+"}\n");
    }
}
