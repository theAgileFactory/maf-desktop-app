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
import java.util.Optional;

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
import framework.security.CommonDeadboltHandler;
import framework.security.IAuthenticator;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.session.IUserSessionManagerPlugin;
import framework.utils.Msg;
import framework.utils.Utilities;
import models.delivery.Release;
import models.finance.BudgetBucket;
import models.pmo.Actor;
import models.pmo.OrgUnit;
import models.pmo.Portfolio;
import models.pmo.PortfolioEntry;
import models.reporting.Reporting;
import models.timesheet.TimesheetReport;
import play.Logger;
import play.cache.CacheApi;
import play.libs.F.Promise;
import play.mvc.Http;
import play.mvc.Result;
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
 * The handler for the authorization mechanism based on Deadbold.<br/>
 * The class also holds a set of static methods which are to manage the user
 * session (basically create, get or delete the session entry containing the
 * unique user id)
 * 
 * @author Pierre-Yves Cloux
 */
public class DefaultDeadboltHandler extends CommonDeadboltHandler {
    private DefaultDynamicResourceHandler dynamicResourceHandler;
    private IAuthenticator authenticator;
    private ISecurityService securityService;
    private IPreferenceManagerPlugin preferenceManagerPlugin;

    public DefaultDeadboltHandler(IUserSessionManagerPlugin userSessionManagerPlugin, IAccountManagerPlugin accountManagerPlugin, ISecurityService securityService, CacheApi cacheApi,
            IPreferenceManagerPlugin preferenceManagerPlugin, IAuthenticator authenticator) {
        super(userSessionManagerPlugin, accountManagerPlugin);
        this.authenticator = authenticator;
        this.dynamicResourceHandler = new DefaultDynamicResourceHandler(userSessionManagerPlugin, cacheApi, securityService,getDynamicResourceHandlers());
        this.securityService=securityService;
        this.preferenceManagerPlugin=preferenceManagerPlugin;
    }

    @Override
    public Result redirectToLoginPage(String redirectUrl) {
        return getAuthenticator().redirectToLoginPage(redirectUrl);
    }

    @Override
    public Result displayAccessForbidden() {
        return badRequest(views.html.error.access_forbidden.render(Msg.get("forbidden.access.title")));
    }

    @Override
    public Promise<Optional<DynamicResourceHandler>> getDynamicResourceHandler(Http.Context context) {
        // WARNING : context can be null in some cases
        return Promise.promise(() -> Optional.of(getDynamicResourceHandler()));
    }
    
    /**
     * Check if the dynamic permission is allowed for the specified id
     * @param name a dynamic permission name
     * @param meta
     * @param deadboltHandler a deadbolt handler
     * @param id a unique id for an object
     * @param context a context
     * @return a promise of a boolean
     */
    public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Long id) {
        return getDynamicResourceHandler().isAllowed(name, meta, deadboltHandler, id, Http.Context.current());
    }
    
    public Map<String, DynamicResourceHandler> getDynamicResourceHandlers(){
        Map<String, DynamicResourceHandler> dynamicAuthenticationHandlers=new HashMap<String, DynamicResourceHandler>();
        dynamicAuthenticationHandlers.put(IMafConstants.PORTFOLIO_ENTRY_VIEW_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(Utilities.getId(context));
                    if (portfolioEntry == null) {
                        return true;
                    } else {
                        return PortfolioEntryDynamicHelper.isPortfolioEntryViewAllowed(portfolioEntry.id, getSecurityService());
                    }
                });
            }
        });
    
        dynamicAuthenticationHandlers.put(IMafConstants.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(Utilities.getId(context));
                    if (portfolioEntry == null) {
                        return true;
                    } else {
                        return PortfolioEntryDynamicHelper.isPortfolioEntryDetailsAllowed(portfolioEntry, getSecurityService());
                    }
                });
            }
        });
    
        dynamicAuthenticationHandlers.put(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(Utilities.getId(context));
                    if (portfolioEntry == null) {
                        return true;
                    } else {
                        return PortfolioEntryDynamicHelper.isPortfolioEntryEditAllowed(portfolioEntry, getSecurityService(), getPreferenceManagerPlugin());
                    }
                });
            }
        });
    
        dynamicAuthenticationHandlers.put(IMafConstants.PORTFOLIO_ENTRY_DELETE_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(Utilities.getId(context));
                    if (portfolioEntry == null) {
                        return true;
                    } else {
                        return PortfolioEntryDynamicHelper.isPortfolioEntryDeleteAllowed(portfolioEntry, getSecurityService());
                    }
                });
            }
        });
    
        dynamicAuthenticationHandlers.put(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_VIEW_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(Utilities.getId(context));
                    if (portfolioEntry == null) {
                        return true;
                    } else {
                        return PortfolioEntryDynamicHelper.isPortfolioEntryViewFinancialAllowed(portfolioEntry, getSecurityService());
                    }
                });
            }
        });
    
        dynamicAuthenticationHandlers.put(IMafConstants.PORTFOLIO_ENTRY_FINANCIAL_EDIT_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(Utilities.getId(context));
                    if (portfolioEntry == null) {
                        return true;
                    } else {
                        return PortfolioEntryDynamicHelper.isPortfolioEntryEditFinancialAllowed(portfolioEntry, getSecurityService());
                    }
                });
            }
        });
    
        dynamicAuthenticationHandlers.put(IMafConstants.PORTFOLIO_ENTRY_REVIEW_REQUEST_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(Utilities.getId(context));
                    if (portfolioEntry == null) {
                        return true;
                    } else {
                        return PortfolioEntryDynamicHelper.isPortfolioEntryReviewRequestAllowed(portfolioEntry, getSecurityService());
                    }
                });
            }
        });
    
        dynamicAuthenticationHandlers.put(IMafConstants.PORTFOLIO_VIEW_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    Portfolio portfolio = PortfolioDao.getPortfolioById(Utilities.getId(context));
                    if (portfolio == null) {
                        return true;
                    } else {
                        return PortfolioDynamicHelper.isPortfolioViewAllowed(portfolio.id, getSecurityService());
                    }
                });
            }
        });
    
        dynamicAuthenticationHandlers.put(IMafConstants.PORTFOLIO_EDIT_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    Portfolio portfolio = PortfolioDao.getPortfolioById(Utilities.getId(context));
                    if (portfolio == null) {
                        return true;
                    } else {
                        return PortfolioDynamicHelper.isPortfolioEditAllowed(portfolio, getSecurityService());
                    }
                });
            }
        });
    
        dynamicAuthenticationHandlers.put(IMafConstants.PORTFOLIO_VIEW_FINANCIAL_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    Portfolio portfolio = PortfolioDao.getPortfolioById(Utilities.getId(context));
                    if (portfolio == null) {
                        return true;
                    } else {
                        return PortfolioDynamicHelper.isPortfolioViewFinancialAllowed(portfolio, getSecurityService());
                    }
                });
            }
        });
    
        dynamicAuthenticationHandlers.put(IMafConstants.BUDGET_BUCKET_VIEW_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    BudgetBucket budgetBucket = BudgetBucketDAO.getBudgetBucketById(Utilities.getId(context));
                    if (budgetBucket == null) {
                        return true;
                    } else {
                        return BudgetBucketDynamicHelper.isBudgetBucketViewAllowed(budgetBucket.id, getSecurityService());
                    }
                });
            }
        });
    
        dynamicAuthenticationHandlers.put(IMafConstants.BUDGET_BUCKET_EDIT_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    BudgetBucket budgetBucket = BudgetBucketDAO.getBudgetBucketById(Utilities.getId(context));
                    if (budgetBucket == null) {
                        return true;
                    } else {
                        return BudgetBucketDynamicHelper.isBudgetBucketEditAllowed(budgetBucket, getSecurityService());
                    }
                });
            }
        });
    
        dynamicAuthenticationHandlers.put(IMafConstants.RELEASE_VIEW_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    Release release = ReleaseDAO.getReleaseById(Utilities.getId(context));
                    if (release == null) {
                        return true;
                    } else {
                        return ReleaseDynamicHelper.isReleaseViewAllowed(release.id, getSecurityService());
                    }
                });
            }
        });
    
        dynamicAuthenticationHandlers.put(IMafConstants.RELEASE_EDIT_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    Release release = ReleaseDAO.getReleaseById(Utilities.getId(context));
                    if (release == null) {
                        return true;
                    } else {
                        return ReleaseDynamicHelper.isReleaseEditAllowed(release, getSecurityService());
                    }
                });
            }
        });
    
        dynamicAuthenticationHandlers.put(IMafConstants.REPORTING_VIEW_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    Reporting report = ReportingDao.getReportingById(Utilities.getId(context));
                    if (report == null) {
                        return true;
                    } else {
                        return ReportingDynamicHelper.isReportViewAllowed(report.id, getSecurityService());
                    }
                });
            }
        });
    
        dynamicAuthenticationHandlers.put(IMafConstants.TIMESHEET_APPROVAL_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    TimesheetReport report = TimesheetDao.getTimesheetReportById(Utilities.getId(context));
    
                    if (report == null) {
                        return true;
                    } else {
                        return TimesheetReportDynamicHelper.isTimesheetReportApprovalAllowed(report.id, getSecurityService());
                    }
                });
            }
        });
    
        dynamicAuthenticationHandlers.put(IMafConstants.ACTOR_VIEW_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    Actor actor = ActorDao.getActorById(Utilities.getId(context));
    
                    if (actor == null) {
                        return true;
                    } else {
                        return ActorDynamicHelper.isActorViewAllowed(actor.id, getSecurityService());
                    }
                });
            }
        });
    
        dynamicAuthenticationHandlers.put(IMafConstants.ACTOR_EDIT_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    Actor actor = ActorDao.getActorById(Utilities.getId(context));
                    if (actor == null) {
                        return true;
                    } else {
                        return ActorDynamicHelper.isActorEditAllowed(actor, getSecurityService());
                    }
                });
            }
        });
    
        dynamicAuthenticationHandlers.put(IMafConstants.ACTOR_DELETE_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    Actor actor = ActorDao.getActorById(Utilities.getId(context));
                    if (actor == null) {
                        return true;
                    } else {
                        return ActorDynamicHelper.isActorDeleteAllowed(actor, getSecurityService());
                    }
                });
            }
        });
    
        dynamicAuthenticationHandlers.put(IMafConstants.ORG_UNIT_VIEW_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    OrgUnit orgUnit = OrgUnitDao.getOrgUnitById(Utilities.getId(context));
    
                    if (orgUnit == null) {
                        return true;
                    } else {
                        return OrgUnitDynamicHelper.isOrgUnitViewAllowed(orgUnit.id, getSecurityService());
                    }
                });
            }
        });
        return dynamicAuthenticationHandlers;
    }

    private DefaultDynamicResourceHandler getDynamicResourceHandler() {
        return dynamicResourceHandler;
    }

    private IAuthenticator getAuthenticator() {
        return authenticator;
    }

    private ISecurityService getSecurityService() {
        return securityService;
    }

    private IPreferenceManagerPlugin getPreferenceManagerPlugin() {
        return preferenceManagerPlugin;
    }

    /**
     * The deadbolt dynamic resource handler.
     * 
     * @author Johann Kohler
     * 
     */
    public static class DefaultDynamicResourceHandler implements DynamicResourceHandler {

        private Map<String, DynamicResourceHandler> dynamicAuthenticationHandlers;

        private IUserSessionManagerPlugin userSessionManagerPlugin;
        private CacheApi cacheApi;
        private ISecurityService securityService;
        private static final Integer CACHE_TTL = 300;

        public DefaultDynamicResourceHandler(
                IUserSessionManagerPlugin userSessionManagerPlugin, 
                CacheApi cacheApi,
                ISecurityService securityService,
                Map<String, DynamicResourceHandler> dynamicAuthenticationHandlers) {
            super();
            this.userSessionManagerPlugin = userSessionManagerPlugin;
            this.cacheApi=cacheApi;
            this.securityService=securityService;
            this.dynamicAuthenticationHandlers=dynamicAuthenticationHandlers;
        }

        @Override
        public Promise<Boolean> checkPermission(String permissionValue, DeadboltHandler deadboltHandler, Context ctx) {
            boolean permissionOk = false;

            try {
                IUserAccount userAccount = getSecurityService().getCurrentUser();
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
            return isAllowed(name, meta, deadboltHandler, Utilities.getId(context), context);
        }
        
        /**
         * Check if the dynamic permission is allowed for the specified id
         * @param name a dynamic permission name
         * @param meta
         * @param deadboltHandler a deadbolt handler
         * @param id a unique id for an object
         * @param context a context
         * @return a promise of a boolean
         */
        private Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Long id, Http.Context context) {
            String cacheKey = getCacheKey(name, id);
            Boolean isAllowed = (Boolean) getCacheApi().get(cacheKey);
            if (isAllowed != null) {
                Logger.debug("dynamic permission " + cacheKey + " read from cache, and result is: " + isAllowed);
                final boolean isAllowedFinal = isAllowed;
                return Promise.promise(() -> isAllowedFinal);
            }

            DynamicResourceHandler handler = getDynamicAuthenticationHandlers().get(name);
            Promise<Boolean> result = Promise.promise(() -> false);
            if (handler == null) {
                Logger.error("No handler available for " + name);
            } else {
                Logger.debug("Dynamic permission: " + name);
                result = handler.isAllowed(name, meta, deadboltHandler, context);
            }

            // set the result
            getCacheApi().set(cacheKey, result.get(ISecurityService.DEFAULT_TIMEOUT), CACHE_TTL);

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
        private String getCacheKey(String name, Long id) {
            IUserSessionManagerPlugin userSessionManagerPlugin = getUserSessionManagerPlugin();
            String cacheKey = IMafConstants.DYNAMIC_PERMISSION_CACHE_PREFIX + userSessionManagerPlugin.getUserSessionId(Http.Context.current()) + "." + name + "."
                    + String.valueOf(id);
            return cacheKey;

        }

        private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
            return userSessionManagerPlugin;
        }

        private CacheApi getCacheApi() {
            return cacheApi;
        }

        private ISecurityService getSecurityService() {
            return securityService;
        }

        private Map<String, DynamicResourceHandler> getDynamicAuthenticationHandlers() {
            return dynamicAuthenticationHandlers;
        }
    }
}
