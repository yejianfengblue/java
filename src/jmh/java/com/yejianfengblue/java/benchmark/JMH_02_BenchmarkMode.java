package com.yejianfengblue.java.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class JMH_02_BenchmarkMode {

    /**
     * {@link Mode#Throughput} means how many times the benchmark method can be completed in one iteration.
     * <br/>
     * One iteration takes 1 second by default, which can be configured by {@link Measurement#time()} and {@link Measurement#timeUnit()}.
     * Those config in {@link Measurement} may be be overridden with runtime options:
     * {@link ChainedOptionsBuilder#measurementTime(TimeValue)}.
     * 
     * @throws InterruptedException
     */
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void measureThroughput() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(100);
    }

    /**
     * The reciprocal throughput
     *
     * @throws InterruptedException
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureAverageTime() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(
                ThreadLocalRandom.current().nextInt(100, 2000)
        );
    }

    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(JMH_02_BenchmarkMode.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
