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
import services.echannel.IEchannelService.EchannelException;

/**
 * The data syndication service.
 * 
 * @author Johann Kohler
 * 
 */
public interface IDataSyndicationService {

    /**
     * Get the domain of the current instance.
     */
    String getCurrentDomain();

    /**
     * Return true if the data syndication system is active.
     */
    boolean isActive();

    /**
     * Return the corresponding date of the given date if it is in the
     * API-standard format, else return null.
     * 
     * @param stringDate
     *            the date as a string
     */
    Date getStringDate(String stringDate);

    /**
     * Get partners, filtered by keywords, that are eligible to be the slave
     * instance of an agreement.
     * 
     * Note: the current instance should be manually subtracted of the list.
     * 
     * @param keywords
     *            the keywords
     */
    List<DataSyndicationPartner> searchFromSlavePartners(String keywords) throws EchannelException;

    /**
     * Get a partner by domain.
     * 
     * @param domain
     *            the partner domain
     */
    DataSyndicationPartner getPartner(String domain) throws EchannelException;

    /**
     * Get all available agreement items.
     */
    List<DataSyndicationAgreementItem> getAgreementItems() throws EchannelException;

    /**
     * Get an agreement item by data type and descriptor.
     * 
     * @param dataType
     *            the item data type
     * @param descriptor
     *            the item descriptor
     */
    DataSyndicationAgreementItem getAgreementItemByDataTypeAndDescriptor(String dataType, String descriptor) throws EchannelException;

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
     * @param slaveBaseUrl
     *            the base URL of the slave instance (for the notification link)
     */
    void submitAgreement(String refId, String name, Date startDate, Date endDate, List<Long> agreementItemIds, String slaveDomain, String slaveBaseUrl)
            throws EchannelException;

    /**
     * Create a new master agreement without slave for the instance.
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
     * @param partnerEmail
     *            the email of the wished partner
     */
    void submitAgreementNoSlave(String refId, String name, Date startDate, Date endDate, List<Long> agreementItemIds, String partnerEmail)
            throws EchannelException;

    /**
     * Accept a pending (or pending_instance) agreement.
     * 
     * @param agreement
     *            the agreement
     */
    void acceptAgreement(DataSyndicationAgreement agreement) throws ApiSignatureException, EchannelException;

    /**
     * Reject a pending agreement.
     * 
     * @param agreement
     *            the agreement
     */
    void rejectAgreement(DataSyndicationAgreement agreement) throws EchannelException;

    /**
     * Cancel an agreement.
     * 
     * @param agreement
     *            the agreement
     */
    void cancelAgreement(DataSyndicationAgreement agreement) throws ApiSignatureException, EchannelException;

    /**
     * Suspend an ongoing agreement.
     * 
     * @param agreement
     *            the agreement
     */
    void suspendAgreement(DataSyndicationAgreement agreement) throws EchannelException;

    /**
     * Restart a suspended agreement.
     * 
     * @param agreement
     *            the agreement
     */
    void restartAgreement(DataSyndicationAgreement agreement) throws EchannelException;

    /**
     * Get an agreement by id.
     * 
     * @param id
     *            the agreement id
     */
    DataSyndicationAgreement getAgreement(Long id) throws EchannelException;

    /**
     * Get the master agreements of the instance.
     */
    List<DataSyndicationAgreement> getAgreementsAsMaster() throws EchannelException;

    /**
     * Get the slave agreements of the instance.
     */
    List<DataSyndicationAgreement> getAgreementsAsSlave() throws EchannelException;

    /**
     * Get the agreement links of an agreement.
     * 
     * @param id
     *            the agreement id
     */
    List<DataSyndicationAgreementLink> getLinksOfAgreement(Long id) throws EchannelException;

    /**
     * Create a new agreement link for a master agreement of the instance.
     * 
     * @param masterPrincipalUid
     *            the UID of the principal that creates the links (in the master
     *            instance)
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
    void submitAgreementLink(String masterPrincipalUid, DataSyndicationAgreement agreement, String name, String description, List<Long> agreementItemIds,
            String dataType, Long masterObjectId) throws EchannelException;

    /**
     * Accept a pending agreement link.
     * 
     * @param agreementLink
     *            the agreement link
     * @param slaveObjectId
     *            the associated slave object id
     */
    void acceptAgreementLink(DataSyndicationAgreementLink agreementLink, Long slaveObjectId) throws EchannelException;

    /**
     * Reject a pending agreement link.
     * 
     * @param agreementLink
     *            the agreement link
     */
    void rejectAgreementLink(DataSyndicationAgreementLink agreementLink) throws EchannelException;

    /**
     * Cancel an agreement link.
     * 
     * @param agreementLink
     *            the agreement link
     */
    void cancelAgreementLink(DataSyndicationAgreementLink agreementLink) throws EchannelException;

    /**
     * Get an agreement link by id.
     * 
     * @param agreementLinkId
     *            the agreement link id
     */
    DataSyndicationAgreementLink getAgreementLink(Long agreementLinkId) throws EchannelException;

    /**
     * Delete an agreement link.
     * 
     * @param agreementLink
     *            the agreement link
     * 
     */
    void deleteAgreementLink(DataSyndicationAgreementLink agreementLink) throws EchannelException;

    /**
     * Get the ongoing agreement links of the instance.
     */
    List<DataSyndicationAgreementLink> getAgreementLinksToSynchronize() throws EchannelException;

    /**
     * Get the agreement links for a master object.
     * 
     * @param dataType
     *            the data type
     * @param masterObjectId
     *            the master object id
     */
    List<DataSyndicationAgreementLink> getAgreementLinksOfMasterObject(String dataType, Long masterObjectId) throws EchannelException;

    /**
     * Get the agreement links for a slave object.
     * 
     * @param dataType
     *            the data type
     * @param slaveObjectId
     *            the slave object id
     */
    List<DataSyndicationAgreementLink> getAgreementLinksOfSlaveObject(String dataType, Long slaveObjectId) throws EchannelException;

    /**
     * Get the agreement links for an item and a slave object.
     * 
     * @param item
     *            the agreement item
     * @param dataType
     *            the data type
     * @param slaveObjectId
     *            the slave object id
     */
    List<DataSyndicationAgreementLink> getAgreementLinksOfItemAndSlaveObject(DataSyndicationAgreementItem item, String dataType, Long slaveObjectId)
            throws EchannelException;

    /**
     * Post data of an agreement link in the slave instance.
     * 
     * Return true if everything was done without error, else return false (the
     * errors are logged)
     * 
     * @param agreementLink
     *            the agreement link
     */
    public void postData(DataSyndicationAgreementLink agreementLink) throws DataSyndicationPostDataException;

    /**
     * The data syndication exception for postData method.
     * 
     * @author Johann Kohler
     *
     */
    public static class DataSyndicationPostDataException extends Exception {

        private static final long serialVersionUID = 1253447786214L;

        private ErrorCode code;

        /**
         * Construct with message.
         * 
         * @param code
         *            the error code
         */
        public DataSyndicationPostDataException(ErrorCode code) {
            super();
            this.code = code;
        }

        /**
         * Get the error code.
         */
        public ErrorCode getCode() {
            return this.code;
        }

        /**
         * The error codes.
         * 
         * @author Johann Kohler
         */
        public static enum ErrorCode {

            E1001, E1002, E1003, E1004, E2001, E2002, E2003, E2004;

            /**
             * Get the message.
             */
            public String getMessageKey() {
                return "data_syndication.post_data.error." + this.name();
            }
        }

    }
}
