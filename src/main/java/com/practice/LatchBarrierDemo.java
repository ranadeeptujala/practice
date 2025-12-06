package com.practice;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LatchBarrierDemo {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== CountDownLatch vs CyclicBarrier ===\n");
        
        // 1. CountDownLatch - Main waits for workers
        countDownLatchDemo();
        
        Thread.sleep(500);
        
        // 2. CyclicBarrier - All threads wait for each other
        cyclicBarrierDemo();
    }
    
    // CountDownLatch: One thread waits for others to finish
    static void countDownLatchDemo() throws InterruptedException {
        System.out.println("1. COUNTDOWNLATCH (main waits for workers):\n");
        
        CountDownLatch latch = new CountDownLatch(3);  // Wait for 3 events
        
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        
        // Workers do their job and count down
        for (int i = 1; i <= 3; i++) {
            final int id = i;
            executor.submit(() -> {
                System.out.println("   Worker-" + id + ": Working...");
                sleep(id * 300);
                System.out.println("   Worker-" + id + ": Done! (countDown)");
                latch.countDown();  // Signal completion
            });
        }
        
        System.out.println("   Main: Waiting for all workers...");
        latch.await();  // Main waits here
        System.out.println("   Main: All workers done! Proceeding...\n");
        
        executor.shutdown();
    }
    
    // CyclicBarrier: All threads wait for each other at a point
    static void cyclicBarrierDemo() throws Exception {
        System.out.println("2. CYCLICBARRIER (all wait for each other):\n");
        
        // Barrier for 3 threads + action when all arrive
        CyclicBarrier barrier = new CyclicBarrier(3, () -> {
            System.out.println("   >>> BARRIER: All arrived! Go! <<<\n");
        });
        
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        
        // Round 1
        System.out.println("   --- ROUND 1 ---");
        for (int i = 1; i <= 3; i++) {
            final int id = i;
            executor.submit(() -> {
                System.out.println("   Thread-" + id + ": Reached barrier (waiting...)");
                try {
                    barrier.await();  // Wait for others
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("   Thread-" + id + ": Passed barrier!");
            });
        }
        
        Thread.sleep(1000);
        
        // Round 2 - CyclicBarrier can be REUSED!
        System.out.println("   --- ROUND 2 (reused!) ---");
        for (int i = 1; i <= 3; i++) {
            final int id = i;
            executor.submit(() -> {
                sleep(id * 200);
                System.out.println("   Thread-" + id + ": Reached barrier again");
                try {
                    barrier.await();  // Reuse same barrier!
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("   Thread-" + id + ": Passed again!");
            });
        }
        
        Thread.sleep(1500);
        executor.shutdown();
    }
    
    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { 
            Thread.currentThread().interrupt(); 
        }
    }
}

