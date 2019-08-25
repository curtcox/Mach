package mach;

import org.junit.Before;
import org.junit.ComparisonFailure;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.Assert.*;
import static mach.Mocks.no;
import static mach.Mocks.verify;
import static org.junit.Assume.assumeTrue;

public class MockFactoryTest {

    MockFactory factory;
    interface Sample {
        String methodWithNoArgs();
        String methodWithOneArg(String arg);
        boolean methodThatReturnsBoolean();
    }
    Method methodWithNoArgs;
    Method methodWithOneArg;

    @Before
    public void setUp() {
        assumeTrue(ShouldRun.Mach);
        methodWithNoArgs = method(Sample.class, "methodWithNoArgs");
        methodWithOneArg = method(Sample.class, "methodWithOneArg");
        factory = new MockFactory();
    }

    private static Method method(Class c, String name) {
        for (Method method : c.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        throw new IllegalArgumentException(name);
    }

    @Test
    public void can_create() {
        new MockFactory();
    }

    @Test
    public void mock_returns_instance_of_given_interface() {
        assertTrue(newMockSample() instanceof Sample);
    }

    private Sample newMockSample() {
        return factory.mock(Sample.class, "name");
    }

    @Test
    public void toString_uses_name_class_and_system_hash() {
        Object mock = newMockSample();
        String expected = "name:" + Sample.class.toString() + "@" + System.identityHashCode(Proxy.getInvocationHandler(mock));
        String actual = mock.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void returns_makes_mock_return_value_on_next_invocation() {
        Sample mock = newMockSample();
        String expected = "next";

        factory.returns(expected); mock.methodWithNoArgs();

        String actual = mock.methodWithNoArgs();
        assertSame(expected,actual);
    }

    @Test
    public void returns_makes_mock_return_primitive_on_next_invocation() {
        Sample mock = newMockSample();
        boolean expected = true;

        factory.returns(expected); mock.methodThatReturnsBoolean();

        boolean actual = mock.methodThatReturnsBoolean();
        assertSame(expected,actual);
    }

    @Test
    public void last_one_wins_for_the_same_invocation() {
        Sample mock = newMockSample();
        String expected = "next";

        factory.returns("first"); mock.methodWithNoArgs();
        factory.returns(expected); mock.methodWithNoArgs();

        String actual = mock.methodWithNoArgs();
        assertSame(expected,actual);
    }

    @Test
    public void mock_throws_exception_when_no_defined_return_value() {
        Sample mock = newMockSample();

        try {
            mock.methodWithNoArgs();
            fail();
        } catch (UnsupportedOperationException e) {
            String message = String.format("[%s] is not defined for [%s]",methodWithNoArgs,mock);
            assertEquals(message,e.getMessage());
        }
    }

    @Test
    public void returns_works_with_multiple_whens_on_different_methods() {
        Sample mock = newMockSample();

        factory.returns("no args"); mock.methodWithNoArgs();
        factory.returns("one arg"); mock.methodWithOneArg("1");

        assertEquals("no args",mock.methodWithNoArgs());
        assertEquals("one arg", mock.methodWithOneArg("1"));
    }

    @Test
    public void invoke_makes_mock_return_value_on_next_invocation() {
        Sample mock = newMockSample();
        String expected = "next";
        factory.returns(expected); mock.methodWithNoArgs();

        String actual = mock.methodWithNoArgs();
        assertSame(expected,actual);
    }

    @Test
    public void return_throws_exception_if_already_in_returns_phase() {
        MockFactory testObject = new MockFactory();

        try {
            testObject.returns("first");
            testObject.returns("next");
            fail();
        } catch (IllegalStateException e) {
            assertEquals("Return value [first] hasn't been mapped, yet.",e.getMessage());
        }
    }

    @Test
    public void verify_does_not_fail_when_method_invoked() {
        Sample mock = newMockSample();
        factory.returns(""); mock.methodWithNoArgs();

        mock.methodWithNoArgs();

        verify();
        mock.methodWithNoArgs();
    }

    @Test
    public void method_returns_previous_result_when_invoked_during_verify() {
        Sample mock = newMockSample();

        String arg = "jello";
        String result = "pudding";
        factory.returns(result); mock.methodWithOneArg(arg);

        mock.methodWithOneArg(arg);

        verify();

        assertSame(result, mock.methodWithOneArg(arg));
    }

    @Test
    public void method_returns_specified_when_invocation_matches_wildcard() {
        Sample mock = newMockSample();

        String expected = "expected";
        factory.returns(expected); factory.wild(null); mock.methodWithOneArg(null);

        String actual = mock.methodWithOneArg(random());

        assertSame(expected, actual);
    }

    @Test
    public void wild_null_is_interpreted_as_an_object_array_with_one_null_value() {
        factory.returns("");
        factory.wild(null);
        assertEquals(1, factory.wildcards.length);
        assertNull(factory.wildcards[0]);
    }

    @Test
    public void arg_returns_previous_arg_during_verify_after_invoking_method_with_wildcard() {
        Sample mock = newMockSample();

        String passedArg = "some random string";
        factory.returns("don't care"); factory.wild(null); mock.methodWithOneArg(null);

        mock.methodWithOneArg(passedArg);

        verify();
        factory.wild(null); mock.methodWithOneArg(null); Object capturedArg = factory.arg();

        assertSame(passedArg, capturedArg);
    }

    @Test
    public void arg_returns_previous_arg_immediately_after_being_invoked() {
        Sample mock = newMockSample();

        String passedArg = "some random string";
        factory.returns("don't care"); factory.wild(null); mock.methodWithOneArg(null);

        mock.methodWithOneArg(passedArg);

        Object capturedArg = factory.arg();
        assertSame(passedArg, capturedArg);
    }

    @Test
    public void verify_fails_when_method_not_invoked() {
        Sample mock = newMockSample();

        verify();

        try {
            mock.methodWithNoArgs();
        } catch (AssertionError e) {
            String message = "Missing invocation " + new Invocation(mock,methodWithNoArgs, new Object[0], new Object[0]);
            assertEquals(message,e.getMessage());
            return;
        }
        fail();
    }

    @Test
    public void invoke_fails_when_method_invoked_with_wrong_value() {
        Sample mock = newMockSample();

        factory.returns("???"); mock.methodWithOneArg("right");

        try {
            mock.methodWithOneArg("wrong");
        } catch (AssertionError e) {
            ComparisonFailure failure = (ComparisonFailure) e;
            Invocation expected = new Invocation(mock,methodWithOneArg, new Object[] {"right"}, null);
            Invocation received = new Invocation(mock,methodWithOneArg, new Object[] {"wrong"}, null);
            assertEquals(received.toString(),failure.getActual());
            assertEquals(expected.toString(),failure.getExpected());
            return;
        }
        fail();
    }

    @Test
    public void no_does_not_fail_when_method_not_invoked() {
        Sample mock = newMockSample();

        no();

        mock.methodWithNoArgs();
    }

    @Test
    public void no_OK_when_method_not_invoked() {
        Sample mock = newMockSample();

        no(); mock.methodWithNoArgs();
        no(); mock.methodWithOneArg("");
        no(); mock.methodThatReturnsBoolean();
    }

    @Test
    public void no_fails_when_method_invoked() {
        Sample mock = newMockSample();
        no(); mock.methodWithNoArgs();

        try {
            mock.methodWithNoArgs();
        } catch (AssertionError e) {
            String message = "Unwanted invocation " + new Invocation(mock,methodWithNoArgs, new Object[0], new Object[0]);
            assertEquals(message,e.getMessage());
            return;
        }
        fail();
    }

    String random() {
        return toString();
    }
}
