/**
 *  QSort.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *
 *  Created:  22/10/2007
 *  Modified: 5/11/2007
 */

package apps.sorting;

import space.*;
import runtime.*;
import java.util.*;
import com.clarkware.profiler.*;

public class QSort implements java.io.Serializable {
    private int[] a;
    private int threshold;

    // empty constructor - needed to enable poison pill
    public QSort() {}

    public QSort(int[] a, int threshold) {
        this.a = a;
        this.threshold = threshold;
    }

    public static void quicksort(int[] a, int p, int r) {
        if(p < r) {
            int q = partition(a, p, r);
            quicksort(a, p, q);
            quicksort(a, q+1, r);
        }
    }

    public static int partition(int[] a, int p, int r) {
        int x = a[p];
        int i = p - 1;
        int j = r + 1;

        while(true) {
            do { --j; } while (a[j] > x);
            do { ++i; } while (a[i] < x);
            if(i < j) {
                int temp = a[i];
                a[i] = a[j];
                a[j] = temp;
            } else {
                return j;
            }
        }
    }

    public static void bubbleSort(int[] a) {
        boolean swapped;

        do {
            swapped = false;

            for(int i = 0; i < a.length-1; i++) {
                if(a[i] > a[i+1]) {
                    int temp = a[i];
                    a[i] = a[i+1];
                    a[i+1] = temp;
                    swapped = true;
                }
            }
        } while(swapped);
    }

    public static void insertionSort(int[] a) {
        for(int i = 1; i < a.length; i++) {
            int value = a[i];
            int j = i-1;

            while((j >= 0) && (a[j] > value)) {
                a[j+1] = a[j];
                --j;
            }
            a[j+1] = value;
        }
    }

    public static void shellSort(int[] a) {
        for(int inc = a.length/2; inc > 0;
             inc = (inc == 2)? 1 : (int) Math.round(inc/2.2))
        {
            for(int i = inc; i < a.length; i++) {
                for(int j = i; j >= inc && a[j-inc] > a[j]; j -= inc) {
                    int temp = a[j];
                    a[j] = a[j-inc];
                    a[j-inc] = temp;
                }
            }
        }
    }

    public static int[] merge(int[] a, int[] b) {
        int[] c = new int[a.length + b.length];
        int aIndex = 0;
        int bIndex = 0;
        int cIndex = 0;

        while((aIndex < a.length) && (bIndex < b.length)) {
            if(a[aIndex] < b[bIndex]) {
                c[cIndex] = a[aIndex];
                cIndex++;
                aIndex++;
            } else {
                c[cIndex] = b[bIndex];
                cIndex++;
                bIndex++;
            }
        }

        if(aIndex < a.length-1) {
            for(int i = aIndex; i < a.length; i++) {
                c[cIndex] = a[i];
                cIndex++;
            }
        }

        if(bIndex < b.length-1) {
            for(int i = bIndex; i < b.length; i++) {
                c[cIndex] = b[i];
                cIndex++;
            }
        }

        return c;
    }

    public static void initData(int[] a, int n) {
        if((a == null) || (a.length < n))
            a = new int[n];

        Random rand = new Random(System.currentTimeMillis());

        for(int i = 0; i < a.length; i++)
            a[i] = rand.nextInt();
    }

    public QSort split() {
        int q = partition(a, 0, a.length-1);
        int[] b = Arrays.copyOfRange(a, 0, q+1);
        int[] c = Arrays.copyOfRange(a, q+1, a.length);

        QSort dup;
        if(b.length > c.length) {
            a = b;
            dup = new QSort(c, threshold);
        } else {
            a = c;
            dup = new QSort(b, threshold);
        }

        return dup;
    }

    public boolean readyToSort() {
        return (a.length <= threshold);
    }

    public int[] getData() {
        return a;
    }

    public int size() { return a.length; }
}
