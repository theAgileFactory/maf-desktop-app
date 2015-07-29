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

/**
 * a source data value (dataObj) is complementary details for a source value.
 * 
 * a source data value is the details of a portfolio entry
 * 
 * note: when a user click on a bar, the portfolio entry details are displayed
 * thanks a popup
 * 
 * @author Johann Kohler
 * 
 */
public class SourceDataValue {

    public String url;
    public String name;
    public String description;
    public String manager;
    public String portfolios;

    /**
     * Construct a source data value.
     * 
     * @param url
     *            the portfolio entry url
     * @param name
     *            the portfolio entry name
     * @param description
     *            the portfolio entry description
     * @param manager
     *            the portfolio entry manager
     * @param portfolios
     *            the portfolios of the portfolio entry
     */
    public SourceDataValue(String url, String name, String description, String manager, String portfolios) {
        this.url = url;
        this.name = name;
        this.description = description;
        this.manager = manager;
        this.portfolios = portfolios;
    }

}
