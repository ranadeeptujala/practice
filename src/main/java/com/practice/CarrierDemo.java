package com.practice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CarrierDemo {
    public static void main(String[] args) throws Exception {
        // Using CACHED thread pool with virtual factory
        ExecutorService executor = Executors.newCachedThreadPool(
            Thread.ofVirtual().name("vt-", 0).factory()
        );
        
        // Submit multiple tasks to see carrier switching
        for (int i = 0; i < 5; i++) {
            final int id = i;
            executor.submit(() -> {
                // BEFORE sleep - on some carrier
                System.out.println("VT-" + id + " BEFORE sleep: " + Thread.currentThread());
                
                try {
                    Thread.sleep(100);  // UNMOUNTS here!
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // AFTER sleep - might be on DIFFERENT carrier!
                System.out.println("VT-" + id + " AFTER  sleep: " + Thread.currentThread());
                
                System.out.println();
            });
        }
        
        Thread.sleep(1000);
        executor.shutdown();
    }
}

