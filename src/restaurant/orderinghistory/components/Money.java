package orderinghistory.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JLabel;
import javax.swing.JPanel;

import orderingpage.ui.ColorTheme;
import orderinghistory.model.HistoryOrder;

// -------> included classes StatusLabel, RoundedPanel, 

/**
 * PURPOSE: One place to format prices on the Order History page. It uses the
 * same currency sign as the menu and the Order Queue ("$"), so an order looks
 * the same on every page. To switch to the peso sign, change CURRENCY to
 * "₱" (the font must be able to draw it).
 */

public class Money {
    public static final String CURRENCY = "$";

    private Money() {
    }

    public static String format(double amount) {
        return CURRENCY + String.format("%.2f", amount);
    }

    public static class StatusLabel extends JLabel {
        private static final Color GREEN = new Color(22, 163, 74);

        public StatusLabel(HistoryOrder order) {
            super(order.isCancelled() ? "Cancelled" : "Completed");
            setFont(ColorTheme.f(Font.BOLD, 13.5f));
            setForeground(order.isCancelled() ? ColorTheme.RED : GREEN);
        }
    }

    public static class RoundedPanel extends JPanel {
        private final int arc; // corner diameter
        private final Color fill;
        private final Color outline; // null = no outline

        public RoundedPanel(int arc, Color fill, Color outline) {
            this.arc = arc;
            this.fill = fill;
            this.outline = outline;
            setOpaque(false);
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2 = ColorTheme.aa(g);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            if (outline != null) {
                g2.setColor(outline);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g); // children are painted after this by Swing
        }
    }
}
