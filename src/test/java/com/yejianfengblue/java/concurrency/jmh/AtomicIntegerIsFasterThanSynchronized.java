package com.yejianfengblue.java.concurrency.jmh;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Slf4j
public class AtomicIntegerIsFasterThanSynchronized {

    private static final int ITERATIONS = 1_000_000;

    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    private static final int THREAD_TOTAL = THREAD_POOL_SIZE * 10;

    /**
     * A benchmark state with scope {@link Scope#Benchmark} to setup the thread pool,
     * which should be shared by both AtomicInteger test and synchronized int test.
     */
    @State(Scope.Thread)
    public static class SharedThreadState {

        ExecutorService executorService;

        @Setup(Level.Iteration)
        public void setup() {
            executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            executorService.shutdownNow();
        }
    }

    /**
     * A per-iteration benchmark state to initialize the {@link AtomicInteger} and validate its value after each iteration.
     */
    @State(Scope.Thread)
    public static class AtomicInt {

        AtomicInteger atomicInt;

        @Setup(Level.Iteration)
        public void init() {
            atomicInt = new AtomicInteger(0);
        }

        @TearDown(Level.Iteration)
        public void validate() {
            assertEquals(ITERATIONS * THREAD_TOTAL,
                    atomicInt.get(),
                    String.format("The value of atomic integer should be %d. Current value is %d",
                            ITERATIONS * THREAD_TOTAL,
                            atomicInt.get()));
        }
    }

    /**
     * A benchmark to measure the performance of the increment operation of an {@link AtomicInteger}, which is shared by
     * multiple threads and each thread attempts to increment this AtomicInteger many times.
     */
    @Benchmark
    public void useAtomicIntegerToIncrementInteger(SharedThreadState sharedThreadState, AtomicInt atomicInt) throws ExecutionException, InterruptedException {

        for (int i = THREAD_TOTAL; i > 0; i--) {

            sharedThreadState.executorService.submit(new Runnable() {

                @Override
                public void run() {
                    for (int i = ITERATIONS; i > 0; i--) {
                        atomicInt.atomicInt.incrementAndGet();
                    }
                }
            });
        }

        // wait for threads to complete
        sharedThreadState.executorService.shutdown();
        sharedThreadState.executorService.awaitTermination(1, TimeUnit.MINUTES);
    }

/////////////////////////////////// Test synchronized

    /**
     * A per-iteration benchmark state to initialize the primitive {@code int} and validate its value after each iteration.
     */
    @State(Scope.Thread)
    public static class SyncInt {

        /**
         * A lock used by the keyword {@code synchronized}
         */
        volatile Object lock;

        volatile int primitiveInteger;

        @Setup(Level.Iteration)
        public void init() {
            lock = new Object();
            primitiveInteger = 0;
        }

        @TearDown(Level.Iteration)
        public void validate() {

            assertEquals(ITERATIONS * THREAD_TOTAL,
                    primitiveInteger,
                    String.format("The value of primitive integer should be %d. Current value is %d",
                            ITERATIONS * THREAD_TOTAL,
                            primitiveInteger));

        }

    }

    /**
     * A benchmark to measure the performance of the increment operation of a primitive {@code int}, which is shared by
     * multiple threads and each thread attempts to increment this {@code int} within a {@code synchronized} block many
     * times.
     */
    @Benchmark
    public void useSynchronizedToIncrementInteger(SharedThreadState sharedThreadState, SyncInt syncInt) throws ExecutionException, InterruptedException {

        for (int i = THREAD_TOTAL; i > 0; i--) {

            sharedThreadState.executorService.submit(new Runnable() {
                @Override
                public void run() {
                    for (int i = ITERATIONS; i > 0; i--) {
                        synchronized (syncInt.lock) {
                            syncInt.primitiveInteger++;
                        }
                    }
                }
            });
        }

        sharedThreadState.executorService.shutdown();
        sharedThreadState.executorService.awaitTermination(1, TimeUnit.MINUTES);
    }

///////////////////////////// Test synchronized

    /**
     * A per-iteration benchmark state to initialize the primitive {@code int}.
     */
    @State(Scope.Thread)
    public static class NonSyncInt {

        volatile int primitiveInteger;

        @Setup(Level.Iteration)
        public void init() {
            primitiveInteger = 0;
        }
    }

    /**
     * A benchmark to measure the performance of the increment operation of a primitive {@code int}, which is shared by
     * multiple threads and each thread attempts to increment this {@code int} without a {@code synchronized} block many
     * times.
     */
    @Benchmark
    public void notUseSynchronizedToIncrementInteger(SharedThreadState sharedThreadState, NonSyncInt nonSyncInt) throws ExecutionException, InterruptedException {

        for (int i = THREAD_TOTAL; i > 0; i--) {

            sharedThreadState.executorService.submit(new Runnable() {
                @Override
                public void run() {
                    for (int i = ITERATIONS; i > 0; i--) {
                        nonSyncInt.primitiveInteger++;
                    }
                }
            });
        }

        sharedThreadState.executorService.shutdown();
        sharedThreadState.executorService.awaitTermination(1, TimeUnit.MINUTES);

        log.info("Non sync int = {}", nonSyncInt.primitiveInteger);
    }

    @Test
    @Disabled("JMH is supposed to be run manually coz it's slow.")
    public void runJmh() throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(AtomicIntegerIsFasterThanSynchronized.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .mode(Mode.AverageTime)
                .build();

        new Runner(opt).run();
    }

}