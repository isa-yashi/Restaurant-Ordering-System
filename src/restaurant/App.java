package restaurant;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import restaurant.orderingpage.cart.CartManager;
import restaurant.orderingpage.dblogic.DataStore;
import restaurant.orderingpage.dblogic.OrderManagement;
import restaurant.orderingpage.dishesandmenu.Menu; 
import restaurant.orderingpage.ui.ColorTheme;
import restaurant.orderingpage.ui.MenuPage;
import restaurant.orderingpage.ui.PlaceholderPage;
import restaurant.orderingpage.ui.Sidebar;

/* This is the main method where you'll run the entire program*/
public class App{

    public static void main(String[] args) {
         SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}

class MainFrame extends JFrame {

    public MainFrame() {
        super("Restaurant - Menu & Ordering");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 832);
        setResizable(false);
        setLocationRelativeTo(null);

        //model / services
        Menu menu = new Menu();
        CartManager cart = new CartManager();
        OrderManagement orders = new OrderManagement();
        DataStore store = new DataStore();

        // loads saved records
        store.loadOrders(menu, orders);
        cart.restoreCart(store.loadCart(menu));
        cart.addListener(() -> store.saveCart(cart.getItems()));   // auto-save the cart on every change
        if (store.getLastError() != null)
            JOptionPane.showMessageDialog(this, store.getLastError(), "Load problem", JOptionPane.ERROR_MESSAGE);

        //pages like Menu & ordering, Order queue etc.
        CardLayout cards = new CardLayout();
        JPanel content = new JPanel(cards);
        content.add(new MenuPage(menu, cart, orders, store), Sidebar.PAGES[0][0]);
        for (int i = 1; i < Sidebar.PAGES.length; i++)         // other pages: teammates replace these
            content.add(new PlaceholderPage(Sidebar.PAGES[i][0]), Sidebar.PAGES[i][0]);

        Sidebar sidebar = new Sidebar(page -> cards.show(content, page));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(ColorTheme.PAGE_BG);
        root.add(sidebar, BorderLayout.WEST);
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);
    }
}