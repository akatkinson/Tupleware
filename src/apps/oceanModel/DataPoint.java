/**
 *  DataPoint.java
 *
 *  Author:   Alistair Atkinson {alatkins@utas.edu.au}
 *  Created:  18/5/2007
 *  Modified: 18/5/2007
 */

package apps.oceanModel;

import java.io.Serializable;

public class DataPoint implements Serializable {
    public String name;
    public int xcoord, ycoord;
    public double value;
    private boolean isBoundary;

    public DataPoint(String name, int xcoord, int ycoord) {
        this.name = name;
        this.xcoord = xcoord;
        this.ycoord = ycoord;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setBoundary(boolean isBoundary) {
        this.isBoundary = isBoundary;
    }

    public boolean isBoundary() {
        return isBoundary;
    }
}
