package orderinghistory.ohlogic;

import orderinghistory.model.HistoryItem;
import orderinghistory.model.HistoryOrder;
import orderingpage.cart.CartItem;
import orderingpage.dblogic.DataStructure.LinkedList;
import orderingpage.dblogic.DataStructure.Stack;
import orderingpage.dblogic.OrderManagement.Order;
import orderingpage.dblogic.OrderManagement.OrderType;
import orderingpage.dishesandmenu.Dish;

/**
 * PURPOSE: The brain of the Order History page. Mirrors OrderManagement
 * (which owns the Queue), but this one owns a STACK (LIFO).
 *
 * The newest finished order sits on top of the stack, so it is the FIRST card
 * in the Order History list. Reading methods (search, oldestFirst) walk the
 * stack and never pop anything.
 *
 * Flow: Order Queue -> record(order) -> push on the stack -> OrderHistoryUI.
 */
public class OrderHistoryManagement {

    // The three tabs of the page.
    public enum StatusFilter {
        ALL, COMPLETED, CANCELLED
    }

    private Stack<HistoryOrder> history = new Stack<>();

    public Stack<HistoryOrder> getHistory() {
        return history;
    }

    public int size() {
        return history.size();
    }

    public boolean isEmpty() {
        return history.isEmpty();
    }

    /**
     * Called when an order leaves the queue as Served / Out for delivery /
     * Cancelled.
     * Takes a SNAPSHOT of it and pushes it on top of the stack.
     *
     * @return the stored snapshot, or null when the order was not finished yet
     *         or is already in the history (so it can never be added twice)
     */

    public HistoryOrder record(Order order) {
        if (order == null || !order.isFinished() || contains(order.getID()))
            return null;

        LinkedList<HistoryItem> lines = new LinkedList<>();
        for (CartItem i : order.getItems()) {
            Dish d = i.getDish();
            lines.add(new HistoryItem(d.getId(), d.getName(), d.getPrice(), i.getQty(),
                    i.getDoneness() == null ? "" : i.getDoneness().getLabel(), d.getImgSrc()));
        }
        HistoryOrder entry = new HistoryOrder(order.getID(), order.getOrderType(), lines, order.getTotal(),
                order.getTimeStamp(), order.getStatus(), order.getPaymentStatus(), order.getPaymentMode());
        history.push(entry);
        return entry;
    }

    // Pushes an already-built entry (used when loading history.txt).
    public boolean push(HistoryOrder entry) {
        if (entry == null || contains(entry.getID()))
            return false;
        history.push(entry);
        return true;
    }

    public boolean contains(String orderId) {
        for (HistoryOrder o : history)
            if (o.getID().equals(orderId))
                return true;
        return false;
    }

    /**
     * The three controls of the page (tab, dining option, search box) combined
     * with AND. The result is newest first, same as the stack order.
     *
     * @param status null or ALL = every order
     * @param type   null = every dining option
     * @param text   matches the order ID or any item name (case-insensitive); blank
     *               = no search
     */
    public LinkedList<HistoryOrder> search(StatusFilter status, OrderType type, String text) {
        LinkedList<HistoryOrder> result = new LinkedList<>();
        String key = text == null ? "" : text.trim().toLowerCase();

        for (HistoryOrder o : history) { // top of the stack first = newest first
            if (status == StatusFilter.COMPLETED && o.isCancelled())
                continue;
            if (status == StatusFilter.CANCELLED && !o.isCancelled())
                continue;
            if (type != null && o.getOrderType() != type)
                continue;
            if (!key.isEmpty() && !matches(o, key))
                continue;
            result.add(o);
        }
        return result;
    }

    private boolean matches(HistoryOrder order, String key) {
        if (order.getID().toLowerCase().contains(key))
            return true;
        for (HistoryItem item : order.getItems())
            if (item.getName().toLowerCase().contains(key))
                return true;
        return false;
    }

    /**
     * Bottom of the stack first (oldest order first). Used to write history.txt in
     * time order.
     */
    public LinkedList<HistoryOrder> oldestFirst() {
        LinkedList<HistoryOrder> result = new LinkedList<>();
        for (HistoryOrder o : history)
            result.addFirst(o); // walking top -> bottom and adding to the front reverses the order
        return result;
    }

    /**
     * Highest number used in any order ID here (D006 -> 6). New orders must
     * continue after it.
     */
    public int highestOrderNumber() {
        int max = 0;
        for (HistoryOrder o : history) {
            String digits = o.getID().replaceAll("\\D", "");
            if (digits.isEmpty())
                continue;
            try {
                max = Math.max(max, Integer.parseInt(digits));
            } catch (NumberFormatException ignored) {
            }
        }
        return max;
    }
}
