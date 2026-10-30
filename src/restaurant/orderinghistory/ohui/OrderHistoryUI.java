
package orderinghistory.ohui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Rectangle;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import orderingpage.dblogic.DataStructure.LinkedList;
import orderingpage.dblogic.OrderManagement.OrderType;
import orderingpage.ui.ColorTheme;
import orderingpage.ui.SearchBar;

import orderinghistory.ohlogic.OrderHistoryManagement;
import orderinghistory.ohlogic.OrderHistoryManagement.StatusFilter;
import orderinghistory.components.DiningOptionDropdown;
import orderinghistory.components.FilterTabs;
import orderinghistory.components.Money.RoundedPanel;
import orderinghistory.components.OrderCard;
import orderinghistory.model.HistoryOrder;


public class OrderHistoryUI extends JPanel {

    private static final Color TEXT = new Color(20, 20, 20);

    private final OrderHistoryManagement history;

    private final ListPanel listPanel = new ListPanel();
    private final JScrollPane scroll = new JScrollPane(listPanel);

    // Current selections of the three controls
    private StatusFilter statusFilter = StatusFilter.ALL;
    private OrderType typeFilter = null;
    private String searchText = "";

    public OrderHistoryUI(OrderHistoryManagement history) {
        this.history = history;

        setLayout(new BorderLayout());
        setBackground(ColorTheme.PAGE_BG);
        setBorder(new EmptyBorder(25, 31, 30, 23));

        RoundedPanel card = new RoundedPanel(30, Color.WHITE, null);

        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        card.add(buildHeader(), BorderLayout.NORTH);
        card.add(buildList(), BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);

        refreshHistory();
    }

    // Header: title, search bar, status tabs, and dining option dropdown
    private JPanel buildHeader() {

        JLabel title = new JLabel("Order History");
        title.setFont(ColorTheme.f(Font.BOLD, 34));
        title.setForeground(TEXT);

        SearchBar search = new SearchBar();
        search.setPreferredSize(new Dimension(340, 56));

        search.setOnSearch(text -> {
            searchText = text;
            refreshHistory();
        });

        JPanel searchWrap = new JPanel(new GridBagLayout());
        searchWrap.setOpaque(false);
        searchWrap.add(search);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(searchWrap, BorderLayout.EAST);

        FilterTabs tabs = new FilterTabs();

        tabs.setOnSelect(filter -> {
            statusFilter = filter;
            refreshHistory();
        });

        DiningOptionDropdown dropdown = new DiningOptionDropdown();

        dropdown.setOnSelect(type -> {
            typeFilter = type;
            refreshHistory();
        });

        JPanel filterRow = new JPanel(new BorderLayout());
        filterRow.setOpaque(false);

        filterRow.add(tabs, BorderLayout.WEST);
        filterRow.add(dropdown, BorderLayout.EAST);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        header.add(titleRow);
        header.add(filterRow);

        return header;
    }

    // Creates the scrollable order list
    private JScrollPane buildList() {

        listPanel.setLayout(
                new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        listPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        listPanel.setBackground(ColorTheme.PAGE_BG);
        listPanel.setOpaque(false);

        scroll.setBorder(null);

        // Correct Swing method: getViewport(), not getViewPort()
        scroll.getViewport().setBackground(ColorTheme.PAGE_BG);

        scroll.getVerticalScrollBar().setUnitIncrement(16);

        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        return scroll;
    }

    // Rebuilds the list using the current filters and search text
    public void refreshHistory() {

        LinkedList<HistoryOrder> shown = history.search(statusFilter, typeFilter, searchText);

        listPanel.removeAll();

        if (shown.isEmpty()) {

            listPanel.add(Box.createVerticalStrut(60));

            // Distinguish an empty history from an empty search result.
            String message = history.isEmpty()
                    ? "No orders yet."
                    : "No orders match your filters.";

            listPanel.add(emptyMessage(message));

        } else {

            for (HistoryOrder order : shown) {

                listPanel.add(new OrderCard(order));

                // Consistent 12-pixel spacing between order cards
                listPanel.add(Box.createVerticalStrut(12));
            }
        }

        listPanel.revalidate();
        listPanel.repaint();

        // Return to the top after refreshing the list
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));
    }

    // Creates the empty-state message
    private JLabel emptyMessage(String text) {

        JLabel label = new JLabel(text, SwingConstants.CENTER);

        label.setFont(ColorTheme.f(Font.PLAIN, 15));
        label.setForeground(ColorTheme.GRAY_TXT);
        label.setAlignmentX(CENTER_ALIGNMENT);

        return label;
    }

    /**
     * Panel that follows the viewport width so that order cards
     * can stretch horizontally without horizontal scrolling.
     */
    private static class ListPanel extends JPanel implements Scrollable {

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(
                Rectangle visible,
                int orientation,
                int direction) {

            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(
                Rectangle visible,
                int orientation,
                int direction) {

            return visible.height;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}