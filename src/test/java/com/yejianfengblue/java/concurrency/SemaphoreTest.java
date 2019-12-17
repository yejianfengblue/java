package com.yejianfengblue.java.concurrency;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Test to {@link Semaphore} via a print queue that could print doc in three diff printers
 * <ol>
 * <li>acquire the semaphore with acquire()
 * <li>operate with shared resource
 * <li>release the semaphore with release()
 * </ol>
 * @author yejianfengblue
 */
class SemaphoreTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    private static class PrintQueue {

        private final Semaphore semaphore;

        private final boolean[] freePrinters;

        private final Lock printersLock;

        private Logger log = LoggerFactory.getLogger(getClass());

        private PrintQueue() {

            semaphore = new Semaphore(3);
            freePrinters = new boolean[]{ true, true, true };
            printersLock = new ReentrantLock();
        }

        /**
         * Simulate the printing of a document by sleep 3-10 seconds.
         * <ol>
         * <li>acquire the semaphore with acquire()
         * <li>operate with shared resource
         * <li>release the semaphore with release()
         * </ol>
         */
        void printJob(Object document) {

            try {
                semaphore.acquire();  // acquire access to the semaphore
                int assignedPrinter = findFreePrinter();  // the index of free printer
                int duration = ThreadLocalRandom.current().nextInt(3, 11);
                log.info("Thread [{}] is printing a Job in Printer {} during {}s",
                        Thread.currentThread().getName(),
                        assignedPrinter,
                        duration);
                TimeUnit.SECONDS.sleep(duration);
                freePrinters[assignedPrinter] = true;  // mark the printer as free
                log.info("Thread [{}] has printed a Job in Printer {}",
                        Thread.currentThread().getName(),
                        assignedPrinter);
            } catch (InterruptedException e) {
                log.error("Error", e);
            } finally {
                semaphore.release();  // release the semaphore
            }

        }

        /**
         * Use a {@link ReentrantLock} to find free printer
         * @return the printer index in array {@link #freePrinters}
         */
        private int findFreePrinter() {

            int occupiedPrinterIndex = -1;

            try {
                printersLock.lock();
                for (int i = 0; i < freePrinters.length; i++) {
                    if (freePrinters[i]) {
                        occupiedPrinterIndex = i;
                        freePrinters[i] = false;  // mark one this printer will be occupied
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Error", e);
            } finally {
                printersLock.unlock();
            }

            return occupiedPrinterIndex;
        }
    }

    private static class Job implements Runnable {

        private PrintQueue printQueue;

        private Logger log = LoggerFactory.getLogger(getClass());

        private Job(PrintQueue printQueue) {
            this.printQueue = printQueue;
        }

        @Override
        public void run() {

            log.info("Thread [{}] is going to print a document", Thread.currentThread().getName());
            printQueue.printJob(new Object());
            log.info("Thread [{}] has printed a document", Thread.currentThread().getName());
        }
    }

    /**
     * The first 3 threads calling acquire() get printed first, while the rest will be blocked.
     * When a thread finishes and releases the semaphore, another waiting thread will acquire it
     */
    @Test
    void test() throws InterruptedException {

        PrintQueue printQueue = new PrintQueue();

        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Job(printQueue), "Thread"+i);
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }
}
