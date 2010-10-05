/**
 *  Neighbourhood.java
 *
 *  Author:   Alistair Atkinson (alatkins@utas.edu.au)
 *  Created:  10/9/2006
 *  Modified: 11/9/2007
 *
 *  Description: Maps a neighbouring nodes location relative to the local node
 *               to a scope.
 */

package runtime;

import scope.*;
import space.*;
import java.util.*;
import java.net.*;

public class Neighbourhood {
    public static enum NEIGHBOUR_LOC { NORTH_WEST, NORTH, NORTH_EAST,
                                       SOUTH_WEST, SOUTH, SOUTH_EAST,
                                       WEST, EAST, DEFAULT          };

    private Hashtable<NEIGHBOUR_LOC, Scope> associations;
    private Scope localScope;

    public Neighbourhood(Scope localScope) {
        associations = new Hashtable<NEIGHBOUR_LOC, Scope>();
        this.localScope = localScope;
        associate(NEIGHBOUR_LOC.NORTH,       TupleSpaceRuntime.DEFAULT_SCOPE);
        associate(NEIGHBOUR_LOC.NORTH_WEST,  TupleSpaceRuntime.DEFAULT_SCOPE);
        associate(NEIGHBOUR_LOC.NORTH_EAST,  TupleSpaceRuntime.DEFAULT_SCOPE);
        associate(NEIGHBOUR_LOC.SOUTH,       TupleSpaceRuntime.DEFAULT_SCOPE);
        associate(NEIGHBOUR_LOC.SOUTH_WEST,  TupleSpaceRuntime.DEFAULT_SCOPE);
        associate(NEIGHBOUR_LOC.SOUTH_EAST,  TupleSpaceRuntime.DEFAULT_SCOPE);
        associate(NEIGHBOUR_LOC.EAST,        TupleSpaceRuntime.DEFAULT_SCOPE);
        associate(NEIGHBOUR_LOC.WEST,        TupleSpaceRuntime.DEFAULT_SCOPE);
        associate(NEIGHBOUR_LOC.DEFAULT,     TupleSpaceRuntime.DEFAULT_SCOPE);
    }

    public Scope associate(NEIGHBOUR_LOC loc, Scope scope) {
        return associations.put(loc, scope);
    }

    public Scope getNeighbour(NEIGHBOUR_LOC loc) {
        Scope s = associations.get(loc);

        if(s == null)
            return TupleSpaceRuntime.DEFAULT_SCOPE;

        return s;
    }

    public Scope getBoundaryScope(NEIGHBOUR_LOC loc) {
        return Scope.union(localScope, getNeighbour(loc));
    }

    public Scope getLocalScope() {
        return localScope;
    }

    public void setLocalScope(Scope s) {
        localScope = s;
    }

    public boolean isAssociated(NEIGHBOUR_LOC loc, Scope name) {
        Scope s = associations.get(loc);

        return s.equals(name);
    }
}
