package security;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.java.JavaAnalyzer;
import be.objectify.deadbolt.java.cache.SubjectCache;
import constants.IMafConstants;
import dao.delivery.ReleaseDAO;
import dao.finance.BudgetBucketDAO;
import dao.pmo.ActorDao;
import dao.pmo.OrgUnitDao;
import dao.pmo.PortfolioDao;
import dao.pmo.PortfolioEntryDao;
import dao.reporting.ReportingDao;
import dao.timesheet.TimesheetDao;
import framework.security.AbstractSecurityServiceImpl;
import framework.security.IAuthenticator;
import framework.security.ISecurityService;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.utils.Utilities;
import models.delivery.Release;
import models.finance.BudgetBucket;
import models.framework_models.account.SystemPermission;
import models.pmo.Actor;
import models.pmo.OrgUnit;
import models.pmo.Portfolio;
import models.pmo.PortfolioEntry;
import models.reporting.Reporting;
import models.timesheet.TimesheetReport;
import play.Logger;
import play.cache.CacheApi;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import security.dynamic.ActorDynamicHelper;
import security.dynamic.BudgetBucketDynamicHelper;
import security.dynamic.OrgUnitDynamicHelper;
import security.dynamic.PortfolioDynamicHelper;
import security.dynamic.PortfolioEntryDynamicHelper;
import security.dynamic.ReleaseDynamicHelper;
import security.dynamic.ReportingDynamicHelper;
import security.dynamic.TimesheetReportDynamicHelper;

public class SecurityServiceImpl extends AbstractSecurityServiceImpl {
    private static Logger.ALogger log = Logger.of(SecurityServiceImpl.class);
    private IPreferenceManagerPlugin preferenceManagerPlugin;
    private II18nMessagesPlugin messagesPlugins;

    @Inject
    public SecurityServiceImpl(JavaAnalyzer deadBoltAnalyzer, SubjectCache subjectCache, IUserSessionManagerPlugin userSessionManagerPlugin,
            IAccountManagerPlugin accountManagerPlugin, ISecurityService securityService, CacheApi cacheApi,
            IAuthenticator authenticator,IPreferenceManagerPlugin preferenceManagerPlugin, II18nMessagesPlugin messagesPlugins) {
        super(deadBoltAnalyzer, subjectCache, userSessionManagerPlugin, accountManagerPlugin, securityService, cacheApi,
                authenticator);
        this.preferenceManagerPlugin=preferenceManagerPlugin;
        this.messagesPlugins=messagesPlugins;
        log.info(">>>>>>>>>>>>>>>> Check permissions consistency");
        if (!SystemPermission.checkPermissions(IMafConstants.class)) {
            log.error("WARNING: permissions in code are not consistent with permissions in database");
        }
        log.info(">>>>>>>>>>>>>>>> Check permissions consistency (end)");
    }

    @Override
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

    @Override
    public Result displayAccessForbidden() {
        return Controller.badRequest(views.html.error.access_forbidden.render(getMessagesPlugins().get("forbidden.access.title")));
    }

    private IPreferenceManagerPlugin getPreferenceManagerPlugin() {
        return preferenceManagerPlugin;
    }

    private II18nMessagesPlugin getMessagesPlugins() {
        return messagesPlugins;
    }
}
