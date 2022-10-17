package com.yejianfengblue.java.concurrency;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Test to {@link ReadWriteLock}
 *
 * @author yejianfengblue
 */
class ReadWriteLockTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    private static class PricesInfo {

        private int price1;

        private int price2;

        private ReadWriteLock lock;

        private Logger log = LoggerFactory.getLogger(getClass());

        PricesInfo() {
            price1 = 0;
            price2 = 0;
            lock = new ReentrantReadWriteLock();
        }

        int getPrice1() {
            lock.readLock().lock();
            int value = price1;
            lock.readLock().unlock();
            return value;
        }

        int getPrice2() {
            lock.readLock().lock();
            int value = price2;
            lock.readLock().unlock();
            return value;
        }

        void setPrices(int price1, int price2) {
            lock.writeLock().lock();
            log.info("PricesInfo: Write lock acquired");

            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            this.price1 = price1;
            this.price2 = price2;
            log.info("PricesInfo: Write lock released");
            lock.writeLock().unlock();
        }

        static class Reader implements Runnable {

            private PricesInfo pricesInfo;

            private Logger log = LoggerFactory.getLogger(getClass());

            Reader(PricesInfo pricesInfo) { this.pricesInfo = pricesInfo; }

            @Override
            public void run() {
                for (int i = 0; i < 20; i++) {
                    log.info("[{}] - price1 = {}",
                            Thread.currentThread().getName(),
                            pricesInfo.getPrice1());
                    log.info("[{}] - price2 = {}",
                            Thread.currentThread().getName(),
                            pricesInfo.getPrice2());
                }
            }
        }

        static class Writer implements Runnable {

            private PricesInfo pricesInfo;

            private Logger log = LoggerFactory.getLogger(getClass());

            Writer(PricesInfo pricesInfo) { this.pricesInfo = pricesInfo; }

            @Override
            public void run() {

                for (int i = 0; i < 3; i++) {

                    log.info("Writer attempts to modify the prices");
                    pricesInfo.setPrices((int) (Math.random() * 10000), (int) (Math.random() * 10000));
                    log.info("Writer modified the prices");

                    try {
                        TimeUnit.MILLISECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Test
    void givenReadWriteLock_whenWriteLockAcquired_whenOtherThreadsCannotAcquireReadLock() throws InterruptedException {

        PricesInfo pricesInfo = new PricesInfo();

        PricesInfo.Reader[] readers = new PricesInfo.Reader[5];
        Thread[] readerThreads = new Thread[5];

        for (int i = 0; i < 5; i++) {
            readers[i] = new PricesInfo.Reader(pricesInfo);
            readerThreads[i] = new Thread(readers[i]);
        }

        PricesInfo.Writer writer = new PricesInfo.Writer(pricesInfo);
        Thread writerThread = new Thread(writer);

        for(int i = 0; i < 5; i++) {
            readerThreads[i].start();
        }
        writerThread.start();

        for(int i = 0; i < 5; i++) {
            readerThreads[i].join();
        }
        writerThread.join();
    }
}
