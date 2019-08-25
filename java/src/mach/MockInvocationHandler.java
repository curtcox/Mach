package mach;

import org.junit.ComparisonFailure;

import java.lang.reflect.*;
import java.util.*;

import static org.junit.Assert.*;
import static mach.Phase.*;

/**
 * Handles the method invocation for a single mock.
 * What that means depends entirely on the current phase.
 */
final class MockInvocationHandler
    implements InvocationHandler
{
    Invocation latest;
    final String name;
    final Class clazz;
    final InvocationMap returns = new InvocationMap();
    final Set<Invocation> nos = new HashSet();
    final InvocationMap invoked = new InvocationMap();
    final MockFactory factory;

    <T> MockInvocationHandler(MockFactory factory, Class clazz, String name) {
        this.factory = factory;
        this.clazz = clazz;
        this.name = name;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (toString(method)) { return toString(); }
        if (equals(method))   { return equals(proxy, args); }
        if (hashCode(method)) { return hashCode(); }

        Invocation invocation = new Invocation(proxy,method,args,factory.wildcards);
        factory.handler = this;

        if (current == Phase.no)      { return no(invocation);      }
        if (current == Phase.returns) { return returns(invocation); }
        if (current == Phase.invoke)  { return invoke(invocation);  }
        if (current == Phase.verify)  { return verify(invocation);  }

        throw new UnsupportedOperationException("Invalid phase : " + current);
    }

    private Object no(Invocation invocation) throws Throwable {
        nos.add(invocation);
        current = invoke;
        return dummy(invocation);
    }

    private Object dummy(Invocation invocation) {
        if (invocation.method.getReturnType().equals(boolean.class)) {
            return false;
        }
        return null;
    }

    private Object returns(Invocation invocation) throws Throwable {
        returns.map(invocation, factory.result);
        return useFactoryValue();
    }

    private Object useFactoryValue() {
        Object value = factory.result;
        factory.result = null;
        current = null;
        factory.wildcards = null;
        return value;
    }

    private Object verify(Invocation expected) throws Throwable {
        if (!invoked.contains(expected)) {
            for (Invocation received : invoked.invocations()) {
                if (received.method.equals(expected.method)) {
                    assertEquals(expected, received);
                }
            }
            fail("Missing invocation " + expected);
        }
        return invoked.getResult(expected);
    }

    private Object invoke(Invocation invocation) throws Throwable {
        if (nos.contains(invocation)) {
            String message = "Unwanted invocation " + invocation;
            fail(message);
        }
        Object result = result(invocation);
        invoked.map(invocation, result);
        latest = invocation;
        return result;
    }

    private Object result(Invocation invocation) throws Throwable {
        if (invocation.returnsVoid()) {
            return null;
        }
        if (returns.contains(invocation)) {
            return returns.getResult(invocation);
        }
        if (thereAreMatchesForMethod(invocation)) {
            throw comparisonFailure(invocation);
        }
        throw undefined(invocation);
    }

    private ComparisonFailure comparisonFailure(Invocation invocation) throws Throwable {
        if (matchingInvocationsForMethod(invocation).size()==1) {
            Invocation expected = expectedForMethod(invocation.method);
            String message = "Actual invocation is not the one expected";
            return new ComparisonFailure(message, expected.toString(), invocation.toString());
        } else {
            String expected = matchingInvocationsForMethod(invocation).toString();
            String message = "Actual invocation is not one of the expected invocations";
            return new ComparisonFailure(message, expected, invocation.toString());
        }
    }

    private UnsupportedOperationException undefined(Invocation invocation) throws Throwable {
        String message = String.format("[%s] is not defined for [%s]",invocation.method,this);
        return new UnsupportedOperationException(message);
    }

    private Invocation expectedForMethod(Method method) {
        for (Invocation i : returns.invocations()) {
            if (i.method == method) {
                return i;
            }
        }
        return null;
    }

    private boolean thereAreMatchesForMethod(Invocation invocation) {
        for (Invocation i : returns.invocations()) {
            if (i.method == invocation.method) {
                return true;
            }
        }
        return false;
    }

    private List<Invocation> matchingInvocationsForMethod(Invocation invocation) {
        List<Invocation> matching = new ArrayList<Invocation>();
        for (Invocation i : returns.invocations()) {
            if (i.method == invocation.method) {
                matching.add(i);
            }
        }
        return matching;
    }

    private static boolean equals(Method method) {
        return method.getName().equals("equals");
    }

    private static boolean toString(Method method) {
        return method.getName().equals("toString");
    }

    private static boolean hashCode(Method method) {
        return method.getName().equals("hashCode");
    }

    @Override
    public String toString() {
        return name + ":" +clazz + "@" + System.identityHashCode(this);
    }

    private boolean equals(Object proxy, Object[] args) {
        if (args==null || args.length != 1) {
            return false;
        }
        return proxy == args[0];
    }
}
