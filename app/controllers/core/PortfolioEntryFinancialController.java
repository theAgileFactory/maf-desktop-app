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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.avaje.ebean.Ebean;

import be.objectify.deadbolt.java.actions.Dynamic;
import constants.IMafConstants;
import controllers.ControllersUtils;
import dao.finance.CurrencyDAO;
import dao.finance.PortfolioEntryBudgetDAO;
import dao.finance.PurchaseOrderDAO;
import dao.finance.WorkOrderDAO;
import dao.pmo.PortfolioEntryDao;
import framework.highcharts.pattern.BasicBar;
import framework.security.ISecurityService;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.utils.CustomAttributeFormAndDisplayHandler;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import models.finance.PortfolioEntryBudget;
import models.finance.PortfolioEntryBudgetLine;
import models.finance.PurchaseOrder;
import models.finance.PurchaseOrderLineItem;
import models.finance.WorkOrder;
import models.governance.LifeCycleInstancePlanning;
import models.pmo.PortfolioEntry;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.CheckPortfolioEntryExists;
import services.budgettracking.IBudgetTrackingService;
import utils.finance.Totals;
import utils.form.EngageWorkOrderAmountSelectorFormData;
import utils.form.PortfolioEntryBudgetLineFormData;
import utils.form.PurchaseOrderLineItemAmountSelectorFormData;
import utils.form.PurchaseOrderSelectorFormData;
import utils.form.WorkOrderFormData;
import utils.table.PortfolioEntryBudgetLineListView;
import utils.table.PurchaseOrderLineItemListView;
import utils.table.WorkOrderListView;

/**
 * The controller which allows to manage the financial part of a portfolio
 * entry.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryFinancialController extends Controller {
    @Inject
    private IUserSessionManagerPlugin userSessionManagerPlugin;
    @Inject
    private IAccountManagerPlugin accountManagerPlugin;
    @Inject
    private ISecurityService securityService;
    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;
    @Inject
    private Configuration configuration;
    @Inject
    private IPreferenceManagerPlugin preferenceManagerPlugin;
    @Inject
    private IBudgetTrackingService budgetTrackingService;

    private static Logger.ALogger log = Logger.of(PortfolioEntryFinancialController.class);

    public static Form<PortfolioEntryBudgetLineFormData> budgetLineFormTemplate = Form.form(PortfolioEntryBudgetLineFormData.class);
    public static Form<WorkOrderFormData> workOrderFormTemplate = Form.form(WorkOrderFormData.class);
    public static Form<PurchaseOrderSelectorFormData> purchaseOrderSelectorFormTemplate = Form.form(PurchaseOrderSelectorFormData.class);
    public static Form<PurchaseOrderLineItemAmountSelectorFormData> lineItemAmountSelectorFormTemplate = Form
            .form(PurchaseOrderLineItemAmountSelectorFormData.class);
    public static Form<EngageWorkOrderAmountSelectorFormData> engageWorkOrderAmountSelectorFormTemplate = Form
            .form(EngageWorkOrderAmountSelectorFormData.class);

    /**
     * Display the details page.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_VIEW_DYNAMIC_PERMISSION)
    public Result details(Long id) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get current planning
        LifeCycleInstancePlanning planning = portfolioEntry.activeLifeCycleInstance.getCurrentLifeCycleInstancePlanning();

        /*
         * create the budget lines table
         */

        // hide columns for budget table
        Set<String> hideColumnsForBudgetTable = new HashSet<String>();
        hideColumnsForBudgetTable.add("portfolioEntryName");
        if (!getSecurityService().dynamic("PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION", "")) {
            hideColumnsForBudgetTable.add("editActionLink");
            hideColumnsForBudgetTable.add("removeActionLink");
        }

        // get or create the budget
        PortfolioEntryBudget budget = planning.portfolioEntryBudget;
        if (budget == null) {
            Logger.debug("a new budget has been automatically created for the portfolio entry " + portfolioEntry.getName());
            budget = new PortfolioEntryBudget();
            planning.portfolioEntryBudget = budget;
            planning.save();
        }

        // construct the table and compute the totals for the default currency
        List<PortfolioEntryBudgetLine> budgetLines = budget.portfolioEntryBudgetLines;
        List<PortfolioEntryBudgetLineListView> portfolioEntryBudgetLineListView = new ArrayList<PortfolioEntryBudgetLineListView>();
        for (PortfolioEntryBudgetLine budgetLine : budgetLines) {
            portfolioEntryBudgetLineListView.add(new PortfolioEntryBudgetLineListView(budgetLine));
        }
        Table<PortfolioEntryBudgetLineListView> budgetLinesTable = PortfolioEntryBudgetLineListView.templateTable.fill(portfolioEntryBudgetLineListView,
                hideColumnsForBudgetTable);

        /*
         * create the work orders tables
         */

        // define the columns to hide for the cost to complete table
        Set<String> hideColumnsForCostToCompleteTable = new HashSet<String>();
        if (!getSecurityService().dynamic("PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION", "")) {
            hideColumnsForCostToCompleteTable.add("editActionLink");
            hideColumnsForCostToCompleteTable.add("deleteActionLink");
        }
        if (!PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder(this.getPreferenceManagerPlugin())
                || !getSecurityService().dynamic("PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION", "")) {
            hideColumnsForCostToCompleteTable.add("selectLineItemActionLink");
        }
        if (PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder(this.getPreferenceManagerPlugin())
                || !getSecurityService().dynamic("PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION", "")) {
            hideColumnsForCostToCompleteTable.add("engageWorkOrder");
        }
        hideColumnsForCostToCompleteTable.add("amountReceived");
        if (!PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder(this.getPreferenceManagerPlugin())) {
            hideColumnsForCostToCompleteTable.add("shared");
        }

        // define the columns to hide for the engaged table
        Set<String> hideColumnsForEngagedTable = new HashSet<String>();
        hideColumnsForEngagedTable.add("selectLineItemActionLink");
        hideColumnsForEngagedTable.add("engageWorkOrder");
        if (!getSecurityService().dynamic("PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION", "")) {
            hideColumnsForEngagedTable.add("editActionLink");
        }
        if (!PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder(this.getPreferenceManagerPlugin())) {
            hideColumnsForEngagedTable.add("shared");
        }
        if (!getSecurityService().dynamic("PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION", "")
                || PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder(this.getPreferenceManagerPlugin())) {
            hideColumnsForEngagedTable.add("deleteActionLink");
        }

        List<WorkOrderListView> costToCompleteWorkOrderListView = new ArrayList<WorkOrderListView>();
        List<WorkOrderListView> engagedWorkOrderListView = new ArrayList<WorkOrderListView>();
        for (WorkOrder workOrder : portfolioEntry.workOrders) {
            if (!workOrder.getComputedIsEngaged(PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder(this.getPreferenceManagerPlugin()))) {
                costToCompleteWorkOrderListView.add(new WorkOrderListView(this.getPreferenceManagerPlugin(), workOrder));
            } else {
                engagedWorkOrderListView.add(new WorkOrderListView(this.getPreferenceManagerPlugin(), workOrder));
            }
        }
        Table<WorkOrderListView> costToCompleteWorkOrderTable = WorkOrderListView.templateTable.fill(costToCompleteWorkOrderListView,
                hideColumnsForCostToCompleteTable);
        Table<WorkOrderListView> engagedWorkOrderTable = WorkOrderListView.templateTable.fill(engagedWorkOrderListView, hideColumnsForEngagedTable);

        /*
         * create the purchase order line items of portfolio entry table
         */
        Table<PurchaseOrderLineItemListView> lineItemsTable = null;
        if (PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder(this.getPreferenceManagerPlugin())) {

            Set<String> hideColumnsForLineItemTable = new HashSet<String>();
            hideColumnsForLineItemTable.add("isAssociated");
            hideColumnsForLineItemTable.add("shared");
            hideColumnsForLineItemTable.add("remainingAmount");
            hideColumnsForLineItemTable.add("isCancelled");
            hideColumnsForLineItemTable.add("selectActionLink");

            List<PurchaseOrderLineItemListView> lineItemListView = new ArrayList<PurchaseOrderLineItemListView>();
            for (PurchaseOrder purchaseOrder : portfolioEntry.purchaseOrders) {
                for (PurchaseOrderLineItem lineItem : purchaseOrder.purchaseOrderLineItems) {
                    if (!lineItem.isAssociated() && !lineItem.isCancelled) {
                        lineItemListView.add(new PurchaseOrderLineItemListView(lineItem));
                    }
                }
            }
            lineItemsTable = PurchaseOrderLineItemListView.templateTable.fill(lineItemListView, hideColumnsForLineItemTable);

            // get the current user
            IUserAccount userAccount;
            try {
                userAccount = getAccountManagerPlugin().getUserAccountFromUid(getUserSessionManagerPlugin().getUserSessionId(ctx()));
            } catch (Exception e) {
                return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
            }

            // if the user hasn't the permission
            // PURCHASE_ORDER_VIEW_ALL_PERMISSION
            // then we remove the action line
            if (!getSecurityService().restrict(IMafConstants.PURCHASE_ORDER_VIEW_ALL_PERMISSION, userAccount)) {
                lineItemsTable.setLineAction(null);
            }

        }

        return ok(views.html.core.portfolioentryfinancial.portfolio_entry_financial_details.render(portfolioEntry, budgetLinesTable,
                costToCompleteWorkOrderTable, engagedWorkOrderTable, lineItemsTable));
    }

    /**
     * Run the budget tracking, meaning update the budget and forecast according
     * to resource allocations.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION)
    public Result budgetTrackingRun(Long id) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get current planning
        LifeCycleInstancePlanning planning = portfolioEntry.activeLifeCycleInstance.getCurrentLifeCycleInstancePlanning();

        Ebean.beginTransaction();
        try {

            // process the run
            getBudgetTrackingService().recomputeAllBugdetAndForecastFromResource(planning);

            Ebean.commitTransaction();
            Ebean.endTransaction();

            Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_financial.budget_tracking.run.successful"));

            return redirect(controllers.core.routes.PortfolioEntryFinancialController.details(id));

        } catch (Exception e) {

            Ebean.rollbackTransaction();
            Ebean.endTransaction();
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());

        }

    }

    /**
     * Display the status page.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_VIEW_DYNAMIC_PERMISSION)
    public Result status(Long id) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // compute the totals
        Double opexTotalBudget = PortfolioEntryDao.getPEAsBudgetAmountByOpex(id, true);
        Double capexTotalBudget = PortfolioEntryDao.getPEAsBudgetAmountByOpex(id, false);
        Double opexTotalCostToComplete = PortfolioEntryDao.getPEAsCostToCompleteAmountByOpex(this.getPreferenceManagerPlugin(), id, true);
        Double capexTotalCostToComplete = PortfolioEntryDao.getPEAsCostToCompleteAmountByOpex(this.getPreferenceManagerPlugin(), id, false);
        Double opexTotalEngaged = PortfolioEntryDao.getPEAsEngagedAmountByOpex(this.getPreferenceManagerPlugin(), id, true);
        Double capexTotalEngaged = PortfolioEntryDao.getPEAsEngagedAmountByOpex(this.getPreferenceManagerPlugin(), id, false);

        /*
         * compute the financial status
         */

        Totals totals = new Totals(opexTotalBudget, capexTotalBudget, opexTotalCostToComplete, capexTotalCostToComplete, opexTotalEngaged, capexTotalEngaged);

        BasicBar basicBar = new BasicBar();
        basicBar.addCategory(Msg.get("core.portfolio_entry_financial.view.status.table.budget.label"));
        basicBar.addCategory(Msg.get("core.portfolio_entry_financial.view.status.table.forecast.label"));

        BasicBar.Elem capexElem = new BasicBar.Elem("CAPEX");
        capexElem.addValue(totals.getCapexBudget());
        capexElem.addValue(totals.getForecast(false));
        basicBar.addElem(capexElem);

        BasicBar.Elem opexElem = new BasicBar.Elem("OPEX");
        opexElem.addValue(totals.getOpexBudget());
        opexElem.addValue(totals.getForecast(true));
        basicBar.addElem(opexElem);

        return ok(views.html.core.portfolioentryfinancial.portfolio_entry_financial_status.render(portfolioEntry, basicBar, totals));

    }

    /**
     * budget lines
     */

    /**
     * Display the details of a budget line.
     * 
     * @param id
     *            the portfolio entry id
     * @param budgetLineId
     *            the budget line id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_VIEW_DYNAMIC_PERMISSION)
    public Result viewBudgetLine(Long id, Long budgetLineId) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the budget line
        PortfolioEntryBudgetLine budgetLine = PortfolioEntryBudgetDAO.getPEBudgetLineById(budgetLineId);

        // construct the corresponding form data (for the custom attributes)
        PortfolioEntryBudgetLineFormData portfolioEntryBudgetLineFormData = new PortfolioEntryBudgetLineFormData(budgetLine);

        return ok(views.html.core.portfolioentryfinancial.portfolio_entry_budget_line_view.render(portfolioEntry, budgetLine,
                portfolioEntryBudgetLineFormData));
    }

    /**
     * Create or edit a budget line.
     * 
     * @param id
     *            the portfolio entry id
     * @param budgetLineId
     *            the budget line id, set to 0 for create case
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION)
    public Result manageBudgetLine(Long id, Long budgetLineId) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // initiate the form with the template
        Form<PortfolioEntryBudgetLineFormData> budgetLineForm = budgetLineFormTemplate;

        boolean fromResource = false;

        // edit case: inject values
        if (!budgetLineId.equals(Long.valueOf(0))) {

            PortfolioEntryBudgetLine portfolioEntryBudgetLine = PortfolioEntryBudgetDAO.getPEBudgetLineById(budgetLineId);

            fromResource = portfolioEntryBudgetLine.resourceObjectType != null;

            // security: the portfolioEntry must be related to the object
            if (!portfolioEntryBudgetLine.portfolioEntryBudget.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id.equals(id)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            budgetLineForm = budgetLineFormTemplate.fill(new PortfolioEntryBudgetLineFormData(portfolioEntryBudgetLine));

            // add the custom attributes values
            CustomAttributeFormAndDisplayHandler.fillWithValues(budgetLineForm, PortfolioEntryBudgetLine.class, budgetLineId);
        } else {
            // add the custom attributes default values
            CustomAttributeFormAndDisplayHandler.fillWithValues(budgetLineForm, PortfolioEntryBudgetLine.class, null);
        }

        return ok(views.html.core.portfolioentryfinancial.portfolio_entry_budget_line_manage.render(portfolioEntry, fromResource, budgetLineForm,
                CurrencyDAO.getCurrencySelectableAsVH()));
    }

    /**
     * Save a budget line.
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION)
    public Result processManageBudgetLine() {

        // bind the form
        Form<PortfolioEntryBudgetLineFormData> boundForm = budgetLineFormTemplate.bindFromRequest();

        // get the portfolioEntry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the fromResource flag
        boolean fromResource = Boolean.valueOf(boundForm.data().get("fromResource"));

        if (boundForm.hasErrors() || CustomAttributeFormAndDisplayHandler.validateValues(boundForm, PortfolioEntryBudgetLine.class)) {
            return ok(views.html.core.portfolioentryfinancial.portfolio_entry_budget_line_manage.render(portfolioEntry, fromResource, boundForm,
                    CurrencyDAO.getCurrencySelectableAsVH()));
        }

        PortfolioEntryBudgetLineFormData portfolioEntryBudgetLineFormData = boundForm.get();

        PortfolioEntryBudgetLine budgetLine = new PortfolioEntryBudgetLine();

        if (portfolioEntryBudgetLineFormData.budgetLineId == null) { // create
                                                                     // case

            // get current planning
            LifeCycleInstancePlanning planning = portfolioEntry.activeLifeCycleInstance.getCurrentLifeCycleInstancePlanning();

            // get the budget
            PortfolioEntryBudget budget = planning.portfolioEntryBudget;

            budgetLine.portfolioEntryBudget = budget;

            portfolioEntryBudgetLineFormData.fill(budgetLine);

            budgetLine.save();

            Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_financial.budget_line.add.successful"));

        } else { // edit case

            budgetLine = PortfolioEntryBudgetDAO.getPEBudgetLineById(portfolioEntryBudgetLineFormData.budgetLineId);

            // security: the portfolioEntry must be related to the object
            if (!budgetLine.portfolioEntryBudget.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id.equals(id)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            portfolioEntryBudgetLineFormData.fill(budgetLine);
            budgetLine.update();

            Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_financial.budget_line.edit.successful"));
        }

        // save the custom attributes
        CustomAttributeFormAndDisplayHandler.validateAndSaveValues(boundForm, PortfolioEntryBudgetLine.class, budgetLine.id);

        return redirect(controllers.core.routes.PortfolioEntryFinancialController.details(id));
    }

    /**
     * Delete a budget line.
     * 
     * @param id
     *            the portfolio entry id
     * @param budgetLineId
     *            the budget line id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION)
    public Result deleteBudgetLine(Long id, Long budgetLineId) {

        // get the budget line
        PortfolioEntryBudgetLine budgetLine = PortfolioEntryBudgetDAO.getPEBudgetLineById(budgetLineId);

        // security: the portfolioEntry must be related to the object
        if (!budgetLine.portfolioEntryBudget.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id.equals(id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        // set the delete flag to true
        budgetLine.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_financial.budget_line.delete.successful"));

        return redirect(controllers.core.routes.PortfolioEntryFinancialController.details(id));
    }

    /**
     * work orders
     */

    /**
     * Display the details of a work order.
     * 
     * @param id
     *            the portfolio entry id
     * @param workOrderId
     *            the work order id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_VIEW_DYNAMIC_PERMISSION)
    public Result viewWorkOrder(Long id, Long workOrderId) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the work order
        WorkOrder workOrder = WorkOrderDAO.getWorkOrderById(workOrderId);

        // construct the corresponding form data (for the custom attributes)
        WorkOrderFormData workOrderFormData = new WorkOrderFormData(workOrder);

        return ok(views.html.core.portfolioentryfinancial.portfolio_entry_work_order_view.render(portfolioEntry, workOrder, workOrderFormData));
    }

    /**
     * Create or edit a work order for a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     * @param workOrderId
     *            the work order id, set to 0 for create case
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION)
    public Result manageWorkOrder(Long id, Long workOrderId) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // initiate the form with the template
        Form<WorkOrderFormData> workOrderForm = workOrderFormTemplate;

        // initiate work order
        WorkOrder workOrder = null;

        // edit case: inject values
        if (!workOrderId.equals(Long.valueOf(0))) {

            workOrder = WorkOrderDAO.getWorkOrderById(workOrderId);

            // security: the portfolioEntry must be related to the object
            if (!workOrder.portfolioEntry.id.equals(id)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            workOrderForm = workOrderFormTemplate.fill(new WorkOrderFormData(workOrder));

            // add the custom attributes values
            CustomAttributeFormAndDisplayHandler.fillWithValues(workOrderForm, WorkOrder.class, workOrderId);
        } else {
            // add the custom attributes default values
            CustomAttributeFormAndDisplayHandler.fillWithValues(workOrderForm, WorkOrder.class, null);
        }

        return ok(views.html.core.portfolioentryfinancial.portfolio_entry_work_order_manage.render(portfolioEntry, workOrder, workOrderForm,
                CurrencyDAO.getCurrencySelectableAsVH()));
    }

    /**
     * Save a work order.
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION)
    public Result processManageWorkOrder() {

        // bind the form
        Form<WorkOrderFormData> boundForm = workOrderFormTemplate.bindFromRequest();

        // get the portfolioEntry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the work order (only for edit case)
        WorkOrder workOrder = null;
        if (boundForm.data().get("workOrderId") != null) {
            Long workOrderId = Long.valueOf(boundForm.data().get("workOrderId"));
            workOrder = WorkOrderDAO.getWorkOrderById(workOrderId);
        }

        if (boundForm.hasErrors() || CustomAttributeFormAndDisplayHandler.validateValues(boundForm, WorkOrder.class)) {
            return ok(views.html.core.portfolioentryfinancial.portfolio_entry_work_order_manage.render(portfolioEntry, workOrder, boundForm,
                    CurrencyDAO.getCurrencySelectableAsVH()));
        }

        WorkOrderFormData workOrderFormData = boundForm.get();

        // if given, check the amount received (must be smaller than the amount)
        if (workOrderFormData.amountReceived != null && workOrderFormData.amountReceived.doubleValue() > workOrderFormData.amount.doubleValue() + 0.01) {
            boundForm.reject("amountReceived", Msg.get("object.work_order.amount_received.invalid"));
            return ok(views.html.core.portfolioentryfinancial.portfolio_entry_work_order_manage.render(portfolioEntry, workOrder, boundForm,
                    CurrencyDAO.getCurrencySelectableAsVH()));
        }

        if (workOrderFormData.workOrderId == null) { // create case

            workOrder = new WorkOrder();
            workOrder.portfolioEntry = portfolioEntry;
            workOrder.creationDate = new Date();
            workOrderFormData.fill(workOrder);
            workOrder.save();
            Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_financial.work_order.add.successful"));

        } else { // edit case

            workOrder = WorkOrderDAO.getWorkOrderById(workOrderFormData.workOrderId);

            // security: the portfolioEntry must be related to the object
            if (!workOrder.portfolioEntry.id.equals(id)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            workOrderFormData.fill(workOrder);
            workOrder.update();

            Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_financial.work_order.edit.successful"));
        }

        // save the custom attributes
        CustomAttributeFormAndDisplayHandler.validateAndSaveValues(boundForm, WorkOrder.class, workOrder.id);

        return redirect(controllers.core.routes.PortfolioEntryFinancialController.details(id));
    }

    /**
     * Delete a work order.
     * 
     * @param id
     *            the portfolio entry id
     * @param workOrderId
     *            the work order id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION)
    public Result deleteWorkOrder(Long id, Long workOrderId) {

        // get the work order
        WorkOrder workOrder = WorkOrderDAO.getWorkOrderById(workOrderId);

        // security: the portfolioEntry must be related to the object
        if (!workOrder.portfolioEntry.id.equals(id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        // set the delete flag to true
        workOrder.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_financial.work_order.deleted.successful"));

        return redirect(controllers.core.routes.PortfolioEntryFinancialController.details(id));
    }

    /**
     * Engage a work order (set its isEngaged flag to true), only if the
     * purchase orders are disabled.
     * 
     * Step 1: display the form to specify the amount
     * 
     * @param id
     *            the portfolio entry id
     * @param workOrderId
     *            the work order id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION)
    public Result engageWorkOrderStep1(Long id, Long workOrderId) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the work order
        WorkOrder workOrder = WorkOrderDAO.getWorkOrderById(workOrderId);

        // initiate the form
        Form<EngageWorkOrderAmountSelectorFormData> selectorForm = engageWorkOrderAmountSelectorFormTemplate
                .fill(new EngageWorkOrderAmountSelectorFormData(workOrder));

        return ok(views.html.core.portfolioentryfinancial.portfolio_entry_engage_work_order_1.render(portfolioEntry, workOrder, selectorForm));
    }

    /**
     * Engage a work order (set its isEngaged flag to true), only if the
     * purchase orders are disabled.
     * 
     * Step 2: set the work order as engaged with the given amount and if it's
     * smaller than the original amount then propose to the user to create a new
     * "cost to complete" work order based on the difference
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION)
    public Result engageWorkOrderStep2() {

        // bind the form
        Form<EngageWorkOrderAmountSelectorFormData> boundForm = engageWorkOrderAmountSelectorFormTemplate.bindFromRequest();

        // get the portfolioEntry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the workOrder
        Long workOrderId = Long.valueOf(boundForm.data().get("workOrderId"));
        WorkOrder workOrder = WorkOrderDAO.getWorkOrderById(workOrderId);

        if (boundForm.hasErrors()) {
            return ok(views.html.core.portfolioentryfinancial.portfolio_entry_engage_work_order_1.render(portfolioEntry, workOrder, boundForm));
        }

        EngageWorkOrderAmountSelectorFormData engageWorkOrderAmountSelectorFormData = boundForm.get();

        double remainingAmount = workOrder.amount.doubleValue() - engageWorkOrderAmountSelectorFormData.amount.doubleValue();

        workOrder.amount = new BigDecimal(engageWorkOrderAmountSelectorFormData.amount);
        workOrder.amountReceived = BigDecimal.valueOf(0);
        workOrder.isEngaged = true;
        workOrder.save();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_financial.work_order.engage.successful"));

        if (remainingAmount > 0) {
            return redirect(
                    controllers.core.routes.PortfolioEntryFinancialController.workOrderReportBalance(portfolioEntry.id, workOrder.id, remainingAmount));
        } else {
            return redirect(controllers.core.routes.PortfolioEntryFinancialController.details(portfolioEntry.id));
        }

    }

    /**
     * Select a purchase order line item for a work order.
     * 
     * Step 1: display the form to select the purchase order
     * 
     * @param id
     *            the portfolio entry id
     * @param workOrderId
     *            the work order id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION)
    public Result selectWorkOrderLineItemStep1(Long id, Long workOrderId) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the work order
        WorkOrder workOrder = WorkOrderDAO.getWorkOrderById(workOrderId);

        // initiate the form
        Form<PurchaseOrderSelectorFormData> selectorForm = purchaseOrderSelectorFormTemplate.fill(new PurchaseOrderSelectorFormData(workOrder));

        return ok(views.html.core.portfolioentryfinancial.portfolio_entry_work_order_line_item_select_1.render(portfolioEntry, workOrder, selectorForm));

    }

    /**
     * Select a purchase order line item for a work order.
     * 
     * Step 2: perform the selection of the purchase order, and display a table
     * of line items that is "compatible" with the work order
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION)
    public Result selectWorkOrderLineItemStep2() {

        // bind the form
        Form<PurchaseOrderSelectorFormData> boundForm = purchaseOrderSelectorFormTemplate.bindFromRequest();

        // get the portfolioEntry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the workOrder
        Long workOrderId = Long.valueOf(boundForm.data().get("workOrderId"));
        WorkOrder workOrder = WorkOrderDAO.getWorkOrderById(workOrderId);

        if (boundForm.hasErrors()) {
            return ok(views.html.core.portfolioentryfinancial.portfolio_entry_work_order_line_item_select_1.render(portfolioEntry, workOrder, boundForm));
        }

        PurchaseOrderSelectorFormData purchaseOrderSelectorFormData = boundForm.get();

        // get the purchase order
        PurchaseOrder purchaseOrder = PurchaseOrderDAO.getPurchaseOrderActiveByRefId(purchaseOrderSelectorFormData.purchaseOrderRefId);

        // reject the form if the purchase order is not found
        if (purchaseOrder == null) {
            boundForm.reject("purchaseOrderRefId", Msg.get("core.portfolio_entry_financial.work_order.line_item_select.step1.purchaseorder.error.notfound"));
            return ok(views.html.core.portfolioentryfinancial.portfolio_entry_work_order_line_item_select_1.render(portfolioEntry, workOrder, boundForm));
        }

        /*
         * construct the items table
         */

        // get all active items (for the expenditure type and currency of the
        // WO)
        List<PurchaseOrderLineItem> purchaseOrderLineItems = PurchaseOrderDAO.getPurchaseOrderLineItemActiveAsListByPOAndCurrencyAndOpex(purchaseOrder.id,
                workOrder.currency.code, workOrder.isOpex);

        // construct the items
        List<PurchaseOrderLineItemListView> purchaseOrderLineItemListView = new ArrayList<PurchaseOrderLineItemListView>();
        for (PurchaseOrderLineItem lineItem : purchaseOrderLineItems) {

            // if WO shared: not yet allocated line OR already allocated line to
            // a shared WO but not fully consumed (=> the amount must be
            // requested)
            if (workOrder.shared) {
                // not yet allocated line
                if (lineItem.workOrders == null || lineItem.workOrders.isEmpty()) {
                    purchaseOrderLineItemListView.add(new PurchaseOrderLineItemListView(lineItem, id, workOrderId));
                } else { // already allocated line to a shared WO but not fully
                         // consumed
                    if (lineItem.workOrders.get(0).shared && lineItem.getRemainingAmount(workOrder).doubleValue() > 0.01) {
                        purchaseOrderLineItemListView.add(new PurchaseOrderLineItemListView(lineItem, id, workOrderId));
                    }
                }
            } else { // if WO not shared: not yet allocated line
                if (lineItem.workOrders == null || lineItem.workOrders.isEmpty()) {
                    purchaseOrderLineItemListView.add(new PurchaseOrderLineItemListView(lineItem, id, workOrderId));
                }
            }

        }

        // if the items list is empty then reject the form
        if (purchaseOrderLineItemListView.isEmpty()) {
            boundForm.reject("purchaseOrderRefId", Msg.get("core.portfolio_entry_financial.work_order.line_item_select.step1.purchaseorder.error.noitem"));
            return ok(views.html.core.portfolioentryfinancial.portfolio_entry_work_order_line_item_select_1.render(portfolioEntry, workOrder, boundForm));
        }

        // if there is only one item then go to step 3
        if (purchaseOrderLineItemListView.size() == 1) {
            return redirect(controllers.core.routes.PortfolioEntryFinancialController.selectWorkOrderLineItemStep3(id, workOrderId,
                    purchaseOrderLineItemListView.get(0).id));
        }

        // hide columns
        Set<String> hidePOColumns = new HashSet<String>();
        hidePOColumns.add("purchaseOrderRefId");
        hidePOColumns.add("isAssociated");
        hidePOColumns.add("shared");
        hidePOColumns.add("isCancelled");
        hidePOColumns.add("amount");
        hidePOColumns.add("amountReceived");
        hidePOColumns.add("amountOpen");

        // construct the table
        Table<PurchaseOrderLineItemListView> purchaseOrderLineItemsTable = PurchaseOrderLineItemListView.templateTable.fill(purchaseOrderLineItemListView,
                hidePOColumns);

        // set no line action
        purchaseOrderLineItemsTable.setLineAction(null);

        return ok(views.html.core.portfolioentryfinancial.portfolio_entry_work_order_line_item_select_2.render(portfolioEntry, workOrder, purchaseOrder,
                purchaseOrderLineItemsTable));
    }

    /**
     * Select a purchase order line item for a work order.
     * 
     * Step 3: perform the line item selection, and, only for a shared work
     * order, define the amount to engage (from the line item) for the work
     * order
     * 
     * @param id
     *            the portfolio entry id
     * @param workOrderId
     *            the work order id
     * @param lineItemId
     *            the line item id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION)
    public Result selectWorkOrderLineItemStep3(Long id, Long workOrderId, Long lineItemId) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the work order
        WorkOrder workOrder = WorkOrderDAO.getWorkOrderById(workOrderId);

        // get the purchase order line item
        PurchaseOrderLineItem lineItem = PurchaseOrderDAO.getPurchaseOrderLineItemById(lineItemId);

        if (workOrder.shared) {

            // initiate the amount selector form
            Form<PurchaseOrderLineItemAmountSelectorFormData> selectorForm = lineItemAmountSelectorFormTemplate
                    .fill(new PurchaseOrderLineItemAmountSelectorFormData(workOrder, lineItem));

            return ok(views.html.core.portfolioentryfinancial.portfolio_entry_work_order_line_item_select_3.render(portfolioEntry, workOrder, lineItem,
                    selectorForm));

        } else {
            return selectWorkOrderLineItemProcess(portfolioEntry, workOrder, lineItem, lineItem.amount);
        }
    }

    /**
     * Select a purchase order line item for a work order.
     * 
     * Step 4: only for a shared work order, process the amount to engage
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION)
    public Result selectWorkOrderLineItemStep4() {

        // bind the form
        Form<PurchaseOrderLineItemAmountSelectorFormData> boundForm = lineItemAmountSelectorFormTemplate.bindFromRequest();

        // get the portfolioEntry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the workOrder
        Long workOrderId = Long.valueOf(boundForm.data().get("workOrderId"));
        WorkOrder workOrder = WorkOrderDAO.getWorkOrderById(workOrderId);

        // get the line item
        Long lineItemId = Long.valueOf(boundForm.data().get("lineItemId"));
        PurchaseOrderLineItem lineItem = PurchaseOrderDAO.getPurchaseOrderLineItemById(lineItemId);

        if (boundForm.hasErrors()) {
            return ok(views.html.core.portfolioentryfinancial.portfolio_entry_work_order_line_item_select_3.render(portfolioEntry, workOrder, lineItem,
                    boundForm));
        }

        PurchaseOrderLineItemAmountSelectorFormData selectorFormData = boundForm.get();

        // check the amount (must be smaller or equal to the remaining)
        if (selectorFormData.amount.doubleValue() > lineItem.getRemainingAmount(workOrder).doubleValue() + 0.01) {
            boundForm.reject("amount", Msg.get("core.portfolio_entry_financial.work_order.line_item_select.step3.amount.error.toobig"));
            return ok(views.html.core.portfolioentryfinancial.portfolio_entry_work_order_line_item_select_3.render(portfolioEntry, workOrder, lineItem,
                    boundForm));
        }

        return selectWorkOrderLineItemProcess(portfolioEntry, workOrder, lineItem, BigDecimal.valueOf(selectorFormData.amount));

    }

    /**
     * Select a purchase order line item for a work order.
     * 
     * Process: save the association, and if given, store the amount
     * 
     * @param portfolioEntry
     *            the portfolio entry
     * @param workOrder
     *            the work order
     * @param lineItem
     *            the selected line item
     * @param engagedAmount
     *            the engaged amount
     */
    private static Result selectWorkOrderLineItemProcess(PortfolioEntry portfolioEntry, WorkOrder workOrder, PurchaseOrderLineItem lineItem,
            BigDecimal engagedAmount) {

        workOrder.purchaseOrderLineItem = lineItem;

        double remainingAmount = workOrder.amount.doubleValue() - engagedAmount.doubleValue();

        workOrder.amount = engagedAmount;
        workOrder.amountReceived = BigDecimal.valueOf(0);
        workOrder.save();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_financial.work_order.line_item_select.successful"));

        if (remainingAmount > 0) {
            return redirect(
                    controllers.core.routes.PortfolioEntryFinancialController.workOrderReportBalance(portfolioEntry.id, workOrder.id, remainingAmount));
        } else {
            return redirect(controllers.core.routes.PortfolioEntryFinancialController.details(portfolioEntry.id));
        }

    }

    /**
     * Confirmation page to report the balance of a work order to a new one.
     * 
     * @param id
     *            the portfolio entry id
     * @param workOrderId
     *            the work order id
     * @param amount
     *            the amount to report
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION)
    public Result workOrderReportBalance(Long id, Long workOrderId, Double amount) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the work order
        WorkOrder workOrder = WorkOrderDAO.getWorkOrderById(workOrderId);

        return ok(views.html.core.portfolioentryfinancial.portfolio_entry_work_order_report_balance.render(portfolioEntry, workOrder, amount));
    }

    /**
     * Report the balance of a work order to a new one.
     * 
     * @param id
     *            the portfolio entry id
     * @param workOrderId
     *            the work order id
     * @param amount
     *            the amount to report
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION)
    public Result workOrderReportBalanceSave(Long id, Long workOrderId, Double amount) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the work order
        WorkOrder workOrder = WorkOrderDAO.getWorkOrderById(workOrderId);

        // create a new work order
        WorkOrder newWorkOrder = new WorkOrder();

        newWorkOrder.portfolioEntry = portfolioEntry;
        newWorkOrder.creationDate = workOrder.creationDate;
        newWorkOrder.name = workOrder.name;
        newWorkOrder.description = workOrder.description;
        newWorkOrder.dueDate = workOrder.dueDate;
        newWorkOrder.currency = workOrder.currency;
        newWorkOrder.amount = new BigDecimal(amount);
        newWorkOrder.amountReceived = BigDecimal.ZERO;
        newWorkOrder.isOpex = workOrder.isOpex;
        newWorkOrder.shared = workOrder.shared;
        newWorkOrder.followPackageDates = workOrder.followPackageDates;
        newWorkOrder.portfolioEntryPlanningPackage = workOrder.portfolioEntryPlanningPackage;
        newWorkOrder.startDate = workOrder.startDate;

        newWorkOrder.save();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_financial.work_order.report_balance.successful"));

        return redirect(controllers.core.routes.PortfolioEntryFinancialController.details(portfolioEntry.id));
    }

    /**
     * Get the user session manager service.
     */
    private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return userSessionManagerPlugin;
    }

    /**
     * Get the account manager service.
     */
    private IAccountManagerPlugin getAccountManagerPlugin() {
        return accountManagerPlugin;
    }

    /**
     * Get the security service.
     */
    private ISecurityService getSecurityService() {
        return securityService;
    }

    /**
     * Get the i18n messages service.
     */
    private II18nMessagesPlugin getI18nMessagesPlugin() {
        return i18nMessagesPlugin;
    }

    /**
     * Get the Play configuration service.
     */
    private Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Get the preference manager service.
     */
    private IPreferenceManagerPlugin getPreferenceManagerPlugin() {
        return this.preferenceManagerPlugin;
    }

    /**
     * Get the budget tracking service.
     */
    private IBudgetTrackingService getBudgetTrackingService() {
        return this.budgetTrackingService;
    }
}
