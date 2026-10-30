package orderinghistory.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import orderinghistory.ohlogic.OrderHistoryManagement.StatusFilter;
import orderingpage.ui.ColorTheme;

/**
 * PURPOSE: The pill buttons "All | Completed | Cancelled" under the title.
 * The active pill is red with white text, the others are white with a black
 * outline (same look as the chips of the Order Queue). Only one is active at a
 * time.
 * Tells the page which StatusFilter was picked.
 */

// --------------> included classes StatusFilter, Pill

public class FilterTabs extends JPanel {
    private static final Color TEXT = new Color(20, 20, 20, 20);
    private static final Pill[] pills = new Pill[3];
    private Consumer<StatusFilter> onSelect = s -> {
    };

    public FilterTabs() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        StatusFilter[] filters = { StatusFilter.ALL, StatusFilter.COMPLETED, StatusFilter.CANCELLED };
        String[] names = { "All", "Completed", "Cancelled" };

        for (int i = 0; i < filters.length; i++) {
            final int index = i;
            Pill pill = new Pill(names[i]);
            pill.addActionListener(e -> {
                select(index);
                onSelect.accept(filters[index]);
            });
            pills[i] = pill;
            add(pill);
            add(Box.createHorizontalStrut(12));
        }
        select(0); // "All" starts selected
    }

    public void setOnSelect(Consumer<StatusFilter> callback) {
        this.onSelect = callback;
    }

    private void select(int index) {
        for (int i = 0; i < pills.length; i++)
            pills[i].setActive(i == index);
    }

    /** Fully rounded button: red when active, white with outline when not. */
    private static class Pill extends JButton {
        private boolean active;

        Pill(String text) {
            super(text);
            setFont(ColorTheme.f(Font.BOLD, 13));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            Dimension size = new Dimension(104, 30);
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
        }

        void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = ColorTheme.aa(g);
            int w = getWidth(), h = getHeight();

            g2.setColor(active ? ColorTheme.RED : Color.WHITE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, h, h); // arc = height gives a perfect pill
            if (getModel().isPressed()) {
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
            }
            if (!active) {
                g2.setColor(TEXT);
                g2.setStroke(new java.awt.BasicStroke(1.3f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
            }

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(active ? Color.WHITE : TEXT);
            g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
