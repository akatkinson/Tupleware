/**
 *  OceanModelWorkerv5.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *            Adapted from a program by John Hunter (John.Hunter@utas.edu.au)
 *
 *  Created:  18/5/2007 (Converted from older class apps.WorkerThread)
 *                      (Further adaptations from apps.OceanModelWorkerv2)
 *  Modified: 5/9/2007
 */

package apps.oceanModel;

import java.io.IOException;
import runtime.TupleSpaceRuntime;
import space.*;
import scope.Scope;
import com.clarkware.profiler.*;
import java.util.Vector;

public final class OceanModelWorkerv5 implements Runnable {
    private int port;
    private TupleSpaceRuntime ts;

    public OceanModelWorkerv5(int port) {
        this.port = port;
        ts = new TupleSpaceRuntime(port, false);
    }

    /* Runnable implementation */
    public void run() {
        System.out.println("Worker thread " + toString() + " started.");
        try {
            Profiler.begin("Total time doWork()");

            this.doWork();

            Profiler.end("Total time doWork()");
            Profiler.print();
        } catch(IOException e) {
            System.err.println("Worker thread " + toString() + " has died a horrible death. RIP.");
            e.printStackTrace();
            return;
        }
        System.out.println("Worker thread " + toString() + " finished.");
        //Profiler.print();
        System.out.flush();
        System.exit(1);
    }

    private void doWork() throws IOException {
        Profiler.begin("Sequential");
        ts.start();

        Panel task = (Panel) ts.gts.in(new TupleTemplate("panel", null)).field(1);
        Profiler.end("Sequential");

        while(task.kuv < task.itmax) {
            Profiler.begin("Iteration");
            Profiler.begin("processing");
            task.process();
            Profiler.end("processing");

            if(OceanModelMasterv5.NUM_PROCS == 1) {
                Profiler.end("Iteration");
                continue;
            }

            Profiler.begin("IO");

            //ts.outAll(task.getBoundaryValues());
            //ts.out(new Tuple("intermediate", task.id, task.kuv, task.getBoundaryValues()));
            Vector<Tuple> bvals = task.getBoundaryValues();
            for(Tuple t : bvals)
                ts.out(t);

            Vector<TupleTemplate> templates = task.getBoundaryTemplates();
            Vector<Tuple> intermediate = new Vector<Tuple>(templates.size()-1);

            for(TupleTemplate template : templates) {
                Tuple t = ts.in(template);
                intermediate.addAll((Vector<Tuple>) t.field(3));
            }

            Profiler.end("IO");

            task.updateBoundaries(intermediate);
            Profiler.end("Iteration");

            intermediate = null;
        }

        task.cleanUp();

        //return final result
        Profiler.begin("Sequential");
        ts.gts.out(new Tuple("panel_", new Panel()));
        Profiler.end("Sequential");

        System.out.println("Requests: " + ts.requestCount +" Instances: " + ts.totalRequests);

        ts.stop();
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: java apps.oceanModel.OceanModelWorkerv5 <PORT>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        new OceanModelWorkerv5(port).run();
    }
}
