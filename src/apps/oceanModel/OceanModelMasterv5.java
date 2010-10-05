/**
 *  OceanModelMasterv5.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *            Adapted from a program by John Hunter (John.Hunter@utas.edu.au)
 *
 *  Created:  6/12/2008
 *  Modified: 6/12/2008
 */

package apps.oceanModel;

import java.util.*;
import space.*;
import runtime.*;
import java.io.*;
import java.text.DecimalFormat;

public final class OceanModelMasterv5 {
    public static final String HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 6600;
    public static final int NUM_PROCS = 12;
    private final int GRID_SIZE;
    private final int SLICE_WIDTH;

    // Model-related constants
    private final int im        = 10;
    private final int jm        = 10;
    private final int dx        = 300;
    private final int dy        = 300;
    private final int dt        = 25;
    private final int tout      = 500000;
    private final int tend      = 1225; //2200; //1000000;
    private final double g      = 9.8;
    private final int rho       = 1025;
    private final double rho_a  = 1.2;
    private final int uf        = 2;
    protected final double cd   = 0.000025;
    protected final double cd_a = 0.0013;
    protected final double wx   = 5.0;
    protected final double wy   = 5.0;

    protected final double facgx = g*dt/((double) dx);
    protected final double facgy = g*dt/((double) dy);
    protected final double facbf = cd * ((double) dt);
    protected final double facwx = wx*Math.sqrt(Math.pow(wx,2.0)+Math.pow(wy,2.0))*cd_a*rho_a*dt/((double) rho);
    protected final double facwy = wy*Math.sqrt(Math.pow(wx,2.0)+Math.pow(wy,2.0))*cd_a*rho_a*dt/((double) rho);
    protected final double facex = ((double) dt)/((double) dx);
    protected final double facey = ((double) dt)/((double) dy);
    protected final double hmax  = 0.8/(g*Math.pow(dt,2.0)*(1.0/Math.pow(dx,2.0)+1.0/Math.pow(dy,2.0)));
    protected final int itmax    = (int) Math.round(tend/dt)+2;

    //private double[][] h;
    //private double[][] hu;
    //private double[][] hv;

    private Panel[] panels;

    protected TupleSpaceRuntime ts;

    /**
     *  Constructor
     */
    public OceanModelMasterv5(int port, int gridSize) {
        GRID_SIZE = gridSize;
        SLICE_WIDTH = GRID_SIZE / NUM_PROCS;
        ts = new TupleSpaceRuntime(port, true);
    }

    public void createPanels() {
        panels = new Panel[NUM_PROCS];

        for(int i = 0; i < NUM_PROCS; i++) {
            panels[i] = new Panel(SLICE_WIDTH, GRID_SIZE, i);
            panels[i].numPanels = NUM_PROCS;
            panels[i].itmax = itmax;
            panels[i].facgx = facgx;
            panels[i].facgy = facgy;
            panels[i].facbf = facbf;
            panels[i].facwx = facwx;
            panels[i].facwy = facwy;
            panels[i].facex = facex;
            panels[i].facey = facey;
            panels[i].hmax  = hmax;
            panels[i].itmax = itmax;

            panels[i].SHARE_LEFT = true;
            panels[i].SHARE_RIGHT = true;

            if(i == 0) {
                panels[i].SHARE_LEFT = false;
            }

            if(i == NUM_PROCS-1) {
                panels[i].SHARE_RIGHT = false;
            }
        }
    }

    protected void start() {
        ts.start();

        //Vector<Tuple> tuples = createTuples();
        this.createPanels();

        for(int i = 0; i < NUM_PROCS; i++) {
            ts.out(new Tuple("panel", panels[i]));
        }

        // Collect final results
        panels = new Panel[NUM_PROCS];
        System.gc();

        int timestep = itmax;

        for(int i = 0; i < NUM_PROCS; i++) {
            panels[0] = (Panel) ts.ts.in(new TupleTemplate("panel_", null)).field(1);
        }
        System.out.println("Master: All results received.");

        ts.stop();
    }

    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("Usage: java OceanModelMasterv5 <PORT> <GRID_SIZE>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        int grid = Integer.parseInt(args[1]);

        new OceanModelMasterv5(port, grid).start();
    }
}
