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
package dao.reporting;

import java.util.ArrayList;
import java.util.List;

import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.google.common.collect.Lists;

import models.reporting.Reporting;
import models.reporting.ReportingAuthorization;
import models.reporting.ReportingCategory;
import com.avaje.ebean.Model.Finder;
import play.mvc.Http;

/**
 * DAO for the {@link Reporting} and {@link ReportingAuthorization} and
 * {@link ReportingCategory} objects.
 * 
 * @author Johann Kohler
 */
public abstract class ReportingDao {

    public static Finder<Long, Reporting> findReporting = new Finder<>(Long.class, Reporting.class);

    public static Finder<Long, ReportingAuthorization> findReportingAuthorization = new Finder<>(Long.class, ReportingAuthorization.class);

    public static Finder<Long, ReportingCategory> findReportingCategory = new Finder<>(Long.class, ReportingCategory.class);

    /**
     * Default constructor.
     */
    public ReportingDao() {
    }

    /**
     * Get a report by id.
     * 
     * @param id
     *            the report id
     */
    public static Reporting getReportingById(Long id) {
        return findReporting.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get a report by template name.
     * 
     * @param template
     *            the template name
     */
    public static Reporting getReportingByTemplate(String template) {
        return findReporting.where().eq("deleted", false).eq("template", template).findUnique();
    }

    /**
     * Get all reports of a category.
     * 
     * @param categoryId
     *            the category id
     */
    public static List<Reporting> getReportingAsListByCategory(Long categoryId) {
        return findReporting.where().eq("deleted", false).eq("reportingCategory.id", categoryId).findList();

    }

    /**
     * Get the custom reports.
     */
    public static List<Reporting> getReportingCustomAsList() {
        return findReporting.where().eq("deleted", false).eq("isStandard", false).findList();
    }

    /**
     * Get the all reports.
     */
    public static List<Reporting> getReportingAsList() {
        return findReporting.where().eq("deleted", false).findList();
    }

    /**
     * Get by id.
     * 
     * @param id
     *            the reporting authorization id
     */
    public static ReportingAuthorization getReportingAuthorizationById(Long id) {
        return findReportingAuthorization.where().eq("id", id).findUnique();
    }

    /**
     * Get a reporting category by id.
     * 
     * @param id
     *            the reporting category id
     */
    public static ReportingCategory getReportingCategoryById(Long id) {
        return findReportingCategory.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get the list of ancestors of a reporting category
     * 
     * The first element of the list is the root node, and the last is the
     * concerned node.
     * 
     * @param id
     *            the reporting category id
     */
    public static List<ReportingCategory> getReportingCategoryAncestors(Long id) {
        ReportingCategory leaf = getReportingCategoryById(id);
        List<ReportingCategory> ancestors = new ArrayList<>();
        findReportingCategoryAncestors(ancestors, leaf);
        return Lists.reverse(ancestors);
    }

    /**
     * Find recursively the ancestors of a reporting category.
     * 
     * @param ancestors
     *            the list of ancestors (modified by the method)
     * @param reportingCategory
     *            the current reporting category
     */
    private static void findReportingCategoryAncestors(List<ReportingCategory> ancestors, ReportingCategory reportingCategory) {
        ancestors.add(reportingCategory);
        if (reportingCategory.getParent() != null) {
            findReportingCategoryAncestors(ancestors, reportingCategory.parent);
        }
    }

    /**
     * Get the root categories (meaning categories without parent).
     */
    public static List<ReportingCategory> getReportingCategoryRootsAsList() {
        return findReportingCategory.orderBy("order").where().eq("deleted", false).isNull("parent").findList();
    }

    /**
     * Get the children categories of a category.
     * 
     * @param parentId
     *            the category id for which we need the children
     */
    public static List<ReportingCategory> getReportingCategoryAsListByParent(Long parentId) {
        return findReportingCategory.orderBy("order").where().eq("deleted", false).eq("parent.id", parentId).findList();
    }

    /**
     * Search categories.
     * 
     * @param key
     *            the key word
     */
    public static List<ReportingCategory> getReportingCategoryAsListByKeywords(String key) {

        String sql = "SELECT rc.id FROM `reporting_category` rc LEFT OUTER JOIN `i18n_messages` im ON im.key = rc.name WHERE rc.deleted = 0";

        sql += " AND (im.language = '" + Http.Context.current().lang().code() + "' OR im.language IS NULL)";

        sql += " AND (rc.name LIKE \"" + key + "%\" OR im.value LIKE \"" + key + "%\") ";

        RawSql rawSql = RawSqlBuilder.parse(sql).columnMapping("rc.id", "id").create();

        return findReportingCategory.query().setRawSql(rawSql).findList();

    }

}
