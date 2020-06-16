package com.yejianfengblue.java.concurrency.connection;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Test to thread-safe queue with delayed elements {@link DelayQueue}
 * @author yejianfengblue
 */
class DelayQueueTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    @RequiredArgsConstructor
    private static class Event implements Delayed {

        private final ZonedDateTime startDate;

        @Override
        public long getDelay(TimeUnit unit) {

            return unit.convert(
                    Duration.between(ZonedDateTime.now(), startDate));
        }

        @Override
        public int compareTo(Delayed o) {

            return Long.compare(this.getDelay(TimeUnit.NANOSECONDS),
                    o.getDelay(TimeUnit.NANOSECONDS));
        }
    }

    @RequiredArgsConstructor
    private static class Task implements Runnable {

        private final int threadId;

        private final DelayQueue<Event> queue;

        private Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public void run() {

            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime delay = now.plusSeconds(threadId);
            log.info("Thread [{}] delay = {}", threadId, delay);

            for (int i = 0; i < 100; i++) {
                queue.add(new Event(delay));
            }
        }
    }

    @Test
    void givenDelayQueue_whenPoll_thenOnlyElementsWithExpiredDelayAreReturned() throws InterruptedException {

        DelayQueue<Event> queue = new DelayQueue<>();

        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Task(i + 1, queue));
        }

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        do {

            int counter = 0;
            Event event;

            do {
                // don't use take() here, because take() will block if no elements with an expired delay
                event = queue.poll();
                if (null != event) counter++;
            } while (null != event);

            log.info("{} events are read", counter);
            TimeUnit.MILLISECONDS.sleep(500);
        } while (!queue.isEmpty());
    }
}
