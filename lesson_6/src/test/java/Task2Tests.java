import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class Task2Tests {
    private int[] array;
    private boolean result;
    private int[]   checked_vals;

    public Task2Tests(int[] array, boolean result, int[] checked_vals) {
        this.array = array;
        this.result = result;
        this.checked_vals = checked_vals;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> makeTestData() {
        return Arrays.asList(new Object[][]{
                { new int[] { 1, 1, 1, 4, 4, 1, 4, 4}, true, new int[]{1,4} },
                { new int[] { 1, 1, 1, 1, 1, 1 }, false, new int[]{1,4} },
                { new int[] { 4, 4, 4, 4}, false, new int[]{1,4} },
                { new int[] { 1, 4, 4, 1, 1, 4, 3}, false, new int[]{1,4} },

                //For general test purposes (not for lesson_6)
                { new int[] { 1, 4, 4, 1, 1, 4, 3}, true, new int[]{1, 4, 3} }
        });
    }

    @Test
    public void testNoExceptionCases1() {
        Assert.assertTrue( Main.task2(array, checked_vals) == result );
    }
}
