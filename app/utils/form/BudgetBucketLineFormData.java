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

import dao.finance.CurrencyDAO;
import models.finance.BudgetBucketLine;
import models.framework_models.parent.IModelConstants;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;

/**
 * A budget bucket line form data is used to manage the fields when
 * adding/editing a line for a budget bucket.
 * 
 * @author Johann Kohler
 */
public class BudgetBucketLineFormData {

    // the budget bucket id
    public Long id;

    public Long lineId;

    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String refId;

    @Required
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String name;

    @Required
    public boolean isOpex;

    @Required
    public String currencyCode;

    @Required
    public BigDecimal currencyRate;

    @Required
    public BigDecimal amount;

    /**
     * Default constructor.
     */
    public BudgetBucketLineFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param line
     *            the budget bucket line in the DB
     */
    public BudgetBucketLineFormData(BudgetBucketLine line) {

        this.id = line.budgetBucket.id;
        this.lineId = line.id;

        this.refId = line.refId;
        this.name = line.name;
        this.isOpex = line.isOpex != null ? line.isOpex : false;
        this.currencyCode = line.currency != null ? line.currency.code : null;
        this.currencyRate = line.currencyRate;
        this.amount = line.amount;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param line
     *            the budget bucket line in the DB
     */
    public void fill(BudgetBucketLine line) {
        line.refId = this.refId;
        line.name = this.name;
        line.isOpex = this.isOpex;
        line.currency = CurrencyDAO.getCurrencyByCode(this.currencyCode);
        line.currencyRate = this.currencyRate;
        line.amount = this.amount;
    }

}
