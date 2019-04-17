package networkviewer;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import networkmodel.Road;

/**
 * Class for handling mouse events for the {@link RoadNetworkVisualizer}.
 * 
 * @author abhinav.sunderrajan
 * 
 */
public class MouseBehaviour extends MouseAdapter {
    private double[] prevOffset = null;
    private Point startPoint = null;
    private RoadNetworkVisualizer viewer;
    private static double MAX_ZOOM = 320000.0;
    private static double MIN_ZOOM = 1200.0;

    public MouseBehaviour(RoadNetworkVisualizer viewer) {
	this.viewer = viewer;
    }

    public void mousePressed(MouseEvent e) {
	viewer.panel.requestFocusInWindow();

	prevOffset = new double[] { viewer.offset[0], viewer.offset[1] };
	startPoint = e.getPoint();

	if (e.getSource() instanceof JComponent) {
	    ((JComponent) e.getSource()).repaint();
	}

    }

    public void mouseDragged(MouseEvent e) {
	Point currentPoint = e.getPoint();
	int dx = startPoint.x - currentPoint.x;
	int dy = startPoint.y - currentPoint.y;

	if (SwingUtilities.isLeftMouseButton(e)) {
	    viewer.offset[0] = prevOffset[0] + dx / viewer.zoom;
	    viewer.offset[1] = prevOffset[1] + dy / viewer.zoom;

	    viewer.updateVisibleNodesAndLinks();
	    if (e.getSource() instanceof JComponent) {
		((JComponent) e.getSource()).repaint();
	    }
	}

    }

    public void mouseReleased(MouseEvent e) {

	try {
	    if (SwingUtilities.isLeftMouseButton(e)) {
		Road road = viewer.nearestRoadToMousePointer();
		if (e.isControlDown()) {
		    if (viewer.getSelectedRoads().contains(road)) {
			viewer.getSelectedRoads().remove(road);
		    } else {
			viewer.getSelectedRoads().add(road);
		    }
		}
	    }

	    viewer.updateVisibleNodesAndLinks();
	    if (e.getSource() instanceof JComponent) {
		((JComponent) e.getSource()).repaint();
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}

    }

    public void mouseWheelMoved(MouseWheelEvent e) {
	if (viewer.image != null) {
	    int units = -e.getUnitsToScroll();

	    if (units >= 0) {
		double zoom = viewer.zoom * 1.15;
		viewer.zoom = zoom > MAX_ZOOM ? MAX_ZOOM : zoom;

	    } else {
		double zoom = viewer.zoom / 1.15;
		viewer.zoom = zoom < MIN_ZOOM ? MIN_ZOOM : zoom;
	    }

	    viewer.updateVisibleNodesAndLinks();
	    if (e.getSource() instanceof JComponent) {
		((JComponent) e.getSource()).repaint();
	    }
	}
    }

    public void mouseMoved(MouseEvent e) {

	int x = e.getX();
	int y = e.getY();
	viewer.setMousePosition(x, y);

	if (e.getSource() instanceof JComponent) {
	    ((JComponent) e.getSource()).repaint();
	}

    }

    public static double getMAX_ZOOM() {
	return MAX_ZOOM;
    }

    public static double getMIN_ZOOM() {
	return MIN_ZOOM;
    }

}