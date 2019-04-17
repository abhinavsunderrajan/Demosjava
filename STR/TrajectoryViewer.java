package utils;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONObject;

import medianspeed.ColorHelper;
import networkmodel.RoadNetworkModel;
import networkviewer.OSMRoadNetworkViewer;

/**
 * This trajectory viewer helps visualize the actual vehicle trajectory and the
 * route that was inferred.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class TrajectoryViewer extends OSMRoadNetworkViewer {
    private BufferedImage background = null;
    private int backgroundXOffset = -1;
    private int backgroundYOffset = -1;
    private List<JSONObject> vehiclePositions;
    private static final Random RAND = new Random();

    /**
     * View the vehicle trajectory and the map matched route.
     * 
     * @param title
     * @param roadTableName
     * @throws Exception
     */
    public TrajectoryViewer(String title, RoadNetworkModel model) throws Exception {
	super(title, model);
	vehiclePositions = new ArrayList<>();
    }

    @Override
    public void updateView() {
	super.updateView();
	Graphics g = image.getGraphics();
	Graphics2D g2 = (Graphics2D) g;
	int xo = panel.getWidth() / 2;
	int yo = panel.getHeight() / 2;

	int seq = 0;

	synchronized (lock) {
	    for (JSONObject dataPoint : vehiclePositions) {
		g2.setColor(ColorHelper.numberToColor(dataPoint.getDouble("speed")));

		int x0 = xo + (int) (zoom * (dataPoint.getDouble("lon") - offset[0]));
		int y0 = yo + (int) (zoom * (-dataPoint.getDouble("lat") - offset[1]));

		Polygon arrowHead = new Polygon();
		double angle = Math.toRadians(dataPoint.getDouble("bearing"));

		if (angle == 0.0) {
		    Ellipse2D.Double circle = new Ellipse2D.Double(x0, y0, 15, 15);
		    g2.fill(circle);
		} else {
		    Double xPrime = 8 * Math.cos(angle) + 38 * Math.sin(angle);
		    Double yPrime = 8 * Math.sin(angle) - 38 * Math.cos(angle);
		    xPrime = xPrime + x0 - 8;
		    yPrime = yPrime + y0 + 38;
		    arrowHead.addPoint(xPrime.intValue(), yPrime.intValue());
		    arrowHead.addPoint(x0 - 8, y0 + 38);

		    xPrime = 16 * Math.cos(angle);
		    yPrime = 16 * Math.sin(angle);
		    xPrime = xPrime + x0 - 8;
		    yPrime = yPrime + y0 + 38;

		    arrowHead.addPoint(xPrime.intValue(), yPrime.intValue());
		    g2.fill(arrowHead);
		}

		g.drawString(seq + "", x0 + 12, y0 + 12);
		seq++;
	    }

	}
    }

    public List<JSONObject> getVehiclePositions() {
	return vehiclePositions;
    }

}
