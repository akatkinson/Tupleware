package com.clarkware.profiler;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;

/**
 * The <code>Profiler</code> class is a simple tool
 * used to profile the performance of <i>instrumented</i>
 * regions of source code by recording specific runtime
 * performance measurements.
 * <p>
 * <b>Profiling Code</b>
 * <p>  
 * A region of source code is instrumented by invoking the 
 * static methods <code>begin()</code> and <code>end()</code> 
 * around the source code, designating the beginning 
 * and ending of an event to measure.  These methods require 
 * an event description used to uniquely indentify the event 
 * being measured.  The event description can be any useful 
 * description, but it must be used consistently in the matching 
 * <code>begin()</code> and <code>end()</code> methods.
 * The event description specified for the <code>end()</code> 
 * method must be exactly the same as the event description 
 * used in the corresponding <code>begin()</code> method.
 * <p>
 * Calls to <code>begin()</code> may be nested within other 
 * calls to <code>begin()</code> provided that calls to
 * <code>end()</code> are matched up with their respective
 * calls to <code>begin()</code>.  In other words, matching
 * pairs of <code>begin()</code> and <code>end()</code> 
 * calls must be well formed.  In the case of a mismatch,
 * the current thread's event stack trace is displayed and 
 * the profiler is disabled.
 * <p>
 * <b>Profiler Modes</b>
 * <p> 
 * By default, the <code>begin()</code> and <code>end()</code>
 * methods record the amount of time that elapsed between their
 * invocations.  When the profiler is disabled using the static 
 * <code>setEnabled()</code> method, the <code>begin()</code>
 * and <code>end()</code> methods return immediately to 
 * minimize additional overhead.
 * <p>
 * Memory profiling can optionally be enabled using the static 
 * <code>enableMemory()</code> method to record the amount of 
 * memory consumed or garbage collected during an event.
 * Memory profiling is disabled by default.
 * <p>
 * <b>Profiler Output</b>
 * <p> 
 * At any time, the static <code>print()</code> method can be 
 * invoked to print out the performance measurements collected 
 * for each of the recorded events.  Alternatively, the static 
 * <code>printAndReset()</code> method can be invoked to print 
 * out the performance measurements and then reset all event
 * measurements.  The static <code>printAndClear()</code> method
 * prints out the performance measurements and then clears the
 * event history.  The raw data produced by these methods can 
 * then be formatted as appropriate.  
 * <p>
 * At any time, the static <code>printStackTrace()</code> method
 * can be invoked to print the event stack trace for the current
 * thread.  This is useful for identifying the exact event being 
 * recorded in an active or stalled thread.
 * <p>
 * <b>Collected Measurements</b>
 * <p> 
 * The following performance measurements are collected for 
 * each instrumented event:
 * <ul>
 *   <li>Number of times the event occurred
 *   <li>Total execution time of the event (in milliseconds) 
 *   <li>Average execution time of the event (in milliseconds) 
 *   <li>If memory profiling is enabled: 
 *       <ul>
 *         <li>Total memory consumed during the event (in bytes) 
 *         <li>Total memory garbage collected during the event (in bytes)
 *       </ul>
 * </ul>
 * <p>
 * <b>Multi-Threaded Profiling</b>
 * <p>
 * This class is multi-thread safe to allow multiple threads to
 * be profiled concurrently.  Event measurements are collected 
 * globally for all active threads.
 * <p>
 * The profiler maintains a history of events in a thread-specific
 * stack. The static <code>printStackTrace()</code> method
 * can be invoked to print the event stack trace for the current
 * thread.  This is useful for identifying the exact event being 
 * recorded in an active or stalled thread.
 * <p>
 * <b>Profiler Overhead</b>
 * <p>
 * Recording performance measurements using this class is intended 
 * to be very efficient with negligible overhead.  The total overhead 
 * incurred per combined invocation of <code>begin()</code> and 
 * <code>end()</code>, with memory profiling disabled, has been 
 * measured to not exceed 3 milliseconds using JDK 1.3.  
 * <p>
 * The number of strings and objects created by the <code>begin()</code>
 * and <code>end()</code> methods was kept to a bare minimum to
 * minimize the memory footprint.
 * <p>
 * The calculation of overall performance measurements for recorded 
 * events is delayed until a print method is invoked.
 * <p>
 * Remember, however, that there's no way to avoid Heisenberg's 
 * Uncertainty Principle when measuring the performance of software 
 * (or anything obeying the laws of physics, for that matter).  
 * Therefore, this class can safely be used as a development tool, 
 * but it is not necessarily suitable for production systems under 
 * heavy load.
 * <p>
 * <b>Example Uses</b>
 * <p>
 * <blockquote>
 * <pre>
 *
 * Profiler.begin("String concatentation");
 * String s = "";
 * for (int i=0; i < 10000; i++) {
 *     s = s + "a";
 * }
 * Profiler.end("String concatentation");
 * Profiler.print();
 *
 *
 * Profiler.begin("StringBuffer append");
 * StringBuffer s = new StringBuffer();
 * for (int i=0; i < 10000; i++) {
 *     s.append("a");
 * }
 * Profiler.end("StringBuffer append");
 * Profiler.print();
 *
 * </pre>
 * </blockquote>
 * <p>
 * <b>Example Output</b>
 * <p>
 * <blockquote>
 * <pre>
 *
 * String concatentation:
 * count = 1 total = 960 (ms) average = 960.0 (ms)
 *
 * StringBuffer append:
 * count = 1 total = 79 (ms) average = 79.0 (ms)
 *
 * </pre>
 * </blockquote>
 *
 * @author <b>Mike Clark</b> (mike@clarkware.com)
 * @author Clarkware Consulting, Inc.
 */

public class Profiler {

    //
    // Dictionary of thread-specific event stacks.
    // The key is a registered thread name and the 
    // value is a stack of ThreadTrace objects.
    //
    private static Hashtable threads;

    //
    // Dictionary of global event profiles.
    // The key is an event name and the value
    // is an EventProfile object.
    //
    private static Hashtable events;

    private static boolean isEnabled;
    private static boolean isMemoryEnabled;

    static {
        threads = new Hashtable(50);
        events = new Hashtable(50);
        isEnabled = true;
        isMemoryEnabled = false;

        //
        // Place holder objects to decrease initial
        // object creation cost.
        //
        new EventProfile("");
        new ThreadTrace("");
    }

    /**
     * Designates the beginning of an event to measure.
     *
     * @param event Event description.
     */
    public static final void begin(String event) {

        if (!isEnabled) {
            return;
        }

        ThreadTrace trace = new ThreadTrace(event);
        synchronized (getThreadStack()) {
            getThreadStack().push(trace);
        }

        if (isMemoryEnabled) {
            trace.setStartMemory(Runtime.getRuntime().freeMemory());
        }

        trace.setStartTime(System.currentTimeMillis());
    }

    /**
     * Designates the ending of an event being measured.
     *
     * @param event Event description.
     * @return Event duration (in milliseconds).
     */
    public static final long end(String event) {

        if (!isEnabled) {
            return 0;
        }

        long elapsedTime = 0;
        long endTime = System.currentTimeMillis();

        long endMemory = 0;
        if (isMemoryEnabled) {
            endMemory = Runtime.getRuntime().freeMemory();
        }

        Stack threadStack = getThreadStack();

        synchronized (threadStack) {

            ThreadTrace nextTrace = (ThreadTrace) threadStack.peek();
            if (!nextTrace.getEvent().equals(event)) {
                System.err.println(
                    "Begin/end mismatch occurred at stack trace:");
                printStackTrace(new PrintWriter(System.err, true));
                System.err.println("Profiler has been disabled.");
                enable(false);
                return 0;
            }

            ThreadTrace trace = (ThreadTrace) threadStack.pop();
            EventProfile eventProfile = getEventProfile(trace.getEvent());

            elapsedTime = endTime - trace.getStartTime();
            eventProfile.update(elapsedTime);

            if (isMemoryEnabled) {
                long memory = trace.getStartMemory() - endMemory;
                eventProfile.updateMemory(memory);
            }
        }

        return elapsedTime;
    }

    /**
     * Resets all the event measurements.
     */
    public static final void reset() {

        if (!isEnabled) {
            return;
        }

        Enumeration eventNames = events.keys();
        while (eventNames.hasMoreElements()) {
            String eventName = (String) eventNames.nextElement();
            EventProfile eventProfile = (EventProfile) events.get(eventName);
            eventProfile.reset();
        }
    }

    /**
     * Clears all the events.
     */
    public static final void clear() {

        if (!isEnabled) {
            return;
        }

        events.clear();
    }

    /**
     * Indicates whether the profiler is enabled.
     *
     * @param isEnabled <code>true</code> to enable the profiler;
     *        <code>false</code> otherwise.
     */
    public static final void enable(boolean enabled) {
        isEnabled = enabled;
    }

    /**
     * Determines whether the profiler is enabled.
     *
     * @return <code>true</code> if the profiler is enabled;
     *         <code>false</code> otherwise.
     */
    public static final boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Indicates whether memory profiling is enabled.
     *
     * @param isEnabled <code>true</code> to enable memory profiling;
     *        <code>false</code> otherwise.
     */
    public static final void enableMemory(boolean isEnabled) {
        isMemoryEnabled = isEnabled;
    }

    /**
     * Determines whether memory profiling is enabled.
     *
     * @return <code>true</code> if memory profiling is enabled;
     *         <code>false</code> otherwise.
     */
    public static final boolean isMemoryEnabled() {
        return isMemoryEnabled;
    }

    /**
     * Prints the current thread's event stack to the
     * specified writer.
     *
     * @param writer Writer.
     */
    public static void printStackTrace(PrintWriter writer) {
        if (!isEnabled) {
            return;
        }

        Enumeration traces = getThreadStack().elements();
        while (traces.hasMoreElements()) {
            ThreadTrace trace = (ThreadTrace) traces.nextElement();
            writer.println(trace.getEvent());
        }

        writer.flush();
    }

    /**
     * Prints the collected event measurements to {@link System.out}.
     * 
     * This convenience implementation delegates printing to
     * {@link #print(PrintWriter)}.
     */
    public static final void print() {
        print(new PrintWriter(System.out));
    }
    
    /**
     * Prints the collected event measurements to the
     * specified Outputstream.
     * 
     * This convenience implementation delegates printing to
     * {@link #print(PrintWriter)}.
     *
     * @param out OutputStream to print to.
     */
    public static final void print(OutputStream out) {
        print(new PrintWriter(out));
    }

    /**
     * Prints the collected event measurements to the 
     * specified writer.
     *
     * @param writer Writer.
     */
    public static void print(PrintWriter writer) {
        
        if (!isEnabled) {
            return;
        }

        Iterator profiles = events.values().iterator();
        while (profiles.hasNext()) {
            EventProfile eventProfile = (EventProfile) profiles.next();
            StringBuffer msg = new StringBuffer();
            msg.append(eventProfile.getEvent() + ":\n");
            msg.append("  count = " + eventProfile.getCount());
            msg.append("  total = " + eventProfile.getTotalTime() + " (ms)");
            msg.append(
                "  average = " + eventProfile.getAverageTime() + " (ms)");

            if (isMemoryEnabled) {
                msg.append("\n");
                msg.append(
                    "  memory consumed = "
                        + eventProfile.getTotalConsumedMemory()
                        + " (bytes)");
                msg.append(
                    "  memory collected = "
                        + eventProfile.getTotalCollectedMemory()
                        + " (bytes)");
            }

            msg.append("\n");
            writer.println(msg.toString());
        }

        writer.flush();
    }

    /**
     * Prints the collected event measurements to the 
     * specified writer and resets all event measurements.
     *
     * @param writer Writer.
     */
    public static final void printAndReset(PrintWriter writer) {
        print(writer);
        reset();
    }

    /**
     * Prints the collected event measurements to the 
     * specified writer and clears all events.
     *
     * @param writer Writer.
     */
    public static final void printAndClear(PrintWriter writer) {
        print(writer);
        clear();
    }

    /**
     * Returns the collection of events.
     *
     * @return Events.
     */
    protected static Hashtable getEvents() {
        return events;
    }

    /**
     * Returns the thread-specific event stack for the current thread.
     * <p>
     * If an event stack does not exist for the curren thread,
     * then a new event stack is created and registered.
     *
     * @return Thread event stack.
     */
    protected static final Stack getThreadStack() {

        String threadId = Thread.currentThread().getName();

        //
        // If the thread name is not unique, use the following:
        //
        // String threadId = 
        //	Long.toString(Thread.currentThread().hashCode());
        //

        Stack stack = (Stack) threads.get(threadId);
        if (stack == null) {
            stack = new Stack();
            threads.put(threadId, stack);
        }

        return stack;
    }

    /**
     * Returns the event profile for the specified event.
     * <p>
     * If an event profile does not exist for the specified 
     * event description, then a new event profile is created 
     * and registered.
     *
     * @param event Event description.
     * @return Event profile.
     */
    protected static final EventProfile getEventProfile(String event) {

        EventProfile profile = (EventProfile) events.get(event);
        if (profile == null) {
            profile = new EventProfile(event);
            events.put(event, profile);
        }

        return profile;
    }

    /**
     * The <code>ThreadTrace</code> class contains
     * thread-specific trace information.
     */
    public static class ThreadTrace {

        private String _event;
        private long _startTime;
        private long _startMemory;

        /**
         * Constructs a <code>ThreadTrace</code> with
         * the specified event.
         *
         * @param event Event.
         */
        public ThreadTrace(String event) {
            _event = event;
        }

        /**
         * Returns the event.
         *
         * @return Event.
         */
        public String getEvent() {
            return _event;
        }

        /**
         * Returns the time the event was started.
         *
         * @return Event start time (in milliseconds).
         */
        public long getStartTime() {
            return _startTime;
        }

        /**
         * Sets the time the event was started.
         *
         * @param time Event start time (in milliseconds).
         */
        public void setStartTime(long time) {
            _startTime = time;
        }

        /**
         * Returns the memory available when the event was started.
         *
         * @return Event start memory (in bytes).
         */
        public long getStartMemory() {
            return _startMemory;
        }

        /**
         * Sets the memory available when the event was started.
         *
         * @param memory Event start memory (in bytes).
         */
        public void setStartMemory(long memory) {
            _startMemory = memory;
        }
    }

    /**
     * The <code>EventProfile</code> class contains
     * event-specific performance measurements.
     */
    public static class EventProfile {

        private String _event;
        private int _count;
        private long _totalTime;
        private long _totalConsumedMemory;
        private long _totalCollectedMemory;

        /**
         * Constructs an <code>EventProfile</code> with
         * the specified event.
         *
         * @param event Event.
         */
        public EventProfile(String event) {
            _event = event;
            reset();
        }

        /**
         * Resets the event measurements.
         */
        public void reset() {
            _count = 0;
            _totalTime = 0;
            _totalConsumedMemory = 0;
            _totalCollectedMemory = 0;
        }

        /**
         * Updates the event with the specified 
         * duration (in milliseconds).
         *
         * @param duration Event duration (in milliseconds).
         */
        public void update(long duration) {
            _count++;
            _totalTime = _totalTime + duration;
        }

        /**
         * Updates the event with the specified memory.
         *
         * @param memory Memory (in bytes).
         */
        public void updateMemory(long memory) {
            if (memory < 0) {
                _totalCollectedMemory = _totalCollectedMemory + (-memory);
            }
            _totalConsumedMemory = _totalConsumedMemory + memory;
        }

        /**
         * Returns the event.
         *
         * @return Event.
         */
        public String getEvent() {
            return _event;
        }

        /**
         * Returns the total execution time of the event.
         *
         * @return Total execution time (in milliseconds).
         */
        public long getTotalTime() {
            return _totalTime;
        }

        /**
         * Returns the total memory consumed by the event.
         *
         * @return Total memory (in bytes).
         */
        public long getTotalConsumedMemory() {
            return _totalConsumedMemory;
        }

        /**
         * Returns the total memory reclaimed by the event.
         *
         * @return Total reclaimed memory (in bytes).
         */
        public long getTotalCollectedMemory() {
            return _totalCollectedMemory;
        }

        /**
         * Returns the total number of event executions.
         *
         * @return Total event executions.
         */
        public int getCount() {
            return _count;
        }

        /**
         * Returns the average execution time of the event.
         *
         * @return Average execution time (in milliseconds).
         */
        public double getAverageTime() {
            double time = 0.0;
            if (_count > 0) {
                time = (((double) _totalTime) / _count);
            }

            return time;
        }
    }

    public static void main(String args[]) {

        //Profiler.enableMemory(true);

        Profiler.begin("Test");

        for (int i = 0; i < 2; i++) {

            Profiler.begin("String concatentation");

            String s = "";
            for (int j = 0; j < 10000; j++) {
                s = s + "a";
            }

            Profiler.end("String concatentation");

            Profiler.begin("StringBuffer append");

            StringBuffer sb = new StringBuffer();
            for (int k = 0; k < 100000; k++) {
                sb.append("a");
            }

            Profiler.end("StringBuffer append");

        }

        Profiler.end("Test");

        Profiler.print(new PrintWriter(System.out));
    }
}
