package security;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.java.JavaAnalyzer;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import be.objectify.deadbolt.java.actions.SubjectPresentAction;
import be.objectify.deadbolt.java.actions.Unrestricted;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import constants.IMafConstants;
import framework.security.IAuthenticator;
import framework.services.account.AccountManagementException;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.session.IUserSessionManagerPlugin;
import framework.utils.Utilities;
import models.framework_models.account.SystemPermission;
import play.Logger;
import play.cache.CacheApi;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Result;

/**
 * Handlers cache implementation.<br/>
 * Only one in our current implementation.
 */
@Singleton
public class DefaultHandlerCache implements HandlerCache, ISecurityService {
    private static Logger.ALogger log = Logger.of(DefaultHandlerCache.class);
    private JavaAnalyzer deadBoltAnalyzer;
    private SubjectCache subjectCache;
    private IAccountManagerPlugin accountManagerPlugin;
    private IUserSessionManagerPlugin userSessionManagerPlugin;
    private DefaultDeadboltHandler defaultHandler;

    @Inject
    public DefaultHandlerCache(
            JavaAnalyzer deadBoltAnalyzer,
            SubjectCache subjectCache,
            IUserSessionManagerPlugin userSessionManagerPlugin, 
            IAccountManagerPlugin accountManagerPlugin, 
            ISecurityService securityService,
            IAuthenticator authenticator,
            IPreferenceManagerPlugin preferenceManagerPlugin,
            CacheApi cacheApi) {
        this.deadBoltAnalyzer=deadBoltAnalyzer;
        this.subjectCache=subjectCache;
        this.accountManagerPlugin=accountManagerPlugin;
        this.userSessionManagerPlugin=userSessionManagerPlugin;
        Logger.info(">>>>>>>>>>>>>>>> Check permissions consistency");
        if (!SystemPermission.checkPermissions(IMafConstants.class)) {
            Logger.error("WARNING: permissions in code are not consistent with permissions in database");
        }
        Logger.info(">>>>>>>>>>>>>>>> Check permissions consistency (end)");
        this.defaultHandler = new DefaultDeadboltHandler(userSessionManagerPlugin, accountManagerPlugin, securityService, cacheApi, preferenceManagerPlugin,authenticator);
    }

    @Override
    public DeadboltHandler apply(final String key) {
        return getDefaultHandler();
    }

    @Override
    public DeadboltHandler get() {
        return getDefaultHandler();
    }
    
    @Override
    public IUserAccount getCurrentUser() throws AccountManagementException {
        IUserAccount userAccount=getAccountManagerPlugin().getUserAccountFromUid(getUserSessionManagerPlugin().getUserSessionId(Http.Context.current()));
        return userAccount;
    }

    /* (non-Javadoc)
     * @see security.ISecurityService#checkHasSubject(play.libs.F.Function0)
     */
    @Override
    public Promise<Result> checkHasSubject(final Function0<Result> resultIfHasSubject) {
        try {
            SubjectPresentAction subjectPresentAction = new SubjectPresentAction(getDeadBoltAnalyzer(), getSubjectCache(), this);
            subjectPresentAction.configuration = new SubjectPresent() {

                @Override
                public Class<? extends Annotation> annotationType() {
                    return SubjectPresent.class;
                }

                @Override
                public String handlerKey() {
                    return null;
                }

                @Override
                public boolean forceBeforeAuthCheck() {
                    return false;
                }

                @Override
                public boolean deferred() {
                    return false;
                }

                @Override
                public String content() {
                    return null;
                }
            };
            subjectPresentAction.delegate = new Action<String>() {
                @Override
                public Promise<Result> call(Context arg0) throws Throwable {
                    return Promise.promise(() -> resultIfHasSubject.apply());
                }

            };
            return subjectPresentAction.call(Http.Context.current());
        } catch (Throwable e) {
            log.error("Error while checking if the current context has a subject", e);
            return Promise.promise(new Function0<Result>() {
                @Override
                public Result apply() throws Throwable {
                    return Controller.badRequest();
                }
            });
        }
    }

    /**
     * Gets the {@link be.objectify.deadbolt.core.models.Subject} from the
     * {@link DeadboltHandler}, and logs an error if it's not present. Note that
     * at least one actions ({@link Unrestricted} does not not require a Subject
     * to be present.
     *
     * @param ctx
     *            the request context
     * @param deadboltHandler
     *            the Deadbolt handler
     * @return the Subject, if any
     */
    private Promise<Optional<Subject>> getSubject(final Http.Context ctx, final DeadboltHandler deadboltHandler) {
        if (log.isDebugEnabled()) {
            log.debug("GET SUBJECT with subject cache [" + getSubjectCache() + "]");
        }
        return getSubjectCache().apply(deadboltHandler, ctx).map(option -> {
            if (!option.isPresent()) {
                if (log.isDebugEnabled()) {
                    log.info("Subject not found in Deadbolt subject cache");
                }
            }
            return option;
        });
    }

    /* (non-Javadoc)
     * @see security.ISecurityService#dynamic(java.lang.String, java.lang.String)
     */
    @Override
    public boolean dynamic(String name, String meta) {
        if (log.isDebugEnabled()) {
            log.debug("Check dynamic permission with Handler [" + get() + "]");
            log.debug("Check dynamic permission with Dynamic Handler [" + get().getDynamicResourceHandler(Http.Context.current()) + "]");
        }
        try {
            DeadboltHandler handler = get();
            DynamicResourceHandler dynamicResourceHandler = handler.getDynamicResourceHandler(Http.Context.current()).get(DEFAULT_TIMEOUT).get();
            return dynamicResourceHandler.isAllowed(name, meta, get(), Http.Context.current()).get(DEFAULT_TIMEOUT);
        } catch (Exception e) {
            log.error("Error while trying to check if a user is allowed for the permission name " + name + " and the meta information " + meta, e);
        }
        return false;
    }

    @Override
    public boolean dynamic(String name, String meta, Long id) {
        return getDefaultHandler().isAllowed(name, meta, get(), id).get(DEFAULT_TIMEOUT);
    }

    @Override
    public boolean restrict(List<String[]> deadBoltRoles) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("RESTRICT with handler [" + get() + "] timeout [" + DEFAULT_TIMEOUT + "] for roles " + Utilities.toString(deadBoltRoles));
            }
            Optional<Subject> subjectOption = getSubject(Http.Context.current(), get()).get(DEFAULT_TIMEOUT);
            if (!subjectOption.isPresent()) {
                if (log.isDebugEnabled()) {
                    log.debug("RESTRICT FALSE since no subject found");
                }
                return false;
            }
            Subject subject = subjectOption.get();
            if (log.isDebugEnabled()) {
                log.debug("RESTRICT Subject = " + subject);
            }
            return restrict(deadBoltRoles, subject);
        } catch (Throwable e) {
            log.error("Error while checking restriction for " + Utilities.toString(deadBoltRoles), e);
            return false;
        }
    }

    @Override
    public boolean restrict(List<String[]> deadBoltRoles, String uid) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("RESTRICT for uid [" + uid + "] [" + get() + "] timeout [" + DEFAULT_TIMEOUT + "] for roles "
                        + Utilities.toString(deadBoltRoles));
            }
            Subject subject = getAccountManagerPlugin().getUserAccountFromUid(uid);
            return restrict(deadBoltRoles, subject);
        } catch (Throwable e) {
            log.error("Error while checking restriction for " + Utilities.toString(deadBoltRoles), e);
            return false;
        }
    }

    @Override
    public boolean restrict(List<String[]> deadBoltRoles, Subject subject) {
        try {
            if (subject == null) {
                return false;
            }
            for (String[] rolesArray : deadBoltRoles) {
                if (getDeadBoltAnalyzer().hasAllRoles(Optional.of(subject), rolesArray)) {
                    return true;
                }
            }
            return false;
        } catch (Throwable e) {
            log.error("Error while checking restriction for " + Utilities.toString(deadBoltRoles), e);
            return false;
        }
    }

    @Override
    public boolean restrict(final String roleName, final Subject subject) {
        return getDeadBoltAnalyzer().hasRole(Optional.of(subject), roleName);
    }

    @Override
    public boolean restrict(final String[] roleNames, final Subject subject) {
        return getDeadBoltAnalyzer().hasAllRoles(Optional.of(subject), roleNames);
    }

    @Override
    public boolean restrict(String roleName) throws AccountManagementException {
        return restrict(roleName, getCurrentUser());
    }

    @Override
    public boolean restrict(String[] roleNames) throws AccountManagementException {
        return restrict(roleNames, getCurrentUser());
    }

    private JavaAnalyzer getDeadBoltAnalyzer() {
        return deadBoltAnalyzer;
    }

    private SubjectCache getSubjectCache() {
        return subjectCache;
    }

    private IAccountManagerPlugin getAccountManagerPlugin() {
        return accountManagerPlugin;
    }

    private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return userSessionManagerPlugin;
    }

    private DefaultDeadboltHandler getDefaultHandler() {
        return defaultHandler;
    }
}
