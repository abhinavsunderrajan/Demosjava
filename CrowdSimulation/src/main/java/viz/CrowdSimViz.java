package viz;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import entities.Agent;
import main.CrowdSimCore;

public class CrowdSimViz extends Visualizer {

    public CrowdSimViz(CrowdSimCore core) {
	super("Social Force", core);
    }

    public void updateView() {

	int pwidth = panel.getWidth();
	int pheight = panel.getHeight();

	image = new BufferedImage(pwidth, pheight, BufferedImage.TYPE_INT_RGB);

	Graphics g = image.getGraphics();
	Graphics2D g2 = (Graphics2D) g;
	g2.setColor(Color.WHITE);
	g2.fillRect(0, 0, pwidth, pheight);

	synchronized (syncLock) {
	    for (Agent agent : crowdSim.getAgents()) {

		g2.setColor(Color.red);
		int x0 = (int) (agent.getPosition().x);
		int y0 = (int) (agent.getPosition().y);
		g2.fillOval(x0, y0, 10, 10);
		// Polygon arrowHead = new Polygon();
		// double angle = Math.PI / 2.0 + agent.getVelocity().angle();
		// Double xPrime = 8 * Math.cos(angle) + 38 * Math.sin(angle);
		// Double yPrime = 8 * Math.sin(angle) - 38 * Math.cos(angle);
		// xPrime = xPrime + x0 - 8;
		// yPrime = yPrime + y0 + 38;
		// arrowHead.addPoint(xPrime.intValue(), yPrime.intValue());
		// arrowHead.addPoint(x0 - 8, y0 + 38);
		//
		// xPrime = 16 * Math.cos(angle);
		// yPrime = 16 * Math.sin(angle);
		// xPrime = xPrime + x0 - 8;
		// yPrime = yPrime + y0 + 38;
		//
		// arrowHead.addPoint(xPrime.intValue(), yPrime.intValue());
		// g2.fill(arrowHead);
	    }
	}

    }

}
