import fruits.Apple;
import fruits.Orange;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final Integer intArray[] = { 1, 2, 3, 4, 5 };
    private static final String strArray[] = { "First", "Second", "Third" };

    private static <T> void printArray( T[] array ) {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i] + " ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        //Task 1
        System.out.println("Task 1: before swap: ");
        printArray(intArray);
        swap(intArray, 0, intArray.length - 1);
        System.out.println("Task 1: after swap: ");
        printArray(intArray);

        //Task 2
        final List<Integer> arrayList = toArrayList( intArray );
        System.out.println( "Task 2: List from integer array: " );
        System.out.println( arrayList );

        //Task 3
        final Apple singleApple = new Apple();
        final Orange singleOrange = new Orange();

        final Box<Apple> appleBoxOne = new Box<>(), appleBoxTwo = new Box<>();
        final Box<Orange> orangeBoxOne = new Box<>(), orangeBoxTwo = new Box<>();

        appleBoxOne.add( singleApple );
        //orangeBoxOne.add( singleApple ); -> Cause compile error.
        orangeBoxOne.add( singleOrange );

        System.out.println();
        System.out.println("Task 3");
        System.out.println( "Apple box one: " + appleBoxOne );
        System.out.println( "Orange box one: " + orangeBoxOne );

        System.out.println( "Is boxes weight equal? Answer: " + appleBoxOne.compare( orangeBoxOne ) );

        appleBoxOne.dropTo( appleBoxTwo );
        //orangeBoxOne.dropTo( appleBoxTwo ); //compile error.
        orangeBoxOne.dropTo( orangeBoxTwo );

        System.out.println( "After drop:  " );
        System.out.println( "Apple box one: " + appleBoxOne );
        System.out.println( "Orange box one: " + orangeBoxOne );
        System.out.println( "Apple box two: " + appleBoxTwo );
        System.out.println( "Orange box two: " + orangeBoxTwo );

    }

    private static <T> List<T> toArrayList(final T[] array) {
        final List<T> result = new ArrayList<>(0);

        for( T element : array ) {
            result.add( element );
        }

        return result;
    }

    private static <T> void swap(T[] array, int first, int second) {
        final T temp = array[first];
        array[first] = array[second];
        array[second] = temp;
    }
}
