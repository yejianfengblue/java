package com.yejianfengblue.java.concurrency;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Test to {@code synchronized} with {@link Object#wait()} and {@link Object#notify()} via a producer-buffer-consumer problem:
 * <p>
 *     Consumer thread checks any item in the buffer.
 *     If any, then fetch and consume;
 *     if none, then it sleeps (by wait()) until it's awakened by notify().
 * </p>
 * <p>
 *     Producer thread checks whether the buffer is full.
 *     If not full, then put item;
 *     if full, then it sleeps (by wait()) until it's awakened by notify().
 * </p>
 * <p>
 *     When an execution thread calls wait(), JVM puts this thread to sleep and releases the own object of the
 *     {@code synchronized} block and allows other threads to execute other blocks of {@code synchronized} block
 *     protected by this own object.
 * </p>
 * @author yejianfengblue
 */
class ConditionsInSynchronizedCodeTest {

    private static class EventStorage {

        private Queue<LocalDateTime> storage = new LinkedList<>();

        private Logger log = LoggerFactory.getLogger(getClass());

        /**
         * If buffer is not full, then put item.
         * If buffer is full, then wait()
         */
        synchronized void set() {

            log.info("Set IN");
            while (storage.size() == 10) {
                try {
                    log.info("Set wait");
                    wait();  // cause the current thread to wait until it's awakened by notify()
                    log.info("Set after wait");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            LocalDateTime now = LocalDateTime.now();
            storage.offer(now);
            log.info("Set - {} - {}", storage.size(), now);
            notify();
            log.info("Set OUT");
        }

        synchronized void get() {

            log.info("Get IN");
            while (storage.size() == 0) {
                try {
                    log.info("Get wait");
                    wait();
                    log.info("Get after wait");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            LocalDateTime elem = storage.poll();
            log.info("Get - {} - {}", storage.size(), elem);
            notify();
            log.info("Get OUT");
        }
    }

    @AllArgsConstructor
    private static class Producer implements Runnable {

        private EventStorage eventStorage;

        @Override
        public void run() {

            for (int i = 0; i < 100; i++) {
                eventStorage.set();
            }
        }
    }

    @AllArgsConstructor
    private static class Consumer implements Runnable {

        private EventStorage eventStorage;

        @Override
        public void run() {

            for (int i = 0; i < 100; i++) {
                eventStorage.get();
            }
        }
    }

    @Test
    void givenConditionInSyncBlock_whenConditionNotMet_thenCallWait_whenAnotherThreadCallNotify_thenOriginalWaitingThreadWakeUpAndCheckTheConditionAgain_whenConditionMet_thenExecute() throws InterruptedException {

        EventStorage eventStorage = new EventStorage();

        Producer producer = new Producer(eventStorage);
        Thread producerThread = new Thread(producer);

        Consumer consumer = new Consumer(eventStorage);
        Thread consumerThread = new Thread(consumer);

        producerThread.start();
        consumerThread.start();

        producerThread.join();
        consumerThread.join();
    }
}
