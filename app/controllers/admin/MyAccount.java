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

import javax.inject.Inject;
import javax.persistence.Transient;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import controllers.ControllersUtils;
import framework.commons.IFrameworkConstants;
import framework.services.account.AccountManagementException;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IAuthenticationAccountReaderPlugin;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.services.email.IEmailService;
import framework.utils.EmailUtils;
import framework.utils.Msg;
import framework.utils.Utilities;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * This controller deal with the self administration of its profile by a user.
 * 
 * @author Pierre-Yves Cloux
 * 
 */
public class MyAccount extends Controller {
    @Inject
    private IAccountManagerPlugin accountManagerPlugin;
    @Inject
    private IUserSessionManagerPlugin userSessionManagerPlugin;
    @Inject 
    private IAuthenticationAccountReaderPlugin authenticationReader;
    @Inject
    private IPreferenceManagerPlugin preferenceManagerPlugin;
    @Inject 
    private II18nMessagesPlugin i18nMessagesPlugin;
    @Inject
    private Configuration configuration;
    @Inject
    private IEmailService emailService;
    
    private static Logger.ALogger log = Logger.of(MyAccount.class);
    private static Form<UserAccountFormData> basicDataUpdateForm = Form.form(UserAccountFormData.class, UserAccountFormData.BasicDataChangeGroup.class);
    private static Form<UserAccountFormData> passwordUpdateForm = Form.form(UserAccountFormData.class, UserAccountFormData.PasswordChangeGroup.class);
    private static Form<UserAccountFormData> mailUpdateForm = Form.form(UserAccountFormData.class, UserAccountFormData.EmailChangeGroup.class);

    /**
     * Display the user account information.
     */
    @SubjectPresent
    public Result display() {
        try {
            
            IUserAccount account = getCurrentUserAccount(getAccountManagerPlugin());
            return ok(views.html.admin.myaccount.myaccount_display.render(Messages.get("my.my_profile.sidebar.details"),
                    getAccountManagerPlugin().isAuthenticationRepositoryMasterMode(), getAccountManagerPlugin().isSelfMailUpdateAllowed(), account));
        } catch (AccountManagementException e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }
    }

    /**
     * Display the form to update the basic account information.
     */
    @SubjectPresent
    public Result editBasicData() {
        try {
            
            IUserAccount account = getCurrentUserAccount(getAccountManagerPlugin());
            Form<UserAccountFormData> userAccountFormLoaded = basicDataUpdateForm.fill(new UserAccountFormData(account));
            return ok(views.html.admin.myaccount.myaccount_editbasicdata.render(Messages.get("my.my_profile.sidebar.update_data"),
                    getAccountManagerPlugin().isAuthenticationRepositoryMasterMode(), getAccountManagerPlugin().isSelfMailUpdateAllowed(), userAccountFormLoaded));
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }
    }

    /**
     * Display the form to edit the e-mail address.
     */
    @SubjectPresent
    public Result editEmail() {
        try {
            
            if (!getAccountManagerPlugin().isSelfMailUpdateAllowed()) {
                throw new Exception("Not allowed to update email (either master mode or self mail update not allowed)");
            }
            IUserAccount account = getCurrentUserAccount(getAccountManagerPlugin());
            Form<UserAccountFormData> userAccountFormLoaded = mailUpdateForm.fill(new UserAccountFormData(account));
            return ok(views.html.admin.myaccount.myaccount_editmail.render(Msg.get("my.my_profile.sidebar.update_email"),
                    getAccountManagerPlugin().isAuthenticationRepositoryMasterMode(), getAccountManagerPlugin().isSelfMailUpdateAllowed(), userAccountFormLoaded));
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }
    }

    /**
     * Display the form to edit the password.
     */
    @SubjectPresent
    public Result editPassword() {
        try {
            
            if (!getAccountManagerPlugin().isAuthenticationRepositoryMasterMode()) {
                throw new Exception("Not allowed to update password if the system is not in master mode");
            }
            Form<UserAccountFormData> userAccountFormLoaded = passwordUpdateForm.fill(new UserAccountFormData());
            return ok(views.html.admin.myaccount.myaccount_editpassword.render(Messages.get("my.my_profile.sidebar.update_password"),
                    getAccountManagerPlugin().isAuthenticationRepositoryMasterMode(), getAccountManagerPlugin().isSelfMailUpdateAllowed(), userAccountFormLoaded));
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }
    }

    /**
     * Save the basic data information.
     */
    @SubjectPresent
    public Result saveBasicData() {
        try {
            
            Form<UserAccountFormData> boundForm = basicDataUpdateForm.bindFromRequest();
            if (boundForm.hasErrors()) {
                return badRequest(views.html.admin.myaccount.myaccount_editbasicdata.render(Messages.get("my.my_profile.sidebar.update_data"),
                        getAccountManagerPlugin().isAuthenticationRepositoryMasterMode(), getAccountManagerPlugin().isSelfMailUpdateAllowed(), boundForm));
            }
            UserAccountFormData accountDataForm = boundForm.get();
            
            getAccountManagerPlugin().updatePreferredLanguage(getUserSessionManagerPlugin().getUserSessionId(ctx()), accountDataForm.preferredLanguage);
            getAccountManagerPlugin().updateBasicUserData(getUserSessionManagerPlugin().getUserSessionId(ctx()), accountDataForm.firstName, accountDataForm.lastName);
            Utilities.sendSuccessFlashMessage(Messages.get("my.my_profile.update_data.successful"));
            return redirect(controllers.admin.routes.MyAccount.display());
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }
    }

    /**
     * Create a validation key and send an e-mail to the user to perform an
     * e-mail update validation.
     */
    @SubjectPresent
    public Result saveEmail() {
        try {
            
            if (!getAccountManagerPlugin().isSelfMailUpdateAllowed()) {
                throw new Exception("Not allowed to update email (either master mode or self mail update not allowed)");
            }
            Form<UserAccountFormData> boundForm = mailUpdateForm.bindFromRequest();
            if (boundForm.hasErrors()) {
                return badRequest(views.html.admin.myaccount.myaccount_editmail.render(Messages.get("my.my_profile.sidebar.update_email"),
                        getAccountManagerPlugin().isAuthenticationRepositoryMasterMode(), getAccountManagerPlugin().isSelfMailUpdateAllowed(), boundForm));
            }
            UserAccountFormData accountDataForm = boundForm.get();
            IUserAccount account = getCurrentUserAccount(getAccountManagerPlugin());
            String validationKey = getAccountManagerPlugin().getValidationKey(account.getUid(), accountDataForm.mail);

            if (getAccountManagerPlugin().isMailExistsInAuthenticationBackEnd(accountDataForm.mail)) {
                IUserAccount userAccount = getAccountManagerPlugin().getUserAccountFromEmail(accountDataForm.mail);
                
                String currentUserUid = getUserSessionManagerPlugin().getUserSessionId(ctx());
                if (!userAccount.getUid().equals(currentUserUid)) {
                    boundForm.reject("mail", Msg.get("object.user_account.email.already_exists"));
                    return badRequest(views.html.admin.myaccount.myaccount_editmail.render(Messages.get("my.my_profile.sidebar.update_email"),
                            getAccountManagerPlugin().isAuthenticationRepositoryMasterMode(), getAccountManagerPlugin().isSelfMailUpdateAllowed(), boundForm));
                }
            }

            // Send an e-mail for validation
            emailService.sendEmail(
                    Msg.get("my.my_profile.update_email.message.subject"),
                    play.Configuration.root().getString("maf.email.from"),
                    Utilities.renderViewI18n(
                            "views.html.mail.account_email_update_html",
                            play.Configuration.root().getString("maf.platformName"),
                            account.getFirstName() + " " + account.getLastName(),
                            getPreferenceManagerPlugin().getPreferenceElseConfigurationValue(IFrameworkConstants.PUBLIC_URL_PREFERENCE, "maf.public.url")
                                    + controllers.admin.routes.MyAccount.validateEmailUpdate(validationKey).url()).body(), accountDataForm.mail);

            Utilities.sendSuccessFlashMessage(Messages.get("my.my_profile.update_email.successful"));
            return redirect(controllers.admin.routes.MyAccount.display());
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }
    }

    /**
     * Validate the e-mail update and perform the update according to what has
     * been requested.
     * 
     * @param validationKey
     *            the key for the email validation
     */
    @SubjectPresent
    public Result validateEmailUpdate(String validationKey) {
        try {
            
            if (!getAccountManagerPlugin().isSelfMailUpdateAllowed()) {
                throw new Exception("Not allowed to update email (either master mode or self mail update not allowed)");
            }
            
            String currentUserUid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            String newEmailAddress = getAccountManagerPlugin().checkValidationKey(currentUserUid, validationKey);
            if (newEmailAddress != null) {
                getAccountManagerPlugin().updateMail(currentUserUid, newEmailAddress);
                return ok(views.html.admin.myaccount.myaccount_emailvalidation.render(Messages.get("my.my_profile.update_email.validation.title"), true));
            } else {
                return ok(views.html.admin.myaccount.myaccount_emailvalidation.render(Messages.get("my.my_profile.update_email.validation.title"), false));
            }
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }
    }

    /**
     * Update the password.
     */
    @SubjectPresent
    public Result savePassword() {
        try {
            
            if (!getAccountManagerPlugin().isAuthenticationRepositoryMasterMode()) {
                throw new Exception("Not allowed to update the password if the system is not in master mode");
            }
            Form<UserAccountFormData> boundForm = passwordUpdateForm.bindFromRequest();
            if (boundForm.hasErrors()) {
                return badRequest(views.html.admin.myaccount.myaccount_editpassword.render(Messages.get("my.my_profile.sidebar.update_data"),
                        getAccountManagerPlugin().isAuthenticationRepositoryMasterMode(), getAccountManagerPlugin().isSelfMailUpdateAllowed(), boundForm));
            }
            UserAccountFormData accountDataForm = boundForm.get();
            // Authenticate the user against the old password
            
            if (accountDataForm.oldPasswordCheck == null
                    || !getAuthenticationReader().checkPassword(getUserSessionManagerPlugin().getUserSessionId(ctx()), accountDataForm.oldPasswordCheck)) {
                boundForm.reject("oldPasswordCheck", Msg.get("form.input.oldpassword.invalid"));
                return badRequest(views.html.admin.myaccount.myaccount_editpassword.render(Messages.get("my.my_profile.sidebar.update_data"),
                        getAccountManagerPlugin().isAuthenticationRepositoryMasterMode(), getAccountManagerPlugin().isSelfMailUpdateAllowed(), boundForm));
            }

            // New password does not match check
            if (!accountDataForm.password.equals(accountDataForm.passwordCheck)) {
                boundForm.reject("password", Msg.get("form.input.confirmationpassword.invalid"));
                return badRequest(views.html.admin.myaccount.myaccount_editpassword.render(Messages.get("my.my_profile.sidebar.update_data"),
                        getAccountManagerPlugin().isAuthenticationRepositoryMasterMode(), getAccountManagerPlugin().isSelfMailUpdateAllowed(), boundForm));
            }

            if (Utilities.getPasswordStrength(accountDataForm.password) < 1) {
                boundForm.reject("password", Msg.get("form.input.password.error.insufficient_strength"));
                return badRequest(views.html.admin.myaccount.myaccount_editpassword.render(Messages.get("my.my_profile.sidebar.update_data"),
                        getAccountManagerPlugin().isAuthenticationRepositoryMasterMode(), getAccountManagerPlugin().isSelfMailUpdateAllowed(), boundForm));
            }

            getAccountManagerPlugin().updatePassword(getUserSessionManagerPlugin().getUserSessionId(ctx()), accountDataForm.password);
            Utilities.sendSuccessFlashMessage(Messages.get("my.my_profile.update_password.successful"));
            return redirect(controllers.admin.routes.MyAccount.display());
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagesPlugin());
        }
    }

    /**
     * Return the {@link IUserAccount} for the currently logged user.
     * 
     * @param accountManagerPlugin
     *            the user account manager plugin
     * 
     * @return a user account instance
     * @throws AccountManagementException
     */
    private IUserAccount getCurrentUserAccount(IAccountManagerPlugin accountManagerPlugin) throws AccountManagementException {
        
        return accountManagerPlugin.getUserAccountFromUid(getUserSessionManagerPlugin().getUserSessionId(ctx()));
    }

    /**
     * The data to be used to collect changes on a user account information.
     * 
     * @author Pierre-Yves Cloux
     * 
     */
    public static class UserAccountFormData {

        /**
         * The group for the change password form.
         * 
         * @author Pierre-Yves Cloux
         */
        public interface PasswordChangeGroup {
        }

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
         * Default constructor.
         */
        public UserAccountFormData() {
            super();
        }

        /**
         * Construct the form data thanks a user account.
         * 
         * @param userAccount
         *            the user account
         */
        public UserAccountFormData(IUserAccount userAccount) {
            this.firstName = userAccount.getFirstName();
            this.lastName = userAccount.getLastName();
            this.preferredLanguage = userAccount.getPreferredLanguage() != null ? userAccount.getPreferredLanguage().toLowerCase() : null;
            this.mail = userAccount.getMail();
        }

        @Required(message = "object.user_account.email.invalid", groups = { EmailChangeGroup.class })
        @Email(message = "object.user_account.email.invalid", groups = { EmailChangeGroup.class })
        public String mail;

        @Required(message = "object.user_account.first_name.invalid", groups = { BasicDataChangeGroup.class })
        @MinLength(value = 2, message = "object.user_account.first_name.invalid", groups = { BasicDataChangeGroup.class })
        @MaxLength(value = 64, message = "object.user_account.first_name.invalid", groups = { BasicDataChangeGroup.class })
        public String firstName;

        @Required(message = "object.user_account.last_name.invalid", groups = { BasicDataChangeGroup.class })
        @MinLength(value = 2, message = "object.user_account.last_name.invalid", groups = { BasicDataChangeGroup.class })
        @MaxLength(value = 64, message = "object.user_account.last_name.invalid", groups = { BasicDataChangeGroup.class })
        public String lastName;

        @Required(groups = { BasicDataChangeGroup.class })
        public String preferredLanguage;

        @Required(message = "form.input.password.invalid", groups = { PasswordChangeGroup.class })
        @Transient
        public String password;

        @Required(message = "form.input.password.invalid", groups = { PasswordChangeGroup.class })
        @Transient
        public String passwordCheck;

        @Required(message = "form.input.password.invalid", groups = { PasswordChangeGroup.class })
        @Transient
        public String oldPasswordCheck;
    }

    /**
     * The menu item type.
     * 
     * @author Johann Kohler
     * 
     */
    public static enum MenuItemType {
        VIEW, EDIT_DATA, EDIT_PASSWORD, EDIT_MAIL;
    }

    private IAccountManagerPlugin getAccountManagerPlugin() {
        return accountManagerPlugin;
    }

    private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return userSessionManagerPlugin;
    }

    private IAuthenticationAccountReaderPlugin getAuthenticationReader() {
        return authenticationReader;
    }

    private IPreferenceManagerPlugin getPreferenceManagerPlugin() {
        return preferenceManagerPlugin;
    }

    private II18nMessagesPlugin getI18nMessagesPlugin() {
        return i18nMessagesPlugin;
    }

    private Configuration getConfiguration() {
        return configuration;
    }

}
