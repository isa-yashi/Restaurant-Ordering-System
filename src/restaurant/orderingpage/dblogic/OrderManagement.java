
package orderingpage.dblogic;

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
import orderingpage.cart.CartItem;
import orderingpage.cart.CartManager;
import orderingpage.dblogic.DataStructure.LinkedList;
import orderingpage.dblogic.DataStructure.Queue;
import orderingpage.ui.ColorTheme;

public class OrderManagement {
    private Queue<Order> queue = new Queue<>();
    private int nextNum = 1;

    public Queue<Order> getQueue() {
        return queue;
    }

    public String peekNextId() {
        return peekNextId(OrderType.DINE_IN);
    }

    public String peekNextId(OrderType type) {
        return String.format("%s%03d", type.getPrefix(), nextNum);
    }

    public Order placeOrder(OrderType type, CartManager cart) {
        if (cart.getItems().isEmpty())
            throw new IllegalStateException("The cart is empty.");

        LinkedList<CartItem> ss = new LinkedList<>();
        for (CartItem i : cart.getItems())
            ss.add(new CartItem(i.getDish(), i.getQty(), i.getDoneness()));

        String time = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        Order order = new Order(peekNextId(type), type, ss, cart.totalPrice(), time);
        queue.enqueue(order);
        nextNum++;
        return order;
    }

    public void restore(Queue<Order> saved, int nextNum) {
        this.queue = saved;
        this.nextNum = nextNum;
    }

    /**
     * Makes sure new order numbers continue AFTER numbers that are already used
     * somewhere else (the order history). Without this, once every queued order
     * was finished and the program restarted, numbering would start again at
     * D001 and clash with the IDs in the history.
     */
    public void reserveNumbersUpTo(int highestUsed) {
        nextNum = Math.max(nextNum, highestUsed + 1);
    }

    public boolean removeOrder(Order order) {
        return queue.remove(order);
    }

    public boolean containsOrder(Order order) {
        return queue.contains(order);
    }

    public static class Order {

        private final String id; // sample D001. refer to the ui design.
        private final OrderType type;
        private final LinkedList<CartItem> items;
        private final double total;
        private final String timeStamp;

        private String status = "Preparing";
        private String paymentStatus = "Paid";
        private String paymentMode = "Cash";

        public Order(String id, OrderType type, LinkedList<CartItem> items,
                double total, String timeStamp) {
            this.id = id;
            this.type = type;
            this.items = items;
            this.total = total;
            this.timeStamp = timeStamp;
        }

        // getters
        public String getID() {
            return id;
        }

        public OrderType getOrderType() {
            return type;
        }

        public LinkedList<CartItem> getItems() {
            return items;
        }

        public double getTotal() {
            return total;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean isCancelled() {
            return "Cancelled".equals(status);
        }

        /**
         * True for the three final statuses of the Order Queue. A finished order
         * leaves the queue and goes to the Order History.
         */
        public boolean isFinished() {
            return isCancelled() || "Served".equals(status) || "Out for delivery".equals(status);
        }

        public String getPaymentStatus() {
            return paymentStatus;
        }

        public void setPaymentStatus(String paymentStatus) {
            this.paymentStatus = paymentStatus;
        }

        public String getPaymentMode() {
            return paymentMode;
        }

        public void setPaymentMode(String paymentMode) {
            this.paymentMode = paymentMode;
        }
    }

    public enum OrderType {
        DINE_IN("Dine in", "D"),
        TAKE_AWAY("Take out", "T"),
        DELIVERY("Delivery", "DL");

        private final String label;
        private final String prefix;

        OrderType(String label, String prefix) {
            this.label = label;
            this.prefix = prefix;
        }

        public String getLabel() {
            return label;
        }

        public String getPrefix() {
            return prefix;
        }

        public static OrderType fromName(String label) {
            for (OrderType i : values()) {
                if (i.name().equalsIgnoreCase(label)
                        || i.getLabel().equalsIgnoreCase(label))
                    return i;
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

        public OrderType getType() {
            return type;
        }

        public boolean isSelectedPill() {
            return selected;
        }

        public void setSelectedPill(boolean s) {
            selected = s;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
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
            g2.drawString(getText(),
                    (w - fm.stringWidth(getText())) / 2,
                    (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
