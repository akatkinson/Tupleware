/**
 *  MandelbrotWorker.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *  Created:  1/5/2007
 *  Modified: 13/5/2007
 */

package apps.mandelbrot;

import space.*;
import runtime.*;
import java.util.*;

public class MandelbrotWorker {
    public TupleSpaceRuntime space;

    public MandelbrotWorker(int port) {
        space = new TupleSpaceRuntime(port, false);
    }

    public void execute() {
        space.start();
        /*MBTask task;

        for (;;) {
            task = null;
            byte[][] points;
            try {
                task = (MBTask)
                    space.in(new TupleTemplate("mbtask", null, null)).field(2);

                System.out.println("Worker got task "
                    + task.start +
                    " for job " + task.jobId);

                points = calculateMandelbrot(task);
                MBResult result = new MBResult(task.jobId,
                    task.start, points);

                System.out.println(
                    "Worker writing result for task " +
                    result.start + " for job " + result.jobId);
                space.out(new Tuple("mbresult", new Long(task.jobId), result));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/

        Vector<MBTask> tasks = (Vector<MBTask>) space.in(new TupleTemplate("mbtask", null)).field(1);
        //Vector<Tuple> results = new Vector<Tuple>();

        for(MBTask task : tasks) {
            byte[][] points;

            points = calculateMandelbrot(task);
            MBResult result = new MBResult(task.jobId, task.start, points);

            System.out.println(
                "Worker writing result for task " +
                result.start + " for job " + result.jobId);
            //space.out(new Tuple("mbresult", new Long(task.jobId), result));

            //results.addElement(new Tuple("mbresult", new Long(task.jobId), result));
            space.out(new Tuple("mbresult", new Long(task.jobId), result));
        }

        //space.outAll(results);

        space.stop();

        System.exit(1);
    }

    public static byte[][] calculateMandelbrot(MBTask task) {
        double x, y, xx, a, b;
        int start = task.start.intValue();
        int lines = task.lines.intValue();
        int end = start + task.lines.intValue();
        int width = task.width.intValue();
        int height = task.height.intValue();

        double da = task.x2.doubleValue()/width;
        double db = task.y2.doubleValue()/height;

        b = task.y1.doubleValue();

        byte[][] points = new byte[width][lines];

        for (int i = 0; i < start; i++) {
            b = b + db;
        }

        int k = 0;

        for (int i = start; i < end; i++, k++) {
            a = task.x1.doubleValue();
            for (int j = 0; j < width; j++) {
                byte n = 0;
                x = 0.0;
                y = 0.0;
                while ( (n < 100) && ( (x*x)+(y*y) < 4.0) ) {
                    xx = x * x - y * y + a;
                    y = 2 * x * y + b;
                    x = xx;
                    n++;
                }
                points[j][k] = n;
                a = a + da;
            }
            b = b + db;
        }
        return points;
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: java apps.mandelbrot.MandelbrotWorker <PORT>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        new MandelbrotWorker(port).execute();
    }
}
