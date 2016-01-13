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

import javax.inject.Inject;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import dao.reporting.ReportingDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.taftree.EntityTafTreeNodeWrapper;
import framework.taftree.TafTreeHelper;
import framework.utils.DefaultSelectableValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolder;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Msg;
import framework.utils.Utilities;
import models.reporting.ReportingCategory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * This controller manages the ajax actions for the reporting categories (using
 * the taf tree jQuery plugin).
 * 
 * @author Johann Kohler
 */
@Restrict({ @Group(IMafConstants.REPORTING_ADMINISTRATION_PERMISSION) })
public class ReportingCategoryController extends Controller {
    @Inject
    private II18nMessagesPlugin messagesPlugin;

    /**
     * Action that manages a category.
     */
    public Result manage() {

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

            TafTreeHelper.fill(request(), new EntityTafTreeNodeWrapper<ReportingCategory>(category), getMessagesPlugin());
            category.save();

            return ok(TafTreeHelper.get(new EntityTafTreeNodeWrapper<ReportingCategory>(category), getMessagesPlugin()));

        } catch (IllegalArgumentException e) {
            return badRequest();
        }

    }

    /**
     * Action that loads the children of a category.
     */
    public Result loadChildren() {

        try {

            Long id = TafTreeHelper.getId(request());
            List<ReportingCategory> categories = null;
            if (id == null) {
                categories = ReportingDao.getReportingCategoryRootsAsList();
            } else {
                categories = ReportingDao.getReportingCategoryAsListByParent(id);
            }

            return ok(TafTreeHelper.gets(EntityTafTreeNodeWrapper.fromEntityList(categories), getMessagesPlugin()));

        } catch (IllegalArgumentException e) {
            return badRequest();
        }
    }

    /**
     * Search categories thanks a key word.
     */
    public Result search() {

        String query = request().queryString().get("query") != null ? request().queryString().get("query")[0] : null;
        String value = request().queryString().get("value") != null ? request().queryString().get("value")[0] : null;

        if (query != null) {
            ISelectableValueHolderCollection<Long> categories = new DefaultSelectableValueHolderCollection<Long>();
            for (ReportingCategory category : ReportingDao.getReportingCategoryAsListByKeywords(query)) {
                EntityTafTreeNodeWrapper<ReportingCategory> nodeWraper = new EntityTafTreeNodeWrapper<ReportingCategory>(category);
                categories.add(new DefaultSelectableValueHolder<Long>(category.id, nodeWraper.getTranslatedFullName(getMessagesPlugin())));
            }
            return ok(Utilities.marshallAsJson(categories.getValues()));
        }

        if (value != null) {
            ReportingCategory category = ReportingDao.getReportingCategoryById(Long.valueOf(value));
            EntityTafTreeNodeWrapper<ReportingCategory> nodeWraper = new EntityTafTreeNodeWrapper<ReportingCategory>(category);
            ISelectableValueHolder<Long> categoryAsValueHolder = new DefaultSelectableValueHolder<Long>(category.id,
                    nodeWraper.getTranslatedFullName(getMessagesPlugin()));
            return ok(Utilities.marshallAsJson(categoryAsValueHolder, 0));
        }

        return ok(Json.newObject());

    }

    /**
     * Get the i18n messages service.
     */
    private II18nMessagesPlugin getMessagesPlugin() {
        return messagesPlugin;
    }

}
