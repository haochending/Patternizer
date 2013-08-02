/*
 * Patternizer
 * 
 * Haochen Ding ID: 20329251
 *
 */
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

// create the window and run the demo
public class Patternizer extends JPanel implements MouseInputListener {

	static MyShape shape;
	private static Point M;
	private static Point Mouse;
	private static int curHeight;
	private static int curWidth;
	private static int ColorIndex;
	private static Color[] colors = new Color[] { Color.CYAN, Color.GREEN,
			Color.BLUE, Color.RED, Color.ORANGE, Color.MAGENTA };

	private static ArrayList<ArrayList<MyShape>> replications;

	private static ArrayList<MyShape> shapes;

	// parameters for the center circle
	private static int diameter = 24;
	// the stroke thickness of the line to be drawn
	private static float strokeThickness;

	// boolean value to indicate if the line is dragged from the center circle
	private static boolean isInCircle;
	// boolean value to indicate if the dragging process finished
	private static boolean finishDragging;
	// boolean value to indicate if we entered the scaling mode
	private static boolean scalingMode;
	// index of the selected shape
	private static int selectedShape;
	// last angle of dragging
	private static double lastAngle;
	// if we need to draw mouse point
	private static boolean mousePoint;
	// the total angle rotated
	private static double totalAngle;
	// the length of the original shape
	private static double originalLength;
		

	Patternizer() {
		// add listeners
		addMouseListener(this);
		addMouseMotionListener(this);
		
		strokeThickness = 3.0f;
	}

	public static void main(String[] args) {
		// create the window
		Patternizer canvas = new Patternizer();
		final Dimension expectedDimension = new Dimension(600, 600);
		
		canvas.setBackground(Color.BLACK);

		ColorIndex = 0;
		isInCircle = true;
		finishDragging = true;
		shapes = new ArrayList<MyShape>();
		replications = new ArrayList<ArrayList<MyShape>>();
		M = new Point();
		Mouse = new Point();
		selectedShape = -1;
		mousePoint = false;
		
		

		canvas.addComponentListener(new ComponentListener() {

			@Override
			public void componentHidden(ComponentEvent evt) {
			}

			@Override
			public void componentMoved(ComponentEvent evt) {
			}

			@Override
			public void componentResized(ComponentEvent evt) {
				// handle the window resizing and centralizing the panel
				Component c = (Component) evt.getSource();
				float HeightDiff = c.getHeight() - curHeight;
				float WidthDiff = c.getWidth() - curWidth;
				curHeight = c.getHeight();
				curWidth = c.getWidth();

				for (int i = 0; i < shapes.size(); i++) {
					shapes.get(i).movePoints(HeightDiff / 2, WidthDiff / 2);

					if (replications.get(i).size() != 0) {
						for (int j = 0; j < replications.get(i).size(); j++) {
							replications.get(i).get(j)
									.movePoints(HeightDiff / 2, WidthDiff / 2);
						}
					}
				}
			}

			@Override
			public void componentShown(ComponentEvent evt) {
			}

		});

		JFrame f = new JFrame("Patternizer"); // jframe is the app window
		
		// create a label
		// (we'll use this to display the stroke thickness)
		final JLabel label = new JLabel("Adjust the stroke thickness of the line");
		// set some properties to customize how it looks
		label.setPreferredSize(new Dimension(600, 30));
		label.setHorizontalAlignment( SwingConstants.CENTER );
		label.setBackground(Color.WHITE);
		label.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		label.setFont(new Font("SansSerif", Font.PLAIN, 14));
		label.setForeground(Color.WHITE);
		
		// create a slider
		JSlider slider = new JSlider(0, 100, 50);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider s = (JSlider)e.getSource();
				
				int percentage = s.getValue();
				if ((0 <= percentage) && (percentage < 20)) {
					strokeThickness = 1.0f;
				} else if ((20 <= percentage) && (percentage < 40)) {
					strokeThickness = 2.0f;
				} else if ((40 <= percentage) && (percentage < 60)) {
					strokeThickness = 3.0f;
				} else if ((60 <= percentage) && (percentage < 80)) {
					strokeThickness = 4.0f;
				} else {
					strokeThickness = 5.0f;
				}
				
				label.setText("strokeThickness " + strokeThickness);
			}
		});
		
		f.setLayout(new FlowLayout());
		
		// add the widgets
		canvas.add(label);
		canvas.add(slider);
		
		f.setSize(expectedDimension); // window size
		curHeight = expectedDimension.height;
		curWidth = expectedDimension.width;
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setContentPane(canvas); // add canvas to jframe
		
		f.setVisible(true); // show the window
	}

	// custom graphics drawing
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g; // cast to get 2D drawing methods
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // antialiasing
																// look nicer
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(colors[ColorIndex]);
		g2.fillOval(curWidth / 2 - diameter / 2, curHeight / 2 - diameter / 2,
				diameter, diameter);

		// draw mouse point
		if (mousePoint) {
			g2.setColor(Color.GRAY);
			g2.setStroke(new BasicStroke(2.0f));
			int s = 15;
			g2.drawOval(Mouse.x - s, Mouse.y - s, 2 * s, 2 * s);
		}

		if (shapes.size() != 0) {
			for (int i = 0; i < shapes.size(); i++) {

				shapes.get(i).paint(g2);
				if (replications.get(i).size() != 0) {
					for (int j = 0; j < replications.get(i).size(); j++) {
						replications.get(i).get(j).paint(g2);
					}
				}
			}
		}
		// shape.paint(g2);
		g2.setColor(colors[ColorIndex]);

		g2.fillOval(curWidth / 2 - diameter / 2, curHeight / 2 - diameter / 2,
				diameter, diameter);
	}

	@Override
	public void mouseClicked(MouseEvent evt) {
		int x = evt.getX();
		int y = evt.getY();
		Mouse.x = x;
		Mouse.y = y;

		int left = (int) (Math.pow(x - curWidth / 2, 2) + Math.pow(y
				- curHeight / 2, 2));
		int right = (int) Math.pow(diameter / 2, 2);

		if (left <= right) {
			// change the color of the shape to be drawn
			mousePoint = true;

			if (evt.getClickCount() >= 2) {
				shapes.clear();
				for (int i = 0; i < replications.size(); i++) {
					replications.get(i).clear();
				}
				replications.clear();
			} else {
				if (ColorIndex == 5) {
					ColorIndex = 0;
				} else {
					ColorIndex++;
				}
			}
		} else {
			if (evt.getClickCount() >= 2) {
				// change the color of the selected shape
				
				int index = -1;
				Random generator = new Random();
				int r = generator.nextInt(6); // generate random color index
				for (int i = 0; i < shapes.size(); i++) {
					if (shapes.get(i).isSelected()) {
						index = i;
					}
				}

				if (index >= 0) {
					shapes.get(index).setColour(colors[r]);
					for (int j = 0; j < replications.get(index).size(); j++) {
						replications.get(index).get(j).setColour(colors[r]);
					}
				}
			}
		}
		repaint();
	}

	@Override
	public void mouseEntered(MouseEvent evt) {
	}

	@Override
	public void mouseExited(MouseEvent evt) {
	}

	@Override
	public void mousePressed(MouseEvent evt) {
		int x = evt.getX();
		int y = evt.getY();
		M.x = x;
		M.y = y;
		Mouse.x = x;
		Mouse.y = y;

		int left = (int) (Math.pow(x - curWidth / 2, 2) + Math.pow(y
				- curHeight / 2, 2));
		int right = (int) Math.pow(diameter / 2, 2);

		// unselect the previous selected shape
		for (int i = 0; i < shapes.size(); i++) {
			if (shapes.get(i).isSelected)
				shapes.get(i).setSelected(false);
		}

		// if the press is in the circle area
		if (left <= right) {
			shape = new MyShape();

			shape.setIsClosed(false);
			shape.setIsFilled(false);
			shape.setColour(colors[ColorIndex]);
			shape.setStrokeThickness(strokeThickness);

			isInCircle = true;

			repaint();
		} else {
			isInCircle = false;

			if (shapes.size() == 0)
				return;

			double distance = -1;
			selectedShape = -1;

			for (int i = 0; i < shapes.size(); i++) {
				MyShape shapeBuffer = shapes.get(i);
				double temp = shapeBuffer.getClosestDistance(x, y);

				if (distance == -1 || temp < distance) {
					distance = temp;
					selectedShape = i;
				}
			}
			// System.out.println("Original: "+distance);
			if (replications.size() != 0) {
				for (int j = 0; j < replications.size(); j++) {
					for (int k = 0; k < replications.get(j).size(); k++) {
						MyShape shapeBuffer = replications.get(j).get(k);
						double temp = shapeBuffer.getClosestDistance(x, y);

						if (temp < distance) {
							distance = temp;
							selectedShape = j;
						}
					}
				}
				// System.out.println("Rep: "+distance);
			}
			if (distance <= 5.0 && distance >= 0) {
				shapes.get(selectedShape).setSelected(true);
				scalingMode = true;

				totalAngle = 0;
			}

			repaint();
		}

	}

	@Override
	public void mouseReleased(MouseEvent evt) {
		mousePoint = false;
		Mouse.x = evt.getX();
		Mouse.y = evt.getY();
		if (isInCircle) {
			finishDragging = true;
		}

		if (scalingMode) {
			// update the points for the selected shape and its replications
			shapes.get(selectedShape).copyBuffer();

			for (int i = 0; i < replications.get(selectedShape).size(); i++) {
				replications.get(selectedShape).get(i).copyBuffer();
			}
		}
		scalingMode = false;

		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent evt) {
		int x = evt.getX();
		int y = evt.getY();
		Mouse.x = x;
		Mouse.y = y;

		if (isInCircle) {
			// if the line is dragged from the circle, add it to the shape lists
			// and record the points of the line
			if (finishDragging) {
				shapes.add(shape);
				ArrayList<MyShape> repList = new ArrayList<MyShape>();
				replications.add(repList);
				shape.setSelected(true);
				finishDragging = false;
			}

			shape.addPoint(x, y);
			mousePoint = true;
			repaint();
		}

		if (scalingMode) {
			mousePoint = true;
			
			// calculate the angle and ratio of lengths between 2 vectors
			Point2d P1 = new Point2d(M.x, M.y);
			Point2d P2 = new Point2d(x, y);
			Point2d c = new Point2d(curWidth / 2, curHeight / 2);

			Vector2d v = new Vector2d();
			v.sub(P1, c);
			Vector2d u = new Vector2d();
			u.sub(P2, c);

			if (totalAngle == 0) {
				originalLength = v.length();
			}

			M.x = x;
			M.y = y;

			double angle = v.angle(u);
			double ratio = u.length() / originalLength;

			angle = Math.toDegrees(Math.atan2(v.x * u.y - v.y * u.x, v.x * u.x
					+ v.y * u.y));
			totalAngle = totalAngle + angle;
			// System.out.println(totalAngle);

			// prevent the rotation angle smaller than 0 degree and larger than
			// 360 degree
			if ((totalAngle < 0) || (totalAngle >= 360)) {
				shapes.get(selectedShape).setTransform(0, ratio);
				repaint();
				return;
			}

			// transform the original one
			shapes.get(selectedShape).setTransform(totalAngle, ratio);
			// do the replication
			if (totalAngle > 5) {
				// the number of the circular array
				int numReps = (int) (360 / totalAngle);
				// System.out.println("numReps: " + numReps);

				replications.get(selectedShape).clear();
				for (int i = 0; i < numReps; i++) {
					MyShape shapeRep = new MyShape();

					// initialize the replication
					shapeRep.setIsClosed(false);
					shapeRep.setIsFilled(false);
					shapeRep.setColour(shapes.get(selectedShape).getColour());
					ArrayList<Point2d> buffer = shapes.get(selectedShape)
							.getPoints();
					for (int j = 0; j < buffer.size(); j++) {
						shapeRep.addPoint(buffer.get(j).x, buffer.get(j).y);
					}
					replications.get(selectedShape).add(shapeRep);
					shapeRep.setTransform((i + 2) * totalAngle, ratio);
					// System.out.println("i: " + i);
					// System.out.println((i + 2) * angle);
					repaint();
				}
			}

			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent evt) {
		// used just for the mouse point circle
		mousePoint = false;
		Mouse.x = evt.getX();
		Mouse.y = evt.getY();
		repaint();
	}
}
