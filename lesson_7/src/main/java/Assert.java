import exceptions.AssertionFailureException;

/**
 * Assertions class.
 */
public class Assert {

    /**
     * Asserts that condition equals true.
     * If it isn't throws AssertionFailure exception.
     * @param message аssertion message.
     * @param condition condition.
     * @throws AssertionFailureException
     */
    public static void assertTrue( String message, boolean condition) throws AssertionFailureException {
        if( !condition ) {
            fail( message );
        }
    }

    /**
     * Asserts that condition equals true.
     * If it isn't throws AssertionFailure exception.
     * @param condition condition.
     * @throws AssertionFailureException
     */
    public static void assertTrue( boolean condition ) throws AssertionFailureException {
        assertTrue( null, condition );
    }

    /**
     * Asserts that condition equals false.
     * If it isn't throws AssertionFailure exception.
     * @param message аssertion message.
     * @param condition condition.
     * @throws AssertionFailureException
     */
    public static void assertFalse( String message, boolean condition ) {
        assertTrue( message, !condition );
    }

    /**
     * Asserts that condition equals false.
     * If it isn't throws AssertionFailure exception.
     * @param condition condition.
     * @throws AssertionFailureException
     */
    public static void assertFalse( boolean condition ) {
        assertFalse( null, condition );
    }

    /**
     * Throws exception on assertion failure.
     * @param message exception message.
     * @throws AssertionFailureException
     */
    public static void fail( String message ) throws AssertionFailureException {
        throw (message != null) ? new AssertionFailureException( message ) : new AssertionFailureException("");
    }
}