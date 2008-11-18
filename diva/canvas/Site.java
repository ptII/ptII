/*
 Copyright (c) 1998-2005 The Regents of the University of California
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 *
 */
package diva.canvas;

import java.awt.geom.Point2D;

/** A site represents a point on a figure. Sites are
 * used by manipulators so that they know where to attach grab-handles.
 * Application-specific figures may also provide sites that
 * allows clients to find attachment points and other key points
 * on the figure.
 *
 * @version $Id$
 * @author  John Reekie
 * @Pt.AcceptedRating  Red
 */
public interface Site {
    /** Get the figure to which this site is attached. Usually, this
     * will return a valid Figure, but clients must be aware that
     * certain types of site may return null.
     */
    public Figure getFigure();

    /** Get the ID of this site. Within each figure, the IDs of
     * the sites must be unique.
     */
    public int getID();

    /** Get the angle of the normal to this site, in radians
     * between zero and 2pi. The direction is "out" of the site.
     * The result is meaningful only if hasNormal() returns true.
     */
    public double getNormal();

    /** Get the point location of the site, in the enclosing
     * transform context with default normal.
     */
    public Point2D getPoint();

    /** Get the point location of the site, in the given
     * transform context with the default normal.
     * The given context must be an enclosing
     * context of the site.
     */
    public Point2D getPoint(TransformContext tc);

    /** Get the point location of the site, in the enclosing
     * transform context with the given normal.
     */
    public Point2D getPoint(double normal);

    /** Get the point location of the site, in the given
     * transform context with the given normal.
     * The given context must be an enclosing
     * context of the site.
     */
    public Point2D getPoint(TransformContext tc, double normal);

    /** Get the enclosing transform context of this site.
     */
    public TransformContext getTransformContext();

    /** Get the x-coordinate of the site, in the enclosing
     * transform context.
     */
    public double getX();

    /** Get the y-coordinate of the site, in the enclosing
     * transform context.
     */
    public double getY();

    /** Test if this site has a "normal" to it. The normal
     * is accessible by the methods getNormal()
     * and isNormal(). Generally, sites on the boundary of
     * a shape will return true to this method, and sites
     * in the center of an object will return false.
     */
    public boolean hasNormal();

    /** Test if this site has a normal in the given direction.
     * The direction is that given by one of the static constants
     * NORTH, SOUTH, EAST, or WEST, defined in
     * <b>javax.swing.SwingConstants</b>
     */
    public boolean isNormal(int direction);

    /** Set the normal "out" of the site. If the site cannot
     * change its normal, it can ignore this call, so clients
     * that care should always check the normal after calling.
     * If the site can change its normal, it can also change
     * its position. For example, a site on the perimeter of a
     * figure may move to a different position.
     */
    public void setNormal(double normal);

    /** Translate the site by the indicated distance, where distances
     * are in the local coordinates of the containing pane. Usually,
     * this will mean that the figure is reshaped so that the
     * site moves the given distance. If the site cannot
     * be moved the given distance, then either do nothing, or move
     * it part of the distance. Clients are expected to check the
     * new location of the site.
     */
    public void translate(double x, double y);
}
