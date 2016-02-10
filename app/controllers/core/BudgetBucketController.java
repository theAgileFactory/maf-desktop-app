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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import dao.finance.BudgetBucketDAO;
import dao.finance.CurrencyDAO;
import dao.finance.PortfolioEntryBudgetDAO;
import framework.highcharts.pattern.BasicBar;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.custom_attribute.ICustomAttributeManagerService;
import framework.utils.Msg;
import framework.utils.Pagination;
import framework.utils.Table;
import framework.utils.Utilities;
import models.finance.BudgetBucket;
import models.finance.BudgetBucketLine;
import models.finance.PortfolioEntryBudgetLine;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.CheckBudgetBucketExists;
import services.tableprovider.ITableProvider;
import utils.form.BudgetBucketFormData;
import utils.form.BudgetBucketLineFormData;
import utils.table.BudgetBucketLineListView;
import utils.table.PortfolioEntryBudgetLineListView;

/**
 * The controller which manage the budget buckets.
 * 
 * 
 * @author Johann Kohler
 */
public class BudgetBucketController extends Controller {

    @Inject
    private ITableProvider tableProvider;
    @Inject
    private IPreferenceManagerPlugin preferenceManagerPlugin;
    @Inject
    private ICustomAttributeManagerService customAttributeManagerService;

    public static Form<BudgetBucketFormData> formTemplate = Form.form(BudgetBucketFormData.class);
    public static Form<BudgetBucketLineFormData> lineFormTemplate = Form.form(BudgetBucketLineFormData.class);

    /**
     * Display the details of a budget bucket.
     * 
     * @param id
     *            the budget bucket id
     * @param budgetTablePage
     *            the current page for the budget table
     * @param initiativeBudgetTablePage
     *            the current page for the initiative budget table
     */
    @With(CheckBudgetBucketExists.class)
    @Dynamic(IMafConstants.BUDGET_BUCKET_VIEW_DYNAMIC_PERMISSION)
    public Result view(Long id, Integer budgetTablePage, Integer initiativeBudgetTablePage) {

        // get the budget bucket
        BudgetBucket budgetBucket = BudgetBucketDAO.getBudgetBucketById(id);

        // construct the corresponding form data (for the custom attributes)
        BudgetBucketFormData budgetBucketFormData = new BudgetBucketFormData(budgetBucket);

        // initialize the totals
        Double opexTotalBudget = 0.0;
        Double capexTotalBudget = 0.0;
        Double opexTotalInitiativeBudget = 0.0;
        Double capexTotalInitiativeBudget = 0.0;

        // create the budget table (form budget_bucket_line)
        Pagination<BudgetBucketLine> budgetBucketLinesPagination = BudgetBucketDAO.getBudgetBucketLineAsPaginationByBucket(this.getPreferenceManagerPlugin(),
                id);
        budgetBucketLinesPagination.setCurrentPage(budgetTablePage);
        budgetBucketLinesPagination.setPageQueryName("budgetTablePage");

        List<BudgetBucketLineListView> budgetBucketLineListView = new ArrayList<BudgetBucketLineListView>();
        for (BudgetBucketLine budgetLine : budgetBucketLinesPagination.getListOfObjects()) {
            budgetBucketLineListView.add(new BudgetBucketLineListView(budgetLine));
        }
        Table<BudgetBucketLineListView> budgetBucketLinesTable = this.getTableProvider().get().budgetBucketLine.templateTable.fill(budgetBucketLineListView);

        // create the portfolio entry budget table (call initiative budget)
        Set<String> hideColumnsForInitiativeBudgetTable = new HashSet<String>();
        hideColumnsForInitiativeBudgetTable.add("refId");
        hideColumnsForInitiativeBudgetTable.add("editActionLink");
        hideColumnsForInitiativeBudgetTable.add("removeActionLink");
        hideColumnsForInitiativeBudgetTable.add("budgetBucket");

        Pagination<PortfolioEntryBudgetLine> initiativeBudgetLinesPagination = PortfolioEntryBudgetDAO
                .getPEBudgetLineActiveAsPaginationByBucket(this.getPreferenceManagerPlugin(), id);
        initiativeBudgetLinesPagination.setCurrentPage(initiativeBudgetTablePage);
        initiativeBudgetLinesPagination.setPageQueryName("initiativeBudgetTablePage");

        List<PortfolioEntryBudgetLineListView> initiativeBudgetLineListView = new ArrayList<PortfolioEntryBudgetLineListView>();
        for (PortfolioEntryBudgetLine budgetLine : initiativeBudgetLinesPagination.getListOfObjects()) {
            initiativeBudgetLineListView.add(new PortfolioEntryBudgetLineListView(budgetLine));
        }
        Table<PortfolioEntryBudgetLineListView> initiativeBudgetLinesTable = this.getTableProvider().get().portfolioEntryBudgetLine.templateTable
                .fill(initiativeBudgetLineListView, hideColumnsForInitiativeBudgetTable);

        // compute the total budgets
        opexTotalBudget = BudgetBucketDAO.getBudgetAsAmountByBucketAndOpex(id, true);
        capexTotalBudget = BudgetBucketDAO.getBudgetAsAmountByBucketAndOpex(id, false);
        opexTotalInitiativeBudget = PortfolioEntryBudgetDAO.getBudgetAsAmountByBucketAndOpex(id, true);
        capexTotalInitiativeBudget = PortfolioEntryBudgetDAO.getBudgetAsAmountByBucketAndOpex(id, false);

        // compute the financial status

        BasicBar basicBar = new BasicBar();
        basicBar.addCategory(Msg.get("core.budget_bucket.view.chart.budget"));
        basicBar.addCategory(Msg.get("core.budget_bucket.view.chart.initiative"));

        BasicBar.Elem capexElem = new BasicBar.Elem("CAPEX");
        capexElem.addValue(capexTotalBudget);
        capexElem.addValue(capexTotalInitiativeBudget);
        basicBar.addElem(capexElem);

        BasicBar.Elem opexElem = new BasicBar.Elem("OPEX");
        opexElem.addValue(opexTotalBudget);
        opexElem.addValue(opexTotalInitiativeBudget);
        basicBar.addElem(opexElem);

        return ok(views.html.core.budgetbucket.budget_bucket_view.render(budgetBucket, budgetBucketFormData, budgetBucketLinesTable,
                budgetBucketLinesPagination, initiativeBudgetLinesTable, initiativeBudgetLinesPagination, basicBar));
    }

    /**
     * Form to create a new budget bucket.
     */
    @Restrict({ @Group(IMafConstants.BUDGET_BUCKET_EDIT_ALL_PERMISSION) })
    public Result create() {

        // load the form
        Form<BudgetBucketFormData> form = formTemplate;

        // add the custom attributes default values
        this.getCustomAttributeManagerService().fillWithValues(form, BudgetBucket.class, null);

        return ok(views.html.core.budgetbucket.budget_bucket_new.render(form));
    }

    /**
     * Process the creation of a budget bucket.
     */
    @Restrict({ @Group(IMafConstants.BUDGET_BUCKET_EDIT_ALL_PERMISSION) })
    public Result createSubmit() {

        // bind the form
        Form<BudgetBucketFormData> boundForm = formTemplate.bindFromRequest();

        if (boundForm.hasErrors() || this.getCustomAttributeManagerService().validateValues(boundForm, BudgetBucket.class)) {
            return ok(views.html.core.budgetbucket.budget_bucket_new.render(boundForm));
        }

        BudgetBucketFormData budgetBucketFormData = boundForm.get();

        BudgetBucket budgetBucket = new BudgetBucket();
        budgetBucketFormData.fill(budgetBucket);
        budgetBucket.save();

        Utilities.sendSuccessFlashMessage(Msg.get("core.budget_bucket.new.successful"));

        // save the custom attributes
        this.getCustomAttributeManagerService().validateAndSaveValues(boundForm, BudgetBucket.class, budgetBucket.id);

        return redirect(controllers.core.routes.BudgetBucketController.view(budgetBucket.id, 0, 0));
    }

    /**
     * Form to edit a budget bucket.
     * 
     * @param id
     *            the budget bucket id
     */
    @With(CheckBudgetBucketExists.class)
    @Dynamic(IMafConstants.BUDGET_BUCKET_EDIT_DYNAMIC_PERMISSION)
    public Result edit(Long id) {

        // get the budget bucket
        BudgetBucket budgetBucket = BudgetBucketDAO.getBudgetBucketById(id);

        // load the form
        Form<BudgetBucketFormData> form = formTemplate.fill(new BudgetBucketFormData(budgetBucket));

        // add the custom attributes values
        this.getCustomAttributeManagerService().fillWithValues(form, BudgetBucket.class, id);

        return ok(views.html.core.budgetbucket.budget_bucket_edit.render(budgetBucket, form));
    }

    /**
     * Delete a budget bucket.
     * 
     * @param id
     *            the budget bucket id
     */
    @With(CheckBudgetBucketExists.class)
    @Dynamic(IMafConstants.BUDGET_BUCKET_EDIT_DYNAMIC_PERMISSION)
    public Result delete(Long id) {

        // get the budget bucket
        BudgetBucket budgetBucket = BudgetBucketDAO.getBudgetBucketById(id);

        // set the delete flag to true
        budgetBucket.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("core.budget_bucket.delete.successful"));

        return redirect(controllers.core.routes.SearchController.index());
    }

    /**
     * Save a budget bucket.
     */
    @With(CheckBudgetBucketExists.class)
    @Dynamic(IMafConstants.BUDGET_BUCKET_EDIT_DYNAMIC_PERMISSION)
    public Result save() {

        // bind the form
        Form<BudgetBucketFormData> boundForm = formTemplate.bindFromRequest();

        // get the budget bucket
        Long id = Long.valueOf(boundForm.data().get("id"));
        BudgetBucket budgetBucket = BudgetBucketDAO.getBudgetBucketById(id);

        if (boundForm.hasErrors() || this.getCustomAttributeManagerService().validateValues(boundForm, BudgetBucket.class)) {
            return ok(views.html.core.budgetbucket.budget_bucket_edit.render(budgetBucket, boundForm));
        }

        BudgetBucketFormData formData = boundForm.get();

        formData.fill(budgetBucket);
        budgetBucket.update();

        Utilities.sendSuccessFlashMessage(Msg.get("core.budget_bucket.edit.successful"));

        // save the custom attributes
        this.getCustomAttributeManagerService().validateAndSaveValues(boundForm, BudgetBucket.class, budgetBucket.id);

        return redirect(controllers.core.routes.BudgetBucketController.view(id, 0, 0));
    }

    /**
     * Create or edit a budget bucket line.
     * 
     * @param id
     *            the budget bucket id
     * @param lineId
     *            the budget bucket line id, set to 0 for create case
     */
    @With(CheckBudgetBucketExists.class)
    @Dynamic(IMafConstants.BUDGET_BUCKET_EDIT_DYNAMIC_PERMISSION)
    public Result manageLine(Long id, Long lineId) {

        // get the budget bucket
        BudgetBucket budgetBucket = BudgetBucketDAO.getBudgetBucketById(id);

        // initiate the form with the template
        Form<BudgetBucketLineFormData> lineForm = lineFormTemplate;

        // edit case: inject values
        if (!lineId.equals(Long.valueOf(0))) {

            BudgetBucketLine budgetBucketLine = BudgetBucketDAO.getBudgetBucketLineById(lineId);

            // security: the budget bucket must be related to the object
            if (!budgetBucketLine.budgetBucket.id.equals(id)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            lineForm = lineFormTemplate.fill(new BudgetBucketLineFormData(budgetBucketLine));
        }

        return ok(views.html.core.budgetbucket.budget_bucket_line_manage.render(budgetBucket, lineForm, CurrencyDAO.getCurrencySelectableAsVH()));
    }

    /**
     * Save a budget bucket line.
     */
    @With(CheckBudgetBucketExists.class)
    @Dynamic(IMafConstants.BUDGET_BUCKET_EDIT_DYNAMIC_PERMISSION)
    public Result saveLine() {

        // bind the form
        Form<BudgetBucketLineFormData> boundForm = lineFormTemplate.bindFromRequest();

        // get the budget bucket
        Long id = Long.valueOf(boundForm.data().get("id"));
        BudgetBucket budgetBucket = BudgetBucketDAO.getBudgetBucketById(id);

        if (boundForm.hasErrors()) {
            return ok(views.html.core.budgetbucket.budget_bucket_line_manage.render(budgetBucket, boundForm, CurrencyDAO.getCurrencySelectableAsVH()));
        }

        BudgetBucketLineFormData lineFormData = boundForm.get();

        BudgetBucketLine line = new BudgetBucketLine();

        if (lineFormData.lineId == null) { // create case

            line.budgetBucket = budgetBucket;
            lineFormData.fill(line);
            line.save();

            Utilities.sendSuccessFlashMessage(Msg.get("core.budget_bucket.line.add.successful"));

        } else { // edit case

            line = BudgetBucketDAO.getBudgetBucketLineById(lineFormData.lineId);

            // security: the budget bucket must be related to the object
            if (!line.budgetBucket.id.equals(id)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            lineFormData.fill(line);
            line.update();

            Utilities.sendSuccessFlashMessage(Msg.get("core.budget_bucket.line.edit.successful"));
        }

        return redirect(controllers.core.routes.BudgetBucketController.view(id, 0, 0));
    }

    /**
     * Delete a budget bucket line.
     * 
     * @param id
     *            the budget bucket id
     * @param lineId
     *            the budget bucket line id
     */
    @With(CheckBudgetBucketExists.class)
    @Dynamic(IMafConstants.BUDGET_BUCKET_EDIT_DYNAMIC_PERMISSION)
    public Result deleteLine(Long id, Long lineId) {

        // get the budget bucket line
        BudgetBucketLine line = BudgetBucketDAO.getBudgetBucketLineById(lineId);

        // security: the budget bucket must be related to the object
        if (!line.budgetBucket.id.equals(id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        // set the delete flag to true
        line.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("core.budget_bucket.line.delete.successful"));

        return redirect(controllers.core.routes.BudgetBucketController.view(id, 0, 0));
    }

    /**
     * Get the table provider.
     */
    private ITableProvider getTableProvider() {
        return this.tableProvider;
    }

    /**
     * Get the preference manager service.
     */
    private IPreferenceManagerPlugin getPreferenceManagerPlugin() {
        return this.preferenceManagerPlugin;
    }

    /**
     * Get the custom attribute manager service.
     */
    private ICustomAttributeManagerService getCustomAttributeManagerService() {
        return this.customAttributeManagerService;
    }

}
