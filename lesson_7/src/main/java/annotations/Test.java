package annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates class method that should be run as test.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {
    /**
     * @return Expected exception class.
     */
    Class<? extends Throwable> expected() default Test.None.class;

    /**
     * Dummy type which using when no any exception expected.
     */
    class None extends Throwable {
        private None() {
        }
    }
}