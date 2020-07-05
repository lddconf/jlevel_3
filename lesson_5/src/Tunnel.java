import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Tunnel extends Stage {
    private Semaphore sm;
    private ReadWriteLock settingsLock; //To prevent semaphore change

    public Tunnel() {
        this.length = 80;
        this.description = "Тоннель " + length + " метров";
        sm = new Semaphore(1);
        settingsLock = new ReentrantReadWriteLock();
    }

    public void setLimit(int competitorsLimit) {
        settingsLock.writeLock().lock();
        try {
            sm = new Semaphore(competitorsLimit);
        } finally {
            settingsLock.writeLock().unlock();
        }
    }


    @Override
    public void go(Car c) {
        try {
            try {
                System.out.println(c.getName() + " готовится к этапу(ждет): " + description);
                settingsLock.readLock().lock();
                sm.acquire();
                System.out.println(c.getName() + " начал этап: " + description);
                Thread.sleep(length / c.getSpeed() * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println(c.getName() + " закончил этап: " + description);
                sm.release();
                settingsLock.readLock().unlock();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}