/**
 *  OceanModelMaster.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *            Adapted from a program by John Hunter (John.Hunter@utas.edu.au)
 *
 *  Created:  21/3/2005
 *  Modified: 4/5/2006
 */

package apps.oceanModel;

import java.util.*;
import space.*;
import runtime.*;
import java.io.*;
import java.text.DecimalFormat;
//import com.clarkware.profiler.*;

/**
 *
 */
public final class OceanModelMaster {
    public static final String HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 6600;
    public static final int NUM_THREADS = 4;

    // Model-related constants
    private final int im        = 10;
    private final int jm        = 10;
    private final int dx        = 300;
    private final int dy        = 300;
    private final int dt        = 25;
    private final int tout      = 500000;
    private final int tend      = 1200; //2200; //1000000;
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

    private double[][] h;
    private double[][] hu;
    private double[][] hv;

    protected TupleSpaceRuntime ts;
    //protected Thread[] threads;

    /**
     *  Constructor
     */
    public OceanModelMaster(int port) {
        h  = new double[im+2][jm+2];
        hu = new double[im+2][jm+2];
        hv = new double[im+2][jm+2];

        setBathymetry();

        ts = new TupleSpaceRuntime(port, true);

        /*threads = new Thread[NUM_THREADS];
        for(int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new Thread(new WorkerThread(i, ts));
        }*/
    }

    private void setBathymetry() {
        // Initialise h[][]
        int middle = im/2;

        for(int i = 2; i < im; i++) {
            int offset = Math.abs(middle - i);
            double depth = 10.0*(middle-offset)/((double) middle);

            for(int j = 1; j <= jm; j++) {
                h[i][j] = (depth > hmax)? hmax: depth;
            }
        }

        // Interpolate depth to u and v points
        for(int j = 2; j < jm; j++) {
            for(int i = 3; i < im; i++) {
                hu[i][j] = (h[i-1][j] + h[i][j])/2.0;
            }
        }

        for(int j = 3; j < jm; j++) {
            for(int i = 2; i < im; i++) {
                hv[i][j] = (h[i][j-1] + h[i][j])/2.0;
            }
        }
    }

    private Vector<Tuple> createTuples() {
        Vector<Tuple> tuples = new Vector<Tuple>();

        tuples.add(new Tuple("im", im));       tuples.add(new Tuple("jm", jm));
        tuples.add(new Tuple("dx", dx));       tuples.add(new Tuple("dy", dy));
        tuples.add(new Tuple("dt", dt));       tuples.add(new Tuple("tout", tout));
        tuples.add(new Tuple("tend", tend));   tuples.add(new Tuple("g", g));
        tuples.add(new Tuple("rho", rho));     tuples.add(new Tuple("rho_a", rho_a));
        tuples.add(new Tuple("uf", uf));       tuples.add(new Tuple("cd", cd));
        tuples.add(new Tuple("cd_a", cd_a));   tuples.add(new Tuple("hmax", hmax));
        tuples.add(new Tuple("wx", wx));       tuples.add(new Tuple("wy", wy));
        tuples.add(new Tuple("facgx", facgx)); tuples.add(new Tuple("facgy", facgy));
        tuples.add(new Tuple("facbf", facbf)); tuples.add(new Tuple("facwx", facwx));
        tuples.add(new Tuple("facwy", facwy)); tuples.add(new Tuple("facex", facex));
        tuples.add(new Tuple("facey", facey)); tuples.add(new Tuple("itmax", itmax));

        for(int i = 0; i < h.length; i++) {
            for(int j = 0; j < h[0].length; j++) {
                tuples.add(new Tuple("u", i, j, 0.0, 0));
                tuples.add(new Tuple("u", i, j, 0.0, 1));
                tuples.add(new Tuple("v", i, j, 0.0, 0));
                tuples.add(new Tuple("v", i, j, 0.0, 1));
                tuples.add(new Tuple("hu", i, j, hu[i][j]));
                tuples.add(new Tuple("hv", i, j, hv[i][j]));
                tuples.add(new Tuple("eta", i, j, 0.0, 0));
                tuples.add(new Tuple("eta", i, j, 0.0, 1));
                tuples.add(new Tuple("h", i, j, h[i][j]));
                tuples.add(new Tuple("sfx", i, j, 0.0));
                tuples.add(new Tuple("sfy", i, j, 0.0));
            }
        }

        // Create task tuples with format: <"task", imin, imax, jmin, jmax>
        tuples.add(new Tuple("task", 1, 6, 1, 6));
        tuples.add(new Tuple("task", 6, 10, 1, 6));
        tuples.add(new Tuple("task", 1, 6, 6, 10));
        tuples.add(new Tuple("task", 6, 10, 6, 10));

/*        tuples.add(new Tuple("task", 1,  6, 1,  4));
        tuples.add(new Tuple("task", 1,  6, 4,  7));
        tuples.add(new Tuple("task", 1,  6, 7, 10));
        tuples.add(new Tuple("task", 6, 10, 1,  4));
        tuples.add(new Tuple("task", 6, 10, 4,  7));
        tuples.add(new Tuple("task", 6, 10, 7, 10));*/

        /*tuples.add(new Tuple("task", 1, 4, 1, 4));
        tuples.add(new Tuple("task", 1, 4, 4, 7));
        tuples.add(new Tuple("task", 1, 4, 7, 10));
        tuples.add(new Tuple("task", 4, 7, 1, 4));
        tuples.add(new Tuple("task", 4, 7, 4, 7));
        tuples.add(new Tuple("task", 4, 7, 7, 10));
        tuples.add(new Tuple("task", 7, 10, 1, 4));
        tuples.add(new Tuple("task", 7, 10, 4, 7));
        tuples.add(new Tuple("task", 7, 10, 7, 10));*/

        /*tuples.add(new Tuple("task", 1, 4, 1, 10));
        tuples.add(new Tuple("task", 4, 7, 1, 10));*/

        // Process indices for 30x30 grid
        /*tuples.add(new Tuple("task", 1, 16, 1, 16));
        tuples.add(new Tuple("task", 16, 30, 1, 16));
        tuples.add(new Tuple("task", 1, 16, 16, 30));
        tuples.add(new Tuple("task", 16, 30, 16, 30));*/

        // Process indices for 60x60 grid
        /*tuples.add(new Tuple("task", 1, 31, 1, 31));
        tuples.add(new Tuple("task", 31, 60, 1, 31));
        tuples.add(new Tuple("task", 1, 31, 31, 60));
        tuples.add(new Tuple("task", 31, 60, 31, 60));*/

        // Process indices for 100x100 grid
        /*tuples.add(new Tuple("task", 1, 51, 1, 51));
        tuples.add(new Tuple("task", 51, 100, 1, 51));
        tuples.add(new Tuple("task", 1, 51, 51, 100));
        tuples.add(new Tuple("task", 51, 100, 51, 100));*/

        /*tuples.add(new Tuple("task", 1, 26, 1, 26));
        tuples.add(new Tuple("task", 26, 50, 1, 26));
        tuples.add(new Tuple("task", 1, 26, 26, 50));
        tuples.add(new Tuple("task", 26, 50, 26, 50));*/

        /*tuples.add(new Tuple("task", 1, 34, 1, 34));
        tuples.add(new Tuple("task", 1, 34, 33, 67));
        tuples.add(new Tuple("task", 1, 34, 66, 100));
        tuples.add(new Tuple("task", 33, 67, 1, 34));
        tuples.add(new Tuple("task", 33, 67, 33, 67));
        tuples.add(new Tuple("task", 33, 67, 66, 100));
        tuples.add(new Tuple("task", 66, 100, 1, 34));
        tuples.add(new Tuple("task", 66, 100, 33, 67));
        tuples.add(new Tuple("task", 66, 100, 66, 100));*/

        /*tuples.add(new Tuple("task", 1, 101, 1, 101));
        tuples.add(new Tuple("task", 101, 200, 1, 101));
        tuples.add(new Tuple("task", 1, 101, 101, 200));
        tuples.add(new Tuple("task", 101, 200, 101, 200));*/

        // Process indices for 20x20 grid
        /*tuples.add(new Tuple("task",  1, 11,  1, 11));
        tuples.add(new Tuple("task", 11, 20,  1, 11));
        tuples.add(new Tuple("task",  1, 11, 11, 20));
        tuples.add(new Tuple("task", 12, 20, 11, 20));*/

        /*tuples.add(new Tuple("task", 1, 16, 1, 10));
        tuples.add(new Tuple("task", 1, 16, 10, 20));
        tuples.add(new Tuple("task", 1, 16, 20, 30));
        tuples.add(new Tuple("task", 16, 30, 1, 10));
        tuples.add(new Tuple("task", 16, 30, 10, 20));
        tuples.add(new Tuple("task", 16, 30, 20, 30));*/

        /*tuples.add(new Tuple("task",  1, 10,  1, 10));
        tuples.add(new Tuple("task",  1, 10, 10, 20));
        tuples.add(new Tuple("task",  1, 10, 20, 30));
        tuples.add(new Tuple("task", 10, 20,  1, 10));
        tuples.add(new Tuple("task", 10, 20, 10, 20));
        tuples.add(new Tuple("task", 10, 20, 20, 30));
        tuples.add(new Tuple("task", 20, 30,  1, 10));
        tuples.add(new Tuple("task", 20, 30, 10, 20));
        tuples.add(new Tuple("task", 20, 30, 20, 30));*/

        return tuples;
    }

    protected void start() {
        ts.start();

        //Vector<Tuple> tuples = createTuples();
        ts.outAll(createTuples());

        //for(Tuple t : tuples) {
            //ts.out(t);

            /*try {
                ts.out(t);
            } catch(IOException e) {
                System.err.println("Error occurred initialising tuplespace values. Exiting.");
                e.printStackTrace();
                System.exit(1);
            }*/
        //}

        //tuples = null;

        /*for(Thread t : threads) {
            t.start();
        }

        // Wait for all threads to complete
        for(Thread t : threads) {
            try { t.join(); }
            catch(InterruptedException e) {}
        }*/

        // Collect final results
        double[][] u   = new double[im+2][jm+2];
        double[][] v   = new double[im+2][jm+2];
        double[][] eta = new double[im+2][jm+2];
        //double[][] sfx = new double[im+2][jm+2];
        //double[][] sfy = new double[im+2][jm+2];

        int timestep = itmax;

        while(timestep <= itmax) {
            for(int i = 1; i <= im; i++) {
                for(int j = 1; j <= jm; j++) {
                    u[i][j] = (Double) ts.rd(new TupleTemplate("u", i, j, null, timestep)).field(3);
                    v[i][j] = (Double) ts.rd(new TupleTemplate("v", i, j, null, timestep)).field(3);
                    eta[i][j] = (Double) ts.rd(new TupleTemplate("eta", i, j, null, timestep)).field(3);
                    //sfx[i][j] = (Double) ts.in(new TupleTemplate("sfx", i, j, null)).field(3);
                    //sfy[i][j] = (Double) ts.in(new TupleTemplate("sfy", i, j, null)).field(3);
                }
            }

            // Print output
            printOutput(u, v, eta, true, timestep);
            //printOutputPlain(u, v, eta);

            timestep++;
        }

        ts.stop();
    }

    private void printOutput(double[][] u, double[][] v, double[][] eta, boolean depth, int iteration) {
        DecimalFormat df = new DecimalFormat("  '0'.0000E00 ; -'0'.0000E00 ");
        StringBuffer _h;

        if(depth) {
            for(int j = jm; j >= 1; j--) {
                _h = new StringBuffer("h   j"+j+"it"+iteration);

                if(j < 10)
                    _h.insert(5, "0");

                for(int i = 1; i <= im; i++)
                    _h.append(df.format(h[i][j]));

                System.out.println(alignValues(_h.toString()));
            }
        }

        StringBuffer _u, _v, _eta;

        for(int j = jm; j >= 1; j--) {
            _u = new StringBuffer("u   j"+j+"it"+iteration);
            _v = new StringBuffer("v   j"+j+"it"+iteration);
            _eta = new StringBuffer("eta j"+j+"it"+iteration);

            if(j < 10) {
                _u.insert(5, "0");
                _v.insert(5, "0");
                _eta.insert(5, "0");
            }

            for(int i = 1; i <= im; i++) {
                _u.append(df.format(u[i][j]));
                _v.append(df.format(v[i][j]));
                _eta.append(df.format(eta[i][j]));
            }

            System.out.println(alignValues(_u.toString()));
            System.out.println(alignValues(_v.toString()));
            System.out.println(alignValues(_eta.toString()));
        }
    }

    private String alignValues(final String s) {
        if(s.length() <= 12)
            return s;

        StringBuffer res = new StringBuffer(s);
        int index = res.indexOf("E-");

        if(index == -1) {
            return res.toString();
        } else {
            res.deleteCharAt(index+5);
            return res.substring(0, index+4) + alignValues(res.substring(index+4));
        }
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: java OceanModelMaster <PORT>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        new OceanModelMaster(port).start();
    }
}
