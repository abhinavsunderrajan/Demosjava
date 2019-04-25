package viz;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

import main.CrowdSimCore;

/**
 * The abstract class for visualization.
 * 
 * @author abhinav
 * 
 */
public abstract class Visualizer {

    protected Object syncLock;
    protected Panel panel;
    protected JFrame frame;
    protected BufferedImage image;
    protected double zoom = 1.0;
    protected CrowdSimCore crowdSim;

    public class Panel extends JPanel {
	private static final long serialVersionUID = 1L;

	public Dimension getMinimumSize() {
	    return new Dimension(CrowdSimCore.ARENA_WIDTH, CrowdSimCore.ARENA_HEIGHT);
	}

	public Dimension getPreferredSize() {
	    return new Dimension(CrowdSimCore.ARENA_WIDTH, CrowdSimCore.ARENA_HEIGHT);
	}

	protected void paintComponent(Graphics g) {
	    super.paintComponent(g);

	    if (crowdSim.getAgents().size() > 2) {
		updateView();

		synchronized (syncLock) {
		    if (image != null) {
			g.drawImage(image, 0, 0, null);
		    }
		}
	    }
	}
    }

    /**
     * 
     * @param title
     * @param dbConnectionProperties
     */
    public Visualizer(String title, CrowdSimCore core) {
	this.crowdSim = core;
	syncLock = new Object();
	panel = new Panel();
	panel.setFocusable(true);
	panel.requestFocusInWindow();

	frame = new JFrame(title);
	frame.setLocation(10, 10);
	frame.setDefaultCloseOperation(3);
	frame.setLayout(new BorderLayout());
	frame.add(panel, BorderLayout.CENTER);
	frame.pack();

	frame.setVisible(true);
	frame.repaint();

    }

    public abstract void updateView();

    /**
     * @return the frame
     */
    public JFrame getFrame() {
	return frame;
    }

    public Object getLock() {
	return syncLock;
    }

    /**
     * @return the image
     */
    public BufferedImage getImage() {
	return image;
    }

}
