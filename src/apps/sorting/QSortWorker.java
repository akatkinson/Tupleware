/**
 *  QSortWorker.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *
 *  Created:  22/10/2007
 *  Modified: 26/10/2007
 */

package apps.sorting;

import space.*;
import runtime.*;
import java.io.*;
import com.clarkware.profiler.*;

public class QSortWorker {
    private TupleSpaceRuntime ts;
    private int port;

    public QSortWorker(int port) {
        this.port = port;
        ts = new TupleSpaceRuntime(port, false);
    }

    public void start() {
        Profiler.begin("QSortWorker::TotalRuntime");
        ts.start();

        System.out.println("Worker process started.");

        // get qsort tuple
        System.out.print("Attempting to fetch initial data...");
        //Profiler.begin("QSortWorker::IO");
        Profiler.begin("QSortWorker::Init");
        Tuple t = ts.in(new TupleTemplate("qsort", null, "unsorted"));
        Profiler.end("QSortWorker::Init");
        //Profiler.end("QSortWorker::IO");
        System.out.println("Done.");

        QSort qs = (QSort) t.field(1);
        String status = (String) t.field(2);

        while(!status.equals("complete")) {
            // sort OR split
            if(qs.readyToSort()) {
                System.out.println("Sorting " +qs.getData().length + " items.");
                Profiler.begin("QSortWorker::Sorting");
                Profiler.begin("QSortWorker::Processing");
                //qs.quicksort(qs.getData(), 0, qs.getData().length - 1);
                qs.insertionSort(qs.getData());
                Profiler.end("QSortWorker::Processing");
                Profiler.end("QSortWorker::Sorting");

                try {
                    Profiler.begin("QSortWorker::IO");
                    ts.gts.out(new Tuple("qsort", "sorted", qs));
                    Profiler.end("QSortWorker::IO");
                } catch(IOException e) {
                    System.out.println("Error returning sorted partition to master process");
                }

                System.out.print("Successfully sorted partition.\nAttempting to fetch more data...");

                // try to find unsorted partition to work on
                Profiler.begin("QSortWorker::IO");
                t = ts.in(new TupleTemplate("qsort", null, null));
                Profiler.end("QSortWorker::IO");

                System.out.println("Done.");

                qs = (QSort) t.field(1);
                status = (String) t.field(2);
            } else {
                Profiler.begin("QSortWorker::Partitioning");
                Profiler.begin("QSortWorker::Processing");
                System.out.print("Splitting array of size " +qs.size());
                QSort dup = qs.split();
                System.out.println(" into " + qs.size() + "/" + dup.size());
                Profiler.end("QSortWorker::Processing");
                Profiler.end("QSortWorker::Partitioning");

                System.out.println("Split partition..continuing...");

                ts.out(new Tuple("qsort", dup, "unsorted"));
            }
            System.out.print(".");
        }
        System.out.println();

        System.out.print("Worker process finished...Shutting down...");
        ts.out(new Tuple("qsort", new QSort(), "complete")); // try to get all processes to shut down

        ts.stop();

        System.out.println("Done.");

        Profiler.end("QSortWorker::TotalRuntime");
        Profiler.print();
        //System.exit(0);
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: java apps.sorting.QSortWorker <PORT>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        new QSortWorker(port).start();
    }
}
