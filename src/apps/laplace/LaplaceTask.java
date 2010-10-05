/**
 *  LaplaceTask.java
 */

package apps.laplace;

import java.util.*;
import space.*;

public class LaplaceTask implements java.io.Serializable {
    double[][] panel;
    long guid;
    long north, south, east, west;
    final int TSTEPS;
    int t;

    public LaplaceTask(double[][] panel, long guid, int tsteps) {
        this.panel = panel;
        this.guid  = guid;
        this.TSTEPS = tsteps;
        t = 0;
        north = south = east = west = 0;
    }

    public void solve() {
        for(int i = 1; i < panel.length-1; i++) {
            for(int j = 1; j < panel[0].length; j++) {
                panel[i][j] = 0.25 * (panel[i-1][j] +
                                      panel[i+1][j] +
                                      panel[i][j+1] +
                                      panel[i][j-1]);
            }
        }

        t++;
    }

    public Vector<LaplaceBoundary> getBoundaries() {
        Vector<LaplaceBoundary> bounds = new Vector<LaplaceBoundary>();

        if(north != 0) {
            double[] b = new double[panel[0].length - 2];
            int i = panel.length-2;
            for(int j = 1; j < panel[0].length-1; j++) {
                b[j-1] = panel[i][j];
            }

            bounds.addElement(new LaplaceBoundary(b, guid, north, "south"));
        }

        if(south != 0) {
            double[] b = new double[panel[0].length - 2];
            int i = 1;
            for(int j = 1; j < panel[0].length-1; j++) {
                b[j-1] = panel[i][j];
            }

            bounds.addElement(new LaplaceBoundary(b, guid, south, "north"));
        }

        if(east != 0) {
            double[] b = new double[panel.length - 2];
            int j = 1;
            for(int i = 1; i < panel.length-1; i++) {
                b[i-1] = panel[i][j];
            }

            bounds.addElement(new LaplaceBoundary(b, guid, east, "west"));
        }

        if(west != 0) {
            double[] b = new double[panel.length - 2];
            int j = panel[0].length-2;
            for(int i = 1; i < panel.length-1; i++) {
                b[i-1] = panel[i][j];
            }

            bounds.addElement(new LaplaceBoundary(b, guid, west, "east"));
        }

        return bounds;
    }

    public void updateHalo(Vector<LaplaceBoundary> bounds) {
        for(LaplaceBoundary bound : bounds) {
            double[] b = bound.boundary;

            if(bound.side.equals("north")) {
                int i = panel.length-1;
                for(int j = 1; j <= b.length; j++) {
                    panel[i][j] = b[j-1];
                }
            }

            if(bound.side.equals("south")) {
                int i = 0;
                for(int j = 1; j <= b.length; j++) {
                    panel[i][j] = b[j-1];
                }
            }

            if(bound.side.equals("east")) {
                int j = 0;
                for(int i = 1; i <= b.length; i++) {
                    panel[i][j] = b[i-1];
                }
            }

            if(bound.side.equals("west")) {
                int j = panel[0].length-1;
                for(int i = 1; i <= b.length; i++) {
                    panel[i][j] = b[i-1];
                }
            }
        }
    }

    public Vector<TupleTemplate> getBoundaryTemplates() {
        Vector<TupleTemplate> templates = new Vector<TupleTemplate>();

        if(north != 0) {
            TupleTemplate t = new TupleTemplate("laplace", "boundary", null, Long.toString(guid), "north");
            templates.add(t);
        }

        if(south != 0) {
            TupleTemplate t = new TupleTemplate("laplace", "boundary", null, Long.toString(guid), "south");
            templates.add(t);
        }

        if(east != 0) {
            TupleTemplate t = new TupleTemplate("laplace", "boundary", null, Long.toString(guid), "east");
            templates.add(t);
        }

        if(west != 0) {
            TupleTemplate t = new TupleTemplate("laplace", "boundary", null, Long.toString(guid), "west");
            templates.add(t);
        }

        return templates;
    }
}
