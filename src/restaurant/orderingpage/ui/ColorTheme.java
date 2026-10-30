package orderingpage.ui;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class ColorTheme {
    private ColorTheme() {
    }

    public static final Color RED = new Color(0xD0312D);
    public static final Color PAGE_BG = new Color(0xD9D9D9);
    public static final Color SIDEBAR_BG = new Color(0xF9F9F9);
    public static final Color GRAY_TXT = new Color(0x8A8A8A);
    public static final Color CIRCLE = new Color(0xCCCCCC);
    public static final Color DARK = new Color(0x333333);

    public static final Cursor HAND = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    public static Font f(int style, float size) {
        return new Font("Poppins", style, 12).deriveFont(size);
    }

    public static Graphics2D aa(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        return g2;
    }

    // blending 2 colors
    public static Color mix(Color a, Color b, float t) {
        return new Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

}

class Shadow extends JPanel {
    public static final int sh = 6; // shadow thickness right and bottom
    private final int arc;
    private final Color fillColor;

    public Shadow(int arc, Color fillColor) {
        this.arc = arc;
        this.fillColor = fillColor;
        setOpaque(false);
    }

    public EmptyBorder padding(int top, int left, int bottom, int right) {
        return new EmptyBorder(top, left, bottom + sh, right + sh);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = ColorTheme.aa(g);
        int w = getWidth() - sh, h = getHeight() - sh;
        for (int i = 1; i <= sh; i++) {
            g2.setColor(new Color(0, 0, 0, 9));
            g2.fill(new RoundRectangle2D.Float(i * .5f, i, w, h, arc, arc));
        }
        g2.setColor(fillColor);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
        g2.dispose();
    }
}
