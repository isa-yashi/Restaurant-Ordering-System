package orderinghistory.model;

/**
 * PURPOSE: One item line of a finished order, saved as a SNAPSHOT.
 *
 * It copies the name, price and picture of the dish at the moment the order
 * was finished, so the history never changes later, even if the menu prices
 * are edited or a dish is removed from the menu.
 */

public class HistoryItem {

    private final String dishId;
    private final String name;
    private final double price; // price of ONE piece at the time of the order
    private final int qty;
    private final String doneness; // "Rare", "Medium", "Well Done" or "" when the dish has none
    private final String imgSrc; // same path style the menu uses, e.g. lib/images/RibEyeSteak.jpg

    public HistoryItem(String dishId, String name, double price, int qty, String doneness, String imgSrc) {
        this.dishId = dishId;
        this.name = name;
        this.price = price;
        this.qty = qty;
        this.doneness = doneness == null ? "" : doneness;
        this.imgSrc = imgSrc;
    }

    // getters
    public String getDishId() {
        return dishId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQty() {
        return qty;
    }

    public String getDoneness() {
        return doneness;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public double getLineTotal() {
        return price * qty;
    }
}

// for history order
/**
 * PURPOSE: One finished order stored in the order history (this is what gets
 * pushed onto the history stack).
 *
 * An order lands here when it leaves the Order Queue as Served, Out for
 * delivery or Cancelled. Read-only on purpose: history is a record and must
 * not be edited afterwards.
 */
