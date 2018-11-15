package cz.mariskamartin.producerConsumer;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Warmup(iterations = 7)
@State(Scope.Thread)
public class ProducerConsumerImplJmhBenchmark {
    private static final Logger log = LoggerFactory.getLogger(ProducerConsumerImplJmhBenchmark.class);
    private BlockingQueue<SharedItemJmh> queue;
    private ExecutorService executorService;
    private AtomicLong eventCount;
    private String VALUE = "test1234";
    private SharedItemJmh DONE;

    @Param({"1","4"})
    private int consumerPoolSize;

    private int targetEventCount = 100_000;

    @Param({"com.conversantmedia.util.concurrent.DisruptorBlockingQueue"
            , "java.util.concurrent.LinkedBlockingQueue"
            /*
            , "java.util.concurrent.ArrayBlockingQueue"
            , "com.conversantmedia.util.concurrent.MPMCBlockingQueue"
            , "com.conversantmedia.util.concurrent.PushPullBlockingQueue"
            */
    })
    private String queueClass;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        queue = createBlockingQueueInstance(queueClass);
        executorService = Executors.newCachedThreadPool();
        DONE = new SharedItemJmh("#", new AtomicLong());

        for (int i = 0; i < consumerPoolSize; i++) {
            executorService.submit(new ConsumerWorkerJmh<SharedItemJmh>(queue, DONE) {
                @Override
                public void handle(SharedItemJmh item) {
                    if (VALUE.equals(item.getMsg())) {
                        item.getEventCount().incrementAndGet();
                    } else {
                        throw new IllegalStateException("Expected: " + VALUE + ". Actual: " + item.getMsg());
                    }
                }
            });
        }
        log.info("Trial STARTS");
    }

    @Setup(Level.Invocation)
    public void setupIteration() {
        eventCount = new AtomicLong();
    }

    @TearDown(Level.Trial)
    public void tearDown() throws InterruptedException {
        //stop consumers
        queue.add(DONE);
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        log.info("Trial STOPS");
    }

    @Benchmark
    public void processEvents() {
        for (int i = 0; i < targetEventCount; i++) {
            queue.add(new SharedItemJmh(VALUE, eventCount));
        }

        while (eventCount.get() < targetEventCount) {
            Thread.yield();
        }
    }

    @SuppressWarnings("unchecked")
    private BlockingQueue<SharedItemJmh> createBlockingQueueInstance(String clsName) throws Exception {
        Class<BlockingQueue<SharedItemJmh>> clazz = (Class<BlockingQueue<SharedItemJmh>>) Class.forName(clsName);
        Constructor<BlockingQueue<SharedItemJmh>> constructor = clazz.getConstructor(int.class);
        return constructor.newInstance(targetEventCount);
    }


    public static void main(String[] args) throws Exception {
        int[] threadConfigurations = {1};

        for (int numOfThreads : threadConfigurations) {
            // run benchmarks with specific number of threads
            runBenchmarks(numOfThreads);
        }
    }

    private static void runBenchmarks(int numOfThreads) throws Exception {
        final String resultFileName = "threads_x" + numOfThreads + ".csv";

        Options opts = new OptionsBuilder()
                .include(".*" + ProducerConsumerImplJmhBenchmark.class.getSimpleName() + ".*")
                .forks(1)
                .threads(numOfThreads)
                .jvmArgs("-server", "-Xms2048m", "-Xmx2048m")
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.SECONDS)
                .warmupIterations(5)
                .measurementIterations(10)
                // Use this to selectively constrain/override parameters
                // .param("consumerPoolSize", "256", "512", "1024", "2048", "4096")
                .resultFormat(ResultFormatType.JSON)
                .result(resultFileName)
                .build();

        new Runner(opts).run();
    }
}
