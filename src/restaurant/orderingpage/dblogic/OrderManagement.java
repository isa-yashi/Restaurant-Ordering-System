package restaurant.orderingpage.dblogic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JButton;
import restaurant.orderingpage.cart.CartItem;
import restaurant.orderingpage.cart.CartManager;
import restaurant.orderingpage.dblogic.DataStructure.LinkedList;
import restaurant.orderingpage.dblogic.DataStructure.Queue;
import restaurant.orderingpage.ui.ColorTheme;

public class OrderManagement {
    private Queue<Order> queue = new Queue<>();
    private int nextNum = 1;

    public Queue<Order> getQueue() { return queue; }

    public String peekNextId() { return String.format("D%03d", nextNum); }

    public Order placeOrder(OrderType type, CartManager cart){
        if(cart.getItems().isEmpty()) throw new IllegalStateException("The cart is empty.");
        LinkedList<CartItem> ss = new LinkedList<>();
        for (CartItem i : cart.getItems())
            ss.add(new CartItem(i.getDish(), i.getQty(), i.getDoneness()));
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        Order order = new Order(peekNextId(), type, ss, cart.totalPrice(), time);
        queue.enqueue(order);
        nextNum++;
        return order;
    }

    public void restore(Queue<Order> saved, int nextNum){
        this.queue = saved;
        this.nextNum = nextNum;
    }

    public static class Order {
    
        private final String id; //sample D001. refer to the ui design.
        private final OrderType type;
        private final LinkedList<CartItem> items;
        private final double total;
        private final String timeStamp;

        public Order(String id, OrderType type, LinkedList<CartItem> items, double total, String timeStamp){
            this.id = id;
            this.type = type;
            this.items = items;
            this.total = total;
            this.timeStamp = timeStamp;
        }

        //getters
        public String getID() { return id; }
        public OrderType getOrderType() { return type; }
        public LinkedList<CartItem> getItems() { return items; }
        public double getTotal() { return total; }
        public String getTimeStamp(){ return timeStamp; }
    }

    public enum OrderType{
        DINE_IN("Dine in"),
        TAKE_AWAY("Take away"),
        DELIVERY("Delivery");

        private final String label;

        OrderType(String label) {
            this.label = label;
        }
        public String getLabel() { return label; }

        public static OrderType fromName(String label){
            for(OrderType i : values()){
                if(i.name().equalsIgnoreCase(label)) return i;
            }
            return DINE_IN;
        }
    }

    public static class OrderTypePill extends JButton {
    private final OrderType type;
    private boolean selected;

        public OrderTypePill(OrderType type) {
            super(type.getLabel());
            this.type = type;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(ColorTheme.HAND);
            setFont(ColorTheme.f(Font.PLAIN, 12));
            setPreferredSize(new Dimension(type == OrderType.DINE_IN ? 70 : 78, 28));
        }

        public OrderType getType() { return type; }
        public boolean isSelectedPill() { return selected; }
        public void setSelectedPill(boolean s) { selected = s; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = ColorTheme.aa(g);
            int w = getWidth(), h = getHeight();
            if (selected) {
                g2.setColor(new Color(0x3A3A3A));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
            } else {
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(.5f, .5f, w - 1, h - 1, h, h));
            }
            g2.setColor(selected ? Color.WHITE : Color.BLACK);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}




