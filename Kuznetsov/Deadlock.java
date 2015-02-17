/**
 * Created by Pavel on 15.02.2015.
 */
public class Deadlock {
    public static Integer i, j;

    public static void main(String[] args) {
        i = 5;
        j = 7;
        MyThread t1 = new MyThread();
        MyThread2 t2 = new MyThread2();
        t1.start(); t2.start();
        System.out.println("Finish");

    }
}
    class MyThread extends Thread
    {
        public void run()
        {
            synchronized (Deadlock.i) {
                System.out.println("i belongs to t1");
                try {
                    sleep(5000);
                }catch (InterruptedException e)
                {
                    System.out.println(e.getMessage());
                }
                synchronized (Deadlock.j) {
                    System.out.println("j belongs to t1");
                    System.out.println("No deadlock. Thread 1 have been finished.");
                }
            }
        }
    }
    class MyThread2 extends Thread
    {
        public void run()
        {
            synchronized (Deadlock.j) {
                System.out.println("j belongs to t2");
                try {
                    sleep(5000);
                }catch (InterruptedException e)
                {
                    System.out.println(e.getMessage());
                }
                synchronized (Deadlock.i) {
                    System.out.println("i belongs to t2");
                    System.out.println("No deadlock. Thread 2 have been finished.");
                }
            }
        }
    }

