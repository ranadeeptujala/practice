package com.practice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class VirtualThreadTest {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Virtual Threads Demo ===\n");
        
        // Option 1: Virtual Thread Factory
        ThreadFactory virtualFactory = Thread.ofVirtual()
                .name("virtual-", 0)  // Names: virtual-0, virtual-1, etc.
                .factory();
        
        ExecutorService executor1 = Executors.newCachedThreadPool(virtualFactory);
        
        // Option 2: Built-in Virtual Thread Executor (simpler)
        ExecutorService executor2 = Executors.newVirtualThreadPerTaskExecutor();
        
        System.out.println("Using Virtual Thread Factory:");
        for (int i = 0; i < 5; i++) {
            final int taskId = i;
            executor1.submit(() -> {
                System.out.println("Task " + taskId + " on: " + Thread.currentThread());
                sleep(100);
            });
        }
        
        Thread.sleep(500);
        System.out.println("\nUsing newVirtualThreadPerTaskExecutor:");
        
        for (int i = 0; i < 5; i++) {
            final int taskId = i;
            executor2.submit(() -> {
                System.out.println("Task " + taskId + " on: " + Thread.currentThread());
                sleep(100);
            });
        }
        
        Thread.sleep(500);
        
        // Demonstrate massive concurrency (virtual threads are lightweight!)
        System.out.println("\n=== Spawning 10,000 virtual threads ===");
        long start = System.currentTimeMillis();
        
        try (ExecutorService massiveExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 10_000; i++) {
                massiveExecutor.submit(() -> {
                    sleep(100);  // Each "blocks" for 100ms
                });
            }
        }  // Auto-closes and waits for all tasks
        
        long duration = System.currentTimeMillis() - start;
        System.out.println("10,000 tasks completed in: " + duration + "ms");
        System.out.println("(Would take ~1000 seconds with platform threads!)");
        
        executor1.shutdown();
        executor2.shutdown();
    }
    
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

