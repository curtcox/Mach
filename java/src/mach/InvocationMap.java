package mach;

import java.util.ArrayList;
import java.util.List;

/**
 * A special map, since ordinary maps don't support multiple equivalent keys.
 */
final class InvocationMap {

    final List<Object>results = new ArrayList<Object>();
    final List<Invocation>invocations = new ArrayList<Invocation>();

    public void map(Invocation invocation, Object result) {
        invocations.add(invocation);
        results.add(result);
    }

    public boolean contains(Invocation invocation) {
        return invocations.contains(invocation);
    }

    public Iterable<Invocation> invocations() {
        return invocations;
    }

    public Object getResult(Invocation invocation) {
        for (int i=results.size()-1; i>=0; i--) {
            if (invocations.get(i).equals(invocation)) {
                return results.get(i);
            }
        }
        return null;
    }
}
