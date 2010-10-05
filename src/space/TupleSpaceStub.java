/**
 * TupleSpaceStub.java
 *
 * Author:   Alistair Atkinson (alatkins@utas.edu.au)
 * Created:  4/8/2004
 * Modified: 12/4/2007
 *
 * Description: A stub object that can be used to communicate with
 *              a tuplespace service.
 */

package space;

import static space.TupleSpaceRequest.*;
import static space.TupleSpaceResponse.*;
import scope.Scope;
import java.io.*;
import java.net.*;
import java.util.*;

public class TupleSpaceStub implements TupleSpace, Comparable {
    public final double ADJUST_FACTOR = 0.2;

    private InetSocketAddress sockAddress;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Scope associatedScope;
    public int id;
    public double successFactor;

    public TupleSpaceStub(InetSocketAddress sockAddress) {
        this.sockAddress = sockAddress;
        socket = new Socket();
        in = null;
        out = null;
        successFactor = 0.5;
    }

    public void setScope(Scope s) {
        associatedScope = s;
    }

    public Scope getScope() {
        return associatedScope;
    }

    public InetSocketAddress getSockAddress() {
        return sockAddress;
    }

    public boolean equals(TupleSpaceStub stub) {
        if(sockAddress.equals(stub.getSockAddress()))
            return true;

        return false;
    }

    /**
     * Implementation of TupleSpace interface
     */
    public void out(Tuple t) throws IOException {
        doRequest(t, RequestType.OUT, 0);
    }

    public void outAll(Vector<Tuple> tpls) throws IOException {
        doRequest(new TupleSpaceRequest(RequestType.OUTALL, tpls));
    }

    public Tuple in(TupleTemplate t) throws IOException {
        return doSingleRequest(t, RequestType.IN);
    }

    public Tuple rd(TupleTemplate t) throws IOException {
        return doSingleRequest(t, RequestType.RD);
    }

    public Tuple inp(TupleTemplate t) throws IOException {
        return doSingleRequest(t, RequestType.INP);
    }

    public Tuple rdp(TupleTemplate t) throws IOException {
        return doSingleRequest(t, RequestType.RDP);
    }

    public Vector<Tuple> inAll(TupleTemplate t, int expected) throws IOException {
        return doBatchRequest(t, RequestType.INALL, expected);
    }

    public Vector<Tuple> rdAll(TupleTemplate t, int expected) throws IOException {
        return doBatchRequest(t, RequestType.RDALL, expected);
    }

    public Vector<Tuple> doBatchRequest(TupleTemplate t, RequestType type, int expected) throws IOException {
        return doRequest(t, type, expected).getTuples();
    }

    public Tuple doSingleRequest(TupleTemplate t, RequestType type) throws IOException {
        return doRequest(t, type, 1).getTuple();
    }

    /**
     * The doRequest() methods performs the actual tuplespace request using
     * the given parameters.
     */
    private TupleSpaceResponse doRequest(Tuple tuple, RequestType type, int expected) throws IOException {
System.out.println("Requested tuple/template: " + tuple.toString());
        TupleSpaceRequest request = new TupleSpaceRequest(type, tuple, expected);

        if((!socket.isConnected()) || (out == null) || (in == null)) {
            try {
                socket = new Socket(sockAddress.getAddress(), sockAddress.getPort());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());
            } catch (UnknownHostException e1) {
                // tuplespace unavailable, need to discover another
                System.out.println("UnknownHostException -> "+sockAddress.getAddress());
                return null;
            } catch(Exception e) {
                System.out.println("Failed to connect to: "+sockAddress.getAddress());
                //e.printStackTrace();
                return null;
            }
        }

        out.writeObject(request);
        out.flush();

        TupleSpaceResponse response = null;
        try {

            response = (TupleSpaceResponse) in.readObject();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        // close IO streams
        //in.close();
        //out.close();
        //socket.close();

        if(response == null)
            return null;

        if(response.getStatus() == Status.ERROR)
            throw new IOException();

        return response; // this may still return null if performing an OUT
    }

    /* NEEDLESS CODE DUPLICATION - FIX! */
    private TupleSpaceResponse doRequest(TupleSpaceRequest request) throws IOException {
        if((!socket.isConnected()) || (out == null) || (in == null)) {
            try {
                socket = new Socket(sockAddress.getAddress(), sockAddress.getPort());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());
            } catch (UnknownHostException e1) {
                // tuplespace unavailable, need to discover another
                System.out.println("UnknownHostException -> "+sockAddress.getAddress());
                return null;
            } catch(Exception e) {
                System.out.println("Failed to connect to: "+sockAddress.getAddress());
                //e.printStackTrace();
                return null;
            }
        }

        out.writeObject(request);
        out.flush();

        TupleSpaceResponse response = null;
        try {
            response = (TupleSpaceResponse) in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        // close IO streams
        //in.close();
        //out.close();
        //socket.close();

        if(response == null)
            return null;

        if(response.getStatus() == Status.ERROR)
            throw new IOException();

        return response; // this may still return null if performing an OUT
    }

    public void signalFailure() {
        if(successFactor == 1.0) return;

        successFactor = successFactor+(1.0 - successFactor)*ADJUST_FACTOR;
    }
    
    public void signalSuccess() {
        if(successFactor == 0.0) return;

        successFactor = successFactor-successFactor*ADJUST_FACTOR;
    }

    /* Implementation of the Comparable interface */
    public int compareTo(Object o) {
        if(!(o instanceof TupleSpaceStub))
            return 0;

        if(successFactor < ((TupleSpaceStub) o).successFactor)
            return -1;

        if(successFactor > ((TupleSpaceStub) o).successFactor)
            return 1;

        if(successFactor == ((TupleSpaceStub) o).successFactor)
            return 0;

        return 0;
    }
}
