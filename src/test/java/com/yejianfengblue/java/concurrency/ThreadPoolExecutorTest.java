package com.yejianfengblue.java.concurrency;

import lombok.Getter;
import lombok.ToString;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Test to {@link ThreadPoolExecutor} via a {@link Server} which has a {@link ThreadPoolExecutor} with fix thread pool
 * and receives {@link Task} and executes it.
 *
 * @author yejianfengblue
 */
class ThreadPoolExecutorTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * A task which simulate the process by sleep 1~10 seconds
     */
    private static class Task implements Runnable {

        @Getter @ToString.Include
        private final String name;

        private final LocalDateTime createdTime;

        private Logger log = LoggerFactory.getLogger(getClass());

        Task(String name) {
            this.name = name;
            this.createdTime = LocalDateTime.now();
        }

        @Override
        public void run() {

            log.info("Thread [{}] - Task [{}] is created at {}",
                    Thread.currentThread().getName(),
                    name,
                    createdTime);

            try {
                long duration = ThreadLocalRandom.current().nextInt(1, 11);
                log.info("Thread [{}] - Task [{}] is doing a task during {}s",
                        Thread.currentThread().getName(),
                        name,
                        duration);
                TimeUnit.SECONDS.sleep(duration);
            } catch (InterruptedException e) {
                log.error("Error", e);
            }

            log.info("Thread [{}] - Task [{}] is completed",
                    Thread.currentThread().getName(),
                    name);
        }
    }

    /**
     * This has a {@link ThreadPoolExecutor} and receive {@link Task} and execute it.
     * If a {@link Task} is received after this server is shutdown, then this task is rejected and logged.
     */
    private static class Server {

        private final ThreadPoolExecutor executor;

        private Logger log = LoggerFactory.getLogger(getClass());

        Server(int threadPoolSize) {

            executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadPoolSize);
            executor.setRejectedExecutionHandler((_runnable, _executor) ->
                    log.info("Task {} is rejected, Executor = {}, executor.isTerminating = {}, executor.isTerminated = {}",
                            _runnable.toString(),
                            _executor.toString(),
                            _executor.isTerminating(),
                            _executor.isTerminated()));
        }

        void executeTask(Task task) {

            log.info("Task {} arrives", task.getName());
            executor.execute(task);

            log.info("Pool size = {}, task count = {}, active count = {}, completed task count = {}",
                    executor.getPoolSize(),
                    executor.getTaskCount(),
                    executor.getActiveCount(),
                    executor.getCompletedTaskCount());
        }

        void endServer() {
            log.info("Server ended");
            executor.shutdown();
        }

        void waitTaskCompleteAndEndServer() {
            log.info("Wait task to complete before end server");
            try {
                executor.shutdown();
                executor.awaitTermination(1, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("Server ended");
        }
    }

    @Test
    void givenFixedServerThread_whenTasksMoreThanThreadSizeAreExecuted_thenLateComingTaskArePendedBeforeRunningTasksAreCompleted() {

        // given
        int threadPoolSize = Runtime.getRuntime().availableProcessors();
        Server server = new Server(threadPoolSize);

        // when
        IntStream.range(0, threadPoolSize*3).forEach(value ->
                server.executeTask(new Task("Task #"+value)));
        server.waitTaskCompleteAndEndServer();
    }

    @Test
    void givenServerAlreadyShutdown_whenExecuteTask_thenRejected() {

        // given
        int threadPoolSize = Runtime.getRuntime().availableProcessors();
        Server server = new Server(threadPoolSize);
        server.endServer();

        // send a new task after shutdown executor, that task will be rejected
        log.info("Send a task after executor is shutdown");
        server.executeTask(new Task("I'm the king of the world!!!"));
    }
}
