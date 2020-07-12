import exceptions.TestSuiteException;

public class Main {
    public static void main(String[] args) {
        try {
            final TestSuiteExecutionResult result = TestExecutor.start( "TestSuite" );

            System.out.println("Test suite execution result: "
                    + result.getSucceededNumber() + " tests succeeded, "
                    + result.getFailedNumber() + " tests failed.");
        }
        catch( TestSuiteException e ) {
            System.out.println( e.getMessage() );
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
