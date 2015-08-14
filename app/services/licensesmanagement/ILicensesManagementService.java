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
package services.licensesmanagement;

import services.echannel.request.LoginEventRequest.ErrorCode;

/**
 * The licenses management service.
 * 
 * @author Johann Kohler
 * 
 */
public interface ILicensesManagementService {

    String NAME = "licensesManagementService";

    /**
     * Return true if this is possible to create a new user.
     */
    boolean canCreateUser();

    /**
     * Return true if this is possible to create a new portfolio entry.
     */
    boolean canCreatePortfolioEntry();

    /**
     * Return true if the instance is accessible.
     */
    boolean isInstanceAccessible();

    /**
     * Update the number of consumed users.
     */
    void updateConsumedUsers();

    /**
     * Update the number of consumed portfolio entries.
     */
    void updateConsumedPortfolioEntries();

    /**
     * Update the number of consumed storage.
     */
    void updateConsumedStorage();

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
