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

import javax.persistence.Transient;

import play.Logger;
import play.data.Form;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import controllers.ControllersUtils;
import framework.commons.IFrameworkConstants;
import framework.services.ServiceManager;
import framework.services.account.AccountManagementException;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IAuthenticationAccountReaderPlugin;
import framework.services.account.IUserAccount;
import framework.services.session.IUserSessionManagerPlugin;
import framework.utils.EmailUtils;
import framework.utils.Msg;
import framework.utils.Utilities;

/**
 * This controller deal with the self administration of its profile by a user.
 * 
 * @author Pierre-Yves Cloux
 * 
 */
public class MyAccount extends Controller {

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
            IAccountManagerPlugin accountManagerPlugin = ServiceManager.getService(IAccountManagerPlugin.NAME, IAccountManagerPlugin.class);
            IUserAccount account = getCurrentUserAccount(accountManagerPlugin);
            return ok(views.html.admin.myaccount.myaccount_display.render(Messages.get("my.my_profile.sidebar.details"),
                    accountManagerPlugin.isAuthenticationRepositoryMasterMode(), accountManagerPlugin.isSelfMailUpdateAllowed(), account));
        } catch (AccountManagementException e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Display the form to update the basic account information.
     */
    @SubjectPresent
    public Result editBasicData() {
        try {
            IAccountManagerPlugin accountManagerPlugin = ServiceManager.getService(IAccountManagerPlugin.NAME, IAccountManagerPlugin.class);
            IUserAccount account = getCurrentUserAccount(accountManagerPlugin);
            Form<UserAccountFormData> userAccountFormLoaded = basicDataUpdateForm.fill(new UserAccountFormData(account));
            return ok(views.html.admin.myaccount.myaccount_editbasicdata.render(Messages.get("my.my_profile.sidebar.update_data"),
                    accountManagerPlugin.isAuthenticationRepositoryMasterMode(), accountManagerPlugin.isSelfMailUpdateAllowed(), userAccountFormLoaded));
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Display the form to edit the e-mail address.
     */
    @SubjectPresent
    public Result editEmail() {
        try {
            IAccountManagerPlugin accountManagerPlugin = ServiceManager.getService(IAccountManagerPlugin.NAME, IAccountManagerPlugin.class);
            if (!accountManagerPlugin.isSelfMailUpdateAllowed()) {
                throw new Exception("Not allowed to update email (either master mode or self mail update not allowed)");
            }
            IUserAccount account = getCurrentUserAccount(accountManagerPlugin);
            Form<UserAccountFormData> userAccountFormLoaded = mailUpdateForm.fill(new UserAccountFormData(account));
            return ok(views.html.admin.myaccount.myaccount_editmail.render(Msg.get("my.my_profile.sidebar.update_email"),
                    accountManagerPlugin.isAuthenticationRepositoryMasterMode(), accountManagerPlugin.isSelfMailUpdateAllowed(), userAccountFormLoaded));
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Display the form to edit the password.
     */
    @SubjectPresent
    public Result editPassword() {
        try {
            IAccountManagerPlugin accountManagerPlugin = ServiceManager.getService(IAccountManagerPlugin.NAME, IAccountManagerPlugin.class);
            if (!accountManagerPlugin.isAuthenticationRepositoryMasterMode()) {
                throw new Exception("Not allowed to update password if the system is not in master mode");
            }
            Form<UserAccountFormData> userAccountFormLoaded = passwordUpdateForm.fill(new UserAccountFormData());
            return ok(views.html.admin.myaccount.myaccount_editpassword.render(Messages.get("my.my_profile.sidebar.update_password"),
                    accountManagerPlugin.isAuthenticationRepositoryMasterMode(), accountManagerPlugin.isSelfMailUpdateAllowed(), userAccountFormLoaded));
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Save the basic data information.
     */
    @SubjectPresent
    public Result saveBasicData() {
        try {
            IAccountManagerPlugin accountManagerPlugin = ServiceManager.getService(IAccountManagerPlugin.NAME, IAccountManagerPlugin.class);
            Form<UserAccountFormData> boundForm = basicDataUpdateForm.bindFromRequest();
            if (boundForm.hasErrors()) {
                return badRequest(views.html.admin.myaccount.myaccount_editbasicdata.render(Messages.get("my.my_profile.sidebar.update_data"),
                        accountManagerPlugin.isAuthenticationRepositoryMasterMode(), accountManagerPlugin.isSelfMailUpdateAllowed(), boundForm));
            }
            UserAccountFormData accountDataForm = boundForm.get();
            IUserSessionManagerPlugin userSessionPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
            accountManagerPlugin.updatePreferredLanguage(userSessionPlugin.getUserSessionId(ctx()), accountDataForm.preferredLanguage);
            accountManagerPlugin.updateBasicUserData(userSessionPlugin.getUserSessionId(ctx()), accountDataForm.firstName, accountDataForm.lastName);
            Utilities.sendSuccessFlashMessage(Messages.get("my.my_profile.update_data.successful"));
            return redirect(controllers.admin.routes.MyAccount.display());
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Create a validation key and send an e-mail to the user to perform an
     * e-mail update validation.
     */
    @SubjectPresent
    public Result saveEmail() {
        try {
            IAccountManagerPlugin accountManagerPlugin = ServiceManager.getService(IAccountManagerPlugin.NAME, IAccountManagerPlugin.class);
            if (!accountManagerPlugin.isSelfMailUpdateAllowed()) {
                throw new Exception("Not allowed to update email (either master mode or self mail update not allowed)");
            }
            Form<UserAccountFormData> boundForm = mailUpdateForm.bindFromRequest();
            if (boundForm.hasErrors()) {
                return badRequest(views.html.admin.myaccount.myaccount_editmail.render(Messages.get("my.my_profile.sidebar.update_email"),
                        accountManagerPlugin.isAuthenticationRepositoryMasterMode(), accountManagerPlugin.isSelfMailUpdateAllowed(), boundForm));
            }
            UserAccountFormData accountDataForm = boundForm.get();
            IUserAccount account = getCurrentUserAccount(accountManagerPlugin);
            String validationKey = accountManagerPlugin.getValidationKey(account.getUid(), accountDataForm.mail);

            if (accountManagerPlugin.isMailExistsInAuthenticationBackEnd(accountDataForm.mail)) {
                IUserAccount userAccount = accountManagerPlugin.getUserAccountFromEmail(accountDataForm.mail);
                IUserSessionManagerPlugin userSessionPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
                String currentUserUid = userSessionPlugin.getUserSessionId(ctx());
                if (!userAccount.getUid().equals(currentUserUid)) {
                    boundForm.reject("mail", Msg.get("object.user_account.email.already_exists"));
                    return badRequest(views.html.admin.myaccount.myaccount_editmail.render(Messages.get("my.my_profile.sidebar.update_email"),
                            accountManagerPlugin.isAuthenticationRepositoryMasterMode(), accountManagerPlugin.isSelfMailUpdateAllowed(), boundForm));
                }
            }

            // Send an e-mail for validation
            EmailUtils.sendEmail(
                    Msg.get("my.my_profile.update_email.message.subject"),
                    play.Configuration.root().getString("maf.email.from"),
                    Utilities.renderViewI18n(
                            "views.html.mail.account_email_update_html",
                            play.Configuration.root().getString("maf.platformName"),
                            account.getFirstName() + " " + account.getLastName(),
                            Utilities.getPreferenceElseConfigurationValue(IFrameworkConstants.PUBLIC_URL_PREFERENCE, "maf.public.url")
                                    + controllers.admin.routes.MyAccount.validateEmailUpdate(validationKey).url()).body(), accountDataForm.mail);

            Utilities.sendSuccessFlashMessage(Messages.get("my.my_profile.update_email.successful"));
            return redirect(controllers.admin.routes.MyAccount.display());
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
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
            IAccountManagerPlugin accountManagerPlugin = ServiceManager.getService(IAccountManagerPlugin.NAME, IAccountManagerPlugin.class);
            if (!accountManagerPlugin.isSelfMailUpdateAllowed()) {
                throw new Exception("Not allowed to update email (either master mode or self mail update not allowed)");
            }
            IUserSessionManagerPlugin userSessionPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
            String currentUserUid = userSessionPlugin.getUserSessionId(ctx());
            String newEmailAddress = accountManagerPlugin.checkValidationKey(currentUserUid, validationKey);
            if (newEmailAddress != null) {
                accountManagerPlugin.updateMail(currentUserUid, newEmailAddress);
                return ok(views.html.admin.myaccount.myaccount_emailvalidation.render(Messages.get("my.my_profile.update_email.validation.title"), true));
            } else {
                return ok(views.html.admin.myaccount.myaccount_emailvalidation.render(Messages.get("my.my_profile.update_email.validation.title"), false));
            }
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }
    }

    /**
     * Update the password.
     */
    @SubjectPresent
    public Result savePassword() {
        try {
            IAccountManagerPlugin accountManagerPlugin = ServiceManager.getService(IAccountManagerPlugin.NAME, IAccountManagerPlugin.class);
            if (!accountManagerPlugin.isAuthenticationRepositoryMasterMode()) {
                throw new Exception("Not allowed to update the password if the system is not in master mode");
            }
            Form<UserAccountFormData> boundForm = passwordUpdateForm.bindFromRequest();
            if (boundForm.hasErrors()) {
                return badRequest(views.html.admin.myaccount.myaccount_editpassword.render(Messages.get("my.my_profile.sidebar.update_data"),
                        accountManagerPlugin.isAuthenticationRepositoryMasterMode(), accountManagerPlugin.isSelfMailUpdateAllowed(), boundForm));
            }
            UserAccountFormData accountDataForm = boundForm.get();
            // Authenticate the user against the old password
            IUserSessionManagerPlugin userSessionPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
            IAuthenticationAccountReaderPlugin authenticationReader =
                    ServiceManager.getService(IAuthenticationAccountReaderPlugin.NAME, IAuthenticationAccountReaderPlugin.class);
            if (accountDataForm.oldPasswordCheck == null
                    || !authenticationReader.checkPassword(userSessionPlugin.getUserSessionId(ctx()), accountDataForm.oldPasswordCheck)) {
                boundForm.reject("oldPasswordCheck", Msg.get("form.input.oldpassword.invalid"));
                return badRequest(views.html.admin.myaccount.myaccount_editpassword.render(Messages.get("my.my_profile.sidebar.update_data"),
                        accountManagerPlugin.isAuthenticationRepositoryMasterMode(), accountManagerPlugin.isSelfMailUpdateAllowed(), boundForm));
            }

            // New password does not match check
            if (!accountDataForm.password.equals(accountDataForm.passwordCheck)) {
                boundForm.reject("password", Msg.get("form.input.confirmationpassword.invalid"));
                return badRequest(views.html.admin.myaccount.myaccount_editpassword.render(Messages.get("my.my_profile.sidebar.update_data"),
                        accountManagerPlugin.isAuthenticationRepositoryMasterMode(), accountManagerPlugin.isSelfMailUpdateAllowed(), boundForm));
            }

            if (Utilities.getPasswordStrength(accountDataForm.password) < 1) {
                boundForm.reject("password", Msg.get("form.input.password.error.insufficient_strength"));
                return badRequest(views.html.admin.myaccount.myaccount_editpassword.render(Messages.get("my.my_profile.sidebar.update_data"),
                        accountManagerPlugin.isAuthenticationRepositoryMasterMode(), accountManagerPlugin.isSelfMailUpdateAllowed(), boundForm));
            }

            accountManagerPlugin.updatePassword(userSessionPlugin.getUserSessionId(ctx()), accountDataForm.password);
            Utilities.sendSuccessFlashMessage(Messages.get("my.my_profile.update_password.successful"));
            return redirect(controllers.admin.routes.MyAccount.display());
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
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
        IUserSessionManagerPlugin userSessionPlugin = ServiceManager.getService(IUserSessionManagerPlugin.NAME, IUserSessionManagerPlugin.class);
        return accountManagerPlugin.getUserAccountFromUid(userSessionPlugin.getUserSessionId(ctx()));
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

}
