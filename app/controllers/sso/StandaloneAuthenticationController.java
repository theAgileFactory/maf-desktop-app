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

import models.framework_models.account.Credential;
import models.framework_models.parent.IModelConstants;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.http.client.FormClient;
import org.pac4j.play.Config;

import play.data.Form;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;
import services.licensesmanagement.LicensesManagementServiceImpl;
import services.licensesmanagement.LoginEventRequest.ErrorCode;
import views.html.sso.login;
import views.html.sso.reset_password;
import controllers.admin.UserManager;
import framework.services.ServiceManager;
import framework.services.account.LightAuthenticationLockedAccountException;
import framework.utils.CaptchaManager;
import framework.utils.Msg;
import framework.utils.Utilities;

/**
 * The controller which deals with the standalone authentication based on the
 * {@link Credential} object.
 * 
 * @author Pierre-Yves Cloux
 */
public class StandaloneAuthenticationController extends Controller {
    public static final String ERROR_PARAMETER = "error";
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
        FormClient formClient = (FormClient) Config.getClients().findClient("FormClient");
        String errorParameter = request().getQueryString(ERROR_PARAMETER);
        boolean hasError = false;
        boolean accountLocked = false;
        if (!StringUtils.isBlank(errorParameter)) {

            // event: wrong credential / STANDALONE
            ServiceManager.getService(LicensesManagementServiceImpl.NAME, LicensesManagementServiceImpl.class).addLoginEvent(
                    request().getQueryString(formClient.getUsernameParameter()), false, ErrorCode.WRONG_CREDENTIAL, errorParameter);

            hasError = true;
            if (errorParameter.equals(LightAuthenticationLockedAccountException.class.getSimpleName())) {
                accountLocked = true;
            }
        }
        return ok(login.render(formClient.getCallbackUrl(), hasError, accountLocked));
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
        if (!UserManager.resetUserPasswordFromEmail(resetPasswordRequest.mail, false)) {
            boundForm.reject("mail", Msg.get("authentication.standalone.reset.mail.unknown.message"));
            return badRequest(reset_password.render(uuid, boundForm));
        }
        Utilities.sendSuccessFlashMessage(Msg.get("authentication.standalone.reset.success.message"));
        return redirect(controllers.routes.Application.index());
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
}
