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
package dao.finance;

import java.util.List;

import javax.persistence.PersistenceException;

import models.finance.Supplier;
import com.avaje.ebean.Model.Finder;

/**
 * DAO for the {@link Supplier} object.
 * 
 * @author Pierre-Yves Cloux
 */
public abstract class SupplierDAO {

    /**
     * Default finder for the entity class.
     */
    public static Finder<Long, Supplier> findSupplier = new Finder<>(Long.class, Supplier.class);

    /**
     * Default constructor.
     */
    public SupplierDAO() {
    }

    /**
     * Get a supplier by ref id.
     * 
     * @param refId
     *            the ref id
     */
    public static Supplier getSupplierByRefId(String refId) {
        try {
            return SupplierDAO.findSupplier.where().eq("deleted", false).eq("refId", refId).findUnique();
        } catch (PersistenceException e) {
            return SupplierDAO.findSupplier.where().eq("deleted", false).eq("refId", refId).findList().get(0);
        }
    }

    /**
     * Get an supplier by id.
     * 
     * @param id
     *            the supplier id
     * @return an supplier specified by id
     */
    public static Supplier getSupplierById(Long id) {
        return SupplierDAO.findSupplier.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all suppliers.
     **/
    public static List<Supplier> getSupplierAsList() {

        return SupplierDAO.findSupplier.where().eq("deleted", false).findList();
    }

}
