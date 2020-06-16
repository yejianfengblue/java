package com.yejianfengblue.java.concurrency;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * This is a test which demonstrates the use of keyword {@code volatile}. <br/>
 * An operation updating a Java variable aren't performed directly in the main memory (RAM).
 * CPU have cache memory, the data is first written in the cache and might be moved from the cache to main memory
 * (no guarantee).
 * If multiple threads share a variable, one thread updates this variable but the updated data is not written to the main memory,
 * the other thread can't see this update. So this is a visibility problem.
 * The keyword {@code volatile} guarantees a variable must always be read from and stored in the main memory, not the
 * cache of CPU.
 *
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


    /**
     * A {@link Runnable} which is assigned a {@link Flag} by outside,
     * and increases the local variable {@code i} until the {@link Flag#flag} is set to {@code false}.
     */
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

    /**
     * A {@link Runnable} which is assigned a {@link VolatileFlag} by outside,
     * and increases the local variable {@code i} until the {@link VolatileFlag#flag} is set to {@code false}.
     */
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

    /**
     * <p>
     *     After the first one-second sleep, the {@link VolatileFlag#flag} is set to false, {@link VolatileTask#run()}
     *     detects this change, ends the while loop, and prints a log
     *     <br/>
     *     [Thread-1] VolatileTest$VolatileTask - VolatileTask STOP. i = 1692919099
     * </p>
     * <p>
     *     After the second one-second sleep, the {@link Flag#flag} is set to false too, but {@link Task#run()} can't
     *     detect this change, the while loop continues forever
     * </p>
     * The last two call to {@link Thread#join()} guarantees the main thread where this test is running doesn't end
     * until the other two threads end. Because the {@code taskThread} can't detect the flag change, so this thread
     * runs forever, which causes the main thread doesn't end.
     */
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
