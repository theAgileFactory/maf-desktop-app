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
package security;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import constants.IMafConstants;
import dao.delivery.ReleaseDAO;
import dao.finance.BudgetBucketDAO;
import dao.pmo.ActorDao;
import dao.pmo.OrgUnitDao;
import dao.pmo.PortfolioDao;
import dao.pmo.PortfolioEntryDao;
import dao.reporting.ReportingDao;
import dao.timesheet.TimesheetDao;
import framework.services.ServiceManager;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.session.IUserSessionManagerPlugin;
import models.delivery.Release;
import models.finance.BudgetBucket;
import models.pmo.Actor;
import models.pmo.OrgUnit;
import models.pmo.Portfolio;
import models.pmo.PortfolioEntry;
import models.reporting.Reporting;
import models.timesheet.TimesheetReport;
import play.Logger;
import play.cache.Cache;
import play.libs.F.Promise;
import play.mvc.Http;
import play.mvc.Http.Context;
import security.dynamic.ActorDynamicHelper;
import security.dynamic.BudgetBucketDynamicHelper;
import security.dynamic.OrgUnitDynamicHelper;
import security.dynamic.PortfolioDynamicHelper;
import security.dynamic.PortfolioEntryDynamicHelper;
import security.dynamic.ReleaseDynamicHelper;
import security.dynamic.ReportingDynamicHelper;
import security.dynamic.TimesheetReportDynamicHelper;

/**
 * The deadbolt dynamic resource handler.
 * 
 * @author Johann Kohler
 * 
 */
public class DefaultDynamicResourceHandler implements DynamicResourceHandler {

    private static final Map<String, DynamicResourceHandler> HANDLERS = new HashMap<String, DynamicResourceHandler>();

    private static final Integer CACHE_TTL = 300;

    public static final String PORTFOLIO_ENTRY_VIEW_DYNAMIC_PERMISSION = "PORTFOLIO_ENTRY_VIEW_DYNAMIC_PERMISSION";
    public static final String PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION = "PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION";
    public static final String PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION = "PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION";
    public static final String PORTFOLIO_ENTRY_DELETE_DYNAMIC_PERMISSION = "PORTFOLIO_ENTRY_DELETE_DYNAMIC_PERMISSION";
    public static final String PORTFOLIO_ENTRY_REVIEW_REQUEST_DYNAMIC_PERMISSION = "PORTFOLIO_ENTRY_REVIEW_REQUEST_DYNAMIC_PERMISSION";
    public static final String PORTFOLIO_ENTRY_FINANCIAL_VIEW_DYNAMIC_PERMISSION = "PORTFOLIO_ENTRY_FINANCIAL_VIEW_DYNAMIC_PERMISSION";
    public static final String PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION = "PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION";

    public static final String PORTFOLIO_VIEW_DYNAMIC_PERMISSION = "PORTFOLIO_VIEW_DYNAMIC_PERMISSION";
    public static final String PORTFOLIO_EDIT_DYNAMIC_PERMISSION = "PORTFOLIO_EDIT_DYNAMIC_PERMISSION";
    public static final String PORTFOLIO_VIEW_FINANCIAL_DYNAMIC_PERMISSION = "PORTFOLIO_VIEW_FINANCIAL_DYNAMIC_PERMISSION";

    public static final String BUDGET_BUCKET_VIEW_DYNAMIC_PERMISSION = "BUDGET_BUCKET_VIEW_DYNAMIC_PERMISSION";
    public static final String BUDGET_BUCKET_EDIT_DYNAMIC_PERMISSION = "BUDGET_BUCKET_EDIT_DYNAMIC_PERMISSION";

    public static final String REPORTING_VIEW_DYNAMIC_PERMISSION = "REPORTING_VIEW_DYNAMIC_PERMISSION";

    public static final String TIMESHEET_APPROVAL_DYNAMIC_PERMISSION = "TIMESHEET_APPROVAL_DYNAMIC_PERMISSION";

    public static final String ACTOR_VIEW_DYNAMIC_PERMISSION = "ACTOR_VIEW_DYNAMIC_PERMISSION";
    public static final String ACTOR_EDIT_DYNAMIC_PERMISSION = "ACTOR_EDIT_DYNAMIC_PERMISSION";
    public static final String ACTOR_DELETE_DYNAMIC_PERMISSION = "ACTOR_DELETE_DYNAMIC_PERMISSION";

    public static final String ORG_UNIT_VIEW_DYNAMIC_PERMISSION = "ORG_UNIT_VIEW_DYNAMIC_PERMISSION";

    public static final String RELEASE_VIEW_DYNAMIC_PERMISSION = "RELEASE_VIEW_DYNAMIC_PERMISSION";
    public static final String RELEASE_EDIT_DYNAMIC_PERMISSION = "RELEASE_EDIT_DYNAMIC_PERMISSION";

    /**
     * Create all handlers, one for each dynamic permission.
     */
    static {
        HANDLERS.put(DefaultDynamicResourceHandler.PORTFOLIO_ENTRY_VIEW_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(getId(context));
                    if (portfolioEntry == null) {
                        return true;
                    } else {
                        return PortfolioEntryDynamicHelper.isPortfolioEntryViewAllowed(portfolioEntry.id);
                    }
                });
            }
        });

        HANDLERS.put(DefaultDynamicResourceHandler.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(getId(context));
                    if (portfolioEntry == null) {
                        return true;
                    } else {
                        return PortfolioEntryDynamicHelper.isPortfolioEntryDetailsAllowed(portfolioEntry);
                    }
                });
            }
        });

        HANDLERS.put(DefaultDynamicResourceHandler.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(getId(context));
                    if (portfolioEntry == null) {
                        return true;
                    } else {
                        return PortfolioEntryDynamicHelper.isPortfolioEntryEditAllowed(portfolioEntry);
                    }
                });
            }
        });

        HANDLERS.put(DefaultDynamicResourceHandler.PORTFOLIO_ENTRY_DELETE_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(getId(context));
                    if (portfolioEntry == null) {
                        return true;
                    } else {
                        return PortfolioEntryDynamicHelper.isPortfolioEntryDeleteAllowed(portfolioEntry);
                    }
                });
            }
        });

        HANDLERS.put(DefaultDynamicResourceHandler.PORTFOLIO_ENTRY_FINANCIAL_VIEW_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(getId(context));
                    if (portfolioEntry == null) {
                        return true;
                    } else {
                        return PortfolioEntryDynamicHelper.isPortfolioEntryViewFinancialAllowed(portfolioEntry);
                    }
                });
            }
        });

        HANDLERS.put(DefaultDynamicResourceHandler.PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(getId(context));
                    if (portfolioEntry == null) {
                        return true;
                    } else {
                        return PortfolioEntryDynamicHelper.isPortfolioEntryEditFinancialAllowed(portfolioEntry);
                    }
                });
            }
        });

        HANDLERS.put(DefaultDynamicResourceHandler.PORTFOLIO_ENTRY_REVIEW_REQUEST_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(getId(context));
                    if (portfolioEntry == null) {
                        return true;
                    } else {
                        return PortfolioEntryDynamicHelper.isPortfolioEntryReviewRequestAllowed(portfolioEntry);
                    }
                });
            }
        });

        HANDLERS.put(DefaultDynamicResourceHandler.PORTFOLIO_VIEW_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    Portfolio portfolio = PortfolioDao.getPortfolioById(getId(context));
                    if (portfolio == null) {
                        return true;
                    } else {
                        return PortfolioDynamicHelper.isPortfolioViewAllowed(portfolio.id);
                    }
                });
            }
        });

        HANDLERS.put(DefaultDynamicResourceHandler.PORTFOLIO_EDIT_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    Portfolio portfolio = PortfolioDao.getPortfolioById(getId(context));
                    if (portfolio == null) {
                        return true;
                    } else {
                        return PortfolioDynamicHelper.isPortfolioEditAllowed(portfolio);
                    }
                });
            }
        });

        HANDLERS.put(DefaultDynamicResourceHandler.PORTFOLIO_VIEW_FINANCIAL_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    Portfolio portfolio = PortfolioDao.getPortfolioById(getId(context));
                    if (portfolio == null) {
                        return true;
                    } else {
                        return PortfolioDynamicHelper.isPortfolioViewFinancialAllowed(portfolio);
                    }
                });
            }
        });

        HANDLERS.put(DefaultDynamicResourceHandler.BUDGET_BUCKET_VIEW_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    BudgetBucket budgetBucket = BudgetBucketDAO.getBudgetBucketById(getId(context));
                    if (budgetBucket == null) {
                        return true;
                    } else {
                        return BudgetBucketDynamicHelper.isBudgetBucketViewAllowed(budgetBucket.id);
                    }
                });
            }
        });

        HANDLERS.put(DefaultDynamicResourceHandler.BUDGET_BUCKET_EDIT_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    BudgetBucket budgetBucket = BudgetBucketDAO.getBudgetBucketById(getId(context));
                    if (budgetBucket == null) {
                        return true;
                    } else {
                        return BudgetBucketDynamicHelper.isBudgetBucketEditAllowed(budgetBucket);
                    }
                });
            }
        });

        HANDLERS.put(DefaultDynamicResourceHandler.RELEASE_VIEW_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    Release release = ReleaseDAO.getReleaseById(getId(context));
                    if (release == null) {
                        return true;
                    } else {
                        return ReleaseDynamicHelper.isReleaseViewAllowed(release.id);
                    }
                });
            }
        });

        HANDLERS.put(DefaultDynamicResourceHandler.RELEASE_EDIT_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    Release release = ReleaseDAO.getReleaseById(getId(context));
                    if (release == null) {
                        return true;
                    } else {
                        return ReleaseDynamicHelper.isReleaseEditAllowed(release);
                    }
                });
            }
        });

        HANDLERS.put(DefaultDynamicResourceHandler.REPORTING_VIEW_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    Reporting report = ReportingDao.getReportingById(getId(context));
                    if (report == null) {
                        return true;
                    } else {
                        return ReportingDynamicHelper.isReportViewAllowed(report.id);
                    }
                });
            }
        });

        HANDLERS.put(DefaultDynamicResourceHandler.TIMESHEET_APPROVAL_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    TimesheetReport report = TimesheetDao.getTimesheetReportById(getId(context));
    
                    if (report == null) {
                        return true;
                    } else {
                        return TimesheetReportDynamicHelper.isTimesheetReportApprovalAllowed(report.id);
                    }
                });
            }
        });

        HANDLERS.put(DefaultDynamicResourceHandler.ACTOR_VIEW_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    Actor actor = ActorDao.getActorById(getId(context));
    
                    if (actor == null) {
                        return true;
                    } else {
                        return ActorDynamicHelper.isActorViewAllowed(actor.id);
                    }
                });
            }
        });

        HANDLERS.put(DefaultDynamicResourceHandler.ACTOR_EDIT_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    Actor actor = ActorDao.getActorById(getId(context));
                    if (actor == null) {
                        return true;
                    } else {
                        return ActorDynamicHelper.isActorEditAllowed(actor);
                    }
                });
            }
        });

        HANDLERS.put(DefaultDynamicResourceHandler.ACTOR_DELETE_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    Actor actor = ActorDao.getActorById(getId(context));
                    if (actor == null) {
                        return true;
                    } else {
                        return ActorDynamicHelper.isActorDeleteAllowed(actor);
                    }
                });
            }
        });

        HANDLERS.put(DefaultDynamicResourceHandler.ORG_UNIT_VIEW_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    OrgUnit orgUnit = OrgUnitDao.getOrgUnitById(getId(context));
    
                    if (orgUnit == null) {
                        return true;
                    } else {
                        return OrgUnitDynamicHelper.isOrgUnitViewAllowed(orgUnit.id);
                    }
                });
            }
        });

    }

    @Override
    public Promise<Boolean> checkPermission(String permissionValue, DeadboltHandler deadboltHandler, Context ctx) {
        boolean permissionOk = false;

        try {
            IUserSessionManagerPlugin userSessionManagerPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
            IAccountManagerPlugin accountManagerPlugin = ServiceManager.getService(IAccountManagerPlugin.NAME, IAccountManagerPlugin.class);
            IUserAccount userAccount = accountManagerPlugin.getUserAccountFromUid(userSessionManagerPlugin.getUserSessionId(ctx));

            if (userAccount != null) {
                List<? extends Permission> permissions = userAccount.getPermissions();
                for (Iterator<? extends Permission> iterator = permissions.iterator(); !permissionOk && iterator.hasNext();) {
                    Permission permission = iterator.next();
                    permissionOk = permission.getValue().contains(permissionValue);
                }
            }
        } catch (Exception e) {
            Logger.error("impossible to get the user", e);
        }

        final boolean permissionOkFinal = permissionOk;
        return Promise.promise(() -> permissionOkFinal);
    }

    @Override
    public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Context context) {

        String cacheKey = getCacheKey(name, getId(context));
        Boolean isAllowed = (Boolean) Cache.get(cacheKey);
        if (isAllowed != null) {
            Logger.debug("dynamic permission " + cacheKey + " read from cache, and result is: " + isAllowed);
            final boolean isAllowedFinal = isAllowed;
            return Promise.promise(() -> isAllowedFinal);
        }

        DynamicResourceHandler handler = HANDLERS.get(name);
        Promise<Boolean> result = Promise.promise(() -> false);
        if (handler == null) {
            Logger.error("No handler available for " + name);
        } else {
            Logger.debug("Dynamic permission: " + name);
            result = handler.isAllowed(name, meta, deadboltHandler, context);
        }

        // set the result
        Cache.set(cacheKey, result.get(5000l), CACHE_TTL);

        return result;
    }
    
    /**
     * Define if a specific object is allowed for a dynamic permission.
     * 
     * @param name
     *            the dynamic permission name
     * @param meta
     *            the meta, can be empty
     * @param id
     *            the object id
     */
    public static boolean isStaticAllowedWithObject(String name, String meta, Long id) {

        String cacheKey = getCacheKey(name, id);
        Boolean isAllowed = (Boolean) Cache.get(cacheKey);
        if (isAllowed != null) {
            Logger.debug("dynamic permission " + cacheKey + " read from cache, and result is: " + isAllowed);
            return isAllowed;
        }

        boolean result = false;

        if (name.equals(PORTFOLIO_ENTRY_VIEW_DYNAMIC_PERMISSION)) {
            result = PortfolioEntryDynamicHelper.isPortfolioEntryViewAllowed(id);

        } else if (name.equals(PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION)) {
            PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);
            result = PortfolioEntryDynamicHelper.isPortfolioEntryDetailsAllowed(portfolioEntry);

        } else if (name.equals(PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)) {
            PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);
            result = PortfolioEntryDynamicHelper.isPortfolioEntryEditAllowed(portfolioEntry);

        } else if (name.equals(PORTFOLIO_ENTRY_DELETE_DYNAMIC_PERMISSION)) {
            PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);
            result = PortfolioEntryDynamicHelper.isPortfolioEntryDeleteAllowed(portfolioEntry);

        } else if (name.equals(PORTFOLIO_ENTRY_FINANCIAL_VIEW_DYNAMIC_PERMISSION)) {
            PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);
            result = PortfolioEntryDynamicHelper.isPortfolioEntryViewFinancialAllowed(portfolioEntry);

        } else if (name.equals(PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION)) {
            PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);
            result = PortfolioEntryDynamicHelper.isPortfolioEntryEditFinancialAllowed(portfolioEntry);

        } else if (name.equals(PORTFOLIO_ENTRY_REVIEW_REQUEST_DYNAMIC_PERMISSION)) {
            PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);
            result = PortfolioEntryDynamicHelper.isPortfolioEntryReviewRequestAllowed(portfolioEntry);

        } else if (name.equals(PORTFOLIO_VIEW_DYNAMIC_PERMISSION)) {
            result = PortfolioDynamicHelper.isPortfolioViewAllowed(id);

        } else if (name.equals(PORTFOLIO_EDIT_DYNAMIC_PERMISSION)) {
            Portfolio portfolio = PortfolioDao.getPortfolioById(id);
            result = PortfolioDynamicHelper.isPortfolioEditAllowed(portfolio);

        } else if (name.equals(PORTFOLIO_VIEW_FINANCIAL_DYNAMIC_PERMISSION)) {
            Portfolio portfolio = PortfolioDao.getPortfolioById(id);
            result = PortfolioDynamicHelper.isPortfolioViewFinancialAllowed(portfolio);

        } else if (name.equals(BUDGET_BUCKET_VIEW_DYNAMIC_PERMISSION)) {
            result = BudgetBucketDynamicHelper.isBudgetBucketViewAllowed(id);

        } else if (name.equals(BUDGET_BUCKET_EDIT_DYNAMIC_PERMISSION)) {
            BudgetBucket budgetBucket = BudgetBucketDAO.getBudgetBucketById(id);
            result = BudgetBucketDynamicHelper.isBudgetBucketEditAllowed(budgetBucket);

        } else if (name.equals(REPORTING_VIEW_DYNAMIC_PERMISSION)) {
            result = ReportingDynamicHelper.isReportViewAllowed(id);

        } else if (name.equals(TIMESHEET_APPROVAL_DYNAMIC_PERMISSION)) {
            result = TimesheetReportDynamicHelper.isTimesheetReportApprovalAllowed(id);

        } else if (name.equals(ACTOR_VIEW_DYNAMIC_PERMISSION)) {
            result = ActorDynamicHelper.isActorViewAllowed(id);

        } else if (name.equals(ACTOR_EDIT_DYNAMIC_PERMISSION)) {
            Actor actor = ActorDao.getActorById(id);
            result = ActorDynamicHelper.isActorEditAllowed(actor);

        } else if (name.equals(ACTOR_DELETE_DYNAMIC_PERMISSION)) {
            Actor actor = ActorDao.getActorById(id);
            result = ActorDynamicHelper.isActorDeleteAllowed(actor);

        } else if (name.equals(ORG_UNIT_VIEW_DYNAMIC_PERMISSION)) {
            result = OrgUnitDynamicHelper.isOrgUnitViewAllowed(id);

        } else if (name.equals(RELEASE_VIEW_DYNAMIC_PERMISSION)) {
            result = ReleaseDynamicHelper.isReleaseViewAllowed(id);

        } else if (name.equals(RELEASE_EDIT_DYNAMIC_PERMISSION)) {
            Release release = ReleaseDAO.getReleaseById(id);
            result = ReleaseDynamicHelper.isReleaseEditAllowed(release);

        }

        // set the result
        Cache.set(cacheKey, result, CACHE_TTL);

        return result;

    }

    /**
     * Get the cache key depending of the permission name and the object id.
     * 
     * @param name
     *            the permission name
     * @param id
     *            the object id
     */
    private static String getCacheKey(String name, Long id) {
        IUserSessionManagerPlugin userSessionManagerPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
        String cacheKey = IMafConstants.DYNAMIC_PERMISSION_CACHE_PREFIX + userSessionManagerPlugin.getUserSessionId(Http.Context.current()) + "." + name + "."
                + String.valueOf(id);
        return cacheKey;

    }

    /**
     * Get the current id from the context.
     * 
     * @param context
     *            the context
     */
    public static Long getId(Http.Context context) {
        Long id = null;

        if (context.request().getQueryString("id") != null) {
            // get the id as a query parameter

            id = Long.valueOf(context.request().getQueryString("id"));
        } else if (context.request().headers().get("id") != null) {
            // get the id as a header parameter

            id = Long.valueOf(context.request().headers().get("id")[0]);
        } else if (context.request().body().asFormUrlEncoded() != null && context.request().body().asFormUrlEncoded().get("id") != null) {
            // get the id as a form content parameter

            id = Long.valueOf(context.request().body().asFormUrlEncoded().get("id")[0]);
        } else if (context.request().body().asMultipartFormData() != null && context.request().body().asMultipartFormData().asFormUrlEncoded() != null
                && context.request().body().asMultipartFormData().asFormUrlEncoded().get("id") != null) {
            // get the id as a multipart form content parameter

            id = Long.valueOf(context.request().body().asMultipartFormData().asFormUrlEncoded().get("id")[0]);
        } else {
            // else try to get the id as a route parameter (only at the end of
            // the path), example: https://localhost/portfolio-entry/view/10

            try {
                id = Long.parseLong(context.request().path().substring(context.request().path().lastIndexOf('/') + 1));
            } catch (Exception e) {
                Logger.debug("impossible to find the id as a route parameter");
            }
        }

        return id;

    }
}