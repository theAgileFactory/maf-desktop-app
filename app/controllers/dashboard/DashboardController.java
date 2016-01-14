package controllers.dashboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import framework.services.plugins.IDashboardService;
import framework.services.plugins.IDashboardService.DashboardRowConfiguration;
import framework.services.plugins.IDashboardService.DashboardRowConfiguration.WidgetConfiguration;
import models.framework_models.plugin.DashboardRowTemplate;
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
    @Inject
    private IDashboardService dashboardService;
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
        try {
            byte[] out=objectMapper.writeValueAsBytes(dashboardService.getWidgetCatalog());
            System.out.println("Catalog : "+new String(out));
            
            List<DashboardRowConfiguration> page=new ArrayList<>();
            DashboardRowConfiguration dbc1=new DashboardRowConfiguration();
            page.add(dbc1);
            dbc1.setLayout(DashboardRowTemplate.TPL48_OL_2);
            WidgetConfiguration wc1=new WidgetConfiguration();
            wc1.setId(1l);
            wc1.setUrl("http://www.google.com");
            WidgetConfiguration wc2=new WidgetConfiguration();
            wc1.setId(1l);
            wc1.setUrl("http://www.google.com");
            List<WidgetConfiguration> widgetConfig= Arrays.asList(wc1,wc2);
            dbc1.setWidgetConfigs(widgetConfig);
            dashboardService.createDashboardPage(null, "MyPage", true, page);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
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
        return Promise.promise(() -> ok(getObjectMapper().readTree("{ \"widgetId\" : 1, \"widgetUrl\" : \"/dash/widget/display/1\"}")));
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
