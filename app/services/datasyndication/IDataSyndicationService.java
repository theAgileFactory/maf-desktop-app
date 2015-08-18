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
package services.datasyndication;

import java.util.Date;
import java.util.List;

import framework.services.api.commons.ApiSignatureException;
import services.datasyndication.models.DataSyndicationAgreement;
import services.datasyndication.models.DataSyndicationAgreementItem;
import services.datasyndication.models.DataSyndicationAgreementLink;
import services.datasyndication.models.DataSyndicationPartner;

/**
 * The data syndication service.
 * 
 * @author Johann Kohler
 * 
 */
public interface IDataSyndicationService {

    String NAME = "dataSyndicationService";

    /**
     * Get the domain of the current instance.
     */
    String getCurrentDomain();

    /**
     * Return true if the data syndication system is active.
     */
    boolean isActive();

    /**
     * Get partners, filtered by keywords, that are eligible to be the slave
     * instance of an agreement.
     * 
     * Note: the current instance should be manually subtracted of the list.
     * 
     * @param keywords
     *            the keywords
     */
    List<DataSyndicationPartner> searchFromSlavePartners(String keywords);

    /**
     * Get a partner by domain.
     * 
     * @param domain
     *            the partner domain
     */
    DataSyndicationPartner getPartner(String domain);

    /**
     * Get all available agreement items.
     */
    List<DataSyndicationAgreementItem> getDataAgreementItems();

    /**
     * Create a new master agreement for the instance.
     * 
     * @param refId
     *            the refId
     * @param name
     *            the name
     * @param startDate
     *            the start date
     * @param endDate
     *            the end date
     * @param agreementItemIds
     *            the ids of the authorized items
     * @param slaveDomain
     *            the domain of the slave instance
     */
    void submitAgreement(String refId, String name, Date startDate, Date endDate, List<Long> agreementItemIds, String slaveDomain);

    /**
     * Accept a pending agreement.
     * 
     * @param agreement
     *            the agreement
     */
    void acceptAgreement(DataSyndicationAgreement agreement) throws ApiSignatureException, DataSyndicationException;

    /**
     * Reject a pending agreement.
     * 
     * @param agreement
     *            the agreement
     */
    void rejectAgreement(DataSyndicationAgreement agreement) throws DataSyndicationException;

    /**
     * Cancel an agreement.
     * 
     * @param agreement
     *            the agreement
     */
    void cancelAgreement(DataSyndicationAgreement agreement) throws ApiSignatureException, DataSyndicationException;

    /**
     * Suspend an ongoing agreement.
     * 
     * @param agreement
     *            the agreement
     */
    void suspendAgreement(DataSyndicationAgreement agreement) throws DataSyndicationException;

    /**
     * Restart a suspended agreement.
     * 
     * @param agreement
     *            the agreement
     */
    void restartAgreement(DataSyndicationAgreement agreement) throws DataSyndicationException;

    /**
     * Get an agreement by id.
     * 
     * @param id
     *            the agreement id
     */
    DataSyndicationAgreement getAgreement(Long id);

    /**
     * Delete an agreement.
     * 
     * @param id
     *            the agreement id
     */
    void deleteAgreement(Long id);

    /**
     * Get the master agreements of the instance.
     */
    List<DataSyndicationAgreement> getMasterAgreements();

    /**
     * Get the slave agreements of the instance.
     */
    List<DataSyndicationAgreement> getSlaveAgreements();

    /**
     * Get the agreement links of an agreement.
     * 
     * @param id
     *            the agreement id
     */
    List<DataSyndicationAgreementLink> getLinksOfAgreement(Long id);

    /**
     * Create a new agreement link for a master agreement of the instance.
     * 
     * @param agreement
     *            the corresponding agreement
     * @param name
     *            the name
     * @param description
     *            the description
     * @param agreementItemIds
     *            the ids of the authorized items
     * @param dataType
     *            the data type
     * @param masterObjectId
     *            the id of the master object
     */
    void submitAgreementLink(DataSyndicationAgreement agreement, String name, String description, List<Long> agreementItemIds, String dataType,
            Long masterObjectId) throws DataSyndicationException;

    /**
     * Accept a pending agreement link.
     * 
     * @param agreementLink
     *            the agreement link
     * @param slaveObjectId
     *            the associated slave object id
     */
    void acceptAgreementLink(DataSyndicationAgreementLink agreementLink, Long slaveObjectId) throws DataSyndicationException;

    /**
     * Reject a pending agreement link.
     * 
     * @param agreementLink
     *            the agreement link
     */
    void rejectAgreementLink(DataSyndicationAgreementLink agreementLink) throws DataSyndicationException;

    /**
     * Cancel an agreement link.
     * 
     * @param agreementLink
     *            the agreement link
     */
    void cancelAgreementLink(DataSyndicationAgreementLink agreementLink) throws DataSyndicationException;

    /**
     * Get an agreement link by id.
     * 
     * @param agreementLinkId
     *            the agreement link id
     */
    DataSyndicationAgreementLink getAgreementLink(Long agreementLinkId) throws DataSyndicationException;

    /**
     * Get the ongoing agreement links of the instance.
     */
    List<DataSyndicationAgreementLink> getAgreementLinksToSynchronize() throws DataSyndicationException;

    /**
     * Get the agreement links for a master object.
     * 
     * @param dataType
     *            the data type
     * @param masterObjectId
     *            the master object id
     */
    List<DataSyndicationAgreementLink> getAgreementLinksOfMasterObject(String dataType, Long masterObjectId) throws DataSyndicationException;

    /**
     * Get the agreement links for a slave object.
     * 
     * @param dataType
     *            the data type
     * @param masterObjectId
     *            the slave object id
     */
    List<DataSyndicationAgreementLink> getAgreementLinksOfSlaveObject(String dataType, Long masterObjectId) throws DataSyndicationException;

    /**
     * The data syndication exception.
     * 
     * @author Johann Kohler
     *
     */
    public static class DataSyndicationException extends Exception {
        private static final long serialVersionUID = 456423132897542L;

        /**
         * Construct with message.
         * 
         * @param message
         *            the exception message
         */
        public DataSyndicationException(String message) {
            super(message);
        }

    }

}
