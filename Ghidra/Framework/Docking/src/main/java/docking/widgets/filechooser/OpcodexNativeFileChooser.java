/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package docking.widgets.filechooser;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;

import com.sun.jna.*;
import com.sun.jna.ptr.*;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.COM.Unknown;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.Guid.*;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.win32.StdCallLibrary;

/** Windows Common Item Dialog. All COM objects stay on one dedicated STA thread. */
final class OpcodexNativeFileChooser {

	private static final GUID OPEN_DIALOG = new GUID("{DC1C5A9C-E88A-4DDE-A5A1-60F82A20AEF7}");
	private static final GUID OPEN_INTERFACE = new GUID("{D57C7288-D4AD-4768-BE02-9D969532D960}");
	private static final IID SHELL_ITEM = new IID("{43826D1E-E718-42EE-BC55-A1E261C37BFE}");
	private static final int CANCELLED = 0x800704c7;
	private static final int FORCE_FILESYSTEM = 0x40;
	private static final int PATH_MUST_EXIST = 0x800;
	private static final int PICK_FOLDERS = 0x20;
	private static final int ALLOW_MULTISELECT = 0x200;
	private static final int DONT_ADD_TO_RECENT = 0x2000000;

	record Filter(String description, String pattern) {}
	record Request(String title, String approve, File directory, File initial,
		boolean folders, boolean multiple, List<Filter> filters, int selectedFilter) {}
	record Result(List<File> files, int selectedFilter) {}

	private OpcodexNativeFileChooser() {}

	/** Call on the EDT; the invisible modal owner keeps Swing responsive and properly blocked. */
	static Result show(Component parent, Request request) {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException("Native picker must be opened on the event thread");
		}
		Window owner = parent instanceof Window window ? window :
			(parent == null ? KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() :
			SwingUtilities.getWindowAncestor(parent));
		JDialog bridge = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
		if (owner != null) bridge.setIconImages(owner.getIconImages());
		bridge.setUndecorated(true);
		bridge.setType(Window.Type.UTILITY);
		bridge.setSize(1, 1);
		bridge.setLocationRelativeTo(owner);
		bridge.setOpacity(0f);
		Result[] result = new Result[1];
		Throwable[] failure = new Throwable[1];
		bridge.addWindowListener(new WindowAdapter() {
			@Override public void windowOpened(WindowEvent event) {
				Pointer handle;
				try { handle = Native.getComponentPointer(bridge); }
				catch (RuntimeException | LinkageError error) {
					failure[0] = error;
					bridge.dispose();
					return;
				}
				Thread thread = new Thread(() -> {
					try { result[0] = choose(new HWND(handle), request); }
					catch (RuntimeException | LinkageError error) { failure[0] = error; }
					finally { SwingUtilities.invokeLater(bridge::dispose); }
				}, "Opcodex Windows file picker");
				thread.setDaemon(true);
				thread.start();
			}
		});
		try { bridge.setVisible(true); }
		finally { bridge.dispose(); }
		if (failure[0] != null) throw new IllegalStateException("Windows file picker failed", failure[0]);
		return result[0];
	}

	public interface Shell extends StdCallLibrary {
		Shell INSTANCE = Native.load("shell32", Shell.class);
		HRESULT SHCreateItemFromParsingName(WString path, Pointer context, REFIID iid,
			PointerByReference result);
	}

	@Structure.FieldOrder({"name", "pattern"})
	public static class FilterSpec extends Structure {
		public WString name;
		public WString pattern;
	}

	/** The slots below follow the published IFileDialog / IShellItem SDK vtables. */
	private static final class ComObject extends Unknown implements AutoCloseable {
		ComObject(Pointer pointer) { super(pointer); }
		HRESULT call(int slot, Object... arguments) {
			Object[] parameters = new Object[arguments.length + 1];
			parameters[0] = getPointer();
			System.arraycopy(arguments, 0, parameters, 1, arguments.length);
			return (HRESULT) _invokeNativeObject(slot, parameters, HRESULT.class);
		}
		void checked(int slot, Object... arguments) { COMUtils.checkRC(call(slot, arguments)); }
		ComObject object(int slot, Object... arguments) {
			PointerByReference value = new PointerByReference();
			Object[] parameters = java.util.Arrays.copyOf(arguments, arguments.length + 1);
			parameters[arguments.length] = value;
			checked(slot, parameters);
			return new ComObject(value.getValue());
		}
		@Override public void close() { Release(); }
	}

	private static Result choose(HWND owner, Request request) {
		COMUtils.checkRC(Ole32.INSTANCE.CoInitializeEx(null, Ole32.COINIT_APARTMENTTHREADED));
		try {
			PointerByReference created = new PointerByReference();
			COMUtils.checkRC(Ole32.INSTANCE.CoCreateInstance(OPEN_DIALOG, null,
				WTypes.CLSCTX_INPROC_SERVER, OPEN_INTERFACE, created));
			try (ComObject dialog = new ComObject(created.getValue())) {
				IntByReference options = new IntByReference();
				dialog.checked(10, options); // GetOptions
				// The original chooser accepts typed, new names. Do not impose FILEMUSTEXIST.
				int flags = (options.getValue() & ~0x1000) | FORCE_FILESYSTEM | PATH_MUST_EXIST |
					DONT_ADD_TO_RECENT | (request.folders() ? PICK_FOLDERS : 0) |
					(request.multiple() ? ALLOW_MULTISELECT : 0);
				dialog.checked(9, flags); // SetOptions
				dialog.checked(17, new WString(request.title()));
				dialog.checked(18, new WString(request.approve()));
				if (request.directory() != null && request.directory().isDirectory()) {
					PointerByReference folder = new PointerByReference();
					COMUtils.checkRC(Shell.INSTANCE.SHCreateItemFromParsingName(
						new WString(request.directory().getAbsolutePath()), null,
						new REFIID(SHELL_ITEM), folder));
					try (ComObject item = new ComObject(folder.getValue())) {
						dialog.checked(12, item.getPointer()); // SetFolder
					}
				}
				if (request.initial() != null && !request.initial().isDirectory())
					dialog.checked(15, new WString(request.initial().getName()));
				FilterSpec[] specs = null;
				if (!request.folders() && !request.filters().isEmpty()) {
					specs = (FilterSpec[]) new FilterSpec().toArray(request.filters().size());
					for (int i = 0; i < specs.length; i++) {
						specs[i].name = new WString(request.filters().get(i).description());
						specs[i].pattern = new WString(request.filters().get(i).pattern());
						specs[i].write();
					}
					dialog.checked(4, specs.length, specs[0].getPointer());
					dialog.checked(5, request.selectedFilter() + 1);
				}
				HRESULT shown = dialog.call(3, owner);
				// Keep filter backing strings alive for the entire native call.
				java.lang.ref.Reference.reachabilityFence(specs);
				if (shown.intValue() == CANCELLED) return new Result(List.of(), request.selectedFilter());
				COMUtils.checkRC(shown);
				List<File> files = new ArrayList<>();
				if (request.multiple()) {
					try (ComObject items = dialog.object(27)) { // IFileOpenDialog.GetResults
						IntByReference count = new IntByReference();
						items.checked(7, count); // IShellItemArray.GetCount
						for (int i = 0; i < count.getValue(); i++) {
							try (ComObject item = items.object(8, i)) { files.add(path(item)); }
						}
					}
				}
				else { try (ComObject item = dialog.object(20)) { files.add(path(item)); } }
				IntByReference filter = new IntByReference(request.selectedFilter() + 1);
				if (!request.folders()) dialog.checked(6, filter);
				return new Result(List.copyOf(files), filter.getValue() - 1);
			}
		}
		finally { Ole32.INSTANCE.CoUninitialize(); }
	}

	private static File path(ComObject item) {
		PointerByReference text = new PointerByReference();
		item.checked(5, 0x80058000, text); // IShellItem.GetDisplayName(SIGDN_FILESYSPATH)
		try { return new File(text.getValue().getWideString(0)); }
		finally { Ole32.INSTANCE.CoTaskMemFree(text.getValue()); }
	}
}
