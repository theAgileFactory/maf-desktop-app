package handlers;

import java.lang.reflect.Method;

import javax.inject.Inject;

import framework.services.ServiceStaticAccessor;
import framework.services.configuration.Language;
import framework.utils.Utilities;
import play.Logger;
import play.http.DefaultHttpRequestHandler;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Result;
import services.datasyndication.IDataSyndicationService;

/**
 * Overriding of the default request handler.
 * 
 * @author Johann Kohler
 *
 */
public class MafHttpRequestHandler extends DefaultHttpRequestHandler {

    /**
     * Inject here the needed services for the views.
     */

    @Inject
    private IDataSyndicationService dataSyndicationService;

    @Override
    public Action<Void> createAction(Request request, Method actionMethod) {

        return new Action.Simple() {
            @Override
            public Promise<Result> call(Context ctx) throws Throwable {

                // add the needed services as args
                ctx.args.put(IDataSyndicationService.NAME, dataSyndicationService);

                final Language language = new Language(request.getQueryString("lang"));

                if (ServiceStaticAccessor.getMessagesPlugin().isLanguageValid(language.getCode())) {
                    Logger.debug("change language to: " + language.getCode());
                    ctx.changeLang(language.getCode());
                    // Update the CAS language cookie which is relying on Spring
                    // framework (not really solid yet works)
                    Utilities.setSsoLanguage(ctx, language.getCode());
                }

                return delegate.call(ctx);

            }
        };

    }

}
