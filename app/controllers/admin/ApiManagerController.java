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

import models.framework_models.parent.IModelConstants;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.data.Form;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import framework.services.ServiceManager;
import framework.services.api.commons.ApiSignatureException;
import framework.services.api.server.IApiApplicationConfiguration;
import framework.services.api.server.IApiSignatureService;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Table.ColumnDef.SorterType;
import framework.utils.Utilities;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.ObjectFormatter;

/**
 * The controller to manage the API configuration and testing.<br/>
 * This one makes use of Swagger browser and meta-description framework.
 * 
 * @author Pierre-Yves Cloux
 */
@Restrict({ @Group(IMafConstants.API_MANAGER_PERMISSION), @Group(IMafConstants.API_TESTER_PERMISSION) })
public class ApiManagerController extends Controller {

    private static Logger.ALogger log = Logger.of(ApiManagerController.class);
    private static Form<ApiRegistrationObject> apiRegistrationForm = Form.form(ApiRegistrationObject.class);

    private static Table<IApiApplicationConfiguration> tableTemplate = new Table<IApiApplicationConfiguration>() {
        {
            this.addColumn("applicationName", "applicationName", "object.api_registration.name.label", SorterType.NONE, true);
            this.setJavaColumnFormatter("applicationName", new ObjectFormatter<IApiApplicationConfiguration>());

            this.addColumn("description", "description", "object.api_registration.description.label", SorterType.NONE, true);
            this.setJavaColumnFormatter("description", new ObjectFormatter<IApiApplicationConfiguration>());

            this.addColumn("testable", "testable", "object.api_registration.testable.label", SorterType.NONE, true);
            this.setJavaColumnFormatter("testable", new BooleanFormatter<IApiApplicationConfiguration>());

            this.addColumn("editActionLink", "applicationName", "", SorterType.NONE);
            this.setJavaColumnFormatter("editActionLink", new IColumnFormatter<IApiApplicationConfiguration>() {
                @Override
                public String apply(IApiApplicationConfiguration apiAppConfig, Object value) {
                    String url = routes.ApiManagerController.displayApiRegistrationUpdateForm(apiAppConfig.getApplicationName()).url();
                    return views.html.framework_views.parts.formats.display_with_format.render(url, IMafConstants.EDIT_URL_FORMAT).body();
                }
            });
            this.setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            this.setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

            this.addColumn("deleteActionLink", "applicationName", "", SorterType.NONE);
            this.setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<IApiApplicationConfiguration>() {
                @Override
                public String apply(IApiApplicationConfiguration apiAppConfig, Object value) {
                    String url = routes.ApiManagerController.displayDeleteApiRegistrationForm(apiAppConfig.getApplicationName()).url();
                    return views.html.framework_views.parts.formats.display_with_format.render(url, IMafConstants.DELETE_URL_FORMAT).body();
                }
            });
            this.setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            this.setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

            this.setLineAction(new IColumnFormatter<IApiApplicationConfiguration>() {
                @Override
                public String apply(IApiApplicationConfiguration apiAppConfig, Object value) {
                    return routes.ApiManagerController.displayApiRegistration(apiAppConfig.getApplicationName()).url();
                }
            });

            this.setIdFieldName("applicationName");
        }
    };

    /**
     * Default constructor.
     */
    public ApiManagerController() {
    }

    /**
     * Display a table with the list of registered applications.
     */
    @Restrict({ @Group(IMafConstants.API_MANAGER_PERMISSION) })
    public static Result index() throws ApiSignatureException {
        IApiSignatureService apiSignatureService = ServiceManager.getService(IApiSignatureService.NAME, IApiSignatureService.class);
        Table<IApiApplicationConfiguration> filledTable = tableTemplate.fill(apiSignatureService.listAuthorizedApplications());
        return ok(views.html.admin.api.index.render(filledTable));
    }

    /**
     * Display the details of a named API registration entry.
     * 
     * @param applicationName
     *            the name of the application
     */
    @Restrict({ @Group(IMafConstants.API_MANAGER_PERMISSION) })
    public static Result displayApiRegistration(String applicationName) {
        IApiSignatureService apiSignatureService = ServiceManager.getService(IApiSignatureService.NAME, IApiSignatureService.class);
        try {
            IApiApplicationConfiguration appConfig = apiSignatureService.getApplicationConfigurationFromApplicationName(applicationName);
            return ok(views.html.admin.api.display.render(appConfig));
        } catch (Exception e) {
            log.error("Error while displaying application", e);
            Utilities.sendErrorFlashMessage(Msg.get("admin.api_manager.unknown.application.message", applicationName));
            return redirect(controllers.admin.routes.ApiManagerController.index());
        }
    }

    /**
     * Re-generate the application and secret keys for the specified
     * application.
     * 
     * @param applicationName
     *            the name of the application
     */
    @Restrict({ @Group(IMafConstants.API_MANAGER_PERMISSION) })
    public static Result resetApiRegistrationKeys(String applicationName) {
        IApiSignatureService apiSignatureService = ServiceManager.getService(IApiSignatureService.NAME, IApiSignatureService.class);
        try {
            apiSignatureService.resetApplicationConfigurationKeys(applicationName);
            Utilities.sendSuccessFlashMessage(Msg.get("admin.api_manager.keys.reseted.message", applicationName));
        } catch (Exception e) {
            log.error("Error while resetting application keys", e);
            Utilities.sendErrorFlashMessage(Msg.get("admin.api_manager.unknown.application.message", applicationName));
        }
        return redirect(controllers.admin.routes.ApiManagerController.index());
    }

    /**
     * Delete the specified registration record.
     * 
     * @param applicationName
     *            the name of the application
     */
    @Restrict({ @Group(IMafConstants.API_MANAGER_PERMISSION) })
    public static Result deleteApiRegistration(String applicationName) {
        IApiSignatureService apiSignatureService = ServiceManager.getService(IApiSignatureService.NAME, IApiSignatureService.class);
        try {
            apiSignatureService.deleteApplicationConfiguration(applicationName);
            Utilities.sendSuccessFlashMessage(Msg.get("admin.api_manager.registration.deleted.message", applicationName));
        } catch (Exception e) {
            log.error("Error while deleting the application", e);
            Utilities.sendErrorFlashMessage(Msg.get("admin.api_manager.unknown.application.message", applicationName));
        }
        return redirect(controllers.admin.routes.ApiManagerController.index());
    }

    /**
     * Display the screen which request a validation of the user before reseting
     * the application and secret keys.
     * 
     * @param applicationName
     *            the name of the application
     */
    @Restrict({ @Group(IMafConstants.API_MANAGER_PERMISSION) })
    public static Result displayResetApiRegistrationKeysForm(String applicationName) {
        IApiSignatureService apiSignatureService = ServiceManager.getService(IApiSignatureService.NAME, IApiSignatureService.class);
        try {
            IApiApplicationConfiguration appConfig = apiSignatureService.getApplicationConfigurationFromApplicationName(applicationName);
            return ok(views.html.admin.api.keyreset_validation.render(appConfig));
        } catch (Exception e) {
            log.error("Error while displaying the application keys reset form", e);
            Utilities.sendErrorFlashMessage(Msg.get("admin.api_manager.unknown.application.message", applicationName));
            return redirect(controllers.admin.routes.ApiManagerController.index());
        }
    }

    /**
     * Display the screen which request a validation of the user before deleting
     * the application registration.
     * 
     * @param applicationName
     *            the name of the application
     */
    @Restrict({ @Group(IMafConstants.API_MANAGER_PERMISSION) })
    public static Result displayDeleteApiRegistrationForm(String applicationName) {
        IApiSignatureService apiSignatureService = ServiceManager.getService(IApiSignatureService.NAME, IApiSignatureService.class);
        try {
            IApiApplicationConfiguration appConfig = apiSignatureService.getApplicationConfigurationFromApplicationName(applicationName);
            return ok(views.html.admin.api.delete_validation.render(appConfig));
        } catch (Exception e) {
            log.error("Error while displaying the application delete form", e);
            Utilities.sendErrorFlashMessage(Msg.get("admin.api_manager.unknown.application.message", applicationName));
            return redirect(controllers.admin.routes.ApiManagerController.index());
        }
    }

    /**
     * Display the form to add an API registration.
     */
    @Restrict({ @Group(IMafConstants.API_MANAGER_PERMISSION) })
    public static Result displayApiRegistrationCreationForm() {
        ApiRegistrationObject apiRegistrationObject = new ApiRegistrationObject();
        Form<ApiRegistrationObject> loadedForm = apiRegistrationForm.fill(apiRegistrationObject);
        return ok(views.html.admin.api.create.render(loadedForm));
    }

    /**
     * Display the form to "edit" an API registration. Edit means changing the
     * the authorizations. The application name cannot be changed after the
     * registration is completed.
     * 
     * @param applicationName
     *            the name of the application
     */
    @Restrict({ @Group(IMafConstants.API_MANAGER_PERMISSION) })
    public static Result displayApiRegistrationUpdateForm(String applicationName) {
        IApiSignatureService apiSignatureService = ServiceManager.getService(IApiSignatureService.NAME, IApiSignatureService.class);
        try {
            IApiApplicationConfiguration appConfig = apiSignatureService.getApplicationConfigurationFromApplicationName(applicationName);
            ApiRegistrationObject apiRegistrationObject = new ApiRegistrationObject(appConfig);
            Form<ApiRegistrationObject> loadedForm = apiRegistrationForm.fill(apiRegistrationObject);
            return ok(views.html.admin.api.edit.render(loadedForm, applicationName));
        } catch (Exception e) {
            log.error("Error while displaying the application update form", e);
            Utilities.sendErrorFlashMessage(Msg.get("admin.api_manager.unknown.application.message", applicationName));
            return redirect(controllers.admin.routes.ApiManagerController.index());
        }
    }

    /**
     * Creates a new API registration.
     */
    @Restrict({ @Group(IMafConstants.API_MANAGER_PERMISSION) })
    public static Promise<Result> saveRegistration() {
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                Form<ApiRegistrationObject> boundForm = apiRegistrationForm.bindFromRequest();
                if (boundForm.hasErrors()) {
                    System.out.println(boundForm.errors());
                    return badRequest(views.html.admin.api.create.render(boundForm));
                }
                ApiRegistrationObject apiRegistrationObject = boundForm.get();
                IApiSignatureService apiSignatureService = ServiceManager.getService(IApiSignatureService.NAME, IApiSignatureService.class);

                // Check if the application already exists
                if (apiSignatureService.isApplicationNameExists(apiRegistrationObject.applicationName)) {
                    boundForm.reject("applicationName",
                            Msg.get("admin.api_manager.registration.name.alreadyexists.message", apiRegistrationObject.applicationName));
                    return badRequest(views.html.admin.api.create.render(boundForm));
                }

                // Attempt to create the application (we assume that the
                // exception is related to the authorizations parsing)
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Request to create an application registration with " + apiRegistrationObject);
                    }
                    apiSignatureService.setApplicationConfiguration(apiRegistrationObject.applicationName, apiRegistrationObject.description,
                            apiRegistrationObject.testable, apiRegistrationObject.authorizations);
                    Utilities.sendSuccessFlashMessage(Msg.get("admin.api_manager.registration.created.message", apiRegistrationObject.applicationName));
                    return redirect(controllers.admin.routes.ApiManagerController.index());
                } catch (Exception e) {
                    log.error("Error while creating the application", e);
                    boundForm.reject("authorizations", Msg.get("admin.api_manager.authorizations.invalid.message", e.getMessage()));
                    return badRequest(views.html.admin.api.create.render(boundForm));
                }
            }
        });
    }

    /**
     * Update an existing API registration.
     * 
     * @param originalApplicationName
     *            the name of the application
     */
    @Restrict({ @Group(IMafConstants.API_MANAGER_PERMISSION) })
    public static Promise<Result> updateRegistration(final String originalApplicationName) {
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                Form<ApiRegistrationObject> boundForm = apiRegistrationForm.bindFromRequest();
                if (boundForm.hasErrors()) {
                    return badRequest(views.html.admin.api.edit.render(boundForm, originalApplicationName));
                }
                ApiRegistrationObject apiRegistrationObject = boundForm.get();
                IApiSignatureService apiSignatureService = ServiceManager.getService(IApiSignatureService.NAME, IApiSignatureService.class);

                if (log.isDebugEnabled()) {
                    log.debug("Request to update the application registration for " + originalApplicationName + " with " + apiRegistrationObject);
                }

                // Check if the application already exists
                if (apiSignatureService.isApplicationNameExists(apiRegistrationObject.applicationName)) {
                    boundForm.reject("applicationName",
                            Msg.get("admin.api_manager.registration.name.alreadyexists.message", apiRegistrationObject.applicationName));
                    return badRequest(views.html.admin.api.edit.render(boundForm, originalApplicationName));
                }

                if (!apiRegistrationObject.applicationName.equals(originalApplicationName)) {
                    // Application name changed
                    apiSignatureService.changeApplicationConfigurationName(originalApplicationName, apiRegistrationObject.applicationName);
                }

                // Attempt to update the application (we assume that the
                // exception is related to the authorizations parsing)
                try {
                    apiSignatureService.setApplicationConfiguration(apiRegistrationObject.applicationName, apiRegistrationObject.description,
                            apiRegistrationObject.testable, apiRegistrationObject.authorizations);
                } catch (Exception e) {
                    log.error("Error while updating the application", e);
                    boundForm.reject("authorizations", Msg.get("admin.api_manager.authorizations.invalid.message", e.getMessage()));
                    return badRequest(views.html.admin.api.edit.render(boundForm, originalApplicationName));
                }

                Utilities.sendSuccessFlashMessage(Msg.get("admin.api_manager.registration.updated.message", apiRegistrationObject.applicationName));
                return redirect(controllers.admin.routes.ApiManagerController.index());
            }
        });
    }

    /**
     * Display the page which gives access to the API browser.
     */
    @Restrict({ @Group(IMafConstants.API_TESTER_PERMISSION) })
    public static Result displayBrowser() {
        return ok(views.html.admin.api.browse.render());
    }

    /**
     * Open the API browser based on Swagger.
     * 
     * @param applicationName
     *            the name of an application to test
     */
    @Restrict({ @Group(IMafConstants.API_TESTER_PERMISSION) })
    public static Result openBrowser(String applicationName) {
        String applicationKey = null;
        IApiSignatureService apiSignatureService = ServiceManager.getService(IApiSignatureService.NAME, IApiSignatureService.class);
        if (!StringUtils.isBlank(applicationName)) {
            try {
                // Get the tested application key if any
                IApiApplicationConfiguration appConfig = apiSignatureService.getApplicationConfigurationFromApplicationName(applicationName);
                applicationKey = appConfig.getSignatureGenerator().getApplicationKey();
            } catch (Exception e) {
                log.error("Error while searching for the specified application", e);
                Utilities.sendErrorFlashMessage(Msg.get("admin.api_manager.unknown.application.message", applicationName));
            }
        }
        try {
            return ok(views.html.admin.api.apibrowser.render(applicationKey, applicationName, apiSignatureService.listAuthorizedAndTestableApplications()));
        } catch (Exception e) {
            log.error("Unable to findRelease the list of testable applications", e);
        }
        return ok(views.html.admin.api.apibrowser.render(applicationKey, applicationName, null));
    }

    /**
     * A transfer object used to validate the creation or update of API
     * registrations.
     * 
     * @author Pierre-Yves Cloux
     */
    public static class ApiRegistrationObject {
        private static final int MINIMUM_APPLICATION_NAME_LENGTH = 3;

        @Required
        @MinLength(value = MINIMUM_APPLICATION_NAME_LENGTH, message = "object.api_registration.applicationName.invalid")
        @MaxLength(value = IModelConstants.MEDIUM_STRING, message = "object.api_registration.applicationName.invalid")
        public String applicationName;
        @Required
        @MaxLength(value = IModelConstants.LARGE_STRING, message = "object.api_registration.description.invalid")
        public String description;
        @Required
        public String authorizations = IApiSignatureService.DEFAULT_AUTHORIZATION;
        public boolean testable;

        /**
         * Default constructor.
         */
        public ApiRegistrationObject() {
            super();
        }

        /**
         * Fill the form with the config values.
         * 
         * @param appConfig
         *            the application config
         */
        public ApiRegistrationObject(IApiApplicationConfiguration appConfig) {
            this.applicationName = appConfig.getApplicationName();
            this.description = appConfig.getDescription();
            this.testable = appConfig.isTestable();
            this.authorizations = appConfig.getApiAuthorizationsAsString();
        }

        @Override
        public String toString() {
            return "ApiRegistrationObject [applicationName=" + applicationName + ", description=" + description + ", testable=" + testable
                    + ", authorizations=" + authorizations + "]";
        }
    }
}
