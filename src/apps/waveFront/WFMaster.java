/**
 *  WFMaster.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *
 *  Created:  11/10/2007
 *  Modified: 16/10/2007
 */

package apps.waveFront;

import space.*;
import runtime.*;

public class WFMaster {
    public final int NUM_PROCS    =   4;
    public final int PANELS_X     =   2;
    public final int PANELS_Y     =   2;
    public final int PANEL_X_SIZE = 100;
    public final int PANEL_Y_SIZE = 100;
    public final int T            = 100;
    private final WFEquation eq   = new WFMultiplier();

    private double[][] data = new double[PANEL_X_SIZE*PANELS_X][PANEL_Y_SIZE*PANELS_Y];

    private TupleSpaceRuntime ts;

    public WFMaster(int port) {
        ts = new TupleSpaceRuntime(port, true);
    }

    public void start() {
        ts.start();

        // initialise grid
        eq.initData(data);

        // create panels

        // output to tuplespace
        ts.out(new Tuple("T", new Integer(T)));

        // wait for results

        // shut down
        ts.stop();
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: java apps.waveFront.WFMaster <PORT>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        new WFMaster(port).start();
    }
}