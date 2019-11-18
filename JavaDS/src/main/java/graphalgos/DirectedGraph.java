package graphalgos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

/**
 * A directed graph defined by its adjacency list. Also note that the vertices
 * belonging to {@link Vertex} class maintain the state such as visited.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class DirectedGraph {

    private Map<Vertex, ArrayList<Edge>> adjacencyList;

    public DirectedGraph() {
	adjacencyList = new HashMap<>();
    }

    /**
     * Add a {@link Vertex} to the graph.
     * 
     * @param vertex
     */
    public void addVertex(Vertex vertex) {
	adjacencyList.put(vertex, new ArrayList<Edge>(20));

    }

    /**
     * Add a weighted edge from source to destination.
     * 
     * @param source
     * @param destination
     * @param weight
     */
    public void addEdge(Vertex source, Vertex destination, double weight) {
	if (adjacencyList.containsKey(source) && adjacencyList.containsKey(destination))
	    adjacencyList.get(source).add(new Edge(source, destination, weight));
	else
	    throw new IllegalArgumentException("Source or destination vertex does not exist in the Graph");

    }

    /**
     * Basically prints the adjacency list.
     */
    public void printGraph() {
	for (Entry<Vertex, ArrayList<Edge>> entry : adjacencyList.entrySet())
	    System.out.println(entry.getKey().getLabel() + " --> " + entry.getValue());
    }

    /**
     * Breadth first traversal of the graph from the vertex passed as a parameter.
     * 
     * @param source
     */
    public void traverseAndPrintBFS(Vertex source) {
	Queue<Vertex> q = new LinkedList<>();
	q.add(source);
	source.setVisited(true);
	while (!q.isEmpty()) {
	    Vertex node = q.poll();
	    node.setVisited(true);
	    for (Edge e : adjacencyList.get(node)) {
		Vertex n = e.getDestination();
		if (!n.isVisited()) {
		    n.setVisited(true);
		    q.add(n);
		}
	    }
	    System.out.print(node.getLabel() + " ");

	}

    }

    /**
     * Depth first traversal of the graph from the source vertex passed as a
     * parameter.
     * 
     * @param source
     */
    public void traverseAndPrintDFS(Vertex source) {
	System.out.print(source.getLabel() + " ");
	source.setVisited(true);
	if (adjacencyList.get(source).size() == 0)
	    return;

	for (Edge e : adjacencyList.get(source)) {
	    Vertex n = e.getDestination();
	    if (!n.isVisited())
		traverseAndPrintDFS(n);
	}

    }

    /**
     * Find all paths between source and destination vertices. Uses breadth first
     * search implementation.
     * 
     * @param source
     * @param dest
     */
    public void findAllPaths(Vertex source, Vertex dest) {
	Queue<ArrayList<Vertex>> pathQueue = new LinkedList<>();
	ArrayList<Vertex> path = new ArrayList<>();
	path.add(source);
	pathQueue.add(path);
	while (!pathQueue.isEmpty()) {
	    path = pathQueue.poll();
	    Vertex last = path.get(path.size() - 1);

	    // if last vertex is the desired destination
	    // then print the path
	    if (last.equals(dest))
		System.out.println(path);

	    for (Edge e : adjacencyList.get(last)) {
		Vertex next = e.getDestination();
		if (!path.contains(next)) {
		    next.setVisited(true);
		    ArrayList<Vertex> newPath = new ArrayList<>(path);
		    newPath.add(next);
		    pathQueue.add(newPath);
		}
	    }

	}

    }

    private boolean isCyclicUtil(Vertex vertex) {
	// Mark the current node as visited and
	// part of recursion stack
	if (vertex.isPartOfRecStack())
	    return true;

	if (vertex.isVisited())
	    return false;

	vertex.setVisited(true);
	vertex.setPartOfRecStack(true);

	for (Edge e : adjacencyList.get(vertex)) {
	    Vertex dest = e.getDestination();
	    if (isCyclicUtil(dest))
		return true;
	}

	vertex.setPartOfRecStack(false);
	return false;
    }

    /**
     * Returns true if the graph contains a cycle, else false. This function is a
     * variation of DFS().
     * 
     * @return true if graph is
     */
    public boolean isCyclic() {

	// Call the recursive helper function to
	// detect cycle in different DFS trees
	for (Vertex vertex : adjacencyList.keySet())
	    if (isCyclicUtil(vertex))
		return true;

	return false;
    }

    public Map<Vertex, ArrayList<Edge>> getAdjacencyList() {
	return adjacencyList;
    }

}
