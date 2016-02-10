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
package dao.governance;

import java.util.List;

import com.avaje.ebean.Model.Finder;

import framework.services.account.IPreferenceManagerPlugin;
import framework.utils.Pagination;
import models.governance.ProcessTransitionRequest;
import models.governance.ProcessTransitionRequest.RequestType;

/**
 * DAO for the {@link ProcessTransitionRequest} object.
 * 
 * @author Johann Kohler
 */
public abstract class ProcessTransitionRequestDao {

    public static Finder<Long, ProcessTransitionRequest> findProcessTransitionRequest = new Finder<>(ProcessTransitionRequest.class);

    /**
     * Default constructor.
     */
    public ProcessTransitionRequestDao() {
    }

    /**
     * Get a process transition request by id.
     * 
     * @param id
     *            the request id
     */
    public static ProcessTransitionRequest getProcessTransitionRequestById(Long id) {
        return findProcessTransitionRequest.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all requests.
     */
    public static List<ProcessTransitionRequest> getProcessTransitionRequestAsList() {
        return findProcessTransitionRequest.where().eq("deleted", false).findList();
    }

    /**
     * Get all pending milestone requests as pagination.
     * 
     * @param preferenceManagerPlugin
     *            the preference manager service
     */
    public static Pagination<ProcessTransitionRequest> getProcessTransitionRequestMilestoneApprovalToReviewAsPagination(
            IPreferenceManagerPlugin preferenceManagerPlugin) {
        return new Pagination<>(preferenceManagerPlugin, findProcessTransitionRequest.orderBy("creationDate DESC").where().eq("deleted", false)
                .eq("requestType", RequestType.MILESTONE_APPROVAL.name()).isNull("reviewDate"));
    }

}
