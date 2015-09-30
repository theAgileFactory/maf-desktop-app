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

import com.avaje.ebean.Ebean;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import constants.MafDataType;
import controllers.ControllersUtils;
import framework.commons.IFrameworkConstants;
import framework.commons.message.EventMessage;
import framework.commons.message.SystemLevelRoleTypeEventMessage;
import framework.security.ISecurityService;
import framework.services.account.AccountManagementException;
import framework.services.account.IAccountManagerPlugin;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.plugins.IEventBroadcastingService;
import framework.utils.Msg;
import framework.utils.PreferenceFormAndDisplayHandler;
import framework.utils.Table;
import framework.utils.Utilities;
import models.framework_models.account.SystemLevelRoleType;
import models.framework_models.account.SystemPermission;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import utils.form.RoleFormData;
import utils.table.RoleListView;

/**
 * The administration interface which is used to configuration the BizDock
 * application (system preferences, roles, reference data...).
 * 
 * @author Johann Kohler
 * 
 */
public class ConfigurationController extends Controller {

    @Inject
    private IEventBroadcastingService eventBroadcastingService;
    @Inject
    private IAccountManagerPlugin accountManagerPlugin;
    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;
    @Inject
    private ISecurityService securityService;
    @Inject
    private Configuration configuration;

    private static Logger.ALogger log = Logger.of(ConfigurationController.class);

    private static Form<PrefsData> systemPreferencesFormTemplate = Form.form(PrefsData.class);

    private static Form<RoleFormData> roleFormTemplate = Form.form(RoleFormData.class);

    private static List<String> editableFieldsValues = new ArrayList<String>() {
        private static final long serialVersionUID = -3636216975014360486L;

        {
            add(IFrameworkConstants.DISPLAY_LIST_PAGE_SIZE_PREFERENCE);
            add(IMafConstants.FINANCIAL_USE_PURCHASE_ORDER_PREFERENCE);
            add(IMafConstants.APPLICATION_LOGO_PREFERENCE);
            add(IMafConstants.TIMESHEET_MUST_APPROVE_PREFERENCE);
            add(IMafConstants.TIMESHEET_REMINDER_LIMIT_PREFERENCE);
            add(IMafConstants.TIMESHEET_HOURS_PER_DAY);
            add(IMafConstants.PACKAGE_STATUS_ON_GOING_FULFILLMENT_PERCENTAGE_PREFERENCE);
            add(IMafConstants.ROADMAP_CAPACITY_SIMULATOR_WARNING_LIMIT_PREFERENCE);
            add(IFrameworkConstants.API_AUTHZ_MODE_PREFERENCE);
            add(IFrameworkConstants.NOTIFICATION_SENDING_SYSTEM_PREFERENCE);
        }
    };

    private static List<String> smtpFieldsValues = new ArrayList<String>() {
        private static final long serialVersionUID = -36362169778420486L;

        {
            add(IFrameworkConstants.SMTP_HOST_PREFERENCE);
            add(IFrameworkConstants.SMTP_PORT_PREFERENCE);
            add(IFrameworkConstants.SMTP_SSL_PREFERENCE);
            add(IFrameworkConstants.SMTP_TLS_PREFERENCE);
            add(IFrameworkConstants.SMTP_USER_PREFERENCE);
            add(IFrameworkConstants.SMTP_PASSWORD_PREFERENCE);
        }
    };

    /**
     * Display the correct page according to permission.
     * 
     * @throws AccountManagementException
     */
    @Restrict({ @Group(IMafConstants.ADMIN_CONFIGURATION_PERMISSION), @Group(IMafConstants.ADMIN_CUSTOM_ATTRIBUTE_PERMISSION) })
    public Result index() throws AccountManagementException {

        if (getSecurityService().restrict(IMafConstants.ADMIN_CONFIGURATION_PERMISSION)) {
            return redirect(controllers.admin.routes.ConfigurationController.systemPreferences());
        }

        if (getSecurityService().restrict(IMafConstants.ADMIN_CUSTOM_ATTRIBUTE_PERMISSION)) {
            return redirect(controllers.admin.routes.ConfigurationCustomAttributeController.list(IMafConstants.PortfolioEntry));
        }

        return unauthorized();
    }

    /**
     * The system preferences page.
     */
    @Restrict({ @Group(IMafConstants.ADMIN_CONFIGURATION_PERMISSION) })
    public Result systemPreferences() {
        return ok(views.html.admin.config.systempreferences.index.render());
    }

    /**
     * Form to edit the system preferences.
     */
    @Restrict({ @Group(IMafConstants.ADMIN_CONFIGURATION_PERMISSION) })
    public Result editSystemPreferences() {
        Form<PrefsData> requestData = systemPreferencesFormTemplate.fill(new PrefsData());
        for (String field : editableFieldsValues) {
            PreferenceFormAndDisplayHandler.fillWithPreference(requestData, field);
        }
        return ok(views.html.admin.config.systempreferences.edit.render(requestData));
    }

    /**
     * Process the form to edit the values.
     */
    @Restrict({ @Group(IMafConstants.ADMIN_CONFIGURATION_PERMISSION) })
    public Result saveSystemPreferences() {

        Form<PrefsData> requestData = systemPreferencesFormTemplate.bindFromRequest();

        boolean hasErrors = false;
        for (String field : editableFieldsValues) {
            hasErrors = PreferenceFormAndDisplayHandler.validatePreference(requestData, field) || hasErrors;
        }

        if (!hasErrors) {
            Ebean.beginTransaction();
            try {
                for (String field : editableFieldsValues) {
                    PreferenceFormAndDisplayHandler.validateAndSavePreference(requestData, field);
                }
                Ebean.commitTransaction();

            } catch (Exception e) {
                Ebean.rollbackTransaction();
                return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
            }
        } else {
            return ok(views.html.admin.config.systempreferences.edit.render(requestData));
        }

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.system_preferences.edit.success"));

        return redirect(controllers.admin.routes.ConfigurationController.systemPreferences());
    }

    /**
     * The SMTP configuration page.
     */
    @Restrict({ @Group(IMafConstants.ADMIN_CONFIGURATION_PERMISSION) })
    public Result smtp() {
        return ok(views.html.admin.config.smtp.index.render());
    }

    /**
     * Form to edit the SMTP configuration.
     */
    @Restrict({ @Group(IMafConstants.ADMIN_CONFIGURATION_PERMISSION) })
    public Result editSmtp() {
        Form<PrefsData> requestData = systemPreferencesFormTemplate.fill(new PrefsData());
        for (String field : smtpFieldsValues) {
            PreferenceFormAndDisplayHandler.fillWithPreference(requestData, field);
        }
        return ok(views.html.admin.config.smtp.edit.render(requestData));
    }

    /**
     * Process the form to edit the SMTP configuration.
     */
    @Restrict({ @Group(IMafConstants.ADMIN_CONFIGURATION_PERMISSION) })
    public Result saveSmtp() {

        Form<PrefsData> requestData = systemPreferencesFormTemplate.bindFromRequest();

        boolean hasErrors = false;
        for (String field : smtpFieldsValues) {
            hasErrors = PreferenceFormAndDisplayHandler.validatePreference(requestData, field) || hasErrors;
        }

        if (!hasErrors) {
            Ebean.beginTransaction();
            try {
                for (String field : smtpFieldsValues) {
                    PreferenceFormAndDisplayHandler.validateAndSavePreference(requestData, field);
                }
                Ebean.commitTransaction();
            } catch (Exception e) {
                Ebean.rollbackTransaction();
                return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
            }
        } else {
            return ok(views.html.admin.config.smtp.edit.render(requestData));
        }

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.smtp.edit.success"));

        return redirect(controllers.admin.routes.ConfigurationController.smtp());
    }

    /**
     * The roles page.
     */
    @Restrict({ @Group(IMafConstants.ADMIN_CONFIGURATION_PERMISSION) })
    public Result roles() {

        // get all active role types
        List<SystemLevelRoleType> roleTypes = SystemLevelRoleType.getAllActiveRoles();

        List<RoleListView> rolesListView = new ArrayList<RoleListView>();
        for (SystemLevelRoleType roleType : roleTypes) {
            rolesListView.add(new RoleListView(roleType));
        }

        Table<RoleListView> filledTable = RoleListView.templateTable.fill(rolesListView);

        return ok(views.html.admin.config.roles.index.render(filledTable));
    }

    /**
     * Form to create/edit a role.
     * 
     * @param roleTypeId
     *            the role type id (0 for create)
     * @return
     */
    @Restrict({ @Group(IMafConstants.ADMIN_CONFIGURATION_PERMISSION) })
    public Result manageRole(Long roleTypeId) {

        Form<RoleFormData> roleForm = roleFormTemplate;

        // edit case: inject values
        if (!roleTypeId.equals(Long.valueOf(0))) {

            SystemLevelRoleType role = SystemLevelRoleType.getActiveRoleFromId(roleTypeId);

            roleForm = roleFormTemplate.fill(new RoleFormData(role, getI18nMessagesPlugin()));

        }

        return ok(views.html.admin.config.roles.manage.render(roleForm, SystemPermission.getAllSelectableSystemPermissions()));
    }

    /**
     * Process the form to create/edit a role.
     */
    @Restrict({ @Group(IMafConstants.ADMIN_CONFIGURATION_PERMISSION) })
    public Result processManageRole() {

        // bind the form
        Form<RoleFormData> boundForm = roleFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.roles.manage.render(boundForm, SystemPermission.getAllSelectableSystemPermissions()));
        }

        RoleFormData roleFormData = boundForm.get();

        // check the name is not already used by another role
        SystemLevelRoleType testRole = SystemLevelRoleType.getActiveRoleFromName(roleFormData.name);
        if (testRole != null) {
            if (roleFormData.id != null) { // edit case
                if (!testRole.id.equals(roleFormData.id)) {
                    boundForm.reject("name", Msg.get("object.role.name.already_used"));
                    return ok(views.html.admin.config.roles.manage.render(boundForm, SystemPermission.getAllSelectableSystemPermissions()));
                }
            } else { // new case
                boundForm.reject("name", Msg.get("object.role.name.already_used"));
                return ok(views.html.admin.config.roles.manage.render(boundForm, SystemPermission.getAllSelectableSystemPermissions()));
            }
        }

        SystemLevelRoleType role = null;

        if (roleFormData.id == null) { // create case

            role = new SystemLevelRoleType();
            role.selectable = true;

            roleFormData.fill(role);
            role.save();
            // role.saveManyToManyAssociations("systemPermissions");

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.roles.add.successful"));

        } else { // edit case

            role = SystemLevelRoleType.getActiveRoleFromId(roleFormData.id);

            // Retrieve the current permissions names (see plugins notification
            // below) and the current non-selectable permissions for this role
            List<String> previousPermissionNames = new ArrayList<String>();
            List<SystemPermission> previousNonSelectablePermissions = new ArrayList<SystemPermission>();
            if (role.systemPermissions != null) {
                for (SystemPermission permission : role.systemPermissions) {
                    previousPermissionNames.add(permission.name);
                    if (!permission.isSelectable()) {
                        previousNonSelectablePermissions.add(permission);
                    }
                }
            }

            roleFormData.fill(role);
            role.systemPermissions.addAll(previousNonSelectablePermissions);
            role.update();
            // role.saveManyToManyAssociations("systemPermissions");

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.roles.edit.successful"));

            // Notify of the role update
            SystemLevelRoleTypeEventMessage eventMessage = new SystemLevelRoleTypeEventMessage(roleFormData.id, MafDataType.getSystemLevelRoleType(),
                    EventMessage.MessageType.OBJECT_UPDATED);
            SystemLevelRoleTypeEventMessage.PayLoad payload = new SystemLevelRoleTypeEventMessage.PayLoad();
            payload.setPreviousPermissionNames(previousPermissionNames);
            eventMessage.setPayload(payload);
            getEventBroadcastingService().postOutMessage(eventMessage);
        }

        roleFormData.description.persist(getI18nMessagesPlugin());

        // clean the cache
        try {
            getAccountManagerPlugin().invalidateAllUserAccountsCache();
        } catch (AccountManagementException e) {
            log.error("Unable to flush the user cache after roles modifications", e);
        }

        return redirect(controllers.admin.routes.ConfigurationController.roles());

    }

    /**
     * Delete a role.
     * 
     * @param roleTypeId
     *            the role type id (0 for create)
     * @return
     */
    @Restrict({ @Group(IMafConstants.ADMIN_CONFIGURATION_PERMISSION) })
    public Result deleteRole(Long roleTypeId) {

        SystemLevelRoleType role = SystemLevelRoleType.getActiveRoleFromId(roleTypeId);

        role.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.roles.delete.successful"));

        // clean the cache
        try {
            getAccountManagerPlugin().invalidateAllUserAccountsCache();
        } catch (AccountManagementException e) {
            log.error("Unable to flush the user cache after roles modifications", e);
        }

        return redirect(controllers.admin.routes.ConfigurationController.roles());
    }

    /**
     * The menu item type.
     * 
     * @author Johann Kohler
     * 
     */
    public static enum MenuItemType {
        SYSTEM_PREFERENCES, SMTP, ROLES, REFERENCE_DATA, CUSTOM_ATTRIBUTES;
    }

    /**
     * The data for the preferences form.
     * 
     * @author Pierre-Yves Cloux
     */
    public static class PrefsData {
    }

    /**
     * Get the account manager service.
     */
    private IAccountManagerPlugin getAccountManagerPlugin() {
        return accountManagerPlugin;
    }

    /**
     * Get the i18n messages service.
     */
    private II18nMessagesPlugin getI18nMessagesPlugin() {
        return i18nMessagesPlugin;
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

    private Configuration getConfiguration() {
        return configuration;
    }
}
