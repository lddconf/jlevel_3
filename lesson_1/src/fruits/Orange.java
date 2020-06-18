package fruits;

public class Orange implements Fruit {
    @Override
    public String name() {
        return "Orange";
    }

    @Override
    public float getWeight() {
        return 550;
    }
}
