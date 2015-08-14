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

import java.util.ArrayList;
import java.util.List;

import models.reporting.Reporting;
import models.reporting.ReportingCategory;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import utils.form.ReportingFormData;
import utils.table.ReportingListView;
import constants.IMafConstants;
import dao.reporting.ReportingDao;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

/**
 * The controller which allows to manage the reports.
 * 
 * @author Johann Kohler
 */
@Restrict({ @Group(IMafConstants.REPORTING_ADMINISTRATION_PERMISSION) })
public class ReportingController extends Controller {

    public static Form<ReportingFormData> formTemplate = Form.form(ReportingFormData.class);
    
    /**
     * The reporting administration page.
     */
    public Result index() {

        List<ReportingCategory> roots = ReportingDao.getReportingCategoryRootsAsList();
        Long categoryId = null;
        if (roots != null && roots.size() > 0) {
            categoryId = roots.get(0).id;
        }

        return ok(views.html.admin.reporting.index.render(categoryId));
    }

    /**
     * The reporting administration page for a specific category.
     * 
     * @param categoryId
     *            the category id
     */
    public Result indexForCategory(Long categoryId) {
        return ok(views.html.admin.reporting.index.render(categoryId));
    }

    /**
     * Display the list of reports for a category.
     */
    public Result listFragment() {

        if (request().getQueryString("categoryId") != null) {

            Long categoryId = Long.valueOf(request().getQueryString("categoryId"));

            ReportingCategory category = ReportingDao.getReportingCategoryById(categoryId);

            List<Reporting> reports = ReportingDao.getReportingAsListByCategory(categoryId);

            List<ReportingListView> reportingListView = new ArrayList<ReportingListView>();
            for (Reporting report : reports) {
                reportingListView.add(new ReportingListView(report));
            }

            Table<ReportingListView> table = ReportingListView.templateTable.fill(reportingListView);
            table.setLineAction(null);

            return ok(views.html.admin.reporting.fragment_list.render(category, table));

        } else {
            return badRequest();
        }

    }

    /**
     * Display the form to edit the configuration of a report.
     * 
     * @param id
     *            the report id
     */
    public Result edit(Long id) {

        Reporting report = ReportingDao.getReportingById(id);

        Form<ReportingFormData> reportForm = formTemplate.fill(new ReportingFormData(report));

        return ok(views.html.admin.reporting.edit.render(report, reportForm));
    }

    /**
     * Save the configuration of a report.
     */
    public Result save() {

        // bind the form
        Form<ReportingFormData> boundForm = formTemplate.bindFromRequest();

        // get the report
        Long id = Long.valueOf(request().body().asFormUrlEncoded().get("id")[0]);
        Reporting report = ReportingDao.getReportingById(id);

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.reporting.edit.render(report, boundForm));
        }

        ReportingFormData reportingFormData = boundForm.get();

        reportingFormData.fill(report);
        report.save();
        // report.reportingAuthorization.saveManyToManyAssociations("principals");

        Utilities.sendSuccessFlashMessage(Msg.get("admin.reporting.edit.successful"));

        return redirect(controllers.admin.routes.ReportingController.indexForCategory(report.reportingCategory.id));
    }
}
