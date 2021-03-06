/*
 * Copyright (c) 1998-2020 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.utility;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/** Provides geometry-related utilities. */
public class Geometry {
    private static final Point2D[] NO_LINE_INTERSECTION = new Point2D[0];

    /**
     * Intersects two {@link Rectangle}s, producing a third. Unlike the {@link
     * Rectangle#intersection(Rectangle)} method, the resulting {@link Rectangle}'s width & height
     * will not be set to less than zero when there is no overlap.
     *
     * @param first  The first {@link Rectangle}.
     * @param second The second {@link Rectangle}.
     * @return The intersection of the two {@link Rectangle}s.
     */
    public static Rectangle intersection(Rectangle first, Rectangle second) {
        if (first.width < 1 || first.height < 1 || second.width < 1 || second.height < 1) {
            return new Rectangle();
        }
        int x = Math.max(first.x, second.x);
        int y = Math.max(first.y, second.y);
        int w = Math.min(first.x + first.width, second.x + second.width) - x;
        int h = Math.min(first.y + first.height, second.y + second.height) - y;
        if (w < 0 || h < 0) {
            return new Rectangle();
        }
        return new Rectangle(x, y, w, h);
    }

    /**
     * Unions two {@link Rectangle}s, producing a third. Unlike the {@link
     * Rectangle#union(Rectangle)} method, an empty {@link Rectangle} will not cause the {@link
     * Rectangle}'s boundary to extend to the 0,0 point.
     *
     * @param first  The first {@link Rectangle}.
     * @param second The second {@link Rectangle}.
     * @return The resulting {@link Rectangle}.
     */
    public static Rectangle union(Rectangle first, Rectangle second) {
        boolean firstEmpty  = first.width < 1 || first.height < 1;
        boolean secondEmpty = second.width < 1 || second.height < 1;
        if (firstEmpty && secondEmpty) {
            return new Rectangle();
        }
        if (firstEmpty) {
            return new Rectangle(second);
        }
        if (secondEmpty) {
            return new Rectangle(first);
        }
        return first.union(second);
    }

    /**
     * @param pt The {@link Point} to generate a string for.
     * @return The string form.
     */
    public static final String toString(Point pt) {
        return pt.x + "," + pt.y;
    }

    /**
     * @param encoded A string previously generated by {@link #toString(Point)}.
     * @return A {@link Point} with the encoded string's contents.
     */
    public static final Point toPoint(String encoded) throws NumberFormatException {
        if (encoded == null) {
            throw new NumberFormatException("Not a point");
        }
        String[] parts = encoded.split(",", 2);
        if (parts.length != 2) {
            throw new NumberFormatException("Not a point");
        }
        return new Point(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
    }

    /**
     * @param rect The {@link Rectangle} to generate a string for.
     * @return The string form.
     */
    public static final String toString(Rectangle rect) {
        if (rect.width != 1 || rect.height != 1) {
            return rect.x + "," + rect.y + "," + rect.width + "," + rect.height;
        }
        return rect.x + "," + rect.y;
    }

    /**
     * @param encoded A string previously generated by {@link #toString(Rectangle)}.
     * @return A {@link Rectangle} with the encoded string's contents.
     */
    public static final Rectangle toRectangle(String encoded) throws NumberFormatException {
        if (encoded == null) {
            throw new NumberFormatException("Not a rectangle");
        }
        String[] parts = encoded.split(",", 4);
        if (parts.length != 2 && parts.length != 4) {
            throw new NumberFormatException("Not a rectangle");
        }
        return new Rectangle(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()), parts.length > 2 ? Integer.parseInt(parts[2].trim()) : 1, parts.length > 2 ? Integer.parseInt(parts[3].trim()) : 1);
    }

    /**
     * @param amount The number of pixels to inset the {@link Rectangle}.
     * @param bounds The {@link Rectangle} to inset.
     * @return The {@link Rectangle} that was passed in.
     */
    public static final Rectangle inset(int amount, Rectangle bounds) {
        bounds.x += amount;
        bounds.y += amount;
        bounds.width -= amount * 2;
        bounds.height -= amount * 2;
        if (bounds.width < 0) {
            bounds.width = 0;
        }
        if (bounds.height < 0) {
            bounds.height = 0;
        }
        return bounds;
    }

    /**
     * Tests if the line segment from {@code (x1,y1)} to {@code (x2,y2)} intersects the line segment
     * from {@code (x3,y3)} to {@code (x4,y4)}.
     * <p>
     * Note: This was copied from java.awt.geom.Line2D and modified to use int rather than double.
     *
     * @param x1 the X coordinate of the start point of the first specified line segment
     * @param y1 the Y coordinate of the start point of the first specified line segment
     * @param x2 the X coordinate of the end point of the first specified line segment
     * @param y2 the Y coordinate of the end point of the first specified line segment
     * @param x3 the X coordinate of the start point of the second specified line segment
     * @param y3 the Y coordinate of the start point of the second specified line segment
     * @param x4 the X coordinate of the end point of the second specified line segment
     * @param y4 the Y coordinate of the end point of the second specified line segment
     * @return {@code true} if the first specified line segment and the second specified line
     *         segment intersect each other; {@code false} otherwise.
     */
    public static final boolean linesIntersect(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
        return relativeCCW(x1, y1, x2, y2, x3, y3) * relativeCCW(x1, y1, x2, y2, x4, y4) <= 0 && relativeCCW(x3, y3, x4, y4, x1, y1) * relativeCCW(x3, y3, x4, y4, x2, y2) <= 0;
    }

    /**
     * Returns an indicator of where the specified point {@code (px,py)} lies with respect to the
     * line segment from {@code (x1,y1)} to {@code (x2,y2)}. The return value can be either 1, -1,
     * or 0 and indicates in which direction the specified line must pivot around its first end
     * point, {@code (x1,y1)}, in order to point at the specified point {@code (px,py)}.
     * <p>
     * A return value of 1 indicates that the line segment must turn in the direction that takes the
     * positive X axis towards the negative Y axis. In the default coordinate system used by Java
     * 2D, this direction is counterclockwise.
     * <p>
     * A return value of -1 indicates that the line segment must turn in the direction that takes
     * the positive X axis towards the positive Y axis. In the default coordinate system, this
     * direction is clockwise.
     * <p>
     * A return value of 0 indicates that the point lies exactly on the line segment. Note that an
     * indicator value of 0 is rare and not useful for determining colinearity because of rounding
     * issues.
     * <p>
     * If the point is colinear with the line segment, but not between the end points, then the
     * value will be -1 if the point lies "beyond {@code (x1,y1)}" or 1 if the point lies "beyond
     * {@code (x2,y2)}".
     * <p>
     * Note: This was copied from java.awt.geom.Line2D and modified to use int rather than double.
     *
     * @param x1 the X coordinate of the start point of the specified line segment
     * @param y1 the Y coordinate of the start point of the specified line segment
     * @param x2 the X coordinate of the end point of the specified line segment
     * @param y2 the Y coordinate of the end point of the specified line segment
     * @param px the X coordinate of the specified point to be compared with the specified line
     *           segment
     * @param py the Y coordinate of the specified point to be compared with the specified line
     *           segment
     * @return an integer that indicates the position of the third specified coordinates with
     *         respect to the line segment formed by the first two specified coordinates.
     */
    public static final int relativeCCW(int x1, int y1, int x2, int y2, int px, int py) {
        x2 -= x1;
        y2 -= y1;
        px -= x1;
        py -= y1;
        long ccw = px * (long) y2 - py * (long) x2;
        if (ccw == 0) {
            ccw = px * (long) x2 + py * (long) y2;
            if (ccw > 0) {
                px -= x2;
                py -= y2;
                ccw = px * (long) x2 + py * (long) y2;
                if (ccw < 0) {
                    ccw = 0;
                }
            }
        }
        if (ccw < 0) {
            return -1;
        }
        return ccw > 0 ? 1 : 0;
    }

    /**
     * @param a1 the start of the first line segment.
     * @param a2 the end of the first line segment.
     * @param b1 the start of the second line segment.
     * @param b2 the end of the second line segment.
     * @return the intersection of the two lines, if any. No elements indicates no intersection. One
     *         element indicates intersection at a single point. Two elements indicates an
     *         overlapping segment.
     */
    public static Point2D[] intersection(Point2D a1, Point2D a2, Point2D b1, Point2D b2) {
        return intersection(a1.getX(), a1.getY(), a2.getX(), a2.getY(), b1.getX(), b1.getY(), b2.getX(), b2.getY());
    }

    /**
     * @param a1x the x start of the first line segment.
     * @param a1y the y start of the first line segment.
     * @param a2x the x end of the first line segment.
     * @param a2y the y end of the first line segment.
     * @param b1x the x start of the second line segment.
     * @param b1y the y start of the second line segment.
     * @param b2x the x end of the second line segment.
     * @param b2y the y end of the second line segment.
     * @return the intersection of the two lines, if any. No elements indicates no intersection. One
     *         element indicates intersection at a single point. Two elements indicates an
     *         overlapping segment.
     */
    public static Point2D[] intersection(double a1x, double a1y, double a2x, double a2y, double b1x, double b1y, double b2x, double b2y) {
        boolean aIsPt = a1x == a2x && a1y == a2y;
        boolean bIsPt = b1x == b2x && b1y == b2y;
        if (aIsPt && bIsPt) {
            if (a1x == b1x && a1y == b1y) {
                return new Point2D[]{new Point2D.Double(a1x, a1y)};
            }
        } else if (aIsPt) {
            if (Line2D.ptSegDist(b1x, b1y, b2x, b2y, a1x, a1y) == 0) {
                return new Point2D[]{new Point2D.Double(a1x, a1y)};
            }
        } else if (bIsPt) {
            if (Line2D.ptSegDist(a1x, a1y, a2x, a2y, b1x, b1y) == 0) {
                return new Point2D[]{new Point2D.Double(b1x, b1y)};
            }
        } else {
            double abdx = a1x - b1x;
            double abdy = a1y - b1y;
            double bdx  = b2x - b1x;
            double bdy  = b2y - b1y;
            double uat  = bdx * abdy - bdy * abdx;
            double adx  = a2x - a1x;
            double ady  = a2y - a1y;
            double ubt  = adx * abdy - ady * abdx;
            double ub   = bdy * adx - bdx * ady;
            if (ub == 0) {
                // Parallel, so check for overlap
                if (uat == 0 || ubt == 0) {
                    double ub1, ub2;
                    if (Math.abs(adx) > Math.abs(ady)) {
                        ub1 = (b1x - a1x) / adx;
                        ub2 = (b2x - a1x) / adx;
                    } else {
                        ub1 = (b1y - a1y) / ady;
                        ub2 = (b2y - a1y) / ady;
                    }
                    double left  = Math.max(0, Math.min(ub1, ub2));
                    double right = Math.min(1, Math.max(ub1, ub2));
                    double x     = a2x * left + a1x * (1.0f - left);
                    double y     = a2y * left + a1y * (1.0f - left);
                    if (left < right) {
                        return new Point2D[]{new Point2D.Double(x, y), new Point2D.Double(a2x * right + a1x * (1.0f - right), a2y * right + a1y * (1.0f - right))};
                    } else if (left == right) {
                        return new Point2D[]{new Point2D.Double(x, y)};
                    }
                }
            } else {
                // Not parallel, so find intersection point
                double a = uat / ub;
                if (a >= 0 && a <= 1) {
                    double b = ubt / ub;
                    if (b >= 0 && b <= 1) {
                        return new Point2D[]{new Point2D.Double(a1x + a * adx, a1y + a * ady)};
                    }
                }
            }
        }
        return NO_LINE_INTERSECTION;
    }
}
