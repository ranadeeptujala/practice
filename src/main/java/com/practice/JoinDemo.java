package com.practice;

public class JoinDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Thread Join Demo ===\n");
        
        // Example 1: t2 waits for t1
        System.out.println("1. t2 waits for t1:");
        
        Thread t1 = new Thread(() -> {
            System.out.println("   t1: Starting work...");
            sleep(1000);
            System.out.println("   t1: Finished!");
        });
        
        Thread t2 = new Thread(() -> {
            System.out.println("   t2: Waiting for t1 to finish...");
            try {
                t1.join();  // t2 waits for t1!
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("   t2: t1 is done, now I continue!");
        });
        
        t2.start();  // t2 starts first
        t1.start();  // t1 starts
        
        t1.join();
        t2.join();
        
        // Example 2: Chain - t3 waits for t2, t2 waits for t1
        System.out.println("\n2. Chain: t3 → t2 → t1:");
        
        Thread c1 = new Thread(() -> {
            System.out.println("   c1: Working...");
            sleep(500);
            System.out.println("   c1: Done!");
        });
        
        Thread c2 = new Thread(() -> {
            try {
                c1.join();  // c2 waits for c1
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("   c2: c1 finished, now I work...");
            sleep(500);
            System.out.println("   c2: Done!");
        });
        
        Thread c3 = new Thread(() -> {
            try {
                c2.join();  // c3 waits for c2
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("   c3: c2 finished, now I work...");
            System.out.println("   c3: Done!");
        });
        
        // Start in any order - join() handles the sequence!
        c3.start();
        c2.start();
        c1.start();
        
        c3.join();  // Main waits for c3 (which waits for c2, which waits for c1)
        
        System.out.println("\nAll done!");
    }
    
    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

