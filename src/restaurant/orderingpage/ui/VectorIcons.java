package restaurant.orderingpage.ui;

import java.awt.*;
import java.awt.geom.*;

/**
 * The built-in drawn icons, used whenever you did NOT pick your own picture
 * in icons.java. You normally do not need to edit this file.
 */
final class VectorIcons {
    private VectorIcons() { }

    static void draw(Graphics2D g, String kind, float x, float y, float s, Color c) {
        g.setColor(c);
        g.setStroke(new BasicStroke(Math.max(1.5f, s / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (kind) {
            case icons.SEARCH:
                g.setStroke(new BasicStroke(s / 7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Ellipse2D.Float(x + s * .08f, y + s * .08f, s * .6f, s * .6f));
                g.draw(new Line2D.Float(x + s * .62f, y + s * .62f, x + s * .92f, y + s * .92f));
                break;
            case icons.FILTER:
                g.draw(new Line2D.Float(x, y + s * .3f, x + s, y + s * .3f));
                g.draw(new Line2D.Float(x, y + s * .72f, x + s, y + s * .72f));
                g.setColor(ColorTheme.RED);
                g.fill(new Ellipse2D.Float(x + s * .2f, y + s * .15f, s * .3f, s * .3f));
                g.fill(new Ellipse2D.Float(x + s * .55f, y + s * .57f, s * .3f, s * .3f));
                g.setColor(c);
                g.draw(new Ellipse2D.Float(x + s * .2f, y + s * .15f, s * .3f, s * .3f));
                g.draw(new Ellipse2D.Float(x + s * .55f, y + s * .57f, s * .3f, s * .3f));
                break;
            case icons.PLUS:
                g.setStroke(new BasicStroke(Math.max(1.6f, s / 8f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Line2D.Float(x + s * .15f, y + s * .5f, x + s * .85f, y + s * .5f));
                g.draw(new Line2D.Float(x + s * .5f, y + s * .15f, x + s * .5f, y + s * .85f));
                break;
            case icons.MINUS:
                g.setStroke(new BasicStroke(Math.max(1.6f, s / 8f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Line2D.Float(x + s * .15f, y + s * .5f, x + s * .85f, y + s * .5f));
                break;
            case icons.CART:
            case icons.NAV_CURRENT: {
                Path2D p = new Path2D.Float();
                p.moveTo(x, y + s * .1f); p.lineTo(x + s * .18f, y + s * .1f);
                p.lineTo(x + s * .32f, y + s * .62f); p.lineTo(x + s * .85f, y + s * .62f);
                g.draw(p);
                Path2D b = new Path2D.Float();
                b.moveTo(x + s * .22f, y + s * .28f); b.lineTo(x + s * .95f, y + s * .28f);
                b.lineTo(x + s * .85f, y + s * .58f); b.lineTo(x + s * .3f, y + s * .58f); b.closePath();
                g.fill(b);
                g.fill(new Ellipse2D.Float(x + s * .3f, y + s * .74f, s * .18f, s * .18f));
                g.fill(new Ellipse2D.Float(x + s * .72f, y + s * .74f, s * .18f, s * .18f));
                break; }
            case icons.TRASH: {
                g.draw(new Line2D.Float(x + s * .1f, y + s * .22f, x + s * .9f, y + s * .22f));
                g.draw(new Line2D.Float(x + s * .35f, y + s * .22f, x + s * .35f, y + s * .08f));
                g.draw(new Line2D.Float(x + s * .65f, y + s * .22f, x + s * .65f, y + s * .08f));
                g.draw(new Line2D.Float(x + s * .35f, y + s * .08f, x + s * .65f, y + s * .08f));
                Path2D p = new Path2D.Float();
                p.moveTo(x + s * .2f, y + s * .3f); p.lineTo(x + s * .26f, y + s * .92f);
                p.lineTo(x + s * .74f, y + s * .92f); p.lineTo(x + s * .8f, y + s * .3f); p.closePath();
                g.fill(p);
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(Math.max(1f, s / 14f)));
                for (float t : new float[]{.38f, .5f, .62f})
                    g.draw(new Line2D.Float(x + s * t, y + s * .42f, x + s * t, y + s * .8f));
                break; }
            case icons.NAV_MENU:
            case icons.LOGO: {
                g.draw(new Line2D.Float(x + s * .25f, y + s * .05f, x + s * .25f, y + s * .95f));
                g.draw(new Line2D.Float(x + s * .1f, y + s * .05f, x + s * .1f, y + s * .4f));
                g.draw(new Line2D.Float(x + s * .4f, y + s * .05f, x + s * .4f, y + s * .4f));
                g.draw(new Arc2D.Float(x + s * .1f, y + s * .2f, s * .3f, s * .35f, 180, 180, Arc2D.OPEN));
                g.fill(new RoundRectangle2D.Float(x + s * .7f, y + s * .05f, s * .2f, s * .5f, s * .2f, s * .2f));
                g.draw(new Line2D.Float(x + s * .8f, y + s * .4f, x + s * .8f, y + s * .95f));
                break; }
            case icons.NAV_QUEUE: {
                g.fill(new RoundRectangle2D.Float(x + s * .15f, y + s * .12f, s * .7f, s * .83f, s * .12f, s * .12f));
                g.fill(new RoundRectangle2D.Float(x + s * .33f, y, s * .34f, s * .2f, s * .08f, s * .08f));
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(Math.max(1f, s / 14f)));
                for (float t : new float[]{.42f, .6f, .78f})
                    g.draw(new Line2D.Float(x + s * .3f, y + s * t, x + s * .7f, y + s * t));
                break; }
            case icons.NAV_HISTORY: {
                g.draw(new Arc2D.Float(x + s * .1f, y + s * .1f, s * .8f, s * .8f, 60, 280, Arc2D.OPEN));
                g.draw(new Line2D.Float(x + s * .5f, y + s * .3f, x + s * .5f, y + s * .52f));
                g.draw(new Line2D.Float(x + s * .5f, y + s * .52f, x + s * .66f, y + s * .62f));
                g.fill(new Polygon(new int[]{(int) (x + s * .1f), (int) (x + s * .38f), (int) (x + s * .1f)},
                        new int[]{(int) (y + s * .15f), (int) (y + s * .4f), (int) (y + s * .5f)}, 3));
                break; }
            case icons.NAV_INVENTORY: {
                Path2D top = new Path2D.Float();
                top.moveTo(x + s * .5f, y); top.lineTo(x + s, y + s * .25f);
                top.lineTo(x + s * .5f, y + s * .5f); top.lineTo(x, y + s * .25f); top.closePath();
                g.fill(top);
                Path2D l = new Path2D.Float();
                l.moveTo(x, y + s * .32f); l.lineTo(x + s * .46f, y + s * .57f);
                l.lineTo(x + s * .46f, y + s); l.lineTo(x, y + s * .75f); l.closePath();
                g.fill(l);
                Path2D r = new Path2D.Float();
                r.moveTo(x + s, y + s * .32f); r.lineTo(x + s * .54f, y + s * .57f);
                r.lineTo(x + s * .54f, y + s); r.lineTo(x + s, y + s * .75f); r.closePath();
                g.fill(r);
                break; }
            case icons.CAT_MAIN:
                g.fill(new Arc2D.Float(x + s * .05f, y + s * .2f, s * .9f, s * .9f, 0, 180, Arc2D.PIE));
                g.fill(new RoundRectangle2D.Float(x, y + s * .72f, s, s * .1f, s * .1f, s * .1f));
                g.fill(new Ellipse2D.Float(x + s * .42f, y + s * .08f, s * .16f, s * .16f));
                break;
            case icons.CAT_DRINKS:
                g.fill(new RoundRectangle2D.Float(x + s * .38f, y, s * .24f, s * .4f, s * .1f, s * .1f));
                g.fill(new RoundRectangle2D.Float(x + s * .22f, y + s * .3f, s * .56f, s * .7f, s * .2f, s * .2f));
                break;
            case icons.CAT_DESSERT: {
                Path2D p = new Path2D.Float();
                p.moveTo(x + s * .1f, y + s * .45f); p.lineTo(x + s * .9f, y + s * .45f);
                p.lineTo(x + s * .98f, y + s * .95f); p.lineTo(x + s * .02f, y + s * .95f); p.closePath();
                g.fill(p);
                g.fill(new RoundRectangle2D.Float(x + s * .2f, y + s * .15f, s * .6f, s * .25f, s * .1f, s * .1f));
                g.fill(new Ellipse2D.Float(x + s * .44f, y, s * .12f, s * .14f));
                break; }
            case icons.CAT_SOUPSALAD:
                g.fill(new Arc2D.Float(x, y + s * .15f, s, s * .9f, 180, 180, Arc2D.CHORD));
                g.draw(new Line2D.Float(x, y + s * .6f, x + s, y + s * .6f));
                g.draw(new Line2D.Float(x + s * .3f, y + s * .5f, x + s * .3f, y));
                g.draw(new Line2D.Float(x + s * .5f, y + s * .5f, x + s * .5f, y + s * .05f));
                g.draw(new Line2D.Float(x + s * .7f, y + s * .5f, x + s * .7f, y));
                break;
            default:
                break;
        }
    }
}
