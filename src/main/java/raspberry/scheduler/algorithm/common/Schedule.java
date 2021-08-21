package raspberry.scheduler.algorithm.common;


import raspberry.scheduler.algorithm.sma.MBSchedule;
import raspberry.scheduler.graph.IEdge;
import raspberry.scheduler.graph.IGraph;
import raspberry.scheduler.graph.INode;
import raspberry.scheduler.graph.exceptions.EdgeDoesNotExistException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Linked list implementation of schedule
 */
public class Schedule {

    // Attribute of linked list
    private Schedule _parent; // Parent Schedule
    private int _size; // Size of the partial schedule. # of tasks scheduled.

    // item that stored in linked list
    private ScheduledTask _scheduledTask;
    private int _maxPid;
    private Hashtable<INode, Integer> _inDegreeTable;

    public Schedule(ScheduledTask scheduledTask){
        _size = 1;
        _scheduledTask = scheduledTask;
    }



    public Schedule(Schedule parentSchedule, ScheduledTask scheduledTask){
        _parent = parentSchedule;
        _scheduledTask = scheduledTask;
        _size = parentSchedule.getSize() + 1;
//
//        if (pare)


    }

//    public Schedule createSubSchedule(ScheduledTask scheduledTask){
//        return null;
//    }

//    /**
//     * Create sub-schedule using the calling class as parent
//     * @deprecated
//     * @param scheduledTask task to be schedule
//     * @return sub-schedule
//     */
//    public Schedule createSubSchedule(ScheduledTask scheduledTask){
//        return new Schedule(this, scheduledTask);
//    }
//
    /**
     * Create sub-schedule using the calling class as parent
     * @param scheduledTask scheduled task with start time
     * @param dependencyGraph dependencyGraph
     * @return sub-schedule
     */
    public Schedule createSubSchedule(ScheduledTask scheduledTask, IGraph dependencyGraph){
        Schedule subSchedule = new Schedule(this, scheduledTask);
        subSchedule.setInDegreeTable(
                subSchedule.inDegreeTableWithoutTask(scheduledTask.getTask(),dependencyGraph));
        return subSchedule;
    }

    /**
     * Gets the full path of the partial schedule.
     * (as Schedule instance is linked with parents like linked list)
     *
     * @return : Hashtable :  key : task (INode)
     * Value : List of Integers. ( size of 3 )
     * index 0 : start time of the task
     * index 1 : finsih time of the task
     * index 2 : processor id of the task.
     */
    public Hashtable<INode, int[]> getPath() {
        Hashtable<INode, int[]> tmp;
        if (_parent == null) {
            tmp = new Hashtable<INode, int[]>();
        } else {
            tmp = _parent.getPath();
        }
        tmp.put(_scheduledTask.getTask(), new int[]{
                _scheduledTask.getStartTime(),
                _scheduledTask.getFinishTime(),
                _scheduledTask.getProcessorID()});
        return tmp;
    }


    /* ------------------------------
     *  Getter and Setter
     *
     * ------------------------------
     */
    public Schedule getParent() {
        return _parent;
    }

    public void setParent(Schedule _parent) {
        this._parent = _parent;
    }

    public int getSize() {
        return _size;
    }

    public void setSize(int _size) {
        this._size = _size;
    }

    public void setScheduledTask(ScheduledTask _scheduledTask) {
        this._scheduledTask = _scheduledTask;
    }

    public int getMaxPid() {
        return _maxPid;
    }

    public void setMaxPid(int _maxPid) {
        this._maxPid = _maxPid;
    }

    /**
     * get inDegreeTable
     * @return  inDegreeTable how many parents left for every available task
     */
    public Hashtable<INode, Integer> getInDegreeTable() {
        return _inDegreeTable;
    }

    /**
     * set inDegreeTable
     * @param inDegreeTable how many parents left for every available task
     */
    public void setInDegreeTable(Hashtable<INode, Integer> inDegreeTable) {
        _inDegreeTable = inDegreeTable;
    }



    /* ============================================================
     *  Duplicate schedule detection
     * 
     * ============================================================
     */
    /**
     * Return the last scheduled task in schedule
     * @return last scheduled task
     */
    public ScheduledTask getScheduledTask() {
        return _scheduledTask;
    }

    /**
     *  Return the scheduled task if the task is scheduled
     *  else return null
     * @param task the task that is scheduled
     * @return the scheduled task
     */
    public ScheduledTask getScheduledTask(INode task ){
        Schedule cSchedule = this;
        while (cSchedule != null){
            if (cSchedule.getScheduledTask().getTask() == task){
                return cSchedule.getScheduledTask();
            }
            cSchedule = cSchedule.getParent();
        }
        return null;
    }


    /**
     * todo: new method, please test @see dup-detection
     * @param processorID
     * @return
     */
    public ArrayList<ScheduledTask> getAllTaskInProcessor(int processorID) {
        Schedule cSchedule = this;
        ArrayList<ScheduledTask> result = new ArrayList<>();
        while (cSchedule != null){
            if (cSchedule.getScheduledTask().getProcessorID() == processorID){
                result.add(cSchedule.getScheduledTask());
            }
            cSchedule = cSchedule.getParent();
        }

        return result;
    }

    /**
     * pop the child x, and recalculate dependency
     * @param dependencyGraph graph which contain the task dependency
     * @param task task to be scheduled
     * @return table after popping the child
     */
    public Hashtable<INode, Integer> inDegreeTableWithoutTask(INode task, IGraph dependencyGraph){
        Hashtable<INode, Integer> temp;
        if (this.getParent()!= null){
            temp = new Hashtable<INode, Integer>(this.getParent().getInDegreeTable());
        } else {
            temp = dependencyGraph.getInDegreeCountOfAllNodes();
        }
        temp.remove(task);
        dependencyGraph.getOutgoingEdges(task.getName()).forEach( edge ->
                temp.put( edge.getChild(),  temp.get(edge.getChild()) - 1 ));
        return temp;
    }


    /*
     *      duplicate detection refactor
     */
    /**
     * Computes the earliest time we can schedule a task in a specific processor.
     *
     * @param processorId      : the specific processor we want to schedule task into.
     * @param nodeToBeSchedule : node/task to be scheduled.
     * @return Integer : representing the earliest time. (start time)
     */
    public int calculateEarliestStartTime(int processorId, INode nodeToBeSchedule, IGraph graph) {
        // Find last finish parent node
        // Find last finish time for current processor id.
        Schedule last_processorId_use = null; //last time processor with "processorId" was used.
        Schedule cParentSchedule = this;

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

        cParentSchedule = this;
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
