/**
 *  Scope.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *  Created:  2/8/2005
 *  Modified: 30/11/2006
 */

package scope;

import java.net.SocketException;
import java.util.*;
import runtime.TupleSpaceRuntime;

public class Scope implements java.io.Serializable {
    private String originatorIPAddress;

    public  static final String EMPTY_SCOPE = " ";
    private static final String NAME_SEPERATOR = ":";

    private Vector<Vector<String>> names;

    public Scope(String name) {
        names = new Vector<Vector<String>>();
        Vector<String> v = new Vector<String>();
        v.addElement(name);
        names.addElement(v);
        this.removeDuplicates();
        setOriginatorIP();
    }

    public Scope(Vector<Vector<String>> names) {
        this.names = names;
        this.removeDuplicates();
        setOriginatorIP();
    }

    public Scope() {
        names = new Vector<Vector<String>>();
        Vector<String> v = new Vector<String>();
        v.addElement(EMPTY_SCOPE);
        names.addElement(v);
        this.removeDuplicates();
        setOriginatorIP();
    }

    /* Copy constructor */
    public Scope(Scope scope) {
        names = new Vector<Vector<String>>();
        Vector<Vector<String>> copyFrom = scope.getNames();

        for(Vector<String> elem : copyFrom) {
            Vector<String> v = new Vector<String>();

            for(String s : elem) {
                v.addElement(new String(s));
            }

            names.addElement(v);
        }

        this.removeDuplicates();
        setOriginatorIP();
    }

    public void setOriginatorIP() {
        try {
            originatorIPAddress = TupleSpaceRuntime.getLocalIPAddress();
        } catch (SocketException e) {
            originatorIPAddress = null;
        }
    }

    public String getOriginatorIP() {
        return originatorIPAddress;
    }

    public boolean equals(Object obj) {
        if(obj == null)
            return false;

        if(!(obj instanceof Scope))
            return false;

        if(this.names.size() != ((Scope) obj).names.size())
            return false;

        Vector<String> a, b;
        for(int i = 0; i < names.size(); i++) {
            a = names.elementAt(i);
            b = ((Scope) obj).getNames().elementAt(i);

            if(!a.equals(b))
                return false;
        }

        return true;
    }

    public Iterator<Vector<String>> iterator() {
        return names.iterator();
    }

    public Vector<Vector<String>> getNames() {
        return names;
    }

    public boolean matches(Scope s) {
        Vector<Vector<String>> v = s.getNames();

        for(Vector<String> elem0 : names) {
            for(Vector<String> elem1 : v) {
                if(namesMatch(elem0, elem1))
                    return true;
            }
        }

        return false;
    }

    private static boolean namesMatch(Vector<String> a, Vector<String> b) {
        for(String s : a) {
            if(!b.contains(s))
                return false;
        }

        return true;
    }

    /* Removes any duplicate names that may be in a scope */
    private void removeDuplicates() {
        for(int i = 0; i < names.size()-1; i++) {
            for(int j = i+1; j < names.size(); j++) {
                if(namesMatch(names.elementAt(i), names.elementAt(j)))
                    names.removeElement(i);
            }
        }
    }

    /*
     *  deepen(<"a", "b">) => <<"a">, <"b">>
     */
    private static Vector<Vector<String>> deepen(Vector<String> orig) {
        Vector<Vector<String>> names = new Vector<Vector<String>>();

        for(String s : orig) {
            Vector<String> v = new Vector<String>();
            v.addElement(s);
            names.addElement(v);
        }

        return names;
    }

    /***** UNFINISHED *****/
    public String toString() {
        /*StringBuffer buf = new StringBuffer();

        buf.append("{");

        for(Vector<String> v : names) {
            buf.append("{");

            for(String s : v) {
                buf.append(s + ",");
            }

            buf.append("}");
        }

        buf.append("}");

        return new String(buf);*/
        return new String("Scope.toString() not yet implemented.");
    }

    /*******************
    *                  *
    *  Static methods  *
    *                  *
    *******************/

    /* This *should* generate a scope with a unique name */
    public static Scope makeScope(String name, Object owner) {
        if(owner == null)
            owner = new Object();

        //return (name + NAME_SEPERATOR + owner.toString() + String.valueOf(System.nanoTime()));
        return new Scope(name + NAME_SEPERATOR + owner.toString());
    }

    public static boolean matches(Scope s1, Scope s2) {
        return s1.matches(s2);
    }

    /*
     *  union(A, B) = A U B
     *
     *  Eg.  union([a], [b])       => [a, b]
     *       union([a, b], [b, c]) => [a, b, c]
     *       union([ab], [bc])     => [ab, bc]
     */
    public static Scope union(Scope s1, Scope s2) {
        if(s1 == null)
            return (s2 == null)? null: s2;
        if(s2 == null)
            return (s1 == null)? null: s1;

        Vector<Vector<String>> names = new Vector<Vector<String>>();
        names.addAll(s1.getNames());
        names.addAll(s2.getNames());

        return new Scope(names);
    }

    /*
     *  complement(A, B) = A - B
     *
     *  Eg.  complement([a, b], [b, c]) => [a]
     *       complement([ab], [bc])     => [ab]
     */
    public static Scope complement(Scope s1, Scope s2) {
        if(s2 == null)
            return s1;

        Vector<Vector<String>> names0 = new Vector<Vector<String>>(s1.getNames());
        Vector<Vector<String>> names1 = s2.getNames();

        for(Vector<String> s : names0) {
            for(Vector<String> t : names1) {
                if(namesMatch(s, t))
                    names0.removeElement(s);

            }
        }

        return new Scope(names0);
    }

    /*
     *  deepUnion(A, B) = { x U y | (x, y) <- A x B }
     *
     *  Eg.  deepUnion([a], [b])       => [ab]
     *       deepUnion([a, b], [b, c]) => [ab, ac, b, bc]
     *       deepUnion([ab], [bc])     => [abc]
     */
    public static Scope deepUnion(Scope s1, Scope s2) {
        if((s1 == null)||(s2 == null))
            return null;

        Vector<Vector<String>> names = new Vector<Vector<String>>();

        for(Vector<String> s : s1.getNames()) {
            for(Vector<String> t : s2.getNames()) {
                names.addAll(union(new Scope(deepen(s)), new Scope(deepen(t))).getNames());
            }
        }

        return new Scope(names);
    }

    /*
     *  deepComplement(A, B) = { x - y | (x, y) <- A x B }
     *
     *  Eg.  deepComplement([ab, ac], [a])    => [b, c]
     *       deepComplement([ab, ac], [b, c]) => [a, ab, ac]
     */
    public static Scope deepComplement(Scope s1, Scope s2) {
        if(s2 == null)
            return s1;

        Vector<Vector<String>> names = new Vector<Vector<String>>();

        for(Vector<String> s : s1.getNames()) {
            for(Vector<String> t : s2.getNames()) {
                names.addAll(complement(new Scope(deepen(s)), new Scope(deepen(t))).getNames());
            }
        }

        return new Scope(names);
    }

    /*
     *  intersection() = ...
     *
     *  Eg.  intersection([a, b], [b, c])  => [b]
     */
    public static Scope intersection(Scope s1, Scope s2) {
        if((s1==null)||(s2==null))
            return new Scope(); //return empty scope

        Vector<Vector<String>> names = new Vector<Vector<String>>();

        for(Vector<String> s : s1.getNames()) {
            for(Vector<String> t : s2.getNames()) {
                for(String u : t) {
                    if(s.contains(u)) {
                        Vector<String> v = new Vector<String>();
                        v.addElement(u);
                        names.addElement(v);
                    }
                }
            }
        }

        Scope s = new Scope(names);
        s.removeDuplicates();
        return s;
    }
}
