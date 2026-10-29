package restaurant.orderingpage.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

public class icons {
    public static class iconBtn extends  JButton{
    private final String iconKey;
        public iconBtn(String iconKey, int size){
            this.iconKey = iconKey;
            setPreferredSize(new Dimension(size, size));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(ColorTheme.HAND);
        }

        @Override
        protected void paintComponent(Graphics g){
            Graphics2D g2 = ColorTheme.aa(g);
            icons.paint(g2, iconKey, 2, 2, getWidth() - 4, Color.black);
            g2.dispose();
        }
    }

    private icons(){}

    public static final String SEARCH = "search";
    public static final String FILTER = "filter";
    public static final String NAV_MENU = "nav_menu";
    public static final String NAV_CURRENT = "nav_current";
    public static final String LOGO = "logo";
    public static final String NAV_QUEUE = "nav_queue";
    public static final String NAV_HISTORY = "nav_history";
    public static final String NAV_INVENTORY = "nav_inventory";
    public static final String CAT_MAIN = "nav_main";
    public static final String CAT_DRINKS = "nav_drinks";
    public static final String CAT_DESSERT = "nav_desserts";
    public static final String CAT_SOUPSALAD = "nav_soupsalad";
    public static final String CART = "cart";
    public static final String TRASH = "trash";
    public static final String PLUS = "plus";
    public static final String MINUS = "minus";
    
    /* Organizing it bruh */
    //6-7 search function;
    //8-11 sidebar navigation
    //12-15 category of foods
    //16-19 quantity and cart card on the right side of UI. refer sa design brochacho.

    private static final Map<String, String> SOURCES = new HashMap<>();
    private static void use(String icon, String src) { SOURCES.put(icon, src); }

    //basta sa icon to pag upload whether link, embedded code, image, etc.
    static {
        use(SEARCH, null);              //later ko na ayusin litchi sakit ng pwet ko
        use(FILTER, null);
        use(NAV_MENU, null);
        use(NAV_CURRENT, null);
        use(LOGO, null);
        use(NAV_QUEUE, null);
        use(NAV_HISTORY, null);
        use(NAV_INVENTORY, null);
        use(CAT_MAIN, null);
        use(CAT_DRINKS, null);
        use(CAT_DESSERT, null);
        use(CAT_SOUPSALAD, null);
        use(CART, null);
        use(TRASH, null);
        use(PLUS, null);
        use(MINUS, null);

    }

    public static void paint(Graphics2D g, String icon, float x, float y, float size, Color tint) {
        BufferedImage img = ImgLoader.load(SOURCES.get(icon));
        //no picture set for this icon -> draw the built-in icon instead (prevents a NullPointerException)
        if (img == null) { VectorIcons.draw(g, icon, x, y, size, tint); return; }
        double sc = Math.min(size / img.getWidth(), size / img.getHeight());
        int w = (int) (img.getWidth() * sc), h = (int) (img.getHeight() * sc);
        g.drawImage(img, (int) (x + (size - w) / 2), (int) (y + (size - h) / 2), w, h, null);
    }

    public static boolean hasCustom(String icon) {
        return ImgLoader.load(SOURCES.get(icon)) != null;
    }
}


