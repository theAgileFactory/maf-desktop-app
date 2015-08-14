
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
import controllers.ControllersUtils;
import controllers.api.ApiController;
import framework.services.ServiceStaticAccessor;
import framework.services.api.AbstractApiController;
import framework.services.api.ApiError;
import framework.services.router.ICustomRouterNotificationService;
import play.GlobalSettings;
import play.Logger;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;

/**
 * Global object.<br/>
 * This object contains all the startup code for the application.
 * 
 * @author Pierre-Yves Cloux
 */
public class Global extends GlobalSettings {
    private static Logger.ALogger log = Logger.of(Global.class);

    /**
     * Http code 500.
     * 
     * @param request
     *            the request
     * @param t
     *            the exception
     */
    @Override
    public Promise<Result> onError(final RequestHeader request, final Throwable t) {

        return Promise.promise(new Function0<Result>() {

            public Result apply() throws Throwable {
                return (Result) ControllersUtils.logAndReturnUnexpectedError((Exception) t, Logger.of(Global.class));
            }
        });

    }

    /**
     * Http code 404.
     * 
     * @param request
     *            the request
     */
    @Override
    public Promise<Result> onHandlerNotFound(final RequestHeader request) {
        try {
            ICustomRouterNotificationService customRouterNotificationService = ServiceStaticAccessor.getCustomRouterNotificationService();
            Promise<Result> result = customRouterNotificationService.notify(Controller.ctx());
            if (result != null) {
                return result;
            }
        } catch (Exception e) {
            log.warn("Error while calling the custom router", e);
        }
        return Promise.promise(new Function0<Result>() {
            public Result apply() throws Throwable {
                if (request.path().startsWith(AbstractApiController.STANDARD_API_ROOT_URI)) {
                    return ApiController.getJsonErrorResponse(new ApiError(404, "Not found"));
                } else {
                    return play.mvc.Results.notFound(views.html.error.not_found.render(request.uri()));
                }
            }
        });

    }

    /**
     * Http code 400.
     * 
     * @param request
     *            the request
     * @param error
     *            the error mesage
     */
    @Override
    public Promise<Result> onBadRequest(final RequestHeader request, final String error) {

        return Promise.promise(new Function0<Result>() {

            public Result apply() throws Throwable {
                if (request.path().startsWith(AbstractApiController.STANDARD_API_ROOT_URI)) {
                    return ApiController.getJsonErrorResponse(new ApiError(400, error));
                } else {
                    return play.mvc.Results.badRequest(views.html.error.bad_request.render());
                }
            }

        });

    }
}