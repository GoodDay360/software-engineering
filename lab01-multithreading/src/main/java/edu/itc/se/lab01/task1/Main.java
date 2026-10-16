package edu.itc.se.lab01.task1;


class Countdown extends Thread {
  String name;
  Countdown(String name) {
    this.name = name;
  }
  @Override 
  public void run() {
    try {
      System.out.println("%s: 3".formatted(this.name));
      Thread.sleep(200);
      System.out.println("%s: 2".formatted(this.name));
      Thread.sleep(200);
      System.out.println("%s: 1".formatted(this.name));
      Thread.sleep(200);
      System.out.println("%s: liftoff!".formatted(this.name));
      Thread.sleep(200);
    } catch (InterruptedException e) {
      System.out.println(e);
    }
  }
}

class CountdownTask implements Runnable {
  @Override 
  public void run() {
    try {
      System.out.println("%s: 3".formatted(Thread.currentThread().getName()));
      Thread.sleep(200);
      System.out.println("%s: 2".formatted(Thread.currentThread().getName()));
      Thread.sleep(200);
      System.out.println("%s: 1".formatted(Thread.currentThread().getName()));
      Thread.sleep(200);
      System.out.println("%s: liftoff!".formatted(Thread.currentThread().getName()));
      Thread.sleep(200);
    } catch (InterruptedException e) {
      System.out.println(e);
    }
  }
}


public class Main {
  public static void main(String[] args) throws InterruptedException {
    boolean sequential = args.length > 0 && args[0].equals("--sequential");

    Thread apollo = new Countdown("apollo");
    Thread gemini = new Thread(new CountdownTask(), "gemini");
    Thread mercury = new Thread(()->{
      try {
        System.out.println("%s: 3".formatted(Thread.currentThread().getName()));
        Thread.sleep(200);
        System.out.println("%s: 2".formatted(Thread.currentThread().getName()));
        Thread.sleep(200);
        System.out.println("%s: 1".formatted(Thread.currentThread().getName()));
        Thread.sleep(200);
        System.out.println("%s: liftoff!".formatted(Thread.currentThread().getName()));
        Thread.sleep(200);
      } catch (InterruptedException e) {
        System.out.println(e);
      }
    }, "mercury");

    Thread artemis = Thread.ofVirtual().name("artemis").unstarted(new CountdownTask());


    if (!sequential) {
      System.out.println("Mission control: all launched");
      apollo.start();
      gemini.start();
      mercury.start();
      artemis.start();

      
      apollo.join();
      gemini.join();
      mercury.join();
      artemis.join();
      System.out.println("...");
      System.out.println("Mission control: all landed");

      System.out.println("apollo: virtual=%s state=%s".formatted(apollo.isVirtual(), apollo.getState()));
      System.out.println("gemini: virtual=%s state=%s".formatted(gemini.isVirtual(), gemini.getState()));
      System.out.println("mercury: virtual=%s state=%s".formatted(mercury.isVirtual(), mercury.getState()));
      System.out.println("artemis: virtual=%s state=%s".formatted(artemis.isVirtual(), artemis.getState()));
    }else{
      
      System.out.println("Mission control: all launched");
      apollo.run();
      gemini.run();
      mercury.run();
      artemis.run();
      
      System.out.println("...");
      System.out.println("Mission control: all landed");

      System.out.println("apollo: virtual=%s state=%s".formatted(apollo.isVirtual(), apollo.getState()));
      System.out.println("gemini: virtual=%s state=%s".formatted(gemini.isVirtual(), gemini.getState()));
      System.out.println("mercury: virtual=%s state=%s".formatted(mercury.isVirtual(), mercury.getState()));
      System.out.println("artemis: virtual=%s state=%s".formatted(artemis.isVirtual(), artemis.getState()));
    }

  }
}
