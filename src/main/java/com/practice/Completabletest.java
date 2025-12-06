package com.practice;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Completabletest {
    public static void main(String[] args) {
        System.out.println("Main thread: " + Thread.currentThread().getName());
     ExecutorService executor = Executors.newCachedThreadPool(Thread.ofVirtual().factory());
     ExecutorService executor1 = Executors.newVirtualThreadPerTaskExecutor();
        CompletableFuture<String> userFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("Fetching user on: " + Thread.currentThread().getName());
            sleep(1000);
            return "User: John";
        }, executor);
        
        // Task 2: Fetch order data (simulated)
        CompletableFuture<String> orderFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("Fetching order on: " + Thread.currentThread().getName());
            sleep(1500);
            return "Order: #12345";
        }, executor);
        
        // Task 3: Fetch payment data (simulated)
        CompletableFuture<String> paymentFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("Fetching payment on: " + Thread.currentThread().getName());
            sleep(800);
            return "Payment: $99.99";
        }, executor);
        
        // Wait for ALL tasks to complete concurrently
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            userFuture, orderFuture, paymentFuture
        );
        
        // Combine results after all complete
        CompletableFuture<String> combinedFuture = allFutures.thenApply(v -> {
            String user = userFuture.join();
            String order = orderFuture.join();
            String payment = paymentFuture.join();
            return user + " | " + order + " | " + payment;
        });
        
        // Get final result
        String result = combinedFuture.join();
        System.out.println("\n=== Combined Result ===");
        System.out.println(result);
        
        executor.shutdown();
        System.out.println("\nAll tasks completed!");
    }
    
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
