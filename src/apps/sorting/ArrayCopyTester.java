import java.util.*;

public class ArrayCopyTester {
    public static void main(String[] args) {
        int[] a = { 0,1,2,3,4,5,6,7,8,9 };
        int[] b = Arrays.copyOfRange(a, 0, 5);
        int[] c = Arrays.copyOfRange(a, 5, 10);

        System.out.println("a[]:\n" + Arrays.toString(a));
        System.out.println("b[]:\n" + Arrays.toString(b));
        System.out.println("c[]:\n" + Arrays.toString(c));
    }
}
