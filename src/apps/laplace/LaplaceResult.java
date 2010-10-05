/**
 *  LaplaceResult.java
 */

package apps.laplace;

public class LaplaceResult implements java.io.Serializable {
    double[][] panel;

    public LaplaceResult(double[][] panel) {
        this.panel = new double[panel.length-2][panel[0].length-2];

        // remove halo, preserve data
        for(int i = 1; i < panel.length-1; i++) {
            for(int j = 1; j < panel[0].length-1; j++) {
                this.panel[i-1][j-1] = panel[i][j];
            }
        }
    }
}
