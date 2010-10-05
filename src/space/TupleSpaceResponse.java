/**
 *  TupleSpaceResponse.java
 */

package space;

import java.io.Serializable;
import java.util.Vector;

public class TupleSpaceResponse implements Serializable {
    public static enum Status { SUCCESS, ERROR, TIMED_OUT };
    private Status status;
    private Tuple tuple;
    private Vector<Tuple> tuples;

    public TupleSpaceResponse(Status status) {
        this.status = status;
    }

    public TupleSpaceResponse(Status status, Tuple tuple) {
        this.status = status;
        this.tuple = tuple;
    }

    public TupleSpaceResponse(Status status, Vector<Tuple> tuples) {
        this.status = status;
        this.tuples = tuples;
    }

    public Status getStatus() { return status; }
    public Tuple getTuple() { return tuple; }
    public Vector<Tuple> getTuples() { return tuples; }
}
