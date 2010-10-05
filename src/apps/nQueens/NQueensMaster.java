/**
 *  NQueensMaster.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *  Created:  6/8/2007
 *  Modified: 21/8/2007
 */

package apps.nQueens;

import runtime.*;
import space.*;
import java.util.Vector;

public class NQueensMaster {
    private final int N = 12; // board size

    private TupleSpaceRuntime space;
    private int port;

    public NQueensMaster(int port) {
        this.port = port;
        space = new TupleSpaceRuntime(port, true);
    }

    public void run() {
        space.start();

        QueensTask[] tasks = this.generateTasks();

        Vector<Tuple> tpls = new Vector<Tuple>();
        for(int i = 0; i < space.NODES; i++) {
            tpls.addElement(new Tuple("qtask", new Vector<QueensTask>()));
        }

        for(int i = 0; i < tasks.length; i++) {
            ((Vector<QueensTask>)tpls.elementAt(i % space.NODES).field(1)).addElement(tasks[i]);
        }

        space.outAll(tpls);

        QueensResult[] results = this.retrieveResults();

        int numSolns = 0;
        for(int i = 0; i < results.length; i++) {
            numSolns += results[i].getNumSolns();
        }

        System.out.println("\nFor a board size of " + N + ", there are "
                            + numSolns + " possible solutions.");

        space.stop();
        System.exit(0);
    }

    public QueensTask[] generateTasks() {
        /* When setting queens in two columns, the number of tasks
           produced can be caluculated by:
                tasks = (N-2)*(N-3)+2*(N-2)
                      = (N-2)*(N-1)
         */
        int numTasks = (N-2)*(N-1);
        QueensTask[] tasks = new QueensTask[numTasks];

        int index = 0;
        for(int i = 0; i < N; ++i) {
            for(int j = 0; j < N; ++j) {
                if((j != i) && (j-1 != i) && (j+1 != i)) {
                    tasks[index] = new QueensTask(N, i+1, j+1);
                    ++index;
                }
            }
        }

        return tasks;
    }

    public QueensResult[] retrieveResults() {
        int numTasks = (N-2)*(N-1);
        QueensResult[] results = new QueensResult[numTasks];

        for(int i = 0; i < numTasks; i++) {
            results[i] = (QueensResult) space.in(new TupleTemplate("qresult", null)).field(1);
        }

        return results;
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: java apps.nQueens.NQueensMaster <PORT>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        new NQueensMaster(port).run();
    }
}
