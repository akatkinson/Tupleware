/**
 *  LaplaceBoundary.java
 */

package apps.laplace;

public class LaplaceBoundary implements java.io.Serializable {
    double[] boundary;
    long guid, destGuid;
    String side;


    public LaplaceBoundary(double[] boundary, long guid, long destGuid, String side) {
        this.boundary = boundary;
        this.guid = guid;
        this.destGuid = destGuid;
        this.side = side;
    }
}
