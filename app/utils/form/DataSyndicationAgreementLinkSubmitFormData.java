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

import java.util.ArrayList;
import java.util.List;

import models.framework_models.parent.IModelConstants;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;

/**
 * Form to submit an agreement link from a master instance.
 * 
 * @author Johann Kohler
 */
public class DataSyndicationAgreementLinkSubmitFormData {

    // the master object id
    public Long id;

    @Required
    public Long agreementId;

    @Required
    @MinLength(value = 3)
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String name;

    @Required
    @MaxLength(value = IModelConstants.XLARGE_STRING)
    public String description;

    @Required
    public List<Long> itemIds = new ArrayList<Long>();

    /**
     * Default constructor.
     */
    public DataSyndicationAgreementLinkSubmitFormData() {
    }

    /**
     * Construct with initial values.
     * 
     * @param agreementId
     *            the agreement id
     * @param name
     *            the name
     * @param description
     *            the description
     */
    public DataSyndicationAgreementLinkSubmitFormData(Long agreementId, String name, String description) {
        this.agreementId = agreementId;
        this.name = name;
        this.description = description;
    }

}
