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
package controllers.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.framework_models.common.CustomAttributeDefinition;
import models.framework_models.common.ICustomAttributeValue;
import models.reporting.Reporting;
import models.reporting.ReportingCategory;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.CheckReportingExists;
import security.DefaultDynamicResourceHandler;
import security.dynamic.ReportingDynamicHelper;
import utils.form.ReportingParamsFormData;
import utils.reporting.JasperUtils;
import utils.table.ReportingListView;
import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import com.avaje.ebean.Expr;

import constants.IMafConstants;
import controllers.ControllersUtils;
import dao.reporting.ReportingDao;
import framework.services.account.AccountManagementException;
import framework.taftree.TafTreeHelper;
import framework.utils.CustomAttributeFormAndDisplayHandler;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;

/**
 * The controller which allows to view the reports.
 * 
 * @author Johann Kohler
 */
public class ReportingController extends Controller {

    private static Logger.ALogger log = Logger.of(ReportingController.class);

    public static Form<ReportingParamsFormData> formTemplate = Form.form(ReportingParamsFormData.class);

    /**
     * Display the reporting categories with the list of reports for the main
     * one.
     */
    @Restrict({ @Group(IMafConstants.REPORTING_VIEW_ALL_PERMISSION), @Group(IMafConstants.REPORTING_VIEW_AS_VIEWER_PERMISSION) })
    public static Result index() {
        List<ReportingCategory> roots = ReportingDao.getReportingCategoryRootsAsList();
        Long categoryId = null;
        if (roots != null && roots.size() > 0) {
            categoryId = roots.get(0).id;
        }
        return ok(views.html.core.reporting.index.render(categoryId));
    }

    /**
     * Display the reporting categories with the list of reports for the given
     * one.
     * 
     * @param categoryId
     *            the category id
     */
    public static Result indexForCategory(Long categoryId) {
        return ok(views.html.core.reporting.index.render(categoryId));
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
     * Display the list of authorized reports for a category.
     */
    @Restrict({ @Group(IMafConstants.REPORTING_VIEW_ALL_PERMISSION), @Group(IMafConstants.REPORTING_VIEW_AS_VIEWER_PERMISSION) })
    public static Result listFragment() {

        if (request().getQueryString("categoryId") != null) {

            Long categoryId = Long.valueOf(request().getQueryString("categoryId"));

            ReportingCategory category = ReportingDao.getReportingCategoryById(categoryId);

            List<Reporting> reports;
            try {
                reports = ReportingDynamicHelper.getReportsViewAllowedAsQuery(Expr.eq("reportingCategory.id", categoryId), null).findList();
            } catch (AccountManagementException e) {
                return ControllersUtils.logAndReturnUnexpectedError(e, log);
            }

            List<ReportingListView> reportingListView = new ArrayList<ReportingListView>();
            for (Reporting report : reports) {
                reportingListView.add(new ReportingListView(report));
            }

            Set<String> columnsToHide = new HashSet<String>();
            columnsToHide.add("isPublic");
            columnsToHide.add("editActionLink");

            Table<ReportingListView> table = ReportingListView.templateTable.fill(reportingListView, columnsToHide);

            return ok(views.html.core.reporting.fragment_list.render(category, table));

        } else {
            return badRequest();
        }

    }

    /**
     * Configure the parameters of a report in order to generate it.
     * 
     * @param id
     *            the report id
     */
    @With(CheckReportingExists.class)
    @Dynamic(DefaultDynamicResourceHandler.REPORTING_VIEW_DYNAMIC_PERMISSION)
    public static Result parametrize(Long id) {

        // get the report
        Reporting report = ReportingDao.getReportingById(id);

        // load the form
        Form<ReportingParamsFormData> form = formTemplate.fill(new ReportingParamsFormData(report));

        return ok(views.html.core.reporting.parametrize.render(report, form));
    }

    /**
     * Generate a report.
     */
    @With(CheckReportingExists.class)
    @Dynamic(DefaultDynamicResourceHandler.REPORTING_VIEW_DYNAMIC_PERMISSION)
    public static Result generate() {

        // bind the form
        Form<ReportingParamsFormData> boundForm = formTemplate.bindFromRequest();

        // get the report
        Long id = Long.valueOf(boundForm.data().get("id"));
        Reporting report = ReportingDao.getReportingById(id);

        if (boundForm.hasErrors() || CustomAttributeFormAndDisplayHandler.validateValues(boundForm, ReportingParamsFormData.class, report.template)) {
            return ok(views.html.core.reporting.parametrize.render(report, boundForm));
        }

        ReportingParamsFormData reportingParamsFormData = boundForm.get();

        // construct the report parameters
        Map<String, Object> reportParameters = new HashMap<String, Object>();
        Map<String, String> data = boundForm.data();
        if (data != null) {
            List<ICustomAttributeValue> customAttributeValues = null;
            customAttributeValues = CustomAttributeDefinition.getOrderedCustomAttributeValues(ReportingParamsFormData.class, report.template, null);
            if (customAttributeValues != null) {
                for (ICustomAttributeValue customAttributeValue : customAttributeValues) {
                    String fieldName = CustomAttributeFormAndDisplayHandler.getFieldNameFromDefinitionUuid(customAttributeValue.getDefinition().uuid);
                    customAttributeValue.parse(data.get(fieldName));
                    reportParameters.put(customAttributeValue.getDefinition().uuid, customAttributeValue.getValueAsObject());
                }
            }
        }

        JasperUtils.generate(ctx(), report, reportingParamsFormData.language, Reporting.Format.valueOf(reportingParamsFormData.format), reportParameters);

        Utilities.sendSuccessFlashMessage(Msg.get("core.reporting.generate.request.success"));

        return redirect(controllers.core.routes.ReportingController.indexForCategory(report.reportingCategory.id));
    }
}
