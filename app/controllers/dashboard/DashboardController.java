package controllers.dashboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import framework.services.plugins.IDashboardService;
import framework.services.plugins.IDashboardService.DashboardRowConfiguration;
import framework.services.plugins.IDashboardService.DashboardRowConfiguration.WidgetConfiguration;
import models.framework_models.plugin.DashboardRowTemplate;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * The controlle which deals with dashboards
 * 
 * @author Pierre-Yves Cloux
 *
 */
@SubjectPresent
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
    
    public Result index(Long dashboardPageId){
        return ok(views.html.dashboard.index.render(1l,routes.DashboardController.configure(1l).url()));
    }

    /**
     * Ajax method returning the dashboard configuration for the specified 
     * dashboard page
     * @param dashboardPageId the unique id of a dashboard page
     * @return
     */
    public Promise<Result> configure(Long dashboardPageId) {
        try {
            return Promise.promise(new Function0<Result>() {
                @Override
                public Result apply() throws Throwable {
                    DashboardPageConfiguration dpc=new DashboardPageConfiguration();
                    List<DashboardRowConfiguration> list=new ArrayList<>();
                    DashboardRowConfiguration drc1=new DashboardRowConfiguration();
                    drc1.setLayout(DashboardRowTemplate.TPL12COL_1);
                    drc1.setWidgets(Arrays.asList(new WidgetConfiguration(1l,"/dash/widget/display/1")));
                    list.add(drc1);
                    DashboardRowConfiguration drc2=new DashboardRowConfiguration();
                    drc2.setLayout(DashboardRowTemplate.TPL66COL_2);
                    drc2.setWidgets(Arrays.asList(new WidgetConfiguration(2l,"/dash/widget/display/2"), new WidgetConfiguration(-1l,null)));
                    list.add(drc2);
                    DashboardRowConfiguration drc3=new DashboardRowConfiguration();
                    drc3.setLayout(DashboardRowTemplate.TPL444COL_3);
                    drc3.setWidgets(Arrays.asList(new WidgetConfiguration(3l,"/dash/widget/display/3"), new WidgetConfiguration(-1l,null), new WidgetConfiguration(-1l,null)));
                    list.add(drc3);
                    dpc.setDashboardData(list);
                    JsonNode node=getObjectMapper().valueToTree(dpc);
                    return ok(node);
                }
            });
        } catch (Exception e) {
            return Promise.promise(() -> badRequest());
        }
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
     * Return the widget catalog
     * @return
     */
    public Promise<Result> getWidgetCatalog(){
        String catalog="[{\"identifier\": \"WG1\", \"name\" : \"Une super widget\", \"description\" : \"Widget de la mort qui tue<br/>Sans blague !\", \"image\": \"/assets/images/logo.png\"}"
        +",{\"identifier\": \"WG2\", \"name\" : \"Cool la widget\", \"description\" : \"Widget qui le fait bien<br/>Ouais !\", \"image\": \"/assets/images/logo.png\"}"
        +",{\"identifier\": \"WG3\", \"name\" : \"Top la widget\", \"description\" : \"Widget pas mal du tout<br/>Yesss !\", \"image\": \"/assets/images/logo.png\"}]";
        return Promise.promise(() -> ok(getObjectMapper().readTree(catalog)));
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
     * @param dashboardPageId the unique Id for the dashboard page which is to be associated with this widget
     * @param identifier
     *            a widget identifier
     * @return JSON structure
     */
    public Promise<Result> createNewWidget(Long dashboardPageId, String identifier) {
        return Promise.promise(() -> ok(getObjectMapper().readTree("{ \"id\" : 1, \"url\" : \"/dash/widget/display/1\"}")));
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

    private ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    private IDashboardService getDashboardService() {
        return dashboardService;
    }
    
    /**
     * The data structure (to be serialized to JSON) provided to the dashboard javascript client
     * @author Pierre-Yves Cloux
     */
    public static class DashboardPageConfiguration{
        private Integer maxNumberOfRows=6;
        private String createNewRowAjaxServiceUrl="/dash/row/new/";
        private String createNewWidgetAjaxServiceUrl="/dash/widget/new/";
        private String updateDashboardPageAjaxServiceUrl="/dash/update";
        private String widgetCatalogServiceUrl="/dash/widget/catalog";
        private String ajaxWaitImage="/assets/images/ajax-loader.gif";
        private String unableToLoadWidgetErrorMessage="Unable to load the widget";
        private String confirmWidgetRemoveMessage="Do you really want to remove this widget ?";
        private String confirmDashboardRowRemoveMessage="Do you really want to delete this dashboard row as well as all the widgets which it includes ?";
        private String dragWidgetMessage="Drag your widgets here or";
        private String addANewWidgetMessage="add a new widget";
        private List<DashboardRowConfiguration> dashboardData;
        public Integer getMaxNumberOfRows() {
            return maxNumberOfRows;
        }
        public void setMaxNumberOfRows(Integer maxNumberOfRows) {
            this.maxNumberOfRows = maxNumberOfRows;
        }
        public String getCreateNewRowAjaxServiceUrl() {
            return createNewRowAjaxServiceUrl;
        }
        public void setCreateNewRowAjaxServiceUrl(String createNewRowAjaxServiceUrl) {
            this.createNewRowAjaxServiceUrl = createNewRowAjaxServiceUrl;
        }
        public String getCreateNewWidgetAjaxServiceUrl() {
            return createNewWidgetAjaxServiceUrl;
        }
        public void setCreateNewWidgetAjaxServiceUrl(String createNewWidgetAjaxServiceUrl) {
            this.createNewWidgetAjaxServiceUrl = createNewWidgetAjaxServiceUrl;
        }
        public String getUpdateDashboardPageAjaxServiceUrl() {
            return updateDashboardPageAjaxServiceUrl;
        }
        public void setUpdateDashboardPageAjaxServiceUrl(String updateDashboardPageAjaxServiceUrl) {
            this.updateDashboardPageAjaxServiceUrl = updateDashboardPageAjaxServiceUrl;
        }
        public String getWidgetCatalogServiceUrl() {
            return widgetCatalogServiceUrl;
        }
        public void setWidgetCatalogServiceUrl(String widgetCatalogServiceUrl) {
            this.widgetCatalogServiceUrl = widgetCatalogServiceUrl;
        }
        public String getAjaxWaitImage() {
            return ajaxWaitImage;
        }
        public void setAjaxWaitImage(String ajaxWaitImage) {
            this.ajaxWaitImage = ajaxWaitImage;
        }
        public String getUnableToLoadWidgetErrorMessage() {
            return unableToLoadWidgetErrorMessage;
        }
        public void setUnableToLoadWidgetErrorMessage(String unableToLoadWidgetErrorMessage) {
            this.unableToLoadWidgetErrorMessage = unableToLoadWidgetErrorMessage;
        }
        public String getConfirmWidgetRemoveMessage() {
            return confirmWidgetRemoveMessage;
        }
        public void setConfirmWidgetRemoveMessage(String confirmWidgetRemoveMessage) {
            this.confirmWidgetRemoveMessage = confirmWidgetRemoveMessage;
        }
        public String getConfirmDashboardRowRemoveMessage() {
            return confirmDashboardRowRemoveMessage;
        }
        public void setConfirmDashboardRowRemoveMessage(String confirmDashboardRowRemoveMessage) {
            this.confirmDashboardRowRemoveMessage = confirmDashboardRowRemoveMessage;
        }
        public String getDragWidgetMessage() {
            return dragWidgetMessage;
        }
        public void setDragWidgetMessage(String dragWidgetMessage) {
            this.dragWidgetMessage = dragWidgetMessage;
        }
        public String getAddANewWidgetMessage() {
            return addANewWidgetMessage;
        }
        public void setAddANewWidgetMessage(String addANewWidgetMessage) {
            this.addANewWidgetMessage = addANewWidgetMessage;
        }
        public List<DashboardRowConfiguration> getDashboardData() {
            return dashboardData;
        }
        public void setDashboardData(List<DashboardRowConfiguration> dashboardData) {
            this.dashboardData = dashboardData;
        }
    }
}
