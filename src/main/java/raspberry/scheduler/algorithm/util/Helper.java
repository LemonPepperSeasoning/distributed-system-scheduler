package raspberry.scheduler.algorithm.util;

import raspberry.scheduler.algorithm.astar.ScheduleAStar;
import raspberry.scheduler.algorithm.bNb.ScheduleB;
import raspberry.scheduler.algorithm.common.Schedule;
import raspberry.scheduler.algorithm.sma.MBSchedule;
import raspberry.scheduler.graph.IEdge;
import raspberry.scheduler.graph.IGraph;
import raspberry.scheduler.graph.INode;
import raspberry.scheduler.graph.exceptions.EdgeDoesNotExistException;

import java.util.ArrayList;
import java.util.Hashtable;

public class Helper {

    /**
     * Print the path of the schedule
     * @param schedule
     */
    public static void printPath(MBSchedule schedule){
        System.out.println("");
        Hashtable<INode, int[]> path = schedule.getPath();

        ArrayList<INode> list = new ArrayList(path.keySet());
        list.sort((node1, node2) -> Integer.compare(path.get(node1)[0], path.get(node2)[0]) );
        for (INode i: list){
            System.out.printf("%s : {start:%d}, {finish:%d}, {p_id:%d} \n",
                    i.getName(), path.get(i)[0], path.get(i)[1], path.get(i)[2]);
        }
    }

    public static void printPath(ScheduleB schedule){
        System.out.println(schedule);
        Hashtable<INode, int[]> path = schedule.getPath();

        ArrayList<INode> list = new ArrayList(path.keySet());
        list.sort((node1, node2) -> Integer.compare(path.get(node1)[0], path.get(node2)[0]) );
        for (INode i: list){
            System.out.printf("%s : {start:%d}, {finish:%d}, {p_id:%d} \n",
                    i.getName(), path.get(i)[0], path.get(i)[1], path.get(i)[2]);
        }
    }

    /**
     * Print the path to command line/ terminal.
     *
     * @param x : Partial schedule to print.
     */
    public static void printPath(ScheduleAStar x) {
        System.out.println("");
        Hashtable<INode, int[]> path = x.getPath();
        //path.sort((o1, o2) -> o1.node.getName().compareTo(o2.node.getName()));
        for (INode i : path.keySet()) {
            System.out.printf("%s : {start:%d}, {finish:%d}, {p_id:%d} \n",
                    i.getName(), path.get(i)[0], path.get(i)[1], path.get(i)[2]);
        }
    }

    /**
     * Uti
     * @param table
     */
    public void printHashTable(Hashtable<INode, Integer> table){
        System.out.printf("{ ");
        for (INode i: table.keySet()){
            System.out.printf("%s_%d, ", i.getName(), table.get(i));
        }
        System.out.printf(" }\n");
    }

//    /**
//     * Gets the full path of the partial schedule.
//     * (as Schedule instance is linked with parents like linked list)
//     *
//     * @return : Hashtable :  key : task (INode)
//     * Value : List of Integers. ( size of 3 )
//     * index 0 : start time of the task
//     * index 1 : finsih time of the task
//     * index 2 : processor id of the task.
//     */
//    public Hashtable<INode, int[]> getPath() {
//        Hashtable<INode, int[]> tmp;
//        if (_parent == null) {
//            tmp = new Hashtable<INode, int[]>();
//        } else {
//            tmp = _parent.getPath();
//        }
//        tmp.put(_node, new int[]{_startTime, _finishTime, _pid});
//        return tmp;
//    }

    /*
     *      duplicate detection refactor
     */
    /**
     * Computes the earliest time we can schedule a task in a specific processor.
     *
     * @param parentSchedule   : parent schedule of this partial schedule.
     * @param processorId      : the specific processor we want to schedule task into.
     * @param nodeToBeSchedule : node/task to be scheduled.
     * @return Integer : representing the earliest time. (start time)
     */
    public static int calculateEarliestStartTime(Schedule parentSchedule, int processorId, INode nodeToBeSchedule, IGraph graph) {
        // Find last finish parent node
        // Find last finish time for current processor id.
        Schedule last_processorId_use = null; //last time processor with "processorId" was used.
        Schedule cParentSchedule = parentSchedule;

        while (cParentSchedule != null) {
            if (cParentSchedule.getScheduledTask().getProcessorID() == processorId) {
                last_processorId_use = cParentSchedule;
                break;
            }
            cParentSchedule = cParentSchedule.getParent();
        }

        //last time parent was used. Needs to check for all processor.
        int finished_time_of_last_parent = 0;
        if (last_processorId_use != null) {
            finished_time_of_last_parent = last_processorId_use.getScheduledTask().getFinishTime();
        }

        cParentSchedule = parentSchedule;
        while (cParentSchedule != null) {
            // for edges in current parent scheduled node
            INode last_scheduled_node = cParentSchedule.getScheduledTask().getTask();
            for (IEdge edge : graph.getOutgoingEdges(last_scheduled_node.getName())) {

                // if edge points to  === childNode
                if (edge.getChild() == nodeToBeSchedule && cParentSchedule.getScheduledTask().getProcessorID() != processorId) {
                    //last_parent_processor[ cParentSchedule.p_id ] = true;
                    try {
                        int communicationWeight = graph.getEdgeWeight(cParentSchedule.getScheduledTask().getTask(), nodeToBeSchedule);
                        //  finished_time_of_last_parent  <
                        if (finished_time_of_last_parent < (cParentSchedule.getScheduledTask().getFinishTime() + communicationWeight)) {
                            finished_time_of_last_parent = cParentSchedule.getScheduledTask().getFinishTime() + communicationWeight;
                        }
                    } catch (EdgeDoesNotExistException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
            cParentSchedule = cParentSchedule.getParent();
        }
        return finished_time_of_last_parent;
    }
}
