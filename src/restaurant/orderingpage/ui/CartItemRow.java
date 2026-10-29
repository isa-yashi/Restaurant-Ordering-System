package restaurant.orderingpage.ui;


import java.awt.*;
import javax.swing.*;
import restaurant.orderingpage.cart.CartItem;
import restaurant.orderingpage.cart.CartManager;
import restaurant.orderingpage.dblogic.DataStore;
import restaurant.orderingpage.dblogic.OrderManagement;
import restaurant.orderingpage.dblogic.OrderManagement.Order;
import restaurant.orderingpage.dblogic.OrderManagement.OrderType;
import restaurant.orderingpage.dblogic.OrderManagement.OrderTypePill;
import restaurant.orderingpage.dishesandmenu.Dish;
import restaurant.orderingpage.ui.icons.iconBtn;

public class CartItemRow extends JPanel{
    public CartItemRow(CartItem item, CartManager cart){
        super(new BorderLayout(10,10));
        Dish dish = item.getDish();
        setOpaque(false);
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 128));
        setPreferredSize(new Dimension(100, 128));

        Image img = new Image(dish.getImgSrc(), 16);
        img.setPreferredSize(new Dimension(100, 128));
        add(img, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel name = new JLabel("<html><body style='width:105px'>" + dish.getName() + "</body></html>");
        name.setFont(ColorTheme.f(Font.BOLD, 13.5f));
        JLabel note = new JLabel(item.getDoneness() != null ? item.getDoneness().getLabel() : " ");
        note.setFont(ColorTheme.f(Font.PLAIN, 9.5f));
        note.setForeground(ColorTheme.GRAY_TXT);
        JLabel x = new JLabel("x" + item.getQty());
        x.setFont(ColorTheme.f(Font.PLAIN, 9.5f));
        x.setForeground(ColorTheme.GRAY_TXT);
        JLabel price = new JLabel(String.format("$%.2f", item.getLineTotal()));
        price.setFont(ColorTheme.f(Font.BOLD, 10.5f));

        Buttons plus = new Buttons(true), minus = new Buttons(false);
        JLabel q = new JLabel(String.valueOf(item.getQty()), SwingConstants.CENTER);
        q.setFont(ColorTheme.f(Font.PLAIN, 13));
        q.setPreferredSize(new Dimension(24, 22));
        iconBtn trash = new iconBtn(icons.TRASH, 22);

        JPanel steps = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        steps.setOpaque(false);
        steps.add(plus);
        steps.add(q);
        steps.add(minus);
        JPanel stepRow = new JPanel(new BorderLayout());
        stepRow.setOpaque(false);
        stepRow.add(steps, BorderLayout.WEST);
        stepRow.add(trash, BorderLayout.EAST);
        stepRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        for (JComponent c : new JComponent[]{name, note, x, price, stepRow}) c.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(name);
        info.add(note);
        info.add(x);
        info.add(Box.createVerticalStrut(2));
        info.add(price);
        info.add(Box.createVerticalGlue());
        info.add(stepRow);
        add(info, BorderLayout.CENTER);

        plus.addActionListener(e -> {
            if (item.getQty() >= CartManager.MAX_QTY) {
                JOptionPane.showMessageDialog(this, "You can order at most " + CartManager.MAX_QTY
                        + " of the same dish.", "Quantity limit", JOptionPane.WARNING_MESSAGE);
                return;
            }
            cart.setQty(dish, item.getQty() + 1);
        });
        minus.addActionListener(e -> { if (item.getQty() > 1) cart.setQty(dish, item.getQty() - 1); });
        trash.addActionListener(e -> cart.remove(dish));
    }
}

class CartPanel extends Shadow {
    private final CartManager cart;
    private final OrderManagement orders;
    private final DataStore store;

    private final JPanel list = new JPanel();
    private final JLabel itemsVal = new JLabel(), discVal = new JLabel("$0.00"), totalVal = new JLabel();
    private final JLabel orderLbl = new JLabel();
    private final RoundBtn undoBtn = new RoundBtn("Undo remove", false, 20);
    
    private final OrderTypePill[] pills = {
            new OrderTypePill(OrderType.DINE_IN), 
            new OrderTypePill(OrderType.TAKE_AWAY), 
            new OrderTypePill(OrderType.DELIVERY)
        };

    public CartPanel(CartManager cart, OrderManagement orders, DataStore store) {
        super(40, Color.WHITE);
        this.cart = cart;
        this.orders = orders;
        this.store = store;
        setBorder(padding(18, 14, 20, 14));
        setLayout(new BorderLayout(0, 12));

        // ---- header ----
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        iconBtn cartIcon = new iconBtn(icons.CART, 26);   // icon: IconProvider.CART
        cartIcon.setCursor(Cursor.getDefaultCursor());
        JLabel title = new JLabel("Cart");
        title.setFont(ColorTheme.f(Font.PLAIN, 23));
        left.add(cartIcon);
        left.add(title);
        orderLbl.setFont(ColorTheme.f(Font.PLAIN, 12.5f));
        header.add(left, BorderLayout.WEST);
        header.add(orderLbl, BorderLayout.EAST);

        JPanel pillRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pillRow.setOpaque(false);
        for (OrderTypePill p : pills) {
            pillRow.add(p);
            p.addActionListener(e -> { for (OrderTypePill q : pills) q.setSelectedPill(q == p); });
        }
        pills[0].setSelectedPill(true);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        pillRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(header);
        top.add(Box.createVerticalStrut(14));
        top.add(pillRow);
        add(top, BorderLayout.NORTH);

        // ---- item list ----
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(list, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(holder, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        add(sp, BorderLayout.CENTER);

        // ---- summary + place order ----
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        undoBtn.setFont(ColorTheme.f(Font.PLAIN, 11));
        undoBtn.setPreferredSize(new Dimension(110, 26));
        undoBtn.setMaximumSize(new Dimension(110, 26));
        undoBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        undoBtn.addActionListener(e -> cart.undo());
        bottom.add(undoBtn);
        bottom.add(Box.createVerticalStrut(8));

        bottom.add(summaryRow("Items", itemsVal, false));
        bottom.add(Box.createVerticalStrut(2));
        bottom.add(summaryRow("Discounts", discVal, false));
        bottom.add(Box.createVerticalStrut(10));
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x666666));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        bottom.add(sep);
        bottom.add(Box.createVerticalStrut(10));
        bottom.add(summaryRow("Total", totalVal, true));
        bottom.add(Box.createVerticalStrut(18));

        RoundBtn place = new RoundBtn("Place an order", true, 60);
        place.setFont(ColorTheme.f(Font.PLAIN, 16));
        place.setPreferredSize(new Dimension(100, 52));
        place.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        place.setAlignmentX(Component.LEFT_ALIGNMENT);
        place.addActionListener(e -> placeOrder());
        bottom.add(place);
        add(bottom, BorderLayout.SOUTH);

        cart.addListener(this::rebuild);
        rebuild();
    }

    private JPanel summaryRow(String label, JLabel value, boolean total) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(ColorTheme.f(Font.PLAIN, 10.5f));
        l.setForeground(ColorTheme.GRAY_TXT);
        value.setFont(ColorTheme.f(Font.BOLD, 10.5f));
        if (total) value.setForeground(ColorTheme.RED);
        p.add(l, BorderLayout.WEST);
        p.add(value, BorderLayout.EAST);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    /** Redraws the whole cart from the CartManager. */
    private void rebuild() {
        list.removeAll();
        boolean first = true;
        for (CartItem item : cart.getItems()) {
            if (!first) {
                JSeparator s = new JSeparator();
                s.setForeground(new Color(0x777777));
                s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
                list.add(Box.createVerticalStrut(10));
                list.add(s);
                list.add(Box.createVerticalStrut(14));
            }
            first = false;
            list.add(new CartItemRow(item, cart));
        }
        itemsVal.setText(String.valueOf(cart.totalItems()));
        totalVal.setText(String.format("$%.2f", cart.totalPrice()));
        orderLbl.setText("Order: " + orders.peekNextId());
        undoBtn.setVisible(cart.canUndoRemoved());
        list.revalidate();
        list.repaint();
    }

    private void placeOrder() {
        if (cart.getItems().isEmpty()) {                       // input validation
            JOptionPane.showMessageDialog(this, "Your cart is empty. Please add at least one dish.",
                    "Cannot place order", JOptionPane.WARNING_MESSAGE);
            return;
        }
        OrderType type = OrderType.DINE_IN;
        for (OrderTypePill p : pills) if (p.isSelectedPill()) type = p.getType();

        Order order = orders.placeOrder(type, cart);            // goes to the back of the Queue
        store.saveOrders(orders.getQueue());                  // persist
        cart.clear();                                         // also saves the (empty) cart via listener

        String msg = String.format("Order %s (%s) placed!%nTotal: $%.2f%nOrders waiting in queue: %d",
                order.getID(), order.getOrderType().getLabel(), order.getTotal(), orders.getQueue().size());
        JOptionPane.showMessageDialog(this, msg, "Order placed", JOptionPane.INFORMATION_MESSAGE);
        if (store.getLastError() != null)
            JOptionPane.showMessageDialog(this, store.getLastError(), "Save problem", JOptionPane.ERROR_MESSAGE);
    }
}