/**
 *  ResultHandler.java
 *
 *  Author:     Alistair Atkinson (alatkins@gmail.com)
 *  Created:    3/7/2009
 *  Modified:   3/7/2009
 */

package util;

import java.net.*;
import java.util.*;
import java.io.*;

public class ResultHandler {
    private final int PORT;
    private volatile boolean running;
    
    public ResultHandler(int port) {
        this.PORT = port;
        running = true;
    }
    
    public void start() {
        ServerSocket sock;
        
        try {
            sock = new ServerSocket(PORT);
        } catch(IOException e) {
            return;
        }
        
        Socket in = null;
        Vector<Thread> threads = new Vector<Thread>();
        
        while(running) {
            try {
                in = sock.accept();
            } catch(IOException e) {}
            
            Thread t = new Thread(new RequestHandler(in));
            threads.add(t);
            t.start();
        }
        
        try {
            sock.close();
        } catch(IOException e) {}
        
        for(Thread thrd : threads) {
            try {
                thrd.join();
            } catch(InterruptedException e) {}
        }
    }
    
    protected void processResults() {
        /*
         *  Profiler output is formatted as follows:
         *
         *      IO:
         *      count = 1 total = 960 (ms) average = 960.0 (ms)
         *
         *      Computation:
         *      count = 2 total = 158 (ms) average = 79.0 (ms)
         *
         *  ...and so on for each profiler task.
         *
         *  Generally then:
         *
         *      <Task>:
         *      count = <#> total = <#> (ms) average = <#.#> (ms)
         */
    }
    
    public void stop() {
        running = false;
    }
    
    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: java ResultHandler <PORT>");  
            System.exit(1);
        }
        
        ResultHandler rh = new ResultHandler(Integer.parseInt(args[0]));
    }
}

class RequestHandler implements Runnable {
    public RequestHandler(final Socket socket) {}
    public void run() {}
}

