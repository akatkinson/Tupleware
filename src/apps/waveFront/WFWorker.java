/**
 *  WFWorker.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *
 *  Created:  11/10/2007
 *  Modified: 16/10/2007
 */

package apps.waveFront;

import space.*;
import runtime.*;

public class WFWorker {
    private TupleSpaceRuntime ts;

    private int T;

    public WFWorker(int port) {
        ts = new TupleSpaceRuntime(port, false);
    }

    public void start() {
        ts.start();

        T = (Integer) ts.rd(new TupleTemplate("T", null)).field(1);

        // get panel

        for(int i = 0; i < T; i++) {
            // process T timesteps
        }

        //write data back to global tuplespace

        ts.stop();
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: java apps.waveFront.WFWorker <PORT>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        new WFWorker(port).start();
    }
}