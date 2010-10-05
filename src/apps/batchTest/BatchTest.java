/**
 * BatchTest.java
 *
 * Author:   Alistair Atkinson {alatkins@utas.edu.au}
 * Created:  6/10/2006
 * Modified: 7/10/2006
 */

package apps.batchTest;

import scope.*;
import space.*;
import runtime.*;
import java.util.*;
import com.clarkware.profiler.*;

public class BatchTest {
    private TupleSpaceRuntime ts;
    private Random rand;
    private String name;

    public BatchTest(int port, String name) {
        ts = new TupleSpaceRuntime(port, false);
        ts.setScope(new Scope(name));
        ts.start();
        this.name = name;
        rand = new Random();
        doTests();
        ts.stop();
    }

    public void doTests() {
        Vector<Tuple> tpls = null;
        System.out.print("Writing tuples to space...");
        for(int i = 0; i < 10; i++) {
            ts.out(new Tuple(name, new Integer(rand.nextInt())));
        }

        System.out.println("Done.");

        try {
            int sleepTime = (name.equals("Proc2"))? 60000:5000;
            Thread.currentThread().sleep(sleepTime);
        } catch(InterruptedException e) {}

        //String remoteProcName = (name.equals("Proc1"))? "Proc2": "Proc1";
        String remoteProcName = "Proc2";
        System.out.print("Try to read tuples...");
        Profiler.begin("rdAll() BT");
        tpls = ts.rdAll(new TupleTemplate(remoteProcName, null), 10);
        Profiler.end("rdAll() BT");
        System.out.println("Done.");

        if(tpls == null) {
            System.out.println("No tuples returned! Reverting to manual in ops.");
            //return;
        } else {
            System.out.println("Tuples returned: "+tpls.size());
        }

        /*tpls = new Vector<Tuple>();
        for(int i = 0; i < 10; i++) {
            tpls.addElement(ts.in(new TupleTemplate(remoteProcName, null)));
        }*/

        for(Tuple t : tpls) {
            System.out.println(t.toString());
        }
        Profiler.print();
    }

    public static void main(String[] args) {
        if(args.length != 2) {
            usage();
            System.exit(1);
        }

        int port = 0;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            usage();
            System.exit(1);
        }

        new BatchTest(port, args[1]);
    }

    public static void usage() {
        System.out.println("java apps.BatchTest <port> <name>");
    }
}
