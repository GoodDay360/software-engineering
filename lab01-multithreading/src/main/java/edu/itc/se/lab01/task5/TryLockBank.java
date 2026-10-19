package edu.itc.se.lab01.task5;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class TryLockBank {
    static AtomicLong transfer_complete_count = new AtomicLong(0);

    static final class Account {
        final int id; private long balance;
        Account(int id, long balance) { this.id = id; this.balance = balance; }
        long balance() { return balance; }
        void withdraw(long a) { balance -= a; }
        void deposit(long a)  { balance += a; }
        ReentrantLock lock = new ReentrantLock();
    }

    // BUG: lock order depends on the argument order → circular wait
    static void transfer(Account from, Account to, long amount) {
        int retry_count = 0;
        Random r = new Random();
        while (true){
            Boolean aquired_from_lock = false;
            Boolean aquired_to_lock = false;
            
            try{
                aquired_from_lock = from.lock.tryLock(10, TimeUnit.MILLISECONDS);
                aquired_to_lock = to.lock.tryLock(10, TimeUnit.MILLISECONDS);
                if (!aquired_from_lock || !aquired_to_lock){ 
                    retry_count++;
                    if (aquired_from_lock) from.lock.unlock();
                    if (aquired_to_lock) to.lock.unlock();
                    aquired_from_lock = false;
                    aquired_to_lock = false;
                    Thread.sleep(r.nextInt(500, 1000));
                    continue;
                }
                
                if (from.balance() >= amount) { 
                    from.withdraw(amount); 
                    to.deposit(amount); 
                    if (retry_count > 0){
                        System.out.println("Thread: %s, Retry count: %d".formatted(Thread.currentThread().getName(), retry_count));
                    }   
                    transfer_complete_count.incrementAndGet();
                }
                
                return;
                
            }catch (InterruptedException e) { 
                Thread.currentThread().interrupt();
                return;
            }catch(Exception e){
                e.printStackTrace();
                return;
            }finally{
                if (aquired_from_lock) from.lock.unlock();
                if (aquired_to_lock) to.lock.unlock();
            }
            
        }
        
    }

    public static void watchdog() {
        Thread t = new Thread(()->{
            ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            while (true) {
                try {
                    long[] dt_arr = bean.findDeadlockedThreads();
                    if (dt_arr == null) continue;
                    for (long thread_id: dt_arr){
                        ThreadInfo t_info = bean.getThreadInfo(thread_id);
                        System.out.println("Deadlock Thread name: %s".formatted(t_info.getThreadName()));
                    }
                    System.out.println("===");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public static void main(String[] args) throws InterruptedException {
        watchdog();
        Account[] accounts = new Account[4];
        for (int i = 0; i < accounts.length; i++) accounts[i] = new Account(i, 1_000);

        Thread[] workers = new Thread[8];

        for (int w = 0; w < workers.length; w++) {
            workers[w] = new Thread(() -> {
                var rnd = ThreadLocalRandom.current();
                while (!Thread.currentThread().isInterrupted()) {
                    Account a = accounts[rnd.nextInt(accounts.length)];
                    Account b = accounts[rnd.nextInt(accounts.length)];
                    if (a != b) transfer(a, b, rnd.nextInt(1, 50));
                }
            }, "transfer-" + w);
            workers[w].start();
        }
        
        try{
            Thread.sleep(2_000);
            for (Thread t : workers) 
                t.interrupt();
            for (Thread t : workers) 
                t.join(1_000);
        }catch(Exception e){
            e.printStackTrace();
        }

        long total = 0; 
        for (Account a : accounts) total += a.balance();
        System.out.println("completed transfers = " + transfer_complete_count.get());
        System.out.println("total = " + total + " (expected 4000)");   // never printed – deadlock
    }
}