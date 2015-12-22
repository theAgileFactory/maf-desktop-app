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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.avaje.ebean.ExpressionList;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import constants.IMafConstants;
import controllers.ControllersUtils;
import framework.commons.IFrameworkConstants.Syntax;
import framework.commons.message.EventMessage;
import framework.commons.message.EventMessage.MessageType;
import framework.security.ISecurityService;
import framework.services.account.AccountManagementException;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.ext.api.IExtensionDescriptor.IPluginConfigurationBlockDescriptor;
import framework.services.ext.api.IExtensionDescriptor.IPluginConfigurationBlockDescriptor.ConfigurationBlockEditionType;
import framework.services.ext.api.IExtensionDescriptor.IPluginDescriptor;
import framework.services.plugins.IEventBroadcastingService;
import framework.services.plugins.IPluginManagerService;
import framework.services.plugins.IPluginManagerService.IPluginInfo;
import framework.services.plugins.IPluginManagerService.PluginStatus;
import framework.services.plugins.api.IPluginActionDescriptor;
import framework.services.plugins.api.PluginException;
import framework.services.session.IUserSessionManagerPlugin;
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
import models.framework_models.plugin.PluginLog;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints.Required;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;

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
    private IUserSessionManagerPlugin userSessionManagerPlugin;
    @Inject
    private IPluginManagerService pluginManagerService;
    @Inject
    private IEventBroadcastingService eventBroadcastingService;
    @Inject
    private ISecurityService securityService;
    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;
    @Inject
    private Configuration configuration;

    private static Logger.ALogger log = Logger.of(PluginManagerController.class);
    public static final int MAX_CONFIG_FILE_SIZE = 1 * 1024 * 1024;

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
            addColumnConfiguration("event", "event", "object.plugin_log.event.label", new TextFieldFilterComponent("*"), true, false,
                    SortStatusType.UNSORTED);
            addColumnConfiguration("logMessage", "logMessage", "object.plugin_log.log_message.label", new TextFieldFilterComponent("*"), true, false,
                    SortStatusType.UNSORTED);
            addColumnConfiguration("transactionId", "transactionId", "object.plugin_log.transaction_id.label", new TextFieldFilterComponent("*"), false,
                    false, SortStatusType.UNSORTED);
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
     * Distribute the plugin image.
     * 
     * @param identifier
     *            the image identifier
     * @param isBigImage
     *            true for the big image, esle the small
     */
    @SubjectPresent
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
     * 
     * @throws AccountManagementException
     */
    @Restrict({ @Group(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION), @Group(IMafConstants.API_MANAGER_PERMISSION),
            @Group(IMafConstants.PARTNER_SYNDICATION_PERMISSION) })
    public Result index() throws AccountManagementException {

        if (!getSecurityService().restrict(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION)) {
            if (getSecurityService().restrict(IMafConstants.API_MANAGER_PERMISSION)) {
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
            tableObject.name = pluginInfo.getPluginConfigurationName();
            tableObject.definitionIdentifier = pluginInfo.getDescriptor().getIdentifier();
            tableObject.definitionName = Msg.get(pluginInfo.getDescriptor().getName());
            tableObject.definitionDescription = Msg.get(pluginInfo.getDescriptor().getDescription());
            tableObject.definitionProviderUrl = pluginInfo.getDescriptor().getVendorUrl();
            tableObject.definitionVersion = pluginInfo.getDescriptor().getVersion();
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
        Map<String, Pair<Boolean, IPluginDescriptor>> pluginDescriptors = getPluginManagerService().getAllPluginDescriptors();
        List<Pair<Boolean, IPluginDescriptor>> plugins = new ArrayList<>();
        for (String key : pluginDescriptors.keySet()) {
            Pair<Boolean, IPluginDescriptor> record = pluginDescriptors.get(key);
            IPluginDescriptor pluginDescriptor = record.getRight();
            if (!(registeredDefinitions.contains(pluginDescriptor.getIdentifier()) && !pluginDescriptor.multiInstanceAllowed())) {
                plugins.add(Pair.of(record.getLeft(), pluginDescriptor));
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

        IPluginDescriptor pluginRunnerDescriptor = getPluginManagerService().getAvailablePluginDescriptor(pluginDefinitionIdentifier);
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
        try {
            getPluginManagerService().registerPlugin(pluginRegistrationFormObject.name, pluginRegistrationFormObject.identifier);
            Utilities.sendSuccessFlashMessage(Msg.get("admin.plugin_manager.configuration.new.success", pluginRegistrationFormObject.name));
        } catch (PluginException e) {
            Utilities.sendErrorFlashMessage(Msg.get("admin.plugin_manager.configuration.new.error", pluginRegistrationFormObject.name));
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
        IPluginInfo pluginInfo = getPluginManagerService().getRegisteredPluginDescriptors().get(pluginConfigurationId);
        if (pluginInfo == null) {
            Utilities.sendErrorFlashMessage(Msg.get("admin.plugin_manager.configuration.view.not_exists", pluginConfigurationId));
            return redirect(routes.PluginManagerController.index());
        }
        String pluginConfigurationName = pluginInfo.getPluginConfigurationName();
        try {
            getPluginManagerService().unregisterPlugin(pluginConfigurationId);
            Utilities.sendSuccessFlashMessage(Msg.get("admin.plugin_manager.configuration.delete.success", pluginConfigurationName));
        } catch (Exception e) {
            Utilities.sendErrorFlashMessage(Msg.get("admin.plugin_manager.configuration.delete.error", pluginConfigurationName));
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
        IPluginDescriptor pluginDescriptor = getPluginManagerService().getPluginDescriptor(pluginDefinitionIdentifier);
        if (pluginDescriptor == null) {
            Utilities.sendErrorFlashMessage(Msg.get("admin.plugin_manager.definition.view.not_exists", pluginDefinitionIdentifier));
            return redirect(routes.PluginManagerController.registration());
        }
        return ok(views.html.admin.plugin.pluginmanager_definition_details.render(pluginDescriptor,
                getPluginManagerService().isPluginAvailable(pluginDescriptor.getIdentifier())));
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
        // Plugin configuration block
        List<PluginConfigurationBlockObject> configurationBlocks = new ArrayList<PluginConfigurationBlockObject>();
        if (pluginInfo.getDescriptor().getConfigurationBlockDescriptors() != null) {
            for (IPluginConfigurationBlockDescriptor pluginConfigurationBlockDescriptor : pluginInfo.getDescriptor().getConfigurationBlockDescriptors()
                    .values()) {
                configurationBlocks.add(new PluginConfigurationBlockObject(pluginConfigurationBlockDescriptor, pluginConfigurationId));
            }
        }

        // Plugin logs
        String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
        FilterConfig<PluginLog> filterConfig = pluginLogsFilterConfig.getCurrent(uid, request());

        ExpressionList<PluginLog> pluginLogExpressionList = filterConfig
                .updateWithSearchExpression(PluginLog.getAllPluginLogsForPluginConfigurationId(pluginConfigurationId));
        filterConfig.updateWithSortExpression(pluginLogExpressionList);

        Pagination<PluginLog> pluginLogsPagination = new Pagination<PluginLog>(pluginLogExpressionList);
        pluginLogsPagination.setCurrentPage(filterConfig.getCurrentPage());

        Table<PluginLog> pluginLogsTable = pluginLogsTableTemplate.fill(pluginLogsPagination.getListOfObjects(), filterConfig.getColumnsToHide());

        return ok(views.html.admin.plugin.pluginmanager_configuration_details.render(pluginInfo.getPluginConfigurationName(), pluginConfigurationId,
                pluginInfo, configurationBlocksTableTemplate.fill(configurationBlocks), pluginLogsPagination, pluginLogsTable, filterConfig));
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

        try {

            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            FilterConfig<PluginLog> filledFilterConfig = pluginLogsFilterConfig.persistCurrentInDefault(uid, request());

            if (filledFilterConfig == null) {
                return ok(views.html.framework_views.parts.table.dynamic_tableview_no_more_compatible.render());
            } else {

                ExpressionList<PluginLog> pluginLogExpressionList = filledFilterConfig
                        .updateWithSearchExpression(PluginLog.getAllPluginLogsForPluginConfigurationId(pluginConfigurationId));

                filledFilterConfig.updateWithSortExpression(pluginLogExpressionList);

                Pagination<PluginLog> pagination = new Pagination<PluginLog>(pluginLogExpressionList);
                pagination.setCurrentPage(filledFilterConfig.getCurrentPage());

                Table<PluginLog> table = pluginLogsTableTemplate.fill(pagination.getListOfObjects(), filledFilterConfig.getColumnsToHide());

                return ok(views.html.framework_views.parts.table.dynamic_tableview.render(table, pagination));

            }

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
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
        IPluginInfo pluginInfo = getPluginManagerService().getRegisteredPluginDescriptors().get(pluginConfigurationId);
        if (pluginInfo == null) {
            return badRequest();
        }
        String pluginConfigurationName = pluginInfo.getPluginConfigurationName();
        try {
            getPluginManagerService().startPlugin(pluginConfigurationId);
            Utilities.sendSuccessFlashMessage(Msg.get("admin.plugin_manager.configuration.view.panel.admin.start.success", Msg.get(pluginConfigurationName)));
        } catch (PluginException e) {
            log.error("Exception while attempting to start the plugin " + pluginConfigurationId, e);
            Utilities.sendErrorFlashMessage(Msg.get("admin.plugin_manager.configuration.view.panel.admin.start.error", Msg.get(pluginConfigurationName)));
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
        IPluginInfo pluginInfo = getPluginManagerService().getRegisteredPluginDescriptors().get(pluginConfigurationId);
        if (pluginInfo == null) {
            return badRequest();
        } else {
            String pluginConfigurationName = pluginInfo.getPluginConfigurationName();
            getPluginManagerService().stopPlugin(pluginConfigurationId);
            Utilities.sendSuccessFlashMessage(Msg.get("admin.plugin_manager.configuration.view.panel.admin.stop.success", Msg.get(pluginConfigurationName)));
        }
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
        try {
            Pair<IPluginConfigurationBlockDescriptor, byte[]> configBlock = getPluginManagerService().getPluginConfigurationBlock(pluginConfigurationId,
                    pluginConfigurationBlockIdentifier);
            PluginConfigurationBlockObject pluginConfigurationBlockObject = new PluginConfigurationBlockObject();
            pluginConfigurationBlockObject.value = new String(configBlock.getRight());
            return ok(views.html.admin.plugin.pluginmanager_configblock_edit.render(pluginConfigurationId, configBlock.getLeft(),
                    pluginConfigurationBlockFormTemplate.fill(pluginConfigurationBlockObject),
                    getSyntaxFromConfigurationBlockEditionType(configBlock.getLeft().getEditionType())));
        } catch (PluginException e) {
            log.error("Error while downloading the plugin configuration " + pluginConfigurationBlockIdentifier + " for " + pluginConfigurationId, e);
            return badRequest();
        }
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
        if (pluginInfo == null || pluginInfo.getDescriptor().getConfigurationBlockDescriptors() == null
                || !pluginInfo.getDescriptor().getConfigurationBlockDescriptors().containsKey(pluginConfigurationBlockIdentifier)) {
            return null;
        }
        IPluginConfigurationBlockDescriptor pluginConfigurationBlockDescriptor = pluginInfo.getDescriptor().getConfigurationBlockDescriptors()
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
            return badRequest();
        }

        Form<PluginConfigurationBlockObject> boundForm = pluginConfigurationBlockFormTemplate.bindFromRequest();
        if (boundForm.hasErrors()) {
            return ok(views.html.admin.plugin.pluginmanager_configblock_edit.render(pluginConfigurationId, pluginConfigurationBlockDescriptor, boundForm,
                    getSyntaxFromConfigurationBlockEditionType(pluginConfigurationBlockDescriptor.getEditionType())));
        }

        // Update the configuration block
        PluginConfigurationBlockObject pluginConfigurationBlockObject = boundForm.get();
        try {
            getPluginManagerService().updatePluginConfiguration(pluginConfigurationId, pluginConfigurationBlockIdentifier,
                    (pluginConfigurationBlockObject.value != null ? pluginConfigurationBlockObject.value.getBytes() : null));
            Utilities.sendWarningFlashMessage(
                    Msg.get("admin.plugin_manager.configuration_block.edit.success", Msg.get(pluginConfigurationBlockDescriptor.getName())));
        } catch (PluginException e) {
            return redirect(routes.PluginManagerController.index());
        }
        return redirect(routes.PluginManagerController.pluginConfigurationDetails(pluginConfigurationId));
    }

    /**
     * Export the plugin configuration as an XML file
     * 
     * @param pluginConfigurationId
     *            the plugin configuration id
     * @return
     */
    @Restrict({ @Group(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION) })
    public Result exportConfiguration(Long pluginConfigurationId) {
        if (log.isDebugEnabled()) {
            log.debug("Export of the configuration blocks for the plugin " + pluginConfigurationId);
        }
        response().setContentType("application/xml");
        response().setHeader("Content-disposition", "attachment; filename=export.xml");
        try {
            String configuration = getPluginManagerService().exportPluginConfiguration(pluginConfigurationId);
            if (log.isDebugEnabled()) {
                log.debug("Found the configuration " + configuration);
            }
            return ok(configuration);
        } catch (PluginException e) {
            Utilities.sendErrorFlashMessage(Msg.get("admin.plugin_manager.configuration_block.export.error"));
            return redirect(routes.PluginManagerController.pluginConfigurationDetails(pluginConfigurationId));
        }
    }

    /**
     * Import a previously exported configuration file.<br/>
     * The file is posted using a file input control.
     * 
     * @param pluginConfigurationId
     *            the plugin configuration id
     * @return
     */
    @Restrict({ @Group(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION) })
    @BodyParser.Of(value = BodyParser.MultipartFormData.class, maxLength = MAX_CONFIG_FILE_SIZE)
    public Promise<Result> importConfiguration(Long pluginConfigurationId) {
        // Perform the upload
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Configuration upload requested for " + pluginConfigurationId);
                    }
                    MultipartFormData body = request().body().asMultipartFormData();
                    FilePart filePart = body.getFile("import");
                    if (filePart != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("A file has been found");
                        }
                        String configuration = IOUtils.toString(new FileInputStream(filePart.getFile()));
                        if (log.isDebugEnabled()) {
                            log.debug("Content of the uploaded file is " + configuration);
                        }
                        try {
                            getPluginManagerService().importPluginConfiguration(pluginConfigurationId, configuration);
                            if (log.isDebugEnabled()) {
                                log.debug("Plugin configuration uploaded");
                            }
                            Utilities.sendWarningFlashMessage(Msg.get("admin.plugin_manager.configuration.view.panel.configuration.import.success"));
                        } catch (PluginException e) {
                            log.error("Attempt to upload an invalid plugin configuration for " + pluginConfigurationId, e);
                            Utilities.sendErrorFlashMessage(Msg.get("admin.plugin_manager.configuration_block.import.error"));
                        }
                    } else {
                        Utilities.sendErrorFlashMessage(Msg.get("form.input.file_field.no_file"));
                    }
                } catch (Exception e) {
                    Utilities.sendErrorFlashMessage(
                            Msg.get("admin.shared_storage.upload.file.size.invalid", FileUtils.byteCountToDisplaySize(MAX_CONFIG_FILE_SIZE)));
                    String message = String.format("Failure while uploading the plugin configuration for %d", pluginConfigurationId);
                    log.error(message);
                    throw new IOException(message, e);
                }
                return redirect(routes.PluginManagerController.pluginConfigurationDetails(pluginConfigurationId));
            }
        });
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
            if (pluginInfo == null || pluginInfo.getActionDescriptors() == null || !pluginInfo.getActionDescriptors().containsKey(pluginActionIdentifier)) {
                return badRequest();
            }
            IPluginActionDescriptor pluginActionDescriptor = pluginInfo.getActionDescriptors().get(pluginActionIdentifier);
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
            registeredDefinitions.add(pluginInfo.getDescriptor().getIdentifier());
        }
        return registeredDefinitions;
    }

    /**
     * Get the plugin manager service.
     */
    private IPluginManagerService getPluginManagerService() {
        return pluginManagerService;
    }

    /**
     * Get the event broadcasting service.
     */
    private IEventBroadcastingService getEventBroadcastingService() {
        return eventBroadcastingService;
    }

    /**
     * Get the security service.
     */
    private ISecurityService getSecurityService() {
        return securityService;
    }

    /**
     * Get the user session manager service.
     */
    private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return userSessionManagerPlugin;
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

        HeaderMenuItem pluginsMenu = new HeaderMenuItem("admin.integration.sidebar.plugins", "fa fa-rss", currentType.equals(MenuItemType.PLUGINS));
        pluginsMenu.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.ADMIN_PLUGIN_MANAGER_PERMISSION));
        sideBar.addMenuItem(pluginsMenu);

        pluginsMenu.addSubMenuItem(new ClickableMenuItem("admin.integration.sidebar.plugins.active_plugins",
                controllers.admin.routes.PluginManagerController.index(), "fa fa-plug", false));

        pluginsMenu.addSubMenuItem(new ClickableMenuItem("admin.integration.sidebar.plugins.available_plugins",
                controllers.admin.routes.PluginManagerController.registration(), "fa fa-shopping-bag", false));

        if (isDataSyndicationActive) {

            HeaderMenuItem dataSyndicationMenu = new HeaderMenuItem("admin.integration.sidebar.data_syndication", "fa fa-share-alt",
                    currentType.equals(MenuItemType.DATA_SYNDICATION));
            dataSyndicationMenu.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.PARTNER_SYNDICATION_PERMISSION));
            sideBar.addMenuItem(dataSyndicationMenu);

            dataSyndicationMenu.addSubMenuItem(new ClickableMenuItem("admin.integration.sidebar.data_syndication.master_agreements",
                    controllers.admin.routes.DataSyndicationController.viewMasterAgreements(), "fa fa-sign-out", false));

            dataSyndicationMenu.addSubMenuItem(new ClickableMenuItem("admin.integration.sidebar.data_syndication.consumer_agreements",
                    controllers.admin.routes.DataSyndicationController.viewConsumerAgreements(), "fa fa-sign-in", false));

        }

        HeaderMenuItem apiMenu = new HeaderMenuItem("admin.integration.sidebar.api", "fa fa-exchange", currentType.equals(MenuItemType.API));
        apiMenu.setAuthorizedPermissions(Utilities.getListOfArray(IMafConstants.API_MANAGER_PERMISSION));
        sideBar.addMenuItem(apiMenu);

        apiMenu.addSubMenuItem(
                new ClickableMenuItem("admin.integration.sidebar.api.keys", controllers.admin.routes.ApiManagerController.index(), "fa fa-key", false));

        apiMenu.addSubMenuItem(new ClickableMenuItem("admin.integration.sidebar.api.browser", controllers.admin.routes.ApiManagerController.displayBrowser(),
                "fa fa-globe", false));

        ClickableMenuItem sharedStorageMenu = new ClickableMenuItem("admin.integration.sidebar.shared_storage",
                controllers.admin.routes.SharedStorageManagerController.index(), "fa fa-folder", currentType.equals(MenuItemType.SHARED_STORAGE));
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

    private II18nMessagesPlugin getI18nMessagesPlugin() {
        return i18nMessagesPlugin;
    }

    private Configuration getConfiguration() {
        return configuration;
    }
}
