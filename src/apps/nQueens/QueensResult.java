/**
 *  QueensResult.java
 *
 *  Author:       Alistair Atkinson {alatkins@utas.edu.au}
 *  Created:      1-9-2003
 *  Modified:     13-8-2007
 *
 *  Description:
 */

package apps.nQueens;

public class QueensResult implements java.io.Serializable {
    public Integer numSolutions;

    public QueensResult(int solns) {
        numSolutions = new Integer(solns);
    }

    public int getNumSolns() {
        return numSolutions.intValue();
    }
}
