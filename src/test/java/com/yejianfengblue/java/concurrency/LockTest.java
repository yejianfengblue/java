package com.yejianfengblue.java.concurrency;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Test to {@link Lock}  which a more powerful and flexible mechanism than {@code synchronized}. It has advantages:
 * <ul>
 *     <li>structure sync blocks more flexibly</li>
 *     <li>provides additional functionality, e.g., tryLock()</li>
 *     <li>ReadWriteLock allow a separation of read and write operations with multiple readers and only one modifier</li>
 *     <li>Lock interface offers better performance than {@code synchronized}</li>
 * </ul>
 * @author yejianfengblue
 */
class LockTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    private static class PrintQueue {

        private Lock queueLock;

        private Logger log = LoggerFactory.getLogger(getClass());

        PrintQueue(boolean fair) {
            queueLock = new ReentrantLock(fair);
        }

        void printJob(Object document) {

            queueLock.lock();
            try {
                long durationInSecond = (long)(Math.random()*10);
                log.info("Thread [{}] PrintQueue printing a document 1 during {}s START",
                        Thread.currentThread().getName(),
                        durationInSecond);
                TimeUnit.SECONDS.sleep(durationInSecond);
                log.info("Thread [{}] PrintQueue printing a document 1 during {}s END",
                        Thread.currentThread().getName(),
                        durationInSecond);
            } catch (InterruptedException e) {
                log.info("Error", e);
            } finally {
                queueLock.unlock();
            }

            queueLock.lock();
            try {
                long durationInSecond = (long)(Math.random()*10);
                log.info("Thread [{}] PrintQueue printing a document 2 during {}s START",
                        Thread.currentThread().getName(),
                        durationInSecond);
                TimeUnit.SECONDS.sleep(durationInSecond);
                log.info("Thread [{}] PrintQueue printing a document 2 during {}s END",
                        Thread.currentThread().getName(),
                        durationInSecond);
            } catch (InterruptedException e) {
                log.info("Error", e);
            } finally {
                queueLock.unlock();
            }
        }
    }

    @RequiredArgsConstructor
    private static class Job implements Runnable {

        private final PrintQueue printQueue;

        private Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public void run() {

            log.info("Thread [{}] print a document START", Thread.currentThread().getName());
            printQueue.printJob(new Object());
            log.info("Thread [{}] print a document END", Thread.currentThread().getName());
        }
    }

    @Test
    void givenReentrantLockNonFair_whenMultipleThreadsAccessSameResource_thenForEachThreadPrintPart2ExecutedRightAfterPart1() throws InterruptedException {

        log.info("Running with fair 'false'");

        PrintQueue printQueue = new PrintQueue(false);
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(new Job(printQueue), "Thread-" + i);
        }
        for (int i = 0; i < 10; i++) {
            threads[i].start();
        }
        for (int i = 0; i < 10; i++) {
            threads[i].join();
        }
    }

    @Test
    void givenReentrantLockFair_whenMultipleThreadsAccessSameResource_thenThreadsExecutedInOrderOfWaitLongestFirst() throws InterruptedException {

        log.info("Running with fair 'true'");

        PrintQueue printQueue = new PrintQueue(true);
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(new Job(printQueue), "Thread-" + i);
        }
        for (int i = 0; i < 10; i++) {
            threads[i].start();
        }
        for (int i = 0; i < 10; i++) {
            threads[i].join();
        }
    }

}
