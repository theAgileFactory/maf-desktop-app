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
package services.echannel;

import java.util.Date;
import java.util.List;

import services.datasyndication.models.DataSyndicationAgreement;
import services.datasyndication.models.DataSyndicationAgreementItem;
import services.datasyndication.models.DataSyndicationAgreementLink;
import services.datasyndication.models.DataSyndicationApiKey;
import services.datasyndication.models.DataSyndicationPartner;
import services.echannel.models.NotificationEvent;
import services.echannel.models.RecipientsDescriptor;
import services.echannel.request.LoginEventRequest.ErrorCode;

/**
 * The echannel service.
 * 
 * @author Johann Kohler
 * 
 */
public interface IEchannelService {

    String NAME = "echannelService";

    /**
     * Create a notification event in order to notify some users of another
     * instance.
     * 
     * @param domain
     *            the domain name
     * @param recipientsDescriptor
     *            the recipients descriptor
     * @param title
     *            the title
     * @param message
     *            the message
     * @param actionLink
     *            the action link (when clicking on the notification)
     */
    void createNotificationEvent(String domain, RecipientsDescriptor recipientsDescriptor, String title, String message, String actionLink);

    /**
     * Get the notification events to notify.
     */
    public List<NotificationEvent> getNotificationEventsToNotify();

    /**
     * Return true if this is possible to create a new user.
     * 
     * @param consumedUsers
     *            the current number of consumed users
     */
    boolean canCreateUser(int consumedUsers);

    /**
     * Return true if this is possible to create a new portfolio entry.
     * 
     * @param consumedPortfolioEntries
     *            the current number of consumed portfolio entries
     */
    boolean canCreatePortfolioEntry(int consumedPortfolioEntries);

    /**
     * Return true if the instance is accessible.
     */
    boolean isInstanceAccessible();

    /**
     * Update the number of consumed users.
     * 
     * @param consumedUsers
     *            the current number of consumed users
     */
    void updateConsumedUsers(int consumedUsers);

    /**
     * Update the number of consumed portfolio entries.
     * 
     * @param consumedPortfolioEntries
     *            the current number of consumed portfolio entries
     */
    void updateConsumedPortfolioEntries(int consumedPortfolioEntries);

    /**
     * Update the number of consumed storage.
     * 
     * @param consumedStorage
     *            the current number of consumed storage
     */
    void updateConsumedStorage(int consumedStorage);

    /**
     * Add a login event.
     * 
     * @param uid
     *            the username
     * @param result
     *            true if the login success, else false
     * @param errorCode
     *            the error code (mandatory if result is false)
     * @param errorMessage
     *            the error message
     */
    void addLoginEvent(String uid, Boolean result, ErrorCode errorCode, String errorMessage);

    /**
     * Find the available partners for an agreement with filter capabilities.
     * 
     * @param eligibleAsSlave
     *            true to return the instances that are eligible to be the slave
     *            of an agreement, false to return all
     * @param keywords
     *            the keywords, null to get all
     */
    List<DataSyndicationPartner> findPartners(boolean eligibleAsSlave, String keywords);

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
     * Create a new master agreement for the instance and return it.
     * 
     * The agreement should be then accepted by the slave instance.
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
    DataSyndicationAgreement submitAgreement(String refId, String name, Date startDate, Date endDate, List<Long> agreementItemIds, String slaveDomain);

    /**
     * Accept a pending agreement.
     * 
     * The instance should be the slave of the agreement.
     * 
     * @param id
     *            the agreement id
     * @param apiKey
     *            the API key of the slave instance
     */
    void acceptAgreement(Long id, DataSyndicationApiKey apiKey);

    /**
     * Reject a pending agreement.
     * 
     * The instance should be the slave of the agreement.
     * 
     * @param id
     *            the agreement id
     */
    void rejectAgreement(Long id);

    /**
     * Cancel an agreement.
     * 
     * The instance should the master or the slave of the agreement.
     * 
     * @param id
     *            the agreement id
     */
    void cancelAgreement(Long id);

    /**
     * Suspend an ongoing agreement.
     * 
     * The instance should be the slave of the agreement.
     * 
     * @param id
     *            the agreement id
     */
    void suspendAgreement(Long id);

    /**
     * Restart a suspended agreement.
     * 
     * The instance should be the slave of the agreement.
     * 
     * @param id
     *            the agreement id
     */
    void restartAgreement(Long id);

    /**
     * Get an agreement by id.
     * 
     * The instance should be the master or the slave of the agreement.
     * 
     * @param id
     *            the agreement id
     */
    DataSyndicationAgreement getAgreement(Long id);

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
     * The instance should be the master or the slave of the agreement.
     * 
     * @param id
     *            the agreement id
     */
    List<DataSyndicationAgreementLink> getLinksOfAgreement(Long id);

    /**
     * Create a new agreement link for a master agreement of the instance and
     * return it.
     * 
     * The agreement link should be then accepted by the slave instance.
     * 
     * @param masterPrincipalUid
     *            the uid of the principal that request the agreement link (in
     *            the master instance)
     * @param agreementId
     *            the corresponding agreement id
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
    DataSyndicationAgreementLink submitAgreementLink(String masterPrincipalUid, Long agreementId, String name, String description,
            List<Long> agreementItemIds, String dataType, Long masterObjectId);

    /**
     * Accept a pending agreement link.
     * 
     * The instance should be the slave of the corresponding agreement.
     * 
     * @param id
     *            the agreement link id
     * @param slaveObjectId
     *            the associated slave object id
     */
    void acceptAgreementLink(Long id, Long slaveObjectId);

    /**
     * Reject a pending agreement link.
     * 
     * The instance should be the slave of the corresponding agreement.
     * 
     * @param id
     *            the agreement link id
     */
    void rejectAgreementLink(Long id);

    /**
     * Cancel an agreement link.
     * 
     * The instance should be the master or the slave of the corresponding
     * agreement.
     * 
     * @param id
     *            the agreement link id
     */
    void cancelAgreementLink(Long id);

    /**
     * Get an agreement link by id.
     * 
     * The instance should be the master or the slave of the corresponding
     * agreement.
     * 
     * @param id
     *            the agreement link id
     */
    DataSyndicationAgreementLink getAgreementLink(Long id);

    /**
     * Delete an agreement link.
     * 
     * The instance should be the master or the slave of the corresponding
     * agreement.
     * 
     * @param id
     *            the agreement link id
     */
    void deleteAgreementLink(Long id);

    /**
     * Get the ongoing agreement links of the instance.
     * 
     * The corresponding agreement should be also ongoing.
     */
    List<DataSyndicationAgreementLink> getAgreementLinksToSynchronize();

    /**
     * Get the agreement links of a master object.
     * 
     * This means:<br/>
     * -The instance is the master of the corresponding agreement<br/>
     * -The given masterObjectId corresponds to the master object id of the link
     * 
     * The corresponding agreements should be ONGOING.
     * 
     * @param dataType
     *            the data type
     * @param masterObjectId
     *            the master object id
     */
    List<DataSyndicationAgreementLink> getAgreementLinksOfMasterObject(String dataType, Long masterObjectId);

    /**
     * Get the agreement links of a slave object.
     * 
     * This means:<br/>
     * -The instance is the slave of the corresponding agreement<br/>
     * -The given slaveObjectId corresponds to the slave object id of the link
     * 
     * The corresponding agreements should be ONGOING.
     * 
     * @param dataType
     *            the data type
     * @param slaveObjectId
     *            the slave object id
     */
    List<DataSyndicationAgreementLink> getAgreementLinksOfSlaveObject(String dataType, Long slaveObjectId);

}
