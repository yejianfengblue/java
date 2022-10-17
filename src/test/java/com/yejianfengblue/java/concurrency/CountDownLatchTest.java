package com.yejianfengblue.java.concurrency;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Test to {@link CountDownLatch} which allows one or more threads to wait until a set of operation are made.
 * The impl example is a video conference system, which wait for arrival of all the participants before it begins.
 * <p>
 *     When the internal counter arrives at 0, the class wakes up all threads that were sleeping.
 * </p>
 * <p>
 * {@link CountDownLatch} mechanism can not protect shared resource or a critical section
 * </p>
 * @author yejianfengblue
 */
class CountDownLatchTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    private static class VideoConference implements Runnable {

        private final CountDownLatch controller;

        private Logger log = LoggerFactory.getLogger(getClass());

        VideoConference(int numberOfParticipant) {
            controller = new CountDownLatch(numberOfParticipant);
        }

        void arrive(String participantName) {
            log.info("{} arrived", participantName);
            controller.countDown();
            log.info("Waiting for {} participants", controller.getCount());
        }

        @Override
        public void run() {

            log.info("Expect {} participants", controller.getCount());

            try{
                controller.await();
                log.info("All participants have arrived");
            } catch (InterruptedException e) {
                log.error("Error", e);
            }
        }
    }

    private static class Participant implements Runnable {

        private VideoConference videoConference;

        private String name;

        private Logger log = LoggerFactory.getLogger(getClass());

        Participant(String name, VideoConference videoConference) {
            this.name = name;
            this.videoConference = videoConference;
        }

        @Override
        public void run() {

            // simulate participant arrive at diff time
            try {
                TimeUnit.SECONDS.sleep(ThreadLocalRandom.current().nextInt(1, 10));
            } catch (InterruptedException e) {
                log.error("Error", e);
            }
            videoConference.arrive(name);
        }
    }

    @Test
    void givenVideoConferenceWith10Participants_whenAllParticipantsArrived_thenVideoConferenceBegin() throws InterruptedException {

        VideoConference videoConference = new VideoConference(10);
        Thread videoConferenceThread = new Thread(videoConference);
        videoConferenceThread.start();

        Thread[] participantThreads = new Thread[10];
        for (int i = 0; i < 10; i++) {

            Participant participant = new Participant("Participant #"+i, videoConference);
            participantThreads[i] = new Thread(participant);
            participantThreads[i].start();
        }

        for (int i = 0; i < 10; i++) {

            participantThreads[i].join();
        }
    }
}
