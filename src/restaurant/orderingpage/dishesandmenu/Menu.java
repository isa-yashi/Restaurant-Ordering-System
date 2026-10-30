package orderingpage.dishesandmenu;

import java.util.Comparator;
import orderingpage.dblogic.DataStructure.LinkedList;
import orderingpage.dblogic.SearchAlgo;
import orderingpage.dblogic.SortAlgo;

public class Menu {
    public enum SortOption {
        MENU_ORDER("Menu Order"),
        NAME_ASC("Name (A-Z)"),
        PRICE_ASC("Price (Low to High)"),
        PRICE_DESC("Price (High to Low)");

        private final String label;

        private SortOption(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final LinkedList<Dish> menu = new LinkedList<>();

    public Menu() {
        seedMenu();
    }

    // adding the menu items to the menu list
    private void seedMenu() {
        String d = "w/ a side of onions";
        menu.add(new SteakDish("S001", "Rib Eye Steak", 240, d,
                "https://i.pinimg.com/736x/d6/64/3c/d6643cfef8e19ebfb51b8b0a17f275c2.jpg"));
        menu.add(new SteakDish("S002", "Fillet Mignon Steak", 399, d,
                "https://i.pinimg.com/736x/97/7f/7f/977f7f4ebfc8b13f6f7467f4de184846.jpg"));
        menu.add(new SteakDish("S003", "Surf & Turf Steak", 559, d,
                "https://i.pinimg.com/1200x/bb/5d/08/bb5d084870a3ec914211b3d7e2dc1fa9.jpg"));
        menu.add(new SteakDish("S004", "T-Bone Steak", 199, d,
                "https://i.pinimg.com/236x/fd/f7/02/fdf702f3715ee4baf3efe67d844aac3e.jpg"));
        menu.add(new SteakDish("S005", "N.Y Strip Steak", 229, d,
                "https://i.pinimg.com/236x/36/12/bf/3612bf37236e9c100da54b7feab9e08c.jpg"));
        menu.add(new SteakDish("S006", "Prime Rib Steak", 267, d,
                "https://i.pinimg.com/1200x/91/de/27/91de27226ab8b3a3e1564b1201452bae.jpg"));

        // drinks menu section
        menu.add(new Dish("D001", "Red Wine", 25, "Drinks",
                "A velvet-smooth, full-bodied pour bursting with rich dark fruits and a sophisticated, lingering finish",
                "https://i.pinimg.com/1200x/53/64/87/536487eb38ee8e5841798fdfeeab32c3.jpg"));
        menu.add(new Dish("D002", "Espresso Martini", 17, "Drinks",
                "Fresh espresso meets premium vodka for a luxurious, velvety buzz.",
                "https://i.pinimg.com/736x/48/98/ab/4898ab5693ff617ac345e2cd83021837.jpg"));
        menu.add(new Dish("D003", "French '75", 23, "Drinks", "Crisp gin and bright lemon, elevated by real Champagne.",
                "https://i.pinimg.com/236x/b8/02/01/b80201c13df61fd32f816b0fd4112e0c.jpg"));
        menu.add(new Dish("D004", "Sazerac", 39, "Drinks", "Bold, historic rye with a mesmerizing hint of anise.",
                "https://i.pinimg.com/1200x/46/c0/cb/46c0cb0519aaaaec5735896f5ad4b4b8.jpg"));
        menu.add(new Dish("D005", "Old Fashioned", 21, "Drinks",
                "Timeless, smooth bourbon balanced by warm, aromatic bitters.",
                "https://i.pinimg.com/1200x/bf/cd/85/bfcd85edcac65e04065c1dd2d12227c8.jpg"));
        menu.add(new Dish("D006", "Boulevardier", 28, "Drinks",
                "A rich, bitter-sweet symphony of whiskey and vibrant botanicals",
                "https://i.pinimg.com/1200x/16/3e/0a/163e0a27b73c4fedeb56e2447feb8583.jpg"));

        // dessert
        menu.add(new Dish("E001", "Deconstructed Tart", 20, "Dessert",
                "Modern, crisp pastry shards paired with artful dollops of vibrant fruit curd.",
                "https://i.pinimg.com/736x/06/f8/d6/06f8d63bac6cb97df75f3666439c380b.jpg"));
        menu.add(new Dish("E002", "Classic Crème Brûlée", 26, "Dessert",
                "Velvety vanilla bean custard baked beneath a uniform, torched crust.",
                "https://i.pinimg.com/1200x/05/06/55/05065523b13d7f7f2bfe0d4af544e18b.jpg"));
        menu.add(new Dish("E003", "Mille-Feuille", 19, "Dessert",
                "Crisp, caramelized layers of puff pastry sandwiching a rich vanilla.",
                "https://i.pinimg.com/1200x/aa/64/07/aa640786890f8625799f9e2fca4e3009.jpg"));
        menu.add(new Dish("E004", "Grand Marnier Soufflé", 27, "Dessert",
                "A towering, ultra-airy masterpiece punctured tableside with warm orange cream.",
                "https://i.pinimg.com/736x/87/81/1c/87811c97479b4f5f11380d3c9af9a81f.jpg"));

        // Soup and salad
        menu.add(new Dish("G001", "Mushroom Velouté", 36, "Soups & Salad",
                "Earthy forest mushrooms blended into a smooth cream laced with white truffle oil",
                "https://i.pinimg.com/736x/db/c8/eb/dbc8eb73798b4e2d21bcca78b2aa7bd7.jpg"));
        menu.add(new Dish("G002", "Lobster Bisque", 23, "Soups & Salad",
                "A velvety, rich seafood cream accented with sweet poached Maine lobster tail.",
                "https://i.pinimg.com/1200x/be/76/57/be7657c1e0bda5dc149d079a8a9d80f0.jpg"));
        menu.add(new Dish("G003", "French Onion Soup", 18, "Soups & Salad",
                "Rich beef broth savory with caramelized onions under a blanket of Gruyère.",
                "https://i.pinimg.com/736x/63/23/7d/63237dbb50b82d3032b0ddbab3870edf.jpg"));
        menu.add(new Dish("G004", "Heirloom Tomato Salad", 16, "Soups & Salad",
                "Vibrant garden tomatoes paired with creamy burrata cheese and aged balsamic oil.",
                "https://i.pinimg.com/736x/9f/02/44/9f024407c9c664685e258f7b561c6c51.jpg"));
        menu.add(new Dish("G005", "Shaved Fennel Salad", 19, "Soups & Salad",
                "Crisp fennel ribbons tossed with citrus segments, goat cheese, and mint leaves.",
                "https://i.pinimg.com/736x/d1/3a/ce/d13aceb62189f61f8f54780cf287bdbc.jpg"));
        menu.add(new Dish("G006", "Wedge Salad", 21, "Soups & Salad",
                "Crisp iceberg lettuce topped with creamy blue cheese and smoky slab bacon.",
                "https://i.pinimg.com/1200x/de/0f/36/de0f3671f9115ff408fd3ae348e193a4.jpg"));

    }

    public LinkedList<Dish> getAll() {
        return menu;
    }

    public Dish findById(String id) {
        for (Dish d : menu) {
            if (d.getId().equals(id))
                return d;
        }
        return null;
    }

    public LinkedList<Dish> getVisible(String category, String keyword, SortOption option) {
        LinkedList<Dish> found = SearchAlgo.LinearSearch(menu, keyword);
        LinkedList<Dish> inCategory = new LinkedList<>();

        for (Dish d : found) {
            if (category == null || category.isEmpty() || d.getCategory().equalsIgnoreCase(category)) {
                inCategory.add(d);
            }

        }
        return sort(inCategory, option);
    }

    private LinkedList<Dish> sort(LinkedList<Dish> list, SortOption option) {
        if (option == SortOption.MENU_ORDER || list.size() < 2)
            return list;
        Dish[] arr = new Dish[list.size()];

        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }

        // rule case instead of case
        switch (option) {
            case NAME_ASC ->
                SortAlgo.insertionSort(arr, Comparator.comparing(Dish::getName, String.CASE_INSENSITIVE_ORDER));
            case PRICE_ASC -> SortAlgo.quickSort(arr, Comparator.comparingDouble(Dish::getPrice));
            case PRICE_DESC -> SortAlgo.quickSort(arr, Comparator.comparingDouble(Dish::getPrice).reversed());
            default -> {
            }
        }
        LinkedList<Dish> sortedList = new LinkedList<>();
        for (Dish d : arr)
            sortedList.add(d);
        return sortedList;
    }
}
