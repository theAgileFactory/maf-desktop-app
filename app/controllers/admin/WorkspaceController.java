package controllers.admin;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import framework.services.api.IApiControllerUtilsService;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * A controller for the management of collaborative workspaces.
 * 
 * @author Pierre-Yves Cloux
 */
@SubjectPresent
public class WorkspaceController extends Controller {

    @Inject
    private IApiControllerUtilsService apiControllerUtilsService;

    /**
     * Default constructor.
     */
    public WorkspaceController() {
    }

    /**
     * Display the application start screen.
     * 
     * @return
     */
    public Promise<Result> display() {
        Map<String, String> messages = new HashMap<>();
        messages.put("admin.workspace.admin.panel.title", "Workspace as administrator");
        messages.put("admin.workspace.member.panel.title", "Workspace as member");
        return Promise.promise(() -> ok(views.html.admin.workspace.edition.render("Title", getApiControllerUtilsService().convertAsJsonString(messages))));
    }

    /**
     * Get the API controller utils.
     */
    private IApiControllerUtilsService getApiControllerUtilsService() {
        return apiControllerUtilsService;
    }

}
