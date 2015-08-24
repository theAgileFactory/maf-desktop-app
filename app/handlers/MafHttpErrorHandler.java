package handlers;

import javax.inject.Inject;
import javax.inject.Provider;

import controllers.ControllersUtils;
import controllers.api.ApiController;
import framework.services.ServiceStaticAccessor;
import framework.services.api.AbstractApiController;
import framework.services.api.ApiError;
import framework.services.router.ICustomRouterNotificationService;
import play.Configuration;
import play.Environment;
import play.Logger;
import play.api.OptionalSourceMapper;
import play.api.routing.Router;
import play.http.DefaultHttpErrorHandler;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Http.RequestHeader;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import framework.handlers.AbstractErrorHandler;

public class MafHttpErrorHandler extends AbstractErrorHandler {
    private static Logger.ALogger log = Logger.of(MafHttpErrorHandler.class);
    
    @Inject
    private ICustomRouterNotificationService customRouterNotificationService;

    @Inject
    public MafHttpErrorHandler(Configuration configuration, Environment environment, OptionalSourceMapper optionalSourceMapper, Provider<Router> providerRouter) {
        super(configuration, environment, optionalSourceMapper, providerRouter);
    }

    @Override
    public Promise<Result> onClientError(RequestHeader requestHeader, int statusCode, String error) {
        injectCommonServicesIncontext(Http.Context.current());
        if(statusCode == play.mvc.Http.Status.NOT_FOUND) {
            try {
                Promise<Result> result = getCustomRouterNotificationService().notify(Controller.ctx());
                if (result != null) {
                    return result;
                }
            } catch (Exception e) {
                log.warn("Error while calling the custom router", e);
            }
            return Promise.promise(new Function0<Result>() {
                public Result apply() throws Throwable {
                    if (requestHeader.path().startsWith(AbstractApiController.STANDARD_API_ROOT_URI)) {
                        return ApiController.getJsonErrorResponse(new ApiError(404, "Not found"));
                    } else {
                        return play.mvc.Results.notFound(views.html.error.not_found.render(requestHeader.uri()));
                    }
                }
            });
        }
        if(statusCode == play.mvc.Http.Status.BAD_REQUEST) {
            injectCommonServicesIncontext(Http.Context.current());
            return Promise.promise(new Function0<Result>() {
                public Result apply() throws Throwable {
                    if (requestHeader.path().startsWith(AbstractApiController.STANDARD_API_ROOT_URI)) {
                        return ApiController.getJsonErrorResponse(new ApiError(400, error));
                    } else {
                        return play.mvc.Results.badRequest(views.html.error.bad_request.render());
                    }
                }

            });
        }
        return Promise.<Result>pure(
                play.mvc.Results.status(statusCode, "an unexpected error occured: " + error)
        );
    }

    @Override
    public Promise<Result> onServerError(RequestHeader requestHeader, Throwable t) {
        injectCommonServicesIncontext(Http.Context.current());
        return Promise.promise(new Function0<Result>() {
            public Result apply() throws Throwable {
                return (Result) ControllersUtils.logAndReturnUnexpectedError((Exception) t, log);
            }
        });
    }

    private ICustomRouterNotificationService getCustomRouterNotificationService() {
        return customRouterNotificationService;
    }


}
