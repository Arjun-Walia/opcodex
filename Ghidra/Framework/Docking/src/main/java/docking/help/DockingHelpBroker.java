/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package docking.help;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.List;

import javax.help.*;
import javax.help.event.HelpModelEvent;
import javax.help.event.HelpModelListener;
import javax.swing.*;

import docking.framework.ApplicationInformationDisplayFactory;
import ghidra.util.Msg;
import ghidra.util.SystemUtilities;
import help.CustomTOCView;
import help.GHelpBroker;
import resources.Icons;

/**
 * An extension of the {@link GHelpBroker} that allows {@code Docking} classes to be installed.
 * <p>
 * Additions include:
 * <UL> 
 * 	<LI>A find feature</LI>
 *  <LI>A UI navigation aid</LI>
 *  <LI>An action to refresh the current page</LI>
 * </UL>
 */
public class DockingHelpBroker extends GHelpBroker {

	private HelpModelListener helpModelListener = new HelpIDChangedListener();

	private CalloutRequest callout;
	private JToolBar decoratedToolbar;

	public DockingHelpBroker(HelpSet hs) {
		super(hs);
	}

	@Override
	protected List<Image> getApplicationIcons() {
        // OPCODEX: a help window opened after switching themes must use the current logo.
		return ApplicationInformationDisplayFactory.getWindowIcons();
	}

	@Override
	protected HelpModel getCustomHelpModel() {
		//
		// Unusual Code Alert!: We have opened up access to the help system's HelpModel by way
		//                      of our CustomTOCView object that we install elsewhere.  We need
		//                      access to the model because of a bug in the help system
		//                      (SCR 7639).  Unfortunately, the Java Help system does not give us
		//                      access to the model directly, but we have opened up the access from
		//                      one of our overriding components.  The following code is
		//                      digging-out our custom component to get at the model.  An
		//                      alternative approach would be to just use reflection and violate
		//                      security restrictions, but that seemed worse than this solution.
		//

		WindowPresentation windowPresentation = getWindowPresentation();
		HelpSet helpSet = windowPresentation.getHelpSet();
		NavigatorView tocView = helpSet.getNavigatorView("TOC");
		if (!(tocView instanceof CustomTOCView)) {
			// not sure how this could happen
			Msg.debug(this, "The help system is not using the CustomTOCView class!");
			return null;
		}

		CustomTOCView customTOCView = (CustomTOCView) tocView;
		return customTOCView.getHelpModel();
	}

	@Override
	protected void installHelpSearcher(JHelp jHelp, HelpModel helpModel) {
		helpModel.addHelpModelListener(helpModelListener);
		new HelpViewSearcher(jHelp);
	}

	@Override
	protected void showNavigationAid(URL url) {
		prepareToCallout(url);
	}

	@Override
	protected void installActions(JHelp help) {
		// OPCODEX: keep navigation beside the document and reserve a usable reading width.
		if (generic.theme.builtin.OpcodexUi.active()) {
			javax.swing.SwingUtilities.invokeLater(() -> {
				java.awt.Window window = SwingUtilities.getWindowAncestor(help);
				if (window == null) return;
				java.awt.Rectangle available = window.getGraphicsConfiguration().getBounds();
				java.awt.Insets insets = java.awt.Toolkit.getDefaultToolkit().getScreenInsets(window.getGraphicsConfiguration());
				int width = available.width - insets.left - insets.right;
				int height = available.height - insets.top - insets.bottom;
				window.setSize(Math.min(width, Math.max(1100, window.getWidth())),
					Math.min(height, Math.max(700, window.getHeight())));
				window.setLocation(Math.max(available.x + insets.left,
					Math.min(window.getX(), available.x + insets.left + width - window.getWidth())),
					Math.max(available.y + insets.top,
					Math.min(window.getY(), available.y + insets.top + height - window.getHeight())));
				layoutNavigation(help);
			});
		}
		JToolBar toolbar = null;
		Component[] components = help.getComponents();
		for (Component c : components) {
			if (c instanceof JToolBar) {
				toolbar = (JToolBar) c;
				break;
			}
		}

		if (toolbar == null) {
			// shouldn't happen
			return;
		}

		addHelpActions(toolbar);
		decoratedToolbar = toolbar;
		refreshHelpIcons(help);
		help.addPropertyChangeListener("UI", event -> SwingUtilities.invokeLater(() -> {
			for (Component child : help.getComponents()) {
				if (child instanceof JToolBar current && current != decoratedToolbar) {
					// JavaHelp rebuilds its toolbar on LAF changes; retain zoom and navigation aid.
					addCustomToolbarItems(help);
					addHelpActions(current);
					decoratedToolbar = current;
				}
			}
			refreshHelpIcons(help);
			if (getCurrentURL() != null) reloadHelpPage(getCurrentURL(), true);
		}));
	}

	private void addHelpActions(JToolBar toolbar) {
		// separate the Java help stuff from our actions
		toolbar.addSeparator();

		NavigationAidToggleAction action = new NavigationAidToggleAction();
		toolbar.add(new JButton(action));

		if (SystemUtilities.isInDevelopmentMode()) {

			Action refreshAction = new AbstractAction() {

				{
					putValue(Action.SMALL_ICON, Icons.REFRESH_ICON);
					putValue(Action.SHORT_DESCRIPTION, "Reload the current page");
				}

				@Override
				public void actionPerformed(ActionEvent e) {
					reloadHelpPage(getCurrentURL(), true);
				}
			};
			toolbar.add(new JButton(refreshAction));
		}
	}

	private static void refreshHelpIcons(java.awt.Container parent) {
		boolean modern = generic.theme.builtin.OpcodexUi.active();
		for (Component child : parent.getComponents()) {
			if (child instanceof AbstractButton button) {
				if (button.getParent() instanceof JToolBar) {
					button.setCursor(modern ? java.awt.Cursor.getPredefinedCursor(button.isEnabled() ?
						java.awt.Cursor.HAND_CURSOR : java.awt.Cursor.DEFAULT_CURSOR) : null);
					if (button.getClientProperty("opcodex.help.pointer") == null) {
						button.putClientProperty("opcodex.help.pointer", true);
						button.addPropertyChangeListener("enabled", event -> button.setCursor(
							generic.theme.builtin.OpcodexUi.active() ? java.awt.Cursor.getPredefinedCursor(
								button.isEnabled() ? java.awt.Cursor.HAND_CURSOR : java.awt.Cursor.DEFAULT_CURSOR) : null));
					}
				}
				String glyph = null;
				java.util.List<java.util.EventListener> listeners = new java.util.ArrayList<>();
				java.util.Collections.addAll(listeners, button.getActionListeners());
				java.util.Collections.addAll(listeners, button.getMouseListeners());
				for (java.util.EventListener listener : listeners) {
					glyph = switch (listener.getClass().getName()) {
						case "javax.help.BackAction" -> "left";
						case "javax.help.ForwardAction" -> "right";
						case "javax.help.PrintAction" -> "print";
						case "javax.help.PrintSetupAction" -> "settings";
						case "javax.help.FavoritesAction" -> "bookmark";
						default -> glyph;
					};
				}
				if (glyph != null) {
					if (button.getClientProperty("opcodex.help.originalIcon") == null) {
						button.putClientProperty("opcodex.help.originalIcon", button.getIcon());
						button.putClientProperty("opcodex.help.originalDisabledIcon", button.getDisabledIcon());
					}
					Icon icon = modern ? generic.theme.builtin.OpcodexUi.sized(
						new generic.theme.GIcon("icon.opcodex.glyph." + glyph), 20) :
						(Icon) button.getClientProperty("opcodex.help.originalIcon");
					button.setIcon(icon);
					button.setDisabledIcon(modern ? generic.theme.builtin.OpcodexUi.disabled(icon) :
						(Icon) button.getClientProperty("opcodex.help.originalDisabledIcon"));
					button.setCursor(modern ? java.awt.Cursor.getPredefinedCursor(button.isEnabled() ?
						java.awt.Cursor.HAND_CURSOR : java.awt.Cursor.DEFAULT_CURSOR) : null);
				}
			}
			if (child instanceof JTabbedPane tabs) {
				for (int i = 0; i < tabs.getTabCount(); i++) {
					String glyph = switch (tabs.getComponentAt(i)) {
						case JHelpTOCNavigator navigator -> "book";
						case JHelpIndexNavigator navigator -> "list";
						case JHelpSearchNavigator navigator -> "search";
						case JHelpFavoritesNavigator navigator -> "star";
						default -> null;
					};
					if (glyph == null) continue;
					String key = "opcodex.help.originalTabIcon." + i;
					if (tabs.getClientProperty(key) == null) tabs.putClientProperty(key, tabs.getIconAt(i));
					tabs.setIconAt(i, modern ? new generic.theme.GIcon("icon.opcodex.glyph." + glyph) :
						(Icon) tabs.getClientProperty(key));
				}
			}
			if (child instanceof java.awt.Container nested) refreshHelpIcons(nested);
		}
	}

	private static void layoutNavigation(java.awt.Container parent) {
		for (Component child : parent.getComponents()) {
			if (child instanceof JSplitPane split && split.getOrientation() == JSplitPane.HORIZONTAL_SPLIT &&
				split.getLeftComponent() != null && split.getRightComponent() != null) {
				split.getLeftComponent().setMinimumSize(new java.awt.Dimension(160, 100));
				split.getRightComponent().setMinimumSize(new java.awt.Dimension(320, 100));
				split.setResizeWeight(0);
				split.setDividerLocation(260);
			}
			if (child instanceof java.awt.Container nested) layoutNavigation(nested);
		}
	}

	@Override // opened access
	protected void reloadHelpPage(URL url, boolean preserveLocation) {
		super.reloadHelpPage(url, preserveLocation);
	}

//=================================================================================================
// Navigation Aid Section
//=================================================================================================

	private void prepareToCallout(URL url) {
		if (callout != null) {
			// prevent animations from lingering when moving to new pages
			callout.dispose();
			callout = null;
		}

		callout = new CalloutRequest(this, htmlEditorPane, url);
		callout.runLater();
	}

	private class HelpIDChangedListener implements HelpModelListener {
		@Override
		public void idChanged(HelpModelEvent e) {
			prepareToCallout(e.getURL());
		}
	}

}
