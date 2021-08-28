package raspberry.scheduler.algorithm;

import org.junit.Before;
import org.junit.Test;
import raspberry.scheduler.cli.CLIConfig;
import raspberry.scheduler.cli.CLIParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CSVPerformanceTest {
    private String INPUT_PATH = "src/test/resources/input/";

    private final List<String> _inputFiles = new ArrayList<>(Arrays.asList("Nodes_7_OutTree.dot","Nodes_8_Random.dot","Nodes_9_SeriesParallel.dot","Nodes_10_Random.dot","Nodes_11OutTree.dot","16_466.dot","big.dot"));


    @Test
    public void test2Processors1Core(){

    }
}
