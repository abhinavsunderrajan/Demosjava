package main;

import java.util.HashMap;
import java.util.Map;

import graphalgos.DirectedGraph;
import graphalgos.Vertex;

/**
 * Main class for simple graph operations.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class DirectedGraphDemo {

    public static void main(String[] args) {
	DirectedGraph graph = new DirectedGraph();
	Map<String, Vertex> vertices = new HashMap<>();
	for (int i = 0; i < 5; i++) {
	    Vertex v = new Vertex(i + "");
	    graph.addVertex(v);
	    vertices.put(i + "", v);
	}

	graph.addEdge(vertices.get("0"), vertices.get("1"));
	graph.addEdge(vertices.get("0"), vertices.get("4"));
	graph.addEdge(vertices.get("1"), vertices.get("2"));
	graph.addEdge(vertices.get("1"), vertices.get("3"));
	graph.addEdge(vertices.get("1"), vertices.get("4"));
	graph.addEdge(vertices.get("2"), vertices.get("3"));
	graph.addEdge(vertices.get("3"), vertices.get("4"));

	graph.printGraph();
	System.out.println("__________________________");
	graph.traverseAndPrintBFS(vertices.get("0"));
	vertices.values().stream().forEach(v -> v.setVisited(false));

	System.out.println("\n__________________________");
	graph.traverseAndPrintDFS(vertices.get("0"));
    }

}
