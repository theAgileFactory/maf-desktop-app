package services.database;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import constants.IMafConstants;
import framework.services.database.AbstractDatabaseDependencyServiceImpl;
import framework.services.database.IDatabaseDependencyService;
import models.framework_models.account.SystemLevelRoleType;
import models.framework_models.account.SystemPermission;
import models.framework_models.common.TextCustomAttributeValue;
import play.Configuration;
import play.Environment;
import play.Logger.ALogger;
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
     */
    @Inject
    public DatabaseDependencyServiceImpl(ApplicationLifecycle lifecycle, Environment environment, Configuration configuration, EbeanConfig ebeanConfig) {
        super(lifecycle, environment, configuration, ebeanConfig);
    }

    @Override
    public String getRelease() {
        return "7.0.1";
    }

    @Override
    public void patch(ALogger log) {

        /**
         * Delete planning package filter configuration for all users.
         */

        List<TextCustomAttributeValue> packageFilters = TextCustomAttributeValue.find.where().eq("deleted", false)
                .eq("customAttributeDefinition.deleted", false).eq("customAttributeDefinition.uuid", IMafConstants.PACKAGES_FILTER_STORAGE_PREFERENCE)
                .findList();

        for (TextCustomAttributeValue packageFilter : packageFilters) {
            log.info("delete TextCustomAttributeValue with id " + packageFilter.id);
            packageFilter.doDelete();
        }

        /**
         * Add PARTNER_SYNDICATION_PERMISSION to roles with permission
         * ADMIN_USER_ADMINISTRATION_PERMISSION.
         */

        List<SystemLevelRoleType> roles = SystemLevelRoleType.find.where().eq("deleted", false).eq("systemPermissions.deleted", false)
                .eq("systemPermissions.name", IMafConstants.ADMIN_USER_ADMINISTRATION_PERMISSION).findList();

        SystemPermission permission = SystemPermission.getSystemPermissionByName(IMafConstants.PARTNER_SYNDICATION_PERMISSION);

        for (SystemLevelRoleType role : roles) {
            log.info("role with name " + role.getName() + " has the permission " + IMafConstants.ADMIN_USER_ADMINISTRATION_PERMISSION);

            if (!SystemLevelRoleType.hasPermission(role.id, IMafConstants.PARTNER_SYNDICATION_PERMISSION)) {
                log.info("but hasn't the permission " + IMafConstants.PARTNER_SYNDICATION_PERMISSION + ": add it");
                if (role.systemPermissions == null) {
                    role.systemPermissions = new ArrayList<SystemPermission>();
                }
                role.systemPermissions.add(permission);
                role.save();
            } else {
                log.info("but has already the permission " + IMafConstants.PARTNER_SYNDICATION_PERMISSION + ": do nothing");
            }
        }
    }
}
