package com.yejianfengblue.java.concurrency;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Test to {@link Thread.UncaughtExceptionHandler} to handle runtime exception from Runnable
 *
 * @author yejianfengblue
 */
class ThreadUncaughtExceptionHandler {

    private static class ExceptionHandler implements Thread.UncaughtExceptionHandler {

        private Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public void uncaughtException(Thread t, Throwable e) {

            log.error("Uncaught exception: ", e);
        }
    }

    @Test
    void givenThreadSetUncaughtExceptionHandler_whenThreadRunnableRunThrowUncheckedException_thenExceptionHandlerHandleThisExceptionAndEndThreadExecution() throws InterruptedException {

        Thread thread = new Thread(() -> {
            Integer.parseInt("A");
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.setUncaughtExceptionHandler(new ExceptionHandler());
        thread.start();
        thread.join(); // must call join(), otherwise once test process exits, the threads die
    }
}
