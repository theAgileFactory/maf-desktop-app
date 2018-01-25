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
import play.mvc.Http;

import java.text.NumberFormat;

/**
 * Orthogonal representation of a financial number
 * <ul>
 *     <li>display: display</li>
 *     <li>other: amount</li>
 * </ul>
 * @author Guillaume Petit
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class FinancialNumber {

    public Double amount;

    public String display;

    public FinancialNumber(Double amount) {
        this.amount = amount;
        this.display = NumberFormat.getInstance(Http.Context.current().lang().toLocale()).format(amount);
    }
}