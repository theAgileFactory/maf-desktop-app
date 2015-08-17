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

import models.framework_models.parent.IModelConstants;
import models.pmo.PortfolioEntry;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import services.datasyndication.models.DataSyndicationAgreementLink;

/**
 * Form to accept a PE agreement link with creating a new PE.
 * 
 * @author Johann Kohler
 */
public class DataSyndicationAgreementLinkAcceptNewPEFormData {

    public Long agreementLinkId;

    @Required
    @MinLength(value = 3)
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String name;

    @Required
    @MaxLength(value = IModelConstants.XLARGE_STRING)
    public String description;

    @Required
    public Long managerId;

    @Required
    public Long portfolioEntryTypeId;

    @Required
    public Long lifeCycleProcessId;

    /**
     * Default constructor.
     */
    public DataSyndicationAgreementLinkAcceptNewPEFormData() {
    }

    /**
     * Construct with initial values.
     * 
     * @param agreementLink
     *            the agreement link
     */
    public DataSyndicationAgreementLinkAcceptNewPEFormData(DataSyndicationAgreementLink agreementLink) {
        this.agreementLinkId = agreementLink.id;
        this.name = agreementLink.name;
    }

    /**
     * Get the corresponding portfolio entry.
     */
    public PortfolioEntry getPorfolioEntry() {
        PortfolioEntry portfolioEntry = new PortfolioEntry();

        // TODO

        return portfolioEntry;
    }
}
