import annotations.AfterSuite;
import annotations.BeforeSuite;
import annotations.Priority;
import annotations.Test;
import exceptions.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TestExecutor {

    /**
     * General types of methods in test suite.
     */
    private enum TestSuiteMethodType {

        BEFORE_SUITE_METHOD(0),
        AFTER_SUITE_METHOD(1),
        TEST_METHOD(2),
        DEFAULT_METHOD(3);

        private int value;

        /**
         * Constructor.
         *
         * @param value - integer value which corresponding method type.
         */
        TestSuiteMethodType(final int value) {
            this.value = value;
        }

        /**
         * @return Integer value which corresponding method type.
         */
        public int getValue() {
            return value;
        }
    }

    /**
     * Executes test suite.
     *
     * @param testSuiteName test suite class name.
     * @return Test suite execution result.
     * @throws ClassNotFoundException if class with specified name doesn't exist.
     */
    public static TestSuiteExecutionResult start(String testSuiteName) throws ClassNotFoundException {
        Class testSuite = Class.forName(testSuiteName);
        return start(testSuite);
    }

    /**
     * Executes test suite.
     *
     * @param testSuite test suite class.
     * @return Test suite execution result.
     */
    public static TestSuiteExecutionResult start(Class testSuite) throws TestSuiteException {

        final List<Method> executionList = new ArrayList<>();
        Method beforeSuiteMethod = null;
        Method afterSuiteMethod = null;
        Method[] methods = testSuite.getMethods();

        for (Method method : methods) {
            switch (getMethodType(method)) {
                case BEFORE_SUITE_METHOD:
                    if (beforeSuiteMethod == null) {
                        beforeSuiteMethod = method;
                    } else {
                        throw new TestSuiteException("There should be only one @BeforeSuite annotated method.");
                    }
                    break;
                case AFTER_SUITE_METHOD:
                    if (afterSuiteMethod == null) {
                        afterSuiteMethod = method;
                    } else {
                        throw new TestSuiteException("There should be only one @AfterSuite annotated method.");
                    }
                    break;
                case TEST_METHOD:
                    executionList.add(method);
                    break;
                default:
                    break;
            }
        }

        initialize(beforeSuiteMethod, testSuite);
        sortByPriority(executionList);
        int succeeded = execute( executionList, testSuite );
        finalize(afterSuiteMethod, testSuite);

        return new TestSuiteExecutionResult(succeeded, executionList.size() - succeeded);
    }

    /**
     * Returns test suite method type.
     */
    private static TestSuiteMethodType getMethodType(Method method) throws AnnotationErrorException {

        boolean annotationChecks[] = {false, false, false};

        if (method.getAnnotation(BeforeSuite.class) != null) {
            annotationChecks[TestSuiteMethodType.BEFORE_SUITE_METHOD.getValue()] = true;
        }

        if (method.getAnnotation(AfterSuite.class) != null) {
            annotationChecks[TestSuiteMethodType.AFTER_SUITE_METHOD.getValue()] = true;
        }

        if (method.getAnnotation(Test.class) != null) {
            annotationChecks[TestSuiteMethodType.TEST_METHOD.getValue()] = true;
        }

        final int POSSIBLE_TEST_SUITE_METHOD_TYPES_NUMBER = 3;
        int index = POSSIBLE_TEST_SUITE_METHOD_TYPES_NUMBER;
        for (int i = 0, counter = 0; i < annotationChecks.length; ++i) {
            counter += annotationChecks[i] ? 1 : 0;
            index = annotationChecks[i] ? i : index;
            if (counter > 1) {
                throw new AnnotationErrorException("Different annotation applied to one method.");
            }
        }

        return TestSuiteMethodType.values()[index];
    }

    /**
     * Initializes test suite.
     * @param method    initialization method.
     * @param testSuite test suite class.
     * @throws InitializationException
     */
    private static void initialize(final Method method, final Class testSuite) throws InitializationException {
        try {
            if (method == null) throw new InitializationException();
            method.invoke(testSuite.getConstructor().newInstance());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
            e.printStackTrace();
            throw new InitializationException();
        }
    }


    /**
     * Executes
     * @param methods
     * @param testSuite
     * @return
     */
    private static int execute(final List<Method> methods, final Class testSuite) {
        int succeeded = 0;
        for (Method testMethod : methods) {
            System.out.println("Executing method: " + testMethod.getName() );
            if( executeTest(testMethod, testSuite) ) {
                succeeded += 1;
                System.out.println("Test passed!");
            }
            else {
                System.out.println("Test failed!");
            }
        }
        return succeeded;
    }

    /**
     * Executes test suite method.
     *
     * @param testMethod - method which annotated as {@code @Test}
     * @param testSuite  - test suite class.
     * @return Test result.
     */
    private static boolean executeTest(final Method testMethod, final Class testSuite) {
        try {
            testMethod.invoke(testSuite.getConstructor().newInstance());
            return true;
        } catch (AssertionFailureException e) {
            e.getMessage();
            return false;
        } catch (IllegalAccessException | NoSuchMethodException | InstantiationException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            Test annotation = testMethod.getAnnotation( Test.class );
            return annotation.expected() == e.getCause().getClass();
        }
    }

    /**
     * Finalizes test suite.
     *
     * @param method    - finalization method.
     * @param testSuite - test suite class.
     * @throws FinalizationException
     */
    private static void finalize(final Method method, final Class testSuite) throws FinalizationException {
        try {
            if (method == null) throw new FinalizationException();
            method.invoke(testSuite.getConstructor().newInstance());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
            e.printStackTrace();
            throw new FinalizationException();
        }
    }

    /**
     * Descending order priority comparator.
     */
    private static class PriorityDescendingComparator implements Comparator<Method> {
        @Override
        public int compare(Method first, Method second) {
            int firstPriority = (first.getAnnotation(Priority.class) != null) ? first.getAnnotation(Priority.class).value() : 1;
            int secondPriority = (second.getAnnotation(Priority.class) != null) ? second.getAnnotation(Priority.class).value() : 1;
            return Integer.compare(secondPriority, firstPriority);
        }
    }

    /**
     * Sorts methods which annotated as {@code @Test} by priority.
     */
    private static void sortByPriority(List<Method> methods) {
        Collections.sort(methods, new PriorityDescendingComparator());
    }
}