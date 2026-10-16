package edu.itc.se.lab01.task3;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


class BoundedBuffer implements OrderQueue {
  private final ArrayDeque<Order> item_arr = new ArrayDeque<>();
  private final int capacity;

  public BoundedBuffer(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("Capacity must be positive");
    }

    this.capacity = capacity;
  }

  @Override
  public synchronized void put(Order order) throws InterruptedException {
    while (item_arr.size() == capacity) {
      wait();
    }

    item_arr.addLast(order);

    notifyAll();
  }


  @Override
  public synchronized Order take() throws InterruptedException {
    while (item_arr.isEmpty()) {
      wait();
    }

    Order item = item_arr.pollFirst();

    notifyAll();

    return item;
  }
}


public class Main {
  
  void main(String[] args) {
    int numberOfProducers = 2;
    int numberOfOrders = 50;
    int numberOfConsumers = 3;
    int capacity = 5;
    AtomicInteger processed_order_count = new AtomicInteger(0);

    Random random = new Random();
    BoundedBuffer buffer = new BoundedBuffer(capacity);

    long startTime = System.currentTimeMillis();

    Thread producer_thread = new Thread(()->{
      // Make producers add all orders first
      ArrayList<Thread> p_t = new ArrayList<>();
      for (int i = 0; i < numberOfProducers; i++) {
        Producer producer = new Producer(numberOfOrders, numberOfConsumers, buffer);
        Thread producerThread = new Thread(producer);
        producerThread.start();
        p_t.add(producerThread);
      }

      for (Thread thread : p_t) {
        try {
          thread.join();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

      // Add poison pill when done process all orders
      for (int i = 0; i < numberOfConsumers; i++){
        Order poison_pill = new Order(-1, "poison pill", random.nextInt(1, 6));
        try{
          buffer.put(poison_pill);
        }catch (InterruptedException e){
          e.printStackTrace();
        }
      }

    });
    producer_thread.start();
    
    ArrayList<Thread> consumerThreads = new ArrayList<>();

    for (int i = 1; i <= numberOfConsumers; i++) {
      Consumer consumer = new Consumer(i, numberOfOrders, processed_order_count, buffer);
      Thread consumerThread = new Thread(consumer);
      consumerThread.start();
      consumerThreads.add(consumerThread);
    }

    for (Thread thread : consumerThreads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    System.out.println("Total processed: %d (expected 100) in %s ms".formatted(processed_order_count.get(), System.currentTimeMillis() - startTime));
  }
}
