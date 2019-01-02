package cz.mariskamartin.lambdas;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Fork(value = 1)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@State(Scope.Benchmark)
public class LambdasJmhBenchmark {

    private String test = "Test";

    @Param({"10000","100000"})
    private int invocationRepeatCount;

    @Benchmark
    public void nonCapturingLambda(Blackhole bh) {
        Function<String, String> appendStar = s -> s + "*";
        for (int i = 0; i < invocationRepeatCount; i++) {
            bh.consume(appendStar.apply(test));
        }
    }

    @Benchmark
    public void capturingLambda(Blackhole bh) {
        String star = "*";
        Function<String, String> appendStar = s -> s + star;
        for (int i = 0; i < invocationRepeatCount; i++) {
            bh.consume(appendStar.apply(test));
        }
    }

    /**
     * Start this benchmark
     * @param args
     * @throws RunnerException
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(LambdasJmhBenchmark.class.getSimpleName())
                .forks(1)
//                .jvmArgs("-XX:+PrintFlagsFinal")
                .jvmArgs("-Xms512m", "-Xmx512m")
                .build();

        new Runner(opt).run();
    }
}
