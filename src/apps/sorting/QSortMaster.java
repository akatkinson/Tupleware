/**
 *  QSortMaster.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *
 *  Created:  22/10/2007
 *  Modified: 26/10/2007
 */

package apps.sorting;

import space.*;
import runtime.*;
import java.util.*;
import com.clarkware.profiler.*;

public class QSortMaster {
    private TupleSpaceRuntime ts;
    private int port;
    private int values;
    private int threshold;

    public QSortMaster(int port, int values, int threshold) {
        this.port = port;
        ts = new TupleSpaceRuntime(port, true);

        this.values = values;
        this.threshold = threshold;
    }

    public void start() {
        Profiler.begin("QSortMaster::TotalRuntime");

        System.out.println("Master process started.");

        // construct new quicksort object
        System.out.print("Initialising values...");
        Profiler.begin("Data Init");
        int[] a = new int[values];
        QSort.initData(a, values);
        QSort qs = new QSort(a, threshold);
        Profiler.end("Data Init");
        System.out.println("Done.");

        ts.start();
        // write to tuple space
        System.out.print("Adding data to tuplespace...");
        ts.outRand(new Tuple("qsort", qs, "unsorted"));
        System.out.println("Done.");
        a = null;
        qs = null;

        // collect sorted sections of array
        int n = 0;
        Vector<int[]> sortedPartitions = new Vector<int[]>();
        System.out.print("Waiting for data to be sorted...");
        while(n < values) {
            Tuple t = ts.ts.in(new TupleTemplate("qsort", "sorted", null));
            QSort q = (QSort) t.field(2);
            sortedPartitions.addElement(q.getData());
            n += q.getData().length;
            System.out.println("collected " + n + " elements.");
        }
        System.out.println("Done. " + sortedPartitions.size() + " partitions collected.");

        // distribute poison pill
        ts.outRand(new Tuple("qsort", new QSort(),"complete"));

        // reconstruct array
        a = reconstructArray(sortedPartitions);

        //System.out.println("Sorted array contents ("+ a.length +" elements):");
        //System.out.println(Arrays.toString(a));

        ts.stop();
        Profiler.end("QSortMaster::TotalRuntime");

        Profiler.print();
        //System.exit(0);
    }

    private int[] reconstructArray(Vector<int[]> parts) {
        Profiler.begin("QSortMaster::reconstructArray()");
        int[] res = new int[values];
        int[] nextPart;
        int n = 0;
        do {
            nextPart = nextPartition(parts);
            for(int i = 0; i < nextPart.length; i++) {
                res[n] = nextPart[i];
                n++;
            }
        } while((nextPart != null) && (n < values));

        Profiler.end("QSortMaster::reconstructArray()");
        return res;
    }

    private int[] nextPartition(Vector<int[]> parts) {
        if(parts.isEmpty())
            return null;

        if(parts.size() == 1)
            return parts.elementAt(0);

        int low = 0;
        for(int i = 1; i < parts.size(); i++) {
            if(parts.elementAt(i)[0] < parts.elementAt(low)[0])
                low = i;
        }

        return parts.remove(low);
    }

    public static void main(String[] args) {
        if(args.length != 3) {
            System.out.println("Usage: java apps.sorting.QSortMaster <PORT> <VALUES> <TERMINATION SIZE>");
            System.exit(1);
        }

        int port        = Integer.parseInt(args[0]);
        int values      = Integer.parseInt(args[1]);
        int threshold   = Integer.parseInt(args[2]);

        new QSortMaster(port, values, threshold).start();
    }
}
