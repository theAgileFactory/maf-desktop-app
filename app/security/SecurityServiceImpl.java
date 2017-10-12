package security;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.java.JavaAnalyzer;
import be.objectify.deadbolt.java.cache.SubjectCache;
import constants.IMafConstants;
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
import models.finance.BudgetBucket;
import models.framework_models.account.SystemPermission;
import models.pmo.Actor;
import models.pmo.OrgUnit;
import models.pmo.Portfolio;
import models.pmo.PortfolioEntry;
import models.reporting.Reporting;
import models.timesheet.TimesheetReport;
import play.Configuration;
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
import security.dynamic.ReportingDynamicHelper;
import security.dynamic.TimesheetReportDynamicHelper;

/**
 * The security service.
 * 
 * @author Johann Kohler
 */
@Singleton
public class SecurityServiceImpl extends AbstractSecurityServiceImpl {
    private static Logger.ALogger log = Logger.of(SecurityServiceImpl.class);
    private IPreferenceManagerPlugin preferenceManagerPlugin;
    private II18nMessagesPlugin messagesPlugins;

    /**
     * Default constructor.
     * 
     * @param deadBoltAnalyzer
     *            the deadbold analyzer
     * @param subjectCache
     *            the deadbold cache for subject
     * @param configuration
     *            the Play configuration service
     * @param userSessionManagerPlugin
     *            the user session manager service
     * @param accountManagerPlugin
     *            the account manager service
     * @param cacheApi
     *            the Play cache service
     * @param authenticator
     *            the authenticator service
     * @param preferenceManagerPlugin
     *            the preference manager service
     * @param messagesPlugins
     *            the i18n messages service
     */
    @Inject
    public SecurityServiceImpl(JavaAnalyzer deadBoltAnalyzer, SubjectCache subjectCache, Configuration configuration,
            IUserSessionManagerPlugin userSessionManagerPlugin, IAccountManagerPlugin accountManagerPlugin, CacheApi cacheApi, IAuthenticator authenticator,
            IPreferenceManagerPlugin preferenceManagerPlugin, II18nMessagesPlugin messagesPlugins) {
        super(deadBoltAnalyzer, subjectCache, configuration, userSessionManagerPlugin, accountManagerPlugin, cacheApi, authenticator);

        this.preferenceManagerPlugin = preferenceManagerPlugin;
        this.messagesPlugins = messagesPlugins;
        log.info(">>>>>>>>>>>>>>>> Check permissions consistency");
        if (!SystemPermission.checkPermissions(IMafConstants.class)) {
            log.error("WARNING: permissions in code are not consistent with permissions in database");
        }
        log.info(">>>>>>>>>>>>>>>> Check permissions consistency (end)");
    }

    @Override
    public Map<String, DynamicResourceHandler> getDynamicResourceHandlers() {
        final ISecurityService securityService = this;

        Map<String, DynamicResourceHandler> dynamicAuthenticationHandlers = new HashMap<String, DynamicResourceHandler>();
        dynamicAuthenticationHandlers.put(IMafConstants.PORTFOLIO_ENTRY_VIEW_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(Utilities.getId(context));
                    if (portfolioEntry == null) {
                        return true;
                    } else {
                        return PortfolioEntryDynamicHelper.isPortfolioEntryViewAllowed(portfolioEntry.id, securityService);
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
                        return PortfolioEntryDynamicHelper.isPortfolioEntryDetailsAllowed(portfolioEntry, securityService);
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
                        return PortfolioEntryDynamicHelper.isPortfolioEntryEditAllowed(portfolioEntry, securityService, getPreferenceManagerPlugin());
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
                        return PortfolioEntryDynamicHelper.isPortfolioEntryDeleteAllowed(portfolioEntry, securityService);
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
                        return PortfolioEntryDynamicHelper.isPortfolioEntryViewFinancialAllowed(portfolioEntry, securityService);
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
                        return PortfolioEntryDynamicHelper.isPortfolioEntryEditFinancialAllowed(portfolioEntry, securityService);
                    }
                });
            }
        });

        dynamicAuthenticationHandlers.put(IMafConstants.PORTFOLIO_ENTRY_CONFIRM_ALLOCATIONS_DYNAMIC_PERMISSION, new AbstractDynamicResourceHandler() {
            public Promise<Boolean> isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context context) {
                return Promise.promise(() -> {
                    PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(Utilities.getId(context));
                    if (portfolioEntry == null) {
                        return true;
                    } else {
                        return PortfolioEntryDynamicHelper.isPortfolioEntryConfirmAllocationsAllowed(portfolioEntry, securityService);
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
                        return PortfolioEntryDynamicHelper.isPortfolioEntryReviewRequestAllowed(portfolioEntry, securityService);
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
                        return PortfolioDynamicHelper.isPortfolioViewAllowed(portfolio.id, securityService);
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
                        return PortfolioDynamicHelper.isPortfolioEditAllowed(portfolio, securityService);
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
                        return PortfolioDynamicHelper.isPortfolioViewFinancialAllowed(portfolio, securityService);
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
                        return BudgetBucketDynamicHelper.isBudgetBucketViewAllowed(budgetBucket.id, securityService);
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
                        return BudgetBucketDynamicHelper.isBudgetBucketEditAllowed(budgetBucket, securityService);
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
                        return ReportingDynamicHelper.isReportViewAllowed(report.id, securityService);
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
                        return TimesheetReportDynamicHelper.isTimesheetReportApprovalAllowed(report.id, securityService);
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
                        return ActorDynamicHelper.isActorViewAllowed(actor.id, securityService);
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
                        return ActorDynamicHelper.isActorEditAllowed(actor, securityService);
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
                        return ActorDynamicHelper.isActorDeleteAllowed(actor, securityService);
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
                        return OrgUnitDynamicHelper.isOrgUnitViewAllowed(orgUnit.id, securityService);
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

    /**
     * Get the preference manager service.
     */
    private IPreferenceManagerPlugin getPreferenceManagerPlugin() {
        return preferenceManagerPlugin;
    }

    /**
     * Get the i18n messages service.
     */
    private II18nMessagesPlugin getMessagesPlugins() {
        return messagesPlugins;
    }
}
