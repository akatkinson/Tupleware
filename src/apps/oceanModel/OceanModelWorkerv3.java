/**
 *  OceanModelWorkerv3.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *            Adapted from a program by John Hunter (John.Hunter@utas.edu.au)
 *
 *  Created:  20/4/2007 (Converted from older class apps.WorkerThread)
 *                      (Further adaptations from apps.OceanModelWorkerv2)
 *  Modified: 27/4/2007
 */

package apps.oceanModel;

import java.io.IOException;
import runtime.TupleSpaceRuntime;
import space.*;
import scope.Scope;
import com.clarkware.profiler.*;
import java.util.Vector;

public final class OceanModelWorkerv3 implements Runnable {
    protected enum VerticalPosition   { TOP,  CENTRE, BOTTOM }
    protected enum HorizontalPosition { LEFT, CENTRE, RIGHT  }
    private VerticalPosition vPos;
    private HorizontalPosition hPos;
    private Scope segmentScope;

    private int port;
    private TupleSpaceRuntime ts;

    double[][][] u, v, eta;
    double[][] h, hu, hv;

    int kuv;
    int keta;
    int kuv_old;
    int kuv_new;
    int keta_old;
    int keta_new;

    int itmax, uf;
    double facgx;
    double facgy;
    double facbf;
    double facwx;
    double facwy;
    double facex;
    double facey;

    int imin, imax, jmin, jmax;

    /* These values are used to adjust the boundaries (by a maximum of 1)*/
    private int i_begin, i_end, j_begin, j_end;

    public OceanModelWorkerv3(int port) {
        this.port = port;
        ts = new TupleSpaceRuntime(port, false);

        kuv  = 1;
        keta = 1;
        kuv_old = 0;
        kuv_new = 1;
        keta_old = 0;
        keta_new = 1;
    }

    private void findPosition(int imin, int imax, int jmin, int jmax) {
        int im = (Integer) ts.rd(new TupleTemplate("im", null)).field(1);
        int jm = (Integer) ts.rd(new TupleTemplate("jm", null)).field(1);

        if(imin==1) {
            hPos = HorizontalPosition.LEFT;
            segmentScope = new Scope("LEFT");
            i_begin = 0;
            i_end = 1;
        } else if (imax==im) {
            hPos = HorizontalPosition.RIGHT;
            segmentScope = new Scope("RIGHT");
            i_begin = -1;
            i_end = 0;
        } else {
            hPos = HorizontalPosition.CENTRE;
            segmentScope = new Scope("HCENTRE");
            i_begin = -1;
            i_end = 1;
        }

        if(jmin==1) {
            vPos = VerticalPosition.BOTTOM;
            segmentScope = Scope.union(segmentScope, new Scope("BOTTOM"));
            j_begin = 0;
            j_end = 1;
        } else if (jmax==jm) {
            vPos = VerticalPosition.TOP;
            segmentScope = Scope.union(segmentScope, new Scope("TOP"));
            j_begin = -1;
            j_end = 0;
        } else {
            vPos = VerticalPosition.CENTRE;
            segmentScope = Scope.union(segmentScope, new Scope("VCENTRE"));
            j_begin = -1;
            j_end = 1;
        }
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
        ts.start();

        Tuple task = ts.in(new TupleTemplate("task", null, null, null, null));

        System.out.println("Panel indices " + task.toString());

        imin = (Integer) task.field(1);
        imax = (Integer) task.field(2);
        jmin = (Integer) task.field(3);
        jmax = (Integer) task.field(4);

        /* Work out which section we're working in */
        findPosition(imin, imax, jmin, jmax);
        ts.setBoundaries(imin, imax, jmin, jmax); // set boundaries on runtime

        this.getInitialValues();

        /* ADD 1 FOR FORTRAN (STARTING AT 1) INDEXING */
        u   = new double[imax-imin+2][jmax-jmin+2][2];
        v   = new double[imax-imin+2][jmax-jmin+2][2];
        eta = new double[imax-imin+2][jmax-jmin+2][2];
        h   = new double[imax-imin+2][jmax-jmin+2];
        hu  = new double[imax-imin+2][jmax-jmin+2];
        hv  = new double[imax-imin+2][jmax-jmin+2];

        Profiler.begin("io");

        ts.setScope(segmentScope); // turn on decentralised mode

        Profiler.begin("iterations");
        while(kuv <= itmax) {
            System.out.println("Iteration "+kuv+" of "+itmax);
            Profiler.begin("processing");
            // Step u-velocity:
            for(int j = 2+j_begin; j < u[0].length-1+j_end; j++) {
                for (int i = 3+i_begin; i < u.length-1+i_end; i++){
                    u[i][j][kuv_new] = u[i][j][kuv_old]-facgx*(eta[i][j][keta_old]-eta[i-1][j][keta_old])-facbf*u[i][j][kuv_old]*uf/hu[i][j]+facwx/hu[i][j];
                }
            }

            // Step v-velocity:
            for(int j = 3+j_begin; j < v[0].length-1+j_end; j++) {
                for (int i = 2+i_begin; i < v.length-1+i_end; i++){
                    v[i][j][kuv_new] = v[i][j][kuv_old]-facgy*(eta[i][j][keta_old]-eta[i][j-1][keta_old])-facbf*v[i][j][kuv_old]*uf/hv[i][j]+facwy/hv[i][j];
                }
            }

            this.updateVelocityBoundaryVals();

            /* INCREMENT KUV TIMESTEP */
            kuv++;
            Profiler.end("processing");

            /* SKIP REST OF LOOP IF IN FINAL ITERATION - ALL VALUES WILL BE
               WRITTEN BACK TO TUPLE SPACE AFTER LOOP */
            if(kuv == itmax)
                continue;

            /* REVERSE TIME INDICES */
            kuv_old = Math.abs(kuv_old-1);
            kuv_new = Math.abs(kuv_new-1);

            Profiler.begin("processing");
            /* CALCULATE STEP ELEVATION */
            for(int j = 2+j_begin; j < eta[0].length-1; j++) {
                for(int i = 2+i_begin; i < eta.length-1; i++) {
                    eta[i][j][keta_new] = eta[i][j][keta_old]-(facex*(u[i+1][j][kuv_old]*hu[i+1][j]-u[i][j][kuv_old]*hu[i][j])+facey*(v[i][j+1][kuv_old]*hv[i][j+1]-v[i][j][kuv_old]*hv[i][j]));
                }
            }

            this.updateSurfaceElevationBoundaryVals();

            /* INCREMENT KETA TIMESTEP */
            keta++;

            Profiler.end("io");

            /* REVERSE TIME INDICES */
            keta_old = Math.abs(keta_old-1);
            keta_new = Math.abs(keta_new-1);
        }  // end of loop
        Profiler.end("iterations");

        // Switch back to default non-scoped mode
        ts.setDefaultScope();
        this.returnResults();
        ts.stop();
    }

    private void getInitialValues() {
        /* Read in ALL array values FOR THIS SUBSECTION */
        Vector<Tuple> uValsOld = ts.rdAll(new TupleTemplate("u", null, null, null, kuv_old), imax-imin);
        Vector<Tuple> uValsNew = ts.rdAll(new TupleTemplate("u", null, null, null, kuv_new), imax-imin);
        Vector<Tuple> vValsOld = ts.rdAll(new TupleTemplate("v", null, null, null, kuv_old), imax-imin);
        Vector<Tuple> vValsNew = ts.rdAll(new TupleTemplate("v", null, null, null, kuv_new), imax-imin);
        Vector<Tuple> huVals = ts.rdAll(new TupleTemplate("hu", null, null, null), imax-imin);
        Vector<Tuple> hvVals = ts.rdAll(new TupleTemplate("hv", null, null, null), imax-imin);
        Vector<Tuple> etaValsOld = ts.rdAll(new TupleTemplate("eta", null, null, null, keta_old), imax-imin);
        Vector<Tuple> etaValsNew = ts.rdAll(new TupleTemplate("eta", null, null, null, keta_new), imax-imin);
        Vector<Tuple> hVals = ts.rdAll(new TupleTemplate("h", null, null, null), imax-imin);
        for(Tuple t : uValsOld)
            u[(Integer)t.field(1)%(imax-imin)][(Integer)t.field(2)%(jmax-jmin)][kuv_old] = (Double) t.field(3);
        for(Tuple t : uValsNew)
            u[(Integer)t.field(1)%(imax-imin)][(Integer)t.field(2)%(jmax-jmin)][kuv_new] = (Double) t.field(3);
        for(Tuple t : vValsOld)
            v[(Integer)t.field(1)%(imax-imin)][(Integer)t.field(2)%(jmax-jmin)][kuv_old] = (Double) t.field(3);
        for(Tuple t : vValsNew)
            v[(Integer)t.field(1)%(imax-imin)][(Integer)t.field(2)%(jmax-jmin)][kuv_new] = (Double) t.field(3);
        for(Tuple t : huVals)
            hu[(Integer)t.field(1)%(imax-imin)][(Integer)t.field(2)%(jmax-jmin)] = (Double) t.field(3);
        for(Tuple t : hvVals)
            hv[(Integer)t.field(1)%(imax-imin)][(Integer)t.field(2)%(jmax-jmin)] = (Double) t.field(3);
        for(Tuple t : etaValsOld)
            eta[(Integer)t.field(1)%(imax-imin)][(Integer)t.field(2)%(jmax-jmin)][keta_old] = (Double) t.field(3);
        for(Tuple t : etaValsNew)
            eta[(Integer)t.field(1)%(imax-imin)][(Integer)t.field(2)%(jmax-jmin)][keta_new] = (Double) t.field(3);
        for(Tuple t : hVals)
            h[(Integer)t.field(1)%(imax-imin)][(Integer)t.field(2)%(jmax-jmin)] = (Double) t.field(3);

        System.out.println("got bulk vals");

        itmax = (Integer) ts.rd(new TupleTemplate("itmax", null)).field(1);
        uf    = (Integer) ts.rd(new TupleTemplate("uf", null)).field(1);
        facgx = (Double)  ts.rd(new TupleTemplate("facgx", null)).field(1);
        facgy = (Double)  ts.rd(new TupleTemplate("facgy", null)).field(1);
        facbf = (Double)  ts.rd(new TupleTemplate("facbf", null)).field(1);
        facwx = (Double)  ts.rd(new TupleTemplate("facwx", null)).field(1);
        facwy = (Double)  ts.rd(new TupleTemplate("facwy", null)).field(1);
        facex = (Double)  ts.rd(new TupleTemplate("facex", null)).field(1);
        facey = (Double)  ts.rd(new TupleTemplate("facey", null)).field(1);
        Profiler.end("io");

        System.out.println("got other misc vals");
    }

    private void updateVelocityBoundaryVals() {
        Vector<Tuple> tempVals = new Vector<Tuple>();
        /* WRITE SHARED VELOCITY VALUES BACK TO SPACE */
        for(int j = 1; j < u[0].length; j++)
            tempVals.addElement(new Tuple("u", imax, j+jmin-1, u[imax-imin+1][j][kuv_new], kuv));
        for(int i = 1; i < v.length; i++)
            tempVals.addElement(new Tuple("v", i+imin-1, jmax, v[i][jmax-jmin+1][kuv_new], kuv));

        Profiler.begin("io");
        ts.outAll(tempVals);

        /* Update new values here */
        Vector<Tuple> uVals = ts.rdAll(new TupleTemplate("u", null, null, null, kuv), imax-imin);
        for(Tuple t: uVals) {
            u[1][(Integer)t.field(2)%(jmax-jmin)][kuv_new] = (Double) t.field(3);
        }

        Vector<Tuple> vVals = ts.rdAll(new TupleTemplate("v", null, null, null, kuv), jmax-jmin);
        for(Tuple t : vVals) {
            v[(Integer)t.field(1)%(imax-imin)][1][kuv_new] = (Double) t.field(3);
        }
    }

    private void updateSurfaceElevationBoundaryVals() {
        Vector<Tuple> tempVals = new Vector<Tuple>();
        /* WRITE ETA VALUES BACK TO SPACE */
        for(int j = 1; j < eta[0].length; j++) {
            if((vPos!=VerticalPosition.TOP)&&(j==eta[0].length-1))
                continue;

            tempVals.addElement(new Tuple("eta", imin, j+jmin-1, eta[1][j][keta_new], keta));
        }

        for(int i = 1; i < eta.length; i++) {
            if((hPos!=HorizontalPosition.RIGHT)&&(i==eta.length-1))
                continue;

            tempVals.addElement(new Tuple("eta", i+imin-1, jmin, eta[i][1][keta_new], keta));
        }

        Profiler.begin("io");
        ts.outAll(tempVals);

        /* GET UPDATED ETA VALUES */
        Vector<Tuple> etaVals = ts.inAll(new TupleTemplate("eta", null, null, null, keta), imax-imin);
        for(Tuple t : etaVals) {
            eta[imax-imin+1][(Integer) t.field(2)%(jmax-jmin)][keta_new] = (Double) t.field(3);
        }

        etaVals = ts.inAll(new TupleTemplate("eta", null, null, null, keta), imax-imin);
        for(Tuple t : etaVals) {
            eta[(Integer) t.field(1)%(imax-imin)][jmax-jmin+1][keta_new] = (Double) t.field(3);
        }
    }

    private void returnResults() {
        Vector<Tuple> finalTuples = new Vector<Tuple>();
        //Profiler.begin("io");
        /* After last iteration, write ALL values back to tuplespace. */
        for(int i = 1; i <= imax-imin+1; i++) {
            if(((hPos==HorizontalPosition.CENTRE)||(hPos==HorizontalPosition.RIGHT)) && (i==1))
                continue;

            for(int j = 1; j <= jmax-jmin+1; j++) {
                finalTuples.addElement(new Tuple("u", imin+i-1, jmin+j-1, u[i][j][kuv_new], itmax));
            }
        }

        for(int j = 1; j <= jmax-jmin+1; j++) {
            if(((vPos==VerticalPosition.CENTRE)||(vPos==VerticalPosition.TOP)) && (j==1))
                continue;

            for(int i = 1; i <= imax-imin+1; i++) {
                finalTuples.addElement(new Tuple("v", imin+i-1, jmin+j-1, v[i][j][kuv_new], itmax));
            }
        }

        for(int i = 1; i <= imax-imin+1; i++) {
            if(((hPos==HorizontalPosition.CENTRE)||(hPos==HorizontalPosition.LEFT)) && (i==imax-imin+1))
                continue;

            for(int j = 1; j <= jmax-jmin+1; j++) {
                if(((vPos==VerticalPosition.CENTRE)||(vPos==VerticalPosition.BOTTOM)) && (j==jmax-jmin+1))
                    continue;

                finalTuples.addElement(new Tuple("eta", imin+i-1, jmin+j-1, eta[i][j][keta_new], itmax));
            }
        }
        //Profiler.end("io");

        Profiler.begin("io");
        ts.outAll(finalTuples);
        Profiler.end("io");
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: java apps.oceanModel.OceanModelWorkerv3 <PORT>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        new OceanModelWorkerv3(port).run();
    }
}
