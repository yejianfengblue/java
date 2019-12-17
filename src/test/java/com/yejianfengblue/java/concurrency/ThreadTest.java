package com.yejianfengblue.java.concurrency;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Test to {@link Thread} with {@link Runnable}
 * @author yejianfengblue
 */
class ThreadTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    private static class Calculator implements Runnable {

        private Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public void run() {

            log.info("Thread ID {} [{}] - START",
                    Thread.currentThread().getId(),
                    Thread.currentThread().getName());
            long primeCount = IntStream.range(1, 1000000)
                    .filter(value -> BigInteger.valueOf(value).isProbablePrime(1000))
                    .count();
            log.info("Thead ID {} [{}] - END. Number of primes: {}",
                    Thread.currentThread().getId(),
                    Thread.currentThread().getName(),
                    primeCount);
        }
    }

    @Test
    void givenHighPriorityAndLowPriorityThreads_whenStartTogether_thenHighPriorityExecutedBeforeLowPriority() {

        Thread[] threads = new Thread[10];
        Thread.State[] states = new Thread.State[10];

        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(new Calculator());
            thread.setName("My Thread " + i);
            thread.setPriority( 0 == (i % 2) ? Thread.MAX_PRIORITY : Thread.MIN_PRIORITY);
            threads[i]= thread;
        }

        for (int i = 0; i < 10; i++) {

            log.info("State of Thread [{}] : {}", threads[i].getName(), threads[i].getState());
            states[i] = threads[i].getState();
        }

        for (int i = 0; i < 10; i++) {
            threads[i].start();  // create execution thread
        }

        boolean isFinished = false;
        while (!isFinished) {

            for (int i = 0; i < 10; i++) {
                Thread.State newState = threads[i].getState();
                if (newState != states[i]) {
                    log.info("ID {}, name {}, priority {}, old state {}, new state {}",
                            threads[i].getId(), threads[i].getName(), threads[i].getPriority(),
                            states[i], newState);
                    states[i] = newState;
                }
            }

            isFinished = true;
            for (int i = 0; i < 10; i++) {
                isFinished = (isFinished && (threads[i].getState() == Thread.State.TERMINATED));
            }
        }
    }

    private static class Sleep5sJob implements Runnable {

        private Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public void run() {
            log.info("Sleep START");
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("Sleep END");
        }
    }

    @Test
    void givenMainThreadContainsAnotherThread_whenCallAnotherThreadJoin_thenMainThreadWaitAnotherThreadToFinish() throws InterruptedException {

        Thread sleep5sThread1 = new Thread(new Sleep5sJob(), "sleep5s 1");
        Thread sleep5sThread2 = new Thread(new Sleep5sJob(), "sleep5s 2");

        sleep5sThread1.start();
        sleep5sThread2.start();

        sleep5sThread1.join();  // must call join(), otherwise once test process exits, these threads die
        sleep5sThread2.join();

        log.info("Two sleep5s are finished");
    }
}
