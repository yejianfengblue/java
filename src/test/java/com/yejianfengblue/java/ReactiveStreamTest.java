package com.yejianfengblue.java;

import lombok.Value;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

/**
 * Test to Java 9 {@link Flow.Publisher}, {@link Flow.Subscriber}, and {@link Flow.Subscription}
 * @author yejianfengblue
 */
class ReactiveStreamTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Value
    private static class Item {

        private String title;

        private String content;
    }

    private static class Consumer1 implements Flow.Subscriber<Item> {

        private Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public void onSubscribe(Flow.Subscription subscription) {

            log.info("Consumer1 subscription received");
        }

        @Override
        public void onNext(Item item) {

            log.info("Consumer1 receive item {}", item);
        }

        @Override
        public void onError(Throwable throwable) {

            log.error("Consumer1 error", throwable);
        }

        @Override
        public void onComplete() {

            log.info("Consumer1 complete");
        }
    }

    private static class Consumer2 implements Flow.Subscriber<Item> {

        private Flow.Subscription subscription;

        private Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public void onSubscribe(Flow.Subscription subscription) {

            log.info("Consumer2 subscription received");
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(Item item) {

            log.info("Consumer2 receive item {}", item);
            subscription.request(1);  // request another item
        }

        @Override
        public void onError(Throwable throwable) {

            log.error("Consumer2 error", throwable);
        }

        @Override
        public void onComplete() {

            log.info("Consumer2 complete");
        }
    }

    private static class Consumer3 implements Flow.Subscriber<Item> {

        private Flow.Subscription subscription;

        private Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public void onSubscribe(Flow.Subscription subscription) {

            log.info("Consumer3 subscription received");
            this.subscription = subscription;
            subscription.request(3);
        }

        @Override
        public void onNext(Item item) {

            log.info("Consumer3 receive item {}", item);
        }

        @Override
        public void onError(Throwable throwable) {

            log.error("Consumer3 error", throwable);
        }

        @Override
        public void onComplete() {

            log.info("Consumer3 complete");
        }
    }

    @Test
    void t() throws InterruptedException {

        Consumer1 consumer1 = new Consumer1();
        Consumer2 consumer2 = new Consumer2();
        Consumer3 consumer3 = new Consumer3();

        log.info("Subscribe 3 consumers");
        SubmissionPublisher<Item> publisher = new SubmissionPublisher<>();
        publisher.subscribe(consumer1);
        publisher.subscribe(consumer2);
        publisher.subscribe(consumer3);

        TimeUnit.SECONDS.sleep(1);

        log.info("Submit 10 items");
        for (int i = 0; i < 10; i++) {

            Item item = new Item("Item" + i, LocalDateTime.now().toString());
            publisher.submit(item);
            TimeUnit.SECONDS.sleep(1);
        }
        TimeUnit.SECONDS.sleep(3);
        publisher.close();
        TimeUnit.SECONDS.sleep(3);
    }
}
