/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
import java.awt.*;
import java.awt.event.MouseEvent;
import java.nio.file.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.Callable;
import javax.swing.*;

/** Tests only the launched test app, in its disposable profile. */
public class OpcodexPanelProbe {
    public interface Shot { void take(Window window,String path) throws Exception; }
    private static Class<?> type(String name) throws Exception { return Class.forName(name,true,ClassLoader.getSystemClassLoader()); }
    private static <T>T edt(Callable<T> work) throws Exception {
        Object[] result={null}; Exception[] failure={null};
        SwingUtilities.invokeAndWait(()->{try{result[0]=work.call();}catch(Exception e){failure[0]=e;}});
        if(failure[0]!=null)throw failure[0];return (T)result[0];
    }
    private static AbstractButton named(Component c,String name) {
        if(c instanceof AbstractButton b && name.equals(b.getName()))return b;
        if(c instanceof Container p)for(Component child:p.getComponents()){var b=named(child,name);if(b!=null)return b;}
        return null;
    }
    private static JMenuItem item(JPopupMenu menu,String text) {
        for(Component c:menu.getComponents()) {
            if(c instanceof JMenu sub){var found=item(sub.getPopupMenu(),text);if(found!=null)return found;}
            if(c instanceof JMenuItem i && text.equals(i.getText()))return i;
        }return null;
    }
    private static JPopupMenu overflow(Window window)throws Exception {
        edt(()->{named(window,"More workspace actions").doClick(0);return null;});
        return edt(()->{for(MenuElement e:MenuSelectionManager.defaultManager().getSelectedPath())if(e instanceof JPopupMenu p)return p;throw new AssertionError("Toolbar overflow missing");});
    }
    private static SortedSet<String> actions(Object tool)throws Exception {
        Class<?> action=type("docking.action.DockingActionIf");TreeSet<String> result=new TreeSet<>();
        for(Object value:(Collection<?>)tool.getClass().getMethod("getAllActions").invoke(tool)) {
            Object menu=action.getMethod("getMenuBarData").invoke(value);
            String path=menu==null?"":Arrays.toString((String[])type("docking.action.MenuData").getMethod("getMenuPath").invoke(menu));
            result.add(action.getMethod("getOwner").invoke(value)+" | "+action.getMethod("getName").invoke(value)+" | "+path);
        }return result;
    }
    private static void dividers(Component c,java.util.List<Container> result) {
        if(c.isShowing() && c.getClass().getName().equals("docking.SplitPanel"))result.add((Container)c);
        if(c instanceof Container p)for(Component child:p.getComponents())dividers(child,result);
    }
    private static int verifyResize(Window window,String theme,Shot shot)throws Exception {
        var splits=edt(()->{java.util.List<Container> found=new ArrayList<>();dividers(window,found);return found;});
        int passed=0;
        for(Container split:splits) {
            Method position=split.getClass().getDeclaredMethod("getDividerPosition");position.setAccessible(true);
            float original=(Float)edt(()->position.invoke(split));
            boolean moved=edt(()->{
                Component left=split.getComponent(0),divider=split.getComponent(1),right=split.getComponent(2);
                boolean horizontal=divider.getWidth()<divider.getHeight();
                int before=horizontal?left.getWidth():left.getHeight(),other=horizontal?right.getWidth():right.getHeight();
                int minimumOther=horizontal?right.getMinimumSize().width:right.getMinimumSize().height;
                int step=other-minimumOther>10?55:-55;
                MouseEvent drag=new MouseEvent(divider,MouseEvent.MOUSE_DRAGGED,System.currentTimeMillis(),0,horizontal?step:divider.getWidth()/2,horizontal?divider.getHeight()/2:step,1,false);
                for(var listener:divider.getMouseMotionListeners())listener.mouseDragged(drag);
                int after=horizontal?left.getWidth():left.getHeight(),otherAfter=horizontal?right.getWidth():right.getHeight();
                if(after==before || Integer.signum(after-before)==Integer.signum(otherAfter-other))throw new AssertionError("Panel divider did not resize both neighbors");
                if(!(divider instanceof JComponent grip) || grip.getToolTipText()==null || !grip.getToolTipText().contains("Drag"))throw new AssertionError("Resize affordance missing");
                return true;
            });
            if(moved)passed++;
            if(passed==2)shot.take(window,theme+"/panels/resized-workspace.png");
            edt(()->{
                Component divider=split.getComponent(1);
                MouseEvent twice=new MouseEvent(divider,MouseEvent.MOUSE_CLICKED,System.currentTimeMillis(),0,divider.getWidth()/2,divider.getHeight()/2,2,false);
                for(var listener:divider.getMouseListeners())listener.mouseClicked(twice);
                float current=(Float)position.invoke(split);
                Component left=split.getComponent(0),right=split.getComponent(2);
                boolean horizontal=divider.getWidth()<divider.getHeight();
                int available=horizontal?split.getWidth()-divider.getWidth():split.getHeight()-divider.getHeight();
                int minimumLeft=horizontal?left.getMinimumSize().width:left.getMinimumSize().height;
                int minimumRight=horizontal?right.getMinimumSize().width:right.getMinimumSize().height;
                float expected=available<=minimumLeft+minimumRight?(float)minimumLeft/Math.max(1,minimumLeft+minimumRight):Math.max((float)minimumLeft/available,Math.min(.5f,1f-(float)minimumRight/available));
                if(Math.abs(current-expected)>.02f)throw new AssertionError("Equal-size divider reset failed within pane minimums");
                split.getClass().getMethod("setDividerPosition",float.class).invoke(split,original);split.doLayout();return null;
            });
        }
        if(passed<2)throw new AssertionError("Expected independently resizable sidebar and code panes");
        return passed;
    }
    public static void verify(Object tool,Window window,Path output,String theme,Shot shot)throws Exception {
        SortedSet<String> before=edt(()->actions(tool));
        JPopupMenu menu=overflow(window);
        edt(()->{if(!((JCheckBoxMenuItem)item(menu,"Show complete toolbar")).isSelected()) item(menu,"Show complete toolbar").doClick(0);MenuSelectionManager.defaultManager().clearSelectedPath();return null;});
        Thread.sleep(250);
        if(!before.equals(edt(()->actions(tool))))throw new AssertionError("Complete toolbar changed command registry");
        if(edt(()->named(window,"Symbol Tree"))==null)throw new AssertionError("Complete toolbar did not restore secondary controls");
        shot.take(window,theme+"/panels/complete-toolbar.png");
        Files.writeString(output.resolve(theme+"-icons-Complete-Toolbar.tsv"),edt(()->iconInventory(window)));
        final JPopupMenu compact=overflow(window);
        edt(()->{item(compact,"Show complete toolbar").doClick(0);MenuSelectionManager.defaultManager().clearSelectedPath();return null;});
        if(!before.equals(edt(()->actions(tool))))throw new AssertionError("Focused toolbar changed command registry");
        Files.write(output.resolve(theme+"-commands.txt"),before);
        java.util.List<String> headings=edt(()->{
            JMenuBar bar=((RootPaneContainer)window).getRootPane().getJMenuBar();java.util.List<String> names=new ArrayList<>();
            for(int i=0;i<bar.getMenuCount();i++)if(bar.getMenu(i)!=null)names.add(bar.getMenu(i).getText());return names;
        });
        for(String heading:java.util.List.of("File","Edit","Analysis","Graph","Navigation","Search","Select","Tools","Window","Help"))
            if(!headings.contains(heading))throw new AssertionError("Original menu missing: "+heading);
        Files.write(output.resolve(theme+"-menu-headings.txt"),headings);
        edt(()->{JMenuBar bar=((RootPaneContainer)window).getRootPane().getJMenuBar();for(int i=0;i<bar.getMenuCount();i++)if("Window".equals(bar.getMenu(i).getText()))bar.getMenu(i).doClick();return null;});
        shot.take(window,theme+"/panels/window-menu.png");
        edt(()->{MenuSelectionManager.defaultManager().clearSelectedPath();return null;});
        Object manager=tool.getClass().getMethod("getWindowManager").invoke(tool);
        Class<?> managerType=type("docking.DockingWindowManager"),providerType=type("docking.ComponentProvider");
        java.util.List<String> tested=new ArrayList<>();int resizeCount=0;
        for(String name:java.util.List.of("Functions Window","Symbol Tree","DataTypes Provider","Program Tree","Bytes","Defined Strings","Console","Script Manager","Function Graph")) {
            Object provider=edt(()->managerType.getMethod("getComponentProvider",String.class).invoke(manager,name));
            if(provider==null)throw new AssertionError("Original panel missing: "+name);
            boolean wasVisible=(Boolean)edt(()->providerType.getMethod("isVisible").invoke(provider));
            edt(()->{providerType.getMethod("setVisible",boolean.class).invoke(provider,true);providerType.getMethod("toFront").invoke(provider);return null;});
            if(name.equals("Function Graph")) populateGraph(tool,provider,providerType);
            Thread.sleep(name.equals("Function Graph")?1200:400);
            JComponent component=(JComponent)edt(()->providerType.getMethod("getComponent").invoke(provider));
            if(!edt(component::isShowing))throw new AssertionError("Panel did not open: "+name);
            Window pane=edt(()->SwingUtilities.getWindowAncestor(component));
            shot.take(pane,theme+"/panels/"+name.toLowerCase(Locale.ROOT).replace(' ','-')+".png");
            final Window iconPane = pane;
            Files.writeString(output.resolve(theme + "-icons-" + name.replace(' ', '-') + ".tsv"),
                edt(() -> iconInventory(iconPane)));
            if(name.equals("Console"))resizeCount=verifyResize(window,theme,shot);
            edt(()->{providerType.getMethod("setVisible",boolean.class).invoke(provider,wasVisible);return null;});
            tested.add(name);
        }
        Class<?> workspaces=type("docking.menu.OpcodexWorkspaces");Method focus=workspaces.getDeclaredMethod("focus",managerType);focus.setAccessible(true);
        edt(()->{focus.invoke(null,manager);return null;});
        Files.writeString(output.resolve(theme+"-panel-result.txt"),"Menus="+headings+"\nCommands="+before.size()+"\nToolbar registry unchanged=true\nPanels="+tested+"\nResizable dividers="+resizeCount+"\n");
    }
    private static String cell(String text) { return text == null ? "" : text.replace('\t',' ').replace('\n',' '); }
    private static String glyphs(Icon icon, Set<String> ids) throws Exception {
        if (icon == null) return "none";
        String kind = icon.getClass().getName();
        if (kind.endsWith("MultipleActionDockingToolbarButton$IconWithDropDownArrow")) {
            Field field=icon.getClass().getDeclaredField("baseIcon");field.setAccessible(true);
            return glyphs((Icon)field.get(icon),ids);
        }
        if (kind.equals("generic.theme.GIcon")) {
            ids.add((String) type("generic.theme.GIcon").getMethod("getId").invoke(icon));
            return glyphs((Icon)type("generic.theme.GIcon").getMethod("getDelegate").invoke(icon), ids);
        }
        if (kind.equals("generic.theme.builtin.OpcodexResourceIcon")) {
            Field field=icon.getClass().getDeclaredField("glyph");field.setAccessible(true);return field.get(icon).toString();
        }
        if (kind.equals("generic.theme.builtin.OpcodexUi$PaintedIcon")) {
            Method method=icon.getClass().getDeclaredMethod("source");method.setAccessible(true);return glyphs((Icon)method.invoke(icon), ids);
        }
        if (type("resources.icons.DerivedImageIcon").isInstance(icon))
            return glyphs((Icon)type("resources.icons.DerivedImageIcon").getMethod("getSourceIcon").invoke(icon), ids);
        if (type("resources.MultiIcon").isInstance(icon)) {
            java.util.List<String> parts=new ArrayList<>();
            for (Icon part:(Icon[])type("resources.MultiIcon").getMethod("getIcons").invoke(icon))parts.add(glyphs(part,ids));
            return String.join("+",parts);
        }
        if (type("resources.icons.TranslateIcon").isInstance(icon))
            return "move:"+glyphs((Icon)type("resources.icons.TranslateIcon").getMethod("getBaseIcon").invoke(icon),ids);
        if (type("resources.icons.RotateIcon").isInstance(icon))
            return "rotate:"+glyphs((Icon)type("resources.icons.RotateIcon").getMethod("getSourceIcon").invoke(icon),ids);
        if (type("resources.icons.UrlImageIcon").isInstance(icon)) {
            String path=(String)type("resources.icons.UrlImageIcon").getMethod("getOriginalPath").invoke(icon);
            return path.replaceAll("^.*images/opcodex/ui/(?:light|dark)/", "").replaceAll("-[0-9]+[.]png$", "");
        }
        if (kind.equals("docking.menu.OpcodexToolbarStrip$ScrollIcon")) {
            // Parameterized geometry, like RotateIcon above, has two distinct directions.
            Field direction = icon.getClass().getDeclaredField("forward"); direction.setAccessible(true);
            return "toolbar-scroll-" + ((Boolean)direction.get(icon) ? "right" : "left");
        }
        return kind;
    }
    private static String panel(Component component) throws Exception {
        Window owner=SwingUtilities.getWindowAncestor(component);
        String prefix=owner instanceof Frame frame && frame.getTitle().startsWith("Debugger") ? "Debugger / " : "";
        for (Container parent=component.getParent();parent!=null;parent=parent.getParent()) {
            if (type("docking.DockableComponent").isInstance(parent)) {
                Method method=type("docking.DockableComponent").getDeclaredMethod("getComponentWindowingPlaceholder");
                method.setAccessible(true);Object placeholder=method.invoke(parent);
                // Dynamic and static Listing providers share a name but are separate panels.
                Object provider=type("docking.ComponentPlaceholder").getMethod("getProvider").invoke(placeholder);
                String ownerName=(String)type("docking.ComponentProvider").getMethod("getOwner").invoke(provider);
                return prefix+(String)type("docking.ComponentPlaceholder").getMethod("getName").invoke(placeholder)+" ["+ownerName+"]";
            }
        }
        return prefix+"Main toolbar";
    }
    private static void inventory(Component component, StringBuilder rows) throws Exception {
        Icon icon=null;String name=null;
        if (component instanceof AbstractButton button && button.isShowing()) {icon=button.getIcon();name=button.getName();}
        if (component instanceof JLabel label && label.isShowing() && component.getClass().getSimpleName().equals("ClearFilterLabel")) {
            icon=label.getIcon();name="Clear filter";
        }
        if (icon!=null && name!=null) {
            Set<String> ids=new TreeSet<>();String glyph=glyphs(icon,ids);
            rows.append(cell(panel(component))).append('\t').append(cell(name)).append('\t')
                .append(String.join(",",ids)).append('\t').append(glyph).append('\n');
        }
        if (component instanceof Container parent)for (Component child:parent.getComponents())inventory(child,rows);
    }
    private static String iconInventory(Window window) throws Exception {
        StringBuilder rows=new StringBuilder("panel\tbutton\tids\tglyph\n");inventory(window,rows);return rows.toString();
    }
    public static void writeIconInventory(Window window,Path destination)throws Exception {
        Files.writeString(destination,edt(()->iconInventory(window)));
    }
    private static void populateGraph(Object tool,Object provider,Class<?> providerType)throws Exception {
        Class<?> pmType=type("ghidra.app.services.ProgramManager"),programType=type("ghidra.program.model.listing.Program");
        Object pm=tool.getClass().getMethod("getService",Class.class).invoke(tool,pmType);
        Object program=pmType.getMethod("getCurrentProgram").invoke(pm);
        Object functions=programType.getMethod("getFunctionManager").invoke(program);
        Iterator<?> iterator=(Iterator<?>)type("ghidra.program.model.listing.FunctionManager").getMethod("getFunctions",boolean.class).invoke(functions,true);
        Class<?> functionType=type("ghidra.program.model.listing.Function"),addressType=type("ghidra.program.model.address.Address");
        Object entry=null;
        while(iterator.hasNext()) {
            Object function=iterator.next();
            if("mix_bytes".equals(functionType.getMethod("getName").invoke(function))) {entry=functionType.getMethod("getEntryPoint").invoke(function);break;}
        }
        if(entry==null)throw new AssertionError("Fixture function missing");
        Class<?> goType=type("ghidra.app.services.GoToService");
        Object go=tool.getClass().getMethod("getService",Class.class).invoke(tool,goType);
        Object first=programType.getMethod("getMinAddress").invoke(program);
        Object location=addressType.getMethod("add",long.class).invoke(entry,0x30L);
        edt(()->goType.getMethod("goTo",addressType).invoke(go,first));Thread.sleep(100);
        edt(()->goType.getMethod("goTo",addressType).invoke(go,location));
        long deadline=System.currentTimeMillis()+8000;
        while(System.currentTimeMillis()<deadline) {
            String title=(String)edt(()->providerType.getMethod("getSubTitle").invoke(provider));
            if(title!=null && title.contains("mix_bytes"))return;
            Thread.sleep(100);
        }
        throw new AssertionError("Function Graph did not populate from navigation");
    }
}
