package restaurant.orderingpage.ui;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * The search box. Validates the text (letters, numbers, spaces, & . ' - ; max 30)
 * and shows a red outline + tooltip when the text is not allowed.
 * The icon comes from IconProvider.SEARCH.
 */

public class SearchBar extends Shadow {
    private static final Pattern ALLOWED = Pattern.compile("[\\p{L}\\p{N} &.'-]{0,30}");

    private final JTextField field = new JTextField();
    private boolean invalid;
    private Consumer<String> onSearch;

    public SearchBar() {
        super(60, Color.WHITE);                 // white rounded box with a shadow
        setBorder(padding(0, 62, 0, 20));
        setLayout(new BorderLayout());
        field.setOpaque(false);
        field.setBorder(null);
        field.setFont(ColorTheme.f(Font.PLAIN, 13.5f));
        add(field, BorderLayout.CENTER);

        field.getDocument().addDocumentListener(new DocumentListener() {
            private void changed() {
                String text = field.getText();
                invalid = !ALLOWED.matcher(text).matches();
                setToolTipText(invalid ? "Use letters, numbers and spaces only (max 30 characters)." : null);
                if (!invalid && onSearch != null) onSearch.accept(text.trim());
                repaint();
            }
            @Override public void insertUpdate(DocumentEvent e) { changed(); }
            @Override public void removeUpdate(DocumentEvent e) { changed(); }
            @Override public void changedUpdate(DocumentEvent e) { changed(); }
        });
    }

    public void setOnSearch(Consumer<String> c) { onSearch = c; }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = ColorTheme.aa(g);
        int w = getWidth() - sh, h = getHeight() - sh;   // sh = shadow size from Shadow
        icons.paint(g2, icons.SEARCH, 28, (h - 20) / 2f, 20, Color.BLACK);
        if (field.getText().isEmpty()) {
            g2.setColor(new Color(0x555555));
            g2.setFont(ColorTheme.f(Font.PLAIN, 13.5f));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("Search...", 62, (h - fm.getHeight()) / 2 + fm.getAscent());
        }
        if (invalid) {
            g2.setColor(ColorTheme.RED);
            g2.setStroke(new BasicStroke(1.6f));
            g2.draw(new RoundRectangle2D.Float(.8f, .8f, w - 1.6f, h - 1.6f, h, h));
        }
        g2.dispose();
    }
}