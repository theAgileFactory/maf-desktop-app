package services.database;

import javax.inject.Inject;

import framework.services.database.AbstractDatabaseDependencyServiceImpl;
import framework.services.database.IDatabaseDependencyService;
import play.Configuration;
import play.Environment;
import play.Logger.ALogger;
import play.db.ebean.EbeanConfig;
import play.inject.ApplicationLifecycle;

/**
 * A concrete implementation for the {@link AbstractDatabaseDependencyServiceImpl} and {@link IDatabaseDependencyService}
 * @author Pierre-Yves Cloux
 */
public class DatabaseDependencyServiceImpl extends AbstractDatabaseDependencyServiceImpl {

    @Inject
    public DatabaseDependencyServiceImpl(ApplicationLifecycle lifecycle, Environment environment, Configuration configuration, EbeanConfig ebeanConfig) {
        super(lifecycle, environment, configuration, ebeanConfig);
    }

    @Override
    public String getRelease() {
        return "7.0.1";
    }

    @Override
    public void patchBeforeEbean(ALogger log) {
        log.info("Patching the database before Ebean is activated");
    }
    
    @Override
    public void patchAfterEbean(ALogger log) {
        log.info("Patching the database after Ebean is activated");
    }

}
