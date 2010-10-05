/**
 *  Panel.java
 *
 *  Author:   Alistair Atkinson {alatkins@utas.edu.au}
 *  Created:  18/5/2007
 *  Modified: 6/12/2008
 */

package apps.oceanModel;

import java.io.Serializable;
import java.util.*;
import space.*;
import scope.*;
//import runtime.Neighbourhood;

public class Panel implements Serializable {
    public boolean SHARE_LEFT;
    public boolean SHARE_RIGHT;

    public double[][][] u, v, eta;
    public double[][]   h, hu, hv;

    public int kuv, keta, kuv_old, kuv_new, keta_old, keta_new;
    public int itmax, uf;
    public double facgx, facgy, facbf, facwx, facwy, facex, facey, hmax;

    public int width, length, id, numPanels;

    private Scope localScope;

    public Panel() {}

    public Panel(int width, int length, int id) {
        this.width = width;
        this.length = length;
        this.id = id;

        u   = new double[width+1][length][2];
        v   = new double[width+1][length][2];
        eta = new double[width+1][length][2];
        h   = new double[width+1][length];
        hu  = new double[width+1][length];
        hv  = new double[width+1][length];

        this.init();

        kuv = 1;
        keta = 1;
        kuv_old  = 0;
        kuv_new  = 1;
        keta_old = 0;
        keta_new = 1;
    }

    /* Return all boundary data points encapsulated in Tuple objects */
    public Vector<Tuple> getBoundaryValues() {
        Tuple l = null;
        Tuple r = null;

        Vector<Tuple> vals = new Vector<Tuple>();

        if(SHARE_LEFT) { 
            for(int j = 0; j < length-1; j++) {
                vals.addElement(new Tuple("u", 1, j,
                                        new Double(u[1][j][kuv_new]),
                                        new Integer(kuv), new Integer(id)));
            }

            l = new Tuple("u_inter", id, kuv, vals);
        }

        vals = new Vector<Tuple>();

        if(SHARE_RIGHT) {
            for(int j = 0; j < length-1; j++) {
                vals.addElement(new Tuple("eta", width, j,
                                        new Double(eta[width-1][j][keta_new]),
                                        new Integer(keta), new Integer(id)));
            }

            r = new Tuple("eta_inter", id, keta, vals);
        }

        vals = new Vector<Tuple>();
        
        if(l != null)
            vals.add(l);
        
        if(r != null)
            vals.add(r);

        if(vals.size() == 0) return null;

        return vals;
    }

    /* Return Template objects suitable for retrieving boundary
        data points */
    public Vector<TupleTemplate> getBoundaryTemplates() {
        Vector<TupleTemplate> templates = new Vector<TupleTemplate>();

        /*if(SHARE_LEFT) {
            for(int j = 0; j < length-1; j++) {
                templates.addElement(new TupleTemplate("eta", width, j, null, new Integer(keta), new Integer(id-1)));
            }
        }

        if(SHARE_RIGHT) {
            for(int j = 0; j < length-1; j++) {
                templates.addElement(new TupleTemplate("u", 1, j, null, new Integer(kuv), new Integer(id+1)));
            }
        }*/

        if(SHARE_LEFT) {
            templates.addElement(new TupleTemplate("eta_inter", id-1, keta, null));
        }

        if(SHARE_RIGHT) {
            templates.addElement(new TupleTemplate("u_inter", id+1, kuv, null));
        }

        return templates;
    }

    /* Update boundary data points uding given tuples */
    public void updateBoundaries(Vector<Tuple> vals) {
        for(Tuple val : vals) {
            int j = (Integer) val.field(2);
            String array = (String) val.field(0);

            if(array.equals("u"))
                u[width][j][kuv_new] = (Double) val.field(3);
            else if(array.equals("eta"))
                eta[0][j][keta_new] = (Double) val.field(3);
        }
    }

    /* Process next iteration of this panel */
    public void process() {
        // Step u-velocity:
        for(int j = 1; j < u[0].length-1; j++) {
            for (int i = 2; i < u.length-1; i++){
                u[i][j][kuv_new] = u[i][j][kuv_old]-facgx*(eta[i][j][keta_old]-eta[i-1][j][keta_old])-facbf*u[i][j][kuv_old]*uf/hu[i][j]+facwx/hu[i][j];
            }
        }

        // Step v-velocity:
        for(int j = 2; j < v[0].length-1; j++) {
            for (int i = 1; i < v.length-1; i++){
                v[i][j][kuv_new] = v[i][j][kuv_old]-facgy*(eta[i][j][keta_old]-eta[i][j-1][keta_old])-facbf*v[i][j][kuv_old]*uf/hv[i][j]+facwy/hv[i][j];
            }
        }

        /* INCREMENT KUV TIMESTEP */
        kuv++;

        /* REVERSE TIME INDICES */
        kuv_old = Math.abs(kuv_old-1);
        kuv_new = Math.abs(kuv_new-1);

        /* CALCULATE STEP ELEVATION */
        for(int j = 1; j < eta[0].length-1; j++) {
            for(int i = 1; i < eta.length-1; i++) {
                eta[i][j][keta_new] = eta[i][j][keta_old]-(facex*(u[i+1][j][kuv_old]*hu[i+1][j]-u[i][j][kuv_old]*hu[i][j])+facey*(v[i][j+1][kuv_old]*hv[i][j+1]-v[i][j][kuv_old]*hv[i][j]));
            }
        }

        /* INCREMENT KETA TIMESTEP */
        keta++;

        /* REVERSE TIME INDICES */
        keta_old = Math.abs(keta_old-1);
        keta_new = Math.abs(keta_new-1);
    }

    public void init() {
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < length; j++) {
                u[i][j][0]   = 0.0;
                u[i][j][1]   = 0.0;
                v[i][j][0]   = 0.0;
                v[i][j][1]   = 0.0;
                eta[i][j][0] = 0.0;
                eta[i][j][1] = 0.0;
                h[i][j]  = Math.random();
                hu[i][j] = Math.random();
                hv[i][j] = Math.random();
            }
        }
    }

    public void cleanUp() {
        h = null;
        hu = null;
        hv = null;
        System.gc();

        double[][][] utemp = new double[u.length][u[0].length][2];
        for(int i = 0; i < u.length; i++)
            for(int j = 0; j < u[0].length; j++)
                utemp[i][j][kuv_new] = u[i][j][kuv_new];
        u = utemp;
        utemp = null;

        double[][][] vtemp = new double[v.length][v[0].length][2];
        for(int i = 0; i < v.length; i++)
            for(int j = 0; j < v[0].length; j++)
                vtemp[i][j][kuv_new] = v[i][j][kuv_new];
        v = vtemp;
        vtemp = null;

        double[][][] etatemp = new double[eta.length][eta[0].length][2];
        for(int i = 0; i < eta.length; i++)
            for(int j = 0; j < eta[0].length; j++)
                etatemp[i][j][keta_new] = eta[i][j][keta_new];
        eta = etatemp;
        etatemp = null;
    }
}
