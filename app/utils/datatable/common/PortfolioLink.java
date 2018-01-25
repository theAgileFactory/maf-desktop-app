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
package utils.datatable.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import models.pmo.Portfolio;

/**
 * Orthogonal representation of a @link{Portfolio}
 * <ul>
 *     <li>display: display</li>
 *     <li>other: text</li>
 * </ul>
 * @author Guillaume Petit
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class PortfolioLink {

    public String display;

    public String text;

    public PortfolioLink(Portfolio portfolio) {
        this.display = views.html.modelsparts.display_portfolio.render(portfolio).body();
        this.text = portfolio == null ? "" : portfolio.name;
    }
}