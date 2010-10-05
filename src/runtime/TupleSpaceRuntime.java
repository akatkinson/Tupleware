/**
 *  TupleSpaceRuntime.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *  Created:  6/9/2005
 *  Modified: 24/9/2007
 */

package runtime;

import scope.*;
import space.*;
import java.io.*;
import java.util.*;
import java.net.*;
import com.clarkware.profiler.*;
import static runtime.Neighbourhood.*;

public class TupleSpaceRuntime {
    public final int NODES = 12;

    public final static Scope DEFAULT_SCOPE = new Scope("DEFAULT_SCOPE");
    private Scope thisScope;

    private final InetSocketAddress GTS_ADDRESS =
                                    //new InetSocketAddress("144.6.40.143", 6001); // staff-143
                                    //new InetSocketAddress("144.6.40.116", 6001); // cluster-nhm-01
                                    //new InetSocketAddress("144.6.40.115", 6001); // cluster-nhm-16
                                    //new InetSocketAddress("127.0.0.1", 6001);    // localhost
                                    new InetSocketAddress("10.10.10.11", 6001);    // software lab

    public TupleSpaceStub gts;
    public TupleSpaceImpl ts;
    private TupleSpaceService service;
    private Vector<TupleSpace> remoteSpaces;
    private RequestLogger log;
    private boolean isGlobal;
    private int imin, imax, jmin, jmax;
    public int requestCount, totalRequests;

    public TupleSpaceRuntime(int port, boolean isGlobal) {
        ts = new TupleSpaceImpl();
        remoteSpaces = new Vector<TupleSpace>(1, 1);
        this.isGlobal = isGlobal;
        thisScope = DEFAULT_SCOPE;
        //nbhood = new Neighbourhood(thisScope);
        log = new RequestLogger(3);
        requestCount = 0;
        totalRequests = 0;

        service = new TupleSpaceService(port, ts);
    }

    public void setScope(Scope s) {
        thisScope = s;
    }

    public void setDefaultScope() {
        thisScope = DEFAULT_SCOPE;
    }

    public void setBoundaries(int imin, int imax, int jmin, int jmax) {
        this.imin = imin;
        this.imax = imax;
        this.jmin = jmin;
        this.jmax = jmax;
    }

    public void start() {
        service.start(); // starts TupleSpaceService thread

        try {
            this.register();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public void stop() {
        // deregister this runtime (not yet implemented).

        service.shutdown(); // stops TupleSpaceService thread
        //Profiler.print();
        System.out.println(log.toString());
        log.reset();
    }

    /* Tuple space operations */
    public void out(Tuple t) {
        Profiler.begin("out()");

        ts.out(t);

        /*if(!isGlobal) {
            try {
                gts.out(t);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }*/

        /*for(TupleSpace service : remoteSpaces) {
            try {
                service.out(t);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        Profiler.end("out()");
    }

    public void outAll(Vector<Tuple> tuples) {
        Profiler.begin("outAll()");

        ts.outAll(tuples);

        /*if(!isGlobal) {
            try {
                gts.outAll(tuples);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }*/

        Profiler.end("outAll()");
    }

    /* Output given tuple to every connected remote node */
    public void outEach(Tuple t) {
        Profiler.begin("outEach()");

        for(TupleSpace service : remoteSpaces) {
            try {
                service.out(t);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Profiler.end("outEach()");
    }

    /* Output given tuple to randomly selected remote node */
    public void outRand(Tuple t) {
        Profiler.begin("outRand()");

        int i = (int) Math.round(Math.random()*(remoteSpaces.size()-1));
        //TupleSpace service = remoteSpaces.elementAt(new Random().nextInt(remoteSpaces.size()));
        TupleSpace service = remoteSpaces.elementAt(i);
        try {
            service.out(t);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Profiler.end("outRand()");
    }

    public Tuple in(TupleTemplate t) {
        //sortStubs();
        requestCount++;

        Profiler.begin("in()");

        Tuple tpl = ts.inp(t);
        TupleSpace successful = null;

        if(tpl == null) {
            log.signalFail();
            for(TupleSpace service : remoteSpaces) {
                totalRequests++;
                try {
                    tpl = service.inp(t);

                    if(tpl != null) {
                        successful = service;
                        break;
                    } else {
                        ((TupleSpaceStub) service).signalFailure();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // move last successful stub to front of list
        if(successful != null) {
            remoteSpaces.remove(successful);
            remoteSpaces.add(0, successful);
            //((TupleSpaceStub) successful).signalSuccess();
        }

        /*if(tpl == null) {
            log.signalFail();
            if(isGlobal) {
                tpl = ts.in(t);
            } else {
                try {
                    tpl = gts.in(t);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }*/

        // If we still haven't retrieved required tuple, send out requests
        // and wait for reply.
        if(tpl == null) {
            log.signalFail();
            Vector<Tuple> results = new Vector<Tuple>();
            dispatchConcurrentRequests(remoteSpaces,
                                       results,
                                       t,
                                       ConcurrentRequestThread.IN_OP);

            if(results.size() > 0) {
                tpl = results.elementAt(0);
            }

            if(results.size() > 1) {
                for(int i = 1; i < results.size(); i++)
                    ts.out(results.elementAt(i));
            }
        }

        log.signalSuccess();
        Profiler.end("in()");

        return tpl;
    }

    public Tuple inp(TupleTemplate t) {
        sortStubs();

        Profiler.begin("inp()");

        Tuple tpl = ts.inp(t);
        Profiler.end("inp()");
        return tpl;
    }

    public Tuple rd(TupleTemplate t) {
        //sortStubs();
        requestCount++;

        Profiler.begin("rd()");

        Tuple tpl = ts.rdp(t);
        TupleSpace successful = null;

        if(tpl == null) {
            log.signalFail();
            for(TupleSpace service : remoteSpaces) {
                totalRequests++;
                try {
                    tpl = service.rdp(t);

                    if(tpl != null) {
                        successful = service;
                        break;
                    } else {
                        ((TupleSpaceStub) service).signalFailure();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // move last successful stub to front of list
        if(successful != null) {
            remoteSpaces.remove(successful);
            remoteSpaces.add(0, successful);
            //((TupleSpaceStub) successful).signalSuccess();
        }

        /*if(tpl == null) {
            log.signalFail();
            if(isGlobal) {
                tpl = ts.rd(t);
            } else {
                try {
                    tpl = gts.rd(t);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }*/

        // If we still haven't retrieved required tuple, send out requests
        // and wait for reply.
        if(tpl == null) {
            log.signalFail();
            Vector<Tuple> results = new Vector<Tuple>();
            dispatchConcurrentRequests(remoteSpaces,
                                       results,
                                       t,
                                       ConcurrentRequestThread.RD_OP);

            if(results.size() > 0) {
                tpl = results.elementAt(0);
            }
        }

        Profiler.end("rd()");
        log.signalSuccess();
        return tpl;
    }

    public Tuple rdp(TupleTemplate t) {
        sortStubs();

        Profiler.begin("rdp()");

        Tuple tpl = ts.rdp(t);
        Profiler.end("rdp()");
        return tpl;
    }

    public Vector<Tuple> rdAll(TupleTemplate t, int expected) {
        sortStubs();

        Profiler.begin("rdAll()");

        Vector<Tuple> tpls = new Vector<Tuple>();
        Vector<Tuple> v = ts.rdAll(t, expected);
        //Vector<Tuple> v = new Vector<Tuple>();
        if(v!=null)
            tpls.addAll(v);

        if(tpls.size() >= expected)
            return tpls;

        try{
            int i = 0;
            while((tpls.size() < expected) && (i < remoteSpaces.size())) {
                v = remoteSpaces.elementAt(i).rdAll(t, expected - tpls.size());
                if(v!=null)
                    tpls.addAll(v);
                i++;
            }

            /*if((tpls.size() < expected)&&(!isGlobal)) {
                v = gts.rdAll(t);
                if(v!=null)
                    tpls.addAll(v);
            }*/

            /*if(tpls.size() < expected) {
                dispatchConcurrentRequests(remoteSpaces,
                                           tpls,
                                           t,
                                           expected - tpls.size(),
                                           ConcurrentRequestThread.RD_OP);
            }*/

        } catch(IOException e) {
            if(tpls.size() == expected)
                return tpls;
            else
                return null;
        }

        Profiler.end("rdAll()");
        //System.out.println("Expected: "+expected+" Returned: "+tpls.size());

        return tpls;
    }

    public Vector<Tuple> inAll(TupleTemplate t, int expected) {
        sortStubs();

        Profiler.begin("inAll()");

        Vector<Tuple> tpls = new Vector<Tuple>();
        Vector<Tuple> v = ts.inAll(t, expected);
        if(v!=null) {
            tpls.addAll(v);
        }

        if(tpls.size() >= expected) {
            return tpls;
        }

        try {
            int i = 0;
            while((tpls.size() < expected) && (i < remoteSpaces.size())) {
                v = remoteSpaces.elementAt(i).inAll(t, expected - tpls.size());
                if(v!=null) {
                    tpls.addAll(v);
                    System.out.println(v.size());
                }
                i++;
            }

            /*if((tpls.size() < expected)&&(!isGlobal)) {
                v = gts.inAll(t);
                if(v!=null) {
                    tpls.addAll(v);
                    System.out.println(v.size());
                }
            }*/

            /*if(tpls.size() < expected) {
                dispatchConcurrentRequests(remoteSpaces,
                                           tpls,
                                           t,
                                           expected - tpls.size(),
                                           ConcurrentRequestThread.IN_OP);
            }*/
        } catch(IOException e) {
            if(tpls.size() == expected)
                return tpls;
            else
                return null;
        }

        Profiler.end("inAll()");

        return tpls;
    }

    private void batchReorder(Vector<Tuple> batchedTuples, final int field) {
        // Ensure all tuples are the same size. If not, then they are not
        // mutually comparable and so method returns
        int size = batchedTuples.elementAt(0).size();
        for(Tuple t : batchedTuples) {
            if(t.size() != size)
                return;
        }

        // Convert batchedTuples vector into array
        Tuple[] tpls = batchedTuples.toArray(new Tuple[batchedTuples.size()]);

        // Define our Comparator
        Comparator<Tuple> c = new Comparator<Tuple>() {
            public int compare(Tuple t1, Tuple t2) {
                return ((Comparable) t1.field(field)).compareTo((Comparable) t2.field(field));
            }
        };

        Arrays.sort(tpls, c); // Sort tuples
        batchedTuples = new Vector<Tuple>(batchedTuples.size());
        batchedTuples.copyInto(tpls); // copy array back into vector
    }

    public void register() throws IOException{
        if(isGlobal) {
            for(int i = 0; i < NODES; i++) {
                Tuple peerInfo = ts.rd(new TupleTemplate("node", null, (Integer) 6002+i));

                remoteSpaces.addElement(new TupleSpaceStub(new InetSocketAddress((String) peerInfo.field(1), (Integer) peerInfo.field(2))));
            }

            // create neighbourhood
            /*TupleSpaceStub[][] tss = new TupleSpaceStub[3][3];
            int x = 0;
            for(int i = 0; i < tss.length; i++) {
                for(int j = 0; j < tss[0].length; j++) {
                    tss[i][j] = (TupleSpaceStub) remoteSpaces.elementAt(x);
                    x++;
                }
            }

            for(int i = 0; i < tss.length; i++) {
                for(int j = 0; j < tss[0].length; j++) {
                    Neighbourhoodv2 n = new Neighbourhoodv2(i, j);
                    n.setNeighbours(tss);
                    ts.out(new Tuple("neighbourhood", n));
                }
            }*/

            remoteSpaces.addElement(ts);

            System.out.println("Master successfully registered. " + remoteSpaces.size() +" services registered.");

            return;
        }

        gts = new TupleSpaceStub(new InetSocketAddress(GTS_ADDRESS.getHostName(),
                                                       GTS_ADDRESS.getPort()));

        Tuple localInfo = new Tuple("node",
                                    getLocalIPAddress(),
                                    new Integer(service.getPort()));

        gts.out(localInfo);

        for(int i = 0; i < NODES; i++) {
            if(service.getPort() == 6002+i)
                continue;

            Tuple peerInfo = gts.rd(new TupleTemplate("node", null, (Integer) 6002+i));

            remoteSpaces.addElement(new TupleSpaceStub(new InetSocketAddress((String) peerInfo.field(1), (Integer) peerInfo.field(2))));
        }

        //remoteSpaces.addElement(gts);
        //remoteSpaces.addElement(ts); // add local space? EXPERIMENTAL

        System.out.println("we have all other node's info. " + remoteSpaces.size() +" services registered.");
    }

    private void dispatchConcurrentRequests(Vector<TupleSpace> stubs,
                                            Vector<Tuple> results,
                                            TupleTemplate template,
                                            boolean opType)
    {
        Thread[] requestThreads = new Thread[remoteSpaces.size()];

        for(int i = 0; i < remoteSpaces.size(); i++) {
            requestThreads[i] = new ConcurrentRequestThread(remoteSpaces.elementAt(i),
                                                            results,
                                                            template,
                                                            opType);
        }

        for(Thread t : requestThreads) {
            t.start();
        }

        while(results.size() == 0) {
            synchronized(results) {
                try {
                    results.wait();
                } catch(InterruptedException ie) {}
            }
        }

        for(int i = 0; i < requestThreads.length; i++) {
            requestThreads[i].interrupt();
        }

        for(int i = 0; i < requestThreads.length; i++) {
            try {
                requestThreads[i].join(500); // wait for threads to return?
            } catch(InterruptedException ie) {}
        }
    }

    public static String getLocalIPAddress() throws SocketException {
        Enumeration<InetAddress> interfaceAddresses = null;
            //NetworkInterface.getByName("eth0").getInetAddresses();
            //NetworkInterface.getByName("eth1").getInetAddresses();
        for(int i = 1; i <= 3; i++) {
            try {interfaceAddresses = NetworkInterface.getByName("eth"+i).getInetAddresses();}
            catch(NullPointerException npe) {continue;}
            if(interfaceAddresses != null) break;
        }

        interfaceAddresses.nextElement(); // pop first address (seems to generally be the IPv6 address)

        return interfaceAddresses.nextElement().getHostAddress();
    }

    private boolean isArrayElement(Tuple t) {
        if(t.size() < 4)
            return false;

        // Deal with special case for "task" tuples
        if((t.field(0) != null) &&
           (t.field(0) instanceof String) &&
           (((String)t.field(0)).equals("task")))
        {
            return false;
        }

        return ((t.field(1) != null) &&
                (t.field(2) != null) &&
                (t.field(1) instanceof Integer) &&
                (t.field(2) instanceof Integer));
    }

    private void sortStubs() {
        java.util.Collections.sort(remoteSpaces);
    }
}

class ConcurrentRequestThread extends Thread {
    public static final boolean RD_OP = false;
    public static final boolean IN_OP = true;

    private TupleSpace stub;
    private TupleTemplate template;
    private Vector<Tuple> result;
    private boolean opType;

    public ConcurrentRequestThread(TupleSpace stub,
                                   Vector<Tuple> result,
                                   TupleTemplate template,
                                   boolean opType)
    {
        this.stub = stub;
        this.template = template;
        this.result = result;
        this.opType = opType;
    }

    public void run() {
        while((result.size() == 0) && (!interrupted())) {
            Tuple res;

            try {
                if(opType == RD_OP) {
                    res = stub.rdp(template);
                } else {
                    res = stub.inp(template);
                }
            } catch(IOException ioe) {
                ioe.printStackTrace();
                return;
            }

            if(res != null) {
                synchronized(result) {
                    result.add(res);
                    result.notifyAll();
                    return;
                }
            }

            if(interrupted()) {
                return;
            }

            synchronized(this) {
                if(result.size() == 0) {
                    try {
                        this.wait(250);
                    } catch(InterruptedException ie) {
                        return;
                    }
                }
            }
        }
    }
}
