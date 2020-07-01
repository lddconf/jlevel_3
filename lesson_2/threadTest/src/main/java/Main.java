import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static Object mon = new Object();
    private static int count = 0;
    private static final int THREAD_COUNT = 3;
    private static final int MAX_COUNT = 10;

    public static void main(String[] args) {
        ExecutorService service = Executors.newFixedThreadPool(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            service.execute(new SimpleTask(i));
        }
        service.shutdown();
    }

    static class SimpleTask implements Runnable {
        private int id;

        public SimpleTask(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            synchronized (mon) {
                while (count < MAX_COUNT) {
                    while ( count % THREAD_COUNT != id ) {
                        try {
                            mon.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    mon.notifyAll();

                    System.out.print((char)(id + 65));
                    count++;
                }
            }
        }
    }
}