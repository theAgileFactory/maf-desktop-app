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
import java.util.List;

import javax.inject.Inject;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import controllers.ControllersUtils;
import dao.finance.PurchaseOrderDAO;
import dao.pmo.ActorDao;
import dao.pmo.OrgUnitDao;
import framework.security.ISecurityService;
import framework.services.account.AccountManagementException;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.utils.DefaultSelectableValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.Msg;
import framework.utils.Table;
import models.finance.BudgetBucket;
import models.finance.PurchaseOrder;
import models.pmo.Actor;
import models.pmo.OrgUnit;
import models.pmo.Portfolio;
import models.pmo.PortfolioEntry;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import security.dynamic.BudgetBucketDynamicHelper;
import security.dynamic.PortfolioDynamicHelper;
import security.dynamic.PortfolioEntryDynamicHelper;
import services.tableprovider.ITableProvider;
import utils.table.ActorListView;
import utils.table.BudgetBucketListView;
import utils.table.OrgUnitListView;
import utils.table.PortfolioEntryListView;
import utils.table.PortfolioListView;
import utils.table.PurchaseOrderListView;

/**
 * The controller which allows to search entities.
 * 
 * @author Johann Kohler
 */
@Restrict({ @Group(IMafConstants.SEARCH_PERMISSION) })
public class SearchController extends Controller {

    @Inject
    private ISecurityService securityService;
    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;
    @Inject
    private Configuration configuration;
    @Inject
    private IAccountManagerPlugin accountManagerPlugin;
    @Inject
    private IUserSessionManagerPlugin userSessionManagerPlugin;
    @Inject
    private IPreferenceManagerPlugin preferenceManagerPlugin;
    @Inject
    private ITableProvider tableProvider;

    private static Form<SearchFormData> formTemplate = Form.form(SearchFormData.class);
    private static Logger.ALogger log = Logger.of(SearchController.class);

    /**
     * The list of searchable entity types, its depends of the user permissions.
     * 
     * @param accountManagerPlugin
     *            the account manager service
     * @param userSessionManagerPlugin
     *            the user session manager service
     * @param preferenceManagerPlugin
     *            the preference manager service
     * @param securityService
     *            the security service
     */
    public static DefaultSelectableValueHolderCollection<ObjectTypes> getObjectTypes(IAccountManagerPlugin accountManagerPlugin,
            IUserSessionManagerPlugin userSessionManagerPlugin, IPreferenceManagerPlugin preferenceManagerPlugin, ISecurityService securityService) {

        IUserAccount userAccount = null;
        try {
            userAccount = accountManagerPlugin.getUserAccountFromUid(userSessionManagerPlugin.getUserSessionId(ctx()));
        } catch (Exception e) {
            Logger.error("Impossible to get the current in user", e);
        }

        DefaultSelectableValueHolderCollection<ObjectTypes> objectTypes = new DefaultSelectableValueHolderCollection<ObjectTypes>();

        objectTypes.add(new DefaultSelectableValueHolder<ObjectTypes>(ObjectTypes.PORTFOLIO_ENTRY, Msg.get("core.search.type.portfolio_entry")));

        objectTypes.add(new DefaultSelectableValueHolder<ObjectTypes>(ObjectTypes.ACTOR, Msg.get("core.search.type.actor")));

        objectTypes.add(new DefaultSelectableValueHolder<ObjectTypes>(ObjectTypes.PORTFOLIO, Msg.get("core.search.type.portfolio")));

        objectTypes.add(new DefaultSelectableValueHolder<ObjectTypes>(ObjectTypes.ORGUNIT, Msg.get("core.search.type.org_unit")));

        if (PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder(preferenceManagerPlugin)
                && securityService.restrict(IMafConstants.PURCHASE_ORDER_VIEW_ALL_PERMISSION, userAccount)) {
            objectTypes.add(new DefaultSelectableValueHolder<ObjectTypes>(ObjectTypes.PURCHASE_ORDER, Msg.get("core.search.type.purchase_order")));
        }

        if (securityService.restrict(IMafConstants.BUDGET_BUCKET_VIEW_ALL_PERMISSION, userAccount)) {
            objectTypes.add(new DefaultSelectableValueHolder<ObjectTypes>(ObjectTypes.BUDGET_BUCKET, Msg.get("core.search.type.budget_bucket")));
        }

        return objectTypes;

    }

    /**
     * Display the search form.
     */
    public Result index() {
        return ok(views.html.core.search.index.render(formTemplate, getObjectTypes(this.getAccountManagerPlugin(), this.getUserSessionManagerPlugin(),
                this.getPreferenceManagerPlugin(), this.getSecurityService()), null));

    }

    /**
     * Perform the search.
     */
    public Result search() {

        // bind the form
        Form<SearchFormData> boundForm = formTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            return ok(views.html.core.search.index.render(boundForm, getObjectTypes(this.getAccountManagerPlugin(), this.getUserSessionManagerPlugin(),
                    this.getPreferenceManagerPlugin(), this.getSecurityService()), null));
        }

        SearchFormData searchFormData = boundForm.get();

        // clean the key words
        String keywords = searchFormData.keywords.replaceAll("\\*", "%").trim();

        switch (searchFormData.objectType) {
        case PORTFOLIO_ENTRY:

            Logger.debug("PORTFOLIO_ENTRY");

            // search the portfolioEntries
            Expression expression = Expr.or(Expr.or(Expr.ilike("name", keywords + "%"), Expr.ilike("governanceId", keywords + "%")),
                    Expr.ilike("refId", keywords + "%"));
            List<PortfolioEntry> portfolioEntries;
            try {
                portfolioEntries = PortfolioEntryDynamicHelper.getPortfolioEntriesViewAllowedAsQuery(expression, getSecurityService()).findList();
            } catch (AccountManagementException e) {
                return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
            }

            List<PortfolioEntryListView> portfolioEntryListView = new ArrayList<PortfolioEntryListView>();
            for (PortfolioEntry portfolioEntry : portfolioEntries) {
                portfolioEntryListView.add(new PortfolioEntryListView(portfolioEntry));
            }

            if (portfolioEntryListView.size() > 0) {

                if (portfolioEntryListView.size() == 1) {
                    return redirect(controllers.core.routes.PortfolioEntryController.overview(portfolioEntryListView.get(0).id));
                }

                Table<PortfolioEntryListView> filledTable = this.getTableProvider().get().portfolioEntry.templateTable.fill(portfolioEntryListView,
                        PortfolioEntryListView.getHideNonDefaultColumns(true, true));
                return ok(views.html.core.search.portfolio_entry_table.render(filledTable));
            }

            break;

        case PORTFOLIO:

            Logger.debug("PORTFOLIO");

            // search the portfolios
            List<Portfolio> portfolios = null;
            try {
                portfolios = PortfolioDynamicHelper.getPortfoliosViewAllowedAsQuery(
                        Expr.or(Expr.or(Expr.ilike("name", keywords + "%"), Expr.ilike("refId", keywords + "%")), Expr.ilike("refId", keywords + "%")), null,
                        getSecurityService()).findList();
            } catch (AccountManagementException e) {
                return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
            }

            if (portfolios != null && portfolios.size() > 0) {
                if (portfolios.size() == 1) {
                    return redirect(controllers.core.routes.PortfolioController.overview(portfolios.get(0).id));
                }
                List<PortfolioListView> portfolioListView = new ArrayList<PortfolioListView>();
                for (Portfolio portfolio : portfolios) {
                    portfolioListView.add(new PortfolioListView(portfolio));
                }
                Table<PortfolioListView> filledTable = PortfolioListView.templateTable.fill(portfolioListView, PortfolioListView.hideStakeholderTypeColumn);
                return ok(views.html.core.search.portfolio_table.render(filledTable));
            }

            break;

        case ACTOR:

            Logger.debug("ACTOR");

            List<Actor> actors = ActorDao.getActorAsListByKeywords(keywords);
            if (actors.size() > 0) {
                if (actors.size() == 1) {
                    return redirect(controllers.core.routes.ActorController.view(actors.get(0).id));
                }
                List<ActorListView> actorListView = new ArrayList<ActorListView>();
                for (Actor actor : actors) {
                    actorListView.add(new ActorListView(actor));
                }
                Table<ActorListView> filledTable = ActorListView.templateTable.fill(actorListView);
                return ok(views.html.core.search.actor_table.render(filledTable));
            }

            break;

        case ORGUNIT:

            Logger.debug("ORGUNIT");

            List<OrgUnit> orgUnits = OrgUnitDao.getOrgUnitAsListByKeywordsAndFilter(keywords, false, false, false);
            if (orgUnits.size() > 0) {
                if (orgUnits.size() == 1) {
                    return redirect(controllers.core.routes.OrgUnitController.view(orgUnits.get(0).id, 0));
                }
                List<OrgUnitListView> orgUnitListView = new ArrayList<OrgUnitListView>();
                for (OrgUnit orgUnit : orgUnits) {
                    orgUnitListView.add(new OrgUnitListView(orgUnit));
                }
                Table<OrgUnitListView> filledTable = OrgUnitListView.templateTable.fill(orgUnitListView);
                return ok(views.html.core.search.org_unit_table.render(filledTable));
            }

            break;

        case PURCHASE_ORDER:

            Logger.debug("PURCHASE_ORDER");

            List<PurchaseOrder> purchaseOrders = PurchaseOrderDAO.getPurchaseOrderAsListByRefIdLike(keywords);
            if (purchaseOrders.size() > 0) {
                if (purchaseOrders.size() == 1) {
                    return redirect(controllers.core.routes.PurchaseOrderController.view(purchaseOrders.get(0).id));
                }
                List<PurchaseOrderListView> purchaseOrderListView = new ArrayList<PurchaseOrderListView>();
                for (PurchaseOrder purchaseOrder : purchaseOrders) {
                    purchaseOrderListView.add(new PurchaseOrderListView(purchaseOrder));
                }
                Table<PurchaseOrderListView> filledTable = PurchaseOrderListView.templateTable.fill(purchaseOrderListView);
                return ok(views.html.core.search.purchase_order_table.render(filledTable));
            }

            break;

        case BUDGET_BUCKET:

            Logger.debug("BUDGET_BUCKET");

            // search the budget buckets
            List<BudgetBucket> budgetBuckets;
            try {
                budgetBuckets = BudgetBucketDynamicHelper.getBudgetBucketsViewAllowedAsQuery(
                        Expr.or(Expr.ilike("name", keywords + "%"), Expr.ilike("refId", keywords + "%")), null, getSecurityService()).findList();
            } catch (AccountManagementException e) {
                return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
            }

            List<BudgetBucketListView> budgetBucketsListView = new ArrayList<BudgetBucketListView>();
            for (BudgetBucket budgetBucket : budgetBuckets) {
                budgetBucketsListView.add(new BudgetBucketListView(budgetBucket));
            }

            if (budgetBucketsListView.size() > 0) {

                if (budgetBucketsListView.size() == 1) {
                    return redirect(controllers.core.routes.BudgetBucketController.view(budgetBucketsListView.get(0).id, 0, 0));
                }

                Table<BudgetBucketListView> filledTable = BudgetBucketListView.templateTable.fill(budgetBucketsListView);
                return ok(views.html.core.search.budget_bucket_table.render(filledTable));
            }

            break;
        }

        return ok(views.html.core.search.index.render(boundForm, getObjectTypes(this.getAccountManagerPlugin(), this.getUserSessionManagerPlugin(),
                this.getPreferenceManagerPlugin(), this.getSecurityService()), "core.search.submit.noresult"));
    }

    /**
     * The search form data is used to display the fields of the search form.
     * 
     * @author Johann Kohler
     */
    public static class SearchFormData {

        /**
         * Default constructor.
         */
        public SearchFormData() {

        }

        @Required
        public ObjectTypes objectType;

        @Required
        public String keywords;

    }

    /**
     * List of all possible searchable object (entity) types.
     */
    public static enum ObjectTypes {
        PORTFOLIO_ENTRY, ACTOR, PORTFOLIO, ORGUNIT, PURCHASE_ORDER, BUDGET_BUCKET;
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
     * Get the account manager service.
     */
    private IAccountManagerPlugin getAccountManagerPlugin() {
        return this.accountManagerPlugin;
    }

    /**
     * Get the user session manager service.
     */
    private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return this.userSessionManagerPlugin;
    }

    /**
     * Get the preference manager service.
     */
    private IPreferenceManagerPlugin getPreferenceManagerPlugin() {
        return this.preferenceManagerPlugin;
    }

    /**
     * Get the table provider.
     */
    private ITableProvider getTableProvider() {
        return this.tableProvider;
    }
}
