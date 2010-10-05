/*
 *  TupleTemplate.java
 */

package space;

import scope.*;

public class TupleTemplate extends Tuple {
    private boolean scopeOnly = false;

    public TupleTemplate(Scope scope, Object ... fields) {
        super(scope, (Object[]) fields);
    }

    public TupleTemplate(Scope scope) {
        super(scope, new Object[0]);
        scopeOnly = true;
    }

    public TupleTemplate(Object ... fields) {
        super((Object[]) fields);
    }

    public TupleTemplate(Tuple t) {
        this.fields = new Object[t.fields.length];
        for(int i = 0; i < this.fields.length; i++) {
            this.fields[i] = t.fields[i];
        }
    }

    /**
     *  Implements associative matching rule. Wildcard fields are denoted by
     *  null values. Fields are compared based on their string representation.
     */
    public boolean matches(Tuple tuple) {
        // cannot match if different sizes
        //  (unimportant if we're matching on Scopes only...)
        if((!scopeOnly) && (size() != tuple.size()))
            return false;

        // scope matching rule
        /*if(!this.getScope().matches(tuple.getScope())) {
            return false;
        } else if(scopeOnly) {
            return true;
        }*/

        // test if fields match
        for(int i = 0; i < size(); i++) {
            if((this.field(i) != null)
                && (!this.field(i).toString().equals(tuple.field(i).toString())))
                return false;
        }

        // tuple must match if we get to this point
        return true;
    }

    public String toString() {
        Object[] fieldsCopy = new Object[fields.length];

        for(int i = 0; i < fields.length; i++) {
            if(fields[i] == null)
                fieldsCopy[i] = "null";
            else
                fieldsCopy[i] = fields[i];
        }

        return new Tuple(scope, fieldsCopy).toString();
    }
}
