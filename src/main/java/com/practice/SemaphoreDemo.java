package com.practice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class SemaphoreDemo {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Semaphore vs ReentrantLock ===\n");
        
        // 1. ReentrantLock - Only 1 thread at a time
        System.out.println("1. REENTRANTLOCK (1 thread at a time):");
        reentrantLockDemo();
        
        Thread.sleep(500);
        
        // 2. Semaphore - Multiple threads at a time
        System.out.println("\n2. SEMAPHORE (3 threads at a time):");
        semaphoreDemo();
    }
    
    static void reentrantLockDemo() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        
        for (int i = 0; i < 5; i++) {
            final int id = i;
            executor.submit(() -> {
                lock.lock();
                try {
                    System.out.println("   Thread-" + id + " ENTERED (alone)");
                    Thread.sleep(200);
                    System.out.println("   Thread-" + id + " EXITED");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
            });
        }
        
        Thread.sleep(1500);
        executor.shutdown();
    }
    
    static void semaphoreDemo() throws InterruptedException {
        Semaphore semaphore = new Semaphore(3);  // Allow 3 concurrent!
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        
        for (int i = 0; i < 6; i++) {
            final int id = i;
            executor.submit(() -> {
                try {
                    semaphore.acquire();  // Get permit
                    System.out.println("   Thread-" + id + " ENTERED (permits left: " 
                        + semaphore.availablePermits() + ")");
                    Thread.sleep(500);
                    System.out.println("   Thread-" + id + " EXITED");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    semaphore.release();  // Return permit
                }
            });
        }
        
        Thread.sleep(2000);
        executor.shutdown();
    }
}

