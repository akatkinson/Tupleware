/**
 *  MergesortWorker.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *
 *  Created:  19/11/2007
 *  Modified: 22/11/2007
 */

package apps.sorting;

import space.*;
import runtime.*;
import java.util.*;
import com.clarkware.profiler.*;

public class MergesortWorker {
    private TupleSpaceRuntime ts;
    private int port;

    public MergesortWorker(int port) {
        this.port = port;
        ts = new TupleSpaceRuntime(port, false);
    }

    public void start() {
        ts.start();

        // try to obtain counter
        Tuple count = ts.in(new TupleTemplate("mergesort", "count", null));

        // try to obtain two unsorted array partitions
        //int[] a = ts.gts.in();

        // merge together so sorted

        // return sorted partition to space

        // repeat until no unsorted partitions remain

        ts.stop();
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: java apps.sorting.MergesortWorker <PORT>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        new MergesortWorker(port).start();
    }
}
