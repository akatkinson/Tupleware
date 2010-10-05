/**
 *  TupleSpaceService.java
 *
 *  Author:     Alistair Atkinson (alatkins@utas.edu.au)
 *  Created:    5/8/2004
 *  Modified:   20/6/2007
 *
 *  Description:
 */

package space;

import static space.TupleSpaceRequest.*;
import static space.TupleSpaceResponse.*;
import java.io.*;
import java.net.*;
import java.util.*;
import runtime.*;

public class TupleSpaceService extends Thread {
    public static final int DEFAULT_BULK_TIMEOUT = 2000;

    private TupleSpaceImpl space;
    private int port;
    private ServerSocket srvSocket;

    public TupleSpaceService(int port) {
        this.port = port;
        space = new TupleSpaceImpl();
    }

    public TupleSpaceService(int port, TupleSpaceImpl space) {
        this.port = port;
        this.space = space;
    }

    public int getPort() {
        return port;
    }

    /*
     *  Required Thread.run() method implementation.
     */
    public void run() {
        service();
    }

    public void shutdown() {
        // probably not the most graceful way to close things down :-)
        try {
            if(srvSocket != null)
                srvSocket.close();
        } catch (IOException e) {}
    }

    protected void service() {
        try {
            srvSocket = new ServerSocket(port, 256);
            System.out.println("Socket opened at " + srvSocket);

            while(true) {
                Socket request = srvSocket.accept();
                /*System.out.println("New connection established from "
                                    + request.getInetAddress());*/
                new Thread(new RequestHandler(request, space)).start();
            }
        } catch (IOException ioe) {
            System.out.println("TupleSpaceService exiting.");
        }
    }

    public static void main(String[] args) {
        if(args.length < 1) {
            System.out.println("Usage: java space.TupleSpaceService <port>");
            System.exit(1);
        }

        int port = 0;
        try {
            port = Integer.parseInt(args[0]);
        } catch(Exception e) {
            System.out.println("Supplied port number must be a valid integer.");
            System.exit(1);
        }

        // start new tuplespace service
        System.out.println("TupleSpaceService started on port " + port);
        TupleSpaceService service = new TupleSpaceService(port);
        service.service();
    }
}

/**
 * A Runnable class that will handle tuplespace requests.
 */
class RequestHandler implements Runnable {
    private Socket socket;
    private TupleSpaceImpl space;

    public RequestHandler(Socket socket, TupleSpaceImpl space) {
        this.socket = socket;
        this.space = space;

        if(!socket.isConnected()) {
           System.out.println("GARRR!");
           return;
        }
    }

    public void run() {
        boolean commsErrorOccurred = false;
        ObjectInputStream in = null;
        ObjectOutputStream out = null;

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        } catch(IOException ioe) {
            ioe.printStackTrace();
            return;
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }

        //System.out.println("Handling request from " + socket.getInetAddress());
        for(;;) {
            TupleSpaceResponse response = null;
            Tuple result = null;
            Vector<Tuple> batchResult = null;
            TupleSpaceRequest reqObj = null;

            try {
                reqObj = (TupleSpaceRequest) in.readObject();
            } catch(IOException ioe) {
                ioe.printStackTrace();
                response = new TupleSpaceResponse(Status.ERROR);
                commsErrorOccurred = true;
            } catch(Exception e) {
                e.printStackTrace();
                response = new TupleSpaceResponse(Status.ERROR);
                commsErrorOccurred = true;
            }

            switch(reqObj.getRequestType()) {
            case OUT:
              space.out(reqObj.getTuple());
              break;
            case OUTALL:
              space.outAll(reqObj.getTuples());
              break;
            case IN:
              result = space.in((TupleTemplate) reqObj.getTuple());
              break;
            case INP:
              result = space.inp((TupleTemplate) reqObj.getTuple());
              if(result == null) {
                  try { synchronized(space) {space.wait(250);} }
                  catch(InterruptedException e) { continue; }
                  result = space.inp((TupleTemplate) reqObj.getTuple());
              }
              break;
            case INALL:
              batchResult = space.inAll((TupleTemplate) reqObj.getTuple(),
                                         reqObj.getExpected());
              break;
            case RD:
              result = space.rd((TupleTemplate) reqObj.getTuple());
              break;
            case RDP:
              result = space.rdp((TupleTemplate) reqObj.getTuple());
              if(result == null) {
                  try { synchronized(space) {space.wait(250);} }
                  catch(InterruptedException e) { continue; }
                  result = space.rdp((TupleTemplate) reqObj.getTuple());
              }
              break;
            case RDALL:
              batchResult = space.rdAll((TupleTemplate) reqObj.getTuple(),
                                         reqObj.getExpected());
              break;
            default:
              result = null;
            }

            if(!commsErrorOccurred) {
                if(result != null)
                    response = new TupleSpaceResponse(Status.SUCCESS, result);
                else
                    response = new TupleSpaceResponse(Status.SUCCESS, batchResult);
            }

            /* need to catch error immediately when writing response */
            try {
                if(out != null) {
                    out.writeObject(response);
                    out.flush();
                }
            } catch (IOException e1) {
                e1.printStackTrace();

                /*
                 * If an error occurs while writing response after a destructive operation (IN, INP)
                 * then we need to replace the tuple that was remove from the tuplespace.
                 */
                if(reqObj != null)
                    if((reqObj.getRequestType() == RequestType.IN) || (reqObj.getRequestType() == RequestType.INP))
                        space.out(reqObj.getTuple());

                commsErrorOccurred = true;
            }

            if(commsErrorOccurred) {
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch(Exception e) {
                    return;
                }

                return;
            }

            //System.out.println("Completed request from " + socket.getInetAddress());
        }
    }
}
