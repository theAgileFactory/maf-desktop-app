package controllers.dashboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import framework.services.plugins.DashboardException;
import framework.services.plugins.IDashboardService;
import framework.services.plugins.IDashboardService.DashboardRowConfiguration;
import framework.services.plugins.IDashboardService.DashboardRowConfiguration.WidgetConfiguration;
import framework.services.plugins.IDashboardService.WidgetCatalogEntry;
import models.framework_models.plugin.DashboardRowTemplate;
import play.Logger;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.dashboard.widget;

/**
 * The controller which deals with dashboards.<br/>
 * 
 * @author Pierre-Yves Cloux
 *
 */
@SubjectPresent
public class DashboardController extends Controller {
    private static Logger.ALogger log = Logger.of(DashboardController.class);
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
     * Display the specified dashboard page
     * @param dashboardPageId a unique dashboard page id
     * @return
     */
    public Result index(Long dashboardPageId){
        try{
            List<Triple<String, Boolean, Long>> pages=getDashboardService().getDashboardPages(null);
            if(pages==null || pages.size()==0){
                //Creates a default home page
                List<DashboardRowConfiguration> defaultConfig=new ArrayList<>();
                DashboardRowConfiguration row=new DashboardRowConfiguration();
                row.setLayout(DashboardRowTemplate.TPL12COL_1);
                row.setWidgets(Arrays.asList(new WidgetConfiguration(-1l,null)));
                defaultConfig.add(row);
                getDashboardService().createDashboardPage(null, "Home", true, defaultConfig);
                pages=getDashboardService().getDashboardPages(null);
            }
            if(dashboardPageId==0){
                dashboardPageId=getDashboardService().getHomeDashboardPageId(null);
            }
            Triple<String, Boolean, List<DashboardRowConfiguration>> dashboardPageConfiguration=getDashboardService().getDashboardPageConfiguration(dashboardPageId, null);
            return ok(views.html.dashboard.index.render(
                dashboardPageId,
                dashboardPageConfiguration.getMiddle(),
                dashboardPageConfiguration.getLeft(),
                dashboardPageConfiguration.getRight(),
                pages,
                routes.DashboardController.configure(dashboardPageId).url(),
                routes.DashboardController.indexError(dashboardPageId,"").url()));
        }catch(DashboardException e){
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Display an unexpected error in case the dashboard cannot be displayed
     * @param dashboardPageId a unique dashboard page id
     * @param message a message to be logged
     * @return
     */
    public Result indexError(Long dashboardPageId, String message){
        throw new RuntimeException(new DashboardException("Unable to load the dashboard with id "+dashboardPageId+" : "+message));
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
                    Triple<String, Boolean, List<DashboardRowConfiguration>>  dashboardPageConfiguration=getDashboardService().getDashboardPageConfiguration(dashboardPageId, null);
                    DashboardParameters parameters=new DashboardParameters();
                    parameters.setDragWidgetMessage("Drag your widgets here or");
                    parameters.setAddANewWidgetMessage("add a new widget");
                    parameters.setConfirmDashboardRowRemoveMessage("Do you really want to delete this dashboard row as well as all the widgets which it includes ?");
                    parameters.setConfirmWidgetRemoveMessage("Do you really want to remove this widget ?");
                    parameters.setUnableToLoadWidgetErrorMessage("Unable to load the widget");
                    parameters.setCannotDeleteTheLastRowMessage("A dashboard page must have at least one row !");
                    parameters.setWarningMessageBoxTitleMessage("WARNING");
                    parameters.setMaxNumberOfRowReachedMessage("You reached the max number of rows : ");
                    parameters.setDashboardData(dashboardPageConfiguration.getRight());
                    parameters.setMaxNumberOfRows(6);
                    parameters.setAjaxWaitImage("/assets/images/ajax-loader.gif");
                    parameters.setUpdateDashboardPageAjaxServiceUrl(routes.DashboardController.updateDashboardPage(dashboardPageId).url());
                    parameters.setWidgetCatalogServiceUrl(routes.DashboardController.getWidgetCatalog().url());
                    parameters.setCreateNewRowAjaxServiceUrl(routes.DashboardController.createNewRow("").url());
                    parameters.setCreateNewWidgetAjaxServiceUrl(routes.DashboardController.createNewWidget(dashboardPageId).url());
                    JsonNode node=getObjectMapper().valueToTree(parameters);
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
     * @param dashboardPageId the unique id of a dashboard page
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Promise<Result> updateDashboardPage(Long dashboardPageId) {
        try {
            return Promise.promise(new Function0<Result>() {
                @Override
                public Result apply() throws Throwable {
                    JsonNode json = request().body().asJson();
                    List<DashboardRowConfiguration> dashboardPageConfiguration=getObjectMapper().readValue(json.toString(), new TypeReference<List<DashboardRowConfiguration>>() {});
                    getDashboardService().updateDashboardPageConfiguration(dashboardPageId, null, dashboardPageConfiguration);
                    return ok();
                }
            });
        } catch (Exception e) {
            return Promise.promise(() -> badRequest());
        }
    }
    
    /**
     * Return the widget catalog as JSON
     * @return
     */
    public Promise<Result> getWidgetCatalog(){
        try {
            return Promise.promise(new Function0<Result>() {
                @Override
                public Result apply() throws Throwable {
                    JsonNode node=getObjectMapper().valueToTree(getDashboardService().getWidgetCatalog());
                    return ok(node);
                }
            });
        } catch (Exception e) {
            return Promise.promise(() -> badRequest());
        }
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
     * A POST parameter (as JSON is expected) : a widget entry.
     * 
     * @param dashboardPageId the unique Id for the dashboard page which is to be associated with this widget
     * @return JSON structure
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Promise<Result> createNewWidget(Long dashboardPageId) {
        try {
            return Promise.promise(new Function0<Result>() {
                @Override
                public Result apply() throws Throwable {
                    JsonNode json = request().body().asJson();
                    WidgetCatalogEntry widgetCatalogEntry=getObjectMapper().readValue(json.toString(), WidgetCatalogEntry.class);
                    Pair<Long, String> widgetConfig=getDashboardService().createNewWidget(dashboardPageId, null, widgetCatalogEntry, "A title");
                    JsonNode node=getObjectMapper().readTree("{\"id\" : "+widgetConfig.getLeft()+",\"url\" : \""+widgetConfig.getRight()+"\"}");
                    return ok(node);
                }
            });
        } catch (Exception e) {
            return Promise.promise(() -> badRequest());
        }
    }
    
    /**
     * Return an error widget associated with the specified id
     * @param id
     *            a widget id
     * @return an HTML widget part with a standard error message     
     */
    public Result getErrorWidget(Long id){
        return ok(views.html.dashboard.error_widget.render(id));
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
    public static class DashboardParameters{
        private Integer maxNumberOfRows=6;
        private String createNewRowAjaxServiceUrl;
        private String createNewWidgetAjaxServiceUrl;
        private String updateDashboardPageAjaxServiceUrl;
        private String widgetCatalogServiceUrl;
        private String ajaxWaitImage;
        private String unableToLoadWidgetErrorMessage;
        private String confirmWidgetRemoveMessage;
        private String confirmDashboardRowRemoveMessage;
        private String dragWidgetMessage;
        private String addANewWidgetMessage;
        private String cannotDeleteTheLastRowMessage;
        private String warningMessageBoxTitleMessage;
        private String maxNumberOfRowReachedMessage;
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
        public String getCannotDeleteTheLastRowMessage() {
            return cannotDeleteTheLastRowMessage;
        }
        public void setCannotDeleteTheLastRowMessage(String cannotDeleteTheLastRowMessage) {
            this.cannotDeleteTheLastRowMessage = cannotDeleteTheLastRowMessage;
        }
        public String getWarningMessageBoxTitleMessage() {
            return warningMessageBoxTitleMessage;
        }
        public void setWarningMessageBoxTitleMessage(String warningMessageBoxTitleMessage) {
            this.warningMessageBoxTitleMessage = warningMessageBoxTitleMessage;
        }
        public String getMaxNumberOfRowReachedMessage() {
            return maxNumberOfRowReachedMessage;
        }
        public void setMaxNumberOfRowReachedMessage(String maxNumberOfRowReachedMessage) {
            this.maxNumberOfRowReachedMessage = maxNumberOfRowReachedMessage;
        }
    }
}
