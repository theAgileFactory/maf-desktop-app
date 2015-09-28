package controllers.admin;

import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * A controller for the management of collaborative workspaces
 * @author Pierre-Yves Cloux
 */
public class WorkspaceController extends Controller{

    public WorkspaceController() {
    }
    
    public Promise<Result> displayWorkspaceConfiguration(){
        return  Promise.promise(() -> ok(views.html.admin.workspace.edition.render("Title")));
    }
}
