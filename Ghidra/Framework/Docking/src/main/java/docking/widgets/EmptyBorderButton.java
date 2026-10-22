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
package docking.widgets;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import generic.theme.GColor;
import generic.theme.builtin.OpcodexUi;
import ghidra.docking.util.LookAndFeelUtils;
import resources.ResourceManager;

/**
 * Class that is a JButton that has an empty border and adds a mouse listener
 * so that the button looks raised when the mouse pointer enters the button,
 * and looks lowered when the mouse pointer exits the button.e
 */
public class EmptyBorderButton extends JButton {

	private ButtonStateListener emptyBorderButtonChangeListener;

	private ButtonFocusListener emptyBorderButtonFocusListener;

	/**
	 * A raised beveled border.
	 */
	public static final Border RAISED_BUTTON_BORDER = BorderFactory.createCompoundBorder(
		BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(1, 1, 1, 1));

	/**
	 * An empty border.
	 */
	public static final Border NO_BUTTON_BORDER =
		new EmptyBorder(RAISED_BUTTON_BORDER.getBorderInsets(new JButton()));

	/**
	 * A lowered border beveled border.
	 */
	public static final Border LOWERED_BUTTON_BORDER = BorderFactory.createCompoundBorder(
		BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(1, 1, 1, 1));

	/**
	 * A border to signal when the button has focus.
	 */
	private static final Color FOCUS_COLOR = new GColor("color.border.button.focused");
	public static final Border FOCUSED_BUTTON_BORDER = BorderFactory.createCompoundBorder(
		BorderFactory.createEmptyBorder(2, 2, 2, 2), BorderFactory.createLineBorder(FOCUS_COLOR));

	/**
	 * Construct a new EmptyBorderButton.
	 *
	 */
	public EmptyBorderButton() {
		super();
		init();
	}

	/**
	 * Construct a new EmptyBorderButton that has the given button text.
	 * @param text text of the button
	 */
	public EmptyBorderButton(String text) {
		super(text);
		init();
	}

	/**
	 * Construct a new EmptyBorderButton that has an associated action.
	 * @param a action for the button
	 */
	public EmptyBorderButton(Action a) {
		super(a);
		init();
	}

	/**
	 * Construct a new EmptyBorderButton that has an icon.
	 * @param icon icon for the button
	 */
	public EmptyBorderButton(Icon icon) {
		super(icon);
		init();
	}

	/**
	 * Construct a new EmptyBorderButton that has text and an icon.
	 * @param text button text
	 * @param icon icon for the button
	 */
	public EmptyBorderButton(String text, Icon icon) {
		super(text, icon);
		init();
	}

	private void init() {
		ToolTipManager.sharedInstance().registerComponent(this);
		installLookAndFeelFix();
		clearBorder();
		emptyBorderButtonChangeListener = new ButtonStateListener();
		emptyBorderButtonFocusListener = new ButtonFocusListener();

		addChangeListener(emptyBorderButtonChangeListener);
		addFocusListener(emptyBorderButtonFocusListener);
	}

	@Override
	public void setIcon(Icon newIcon) {
		// OPCODEX: retain vector painting when disabled instead of creating a blurred bitmap.
		Icon disabledIcon = OpcodexUi.active() ? OpcodexUi.disabled(newIcon) :
			ResourceManager.getDisabledIcon(newIcon);
		setDisabledIcon(disabledIcon);
		super.setIcon(newIcon);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension size = super.getPreferredSize();
		int target = Boolean.TRUE.equals(getClientProperty("opcodex.compact")) ? 24 : 32;
		return OpcodexUi.active() ? new Dimension(Math.max(target, size.width), Math.max(target, size.height)) : size;
	}

	@Override
	protected void paintBorder(Graphics g) {
		if (!OpcodexUi.active()) { super.paintBorder(g); return; }
		if (isFocusOwner()) {
			Graphics2D canvas = (Graphics2D) g.create();
			try {
				canvas.setColor(FOCUS_COLOR);
				canvas.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 7, 7);
			} finally { canvas.dispose(); }
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (OpcodexUi.active()) {
			Graphics2D canvas = (Graphics2D) g.create();
			try {
				canvas.setColor(getBackground());
				canvas.fillRect(0, 0, getWidth(), getHeight());
				if (isEnabled() && (getModel().isRollover() || getModel().isPressed() || isSelected())) {
					canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					canvas.setColor(new GColor("system.color.bg.selected.view"));
					canvas.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 7, 7);
				}
			} finally { canvas.dispose(); }
		}
		super.paintComponent(g);
	}

	@Override
	public void setBorder(Border border) {
		// To keep UI from installing an incorrect border (such as when switching themes),
		// only allow borders created by this class to be set.
		if (border == RAISED_BUTTON_BORDER || border == LOWERED_BUTTON_BORDER ||
			border == FOCUSED_BUTTON_BORDER || border == NO_BUTTON_BORDER) {
			super.setBorder(border);
		}
	}

	private void installLookAndFeelFix() {
		// We want our custom buttons to paint themselves blended with the background.  Several 
		// LookAndFeels do not do this (WinXP and Metal), so we override that behavior here.
		setContentAreaFilled(false);
		setOpaque(true);

		// Mac OSX LNF doesn't give us rollover callbacks, so we have to add a mouse listener to
		// do the work
		if (LookAndFeelUtils.isUsingAquaUI(getUI())) {
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					if (e.getButton() == MouseEvent.NOBUTTON) {
						raiseBorder();
					}
				}

				@Override
				public void mouseExited(MouseEvent e) {
					clearBorder();
				}
			});
		}
	}

	public void raiseBorder() {
		setBorder(getRaisedBorder());
	}

	public void clearBorder() {
		setBorder(NO_BUTTON_BORDER);
	}

	@Override
	public void setEnabled(boolean b) {
		setBorder(NO_BUTTON_BORDER);
		super.setEnabled(b);
	}

    // OPCODEX: apply the same pointer behavior to panel/header buttons and split controls.
    @Override public Cursor getCursor() {
        if (OpcodexUi.active()) return Cursor.getPredefinedCursor(
            isEnabled() ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR);
        return super.getCursor();
    }

	protected void updateBorderBasedOnState() {
		if (!isEnabled()) {
			return;
		}

		ButtonModel buttonModel = getModel();
		boolean pressed = buttonModel.isPressed();
		boolean rollover = buttonModel.isRollover();
		boolean armed = buttonModel.isArmed();

		if (pressed && (rollover || armed)) {
			setBorder(getLoweredBorder());
		}
		else if (rollover) {
			setBorder(getRaisedBorder());
		}
		else if (isFocusOwner()) {
			setBorder(getFocusedBorder());
		}
		else {
			setBorder(NO_BUTTON_BORDER);
		}
	}

	protected Border getFocusedBorder() {
		return FOCUSED_BUTTON_BORDER;
	}

	protected Border getRaisedBorder() {
		return RAISED_BUTTON_BORDER;
	}

	protected Border getLoweredBorder() {
		return LOWERED_BUTTON_BORDER;
	}

	public void removeListeners() {
		removeChangeListener(emptyBorderButtonChangeListener);
		removeFocusListener(emptyBorderButtonFocusListener);
	}

	private class ButtonStateListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			updateBorderBasedOnState();
		}
	}

	private class ButtonFocusListener implements FocusListener {

		@Override
		public void focusGained(FocusEvent e) {
			updateBorderBasedOnState();
		}

		@Override
		public void focusLost(FocusEvent e) {
			updateBorderBasedOnState();
		}

	}
}
