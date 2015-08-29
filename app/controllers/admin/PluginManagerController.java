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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import controllers.ControllersUtils;
import framework.commons.IFrameworkConstants.Syntax;
import framework.commons.message.EventMessage;
import framework.commons.message.EventMessage.MessageType;
import framework.services.account.AccountManagementException;
import framework.services.plugins.IEventBroadcastingService;
import framework.services.plugins.IPluginManagerService;
import framework.services.plugins.IPluginManagerService.IPluginInfo;
import framework.services.plugins.IPluginManagerService.PluginStatus;
import framework.services.plugins.api.IPluginActionDescriptor;
import framework.services.plugins.api.IPluginConfigurationBlockDescriptor;
import framework.services.plugins.api.IPluginConfigurationBlockDescriptor.ConfigurationBlockEditionType;
import framework.services.plugins.api.IStaticPluginRunnerDescriptor;
import framework.services.plugins.api.PluginException;
import framework.utils.FilterConfig;
import framework.utils.IColumnFormatter;
import framework.utils.Menu.ClickableMenuItem;
import framework.utils.Menu.HeaderMenuItem;
import framework.utils.Msg;
import framework.utils.Pagination;
import framework.utils.SideBar;
import framework.utils.Table;
import framework.utils.Table.ColumnDef.SorterType;
import framework.utils.Utilities;
import models.framework_models.plugin.PluginConfiguration;
import models.framework_models.plugin.PluginConfigurationBlock;
import models.framework_models.plugin.PluginDefinition;
import models.framework_models.plugin.PluginIdentificationLink;
import models.framework_models.plugin.PluginLog;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints.Required;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;
import security.ISecurityService;

/**
 * The GUI for managing the plugins.
 * 
 * That is to say :
 * <ul>
 * <li>Displaying the running plugins</li>
 * <li>Creating a new plugin instance</li>
 * <li>Starting/stopping a plugin instance</li>
 * <li>Displaying the log</li>
 * <li>Flushing the logs</li>
 * <li>Changing the plugin configuration</li>
 * </ul>
 * 
 * @author Pierre-Yves Cloux
 */
public class PluginManagerController extends Controller {
    @Inject
    private IPluginManagerService pluginManagerService;
    @Inject
    private IEventBroadcastingService eventBroadcastingService;
    @Inject
    private ISecurityService securityService;

    private static Logger.ALogger log = Logger.of(PluginManagerController.class);

    /**
     * Form managing the registration of a plugin.
     */
    public static Form<PluginRegistrationFormObject> registrationFormTemplate = Form.form(PluginRegistrationFormObject.class);

    /**
     * Form managing the edition of a configuration block.
     */
    public static Form<PluginConfigurationBlockObject> pluginConfigurationBlockFormTemplate = Form.form(PluginConfigurationBlockObject.class);

    /**
     * Table displaying the various available configuration blocks for a plugin.
     */
    public static Table<PluginConfigurationBlockObject> configurationBlocksTableTemplate = new Table<PluginConfigurationBlockObject>() {
        {

            this.addColumn("name", "pluginConfigurationBlockIdentifier", "object.plugin_configuration_block.name.label", SorterType.NONE);
            this.setJavaColumnFormatter("name", new IColumnFormatter<PluginConfigurationBlockObject>() {
                @Override
                public String apply(PluginConfigurationBlockObject object, Object value) {
                    return Msg.get(object.getPluginConfigurationBlockDescriptor().getName());
                }
            });

            this.addColumn("description", "pluginConfigurationBlockIdentifier", "object.plugin_configuration_block.description.label", SorterType.NONE);
            this.setJavaColumnFormatter("description", new IColumnFormatter<PluginConfigurationBlockObject>() {
                @Override
                public String apply(PluginConfigurationBlockObject object, Object value) {
                    return Msg.get(object.getPluginConfigurationBlockDescriptor().getDescription());
                }
            });

            this.addColumn("type", "pluginConfigurationBlockIdentifier", "", SorterType.NONE);
            this.setJavaColumnFormatter("type", new IColumnFormatter<PluginConfigurationBlockObject>() {
                @Override
                public String apply(PluginConfigurationBlockObject object, Object value) {
                    switch (object.getPluginConfigurationBlockDescriptor().getEditionType()) {
                    case FILE:
                        return IMafConstants.BOOTSTRAP_FILETYPE_BINARY;
                    case XML:
                        return IMafConstants.BOOTSTRAP_FILETYPE_XML;
                    case PROPERTIES:
                        return IMafConstants.BOOTSTRAP_FILETYPE_PROPERTIES;
                    case JAVASCRIPT:
                        return IMafConstants.BOOTSTRAP_FILETYPE_JAVASCRIPT;
                    default:
                        return IMafConstants.BOOTSTRAP_FILETYPE_TXT;
                    }
                }
            });
            setColumnCssClass("type", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("type", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

            this.setLineAction(new IColumnFormatter<PluginConfigurationBlockObject>() {
                @Override
                public String apply(PluginConfigurationBlockObject object, Object value) {
                    return routes.PluginManagerController
                            .editConfigurationBlock(object.pluginConfigurationId, object.getPluginConfigurationBlockDescriptor().getIdentifier()).url();
                }
            });

            this.setIdFieldName("pluginConfigurationBlockIdentifier");
        }
    };

    /**
     * Table displaying the logs for a plugin.
     */
    public static Table<PluginLog> pluginLogsTableTemplate = new Table<PluginLog>() {
        {
            this.addColumn("lastUpdate", "lastUpdate", "object.plugin_log.last_update.label", SorterType.NONE);
            this.addColumn("isError", "isError", "object.plugin_log.is_error.label", SorterType.NONE);
            this.setJavaColumnFormatter("isError", new IColumnFormatter<PluginLog>() {
                @Override
                public String apply(PluginLog object, Object value) {
                    if (object.isError) {
                        return String.format(IMafConstants.LABEL_DANGER_FORMAT, "ERROR");
                    }
                    return String.format(IMafConstants.LABEL_INFO_FORMAT, "INFO");
                }
            });
            this.addColumn("event", "event", "object.plugin_log.event.label", SorterType.NONE);
            this.addColumn("logMessage", "logMessage", "object.plugin_log.log_message.label", SorterType.NONE, false);
            this.addColumn("transactionId", "transactionId", "object.plugin_log.transaction_id.label", SorterType.NONE);
            this.addColumn("dataType", "dataType", "object.plugin_log.data_type.label", SorterType.NONE);
            this.addColumn("internalId", "internalId", "object.plugin_log.internal_id.label", SorterType.NONE);
            this.addColumn("externalId", "externalId", "object.plugin_log.external_id.label", SorterType.NONE);
            this.setIdFieldName("id");
        }
    };

    /**
     * Filter config for the managing the search or order features on logs.
     */
    private static FilterConfig<PluginLog> pluginLogsFilterConfig = new FilterConfig<PluginLog>() {
        {
            addColumnConfiguration("lastUpdate", "lastUpdate", "object.plugin_log.last_update.label", new TextFieldFilterComponent("*"), true, false,
                    SortStatusType.DESC);
            addColumnConfiguration("isError", "isError", "object.plugin_log.is_error.label", new CheckboxFilterComponent(true), true, true,
                    SortStatusType.UNSORTED);
            addColumnConfiguration("event", "event", "object.plugin_log.event.label", new TextFieldFilterComponent("*"), true, false, SortStatusType.UNSORTED);
            addColumnConfiguration("logMessage", "logMessage", "object.plugin_log.log_message.label", new TextFieldFilterComponent("*"), true, false,
                    SortStatusType.UNSORTED);
            addColumnConfiguration("transactionId", "transactionId", "object.plugin_log.transaction_id.label", new TextFieldFilterComponent("*"), false, false,
                    SortStatusType.UNSORTED);
            addColumnConfiguration("dataType", "dataType", "object.plugin_log.data_type.label", new TextFieldFilterComponent("*"), false, false,
                    SortStatusType.UNSORTED);
            addColumnConfiguration("internalId", "internalId", "object.plugin_log.internal_id.label", new TextFieldFilterComponent("*"), false, false,
                    SortStatusType.UNSORTED);
            addColumnConfiguration("externalId", "externalId", "object.plugin_log.external_id.label", new TextFieldFilterComponent("*"), false, false,
                    SortStatusType.UNSORTED);
        }
    };

    /**
     * Table listing the various plugin instances (PluginConfiguration).
     */
    public static Table<PluginConfigurationDescriptionTableObject> pluginConfigurationsTableTemplate = new Table<PluginManagerController.PluginConfigurationDescriptionTableObject>() {
        {
            this.addColumn("id", "id", "object.plugin_configuration.id.label", SorterType.NONE);
            this.addColumn("name", "name", "object.plugin_configuration.name.label", SorterType.NONE);
            this.addColumn("definitionName", "definitionName", "object.plugin_definition.name.label", SorterType.NONE);
            this.addColumn("definitionVersion", "definitionVersion", "object.plugin_definition.version.label", SorterType.NONE);
            this.addColumn("status", "status", "object.plugin_configuration.status.label", SorterType.NONE);
            this.setJavaColumnFormatter("status", new IColumnFormatter<PluginConfigurationDescriptionTableObject>() {
                @Override
                public String apply(PluginConfigurationDescriptionTableObject object, Object value) {
                    return getHtmlFromPluginStatus(object.status);
                }
            });
            this.setLineAction(new IColumnFormatter<PluginConfigurationDescriptionTableObject>() {
                @Override
                public String apply(PluginConfigurationDescriptionTableObject object, Object value) {
                    return routes.PluginManagerController.pluginConfigurationDetails(object.id).url();
                }

            });
            this.setIdFieldName("id");
        }
    };

    /**
     * Default controller.
     */
    public PluginManagerController() {
    }

    /**
     * Distribute the plugin image
     * 
     * @return
     */
    @Restrict({ @Group(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION) })
    public Promise<Result> image(String identifier, boolean isBigImage) {

        InputStream inStream = null;
        if (isBigImage) {
            inStream = getPluginManagerService().getPluginBigImageSrc(identifier);
        } else {
            inStream = getPluginManagerService().getPluginSmallImageSrc(identifier);
        }
        if (inStream == null) {
            if (log.isDebugEnabled()) {
                log.debug("Image [" + (isBigImage ? "BIG" : "SMALL") + "] not found for plugin " + identifier);
            }
            return Promise.promise(() -> notFound());
        }
        if (log.isDebugEnabled()) {
            log.debug("Image [" + (isBigImage ? "BIG" : "SMALL") + "] was found for plugin " + identifier);
        }
        final InputStream finalInStream = inStream;
        return Promise.promise(() -> ok(finalInStream));
    }

    /**
     * Display the overview display for the plugins.<br/>
     * It consists in a list of plugins with their status (started/stopped).
     * @throws AccountManagementException 
     */
    @Restrict({ @Group(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION), @Group(IMafConstants.API_MANAGER_PERMISSION),
            @Group(IMafConstants.PARTNER_SYNDICATION_PERMISSION) })
    public Result index() throws AccountManagementException {

        if (!getSecurityService().currentUserHasRole(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION)) {
            if (getSecurityService().currentUserHasRole(IMafConstants.API_MANAGER_PERMISSION)) {
                return redirect(controllers.admin.routes.ApiManagerController.index());
            } else {
                return redirect(controllers.admin.routes.DataSyndicationController.viewMasterAgreements());
            }
        }

        List<PluginConfigurationDescriptionTableObject> pluginConfigurations = new ArrayList<PluginConfigurationDescriptionTableObject>();

        Map<Long, IPluginInfo> registeredPlugins = getPluginManagerService().getRegisteredPluginDescriptors();
        for (Long pluginConfigurationId : registeredPlugins.keySet()) {
            PluginConfigurationDescriptionTableObject tableObject = new PluginConfigurationDescriptionTableObject();
            tableObject.id = pluginConfigurationId;
            IPluginInfo pluginInfo = registeredPlugins.get(pluginConfigurationId);
            tableObject.name = PluginConfiguration.getAvailablePluginById(pluginConfigurationId).name;
            tableObject.definitionIdentifier = pluginInfo.getStaticDescriptor().getPluginDefinitionIdentifier();
            tableObject.definitionName = Msg.get(pluginInfo.getStaticDescriptor().getName());
            tableObject.definitionDescription = Msg.get(pluginInfo.getStaticDescriptor().getDescription());
            tableObject.definitionProviderUrl = pluginInfo.getStaticDescriptor().getVendorUrl().toString();
            tableObject.definitionVersion = pluginInfo.getStaticDescriptor().getVersion();
            tableObject.status = pluginInfo.getPluginStatus();
            pluginConfigurations.add(tableObject);
        }
        return ok(views.html.admin.plugin.pluginmanager_index.render(pluginConfigurationsTableTemplate.fill(pluginConfigurations)));
    }

    /**
     * Display the screen which allow to create or delete a plugin
     * configuration.<br/>
     * Which is in the {@link IPluginManagerService} consists in a registration
     * at non registration.
     */
    @Restrict({ @Group(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION) })
    public Result registration() {

        // Identify already registered plugin definitions
        Set<String> registeredDefinitions = getAlreadyRegisteredPlugins(getPluginManagerService());

        // Select the definitions to be displayed (remove the mono instance
        // plugins which are already registered)
        Map<Pair<String, Boolean>, IStaticPluginRunnerDescriptor> pluginDescriptors = getPluginManagerService().getAllPluginDescriptors();
        List<Pair<Boolean, IStaticPluginRunnerDescriptor>> plugins = new ArrayList<>();
        for (Pair<String, Boolean> key : pluginDescriptors.keySet()) {
            IStaticPluginRunnerDescriptor pluginDescriptor = pluginDescriptors.get(key);
            if (!(registeredDefinitions.contains(pluginDescriptor.getPluginDefinitionIdentifier()) && !pluginDescriptor.multiInstanceAllowed())) {
                plugins.add(Pair.of(key.getRight(), pluginDescriptor));
            }
        }

        return ok(views.html.admin.plugin.pluginmanager_registration.render(plugins));
    }

    /**
     * Display the form for registering a plugin (= creating a new plugin
     * configuration and registering it).
     * 
     * @param pluginDefinitionIdentifier
     *            a plugin definition identifier (the template for the plugin)
     */
    @Restrict({ @Group(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION) })
    public Result displayRegistrationForm(String pluginDefinitionIdentifier) {
        PluginRegistrationFormObject pluginRegistrationFormObject = new PluginRegistrationFormObject();

        IStaticPluginRunnerDescriptor pluginRunnerDescriptor = getPluginManagerService().getAvailablePluginDescriptor(pluginDefinitionIdentifier);
        if (pluginRunnerDescriptor == null || (!pluginRunnerDescriptor.multiInstanceAllowed()
                && getAlreadyRegisteredPlugins(getPluginManagerService()).contains(pluginDefinitionIdentifier))) {
            return badRequest();
        }
        String definitionName = Msg.get(pluginRunnerDescriptor.getName());
        pluginRegistrationFormObject.definitionName = definitionName;
        pluginRegistrationFormObject.identifier = pluginDefinitionIdentifier;
        pluginRegistrationFormObject.name = definitionName + " " + String.format("%1$td/%1$tm/%1$tY", new Date());
        return ok(views.html.admin.plugin.pluginmanager_registration_form.render(registrationFormTemplate.fill(pluginRegistrationFormObject)));
    }

    /**
     * Create the {@link PluginConfiguration} instance and register the plugin.
     */
    @Restrict({ @Group(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION) })
    public Result registerPlugin() {
        Form<PluginRegistrationFormObject> boundForm = registrationFormTemplate.bindFromRequest();
        if (boundForm.hasErrors()) {
            return ok(views.html.admin.plugin.pluginmanager_registration_form.render(boundForm));
        }

        // Create the configuration record
        PluginRegistrationFormObject pluginRegistrationFormObject = boundForm.get();
        PluginConfiguration pluginConfiguration = new PluginConfiguration();
        pluginConfiguration.name = pluginRegistrationFormObject.name;
        pluginConfiguration.isAutostart = true;
        pluginConfiguration.pluginDefinition = PluginDefinition.getAvailablePluginDefinitionFromIdentifier(pluginRegistrationFormObject.identifier);

        if (pluginConfiguration.pluginDefinition == null) {
            Utilities.sendSuccessFlashMessage(Msg.get("admin.plugin_manager.configuration.new.success", pluginConfiguration.name));
            return redirect(routes.PluginManagerController.registration());
        }

        pluginConfiguration.save();

        // Trigger the registration

        try {
            getPluginManagerService().registerPlugin(pluginConfiguration.id);
            Utilities.sendSuccessFlashMessage(Msg.get("admin.plugin_manager.configuration.new.success", pluginConfiguration.name));
        } catch (PluginException e) {
            Utilities.sendErrorFlashMessage(Msg.get("admin.plugin_manager.configuration.new.error", pluginConfiguration.name));
            log.error("Failed to register manually the plugin in the manager controller", e);
        }

        return redirect(routes.PluginManagerController.index());
    }

    /**
     * Delete the plugin instance and unregister it from the manager.
     * 
     * @param pluginConfigurationId
     *            the plugin configuration id
     */
    @Restrict({ @Group(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION) })
    public Result unregisterPlugin(Long pluginConfigurationId) {

        PluginConfiguration configuration = PluginConfiguration.getPluginById(pluginConfigurationId);

        if (configuration == null) {
            return badRequest();
        }

        Ebean.beginTransaction();

        try {

            // due to an issue with play, the children are not removed with
            // cascade property, they should be manually removed.
            for (PluginIdentificationLink pluginIdentificationLink : configuration.pluginIdentificationLinks) {
                for (PluginIdentificationLink child : pluginIdentificationLink.children) {
                    child.delete();
                }
                if (pluginIdentificationLink.parent == null) {
                    pluginIdentificationLink.delete();
                }
            }

            // Delete the associated configuration
            configuration.delete();

            // Unregister the plugin

            getPluginManagerService().unregisterPlugin(pluginConfigurationId);

            Utilities.sendSuccessFlashMessage(Msg.get("admin.plugin_manager.configuration.delete.success", configuration.name));

            Ebean.commitTransaction();

        } catch (Exception e) {

            Ebean.rollbackTransaction();

            Utilities.sendErrorFlashMessage(Msg.get("admin.plugin_manager.configuration.delete.error", configuration.name));
            log.error("Fail to unregister a plugin configuration manually", e);
        }
        return redirect(routes.PluginManagerController.index());
    }

    /**
     * Display the details of the plugin definition.
     * 
     * @param pluginDefinitionIdentifier
     *            a plugin definition identifier
     * @return
     */
    @Restrict({ @Group(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION) })
    public Result pluginDefinitionDetails(String pluginDefinitionIdentifier) {

        IStaticPluginRunnerDescriptor pluginRunnerDescriptor = getPluginManagerService().getPluginDescriptor(pluginDefinitionIdentifier);
        if (pluginRunnerDescriptor == null) {
            Utilities.sendErrorFlashMessage(Msg.get("admin.plugin_manager.definition.view.not_exists", pluginDefinitionIdentifier));
            return redirect(routes.PluginManagerController.registration());
        }
        return ok(views.html.admin.plugin.pluginmanager_definition_details.render(pluginRunnerDescriptor,
                getPluginManagerService().isPluginAvailable(pluginRunnerDescriptor.getPluginDefinitionIdentifier())));
    }

    /**
     * Display the detailed view for a plugin.<br/>
     * This detailed view contains the description of the plugin, the status,
     * the type of Data which the plugin is supporting, the logs, the
     * configuration blocks, etc.
     * 
     * @param pluginConfigurationId
     *            the Id of the {@link PluginConfiguration} object
     * @return
     */
    @Restrict({ @Group(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION) })
    public Result pluginConfigurationDetails(Long pluginConfigurationId) {

        IPluginInfo pluginInfo = getPluginManagerService().getRegisteredPluginDescriptors().get(pluginConfigurationId);

        if (pluginInfo == null) {
            Utilities.sendErrorFlashMessage(Msg.get("admin.plugin_manager.configuration.view.not_exists", pluginConfigurationId));
            return redirect(routes.PluginManagerController.index());
        }

        // Plugin config
        PluginConfiguration configuration = PluginConfiguration.getAvailablePluginById(pluginConfigurationId);

        // Plugin configuration block
        List<PluginConfigurationBlockObject> configurationBlocks = new ArrayList<PluginConfigurationBlockObject>();
        if (pluginInfo.getStaticDescriptor().getConfigurationBlockDescriptors() != null) {
            for (IPluginConfigurationBlockDescriptor pluginConfigurationBlockDescriptor : pluginInfo.getStaticDescriptor().getConfigurationBlockDescriptors()
                    .values()) {
                configurationBlocks.add(new PluginConfigurationBlockObject(pluginConfigurationBlockDescriptor, pluginConfigurationId));
            }
        }

        // Plugin logs
        ExpressionList<PluginLog> pluginLogExpressionList = pluginLogsFilterConfig
                .updateWithSearchExpression(PluginLog.getAllPluginLogsForPluginConfigurationId(pluginConfigurationId));
        pluginLogsFilterConfig.updateWithSortExpression(pluginLogExpressionList);
        Pagination<PluginLog> pluginLogsPagination = new Pagination<PluginLog>(pluginLogExpressionList);
        pluginLogsPagination.setCurrentPage(pluginLogsFilterConfig.getCurrentPage());
        Table<PluginLog> pluginLogsTable = pluginLogsTableTemplate.fill(pluginLogsPagination.getListOfObjects(), pluginLogsFilterConfig.getColumnsToHide());

        return ok(views.html.admin.plugin.pluginmanager_configuration_details.render(configuration.name, pluginConfigurationId, pluginInfo,
                configurationBlocksTableTemplate.fill(configurationBlocks), pluginLogsPagination, pluginLogsTable, pluginLogsFilterConfig));
    }

    /**
     * Dynamic management of the table.
     * 
     * @param pluginConfigurationId
     *            a plugin configuration id
     * @return
     */
    @Restrict({ @Group(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION) })
    public Result filterPluginLogs(Long pluginConfigurationId) {

        JsonNode json = request().body().asJson();

        try {

            FilterConfig<PluginLog> filledFilterConfig = pluginLogsFilterConfig.parseResponse(json);

            ExpressionList<PluginLog> pluginLogExpressionList = filledFilterConfig
                    .updateWithSearchExpression(PluginLog.getAllPluginLogsForPluginConfigurationId(pluginConfigurationId));

            filledFilterConfig.updateWithSortExpression(pluginLogExpressionList);

            Pagination<PluginLog> pagination = new Pagination<PluginLog>(pluginLogExpressionList);
            pagination.setCurrentPage(filledFilterConfig.getCurrentPage());

            Table<PluginLog> table = pluginLogsTableTemplate.fill(pagination.getListOfObjects(), filledFilterConfig.getColumnsToHide());

            return ok(views.html.framework_views.parts.table.dynamic_tableview.render(table, pagination));

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Start the specified plugin.
     * 
     * @param pluginConfigurationId
     *            the Id of the {@link PluginConfiguration} object
     * @return
     */
    @Restrict({ @Group(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION) })
    public Result startPlugin(Long pluginConfigurationId) {
        PluginConfiguration configuration = PluginConfiguration.getAvailablePluginById(pluginConfigurationId);
        if (configuration == null) {
            return badRequest();
        }

        try {
            getPluginManagerService().startPlugin(pluginConfigurationId);
            Utilities.sendSuccessFlashMessage(Msg.get("admin.plugin_manager.configuration.view.panel.admin.start.success", configuration.name));
        } catch (PluginException e) {
            Utilities.sendSuccessFlashMessage(Msg.get("admin.plugin_manager.configuration.view.panel.admin.start.error", configuration.name));
        }
        return redirect(routes.PluginManagerController.pluginConfigurationDetails(pluginConfigurationId));
    }

    /**
     * Stop the specified plugin.
     * 
     * @param pluginConfigurationId
     *            the Id of the {@link PluginConfiguration} object
     * @return
     */
    @Restrict({ @Group(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION) })
    public Result stopPlugin(Long pluginConfigurationId) {
        PluginConfiguration configuration = PluginConfiguration.getAvailablePluginById(pluginConfigurationId);
        if (configuration == null) {
            return badRequest();
        }

        getPluginManagerService().stopPlugin(pluginConfigurationId);
        Utilities.sendSuccessFlashMessage(Msg.get("admin.plugin_manager.configuration.view.panel.admin.stop.success", configuration.name));
        return redirect(routes.PluginManagerController.pluginConfigurationDetails(pluginConfigurationId));
    }

    /**
     * Flush the logs which are associated with this plugin.
     * 
     * @param pluginConfigurationId
     *            the Id of the {@link PluginConfiguration} object
     * @return
     */
    @Restrict({ @Group(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION) })
    public Result flushLogs(Long pluginConfigurationId) {
        PluginLog.flushPluginLog(pluginConfigurationId);
        Utilities.sendSuccessFlashMessage(Msg.get("admin.plugin_manager.configuration.view.panel.log.flush.success"));
        return redirect(routes.PluginManagerController.pluginConfigurationDetails(pluginConfigurationId));
    }

    /**
     * Update the specified {@link PluginConfigurationBlock} for the specified
     * {@link PluginConfiguration}.
     * 
     * @param pluginConfigurationId
     *            the plugin configuration id
     * @param pluginConfigurationBlockIdentifier
     *            the plugin configuration block identifier
     */
    @Restrict({ @Group(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION) })
    public Result editConfigurationBlock(Long pluginConfigurationId, String pluginConfigurationBlockIdentifier) {
        IPluginConfigurationBlockDescriptor pluginConfigurationBlockDescriptor = getPluginConfigurationBlockDescriptor(pluginConfigurationId,
                pluginConfigurationBlockIdentifier);
        if (pluginConfigurationBlockDescriptor == null) {
            return redirect(routes.PluginManagerController.index());
        }

        PluginConfigurationBlockObject pluginConfigurationBlockObject = new PluginConfigurationBlockObject();
        PluginConfigurationBlock pluginConfigurationBlock = PluginConfigurationBlock.getPluginConfigurationBlockFromIdentifier(pluginConfigurationId,
                pluginConfigurationBlockIdentifier);
        if (pluginConfigurationBlock != null) {
            pluginConfigurationBlockObject.value = new String(pluginConfigurationBlock.configuration);
        } else {
            pluginConfigurationBlockObject.value = new String(pluginConfigurationBlockDescriptor.getDefaultValue());
        }
        return ok(views.html.admin.plugin.pluginmanager_configblock_edit.render(pluginConfigurationId, pluginConfigurationBlockDescriptor,
                pluginConfigurationBlockFormTemplate.fill(pluginConfigurationBlockObject),
                getSyntaxFromConfigurationBlockEditionType(pluginConfigurationBlockDescriptor.getEditionType())));
    }

    /**
     * Return the configuration block descriptor associated with the specified
     * plugin id and the specified identifier.
     * 
     * @param pluginConfigurationId
     *            a plugin id
     * @param pluginConfigurationBlockIdentifier
     *            a configration block identifier
     * @return
     */
    private IPluginConfigurationBlockDescriptor getPluginConfigurationBlockDescriptor(Long pluginConfigurationId, String pluginConfigurationBlockIdentifier) {

        IPluginInfo pluginInfo = getPluginManagerService().getRegisteredPluginDescriptors().get(pluginConfigurationId);
        if (pluginInfo == null || pluginInfo.getStaticDescriptor().getConfigurationBlockDescriptors() == null
                || !pluginInfo.getStaticDescriptor().getConfigurationBlockDescriptors().containsKey(pluginConfigurationBlockIdentifier)) {
            return null;
        }
        IPluginConfigurationBlockDescriptor pluginConfigurationBlockDescriptor = pluginInfo.getStaticDescriptor().getConfigurationBlockDescriptors()
                .get(pluginConfigurationBlockIdentifier);
        return pluginConfigurationBlockDescriptor;
    }

    /**
     * Return a {@link Syntax} from the
     * {@link IPluginConfigurationBlockDescriptor} edition type.
     * 
     * @param configurationBlockEditionType
     *            an edition type
     * @return a syntax
     */
    private Syntax getSyntaxFromConfigurationBlockEditionType(ConfigurationBlockEditionType configurationBlockEditionType) {
        switch (configurationBlockEditionType) {
        case XML:
            return Syntax.XML;
        case PROPERTIES:
            return Syntax.PROPERTIES;
        case JAVASCRIPT:
            return Syntax.JAVASCRIPT;
        case VELOCITY:
            return Syntax.VELOCITY;
        default:
            return null;
        }
    }

    /**
     * Update the specified {@link PluginConfigurationBlock} for the specified
     * {@link PluginConfiguration}.
     * 
     * @param pluginConfigurationId
     *            the plugin configuration id
     * @param pluginConfigurationBlockIdentifier
     *            the plugin configuration block identifier
     */
    @Restrict({ @Group(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION) })
    public Result updateConfigurationBlock(Long pluginConfigurationId, String pluginConfigurationBlockIdentifier) {
        IPluginConfigurationBlockDescriptor pluginConfigurationBlockDescriptor = getPluginConfigurationBlockDescriptor(pluginConfigurationId,
                pluginConfigurationBlockIdentifier);
        if (pluginConfigurationBlockDescriptor == null) {
            return redirect(routes.PluginManagerController.index());
        }

        Form<PluginConfigurationBlockObject> boundForm = pluginConfigurationBlockFormTemplate.bindFromRequest();
        if (boundForm.hasErrors()) {
            return ok(views.html.admin.plugin.pluginmanager_configblock_edit.render(pluginConfigurationId, pluginConfigurationBlockDescriptor, boundForm,
                    getSyntaxFromConfigurationBlockEditionType(pluginConfigurationBlockDescriptor.getEditionType())));
        }

        // Update the configuration block
        PluginConfigurationBlockObject pluginConfigurationBlockObject = boundForm.get();
        PluginConfigurationBlock pluginConfigurationBlock = PluginConfigurationBlock.getPluginConfigurationBlockFromIdentifier(pluginConfigurationId,
                pluginConfigurationBlockIdentifier);
        if (pluginConfigurationBlock == null) {
            pluginConfigurationBlock = new PluginConfigurationBlock();
            pluginConfigurationBlock.configurationType = pluginConfigurationBlockDescriptor.getEditionType().name();
            pluginConfigurationBlock.identifier = pluginConfigurationBlockIdentifier;
            pluginConfigurationBlock.pluginConfiguration = PluginConfiguration.getPluginById(pluginConfigurationId);
        }
        pluginConfigurationBlock.version = pluginConfigurationBlockDescriptor.getVersion();
        pluginConfigurationBlock.configuration = pluginConfigurationBlockObject.value != null ? pluginConfigurationBlockObject.value.getBytes() : null;
        pluginConfigurationBlock.save();

        Utilities.sendSuccessFlashMessage(
                Msg.get("admin.plugin_manager.configuration_block.edit.success", Msg.get(pluginConfigurationBlockDescriptor.getName())));

        return redirect(routes.PluginManagerController.pluginConfigurationDetails(pluginConfigurationId));
    }

    /**
     * Return the default value for the plugin configuation block as "text".
     * <br/>
     * Here are the parameters for this method:
     * <ul>
     * <li>pluginConfigurationId : the plugin configuration id</li>
     * <li>pluginConfigurationBlockIdentifier : the identifier of the plugin
     * configuration block</li>
     * </ul>
     * 
     * @param pluginConfigurationId
     *            the plugin configuration id
     * @param pluginConfigurationBlockIdentifier
     *            the plugin configuration block identifier
     */
    @Restrict({ @Group(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION) })
    public Result getDefaultConfigurationBlockValue(Long pluginConfigurationId, String pluginConfigurationBlockIdentifier) {
        IPluginConfigurationBlockDescriptor pluginConfigurationBlockDescriptor = getPluginConfigurationBlockDescriptor(pluginConfigurationId,
                pluginConfigurationBlockIdentifier);
        if (pluginConfigurationBlockDescriptor == null) {
            return badRequest();
        }
        return ok(new String(pluginConfigurationBlockDescriptor.getDefaultValue()));
    }

    /**
     * Post an admin action to the plugin.
     * 
     * @param pluginConfigurationId
     *            a plugin
     * @param pluginActionIdentifier
     *            the action identifier
     */
    @Restrict({ @Group(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION) })
    public Result postAdminActionToPlugin(Long pluginConfigurationId, String pluginActionIdentifier) {
        try {

            IPluginInfo pluginInfo = getPluginManagerService().getRegisteredPluginDescriptors().get(pluginConfigurationId);
            if (pluginInfo == null || !pluginInfo.getStaticDescriptor().getActionDescriptors().containsKey(pluginActionIdentifier)) {
                return badRequest();
            }
            IPluginActionDescriptor pluginActionDescriptor = pluginInfo.getStaticDescriptor().getActionDescriptors().get(pluginActionIdentifier);
            EventMessage eventMessage = new EventMessage();
            eventMessage.setPluginConfigurationId(pluginConfigurationId);
            eventMessage.setMessageType(MessageType.CUSTOM);
            eventMessage.setPayload(pluginActionDescriptor.getPayLoad(null));
            getEventBroadcastingService().postOutMessage(eventMessage);

            log.info(String.format("Admin message for plugin %d posted with transaction id %s", pluginConfigurationId, eventMessage.getTransactionId()));
        } catch (Exception e) {
            log.error(String.format("Error with admin message for plugin %d", pluginConfigurationId), e);
            return badRequest();
        }
        return ok();
    }

    /**
     * Return an HTML representation of the plugin status.
     * 
     * @param pluginStatus
     *            a plugin status
     * @return a String (HTML)
     */
    public static String getHtmlFromPluginStatus(PluginStatus pluginStatus) {
        if (pluginStatus.equals(PluginStatus.STARTED)) {
            return String.format(IMafConstants.LABEL_SUCCESS_FORMAT, PluginStatus.STARTED.name());
        }
        if (pluginStatus.equals(PluginStatus.STARTING) || pluginStatus.equals(PluginStatus.STOPPING)) {
            return String.format(IMafConstants.LABEL_WARNING_FORMAT, pluginStatus.name());
        }
        if (pluginStatus.equals(PluginStatus.START_FAILED)) {
            return String.format(IMafConstants.LABEL_DANGER_FORMAT, PluginStatus.START_FAILED.name());
        }
        if (pluginStatus.equals(PluginStatus.STOPPED)) {
            return String.format(IMafConstants.LABEL_DEFAULT_FORMAT, PluginStatus.STOPPED.name());
        }
        return "";
    }

    /**
     * Get the plugins that are already registered.
     * 
     * @param pluginManagerService
     *            the plugin manager service
     */
    private Set<String> getAlreadyRegisteredPlugins(IPluginManagerService pluginManagerService) {
        Collection<IPluginInfo> pluginInfos = pluginManagerService.getRegisteredPluginDescriptors().values();
        Set<String> registeredDefinitions = new HashSet<String>();
        for (IPluginInfo pluginInfo : pluginInfos) {
            registeredDefinitions.add(pluginInfo.getStaticDescriptor().getPluginDefinitionIdentifier());
        }
        return registeredDefinitions;
    }

    private IPluginManagerService getPluginManagerService() {
        return pluginManagerService;
    }

    private IEventBroadcastingService getEventBroadcastingService() {
        return eventBroadcastingService;
    }

    private ISecurityService getSecurityService() {
        return securityService;
    }

    /**
     * {@link Form} object to be used for editing a plugin configuration block.
     * 
     * @author Pierre-Yves Cloux
     */
    public static class PluginConfigurationBlockObject {
        private IPluginConfigurationBlockDescriptor pluginConfigurationBlockDescriptor;

        /**
         * Default constructor.
         */
        public PluginConfigurationBlockObject() {
            super();
        }

        /**
         * Construct a configuration block object with a block descriptor.
         * 
         * @param pluginConfigurationBlockDescriptor
         *            the plugin configuration block descriptor
         * @param pluginConfigurationId
         *            the plugin configuration id
         */
        public PluginConfigurationBlockObject(IPluginConfigurationBlockDescriptor pluginConfigurationBlockDescriptor, Long pluginConfigurationId) {
            super();
            this.pluginConfigurationBlockDescriptor = pluginConfigurationBlockDescriptor;
            this.pluginConfigurationBlockIdentifier = pluginConfigurationBlockDescriptor.getIdentifier();
            this.pluginConfigurationId = pluginConfigurationId;
        }

        public Long pluginConfigurationId;
        public String pluginConfigurationBlockIdentifier;
        public String value;

        /**
         * Get the plugin configuration block descriptor.
         */
        public IPluginConfigurationBlockDescriptor getPluginConfigurationBlockDescriptor() {
            return pluginConfigurationBlockDescriptor;
        }
    }

    /**
     * {@link Form} object to be used for creating a new
     * {@link PluginConfiguration} object and registering the plugin.
     * 
     * @author Pierre-Yves Cloux
     */
    public static class PluginRegistrationFormObject {
        public String definitionName;
        public String identifier;
        @Required
        public String name;
    }

    /**
     * {@link Table} object to be used for displaying a list of plugin object
     * definitions.
     * 
     * @author Pierre-Yves Cloux
     */
    public static class PluginDefinitionDescriptionTableObject {
        public String identifier;
        public String name;
        public String description;
        public String providerUrl;
        public String version;
        public boolean isAvailable;
    }

    /**
     * {@link Table} object to be used to display the running plugin
     * configurations.
     * 
     * @author Pierre-Yves Cloux
     */
    public static class PluginConfigurationDescriptionTableObject {
        public Long id;
        public String name;
        public String definitionIdentifier;
        public String definitionName;
        public String definitionDescription;
        public String definitionProviderUrl;
        public String definitionVersion;
        public PluginStatus status;
    }

    /**
     * Construct the integration icons bar depending of the sign-in user
     * permissions.
     * 
     * @param isDataSyndicationActive
     *            true if the data syndication is active (conf)
     * @param currentType
     *            the current menu item type, useful to select the correct item
     */
    public static SideBar getIconsBar(Boolean isDataSyndicationActive, MenuItemType currentType) {

        SideBar sideBar = new SideBar();

        HeaderMenuItem pluginsMenu = new HeaderMenuItem("admin.integration.sidebar.plugins", "glyphicons glyphicons-remote-control",
                currentType.equals(MenuItemType.PLUGINS));
        pluginsMenu.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION));
        sideBar.addMenuItem(pluginsMenu);

        pluginsMenu.addSubMenuItem(new ClickableMenuItem("admin.integration.sidebar.plugins.active_plugins",
                controllers.admin.routes.PluginManagerController.index(), "glyphicons glyphicons-electrical-plug", false));

        pluginsMenu.addSubMenuItem(new ClickableMenuItem("admin.integration.sidebar.plugins.available_plugins",
                controllers.admin.routes.PluginManagerController.registration(), "glyphicons glyphicons-shopping-bag", false));

        if (isDataSyndicationActive) {

            HeaderMenuItem dataSyndicationMenu = new HeaderMenuItem("admin.integration.sidebar.data_syndication", "glyphicons glyphicons-share-alt",
                    currentType.equals(MenuItemType.DATA_SYNDICATION));
            dataSyndicationMenu.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.PARTNER_SYNDICATION_PERMISSION));
            sideBar.addMenuItem(dataSyndicationMenu);

            dataSyndicationMenu.addSubMenuItem(new ClickableMenuItem("admin.integration.sidebar.data_syndication.master_agreements",
                    controllers.admin.routes.DataSyndicationController.viewMasterAgreements(), "glyphicons glyphicons-queen", false));

            dataSyndicationMenu.addSubMenuItem(new ClickableMenuItem("admin.integration.sidebar.data_syndication.consumer_agreements",
                    controllers.admin.routes.DataSyndicationController.viewConsumerAgreements(), "glyphicons glyphicons-pawn", false));

        }

        HeaderMenuItem apiMenu = new HeaderMenuItem("admin.integration.sidebar.api", "glyphicons glyphicons-transfer", currentType.equals(MenuItemType.API));
        apiMenu.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.API_MANAGER_PERMISSION));
        sideBar.addMenuItem(apiMenu);

        apiMenu.addSubMenuItem(new ClickableMenuItem("admin.integration.sidebar.api.keys", controllers.admin.routes.ApiManagerController.index(),
                "glyphicons glyphicons-keys", false));

        apiMenu.addSubMenuItem(new ClickableMenuItem("admin.integration.sidebar.api.browser", controllers.admin.routes.ApiManagerController.displayBrowser(),
                "glyphicons glyphicons-global", false));

        ClickableMenuItem sharedStorageMenu = new ClickableMenuItem("admin.integration.sidebar.shared_storage",
                controllers.admin.routes.SharedStorageManagerController.index(), "glyphicons glyphicons-inbox-in",
                currentType.equals(MenuItemType.SHARED_STORAGE));
        sharedStorageMenu.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.API_MANAGER_PERMISSION));
        sideBar.addMenuItem(sharedStorageMenu);

        return sideBar;
    }

    /**
     * The menu item type.
     * 
     * @author Johann Kohler
     * 
     */
    public static enum MenuItemType {
        PLUGINS, DATA_SYNDICATION, API, SHARED_STORAGE;
    }
}
