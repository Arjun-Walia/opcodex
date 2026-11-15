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
package docking;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import generic.theme.GColor;

public class SplitPanel extends JPanel {
	private static int DIVIDER_SIZE = 4;
	private Component leftComp;
	private Component rightComp;
	private Component divider;
	private boolean isHorizontal;
	//private int dividerLocation = Integer.MIN_VALUE;
	private float dividerPosition = 0;
	// OPCODEX: a visible grip and generous drag target without raised pane frames.
	private int dividerSize() {
		return generic.theme.builtin.OpcodexUi.active() ? 10 : DIVIDER_SIZE;
	}

	public SplitPanel(SplitNode splitNode, Component leftComp, Component rightComp,
			boolean isHorizontal) {
		setLayout(new SplitPanelLayout(isHorizontal));
		this.leftComp = leftComp;
		this.rightComp = rightComp;
		this.isHorizontal = isHorizontal;
		divider = new Divider();
		divider.setBackground(new GColor("laf.color.SplitPane.background"));
		add(leftComp);
		add(divider);
		add(rightComp);
	}

	public boolean isLeft(Component c) {
		return SwingUtilities.isDescendingFrom(c, leftComp);
	}

	float getDividerPosition() {
		return dividerPosition;
	}

	public void setDividerPosition(float newPosition) {
		dividerPosition = newPosition;
		validate();
	}

	class SplitPanelLayout implements LayoutManager {
		boolean horizontal;

		SplitPanelLayout(boolean isHorizontal) {
			this.horizontal = isHorizontal;
		}

		@Override
		public void layoutContainer(Container parent) {
			Dimension size = parent.getSize();
			if (size.width <= 0 || size.height <= 0) {
				return;
			}
			Dimension minSize1 = leftComp.getMinimumSize();
			Dimension minSize2 = rightComp.getMinimumSize();
			Insets insets = parent.getInsets();
			int width = size.width - insets.left - insets.right;
			int height = size.height - insets.top - insets.bottom;

			if (dividerPosition == 0) {
				Dimension d1 = leftComp.getPreferredSize();
				Dimension d2 = rightComp.getPreferredSize();
				Dimension d3 = leftComp.getSize();
				Dimension d4 = rightComp.getSize();
				if (horizontal) {
					if (d3.width > 0 && d4.width > 0) {
						d1 = d3;
						d2 = d4;
					}
					int prefWidth = d1.width + d2.width;
					dividerPosition = (float) d1.width / (float) prefWidth;
				}
				else {
					if (d3.height > 0 && d4.height > 0) {
						d1 = d3;
						d2 = d4;
					}
					int prefHeight = d1.height + d2.height;
					dividerPosition = (float) d1.height / (float) prefHeight;
				}
			}

			if (horizontal) {
				width -= dividerSize();
				int minWidth = minSize1.width + minSize2.width;
				int dividerPixelPosition = Math.round(width * dividerPosition);

				if (width <= minWidth) {
					dividerPosition = (float) minSize1.width / (float) minWidth;
				}
				else if (dividerPixelPosition <= minSize1.width) {
					dividerPosition = (float) minSize1.width / (float) width;
				}
				else if (dividerPixelPosition >= width - minSize2.width) {
					dividerPosition = (float) (width - minSize2.width) / (float) width;
				}

				dividerPixelPosition = Math.round(width * dividerPosition);
				leftComp.setBounds(insets.left, insets.top, dividerPixelPosition, height);
				divider.setBounds(insets.left + dividerPixelPosition, insets.top, dividerSize(),
					height);
				rightComp.setBounds(insets.left + dividerPixelPosition + dividerSize(), insets.top,
					width - dividerPixelPosition, height);
			}
			else {
				height -= dividerSize();
				int minHeight = minSize1.height + minSize2.height;
				int dividerPixelPosition = Math.round(height * dividerPosition);

				if (height <= minHeight) {
					dividerPosition = (float) minSize1.height / (float) minHeight;
				}
				else if (dividerPixelPosition <= minSize1.height) {
					dividerPosition = (float) minSize1.height / (float) height;
				}
				else if (dividerPixelPosition >= height - minSize2.height) {
					dividerPosition = (float) (height - minSize2.height) / (float) height;
				}

				dividerPixelPosition = Math.round(height * dividerPosition);

				leftComp.setBounds(insets.left, insets.top, width, dividerPixelPosition);
				divider.setBounds(insets.left, insets.top + dividerPixelPosition, width,
					dividerSize());
				rightComp.setBounds(insets.left, dividerPixelPosition + dividerSize() + insets.top,
					width, height - dividerPixelPosition);

			}
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			Dimension d1 = leftComp.getMinimumSize();
			Dimension d2 = rightComp.getMinimumSize();
			Insets insets = parent.getInsets();
			if (horizontal) {
				return new Dimension(
					d1.width + d2.width + dividerSize() + insets.left + insets.right,
					Math.max(d1.height, d2.height) + insets.top + insets.bottom);
			}
			return new Dimension(Math.max(d1.width, d2.width) + insets.left + insets.right,
				d1.height + d2.height + dividerSize() + insets.top + insets.bottom);
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			Dimension d1 = leftComp.getPreferredSize();
			Dimension d2 = rightComp.getPreferredSize();
			Insets insets = parent.getInsets();
			if (horizontal) {
				return new Dimension(
					d1.width + d2.width + dividerSize() + insets.left + insets.right,
					Math.max(d1.height, d2.height) + insets.top + insets.bottom);
			}
			return new Dimension(Math.max(d1.width, d2.width) + insets.left + insets.right,
				d1.height + d2.height + dividerSize() + insets.top + insets.bottom);
		}

		@Override
		public void addLayoutComponent(String name, Component comp) {
		}

		@Override
		public void removeLayoutComponent(Component comp) {
		}
	}

	class Divider extends JPanel {
		private boolean hovered;
		Divider() {
			setToolTipText("Drag to resize panels; double-click for equal sizes");
			getAccessibleContext().setAccessibleName("Resize panels");
			addMouseListener(new java.awt.event.MouseAdapter() {
				@Override public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true; repaint(); }
				@Override public void mouseExited(java.awt.event.MouseEvent e) { hovered = false; repaint(); }
				@Override public void mouseClicked(java.awt.event.MouseEvent e) {
					if (generic.theme.builtin.OpcodexUi.active() && e.getClickCount() == 2) {
						dividerPosition = .5f;
						SplitPanel.this.doLayout();
						SplitPanel.this.repaint();
					}
				}
			});
			if (isHorizontal) {
				setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
			}
			else {
				setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
			}

			addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseDragged(MouseEvent e) {
					if (isHorizontal) {
						dividerPosition += (float) e.getX() / (float) SplitPanel.this.getWidth();
					}
					else {
						dividerPosition += (float) e.getY() / (float) SplitPanel.this.getHeight();
					}

					SplitPanel.this.doLayout();
					SplitPanel.this.validate();
				}
			});
		}

		@Override protected void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);
			if (generic.theme.builtin.OpcodexUi.active()) {
				Graphics2D g = (Graphics2D) graphics.create();
				try {
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					int x = getWidth() / 2, y = getHeight() / 2;
					g.setColor(new GColor("system.color.bg.border"));
					if (isHorizontal) {
						g.drawLine(x, 0, x, getHeight());
						g.fillRoundRect(x - 4, y - 18, 8, 36, 8, 8);
					}
					else {
						g.drawLine(0, y, getWidth(), y);
						g.fillRoundRect(x - 18, y - 4, 36, 8, 8, 8);
					}
					g.setColor(new GColor(hovered ? "color.border.button.focused" : "system.color.fg.disabled"));
					for (int offset : new int[] {-6, 0, 6}) {
						g.fillOval(x - 1 + (isHorizontal ? 0 : offset),
							y - 1 + (isHorizontal ? offset : 0), 2, 2);
					}
				}
				finally { g.dispose(); }
			}
		}

	}
}
