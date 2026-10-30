package orderinghistory.model;

import orderingpage.dblogic.DataStructure.LinkedList;
import orderingpage.dblogic.OrderManagement.OrderType;

public class HistoryOrder {
    // The final queue statuses that count as "Cancelled". Everything else
    // that reaches the history (Served, Out for delivery) counts as "Completed".
    public static final String CANCELLED = "Cancelled";
    private final String id;
    private final OrderType type;
    private final LinkedList<HistoryItem> items;
    private final double total;
    private final String timeStamp;
    private final String status;
    private final String paymentStatus;
    private final String paymentMode;

    public HistoryOrder(
            String id, OrderType type,
            LinkedList<HistoryItem> items, double total,
            String timeStamp, String status, String paymentStatus,
            String paymentMode) {

        this.id = id;
        this.type = type;
        this.items = items;
        this.total = total;
        this.timeStamp = timeStamp;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.paymentMode = paymentMode;
    }

    // getters
    public String getID() {
        return id;
    }

    public OrderType getOrderType() {
        return type;
    }

    public LinkedList<HistoryItem> getItems() {
        return items;
    }

    public double getTotal() {
        return total;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public boolean isCancelled() {
        return CANCELLED.equalsIgnoreCase(status);
    }

    // "Total items N" on the card = number of item lines, like the design
    public int getItemCount() {
        return items.size();
    }
}
