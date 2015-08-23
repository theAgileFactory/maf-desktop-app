package security;

import javax.inject.Inject;
import javax.inject.Singleton;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;
import constants.IMafConstants;
import controllers.sso.Authenticator;
import framework.services.account.IAccountManagerPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import models.framework_models.account.SystemPermission;
import play.Logger;

/**
 * Handlers cache implementation.<br/>
 * Only one in our current implementation.
 */
@Singleton
public class DefaultHandlerCache implements HandlerCache {
    private final DeadboltHandler defaultHandler;

    @Inject
    public DefaultHandlerCache(
            IUserSessionManagerPlugin userSessionManagerPlugin, 
            IAccountManagerPlugin accountManagerPlugin,
            Authenticator authenticator) {
        Logger.info(">>>>>>>>>>>>>>>> Check permissions consistency");
        if (!SystemPermission.checkPermissions(IMafConstants.class)) {
            Logger.error("WARNING: permissions in code are not consistent with permissions in database");
        }
        Logger.info(">>>>>>>>>>>>>>>> Check permissions consistency (end)");
        this.defaultHandler = new DefaultDeadboltHandler(userSessionManagerPlugin, accountManagerPlugin, authenticator);
    }

    @Override
    public DeadboltHandler apply(final String key) {
        return defaultHandler;
    }

    @Override
    public DeadboltHandler get() {
        return defaultHandler;
    }
}
