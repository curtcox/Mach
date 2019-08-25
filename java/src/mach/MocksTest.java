package mach;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;

import static mach.Mocks.*;
import static mach.Mocks.no;
import static mach.Mocks.returns;
import static mach.Mocks.verify;
import static mach.Phase.*;
import static mach.Phase.no;
import static mach.Phase.verify;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class MocksTest {

    Sample sample;

    interface Sample {
        String getValue();
        String getNext(String current);
    }

    @Before
    public void setUp() {
        assumeTrue(ShouldRun.Mach);
    }

    @Before
    public void init() {
        Mocks.init(this);
    }

    @Test
    public void init_assigns_values_to_null_instance_variables_that_are_interfaces() {
        assertTrue(sample instanceof Sample);
    }

    @Test
    public void init_sets_phase_to_invoke() {
        assertEquals(invoke,current);
    }

    @Test
    public void mock_implements_specified_interface() {
        assertTrue(mock("name",Sample.class) instanceof Sample);
    }

    @Test
    public void returns_makes_mock_return_specified_value_for_method_with_no_args() {
        Sample sample = mock("name",Sample.class);
        String expected = "expected";
        returns(expected);  sample.getValue();

        go();
        String actual = sample.getValue();

        assertSame(expected,actual);
    }

    @Test
    public void returns_makes_mock_return_specified_value_for_method_with_one_arg() {
        Sample sample = mock("name",Sample.class);
        String expected = "2nd";
        returns(expected);  sample.getNext("1st");

        go();
        String actual = sample.getNext("1st");

        assertSame(expected,actual);
    }

    @Test
    public void returns_makes_mock_return_specified_value_for_method_with_one_arg_using_wildcard() {
        Sample sample = mock("name",Sample.class);
        String expected = "2nd";
        returns(expected);  wild("*"); sample.getNext("*");

        go();
        String actual = sample.getNext("1st");

        assertSame(expected,actual);
    }

    @Test
    public void returns_makes_mock_return_specified_value_for_method_with_one_arg_using_null_as_wildcard() {
        Sample sample = mock("name",Sample.class);
        String expected = "2nd";
        returns(expected);  wild(null); sample.getNext(null);

        go();
        String actual = sample.getNext("1st");

        assertSame(expected,actual);
    }

    @Test
    public void arg_makes_mock_return_arg_value_for_method_with_one_arg_during_verify() {
        Sample sample = mock("name",Sample.class);
        returns("don't care, but needed");  wild(null); sample.getNext(null);

        String expected = "arg value used";
        go();
        sample.getNext(expected);

        verify();
        wild(null); sample.getNext(null); String actual = arg();

        assertSame(expected,actual);
    }

    @Test
    public void verify_sets_current_state_to_verify() {
        verify();
        assertSame(verify,current);
    }

    @Test
    public void no_sets_current_state_to_no() {
        no();
        assertSame(no,current);
    }

    @Test
    public void verify_does_not_fail_when_method_invoked() {
        Sample sample = mock("name",Sample.class);
        returns("");  sample.getValue();

        go();
        sample.getValue();

        verify();
        sample.getValue();
    }

    @Test
    public void verify_fails_when_method_not_invoked() {
        Sample sample = mock("name",Sample.class);

        verify();

        try {
            sample.getValue();
        } catch (AssertionError e) {
            return;
        }
        fail();
    }

    @Test
    public void no_does_not_fail_when_method_not_invoked() {
        Sample sample = mock("name",Sample.class);

        no();

        sample.getValue();
    }

    @Test
    public void no_fails_when_method_invoked() {
        Sample sample = mock("name",Sample.class);

        no(); sample.getValue();

        try {
            sample.getValue();
        } catch (AssertionError e) {
            return;
        }
        fail();
    }

    @Test
    public void no_fails_when_method_with_argument_invoked() {
        Sample sample = mock("name",Sample.class);

        no(); sample.getNext("forbidden");

        try {
            sample.getNext("forbidden");
        } catch (AssertionError e) {
            return;
        }
        fail();
    }

    @Test
    public void no_does_not_fail_when_valid_argument_invoked() {
        Sample sample = mock("name",Sample.class);
        _(""); sample.getNext("OK");
        no(); _(""); sample.getNext("forbidden");

        sample.getNext("OK");
    }

    @Test
    public void no_fails_when_invalid_argument_invoked() {
        Sample sample = mock("name",Sample.class);
        _(""); sample.getNext("OK");
        no(); _(""); sample.getNext("forbidden");

        try {
            go();
            sample.getNext("forbidden");
        } catch (AssertionError e) {
            return;
        }
        fail();
    }

    @Test
    public void toString_returns_name_class_and_hash() {
        Sample sample = mock("name",Sample.class);
        String expected = "name:" + Sample.class + "@" + System.identityHashCode(Proxy.getInvocationHandler(sample));
        String actual = sample.toString();
        assertEquals(expected,actual);
    }
}
