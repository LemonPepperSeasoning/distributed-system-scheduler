package raspberry.scheduler;

import raspberry.scheduler.algorithm.Algorithm;
import raspberry.scheduler.algorithm.astar.Astar;
import raspberry.scheduler.algorithm.astar.AstarParallel;
import raspberry.scheduler.algorithm.bNb.BNB2;
import raspberry.scheduler.algorithm.bNb.BNBParallel;
import raspberry.scheduler.algorithm.common.OutputSchedule;
import raspberry.scheduler.cli.CLIConfig;
import raspberry.scheduler.cli.CLIParser;
import raspberry.scheduler.cli.exception.ParserException;
import raspberry.scheduler.graph.IGraph;
import raspberry.scheduler.io.GraphReader;
import raspberry.scheduler.io.Logger;
import raspberry.scheduler.io.Writer;
import raspberry.scheduler.app.*;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.*;

public class Main {
    public static final boolean COLLECT_STATS_ENABLE = true;
    private static double _startTime;
    final Duration timeout = Duration.ofSeconds(30);
    private static IGraph _graph;
    private static CLIConfig _config;
    private static ThreadPoolExecutor _threadpool;

    public static void main(String[] inputs) throws NumberFormatException {
        
        try {
            _config = CLIParser.parser(inputs);
            GraphReader reader = new GraphReader(_config.getDotFile());

            // Start visualisation if appropriate argument is given.
            if (_config.getVisualise()) {
                startVisualisation(_config, reader);
            } else {
                _graph = reader.read();
                if (COLLECT_STATS_ENABLE) {
                    for(int i = 0; i<10; i++) {
                        startCollectStats();
                    }
                } else {
                    Algorithm algo = new Astar(_graph, _config.get_numProcessors(), Integer.MAX_VALUE);
                    OutputSchedule outputSchedule = algo.findPath();
                    Writer writer = new Writer(_config.getOutputFile(), _graph, outputSchedule);
                    writer.write();
                }


            }
        } catch (IOException | ParserException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void runOnce() throws IOException {
        _startTime = System.nanoTime();
        BNBParallel algo = new BNBParallel(_graph, _config.get_numProcessors(),Integer.MAX_VALUE, _config.getNumCores());
        algo.findPath();
        //      _threadpool = algo.getThreadPool();
        Logger.log("BNB Parallel", _config.getDotFile(), _config.get_numProcessors(), Double.toString((System.nanoTime() - _startTime)/1000000000.0));

    }

    private static void startCollectStats() throws IOException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future future = executor.submit(() -> {
            try {
                runOnce();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        try {
            future.get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            Logger.log("BNB Parallel", _config.getDotFile(), _config.get_numProcessors(), "Timeout - 30 seconds");
            System.out.println("process killed");
            //_threadpool.shutdownNow();
            future.cancel(true);
        }catch( InterruptedException | ExecutionException e){
            System.out.println("exception has been thrown");
        } finally{
            executor.shutdownNow();
        }
    }

    private static void startVisualisation(CLIConfig config, GraphReader reader) {
//        new Thread(()-> {
        App.main(config, reader);
//        }).start();
    }
}