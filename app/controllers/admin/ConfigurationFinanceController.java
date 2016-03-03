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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.avaje.ebean.Ebean;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import controllers.ControllersUtils;
import dao.finance.BudgetBucketDAO;
import dao.finance.CurrencyDAO;
import dao.finance.PortfolioEntryBudgetDAO;
import dao.finance.PortfolioEntryResourcePlanDAO;
import dao.finance.PurchaseOrderDAO;
import dao.finance.WorkOrderDAO;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import models.finance.Currency;
import models.finance.PortfolioEntryBudgetLineType;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import services.tableprovider.ITableProvider;
import utils.form.CurrencyFormData;
import utils.form.PortfolioEntryBudgetLineTypeFormData;
import utils.table.CurrencyListView;
import utils.table.PortfolioEntryBudgetLineTypeListView;

/**
 * Manage the currencies.
 * 
 * @author Johann Kohler
 * 
 */
@Restrict({ @Group(IMafConstants.ADMIN_CONFIGURATION_PERMISSION) })
public class ConfigurationFinanceController extends Controller {

    private static Logger.ALogger log = Logger.of(ConfigurationFinanceController.class);

    private static Form<CurrencyFormData> currencyFormTemplate = Form.form(CurrencyFormData.class);
    private static Form<PortfolioEntryBudgetLineTypeFormData> peBudgetLineTypeFormTemplate = Form.form(PortfolioEntryBudgetLineTypeFormData.class);

    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;

    @Inject
    private ITableProvider tableProvider;

    @Inject
    private Configuration configuration;

    /**
     * Display the lists of data.
     */
    public Result list() {

        // Currencies
        List<CurrencyListView> currenciesListView = new ArrayList<CurrencyListView>();
        for (Currency currency : CurrencyDAO.getCurrencyAsListByActive(null)) {
            currenciesListView.add(new CurrencyListView(currency));
        }

        Table<CurrencyListView> currenciesTable = this.getTableProvider().get().currency.templateTable.fill(currenciesListView);

        // Portfolio Entry Budget Line Type
        List<PortfolioEntryBudgetLineTypeListView> peBudgetLineTypeListView = new ArrayList<PortfolioEntryBudgetLineTypeListView>();
        for (PortfolioEntryBudgetLineType peBudgetLineType : PortfolioEntryBudgetDAO.getPEBudgetLineTypeAsList()) {
            peBudgetLineTypeListView.add(new PortfolioEntryBudgetLineTypeListView(peBudgetLineType));
        }

        Table<PortfolioEntryBudgetLineTypeListView> peBudgetLineTypeTable = this.getTableProvider().get().portfolioEntryBudgetLineType.templateTable
                .fill(peBudgetLineTypeListView);

        return ok(views.html.admin.config.datareference.currency.list.render(currenciesTable, peBudgetLineTypeTable));
    }

    /**
     * Edit or create a currency.
     * 
     * @param currencyId
     *            the currency id (set 0 for create case)
     */
    public Result manageCurrency(Long currencyId) {

        boolean isDefault = false;

        // initiate the form with the template
        Form<CurrencyFormData> currencyForm = currencyFormTemplate;

        // edit case: inject values
        if (!currencyId.equals(Long.valueOf(0))) {

            Currency currency = CurrencyDAO.getCurrencyById(currencyId);

            isDefault = currency.isDefault;

            currencyForm = currencyFormTemplate.fill(new CurrencyFormData(currency));

        }

        return ok(views.html.admin.config.datareference.currency.currency_manage.render(currencyForm, isDefault));

    }

    /**
     * Process the edit/create form of a currency.
     */
    public Result processManageCurrency() {

        // bind the form
        Form<CurrencyFormData> boundForm = currencyFormTemplate.bindFromRequest();

        // get the isDefault flag
        Boolean isDefault = Boolean.parseBoolean(boundForm.data().get("isDefault"));

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.currency.currency_manage.render(boundForm, isDefault));
        }

        CurrencyFormData currencyFormData = boundForm.get();

        Currency currency = null;

        if (currencyFormData.id == null) { // create case

            currency = new Currency();
            currency.isDefault = false;
            currencyFormData.fill(currency);
            currency.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.currency.add.successful"));

        } else { // edit case

            currency = CurrencyDAO.getCurrencyById(currencyFormData.id);
            currencyFormData.fill(currency);
            currency.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.currency.edit.successful"));
        }

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationFinanceController.list());
    }

    /**
     * Delete a currency.
     * 
     * @param currencyId
     *            the currency id
     */
    public Result deleteCurrency(Long currencyId) {

        Currency currency = CurrencyDAO.getCurrencyById(currencyId);

        // not possible to delete the default currency
        if (currency.isDefault) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        // not possible when a non-deleted data is associated to it
        if (PurchaseOrderDAO.hasPurchaseOrderLineItemByCurrency(currency.code) || WorkOrderDAO.hasWorkOrderByCurrency(currency.code)
                || PurchaseOrderDAO.hasGoodsReceiptByCurrency(currency.code) || PortfolioEntryResourcePlanDAO.hasAllocatedActorByCurrency(currency.code)
                || PortfolioEntryResourcePlanDAO.hasAllocatedCompetencyByCurrency(currency.code)
                || PortfolioEntryResourcePlanDAO.hasAllocatedOrgUnitByCurrency(currency.code) || BudgetBucketDAO.hasBudgetBucketLineByCurrency(currency.code)
                || PortfolioEntryBudgetDAO.hasPEBudgetLineByCurrency(currency.code)) {

            Utilities.sendErrorFlashMessage(Msg.get("admin.configuration.reference_data.currency.delete.error.used"));
            return redirect(controllers.admin.routes.ConfigurationFinanceController.list());

        }

        currency.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.currency.delete.successful"));

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationFinanceController.list());
    }

    /**
     * Set a currency as the default one.
     * 
     * @param currencyId
     *            the currency id
     */
    public Result setCurrencyAsDefault(Long currencyId) {

        Ebean.beginTransaction();
        try {

            Currency defaultCurrency = CurrencyDAO.getCurrencyDefault();
            Currency newDefaultCurrency = CurrencyDAO.getCurrencyById(currencyId);

            for (Currency currency : CurrencyDAO.getCurrencyAsListByActive(null)) {
                if (!currency.id.equals(newDefaultCurrency.id)) {
                    currency.conversionRate = currency.conversionRate.divide(newDefaultCurrency.conversionRate, 8, RoundingMode.HALF_UP);
                    currency.update();
                }
            }

            // TODO change all existing rate ????
            // PurchaseOrderLineItem
            // WorkOrder
            // GoodsReceipt
            // AllocatedActor
            // AllocatedCompetency
            // AllocatedOrgUnit
            // BudgetBucketLine
            // PEBudgetLine

            defaultCurrency.isDefault = false;
            defaultCurrency.update();

            newDefaultCurrency.isDefault = true;
            newDefaultCurrency.isActive = true;
            newDefaultCurrency.conversionRate = BigDecimal.ONE;
            newDefaultCurrency.update();

            Ebean.commitTransaction();

        } catch (Exception e) {
            Ebean.rollbackTransaction();
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.currency.set_as_default.successful"));

        return redirect(controllers.admin.routes.ConfigurationFinanceController.list());
    }

    /**
     * Edit or create a Portfolio Entry Budget Line Type.
     * 
     * @param id
     *            the pe budget line type id (set 0 for create case)
     */
    public Result managePEBudgetLineType(Long id) {

        boolean isDefault = false;

        // initiate the form with the template
        Form<PortfolioEntryBudgetLineTypeFormData> peBudgetListTypeForm = peBudgetLineTypeFormTemplate;

        // edit case: inject values
        if (!id.equals(Long.valueOf(0))) {

            PortfolioEntryBudgetLineType peBudgetLineType = PortfolioEntryBudgetDAO.getPEBudgetLineTypeById(id);

            peBudgetListTypeForm = peBudgetLineTypeFormTemplate.fill(new PortfolioEntryBudgetLineTypeFormData(peBudgetLineType));

        }

        return ok(views.html.admin.config.datareference.currency.pe_budget_line_type_manage.render(peBudgetListTypeForm));
    }

    /**
     * Process the edit/create form of a Portfolio Entry Budget Line Type.
     */
    public Result processManagePEBudgetLineType() {

        // bind the form
        Form<PortfolioEntryBudgetLineTypeFormData> boundForm = peBudgetLineTypeFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.currency.pe_budget_line_type_manage.render(boundForm));
        }

        PortfolioEntryBudgetLineTypeFormData peBudgetLineTypeFormData = boundForm.get();

        PortfolioEntryBudgetLineType peBudgetLineType = null;

        if (peBudgetLineTypeFormData.id == null) { // create case

            peBudgetLineType = new PortfolioEntryBudgetLineType();
            peBudgetLineTypeFormData.fill(peBudgetLineType);
            peBudgetLineType.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.finance.pe_budget_line_type.add.successful"));

        } else { // edit case

            peBudgetLineType = PortfolioEntryBudgetDAO.getPEBudgetLineTypeById(peBudgetLineTypeFormData.id);
            peBudgetLineTypeFormData.fill(peBudgetLineType);
            peBudgetLineType.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.finance.pe_budget_line_type.edit.successful"));
        }

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationFinanceController.list());
    }

    /**
     * Delete a currency.
     * 
     * @param peBudgetLineTypeId
     *            the currency id
     */
    public Result deletePEBudgetLineType(Long peBudgetLineTypeId) {

        PortfolioEntryBudgetLineType peBudgetLineType = PortfolioEntryBudgetDAO.getPEBudgetLineTypeById(peBudgetLineTypeId);

        peBudgetLineType.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.finance.pe_budget_line_type.delete.successful"));

        this.getTableProvider().flushFilterConfig();

        return redirect(controllers.admin.routes.ConfigurationFinanceController.list());
    }

    /**
     * Get the i18n messages service.
     */
    private II18nMessagesPlugin getI18nMessagesPlugin() {
        return i18nMessagesPlugin;
    }

    /**
     * Get the table provider.
     */
    private ITableProvider getTableProvider() {
        return this.tableProvider;
    }

    /**
     * Get the Play configuration service.
     */
    private Configuration getConfiguration() {
        return this.configuration;
    }

}
