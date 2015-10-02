package services.database;

import javax.inject.Inject;
import javax.inject.Singleton;

import framework.services.database.AbstractDatabaseDependencyServiceImpl;
import framework.services.database.IDatabaseDependencyService;
import play.Configuration;
import play.Environment;
import play.Logger.ALogger;
import play.db.DBApi;
import play.db.ebean.EbeanConfig;
import play.inject.ApplicationLifecycle;

/**
 * A concrete implementation for the
 * {@link AbstractDatabaseDependencyServiceImpl} and
 * {@link IDatabaseDependencyService}.
 * 
 * @author Pierre-Yves Cloux
 */
@Singleton
public class DatabaseDependencyServiceImpl extends AbstractDatabaseDependencyServiceImpl {

    /**
     * Constructor the dependency implementation.
     * 
     * @param lifecycle
     *            the lifecycle service
     * @param environment
     *            the environment service
     * @param configuration
     *            the configuration service
     * @param ebeanConfig
     *            the eban configuration
     * @param dbApi
     *            the play database API (this must prevent the database API to
     *            be closed before this service is stopped)
     */
    @Inject
    public DatabaseDependencyServiceImpl(ApplicationLifecycle lifecycle, Environment environment, Configuration configuration, EbeanConfig ebeanConfig,
            DBApi dbApi) {
        super(lifecycle, environment, configuration, ebeanConfig, dbApi);
    }

    @Override
    public String getRelease() {
        return "7.0.1";
    }

    @Override
    public void patch(ALogger log) {

    }
}
