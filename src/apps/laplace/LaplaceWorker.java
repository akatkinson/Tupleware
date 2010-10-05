/**
 *  LaplaceWorker.java
 *
 *  Author: Alistair Atkinson {alatkins@utas.edu.au}
 */

package apps.laplace;

import space.*;
import runtime.*;
import java.util.*;
import java.io.*;
import com.clarkware.profiler.*;

public class LaplaceWorker {
    private int port;
    private TupleSpaceRuntime ts;
    private double[][] v;

    public LaplaceWorker(int port) {
        this.port = port;
        ts = new TupleSpaceRuntime(port, false);
    }

    public void start() {
        ts.start();

        // get initial values
        LaplaceTask task;
        try {
            task = (LaplaceTask) ts.gts.in(new TupleTemplate("laplace", "task", null)).field(2);
        } catch(IOException e) {
            e.printStackTrace();
            return;
        }

        do {
            writeBoundaries(task);
            refreshHalo(task);
            task.solve();
        } while(task.t <= task.TSTEPS);

        // return results
        LaplaceResult result = new LaplaceResult(task.panel);

        try {
            ts.gts.out(new Tuple("laplace", "final", result));
        } catch(IOException e) {}

        ts.stop();
    }

    private void writeBoundaries(LaplaceTask task) {
        Vector<LaplaceBoundary> bounds = task.getBoundaries();
        for(LaplaceBoundary bound : bounds) {
            ts.out(new Tuple("laplace", "boundary", bound, Long.toString(bound.destGuid), bound.side));
        }
    }

    private void refreshHalo(LaplaceTask task) {
        Vector<TupleTemplate> templates = task.getBoundaryTemplates();
        Vector<LaplaceBoundary> boundaries = new Vector<LaplaceBoundary>();

        for(TupleTemplate t : templates) {
            Tuple tpl = ts.in(t);
            boundaries.addElement((LaplaceBoundary) tpl.field(2));
        }

        task.updateHalo(boundaries);
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: java apps.laplace.LaplaceWorker <PORT>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        new LaplaceWorker(port).start();
    }
}
