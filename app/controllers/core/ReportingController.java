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

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.avaje.ebean.Expr;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import controllers.ControllersUtils;
import dao.reporting.ReportingDao;
import framework.security.ISecurityService;
import framework.services.account.AccountManagementException;
import framework.services.configuration.II18nMessagesPlugin;
import framework.taftree.EntityTafTreeNodeWrapper;
import framework.taftree.TafTreeHelper;
import framework.utils.CustomAttributeFormAndDisplayHandler;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import models.framework_models.common.CustomAttributeDefinition;
import models.framework_models.common.ICustomAttributeValue;
import models.reporting.Reporting;
import models.reporting.ReportingCategory;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.CheckReportingExists;
import security.dynamic.ReportingDynamicHelper;
import services.tableprovider.ITableProvider;
import utils.form.ReportingParamsFormData;
import utils.reporting.IReportingUtils;
import utils.table.ReportingListView;

/**
 * The controller which allows to view the reports.
 * 
 * @author Johann Kohler
 */
public class ReportingController extends Controller {
    @Inject
    private IReportingUtils reportingUtils;
    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;
    @Inject
    private ISecurityService securityService;
    @Inject
    private Configuration configuration;
    @Inject
    private ITableProvider tableProvider;

    private static Logger.ALogger log = Logger.of(ReportingController.class);

    public static Form<ReportingParamsFormData> formTemplate = Form.form(ReportingParamsFormData.class);

    /**
     * Display the reporting categories with the list of reports for the main
     * one.
     */
    @Restrict({ @Group(IMafConstants.REPORTING_VIEW_ALL_PERMISSION), @Group(IMafConstants.REPORTING_VIEW_AS_VIEWER_PERMISSION) })
    public Result index() {
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
    @Restrict({ @Group(IMafConstants.REPORTING_VIEW_ALL_PERMISSION), @Group(IMafConstants.REPORTING_VIEW_AS_VIEWER_PERMISSION) })
    public Result indexForCategory(Long categoryId) {
        return ok(views.html.core.reporting.index.render(categoryId));
    }

    /**
     * Action that loads the children of a category.
     */
    @Restrict({ @Group(IMafConstants.REPORTING_VIEW_ALL_PERMISSION), @Group(IMafConstants.REPORTING_VIEW_AS_VIEWER_PERMISSION) })
    public Result loadChildren() {

        try {

            Long id = TafTreeHelper.getId(request());
            List<ReportingCategory> categories = null;
            if (id == null) {
                categories = ReportingDao.getReportingCategoryRootsAsList();
            } else {
                categories = ReportingDao.getReportingCategoryAsListByParent(id);
            }

            return ok(TafTreeHelper.gets(EntityTafTreeNodeWrapper.fromEntityList(categories), getI18nMessagesPlugin()));

        } catch (IllegalArgumentException e) {
            return badRequest();
        }
    }

    /**
     * Display the list of authorized reports for a category.
     */
    @Restrict({ @Group(IMafConstants.REPORTING_VIEW_ALL_PERMISSION), @Group(IMafConstants.REPORTING_VIEW_AS_VIEWER_PERMISSION) })
    public Result listFragment() {

        if (request().getQueryString("categoryId") != null) {

            Long categoryId = Long.valueOf(request().getQueryString("categoryId"));

            ReportingCategory category = ReportingDao.getReportingCategoryById(categoryId);

            List<Reporting> reports;
            try {
                reports = ReportingDynamicHelper.getReportsViewAllowedAsQuery(
                        Expr.and(Expr.eq("isActive", true), Expr.eq("reportingCategory.id", categoryId)), null, getSecurityService()).findList();
            } catch (AccountManagementException e) {
                return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
            }

            List<ReportingListView> reportingListView = new ArrayList<ReportingListView>();
            for (Reporting report : reports) {
                reportingListView.add(new ReportingListView(report));
            }

            Set<String> columnsToHide = new HashSet<String>();
            columnsToHide.add("isPublic");
            columnsToHide.add("editActionLink");

            Table<ReportingListView> table = this.getTableProvider().get().reporting.templateTable.fill(reportingListView, columnsToHide);

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
    @Dynamic(IMafConstants.REPORTING_VIEW_DYNAMIC_PERMISSION)
    public Result parametrize(Long id) {

        // get the report
        Reporting report = ReportingDao.getReportingById(id);

        if (report.isActive) {

            // load the form
            Form<ReportingParamsFormData> form = formTemplate
                    .fill(new ReportingParamsFormData(report, getI18nMessagesPlugin().getCurrentLanguage().getCode()));

            return ok(views.html.core.reporting.parametrize.render(report, form));

        } else {
            return ok(views.html.error.access_forbidden.render(""));
        }
    }

    /**
     * Generate a report.
     */
    @With(CheckReportingExists.class)
    @Dynamic(IMafConstants.REPORTING_VIEW_DYNAMIC_PERMISSION)
    public Result generate() {

        // bind the form
        Form<ReportingParamsFormData> boundForm = formTemplate.bindFromRequest();

        // get the report
        Long id = Long.valueOf(boundForm.data().get("id"));
        Reporting report = ReportingDao.getReportingById(id);

        if (report.isActive) {

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
                        if (customAttributeValue.getAttributeType().isMultiValued()) {
                            List<String> stringValues = new ArrayList<String>();
                            for (String key : data.keySet()) {
                                if (key.startsWith(fieldName + "[")) {
                                    String stringValue = data.get(key);
                                    stringValues.add(stringValue);
                                }
                            }
                            customAttributeValue.parse(StringUtils.join(stringValues, ICustomAttributeValue.MULTI_VALUE_SEPARATOR));
                        } else {
                            customAttributeValue.parse(data.get(fieldName));
                        }
                        reportParameters.put(customAttributeValue.getDefinition().uuid, customAttributeValue.getValueAsObject());
                    }
                }
            }

            getReportingUtils().generate(ctx(), report, reportingParamsFormData.language, Reporting.Format.valueOf(reportingParamsFormData.format),
                    reportParameters);

            Utilities.sendSuccessFlashMessage(Msg.get("core.reporting.generate.request.success"));

            return redirect(controllers.core.routes.ReportingController.indexForCategory(report.reportingCategory.id));

        } else {
            return ok(views.html.error.access_forbidden.render(""));
        }
    }

    /**
     * Get the reporting utils.
     */
    private IReportingUtils getReportingUtils() {
        return reportingUtils;
    }

    /**
     * Get the i18n messages service.
     */
    private II18nMessagesPlugin getI18nMessagesPlugin() {
        return i18nMessagesPlugin;
    }

    /**
     * Get the security service.
     */
    private ISecurityService getSecurityService() {
        return securityService;
    }

    /**
     * Get the Play configuration service.
     */
    private Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Get the table provider.
     */
    private ITableProvider getTableProvider() {
        return this.tableProvider;
    }
}
