/**
 *  MBTask.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *  Created:  2/5/2007
 *  Modified: 2/5/2007
 */

package apps.mandelbrot;

import java.io.Serializable;

public class MBTask implements Serializable {
    public Long jobId;      // id for Mandelbrot computation

    // region for which application is computing Mandelbrot
    public Double x1;       // starting x
    public Double y1;       // starting y
    public Double x2;       // ending x
    public Double y2;       // ending y

    // slice of the region this task computes
    public Integer start;   // starting scan line
    public Integer width;   // width of scan line in image
    public Integer height;  // number of scan lines in image
    public Integer lines;   // number of scan lines per task

    public MBTask() {}
}
