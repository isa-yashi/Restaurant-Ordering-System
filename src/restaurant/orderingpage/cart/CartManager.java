package restaurant.orderingpage.cart;

import restaurant.orderingpage.dblogic.DataStructure.LinkedList;   // our own data structures,
import restaurant.orderingpage.dblogic.DataStructure.Stack;        // NOT java.util
import restaurant.orderingpage.dishesandmenu.Dish;
import restaurant.orderingpage.dishesandmenu.Dish.Doneness;

public class CartManager {
    public interface CartListener{
        void cartChanged();
    }

    public static final int MAX_QTY = 100;

    private final LinkedList<CartItem> items = new LinkedList<>();
    private final Stack<CartItem> removed = new Stack<>();
    private final LinkedList<CartListener> listener = new LinkedList<>();

    public void addListener(CartListener l) {
        listener.add(l);
    }

    //kada may changes na gagawin, use this method to update
    private void fire(){
        for(CartListener l : listener) {
            l.cartChanged();
        }
    }

    public LinkedList<CartItem> getItems(){
        return items;
    }

    public CartItem find(Dish dish){
        for(CartItem i : items){
            if(i.getDish().equals(dish)){
                return i;
            }
        }
        return null;
    }

    /** Adds the dish, or replaces its quantity/doneness if it is already there. */
    public void add(Dish dish, int qty, Doneness doneness){
        if(qty < 1) qty = 1;
        if(qty > MAX_QTY) qty = MAX_QTY;
        CartItem existing = find(dish);
        if(existing != null){
            existing.setQty(qty);
            existing.setDoneness(doneness);
        } else {
            items.add(new CartItem(dish, qty, doneness));
        }
        fire();
    }

    public void setQty(Dish dish, int qty){
        CartItem item = find(dish);
        if(item == null) return;
        if(qty <= 0) {          // 0 (or less) = remove it from the cart
            remove(dish);
            return;
        }
        item.setQty(Math.min(qty, MAX_QTY));
        fire();                 // tell the cart panel and the dish cards to refresh
    }

    public void setDoneness(Dish dish, Doneness d){
        CartItem item = find(dish);
        if(item == null) return;
        item.setDoneness(d);
        fire();
    }

    public boolean canUndoRemoved() {
        return !removed.isEmpty();
    }

    //removes dish
    public void remove(Dish dish){
        CartItem item = find(dish);
        if(item == null) return;
        items.remove(item);
        removed.push(item);
        fire();
    }

    //undo's removed dish
    public void undo(){
        if(removed.isEmpty()) return;
        CartItem back = removed.pop();
        if(find(back.getDish()) == null) items.add(back);
        fire();
    }

    //clearing the cart after an order has been placed.
    public void clear(){
        items.clear();
        removed.clear();
        fire();
    }

    //used when loading a saved cart at start up
    public void restoreCart(LinkedList<CartItem> saved){
        items.clear();
        for(CartItem i : saved) items.add(i);
        fire();
    }

    public int totalItems(){
        int n = 0;
        for(CartItem i : items) n += i.getQty();
        return n;
    }

    public double totalPrice(){
        double t = 0;
        for(CartItem i : items) t += i.getLineTotal();
        return t;
    }
}
