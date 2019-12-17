package com.yejianfengblue.java.concurrency;

import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * A fixed to {@link NoSynchronizedTest} by declare with keyword <i>synchronized</i> the shared methods or code blocks
 * which can modify fields.
 * <p>
 * When declare {@code synchronized} in one or more non-static methods of an object, only one execution thread will
 * have access to <b>all</b> these methods. Other threads, trying to access any method declared with {@code synchronized}
 * of the same object, will be suspended until the first thread finishes the execution of the method.
 * <p>
 * Only one execution thread will have access to one of the static methods declared with {@code synchronized}, but a
 * diff thread can access other non-static methods of an object of that class. Be careful if static method and
 * non-static method change the same field.
 * <p>
 * <ul>
 *     <li>The <i>own</i> object of methods declared with {@code synchronized} is {@code this}</li>
 *     <li>The <i>own</i> object of static methods declared with {@code synchronized} is the class object</li>
 *     <li>The <i>own</i> object of code block declared with {@code synchronized} can be {@code this} or other obj ref as well</li>
 * </ul>
 * @author yejianfengblue
 */
class SynchronizedTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    static class ParkingCash {

        private static final int cost = 1;

        private int cash;

        private Logger log = LoggerFactory.getLogger(getClass());

        ParkingCash() {
            cash = 0;
        }

        synchronized void vehiclePay() {
            cash += cost;
        }

        void close() {
            log.info("Closing accounting");
            int total;
            synchronized (this) {
                total = cash;
                cash = 0;
            }
            log.info("Total amount is {}", total);
        }
    }

    @Getter
    static class ParkingStats {

        private int numberCars;

        private final Object controlNumberCars;  // sync owner of field numberCars

        private int numberMotorcycles;

        private final Object controlNumberMotorcycles;  // sync owner of field numberMotorcycles

        private ParkingCash parkingCash;

        ParkingStats(ParkingCash parkingCash) {

            numberCars = 0;
            controlNumberCars = new Object();
            numberMotorcycles = 0;
            controlNumberMotorcycles = new Object();
            this.parkingCash = parkingCash;
        }

        void carIn() {
            synchronized (controlNumberCars) {
                numberCars++;
            }
        }

        void carOut() {
            synchronized (controlNumberCars) {
                numberCars--;
            }
            parkingCash.vehiclePay();
        }

        void motoIn() {
            synchronized (controlNumberMotorcycles) {
                numberMotorcycles++;
            }
        }

        void motoOut() {
            synchronized (controlNumberMotorcycles) {
                numberMotorcycles--;
            }
            parkingCash.vehiclePay();
        }
    }

    /**
     * Simulate 2 cars in, 1 moto in, 1 moto out, 2 cars out
     */
    static class Sensor implements Runnable {

        private ParkingStats parkingStats;

        Sensor(ParkingStats parkingStats) {

            this.parkingStats = parkingStats;
        }

        @Override
        public void run () {

            for (int i = 0; i < 10; i++) {

                parkingStats.carIn();
                parkingStats.carIn();
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                parkingStats.motoIn();
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                parkingStats.motoOut();

                parkingStats.carOut();
                parkingStats.carOut();
            }
        }
    }

    @Test
    void givenSharedVariablesWhichAreUpdatedBySyncBlockOrSyncMethod_whenMultipleThreadsUpdateThem_thenVariableFinalValueIsCorrect() throws InterruptedException {

        ParkingCash parkingCash = new ParkingCash();
        ParkingStats parkingStats = new ParkingStats(parkingCash);
        log.info("Parking Simulator");

        int numberSensors = 2 * Runtime.getRuntime().availableProcessors();

        Thread[] threads = new Thread[numberSensors];
        for (int i = 0; i < numberSensors; i++) {
            Sensor sensor = new Sensor(parkingStats);
            Thread thread = new Thread(sensor);
            thread.start();
            threads[i] = thread;
        }

        for (int i = 0; i< numberSensors; i++) {

            threads[i].join();
        }

        log.info("Number of sensors: {}", numberSensors);  // expect CPU core * 2
        log.info("Number of cars: {}", parkingStats.getNumberCars());  // expect 0
        log.info("Number of motorcycles: {}", parkingStats.getNumberMotorcycles());  // expect 0
        parkingCash.close();  // total amount should be numberSensors * 30
    }
}
