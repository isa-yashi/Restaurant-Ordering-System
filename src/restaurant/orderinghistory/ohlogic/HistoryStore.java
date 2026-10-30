package orderinghistory.ohlogic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import orderinghistory.model.HistoryItem;
import orderinghistory.model.HistoryOrder;
import orderingpage.dblogic.DataStructure.LinkedList;
import orderingpage.dblogic.OrderManagement.OrderType;

/**
 * PURPOSE: Saves / loads the order history as a plain text file in the "data"
 * folder (same idea as DataStore), so the history is still there after
 * closing the program.
 *
 * data/history.txt, oldest order first:
 * ORDER|id|TYPE|total|timestamp|status|paymentStatus|paymentMode
 * ITEM|dishId|name|price|quantity|DONENESS|imageSource
 *
 * Because it is written oldest first, loading just pushes line by line and the
 * newest order ends up on top of the stack again.
 */

public class HistoryStore {
    private final File dir = new File("data");
    private final File historyFile = new File(dir, "history.txt");

    /** Last error text (null if none) so the UI can show a JOptionPane. */
    private String lastError;

    public String getLastError() {
        return lastError;
    }

    private static String clean(String s) {
        return s == null ? "" : s.replace("|", "/").replace("\n", " ");
    }

    public void save(OrderHistoryManagement history) {
        try {
            if (!dir.exists() && !dir.mkdirs())
                throw new IOException("cannot create folder 'data'");

            try (BufferedWriter w = Files.newBufferedWriter(historyFile.toPath(), StandardCharsets.UTF_8)) {
                for (HistoryOrder o : history.oldestFirst()) {
                    w.write("ORDER|" + o.getID() + "|" + o.getOrderType().name() + "|" + o.getTotal() + "|"
                            + clean(o.getTimeStamp()) + "|" + clean(o.getStatus()) + "|"
                            + clean(o.getPaymentStatus()) + "|" + clean(o.getPaymentMode()));
                    w.newLine();
                    for (HistoryItem i : o.getItems()) {
                        w.write("ITEM|" + clean(i.getDishId()) + "|" + clean(i.getName()) + "|" + i.getPrice() + "|"
                                + i.getQty() + "|" + clean(i.getDoneness()) + "|" + clean(i.getImgSrc()));
                        w.newLine();
                    }
                }
            }
            lastError = null;
        } catch (IOException e) {
            lastError = "Could not save the order history: " + e.getMessage();
        }
    }

    /**
     * Fills the (empty) history from data/history.txt. Missing file = empty
     * history.
     */
    public void load(OrderHistoryManagement history) {
        if (!historyFile.exists())
            return;

        try (BufferedReader r = Files.newBufferedReader(historyFile.toPath(), StandardCharsets.UTF_8)) {
            String line;
            String id = null, time = "", status = "", payStatus = "", payMode = "";
            OrderType type = OrderType.DINE_IN;
            double total = 0;
            LinkedList<HistoryItem> items = null;

            while ((line = r.readLine()) != null) {
                String[] p = line.split("\\|", -1);
                if (p[0].equals("ORDER") && p.length >= 8) {
                    if (id != null) // the previous order is complete, store it
                        history.push(new HistoryOrder(id, type, items, total, time, status, payStatus, payMode));
                    id = p[1];
                    type = OrderType.fromName(p[2]);
                    total = parseDouble(p[3]);
                    time = p[4];
                    status = p[5];
                    payStatus = p[6];
                    payMode = p[7];
                    items = new LinkedList<>();
                } else if (p[0].equals("ITEM") && p.length >= 7 && items != null) {
                    items.add(new HistoryItem(p[1], p[2], parseDouble(p[3]), Math.max(1, parseInt(p[4], 1)),
                            p[5], p[6].isEmpty() ? null : p[6]));
                }
            }
            if (id != null)
                history.push(new HistoryOrder(id, type, items, total, time, status, payStatus, payMode));
            lastError = null;
        } catch (IOException e) {
            lastError = "Could not load the order history: " + e.getMessage();
        }
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
