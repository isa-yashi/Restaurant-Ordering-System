package orderqueue.oqUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import orderingpage.dblogic.OrderManagement;
import orderingpage.dblogic.OrderManagement.Order;
//import orderingpage.dblogic.OrderManagement.OrderType;

/* 
 * Everything that is not a stock Swing widget (rounded cards, pill
 * buttons, icons, the search box, the dropdown, the fancy checkbox) is
 * drawn by hand with Graphics2D inside small nested classes. That is
 * why the file has several nested classes: each one is one "widget".
 *
 * Compile & run:
 *     javac OrderQueueUI.java
 *     java OrderQueueUI
 */
public class OrderQueueUI extends JPanel {

    // =====================================================================
    // 1. SHARED COLORS / FONTS / CONSTANTS
    // Kept in one place so the whole look can be re-themed quickly.
    // =====================================================================
    static final Color SIDEBAR_BG = new Color(247, 247, 247); // light gray sidebar
    static final Color PAGE_BG = new Color(214, 214, 214); // darker gray behind the white card
    static final Color RED = new Color(211, 47, 47); // active chip + selected sidebar marker
    static final Color BLUE = new Color(0, 71, 171); // Edit button + checked checkbox
    static final Color TEXT = new Color(20, 20, 20); // near-black text
    static final Color HEADER_BG = new Color(220, 220, 220); // table header strip
    static final Color LINE = new Color(205, 205, 205); // table row separator lines

    // Peso sign written as a unicode escape so the file compiles correctly
    // no matter what encoding your editor/IDE saves it in.
    // static final String PESO = "\u20B1";
    static final String CURRENCY = "$";

    // Font object with the given style and size
    static Font font(int style, int size) {
        return new Font("SansSerif", style, size);
    }

    // setting all 3 component sizes to prevent layouts from stretching or shrinking
    // our widgets
    // SIZEEE
    static void fixSize(JComponent c, int w, int h) {
        Dimension d = new Dimension(w, h);
        c.setPreferredSize(d);
        c.setMinimumSize(d);
        c.setMaximumSize(d);
    }

    // graphics to make the shapes look smooth, custom rendering, drawing shapes
    // text and colors
    static Graphics2D aa(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2; // dispose the copy when finished to clean up memory
    }

    // =====================================================================
    // 2. STATE (instance fields, individual objects)
    // =====================================================================

    private DefaultTableModel model; // holds the order data
    // private DefaultTableModel historyModel;
    private JTable table; // shows the model
    // private JTable historyTable;
    private TableRowSorter<DefaultTableModel> sorter; // filter rows without deleting them
    private TableColumn checkboxColumn;
    private boolean selectionMode;
    private boolean deleteSelectionMode;
    private CheckBoxRenderer checkboxRenderer;
    private PillButton cancelButton;
    private PillButton selectAllButton;
    private PillButton editButton;
    private PillButton deleteButton;
    private SearchField searchField;
    private JLabel lastUpdateLabel;
    // private JPanel contentCards;
    // private final List<SidebarItem> sidebarItems = new ArrayList<>();

    // Current filter selections
    private String typeFilter = "All";
    private String statusFilter = "All status"; // value picked in the dropdown
    private final OrderManagement orders;

    // Called when an order reaches a final status (Served / Out for delivery /
    // Cancelled). App.java uses it to put the order into the Order History.
    private Consumer<Order> onOrderFinished = o -> {
    };

    // =====================================================================
    // 3. WINDOW SETUP
    // =====================================================================

    public OrderQueueUI(OrderManagement orders) {
        this.orders = orders;

        setLayout(new BorderLayout());
        setBackground(PAGE_BG);

        add(buildContent(), BorderLayout.CENTER);
    }

    /** Who should receive an order when it is finished (the Order History). */
    public void setOnOrderFinished(Consumer<Order> callback) {
        this.onOrderFinished = callback == null ? o -> {
        } : callback;
    }

    /**
     * A finished order (Served, Out for delivery or Cancelled) leaves the queue
     * and is handed to the Order History. Orders that are still Preparing or
     * Serving are ignored. Not used for "Delete": deleting an order from the
     * queue erases it and does NOT add it to the history.
     */
    public void sendToHistory(Order order) {
        if (order == null || !order.isFinished())
            return;
        orders.removeOrder(order); // out of the queue
        onOrderFinished.accept(order); // into the history
    }

    // =====================================================================
    // 4. SIDEBAR (logo + navigation)
    // =====================================================================

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS)); // stack vertically
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(275, 0)); // height follows window

        sidebar.add(new LogoHeader()); // restau logo and restau name

        // String[] labels = { "Menu & Ordering", "Order Queue", "Order History",
        // "Inventory" };
        // int[] glyphs = { Icons.MENU, Icons.CART, Icons.CLIPBOARD, Icons.HISTORY,
        // Icons.CUBE };

        String[] labels = { "Menu & Ordering", "Order Queue", "Order History", "Inventory" };
        int[] glyphs = { Icons.MENU, Icons.CART, Icons.CLIPBOARD, Icons.CUBE };

        for (int i = 0; i < labels.length; i++) {
            boolean selected = labels[i].equals("Order Queue"); // this screen is "Order Queue"
            // NOTE: only visual here. To make the other entries switch screens,
            // add a MouseListener to SidebarItem and swap pages.
            sidebar.add(new SidebarItem(labels[i], glyphs[i], selected));
        }

        sidebar.add(Box.createVerticalGlue()); // push everything to the top
        return sidebar;
    }

    // =====================================================================
    // 5. RIGHT SIDE: centered rectangle shape :0
    // =====================================================================

    private JPanel buildContent() {

        /*
         * JPanel page = new JPanel(new BorderLayout());
         * page.setBackground(PAGE_BG);
         * // Padding
         * page.setBorder(new EmptyBorder(25, 31, 30, 23)); // (top, left, bottom,
         * right)
         *
         * // Rounded corners
         * RoundedPanel card = new RoundedPanel(30, Color.WHITE);
         * card.setLayout(new BorderLayout());
         * card.setBorder(new EmptyBorder(18, 18, 18, 18)); // inner padding
         * 
         * JComponent header = buildHeader();
         * JComponent tableArea = buildTable();
         * JComponent footer = buildFooter();
         * 
         * card.add(header, BorderLayout.NORTH); // title area
         * card.add(tableArea, BorderLayout.CENTER); // table
         * card.add(footer, BorderLayout.SOUTH); // Cancel / Select All / Edit
         * /*
         * page.add(card, BorderLayout.CENTER);
         * return page;
         **/
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(PAGE_BG);

        // Padding
        page.setBorder(new EmptyBorder(25, 31, 30, 23));

        // Rounded corners
        RoundedPanel card = new RoundedPanel(30, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JComponent header = buildHeader();
        JComponent tableArea = buildTable();
        JComponent footer = buildFooter();

        card.add(header, BorderLayout.NORTH);
        card.add(tableArea, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        page.add(card, BorderLayout.CENTER);
        return page;

    }

    // ---------------------------------------------------------------------
    // 5.1 Header
    // ---------------------------------------------------------------------
    public void refreshOrders() {

        model.setRowCount(0);

        for (Object[] row : getOrderData()) {
            model.addRow(row);
        }

        applyFilters();

        refreshLastUpdate();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        // Title block on the left, tools on the right
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("Order Queue");
        title.setFont(font(Font.BOLD, 36));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        lastUpdateLabel = new JLabel();
        lastUpdateLabel.setFont(font(Font.PLAIN, 13));
        lastUpdateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        refreshLastUpdate(); // current time

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(title);
        titleBox.add(lastUpdateLabel);

        // Search box, three icon buttons, sa kanan
        searchField = new SearchField();
        // Live filtering: re-apply filters every time the text changes.
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                applyFilters();
            }

            public void removeUpdate(DocumentEvent e) {
                applyFilters();
            }

            public void changedUpdate(DocumentEvent e) {
                applyFilters();
            }
        });

        IconButton editIcon = new IconButton(IconButton.EDIT, "Show order checkboxes");
        IconButton trashIcon = new IconButton(IconButton.TRASH, "Delete selected orders");
        IconButton refreshIcon = new IconButton(IconButton.REFRESH, "Refresh orders");
        // editIcon.addActionListener(e -> editSelected());
        editIcon.addActionListener(e -> {
            if (selectionMode && !deleteSelectionMode) {
                setSelectionMode(false);
            } else {
                setSelectionMode(true, false);
            }
            editIcon.setToolTipText(selectionMode ? "Hide Status Orders" : "Edit Status Orders");
        });
        trashIcon.addActionListener(e -> {
            if (selectionMode && deleteSelectionMode) {
                setSelectionMode(false);
            } else {
                setSelectionMode(true, true);
            }
            trashIcon.setToolTipText(selectionMode && deleteSelectionMode
                    ? "Hide delete checkboxes"
                    : "Select orders to delete");
        });
        refreshIcon.addActionListener(e -> refreshLastUpdate());

        JPanel tools = new JPanel();
        tools.setOpaque(false);
        tools.setLayout(new BoxLayout(tools, BoxLayout.X_AXIS));
        tools.add(searchField);
        tools.add(Box.createHorizontalStrut(10)); // fixed gap between widgets
        tools.add(editIcon);
        tools.add(Box.createHorizontalStrut(6));
        tools.add(trashIcon);
        tools.add(Box.createHorizontalStrut(6));
        tools.add(refreshIcon);

        // Wrap the tools so they stay at the TOP of the row instead of
        // stretching down next to the two-line title block.
        JPanel toolsWrap = new JPanel(new BorderLayout());
        toolsWrap.setOpaque(false);
        toolsWrap.setBorder(new EmptyBorder(4, 0, 0, 0));
        toolsWrap.add(tools, BorderLayout.NORTH);

        top.add(titleBox, BorderLayout.WEST);
        top.add(toolsWrap, BorderLayout.EAST);

        // ----- Row 2: filter chips on the left, status dropdown on the right -----
        JPanel filterRow = new JPanel(new BorderLayout());
        filterRow.setOpaque(false);
        filterRow.setBorder(new EmptyBorder(10, 0, 14, 0)); // space above/below the row

        JPanel chips = new JPanel();
        chips.setOpaque(false);
        chips.setLayout(new BoxLayout(chips, BoxLayout.X_AXIS));

        String[] chipNames = { "All", "Dine in", "Take out", "Delivery" };
        List<PillButton> chipButtons = new ArrayList<>();
        for (String name : chipNames) {
            PillButton chip = new PillButton(name, Font.BOLD, 14);
            chipButtons.add(chip);
            chip.addActionListener(e -> {
                typeFilter = name; // remember which chip is active
                for (PillButton other : chipButtons) { // restyle all chips: only one is red
                    styleChip(other, other == chip);
                }
                applyFilters(); // hide rows that don't match
            });
            chips.add(chip);
            chips.add(Box.createHorizontalStrut(12));
        }
        for (PillButton chip : chipButtons) {
            styleChip(chip, chip.getText().equals("All")); // "All" starts selected
        }

        String[] statuses = { "All status", "Preparing", "Serving", "Served", "Out for delivery", "Cancelled" };
        StatusCombo statusCombo = new StatusCombo(statuses, picked -> {
            statusFilter = picked;
            applyFilters();
        });

        filterRow.add(chips, BorderLayout.WEST);
        filterRow.add(statusCombo, BorderLayout.EAST);

        header.add(top);
        header.add(filterRow);
        return header;
    }

    // Red filled chip when active, white outlined chip when not.
    private void styleChip(PillButton chip, boolean active) {
        if (active) {
            chip.setColors(RED, Color.WHITE, null); // fill, text, no outline
        } else {
            chip.setColors(Color.WHITE, TEXT, TEXT); // fill, text, outline
        }
        chip.repaint();
    }

    // for data
    private Object[][] getOrderData() {
        List<Object[]> rows = new ArrayList<>();

        for (Order order : orders.getQueue()) {

            String orderNo = order.getID();

            String status = order.getStatus();
            String paid = order.getPaymentStatus();
            String paymentMode = order.getPaymentMode();

            String date = "";
            String time = "";

            try {
                java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(
                        order.getTimeStamp(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                date = dateTime.format(
                        DateTimeFormatter.ofPattern("MM/dd/yy"));

                time = dateTime.format(
                        DateTimeFormatter.ofPattern("HH:mm"));

            } catch (Exception e) {
                e.printStackTrace();
            }

            rows.add(new Object[] {
                    false,
                    orderNo,
                    status,
                    paid,
                    paymentMode,
                    CURRENCY + String.format("%.2f", order.getTotal()),
                    date,
                    time
            });
        }

        return rows.toArray(new Object[0][]);
    }

    // ---------------------------------------------------------------------
    // 5b. The orders table
    // ---------------------------------------------------------------------
    private JComponent buildTable() {
        String[] columns = { "", "Order No.", "Status", "Paid", "Payment mode", "Order total", "Date", "Time" };

        // Sample rows copied from the mockup. Column 0 is the checkbox (Boolean).
        /*
         * Object[][] data = {
         * { false, "D101", "Preparing", "Paid", "Cash", CURRENCY + "500.00",
         * "10/26/26", "12:21" },
         * { false, "T101", "Preparing", "Paid", "Credit/Debit", CURRENCY + "450.00",
         * "10/26/26", "13:30" },
         * { false, "DL101", "Preparing", "Paid", "Online", CURRENCY + "1.67",
         * "10/26/26", "14:00" },
         * // { true, "D101", "Cancelled", "-", "-", "-", "10/26/26", "15:49" },
         * { false, "DL102", "Preparing", "-", "Paid", "Credit/Debit", CURRENCY +
         * "50.00", "10/26/26", "16:00" }
         * };
         */

        Object[][] data = getOrderData();

        model = new DefaultTableModel(data, columns) {
            @Override
            public Class<?> getColumnClass(int c) {
                return c == 0 ? Boolean.class : String.class; // tells the table col 0 if true/false
            }

            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        // lines inside the table
        table = new JTable(model) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(LINE);
                int rh = getRowHeight();
                for (int r = getRowCount(); (r + 1) * rh <= getHeight(); r++) {
                    int y = (r + 1) * rh - 1;
                    g.drawLine(0, y, getWidth(), y);
                }
            }
        };

        table.setRowHeight(42); // tall rows
        table.setFont(font(Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setBackground(Color.WHITE);
        table.setShowVerticalLines(false); // only horizontal separators
        table.setShowHorizontalLines(true);
        table.setGridColor(LINE);
        table.setIntercellSpacing(new Dimension(0, 1)); // 1px line between rows
        table.setFillsViewportHeight(true); // table background fills the empty area
        table.setRowSelectionAllowed(false); // no blue row highlight; we use checkboxes
        table.setFocusable(false);

        // Sorter = filtering of status
        sorter = new TableRowSorter<>(model);
        for (int i = 0; i < columns.length; i++)
            sorter.setSortable(i, false);
        table.setRowSorter(sorter);

        // UI ng cells
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER); // center all text cells
        table.setDefaultRenderer(String.class, center);
        // table.getColumnModel().getColumn(0).setCellRenderer(new CheckBoxRenderer());
        checkboxRenderer = new CheckBoxRenderer();
        table.getColumnModel().getColumn(0).setCellRenderer(checkboxRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(new StatusCellRenderer());
        checkboxColumn = table.getColumnModel().getColumn(0);
        // table.removeColumn(checkboxColumn);

        // Header ng table
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        headerRenderer.setBackground(HEADER_BG);
        headerRenderer.setForeground(TEXT);
        headerRenderer.setOpaque(true);
        table.getTableHeader().setDefaultRenderer(headerRenderer);
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);

        // table coliumn widths
        int[] widths = { 50, 90, 150, 100, 130, 110, 100, 80 };
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.removeColumn(checkboxColumn);

        // Pag cinlick yung checkbox, ma toggle ang row or order
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint());
                int viewCol = table.columnAtPoint(e.getPoint());
                // if (viewRow >= 0 && viewCol == 0) {
                if (selectionMode && viewRow >= 0 && viewCol >= 0
                        && table.convertColumnIndexToModel(viewCol) == 0) {
                    // Convert because filtering makes "row on screen" != "row in model".
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    boolean current = (Boolean) model.getValueAt(modelRow, 0);
                    model.setValueAt(!current, modelRow, 0);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder()); // remove the default gray frame
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    // ---------------------------------------------------------------------
    // 5c. Footer: Cancel / Select All / Edit
    // ---------------------------------------------------------------------
    private JComponent buildFooter() {
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.X_AXIS));
        footer.setBorder(new EmptyBorder(14, 0, 0, 0));

        PillButton cancel = new PillButton("Cancel", Font.PLAIN, 14);
        cancel.setColors(Color.WHITE, TEXT, TEXT);
        cancel.addActionListener(e -> setAllChecked(false)); // un-tick everything
        cancel.addActionListener(e -> setSelectionMode(false));
        cancelButton = cancel;
        cancelButton.setVisible(selectionMode);

        PillButton selectAll = new PillButton("Select All", Font.PLAIN, 14);
        selectAll.setColors(Color.WHITE, TEXT, TEXT);
        selectAll.addActionListener(e -> setAllChecked(true)); // tick every visible row
        selectAllButton = selectAll;
        selectAllButton.setVisible(selectionMode);

        PillButton edit = new PillButton("Edit", Font.BOLD, 14);
        edit.setColors(BLUE, Color.WHITE, null);
        edit.addActionListener(e -> editSelected());
        editButton = edit;
        editButton.setVisible(selectionMode);

        PillButton delete = new PillButton("Delete", Font.BOLD, 14);
        delete.setColors(RED, Color.WHITE, null);
        delete.addActionListener(e -> deleteSelected());
        deleteButton = delete;
        deleteButton.setVisible(selectionMode && deleteSelectionMode);

        footer.add(Box.createHorizontalGlue()); // glue pushes the buttons to the right
        footer.add(cancel);
        footer.add(Box.createHorizontalStrut(10));
        footer.add(selectAll);
        footer.add(Box.createHorizontalStrut(10));
        footer.add(edit);
        footer.add(delete);
        return footer;
    }

    // =====================================================================
    // 6. BEHAVIOR (filtering, selection, delete, edit)
    // =====================================================================

    // Prefer the saved order type so older IDs are categorized correctly.
    private String typeOf(String orderNo) {
        for (Order order : orders.getQueue()) {
            if (order.getID().equals(orderNo))
                return order.getOrderType().getLabel();
        }
        if (orderNo.startsWith("DL"))
            return "Delivery";
        if (orderNo.startsWith("D"))
            return "Dine in";
        if (orderNo.startsWith("T"))
            return "Take out";
        return "";
    }

    /**
     * Rebuilds the row filter from the three controls: chip, dropdown, search text.
     */

    private void applyFilters() {
        final String query = searchField.getText().trim().toLowerCase();

        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> row) {
                // 1) Chip filter by the saved order type.
                if (!typeFilter.equals("All") && !typeOf(row.getStringValue(1)).equals(typeFilter)) {
                    return false;
                }
                // 2) Dropdown filter. "Cancelled" lives in the Paid column in the mockup,
                // so we compare against both the Status and Paid columns.
                if (!statusFilter.equals("All status")
                        && !statusFilter.equals(row.getStringValue(2))
                        && !statusFilter.equals(row.getStringValue(3))) {
                    return false;
                }
                // 3) Search text: matches if ANY text column contains the query.
                if (!query.isEmpty()) {
                    for (int c = 1; c < row.getValueCount(); c++) {
                        if (row.getStringValue(c).toLowerCase().contains(query))
                            return true;
                    }
                    return false;
                }
                return true;
            }
        });
    }

    /** Ticks or un-ticks every row currently visible (after filtering). */
    private void setAllChecked(boolean checked) {
        for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
            model.setValueAt(checked, table.convertRowIndexToModel(viewRow), 0);
        }
    }

    private void setSelectionMode(boolean enabled) {
        setSelectionMode(enabled, false);
    }

    private void setSelectionMode(boolean enabled, boolean deleting) {
        selectionMode = enabled;
        deleteSelectionMode = enabled && deleting;
        if (checkboxRenderer != null) {
            checkboxRenderer.setCheckedColor(deleteSelectionMode ? RED : BLUE);
            table.repaint();
        }
        if (cancelButton != null) {
            cancelButton.setVisible(enabled);
            cancelButton.getParent().revalidate();
            cancelButton.getParent().repaint();
        }
        if (selectAllButton != null) {
            selectAllButton.setVisible(enabled);
            selectAllButton.getParent().revalidate();
            selectAllButton.getParent().repaint();
        }
        if (editButton != null) {
            editButton.setVisible(enabled && !deleteSelectionMode);
            editButton.getParent().revalidate();
            editButton.getParent().repaint();
        }
        if (deleteButton != null) {
            deleteButton.setVisible(enabled && deleteSelectionMode);
            deleteButton.getParent().revalidate();
            deleteButton.getParent().repaint();
        }
        if (enabled && table.getColumnModel().getColumnCount() > 0
                && table.convertColumnIndexToModel(0) != 0) {
            table.addColumn(checkboxColumn);
            table.moveColumn(table.getColumnCount() - 1, 0);
            for (int row = 0; row < model.getRowCount(); row++) {
                model.setValueAt(false, row, 0);
            }
        } else if (!enabled && checkboxColumn != null
                && table.getColumnModel().getColumnCount() > 0
                && table.convertColumnIndexToModel(0) == 0) {
            table.removeColumn(checkboxColumn);
            for (int row = 0; row < model.getRowCount(); row++) {
                model.setValueAt(false, row, 0);
            }
        }
    }

    // Collects the indexes of all ticked rows.
    private List<Integer> checkedModelRows() {
        List<Integer> rows = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            if ((Boolean) model.getValueAt(i, 0))
                rows.add(i);
        }
        return rows;
    }

    // Opens the status update dialog for the selected orders
    private void editSelected() {
        List<Integer> rows = checkedModelRows();
        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Tick at least one order first.",
                    "Edit",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JPanel statusRows = new JPanel();
        statusRows.setLayout(new BoxLayout(statusRows, BoxLayout.Y_AXIS));
        statusRows.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel dialogTitle = new JLabel("Status Update", SwingConstants.CENTER);
        dialogTitle.setFont(font(Font.BOLD, 18));
        dialogTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusRows.add(dialogTitle);
        JLabel dialogSubtitle = new JLabel(rows.size() == 1
                ? "Suggested next status for the selected order"
                : "Choose a next status for each order", SwingConstants.CENTER);
        dialogSubtitle.setFont(font(Font.PLAIN, 12));
        dialogSubtitle.setForeground(new Color(105, 105, 105));
        dialogSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusRows.add(Box.createVerticalStrut(4));
        statusRows.add(dialogSubtitle);
        statusRows.add(Box.createVerticalStrut(12));

        Map<Integer, JComboBox<String>> statusChoices = new LinkedHashMap<>();
        for (int modelRow : rows) {
            String orderNo = model.getValueAt(modelRow, 1).toString();
            String currentStatus = model.getValueAt(modelRow, 2).toString();
            List<String> available = availableStatusUpdates(orderNo, currentStatus);

            JPanel orderRow = new JPanel(new BorderLayout(12, 0));
            orderRow.setBorder(new EmptyBorder(4, 0, 4, 0));
            orderRow.add(new JLabel(orderNo + "  -  " + currentStatus), BorderLayout.WEST);
            if (available.isEmpty()) {
                orderRow.add(new JLabel("No available update"), BorderLayout.EAST);
            } else {
                JComboBox<String> statusChoice = new JComboBox<>(available.toArray(new String[0]));
                statusChoice.setFont(font(Font.PLAIN, 12));
                statusChoice.setPreferredSize(new Dimension(168, 30));
                statusChoice.setMaximumSize(new Dimension(168, 30));
                statusChoice.setBackground(Color.WHITE);
                statusChoice.setFocusable(false);
                statusChoice.setMaximumRowCount(3);
                statusChoice.setRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                            boolean isSelected, boolean cellHasFocus) {
                        JLabel item = (JLabel) super.getListCellRendererComponent(
                                list, value, index, isSelected, cellHasFocus);
                        item.setBorder(new EmptyBorder(5, 9, 5, 9));
                        return item;
                    }
                });
                statusChoices.put(modelRow, statusChoice);
                orderRow.add(statusChoice, BorderLayout.EAST);
            }
            orderRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            statusRows.add(orderRow);
        }

        if (statusChoices.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The selected order(s) have no available status updates.",
                    "Status Update", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JScrollPane scroll = new JScrollPane(statusRows);
        scroll.setPreferredSize(new Dimension(440, Math.min(360, 92 + rows.size() * 44)));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(statusRows.getBackground());
        int answer = JOptionPane.showConfirmDialog(this, scroll, "Status Update",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer == JOptionPane.OK_OPTION) {
            for (Map.Entry<Integer, JComboBox<String>> choice : statusChoices.entrySet()) {
                Object newStatus = choice.getValue().getSelectedItem();

                String orderId = model.getValueAt(choice.getKey(), 1).toString();

                Order order = findOrder(orderId);

                if (order != null) {
                    order.setStatus(newStatus.toString());
                }

                model.setValueAt(newStatus, choice.getKey(), 2);

                if ("Served".equals(newStatus)
                        || "Out for delivery".equals(newStatus)
                        || "Cancelled".equals(newStatus)) {
                    scheduleOrderRemoval(choice.getKey()); // the row fades out of the table after 3 seconds
                    sendToHistory(order); // and the order itself moves to the Order History
                }
            }
        }
    }

    private Order findOrder(String orderId) {
        for (Order order : orders.getQueue()) {
            if (order.getID().equals(orderId)) {
                return order;
            }
        }

        return null;
    }

    // private void scheduleServedOrderRemoval(int modelRow) {
    private void scheduleOrderRemoval(int modelRow) {
        Object orderToRemove = model.getDataVector().get(modelRow);
        Timer removalTimer = new Timer(3000, e -> {
            for (int row = 0; row < model.getRowCount(); row++) {
                if (model.getDataVector().get(row) == orderToRemove) {
                    model.removeRow(row);
                    break;
                }
            }
        });
        removalTimer.setRepeats(false);
        removalTimer.start();
    }

    private List<String> availableStatusUpdates(String orderNo, String currentStatus) {
        List<String> available = new ArrayList<>();
        String orderType = typeOf(orderNo);
        if ("Preparing".equals(currentStatus)) {
            if ("Dine in".equals(orderType)) {
                available.add("Serving");
                available.add("Served");
                available.add("Cancelled");
            } else if ("Take out".equals(orderType)) {
                available.add("Serving");
                available.add("Served");
                available.add("Cancelled");
            } else if ("Delivery".equals(orderType)) {
                available.add("Out for delivery");
                available.add("Cancelled");
            }
        } else if ("Serving".equals(currentStatus)) {
            available.add("Serving");
            available.add("Served");
            available.add("Cancelled");
        }
        return available;
    }

    private static Color statusColor(String status) {
        if ("Preparing".equals(status))
            return new Color(249, 115, 22);
        if ("Serving".equals(status))
            return new Color(235, 218, 40);
        if ("Out for delivery".equals(status))
            return new Color(34, 197, 94);
        if ("Served".equals(status))
            return new Color(34, 197, 94);
        if ("Cancelled".equals(status))
            return new Color(179, 13, 7);
        return null;
    }

    // Deletes all ticked rows after asking for confirmation.
    private void deleteSelected() {
        List<Integer> rows = checkedModelRows();
        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tick at least one order first.", "Delete",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "Delete " + rows.size() + " order(s)?",
                "Delete", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            // FIFO: Remove from the BOTTOM up so earlier indexes stay valid while deleting.
            for (int i = rows.size() - 1; i >= 0; i--) {
                int row = rows.get(i);
                String orderId = model.getValueAt(row, 1).toString();
                Order order = findOrder(orderId);
                if (order != null) {
                    orders.removeOrder(order);
                }
                model.removeRow(rows.get(i));
            }
            setSelectionMode(false);
        }
    }

    // sinusulat yung last update using current time
    private void refreshLastUpdate() {
        if (lastUpdateLabel == null)
            return;
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH)).toLowerCase();
        lastUpdateLabel.setText("Last update (today, " + time + ")");
    }

    // =====================================================================
    // 7. CUSTOM WIDGETS (nested static classes, all painted by hand)
    // =====================================================================

    // ---------------------------------------------------------------------
    // RoundedPanel: a panel whose background is a rounded rectangle
    // ---------------------------------------------------------------------
    static class RoundedPanel extends JPanel {
        private final int arc; // corner diameter
        private final Color fill;

        RoundedPanel(int arc, Color fill) {
            this.arc = arc;
            this.fill = fill;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = aa(g);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(g); // children are painted after this by Swing
        }
    }

    // ---------------------------------------------------------------------
    // PillButton: fully rounded button used for chips and footer buttons
    // ---------------------------------------------------------------------

    static class PillButton extends JButton {
        private Color fill = Color.WHITE;
        private Color fg = TEXT;
        private Color outline = TEXT; // null = no outline

        PillButton(String text, int fontStyle, int fontSize) {
            super(text);
            setFont(font(fontStyle, fontSize));
            // Turn off every built-in painting; we draw it ourselves.
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            fixSize(this, 98, 28); // every pill in the mockup is ~98 x 28
        }

        void setColors(Color fill, Color fg, Color outline) {
            this.fill = fill;
            this.fg = fg;
            this.outline = outline;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = aa(g);
            int w = getWidth(), h = getHeight();

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w - 1, h - 1, h, h); // arc = height => perfect pill

            if (getModel().isPressed()) { // quick pressed feedback
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
            }
            if (outline != null) {
                g2.setColor(outline);
                g2.setStroke(new BasicStroke(1.3f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
            }

            // Center the label using font metrics.
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(getText())) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(fg);
            g2.drawString(getText(), tx, ty);
            g2.dispose();
        }
    }

    // ---------------------------------------------------------------------
    // SearchField: text box with rounded outline, magnifier and placeholder
    // ---------------------------------------------------------------------
    static class SearchField extends JTextField {
        SearchField() {
            setOpaque(false); // we draw the white pill ourselves
            setFont(font(Font.PLAIN, 13));
            setCaretColor(TEXT);
            setBorder(new EmptyBorder(0, 46, 0, 16)); // left inset leaves room for the magnifier
            fixSize(this, 250, 34);
        }

        @Override
        protected void paintComponent(Graphics g) {
            int w = getWidth(), h = getHeight();

            // 1) white pill background (drawn BEFORE the text)
            Graphics2D g2 = aa(g);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
            g2.dispose();

            // 2) the actual text + caret (the normal JTextField painting)
            super.paintComponent(g);

            // 3) outline, magnifier, placeholder (drawn on top)
            g2 = aa(g);
            g2.setColor(TEXT);
            g2.setStroke(new BasicStroke(1.3f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);

            int cy = h / 2 - 1;
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(12, cy - 8, 16, 16); // lens
            g2.drawLine(25, cy + 6, 30, cy + 11); // handle

            if (getText().isEmpty()) { // placeholder only when nothing is typed
                g2.setFont(getFont());
                g2.setColor(new Color(70, 70, 70));
                FontMetrics fm = g2.getFontMetrics();
                int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString("Search", getInsets().left, ty);
            }
            g2.dispose();
        }
    }

    // ---------------------------------------------------------------------
    // IconButton: flat button drawing an edit / trash / refresh icon
    // ---------------------------------------------------------------------
    static class IconButton extends JButton {
        static final int EDIT = 0, TRASH = 1, REFRESH = 3;
        private final int kind;

        IconButton(int kind, String tooltip) {
            this.kind = kind;
            setToolTipText(tooltip);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true); // so we can show a hover highlight
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            fixSize(this, 34, 34);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = aa(g);

            if (getModel().isRollover()) { // light gray rounded hover background
                g2.setColor(new Color(0, 0, 0, 18));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }

            g2.setColor(TEXT);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (kind) {
                case EDIT:
                    g2.drawRoundRect(6, 9, 17, 19, 6, 6); // note-pad outline
                    g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(15, 19, 26, 8); // pencil body
                    break;
                case TRASH:
                    g2.drawLine(8, 10, 26, 10); // lid
                    g2.drawLine(14, 10, 14, 7); // handle (left, top, right)
                    g2.drawLine(14, 7, 20, 7);
                    g2.drawLine(20, 7, 20, 10);
                    g2.drawRoundRect(10, 12, 14, 16, 4, 4); // bin body
                    g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(14, 16, 14, 24); // inner slats
                    g2.drawLine(20, 16, 20, 24);
                    break;
                case REFRESH:
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Path2D refreshArrow = new Path2D.Float();
                    refreshArrow.moveTo(25, 19);
                    refreshArrow.curveTo(25, 26, 20, 29, 14, 28);
                    refreshArrow.curveTo(5, 27, 2, 19, 4, 12);
                    refreshArrow.curveTo(6, 4, 13, 2, 19, 4);
                    refreshArrow.curveTo(23, 5, 25, 8, 27, 11);
                    refreshArrow.lineTo(27, 6);
                    refreshArrow.moveTo(27, 11);
                    refreshArrow.lineTo(20, 11);
                    g2.draw(refreshArrow);
                    break;
            }
            g2.dispose();
        }
    }

    // ---------------------------------------------------------------------
    // StatusCombo: rounded "All status v" dropdown built from a JPopupMenu
    // (a plain JComboBox is hard to restyle, so we fake one)
    // ---------------------------------------------------------------------
    static class StatusCombo extends JComponent {
        private final String[] items;
        private final Consumer<String> onChange; // called with the picked text
        private String selected;

        StatusCombo(String[] items, Consumer<String> onChange) {
            this.items = items;
            this.onChange = onChange;
            this.selected = items[0];
            setFont(font(Font.PLAIN, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            fixSize(this, 142, 33);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    showMenu();
                }
            });
        }

        private void showMenu() {
            JPopupMenu menu = new JPopupMenu();
            for (String s : items) {
                JMenuItem mi = new JMenuItem(s);
                mi.addActionListener(e -> {
                    selected = s; // update label shown in the box
                    repaint();
                    onChange.accept(s); // tell the main window
                });
                menu.add(mi);
            }
            menu.show(this, 0, getHeight()); // open directly below the box
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = aa(g);
            int w = getWidth(), h = getHeight();

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
            g2.setColor(TEXT);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(selected, 15, (h - fm.getHeight()) / 2 + fm.getAscent());

            // v made from two lines)
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = w - 25, cy = h / 2;
            g2.drawLine(cx - 8, cy - 3, cx, cy + 4);
            g2.drawLine(cx, cy + 4, cx + 8, cy - 3);
            g2.dispose();
        }
    }

    // ---------------------------------------------------------------------
    // StatusCellRenderer: shows a colored dot beside each order status.
    // ---------------------------------------------------------------------
    static class StatusCellRenderer extends DefaultTableCellRenderer {
        StatusCellRenderer() {
            setHorizontalAlignment(SwingConstants.LEFT);
            setIconTextGap(8);
            setBorder(new EmptyBorder(0, 12, 0, 0));
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(0, 0, 0, 0));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                boolean focus, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            String status = value == null ? "" : value.toString();
            Color color = statusColor(status);
            setIcon(color == null ? null : new StatusDotIcon(color));
            return this;
        }
    }

    static class StatusDotIcon implements Icon {
        private final Color color;

        StatusDotIcon(Color color) {
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return 10;
        }

        @Override
        public int getIconHeight() {
            return 10;
        }

        @Override
        public void paintIcon(Component component, Graphics g, int x, int y) {
            Graphics2D g2 = aa(g);
            g2.setColor(color);
            g2.fillOval(x, y, 9, 9);
            g2.dispose();
        }
    }

    // ---------------------------------------------------------------------
    // CheckBoxRenderer: draws the rounded checkbox inside table cells.
    // A renderer is only a "stamp": the table calls it once per cell and
    // paints whatever the returned component looks like at that moment.
    // ---------------------------------------------------------------------
    static class CheckBoxRenderer extends JComponent implements TableCellRenderer {
        private boolean checked;
        private Color checkedColor = BLUE;

        void setCheckedColor(Color color) {
            checkedColor = color;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean sel,
                boolean focus, int row, int col) {
            checked = Boolean.TRUE.equals(value); // remember this cell's value, then get painted
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = aa(g);
            int size = 26;
            int x = (getWidth() - size) / 2; // center the box in the cell
            int y = (getHeight() - size) / 2;

            if (checked) {
                // g2.setColor(RED);
                g2.setColor(checkedColor);
                g2.fillRoundRect(x, y, size, size, 8, 8);
                g2.setColor(Color.WHITE); // the tick mark
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 7, y + 13, x + 11, y + 18);
                g2.drawLine(x + 11, y + 18, x + 19, y + 8);
            } else {
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x, y, size, size, 8, 8);
                g2.setColor(new Color(190, 190, 190));
                g2.setStroke(new BasicStroke(1.3f));
                g2.drawRoundRect(x, y, size, size, 8, 8);
            }
            g2.dispose();
        }
    }

    // ---------------------------------------------------------------------
    // LogoHeader: round logo placeholder + "LOREM IPSUM" title
    // (To use a real logo image, draw an ImageIcon here instead of the circle.)
    // ---------------------------------------------------------------------
    static class LogoHeader extends JComponent {
        LogoHeader() {
            fixSize(this, 275, 165);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = aa(g);

            // White circle with a faint ring (the logo badge).
            g2.setColor(Color.WHITE);
            g2.fillOval(24, 38, 70, 70);
            g2.setColor(new Color(232, 232, 232));
            g2.drawOval(24, 38, 70, 70);

            // Dark disc with a white fork & knife, then the "Restaurant" caption.
            g2.setColor(new Color(40, 40, 40));
            g2.fillOval(46, 46, 26, 26);
            Icons.draw(g2, Icons.MENU, 59, 59, Color.WHITE, SIDEBAR_BG);

            g2.setColor(new Color(40, 40, 40));
            g2.setFont(font(Font.BOLD, 10));
            String caption = "Restaurant";
            g2.drawString(caption, 59 - g2.getFontMetrics().stringWidth(caption) / 2, 91);

            // Two-line brand title.
            g2.setFont(font(Font.BOLD, 28));
            g2.setColor(TEXT);
            g2.drawString("LOREM", 108, 67);
            g2.drawString("IPSUM", 108, 101);
            g2.dispose();
        }
    }

    // ---------------------------------------------------------------------
    // SidebarItem: one navigation row (icon + label + red marker if selected)
    // ---------------------------------------------------------------------
    static class SidebarItem extends JComponent {
        private final String label;
        private final int glyph;
        private final boolean selected;

        SidebarItem(String label, int glyph, boolean selected) {
            this.label = label;
            this.glyph = glyph;
            this.selected = selected;
            fixSize(this, 275, 72);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = aa(g);
            int w = getWidth(), h = getHeight();

            // Red marker hugging the right edge (rounded on the left only,
            // because the right half is drawn outside the component and clipped).
            if (selected) {
                g2.setColor(RED);
                g2.fillRoundRect(w - 8, h / 2 - 24, 16, 48, 10, 10);
            }

            // Icon sits slightly above the text baseline, like the mockup.
            Icons.draw(g2, glyph, 72, h / 2 - 8, new Color(30, 30, 30), SIDEBAR_BG);

            g2.setFont(font(Font.PLAIN, 14));
            g2.setColor(new Color(40, 40, 40));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, 108, h / 2 + fm.getAscent() / 2 - 2); // roughly vertically centered
            g2.dispose();
        }
    }

    // ---------------------------------------------------------------------
    // Icons: tiny hand-drawn glyphs for the sidebar (about 20x20 each)
    // ---------------------------------------------------------------------
    static class Icons {
        static final int MENU = 0, CART = 1, CLIPBOARD = 2, HISTORY = 3, CUBE = 4;

        /**
         * @param cx,cy  center of the icon
         * @param color  main drawing color
         * @param cutout color used for "holes" (inner lines), usually the background
         *               color
         */
        static void draw(Graphics2D g, int type, int cx, int cy, Color color, Color cutout) {
            g.setColor(color);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (type) {
                case MENU: // fork and knife
                    g.fillRect(cx - 8, cy - 9, 2, 7); // fork prongs
                    g.fillRect(cx - 4, cy - 9, 2, 7);
                    g.fillRoundRect(cx - 8, cy - 4, 6, 5, 3, 3); // fork head
                    g.fillRect(cx - 6, cy, 2, 9); // fork handle
                    g.fillRoundRect(cx + 3, cy - 9, 4, 12, 4, 4); // knife blade
                    g.fillRect(cx + 4, cy, 2, 9); // knife handle
                    break;

                case CART: {
                    Path2D handle = new Path2D.Float(); // cart handle + frame line
                    handle.moveTo(cx - 10, cy - 8);
                    handle.lineTo(cx - 6, cy - 8);
                    handle.lineTo(cx - 3, cy + 3);
                    handle.lineTo(cx + 7, cy + 3);
                    g.draw(handle);
                    g.fillPolygon(new int[] { cx - 5, cx + 9, cx + 7, cx - 3 }, // basket
                            new int[] { cy - 5, cy - 5, cy + 1, cy + 1 }, 4);
                    g.fillOval(cx - 4, cy + 6, 4, 4); // wheels
                    g.fillOval(cx + 4, cy + 6, 4, 4);
                    break;
                }

                case CLIPBOARD:
                    g.fillRoundRect(cx - 7, cy - 8, 14, 18, 4, 4); // board
                    g.fillRoundRect(cx - 3, cy - 10, 6, 5, 2, 2); // clip
                    g.setColor(cutout); // text lines cut into the board
                    g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.drawLine(cx - 4, cy - 1, cx + 4, cy - 1);
                    g.drawLine(cx - 4, cy + 3, cx + 4, cy + 3);
                    break;

                case HISTORY:
                    g.drawArc(cx - 8, cy - 8, 16, 16, 60, 290); // circle with a gap
                    g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.drawLine(cx, cy - 4, cx, cy); // clock hands
                    g.drawLine(cx, cy, cx + 3, cy + 2);
                    break;

                default: // CUBE: hexagon with three inner edges
                    g.fillPolygon(new int[] { cx, cx + 8, cx + 8, cx, cx - 8, cx - 8 },
                            new int[] { cy - 9, cy - 4, cy + 4, cy + 9, cy + 4, cy - 4 }, 6);
                    g.setColor(cutout);
                    g.setStroke(new BasicStroke(1.4f));
                    g.drawLine(cx - 8, cy - 4, cx, cy);
                    g.drawLine(cx + 8, cy - 4, cx, cy);
                    g.drawLine(cx, cy, cx, cy + 9);
            }
        }
    }

    // =====================================================================
    // 8. ENTRY POINT
    // =====================================================================
    public static void main(String[] args) {
        // Swing must be created/updated on the Event Dispatch Thread (EDT).
        // SwingUtilities.invokeLater(() -> new OrderQueueUI().setVisible(true));

        SwingUtilities.invokeLater(() -> {
            OrderManagement orders = new OrderManagement();
            OrderQueueUI queueUI = new OrderQueueUI(orders);

            JFrame frame = new JFrame("Order Queue");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 750);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(queueUI);
            frame.setVisible(true);
        });
    }
}