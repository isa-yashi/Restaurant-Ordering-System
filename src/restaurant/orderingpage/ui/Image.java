package restaurant.orderingpage.ui;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;
import javax.imageio.*;
import javax.swing.JComponent;

public class Image extends JComponent{
    private final BufferedImage img;
    private final int arc;
    private final String key;

    public Image(String src, int arc){
        this.arc = arc;
        this.key = src == null ? "none" : src;
        this.img = ImgLoader.load(src);
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = ColorTheme.aa(g);
        int w = getWidth(), h = getHeight();
        g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
        if (img != null) {                         // "cover" scaling
            double sc = Math.max((double) w / img.getWidth(), (double) h / img.getHeight());
            int dw = (int) (img.getWidth() * sc), dh = (int) (img.getHeight() * sc);
            g2.drawImage(img, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
        } else {
            placeholder(g2, w, h);
        }
        g2.dispose();
    }

    private void placeholder(Graphics2D g2, int w, int h) {
        Random r = new Random(key.hashCode());
        g2.setPaint(new GradientPaint(0, 0, new Color(38, 30, 26), w, h, new Color(78, 58, 44)));
        g2.fillRect(0, 0, w, h);
        float pd = Math.min(w, h) * .86f;
        float px = w * .5f - pd / 2, py = h * .56f - pd / 2;
        g2.setColor(new Color(215, 212, 205));
        g2.fill(new Ellipse2D.Float(px, py, pd, pd));
        g2.setColor(new Color(235, 232, 226));
        g2.fill(new Ellipse2D.Float(px + pd * .06f, py + pd * .06f, pd * .88f, pd * .88f));
        g2.setColor(new Color(240, 230, 200));
        g2.fill(new Ellipse2D.Float(px + pd * .1f, py + pd * .4f, pd * .3f, pd * .28f));
        g2.setColor(new Color(225, 120, 30));
        for (int i = 0; i < 3; i++)
            g2.fill(new RoundRectangle2D.Float(px + pd * (.12f + i * .1f), py + pd * .72f, pd * .07f, pd * .2f, 8, 8));
        g2.setColor(new Color(70, 120, 50));
        for (int i = 0; i < 4; i++)
            g2.fill(new Ellipse2D.Float(px + pd * (.1f + r.nextFloat() * .25f), py + pd * (.15f + r.nextFloat() * .2f), pd * .09f, pd * .09f));
        g2.setColor(new Color(92, 42, 30));
        g2.fill(new RoundRectangle2D.Float(px + pd * .42f, py + pd * .22f, pd * .5f, pd * .5f, pd * .18f, pd * .18f));
        g2.setColor(new Color(176, 76, 66));
        g2.fill(new RoundRectangle2D.Float(px + pd * .48f, py + pd * .3f, pd * .38f, pd * .3f, pd * .12f, pd * .12f));
        g2.setColor(new Color(40, 22, 16));
        g2.setStroke(new BasicStroke(2f));
        for (int i = 0; i < 3; i++)
            g2.draw(new Line2D.Float(px + pd * (.5f + i * .12f), py + pd * .26f, px + pd * (.58f + i * .12f), py + pd * .68f));
    }
}

//this is only for the different types ng pag upload ko ng image kaya gumamit ng try catch.
class ImgLoader{
    private ImgLoader(){}

    private static final Map<String, BufferedImage> cache = new HashMap<>();

    public static BufferedImage load(String src){
        if(src == null || src.trim().isEmpty()) return null;
        if(cache.containsKey(src)) return cache.get(src);
        BufferedImage img = null;

        try {
            if(src.startsWith("data:image")){
                String base64 = src.substring(src.indexOf(',')  + 1);
                img = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64)));
            } else if(src.startsWith("https://") || src.startsWith("http://")){
                img = ImageIO.read(new URL(src));
            } else {
                File f = new File(src);
                if(f.exists()) { img = ImageIO.read(f);}
                else {
                    URL url = ImgLoader.class.getResource("/" + src);
                    if(url != null) img = ImageIO.read(url);
                }
            }
        } catch (IOException ignoredException) { }
        cache.put(src, img);
        return img;
    }
}
