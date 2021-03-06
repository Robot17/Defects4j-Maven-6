/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2007, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, 
 * USA.  
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * --------------------
 * AxisLabelEntity.java
 * --------------------
 * (C) Copyright 2007, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: PeriodAxis.java,v 1.16.2.7 2007/03/22 12:13:27 mungady Exp $
 *
 * Changes
 * -------
 * 02-Jul-2007 : Version 1 (DG);
 *
 */

package org.jfree.chart.entity;

import java.awt.Shape;

import org.jfree.chart.axis.Axis;

/**
 * A chart entity that represents the label for an axis.
 * 
 * @since 1.2.0
 */
public class AxisLabelEntity extends ChartEntity {
    
    /** The axis. */
    private Axis axis;
    
    /**
     * Creates a new entity representing the label on an axis.
     * 
     * @param axis  the axis.
     * @param hotspot  the hotspot.
     * @param toolTipText  the tool tip text (<code>null</code> permitted).
     * @param url  the url (<code>null</code> permitted).
     */
    public AxisLabelEntity(Axis axis, Shape hotspot, String toolTipText, 
            String url) {
        super(hotspot, toolTipText, url);
    }
    
    /**
     * Returns the axis for this entity.
     * 
     * @return The axis.
     */
    public Axis getAxis() {
        return this.axis;
    }

}
