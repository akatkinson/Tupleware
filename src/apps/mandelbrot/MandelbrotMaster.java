/**
 *  MandelbrotMaster.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *  Created:  1/5/2007
 *  Modified: 13/5/2007
 */

package apps.mandelbrot;

import space.*;
import runtime.*;
import java.util.*;
import com.clarkware.profiler.*;

public class MandelbrotMaster {
    private TupleSpaceRuntime space;
    private int xsize = 640; // dimensions of window
    private int ysize = 480;
    private ArrayList<Long> openJobs;
    private Vector<MBResult> results;
    private int tasks = 12;  // number of tasks to generate (per set being computed)
    private int sets  = 300;   // number of mandelbrot sets to compute
    private int lines;       // # of scan lines per task

    // initial region for which Mandelbrot is being computed
    private double x1 = -2.25;
    private double x2 =  3.0;
    private double y1 = -1.8;
    private double y2 =  3.3;

    private boolean done = false;   // computation finished?
    private int progress;           // number of scan lines

    public MandelbrotMaster(int port) {
        space = new TupleSpaceRuntime(port, true);
        lines = ysize / tasks;  // scan lines per task
        openJobs = new ArrayList<Long>();
        results = new Vector<MBResult>();
    }

    public void execute() {
        space.start();

        Profiler.begin("Total Runtime");

        Profiler.begin("Task Generation");
        Vector<MBTask> mbtasks = new Vector<MBTask>();
        for(int i = 0; i < sets; i++)
            this.generateTasks(mbtasks);

        while(mbtasks.size() > 0) {
            Vector<MBTask> v = new Vector<MBTask>();
            for(int i = 0; i < sets; i++) {
                v.addElement(mbtasks.remove(0));
            }

            space.out(new Tuple("mbtask", v));
        }
        Profiler.end("Task Generation");

        Profiler.begin("Task Collection");
        for(Long jobId : openJobs)
            this.collectResults(jobId);
        Profiler.end("Task Collection");

        Profiler.begin("Task Processing");
        System.out.print("Processing results...");
        for(MBResult result : results)
            processResult(result);
        System.out.println("done.");
        Profiler.end("Task Processing");

        Profiler.end("Total Runtime");

        space.stop();

        Profiler.print();

        System.exit(1);
    }

    private void generateTasks(Vector<MBTask> tasks) {
        MBTask task = new MBTask();

        long millis = System.currentTimeMillis();
        task.jobId = new Long(millis);

        task.x1 = new Double(x1);
        task.x2 = new Double(x2);
        task.y1 = new Double(y1);
        task.y2 = new Double(y2);

        task.width  = new Integer(xsize);
        task.height = new Integer(ysize);
        task.lines  = new Integer(lines);

        for (int i = 0; i < ysize; i += lines) {
            task.start = new Integer(i);
            try {
                /*System.out.println("Master writing task " +
                    task.start + " for job " + task.jobId);*/

                //space.out(new Tuple("mbtask", task.jobId, task));
                tasks.addElement(task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        openJobs.add(task.jobId);
        //return task;
    }

    private void collectResults(Long jobId) {
        System.out.println("MandelbrotMaster::collectResults for task " + jobId);
        MBResult result = null;

        // Iterative retrieval using singular operations
        for(int i = 0; i < tasks; i++) {
            try {
                result = (MBResult) space.ts.rd(new TupleTemplate("mbresult", jobId, null)).field(2);

                System.out.println("Master collected result " +
                    result.start + " for job " + result.jobId);
                results.add(result);
                //progress += lines;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Using bulk operations
        /*Vector<Tuple> v = space.rdAll(new TupleTemplate("mbresult", jobId, null), tasks);

        for(Tuple t : v) {
            results.add((MBResult) t.field(2));
        }*/
    }

    private void processResult(MBResult result) {
        //System.out.println("Result " + result.jobId + " processed.");
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: java apps.mandelbrot.MandelbrotMaster <PORT>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        new MandelbrotMaster(port).execute();
    }
}
