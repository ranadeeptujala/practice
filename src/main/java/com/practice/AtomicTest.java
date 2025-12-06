package com.practice;


public class AtomicTest {
    public static void main(String[] args) throws InterruptedException {
       // System.out.println("Running AtomicTest!");
        //AtomicInteger atomicInteger = new AtomicInteger(0);
        
       /* System.out.println("Initial value: " + atomicInteger.get());
        atomicInteger.set(10);
        System.out.println("Updated value: " + atomicInteger.get());
        atomicInteger.incrementAndGet();
        System.out.println("Incremented value: " + atomicInteger.get());
        atomicInteger.decrementAndGet();
        System.out.println("Decremented value: " + atomicInteger.get());
        atomicInteger.addAndGet(5);
        AtomicTest atomicTest = new AtomicTest();*/

        Thread vThread = Thread.ofVirtual().start(() -> {
            System.out.println("Running on virtual thread: " + Thread.currentThread());
        });
        
        vThread.join();  // Wait for virtual thread to finish!
    
    Thread v2ThThread=Thread.ofVirtual().start(()->{
        System.out.println("Running on virtual thread: " + Thread.currentThread()   );
    });
    v2ThThread.join();  // Wait for virtual thread to finish!
}

}