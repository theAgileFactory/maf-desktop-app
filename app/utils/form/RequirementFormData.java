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

import models.delivery.Requirement;

/**
 * A requirement form data is used to manage the fields when editing a
 * requirement.
 * 
 * @author Johann Kohler
 */
public class RequirementFormData {

    // the portfolioEntry id
    public Long id;

    public Long requirementId;

    /**
     * Default constructor.
     */
    public RequirementFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param requirement
     *            the requirement in the DB
     */
    public RequirementFormData(Requirement requirement) {
        this.id = requirement.portfolioEntry.id;
        this.requirementId = requirement.id;
    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param requirement
     *            the requirement in the DB
     */
    public void fill(Requirement requirement) {
    }
}
