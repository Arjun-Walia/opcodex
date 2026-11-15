/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.ptr.IntByReference;

// In-process test helper. Enumerates only this test process's own dialog windows.
public class OpcodexNativePickerProbe {
    static HWND find(String title) {
        HWND[] found = new HWND[1];
        User32.INSTANCE.EnumWindows((window, data) -> {
            IntByReference pid = new IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(window, pid);
            if (pid.getValue() != (int)ProcessHandle.current().pid()) return true;
            if (!User32.INSTANCE.IsWindowVisible(window)) return true;
            char[] text = new char[512]; User32.INSTANCE.GetWindowText(window,text,text.length);
            if (title.equals(com.sun.jna.Native.toString(text))) found[0]=window;
            return true;
        },null);
        return found[0];
    }
    public static void finish(String title, Path screenshot, boolean approve) throws Exception {
        HWND window=null; long deadline=System.currentTimeMillis()+30000;
        while (window==null && System.currentTimeMillis()<deadline) { window=find(title); Thread.sleep(100); }
        if (window==null) throw new AssertionError("Windows Common Item Dialog did not appear: "+title);
        // Swing must continue dispatching while the native shell dialog is open.
        SwingUtilities.invokeAndWait(() -> {});
        User32.INSTANCE.SetForegroundWindow(window);
        Thread.sleep(800);
        if (title.endsWith(" - multiple")) {
            HWND[] filename={null};
            User32.INSTANCE.EnumChildWindows(window,(child,data)->{
                char[] text=new char[512],kind=new char[128];
                User32.INSTANCE.GetClassName(child,kind,kind.length);
                User32.INSTANCE.GetWindowText(child,text,text.length);
                if("Edit".equals(com.sun.jna.Native.toString(kind)) && "sample Ω one.opc".equals(com.sun.jna.Native.toString(text))) filename[0]=child;
                return true;
            },null);
            if(filename[0]==null) throw new AssertionError("Native filename field missing");
            com.sun.jna.Memory text=new com.sun.jna.Memory(256); text.setWideString(0,"\"sample Ω one.opc\" \"sample two.opc\"");
            User32.INSTANCE.SendMessage(filename[0],0xC,new WPARAM(0),new LPARAM(com.sun.jna.Pointer.nativeValue(text)));
        }
        if (screenshot!=null) {
            RECT rect=new RECT(); User32.INSTANCE.GetClientRect(window,rect);
            BufferedImage shot=GDI32Util.getScreenshot(window);
            Files.createDirectories(screenshot.getParent());
            ImageIO.write(shot.getSubimage(0,0,rect.right,rect.bottom),"png",screenshot.toFile());
        }
        User32.INSTANCE.PostMessage(window,approve?0x111:0x10,new WPARAM(approve?1:0),new LPARAM(0));
    }
    private static Class<?> type(String name) throws Exception { return Class.forName(name,true,ClassLoader.getSystemClassLoader()); }
    public static void verify(Window parent,Path output) throws Exception {
        Path root=output.resolve("picker-fixtures"); Files.createDirectories(root);
        Path first=root.resolve("sample Ω one.opc"),second=root.resolve("sample two.opc");
        Files.writeString(first,"first fixture"); Files.writeString(second,"second fixture");
        for (String mode : new String[]{"single","cancel","folder","multiple","new-name"}) {
            Object[] holder=new Object[1];
            SwingUtilities.invokeAndWait(() -> { try {
                Class<?> clazz=type("docking.widgets.filechooser.GhidraFileChooser");
                Object chooser=clazz.getConstructor(Component.class).newInstance(parent); holder[0]=chooser;
                clazz.getMethod("setTitle",String.class).invoke(chooser,"Opcodex picker test - "+mode);
                clazz.getMethod("setLastDirectoryPreference",String.class).invoke(chooser,"Opcodex.NativePicker.Probe."+mode);
                if(mode.equals("folder")) {
                    Class<?> modes=type("docking.widgets.filechooser.GhidraFileChooserMode");
                    clazz.getMethod("setFileSelectionMode",modes).invoke(chooser,Enum.valueOf((Class)modes,"DIRECTORIES_ONLY"));
                }
                if(mode.equals("multiple")) clazz.getMethod("setMultiSelectionEnabled",boolean.class).invoke(chooser,true);
                File initial=mode.equals("folder")?root.toFile():mode.equals("new-name")?root.resolve("new file.opc").toFile():first.toFile();
                clazz.getMethod("setSelectedFile",File.class).invoke(chooser,initial);
            } catch(Exception error) {throw new RuntimeException(error);} });
            Object chooser=holder[0];
            FutureTask<Object> task=new FutureTask<>(() -> chooser.getClass().getMethod(mode.equals("multiple")?"getSelectedFiles":"getSelectedFile").invoke(chooser));
            SwingUtilities.invokeLater(task);
            finish("Opcodex picker test - "+mode,mode.equals("single")?output.resolve("dark/native-file-picker.png"):mode.equals("folder")?output.resolve("dark/native-folder-picker.png"):null,!mode.equals("cancel"));
            Object result=task.get(15,TimeUnit.SECONDS);
            if(mode.equals("cancel")) { if(result!=null || !(Boolean)chooser.getClass().getMethod("wasCancelled").invoke(chooser)) throw new AssertionError("Cancel selection leaked"); }
            else if(mode.equals("multiple")) { if(!new HashSet<>((java.util.List<?>)result).equals(Set.of(first.toFile(),second.toFile()))) throw new AssertionError("Multi selection mismatch: "+result); }
            else {
                File expected=mode.equals("folder")?root.toFile():mode.equals("new-name")?root.resolve("new file.opc").toFile():first.toFile();
                if(!expected.equals(result)) throw new AssertionError(mode+" selection mismatch: "+result);
                if(!expected.equals(chooser.getClass().getMethod("getSelectedFile",boolean.class).invoke(chooser,false))) throw new AssertionError("Selection not retained");
            }
            SwingUtilities.invokeAndWait(() -> { try { chooser.getClass().getMethod("dispose").invoke(chooser); }catch(Exception error){throw new RuntimeException(error);} });
        }
        Files.writeString(output.resolve("native-picker-result.json"),"{\"single\":true,\"cancel\":true,\"folders\":true,\"multiple\":true,\"newName\":true,\"responsiveSwing\":true}\n");
    }
}
