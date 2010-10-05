/**
 *  QSortLocal.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *
 *  Created:  31/10/2007
 *  Modified: 31/10/2007
 */

package apps.sorting;

import com.clarkware.profiler.*;
import java.util.Arrays;

public class QSortLocal {
    private int numVals;

    public QSortLocal(int vals) {
        numVals = vals;
    }

    public void start() {
        int[] a = new int[numVals];
        QSort.initData(a, numVals);
        //int[] b = Arrays.copyOf(a, a.length);
        //int[] c = Arrays.copyOf(a, a.length);
        //int[] d = Arrays.copyOf(a, a.length);

        /*Profiler.begin("QSort::quicksort");
        //QSort.quicksort(a, 0, a.length-1);
        Profiler.end("QSort::quicksort");*/

        /*Profiler.begin("QSort::bubblesort");
        QSort.bubbleSort(b);
        Profiler.end("QSort::bubblesort");*/

        Profiler.begin("QSort::insertionsort");
        QSort.insertionSort(a);
        Profiler.end("QSort::insertionsort");

        /*Profiler.begin("QSort::shellsort");
        QSort.shellSort(b);
        Profiler.end("QSort::shellsort");*/
        Profiler.print();
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: java apps.sorting.QSortWorker <VALUES>");
            System.exit(1);
        }

        int vals = Integer.parseInt(args[0]);

        new QSortLocal(vals).start();
    }
}
