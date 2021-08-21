package raspberry.scheduler.algorithm.heuristic;

import org.junit.Test;
import raspberry.scheduler.algorithm.OutputChecker;
import raspberry.scheduler.algorithm.astar.Astar;
import raspberry.scheduler.algorithm.astar.WeightedAstar;
import raspberry.scheduler.algorithm.bNb.BNB2;
import raspberry.scheduler.algorithm.common.OutputSchedule;
import raspberry.scheduler.graph.IGraph;
import raspberry.scheduler.graph.exceptions.EdgeDoesNotExistException;
import raspberry.scheduler.io.GraphReader;

import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestHeuristicFunction {

    private String INPUT_PATH = "src/test/resources/input/";

    /**
     * Test performance of A* algorithm and correctness of output
     * Name: Nodes_10_Random.dot
     * Expected total Time for schedule: 50
     * @throws FileNotFoundException file does not exists
     */
    @Test
    public void testNodes10Random4Processor() throws FileNotFoundException, EdgeDoesNotExistException {
        // read input graph and find path
        OutputSchedule output = readAndFindPath("Nodes_10_Random.dot", 4);
        assertEquals(50, output.getFinishTime());
    }

    /**
     * Helper method to read the file and run a star
     * with specified number of processor.
     * Do validity check upon finish
     * @param filename filename of the dot file of dependency graph
     * @param numProcessors number of resource available to allocate to task
     * @return output schedule
     * @throws FileNotFoundException if file does not exists
     * @throws EdgeDoesNotExistException if get edges yield error
     */
    private OutputSchedule readAndFindPath(String filename, int numProcessors) throws
            FileNotFoundException, EdgeDoesNotExistException {

        // read graph
        GraphReader reader = new GraphReader(INPUT_PATH+ filename);
        IGraph graph = reader.read();

        // run and time a* algorithm
        long startTime = System.nanoTime();

        Astar astar = new Astar(graph,numProcessors, Integer.MAX_VALUE);
        OutputSchedule output = astar.findPath();

        System.out.printf("------------------------\n" +
                        "File: %s, Number of Processor: %d \nRUNNING TIME : %.2f seconds\n",
                filename, numProcessors, (System.nanoTime() - startTime) / 1000000000.0);

        // check if output violate any dependency
        if (!OutputChecker.isValid(graph,output)){
            fail("Schedule is not valid");
        }

        return output;
    }
}
