package graphalgos;

/**
 * Vertex representation
 * 
 * @author abhinav.sunderrajan
 *
 */
public class Vertex {
    private String label;
    private boolean visited;

    public Vertex(String label) {
	this.label = label;
	this.visited = false;
    }

    @Override
    public boolean equals(Object o) {
	if (o instanceof Vertex)
	    return ((Vertex) o).label.equals(this.label);
	else
	    return false;
    }

    @Override
    public int hashCode() {
	return this.label.hashCode();
    }

    @Override
    public String toString() {
	return this.label;
    }

    public String getLabel() {
	return label;
    }

    public void setLabel(String label) {
	this.label = label;
    }

    public boolean isVisited() {
	return visited;
    }

    public void setVisited(boolean visited) {
	this.visited = visited;
    }

}
