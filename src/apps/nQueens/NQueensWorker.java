/**
 *  NQueensWorker.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *  Created:  6/8/2007
 *  Modified: 21/8/2007
 */

package apps.nQueens;

import runtime.*;
import space.*;
import java.util.*;
import com.clarkware.profiler.*;

public class NQueensWorker {
    private TupleSpaceRuntime space;
    private int port;

    public NQueensWorker(int port) {
        space = new TupleSpaceRuntime(port, false);
    }

    public void run() {
        space.start();

        Vector<QueensTask> tasks = getTask();
        Vector<QueensResult> results = new Vector<QueensResult>();

        for(QueensTask t : tasks) {
            results.addElement(process(t));
        }

        returnResult(results);

        space.stop();

        System.exit(0);
    }

    public Vector<QueensTask> getTask() {
        try {
            return (Vector<QueensTask>) space.gts.inp(new TupleTemplate("qtask", null)).field(1);
        } catch(java.io.IOException e) {
            return null;
        }
    }

    public QueensResult process(QueensTask task) {
        return task.execute();
    }

    public void returnResult(Vector<QueensResult> results) {
        Vector<Tuple> tpls = new Vector<Tuple>();
        for(QueensResult r : results) {
            tpls.addElement(new Tuple("qresult", r));
        }

        space.outAll(tpls);
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: java apps.nQueens.NQueensWorker <PORT>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        new NQueensWorker(port).run();
    }
}
