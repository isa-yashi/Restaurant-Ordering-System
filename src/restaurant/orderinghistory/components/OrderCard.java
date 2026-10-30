package orderinghistory.components;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import orderinghistory.model.HistoryOrder;
import orderingpage.ui.ColorTheme;
import orderinghistory.components.Money.RoundedPanel;
import orderinghistory.components.Money.StatusLabel;
import orderinghistory.model.HistoryItem;

/**
 * PURPOSE: One bordered card in the list = ONE finished order
 * (e.g. "Order Id: D001").
 *
 * Collapsed it shows only the first item; "View More" opens every item of the
 * order, "View Less" closes it again. Orders with one item have no toggle.
 */

public class OrderCard extends RoundedPanel {
    private static final Color OUTLINE = new Color(0x9A9A9A);
    private static final DateTimeFormatter STORED = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter SHOWN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final HistoryOrder order;
    private final JPanel itemsPanel = new JPanel();
    private final JButton viewMoreButton = new JButton();
    private boolean expanded = false;

    public OrderCard(HistoryOrder order) {
        super(18, Color.WHITE, OUTLINE);
        this.order = order;

        setLayout(new BorderLayout(0, 8));
        setBorder(new EmptyBorder(14, 20, 10, 20));

        add(buildHeader(), BorderLayout.NORTH);

        itemsPanel.setOpaque(false);
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        add(itemsPanel, BorderLayout.CENTER);

        add(buildFooter(), BorderLayout.SOUTH);

        rebuildItems();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel id = new JLabel("Order id: " + order.getID());
        id.setFont(ColorTheme.f(Font.PLAIN, 14));
        id.setForeground(ColorTheme.DARK);

        JLabel meta = new JLabel(order.getOrderType().getLabel() + " · " + prettyTime(order.getTimeStamp()));
        meta.setFont(ColorTheme.f(Font.PLAIN, 12));
        meta.setForeground(ColorTheme.GRAY_TXT);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(id);
        left.add(Box.createHorizontalStrut(14));
        left.add(meta);

        header.add(left, BorderLayout.WEST);
        header.add(new StatusLabel(order), BorderLayout.EAST);
        return header;
    }

    /** "Total items N: $X" on the right and the View More toggle in the middle. */

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout(0, 4));
        footer.setOpaque(false);

        JLabel total = new JLabel("Total items " + order.getItemCount() + ": " + Money.format(order.getTotal()));
        total.setFont(ColorTheme.f(Font.BOLD, 13.5f));
        total.setForeground(Color.BLACK);

        JPanel totalRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        totalRow.setOpaque(false);
        totalRow.add(total);
        footer.add(totalRow, BorderLayout.NORTH);

        if (order.getItemCount() > 1) { // nothing to expand for a one-item order
            viewMoreButton.setFont(ColorTheme.f(Font.PLAIN, 12));
            viewMoreButton.setForeground(ColorTheme.GRAY_TXT);
            viewMoreButton.setBorderPainted(false);
            viewMoreButton.setContentAreaFilled(false);
            viewMoreButton.setFocusPainted(false);
            viewMoreButton.setHorizontalTextPosition(SwingConstants.LEFT);
            viewMoreButton.setIconTextGap(8);
            viewMoreButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            viewMoreButton.addActionListener(e -> toggleViewMore());
            updateToggle();

            JPanel moreRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            moreRow.setOpaque(false);
            moreRow.add(viewMoreButton);
            footer.add(moreRow, BorderLayout.SOUTH);
        }
        return footer;
    }

    private void toggleViewMore() {
        expanded = !expanded;
        rebuildItems();
        updateToggle();
        revalidate();
        repaint();
    }

    private void updateToggle() {
        viewMoreButton.setText(expanded ? "View Less" : "View More");
        viewMoreButton.setIcon(new ChevronIcon(expanded));
    }

    /** Collapsed = first item only, expanded = every item of the order. */
    private void rebuildItems() {
        itemsPanel.removeAll();
        int shown = expanded ? order.getItemCount() : Math.min(1, order.getItemCount());
        int count = 0;
        for (HistoryItem item : order.getItems()) {
            if (count >= shown)
                break;
            if (count > 0)
                itemsPanel.add(Box.createVerticalStrut(14));
            itemsPanel.add(new OrderItemRow(item));
            count++;
        }
    }

    private static String prettyTime(String stored) {
        try {
            return LocalDateTime.parse(stored, STORED).format(SHOWN);
        } catch (Exception e) {
            return stored == null ? "" : stored; // unknown format: show it as it was saved
        }
    }

    /** The card keeps its own height inside the vertical list. */
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    /**
     * Small chevron drawn with two lines (no font needed, so it can never show as a
     * box).
     */
    private static class ChevronIcon implements Icon {
        private final boolean up;

        ChevronIcon(boolean up) {
            this.up = up;
        }

        @Override
        public int getIconWidth() {
            return 14;
        }

        @Override
        public int getIconHeight() {
            return 8;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = ColorTheme.aa(g);
            g2.setColor(ColorTheme.GRAY_TXT);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int edge = up ? y + 7 : y + 1;
            int tip = up ? y + 1 : y + 7;
            g2.drawLine(x + 1, edge, x + 7, tip);
            g2.drawLine(x + 7, tip, x + 13, edge);
            g2.dispose();
        }
    }

}
