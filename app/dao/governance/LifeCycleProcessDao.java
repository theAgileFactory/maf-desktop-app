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

import models.governance.LifeCycleInstance;
import models.governance.LifeCycleProcess;
import com.avaje.ebean.Model.Finder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;

/**
 * DAO for the {@link LifeCycleInstance} and {@link LifeCycleProcess} objects.
 * 
 * @author Johann Kohler
 */
public abstract class LifeCycleProcessDao {

    public static Finder<Long, LifeCycleProcess> findLifeCycleProcess = new Finder<>(LifeCycleProcess.class);

    /**
     * Default constructor.
     */
    public LifeCycleProcessDao() {
    }

    /**
     * Get a life cycle process by id.
     * 
     * @param id
     *            the life cycle process id
     */
    public static LifeCycleProcess getLCProcessById(Long id) {
        return findLifeCycleProcess.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all life cycle processes.
     */
    public static List<LifeCycleProcess> getLCProcessAsList() {
        return findLifeCycleProcess.where().eq("deleted", false).findList();
    }

    /**
     * Get all active life cycle process.
     */
    public static List<LifeCycleProcess> getLCProcessActiveAsList() {
        return findLifeCycleProcess.where().eq("deleted", false).eq("isActive", true).findList();
    }

    /**
     * Get all active life cycle process as value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getLCProcessActiveAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getLCProcessActiveAsList());
    }

}
