package security;

import javax.inject.Singleton;

import framework.services.StaticModuleInitializer;
import play.Logger;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;
import be.objectify.deadbolt.java.cache.HandlerCache;

/**
 * Creates a binding for the Deadbolt implementation (injection)
 */
public class DefaultDeadboltHook extends Module {
    private static Logger.ALogger log = Logger.of(DefaultDeadboltHook.class);
    @Override
    public Seq<Binding<?>> bindings(final Environment environment, final Configuration configuration) {
        log.info("Deadbold module initialization");
        return seq(bind(HandlerCache.class).to(DefaultHandlerCache.class).in(Singleton.class));
    }
}
