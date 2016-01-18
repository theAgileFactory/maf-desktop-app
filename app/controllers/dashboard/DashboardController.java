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
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.configuration.IImplementationDefinedObjectService;
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
    @Inject
    private II18nMessagesPlugin i18nMessagePlugin;
    @Inject
    private IImplementationDefinedObjectService implementationDefinedObjectService;
    
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
     * Delete the specified dashboard page
     * @param dashboardPageId a unique dashboard page id
     * @return
     */
    public Promise<Result> deleteDashboardPage(Long dashboardPageId){
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                try{
                    getDashboardService().deleteDashboardPage(dashboardPageId, null);
                    return ok();
                } catch (Exception e) {
                    log.error("Unable to delete the dashboard page",e);
                    return badRequest();
                }
            }
        });
    }
    
    /**
     * Add a new dashboard page.<br/>
     * This is a JSON post of the following format:
     * <pre>
     * {@code
     *      {name : "pageName", "isHome" : true}
     * }
     * </pre>
     * @return the id of the newly created page as JSON
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Promise<Result> addNewDashboardPage(){
            return Promise.promise(new Function0<Result>() {
                @Override
                public Result apply() throws Throwable {
                    try {
                        JsonNode json = request().body().asJson();
                        List<DashboardRowConfiguration> defaultConfig=new ArrayList<>();
                        DashboardRowConfiguration row=new DashboardRowConfiguration();
                        row.setLayout(DashboardRowTemplate.TPL12COL_1);
                        row.setWidgets(Arrays.asList(new WidgetConfiguration(-1l,null)));
                        defaultConfig.add(row);
                        Long newDashboardPageId=getDashboardService().createDashboardPage(null, 
                                json.get("name").asText(), 
                                json.get("isHome").asBoolean(), 
                                defaultConfig);
                        JsonNode node=getObjectMapper().readTree("{\"id\" : "+newDashboardPageId+"}");
                        return ok(node);
                    } catch (Exception e) {
                        log.error("Unable to add a new dashboard page",e);
                        return badRequest();
                    }
                }
            });

    }

    /**
     * Ajax method returning the dashboard configuration for the specified 
     * dashboard page
     * @param dashboardPageId the unique id of a dashboard page
     * @return
     */
    public Promise<Result> configure(Long dashboardPageId) {
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                try{
                    Triple<String, Boolean, List<DashboardRowConfiguration>>  dashboardPageConfiguration=getDashboardService().getDashboardPageConfiguration(dashboardPageId, null);
                    DashboardParameters parameters=new DashboardParameters();
                    parameters.setDragWidgetMessage(getI18nMessagePlugin().get("dashboard.drag.widget.message"));
                    parameters.setAddANewWidgetMessage(getI18nMessagePlugin().get("dashboard.add.new.widget.message"));
                    parameters.setConfirmDashboardRowRemoveMessage(getI18nMessagePlugin().get("dashboard.confirm.row.remove.message"));
                    parameters.setConfirmWidgetRemoveMessage(getI18nMessagePlugin().get("dashboard.confirm.widget.remove.message"));
                    parameters.setUnableToLoadWidgetErrorMessage(getI18nMessagePlugin().get("dashboard.unable.load.widget.error.message"));
                    parameters.setCannotDeleteTheLastRowMessage(getI18nMessagePlugin().get("dashboard.cannot.delete.last.row.message"));
                    parameters.setWarningMessageBoxTitleMessage(getI18nMessagePlugin().get("dashboard.warning.messagebox.title.message"));
                    parameters.setMaxNumberOfRowReachedMessage(getI18nMessagePlugin().get("dashboard.maxnumber.row.message"));
                    parameters.setUnexpectedErrorMessage(getI18nMessagePlugin().get("dashboard.unexpected.error.message"));
                    parameters.setConfirmCurrentPageRemoveMessage(getI18nMessagePlugin().get("dashboard.confirm.currentpage.remove.message"));
                    parameters.setDashboardData(dashboardPageConfiguration.getRight());
                    parameters.setMaxNumberOfRows(4);
                    parameters.setAjaxWaitImage(getImplementationDefinedObjectService().getRouteForAjaxWaitImage().url());
                    parameters.setUpdateDashboardPageAjaxServiceUrl(routes.DashboardController.updateDashboardPage(dashboardPageId).url());
                    parameters.setWidgetCatalogServiceUrl(routes.DashboardController.getWidgetCatalog().url());
                    parameters.setCreateNewRowAjaxServiceUrl(routes.DashboardController.createNewRow("").url());
                    parameters.setCreateNewWidgetAjaxServiceUrl(routes.DashboardController.createNewWidget(dashboardPageId).url());
                    parameters.setRemoveCurrentDashboardPageServiceUrl(routes.DashboardController.deleteDashboardPage(dashboardPageId).url());
                    parameters.setAddNewDashboardPageServiceUrl(routes.DashboardController.addNewDashboardPage().url());
                    parameters.setDisplayDashboardPageServiceUrl(routes.DashboardController.index(0).url());
                    parameters.setSetAsHomePageServiceUrl(routes.DashboardController.setAsHomePage(dashboardPageId).url());
                    JsonNode node=getObjectMapper().valueToTree(parameters);
                    return ok(node);
                } catch (Exception e) {
                    log.error("Unable to configure the dashboard page",e);
                    return badRequest();
                }
            }
        });
    }

    /**
     * Update the dashboard page configuration.<br/>
     * This method expects a POST with a JSON structure.
     * @param dashboardPageId the unique id of a dashboard page
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Promise<Result> updateDashboardPage(Long dashboardPageId) {
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                try{
                    JsonNode json = request().body().asJson();
                    List<DashboardRowConfiguration> dashboardPageConfiguration=getObjectMapper().readValue(json.toString(), new TypeReference<List<DashboardRowConfiguration>>() {});
                    getDashboardService().updateDashboardPageConfiguration(dashboardPageId, null, dashboardPageConfiguration);
                    return ok();
                } catch (Exception e) {
                    log.error("Unable to update the dashboard page",e);
                    return badRequest();
                }
            }
        });
    }
    
    /**
     * Return the widget catalog as JSON
     * @return
     */
    public Promise<Result> getWidgetCatalog(){
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                try {
                    JsonNode node=getObjectMapper().valueToTree(getDashboardService().getWidgetCatalog());
                    return ok(node);
                } catch (Exception e) {
                    log.error("Unable to return the widget catalog",e);
                    return badRequest();
                }
            }
        });
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
     * Set the specfied page as home page.
     * 
     * @param dashboardPageId the unique id of a dashboard page
     */
    public Promise<Result> setAsHomePage(Long dashboardPageId) {
        try {
            getDashboardService().setDashboardPageAsHome(dashboardPageId, null);
            return Promise.promise(() -> ok());
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
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                try{
                    JsonNode json = request().body().asJson();
                    WidgetCatalogEntry widgetCatalogEntry=getObjectMapper().readValue(json.toString(), WidgetCatalogEntry.class);
                    Pair<Long, String> widgetConfig=getDashboardService().createNewWidget(dashboardPageId, null, widgetCatalogEntry, "A title");
                    JsonNode node=getObjectMapper().readTree("{\"id\" : "+widgetConfig.getLeft()+",\"url\" : \""+widgetConfig.getRight()+"\"}");
                    return ok(node);
                } catch (Exception e) {
                    log.error("Unable to create a new widget",e);
                    return badRequest();
                }
            }
        });
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
    
    private II18nMessagesPlugin getI18nMessagePlugin() {
        return i18nMessagePlugin;
    }

    private IImplementationDefinedObjectService getImplementationDefinedObjectService() {
        return implementationDefinedObjectService;
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
        private String removeCurrentDashboardPageServiceUrl;
        private String addNewDashboardPageServiceUrl;
        private String widgetCatalogServiceUrl;
        private String displayDashboardPageServiceUrl;
        private String setAsHomePageServiceUrl;
        private String ajaxWaitImage;
        private String unableToLoadWidgetErrorMessage;
        private String confirmWidgetRemoveMessage;
        private String confirmDashboardRowRemoveMessage;
        private String dragWidgetMessage;
        private String addANewWidgetMessage;
        private String cannotDeleteTheLastRowMessage;
        private String warningMessageBoxTitleMessage;
        private String maxNumberOfRowReachedMessage;
        private String unexpectedErrorMessage;
        private String confirmCurrentPageRemoveMessage;
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
        public String getUnexpectedErrorMessage() {
            return unexpectedErrorMessage;
        }
        public void setUnexpectedErrorMessage(String unexpectedErrorMessage) {
            this.unexpectedErrorMessage = unexpectedErrorMessage;
        }
        public String getRemoveCurrentDashboardPageServiceUrl() {
            return removeCurrentDashboardPageServiceUrl;
        }
        public void setRemoveCurrentDashboardPageServiceUrl(String removeCurrentDashboardPageServiceUrl) {
            this.removeCurrentDashboardPageServiceUrl = removeCurrentDashboardPageServiceUrl;
        }
        public String getAddNewDashboardPageServiceUrl() {
            return addNewDashboardPageServiceUrl;
        }
        public void setAddNewDashboardPageServiceUrl(String addNewDashboardPageServiceUrl) {
            this.addNewDashboardPageServiceUrl = addNewDashboardPageServiceUrl;
        }
        public String getConfirmCurrentPageRemoveMessage() {
            return confirmCurrentPageRemoveMessage;
        }
        public void setConfirmCurrentPageRemoveMessage(String confirmCurrentPageRemoveMessage) {
            this.confirmCurrentPageRemoveMessage = confirmCurrentPageRemoveMessage;
        }
        public String getDisplayDashboardPageServiceUrl() {
            return displayDashboardPageServiceUrl;
        }
        public void setDisplayDashboardPageServiceUrl(String displayDashboardPageServiceUrl) {
            this.displayDashboardPageServiceUrl = displayDashboardPageServiceUrl;
        }
        public String getSetAsHomePageServiceUrl() {
            return setAsHomePageServiceUrl;
        }
        public void setSetAsHomePageServiceUrl(String setAsHomePageServiceUrl) {
            this.setAsHomePageServiceUrl = setAsHomePageServiceUrl;
        }
    }
}
