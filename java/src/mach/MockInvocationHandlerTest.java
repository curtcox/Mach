package mach;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static mach.Phase.invoke;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static mach.Phase.current;
import static org.junit.Assume.assumeTrue;

public class MockInvocationHandlerTest {

    String name;
    Class clazz;
    MockFactory factory = new MockFactory();
    Object proxy = new Object();
    Object[] args;

    MockInvocationHandler testObject = new MockInvocationHandler(factory,clazz,name);

    @Before
    public void setUp() {
        assumeTrue(ShouldRun.Mach);
    }

    @Test
    public void can_create() {
        new MockInvocationHandler(factory,clazz,name);
    }

    @Test
    public void invoke_throws_exception_when_phase_is_null() throws Throwable {
        Method method = getMethod(Map.class,"size");
        current = null;
        try {
            testObject.invoke(proxy,method,args);
            fail();
        } catch (UnsupportedOperationException e) {
            String message = "Invalid phase : null";
            assertEquals(message,e.getMessage());
        }
    }

    private Method getMethod(Class c, String methodName) {
        for (Method method : c.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new UnsupportedOperationException(methodName);
    }

    @Test
    public void invoke_fails_when_method_invoked_with_wrong_value() throws Throwable {
        Method method = getMethod(Map.class,"get");
        factory.returns("???");
        testObject.invoke(proxy, method, new Object[]{"right"});

        try {
            current = invoke;
            testObject.invoke(proxy,method,new Object[] {"wrong"});
        } catch (AssertionError e) {
            org.junit.ComparisonFailure failure = (org.junit.ComparisonFailure) e;
            Invocation expected = new Invocation(testObject,method, new Object[] {"right"}, null);
            Invocation received = new Invocation(testObject,method, new Object[] {"wrong"}, null);
            assertEquals(received.toString(),failure.getActual());
            assertEquals(expected.toString(),failure.getExpected());
            return;
        }
        fail();
    }

    @Test
    public void invoke_with_no_args_returns_given_value_when_invoked_after_return_value_has_been_set() throws Throwable {
        Method method = getMethod(Map.class,"size");
        int expected = 42;
        factory.returns(expected);
        testObject.invoke(proxy,method,args);
        current = invoke;
        int actual = (Integer) testObject.invoke(proxy,method,args);

        assertEquals(expected, actual);
    }

    @Test
    public void invoke_with_one_arg_returns_given_value_when_invoked_after_return_value_has_been_set() throws Throwable {
        Method method = getMethod(Map.class,"get");
        String expected = "value";
        factory.returns(expected);
        testObject.invoke(proxy,method,args("name"));
        current = invoke;
        String actual = (String) testObject.invoke(proxy,method,args("name"));

        assertEquals(expected,actual);
    }

    @Test
    public void equals_returns_true_when_given_its_proxy() throws Throwable {
        Method method = getMethod(Object.class,"equals");
        assertEquals(Boolean.TRUE, testObject.invoke(proxy, method, new Object[]{proxy}));
    }

    @Test
    public void equals_returns_false_when_not_given_its_proxy() throws Throwable {
        Method method = getMethod(Object.class,"equals");
        assertEquals(Boolean.FALSE, testObject.invoke(proxy, method, new Object[]{null}));
    }

    private static Object[] args(Object... args) {
        return args;
    }

}
