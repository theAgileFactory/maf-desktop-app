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

import play.data.validation.Constraints.Required;

/**
 * Form to select a deliverable from another portfolio entry in order to follow
 * it.
 * 
 * @author Johann Kohler
 */
public class FollowDeliverableFormData {

    // the portfolio entry id
    public Long id;

    @Required
    public Long otherPortfolioEntry;

    @Required
    public Long otherDelivrable;

    /**
     * Default constructor.
     */
    public FollowDeliverableFormData() {
    }

}
