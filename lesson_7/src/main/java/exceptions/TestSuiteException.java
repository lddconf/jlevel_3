package exceptions;

/**
 * Base class for exception which can be thrown at test suite execution.
 */
public class TestSuiteException extends RuntimeException {

    /**
     * Constructor.
     * @param message - exception message.
     */
    public TestSuiteException( String message) {
        super( message );
    }


}