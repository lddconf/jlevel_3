package exceptions;

public class AssertionFailureException extends TestSuiteException {
    /**
     * Constructor.
     *
     * @param message - exception message.
     */
    public AssertionFailureException(String message) {
        super(message);
    }
}