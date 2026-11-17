// Counter shared by multiple threads -- access is not synchronized

public class task1code extends Thread {

   private static int counter = 0;
   private static final int limit = 1000;

   public static void main(String argv[]) throws InterruptedException {
      task1code t1 = new task1code();
      task1code t2 = new task1code();
      task1code t3 = new task1code();

      t1.start();
      t2.start();
      t3.start();

      t1.join();
      t2.join();
      t3.join();

      System.out.println("Final counter : " + counter + " (limit was " + limit + ")");
   }

   public void run() {
      while (true) {
         synchronized (task1code.class) {
            if (counter >= limit) break;
            incrementCounter();   // can stay as a plain method
         }
      }
   }
   public synchronized void  incrementCounter() {
      System.out.println(Thread.currentThread().getName() + " counter : " + counter);
      counter++;
   }

}
