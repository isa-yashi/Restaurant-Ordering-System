package orderingpage.dblogic;

import java.util.Comparator;

public class SortAlgo {
    private SortAlgo() {
    } // Prevent instantiation

    // insertion sort
    public static <T> void insertionSort(T[] a, Comparator<? super T> cmp) {
        for (int i = 1; i < a.length; i++) {
            T key = a[i];
            int j = i - 1;
            while (j >= 0 && cmp.compare(a[j], key) > 0) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    // quicksort
    public static <T> void quickSort(T[] a, Comparator<? super T> cmp) {
        quickSort(a, 0, a.length - 1, cmp);
    }

    private static <T> void quickSort(T[] a, int low, int high, Comparator<? super T> cmp) {
        if (low >= high)
            return;
        T pivot = a[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (cmp.compare(a[j], pivot) <= 0) {
                i++;
                T temp = a[i];
                a[i] = a[j];
                a[j] = temp;
            }
        }
        T temp = a[i + 1];
        a[i + 1] = a[high];
        a[high] = temp;
        int p = i + 1;
        quickSort(a, low, p - 1, cmp);
        quickSort(a, p + 1, high, cmp);
    }

}
