package fruits;

public class Apple implements Fruit {
    @Override
    public String name() {
        return "Apple";
    }

    @Override
    public float getWeight() {
        return 200;
    }
}
