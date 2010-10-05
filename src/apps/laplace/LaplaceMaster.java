/**
 *  LaplaceMaster.java
 *
 *  Author: Alistair Atkinson {alatkins@utas.edu.au}
 */

package apps.laplace;

import space.*;
import runtime.*;
import java.util.*;
import com.clarkware.profiler.*;

public class LaplaceMaster {
    private int port;
    private TupleSpaceRuntime ts;

    private final int N;
    private final int SIZE;
    private final int TSTEPS;
    private double[][] v;

    public LaplaceMaster(int port, int N, int tsteps) {
        this.port = port;
        ts = new TupleSpaceRuntime(port, true);
        this.N = N;
        this.SIZE = 3*N;
        this.TSTEPS = tsteps;
        v = new double[SIZE+1][SIZE+1];
    }

    public void start() {
        // initialise data
        for(int i = 1; i <= SIZE/2; i++) {
            for(int j = 1; j <= SIZE/2; j++) {
                v[i][j] = 100.0 * (i + j) / SIZE;
                v[SIZE-i][j] = v[i][SIZE-j] = v[SIZE-i][SIZE-j] = v[i][j];
            }
        }

        // inner boundary
        for(int i = N; i < 2*N; i++) {
            for(int j = N; j < 2*N; j++) {
                v[i][j] = 100.0;
            }
        }

        Vector<LaplaceTask> tasks = createTasks(ts.NODES);

        v = null;

        ts.start();

        for(LaplaceTask task : tasks) {
            ts.out(new Tuple("laplace", "task", task));
        }

        int numTasks = tasks.size();
        tasks = null;
        Vector<LaplaceResult> res = new Vector<LaplaceResult>();

        for(int i = 0; i < numTasks; i++) {
            res.addElement((LaplaceResult) ts.ts.in(new TupleTemplate("laplace", "final", null)).field(2));
        }

        ts.stop();
    }

    private Vector<LaplaceTask> createTasks(int nodes) {
        Vector<LaplaceTask> a = new Vector<LaplaceTask>();
        LaplaceTask[][] tasks;

        int x, y;
        switch(nodes) {
          case 1:   x = 1;
                    y = 1;
                    break;
          case 2:   x = 1;
                    y = 2;
                    break;
          case 4:   x = 2;
                    y = 2;
                    break;
          case 6:   x = 2;
                    y = 3;
                    break;
          case 8:   x = 2;
                    y = 4;
                    break;
          case 10:  x = 2;
                    y = 5;
                    break;
          case 12:  x = 3;
                    y = 4;
                    break;
          case 14:  x = 2;
                    y = 7;
                    break;
          case 16:  x = 4;
                    y = 4;
                    break;
          default:  x = 1;
                    y = 1;
        }

        tasks = new LaplaceTask[x][y];

        // create panels
        for(int i = 0; i < x; i++) {
            for(int j = 0; j < y; j++) {
                tasks[i][j] = generatePanel(i, j, x, y);
            }
        }

        // set up references between panels
        for(int i = 0; i < x; i++) {
            for(int j = 0; j < y; j++) {
                // northern neighbour
                if(i < x - 1)
                    tasks[i][j].north = tasks[i+1][j].guid;

                // southern neighbour
                if(i > 0)
                    tasks[i][j].south = tasks[i-1][j].guid;

                // easter neighbour
                if(j > 0)
                    tasks[i][j].east  = tasks[i][j-1].guid;

                // western neighbour
                if(j < y - 1)
                    tasks[i][j].west  = tasks[i][j+1].guid;
            }
        }

        // add panels to vector
        for(int i = 0; i < x; i++) {
            for(int j = 0; j < y; j++) {
                a.addElement(tasks[i][j]);
            }
        }

        return a;
    }

    private LaplaceTask generatePanel(int i, int j, int x, int y) {
        double[][] panel = new double[SIZE/x+2][SIZE/y+2];
        int p = SIZE / x * i;
        int q = SIZE / y * j;

        for(int f = 1; f < panel.length-1; f++) {
            for(int g = 1; g < panel[0].length-1; g++) {
                panel[f][g] = v[f+p][g+q];
            }
        }

        return new LaplaceTask(panel, System.currentTimeMillis(), TSTEPS);
    }

    public static void main(String[] args) {
        if(args.length != 3) {
            System.out.println("Usage: java apps.laplace.LaplaceMaster <PORT> <DIM> <TIMESTEPS>");
            System.exit(1);
        }

        int port   = Integer.parseInt(args[0]);
        int N      = Integer.parseInt(args[1]);
        int tsteps = Integer.parseInt(args[2]);

        new LaplaceMaster(port, N, tsteps).start();
    }
}