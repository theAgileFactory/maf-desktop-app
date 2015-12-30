package controllers.dashboard;

import framework.services.widgets.DashboardPageTemplate;
import framework.services.widgets.DashboardWidgetPosition;
import models.framework_models.widgets.DashboardPage;
import models.framework_models.widgets.DashboardWidget;
import play.mvc.Controller;
import play.mvc.Result;

public class DashboardController extends Controller{

    public DashboardController() {
    }

    public Result index() {
        DashboardPage page=new DashboardPage();
        page.name="A page";
        page.template=DashboardPageTemplate.FULL_PAGE.name();
        page.save();
        
        DashboardWidget widget=new DashboardWidget();
        widget.dashboardPage=page;
        widget.identifier="TOTO";
        widget.order=1;
        widget.position=DashboardWidgetPosition.LEFT.name();
        widget.save();
        
        return ok(views.html.dashboard.index.render());
    }
    
    public Result edit() {
        return ok(views.html.dashboard.edit.render());
    }
}
