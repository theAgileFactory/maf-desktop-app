package controllers.dashboard;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import framework.services.widgets.DashboardRowTemplate;
import play.libs.F.Promise;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * The dashboard controller.
 * 
 * @author Pierre-Yves Cloux
 *
 */
public class DashboardController extends Controller {

    private ObjectMapper objectMapper;

    /**
     * Default constructor.
     */
    public DashboardController() {
        objectMapper = new ObjectMapper();
    }

    /**
     * The index page.
     */
    public Result index() {
        return ok(views.html.dashboard.index.render());
    }

    /**
     * Update the dashboard page configuration.<br/>
     * This method expects a POST with a JSON structure.
     * 
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Promise<Result> updateDashboardPage() {
        JsonNode json = request().body().asJson();
        System.out.println("page : " + json);
        return Promise.promise(() -> ok());
    }

    /**
     * Create a new empty row to be inserted into the dashboard.
     * 
     * @param templateIdentifier
     *            the identifier of the template for the row
     */
    public Promise<Result> createNewRow(String templateIdentifier) {
        try {
            DashboardRowTemplate template = DashboardRowTemplate.valueOf(templateIdentifier);
            return Promise.promise(() -> ok(views.html.dashboard.row.render(StringUtils.split(template.getLayout(), ','))));
        } catch (Exception e) {
            return Promise.promise(() -> badRequest());
        }
    }

    /**
     * Create a new widget from the specified widget identifier and return the
     * widget id as a JSON structure.
     * 
     * <pre>
     * {@code
     *  {widgetId : 1}
     * }
     * </pre>
     * 
     * @param identifier
     *            a widget identifier
     * @return JSON structure
     */
    public Promise<Result> createNewWidget(String identifier) {
        return Promise.promise(() -> ok(getObjectMapper().readTree("{ \"widgetId\" : 1}")));
    }

    /**
     * Return a widget in DISPLAY mode.
     * 
     * @param id
     *            a widget id
     * 
     * @return an HTML widget part
     */
    public Promise<Result> displayWidget(Long id) {
        if (id == 3) {
            return Promise.promise(() -> badRequest());
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        return Promise.promise(() -> ok(views.html.dashboard.display_widget.render(id, UUID.randomUUID().toString())));
    }

    /**
     * Return a widget in EDIT mode.
     * 
     * @param id
     *            a widget id
     * 
     * @return an HTML widget part
     */
    public Promise<Result> editWidget(Long id) {
        if (id == 3) {
            return Promise.promise(() -> badRequest());
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        return Promise.promise(() -> ok(views.html.dashboard.edit_widget.render(id, UUID.randomUUID().toString())));
    }

    /**
     * Get the object mapper.
     */
    private ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
