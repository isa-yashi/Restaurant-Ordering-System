package orderinghistory.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import orderingpage.dblogic.OrderManagement.OrderType;
import orderingpage.ui.ColorTheme;

/**
 * PURPOSE: The "Dining option" dropdown on the right (All / Dine in / Take out
 * /
 * Delivery). It works together with FilterTabs: both filters apply at the same
 * time.
 * Tells the page which OrderType was picked (null = All).
 *
 * Drawn by hand with a popup menu, like the "All status" dropdown of the Order
 * Queue.
 */

public class DiningOptionDropdown extends JComponent {

    private static final Color TEXT = new Color(20, 20, 20);
    private static final String TITLE = "Dining option";

    private final String[] labels;
    private final OrderType[] types;
    private String shown = TITLE;
    private Consumer<OrderType> onSelect = t -> {
    };

    public DiningOptionDropdown() {
        OrderType[] real = OrderType.values();
        labels = new String[real.length + 1];
        types = new OrderType[real.length + 1];
        labels[0] = "All";
        types[0] = null;

        for (int i = 0; i < real.length; i++) {
            labels[i + 1] = real[i].getLabel(); // Dine in, Takeout, Delivery
            types[i + 1] = real[i];
        }

        setFont(ColorTheme.f(Font.PLAIN, 12.5f));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Dimension size = new Dimension(160, 34);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);

        MouseListener mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showMenu();
            }
        };
        addMouseListener(mouse);
    }

    public void setOnSelect(Consumer<OrderType> onSelect) {
        this.onSelect = onSelect;
    }

    private void showMenu() {
        JPopupMenu menu = new JPopupMenu();

        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            JMenuItem item = new JMenuItem(labels[i]);
            item.setFont(getFont());
            item.addActionListener(e -> {
                shown = index == 0 ? TITLE : labels[index];
                repaint();
                onSelect.accept(types[index]);
            });
            menu.add(item);
        }
        menu.show(this, 0, getHeight()); // opens directly below the box
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Draw the box
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
        g2.setColor(TEXT);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

        // Draw the text
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(shown.equals(TITLE) ? ColorTheme.GRAY_TXT : TEXT);
        g2.drawString(shown, 14, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());

        g2.setColor(TEXT);
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int cx = getWidth() - 22, cy = getHeight() / 2;
        g2.drawLine(cx - 7, cy - 3, cx, cy + 4);
        g2.drawLine(cx, cy + 4, cx + 7, cy - 3);
        g2.dispose();
    }
}
