/*
 *  MyShape
 *
 */
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

// simple shape class
class MyShape {

	// shape model
	ArrayList<Point2d> points;
	ArrayList<Point2d> buffer;
	Boolean isFilled = false; // shape is polyline or polygon
	Boolean isClosed = false; // polygon is filled or not
	Color colour = Color.BLACK;
	float strokeThickness = 3.0f;

	public Color getColour() {
		return colour;
	}

	public void setColour(Color colour) {
		this.colour = colour;
	}

	public float getStrokeThickness() {
		return strokeThickness;
	}

	public void setStrokeThickness(float strokeThickness) {
		this.strokeThickness = strokeThickness;
	}

	public Boolean getIsFilled() {
		return isFilled;
	}

	public void setIsFilled(Boolean isFilled) {
		this.isFilled = isFilled;
	}

	public Boolean getIsClosed() {
		return isClosed;
	}

	public void setIsClosed(Boolean isClosed) {
		this.isClosed = isClosed;
	}

	// for selection
	boolean isSelected;
	boolean isMove;

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	// for drawing
	Boolean hasChanged = false; // dirty bit if shape geometry changed
	int[] x_points, y_points;

	public float rotation = 0;
	public double scale = 1.0;

	// replace all points with array
	public void setPoints(double[][] pts) {
		points = new ArrayList<Point2d>();
		for (double[] p : pts) {
			points.add(new Point2d(p[0], p[1]));
		}
		hasChanged = true;
	}

	// move the points according to the window resizing
	public void movePoints(float height, float width) {
		for (int i = 0; i < points.size(); i++) {
			points.get(i).x = points.get(i).x + width;
			points.get(i).y = points.get(i).y + height;
		}
		hasChanged = true;
		isMove = true;
	}

	// add a point to end of shape
	public void addPoint(double x, double y) {
		if (points == null)
			points = new ArrayList<Point2d>();
		points.add(new Point2d(x, y));
		hasChanged = true;
	}

	ArrayList<Point2d> getPoints() {
		return points;
	}
	
	// get the closest distance among all the line segments of the shape
	public double getClosestDistance(int x, int y) {
		Point2d M = new Point2d(x, y);
		double distance = -1;

		for (int i = 0; i < points.size() - 1; i++) {
			Point2d P1 = new Point2d(points.get(i));
			Point2d P2 = new Point2d(points.get(i + 1));

			Point2d CP = closestPoint(M, P1, P2);

			double temp = M.distance(CP);

			if (distance == -1 || temp < distance)
				distance = temp;
		}

		return distance;
	}

	// initialize the angle and scale for the next transformation
	public void setTransform(double angle, double ratio) {
		rotation = (float) angle;
		scale = ratio;
		hasChanged = true;
	}

	// update the points of the shape from a buffer array
	public void copyBuffer() {
		if (buffer.size() != 0) {
			points.clear();
			for (int i = 0; i < buffer.size(); i++) {
				Point2d temp = new Point2d(buffer.get(i).x, buffer.get(i).y);
				points.add(temp);
			}
			buffer.clear();
		}
	}

	// paint the shape
	public void paint(Graphics2D g2) {

		double originX = points.get(0).x;
		double originY = points.get(0).y;

		AffineTransform oldTransform = g2.getTransform();
		AffineTransform S = AffineTransform.getScaleInstance(scale, scale);
		AffineTransform R = AffineTransform.getRotateInstance(Math
				.toRadians(rotation));
		AffineTransform T1 = AffineTransform.getTranslateInstance(-originX,
				-originY);
		AffineTransform T2 = AffineTransform.getTranslateInstance(originX,
				originY);

		// update the shape in java Path2D object if it changed
		if (hasChanged) {
			x_points = new int[points.size()];
			y_points = new int[points.size()];
			if (buffer == null)
				buffer = new ArrayList<Point2d>();
			if (buffer.size() != 0)
				buffer.clear();

			if (isMove) {
				for (int i = 0; i < points.size(); i++) {
					x_points[i] = (int) points.get(i).x;
					y_points[i] = (int) points.get(i).y;
				}
				isMove = false;
			} else {
				for (int i = 0; i < points.size(); i++) {
					Point2d transferedPoint = transform(
							T2,
							transform(R,
									transform(S, transform(T1, points.get(i)))));

					buffer.add(transferedPoint);

					x_points[i] = (int) transferedPoint.x;
					y_points[i] = (int) transferedPoint.y;
				}
			}
			hasChanged = false;
		}

		// don't draw if path2D is empty (not shape)
		if (x_points != null) {
			// special draw for selection
			if (isSelected) {
				g2.setColor(Color.YELLOW);
				g2.setStroke(new BasicStroke(strokeThickness * 4));
				if (isClosed)
					g2.drawPolygon(x_points, y_points, points.size());
				else
					g2.drawPolyline(x_points, y_points, points.size());
			}

			g2.setColor(colour);

			// call right drawing function
			if (isFilled) {
				g2.fillPolygon(x_points, y_points, points.size());
			} else {
				g2.setStroke(new BasicStroke(strokeThickness));
				if (isClosed)
					g2.drawPolygon(x_points, y_points, points.size());
				else
					g2.drawPolyline(x_points, y_points, points.size());

			}
		}
		g2.setTransform(oldTransform);
	}

	// find closest point
	static Point2d closestPoint(Point2d M, Point2d P1, Point2d P2) {
		Vector2d v = new Vector2d();
		v.sub(P2, P1); // v = P2 - P1

		// early out if line is less than 1 pixel long
		if (v.lengthSquared() < 0.5)
			return P1;

		Vector2d u = new Vector2d();
		u.sub(M, P1); // u = M - P1

		// scalar of vector projection ...
		double s = u.dot(v) / v.dot(v);

		// find point for constrained line segment
		if (s < 0) {
			return P1;
		} else if (s > 1) {
			return P2;
		} else {
			Point2d I = P1;
			Vector2d w = new Vector2d();
			w.scale(s, v); // w = s * v
			I.add(w); // I = P1 + w;

			return I;
		}
	}

	// return perpendicular vector
	static public Vector2d perp(Vector2d a) {
		return new Vector2d(-a.y, a.x);
	}

	// line-line intersection
	// return (NaN,NaN) if not intersection, otherwise returns intersecting
	// point
	static Point2d lineLineIntersection(Point2d P0, Point2d P1, Point2d Q0,
			Point2d Q1) {

		// TODO: implement

		return new Point2d();
	}

	// affine transform helper
	// return P_prime = T * P
	Point2d transform(AffineTransform T, Point2d P) {
		Point2D.Double p = new Point2D.Double(P.x, P.y);
		Point2D.Double q = new Point2D.Double();
		T.transform(p, q);
		return new Point2d(q.x, q.y);

	}

	// hit test with this shape
	public boolean hittest(double x, double y) {
		if (points != null) {

			// TODO Implement

		}

		return false;
	}
}
