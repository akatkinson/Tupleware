/**
 *  RequestLogger.java
 *
 *  Author:   Alistair Atkinson {alatkins@utas.edu.au}
 *  Created:  28/8/2007
 *  Modified: 3/9/2007
 */

package runtime;

public class RequestLogger {
    private int[] log;
    private int i;

    public RequestLogger(int n) {
        log = new int[n];
        for(int i = 0; i < n; i++) {
            log[i] = 0;
        }

        i = 0;
    }

    public void signalSuccess() {
        log[i] += 1;
        i = 0;
    }

    public int signalFail() {
        if(i < log.length-1)
            i++;

        return i;
    }

    public void abort() {
        i = 0;
    }

    public int totalRequests() {
        int sum = 0;

        for(int i = 0; i < log.length; i++) {
            sum += log[i];
        }

        return sum;
    }

    public void reset() {
        for(int i = 0; i < log.length; i++) {
            log[i] = 0;
        }

        i = 0;
    }

    private double percentageSuccess(int n) {
        if((n < 0) || (n >= log.length) || (totalRequests() == 0))
            return 0.0;

        return ((double) log[n] / (double) this.totalRequests()) * 100.0;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("-----------------------\n");
        buf.append("- Request Log Output  -\n");
        buf.append("-----------------------\n");
        buf.append("\n Total requests logged: " + totalRequests() + "\n");
        buf.append("\nNo.\tSuccess\t% Success\n");

        for(int i = 0; i < log.length; i++) {
            String s = Double.toString(percentageSuccess(i));
            String t = (s.length() > 3)? s.substring(0, 2) : s;

            buf.append(i + "\t" + log[i] + "\t" + t + "\n");
        }

        return buf.toString();
    }
}
