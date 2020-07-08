import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static int[] task1(int[] input, int search_val) {
        int last_index = -1;

        if ( input == null || input.length < 1 ) throw new RuntimeException("No searched value of " + search_val );;

        //Search for value
        for (int i = 0; i < input.length; i++) {
            if ( search_val == input[i]) {
                last_index = i;
            }
        }gee

        //Create subarray
        if ( last_index >= 0 ) {
            return Arrays.copyOfRange(input, last_index + 1, input.length );
        }
        throw new RuntimeException("No searched value of " + search_val );
    }

    public static boolean task2(int[] array, int...checked_vals ) {
        boolean[] checked = new boolean[checked_vals.length];
        boolean result = true;
        Arrays.fill(checked, 0, checked.length, false);

        if ( array.length == 0 || checked_vals.length == 0) {
            return false;
        }

        //Check values in array
        for ( int val: array ) {
            boolean inSet = false;
            for (int i = 0; i < checked_vals.length; i++) {
                if ( val == checked_vals[i]) {
                    checked[i] = inSet = true;
                    break;
                }
            }
            if ( !inSet ) {
                return false;
            }
        }

        for ( boolean a : checked ) {
            result &= a;
        }
        return result;
    }

    public static void main(String[] args) {

        int[] a = new int[] { 1, 2, 0, 4, 2, 3, 0, 1, 7 };
        System.out.println( Arrays.toString(task1(a,1)) );
        System.out.println( task2(new int[] { 1, 4, 1 },4, 1) );

    }
}
