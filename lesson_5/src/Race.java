import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Race {
    private ArrayList<Stage> stages;
    private CyclicBarrier    atLine;
    private ReadWriteLock    raceConfigLock;
    private int              competitorsCount;
    private AtomicInteger    finishedCompetitors;
    public ArrayList<Stage>  getStages() {
        return stages;
    }

    public Race(Stage... stages) {
        this.stages = new ArrayList<>(Arrays.asList(stages));
        finishedCompetitors = new AtomicInteger(0);
        raceConfigLock = new ReentrantReadWriteLock();
    }

    public boolean addCompetitor(Car player) {
        if ( raceConfigLock.writeLock().tryLock() ) {
            ++competitorsCount;
            raceConfigLock.writeLock().unlock();
            return true;
        }
        return false;
    }


    public void waitForCompetitorsAndBegin() throws BrokenBarrierException, InterruptedException {

        if ( !raceConfigLock.writeLock().tryLock() ) {
            return;
        }
        //Prepare to start
        atLine = new CyclicBarrier(competitorsCount+1);
        finishedCompetitors.set(0);

        this.stages.forEach((Stage s)->{
            if ( s instanceof Tunnel ) {
                ((Tunnel)s).setLimit(competitorsCount/2);
            }
        });

        raceConfigLock.writeLock().unlock();

        //Wait while other players ready to run and run race
        waitForOthers();

        //For next usage
        raceConfigLock.writeLock().lock();
        atLine.reset();
        raceConfigLock.writeLock().unlock();
    }

    private void waitForOthers() throws BrokenBarrierException, InterruptedException {
        raceConfigLock.readLock().lock();
        //Wait for other playes and run
        if ( atLine == null ) {
            throw new RuntimeException("Race not started!");
        }
        try {
            atLine.await();
        }  finally {
            raceConfigLock.readLock().unlock();
        }
    }

    public void toStartGridAndRun(Car car) throws BrokenBarrierException, InterruptedException {
        waitForOthers();
    }

    public void competitorFinished(Car competitor) throws BrokenBarrierException, InterruptedException {
        if (finishedCompetitors.getAndIncrement() == 0) {
            System.out.println(competitor.getName() + " - WIN");
        }
        waitForOthers();
    }

    public void waitForCompetitorsFinished() throws BrokenBarrierException, InterruptedException {
        waitForOthers();

        raceConfigLock.writeLock().lock();
        atLine = null;
        raceConfigLock.writeLock().unlock();
    }
}