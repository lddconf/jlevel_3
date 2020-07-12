package exceptions;

/**
 * Exception of this type should be thrown when test suite initialization failed.
 */
public class InitializationException extends TestSuiteException {
    private static final String DEFAULT_DESCRIPTION = "Test suite initialization was failed!";


    /**
     * {@inheritDoc}
     */
    public InitializationException() {
        super( DEFAULT_DESCRIPTION );
    }

    /**
     * {@inheritDoc}
     */
    public InitializationException(String message ) {
        super( message );
    }
}