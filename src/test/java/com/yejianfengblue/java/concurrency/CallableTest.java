package com.yejianfengblue.java.concurrency;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Test to {@link Callable} and get the result from a {@link Future}
 * @author yejianfengblue
 */
class CallableTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * A {@link Callable} to calculate the factorial n!
     */
    private static class FactorialCalculator implements Callable<Integer> {

        private final Integer number;

        private Logger log = LoggerFactory.getLogger(getClass());

        FactorialCalculator(Integer number) {
            this.number = number;
        }

        @Override
        public Integer call() throws Exception {

            int result = 1;
            if (0 == number || 1 == number) {
                result = 1;
            } else {
                for (int i = 2; i <= number; i++) {
                    result *= i;
                    TimeUnit.SECONDS.sleep(1);
                }
            }
            log.info("{}! = {}",
                    number,
                    result);

            return result;
        }
    }

    @Test
    void givenMultipleCallable_whenAllCallableAreCompleted_thenGetResultFromFuture() throws InterruptedException, ExecutionException {

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

        List<Future<Integer>> resultList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {

            Integer number = ThreadLocalRandom.current().nextInt(5, 10);
            FactorialCalculator factorialCalculator = new FactorialCalculator(number);
            Future<Integer> result = executor.submit(factorialCalculator);
            resultList.add(result);
        }

        // wait until all tasks are completed, print the statistic
        do {
            log.info("taskCount = {}, activeCount = {}, completedTaskCount = {}",
                    executor.getTaskCount(),
                    executor.getActiveCount(),
                    executor.getCompletedTaskCount());
            for (int i = 0; i < resultList.size(); i++) {
                Future<Integer> result = resultList.get(i);
                log.info("Task {} : {}", i, result.isDone());
            }
            TimeUnit.SECONDS.sleep(1);
        } while (executor.getCompletedTaskCount() < resultList.size());

        log.info("All tasks are completed");

        for (int i = 0; i < resultList.size(); i++) {
            Future<Integer> result = resultList.get(i);
            Integer resultNumber = result.get();
            log.info("{} - {}", i, resultNumber);
        }
        executor.shutdown();
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class UserValidator {

        @Getter
        private final String name;

        private Logger log = LoggerFactory.getLogger(getClass());

        UserValidator(String name) {
            this.name = name;
        }

        /**
         * Simulate the user validation process by sleep 1~9 second, and return a random boolean
         * @return validation successful or not
         */
        boolean validate(String username, String password) {

            try {
                TimeUnit.SECONDS.sleep(ThreadLocalRandom.current().nextInt(1, 10));
            } catch (InterruptedException e) {
                log.error("Error", e);
                return false;
            }

            return ThreadLocalRandom.current().nextBoolean();
        }
    }

    @RequiredArgsConstructor
    private static class UserValidationTask implements Callable<String> {

        private final UserValidator userValidator;

        private final String username;

        private final String password;

        private Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public String call() throws Exception {

            if (!userValidator.validate(username, password)) {
                log.error("User cannot be validated by validator {}", userValidator.getName());
                throw new Exception("User cannot be validated");
            } else {
                log.info("User is validated by validator {}", userValidator.getName());
                return userValidator.getName();
            }
        }
    }

    @RepeatedTest(4)
    void testExecutorServiceInvokeAny() throws ExecutionException, InterruptedException {

        UserValidationTask ldapTask = new UserValidationTask(new UserValidator("LDAP"),
                "testUsername", "testPassword");
        UserValidationTask dbTask = new UserValidationTask(new UserValidator("DB"),
                "testUsername", "testPassword");

        ExecutorService executor = Executors.newCachedThreadPool();
        log.info("START");
        String result = executor.invokeAny(List.of(ldapTask, dbTask));

        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);
        log.info("END");
    }
}
