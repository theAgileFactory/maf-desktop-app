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

import models.framework_models.api.ApiRegistration;
import services.echannel.request.LoginEventRequest.ErrorCode;
import services.echannel.response.DataSyndicationAgreement;
import services.echannel.response.DataSyndicationAgreementItem;
import services.echannel.response.DataSyndicationAgreementLink;
import services.echannel.response.DataSyndicationPartner;

/**
 * The echannel service.
 * 
 * @author Johann Kohler
 * 
 */
public interface IEchannelService {

    String NAME = "echannelService";

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
     * Get all partners that are eligible for the slave instance of an
     * agreement.
     */
    List<DataSyndicationPartner> getSlavePartners();

    /**
     * Get partners, filtered by keywords, that are eligible for the slave
     * instance of an agreement.
     * 
     * @param keywords
     *            the keywords
     */
    List<DataSyndicationPartner> searchFromSlavePartners(String keywords);

    /**
     * Create a new master agreement.
     * 
     * The agreement should then be accepted by the slave instance.
     * 
     * @param refId
     *            the refId
     * @param name
     *            the name
     * @param startDate
     *            the start date
     * @param endDate
     *            the end date
     * @param agreementItems
     *            the authorized items
     * @param slaveDomain
     *            the domain of the slave instance
     * @param permissions
     *            the permissions used to notify the slave instance
     */
    void submitAgreement(String refId, String name, Date startDate, Date endDate, List<DataSyndicationAgreementItem> agreementItems, String slaveDomain,
            String permissions);

    /**
     * Accept a pending agreement (call by a slave instance).
     * 
     * @param id
     *            the agreement id
     * @param apiKey
     *            the API key of the slave instance
     * @param permissions
     *            the permissions used to notify the master instance
     */
    void acceptAgreement(Long id, ApiRegistration apiKey, String permissions);

    /**
     * Reject a pending agreement (call by a slave instance).
     * 
     * @param id
     *            the agreement id
     * @param permissions
     *            the permissions used to notify the master instance
     */
    void rejectAgreement(Long id, String permissions);

    /**
     * Cancel an ongoing agreement.
     * 
     * @param id
     *            the agreement id
     * @param permissions
     *            the permissions used to notify the master and the slave
     *            instances
     */
    void cancelAgreement(Long id, String permissions);

    /**
     * Suspend an ongoing agreement (call by a master instance).
     * 
     * @param id
     *            the agreement id
     * @param permissions
     *            the permissions used to notify the slave instance
     */
    void suspendAgreement(Long id, String permissions);

    /**
     * Restart a suspended agreement (call by a master instance).
     * 
     * @param id
     *            the agreement id
     * @param permissions
     *            the permissions used to notify the slave instance
     */
    void restartAgreement(Long id, String permissions);

    /**
     * Create a new agreement link (call by a master instance).
     * 
     * @param agreement
     *            the corresponding agreement
     * @param agreementItems
     *            the authorized items
     * @param dataType
     *            the data type
     * @param masterObjectId
     *            the id of the master object
     * @param permissions
     *            the permissions used to notify the slave instance
     */
    void submitAgreementLink(DataSyndicationAgreement agreement, List<DataSyndicationAgreementItem> agreementItems, String dataType, Long masterObjectId,
            String permissions);

    /**
     * Accept a pending agreement link (call by a slave instance).
     * 
     * @param id
     *            the agreement link id
     * @param slaveObjectId
     *            the associated slave object id
     * @param permissions
     *            the permissions used to notify the master instance
     */
    void acceptAgreementLink(Long id, Long slaveObjectId, String permissions);

    /**
     * Reject a pending agreement link (call by a slave instance).
     * 
     * @param id
     *            the agreement link id
     * @param permissions
     *            the permissions used to notify the master instance
     */
    void rejectAgreementLink(Long id, String permissions);

    /**
     * Cancel an ongoing agreement link.
     * 
     * @param id
     *            the agreement link id
     * @param permissions
     *            the permissions used to notify the master and the slave
     *            instances
     */
    void cancelAgreementLink(Long id, String permissions);

    /**
     * Get an agreement link by id.
     * 
     * @param id
     *            the agreement link id
     */
    DataSyndicationAgreementLink getAgreementLink(Long id);

    /**
     * Get the ongoing agreement links.
     */
    List<DataSyndicationAgreementLink> getAgreementLinksToSynchronize();

    /**
     * Get the agreement links for a master object.
     * 
     * @param dataType
     *            the data type
     * @param masterObjectId
     *            the master object id
     */
    List<DataSyndicationAgreementLink> getAgreementLinksOfMasterObject(String dataType, Long masterObjectId);

    /**
     * Get the agreement links for a slave object.
     * 
     * @param dataType
     *            the data type
     * @param masterObjectId
     *            the slave object id
     */
    List<DataSyndicationAgreementLink> getAgreementLinksOfSlaveObject(String dataType, Long masterObjectId);

}
