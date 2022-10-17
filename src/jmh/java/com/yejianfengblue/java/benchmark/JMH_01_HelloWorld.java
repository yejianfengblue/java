package com.yejianfengblue.java.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * The annotation {@link Benchmark} registers a public method as a benchmark in the benchmark list.
 * JMH generates code to run the benchmark at /target/generated-sources/annotations/.../generated/{benchmarkClass_benchmarkMethod}...
 *
 * JMH maintains a worker thread pool.
 * One thread runs the benchmark method one time and then is returned back to the thread pool.
 *
 * By default, 20 warmup iteration is fired before 20 true iteration.
 * And above operation is repeated 10 times which is called forks.
 * The measure unit is ops/s.
 */
public class JMH_01_HelloWorld {

    @Benchmark
    public void helloWorld() {
        // this method was intentionally left blank.
    }

    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(JMH_01_HelloWorld.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
