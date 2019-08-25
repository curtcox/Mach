package mach;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class InvocationTest {

    Object proxy = new Object();
    Method method = proxy.getClass().getDeclaredMethods()[0];

    @Before
    public void setUp() {
        assumeTrue(ShouldRun.Mach);
    }

    @Test
    public void can_create_with_empty_array() {
        Invocation invocation = new Invocation(proxy,method,args(),wild());

        assertSame(proxy,invocation.proxy);
        assertSame(method,invocation.method);
        assertEquals(0, invocation.args.size());
    }

    private Invocation withArgs(Object[] args) {
        return new Invocation(proxy,method,args,null);
    }

    private Invocation withArgsWild(Object[] args, Object[] wild) {
        return new Invocation(proxy,method,args,wild);
    }

    @Test
    public void can_create_with_null_wildcard_array() {
        withArgs(args());
        withArgs(args(1));
        withArgs(args(1,2));
        withArgs(args(1,2,3));
    }

    @Test
    public void throws_IllegalArgumentException_when_more_args_than_wildcards() {
        try {
            new Invocation(proxy,method,args("x"),wild());
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Must have the same number of arguments and wildcards",e.getMessage());
        }
    }

    @Test
    public void throws_IllegalArgumentException_when_more_wildcards_than_args() {
        try {
            new Invocation(proxy,method,args(),wild(""));
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Must have the same number of arguments and wildcards",e.getMessage());
        }
    }

    @Test
    public void equal_values_are_equals_when_there_are_no_wildcards() {
        assertEqual(withArgs( args()),               withArgs(args()));
        assertEqual(withArgs(args("")),              withArgs(args("")));
        assertEqual(withArgs(args(new ArrayList())), withArgs(args(Collections.emptyList())));
    }

    @Test
    public void equal_values_are_equals_when_there_are_wildcards() {
        assertEqual(withArgsWild(args("x"),      wild("z")),      withArgsWild(args("x"),      wild("y")));
        assertEqual(withArgsWild(args("x", "y"), wild("*", "*")), withArgsWild(args("x", "y"), wild("", "")));
    }

    @Test
    public void equal_values_are_equals_when_wildcards_are_null() {
        assertEqual(withArgsWild(args("x"),           wild(null)),            withArgsWild(args("x"),           wild(null)));
        assertEqual(withArgsWild(args("x", "y"),      wild(null, null)),      withArgsWild(args("x", "y"),      wild(null, null)));
        assertEqual(withArgsWild(args("x", "y", "z"), wild(null, null, null)),withArgsWild(args("x", "y", "z"), wild(null, null, null)));
    }

    @Test
    public void null_values_are_equals_when_wildcards_are_null() {
        assertEqual(withArgsWild(args("x"),           wild(null)),             withArgsWild(args(null),             wild(null)));
        assertEqual(withArgsWild(args("x", "y"),      wild(null, null)),       withArgsWild(args(null, null),       wild(null, null)));
        assertEqual(withArgsWild(args("x", "y", "z"), wild(null, null, null)), withArgsWild(args(null, null, null), wild(null, null, null)));
    }

    @Test
    public void equal_values_are_equals_when_wildcards_are_null_and_values_balk_at_null_comparison() {
        assertEqual(withArgsWild(args(x("x")),                 wild(null)),             withArgsWild(args(x("x")),                 wild(null)));
        assertEqual(withArgsWild(args(x("x"), x("y")),         wild(null, null)),       withArgsWild(args(x("x"), x("y")),         wild(null, null)));
        assertEqual(withArgsWild(args(x("x"), x("y"), x("z")), wild(null, null, null)), withArgsWild(args(x("x"), x("y"), x("z")), wild(null, null, null)));
    }

    @Test
    public void null_values_are_equals_when_wildcards_are_null_and_values_balk_at_null_comparison() {
        assertEqual(withArgsWild(args(x("x")),                 wild(null)),             withArgsWild(args(null), wild(null)));
        assertEqual(withArgsWild(args(x("x"), x("y")),         wild(null, null)),       withArgsWild(args(null, null), wild(null, null)));
        assertEqual(withArgsWild(args(x("x"), x("y"), x("z")), wild(null, null, null)), withArgsWild(args(null, null, null), wild(null, null, null)));
    }

    static class X {
        final Object value;
        X(Object value) {
            this.value = value;
        }
        @Override
        public boolean equals(Object o) {
            // Despite "Effective Java" this is the form of equals implementation I usually want.
            X that = (X) o;
            return value.equals(that.value);
        }
    }

    private X x(final String value) {
        return new X(value);
    }

    @Test
    public void equal_values_are_equals_when_there_are_wildcards_in_unequal_spots() {
        assertEqual(withArgsWild( args("x"),           wild("x")),         withArgsWild( args("y"),           null));
        assertEqual(withArgsWild( args("x"),           wild("x")),         withArgsWild( args("y"),           wild("z")));
        assertEqual(withArgsWild( args("f", "*", "x"), wild("", "*", "")), withArgsWild( args("f", "o", "x"), wild("", "", "")));
    }

    private void assertEqual(Invocation a, Invocation b) {
        assertEquals(a,b);
        assertEquals(b,a);
        assertEquals(a.hashCode(),b.hashCode());
    }

    @Test
    public void mock_equals_itself() {
        Map map = Mocks.mock("foo", Map.class);
        assertEqual(new Invocation(proxy, method, args(map), wild("")), new Invocation(proxy, method, args(map), wild("")));
    }

    @Test
    public void different_arguments_are_not_equal() {
        assertFalse(withArgs(args()).equals(withArgs(args(null))));
        assertFalse(withArgs(args(0)).equals(withArgs( args(1))));
        assertFalse(withArgs(args("")).equals(withArgs( args(null))));
        assertFalse(withArgs(args(null)).equals(withArgs( args(""))));
    }

    @Test
    public void different_proxy_is_not_equal() {
        Object different = new Object();
        assertFalse(new Invocation(proxy, method,args(),wild()).equals(new Invocation(different, method,args(),wild())));
    }

    @Test
    public void different_method_is_not_equal() {
        Method different = proxy.getClass().getDeclaredMethods()[1];
        assertFalse(new Invocation(proxy, method,args(),wild()).equals(new Invocation(proxy, different,args(),wild())));
    }

    @Test
    public void method_that_returns_void() throws NoSuchMethodException {
        Method void_method = Iterator.class.getMethod("remove");
        assertTrue(new Invocation(proxy,void_method,args(),wild()).returnsVoid());
    }

    @Test
    public void method_that_returns_primitive() throws NoSuchMethodException {
        Method void_method = Iterator.class.getMethod("hasNext");
        assertFalse(new Invocation(proxy,void_method,args(),wild()).returnsVoid());
    }

    @Test
    public void method_that_returns_object() throws NoSuchMethodException {
        Method void_method = Iterator.class.getMethod("next");
        assertFalse(new Invocation(proxy, void_method, args(), wild()).returnsVoid());
    }

    Object[] args(Object... args) {
        if (args==null) {
            return new Object[] {null};
        }
        return args;
    }

    Object[] wild(Object... args) {
        return args(args);
    }

}
