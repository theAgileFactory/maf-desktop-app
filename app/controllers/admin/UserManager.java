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

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;

import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import controllers.ControllersUtils;
import dao.pmo.ActorDao;
import framework.commons.IFrameworkConstants;
import framework.services.account.AccountManagementException;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.notification.INotificationManagerPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.services.storage.IPersonalStoragePlugin;
import framework.services.system.ISysAdminUtils;
import framework.utils.EmailUtils;
import framework.utils.IColumnFormatter;
import framework.utils.ISelectableValueHolder;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Table.ColumnDef.SorterType;
import framework.utils.TableExcelRenderer;
import framework.utils.Utilities;
import models.framework_models.account.NotificationCategory;
import models.framework_models.account.NotificationCategory.Code;
import models.framework_models.account.SystemLevelRoleType;
import models.framework_models.parent.IModelConstants;
import models.pmo.Actor;
import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import scala.concurrent.duration.Duration;
import services.licensesmanagement.ILicensesManagementService;
import services.licensesmanagement.LicensesManagementServiceImpl;

/**
 * The administration interface which is to be used to create, update, delete
 * (or unactivate) a user.
 * 
 * @author Pierre-Yves Cloux
 * 
 */
@Restrict({ @Group(IMafConstants.ADMIN_USER_ADMINISTRATION_PERMISSION) })
public class UserManager extends Controller {
    @Inject
    private IAccountManagerPlugin accountManagerPlugin;
    @Inject
    private ILicensesManagementService licensesManagementService ;
    @Inject
    private INotificationManagerPlugin notificationManagerPlugin ;
    @Inject
    private IPersonalStoragePlugin personalStoragePlugin;
    @Inject
    private IUserSessionManagerPlugin userSessionManagerPlugin ;
    @Inject
    private ISysAdminUtils sysAdminUtils;
    
    private static Logger.ALogger log = Logger.of(UserManager.class);
    private static Form<UserSeachFormData> userSearchForm = Form.form(UserSeachFormData.class);
    private static Form<UserAccountFormData> basicDataUpdateForm = Form.form(UserAccountFormData.class, UserAccountFormData.BasicDataChangeGroup.class);
    private static Form<UserAccountFormData> rolesUpdateForm = Form.form(UserAccountFormData.class, UserAccountFormData.RolesChangeGroup.class);
    private static Form<UserAccountFormData> mailUpdateForm = Form.form(UserAccountFormData.class, UserAccountFormData.EmailChangeGroup.class);
    private static Form<UserAccountFormData> creationForm = Form.form(UserAccountFormData.class, UserAccountFormData.UserCreationGroup.class);
    private static Form<UserAccountFormData> changeAccountTypeForm = Form.form(UserAccountFormData.class, UserAccountFormData.AccountTypeChangeGroup.class);
    private static Form<SelectActorForUserFormData> selectActorForUserFormData = Form.form(SelectActorForUserFormData.class);

    // Options to be used with the search form (radio buttons)
    public static final String UID_SEARCH_OPTION = "uid";
    public static final String MAIL_SEARCH_OPTION = "mail";
    public static final String FULLNAME_SEARCH_OPTION = "fullName";

    // Table to display search results
    private static Table<IUserAccount> tableTemplate = new Table<IUserAccount>() {
        {
            this.addColumn("firstName", "firstName", "object.user_account.first_name.label", SorterType.NONE);
            this.addColumn("lastName", "lastName", "object.user_account.last_name.label", SorterType.NONE);
            this.addColumn("mail", "mail", "object.user_account.email.label", SorterType.NONE);

            this.setLineAction(new IColumnFormatter<IUserAccount>() {
                @Override
                public String apply(IUserAccount userAccount, Object value) {
                    return routes.UserManager.displayUser(userAccount.getUid()).url();
                }
            });

            this.setIdFieldName("uid");
        }
    };

    /**
     * Display the form to be used to search a user to be later modified.
     */
    public Result displayUserSearchForm() {
        Form<UserSeachFormData> userSearchFormLoaded = userSearchForm.fill(new UserSeachFormData());
        return ok(views.html.admin.usermanager.usermanager_search.render(Msg.get("admin.user_manager.sidebar.search"), userSearchFormLoaded));
    }

    /**
     * Find a user using either a UID or a mail address.<br/>
     * The option is provided as a parameter of the form.
     */
    public Result findUser() {
        Form<UserSeachFormData> boundForm = userSearchForm.bindFromRequest();
        if (boundForm.hasErrors()) {
            return badRequest(views.html.admin.usermanager.usermanager_search.render(Msg.get("admin.user_manager.sidebar.search"), boundForm));
        }
        
        UserSeachFormData searchFormData = boundForm.get();

        // Reject if search by UID but no uid
        if (searchFormData.searchType.equals(UID_SEARCH_OPTION)
                && (searchFormData.uid == null || searchFormData.uid.trim().equals("") || searchFormData.uid.indexOf('*') != -1)) {
            boundForm.reject("uid", Msg.get("error.required"));
            return badRequest(views.html.admin.usermanager.usermanager_search.render(Msg.get("admin.user_manager.sidebar.search"), boundForm));
        }
        // Reject if search by UID but no uid
        if (searchFormData.searchType.equals(MAIL_SEARCH_OPTION)
                && (searchFormData.mail == null || searchFormData.mail.trim().equals("") || searchFormData.mail.indexOf('*') != -1)) {
            boundForm.reject("mail", Msg.get("error.required"));
            return badRequest(views.html.admin.usermanager.usermanager_search.render(Msg.get("admin.user_manager.sidebar.search"), boundForm));
        }
        // Reject if search by full name but no full name
        if (searchFormData.searchType.equals(FULLNAME_SEARCH_OPTION) && (searchFormData.fullName == null || searchFormData.fullName.trim().equals(""))) {
            boundForm.reject("fullName", Msg.get("error.required"));
            return badRequest(views.html.admin.usermanager.usermanager_search.render(Msg.get("admin.user_manager.sidebar.search"), boundForm));
        }

        IUserAccount foundUser = null;
        if (searchFormData.searchType.equals(UID_SEARCH_OPTION)) {
            // Search by uid
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Search user by uid with " + searchFormData.uid);
                }
                IUserAccount userAccount = getAccountManagerPlugin().getUserAccountFromUid(searchFormData.uid);
                if (userAccount == null || userAccount.isMarkedForDeletion() || !userAccount.isDisplayed()) {
                    boundForm.reject("uid", Msg.get("admin.user_manager.search.not_found", searchFormData.uid));
                    return badRequest(views.html.admin.usermanager.usermanager_search.render(Msg.get("admin.user_manager.sidebar.search"), boundForm));
                }
                foundUser = userAccount;
            } catch (AccountManagementException e) {
                return ControllersUtils.logAndReturnUnexpectedError(e, log);
            }
        }
        if (searchFormData.searchType.equals(MAIL_SEARCH_OPTION)) {
            // Search by e-mail
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Search user by mail with " + searchFormData.mail);
                }
                IUserAccount userAccount = getAccountManagerPlugin().getUserAccountFromEmail(searchFormData.mail);
                if (userAccount == null || userAccount.isMarkedForDeletion() || !userAccount.isDisplayed()) {
                    boundForm.reject("mail", Msg.get("admin.user_manager.search.not_found", searchFormData.mail));
                    return badRequest(views.html.admin.usermanager.usermanager_search.render(Msg.get("admin.user_manager.sidebar.search"), boundForm));
                }
                foundUser = userAccount;
            } catch (AccountManagementException e) {
                return ControllersUtils.logAndReturnUnexpectedError(e, log);
            }
        }
        if (searchFormData.searchType.equals(FULLNAME_SEARCH_OPTION)) {
            // Search by last name
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Search user by name with " + searchFormData.fullName);
                }
                List<IUserAccount> userAccounts = getAccountManagerPlugin().getUserAccountsFromName(searchFormData.fullName);
                if (userAccounts == null || userAccounts.size() == 0) {
                    boundForm.reject("fullName", Msg.get("admin.user_manager.search.not_found", searchFormData.fullName));
                    return badRequest(views.html.admin.usermanager.usermanager_search.render(Msg.get("admin.user_manager.sidebar.search"), boundForm));
                }
                if (userAccounts.size() > 1) {
                    // Filter the accounts marked for deletion
                    CollectionUtils.filter(userAccounts, new Predicate() {
                        @Override
                        public boolean evaluate(Object userAccountAsObject) {
                            IUserAccount userAccount = (IUserAccount) userAccountAsObject;
                            try {
                                return !userAccount.isMarkedForDeletion() && userAccount.isDisplayed();
                            } catch (AccountManagementException e) {
                                return false;
                            }
                        }
                    });
                    Table<IUserAccount> foundUserAccountsTable = tableTemplate.fill(userAccounts);
                    // Multiple user found, display the selection list
                    return ok(views.html.admin.usermanager.usermanager_search_results.render(Msg.get("admin.user_manager.search.result.title"),
                            foundUserAccountsTable));
                }
                foundUser = userAccounts.get(0); // Only one user
            } catch (AccountManagementException e) {
                return ControllersUtils.logAndReturnUnexpectedError(e, log);
            }
        }
        return ok(views.html.admin.usermanager.usermanager_display.render(Msg.get("admin.user_manager.sidebar.search"),
                getAccountManagerPlugin().isAuthenticationRepositoryMasterMode(), foundUser));
    }

    /**
     * Display the user associated with the specified user id.
     * 
     * @param uid
     *            a unique user id
     */
    public Result displayUser(String uid) {
        
        IUserAccount userAccount = null;
        try {
            userAccount = getAccountManagerPlugin().getUserAccountFromUid(uid);
            // Do not display the users marked for deletion
            if (userAccount == null || userAccount.isMarkedForDeletion() || !userAccount.isDisplayed()) {
                Utilities.sendErrorFlashMessage(Msg.get("admin.user_manager.unknown_user"));
                return displayUserSearchForm();
            }
        } catch (AccountManagementException e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
        return ok(views.html.admin.usermanager.usermanager_display.render(Msg.get("admin.user_manager.sidebar.search"),
                getAccountManagerPlugin().isAuthenticationRepositoryMasterMode(), userAccount));
    }

    /**
     * Display the form to edit the user basic data.
     * 
     * @param uid
     *            a unique user id
     */
    public Result editBasicData(String uid) {
        try {
            IUserAccount account = getAccountManagerPlugin().getUserAccountFromUid(uid);
            if (account == null || account.isMarkedForDeletion() || !account.isDisplayed()) {
                Utilities.sendErrorFlashMessage(Msg.get("admin.user_manager.unknown_user"));
                return displayUserSearchForm();
            }
            Form<UserAccountFormData> userAccountFormLoaded = basicDataUpdateForm.fill(new UserAccountFormData(account));
            return ok(views.html.admin.usermanager.usermanager_editbasicdata.render(Msg.get("admin.user_manager.edit_basic_data.title"),
                    userAccountFormLoaded));
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Display the form to edit the user account type.
     * 
     * @param uid
     *            a unique user id
     */
    public Result editUserAccountType(String uid) {
        try {
            IUserAccount account = getAccountManagerPlugin().getUserAccountFromUid(uid);
            if (account == null || account.isMarkedForDeletion() || !account.isDisplayed()) {
                Utilities.sendErrorFlashMessage(Msg.get("admin.user_manager.unknown_user"));
                return displayUserSearchForm();
            }
            Form<UserAccountFormData> userAccountFormLoaded = changeAccountTypeForm.fill(new UserAccountFormData(account));
            return ok(views.html.admin.usermanager.usermanager_editaccounttype.render(Msg.get("admin.user_manager.edit_account_type.title"),
                    IUserAccount.AccountType.getValueHolder(), userAccountFormLoaded));
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Save the modified user account type.
     */
    public Result saveUserAccountType() {
        try {
            Form<UserAccountFormData> boundForm = changeAccountTypeForm.bindFromRequest();
            if (boundForm.hasErrors()) {
                return badRequest(views.html.admin.usermanager.usermanager_editaccounttype.render(Msg.get("admin.user_manager.edit_basic_data.title"),
                        IUserAccount.AccountType.getValueHolder(), boundForm));
            }
            UserAccountFormData accountDataForm = boundForm.get();
            getAccountManagerPlugin().updateUserAccountType(accountDataForm.uid, IUserAccount.AccountType.valueOf(accountDataForm.accountType));

            getLicensesManagementService().updateConsumedUsers();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.user_manager.update.success"));
            return redirect(controllers.admin.routes.UserManager.displayUser(accountDataForm.uid));
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Save the modified basic data.
     */
    public Result saveBasicData() {
        try {
            Form<UserAccountFormData> boundForm = basicDataUpdateForm.bindFromRequest();
            if (boundForm.hasErrors()) {
                return badRequest(
                        views.html.admin.usermanager.usermanager_editbasicdata.render(Msg.get("admin.user_manager.edit_basic_data.title"), boundForm));
            }
            UserAccountFormData accountDataForm = boundForm.get();
            getAccountManagerPlugin().updateBasicUserData(accountDataForm.uid, accountDataForm.firstName, accountDataForm.lastName);
            getAccountManagerPlugin().updatePreferredLanguage(accountDataForm.uid, accountDataForm.preferredLanguage);
            Utilities.sendSuccessFlashMessage(Msg.get("admin.user_manager.update.success"));
            return redirect(controllers.admin.routes.UserManager.displayUser(accountDataForm.uid));
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Display the e-mail address edition form.
     * 
     * @param uid
     *            a unique user id
     */
    public Result editMail(String uid) {
        try {
            IUserAccount account = getAccountManagerPlugin().getUserAccountFromUid(uid);
            if (account == null || account.isMarkedForDeletion() || !account.isDisplayed()) {
                Utilities.sendErrorFlashMessage(Msg.get("admin.user_manager.unknown_user"));
                return displayUserSearchForm();
            }
            Form<UserAccountFormData> userAccountFormLoaded = mailUpdateForm.fill(new UserAccountFormData(account));
            return ok(views.html.admin.usermanager.usermanager_editmail.render(Msg.get("admin.user_manager.edit_email.title"), userAccountFormLoaded));
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Save the new e-mail address.
     */
    public Result saveMail() {
        try {
            Form<UserAccountFormData> boundForm = mailUpdateForm.bindFromRequest();
            if (boundForm.hasErrors()) {
                return badRequest(views.html.admin.usermanager.usermanager_editmail.render(Msg.get("admin.user_manager.edit_email.title"), boundForm));
            }

            UserAccountFormData accountDataForm = boundForm.get();

            if (getAccountManagerPlugin().isMailExistsInAuthenticationBackEnd(accountDataForm.mail)) {
                IUserAccount userAccount = getAccountManagerPlugin().getUserAccountFromEmail(accountDataForm.mail);
                if (userAccount == null || !userAccount.getUid().equals(accountDataForm.uid)) {
                    boundForm.reject("mail", Msg.get("object.user_account.email.already_exists"));
                    return badRequest(views.html.admin.usermanager.usermanager_editmail.render(Msg.get("admin.user_manager.edit_email.title"), boundForm));
                }
            }

            getAccountManagerPlugin().updateMail(accountDataForm.uid, accountDataForm.mail);
            Utilities.sendSuccessFlashMessage(Msg.get("admin.user_manager.update.success"));
            return redirect(controllers.admin.routes.UserManager.displayUser(accountDataForm.uid));
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Reset the password for the specified user.
     * 
     * @param uid
     *            the unique id for a user
     */
    public Result resetPassword(String uid) {
        try {
            resetUserPasswordFromUid(getAccountManagerPlugin(),uid, true);
            return redirect(controllers.admin.routes.UserManager.displayUser(uid));
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * The method to trigger a password reset for the specified uid.<br/>
     * This sends an e-mail to the user with a validation key.
     * 
     * @param accountManagerPlugin a user account manager reference
     * @param uid
     *            a unique user id
     * @param eraseCurrentPassword
     *            true if the current user password is cleared before sending
     *            the password reset message
     * @throws Exception
     * @throws AccountManagementException
     */
    public static void resetUserPasswordFromUid(IAccountManagerPlugin accountManagerPlugin,String uid, boolean eraseCurrentPassword) throws Exception, AccountManagementException {
        if (!accountManagerPlugin.isAuthenticationRepositoryMasterMode()) {
            return;
        }
        IUserAccount userAccount = accountManagerPlugin.getUserAccountFromUid(uid);
        if (userAccount == null || userAccount.isMarkedForDeletion() || !userAccount.isDisplayed()) {
            throw new Exception("user not found");
        }
        resetUserPassword(accountManagerPlugin, userAccount, eraseCurrentPassword);
        Utilities.sendSuccessFlashMessage(Msg.get("admin.user_manager.reset_password.success"));
    }

    /**
     * The method to trigger a password reset for the specified mail.<br/>
     * This sends an e-mail to the user with a validation key.
     * 
     * @param accountManagerPlugin a user account manager reference
     * @param mail
     *            a user e-mail
     * @param eraseCurrentPassword
     *            true if the current user password is cleared before sending
     *            the password reset message
     * @throws Exception
     * @throws AccountManagementException
     * @return a boolean (false if the e-mail does not exists)
     */
    public static boolean resetUserPasswordFromEmail(IAccountManagerPlugin accountManagerPlugin,String mail, boolean eraseCurrentPassword) {
        try {
            IUserAccount userAccount = accountManagerPlugin.getUserAccountFromEmail(mail);
            if (userAccount == null || userAccount.isMarkedForDeletion() || !userAccount.isDisplayed()) {
                return false;
            }
            resetUserPassword(accountManagerPlugin, userAccount, eraseCurrentPassword);
            return true;
        } catch (Exception e) {
            log.error("Exception following a password reset attempt with mail " + mail, e);
            return false;
        }
    }

    /**
     * Reset the user password of the user associated with the specified
     * account.
     * 
     * @param accountManagerPlugin
     *            the account manager plugin
     * @param userAccount
     *            the user account
     * @param eraseCurrentPassword
     *            true if the current user password is cleared before sending
     *            the password reset message
     */
    private static void resetUserPassword(IAccountManagerPlugin accountManagerPlugin, IUserAccount userAccount, boolean eraseCurrentPassword)
            throws AccountManagementException {
        // Generate a random password
        String validationData = RandomStringUtils.randomAlphanumeric(12);
        String validationKey = accountManagerPlugin.getValidationKey(userAccount.getUid(), validationData);
        if (eraseCurrentPassword) {
            accountManagerPlugin.updatePassword(userAccount.getUid(), validationData);
        }

        // Send an e-mail for reseting password
        String resetPasswordUrl = Utilities.getPreferenceElseConfigurationValue(Play.application().configuration(),IFrameworkConstants.PUBLIC_URL_PREFERENCE, "maf.public.url")
                + controllers.admin.routes.PasswordReset.displayPasswordResetForm(userAccount.getUid(), validationKey).url();
        EmailUtils.sendEmail(Msg.get("admin.user_manager.reset_password.mail.subject"), play.Configuration.root().getString("maf.email.from"),
                Utilities.renderViewI18n("views.html.mail.account_password_reset_html", play.Configuration.root().getString("maf.platformName"),
                        userAccount.getFirstName() + " " + userAccount.getLastName(), resetPasswordUrl).body(),
                userAccount.getMail());
    }

    /**
     * Display the form which is to be used to edit the roles of the specified
     * user.
     * 
     * @param uid
     *            the unique ID of the user to edit
     */
    public Result editRoles(String uid) {
        try {
            
            IUserAccount account = getAccountManagerPlugin().getUserAccountFromUid(uid);
            Form<UserAccountFormData> userAccountFormLoaded = rolesUpdateForm.fill(new UserAccountFormData(account));
            return ok(views.html.admin.usermanager.usermanager_editroles.render(Msg.get("admin.user_manager.edit_roles.title"),
                    SystemLevelRoleType.getAllActiveRolesAsValueHolderCollection(), userAccountFormLoaded));
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Update the user account with the roles specified by the administrator.
     */
    public Result saveRoles() {
        try {
            
            Form<UserAccountFormData> boundForm = rolesUpdateForm.bindFromRequest();
            if (boundForm.hasErrors()) {
                return badRequest(views.html.admin.usermanager.usermanager_editmail.render(Msg.get("admin.user_manager.edit_roles.title"), boundForm));
            }
            UserAccountFormData accountDataForm = boundForm.get();

            // Remove the null values which may be present in the list (see the
            // view to understand)
            accountDataForm.systemLevelRoleTypes.removeAll(Collections.singleton(null));
            getAccountManagerPlugin().overwriteSystemLevelRoleTypes(accountDataForm.uid, accountDataForm.systemLevelRoleTypes);

            Utilities.sendSuccessFlashMessage(Msg.get("admin.user_manager.update.success"));
            return redirect(controllers.admin.routes.UserManager.displayUser(accountDataForm.uid));
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Lock or unlock the specified account.
     * 
     * @param uid
     *            a user unique id
     * @param activationStatus
     *            the status (true to unlock, false to lock)
     */
    public Result changeActivationStatus(String uid, boolean activationStatus) {

        

        IUserAccount userAccount = null;
        try {
            userAccount = getAccountManagerPlugin().getUserAccountFromUid(uid);
            if (userAccount == null || userAccount.isMarkedForDeletion() || !userAccount.isDisplayed()) {
                Utilities.sendErrorFlashMessage(Msg.get("admin.user_manager.unknown_user"));
                return displayUserSearchForm();
            }
        } catch (AccountManagementException e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }

        try {
            getAccountManagerPlugin().updateActivationStatus(uid, activationStatus);
            Utilities.sendSuccessFlashMessage(Msg.get("admin.user_manager.change_status.success", uid));
        } catch (AccountManagementException e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }

        // Notify the user with the lock/unlock action
        try {
            EmailUtils
                    .sendEmail(Msg.get("admin.user_manager.change_status.mail.subject"), play.Configuration.root().getString("maf.email.from"),
                            Utilities.renderViewI18n("views.html.mail.account_activation_status_changed_html",
                                    play.Configuration.root().getString("maf.platformName"), userAccount.getFirstName() + " " + userAccount.getLastName(),
                                    userAccount.isActive()).body(),
                            userAccount.getMail());
        } catch (Exception e) {
            log.error("Unexpected error while sending an e-mail notification", e);
        }

        return redirect(routes.UserManager.displayUserSearchForm());
    }

    /**
     * Display the creation form for a new user.
     */
    public Result displayUserCreationForm() {

        if (!getLicensesManagementService().canCreateUser()) {
            Utilities.sendErrorFlashMessage(Msg.get("licenses_management.cannot_create_user"));
        }

        Form<UserAccountFormData> userAccountFormLoaded = creationForm.fill(new UserAccountFormData());
        return ok(views.html.admin.usermanager.usermanager_create.render(Msg.get("admin.user_manager.sidebar.create"),
                SystemLevelRoleType.getAllActiveRolesAsValueHolderCollection(), IUserAccount.AccountType.getValueHolder(), userAccountFormLoaded));
    }

    /**
     * Create the new user from the data provided in the creation form.
     */
    public Result saveNewUser() {

        if (!getLicensesManagementService().canCreateUser()) {
            Utilities.sendErrorFlashMessage(Msg.get("licenses_management.cannot_create_user"));
            return redirect(controllers.admin.routes.UserManager.displayUserCreationForm());
        }

        try {
            
            Form<UserAccountFormData> boundForm = creationForm.bindFromRequest();
            if (boundForm.hasErrors()) {
                return badRequest(views.html.admin.usermanager.usermanager_create.render(Msg.get("admin.user_manager.sidebar.create"),
                        SystemLevelRoleType.getAllActiveRolesAsValueHolderCollection(), IUserAccount.AccountType.getValueHolder(), boundForm));
            }
            UserAccountFormData accountDataForm = boundForm.get();

            // Check if the user already exists
            IUserAccount account = getAccountManagerPlugin().getUserAccountFromUid(accountDataForm.uid);
            if (account != null) {
                boundForm.reject("uid", Msg.get("object.user_account.uid.already_exists"));
                return badRequest(views.html.admin.usermanager.usermanager_create.render(Msg.get("admin.user_manager.sidebar.create"),
                        SystemLevelRoleType.getAllActiveRolesAsValueHolderCollection(), IUserAccount.AccountType.getValueHolder(), boundForm));
            }

            // Check if the mail already exists in the backend
            if (getAccountManagerPlugin().isMailExistsInAuthenticationBackEnd(accountDataForm.mail)) {
                boundForm.reject("mail", Msg.get("object.user_account.email.already_exists"));
                return badRequest(views.html.admin.usermanager.usermanager_create.render(Msg.get("admin.user_manager.sidebar.create"),
                        SystemLevelRoleType.getAllActiveRolesAsValueHolderCollection(), IUserAccount.AccountType.getValueHolder(), boundForm));
            }

            // Remove the null values which may be present in the list (see the
            // view to understand)
            accountDataForm.systemLevelRoleTypes.removeAll(Collections.singleton(null));

            // Create a new user
            getAccountManagerPlugin().createNewUserAccount(accountDataForm.uid, IUserAccount.AccountType.valueOf(accountDataForm.accountType),
                    accountDataForm.firstName, accountDataForm.lastName, accountDataForm.mail, accountDataForm.systemLevelRoleTypes);

            getLicensesManagementService().updateConsumedUsers();

            // update the preferred language
            getAccountManagerPlugin().updatePreferredLanguage(accountDataForm.uid, accountDataForm.preferredLanguage);

            Utilities.sendSuccessFlashMessage(Msg.get("admin.user_manager.create.success"));

            // If in master mode, the password is managed by the system
            if (getAccountManagerPlugin().isAuthenticationRepositoryMasterMode()) {
                // Generate a random password
                String password = RandomStringUtils.randomAlphanumeric(12);
                String validationKey = getAccountManagerPlugin().getValidationKey(accountDataForm.uid, password);
                getAccountManagerPlugin().updatePassword(accountDataForm.uid, password);

                // Notify account creation & password setup
                String resetPasswordUrl = Utilities.getPreferenceElseConfigurationValue(Play.application().configuration(),IFrameworkConstants.PUBLIC_URL_PREFERENCE, "maf.public.url")
                        + controllers.admin.routes.PasswordReset.displayPasswordResetForm(accountDataForm.uid, validationKey).url();
                EmailUtils.sendEmail(Msg.get("admin.user_manager.create.mail.subject"), play.Configuration.root().getString("maf.email.from"),
                        Utilities.renderViewI18n("views.html.mail.account_creation_html", getAccountManagerPlugin().isAuthenticationRepositoryMasterMode(),
                                play.Configuration.root().getString("maf.platformName"), accountDataForm.firstName + " " + accountDataForm.lastName,
                                resetPasswordUrl, accountDataForm.uid).body(),
                        accountDataForm.mail);
            }

            // check if there is an existing actor for the uid
            Actor actor = ActorDao.getActorByUid(accountDataForm.uid);
            if (actor == null) {
                return ok(views.html.admin.usermanager.usermanager_create_actor.render(accountDataForm.uid, selectActorForUserFormData));
            } else {
                return redirect(controllers.admin.routes.UserManager.displayUser(accountDataForm.uid));
            }
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Create a actor from the user data.
     */
    public Result createActorFromUser() {

        // get the uid
        DynamicForm requestData = Form.form().bindFromRequest();
        String uid = requestData.get("uid");

        Actor actor = ActorDao.getActorByUid(uid);
        if (actor != null) {
            return redirect(controllers.admin.routes.UserManager.displayUser(uid));
        }

        try {

            
            IUserAccount account = getAccountManagerPlugin().getUserAccountFromUid(uid);
            if (account == null || account.isMarkedForDeletion() || !account.isDisplayed()) {
                Utilities.sendErrorFlashMessage(Msg.get("admin.user_manager.unknown_user"));
                return displayUserSearchForm();
            }

            Actor newActor = new Actor();
            newActor.firstName = account.getFirstName();
            newActor.isActive = true;
            newActor.lastName = account.getLastName();
            newActor.mail = account.getMail();
            newActor.uid = uid;
            newActor.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.user_manager.create.actor.create.successful"));
            return redirect(controllers.admin.routes.UserManager.displayUser(uid));

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }

    }

    /**
     * Associate an existing actor to a user.
     */
    public Result selectActorForUser() {

        // bind the form
        Form<SelectActorForUserFormData> boundForm = selectActorForUserFormData.bindFromRequest();

        // get the uid
        String uid = boundForm.data().get("uid");

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.usermanager.usermanager_create_actor.render(uid, boundForm));
        }

        SelectActorForUserFormData formData = boundForm.get();

        Actor actor = ActorDao.getActorById(formData.actor);
        if (actor != null && (actor.uid == null || actor.uid.equals(""))) {
            actor.uid = uid;
            actor.save();
            Utilities.sendSuccessFlashMessage(Msg.get("admin.user_manager.create.actor.select.successful"));
        }

        return redirect(controllers.admin.routes.UserManager.displayUser(uid));
    }

    /**
     * Export as excel the list of MAF users.
     */
    public Result exportAsExcel() {
        final String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());

        final String fileName = String.format("users_%1$td_%1$tm_%1$ty_%1$tH-%1$tM-%1$tS.xlsx", new Date());

        final String successTitle = Msg.get("excel.export.success.title");
        final String successMessage = Msg.get("excel.export.success.message", fileName, Msg.get("admin.user_manager.sidebar.export"));
        final String failureTitle = Msg.get("excel.export.failure.title");
        final String failureMessage = Msg.get("excel.export.failure.message", Msg.get("admin.user_manager.sidebar.export"));

        final String uidLabel = Msg.get("object.user_account.uid.label");
        final String firstNameLabel = Msg.get("object.user_account.first_name.label");
        final String lastNameLabel = Msg.get("object.user_account.last_name.label");
        final String emailLabel = Msg.get("object.user_account.email.label");
        final String isActiveLabel = Msg.get("object.user_account.is_active.label");
        final String rolesLabel = Msg.get("object.user_account.roles.label");
        final String permissionsLabel = Msg.get("object.user_account.permissions.label");

        getSysAdminUtils().scheduleOnce(false, "UserManager Excel Export", Duration.create(0, TimeUnit.MILLISECONDS), new Runnable() {
            @Override
            public void run() {
                Table<IUserAccount> exportTemplate = new Table<IUserAccount>() {
                    {
                        this.addColumn("uid", "uid", uidLabel, SorterType.NONE);
                        this.addColumn("firstName", "firstName", firstNameLabel, SorterType.NONE);
                        this.addColumn("lastName", "lastName", lastNameLabel, SorterType.NONE);
                        this.addColumn("mail", "mail", emailLabel, SorterType.NONE);
                        this.addColumn("active", "active", isActiveLabel, SorterType.NONE);
                        this.addColumn("mafUid", "mafUid", "BizDock Id", SorterType.NONE);
                        this.addColumn("systemLevelRoleTypeNames", "uid", rolesLabel, SorterType.NONE);
                        this.setJavaColumnFormatter("systemLevelRoleTypeNames", new IColumnFormatter<IUserAccount>() {
                            @Override
                            public String apply(IUserAccount userAccount, Object value) {
                                return StringUtils.join(userAccount.getSystemLevelRoleTypeNames(), ",");
                            }
                        });
                        this.addColumn("systemPermissionNames", "uid", permissionsLabel, SorterType.NONE);
                        this.setJavaColumnFormatter("systemPermissionNames", new IColumnFormatter<IUserAccount>() {
                            @Override
                            public String apply(IUserAccount userAccount, Object value) {
                                List<String> permissions = new ArrayList<>();
                                for (Role role : userAccount.getSelectableRoles()) {
                                    permissions.add(role.getName());
                                }
                                return StringUtils.join(permissions, ",");
                            }
                        });
                        this.setIdFieldName("uid");
                    }
                };

                try {

                    Table<IUserAccount> exportTable = exportTemplate.fill(getAccountManagerPlugin().getUserAccountsFromName("*"));
                    byte[] excelFile = TableExcelRenderer.renderFormatted(exportTable);

                    OutputStream out = getPersonalStoragePlugin().createNewFile(uid, fileName);
                    IOUtils.copy(new ByteArrayInputStream(excelFile), out);

                    getNotificationManagerPlugin().sendNotification(uid, NotificationCategory.getByCode(Code.DOCUMENT), successTitle, successMessage,
                            controllers.my.routes.MyPersonalStorage.index().url());

                } catch (Exception e) {

                    log.error("Unable to export the users excel file", e);

                    getNotificationManagerPlugin().sendNotification(uid, NotificationCategory.getByCode(Code.ISSUE), failureTitle, failureMessage,
                            controllers.admin.routes.UserManager.displayUserSearchForm().url());

                }

            }
        });

        Utilities.sendSuccessFlashMessage(Msg.get("admin.user_manager.export.success"));

        return redirect(controllers.admin.routes.UserManager.displayUserSearchForm());

    }

    /**
     * Delete the specified user.
     * 
     * @param uid
     *            a unique user id
     */
    public Result deleteUser(String uid) {
        
        try {
            getAccountManagerPlugin().deleteAccount(uid);

            Utilities.sendSuccessFlashMessage(Msg.get("admin.user_manager.delete.success", uid));

            getLicensesManagementService().updateConsumedUsers();

            getNotificationManagerPlugin().sendNotificationWithPermission(IMafConstants.ADMIN_USER_ADMINISTRATION_PERMISSION,
                    NotificationCategory.getByCode(Code.USER_MANAGEMENT), Msg.get("admin.user_manager.delete.notification.title"),
                    Msg.get("admin.user_manager.delete.notification.message", uid, new Date().toString()),
                    controllers.admin.routes.UserManager.displayUserSearchForm().url());
        } catch (AccountManagementException e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
        return redirect(routes.UserManager.displayUserSearchForm());
    }

    private IAccountManagerPlugin getAccountManagerPlugin() {
        return accountManagerPlugin;
    }

    private ILicensesManagementService getLicensesManagementService() {
        return licensesManagementService;
    }

    private INotificationManagerPlugin getNotificationManagerPlugin() {
        return notificationManagerPlugin;
    }

    private IPersonalStoragePlugin getPersonalStoragePlugin() {
        return personalStoragePlugin;
    }

    private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return userSessionManagerPlugin;
    }

    /**
     * A wrapper for the {@link SystemLevelRoleType}.<br/>
     * This one is required by the "checkboxlist" view part to be used to
     * display the list of roles to be selected.
     * 
     * @author Pierre-Yves Cloux
     */
    public static class SelectableRole implements ISelectableValueHolder<String> {
        private SystemLevelRoleType systemLevelRoleType;

        /**
         * Construct a selectable role thanks a DB entry.
         * 
         * @param systemLevelRoleType
         *            the system level role type in the DB
         */
        private SelectableRole(SystemLevelRoleType systemLevelRoleType) {
            this.systemLevelRoleType = systemLevelRoleType;
        }

        /**
         * Get the list of possible roles.
         */
        public static List<ISelectableValueHolder<String>> getListOfPossibleRoles() {
            ArrayList<ISelectableValueHolder<String>> possibleValues = new ArrayList<ISelectableValueHolder<String>>();
            List<SystemLevelRoleType> systemLevelRoleTypes = SystemLevelRoleType.getAllActiveRoles();
            if (systemLevelRoleTypes != null) {
                for (SystemLevelRoleType systemLevelRoleType : systemLevelRoleTypes) {
                    possibleValues.add(new SelectableRole(systemLevelRoleType));
                }
            }
            return possibleValues;
        }

        @Override
        public String getDescription() {
            return getSystemLevelRoleType().getDescriptionAsMessage();
        }

        @Override
        public String getName() {
            return getSystemLevelRoleType().getName();
        }

        @Override
        public String getValue() {
            return getSystemLevelRoleType().getName();
        }

        /**
         * Get the system level role type.
         */
        private SystemLevelRoleType getSystemLevelRoleType() {
            return systemLevelRoleType;
        }

        @Override
        public String getUrl() {
            return null;
        }

        @Override
        public void setUrl(String url) {
        }

        @Override
        public boolean isSelectable() {
            return getSystemLevelRoleType().selectable;
        }

        @Override
        public boolean isDeleted() {
            return getSystemLevelRoleType().deleted;
        }

        @Override
        public int compareTo(Object o) {
            @SuppressWarnings("unchecked")
            ISelectableValueHolder<String> v = (ISelectableValueHolder<String>) o;
            return this.getName().compareTo(v.getName());
        }
    }

    /**
     * The object which contains the User search parameters.
     * 
     * @author Pierre-Yves Cloux
     * 
     */
    public static class UserSeachFormData {
        public String uid;
        public String mail;
        public String fullName;

        @Required
        public String searchType;

        /**
         * Default constructor.
         */
        public UserSeachFormData() {
            super();
        }
    }

    /**
     * The fields to select an actor for a user.
     * 
     * @author Johann Kohler
     * 
     */
    public static class SelectActorForUserFormData {

        public String uid;

        @Required
        public Long actor;

        /**
         * Default constructor.
         */
        public SelectActorForUserFormData() {
        }

    }

    /**
     * The data to be used to collect changes on a user account information.
     * 
     * @author Pierre-Yves Cloux
     * 
     */
    public static class UserAccountFormData {

        /**
         * The group for the change email form.
         * 
         * @author Pierre-Yves Cloux
         */
        public interface EmailChangeGroup {
        }

        /**
         * The group for the change basic data form.
         * 
         * @author Pierre-Yves Cloux
         */
        public interface BasicDataChangeGroup {
        }

        /**
         * The group for the change roles form.
         * 
         * @author Pierre-Yves Cloux
         */
        public interface RolesChangeGroup {
        }

        /**
         * The group for the create user form.
         * 
         * @author Pierre-Yves Cloux
         */
        public interface UserCreationGroup {
        }

        /**
         * The group for the update account type form.
         * 
         * @author Pierre-Yves Cloux
         */
        public interface AccountTypeChangeGroup {
        }

        /**
         * Default constructor.
         */
        public UserAccountFormData() {
            super();

        }

        /**
         * Creates a form data holder from a {@link IUserAccount}.
         * 
         * @param userAccount
         *            the user account
         */
        public UserAccountFormData(IUserAccount userAccount) {
            this.uid = userAccount.getUid();
            this.firstName = userAccount.getFirstName();
            this.lastName = userAccount.getLastName();
            this.mail = userAccount.getMail();
            this.accountType = userAccount.getAccountType().name();
            // Set the permissions
            for (Role role : userAccount.getRoles()) {
                this.permissions.add(role.getName());
            }
            // Set the roles
            for (String systemLevelRoleTypes : userAccount.getSystemLevelRoleTypeNames()) {
                this.systemLevelRoleTypes.add(systemLevelRoleTypes);
            }
            this.preferredLanguage = userAccount.getPreferredLanguage() != null ? userAccount.getPreferredLanguage().toLowerCase() : null;
        }

        @Required(groups = { UserCreationGroup.class, EmailChangeGroup.class, BasicDataChangeGroup.class, RolesChangeGroup.class })
        @MinLength(value = IModelConstants.MIN_UID_LENGTH, message = "object.user_account.uid.invalid", groups = { UserCreationGroup.class })
        @MaxLength(value = 60, message = "object.user_account.uid.invalid", groups = { UserCreationGroup.class })
        @Pattern(value = IModelConstants.UID_VALIDATION_PATTERN, message = "object.user_account.uid.invalid", groups = { UserCreationGroup.class })
        public String uid;

        @Required(groups = { UserCreationGroup.class, AccountTypeChangeGroup.class })
        public String accountType;

        @Required(groups = { UserCreationGroup.class, EmailChangeGroup.class })
        @Pattern(value = IModelConstants.EMAIL_VALIDATION_PATTERN, message = "object.user_account.email.invalid",
                groups = { UserCreationGroup.class, EmailChangeGroup.class })
        public String mail;

        @Required(groups = { UserCreationGroup.class, BasicDataChangeGroup.class })
        @MinLength(value = IModelConstants.MIN_NAME_LENGTH, message = "object.user_account.first_name.invalid",
                groups = { UserCreationGroup.class, BasicDataChangeGroup.class })
        @MaxLength(value = IModelConstants.MEDIUM_STRING, message = "object.user_account.first_name.invalid",
                groups = { UserCreationGroup.class, BasicDataChangeGroup.class })
        public String firstName;

        @Required(groups = { UserCreationGroup.class, BasicDataChangeGroup.class })
        @MinLength(value = IModelConstants.MIN_NAME_LENGTH, message = "object.user_account.last_name.invalid",
                groups = { UserCreationGroup.class, BasicDataChangeGroup.class })
        @MaxLength(value = IModelConstants.MEDIUM_STRING, message = "object.user_account.last_name.invalid",
                groups = { UserCreationGroup.class, BasicDataChangeGroup.class })
        public String lastName;

        @Required(groups = { UserCreationGroup.class, BasicDataChangeGroup.class })
        public String preferredLanguage;

        public List<String> permissions = new ArrayList<String>();

        public List<String> systemLevelRoleTypes = new ArrayList<String>();
    }

    /**
     * The menu item type.
     * 
     * @author Johann Kohler
     * 
     */
    public static enum MenuItemType {
        NONE, SEARCH, CREATE;
    }

    private ISysAdminUtils getSysAdminUtils() {
        return sysAdminUtils;
    }
}
