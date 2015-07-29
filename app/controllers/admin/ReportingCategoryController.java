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
package controllers.admin;

import java.util.List;

import models.reporting.ReportingCategory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import dao.reporting.ReportingDao;
import framework.taftree.TafTreeHelper;
import framework.utils.DefaultSelectableValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolder;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Msg;
import framework.utils.Utilities;

/**
 * This controller manages the ajax actions for the reporting categories (using
 * the taf tree jQuery plugin).
 * 
 * @author Johann Kohler
 */
@Restrict({ @Group(IMafConstants.REPORTING_ADMINISTRATION_PERMISSION) })
public class ReportingCategoryController extends Controller {

    /**
     * Action that manages a category.
     */
    public static Result manage() {

        try {

            Long id = TafTreeHelper.getId(request());
            ReportingCategory category = null;
            if (id == null) {
                category = new ReportingCategory();
            } else {
                category = ReportingDao.getReportingCategoryById(id);
            }

            // impossible to delete a category which contains a report
            String action = TafTreeHelper.getAction(request());
            if (action != null && action.equals("delete") && category.reports.size() > 0) {
                return badRequest(Msg.get("object.reporting.category.delete.error"));
            }

            TafTreeHelper.fill(request(), category);
            category.save();

            return ok(TafTreeHelper.get(category));

        } catch (IllegalArgumentException e) {
            return badRequest();
        }

    }

    /**
     * Action that loads the children of a category.
     */
    public static Result loadChildren() {

        try {

            Long id = TafTreeHelper.getId(request());
            List<ReportingCategory> categories = null;
            if (id == null) {
                categories = ReportingDao.getReportingCategoryRootsAsList();
            } else {
                categories = ReportingDao.getReportingCategoryAsListByParent(id);
            }

            return ok(TafTreeHelper.gets(categories));

        } catch (IllegalArgumentException e) {
            return badRequest();
        }
    }

    /**
     * Search categories thanks a key word.
     */
    public static Result search() {

        String query = request().queryString().get("query") != null ? request().queryString().get("query")[0] : null;
        String value = request().queryString().get("value") != null ? request().queryString().get("value")[0] : null;

        if (query != null) {
            ISelectableValueHolderCollection<Long> categories = new DefaultSelectableValueHolderCollection<Long>();
            for (ReportingCategory category : ReportingDao.getReportingCategoryAsListByKeywords(query)) {
                categories.add(new DefaultSelectableValueHolder<Long>(category.id, category.getTranslatedFullName()));
            }
            return ok(Utilities.marshallAsJson(categories.getValues()));
        }

        if (value != null) {
            ReportingCategory category = ReportingDao.getReportingCategoryById(Long.valueOf(value));
            ISelectableValueHolder<Long> categoryAsValueHolder = new DefaultSelectableValueHolder<Long>(category.id, category.getTranslatedFullName());
            return ok(Utilities.marshallAsJson(categoryAsValueHolder, 0));
        }

        return ok(Json.newObject());

    }

}
