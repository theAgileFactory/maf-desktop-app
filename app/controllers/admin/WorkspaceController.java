package controllers.admin;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import framework.services.api.IApiControllerUtilsService;
import framework.services.configuration.II18nMessagesPlugin;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * A controller for the management of collaborative workspaces
 * @author Pierre-Yves Cloux
 */
public class WorkspaceController extends Controller{
    @Inject
    private IApiControllerUtilsService apiControllerUtilsService;
    @Inject
    private II18nMessagesPlugin i18nMessagePlugin;
    
    public WorkspaceController() {
    }
    
    /**
     * Display the application start screen
     * @return
     */
    public Promise<Result> display(){
        Map<String, String> messages=new HashMap<>();
        messages.put("members.button.title", "Members");
        return  Promise.promise(() -> ok(views.html.admin.workspace.edition.render("Title", getApiControllerUtilsService().convertAsJsonString(messages))));
    }

    private IApiControllerUtilsService getApiControllerUtilsService() {
        return apiControllerUtilsService;
    }

    private II18nMessagesPlugin getI18nMessagePlugin() {
        return i18nMessagePlugin;
    }
}
