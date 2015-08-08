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

import models.finance.CostCenter;
import com.avaje.ebean.Model.Finder;
import play.mvc.Http;

import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;

import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;

/**
 * DAO for the {@link CostCenter} object.
 * 
 * @author Pierre-Yves Cloux
 */
public abstract class CostCenterDAO {

    /**
     * Default finder for the entity class.
     */
    public static Finder<Long, CostCenter> find = new Finder<>(CostCenter.class);

    /**
     * Default constructor.
     */
    public CostCenterDAO() {
    }

    /**
     * Get a cost center by ref id.
     * 
     * @param refId
     *            the ref id
     */
    public static CostCenter getCostCenterByRefId(String refId) {
        try {
            return CostCenterDAO.find.where().eq("deleted", false).eq("refId", refId).findUnique();
        } catch (PersistenceException e) {
            return CostCenterDAO.find.where().eq("deleted", false).eq("refId", refId).findList().get(0);
        }
    }

    /**
     * Get all cost centers.
     */
    public static List<CostCenter> getCostCenterAsList() {
        return CostCenterDAO.find.where().eq("deleted", false).findList();
    }

    /**
     * Search from all cost centers for which the criteria matches with the name
     * of the ref ID.
     * 
     * note: the name is either directly in the cost center table or in the i18n
     * messages table
     * 
     * @param key
     *            the search criteria (wild cards should be %)
     */
    public static List<CostCenter> getCostCenterAsListByName(String key) {

        String sql = "SELECT cc.id FROM `cost_center` cc LEFT OUTER JOIN `i18n_messages` im ON im.key = cc.name WHERE cc.deleted = 0";

        sql += " AND (im.language = '" + Http.Context.current().lang().code() + "' OR im.language IS NULL)";

        sql += " AND (cc.name LIKE \"" + key + "%\" OR cc.ref_id LIKE \"" + key + "%\" OR im.value LIKE \"" + key + "%\") ";

        RawSql rawSql = RawSqlBuilder.parse(sql).columnMapping("cc.id", "id").create();

        return CostCenterDAO.find.query().setRawSql(rawSql).findList();
    }

    /**
     * Get all cost centers as value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getCostCenterSelectableAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getCostCenterAsList());
    }

    /**
     * Search from all cost centers with the search process defined by the
     * method "searchFromAllCostCenters" and return a value holder collection.
     * 
     * @param key
     *            the search criteria (wild cards should be %)
     */
    public static ISelectableValueHolderCollection<Long> getCostCenterSelectableAsVHByName(String key) {
        return new DefaultSelectableValueHolderCollection<>(getCostCenterAsListByName(key));
    }

    /**
     * Get an cost center by id.
     * 
     * @param id
     *            the cost center id
     */
    public static CostCenter getCostCenterById(Long id) {
        return CostCenterDAO.find.where().eq("deleted", false).eq("id", id).findUnique();
    }

}
