package restaurant.orderingpage.ui;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;

public class CategoryPill extends JButton{

/** "Main Dish", "Drinks"... pill buttons. The icon comes from IconProvider (CAT_* keys). */

    private final String iconKey;
    private boolean selected;

    public CategoryPill(String text, String iconKey) {
        super(text);
        this.iconKey = iconKey;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(ColorTheme.HAND);
        setFont(ColorTheme.f(Font.PLAIN, 13));
        setPreferredSize(new Dimension(142, 46));
    }

    public void setSelectedPill(boolean s) { selected = s; repaint(); }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = ColorTheme.aa(g);
        int w = getWidth() - 5, h = getHeight() - 5;
        for (int i = 1; i <= 5; i++) {
            g2.setColor(new Color(0, 0, 0, 10));
            g2.fill(new RoundRectangle2D.Float(i * .4f, i, w, h, h, h));
        }
        g2.setColor(selected ?  ColorTheme.RED : Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
        Color fg = selected ? Color.WHITE : Color.BLACK;
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int is = 20, gap = 10, tw = fm.stringWidth(getText());
        int x0 = (w - (is + gap + tw)) / 2;
        icons.paint(g2, iconKey, x0, (h - is) / 2f, is, fg);
        g2.setColor(fg);
        g2.drawString(getText(), x0 + is + gap, (h - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }

    /* private void setOpaque(boolean b) {
        throw new UnsupportedOperationException("Not supported yet.");
    } */
}
