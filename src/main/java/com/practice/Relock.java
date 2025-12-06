package com.practice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class Relock {
    
    private static int counter = 0;
    private static final ReentrantLock lock = new ReentrantLock();
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== ReentrantLock Demo ===\n");
        
        // 1. Basic Lock Usage
        basicLockDemo();
        
        // 2. Multiple Threads with Lock
        multiThreadDemo();
        
        // 3. tryLock (non-blocking)
        tryLockDemo();
        
        // 4. Reentrant (same thread can lock multiple times)
        reentrantDemo();
        
        // 5. With Virtual Threads (better than synchronized!)
        virtualThreadDemo();
    }
    
    // 1. Basic Lock Usage
    static void basicLockDemo() {
        System.out.println("1. BASIC LOCK:");
        ReentrantLock myLock = new ReentrantLock();
        
        myLock.lock();  // Acquire lock
        try {
            System.out.println("   Critical section - only one thread here");
        } finally {
            myLock.unlock();  // ALWAYS unlock in finally!
        }
        System.out.println();
    }
    
    // 2. Multiple Threads - Protecting Shared Resource
    static void multiThreadDemo() throws InterruptedException {
        System.out.println("2. MULTIPLE THREADS:");
        counter = 0;
        
        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                lock.lock();
                try {
                    counter++;  // Safe increment
                } finally {
                    lock.unlock();
                }
            }
        };
        
        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        
        System.out.println("   Counter (expected 2000): " + counter);
        System.out.println();
    }
    
    // 3. tryLock - Non-blocking attempt
    static void tryLockDemo() throws InterruptedException {
        System.out.println("3. TRY LOCK (non-blocking):");
        ReentrantLock myLock = new ReentrantLock();
        
        // Thread 1: Holds the lock for 2 seconds
        Thread holder = new Thread(() -> {
            myLock.lock();
            System.out.println("   Thread-1: Got lock, holding for 2 sec...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                myLock.unlock();
                System.out.println("   Thread-1: Released lock");
            }
        });
        
        // Thread 2: Tries to get lock (non-blocking)
        Thread tryThread = new Thread(() -> {
            System.out.println("   Thread-2: Trying to get lock...");
            boolean acquired = myLock.tryLock();  // Returns FALSE immediately (no wait!)
            if (acquired) {
                try {
                    System.out.println("   Thread-2: Got the lock!");
                } finally {
                    myLock.unlock();
                }
            } else {
                System.out.println("   Thread-2: Lock is BUSY! Doing something else...");
            }
        });
        
        holder.start();
        Thread.sleep(100);  // Let holder grab lock first
        tryThread.start();
        
        holder.join();
        tryThread.join();
        System.out.println();
    }
    
    // 4. Reentrant - Same thread can lock multiple times
    static void reentrantDemo() {
        System.out.println("4. REENTRANT (same thread locks multiple times):");
        ReentrantLock myLock = new ReentrantLock();
        
        myLock.lock();  // Hold count: 1
        System.out.println("   Hold count: " + myLock.getHoldCount());
        
        myLock.lock();  // Hold count: 2 (same thread can lock again!)
        System.out.println("   Hold count: " + myLock.getHoldCount());
        
        myLock.unlock();  // Hold count: 1
        System.out.println("   Hold count: " + myLock.getHoldCount());
        
        myLock.unlock();  // Hold count: 0 (fully released)
        System.out.println("   Hold count: " + myLock.getHoldCount());
        System.out.println();
    }
    
    // 5. With Virtual Threads (allows unmounting, unlike synchronized)
    static void virtualThreadDemo() throws InterruptedException {
        System.out.println("5. WITH VIRTUAL THREADS:");
        ReentrantLock myLock = new ReentrantLock();
        
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        
        for (int i = 0; i < 3; i++) {
            final int id = i;
            executor.submit(() -> {
                myLock.lock();  // ✅ Allows VT to unmount while waiting!
                try {
                    System.out.println("   VT-" + id + " has lock on " + Thread.currentThread());
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    myLock.unlock();
                }
            });
        }
        
        Thread.sleep(500);
        executor.shutdown();
        System.out.println();
    }
}
