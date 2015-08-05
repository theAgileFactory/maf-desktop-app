package security;

import javax.inject.Singleton;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;

/**
 * Handlers cache implementation.<br/>
 * Only one in our current implementation.
 */
@Singleton
public class DefaultHandlerCache implements HandlerCache {
    private final DeadboltHandler defaultHandler = new DefaultDeadboltHandler();

    public DefaultHandlerCache() {
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
