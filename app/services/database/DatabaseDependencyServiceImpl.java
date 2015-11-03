package services.database;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;

import dao.pmo.PortfolioEntryPlanningPackageDao;
import framework.services.database.AbstractDatabaseDependencyServiceImpl;
import framework.services.database.IDatabaseDependencyService;
import models.pmo.PortfolioEntryPlanningPackage;
import models.pmo.PortfolioEntryPlanningPackagePattern;
import models.pmo.PortfolioEntryPlanningPackageType;
import play.Configuration;
import play.Environment;
import play.Logger;
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

        List<PortfolioEntryPlanningPackageType> planningPackageTypes = PortfolioEntryPlanningPackageDao.getPEPlanningPackageTypeAsList();
        if (planningPackageTypes.size() == 0) {

            // Create the 6 default types

            log.info("start insert default (gray)");

            PortfolioEntryPlanningPackageType defaultType = new PortfolioEntryPlanningPackageType();
            defaultType.name = "portfolio_entry_planning_package_type.name.default";
            defaultType.isActive = true;
            defaultType.cssClass = "default";
            defaultType.save();

            addI18nKey("portfolio_entry_planning_package_type.name.default", "Gray", "en");
            addI18nKey("portfolio_entry_planning_package_type.name.default", "Gris", "fr");
            addI18nKey("portfolio_entry_planning_package_type.name.default", "Grau", "de");

            log.info("end insert default (gray)");

            log.info("start insert info (cyan)");

            PortfolioEntryPlanningPackageType infoType = new PortfolioEntryPlanningPackageType();
            infoType.name = "portfolio_entry_planning_package_type.name.info";
            infoType.isActive = true;
            infoType.cssClass = "info";
            infoType.save();

            addI18nKey("portfolio_entry_planning_package_type.name.info", "Cyan", "en");
            addI18nKey("portfolio_entry_planning_package_type.name.info", "Cyan", "fr");
            addI18nKey("portfolio_entry_planning_package_type.name.info", "Türkis", "de");

            log.info("end insert info (cyan)");

            log.info("start insert primary (blue)");

            PortfolioEntryPlanningPackageType primaryType = new PortfolioEntryPlanningPackageType();
            primaryType.name = "portfolio_entry_planning_package_type.name.primary";
            primaryType.isActive = true;
            primaryType.cssClass = "primary";
            primaryType.save();

            addI18nKey("portfolio_entry_planning_package_type.name.primary", "Blue", "en");
            addI18nKey("portfolio_entry_planning_package_type.name.primary", "Bleu", "fr");
            addI18nKey("portfolio_entry_planning_package_type.name.primary", "Blau", "de");

            log.info("end insert primary (blue)");

            log.info("start insert success (green)");

            PortfolioEntryPlanningPackageType successType = new PortfolioEntryPlanningPackageType();
            successType.name = "portfolio_entry_planning_package_type.name.success";
            successType.isActive = true;
            successType.cssClass = "success";
            successType.save();

            addI18nKey("portfolio_entry_planning_package_type.name.success", "Green", "en");
            addI18nKey("portfolio_entry_planning_package_type.name.success", "Vert", "fr");
            addI18nKey("portfolio_entry_planning_package_type.name.success", "Grün", "de");

            log.info("end insert success (green)");

            log.info("start insert warning (amber)");

            PortfolioEntryPlanningPackageType warningType = new PortfolioEntryPlanningPackageType();
            warningType.name = "portfolio_entry_planning_package_type.name.warning";
            warningType.isActive = true;
            warningType.cssClass = "warning";
            warningType.save();

            addI18nKey("portfolio_entry_planning_package_type.name.warning", "Amber", "en");
            addI18nKey("portfolio_entry_planning_package_type.name.warning", "Orange", "fr");
            addI18nKey("portfolio_entry_planning_package_type.name.warning", "Orange", "de");

            log.info("end insert warning (amber)");

            log.info("start insert danger (red)");

            PortfolioEntryPlanningPackageType dangerType = new PortfolioEntryPlanningPackageType();
            dangerType.name = "portfolio_entry_planning_package_type.name.danger";
            dangerType.isActive = true;
            dangerType.cssClass = "danger";
            dangerType.save();

            addI18nKey("portfolio_entry_planning_package_type.name.danger", "Red", "en");
            addI18nKey("portfolio_entry_planning_package_type.name.danger", "Rouge", "fr");
            addI18nKey("portfolio_entry_planning_package_type.name.danger", "Rot", "de");

            log.info("end insert danger (red)");

            // Adapt the current existing planning packages

            log.info("start reconciliate planning packages");

            for (PortfolioEntryPlanningPackage planningPackage : PortfolioEntryPlanningPackageDao.findPortfolioEntryPlanningPackage.where()
                    .eq("deleted", false).findList()) {

                if (planningPackage.cssClass != null && planningPackage.cssClass.equals("info")) {
                    planningPackage.portfolioEntryPlanningPackageType = infoType;
                } else if (planningPackage.cssClass != null && planningPackage.cssClass.equals("primary")) {
                    planningPackage.portfolioEntryPlanningPackageType = primaryType;
                } else if (planningPackage.cssClass != null && planningPackage.cssClass.equals("success")) {
                    planningPackage.portfolioEntryPlanningPackageType = successType;
                } else if (planningPackage.cssClass != null && planningPackage.cssClass.equals("warning")) {
                    planningPackage.portfolioEntryPlanningPackageType = warningType;
                } else if (planningPackage.cssClass != null && planningPackage.cssClass.equals("danger")) {
                    planningPackage.portfolioEntryPlanningPackageType = dangerType;
                } else {
                    planningPackage.portfolioEntryPlanningPackageType = defaultType;
                }

                log.info("the planning package \"" + planningPackage.name + " (" + planningPackage.id + ")\" with cssClass \"" + planningPackage.cssClass
                        + "\" has been reconciliated with the type \"" + planningPackage.portfolioEntryPlanningPackageType.cssClass + " ("
                        + planningPackage.portfolioEntryPlanningPackageType.id + ")\"");

                planningPackage.save();

            }

            log.info("end reconciliate planning packages");

            // Adapt the current existing planning package patterns

            log.info("start reconciliate planning package patterns");

            for (PortfolioEntryPlanningPackagePattern planningPackagePattern : PortfolioEntryPlanningPackageDao.findPortfolioEntryPlanningPackagePattern
                    .where().eq("deleted", false).findList()) {

                if (planningPackagePattern.cssClass != null && planningPackagePattern.cssClass.equals("info")) {
                    planningPackagePattern.portfolioEntryPlanningPackageType = infoType;
                } else if (planningPackagePattern.cssClass != null && planningPackagePattern.cssClass.equals("primary")) {
                    planningPackagePattern.portfolioEntryPlanningPackageType = primaryType;
                } else if (planningPackagePattern.cssClass != null && planningPackagePattern.cssClass.equals("success")) {
                    planningPackagePattern.portfolioEntryPlanningPackageType = successType;
                } else if (planningPackagePattern.cssClass != null && planningPackagePattern.cssClass.equals("warning")) {
                    planningPackagePattern.portfolioEntryPlanningPackageType = warningType;
                } else if (planningPackagePattern.cssClass != null && planningPackagePattern.cssClass.equals("danger")) {
                    planningPackagePattern.portfolioEntryPlanningPackageType = dangerType;
                } else {
                    planningPackagePattern.portfolioEntryPlanningPackageType = defaultType;
                }

                log.info("the planning package pattern \"" + planningPackagePattern.name + " (" + planningPackagePattern.id + ")\" with cssClass \""
                        + planningPackagePattern.cssClass + "\" has been reconciliated with the type \""
                        + planningPackagePattern.portfolioEntryPlanningPackageType.cssClass + " ("
                        + planningPackagePattern.portfolioEntryPlanningPackageType.id + ")\"");

                planningPackagePattern.save();

            }

            log.info("end reconciliate planning package patterns");

        } else {
            Logger.info("There is already at least one planning package type: do nothing");
        }

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
