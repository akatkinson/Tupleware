/**
 *  TupleSpaceRequest.java
 */

package space;

import java.io.Serializable;
import java.util.*;

public class TupleSpaceRequest implements Serializable {
    public static enum RequestType { OUT, IN, INP, RD, RDP, INALL, RDALL, OUTALL };

    private RequestType type;
    private Tuple tuple;
    private Vector<Tuple> tpls;
    private Integer expected;

    public TupleSpaceRequest(RequestType type, Tuple tuple) {
        this.type = type;
        this.tuple = tuple;
    }

    public TupleSpaceRequest(RequestType type, Tuple tuple, int expected) {
        this.type = type;
        this.tuple = tuple;
        this.expected = new Integer(expected);
    }

    public TupleSpaceRequest(RequestType type, Vector<Tuple> tpls) {
        this.type = type;
        this.tpls = tpls;
    }

    public Tuple getTuple() { return tuple; }
    public Vector<Tuple> getTuples() { return tpls; }
    public RequestType getRequestType() { return type; }
    public int getExpected() {
        return (expected == null)? 1: (int) expected;
    }
}
