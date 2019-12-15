package com.yejianfengblue.java.concurrency;

import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Impl an wrong example without any sync mechanism.
 * Multiple threads have the same object {@link ParkingStats} and {@link ParkingCash}.
 * ParkingStats and ParkingCash have methods to modify their fields.
 * When multiple threads operates with those methods, the fields will end up with wrong value.
 *
 * @author yejianfengblue
 */
class NoSynchronizedTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    private static class ParkingCash {

        private static final int cost = 1;

        private int cash;

        private Logger log = LoggerFactory.getLogger(getClass());

        ParkingCash() {
            cash = 0;
        }

        void vehiclePay() {
            cash += cost;
        }

        void close() {
            log.info("Closing accounting");
            int total = cash;
            cash = 0;
            log.info("Total amount is {}", total);
        }
    }

    @Getter
    private static class ParkingStats {

        private int numberCars;

        private int numberMotorcycles;

        private ParkingCash parkingCash;

        ParkingStats(ParkingCash parkingCash) {

            numberCars = 0;
            numberMotorcycles = 0;
            this.parkingCash = parkingCash;
        }

        void carIn() {
            numberCars++;
        }

        void carOut() {
            numberCars--;
            parkingCash.vehiclePay();
        }

        void motoIn() {
            numberMotorcycles++;
        }

        void motoOut() {
            numberMotorcycles--;
            parkingCash.vehiclePay();
        }
    }

    /**
     * Simulate 2 cars in, 1 moto in, 1 moto out, 2 cars out
     */
    private static class Sensor implements Runnable {

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
    void givenSharedVariables_whenMultipleThreadsUpdateThem_thenVariableFinalValueIsIncorrect() throws InterruptedException {

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

        log.info("Number of sensors: {}", numberSensors);
        log.info("Number of cars: {}", parkingStats.getNumberCars());
        log.info("Number of motorcycles: {}", parkingStats.getNumberMotorcycles());
        parkingCash.close();
    }
}
