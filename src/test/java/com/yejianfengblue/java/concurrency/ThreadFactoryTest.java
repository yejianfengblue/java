package com.yejianfengblue.java.concurrency;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Test to {@link ThreadFactory}, with which we centralize the creation of thread with advantages:
 * <ul>
 *     <li>easy to change class of objects created or the way to create them</li>
 *     <li>easy to limit creation of objects for limited resources, e.g., only have <i>n</i> objects of a given type</li>
 *     <li>easy to generate statistical data about the creation of objects</li>
 * </ul>
 *
 * @author yejianfengblue
 */
class ThreadFactoryTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    private static class MyThreadFactory implements ThreadFactory {

        private int counter;

        private String name;

        private List<String> stats;

        MyThreadFactory(String name) {
            counter = 0;
            this.name = name;
            stats = new ArrayList<>();
        }

        @Override
        public Thread newThread(Runnable r) {

            Thread thread = new Thread(r, name + "-Thread-" + counter);
            counter++;
            stats.add(String.format("Created thread %d with name %s on %s",
                    thread.getId(), thread.getName(), LocalDateTime.now().toString())
            );
            return thread;
        }

        List<String> getStats() {
            return stats;
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
    void threadFactoryTest() {

        MyThreadFactory myThreadFactory = new MyThreadFactory("MyThreadFactory");
        log.info("Start 10 threads");
        for (int i = 0; i < 10; i++) {
            Thread thread = myThreadFactory.newThread(new Sleep5sJob());
            thread.start();
        }
        log.info("MyThreadFactory stats: {}", String.join("\n", myThreadFactory.getStats()));
    }
}
