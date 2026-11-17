import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

enum State { RELEASED, WANTED, HELD }
enum Type { REQUEST, REPLY }
record Message(Type type, int from, int ts) {}

class RAProcess {
    final int id, n;
    final LamportClock clock = new LamportClock();
    final LinkedBlockingQueue<Message> queue = new LinkedBlockingQueue<>();
    final List<Integer> deferred = new ArrayList<>();
    final Semaphore replies = new Semaphore(0);
    final Object lock = new Object();
    State state = State.RELEASED;
    int myTs;
    RAProcess[] peers;
    static final AtomicInteger inCS = new AtomicInteger();

    RAProcess(int id, int n) { this.id = id; this.n = n; }

    void requestCS() throws InterruptedException {
        synchronized (lock) {                      // one uninterrupted step
            state = State.WANTED;
            clock.sendEvent();                     // ONE tick
            myTs = clock.getValue();               // same value for all requests
            System.out.println("P" + id + " REQUEST ts=" + myTs);
            for (RAProcess p : peers)
                if (p != this) p.queue.put(new Message(Type.REQUEST, id, myTs));
        }
        replies.acquire(n - 1);                    // wait OUTSIDE the lock
        synchronized (lock) { state = State.HELD; }
    }

    void exitCS() throws InterruptedException {
        synchronized (lock) {
            state = State.RELEASED;
            for (int to : deferred) sendReply(to);
            deferred.clear();
        }
    }

    // caller must hold lock
    void sendReply(int to) throws InterruptedException {
        clock.sendEvent();
        peers[to].queue.put(new Message(Type.REPLY, id, clock.getValue()));
    }

    void handlerLoop() {
        try {
            while (true) {
                Message m = queue.take();
                synchronized (lock) {
                    clock.receiveEvent(m.from(), m.ts());   // update on EVERY message
                    if (m.type() == Type.REPLY) { replies.release(); continue; }
                    boolean mineFirst = state == State.WANTED &&
                            (myTs < m.ts() || (myTs == m.ts() && id < m.from()));
                    if (state == State.HELD || mineFirst) deferred.add(m.from());
                    else sendReply(m.from());
                }
            }
        } catch (InterruptedException e) { }
    }

    void requesterLoop(int rounds) throws InterruptedException {
        Random r = new Random();
        for (int k = 0; k < rounds; k++) {
            Thread.sleep(r.nextInt(500));
            requestCS();
            int inside = inCS.incrementAndGet();
            System.out.println("P" + id + " ENTER" + (inside > 1 ? "  *** VIOLATION ***" : ""));
            Thread.sleep(200);
            System.out.println("P" + id + " EXIT");
            inCS.decrementAndGet();
            exitCS();
        }
    }
}

public class RicartAgrawala {
    public static void main(String[] args) throws Exception {
        int n = 3, rounds = 5;
        RAProcess[] ps = new RAProcess[n];
        for (int i = 0; i < n; i++) ps[i] = new RAProcess(i, n);
        for (RAProcess p : ps) p.peers = ps;

        Thread[] requesters = new Thread[n];
        for (int i = 0; i < n; i++) {
            RAProcess p = ps[i];
            Thread h = new Thread(p::handlerLoop);
            h.setDaemon(true);                     // lets the program exit at the end
            h.start();
            requesters[i] = new Thread(() -> {
                try { p.requesterLoop(rounds); } catch (InterruptedException e) { }
            });
        }
        for (Thread t : requesters) t.start();
        for (Thread t : requesters) t.join();
        System.out.println("Done. No violations means mutual exclusion held.");
    }
}