package com.yejianfengblue.java.concurrency.connection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author yejianfengblue
 */
class ConcurrentHashMapTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    @AllArgsConstructor
    @Getter
    @ToString
    private static class Operation {

        private String operation;

        private String user;

        private ZonedDateTime time;
    }

    @RequiredArgsConstructor
    private static class HashFiller implements Runnable {

        private final ConcurrentHashMap<String, ConcurrentLinkedDeque<Operation>> userHashMap;

        @Override
        public void run() {

            ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
            for (int i = 0; i < 100; i++) {

                Operation operation = new Operation(
                        "OP" + threadLocalRandom.nextInt(10),
                        "USER" + threadLocalRandom.nextInt(100),
                        ZonedDateTime.now());
                // The entire computeIfAbsent() method invocation is performed atomically, see javadoc
                ConcurrentLinkedDeque<Operation> opList = userHashMap.computeIfAbsent(
                        operation.getUser(),
                        user -> new ConcurrentLinkedDeque<>());
                opList.add(operation);
            }
        }
    }

    @Test
    void test() throws InterruptedException {

        ConcurrentHashMap<String, ConcurrentLinkedDeque<Operation>> userHashMap = new ConcurrentHashMap<>();
        HashFiller hashFiller = new HashFiller(userHashMap);

        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(hashFiller);
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(100, userHashMap.size());

        userHashMap.forEach(10,
                (user, list) -> log.info("parallel list size = {}", list.size())
        );

        userHashMap.forEachEntry(10,
                entry -> log.info("key = {}, valueSize = {}", entry.getKey(), entry.getValue().size())
        );

        // return first non-null value
        Operation op = userHashMap.search(10,
                (user, list) -> {
                    for (Operation operation : list) {
                        if (operation.getOperation().endsWith("1")) {
                            return operation;
                        }
                    }
                    return null;
                }
        );
        log.info("Found operation with operation code ends with 1 = {}", op);

        ConcurrentLinkedDeque<Operation> operations = userHashMap.search(10,
                (user, list) -> {
                    if (list.size() > 10) {
                        return list;
                    } else {
                        return null;
                    }
                }
        );
        log.info("Found first operation where one user with more than 10 operations = {}, size = {}",
                operations.getFirst().getUser(),
                operations.size());

        // use reduce() to calculate the total number of operations
        assertEquals(1000,
                userHashMap.reduce(10,
                        (user, list) -> list.size(),
                        Integer::sum)
        );
    }
}
