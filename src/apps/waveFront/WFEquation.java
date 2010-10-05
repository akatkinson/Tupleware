/**
 *  WFEquation.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *
 *  Created:  15/10/2007
 *  Modified: 15/10/2007
 */

package apps.waveFront;

import space.*;
import runtime.*;

public interface WFEquation {
    public double solve(double m, double n, double o);
    public void initData(double[][] data);
}
