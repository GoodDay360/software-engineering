package edu.itc.se.lab01.task2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

interface Counter {
  void increment();

  long get();
}

class UnsafeCounter implements Counter {
  long count = 0;
  @Override 
  public void increment(){
    count++;
  }

  @Override 
  public long get() {
    return count;
  }
}


class SynchronizedCounter implements Counter {
  long count = 0;
  @Override 
  public synchronized void increment(){
    count++;
  }

  @Override 
  public synchronized long get() {
    return count;
  }
}

class LockCounter implements Counter{
  long count = 0;
  Lock lock = new ReentrantLock();
  @Override 
  public void increment(){
    lock.lock();
    try {
      count++;
    } finally {
      lock.unlock();
    }
  }

  @Override 
  public long get() {
    lock.lock();
    try {
      return count;
    } finally {
      lock.unlock();
    }
  }

}

class AtomicCounter implements Counter {

  AtomicLong count = new AtomicLong(0);
  @Override
  public void increment() {
    count.incrementAndGet();
  }

  @Override 
  public long get() {
    return count.get();
  }
}

class LongAdderCounter implements Counter {
  // LongAdder is faster under high contention because because it does not update on the same memory location
  // instead it maintains a set of variables and updates them independently.
  LongAdder count = new LongAdder();
  @Override
  public void increment() {
    count.increment();
  }

  @Override 
  public long get() {
    return count.sum();
  }
}



public class Bench {

  static final int THREADS = 8;
  static final int ITERATIONS = 1_000_000;

  static void run(String label, Counter counter) throws InterruptedException {
    long start = System.nanoTime();
    List<Thread> threads = new ArrayList<Thread>();
    for (int i = 0; i < Bench.THREADS; i++){
      Thread new_thread = new Thread(()->{
        for (int j = 0; j < Bench.ITERATIONS; j++){
          counter.increment();
        }
      }, "inc- %d".formatted(i));
      threads.add(new_thread);
      new_thread.start();
    }

    for (Thread thread : threads) {
      thread.join();
    }
    long ms = (System.nanoTime() - start) / 1_000_000;

    long expected = (long) THREADS * ITERATIONS;

    System.out.println(
      "%s-22s value=%,d correct=%-5b %d ms"
      .formatted(
        label, counter.get(), counter.get() == expected, ms
      )
    );

  }


  public static void main(String[] args) throws InterruptedException {
    run("UnsafeCounter", new UnsafeCounter());
    run("SynchronizedCounter", new SynchronizedCounter());
    run("LockCounter", new LockCounter());
    run("AtomicCounter", new AtomicCounter());
    run("LongAdderCounter", new LongAdderCounter());
  }
  
}
