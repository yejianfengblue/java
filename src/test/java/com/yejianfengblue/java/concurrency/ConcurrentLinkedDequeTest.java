package com.yejianfengblue.java.concurrency;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test to non-blocking deque {@link java.util.concurrent.ConcurrentLinkedDeque}
 * by 100 threads each adding 10000 elements and 100 threads each polling 10000 elements
 *
 * @author yejianfengblue
 */
class ConcurrentLinkedDequeTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    /** Offer (add without throwing exception) 10000 elements to the provided {@link ConcurrentLinkedDeque}*/
    @RequiredArgsConstructor
    private static class AddTask implements Runnable {

        private final ConcurrentLinkedDeque<String> list;

        private Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public void run() {

            String threadName = Thread.currentThread().getName();
            IntStream.range(0, 10000)
                    .forEachOrdered(value ->
                            list.offerLast(threadName + "-element-" + value)
                    );
        }
    }

    /** Poll the first and last element from the provided {@link ConcurrentLinkedDeque} for 5000 times */
    @RequiredArgsConstructor
    private static class PollTask implements Runnable {

        private final ConcurrentLinkedDeque<String> list;

        private Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public void run() {

            for (int counter = 0; counter < 5000; counter++) {
                list.pollFirst();  // Retrieves and removes the first element of this deque, or returns null if this deque is empty
                list.pollLast();  // Retrieves and removes the last element of this deque, or returns null if this deque is empty
            }
        }
    }

    @Test
    void givenConcurrentLinkedDeque_whenMultipleThreadsAddElements_thenDataIsConsistent() throws InterruptedException {

        ConcurrentLinkedDeque<String> list = new ConcurrentLinkedDeque<>();

        log.info("Create 100 AddTask");
        Thread[] addTaskThreads = new Thread[100];
        for (int i = 0; i < addTaskThreads.length; i++) {
            addTaskThreads[i] = new Thread(new AddTask(list));
        }

        log.info("Start 100 AddTask");
        for (Thread addTaskThread : addTaskThreads) {
            addTaskThread.start();
        }

        log.info("Waiting for the completion of 100 AddTask");
        for (Thread addTaskThread : addTaskThreads) {
            addTaskThread.join();
        }

        log.info("List size = {}", list.size());
        assertEquals(1000000, list.size());
    }

    @Test
    void givenConcurrentLinkedDeque_whenMultipleThreadsPollElements_thenDataIsConsistent() throws InterruptedException {

        ConcurrentLinkedDeque<String> list = new ConcurrentLinkedDeque<>();

        log.info("Create 100 AddTask");
        Thread[] addTaskThreads = new Thread[100];
        for (int i = 0; i < addTaskThreads.length; i++) {
            addTaskThreads[i] = new Thread(new AddTask(list));
        }

        log.info("Start 100 AddTask");
        for (Thread addTaskThread : addTaskThreads) {
            addTaskThread.start();
        }

        log.info("Waiting for the completion of 100 AddTask");
        for (Thread addTaskThread : addTaskThreads) {
            addTaskThread.join();
        }

        log.info("List size = {}", list.size());
        assertEquals(1000000, list.size());

        log.info("Create 100 PollTask");
        Thread[] pollTaskThreads = new Thread[100];
        for (int i = 0; i < pollTaskThreads.length; i++) {
            pollTaskThreads[i] = new Thread(new PollTask(list));
        }

        log.info("Start 100 PollTask");
        for (Thread pollTaskThread : pollTaskThreads) {
            pollTaskThread.start();
        }

        log.info("Waiting for the completion of 100 PollTask");
        for (Thread pollTaskThread : pollTaskThreads) {
            pollTaskThread.join();
        }

        log.info("List size = {}", list.size());
        assertTrue(list.isEmpty());
    }
}
