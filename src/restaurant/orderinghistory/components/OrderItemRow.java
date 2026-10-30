package orderinghistory.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import orderinghistory.model.HistoryItem;
import orderingpage.ui.ColorTheme;
import orderingpage.ui.Image; // the project's own picture component (rounded, "cover" scaling)

/**
 * PURPOSE: One item line inside an OrderCard: picture on the left, name and
 * doneness in the middle, quantity ("3x") and price on the right.
 * The picture comes from the dish image path saved with the order.
 */

public class OrderItemRow extends JPanel {

    private static final int IMG_W = 120, IMG_H = 88;

    public OrderItemRow(HistoryItem item) {
        setLayout(new BorderLayout(18, 0));
        setOpaque(false);

        Image picture = new Image(item.getImgSrc(), 14);
        Dimension size = new Dimension(IMG_W, IMG_H);
        picture.setPreferredSize(size);
        picture.setMinimumSize(size);
        picture.setMaximumSize(size);

        add(picture, BorderLayout.WEST);
        add(buildDetails(item), BorderLayout.CENTER);
        add(buildPrice(item), BorderLayout.EAST);
    }

    private JPanel buildDetails(HistoryItem item) {
        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(item.getName());
        name.setFont(ColorTheme.f(Font.PLAIN, 15));
        name.setForeground(ColorTheme.DARK);
        name.setAlignmentX(LEFT_ALIGNMENT);

        details.add(Box.createVerticalStrut(8));
        details.add(name);

        if (!item.getDoneness().isEmpty()) { // only steaks have a doneness
            JLabel note = new JLabel("Doneness: " + item.getDoneness());
            note.setFont(ColorTheme.f(Font.PLAIN, 12));
            note.setForeground(ColorTheme.GRAY_TXT);
            note.setAlignmentX(LEFT_ALIGNMENT);
            details.add(Box.createVerticalStrut(2));
            details.add(note);
        }
        return details;
    }

    private JPanel buildPrice(HistoryItem item) {
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JLabel qty = new JLabel(item.getQty() + "x");
        qty.setFont(ColorTheme.f(Font.PLAIN, 12.5f));
        qty.setForeground(ColorTheme.GRAY_TXT);
        qty.setAlignmentX(RIGHT_ALIGNMENT);

        JLabel price = new JLabel(Money.format(item.getPrice()));
        price.setFont(ColorTheme.f(Font.PLAIN, 13));
        price.setForeground(ColorTheme.DARK);
        price.setAlignmentX(RIGHT_ALIGNMENT);

        right.add(Box.createVerticalGlue()); // push the quantity and price to the bottom
        right.add(qty);
        right.add(price);
        return right;
    }
}
