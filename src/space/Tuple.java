/**
 * Tuple.java
 *
 * Author:   Alistair Atkinson (alatkins@utas.edu.au)
 * Created:
 * Modified: 11/10/2005
 *
 * Description:
 */

package space;

import java.io.Serializable;
import scope.*;

public class Tuple implements Serializable {
    protected Object[] fields;
    protected Scope scope;

    public Tuple(Object ... fields) {
    	this.fields = (Object[]) fields;
    	this.scope = new Scope(Scope.EMPTY_SCOPE);
    }

    public Tuple(Scope scope, Object ... fields) {
        this.fields = (Object[]) fields;  // Eclipse likes this better
        //this.fields = fields;  // Java 5 acceptable version
        this.scope = scope;
    }

    public int size() {
        return fields.length;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }
    
    public boolean scoped() {
    	return (scope == null);
    }

    public Object field(int i) {
        return (i < size() && i >=0)? fields[i]: null;
    }

    /* Overrides Object.toString() */
    public String toString() {
        StringBuffer s = new StringBuffer();

        s.append("<");
        for(int i = 0; i < fields.length; i++) {
            s.append(fields[i].toString() + ", ");
        }

        s.replace(s.length()-2, s.length(), ">");

        return s.toString();
    }
}
