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

import play.Configuration;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints.Required;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import controllers.ControllersUtils;
import framework.services.account.IAccountManagerPlugin;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.Msg;
import framework.utils.Utilities;

/**
 * This controller is used in combination with the {@link UserManager}
 * controller.<br/>
 * It should be called when a password is to be reseted.<br/>
 * The user receives an e-mail with a validation link which allow him/her to
 * change his/her password.
 * <p>
 * WARNING: this controller is not protected.<br/>
 * In order to avoid any misuse, once the validation key is verified, a
 * temporary session key is set in the user session with a dynamically generated
 * unique key. This one acts as a session id and is passed in the various
 * subsequent requests.
 * </p>
 * 
 * @author Pierre-Yves Cloux
 */
public class PasswordReset extends Controller {
    @Inject
    private IAccountManagerPlugin accountManagerPlugin;
    @Inject
    private II18nMessagesPlugin messagesPlugin;
    @Inject 
    private Configuration configuration;
    
    private static Logger.ALogger log = Logger.of(PasswordReset.class);
    public static final String CURRENT_USER_UID = "MAF_USER_ID";
    private static Form<PasswordFormData> passwordUpdateForm = Form.form(PasswordFormData.class);

    /**
     * Display which enable password reset.
     * 
     * @param uid
     *            the unique id of a user
     * @param validationKey
     *            the validation key for this password reset
     */
    public Result displayPasswordResetForm(String uid, String validationKey) {
        try {
            if (!getAccountManagerPlugin().isAuthenticationRepositoryMasterMode()) {
                throw new Exception("Not allowed to update password in slave mode");
            }
            String validationData = getAccountManagerPlugin().checkValidationKey(uid, validationKey);
            // Set the uid to update in session
            session().put(CURRENT_USER_UID, uid);
            if (validationData == null) {
                return badRequest(views.html.admin.passwordreset.validation_key_invalid.render(Msg.get("validationkey.invalid.title")));
            }
            PasswordFormData passwordFormData = new PasswordFormData();
            Form<PasswordFormData> boundForm = passwordUpdateForm.fill(passwordFormData);
            return badRequest(views.html.admin.passwordreset.passwordreset_form.render(Messages.get("my.my_profile.sidebar.update_data"), boundForm));
        } catch (Exception e) {
            log.error("Exception while validating a password reset key", e);
            return badRequest(views.html.admin.passwordreset.validation_key_invalid.render(Msg.get("validationkey.invalid.title")));
        }
    }

    /**
     * Update the password associated with the specified uid.
     */
    public Result savePassword() {
        try {
            if (!session().containsKey(CURRENT_USER_UID)) {
                throw new Exception("Attempt to save a password without a valid session");
            }
            String uid = session().get(CURRENT_USER_UID);
            if (!getAccountManagerPlugin().isAuthenticationRepositoryMasterMode()) {
                throw new Exception("Not allowed to update the password if the system is not in master mode");
            }
            Form<PasswordFormData> boundForm = passwordUpdateForm.bindFromRequest();
            if (boundForm.hasErrors()) {
                return badRequest(views.html.admin.passwordreset.passwordreset_form.render(Messages.get("my.my_profile.sidebar.update_data"), boundForm));
            }
            PasswordFormData passwordFormData = boundForm.get();
            if (!passwordFormData.password.equals(passwordFormData.passwordCheck)) {
                boundForm.reject("password", Msg.get("form.input.confirmationpassword.invalid"));
                return badRequest(views.html.admin.passwordreset.passwordreset_form.render(Messages.get("my.my_profile.sidebar.update_data"), boundForm));
            }
            if (Utilities.getPasswordStrength(passwordFormData.password) < 1) {
                boundForm.reject("password", Msg.get("form.input.password.error.insufficient_strength"));
                return badRequest(views.html.admin.passwordreset.passwordreset_form.render(Messages.get("my.my_profile.sidebar.update_data"), boundForm));
            }
            getAccountManagerPlugin().updatePassword(uid, passwordFormData.password);
            getAccountManagerPlugin().resetValidationKey(uid);
            Utilities.sendSuccessFlashMessage(Messages.get("my.my_profile.update_password.successful"));
            session().clear();
            return redirect(controllers.routes.Application.index());
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log,getConfiguration(),getMessagesPlugin());
        }
    }

    /**
     * The data to be used to collect changes on a user password.
     * 
     * @author Pierre-Yves Cloux
     * 
     */
    public static class PasswordFormData {
        @Required(message = "form.input.password.invalid")
        @Transient
        public String password;

        @Required(message = "form.input.password.invalid")
        @Transient
        public String passwordCheck;
    }

    private IAccountManagerPlugin getAccountManagerPlugin() {
        return accountManagerPlugin;
    }

    private II18nMessagesPlugin getMessagesPlugin() {
        return messagesPlugin;
    }

    private Configuration getConfiguration() {
        return configuration;
    }
}