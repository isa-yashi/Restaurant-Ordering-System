package orderingpage.dishesandmenu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;
import orderingpage.ui.ColorTheme;

public class Dish {
    public enum Doneness {
        RARE("Rare"), MEDIUM("Medium"), WELL_DONE("Well Done");

        private final String label;

        Doneness(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        /**
         * Returns the Doneness enum matching the given label (case-insensitive).
         * If no match is found, returns MEDIUM as the default value.
         *
         * @param label the doneness label to match
         * @return the corresponding Doneness, or MEDIUM if no match is found
         */
        public static Doneness fromString(String label) {
            for (Doneness d : values()) {
                if (d.label.equalsIgnoreCase(label) || d.name().equalsIgnoreCase(label))
                    return d;
            }
            return MEDIUM; // default value if no match is found.
        }
    }

    public static class DonenessChips extends JComponent {
        private final Doneness[] options = Doneness.values();
        private Doneness selected = Doneness.MEDIUM;
        private int hoverIndex = -1;
        private Runnable onChange;

        public DonenessChips() {
            setPreferredSize(new Dimension(100, 22));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
            setCursor(ColorTheme.HAND);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int i = indexAt(e.getX());
                    if (i >= 0 && options[i] != selected) {
                        selected = options[i];
                        repaint();
                        if (onChange != null)
                            onChange.run();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hoverIndex = -1;
                    repaint();
                }
            });
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    hoverIndex = indexAt(e.getX());
                    repaint();
                }
            });
        }

        public void setOnChange(Runnable r) {
            onChange = r;
        }

        public Doneness getSelected() {
            return selected;
        }

        public void setSelected(Doneness d) {
            if (d != null) {
                selected = d;
                repaint();
            }
        }

        private int indexAt(int x) {
            float cw = getWidth() / (float) options.length;
            int i = (int) (x / cw);
            return i >= 0 && i < options.length ? i : -1;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = ColorTheme.aa(g);
            int n = options.length, gap = 5;
            float cw = (getWidth() - gap * (n - 1)) / (float) n;
            int h = getHeight();
            g2.setFont(ColorTheme.f(Font.PLAIN, 9.5f));
            FontMetrics fm = g2.getFontMetrics();
            for (int i = 0; i < n; i++) {
                float x = i * (cw + gap);
                boolean sel = options[i] == selected;
                g2.setColor(sel ? ColorTheme.RED : (i == hoverIndex ? new Color(0xE2E2E2) : new Color(0xF0F0F0)));
                g2.fill(new RoundRectangle2D.Float(x, 0, cw, h, h, h));
                g2.setColor(sel ? Color.WHITE : ColorTheme.DARK);
                String t = options[i].getLabel();
                g2.drawString(t, x + (cw - fm.stringWidth(t)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }

    private final String id, name, categ, description, imgSrc;
    private final double price;

    // constructor
    public Dish(String id, String name, double price, String categ, String description, String imgSrc) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.categ = categ;
        this.description = description;
        this.imgSrc = imgSrc;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return categ;
    }

    public String getDescription() {
        return description;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public boolean hasDoneness() {
        return false;
    }

    // Overrides methods for equality and hashcode based on the unique id of the
    // dish
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Dish && ((Dish) obj).id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

}

class SteakDish extends Dish {

    public SteakDish(String id, String name, double price, String description, String imageSource) {
        super(id, name, price, "Main Dish", description, imageSource);
    }

    @Override
    public boolean hasDoneness() {
        return true;
    }

    public Doneness getDefaultDoneness() {
        return Doneness.MEDIUM;
    }
}
