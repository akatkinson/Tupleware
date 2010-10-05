/**
 * TupleSpaceImpl.java
 *
 * Author:   Alistair Atkinson (alatkins@utas.edu.au)
 * Created:  9/8/2004
 * Modified: 12/4/2007
 *
 * NOTE: Latest changes to this class added a Hashtable for tuple storage,
 *       replacing the Vector which was previously used. These changes make
 *       this class suitable only for the OceanModelMaster application. The previous
 *       version of this class is saved as TupleSpaceImpl.java.BAK.
 */

package space;

import java.util.*;

public class TupleSpaceImpl implements TupleSpace {
    private Hashtable<String, Vector<Tuple>> tuples;

    public TupleSpaceImpl() {
        tuples = new Hashtable<String, Vector<Tuple>>();
    }

    public void out(Tuple t) {
        Vector<Tuple> vals = tuples.get(generateKey(t));
        if(vals == null) {
            vals = new Vector<Tuple>();
            tuples.put(generateKey(t), vals);
        }

        synchronized(vals) {
            vals.add(0, t);
            vals.notifyAll();
        }

        synchronized(tuples) {
            tuples.notifyAll();
        }
    }

    public void outAll(Collection<Tuple> tpls) {
        for(Tuple t : tpls) {
            Vector<Tuple> vals = tuples.get(generateKey(t));
            if(vals == null) {
                vals = new Vector<Tuple>();
                tuples.put(generateKey(t), vals);
            }

            synchronized(vals) {
                vals.add(0, t);
                vals.notifyAll();
            }
        }

        synchronized(tuples) {
            tuples.notifyAll();
        }
    }

    public Tuple in(TupleTemplate t) {
        return findTuple(t, true, true);
    }

    public Tuple inp(TupleTemplate t) {
        return findTuple(t, true, false);
    }

    public Vector<Tuple> inAll(TupleTemplate t, int expected) {
        return findAllTuples(t, true, expected);
    }

    public Tuple rd(TupleTemplate t) {
        return findTuple(t, false, true);
    }

    public Tuple rdp(TupleTemplate t) {
        return findTuple(t, false, false);
    }

    public Vector<Tuple> rdAll(TupleTemplate t, int expected) {
        return findAllTuples(t, false, expected);
    }

    /*
     * Returns first tuple found which matches given template. Otherwise,
     * returns null.
     */
    private Tuple findTuple(TupleTemplate t, boolean remove, boolean block) {
        //System.out.println("Handling request for template "+t.toString());

        Vector<Tuple> vals = tuples.get(generateKey(t));

        while(vals == null) {
            if(block) {
                synchronized(tuples) {
                    try {
                        tuples.wait();
                    } catch(InterruptedException e) {
                        return null;
                    }
                }
            } else {
                return null;
            }

            vals = tuples.get(generateKey(t));
        }

        synchronized(vals) {
            Tuple result = null;

            for(;;) {
                for(Tuple i: vals) {
                    if(t.matches(i)) {
                        result = i;

                        if(remove)
                            vals.remove(result);

                        return result;
                    }
                }

                if(block) {
                    try { vals.wait(1000); }
                    catch(InterruptedException e) {
                        return null;
                    }
                } else {
                    return result;
                }
            }
        }
    }

    private Vector<Tuple> findAllTuples(TupleTemplate t,
                                        boolean remove,
                                        int expected)
    {
        Vector<Tuple> allTuples = new Vector<Tuple>();
        Vector<Tuple> matchingTuples = new Vector<Tuple>();

        synchronized(tuples) {
            //for(;;) {
                Collection<Vector<Tuple>> c = tuples.values();

                for(Vector<Tuple> v : c) {
                    if(v!=null)
                        allTuples.addAll(v);
                }

                /*System.out.println("Template: " + t.toString());
                for(Tuple tpl : allTuples)
                    System.out.println(tpl.toString());*/

                for(Tuple tuple : allTuples) {
                    if(t.matches(tuple)) {
                        matchingTuples.addElement(tuple);

                        if(remove) {
                            tuples.remove(tuple);
                        }
                    }

                    if(matchingTuples.size() >= expected) {
                        tuples.notifyAll();
                        /*System.out.println("TupleSpaceImpl - returned vals: "
                                            + matchingTuples.size()
                                            + " of " + expected);*/
                        return matchingTuples;
                    }
                }

                /*if(matchingTuples.size() < expected) {
                    if(block) {
                        try {
                            tuples.wait(1000);
                        } catch(InterruptedException ie) {
                            return matchingTuples;
                        }
                    } else {
                        return matchingTuples;
                    }
                }
            }*/
            /*System.out.println("TupleSpaceImpl - returned vals: "
                               + matchingTuples.size()
                               + " of " + expected);*/
            return matchingTuples; // remove if above commented code being used
        }
    }

    private String generateKey(Tuple t) {
        System.out.println(t.field(0));

        /* SPECIAL CASES */
        if(t.field(0).equals("task"))
            return new String("task");

        if(t.field(0).equals("node"))
            return new String("node");

        if(t.field(0).equals("neighbourhood"))
            return new String("neighbourhood");

        if(t.field(0).equals("panel"))
            return new String("panel");

        if(t.field(0).equals("panel_"))
            return new String("panel_");

        if(t.field(0).equals("intermediate"))
            return new String("intermediate");

        if(t.field(0).equals("eta_inter"))
            return new String("eta_inter");

        if(t.field(0).equals("u_inter"))
            return new String("u_inter");

        if(t.field(0).equals("qtask"))
            return new String("qtask");

        if(t.field(0).equals("qresult"))
            return new String("qresult");

        if(t.field(0).equals("qsort"))
            return new String("qsort");

        if(t.field(0).equals("laplace"))
            return new String("laplace");

        if((t.field(0) instanceof String) && (((String)t.field(0)).contains("mb")))
            return new String("mandelbrot");

        StringBuffer buf = new StringBuffer();
        int it = (t.size() > 2)? 3: 1;

        for(int i = 0; i < it; i++) {
            buf.append(t.field(i).toString());
        }

        return new String(buf);
    }

    public int compareTo(Object o) { return 0; }
}
