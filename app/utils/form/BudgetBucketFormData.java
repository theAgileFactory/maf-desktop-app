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

import models.finance.BudgetBucket;
import models.framework_models.parent.IModelConstants;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import dao.pmo.ActorDao;

/**
 * An budget bucket form data is used to manage the fields when create/edit a
 * budget bucket.
 * 
 * @author Johann Kohler
 */
public class BudgetBucketFormData {

    public Long id;

    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String refId;

    @Required
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String name;

    @Required
    public Long owner;

    @Required
    public boolean isApproved;

    @Required
    public boolean isActive;

    /**
     * Default constructor.
     */
    public BudgetBucketFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param budgetBucket
     *            the budget bucket in the DB
     */
    public BudgetBucketFormData(BudgetBucket budgetBucket) {

        id = budgetBucket.id;
        refId = budgetBucket.refId;
        name = budgetBucket.name;
        owner = budgetBucket.owner != null ? budgetBucket.owner.id : null;
        isApproved = budgetBucket.isApproved;
        isActive = budgetBucket.isActive;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param budgetBucket
     *            the budget bucket in the DB
     */
    public void fill(BudgetBucket budgetBucket) {

        budgetBucket.refId = refId;
        budgetBucket.name = name;
        budgetBucket.owner = owner != null ? ActorDao.getActorById(owner) : null;
        budgetBucket.isApproved = isApproved;
        budgetBucket.isActive = isActive;

    }

}
