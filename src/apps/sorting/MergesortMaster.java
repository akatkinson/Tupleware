/**
 *  MergesortMaster.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *
 *  Created:  19/11/2007
 *  Modified: 29/11/2007
 */

package apps.sorting;

import space.*;
import runtime.*;
import java.util.*;
import com.clarkware.profiler.*;

public class MergesortMaster {
    private int vals;
    private int port;
    private TupleSpaceRuntime ts;

    public MergesortMaster(int port, int vals) {
        this.vals = vals;
        this.port = port;
        ts = new TupleSpaceRuntime(port, true);
    }

    public void start() {
        ts.start();

        // initialise values
        int[] a = new int[vals];
        QSort.initData(a, vals);
        Vector<int[]> parts = producePartitions(a);
        for(int[] part : parts)
            ts.ts.out(new Tuple("mergesort", part, new Integer(part.length)));

        ts.ts.out(new Tuple("mergesort", "count", parts.size()));

        // wait for fully sorted array

        ts.ts.in(new TupleTemplate("mergesort", null, new Integer(vals)));

        ts.outEach(new Tuple("mergesort", "complete"));

        ts.stop();
    }

    private Vector<int[]> producePartitions(int[] a) {
        Vector<int[]> parts = new Vector<int[]>();

        return parts;
    }

    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("Usage: java apps.sorting.MergesortMaster <PORT> <VALUES>");
            System.exit(1);
        }

        int port        = Integer.parseInt(args[0]);
        int values      = Integer.parseInt(args[1]);

        new MergesortMaster(port, values).start();
    }
}
