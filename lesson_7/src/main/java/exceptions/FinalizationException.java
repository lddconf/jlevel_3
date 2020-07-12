package exceptions;

/**
 * Exception of this type should be thrown when test suite finalization failed.
 */
public class FinalizationException extends TestSuiteException {
    private static final String DEFAULT_DESCRIPTION = "Test suite finalization was failed!";

    /**
     * Constructor.
     */
    public FinalizationException() {
        super(DEFAULT_DESCRIPTION);
    }

    /**
     * Constructor.
     * @param message exception message.
     */
    public FinalizationException(String message) {
        super(message);
    }
}