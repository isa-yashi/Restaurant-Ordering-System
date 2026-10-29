package restaurant.orderingpage.dblogic;

import restaurant.orderingpage.dblogic.DataStructure.LinkedList;
import restaurant.orderingpage.dishesandmenu.Dish;

public final class SearchAlgo {
    private SearchAlgo() {} // Prevent instantiation

    public static LinkedList<Dish> LinearSearch(LinkedList<Dish> dishes, String keyword){
        LinkedList<Dish> result = new LinkedList<>();
        String key = keyword == null ? "" : keyword.trim().toLowerCase();
        for(Dish d : dishes){
            if(key.isEmpty() || d.getName().toLowerCase().contains(key) || d.getDescription().toLowerCase().contains(key)){
                result.add(d);
            }
        }
        return result;
    }

    public static int binarySearchByName(Dish[] sortedByName, String name){
        int low = 0, high = sortedByName.length - 1;
        while(low <= high){
            int mid = (low + high) >>> 1;
            int cmp = sortedByName[mid].getName().compareToIgnoreCase(name);
            if(cmp == 0) return mid;
            if(cmp < 0) low = mid + 1;
            else high = mid - 1;
        }
        return -1;
    }
}
