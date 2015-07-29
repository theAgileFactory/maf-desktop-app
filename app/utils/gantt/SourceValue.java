/*! LICENSE
 *
 * Copyright (c) 2015, The Agile Factory SA and/or its affiliates. All rights
 * reserved.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package utils.gantt;

import java.util.Date;

/**
 * a gantt source value represents an interval (bar) for a source item.
 * 
 * a source value is a life cycle phase
 * 
 * @author Johann Kohler
 * 
 */
public class SourceValue {

    public String to;
    public String from;
    public String desc;
    public String label;
    public String customClass;
    public SourceDataValue dataObj;

    /**
     * Construct a source value.
     * 
     * @param from
     *            the from date
     * @param to
     *            the to date
     * @param desc
     *            the description
     * @param label
     *            the label
     * @param customClass
     *            the custom CSS class
     * @param dataObj
     *            the source data value
     */
    public SourceValue(Date from, Date to, String desc, String label, String customClass, SourceDataValue dataObj) {
        this.to = String.valueOf(to.getTime());
        this.from = String.valueOf(from.getTime());
        this.desc = desc;
        this.label = label;
        this.customClass = customClass;
        this.dataObj = dataObj;
    }

}
