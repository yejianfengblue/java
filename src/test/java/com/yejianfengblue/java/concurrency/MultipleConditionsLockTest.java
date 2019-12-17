package com.yejianfengblue.java.concurrency;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Test to {@link Lock} and {@link Condition}
 * @author yejianfengblue
 */
class MultipleConditionsLockTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * A mocked file to be read by {@link Producer}
     */
    private static class FileMock {

        private String[] content;

        private int index;  // the start index to be retrieved

        private Logger log = LoggerFactory.getLogger(getClass());

        /**
         * Initial and populate with random char
         * @param size  number of line
         * @param length  line length
         */
        FileMock(int size, int length) {
            content = new String[size];
            for (int i = 0; i < size; i++) {
                StringBuilder buffer = new StringBuilder(length);
                for (int j = 0; j < length; j++) {
                    int randomChar = (int) (Math.random() * 255);
                    buffer.append((char)randomChar);
                }
                content[i] = buffer.toString();
            }
            index = 0;
        }
        boolean hasMoreLines() {
            return index < content.length;
        }

        /** Must call {@link #hasMoreLines()} before call this method */
        String getOneLine() {
            if (hasMoreLines()) {
                log.info("FileMock: remain {}", content.length-index);
                return content[index++];
            } else {
                return null;
            }
        }
    }

    /**
     * A buffer shared by both the producers and consumers
     */
    private static class Buffer {

        /** store the shared data */
        private final LinkedList<String> buffer;

        /** control access to the blocks of code that modify the buffer */
        private final int maxSize;

        private final Lock lock;

        private final Condition lines;

        private final Condition space;

        /** indicate whether there are lines in the buffer */
        private boolean pendingLines;

        private Logger log = LoggerFactory.getLogger(getClass());

        Buffer(int maxSize) {
            this.maxSize = maxSize;
            buffer = new LinkedList<>();
            lock = new ReentrantLock();
            lines = lock.newCondition();
            space = lock.newCondition();
            pendingLines = true;
        }

        void insert(String line) {

            lock.lock();

            try {
                while (buffer.size() == maxSize) {
                    // wait for free space, the thread will be woken up when another thread calls the signal()
                    // or signalAll() method in the 'space' condition
                    space.await();
                }
                buffer.offer(line);
                log.info("Thread [{}] inserted line {}",
                        Thread.currentThread().getName(),
                        buffer.size());
                // wake up all the threads that are waiting for lines in the buffer
                lines.signalAll();
            } catch (InterruptedException e) {
                log.error("Error", e);
            } finally {
                lock.unlock();
            }
        }

        String get() {

            String line = null;
            lock.lock();
            try {

                while ((0 == buffer.size()) && hasPendingLines()) {
                    lines.await();  // wait for a lines in the buffer
                }

                if (hasPendingLines()) {
                    line = buffer.poll();
                    log.info("Thread [{}] - Line read = {}",
                            Thread.currentThread().getName(),
                            buffer.size());
                    space.signalAll();
                }
            } catch (InterruptedException e) {
                log.error("Error", e);
            } finally {
                lock.unlock();
            }
            return line;
        }

        /** If true, then some producer is inserting lines into this buffer;
         * If false, no more lines will be inserted
         */
        synchronized void setPendingLines(boolean pendingLines) {
            this.pendingLines = pendingLines;
        }

        synchronized boolean hasPendingLines() {
            return pendingLines || buffer.size() > 0;
        }
    }

    /**
     * Read from {@link FileMock} and add to {@link Buffer}
     */
    private static class Producer implements Runnable {

        private FileMock fileMock;

        private Buffer buffer;

        private Logger log = LoggerFactory.getLogger(getClass());

        Producer(FileMock fileMock, Buffer buffer) {
            this.fileMock = fileMock;
            this.buffer = buffer;
        }

        @Override
        public void run() {

            buffer.setPendingLines(true);  // tell Buffer I'm waiting
            while (fileMock.hasMoreLines()) {
                String line = fileMock.getOneLine();
                buffer.insert(line);
            }
            buffer.setPendingLines(false);  // say bye to buffer
        }
    }

    /**
     * Read from {@link Buffer}
     */
    private static class Consumer implements Runnable {

        private Buffer buffer;

        private Logger log = LoggerFactory.getLogger(getClass());

        Consumer(Buffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public void run() {
            while (buffer.hasPendingLines()) {
                String line = buffer.get();

                // pretend processing the line
                try {
                    TimeUnit.MILLISECONDS.sleep((new Random()).nextInt(100));
                } catch (InterruptedException e) {
                    log.error("Error", e);
                }
            }
        }
    }

    @Test
    void test() throws InterruptedException {

        // a mocked file containing 100 lines, each line has 10 random char
        FileMock fileMock = new FileMock(100, 10);

        Buffer buffer = new Buffer(20);

        Thread producerThread = new Thread(new Producer(fileMock, buffer), "Producer");

        Thread[] consumerThreads = new Thread[3];
        for (int i = 0; i < 3; i++) {
            consumerThreads[i] = new Thread(new Consumer(buffer), "Consumer "+i);
        }

        producerThread.start();
        for (int i = 0; i < 3; i++) {
            consumerThreads[i].start();
        }

        producerThread.join();
        for (int i = 0; i < 3; i++) {
            consumerThreads[i].join();
        }
    }
}
