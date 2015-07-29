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

import java.util.ArrayList;
import java.util.List;

/**
 * A gantt source item represents a line in the gantt view. Each line can
 * include many interval (called sourceValues).
 * 
 * a source item is a portfolio entry
 * 
 * note: for visual reasons we chose to display the different phases of a
 * portfolio entry in many lines and not in one. So we create a source item for
 * each phase, given the first include the portfolio entry name.
 * 
 * @author Johann Kohler
 * 
 */
public class SourceItem {

    public String name;
    public String desc;
    public List<SourceValue> values = new ArrayList<SourceValue>();

    /**
     * Construct a source item.
     * 
     * @param name
     *            the name
     * @param desc
     *            the description
     */
    public SourceItem(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

}
