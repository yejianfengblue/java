package com.yejianfengblue.java.concurrency.connection;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Test to blocking deque {@link java.util.concurrent.LinkedBlockingDeque}
 * by
 *
 * @author yejianfengblue
 */
class LinkedBlockingDequeTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * A Runnable that puts 5 elements into the given {@link LinkedBlockingDeque} and sleeps 2s, with this cycle
     * repeated 3 times
     */
    @RequiredArgsConstructor
    private static class Client implements Runnable {

        private final LinkedBlockingDeque<String> requestList;

        private Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public void run() {

            for (int i = 0; i < 3; i++) {

                log.info("Will put last 5 requests");
                for (int j = 0; j < 5; j++) {

                    String request = i + "-" + j;
                    try {
                        requestList.putLast(request);  //
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // if another thread adds or removes element, below logging size and remainingCapacity may be incorrect
                    log.info("Client put last {}, size = {}, remainingCapacity = {}", request, requestList.size(), requestList.remainingCapacity());
                }

                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.info("Client End");
        }
    }

    @Test
    void test() throws InterruptedException {

        LinkedBlockingDeque<String> list = new LinkedBlockingDeque<>(3);  // fixed size 3

        Client client = new Client(list);
        Thread clientThread = new Thread(client);
        clientThread.start();

        // sleep 1s before take, to avoid above put and below take occurs in same millisecond and logging order mess up
        TimeUnit.SECONDS.sleep(1);

        for (int i = 0; i < 5; i++) {

            log.info("Will take first 3 requests");
            for (int j = 0; j < 3; j++) {

                String request = list.takeFirst();
                log.info("Take first {}, size = {}", request, list.size());
            }
            TimeUnit.SECONDS.sleep(1);
        }

        clientThread.join();
        log.info("END");
    }
}
/*
35.214 [Thread-0] LinkedBlockingDequeTest$Client - Will put last 5 requests
35.233 [Thread-0] LinkedBlockingDequeTest$Client - Client put last 0-0, size = 1, remainingCapacity = 2
35.233 [Thread-0] LinkedBlockingDequeTest$Client - Client put last 0-1, size = 2, remainingCapacity = 1
35.233 [Thread-0] LinkedBlockingDequeTest$Client - Client put last 0-2, size = 3, remainingCapacity = 0
// deque is full, first put batch is blocked
36.208 [main] LinkedBlockingDequeTest - Will take first 3 requests
36.209 [main] LinkedBlockingDequeTest - Take first 0-0, size = 2
// remaining element to put in first put batch
36.209 [Thread-0] LinkedBlockingDequeTest$Client - Client put last 0-3, size = 3, remainingCapacity = 0
36.209 [main] LinkedBlockingDequeTest - Take first 0-1, size = 2
// remaining element to put in first put batch
36.211 [Thread-0] LinkedBlockingDequeTest$Client - Client put last 0-4, size = 3, remainingCapacity = 0
36.211 [main] LinkedBlockingDequeTest - Take first 0-2, size = 2


37.211 [main] LinkedBlockingDequeTest - Will take first 3 requests
37.211 [main] LinkedBlockingDequeTest - Take first 0-3, size = 1
37.211 [main] LinkedBlockingDequeTest - Take first 0-4, size = 0
// deque is empty, second take batch is blocked, with 1 element to take
// because the first put batch is blocked for 1s, plus its own sleep 3s, so second put batch starts at 35+4=39
39.211 [Thread-0] LinkedBlockingDequeTest$Client - Will put last 5 requests
39.211 [Thread-0] LinkedBlockingDequeTest$Client - Client put last 1-0, size = 1, remainingCapacity = 2
39.211 [main] LinkedBlockingDequeTest - Take first 1-0, size = 0
39.211 [Thread-0] LinkedBlockingDequeTest$Client - Client put last 1-1, size = 1, remainingCapacity = 2
39.211 [Thread-0] LinkedBlockingDequeTest$Client - Client put last 1-2, size = 2, remainingCapacity = 1
39.211 [Thread-0] LinkedBlockingDequeTest$Client - Client put last 1-3, size = 3, remainingCapacity = 0
40.211 [main] LinkedBlockingDequeTest - Will take first 3 requests
40.211 [main] LinkedBlockingDequeTest - Take first 1-1, size = 2
40.211 [Thread-0] LinkedBlockingDequeTest$Client - Client put last 1-4, size = 3, remainingCapacity = 0
40.211 [main] LinkedBlockingDequeTest - Take first 1-2, size = 2
40.211 [main] LinkedBlockingDequeTest - Take first 1-3, size = 1
41.211 [main] LinkedBlockingDequeTest - Will take first 3 requests
41.211 [main] LinkedBlockingDequeTest - Take first 1-4, size = 0
43.211 [Thread-0] LinkedBlockingDequeTest$Client - Will put last 5 requests
43.211 [Thread-0] LinkedBlockingDequeTest$Client - Client put last 2-0, size = 1, remainingCapacity = 2
43.211 [main] LinkedBlockingDequeTest - Take first 2-0, size = 0
43.211 [Thread-0] LinkedBlockingDequeTest$Client - Client put last 2-1, size = 1, remainingCapacity = 2
43.211 [main] LinkedBlockingDequeTest - Take first 2-1, size = 0
43.211 [Thread-0] LinkedBlockingDequeTest$Client - Client put last 2-2, size = 1, remainingCapacity = 2
43.211 [Thread-0] LinkedBlockingDequeTest$Client - Client put last 2-3, size = 2, remainingCapacity = 1
43.211 [Thread-0] LinkedBlockingDequeTest$Client - Client put last 2-4, size = 3, remainingCapacity = 0
44.212 [main] LinkedBlockingDequeTest - Will take first 3 requests
44.212 [main] LinkedBlockingDequeTest - Take first 2-2, size = 2
44.212 [main] LinkedBlockingDequeTest - Take first 2-3, size = 1
44.212 [main] LinkedBlockingDequeTest - Take first 2-4, size = 0
 */
