package com.yejianfengblue.java.concurrency;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Test to thread-safe sorted map {@link ConcurrentSkipListMap}
 * @author yejianfengblue
 */
class ConcurrentSkipListMapTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    @RequiredArgsConstructor
    @Getter
    @ToString
    private static class Contact {

        private final String name;

        private final String phone;
    }

    @RequiredArgsConstructor
    private static class Task implements Runnable {

        private final String id;

        private final ConcurrentSkipListMap<String, Contact> map;

        private Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public void run() {

            for (int i = 0; i < 1000; i++) {
                Contact contact = new Contact(id, String.valueOf(i + 1000));  // A~Z, A1xxx~Z1999
                map.put(id + contact.getPhone(), contact);
            }
        }
    }

    @Test
    void givenConcurrentSkipListMap_whenPollFirstEntry_thenEntryWithLeastKeyIsReturned() throws InterruptedException {

        ConcurrentSkipListMap<String, Contact> map = new ConcurrentSkipListMap<>();

        Thread[] threads = new Thread[26];
        int counter = 0;
        for (char i = 'A'; i <= 'Z'; i++) {

            threads[counter] = new Thread(new Task(String.valueOf(i), map));
            threads[counter].start();
            counter++;
        }

        for (Thread thread : threads) {
            thread.join();
        }

        log.info("Map size = {}", map.size());

        log.info("First entry = {}", map.firstEntry());
        log.info("First entry = {}", map.lastEntry());

        log.info("Submap from A1996 to B1002:");
        ConcurrentNavigableMap<String, Contact> subMap = map.subMap("A1996", "B1002");
        Map.Entry<String, Contact> subMapEntry;
        do {
            subMapEntry = subMap.pollFirstEntry();
            if (null != subMapEntry) {
                log.info(subMapEntry.toString());
            }
        } while (null != subMapEntry);
    }
}
