package edu.itc.se.lab01.task3;

import java.util.*;

public class Producer implements Runnable {

  int numberOfOrders;
  int numberOfConsumers;
  OrderQueue<Order> queue;


  Producer(int numberOfOrders, int numberOfConsumers, OrderQueue<Order> queue){
    this.numberOfOrders = numberOfOrders;
    this.numberOfConsumers = numberOfConsumers;
    this.queue = queue;
  }

  @Override 
  public void run(){
    Random random = new Random();
    for (int i = 0; i < this.numberOfOrders; i++){
      Order new_order = new Order(i, "item-"+i, random.nextInt(1, 6));
      try{
        queue.put(new_order);
        Thread.sleep(random.nextInt(0,21));
      }catch (InterruptedException e){
        e.printStackTrace();
      }
    }

    
  }
  
}
