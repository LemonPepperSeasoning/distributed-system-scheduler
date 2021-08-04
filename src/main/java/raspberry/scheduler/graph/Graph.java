package raspberry.scheduler.graph;


import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Graph implements IGraph{
    private String name;
    public Hashtable<String, INode> nodes;
    public Hashtable<String, List<IEdge>> InDegreeAdjacencyList;
    public Hashtable<String, List<IEdge>> OutDegreeAdjacencyList;
    private Hashtable<String,Integer> _criticalPathWeightTable;

    /**
     * Constructor
     * @param name name of the graph
     */
    public Graph(String name){
        this.name = name;
        nodes = new Hashtable<String, INode>();
        InDegreeAdjacencyList = new Hashtable<String, List<IEdge>>();
        OutDegreeAdjacencyList = new Hashtable<String, List<IEdge>>();
    }

    @Override
    public INode getNode(String id) {
        return nodes.get(id);
    }

    @Override
    public void addNode(String id, int value) {
        INode node = new Node(id, value);
        InDegreeAdjacencyList.put(id, new ArrayList<IEdge>());
        OutDegreeAdjacencyList.put(id, new ArrayList<IEdge>());
        nodes.put(id,node);
    }

    @Override
    public void addEdge(String parentNodeID, String childNodeID, int weight) {
        INode p = nodes.get(parentNodeID);
        INode c = nodes.get(childNodeID);
        IEdge e = new Edge(p, c, weight);
        OutDegreeAdjacencyList.get(parentNodeID).add(e);
        InDegreeAdjacencyList.get(childNodeID).add(e);
    }

    @Override
    public List<IEdge> getOutgoingEdges(String id) {
        return OutDegreeAdjacencyList.get(id);
    }

    @Override
    public List<IEdge> getIngoingEdges(String id) {
        return InDegreeAdjacencyList.get(id);
    }

    @Override
    public int getEdgeWeight(INode parent, INode child) throws EdgeDoesNotExistException {
        for (IEdge edge : OutDegreeAdjacencyList.get(parent.getName())){
            if (edge.getChild() == child){
                return edge.getWeight();
            }
        }
        throw new EdgeDoesNotExistException("Edge does not exists");
    }

    @Override
    public Collection<INode> getAllNodes() {
        return this.nodes.values();
    }

    @Override
    public int getNumNodes() {
        return this.nodes.size();
    }


    @Override
    public String toString(){
    	StringBuilder output = new StringBuilder("Graph: " + this.name + "\n");
    	for (String name: OutDegreeAdjacencyList.keySet()) {
    	    String key = name.toString();
    	    String value = OutDegreeAdjacencyList.get(name).toString();
    	    output.append("Node:")
                    .append(key)
                    .append(" cost=")
                    .append(nodes.get(key).getValue())
                    .append(" ")
                    .append(value)
                    .append("\n");
    	}
        return output.toString();
    }


    @Override
    public Hashtable<INode,Integer> getCriticalPathWeightTable(){
        _criticalPathWeightTable = new Hashtable<String, Integer>();
        ArrayList<String> start = new ArrayList<String>();
        InDegreeAdjacencyList.forEach( (k,v) -> {
            if (v.size() == 0){
                start.add(k);
            }
            _criticalPathWeightTable.put(k,-1);
        });
        start.forEach(node -> {
            int val = dfs(node);
            if (_criticalPathWeightTable.containsKey(node)){
                _criticalPathWeightTable.put(node, Math.max(_criticalPathWeightTable.get((node)), val));
            } else {
                _criticalPathWeightTable.put(node, val);
            }
        });

        System.out.println("table" + _criticalPathWeightTable);

        Hashtable<INode, Integer> result = new Hashtable<INode, Integer>();
        _criticalPathWeightTable.forEach((k,v) -> result.put(nodes.get(k), v - nodes.get(k).getValue()));
        System.out.println("result" + result);
        return result;
    }

    private int dfs(String node){
        //System.out.println(node);
        //System.out.println(_criticalPathWeightTable);
        List<IEdge> edges = getOutgoingEdges(node);
        int computeTime = nodes.get(node).getValue();
        if (edges.size() == 0 ){
            _criticalPathWeightTable.put(node,computeTime);
            //System.out.println("return");
            return computeTime;
        } else {
            AtomicInteger currentMax = new AtomicInteger(_criticalPathWeightTable.get(node));
            edges.forEach(edge -> {
                currentMax.set(Math.max(currentMax.get(),computeTime + dfs(edge.getChild().getName())));
            });
            _criticalPathWeightTable.put(node,currentMax.intValue());
            return currentMax.intValue();
        }


    }
//    // This path would be optimal solution for 1 processor scheduling.
//    public Stack getTopologicalOrder_DFS(){
//        //Compute topological order and return it.
//        toVisit = new ArrayList<Node>( adjacencyList.keySet() );
//        topologicalOrder = new Stack();
//        while ( ! toVisit.isEmpty() ){
//            recursiveTopological( toVisit.get(0) );
//        }
//        return topologicalOrder;
//    }
//
//    // Recursive function to compute topological order.
//    public void recursiveTopological(Node x){
//        if ( ! adjacencyList.get(x).isEmpty() ){
//            for ( Edge i : adjacencyList.get(x) ){
//                if ( toVisit.contains(i.childNode)) {
//                    recursiveTopological(i.childNode);
//                }
//            }
//        }
//        toVisit.remove(x);
//        topologicalOrder.push(x);
//    }
//
//    public void getTopologicalOrder_BFS(){
//        //Yet to be implemented.
//        return;
//    }

}
