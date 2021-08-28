package raspberry.scheduler.algorithm;

import org.junit.Before;
import org.junit.Test;
import raspberry.scheduler.algorithm.astar.Astar;
import raspberry.scheduler.algorithm.astar.AstarParallel;
import raspberry.scheduler.algorithm.astar.WeightedAstar;
import raspberry.scheduler.algorithm.bNb.BNB;
import raspberry.scheduler.algorithm.bNb.BNBParallel;
import raspberry.scheduler.algorithm.common.OutputSchedule;
import raspberry.scheduler.cli.CLIConfig;
import raspberry.scheduler.cli.CLIParser;
import raspberry.scheduler.graph.IGraph;
import raspberry.scheduler.graph.exceptions.EdgeDoesNotExistException;
import raspberry.scheduler.io.GraphReader;
import raspberry.scheduler.io.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.fail;

public class CSVPerformanceTest {
    private String INPUT_PATH = "src/test/resources/input/";

    private enum Classes {BNB, BNBParallel, Astar, AstarParallel, WeightedAstar}

    private final List<Integer> _processorNums = new ArrayList<>(Arrays.asList(1, 2, 4, 8, 12, 16));
    private final List<Integer> _coreNums = new ArrayList<>(Arrays.asList(1, 2, 4, 8));

    private final List<String> _inputFiles = new ArrayList<>(Arrays.asList("Nodes_7_OutTree.dot", "Nodes_8_Random.dot","Nodes_9_SeriesParallel.dot", "Nodes_10_Random.dot", "Nodes_11OutTree.dot", "16_466.dot", "big.dot" ));


    @Test
    public void writeStats()throws IOException {
        for (String file : _inputFiles) {
            String filePath = INPUT_PATH.concat(file);
            for (int coreNum : _coreNums) {
                for (int processors : _processorNums) {
                    for (Classes algo : Classes.values()) {
                        for (int i = 0; i < 5; i++) {
                            readAndFindPath(filePath, processors, coreNum, algo);
                        }
                    }
                }
            }
        }
    }


    private void readAndFindPath(String filename, int numProcessors, int coreNum, Classes algo) throws
            IOException {

        GraphReader reader = new GraphReader(filename);
        IGraph graph = reader.read();


        Algorithm algorithm;
        switch (algo) {
            case BNB:
                if (coreNum != 1) {
                    return;
                }
                algorithm = new BNB(graph, numProcessors, Integer.MAX_VALUE);
                break;
            case BNBParallel:
                if (coreNum == 1) {
                    return;
                }
                algorithm = new BNBParallel(graph, numProcessors, Integer.MAX_VALUE, coreNum);
                break;
            case Astar:
                if (coreNum != 1) {
                    return;
                }
                algorithm = new Astar(graph, numProcessors, Integer.MAX_VALUE);
                break;
            case AstarParallel:
                if (coreNum == 1) {
                    return;
                }
                algorithm = new AstarParallel(graph, numProcessors, Integer.MAX_VALUE);
                break;
            case WeightedAstar:
                if (coreNum != 1) {
                    return;
                }
                algorithm = new WeightedAstar(graph, numProcessors);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + algo);
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future future = executor.submit(() -> {
            long startTime = System.nanoTime();
            algorithm.findPath();
            try {
                Logger.log(algo.toString(), filename, numProcessors, coreNum, Double.toString((System.nanoTime() - startTime) / 1000000000.0));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        try {
            future.get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            Logger.log(algo.toString(), filename, numProcessors, coreNum, "Timeout - more than 30seconds");
            System.out.println("process killed");
            //_threadpool.shutdownNow();
            future.cancel(true);
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("exception has been thrown");
        } finally {
            if( algo == Classes.AstarParallel){
                ((AstarParallel)algorithm).shutdownThreadPool();
            }else if(algo == Classes.BNBParallel){
                ((BNBParallel)algorithm).shutdownThreadPool();
            }
            executor.shutdownNow();
        }


        //long startTime = System.nanoTime();
//        OutputSchedule output = algorithm.findPath();
//        Logger.log(algo.toString(), filename, numProcessors,coreNum, Double.toString((System.nanoTime() - startTime)/1000000000.0));


    }



}
