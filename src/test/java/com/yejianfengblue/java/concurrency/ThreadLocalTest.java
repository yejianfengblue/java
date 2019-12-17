package com.yejianfengblue.java.concurrency;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Test to {@link ThreadLocal} to make field in shared object to be thread-safe, by the way each thread hold one copy of field
 *
 * @author yejianfengblue
 */
class ThreadLocalTest {

    private static class ThreadUnsafeCommand implements Runnable {

        private Logger log = LoggerFactory.getLogger(getClass());

        private LocalDateTime startDateTime;

        @Override
        public void run() {

            startDateTime = LocalDateTime.now();

            log.info("Thread [{}] START with startDateTime {}", Thread.currentThread().getName(), startDateTime);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("Thread [{}] END with startDateTime {}", Thread.currentThread().getName(), startDateTime);
        }
    }

    @Test
    void givenOneRunnableWithNormalField_whenMultipleThreadsUseThatRunnable_thenOneThreadCanSeeDiffValueOfThatFieldInThatRunnableIfAnotherThreadModifiedThatField_thatIsNotThreadSafe() throws InterruptedException {

        ThreadUnsafeCommand threadUnsafeCommand = new ThreadUnsafeCommand();
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(threadUnsafeCommand);
            thread.start();
            TimeUnit.SECONDS.sleep(2);
        }
    }

    private static class ThreadSafeCommand implements Runnable {

        private Logger log = LoggerFactory.getLogger(getClass());

        private ThreadLocal<LocalDateTime> startDateTime = ThreadLocal.withInitial(LocalDateTime::now);

        @Override
        public void run() {

            log.info("Thread [{}] START with startDateTime {}", Thread.currentThread().getName(), startDateTime.get());
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("Thread [{}] END with startDateTime {}", Thread.currentThread().getName(), startDateTime.get());
            startDateTime.remove();
        }
    }

    @Test
    void givenOneRunnableWithThreadLocalField_whenMultipleThreadsUseThatRunnable_thenEachThreadHoldOneCopyOfThatFieldAndThusThreadSafe() throws InterruptedException {

        ThreadSafeCommand threadSafeCommand = new ThreadSafeCommand();
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(threadSafeCommand);
            thread.start();
            TimeUnit.SECONDS.sleep(2);
        }
    }
}
