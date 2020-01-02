package com.yejianfengblue.java.concurrency;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.PriorityBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test to blocking priority queue {@link java.util.concurrent.PriorityBlockingQueue}
 * @author yejianfengblue
 */
class PriorityBlockingQueueTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    @RequiredArgsConstructor
    @Getter
    @ToString
    private static class Event implements Comparable<Event> {

        private final int threadId;

        private final int priority;

        @Override
        public int compareTo(Event o) {

            return Integer.compare(this.priority, o.priority);
        }
    }

    @RequiredArgsConstructor
    private static class Task implements Runnable {

        private final int threadId;

        private final PriorityBlockingQueue<Event> queue;

        @Override
        public void run() {

            for (int i = 0; i < 1000; i++) {
                queue.put(new Event(threadId, i));
            }
        }
    }

    @Test
    void givenPriorityBlockingQueueAndMultipleThreadsPutElementWithDiffPriorityValue_whenTake_thenElementIsReturnedAccordingToPriority() throws InterruptedException {

        PriorityBlockingQueue<Event> queue = new PriorityBlockingQueue<>();

        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Task(i, queue));
        }

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(5000, queue.size());

        for (int i = 0; i < 1000; i++) {
            assertEquals(i, queue.take().getPriority());
            assertEquals(i, queue.take().getPriority());
            assertEquals(i, queue.take().getPriority());
            assertEquals(i, queue.take().getPriority());
            assertEquals(i, queue.take().getPriority());
        }

        assertTrue(queue.isEmpty());
    }
}
