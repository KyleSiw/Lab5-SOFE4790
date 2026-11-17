package Task_2_Code;// Counter shared by multiple threads -- access is not synchronized

import java.util.concurrent.Semaphore;

public class task2Code extends Thread {

   private static int counter = 0;
   private static final int limit = 500;
   private static final int max_available = 1;
   private static final Semaphore sem =  new Semaphore(max_available, true);

   public static void main(String argv[]) throws InterruptedException {
      task2Code t1 = new task2Code();
      task2Code t2 = new task2Code();
      task2Code t3 = new task2Code();

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
          try {
              sem.acquire();
          } catch (InterruptedException e) {
              throw new RuntimeException(e);
          }
          if(counter >= limit){
              sem.release();
              break;
          }
          incrementCounter();
          sem.release();
      }
   }

   public void incrementCounter() {
      System.out.println(Thread.currentThread().getName() + " counter : " + counter);
      counter++;
   }

}
