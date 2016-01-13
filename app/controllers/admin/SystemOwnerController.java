/*! LICENSE
 *
 * Copyright (c) 2015, The Agile Factory SA and/or its affiliates. All rights
 * reserved.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package controllers.admin;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.ext.api.IExtensionDescriptor.IPluginDescriptor;
import framework.services.plugins.IPluginManagerService;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Table.ColumnDef.SorterType;
import models.framework_models.account.Principal;
import play.Configuration;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * A controller to be used by the system owner to display and modify the core
 * configuration of the system. This may imply ordering additional features,
 * raising a change request, etc.
 * 
 * @author Pierre-Yves Cloux
 */
@Restrict({ @Group(IMafConstants.ADMIN_SYSTEM_OWNER_PERMISSION) })
public class SystemOwnerController extends Controller {
    @Inject
    private IAccountManagerPlugin accountManagerPlugin;
    @Inject
    private IPluginManagerService pluginManagerService;
    @Inject
    private Configuration configuration;

    private static Logger.ALogger log = Logger.of(SystemOwnerController.class);
    /**
     * Table listing the various plugin definitions.
     */
    public static Table<PluginDefinitionTableObject> pluginDefinitionsTableTemplate = new Table<PluginDefinitionTableObject>() {
        {
            this.addColumn("name", "name", "object.plugin_definition.name.label", SorterType.NONE);
            this.addColumn("version", "version", "object.plugin_definition.description.label", SorterType.NONE);
            this.addColumn("isAvailable", "isAvailable", "object.plugin_definition.is_available.label", SorterType.NONE);
            this.setLineAction(new IColumnFormatter<PluginDefinitionTableObject>() {
                @Override
                public String apply(PluginDefinitionTableObject object, Object value) {
                    return routes.PluginManagerController.pluginDefinitionDetails(object.identifier).url();
                }

            });
            this.setIdFieldName("identifier");
        }
    };

    /**
     * Default constructor.
     */
    public SystemOwnerController() {
    }

    /**
     * Display the basic information page.
     * 
     * <ul>
     * <li>The sizing of the system</li>
     * <li>The remaining storage</li>
     * <li>The number of registered users</li>
     * <li>The available plugins</li>
     * </ul>
     * 
     * @return
     */
    public Result info() {
        // Find system owners
        List<String> systemOwners = new ArrayList<String>();
        List<Principal> systemOnwerPrincipals = Principal.getPrincipalsWithPermission(IMafConstants.ADMIN_SYSTEM_OWNER_PERMISSION);
        for (Principal principal : systemOnwerPrincipals) {
            IUserAccount userAccount;
            try {
                userAccount = getAccountManagerPlugin().getUserAccountFromUid(principal.uid);
                if (userAccount != null) {
                    systemOwners.add(String.format("%s %s (%s)", userAccount.getFirstName(), userAccount.getLastName(), userAccount.getMail()));
                }
            } catch (Exception e) {
                log.error("Unexpected error while looking for system owners", e);
            }
        }

        String authenticationMode = "Slave";
        // Check master mode for authentication
        if (getConfiguration().getBoolean("maf.ic_ldap_master")) {
            authenticationMode = "Master";
        }

        // Get the number of active users
        SystemInfo systemInfo = new SystemInfo(Principal.getActivePrincipalCount(), Principal.getRegisteredPrincipalCount(), systemOwners, "1.2", "Bronze",
                "unknown", authenticationMode);

        // List of available plugins
        List<PluginDefinitionTableObject> pluginDescriptions = new ArrayList<PluginDefinitionTableObject>();

        for (Pair<Boolean, IPluginDescriptor> pluginDefinitionRecord : getPluginManagerService().getAllPluginDescriptors().values()) {
            PluginDefinitionTableObject tableObject = new PluginDefinitionTableObject();
            IPluginDescriptor pluginDescriptor = pluginDefinitionRecord.getRight();
            tableObject.identifier = pluginDescriptor.getIdentifier();
            tableObject.name = Msg.get(pluginDescriptor.getName());
            tableObject.version = pluginDescriptor.getVersion();
            pluginDescriptions.add(tableObject);
        }
        return ok(views.html.admin.systemowner.info.render(systemInfo, pluginDefinitionsTableTemplate.fill(pluginDescriptions)));
    }

    /**
     * Get the account manager service.
     */
    private IAccountManagerPlugin getAccountManagerPlugin() {
        return accountManagerPlugin;
    }

    /**
     * Get the plugin manager service.
     */
    private IPluginManagerService getPluginManagerService() {
        return pluginManagerService;
    }

    /**
     * The object which holds some information about the system configuration.
     * 
     * @author Pierre-Yves Cloux
     */
    public static class SystemInfo {
        private int numberOfActiveUsers;
        private int numberOfRegisteredUsers;
        private List<String> systemOwners;
        private String bizDockVersion;
        private String systemSizing;
        private String remainingStorageSpace;
        private String authenticationMode;

        /**
         * Constructor.
         * 
         * @param numberOfActiveUsers
         *            number of active users
         * @param numberOfRegisteredUsers
         *            number of registered users
         * @param systemOwners
         *            the system owners
         * @param bizDockVersion
         *            the BizDock version
         * @param systemSizing
         *            the system sizing
         * @param remainingStorageSpace
         *            the remaining storage space
         * @param authenticationMode
         *            the authentication mode
         */
        public SystemInfo(int numberOfActiveUsers, int numberOfRegisteredUsers, List<String> systemOwners, String bizDockVersion, String systemSizing,
                String remainingStorageSpace, String authenticationMode) {
            super();
            this.numberOfActiveUsers = numberOfActiveUsers;
            this.numberOfRegisteredUsers = numberOfRegisteredUsers;
            this.systemOwners = systemOwners;
            this.bizDockVersion = bizDockVersion;
            this.systemSizing = systemSizing;
            this.remainingStorageSpace = remainingStorageSpace;
            this.authenticationMode = authenticationMode;
        }

        /**
         * Get the number of active users.
         */
        public int getNumberOfActiveUsers() {
            return numberOfActiveUsers;
        }

        /**
         * Get the number of registered users.
         */
        public int getNumberOfRegisteredUsers() {
            return numberOfRegisteredUsers;
        }

        /**
         * Get the system owners.
         */
        public List<String> getSystemOwners() {
            return systemOwners;
        }

        /**
         * Get the BizDock version.
         */
        public String getBizDockVersion() {
            return bizDockVersion;
        }

        /**
         * Get the system sizing.
         * 
         * @return
         */
        public String getSystemSizing() {
            return systemSizing;
        }

        /**
         * Get the remaining storage space.
         */
        public String getRemainingStorageSpace() {
            return remainingStorageSpace;
        }

        /**
         * Get the the authentication mode.
         */
        public String getAuthenticationMode() {
            return authenticationMode;
        }
    }

    /**
     * {@link Table} object to be used for displaying a list of plugin object
     * definitions.
     * 
     * @author Pierre-Yves Cloux
     */
    public static class PluginDefinitionTableObject {
        public String identifier;
        public String name;
        public String version;
        public String isAvailable;
    }

    /**
     * Get the Play configuration service.
     */
    private Configuration getConfiguration() {
        return configuration;
    }
}
