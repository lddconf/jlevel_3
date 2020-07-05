import org.junit.Assert;
import org.junit.Test;

public class Task1Tests {

    @Test
    public void testSubArray() {
        Assert.assertArrayEquals(new int[]{ 7 }, Main.task1(new int[] { 1, 2, 0, 4, 2, 3, 0, 4, 7 }, 4));
    }

    @Test
    public void testEmptyArray() {
        Assert.assertArrayEquals(new int[]{}, Main.task1(new int[] { 1, 2, 0, 4, 2, 3, 0, 4, 4 }, 4));
    }

    @Test( expected = RuntimeException.class )
    public void testNoExpectedValue() {
        Main.task1(new int[] { 1, 2 }, 4);
    }

    @Test( expected = RuntimeException.class )
    public void testEmptyInputArray() {
        Main.task1(new int[] {}, 4);
    }

    @Test( expected = RuntimeException.class )
    public void testNullInputArray() {
        Main.task1(null, 4);
    }
}
