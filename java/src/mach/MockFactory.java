package mach;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static mach.Phase.current;
/**
 * For making mocks, to use in testing.
 * During normal usage, there is one MockFactory per JUnit test class.
 */
final class MockFactory {

    /**
     * The last specified return.
     * This value will be recorded and cleared by invocation in the "returns" phase.
     */
    Object result;

    /**
     * An array of values that are use to match unknown parameter values.
     * This value will be recorded and cleared by invocation in the "returns" phase.
     */
    Object[] wildcards;

    /**
     * The last invocation handler from this factory that was used for a method
     * invocation.
     */
    MockInvocationHandler handler;

    /**
     * Create the specified mock.
     */
    <T> T mock(Class<T> clazz, String name) {
        ClassLoader loader = MockFactory.class.getClassLoader();
        Class<?>[] interfaces = new Class[] { clazz };
        InvocationHandler handler = new MockInvocationHandler(this,clazz,name);
        return clazz.cast(Proxy.newProxyInstance(loader, interfaces, handler));
    }

    /**
     * Used to specify the value to be returned.
     * The next mock invocation will specify what method invocation returns it.
     */
    void returns(Object value) {
        if (result != null) {
            String message = String.format("Return value [%s] hasn't been mapped, yet.",result);
            throw new IllegalStateException(message);
        }
        result = value;
        current = Phase.returns;
    }

    /**
     * Used to specify any wildcards to be used.
     * The next mock invocation will specify what method invocation uses it.
     */
    void wild(Object... wildcards) {
        if (current==Phase.returns || current==Phase.verify) {
            this.wildcards = wildcards==null ? new Object[1] : wildcards;
        } else {
            String message = "Specify a (possibly void) result before specifying wildcards.";
            throw new IllegalStateException(message);
        }
    }

    public <T> T arg() {
        return (T) handler.latest.args.get(0);
    }
}
