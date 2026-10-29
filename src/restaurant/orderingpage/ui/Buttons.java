package restaurant.orderingpage.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

public class Buttons extends JButton{
    public static final boolean showCircleBG = true;

    private final boolean plus;

    public Buttons(boolean plus){
        this.plus = plus;
        setPreferredSize(new Dimension(22, 22));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(ColorTheme.HAND);
        setFocusPainted(false);
    }

    @Override 
    protected void paintComponent(Graphics g){
        Graphics2D g2 = ColorTheme.aa(g);
        int w = getWidth(), h = getHeight();
        String key = plus ? icons.PLUS : icons.MINUS;
        g2.setColor(getModel().isPressed() ? ColorTheme.CIRCLE.darker() : ColorTheme.CIRCLE);
        g2.fill(new Ellipse2D.Float(1, 1, w - 2, h - 2));
        icons.paint(g2, key, w * .25f, h * .25f, w * .5f, ColorTheme.DARK);
        g2.dispose();
    }

}


class RoundBtn extends JButton {
    private boolean filled;
    private final int arc;
    private String iconKey;               // optional icon drawn before the text
    private float progress = 0f;          // 0 = not hovered, 1 = fully filled
    private boolean hover;
    private final Timer timer;

    public RoundBtn(String text, boolean filled, int arc) {
        super(text);
        this.filled = filled;
        this.arc = arc;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(ColorTheme.HAND);
        setFont(ColorTheme.f(Font.PLAIN, 13));

        timer = new Timer(14, e -> {                       // animation tick (~70 fps)
            progress += hover ? 0.14f : -0.14f;
            if (progress >= 1f) { progress = 1f; ((Timer) e.getSource()).stop(); }
            if (progress <= 0f) { progress = 0f; ((Timer) e.getSource()).stop(); }
            repaint();
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; timer.start(); }
            @Override public void mouseExited(MouseEvent e)  { hover = false; timer.start(); }
        });
    }

    public void setFilled(boolean filled) { this.filled = filled; repaint(); }
    public boolean isFilled() { return filled; }
    public void setIconKey(String iconKey) { this.iconKey = iconKey; }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = ColorTheme.aa(g);
        int w = getWidth(), h = getHeight();
        RoundRectangle2D shape = new RoundRectangle2D.Float(0, 0, w, h, arc, arc);
        Color fg;
        if (filled) {
            g2.setColor(ColorTheme.mix(ColorTheme.RED, new Color(0x9E221F), progress));
            g2.fill(shape);
            fg = Color.WHITE;
        } else {
            g2.setColor(Color.WHITE);
            g2.fill(shape);
            Shape old = g2.getClip();
            g2.setClip(shape);
            g2.setColor(ColorTheme.RED);
            g2.fillRect(0, 0, Math.round(w * progress), h);     // the red "fill" wipe
            g2.setClip(old);
            g2.setColor(ColorTheme.RED);
            g2.setStroke(new BasicStroke(1.6f));
            g2.draw(new RoundRectangle2D.Float(.8f, .8f, w - 1.6f, h - 1.6f, arc, arc));
            fg = ColorTheme.mix(ColorTheme.RED, Color.WHITE, progress);
        }
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(getText());
        int is = iconKey != null ? 20 : 0, gap = iconKey != null ? 10 : 0;
        int x0 = (w - (tw + is + gap)) / 2;
        if (iconKey != null) icons.paint(g2, iconKey, x0, (h - is) / 2f, is, fg);
        g2.setColor(fg);
        g2.drawString(getText(), x0 + is + gap, (h - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}