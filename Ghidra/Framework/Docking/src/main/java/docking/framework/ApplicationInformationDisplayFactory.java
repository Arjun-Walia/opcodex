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
package docking.framework;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import docking.widgets.label.GIconLabel;
import docking.widgets.label.GLabel;
import generic.theme.*;
import generic.theme.GThemeDefaults.Colors.Palette;
import ghidra.framework.Application;
import ghidra.framework.PluggableServiceRegistry;
import ghidra.util.HelpLocation;
import ghidra.util.Msg;

public class ApplicationInformationDisplayFactory {

	private static final String ICON_HOME = "icon.docking.application.home";
	private static final String ICON_16 = "icon.docking.application.16";
	private static final String ICON_128 = "icon.docking.application.128";

    // OPCODEX: AWT copies image pixels into native window handles. Theme-aware GIcons alone
    // cannot refresh those copies; resubmit fresh images after the theme transaction finishes.
    private static final ThemeListener WINDOW_BRANDING = event -> {
        if (!event.isLookAndFeelChanged() && !event.hasAnyIconChanged()) return;
        SwingUtilities.invokeLater(() -> {
            List<Image> icons = getWindowIcons();
            for (Window window : Window.getWindows()) {
                // Preserve the original macOS policy of letting the OS choose frame icons.
                if (!(window instanceof Frame) || ghidra.framework.Platform.CURRENT_PLATFORM
                        .getOperatingSystem() != ghidra.framework.OperatingSystem.MAC_OS_X)
                    window.setIconImages(icons);
            }
            ghidra.docking.util.LookAndFeelUtils.performPlatformSpecificFixups();
            ToolTipManager tips = ToolTipManager.sharedInstance();
            boolean modern = generic.theme.builtin.OpcodexUi.active();
            tips.setInitialDelay(modern ? 350 : 750);
            tips.setDismissDelay(modern ? 12000 : 4000);
        });
    };
    private static ThemeManager brandingManager;

    private static synchronized void followWindowBranding() {
        // This factory is sometimes loaded before the real theme manager replaces its stub.
        ThemeManager current = ThemeManager.getInstance();
        if (!(current instanceof ApplicationThemeManager) || current == brandingManager) return;
        if (brandingManager != null) brandingManager.removeThemeListener(WINDOW_BRANDING);
        brandingManager = current;
        current.addThemeListener(WINDOW_BRANDING);
    }

	static {
		PluggableServiceRegistry.registerPluggableService(
			ApplicationInformationDisplayFactory.class, new ApplicationInformationDisplayFactory());
	}

	public static String createSplashScreenTitle() {
		ApplicationInformationDisplayFactory factory = PluggableServiceRegistry.getPluggableService(
			ApplicationInformationDisplayFactory.class);
		return factory.doCreateSplashScreenTitle();
	}

	public static String createAboutTitle() {
		ApplicationInformationDisplayFactory factory = PluggableServiceRegistry.getPluggableService(
			ApplicationInformationDisplayFactory.class);
		return factory.doCreateAboutTitle();
	}

	public static List<Image> getWindowIcons() {
        followWindowBranding();
		ApplicationInformationDisplayFactory factory = PluggableServiceRegistry.getPluggableService(
			ApplicationInformationDisplayFactory.class);
		return factory.doGetWindowIcons();
	}

	public static Image getLargestWindowIcon() {
		List<Image> windowIcons = getWindowIcons();

		Image bestImageSoFar = null;
		for (Image image : windowIcons) {
			int width = image.getWidth(null);
			if (bestImageSoFar == null) {
				bestImageSoFar = image;
			}
			else if (width > bestImageSoFar.getWidth(null)) {
				bestImageSoFar = image;
			}
		}
		return bestImageSoFar;
	}

	public static Icon getHomeIcon() {
		ApplicationInformationDisplayFactory factory = PluggableServiceRegistry.getPluggableService(
			ApplicationInformationDisplayFactory.class);
		return factory.doGetHomeIcon();
	}

	public static Runnable getHomeCallback() {
		ApplicationInformationDisplayFactory factory = PluggableServiceRegistry.getPluggableService(
			ApplicationInformationDisplayFactory.class);
		return factory.doGetHomeCallback();
	}

	public static JComponent createSplashScreenComponent() {
		ApplicationInformationDisplayFactory factory = PluggableServiceRegistry.getPluggableService(
			ApplicationInformationDisplayFactory.class);
		return factory.doCreateSplashScreenComponent();
	}

	public static JComponent createAboutComponent() {
		ApplicationInformationDisplayFactory factory = PluggableServiceRegistry.getPluggableService(
			ApplicationInformationDisplayFactory.class);
		return factory.doCreateAboutComponent();
	}

	public static HelpLocation createHelpLocation() {
		ApplicationInformationDisplayFactory factory = PluggableServiceRegistry.getPluggableService(
			ApplicationInformationDisplayFactory.class);
		return factory.doCreateHelpLocation();
	}

	protected String doCreateSplashScreenTitle() {
		return Application.getName() + " " + Application.getApplicationVersion();
	}

	protected String doCreateAboutTitle() {
		return "About " + Application.getName();
	}

	protected JComponent doCreateSplashScreenComponent() {

		final Icon icon = getSplashScreenIcon128();

		final JPanel panel = new JPanel(new BorderLayout());
		panel.setPreferredSize(new Dimension(400, 400));

		Color background = new GColor("color.bg.splashscreen");
		panel.setBackground(background);

		JLabel nameLabel = new GLabel(Application.getName());
		nameLabel.setForeground(Palette.GRAY);
		Font newFont = Gui.getFont("font.splash.header.default");
		nameLabel.setFont(newFont);
		nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		nameLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(nameLabel, BorderLayout.NORTH);

		final JPanel imagePanel = new JPanel(new BorderLayout());
		imagePanel.setBackground(background);
		JLabel imageLabel = new GIconLabel(icon);
		imageLabel.setVerticalAlignment(SwingConstants.CENTER);
		imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		imagePanel.add(imageLabel);

		panel.add(imagePanel, BorderLayout.CENTER);

		return panel;
	}

	protected Icon getSplashScreenIcon128() {
		return new GIcon(ICON_128);
	}

	protected List<Image> doGetWindowIcons() {
		List<Image> list = new ArrayList<>();
		list.add(new GIcon(ICON_128).getImageIcon().getImage());
		list.add(new GIcon(ICON_16).getImageIcon().getImage());
		return list;
	}

	protected JComponent doCreateAboutComponent() {
		return doCreateSplashScreenComponent();
	}

	protected HelpLocation doCreateHelpLocation() {
		return null;
	}

	protected Icon doGetHomeIcon() {
		return new GIcon(ICON_HOME);
	}

	protected Runnable doGetHomeCallback() {
		return () -> Msg.info(this, "Home button pressed");
	}
}
