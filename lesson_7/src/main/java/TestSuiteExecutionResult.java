public class TestSuiteExecutionResult {

    private final int succeeded;
    private  final int failed;

    /**
     * Constructor.
     * @param succeeded - number of successfully finished tests.
     * @param failed - number of failed tests.
     */
    public TestSuiteExecutionResult( final int succeeded, final int failed ) {
        this.succeeded = succeeded;
        this.failed = failed;
    }

    /**
     * @return Number of successfully finished tests.
     */
    public int getSucceededNumber() {
        return succeeded;
    }

    /**
     * @return Number of failed tests.
     */
    public int getFailedNumber() {
        return failed;
    }

    /**
     * @return Overall tests number.
     */
    public int getTestsNumber() {
        return succeeded + failed;
    }
}