package edu.itc.se.lab01.task4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class Main {
  Callable<Long> PrimeCounter(long from, long to) {
    return () -> {
      long count = 0;
      for (long n = Math.max(from, 2); n < to; n++) {
          boolean prime = true;
          for (long d = 2; d * d <= n; d++) if (n % d == 0) { prime = false; break; }
          if (prime) count++;
      }
      return count;
    };
  }

  Callable<String> FakeDownload(int id, String url) {
    
    return () -> {
      try{
        if (id == 10){
          Thread.sleep(5000);
        }else{
          Thread.sleep(200);
        }
        if (id % 50 == 0){
          throw new IOException("boom");
        }
        return "%s -> %d bytes".formatted(url, url.length());
      }catch (Exception e) {
        throw new RuntimeException(e);
      }
    };

  }

  public void main(String[] args) {
    int count_prime_chunk = 4_000_000/16;

    ExecutorService count_prime_executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    ArrayList<Callable<Long>> prime_count_task = new ArrayList<>();

    for (int i = 0; i < 16; i++){
      final int calculated_from = (i * count_prime_chunk) + 1;
      final int calculated_to = (i+1) * count_prime_chunk;
      // System.out.println("%d-%d".formatted(calculated_from, calculated_to));
      prime_count_task.add(PrimeCounter(calculated_from, calculated_to));
    }

    try{
      long start = System.nanoTime();
      List<Future<Long>> futures = count_prime_executors.invokeAll(prime_count_task);
      long total_prime_count = 0;
      for (Future<Long> future: futures){
        total_prime_count += future.get();
      }

      count_prime_executors.shutdown();

      long ms = (System.nanoTime() - start) / 1_000_000;
      System.out.println("pool: %d primes in %d ms".formatted(total_prime_count, ms));
    }catch (Exception e) {
      throw new RuntimeException(e);
    }


    // Fake Download

    
    ArrayList<Callable<String>> download_task = new ArrayList<>();
    

    for (int i = 0; i < 500; i++){
      download_task.add(FakeDownload(i+1,"https://example.com/%d".formatted(i)));
    }
    



    try{
      ExecutorService download_executors_1 = Executors.newFixedThreadPool(20);
      long start = System.nanoTime();
      ArrayList<Future<String>> futures = new ArrayList<>();
      for (Callable<String> task: download_task){
        futures.add(download_executors_1.submit(task));
      }

      int success = 0;
      int failed = 0;
      int timeout = 0;
      for (Future<String> future: futures){
        try{
          future.get(1, TimeUnit.SECONDS);
          success++;
        }catch (ExecutionException e){
          failed++;
        }catch (TimeoutException e){
          timeout++;
          System.out.println("There a download using fixed 20 threads timeout");
        }
      }
      System.out.println("Download using fixed 20 threads: %d ms".formatted((System.nanoTime() - start) / 1_000_000));
      System.out.println("Summary: success=%d timeout=%d failed=%d".formatted(success, timeout, failed));
      download_executors_1.shutdown();
    }catch (Exception e) {
      throw new RuntimeException(e);
    }

    try{
      ExecutorService download_executors_2 = Executors.newVirtualThreadPerTaskExecutor();
      long start = System.nanoTime();
      ArrayList<Future<String>> futures = new ArrayList<>();
      for (Callable<String> task: download_task){
        futures.add(download_executors_2.submit(task));
      }
      int success = 0;
      int failed = 0;
      int timeout = 0;
      for (Future<String> future: futures){
        try{
          future.get(1, TimeUnit.SECONDS);
          success++;
        }catch (ExecutionException e){
          failed++;
        }catch (TimeoutException e){
          timeout++;
          System.out.println("There a download using virtual threads per task timeout");
        }
      }
      System.out.println("Download using virtual threads per task: %d ms".formatted((System.nanoTime() - start) / 1_000_000));
      System.out.println("Summary: success=%d timeout=%d failed=%d".formatted(success, timeout, failed));

      download_executors_2.shutdown();
    }catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    


  }
}
