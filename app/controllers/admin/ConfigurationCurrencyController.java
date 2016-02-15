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

import javax.inject.Inject;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import dao.finance.CurrencyDAO;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.Table;
import models.finance.Currency;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import services.tableprovider.ITableProvider;
import utils.form.CurrencyFormData;
import utils.table.CurrencyListView;

/**
 * Manage the currencies.
 * 
 * @author Johann Kohler
 * 
 */
@Restrict({ @Group(IMafConstants.ADMIN_CONFIGURATION_PERMISSION) })
public class ConfigurationCurrencyController extends Controller {

    private static Form<CurrencyFormData> currencyFormTemplate = Form.form(CurrencyFormData.class);

    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;

    @Inject
    private ITableProvider tableProvider;

    /**
     * Display the lists of data.
     */
    public Result list() {

        List<CurrencyListView> currenciesListView = new ArrayList<CurrencyListView>();
        for (Currency currency : CurrencyDAO.getCurrencyAsListByActive(null)) {
            currenciesListView.add(new CurrencyListView(currency));
        }

        Table<CurrencyListView> currenciesTable = this.getTableProvider().get().currency.templateTable.fill(currenciesListView);

        return ok(views.html.admin.config.datareference.currency.list.render(currenciesTable));
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
        return TODO;
    }

    /**
     * Delete a currency.
     * 
     * @param currencyId
     *            the currency id
     */
    public Result deleteCurrency(Long currencyId) {
        // TODO not possible when default
        // TODO not possible when a non-deleted data is associated to it
        return TODO;
    }

    /**
     * Set a currency as the default one.
     * 
     * @param currencyId
     *            the currency id
     */
    public Result setCurrencyAsDefault(Long currencyId) {
        // TODO also as active

        return TODO;
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

}
