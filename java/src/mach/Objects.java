package mach;

import java.util.List;

/**
 * Useful methods for ObjectS.
 * @author Curt
 */
final class Objects {

    private static boolean areEqualArrays(Object[] a, Object[] b) {
        if (a.length!=b.length) {
            return false;
        }
        for (int i=0; i<a.length; i++) {
            if (!areEqual(a[i],b[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean areEqualLists(List a, List b) {
        if (a.size()!=b.size()) {
            return false;
        }
        for (int i=0; i<a.size(); i++) {
            if (!areEqual(a.get(i),b.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return equal if the given objects are equal.
     * Special care is taken to avoid asking an object to compare itself to null.
     */
    public static boolean areEqual(Object a, Object b) {
        if (a==null && b==null) {
            return true;
        }
    
        if ((a==null) != (b==null)) {
            return false;
        }
        
        if (a instanceof Object[] && b instanceof Object[]) {
            return areEqualArrays((Object[])a,(Object[])b);
        }

        if (a instanceof List && b instanceof List) {
            return areEqualLists((List)a,(List)b);
        }

        return a.equals(b);
    }

}
