package orderingpage.dblogic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import orderingpage.cart.CartItem;
import orderingpage.cart.CartManager;
import orderingpage.dblogic.DataStructure.LinkedList;
import orderingpage.dblogic.DataStructure.Queue;
import orderingpage.dblogic.OrderManagement.Order;
import orderingpage.dblogic.OrderManagement.OrderType;
import orderingpage.dishesandmenu.Dish;
import orderingpage.dishesandmenu.Dish.Doneness;
import orderingpage.dishesandmenu.Menu;

/**
 * Saves / loads the cart and the order queue as plain text files in the
 * "data" folder, so everything is still there after closing the program.
 *
 * data/cart.txt -> dishId|quantity|DONENESS
 * data/orders.txt -> ORDER|id|TYPE|total|timestamp
 * ITEM|dishId|name|price|quantity|DONENESS
 */

public class DataStore {
    private final File dir = new File("data");
    private final File cartFile = new File(dir, "cart.txt");
    private final File orderFile = new File(dir, "orders.txt");

    /** Last error text (null if none) so the UI can show a JOptionPane. */
    private String lastError;

    public String getLastError() {
        return lastError;
    }

    private static String clean(String s) {
        return s.replace("|", "/").replace("\n", " ");
    }

    // ---------- cart ----------
    public void saveCart(LinkedList<CartItem> items) {
        try {
            ensureDir();
            try (BufferedWriter w = Files.newBufferedWriter(cartFile.toPath(), StandardCharsets.UTF_8)) {
                for (CartItem i : items) {
                    w.write(i.getDish().getId() + "|" + i.getQty() + "|"
                            + (i.getDoneness() == null ? "" : i.getDoneness().name()));
                    w.newLine();
                }
            }
            lastError = null;
        } catch (IOException e) {
            lastError = "Could not save the cart: " + e.getMessage();
        }
    }

    public LinkedList<CartItem> loadCart(Menu menu) {
        LinkedList<CartItem> result = new LinkedList<>();
        if (!cartFile.exists())
            return result;
        try (BufferedReader r = Files.newBufferedReader(cartFile.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split("\\|", -1);
                if (p.length < 3)
                    continue;
                Dish d = menu.findById(p[0]);
                int qty = parseInt(p[1], 0);
                if (d == null || qty < 1)
                    continue; // skip bad / unknown lines
                result.add(new CartItem(d, Math.min(qty, CartManager.MAX_QTY), Doneness.fromString(p[2])));
            }
        } catch (IOException e) {
            lastError = "Could not load the cart: " + e.getMessage();
        }
        return result;
    }

    // ---------- orders ----------
    public void saveOrders(Queue<Order> orders) {
        try {
            ensureDir();
            try (BufferedWriter w = Files.newBufferedWriter(orderFile.toPath(), StandardCharsets.UTF_8)) {
                for (Order o : orders) {
                    w.write("ORDER|" + o.getID() + "|" + o.getOrderType().name() + "|" + o.getTotal() + "|"
                            + clean(o.getTimeStamp()));
                    w.newLine();
                    for (CartItem i : o.getItems()) {
                        Dish d = i.getDish();
                        w.write("ITEM|" + d.getId() + "|" + clean(d.getName()) + "|" + d.getPrice() + "|"
                                + i.getQty() + "|" + (i.getDoneness() == null ? "" : i.getDoneness().name()));
                        w.newLine();
                    }
                }
            }
            lastError = null;
        } catch (IOException e) {
            lastError = "Could not save the orders: " + e.getMessage();
        }
    }

    /**
     * Loads the orders and tells the OrderManager what the next order number is.
     */
    public void loadOrders(Menu menu, OrderManagement manager) {
        Queue<Order> queue = new Queue<>();
        int maxNumber = 0;
        if (orderFile.exists()) {
            try (BufferedReader r = Files.newBufferedReader(orderFile.toPath(), StandardCharsets.UTF_8)) {
                String line, id = null, time = "";
                OrderType type = OrderType.DINE_IN;
                double total = 0;
                LinkedList<CartItem> items = null;
                while ((line = r.readLine()) != null) {
                    String[] p = line.split("\\|", -1);
                    if (p[0].equals("ORDER") && p.length >= 5) {
                        if (id != null) {
                            queue.enqueue(new Order(id, type, items, total, time));
                        }
                        id = p[1];
                        type = OrderType.fromName(p[2]);
                        total = parseDouble(p[3]);
                        time = p[4];
                        items = new LinkedList<>();
                        maxNumber = Math.max(maxNumber, parseInt(id.replaceAll("\\D", ""), 0));
                    } else if (p[0].equals("ITEM") && p.length >= 6 && items != null) {
                        Dish d = menu.findById(p[1]);
                        if (d == null)
                            d = new Dish(p[1], p[2], parseDouble(p[3]), "Main Dish", "", null);
                        items.add(new CartItem(d, Math.max(1, parseInt(p[4], 1)), Doneness.fromString(p[5])));
                    }
                }
                if (id != null) {
                    queue.enqueue(new Order(id, type, items, total, time));
                }
            } catch (IOException e) {
                lastError = "Could not load the orders: " + e.getMessage();
            }
        }
        manager.restore(queue, maxNumber + 1);
    }

    private void ensureDir() throws IOException {
        if (!dir.exists() && !dir.mkdirs())
            throw new IOException("cannot create folder 'data'");
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static double parseDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
