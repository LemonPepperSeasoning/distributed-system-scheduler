package raspberry.scheduler.algorithm.astar;

import raspberry.scheduler.algorithm.common.OutputSchedule;
import raspberry.scheduler.algorithm.common.ScheduledTask;
import raspberry.scheduler.algorithm.common.Solution;
import raspberry.scheduler.app.visualisation.model.AlgoObservable;
import raspberry.scheduler.graph.IEdge;
import raspberry.scheduler.graph.IGraph;
import raspberry.scheduler.graph.INode;
import raspberry.scheduler.graph.exceptions.EdgeDoesNotExistException;

import java.util.*;

public class AstarVisualiser extends Astar{

    protected AlgoObservable _observable;

    /**
     * Constructor for A*
     *
     * @param graphToSolve  : graph to solve (graph represents the task and dependencies)
     * @param numProcessors : number of processor we can use to schedule tasks.
     */
    public AstarVisualiser(IGraph graphToSolve, int numProcessors, int upperBound) {
        super(graphToSolve,numProcessors,upperBound);
        _observable = AlgoObservable.getInstance();
    }

    public AstarVisualiser(IGraph graphToSolve, int numProcessors) {
        super(graphToSolve,numProcessors);
    }

    /**
     * Compute the optimal scheduling
     *
     * @return OutputSchedule : the optimal path/scheduling.
     */
    @Override
    public OutputSchedule findPath() {
        /*
         * find the path
         * "master" stores, schedule and its counterTable.
         * "rootTable" is the table all counterTable is based of off.
         * --> stores a node and number of incoming edges.
         */
        getH();
        getHadvanced();

//        Hashtable<Schedule, Hashtable<INode, Integer>> master = new Hashtable<Schedule, Hashtable<INode, Integer>>();
        Hashtable<INode, Integer> rootTable = this.getRootTable();

        for (INode node : rootTable.keySet()) {
            if (rootTable.get(node) == 0) {

//                ScheduleAStar newSchedule = new ScheduleAStar(
//                        0, null, node, 1, getChildTable(rootTable, node));
//
                ScheduleAStar newSchedule = new ScheduleAStar(
                        new ScheduledTask(1,node, 0),
                        getChildTable(rootTable, node)
                );

                newSchedule.addHeuristic(
                        Collections.max(Arrays.asList(
                                0,
                                h(newSchedule),
                                h1(getChildTable(rootTable, node), newSchedule),
                                h2(newSchedule)
                        )));
//                master.put(newSchedule, getChildTable(rootTable, i));
                _pq.add(newSchedule);
            }
        }

        ScheduleAStar cSchedule;
        int duplicate = 0; // Duplicate counter, Used for debugging purposes.
        _observable.setIterations(0);
        _observable.setIsFinish(false);
        //  System.out.println(_observable.getIterations());
        while (true) {
//            System.out.printf("PQ SIZE: %d\n", _pq.size());
            _observable.increment();
            //System.out.println(_observable.getIterations());
            cSchedule = _pq.poll();

            Solution cScheduleSolution = new Solution(cSchedule, _numP);
            _observable.setSolution(cScheduleSolution);

            ArrayList<ScheduleAStar> listVisitedForSize = _visited.get(cSchedule.getHash());

            if (listVisitedForSize != null && isIrrelevantDuplicate(listVisitedForSize, cSchedule)) {
                duplicate++;
                continue;
            } else {
                if (listVisitedForSize == null) {
                    listVisitedForSize = new ArrayList<ScheduleAStar>();
                    _visited.put(cSchedule.getHash(), listVisitedForSize);
                }
                listVisitedForSize.add(cSchedule);
            }

            // Return if all task is scheduled
            if (cSchedule.getSize() == _numNode) {
                break;
            }
//            Hashtable<INode, Integer> cTable = master.get(cSchedule);
//            master.remove(cSchedule);
            Hashtable<INode, Integer> cTable = cSchedule._inDegreeTable;
            // Find the next empty processor. (
            int currentMaxPid = cSchedule.getMaxPid();
            int pidBound;
            if (currentMaxPid + 1 > _numP) {
                pidBound = _numP;
            } else {
                pidBound = currentMaxPid + 1;
            }
            for (INode node : cTable.keySet()) {
                if (cTable.get(node) == 0) {
                    for (int pid = 1; pid <= pidBound; pid++) {
                        int start = calculateEarliestStartTime(cSchedule, pid, node);


                        Hashtable<INode, Integer> newTable = getChildTable(cTable, node);

                        ScheduleAStar newSchedule = new ScheduleAStar(
                                cSchedule,
                                new ScheduledTask(pid, node, start),
                                newTable);

                        newSchedule.addHeuristic(
                                Collections.max(Arrays.asList(
                                        0,
                                        h(newSchedule),
                                        h1(newTable, newSchedule),
                                        h2(newSchedule)
                                )));

                        if (newSchedule.getTotal() <= _upperBound){
                            ArrayList<ScheduleAStar> listVisitedForSizeV2 = _visited.get(newSchedule.getHash());
                            if (listVisitedForSizeV2 != null && isIrrelevantDuplicate(listVisitedForSizeV2, newSchedule)) {
                                duplicate++;
                            }else{
                                _pq.add(newSchedule);
                            }
                        }
                    }
                }
            }
        }
        System.out.printf("PQ SIZE: %d\n", _pq.size());
        System.out.printf("\nDUPLCIATE : %d\n", duplicate);

        _observable.setIsFinish(true);
        _observable.setSolution(new Solution(cSchedule,_numP));

        return new Solution(cSchedule, _numP);
    }

}

