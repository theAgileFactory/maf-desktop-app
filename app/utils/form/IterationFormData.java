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

import models.delivery.Iteration;

/**
 * An iteration form data is used to manage the fields when editing an
 * iteration.
 * 
 * @author Johann Kohler
 */
public class IterationFormData {

    // the portfolio entry id
    public Long id;

    public Long iterationId;

    public Integer storyPoints;

    /**
     * Default constructor.
     */
    public IterationFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param iteration
     *            the iteration in the DB
     */
    public IterationFormData(Iteration iteration) {

        this.id = iteration.portfolioEntry.id;
        this.iterationId = iteration.id;
        this.storyPoints = iteration.storyPoints;

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param iteration
     *            the iteration in the DB
     */
    public void fill(Iteration iteration) {
        iteration.storyPoints = this.storyPoints;
    }
}
