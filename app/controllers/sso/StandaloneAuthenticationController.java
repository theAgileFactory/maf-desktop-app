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
package controllers.sso;

import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;

import controllers.admin.UserManager;
import framework.security.AbstractStandaloneAuthenticationController;
import framework.utils.CaptchaManager;
import framework.utils.Msg;
import framework.utils.Utilities;
import models.framework_models.account.Credential;
import models.framework_models.parent.IModelConstants;
import play.data.Form;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Result;
import services.echannel.request.LoginEventRequest.ErrorCode;
import services.licensesmanagement.ILicensesManagementService;
import views.html.sso.login;
import views.html.sso.reset_password;

/**
 * The controller which deals with the standalone authentication based on the
 * {@link Credential} object.
 * 
 * @author Pierre-Yves Cloux
 */
public class StandaloneAuthenticationController extends AbstractStandaloneAuthenticationController {
    @Inject
    private ILicensesManagementService licensesManagementService;
    @Inject
    private UserManager userManagerController;

    private static Form<ResetPasswordRequest> passwordResetRequestForm = Form.form(ResetPasswordRequest.class);

    /**
     * Default constructor.
     */
    public StandaloneAuthenticationController() {
    }

    /**
     * Display the authentication form.
     */
    public Result displayLoginForm() {
        LoginInfo loginInfo = getLoginInfo();
        if (loginInfo.hasErrors()) {

            // event: wrong credential / STANDALONE
            getLicensesManagementService().addLoginEvent(request().getQueryString(loginInfo.getUserLogin()), false, ErrorCode.WRONG_CREDENTIAL,
                    loginInfo.getErrorMessage());
        }
        return ok(login.render(loginInfo.getLoginFormActionUrl(), loginInfo.hasErrors(), loginInfo.isAccountLocked()));
    }

    /**
     * Return the byte stream for the captcha image to be displayed.
     * 
     * @param uuid
     *            the unique captcha id
     */
    public Promise<Result> getCaptchaImage(final String uuid) {
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                String decodedUuid = new String(Base64.decodeBase64(uuid));
                response().setContentType("image/png");
                return ok(CaptchaManager.createCaptcha(decodedUuid));
            }
        });
    }

    /**
     * Display reset password form.
     */
    public Result displayResetPasswordForm() {
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        String encodedUuid = new String(Base64.encodeBase64(UUID.randomUUID().toString().getBytes(), false, true));
        Form<ResetPasswordRequest> formLoaded = passwordResetRequestForm.fill(resetPasswordRequest);
        return ok(reset_password.render(encodedUuid, formLoaded));
    }

    /**
     * Display reset password form.
     * 
     * @param uuid
     *            the user uid
     */
    public Result triggerResetPassword(final String uuid) {
        Form<ResetPasswordRequest> boundForm = passwordResetRequestForm.bindFromRequest();
        if (boundForm.hasErrors()) {
            return badRequest(reset_password.render(uuid, boundForm));
        }
        ResetPasswordRequest resetPasswordRequest = boundForm.get();
        String decodedUuid = new String(Base64.decodeBase64(uuid));
        if (!CaptchaManager.validateCaptcha(decodedUuid, resetPasswordRequest.captchaText)) {
            boundForm.reject("captchaText", Msg.get("captcha.error.wrong_word"));
            return badRequest(reset_password.render(uuid, boundForm));
        }
        if (!getUserManagerController().resetUserPasswordFromEmail(resetPasswordRequest.mail, false)) {
            boundForm.reject("mail", Msg.get("authentication.standalone.reset.mail.unknown.message"));
            return badRequest(reset_password.render(uuid, boundForm));
        }
        Utilities.sendSuccessFlashMessage(Msg.get("authentication.standalone.reset.success.message"));
        return redirect(controllers.dashboard.routes.DashboardController.index(0, false));
    }

    /**
     * The form object which holds the password reset data.
     * 
     * @author Pierre-Yves Cloux
     */
    public static class ResetPasswordRequest {
        @Required(message = "authentication.standalone.reset.mail.validation.message")
        @Pattern(value = IModelConstants.EMAIL_VALIDATION_PATTERN, message = "authentication.standalone.reset.mail.validation.message")
        public String mail;
        @Required(message = "captcha.error.wrong_word")
        public String captchaText;
    }

    /**
     * Get the licenses management service.
     */
    private ILicensesManagementService getLicensesManagementService() {
        return licensesManagementService;
    }

    /**
     * Get the user manager controller.
     */
    private UserManager getUserManagerController() {
        return userManagerController;
    }
}
