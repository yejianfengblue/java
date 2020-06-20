package com.yejianfengblue.java.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

public class JMH_03_State {


    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void measureThroughput(ThreadState threadState) throws AssertionError {
        threadState.value++;
    }

    @State(Scope.Thread)
    public static class ThreadState {

        long value;

        @Setup(Level.Iteration)
        public void setUp() {
            value = 0;
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            if (0 == value) {
                throw new AssertionError("value should be changed. Current value = " + value);
            }
        }
    }


    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(JMH_03_State.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
