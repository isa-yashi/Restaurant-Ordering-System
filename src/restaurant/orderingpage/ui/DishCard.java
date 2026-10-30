package orderingpage.ui;

import java.awt.*;
import javax.swing.*;
import orderingpage.cart.CartItem;
import orderingpage.cart.CartManager;
import orderingpage.dishesandmenu.Dish;
import orderingpage.dishesandmenu.Dish.Doneness;
import orderingpage.dishesandmenu.Dish.DonenessChips;

/**
 * One dish on the menu grid: picture, name, price, doneness chips (steaks
 * only),
 * quantity stepper and the "Add to cart" button.
 * It listens to the cart so it always shows the correct state.
 */
public class DishCard extends Shadow {
    private final Dish dish;
    private final CartManager cart;

    private int qty = 0;
    private Doneness doneness = Doneness.MEDIUM;
    private boolean wasInCart = false;

    private final JLabel qtyLabel = new JLabel("0", SwingConstants.CENTER);
    private final RoundBtn addBtn = new RoundBtn("Add to cart", false, 14);
    private final DonenessChips chips = new DonenessChips();

    public DishCard(Dish dish, CartManager cart) {
        super(18, Color.WHITE);
        this.dish = dish;
        this.cart = cart;

        setBorder(padding(9, 9, 9, 9));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // ---- picture: set per dish in MenuManager.seedMenu() ----
        Image img = new Image(dish.getImgSrc(), 14);
        img.setPreferredSize(new Dimension(100, 104));
        img.setMinimumSize(new Dimension(10, 104));
        img.setMaximumSize(new Dimension(Integer.MAX_VALUE, 104));
        img.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(img);
        add(Box.createVerticalStrut(9));

        // ---- name + price ----
        JPanel title = new JPanel(new BorderLayout());
        title.setOpaque(false);
        JLabel name = new JLabel(dish.getName());
        name.setFont(ColorTheme.f(Font.BOLD, 13.5f));
        JLabel price = new JLabel("$" + (int) dish.getPrice());
        price.setFont(ColorTheme.f(Font.BOLD, 13.5f));
        title.add(name, BorderLayout.WEST);
        title.add(price, BorderLayout.EAST);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        add(title);

        JLabel desc = new JLabel("<html><body style='width:150px'>" + dish.getDescription() + "</body></html>");
        desc.setFont(ColorTheme.f(Font.PLAIN, 10f));
        desc.setForeground(new Color(0x444444));
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(desc);
        add(Box.createVerticalStrut(5));

        // ---- doneness chips (only for dishes that have a doneness) ----
        JPanel chipWrap = new JPanel(new BorderLayout());
        chipWrap.setOpaque(false);
        chipWrap.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        chipWrap.add(chips);
        chipWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        chipWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        chipWrap.setVisible(dish.hasDoneness());
        add(chipWrap);

        // ---- quantity stepper ----
        // SteppingButton - > Buttons
        Buttons plus = new Buttons(true), minus = new Buttons(false);
        qtyLabel.setFont(ColorTheme.f(Font.PLAIN, 13));
        qtyLabel.setPreferredSize(new Dimension(26, 22));
        JPanel stepper = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        stepper.setOpaque(false);
        stepper.add(plus);
        stepper.add(qtyLabel);
        stepper.add(minus);
        stepper.setAlignmentX(Component.LEFT_ALIGNMENT);
        stepper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        add(stepper);
        add(Box.createVerticalStrut(6));

        addBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtn.setPreferredSize(new Dimension(100, 28));
        addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        add(addBtn);

        // ---- actions ----
        plus.addActionListener(e -> {
            if (qty >= CartManager.MAX_QTY) {
                JOptionPane.showMessageDialog(this, "You can order at most " + CartManager.MAX_QTY
                        + " of the same dish.", "Quantity limit", JOptionPane.WARNING_MESSAGE);
                return;
            }
            qty++;
            if (inCart())
                cart.setQty(dish, qty);
            else
                refreshLabels();
        });
        minus.addActionListener(e -> {
            if (qty > 0)
                qty--;
            if (inCart())
                cart.setQty(dish, qty); // 0 removes it from the cart
            else
                refreshLabels();
        });
        addBtn.addActionListener(e -> {
            if (inCart()) {
                cart.remove(dish);
                return;
            } // click again = remove
            if (qty == 0)
                qty = 1; // validation: at least one
            cart.add(dish, qty, doneness);
        });
        chips.setOnChange(() -> {
            doneness = chips.getSelected();
            if (inCart())
                cart.setDoneness(dish, doneness);
        });

        cart.addListener(this::sync);
        sync();
    }

    public Dish getDish() {
        return dish;
    }

    private boolean inCart() {
        return cart.find(dish) != null;
    }

    /** Makes the card match the cart (called every time the cart changes). */
    private void sync() {
        CartItem item = cart.find(dish);
        if (item != null) {
            qty = item.getQty();
            if (item.getDoneness() != null) {
                doneness = item.getDoneness();
                chips.setSelected(doneness);
            }
            wasInCart = true;
        } else if (wasInCart) {
            qty = 0;
            wasInCart = false;
        }
        refreshLabels();
    }

    private void refreshLabels() {
        qtyLabel.setText(String.valueOf(qty));
        boolean added = inCart();
        addBtn.setFilled(added);
        addBtn.setText(added ? "Added to cart" : "Add to cart");
        repaint();
    }
}
