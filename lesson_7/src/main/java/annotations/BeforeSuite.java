package annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates class method that should be executed before all tests execution.
 * This annotation should be applied to only one method in class.
 * Method with this annotation can't be annotated as {@code @Test} or {@code @AfterSuite}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BeforeSuite {
}