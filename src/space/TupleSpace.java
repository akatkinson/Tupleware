/**
 * Created on Aug 6, 2004
 */

package space;

import java.io.IOException;
import java.util.Vector;

public interface TupleSpace extends Comparable {
    public void out(Tuple t) throws IOException;
    public Tuple in(TupleTemplate t) throws IOException;
    public Tuple rd(TupleTemplate t) throws IOException;
    public Tuple inp(TupleTemplate t) throws IOException;
    public Tuple rdp(TupleTemplate t) throws IOException;
    public Vector<Tuple> inAll(TupleTemplate t, int expected) throws IOException;
    public Vector<Tuple> rdAll(TupleTemplate t, int expected) throws IOException;
    public int compareTo(Object o); // this is such a hack putting this here
}
