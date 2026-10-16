package edu.itc.se.lab01.task3;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Consumer implements Runnable {

  int consumer_id;
  int capacity;
  OrderQueue queue;
  AtomicInteger processed_order_count = new AtomicInteger(0);

  Consumer(int consumer_id, int capacity, AtomicInteger processed_order_count, OrderQueue queue){
    this.consumer_id = consumer_id;
    this.capacity = capacity;
    this.queue = queue;
    this.processed_order_count = processed_order_count;
  }

  @Override 
  public void run(){
    Random random = new Random();
    try{
      while (true){
        Order take_order = this.queue.take();
        Thread.sleep(random.nextInt(5,31));
        System.out.println("consumer-%d processed Order[id=%d, item=%s, qty=%d]".formatted(this.consumer_id, take_order.id(), take_order.item(), take_order.qty()));
        
        
        if (take_order.id() == -1){
          System.out.println("consumer-%d received poison pill, stopping".formatted(this.consumer_id));
          break;
        }else{
          this.processed_order_count.incrementAndGet();
        }
      }
    }catch (InterruptedException e){
      e.printStackTrace();
    }
  }
  
}
