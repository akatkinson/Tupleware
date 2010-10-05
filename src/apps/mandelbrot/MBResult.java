/**
 *  MBResult.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *  Created:  2/5/2007
 *  Modified: 2/5/2007
 */

package apps.mandelbrot;

import java.io.Serializable;

public class MBResult implements Serializable {
    public Long jobId;      // id for Mandelbrot computation
    public Integer start;   // starting scan line of results
    public byte[][] points; // pixel values as byte integers

    public MBResult() {}

    public MBResult(Long jobId, Integer start, byte[][] points) {
        this.jobId = jobId;
        this.start = start;
        this.points = points;
    }
}
