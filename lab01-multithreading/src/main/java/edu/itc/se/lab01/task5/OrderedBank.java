package edu.itc.se.lab01.task5;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class OrderedBank {
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
        Account first;
        Account second;

        if (from.id < to.id){
            first = from;
            second = to;
        }else{
            first = to;
            second = from;
        }

        synchronized (first) {
            synchronized (second) {
                if (from.balance() >= amount) { 
                    from.withdraw(amount); to.deposit(amount); 
                    transfer_complete_count.incrementAndGet();
                }
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
            for (Thread t : workers) t.interrupt();
        }catch(Exception e){
            e.printStackTrace();
        }

        long total = 0; 
        for (Account a : accounts) total += a.balance();
        System.out.println("completed transfers = " + transfer_complete_count.get());
        System.out.println("total = " + total + " (expected 4000)");   // never printed – deadlock
    }
}