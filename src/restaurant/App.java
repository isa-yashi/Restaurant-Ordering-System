import java.awt.BorderLayout;
import java.awt.CardLayout;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import orderinghistory.ohui.OrderHistoryUI;
import orderingpage.cart.CartManager;
import orderingpage.dblogic.DataStore;
import orderingpage.dblogic.OrderManagement;
import orderingpage.dishesandmenu.Menu;
import orderingpage.ui.ColorTheme;
import orderingpage.ui.MenuPage;
import orderingpage.ui.PlaceholderPage;
import orderingpage.ui.Sidebar;
import orderqueue.oqUI.OrderQueueUI;
import orderinghistory.ohlogic.OrderHistoryManagement;
import orderinghistory.ohlogic.HistoryStore;

public class App {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}

class MainFrame extends JFrame {

    // Sidebar.PAGES indexes
    private static final int MENU_PAGE = 0;
    private static final int QUEUE_PAGE = 1;
    private static final int ORDER_HISTORY_PAGE = 2;

    private CardLayout cards;
    private JPanel content;

    private OrderQueueUI queuePage;
    private OrderHistoryUI historyPage;

    public MainFrame() {

        super("Restaurant - Menu & Ordering");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 832);
        setResizable(false);
        setLocationRelativeTo(null);

        // =========================================
        // 1. INITIALIZE SHARED OBJECTS
        // =========================================

        Menu menu = new Menu();
        CartManager cart = new CartManager();
        OrderManagement orders = new OrderManagement();
        DataStore store = new DataStore();

        // The Order History (a stack) and its file (data/history.txt)
        OrderHistoryManagement history = new OrderHistoryManagement();
        HistoryStore historyStore = new HistoryStore();

        // Load saved data before creating pages
        store.loadOrders(menu, orders);

        historyStore.load(history);
        // New order numbers must continue after the ones already in the history,
        // otherwise numbering would restart at D001 once the queue is empty.
        orders.reserveNumbersUpTo(history.highestOrderNumber());

        cart.restoreCart(store.loadCart(menu));

        cart.addListener(() -> store.saveCart(cart.getItems()));

        if (store.getLastError() != null) {
            JOptionPane.showMessageDialog(
                    this,
                    store.getLastError(),
                    "Load problem",
                    JOptionPane.ERROR_MESSAGE);
        }

        // =========================================
        // 2. CREATE PAGES
        // =========================================

        MenuPage menuPage = new MenuPage(menu, cart, orders, store);

        queuePage = new OrderQueueUI(orders);
        historyPage = new OrderHistoryUI(history);

        queuePage.setOnOrderFinished(order -> {
            history.record(order);
            store.saveOrders(orders.getQueue()); // the queue file no longer lists it
            historyStore.save(history); // the history file now lists it
            historyPage.refreshHistory();

            if (store.getLastError() != null || historyStore.getLastError() != null) {
                JOptionPane.showMessageDialog(
                        this,
                        store.getLastError() != null ? store.getLastError() : historyStore.getLastError(),
                        "Save problem",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // =========================================
        // 3. CARD LAYOUT
        // =========================================

        cards = new CardLayout();
        content = new JPanel(cards);

        // Register pages using the exact sidebar names
        content.add(
                menuPage,
                Sidebar.PAGES[MENU_PAGE][0]);

        content.add(
                queuePage,
                Sidebar.PAGES[QUEUE_PAGE][0]);

        content.add(
                historyPage,
                Sidebar.PAGES[ORDER_HISTORY_PAGE][0]);

        // Other pages
        for (int i = 0; i < Sidebar.PAGES.length; i++) {

            if (i == MENU_PAGE || i == QUEUE_PAGE || i == ORDER_HISTORY_PAGE) {
                continue;
            }

            content.add(
                    new PlaceholderPage(Sidebar.PAGES[i][0]),
                    Sidebar.PAGES[i][0]);
        }

        // =========================================
        // 4. SIDEBAR NAVIGATION
        // =========================================

        Sidebar sidebar = new Sidebar(page -> {

            // Show the selected page
            cards.show(content, page);

            // Refresh only when Order Queue is selected
            if (page.equals(Sidebar.PAGES[QUEUE_PAGE][0])) {
                queuePage.refreshOrders();
            }

            // Refresh only when Order History is selected
            if (page.equals(Sidebar.PAGES[ORDER_HISTORY_PAGE][0])) {
                historyPage.refreshHistory();
            }

        });

        // =========================================
        // 5. MAIN WINDOW
        // =========================================

        JPanel root = new JPanel(new BorderLayout());

        root.setBackground(ColorTheme.PAGE_BG);

        root.add(sidebar, BorderLayout.WEST);
        root.add(content, BorderLayout.CENTER);

        setContentPane(root);

        // Start at Menu & Ordering
        cards.show(content, Sidebar.PAGES[MENU_PAGE][0]);
    }
}
