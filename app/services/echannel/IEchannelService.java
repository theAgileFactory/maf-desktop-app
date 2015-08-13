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

}
