/**
 *  WFMultiplier.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *
 *  Created:  15/10/2007
 *  Modified: 15/10/2007
 */

package apps.waveFront;

import space.*;
import runtime.*;

public class WFMultiplier implements WFEquation {
    public double solve(double m, double n, double o) {
        return m * n * o;
    }

    public void initData(double[][] data) {
        for(int i = 0; i < data.length; i++)
            data[i][0] = Math.random() * 9999;

        for(int j = 1; j < data[0].length; j++)
            data[0][j] = Math.random() * 9999;

        for(int i = 1; i < data.length; i++) {
            for(int j = 1; j < data[0].length; j++) {
                data[i][j] = 0.0;
            }
        }
    }
}
