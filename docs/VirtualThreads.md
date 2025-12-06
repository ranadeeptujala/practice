# Virtual Threads in Java 21+

## Overview

Virtual threads are lightweight threads introduced in Java 21 (Project Loom). They enable high-throughput concurrent applications with minimal resource usage.

---

## Key Concepts

### Platform Threads vs Virtual Threads

| Feature | Platform Thread | Virtual Thread |
|---------|-----------------|----------------|
| Memory | ~1MB stack | ~few KB |
| Creation cost | Expensive | Cheap |
| Max count | ~thousands | **millions** |
| Managed by | OS | JVM |
| Best for | CPU-bound tasks | I/O-bound tasks |

### Carrier Threads

Carrier threads are **platform threads** that execute virtual threads.

```
┌─────────────────────────────────────────────────┐
│           ForkJoinPool (Carrier Pool)           │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐         │
│  │Carrier-1│  │Carrier-2│  │Carrier-3│  ...    │  ← Platform Threads
│  └────┬────┘  └────┬────┘  └────┬────┘         │    (~CPU cores)
│       │            │            │               │
│   ┌───┴───┐    ┌───┴───┐    ┌───┴───┐          │
│   ▼   ▼   ▼    ▼   ▼   ▼    ▼   ▼   ▼          │
│  VT1 VT2 VT3  VT4 VT5 VT6  VT7 VT8 VT9         │  ← Virtual Threads
│                                                 │    (millions possible)
└─────────────────────────────────────────────────┘
```

### Mount / Unmount

- **Mount**: Virtual thread starts running on a carrier
- **Unmount**: Virtual thread releases carrier when blocking (I/O, sleep, etc.)

```
VT-1 running on Carrier-1
         │
         ▼
VT-1 calls Thread.sleep(1000)
         │
         ▼
VT-1 UNMOUNTS (Carrier-1 is FREE!)
         │
         ▼
Carrier-1 picks up VT-2
         │
         ▼
Sleep completes → VT-1 MOUNTS on any free carrier
```

### What Triggers Unmount?

| Blocking Operation | Unmounts? |
|-------------------|-----------|
| `Thread.sleep()` | ✅ Yes |
| `InputStream.read()` | ✅ Yes |
| `Socket` operations | ✅ Yes |
| `HttpClient` calls | ✅ Yes |
| `BlockingQueue.take()` | ✅ Yes |
| `Lock.lock()` (ReentrantLock) | ✅ Yes |
| `synchronized` block | ❌ **NO (Pins!)** |
| CPU computation | ❌ No (not blocking) |

### Carrier Switching Demo

After unmount/remount, a virtual thread may run on a **different carrier**:

```java
public class CarrierDemo {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newCachedThreadPool(
            Thread.ofVirtual().name("vt-", 0).factory()
        );
        
        for (int i = 0; i < 5; i++) {
            final int id = i;
            executor.submit(() -> {
                // BEFORE sleep - on some carrier
                System.out.println("VT-" + id + " BEFORE: " + Thread.currentThread());
                
                Thread.sleep(100);  // UNMOUNTS here!
                
                // AFTER sleep - might be on DIFFERENT carrier!
                System.out.println("VT-" + id + " AFTER:  " + Thread.currentThread());
            });
        }
        
        Thread.sleep(1000);
        executor.shutdown();
    }
}
```

**Sample Output:**
```
VT-1 BEFORE: VirtualThread[#22,vt-1]/runnable@ForkJoinPool-1-worker-2
VT-1 AFTER:  VirtualThread[#22,vt-1]/runnable@ForkJoinPool-1-worker-6  ← CHANGED!
```

| Part | Stays Same? |
|------|-------------|
| Virtual Thread ID (`#22, vt-1`) | ✅ Always same |
| Carrier (`worker-X`) | ❌ Can change after unmount |

### ForkJoinPool is ALWAYS Used for Carriers

No matter which executor you use, the JVM **always** uses `ForkJoinPool` for carrier threads:

```
┌─────────────────────────────────────────────────────────┐
│                    YOUR EXECUTOR                         │
│   (CachedThreadPool / VirtualPerTask / Fixed)           │
│            Manages VIRTUAL THREADS                       │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                  ForkJoinPool (JVM)                      │
│            Manages CARRIER THREADS                       │
│      (This is ALWAYS used for virtual threads)          │
└─────────────────────────────────────────────────────────┘
```

| Executor Type | Carrier Pool | Carriers Can Change? |
|---------------|--------------|---------------------|
| `newVirtualThreadPerTaskExecutor()` | ForkJoinPool | ✅ Yes |
| `newCachedThreadPool(virtualFactory)` | ForkJoinPool | ✅ Yes |
| `newFixedThreadPool(n, virtualFactory)` | ForkJoinPool | ✅ Yes |

---

## Creating Virtual Threads

### Method 1: Direct Creation

```java
// Start a virtual thread directly
Thread vThread = Thread.ofVirtual()
    .name("my-vthread")
    .start(() -> {
        System.out.println("Running on: " + Thread.currentThread());
    });

vThread.join();
```

### Method 2: Virtual Thread Per Task Executor (Recommended)

```java
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

executor.submit(() -> {
    System.out.println("Task on: " + Thread.currentThread());
});

executor.shutdown();
```

### Method 3: Cached Pool with Virtual Factory

```java
ThreadFactory factory = Thread.ofVirtual()
    .name("vthread-", 0)  // vthread-0, vthread-1, etc.
    .factory();

ExecutorService executor = Executors.newCachedThreadPool(factory);
```

### Method 4: Fixed Pool with Virtual Factory

```java
// Limit concurrency (e.g., for rate limiting)
ExecutorService executor = Executors.newFixedThreadPool(10, 
    Thread.ofVirtual().factory());
```

---

## Executor Comparison

| Executor | Thread Reuse | Best For |
|----------|--------------|----------|
| `newVirtualThreadPerTaskExecutor()` | No (new each) | Most cases ✅ |
| `newCachedThreadPool(virtualFactory)` | Yes | Custom naming |
| `newFixedThreadPool(n, virtualFactory)` | Yes | Rate limiting |

---

## With CompletableFuture

```java
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

// Run tasks concurrently on virtual threads
CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
    // This runs on a virtual thread
    return fetchFromDatabase();
}, executor);  // ← Pass executor!

CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
    return callExternalAPI();
}, executor);

// Wait for both
CompletableFuture.allOf(future1, future2).join();
```

**Important:** Always pass the executor! Without it, `supplyAsync()` uses `ForkJoinPool.commonPool()` (platform threads).

### supplyAsync vs runAsync

| Feature | `supplyAsync` | `runAsync` |
|---------|---------------|------------|
| **Returns** | `CompletableFuture<T>` | `CompletableFuture<Void>` |
| **Has result?** | ✅ Yes | ❌ No |
| **Takes** | `Supplier<T>` | `Runnable` |
| **Use when** | You need a return value | Fire-and-forget task |

```java
// supplyAsync - RETURNS a value
CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
    return "Hello";  // Returns String
}, executor);
String result = future1.join();  // "Hello"

// runAsync - NO return value
CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
    System.out.println("Just doing work...");  // No return
}, executor);
future2.join();  // Returns null
```

| Scenario | Use |
|----------|-----|
| Fetch data from DB | `supplyAsync` |
| Call API and get response | `supplyAsync` |
| Send email notification | `runAsync` |
| Write to log file | `runAsync` |

---

## Best Practices

### ✅ Do

```java
// Use for I/O-bound tasks
executor.submit(() -> {
    httpClient.get(url);      // Network I/O
    database.query(sql);      // Database I/O
    Files.readAllBytes(path); // File I/O
});

// Use try-with-resources for auto-shutdown
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(task1);
    executor.submit(task2);
}  // Auto-closes and waits
```

### ❌ Don't

```java
// Don't use for CPU-bound tasks
executor.submit(() -> {
    // Heavy computation - use platform threads instead
    calculatePrimes(1_000_000);
});

// Don't use synchronized blocks (pins carrier thread)
synchronized (lock) {  // ❌ Pins virtual thread to carrier
    // work
}

// Use ReentrantLock instead
lock.lock();  // ✅ Allows unmounting
try {
    // work
} finally {
    lock.unlock();
}
```

---

## Pinning (What to Avoid)

**Pinning** = Virtual thread cannot unmount from carrier.

### Causes of Pinning:

1. **synchronized blocks/methods**
2. **Native methods (JNI)**

### Detect Pinning:

```bash
# Run with JVM flag to see pinning
java -Djdk.tracePinnedThreads=full MyApp
```

### Fix Pinning:

```java
// ❌ BAD - causes pinning
synchronized (lock) {
    doBlockingIO();
}

// ✅ GOOD - no pinning
ReentrantLock lock = new ReentrantLock();
lock.lock();
try {
    doBlockingIO();
} finally {
    lock.unlock();
}
```

---

## Performance Example

```java
public static void main(String[] args) {
    long start = System.currentTimeMillis();
    
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
        for (int i = 0; i < 100_000; i++) {
            executor.submit(() -> {
                Thread.sleep(1000);  // Simulates I/O
                return "done";
            });
        }
    }
    
    long duration = System.currentTimeMillis() - start;
    System.out.println("100,000 tasks in: " + duration + "ms");
    // ~1-2 seconds with virtual threads
    // Would take ~100,000 seconds with platform threads!
}
```

---

## Summary

| Concept | Description |
|---------|-------------|
| Virtual Thread | Lightweight thread managed by JVM |
| Carrier Thread | Platform thread that runs virtual threads |
| Mount | VT starts on carrier |
| Unmount | VT releases carrier when blocking |
| Pinning | VT stuck on carrier (avoid!) |

### When to Use Virtual Threads:

- ✅ High-concurrency I/O (web servers, APIs)
- ✅ Database connections
- ✅ File operations
- ❌ CPU-intensive computation (use platform threads)

---

## Quick Reference

```java
// Create virtual thread
Thread.ofVirtual().start(() -> { });

// Virtual thread executor
Executors.newVirtualThreadPerTaskExecutor();

// Custom factory
Thread.ofVirtual().name("prefix-", 0).factory();

// Check if virtual
Thread.currentThread().isVirtual();
```

