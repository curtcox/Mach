package mach;

/**
 * The phase of mocking.
 */
enum Phase {

    /**
     * In this phase, the next invocation will define what is returned.
     * It is used to define behaviour prior to test execution.
     *
     * When in this phase, invoking a method on a mock will record the invocation.
     */
    returns,

    /**
     * In this phase, the next invocation will forbid invocations after it.
     * It is used to prohibit behaviour prior to test execution.
     *
     * When in this phase, invoking a method on a mock will record the invocation.
     */
    no,

    /**
     * In this phase, invocations will behave according to specification.
     * This is the only phase that should be used by the object being tested.
     */
    invoke,

    /**
     * In this phase, the next invocation will verify subsequent invocations happened also occurred previously.
     */
    verify;



    /**
     * The current phase.
     * This is essentially a global variable, so you should probably avoid using this framework to write
     * multi-threaded tests.
     * On the bright side, this whole class is package private.
     */
    static Phase current;

}
