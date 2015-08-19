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
package services.echannel.request;

import java.util.Date;
import java.util.List;

/**
 * The content for the submit agreement request.
 * 
 * @author Johann Kohler
 * 
 */
public class SubmitDataSyndicationAgreementRequest {

    public String refId;
    public String name;
    public Date startDate;
    public Date endDate;
    public List<Long> agreementItemIds;
    public String slaveDomain;

    /**
     * Default constructor.
     */
    public SubmitDataSyndicationAgreementRequest() {
    }

}
