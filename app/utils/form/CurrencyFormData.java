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
package utils.form;

import java.math.BigDecimal;

import models.finance.Currency;

/**
 * A currency form data is used to manage the fields when adding/editing a
 * currency.
 * 
 * @author Johann Kohler
 */
public class CurrencyFormData {

    public Long id;

    public boolean isActive = true;

    public String code;

    public BigDecimal conversionRate;

    public String symbol;

    /**
     * Default constructor.
     */
    public CurrencyFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param currency
     *            the currency in the DB
     */
    public CurrencyFormData(Currency currency) {
        this.id = currency.id;
        this.isActive = currency.isActive;
        this.code = currency.code;
        this.conversionRate = currency.conversionRate;
        this.symbol = currency.symbol;
    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param currency
     *            the currency in the DB
     */
    public void fill(Currency currency) {
        currency.isActive = this.isActive;
        currency.code = this.code;
        currency.conversionRate = this.conversionRate;
        currency.symbol = this.symbol;
    }
}
