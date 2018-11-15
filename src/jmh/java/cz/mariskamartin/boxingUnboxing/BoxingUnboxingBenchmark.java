package cz.mariskamartin.boxingUnboxing;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.IntBinaryOperator;
import java.util.stream.IntStream;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.HotspotMemoryProfiler;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@Warmup(iterations = 5)
@Measurement(iterations = 10)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Fork(3)
public class BoxingUnboxingBenchmark {

    @Param({"10000","100000"})
    int size;

    Integer[] objects;

    int[] primitives;

    @Setup
    public void setup() {
        objects = new Integer[size];
        primitives = new int[size];

        Random random = new Random(123);
        for (int i = 0; i < size; i++) {
            int toAdd = random.nextInt();
            objects[i] = toAdd;
            primitives[i] = toAdd;
        }
    }

    @Benchmark
    public int primitives() {
        int sum = 0;
        for (int i = 0; i < size; i++) {
            sum += primitives[i];
        }
        return sum;
    }

    @Benchmark
    public int objects() {
        int sum = 0;
        for (int i = 0; i < size; i++) {
            sum += objects[i];
        }
        return sum;
    }

    @Benchmark
    public int intStream() {
        int sum = 0;
        sum = IntStream.of(primitives).sum();
        return sum;
    }

//    @Benchmark
//    public int vavrIntSum() {
//        int sum = 0;
//        sum = io.vavr.collection.List.ofAll(primitives).sum().intValue();
//        return sum;
//    }


    @Benchmark
    public int intStreamReduce() {
        int sum = 0;
        sum = IntStream.of(primitives).reduce((int x, int y) -> x +y).getAsInt();
        return sum;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BoxingUnboxingBenchmark.class.getSimpleName())
                //                            .addProfiler(StackProfiler.class)
                //                            .addProfiler(HotspotMemoryProfiler.class)
                .jvmArgs("-Xmx512m")
                .forks(1)
                .measurementIterations(3)
                .warmupIterations(1)
                .build();
        new Runner(opt).run();
    }
}
