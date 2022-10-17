package com.yejianfengblue.java.concurrency;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * Test to {@link AtomicLong}, {@link LongAdder} and {@link DoubleAccumulator}
 * by simulate a account is updated (add and subtract) by multiple threads.
 * <p>
 *     The atomic variable class implementation use "compare and set" to guarantee atomicity
 * </p>
 * @author yejianfengblue
 */
class AtomicVarTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Getter
    private static class Account {

        private final AtomicLong balance;

        private final LongAdder operations;

        private final DoubleAccumulator commission;

        Account() {
            balance = new AtomicLong();
            operations = new LongAdder();
            commission = new DoubleAccumulator((x, y) -> x + y * 0.2, 0);  // 20% charge
        }

        void setBalance(long balance) {

            this.balance.set(balance);
            operations.reset();  // initialize
            commission.reset();  // initialize
        }

        void addAmount(long amount) {

            this.balance.getAndAdd(amount);
            this.operations.increment();
            this.commission.accumulate(amount);
        }

        void subtractAmount(long amount) {

            this.balance.getAndAdd(-amount);
            this.operations.increment();
            this.commission.accumulate(amount);
        }
    }

    /**
     * Simulate a company that increments the balance of account by 1000 for 10 times
     */
    @RequiredArgsConstructor
    private static class Company implements Runnable {

        private final Account account;

        @Override
        public void run() {

            for (int i = 0; i < 10; i++) {
                account.addAmount(1000);
            }
        }
    }

    /**
     * Simulate a bank that take out 1000 money for 10 times
     */
    @RequiredArgsConstructor
    private static class Bank implements Runnable {

        private final Account account;

        @Override
        public void run() {

            for (int i = 0; i < 10; i++) {
                account.subtractAmount(1000);
            }
        }
    }

    @Test
    void test() throws InterruptedException {

        Account account = new Account();
        account.setBalance(1000);

        Company company = new Company(account);
        Thread companyThread = new Thread(company);

        Bank bank = new Bank(account);
        Thread bankThread = new Thread(bank);

        log.info("Account initial balance = {}", account.getBalance());

        companyThread.start();
        bankThread.start();

        companyThread.join();
        bankThread.join();

        log.info("Account final balance = {}", account.getBalance().longValue());
        log.info("Account operations = {}", account.getOperations().longValue());
        log.info("Account accumulated commissions = {}", account.getCommission().doubleValue());
    }
}
