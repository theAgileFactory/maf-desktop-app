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
import java.util.ArrayList;
import java.util.List;

import dao.finance.CurrencyDAO;
import framework.utils.Msg;
import models.finance.Currency;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;

/**
 * A currency form data is used to manage the fields when adding/editing a
 * currency.
 * 
 * @author Johann Kohler
 */
public class CurrencyFormData {

    public Long id;

    public boolean isActive;

    @Required
    @play.data.validation.Constraints.Pattern(value = "[a-zA-Z]{3}", message = "object.currency.code.error.bad_format")
    public String code;

    @Required
    public BigDecimal conversionRate;

    @Required
    public String symbol;

    /**
     * Default constructor.
     */
    public CurrencyFormData() {
    }

    /**
     * Form validation.
     */
    public List<ValidationError> validate() {

        List<ValidationError> errors = new ArrayList<>();

        // check the code is not already used
        Currency currency = CurrencyDAO.getCurrencyByCode(this.code);
        if (currency != null) {
            if (this.id != null) { // edit case
                if (!currency.id.equals(this.id)) {
                    errors.add(new ValidationError("code", Msg.get("object.currency.code.error.already_used")));
                }
            } else { // new case
                errors.add(new ValidationError("code", Msg.get("object.currency.code.error.already_used")));
            }
        }

        return errors.isEmpty() ? null : errors;
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
        if (currency.isDefault) {
            currency.isActive = true;
            currency.conversionRate = BigDecimal.ONE;
        } else {
            currency.isActive = this.isActive;
            currency.conversionRate = this.conversionRate;
        }
        currency.code = this.code.toUpperCase();
        currency.symbol = this.symbol;
    }
}
