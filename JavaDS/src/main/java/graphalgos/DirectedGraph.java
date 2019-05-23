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

    private Map<Vertex, ArrayList<Vertex>> adjacencyList;

    public DirectedGraph() {
	adjacencyList = new HashMap<>();
    }

    /**
     * Add a {@link Vertex} to the graph.
     * 
     * @param vertex
     */
    public void addVertex(Vertex vertex) {
	adjacencyList.put(vertex, new ArrayList<Vertex>(50));

    }

    /**
     * Add an edge from the source
     * 
     * @param source
     * @param destination
     */
    public void addEdge(Vertex source, Vertex destination) {
	if (adjacencyList.containsKey(source) && adjacencyList.containsKey(destination))
	    adjacencyList.get(source).add(destination);
	else
	    throw new IllegalArgumentException("Source or destination vertex does not exist in the Graph");

    }

    /**
     * Basically prints the adjacency list.
     */
    public void printGraph() {
	for (Entry<Vertex, ArrayList<Vertex>> entry : adjacencyList.entrySet())
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
	    for (Vertex n : adjacencyList.get(node)) {
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

	for (Vertex n : adjacencyList.get(source)) {
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
	Queue<ArrayList<Vertex>> q = new LinkedList<>();
	ArrayList<Vertex> path = new ArrayList<>();
	path.add(source);
	q.add(path);
	while (!q.isEmpty()) {
	    path = q.poll();
	    Vertex last = path.get(path.size() - 1);

	    // if last vertex is the desired destination
	    // then print the path
	    if (last.equals(dest))
		System.out.println(path);

	    for (Vertex n : adjacencyList.get(last)) {
		if (!path.contains(n)) {
		    n.setVisited(true);
		    ArrayList<Vertex> newPath = new ArrayList<>(path);
		    newPath.add(n);
		    q.add(newPath);
		}
	    }

	}

    }

}
