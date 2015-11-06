package services.database;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;

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
        return "9.0.0";
    }

    @Override
    public void patch(ALogger log) {

        log.info("START PATCHER " + this.getRelease());

        log.info("END PATCHER " + this.getRelease());

    }

    /**
     * Add a i18k key.
     * 
     * @param key
     *            the key
     * @param value
     *            the value
     * @param languageCode
     *            the language
     */
    private void addI18nKey(String key, String value, String languageCode) {
        SqlUpdate insertKeyQuery = Ebean.createSqlUpdate("insert into i18n_messages (`key`, language, value) values (:key, :language, :value)");
        insertKeyQuery.setParameter("value", value);
        insertKeyQuery.setParameter("key", key);
        insertKeyQuery.setParameter("language", languageCode);
        insertKeyQuery.execute();
    }

}
