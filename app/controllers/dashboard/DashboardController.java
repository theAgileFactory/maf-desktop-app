package controllers.dashboard;

import java.util.Date;
import java.util.UUID;

import framework.services.widgets.DashboardPageTemplate;
import framework.services.widgets.DashboardWidgetPosition;
import models.framework_models.widgets.DashboardPage;
import models.framework_models.widgets.DashboardWidget;
import play.mvc.Controller;
import play.mvc.Result;
import play.libs.F.Promise;
import play.libs.Json;

public class DashboardController extends Controller{

    public DashboardController() {
    }

    public Result index() {
        return ok(views.html.dashboard.index.render());
    }
    
    public Promise<Result> createNewRow(Integer numberOfColumns) {
        return Promise.promise(() -> ok(views.html.dashboard.row.render(numberOfColumns)));
    }
    
    public Promise<Result> createNewWidget(String newIdentifier){
        return Promise.promise(() -> ok(Json.parse("{ \"widgetId\" : 1}")));
    }
    
    public Promise<Result> displayWidget(Long id) {
        if(id==3){
            return Promise.promise(() ->badRequest());
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        return Promise.promise(() -> ok(views.html.dashboard.display_widget.render(id,UUID.randomUUID().toString())));
    }
    
    public Promise<Result> editWidget(Long id) {
        if(id==3){
            return Promise.promise(() ->badRequest());
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        return Promise.promise(() -> ok(views.html.dashboard.edit_widget.render(id,UUID.randomUUID().toString())));
    }
}
