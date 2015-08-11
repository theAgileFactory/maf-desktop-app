package security;

import javax.inject.Inject;
import javax.inject.Singleton;

import controllers.sso.Authenticator;
import framework.services.account.IAccountManagerPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;

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
