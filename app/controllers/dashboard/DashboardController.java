package controllers.dashboard;

import play.mvc.Controller;
import play.mvc.Result;

public class DashboardController extends Controller{

    public DashboardController() {
    }

    public Result index() {
        return ok(views.html.dashboard.index.render());
    }
    
    public Result edit() {
        return ok(views.html.dashboard.edit.render());
    }
}
