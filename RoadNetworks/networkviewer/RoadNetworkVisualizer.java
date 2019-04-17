package networkviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

import networkmodel.Road;
import networkmodel.RoadNetworkModel;
import networkmodel.RoadNode;

/**
 * The abstract class for visualizing the road network. Extend it accordingly to
 * visualize and overlay data as per requirements. This class provides
 * functionalities such as pan and zoom. Mouse and keyboard events.
 * 
 * @author abhinav.sunderrajan
 * 
 */
public abstract class RoadNetworkVisualizer {

    private List<String> zoomRoadTypes;

    protected RoadNetworkModel model;
    protected Road nearestRoad = null;
    protected Object lock = new Object();
    protected int prevMousePosX = 0;
    protected int prevMousePosY = 0;
    private double mnx = Integer.MAX_VALUE;
    private double mxx = Integer.MIN_VALUE;
    private double mny = Integer.MAX_VALUE;
    private double mxy = Integer.MIN_VALUE;
    private double width = -1;
    private double height = -1;
    protected double[] offset = null;
    protected Panel panel;
    protected JFrame frame;
    protected BufferedImage image;
    protected double zoom;
    protected Set<RoadNode> visibleNodes = new HashSet<RoadNode>();
    protected Set<Road> visibleRoads = new HashSet<Road>();
    protected boolean showDirectionArrows = true;
    protected Set<Road> selectedRoads = new HashSet<Road>();
    protected Set<RoadNode> selectedNodes = new HashSet<RoadNode>();

    // show only these roads when you are zoomed out. This is to ensure that
    // visualization is smoother.
    private static final String[] ZOOM_ARR = { "primary_link", "secondary_link", "trunk", "motorway_link", "motorway",
	    "secondary", "trunk_link", "primary", };
    // the max zoom level to show all roads
    private static final double ZOOM_LEVEL_SHOW_ALL_ROADS = 0.05;

    /**
     * {@link https://stackoverflow.com/questions/5446396/concerns-about-the-function-of-jpanel-paintcomponent}
     * Read the above link for good explanation of what is happening.
     *
     * 
     * @author abhinav.sunderrajan
     *
     */
    public class Panel extends JPanel {
	private static final long serialVersionUID = 1L;

	public Dimension getMinimumSize() {
	    return new Dimension(1200, 700);
	}

	public Dimension getPreferredSize() {
	    return new Dimension(1200, 700);
	}

	@Override
	protected void paintComponent(Graphics g) {
	    // super paint component is the like eraser. Read the brilliant stackoverflow
	    // post in the link above.

	    super.paintComponent(g);

	    if (model.getAllNodes().size() > 2) {
		synchronized (lock) {
		    updateView();
		    if (image != null)
			g.drawImage(image, 0, 0, null);

		}
	    }
	}
    }

    /**
     * 
     * @param title
     */
    public RoadNetworkVisualizer(String title, RoadNetworkModel model) {
	this.model = model;
	MouseBehaviour behavior = new MouseBehaviour(this);
	this.zoom = MouseBehaviour.getMIN_ZOOM();
	for (RoadNode node : model.getBeginAndEndNodes()) {
	    if (node.getX() < mnx)
		mnx = node.getX();
	    if (node.getX() > mxx)
		mxx = node.getX();
	    if (-node.getY() < mny)
		mny = -node.getY();
	    if (-node.getY() > mxy)
		mxy = -node.getY();

	    width = mxx - mnx;
	    height = mxy - mny;
	    offset = new double[] { mnx + width / 2, mny + height / 2 };
	}

	zoomRoadTypes = Arrays.asList(ZOOM_ARR);

	// select all nodes that are within the range
	visibleNodes = new HashSet<RoadNode>();

	// get all visible links
	visibleRoads = new HashSet<Road>();

	panel = new Panel();
	panel.addMouseMotionListener(behavior);
	panel.addMouseListener(behavior);
	panel.addMouseWheelListener(behavior);
	panel.setFocusable(true);
	panel.requestFocusInWindow();
	panel.addKeyListener(new KeyBoardBehaviour(this));

	frame = new JFrame(title);
	frame.setLocation(10, 10);
	frame.setDefaultCloseOperation(3);
	frame.setLayout(new BorderLayout());
	frame.add(panel, BorderLayout.CENTER);
	frame.pack();
	updateVisibleNodesAndLinks();

	frame.setVisible(true);
	frame.repaint();

    }

    /**
     * Method for updating the visible roads and nodes at the current level of zoom.
     */
    public void updateVisibleNodesAndLinks() {
	// visibleNodes.clear();
	visibleRoads.clear();

	double vwidth = panel.getWidth() / zoom;
	double vheight = panel.getHeight() / zoom;

	double mnx = offset[0] - 0.5 * vwidth;
	double mxx = offset[0] + 0.5 * vwidth;
	double mny = offset[1] - 0.5 * vheight;
	double mxy = offset[1] + 0.5 * vheight;

	// a much faster operation
	model.getAllRoadsMap().values().parallelStream().forEach(road -> {
	    for (RoadNode node : road.getRoadNodes()) {
		if (node.getX() >= mnx && node.getX() <= mxx && -node.getY() >= mny && -node.getY() <= mxy) {
		    if (vwidth < ZOOM_LEVEL_SHOW_ALL_ROADS && vheight < ZOOM_LEVEL_SHOW_ALL_ROADS) {
			visibleRoads.add(road);
		    } else {
			if (zoomRoadTypes.contains(road.getRoadType()))
			    visibleRoads.add(road);
		    }
		    break;
		}
	    }
	});

    }

    /**
     * Implement method to visualize roads and other geo-spatial data. This method
     * is called when the frame for the visualization is repainted.
     */
    public abstract void updateView();

    public void setMousePosition(int x, int y) {
	this.prevMousePosX = x;
	this.prevMousePosY = y;
	double vwidth = panel.getWidth() / zoom;
	double vheight = panel.getHeight() / zoom;

	// show the nearest road to mouse pointer only after a certain zoom level to
	// minimize unnecessary searching.
	if (offset != null && vwidth < ZOOM_LEVEL_SHOW_ALL_ROADS && vheight < ZOOM_LEVEL_SHOW_ALL_ROADS)
	    nearestRoad = nearestRoadToMousePointer();
    }

    /**
     * Draw road
     * 
     * @param road
     * @param g
     */
    protected void drawRoad(Road road, Graphics g) {

	List<? extends RoadNode> nodeIds = road.getRoadNodes();
	for (int i = 0; i < nodeIds.size() - 1; i++) {

	    RoadNode node1 = nodeIds.get(i);
	    RoadNode node2 = nodeIds.get(i + 1);

	    int[] xy0 = lonLatToVizXY(node1.getX(), node1.getY());
	    int[] xy1 = lonLatToVizXY(node2.getX(), node2.getY());
	    if (g.getColor() == Color.ORANGE) {
		g.setFont(new Font("TimesRoman", Font.BOLD, 22));
		g.drawString("" + road.getRoadId(), (xy0[0] + xy1[0]) / 2, (xy0[1] + xy1[1]) / 2);
	    }

	    g.drawLine(xy0[0], xy0[1], xy1[0], xy1[1]);
	    if (showDirectionArrows) {

		if (road.isOneWay()) {
		    this.drawArrowFromLine(g, xy0[0], xy0[1], xy1[0], xy1[1], 0.5 * Math.PI);
		    this.drawArrowFromLine(g, xy0[0], xy0[1], xy1[0], xy1[1], 1.5 * Math.PI);
		} else {
		    this.drawArrowFromLine(g, xy0[0], xy0[1], xy1[0], xy1[1], 0.5 * Math.PI);
		    this.drawArrowFromLine(g, xy0[0], xy0[1], xy1[0], xy1[1], 1.5 * Math.PI);
		    this.drawArrowFromLine(g, xy1[0], xy1[1], xy0[0], xy0[1], 0.5 * Math.PI);
		    this.drawArrowFromLine(g, xy1[0], xy1[1], xy0[0], xy0[1], 1.5 * Math.PI);
		}
	    }

	}

    }

    /**
     * Draw a polygon usually representing a planning area
     * 
     * @param polygon
     * @param g       the graphics object used by the renderer.
     */
    protected void drawPolygon(Polygon polygon, Graphics g) {

	Coordinate[] coords = polygon.getCoordinates();
	for (int i = 0; i < coords.length - 1; i++) {

	    int[] xy0 = lonLatToVizXY(coords[i].x, coords[i].y);
	    int[] xy1 = lonLatToVizXY(coords[i + 1].x, coords[i + 1].y);

	    g.drawLine(xy0[0], xy0[1], xy1[0], xy1[1]);

	}

    }

    private void drawArrowFromLine(Graphics g, int x0, int y0, int x1, int y1, double a) {
	double xm = (x0 + x1) / 2;
	double ym = (y0 + y1) / 2;

	double xx1 = xm - x0;
	double yy1 = ym - y0;

	double xx3 = xx1 * Math.cos(a) - yy1 * Math.sin(a) + x0;
	double yy3 = xx1 * Math.sin(a) + yy1 * Math.cos(a) + y0;

	double dx = xx3 - xm;
	double dy = yy3 - ym;
	double d = Math.hypot(dx, dy);

	double f = (zoom / 20000.0) / d;
	if (Double.isInfinite(f))
	    return;

	dx *= f;
	dy *= f;

	double xx4 = xm + dx;
	double yy4 = ym + dy;

	g.drawLine((int) xm, (int) ym, (int) xx4, (int) yy4);
    }

    /**
     * Return the road nearest to the mouse pointer.
     * 
     * @return
     */
    public Road nearestRoadToMousePointer() {

	Double nearestD = null;
	Road nearest = null;
	for (Road link : visibleRoads) {

	    for (int i = 1; i < link.getRoadNodes().size(); i++) {
		int[] xy1 = lonLatToVizXY(link.getRoadNodes().get(i - 1).getX(), link.getRoadNodes().get(i - 1).getY());
		int[] xy2 = lonLatToVizXY(link.getRoadNodes().get(i).getX(), link.getRoadNodes().get(i).getY());

		double a = prevMousePosX - xy1[0];
		double b = prevMousePosY - xy1[1];
		double c = xy2[0] - xy1[0];
		double d = xy2[1] - xy1[1];

		double dot = a * c + b * d;
		double lenSq = c * c + d * d;
		double param = dot / lenSq;

		double xx = xy1[0] + param * c;
		double yy = xy1[1] + param * d;

		if (param < 0 || (xy1[0] == xy2[0] && xy1[1] == xy2[1])) {
		    xx = xy1[0];
		    yy = xy1[1];
		} else if (param > 1) {
		    xx = xy2[0];
		    yy = xy2[1];
		}

		double ddx = prevMousePosX - xx;
		double ddy = prevMousePosY - yy;
		double dd = Math.sqrt(ddx * ddx + ddy * ddy);

		if (nearestD == null || dd < nearestD) {
		    nearestD = dd;
		    nearest = link;
		}

	    }

	}

	return nearest;
    }

    public Panel getPanel() {
	return panel;
    }

    public Set<Road> getSelectedRoads() {
	return selectedRoads;
    }

    public Set<RoadNode> getSelectedNodes() {
	return selectedNodes;
    }

    public RoadNetworkModel getModel() {
	return model;
    }

    /**
     * @return the frame
     */
    public JFrame getMapFrame() {
	return frame;
    }

    public Object getLock() {
	return lock;
    }

    /**
     * @return the image
     */
    public BufferedImage getImage() {
	return image;
    }

    /**
     * Convert latitude longitude corresponding XY in the visualization
     * 
     * @param lon longitude
     * @param lat latitude
     * @return xy in the viz
     */
    protected int[] lonLatToVizXY(double lon, double lat) {

	int x = panel.getWidth() / 2 + (int) (zoom * (lon - offset[0]));
	int y = panel.getHeight() / 2 + (int) (zoom * (-lat - offset[1]));
	return new int[] { x, y };

    }

    /**
     * Convert xy coordinates on the visualization to actual lon/lat coordinates.
     * 
     * @param x
     * @param y
     * @return
     */
    protected double[] vizXYToLonLat(int x, int y) {
	double lon = (x - panel.getWidth() / 2.0) / (double) zoom + offset[0];
	double lat = -((x - panel.getHeight() / 2.0) / (double) zoom + offset[1]);

	return new double[] { lon, lat };

    }

}
