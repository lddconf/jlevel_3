import fruits.Fruit;

import java.util.ArrayList;
import java.util.List;

public class Box <T extends Fruit> {
    private final List<T> items = new ArrayList<>(0);

    public void add(T item) {
        items.add( item );
    }

    public float getWeight() {
        float result = 0.f;
        for( T item : items ) {
            result += item.getWeight();
        }
        return result;
    }

    public int getItemsNumber() {
        return items.size();
    }

    public boolean compare(Box box) {
        return Float.compare( getWeight(), box.getWeight() ) == 0;
    }

    @Override
    public String toString() {
        return "Items count: " + getItemsNumber() + ", Weight: " + getWeight();
    }

    public void dropTo( Box<T> box ) {
        for( T item : items ) {
            box.add( item );
        }
        items.clear();
    }
}
