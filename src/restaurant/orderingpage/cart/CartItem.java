package restaurant.orderingpage.cart;

import restaurant.orderingpage.dishesandmenu.Dish;
import restaurant.orderingpage.dishesandmenu.Dish.Doneness;

public class CartItem {
    private final Dish dish;
    private int qty;
    private Doneness doneness;    //null when dish has no doneness

    public CartItem(Dish dish, int qty, Doneness doneness){
        this.dish = dish;
        this.qty = qty;
        this.doneness = dish.hasDoneness() ? (doneness == null ? Doneness.MEDIUM : doneness) : null; 
        //assigns items doneness, if no doneness or null; value is set to medium
        //HOW TO READ IT: condition ? valueIfTrue : valueIfFalse
    }


    //getters
    public Dish getDish() { 
        return dish; 
    }
    public int getQty(){ 
        return qty; 
    }
    public Doneness getDoneness() { 
        return doneness;
     }
    public double getLineTotal() { 
        return dish.getPrice() * qty; 
    }

    //setters
    public void setQty(int qty) { 
        this.qty = qty; 
    }
    public void setDoneness(Doneness doneness) {
        if(dish.hasDoneness()){
           this.doneness = doneness; 
        }
    }
}
