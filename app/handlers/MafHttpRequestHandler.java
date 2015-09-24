package handlers;

import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.inject.Singleton;

import framework.handlers.AbstractRequestHandler;
import framework.security.ISecurityService;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.configuration.Language;
import framework.services.notification.INotificationManagerPlugin;
import framework.utils.Utilities;
import play.Logger;
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
@Singleton
public class MafHttpRequestHandler extends AbstractRequestHandler {

    @Inject
    private II18nMessagesPlugin messagesPlugin;

    @Inject
    private IDataSyndicationService dataSyndicationService;

    @Inject
    private ISecurityService securityService;

    @Inject
    private INotificationManagerPlugin notificationService;

    /**
     * Default constructor.
     */
    public MafHttpRequestHandler() {
    }

    @Override
    public Action<Void> createAction(Request request, Method actionMethod) {

        return new Action.Simple() {
            @Override
            public Promise<Result> call(Context ctx) throws Throwable {
                // Inject the required services into the context
                injectCommonServicesIncontext(ctx);
                final Language language = new Language(request.getQueryString("lang"));

                if (messagesPlugin.isLanguageValid(language.getCode())) {
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

    /**
     * Inject the common service.
     * 
     * @param context
     *            the play context
     */
    protected void injectCommonServicesIncontext(Context context) {
        super.injectCommonServicesIncontext(context);
        context.args.put(IDataSyndicationService.class.getName(), dataSyndicationService);
        context.args.put(ISecurityService.class.getName(), securityService);
        context.args.put(INotificationManagerPlugin.class.getName(), notificationService);
    }
}
