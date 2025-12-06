package com.practice;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class AtomicTest {
    public static void main(String[] args) {
        System.out.println("Running AtomicTest!");
        AtomicInteger atomicInteger = new AtomicInteger(0);
        
        System.out.println("Initial value: " + atomicInteger.get());
        atomicInteger.set(10);
        System.out.println("Updated value: " + atomicInteger.get());
        atomicInteger.incrementAndGet();
        System.out.println("Incremented value: " + atomicInteger.get());
        atomicInteger.decrementAndGet();
        System.out.println("Decremented value: " + atomicInteger.get());
        atomicInteger.addAndGet(5);
        AtomicTest atomicTest = new AtomicTest();
        
    }

    public void testAtoicBoolean(){
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        System.out.println("Initial value: " + atomicBoolean.get());
        atomicBoolean.set(true);
        System.out.println("Updated value: " + atomicBoolean.get());
        atomicBoolean.compareAndSet(true, false);
        System.out.println("Updated value: " + atomicBoolean.get());
        atomicBoolean.compareAndSet(false, true);
        System.out.println("Updated value: " + atomicBoolean.get());
    }
}

