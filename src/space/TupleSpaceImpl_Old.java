/**
 * TupleSpaceImpl.java
 * 
 * Author:	 Alistair Atkinson (alatkins@utas.edu.au)
 * Created:  9/8/2004
 * Modified: 23/5/2005 
 * 
 * Description:
 */

package space;

import java.util.*;

public class TupleSpaceImpl_Old {
    private Vector<Tuple> tuples;
    
    public TupleSpaceImpl_Old() {
        tuples = new Vector<Tuple>();
    }
    
    public void out(Tuple t) {
        synchronized(tuples) {
            //tuples.addElement(t);
            tuples.add(0, t);
            tuples.notifyAll();
        }
    }

    public Tuple in(TupleTemplate t) {
        Tuple result = null;
        
        while(result == null) {
            synchronized(tuples) {
                result = findTuple(t);
                if(result != null) {
                    tuples.remove(result);
                    return result;
                }
                
                try { tuples.wait(); } 
                catch(InterruptedException e) {
                    return null;
                }
            }
        }
        
        return result;
    }

    public Tuple inp(TupleTemplate t) {
        Tuple result = null;
        
        synchronized(tuples) {
            result = findTuple(t);
            if(result != null) {
                tuples.remove(result);
            }
        }
        
        return result;
    }
    
    public Tuple rd(TupleTemplate t) {
        Tuple result = null;
        
        while(result == null) {
            synchronized(tuples) {
                result = findTuple(t);
                
                if(result == null) {
	                try { tuples.wait(); } 
	                catch(InterruptedException e) {
	                    return null;
	                }
                }
            }
            
            if(result != null) {
                return result;
            }
        }
        
        return result;
    }

    public Tuple rdp(TupleTemplate t) {
        Tuple result = null;
        
        synchronized(tuples) {
            result = findTuple(t);
        }
        
        return result;
    }
    
    /* 
     * Returns first tuple found which matches given template. Otherwise, 
     * returns null. 
     */
    private Tuple findTuple(TupleTemplate t) {
        for(Tuple i: tuples) {
            if(t.matches(i))
                return i;
        }
        
        /*for(int i = tuples.size()-1; i >= 0; i--) {
            Tuple tuple = tuples.elementAt(i);
            
            if(t.matches(tuple))
                return tuple;
        }*/
        
        return null;
    }
}
