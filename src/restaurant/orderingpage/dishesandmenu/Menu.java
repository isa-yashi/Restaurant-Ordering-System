package restaurant.orderingpage.dishesandmenu;

import java.util.Comparator;
import restaurant.orderingpage.dblogic.DataStructure.LinkedList;
import restaurant.orderingpage.dblogic.SearchAlgo;
import restaurant.orderingpage.dblogic.SortAlgo;


public class Menu {
    public enum SortOption{
        MENU_ORDER("Menu Order"),
        NAME_ASC("Name (A-Z)"),
        PRICE_ASC("Price (Low to High)"),
        PRICE_DESC("Price (High to Low)");

        private final String label;
        private SortOption(String label) {
            this.label = label;
        }
        @Override public String toString() { return label; }
    }

    private final LinkedList<Dish> menu = new LinkedList<>();

    public Menu(){
        seedMenu();
    }

    //adding the menu items to the menu list
    private void seedMenu(){
        String d = "w/ a side of onions";
        menu.add(new SteakDish("S001", "Rib Eye Steak", 240, d, "lib/images/RibEyeSteak.jpg"));
        menu.add(new SteakDish("S002", "Fillet Mignon Steak", 399, d, "lib/images/FilletMignon.jpg"));
        menu.add(new SteakDish("S003", "Surf & Turf Steak", 559, d, "lib/images/SurfSteak.jpg"));
        menu.add(new SteakDish("S004","T-Bone Steak", 199, d, "lib/images/BoneSteak.jpg"));
        menu.add(new SteakDish("S005", "N.Y Strip Steak", 229, d,"lib/images/nyStripSteak.jpg"));
        menu.add(new SteakDish("S006", "Prime Rib Steak", 267, d, "lib/images/PrimeRibSteak.jpg"));

        //drinks menu section
        menu.add(new Dish("D001", "Red Wine", 25, "Drinks", "A velvet-smooth, full-bodied pour bursting with rich dark fruits and a sophisticated, lingering finish", "lib/images/RedWine.jpg"));
        menu.add(new Dish("D002", "Espresso Martini", 17, "Drinks", "Fresh espresso meets premium vodka for a luxurious, velvety buzz.", "lib/images/EspressoMartini.jpg"));
        menu.add(new Dish("D003", "French '75", 23, "Drinks", "Crisp gin and bright lemon, elevated by real Champagne.", "lib/images/French75.jpg"));
        menu.add(new Dish("D004", "Sazerac", 39, "Drinks", "Bold, historic rye with a mesmerizing hint of anise.", "lib/images/Sazerac.jpg"));
        menu.add(new Dish("D005", "Old Fashioned", 21, "Drinks", "Timeless, smooth bourbon balanced by warm, aromatic bitters.", "lib/images/OldFashioned.jpg"));
        menu.add(new Dish("D006", "Boulevardier", 28, "Drinks", "A rich, bitter-sweet symphony of whiskey and vibrant botanicals", "lib/images/Boulevard.jpg"));

        //dessert
        menu.add(new Dish("E001", "Deconstructed Tart", 20, "Dessert", "Modern, crisp pastry shards paired with artful dollops of vibrant fruit curd.", "lib/images/DecoTart.jpg"));
        menu.add(new Dish("E002", "Classic Crème Brûlée", 26, "Dessert", "Velvety vanilla bean custard baked beneath a uniform, torched crust.", "lib/images/Recette2.jpg"));
        menu.add(new Dish("E003", "Mille-Feuille", 19, "Dessert", "Crisp, caramelized layers of puff pastry sandwiching a rich vanilla.", "lib/images/Millie.jpg"));
        menu.add(new Dish("E004", "Grand Marnier Soufflé", 27, "Dessert", "A towering, ultra-airy masterpiece punctured tableside with warm orange cream.", "lib/images/Grand.jpg"));
        
        //Soup and salad
        menu.add(new Dish("G001", "Mushroom Velouté", 36, "Soups & Salad", "Earthy forest mushrooms blended into a smooth cream laced with white truffle oil", "lib/images/Mushroom.jpg"));
        menu.add(new Dish("G002", "Lobster Bisque", 23, "Soups & Salad", "A velvety, rich seafood cream accented with sweet poached Maine lobster tail.", "lib/images/Lobster.jpg"));
        menu.add(new Dish("G003", "French Onion Soup", 18, "Soups & Salad", "Rich beef broth savory with caramelized onions under a blanket of Gruyère.", "lib/images/OnionSoup.jpg"));
        menu.add(new Dish("G004", "Heirloom Tomato Salad", 16, "Soups & Salad", "Vibrant garden tomatoes paired with creamy burrata cheese and aged balsamic oil.", "lib/images/tomato.jpg"));
        menu.add(new Dish("G005", "Shaved Fennel Salad", 19, "Soups & Salad", "Crisp fennel ribbons tossed with citrus segments, goat cheese, and mint leaves.", "lib/images/Fennel.jpg"));
        menu.add(new Dish("G006", "Wedge Salad", 21, "Soups & Salad", "Crisp iceberg lettuce topped with creamy blue cheese and smoky slab bacon.", "lib/images/Wedge.jpg"));

    }   

    public LinkedList<Dish> getAll() { return menu; }

    public Dish findById(String id){
        for(Dish d : menu){
            if(d.getId().equals(id)) return d;
        }
        return null;
    }

    public LinkedList<Dish> getVisible(String category, String keyword, SortOption option){
        LinkedList<Dish> found = SearchAlgo.LinearSearch(menu, keyword);
        LinkedList<Dish> inCategory = new LinkedList<>();

        for(Dish d : found){
            if(category == null || category.isEmpty() || d.getCategory().equalsIgnoreCase(category)){
                inCategory.add(d);
            }
            
        }
        return sort(inCategory, option);
    }

    private LinkedList<Dish> sort(LinkedList<Dish> list, SortOption option){    
        if (option == SortOption.MENU_ORDER || list.size() < 2) return list;
        Dish[] arr = new Dish[list.size()];

        for(int i=0; i < list.size(); i++){
            arr[i] = list.get(i);
        }

        //rule case instead of case
        switch(option){
            case NAME_ASC -> SortAlgo.insertionSort(arr, Comparator.comparing(Dish::getName, String.CASE_INSENSITIVE_ORDER));
            case PRICE_ASC -> SortAlgo.quickSort(arr, Comparator.comparingDouble(Dish::getPrice));
            case PRICE_DESC -> SortAlgo.quickSort(arr, Comparator.comparingDouble(Dish::getPrice).reversed());
            default -> {
            }
        }
        LinkedList<Dish> sortedList = new LinkedList<>();
        for(Dish d : arr) sortedList.add(d);
        return sortedList;
    }
}
