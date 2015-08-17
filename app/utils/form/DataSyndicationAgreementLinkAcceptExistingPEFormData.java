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

import services.datasyndication.models.DataSyndicationAgreementLink;

/**
 * Form to accept a PE agreement link with an existing PE.
 * 
 * @author Johann Kohler
 */
public class DataSyndicationAgreementLinkAcceptExistingPEFormData {

    public Long agreementLinkId;

    public Long portfolioEntryId;

    /**
     * Default constructor.
     */
    public DataSyndicationAgreementLinkAcceptExistingPEFormData() {
    }

    /**
     * Construct with initial values.
     * 
     * @param agreementLink
     *            the agreement link
     */
    public DataSyndicationAgreementLinkAcceptExistingPEFormData(DataSyndicationAgreementLink agreementLink) {
        this.agreementLinkId = agreementLink.id;
    }

}
