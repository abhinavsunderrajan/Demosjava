package graphalgos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@linkplain https://www.geeksforgeeks.org/prims-minimum-spanning-tree-mst-greedy-algo-5/}
 * 
 * @author abhinav.sunderrajan
 *
 */
public class PrimsAlgo {

    // Number of vertices in the graph
    private static final int V = 9;
    // Array to store constructed MST
    private static int parent[] = new int[V];
    private static double key[] = new double[V];
    private static boolean mstSet[] = new boolean[V];

    private static DirectedGraph graph;
    private static Map<String, Vertex> vertices = new HashMap<>();

    public static void main(String[] args) {
	createGraph();
	for (int i = 0; i < V; i++) {
	    key[i] = Double.MAX_VALUE;
	    mstSet[i] = false;
	}
	// Always include first 1st vertex in MST.
	key[0] = 0; // Make key 0 so that this vertex is
		    // picked as first vertex
	parent[0] = -1; // First node is always root of MST

	// The MST will have V vertices
	for (int count = 0; count < V - 1; count++) {
	    // Pick thd minimum key vertex from the set of vertices
	    // not yet included in MST
	    int u = minKey();

	    // Add the picked vertex to the MST Set
	    mstSet[u] = true;

	    Map<Vertex, ArrayList<Edge>> adjacencyList = graph.getAdjacencyList();
	    for (Edge e : adjacencyList.get(vertices.get(u + ""))) {
		int v = Integer.parseInt(e.getDestination().toString());
		if (mstSet[v] == false && e.getWeight() < key[v]) {
		    parent[v] = u;
		    key[v] = e.getWeight();
		}
	    }
	}

	printMST();

    }

    // A utility function to print the constructed MST stored in
    // parent[]
    private static void printMST() {
	System.out.println("Edge \tWeight");
	for (int i = 1; i < V; i++) {
	    String p = parent[i] + "";
	    double weight = graph.getAdjacencyList().get(vertices.get(i + "")).stream()
		    .filter(e -> e.getDestination().toString().equals(p + "")).collect(Collectors.toList()).get(0)
		    .getWeight();
	    System.out.println(parent[i] + " - " + i + "\t" + weight);
	}
    }

    private static int minKey() {
	// Initialize min value
	double min = Double.MAX_VALUE;
	int min_index = -1;

	for (int v = 0; v < V; v++)
	    if (mstSet[v] == false && key[v] < min) {
		min = key[v];
		min_index = v;
	    }

	return min_index;
    }

    private static void createGraph() {
	graph = new DirectedGraph();

	for (int i = 0; i <= V; i++) {
	    Vertex v = new Vertex(i + "");
	    graph.addVertex(v);
	    vertices.put(i + "", v);
	}
	graph.addEdge(vertices.get("0"), vertices.get("1"), 4);
	graph.addEdge(vertices.get("0"), vertices.get("7"), 8);

	graph.addEdge(vertices.get("1"), vertices.get("0"), 4);
	graph.addEdge(vertices.get("1"), vertices.get("7"), 11);
	graph.addEdge(vertices.get("1"), vertices.get("2"), 8);

	graph.addEdge(vertices.get("2"), vertices.get("1"), 8);
	graph.addEdge(vertices.get("2"), vertices.get("8"), 2);
	graph.addEdge(vertices.get("2"), vertices.get("3"), 7);
	graph.addEdge(vertices.get("2"), vertices.get("5"), 4);

	graph.addEdge(vertices.get("3"), vertices.get("2"), 7);
	graph.addEdge(vertices.get("3"), vertices.get("5"), 14);
	graph.addEdge(vertices.get("3"), vertices.get("4"), 9);

	graph.addEdge(vertices.get("4"), vertices.get("3"), 9);
	graph.addEdge(vertices.get("4"), vertices.get("5"), 10);

	graph.addEdge(vertices.get("5"), vertices.get("2"), 4);
	graph.addEdge(vertices.get("5"), vertices.get("3"), 14);
	graph.addEdge(vertices.get("5"), vertices.get("4"), 10);
	graph.addEdge(vertices.get("5"), vertices.get("6"), 2);

	graph.addEdge(vertices.get("6"), vertices.get("7"), 1);
	graph.addEdge(vertices.get("6"), vertices.get("8"), 6);
	graph.addEdge(vertices.get("6"), vertices.get("5"), 2);

	graph.addEdge(vertices.get("7"), vertices.get("0"), 8);
	graph.addEdge(vertices.get("7"), vertices.get("1"), 11);
	graph.addEdge(vertices.get("7"), vertices.get("8"), 7);
	graph.addEdge(vertices.get("7"), vertices.get("6"), 1);

	graph.addEdge(vertices.get("8"), vertices.get("7"), 7);
	graph.addEdge(vertices.get("8"), vertices.get("2"), 2);
	graph.addEdge(vertices.get("8"), vertices.get("6"), 6);

    }

}
