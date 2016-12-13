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
package dao.pmo;

import java.util.List;

import javax.persistence.PersistenceException;

import org.apache.commons.lang3.StringUtils;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model.Finder;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.SqlUpdate;

import framework.services.account.IPreferenceManagerPlugin;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Pagination;
import models.pmo.OrgUnit;
import models.pmo.OrgUnitType;
import play.mvc.Http;

/**
 * DAO for the {@link OrgUnit} and {@link OrgUnitTypeRequest} objects.
 * 
 * @author Johann Kohler
 */
public abstract class OrgUnitDao {

    public static Finder<Long, OrgUnit> findOrgUnit = new Finder<>(OrgUnit.class);

    public static Finder<Long, OrgUnitType> findOrgUnitType = new Finder<>(OrgUnitType.class);

    /**
     * Default constructor.
     */
    public OrgUnitDao() {
    }

    /**
     * Get an org unit by id.
     * 
     * @param id
     *            the org unit id
     * @return an org unit specified by id
     */
    public static OrgUnit getOrgUnitById(Long id) {
        return findOrgUnit.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get an org unit by refId.
     * 
     * @param refId
     *            the org unit refId
     */
    public static OrgUnit getOrgUnitByRefId(String refId) {
        try {
            return findOrgUnit.where().eq("deleted", false).eq("refId", refId).findUnique();
        } catch (PersistenceException e) {
            return findOrgUnit.where().eq("deleted", false).eq("refId", refId).findList().get(0);
        }
    }

    /**
     * Get all active org units.
     */
    public static List<OrgUnit> getOrgUnitActiveAsList() {
        return findOrgUnit.where().eq("deleted", false).eq("isActive", true).findList();
    }

    /**
     * Get the active children of an org unit.
     * 
     * @param parentId
     *            the parent id to find the children
     */
    public static List<OrgUnit> getOrgUnitActiveAsListByParent(Long parentId) {
        return findOrgUnit.where().eq("deleted", false).eq("isActive", true).eq("parent.id", parentId).findList();
    }

    /**
     * Get the org units for which the given actor is the manager, as a
     * pagination object.
     * 
     * @param preferenceManagerPlugin
     *            the preference manager service
     * @param actorId
     *            the actor id of a manager
     * @param viewAll
     *            set to true if the non-active org units must be also returned
     * 
     */
    public static Pagination<OrgUnit> getOrgUnitAsPaginationByActor(IPreferenceManagerPlugin preferenceManagerPlugin, Long actorId, boolean viewAll) {

        ExpressionList<OrgUnit> e = findOrgUnit.where().eq("deleted", false).eq("manager.id", actorId);

        if (!viewAll) {
            e = e.eq("isActive", true);
        }

        return new Pagination<>(preferenceManagerPlugin, e);
    }

    /**
     * Get all active sponsoring units.
     */
    public static List<OrgUnit> getOrgUnitActiveCanSponsorAsList() {
        return findOrgUnit.where().eq("deleted", false).eq("isActive", true).eq("canSponsor", true).findList();
    }

    /**
     * Get all active delivery units.
     */
    public static List<OrgUnit> getOrgUnitActiveCanDeliverAsList() {
        return findOrgUnit.where().eq("deleted", false).eq("isActive", true).eq("canDeliver", true).findList();
    }

    /**
     * Get all active delivery units that are delivery units of a portfolio
     * entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static List<OrgUnit> getOrgUnitActiveCanDeliverAsListByPE(Long portfolioEntryId) {
        return findOrgUnit.where().eq("deleted", false).eq("isActive", true).eq("canDeliver", true).eq("deliveredActivities.id", portfolioEntryId).findList();
    }

    /**
     * WARNING: use carefully this method since it unactivate ALL THE ORG UNITS
     * in the database.
     * 
     * @param whereClause
     *            a where clause (without the where statement)
     */
    public static int unactivateOrgUnits(String whereClause) {
        String sql = "update org_unit set is_active=0 " + (StringUtils.isBlank(whereClause) ? "" : "where " + whereClause);
        SqlUpdate update = Ebean.createSqlUpdate(sql);
        return Ebean.execute(update);
    }

    /**
     * Search from org unit for which the criteria matches with the name or the
     * ref id.
     * 
     * note: the name is either directly in the org unit table or in the i18n
     * messages table
     * 
     * @param key
     *            the search criteria (use % for wild cards)
     * @param mustBeActive
     *            if true then the search is done only on active org units
     * @param mustBeSponsor
     *            if true then the search is done only sponsoring units
     * @param mustBeDelivery
     *            if the then the search is done only on delivery units
     */
    public static List<OrgUnit> getOrgUnitAsListByKeywordsAndFilter(String key, boolean mustBeActive, boolean mustBeSponsor, boolean mustBeDelivery) {

        key = key.replace("\"", "\\\"");

        String sql = "SELECT ou.id FROM `org_unit` ou LEFT OUTER JOIN `i18n_messages` im ON im.key = ou.name WHERE ou.deleted = 0";

        if (mustBeActive) {
            sql += " AND ou.is_active = 1";
        }

        if (mustBeSponsor) {
            sql += " AND ou.can_sponsor = 1";
        }

        if (mustBeDelivery) {
            sql += " AND ou.can_deliver = 1";
        }

        sql += " AND (im.language = '" + Http.Context.current().lang().code() + "' OR im.language IS NULL)";

        sql += " AND (ou.name LIKE \"" + key + "%\" OR ou.ref_id LIKE \"" + key + "%\" OR im.value LIKE \"" + key + "%\") ";

        RawSql rawSql = RawSqlBuilder.parse(sql).columnMapping("ou.id", "id").create();

        return findOrgUnit.query().setRawSql(rawSql).findList();
    }
    public static List<OrgUnit> getOrgUnitAsListByKeywordsAndFilter2(String key, boolean mustBeActive, boolean mustBeSponsor, boolean mustBeDelivery) {
        key = key.replace("\"", "\\\"");
        String sql = "SELECT ou.id FROM `org_unit` ou LEFT OUTER JOIN `i18n_messages` im ON im.key = ou.name WHERE ou.deleted = 0";
        if (mustBeActive) {
            sql += " AND ou.is_active = 1";
        }
        if (mustBeSponsor) {
            sql += " AND ou.can_sponsor = 1";
        }
        if (mustBeDelivery) {
            sql += " AND ou.can_deliver = 1";
        }
        sql += " AND (im.language = '" + Http.Context.current().lang().code() + "' OR im.language IS NULL)";
        sql += " AND (ou.name LIKE \"" + key + "\" OR ou.ref_id LIKE \"" + key + "\" OR im.value LIKE \"" + key + "\") ";
        RawSql rawSql = RawSqlBuilder.parse(sql).columnMapping("ou.id", "id").create();
        return findOrgUnit.query().setRawSql(rawSql).findList();
    }

    /**
     * Search from the active delivery units that are delivery units of a
     * portfolio entry and for which the criteria matches with the name or the
     * ref id.
     * 
     * note: the name is either directly in the org unit table or in the i18n
     * messages table
     * 
     * @param key
     *            the search criteria (use % for wild cards)
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static List<OrgUnit> getOrgUnitActiveCanDeliverAsListByKeywordsAndPE(String key, Long portfolioEntryId) {

        key = key.replace("\"", "\\\"");

        String sql = "SELECT ou.id FROM `org_unit` ou " + "JOIN portfolio_entry_has_delivery_unit pehdu ON ou.id = pehdu.org_unit_id "
                + "LEFT OUTER JOIN `i18n_messages` im ON im.key = ou.name WHERE ou.deleted = 0 AND ou.is_active = 1 AND ou.can_deliver = 1 "
                + "AND pehdu.portfolio_entry_id = '" + portfolioEntryId + "'";

        sql += " AND (im.language = '" + Http.Context.current().lang().code() + "' OR im.language IS NULL)";

        sql += " AND (ou.name LIKE \"" + key + "%\" OR ou.ref_id LIKE \"" + key + "%\" OR im.value LIKE \"" + key + "%\") ";

        RawSql rawSql = RawSqlBuilder.parse(sql).columnMapping("ou.id", "id").create();

        return findOrgUnit.query().setRawSql(rawSql).findList();
    }

    /**
     * Get all active org units as value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getOrgUnitActiveAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getOrgUnitActiveAsList());
    }

    /**
     * Get all active sponsoring units as value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getOrgUnitActiveCanSponsorAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getOrgUnitActiveCanSponsorAsList());
    }

    /**
     * Get all active delivery units as value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getOrgUnitActiveCanDeliverAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getOrgUnitActiveCanDeliverAsList());
    }

    /**
     * Get all active delivery units that are delivery units of a portfolio
     * entry and return a value holder collection.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static ISelectableValueHolderCollection<Long> getOrgUnitActiveCanDeliverAsVHByPE(Long portfolioEntryId) {
        return new DefaultSelectableValueHolderCollection<>(getOrgUnitActiveCanDeliverAsListByPE(portfolioEntryId));
    }

    /**
     * Search from all selectable org unit and return a value holder collection.
     * 
     * @param key
     *            the key word
     */
    public static ISelectableValueHolderCollection<Long> getOrgUnitActiveAsVHByKeywords(String key) {
        return new DefaultSelectableValueHolderCollection<>(getOrgUnitAsListByKeywordsAndFilter(key, true, false, false));
    }

    /**
     * Search from all org unit and return a value holder collection.
     * 
     * @param key
     *            the key word
     */
    public static ISelectableValueHolderCollection<Long> getOrgUnitActiveCanSponsorAsVHByKeywords(String key) {
        return new DefaultSelectableValueHolderCollection<>(getOrgUnitAsListByKeywordsAndFilter(key, true, true, false));
    }

    /**
     * Search from all selectable delivery unit and return a value holder
     * collection.
     * 
     * @param key
     *            the key word
     */
    public static ISelectableValueHolderCollection<Long> getOrgUnitActiveCanDeliverAsVHByKeywords(String key) {
        return new DefaultSelectableValueHolderCollection<>(getOrgUnitAsListByKeywordsAndFilter(key, true, false, true));
    }

    /**
     * Search from the active delivery units that are delivery units of a
     * portfolio entry and return a value holder collection.
     * 
     * @param key
     *            the key word
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static ISelectableValueHolderCollection<Long> getOrgUnitActiveCanDeliverAsVHByKeywordsAndPE(String key, Long portfolioEntryId) {
        return new DefaultSelectableValueHolderCollection<>(getOrgUnitActiveCanDeliverAsListByKeywordsAndPE(key, portfolioEntryId));
    }

    /**
     * Get the org units list with filters.
     * 
     * @param isActive
     *            true to return only active org units, false only non-active,
     *            null all.
     * @param managerId
     *            if not null then return only org units with the given manager.
     * @param parentId
     *            if not null then return only org units with the given parent.
     * @param orgUnitTypeId
     *            if not null then return only org units with the given type.
     * @param canSponsor
     *            if not null then return only sponsoring units.
     * @param canDeliver
     *            if not null then return only delivery units.
     **/
    public static List<OrgUnit> getOrgUnitAsListByFilter(Boolean isActive, Long managerId, Long parentId, Long orgUnitTypeId, Boolean canSponsor,
            Boolean canDeliver) {

        ExpressionList<OrgUnit> e = findOrgUnit.where().eq("deleted", false);

        if (isActive != null) {
            e = e.eq("isActive", isActive);
        }
        if (managerId != null) {
            e = e.eq("manager.id", managerId);
        }
        if (parentId != null) {
            e = e.eq("parent.id", parentId);
        }
        if (orgUnitTypeId != null) {
            e = e.eq("orgUnitType.id", orgUnitTypeId);
        }
        if (canSponsor != null) {
            e = e.eq("canSponsor", canSponsor);
        }
        if (canDeliver != null) {
            e = e.eq("canDeliver", canDeliver);
        }

        return e.findList();
    }

    /**
     * Get an org unit type by id.
     * 
     * @param id
     *            the org unit type id
     */
    public static OrgUnitType getOrgUnitTypeById(Long id) {
        return findOrgUnitType.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get an org unit type by ref id.
     * 
     * @param refId
     *            the org unit type ref Id.
     */
    public static OrgUnitType getOrgUnitTypeByRefId(String refId) {
        try {
            return findOrgUnitType.where().eq("deleted", false).eq("refId", refId).findUnique();
        } catch (PersistenceException e) {
            return findOrgUnitType.where().eq("deleted", false).eq("refId", refId).findList().get(0);
        }
    }

    /**
     * Get all org unit types.
     */
    public static List<OrgUnitType> getOrgUnitTypeAsList() {
        return findOrgUnitType.where().eq("deleted", false).findList();
    }

    /**
     * Get all selectable org unit types.
     */
    public static List<OrgUnitType> getOrgUnitTypeActiveAsList() {
        return findOrgUnitType.where().eq("deleted", false).eq("selectable", true).findList();
    }

    /**
     * Get all selectable org unit types as value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getOrgUnitTypeActiveAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getOrgUnitTypeActiveAsList());
    }

    /**
     * Get the org unit types list with filters.
     * 
     * @param isActive
     *            true to return only active org units, false only non-active,
     *            null all.
     */
    public static List<OrgUnitType> getOrgUnitTypeAsListByFilter(Boolean isActive) {
        if (isActive != null) {
            return findOrgUnitType.where().eq("deleted", false).eq("selectable", isActive).findList();
        } else {
            return getOrgUnitTypeAsList();
        }
    }

}
