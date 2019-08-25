package mach;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Iterator;

import static org.junit.Assert.*;

public class InvocationMapTest {

    Object proxy = new Object();
    Method method = proxy.getClass().getDeclaredMethods()[0];
    Method method2 = proxy.getClass().getDeclaredMethods()[1];
    Object result = new Object();
    Invocation invocation = new Invocation(proxy,method,null,null);
    Invocation invocation2 = new Invocation(proxy,method2,null,null);
    InvocationMap map = new InvocationMap();

    @Test
    public void can_create() {
        assertNotNull(new InvocationMap());
    }

    @Test
    public void initially_contains_no_invocations() {
        assertFalse(map.contains(invocation));
    }

    @Test
    public void initially_invocations_iterable_hasNext_is_false() {
        Iterable iterable = new InvocationMap().invocations();
        Iterator iterator = iterable.iterator();
        assertFalse(iterator.hasNext());
    }

    @Test
    public void getResult_returns_result_when_previously_mapped() {
        map.map(invocation,result);
        Object actual = map.getResult(invocation);

        assertSame(result,actual);
    }

    @Test
    public void invocation_returns_invocation_when_previously_mapped() {
        map.map(invocation,result);
        Invocation actual = map.invocations.iterator().next();

        assertSame(invocation,actual);
    }

    @Test
    public void contains_returns_true_when_mapped() {
        map.map(invocation,result);
        assertTrue(map.contains(invocation));
    }

    @Test
    public void contains_only_returns_true_for_mapped_results() {
        map.map(invocation,result);
        assertFalse(map.contains(invocation2));
    }

    @Test
    public void getResult_returns_appropriate_result_when_multiple_results_mapped() {
        map.map(invocation,result);
        Object result2 = new Object();
        map.map(invocation2,result2);

        assertSame(result,map.getResult(invocation));
        assertSame(result2,map.getResult(invocation2));
    }

    @Test
    public void last_mapped_result_wins_for_the_same_invocation() {
        map.map(invocation,result);
        Object result2 = new Object();
        map.map(invocation,result2);

        assertSame(result2,map.getResult(invocation));

    }
}