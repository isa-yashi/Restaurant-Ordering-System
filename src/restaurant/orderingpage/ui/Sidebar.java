package orderingpage.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Left navigation bar. The red indicator SLIDES to whichever item the mouse
 * hovers over, and slides back to the current page when the mouse leaves.
 * Clicking an item makes it the current page and calls onNavigate(pageName).
 *
 * To add / rename pages edit the PAGES array below (App.java uses the same
 * names).
 */
public class Sidebar extends JPanel {

    // page name + icon key
    public static final String[][] PAGES = {
            { "Menu & Ordering", icons.NAV_MENU },
            { "Order Queue", icons.NAV_QUEUE },
            { "Order History", icons.NAV_HISTORY }
    };

    private final NavItem[] items = new NavItem[PAGES.length];
    private int selected = 0;
    private float indicatorY = -1, targetY;
    private final Timer anim;

    public Sidebar(Consumer<String> onNavigate) {
        setBackground(ColorTheme.SIDEBAR_BG);
        setPreferredSize(new Dimension(237, 100));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(96, 0, 0, 0));

        // ---- brand: logo + name ----
        JPanel brand = new JPanel(new BorderLayout());
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(0, 20, 0, 0));
        brand.setMaximumSize(new Dimension(Short.MAX_VALUE, 72));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel name = new JLabel("<html>STEAK<br>CREW</html>"); // <- your restaurant name
        name.setFont(new Font("Impact", Font.BOLD, 27));
        name.setBorder(new EmptyBorder(0, 14, 0, 0));
        brand.add(new Logo(), BorderLayout.WEST);
        brand.add(name, BorderLayout.CENTER);
        add(brand);
        add(Box.createVerticalStrut(52));

        // ---- animation timer (eases the indicator toward targetY) ----
        anim = new Timer(12, e -> {
            indicatorY += (targetY - indicatorY) * 0.25f;
            if (Math.abs(targetY - indicatorY) < 0.6f) {
                indicatorY = targetY;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });

        // ---- items ----
        for (int i = 0; i < PAGES.length; i++) {
            final int index = i;
            NavItem item = new NavItem(PAGES[i][0], PAGES[i][1]);
            item.setAlignmentX(Component.LEFT_ALIGNMENT);
            item.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    moveIndicatorTo(index);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    returnToSelected();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    selected = index;
                    moveIndicatorTo(index);
                    onNavigate.accept(PAGES[index][0]);
                }
            });
            items[i] = item;
            add(item);
            add(Box.createVerticalStrut(17));
        }
        add(Box.createVerticalGlue());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                returnToSelected();
            }
        });
    }

    private void moveIndicatorTo(int index) {
        targetY = items[index].getY() + 2;
        anim.start();
    }

    /**
     * When the mouse has left the whole sidebar, slide back to the current page.
     */
    private void returnToSelected() {
        if (getMousePosition(true) == null)
            moveIndicatorTo(selected);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (indicatorY < 0) {
            indicatorY = targetY = items[selected].getY() + 2;
        }
        Graphics2D g2 = ColorTheme.aa(g);
        g2.setColor(ColorTheme.RED);
        g2.fill(new RoundRectangle2D.Float(getWidth() - 12, indicatorY, 20, NavItem.HEIGHT - 4, 12, 12));
        g2.dispose();
    }
}

/** One sidebar entry (icon + text). The red indicator is drawn by Sidebar. */
class NavItem extends JComponent {
    static final int HEIGHT = 48;
    private final String text;
    private final String iconKey;

    NavItem(String text, String iconKey) {
        this.text = text;
        this.iconKey = iconKey;
        setPreferredSize(new Dimension(237, HEIGHT));
        setMaximumSize(new Dimension(Short.MAX_VALUE, HEIGHT));
        setCursor(ColorTheme.HAND);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = ColorTheme.aa(g);
        int h = getHeight();
        icons.paint(g2, iconKey, 56, (h - 17) / 2f, 17, Color.BLACK);
        g2.setColor(Color.BLACK);
        g2.setFont(ColorTheme.f(Font.PLAIN, 14));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, 101, (h - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}

/**
 * The round restaurant badge at the top-left. Set icons.LOGO to use your own
 * logo.
 */
class Logo extends JComponent {
    private static final String NAME = "Restaurant";
    private static final String TAGLINE = "Food & Drinks";

    Logo() {
        setPreferredSize(new Dimension(70, 70));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = ColorTheme.aa(g);
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Float(0, 0, 68, 68));
        if (icons.hasCustom(icons.LOGO)) {
            Shape old = g2.getClip();
            g2.setClip(new Ellipse2D.Float(0, 0, 68, 68));
            icons.paint(g2, icons.LOGO, 0, 0, 68, Color.BLACK);
            g2.setClip(old);
        } else {
            icons.paint(g2, icons.LOGO, 26, 12, 17, new Color(0x222222));
            g2.setColor(new Color(0x222222));
            g2.setFont(ColorTheme.f(Font.BOLD, 9.5f));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(NAME, (68 - fm.stringWidth(NAME)) / 2, 42);
            g2.setFont(ColorTheme.f(Font.PLAIN, 5.5f));
            fm = g2.getFontMetrics();
            g2.drawString(TAGLINE, (68 - fm.stringWidth(TAGLINE)) / 2, 49);
        }
        g2.setColor(new Color(0xCCCCCC));
        g2.draw(new Ellipse2D.Float(0, 0, 67, 67));
        g2.dispose();
    }
}
