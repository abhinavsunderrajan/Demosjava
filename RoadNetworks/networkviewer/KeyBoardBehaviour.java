package networkviewer;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;

import javax.imageio.ImageIO;

import networkmodel.Road;

/**
 * Class for handling all keyboard events for {@link RoadNetworkVisualizer}.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class KeyBoardBehaviour implements KeyListener {

    private RoadNetworkVisualizer viewer;
    private StringBuffer buffer = new StringBuffer("");

    public KeyBoardBehaviour(RoadNetworkVisualizer viewer) {
	this.viewer = viewer;

    }

    @Override
    public void keyPressed(KeyEvent event) {
	// for enter
	if (event.getKeyChar() == '\n') {
	    if (isParsable(buffer.toString())) {
		Road road = viewer.model.getAllRoadsMap().get(Long.parseLong(buffer.toString()));
		if (road != null)
		    viewer.selectedRoads.add(road);
		else
		    System.err.println("Road id: " + buffer.toString() + " does not exist");

	    } else
		System.err.println("String: " + buffer.toString() + " is not a valid road id");
	    buffer.setLength(0);
	} else {
	    buffer.append(event.getKeyChar());
	}
    }

    @Override
    public void keyReleased(KeyEvent event) {
	if (event.isControlDown()) {
	    switch (event.getKeyCode()) {
	    // Represents character p
	    case 80:
		viewer.getSelectedRoads().forEach(road -> {
		    System.out.println(road + "\t" + road.getBeginNode().getY() + "," + road.getBeginNode().getX()
			    + "\t" + road.getRoadType());
		});

		break;

	    // represents character c
	    case 67:
		if (viewer.nearestRoad != null) {
		    StringSelection stringSelection = new StringSelection(
			    viewer.nearestRoad.getBeginNode().getY() + "," + viewer.nearestRoad.getBeginNode().getX());
		    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		    clipboard.setContents(stringSelection, null);
		}
		break;

	    // represents character s, save the panel as an image
	    case 83:
		BufferedImage bi = new BufferedImage(viewer.getPanel().getWidth(), viewer.getPanel().getHeight(),
			BufferedImage.TYPE_INT_RGB);
		viewer.getPanel().paint(bi.getGraphics());
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		File file = new File("src/main/resources/road-network-view " + ts.toString() + ".jpg");
		try {
		    ImageIO.write(bi, "jpg", file);
		    Desktop.getDesktop().open(file);
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		break;

	    }

	}

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    public static boolean isParsable(String input) {
	boolean parsable = true;
	try {
	    Long.parseLong(input);
	} catch (NumberFormatException e) {
	    parsable = false;
	}
	return parsable;
    }

}
