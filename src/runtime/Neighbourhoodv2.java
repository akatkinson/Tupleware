/**
 *  Neighbourhoodv2.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *  Created:  9/10/2007
 *  Modified: 9/10/2007
 *
 *  Description:
 */

package runtime;

import scope.*;
import space.*;

public class Neighbourhoodv2 implements java.io.Serializable {
    private TupleSpaceStub[][] neighbours;
    private int i,j;

    public Neighbourhoodv2(int i, int j) {
        this.i = i;
        this.j = j;
    }

    public void setNeighbours(TupleSpaceStub[][] neighbours) {
        this.neighbours = neighbours;
    }

    public TupleSpaceStub getNeighbour(int i, int j) {
        if((i > neighbours.length) || (j > neighbours[0].length) || (i < 0) || (j < 0))
            return null;

        return neighbours[i][j];
    }
}
