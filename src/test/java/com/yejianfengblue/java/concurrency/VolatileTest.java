package com.yejianfengblue.java.concurrency;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author yejianfengblue
 */
class VolatileTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    private static class Flag {

        boolean flag = true;
    }

    private static class VolatileFlag {

        volatile boolean flag = true;
    }

    @RequiredArgsConstructor
    private static class Task implements Runnable {

        private final Flag flag;

        private Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public void run() {

            int i = 0;
            while (flag.flag) {
                i++;
            }
            log.info("Task STOP. i = {}", i);
        }
    }

    @RequiredArgsConstructor
    private static class VolatileTask implements Runnable {

        private final VolatileFlag volatileFlag;

        private Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public void run() {

            int i = 0;
            while (volatileFlag.flag) {
                i++;
            }
            log.info("VolatileTask STOP. i = {}", i);
        }
    }

    @Disabled
    @Test
    void test() throws InterruptedException {

        Flag flag = new Flag();
        Thread taskThread = new Thread(new Task(flag));
        taskThread.start();

        VolatileFlag volatileFlag = new VolatileFlag();
        Thread volatileTaskThread = new Thread(new VolatileTask(volatileFlag));
        volatileTaskThread.start();

        TimeUnit.SECONDS.sleep(1);

        log.info("Going to stop volatile task");
        volatileFlag.flag = false;
        log.info("Volatile task STOP");

        TimeUnit.SECONDS.sleep(1);

        log.info("Going to stop task");
        // this doesn't trigger Task to stop, because the value change doesn't reflect to main memory
        flag.flag = false;

        TimeUnit.SECONDS.sleep(1);

        taskThread.join();
        volatileTaskThread.join();
    }
}
