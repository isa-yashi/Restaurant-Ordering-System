package restaurant.orderingpage.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.border.EmptyBorder;

import restaurant.orderingpage.cart.CartManager;

import restaurant.orderingpage.dblogic.DataStructure.LinkedList;
import restaurant.orderingpage.dblogic.DataStore;
import restaurant.orderingpage.dblogic.OrderManagement;
import restaurant.orderingpage.dishesandmenu.Dish;
import restaurant.orderingpage.dishesandmenu.Menu.SortOption;
import restaurant.orderingpage.dishesandmenu.Menu;
import restaurant.orderingpage.ui.SearchBar;

public class MenuPage extends JPanel {
    private final Menu menu;
    private final CartManager cart;

    private final LinkedList<DishCard> cards = new LinkedList<>();
    private final JPanel grid = new JPanel(new GridLayout(0, 3, 12, 8));

    private String activeCategory = "Main Dish";
    private String searchText = "";
    private SortOption sortOption = SortOption.MENU_ORDER;

    public MenuPage(Menu menu, CartManager cart, OrderManagement orders, DataStore store) {
        super(new BorderLayout());
        this.menu = menu;
        this.cart = cart;
        setBackground(ColorTheme.PAGE_BG);

        add(buildCenter(), BorderLayout.CENTER);

        CartPanel cartPanel = new CartPanel(cart, orders, store);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(22, 4, 26, 24));
        wrap.setPreferredSize(new Dimension(336, 100));
        wrap.add(cartPanel);
        add(wrap, BorderLayout.EAST);
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(40, 26, 14, 14));

        // ---- search + filter ----
        SearchBar search = new SearchBar();
        search.setOnSearch(text -> { searchText = text; refreshGrid(); });

        RoundBtn filter = new RoundBtn("Filter", true, 14);
        filter.setIconKey(icons.FILTER);
        filter.setFont(ColorTheme.f(Font.PLAIN, 14));
        filter.setPreferredSize(new Dimension(122, 48));
        filter.addActionListener(e -> showFilterDialog());

        JPanel filterWrap = new JPanel(new BorderLayout());
        filterWrap.setOpaque(false);
        filterWrap.setBorder(new EmptyBorder(0, 0, 8, 0));
        filterWrap.add(filter);

        JPanel searchRow = new JPanel(new BorderLayout(18, 0));
        searchRow.setOpaque(false);
        searchRow.add(search, BorderLayout.CENTER);
        searchRow.add(filterWrap, BorderLayout.EAST);
        searchRow.setPreferredSize(new Dimension(100, 56));
        searchRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 56));
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ---- category pills ----
        JPanel pills = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pills.setOpaque(false);
        String[][] cats = {
                {"Main Dish", icons.CAT_MAIN}, {"Drinks", icons.CAT_DRINKS},
                {"Dessert", icons.CAT_DESSERT}, {"Soups & Salad", icons.CAT_SOUPSALAD}};
        CategoryPill[] pillBtns = new CategoryPill[cats.length];
        for (int i = 0; i < cats.length; i++) {
            final String cat = cats[i][0];
            CategoryPill p = new CategoryPill(cat, cats[i][1]);
            p.setSelectedPill(cat.equals(activeCategory));
            pillBtns[i] = p;
            p.addActionListener(e -> {
                activeCategory = cat;
                for (CategoryPill q : pillBtns) q.setSelectedPill(q == p);
                refreshGrid();
            });
            pills.add(p);
        }
        pills.setPreferredSize(new Dimension(100, 54));
        pills.setMaximumSize(new Dimension(Short.MAX_VALUE, 54));
        pills.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.add(searchRow);
        head.add(Box.createVerticalStrut(18));
        head.add(pills);
        head.add(Box.createVerticalStrut(6));
        center.add(head, BorderLayout.NORTH);

        // ---- dish grid (scrolls if there are more than two rows) ----
        grid.setOpaque(false);
        for (Dish d : menu.getAll()) cards.add(new DishCard(d, cart));
        JPanel gridHolder = new WidthTrackingPanel();
        gridHolder.setOpaque(false);
        gridHolder.add(grid, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(gridHolder, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(20);
        center.add(sp, BorderLayout.CENTER);

        refreshGrid();
        return center;
    }

    /** Searches (linear search), filters by category, sorts, then shows the cards. */
    private void refreshGrid() {
        LinkedList<Dish> visible = menu.getVisible(activeCategory, searchText, sortOption);
        grid.removeAll();
        for (Dish d : visible) {
            for (DishCard c : cards) if (c.getDish().equals(d)) { grid.add(c); break; }
        }
        if (visible.isEmpty()) {
            JLabel none = new JLabel("No dishes found.");
            none.setFont(ColorTheme.f(Font.PLAIN, 14));
            none.setForeground(ColorTheme.GRAY_TXT);
            none.setBorder(new EmptyBorder(12, 6, 0, 0));
            grid.add(none);
        }
        grid.revalidate();
        grid.repaint();
    }

    private void showFilterDialog() {
        JComboBox<SortOption> combo = new JComboBox<>(SortOption.values());
        combo.setSelectedItem(sortOption);
        int result = JOptionPane.showConfirmDialog(this, new Object[]{"Sort dishes by:", combo},
                "Filter", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            sortOption = (SortOption) combo.getSelectedItem();
            refreshGrid();
        }
    }

    /** Panel that always matches the scroll pane's width, so the 3 columns share it evenly. */
    private static class WidthTrackingPanel extends JPanel implements Scrollable {
        WidthTrackingPanel() { super(new BorderLayout()); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 20; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 120; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
}
