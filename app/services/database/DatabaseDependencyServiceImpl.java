package services.database;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;

import dao.governance.LifeCycleMilestoneDao;
import dao.governance.LifeCycleProcessDao;
import dao.pmo.PortfolioEntryDao;
import dao.pmo.StakeholderDao;
import framework.services.database.AbstractDatabaseDependencyServiceImpl;
import framework.services.database.IDatabaseDependencyService;
import framework.utils.MultiLanguagesString;
import models.framework_models.plugin.PluginDefinition;
import models.governance.LifeCycleMilestone;
import models.governance.LifeCycleMilestoneInstanceStatusType;
import models.governance.LifeCyclePhase;
import models.governance.LifeCycleProcess;
import models.pmo.PortfolioEntryType;
import models.pmo.StakeholderType;
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
        return "10.0.0";
    }

    @Override
    public void patch(ALogger log) {

        log.info("START PATCHER " + this.getRelease());

        // Create a Release type if not existing
        log.info("START RELEASE TYPE");
        if (PortfolioEntryDao.getPETypeReleaseAsList().size() == 0) {
            log.info("Create a portfolio entry type with isRelease = true ");

            try {
                Ebean.beginTransaction();

                String releaseTypeName = MultiLanguagesString.generateKey();
                String releaseTypeDescription = MultiLanguagesString.generateKey();

                this.addI18nKey(releaseTypeName, "Release", "en");
                this.addI18nKey(releaseTypeName, "Release", "fr");
                this.addI18nKey(releaseTypeName, "Release", "de");

                this.addI18nKey(releaseTypeDescription, "Distribution of a new or upgraded version of software", "en");
                this.addI18nKey(releaseTypeDescription, "Distribution d'une version nouvelle ou améliorée d'un logiciel", "fr");
                this.addI18nKey(releaseTypeDescription, "Verteilung eines neuen oder aktualisierten Version der Software", "de");

                PortfolioEntryType portfolioEntryType = new PortfolioEntryType();
                portfolioEntryType.description = releaseTypeDescription;
                portfolioEntryType.isRelease = true;
                portfolioEntryType.name = releaseTypeName;
                portfolioEntryType.selectable = true;
                portfolioEntryType.stakeholderTypes = new ArrayList<>();
                for (StakeholderType stakeholderType : StakeholderDao.getStakeholderTypeAsList()) {
                    portfolioEntryType.stakeholderTypes.add(stakeholderType);
                }
                portfolioEntryType.save();

                Ebean.commitTransaction();
                Ebean.endTransaction();

            } catch (Exception e) {

                Logger.error("Impossible to create the release type", e);

                Ebean.rollbackTransaction();
                Ebean.endTransaction();

            }

        } else {
            log.info("A portfolio entry type with isRelease = true is alreary existing");
        }
        log.info("END RELEASE TYPE");

        // Create a Release life cycle process if not existing
        log.info("START RELEASE LIFE CYCLE PROCESS");
        if (LifeCycleProcessDao.findLifeCycleProcess.where().eq("deleted", false).eq("isRelease", true).findRowCount() == 0) {
            log.info("Create a life cycle process with isRelease = true ");

            List<LifeCycleMilestoneInstanceStatusType> statusTypes = LifeCycleMilestoneDao.findLifeCycleMilestoneInstanceStatusType.where()
                    .eq("deleted", false).eq("isApproved", true).findList();
            // .getLCMilestoneInstanceStatusTypeByName("life_cycle_milestone_instance_status_type.name.approved");
            if (statusTypes == null || statusTypes.size() == 0) {
                log.warn("Impossible to find an approved status type: the process is not created");
            } else {

                try {
                    Ebean.beginTransaction();

                    String releaseProcessShortName = MultiLanguagesString.generateKey();
                    String releaseProcessName = MultiLanguagesString.generateKey();
                    String releaseProcessDescription = MultiLanguagesString.generateKey();

                    this.addI18nKey(releaseProcessShortName, "Release", "en");
                    this.addI18nKey(releaseProcessShortName, "Release", "fr");
                    this.addI18nKey(releaseProcessShortName, "Release", "de");

                    this.addI18nKey(releaseProcessName, "Release", "en");
                    this.addI18nKey(releaseProcessName, "Release", "fr");
                    this.addI18nKey(releaseProcessName, "Release", "de");

                    this.addI18nKey(releaseProcessDescription, "Default life cycle process for a release", "en");
                    this.addI18nKey(releaseProcessDescription, "Processus de gouvernance standard pour une release", "fr");
                    this.addI18nKey(releaseProcessDescription, "Grundeinstellung Lieferzyklus", "de");

                    LifeCycleProcess lifeCycleProcess = new LifeCycleProcess();
                    lifeCycleProcess.description = releaseProcessDescription;
                    lifeCycleProcess.isActive = true;
                    lifeCycleProcess.isRelease = true;
                    lifeCycleProcess.name = releaseProcessName;
                    lifeCycleProcess.shortName = releaseProcessShortName;
                    lifeCycleProcess.save();
                    log.info("Process created");

                    String startMilestoneShortName = MultiLanguagesString.generateKey();
                    String endMilestoneShortName = MultiLanguagesString.generateKey();
                    String startMilestoneName = MultiLanguagesString.generateKey();
                    String endMilestoneName = MultiLanguagesString.generateKey();

                    this.addI18nKey(startMilestoneShortName, "Start", "en");
                    this.addI18nKey(startMilestoneShortName, "Début", "fr");
                    this.addI18nKey(startMilestoneShortName, "Anfang", "de");

                    this.addI18nKey(endMilestoneShortName, "End", "en");
                    this.addI18nKey(endMilestoneShortName, "Fin", "fr");
                    this.addI18nKey(endMilestoneShortName, "Ende", "de");

                    this.addI18nKey(startMilestoneName, "Implementation start", "en");
                    this.addI18nKey(startMilestoneName, "Début de l'implémentation", "fr");
                    this.addI18nKey(startMilestoneName, "Umsetzung Anfang", "de");

                    this.addI18nKey(endMilestoneName, "Implementation end", "en");
                    this.addI18nKey(endMilestoneName, "Fin de l'implémentation", "fr");
                    this.addI18nKey(endMilestoneName, "Umsetzung Ende", "de");

                    LifeCycleMilestone startMilestone = new LifeCycleMilestone();
                    startMilestone.lifeCycleProcess = lifeCycleProcess;
                    startMilestone.defaultLifeCycleMilestoneInstanceStatusType = statusTypes.get(0);
                    startMilestone.isActive = true;
                    startMilestone.isReviewRequired = false;
                    startMilestone.name = startMilestoneName;
                    startMilestone.order = 1;
                    startMilestone.shortName = startMilestoneShortName;
                    startMilestone.type = LifeCycleMilestone.Type.IMPLEMENTATION_START_DATE;
                    startMilestone.save();
                    log.info("Start milestone created");

                    LifeCycleMilestone endMilestone = new LifeCycleMilestone();
                    endMilestone.lifeCycleProcess = lifeCycleProcess;
                    endMilestone.defaultLifeCycleMilestoneInstanceStatusType = statusTypes.get(0);
                    endMilestone.isActive = true;
                    endMilestone.isReviewRequired = false;
                    endMilestone.name = endMilestoneName;
                    endMilestone.order = 2;
                    endMilestone.shortName = endMilestoneShortName;
                    endMilestone.type = LifeCycleMilestone.Type.IMPLEMENTATION_END_DATE;
                    endMilestone.save();
                    log.info("End milestone created");

                    String phaseName = MultiLanguagesString.generateKey();

                    this.addI18nKey(phaseName, "Implementation", "en");
                    this.addI18nKey(phaseName, "Implémentation", "fr");
                    this.addI18nKey(phaseName, "Umsetzung", "de");

                    LifeCyclePhase phase = new LifeCyclePhase();
                    phase.endLifeCycleMilestone = endMilestone;
                    phase.gapDaysEnd = 0;
                    phase.gapDaysStart = 0;
                    phase.isRoadmapPhase = true;
                    phase.lifeCycleProcess = lifeCycleProcess;
                    phase.name = phaseName;
                    phase.order = 1;
                    phase.startLifeCycleMilestone = startMilestone;
                    phase.save();
                    log.info("Phase created");

                    Ebean.commitTransaction();
                    Ebean.endTransaction();

                } catch (Exception e) {

                    Logger.error("Impossible to create the release process", e);

                    Ebean.rollbackTransaction();
                    Ebean.endTransaction();

                }

            }

        } else {
            log.info("A life cycle process type with isRelease = true is alreary existing");
        }

        log.info("END RELEASE LIFE CYCLE PROCESS");

        // add plugin definitions if not existing
        log.info("START PLUGIN DEFINITIONS");

        if (PluginDefinition.getPluginDefinitionFromIdentifier("genint1") == null) {
            log.info("Create the plugin definition genint1");
            try {
                Ebean.beginTransaction();

                PluginDefinition genint1 = new PluginDefinition();
                genint1.clazz = "services.plugins.system.genint1.GenericExternalIntegrationPluginRunner";
                genint1.identifier = "genint1";
                genint1.isAvailable = false;
                genint1.save();

                Ebean.commitTransaction();
                Ebean.endTransaction();

            } catch (Exception e) {

                Logger.error("Impossible to create the genint1 plugin definition", e);

                Ebean.rollbackTransaction();
                Ebean.endTransaction();

            }

        } else {
            log.info("The plugin definition genint1 is already existing");
        }

        if (PluginDefinition.getPluginDefinitionFromIdentifier("schedule1") == null) {
            log.info("Create the plugin definition schedule1");
            try {
                Ebean.beginTransaction();

                PluginDefinition schedule1 = new PluginDefinition();
                schedule1.clazz = "services.plugins.system.schedule1.HookScriptSchedulerPluginRunner";
                schedule1.identifier = "schedule1";
                schedule1.isAvailable = false;
                schedule1.save();

                Ebean.commitTransaction();
                Ebean.endTransaction();

            } catch (Exception e) {

                Logger.error("Impossible to create the schedule1 plugin definition", e);

                Ebean.rollbackTransaction();
                Ebean.endTransaction();

            }

        } else {
            log.info("The plugin definition schedule1 is already existing");
        }

        PluginDefinition sharepointPlugin = PluginDefinition.getPluginDefinitionFromIdentifier("sharp1");
        if (sharepointPlugin != null) {
            log.info("Delete the sharepoint plugin definition.");
            try {
                Ebean.beginTransaction();
                sharepointPlugin.delete();
                Ebean.commitTransaction();
                Ebean.endTransaction();

            } catch (Exception e) {

                Logger.error("Impossible to delete the sharp1 plugin definition", e);

                Ebean.rollbackTransaction();
                Ebean.endTransaction();

            }
        } else {
            log.info("The sharepoint plugin definition is already not existing.");
        }

        log.info("END PLUGIN DEFINITIONS");

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
