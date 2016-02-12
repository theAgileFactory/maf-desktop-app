package controllers.dashboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import controllers.ControllersUtils;
import dao.pmo.ActorDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.configuration.IImplementationDefinedObjectService;
import framework.services.plugins.DashboardException;
import framework.services.plugins.IDashboardService;
import framework.services.plugins.IDashboardService.DashboardRowConfiguration;
import framework.services.plugins.IDashboardService.DashboardRowConfiguration.WidgetConfiguration;
import framework.services.plugins.IDashboardService.WidgetCatalogEntry;
import framework.services.plugins.IPluginManagerService;
import framework.services.plugins.IPluginManagerService.IPluginInfo;
import framework.services.session.IUserSessionManagerPlugin;
import framework.utils.Msg;
import models.framework_models.plugin.DashboardPage;
import models.framework_models.plugin.DashboardRowTemplate;
import models.framework_models.plugin.DashboardWidget;
import models.framework_models.plugin.DashboardWidgetColor;
import models.framework_models.plugin.PluginConfiguration;
import models.pmo.Actor;
import play.Configuration;
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
    @Inject
    private IUserSessionManagerPlugin userSessionManagerPlugin;
    @Inject
    private IPluginManagerService pluginManagerService;
    @Inject
    private Configuration configuration;

    private ObjectMapper objectMapper;

    /**
     * Default constructor.
     */
    public DashboardController() {
        objectMapper = new ObjectMapper();
    }

    /**
     * Display the specified dashboard page.
     * 
     * @param id
     *            a unique dashboard page id
     * @param editMode
     *            true if the edit mode should be opened
     */
    public Promise<Result> index(final Long id, Boolean editMode) {

        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {

                try {

                    // check if the user has an actor
                    boolean hasActor = false;
                    String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
                    Actor actor = ActorDao.getActorByUid(uid);
                    if (actor != null && actor.id != null) {
                        hasActor = true;
                    }

                    List<Triple<String, Boolean, Long>> pages = getDashboardService().getDashboardPages(null);
                    if (pages == null || pages.size() == 0) {

                        Logger.info("The user has no dashboard page => try to create the default configuration");

                        // create an empty page
                        List<DashboardRowConfiguration> defaultConfig = new ArrayList<>();
                        getDashboardService().createDashboardPage(null, Msg.get("dashboard.object.page.name.default"), true, defaultConfig);
                        pages = getDashboardService().getDashboardPages(null);
                        Triple<String, Boolean, Long> page = pages.get(0);

                        Ebean.beginTransaction();
                        try {

                            // Add the default widgets for the home page

                            PluginConfiguration pluginConfiguration = PluginConfiguration.find.where().eq("pluginDefinition.identifier", "widgetkit1")
                                    .findUnique();
                            DashboardPage dashboardPage = DashboardPage.find.where().eq("id", page.getRight()).findUnique();
                            IPluginInfo info = getPluginManagerService().getRegisteredPluginDescriptors().get(pluginConfiguration.id);

                            Triple<Long, String, String> widget1Info = generateWidget("WG6", dashboardPage, pluginConfiguration, info);
                            Triple<Long, String, String> widget2Info = generateWidget("WG3", dashboardPage, pluginConfiguration, info);
                            Triple<Long, String, String> widget3Info = generateWidget("WG2", dashboardPage, pluginConfiguration, info);
                            Triple<Long, String, String> widget4Info = generateWidget("WG4", dashboardPage, pluginConfiguration, info);

                            DashboardRowConfiguration row1 = new DashboardRowConfiguration();
                            row1.setLayout(DashboardRowTemplate.TPL84COL_2);
                            row1.setWidgets(Arrays.asList(new WidgetConfiguration(widget1Info.getLeft(), widget1Info.getMiddle(), widget1Info.getRight()),
                                    new WidgetConfiguration(widget2Info.getLeft(), widget2Info.getMiddle(), widget2Info.getRight())));
                            defaultConfig.add(row1);

                            DashboardRowConfiguration row2 = new DashboardRowConfiguration();
                            row2.setLayout(DashboardRowTemplate.TPL84COL_2);
                            row2.setWidgets(Arrays.asList(new WidgetConfiguration(widget3Info.getLeft(), widget3Info.getMiddle(), widget3Info.getRight()),
                                    new WidgetConfiguration(widget4Info.getLeft(), widget4Info.getMiddle(), widget4Info.getRight())));
                            defaultConfig.add(row2);

                            Ebean.commitTransaction();
                            Ebean.endTransaction();

                        } catch (Exception e) {

                            Ebean.rollbackTransaction();
                            Ebean.endTransaction();
                            Logger.error("Impossible to create the default configuration for the widgets => a blank page is created instead", e);

                            // Add a blank row
                            DashboardRowConfiguration row = new DashboardRowConfiguration();
                            row.setLayout(DashboardRowTemplate.TPL12COL_1);
                            row.setWidgets(Arrays.asList(new WidgetConfiguration(-1L, null, null)));
                            defaultConfig.add(row);

                        }

                        getDashboardService().updateDashboardPageConfiguration(page.getRight(), null, defaultConfig);

                    }

                    Long pageId = id;
                    if (id == 0) {
                        pageId = getDashboardService().getHomeDashboardPageId(null);
                    }
                    Triple<String, Boolean, List<DashboardRowConfiguration>> dashboardPageConfiguration = getDashboardService()
                            .getDashboardPageConfiguration(pageId, null);

                    if (dashboardPageConfiguration == null) {
                        return notFound(views.html.error.not_found.render(""));
                    }

                    return ok(views.html.dashboard.index.render(pageId, editMode, dashboardPageConfiguration.getMiddle(),
                            dashboardPageConfiguration.getLeft(), dashboardPageConfiguration.getRight(), pages,
                            routes.DashboardController.configure(pageId).url(), routes.DashboardController.indexError(pageId, "").url(), hasActor));
                } catch (DashboardException e) {

                    return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getI18nMessagePlugin());
                }

            }
        });
    }

    /**
     * Generate a widget for the given id.
     * 
     * @param widgetIdentifier
     *            the widget identifier
     * @param page
     *            the page to add the widget
     * @param pluginConfiguration
     *            the corresponding plugin configuration
     * @param info
     *            the corresponding plugin info
     * 
     * @return a triple<widgetId, url, widgetIdentifier>
     */
    private Triple<Long, String, String> generateWidget(String widgetIdentifier, DashboardPage page, PluginConfiguration pluginConfiguration,
            IPluginInfo info) {

        DashboardWidget widget = new DashboardWidget();
        widget.color = DashboardWidgetColor.PRIMARY.getColor();
        widget.dashboardPage = page;
        widget.identifier = widgetIdentifier;
        widget.pluginConfiguration = pluginConfiguration;
        widget.title = "";
        widget.config = null;
        widget.save();

        return Triple.of(widget.id, info.getLinkToDisplayWidget(widgetIdentifier, widget.id), widgetIdentifier);
    }

    /**
     * Display an unexpected error in case the dashboard cannot be displayed.
     * 
     * @param dashboardPageId
     *            a unique dashboard page id
     * @param message
     *            a message to be logged
     * @return
     */
    public Result indexError(Long dashboardPageId, String message) {
        throw new RuntimeException(new DashboardException("Unable to load the dashboard with id " + dashboardPageId + " : " + message));
    }

    /**
     * Delete the specified dashboard page.
     * 
     * @param dashboardPageId
     *            a unique dashboard page id
     * @return
     */
    public Promise<Result> deleteDashboardPage(Long dashboardPageId) {
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                try {
                    getDashboardService().deleteDashboardPage(dashboardPageId, null);
                    return ok();
                } catch (Exception e) {
                    log.error("Unable to delete the dashboard page", e);
                    return badRequest();
                }
            }
        });
    }

    /**
     * Add a new dashboard page.<br/>
     * This is a JSON post of the following format:
     * 
     * <pre>
     * {@code
     *      {name : "pageName", "isHome" : true}
     * }
     * </pre>
     * 
     * @return the id of the newly created page as JSON
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Promise<Result> addNewDashboardPage() {
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                try {
                    JsonNode json = request().body().asJson();
                    List<DashboardRowConfiguration> defaultConfig = new ArrayList<>();
                    DashboardRowConfiguration row = new DashboardRowConfiguration();
                    row.setLayout(DashboardRowTemplate.TPL12COL_1);
                    row.setWidgets(Arrays.asList(new WidgetConfiguration(-1L, null, null)));
                    defaultConfig.add(row);
                    Long newDashboardPageId = getDashboardService().createDashboardPage(null, json.get("name").asText(), json.get("isHome").asBoolean(),
                            defaultConfig);
                    JsonNode node = getObjectMapper().readTree("{\"id\" : " + newDashboardPageId + "}");
                    return ok(node);
                } catch (Exception e) {
                    log.error("Unable to add a new dashboard page", e);
                    return badRequest();
                }
            }
        });

    }

    /**
     * Ajax method returning the dashboard configuration for the specified
     * dashboard page.
     * 
     * @param dashboardPageId
     *            the unique id of a dashboard page
     * @return
     */
    public Promise<Result> configure(Long dashboardPageId) {
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                try {
                    Triple<String, Boolean, List<DashboardRowConfiguration>> dashboardPageConfiguration = getDashboardService()
                            .getDashboardPageConfiguration(dashboardPageId, null);
                    DashboardParameters parameters = new DashboardParameters();
                    parameters.setDragWidgetMessage(getI18nMessagePlugin().get("dashboard.drag.widget.message"));
                    parameters.setAddANewWidgetMessage(getI18nMessagePlugin().get("dashboard.drag.widget.link"));
                    parameters.setConfirmDashboardRowRemoveMessage(getI18nMessagePlugin().get("dashboard.row.delete.confirm.message"));
                    parameters.setConfirmWidgetRemoveMessage(getI18nMessagePlugin().get("dashboard.widget.delete.confirm.message"));
                    parameters.setUnableToLoadWidgetErrorMessage(getI18nMessagePlugin().get("dashboard.unable.load.widget.error.message"));
                    parameters.setCannotDeleteTheLastRowMessage(getI18nMessagePlugin().get("dashboard.row.delete.error.message"));
                    parameters.setWarningMessageBoxTitleMessage(getI18nMessagePlugin().get("dashboard.warning.messagebox.title.message"));
                    parameters.setMaxNumberOfRowReachedMessage(getI18nMessagePlugin().get("dashboard.maxnumber.row.message"));
                    parameters.setUnexpectedErrorMessage(getI18nMessagePlugin().get("dashboard.unexpected.error.message"));
                    parameters.setConfirmCurrentPageRemoveMessage(getI18nMessagePlugin().get("dashboard.page.delete.confirm.message"));
                    parameters.setDashboardData(dashboardPageConfiguration.getRight());
                    parameters.setWidgetCatalog(getDashboardService().getWidgetCatalog());
                    parameters.setMaxNumberOfRows(4);
                    parameters.setAjaxWaitImage(getImplementationDefinedObjectService().getRouteForAjaxWaitImage().url());
                    parameters.setUpdateDashboardPageAjaxServiceUrl(routes.DashboardController.updateDashboardPage(dashboardPageId).url());
                    parameters.setWidgetCatalogServiceUrl(routes.DashboardController.getWidgetCatalog().url());
                    parameters.setCreateNewRowAjaxServiceUrl(routes.DashboardController.createNewRow("").url());
                    parameters.setCreateNewWidgetAjaxServiceUrl(routes.DashboardController.createNewWidget(dashboardPageId).url());
                    parameters.setRemoveCurrentDashboardPageServiceUrl(routes.DashboardController.deleteDashboardPage(dashboardPageId).url());
                    parameters.setAddNewDashboardPageServiceUrl(routes.DashboardController.addNewDashboardPage().url());
                    parameters.setDisplayDashboardPageServiceUrl(routes.DashboardController.index(0, false).url());
                    parameters.setErrorWidgetServiceUrl(routes.DashboardController.getErrorWidget(0).url());
                    parameters.setSetAsHomePageServiceUrl(routes.DashboardController.setAsHomePage(dashboardPageId).url());
                    parameters.setRenamePageServiceUrl(routes.DashboardController.renameDashboardPage(dashboardPageId, "").url());

                    JsonNode node = getObjectMapper().valueToTree(parameters);
                    return ok(node);
                } catch (Exception e) {
                    log.error("Unable to configure the dashboard page", e);
                    return badRequest();
                }
            }
        });
    }

    /**
     * Update the dashboard page configuration.<br/>
     * This method expects a POST with a JSON structure.
     * 
     * @param dashboardPageId
     *            the unique id of a dashboard page
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Promise<Result> updateDashboardPage(Long dashboardPageId) {
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                try {
                    JsonNode json = request().body().asJson();
                    List<DashboardRowConfiguration> dashboardPageConfiguration = getObjectMapper().readValue(json.toString(),
                            new TypeReference<List<DashboardRowConfiguration>>() {
                    });
                    getDashboardService().updateDashboardPageConfiguration(dashboardPageId, null, dashboardPageConfiguration);
                    return ok();
                } catch (Exception e) {
                    log.error("Unable to update the dashboard page", e);
                    return badRequest();
                }
            }
        });
    }

    /**
     * Return the widget catalog as JSON.
     */
    public Promise<Result> getWidgetCatalog() {
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                try {
                    JsonNode node = getObjectMapper().valueToTree(getDashboardService().getWidgetCatalog());
                    return ok(node);
                } catch (Exception e) {
                    log.error("Unable to return the widget catalog", e);
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
     * @param dashboardPageId
     *            the unique id of a dashboard page
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
     * Rename the current page.
     * 
     * @param dashboardPageId
     *            the unique id of a dashboard page
     * @param name
     *            the new name for the page
     */
    public Promise<Result> renameDashboardPage(Long dashboardPageId, String name) {
        try {
            getDashboardService().updateDashboardPageName(dashboardPageId, null, name);
            JsonNode node = getObjectMapper().readTree("{\"id\" : " + dashboardPageId + "}");
            return Promise.promise(() -> ok(node));
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
     * @param dashboardPageId
     *            the unique Id for the dashboard page which is to be associated
     *            with this widget
     * @return JSON structure
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Promise<Result> createNewWidget(Long dashboardPageId) {
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                try {
                    JsonNode json = request().body().asJson();
                    WidgetCatalogEntry widgetCatalogEntry = getObjectMapper().readValue(json.toString(), WidgetCatalogEntry.class);
                    Pair<Long, String> widgetConfig = getDashboardService().createNewWidget(dashboardPageId, null, widgetCatalogEntry, "A title");
                    JsonNode node = getObjectMapper().readTree("{\"id\" : " + widgetConfig.getLeft() + ",\"url\" : \"" + widgetConfig.getRight()
                            + "\",\"identifier\" : \"" + widgetCatalogEntry.getIdentifier() + "\"}");
                    return ok(node);
                } catch (Exception e) {
                    log.error("Unable to create a new widget", e);
                    return badRequest();
                }
            }
        });
    }

    /**
     * Return an error widget to be displayed when loading a widget failed.
     * 
     * @param widgetId
     *            the widget id
     * @return an error widget fragment
     */
    public Promise<Result> getErrorWidget(Long widgetId) {
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                try {
                    // Check if the widget exists
                    if (getDashboardService().isWidgetExists(widgetId)) {
                        return ok(views.html.framework_views.dashboard.error_widget.render(widgetId,
                                getI18nMessagePlugin().get("dashboard.widget.error.title", String.valueOf(widgetId)),
                                getI18nMessagePlugin().get("dashboard.widget.error.unexpected")));
                    }
                    return ok(views.html.framework_views.dashboard.error_widget.render(widgetId,
                            getI18nMessagePlugin().get("dashboard.widget.error.title", String.valueOf(widgetId)),
                            getI18nMessagePlugin().get("dashboard.widget.error.deleted")));
                } catch (Exception e) {
                    log.error("Unexpected error for the widget error !", e);
                    return ok(views.html.framework_views.dashboard.error_widget.render(widgetId,
                            getI18nMessagePlugin().get("dashboard.widget.error.title", String.valueOf(widgetId)),
                            getI18nMessagePlugin().get("dashboard.widget.error.unexpected")));
                }
            }
        });
    }

    /**
     * Get the object mapper.
     */
    private ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Get the dashboard service.
     */
    private IDashboardService getDashboardService() {
        return dashboardService;
    }

    /**
     * Get the i18n messages service.
     * 
     * @return
     */
    private II18nMessagesPlugin getI18nMessagePlugin() {
        return i18nMessagePlugin;
    }

    /**
     * Get the user sesion manager service.
     */
    private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return this.userSessionManagerPlugin;
    }

    /**
     * Get the plugin manager service.
     */
    private IPluginManagerService getPluginManagerService() {
        return this.pluginManagerService;
    }

    /**
     * Get the implementation defined object service.
     */
    private IImplementationDefinedObjectService getImplementationDefinedObjectService() {
        return implementationDefinedObjectService;
    }

    /**
     * Get the Play configuration.
     */
    private Configuration getConfiguration() {
        return this.configuration;
    }

    /**
     * The data structure (to be serialized to JSON) provided to the dashboard
     * javascript client.
     * 
     * @author Pierre-Yves Cloux
     */
    public static class DashboardParameters {
        private Integer maxNumberOfRows = 6;
        private String createNewRowAjaxServiceUrl;
        private String createNewWidgetAjaxServiceUrl;
        private String updateDashboardPageAjaxServiceUrl;
        private String removeCurrentDashboardPageServiceUrl;
        private String addNewDashboardPageServiceUrl;
        private String widgetCatalogServiceUrl;
        private String displayDashboardPageServiceUrl;
        private String setAsHomePageServiceUrl;
        private String renamePageServiceUrl;
        private String errorWidgetServiceUrl;
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
        private List<WidgetCatalogEntry> widgetCatalog;

        /**
         * Get the max number of rows.
         */
        public Integer getMaxNumberOfRows() {
            return maxNumberOfRows;
        }

        /**
         * Set the max number of rows.
         * 
         * @param maxNumberOfRows
         *            the max number of rows
         */
        public void setMaxNumberOfRows(Integer maxNumberOfRows) {
            this.maxNumberOfRows = maxNumberOfRows;
        }

        /**
         * Get the URL of the "create new row" service.
         */
        public String getCreateNewRowAjaxServiceUrl() {
            return createNewRowAjaxServiceUrl;
        }

        /**
         * Set the URL of the "create new row" service.
         * 
         * @param createNewRowAjaxServiceUrl
         *            the url
         */
        public void setCreateNewRowAjaxServiceUrl(String createNewRowAjaxServiceUrl) {
            this.createNewRowAjaxServiceUrl = createNewRowAjaxServiceUrl;
        }

        /**
         * Get the URL of the "create new widget" service.
         */
        public String getCreateNewWidgetAjaxServiceUrl() {
            return createNewWidgetAjaxServiceUrl;
        }

        /**
         * Set the URL of the "create new widget" service.
         * 
         * @param createNewWidgetAjaxServiceUrl
         *            the url
         */
        public void setCreateNewWidgetAjaxServiceUrl(String createNewWidgetAjaxServiceUrl) {
            this.createNewWidgetAjaxServiceUrl = createNewWidgetAjaxServiceUrl;
        }

        /**
         * Get the URL of the "update dashboard" service.
         */
        public String getUpdateDashboardPageAjaxServiceUrl() {
            return updateDashboardPageAjaxServiceUrl;
        }

        /**
         * Set the URL of the "update dashboard page" service.
         * 
         * @param updateDashboardPageAjaxServiceUrl
         *            the url
         */
        public void setUpdateDashboardPageAjaxServiceUrl(String updateDashboardPageAjaxServiceUrl) {
            this.updateDashboardPageAjaxServiceUrl = updateDashboardPageAjaxServiceUrl;
        }

        /**
         * Get the URL of the "widget catalog" service.
         */
        public String getWidgetCatalogServiceUrl() {
            return widgetCatalogServiceUrl;
        }

        /**
         * Set the URL of the "widget catalog" service.
         * 
         * @param widgetCatalogServiceUrl
         *            the url
         */
        public void setWidgetCatalogServiceUrl(String widgetCatalogServiceUrl) {
            this.widgetCatalogServiceUrl = widgetCatalogServiceUrl;
        }

        /**
         * Get the url of ajax wait image.
         */
        public String getAjaxWaitImage() {
            return ajaxWaitImage;
        }

        /**
         * Set the url of ajax wait image.
         * 
         * @param ajaxWaitImage
         *            the url
         */
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

        public String getRenamePageServiceUrl() {
            return renamePageServiceUrl;
        }

        public void setRenamePageServiceUrl(String renamePageServiceUrl) {
            this.renamePageServiceUrl = renamePageServiceUrl;
        }

        public String getErrorWidgetServiceUrl() {
            return errorWidgetServiceUrl;
        }

        public void setErrorWidgetServiceUrl(String errorWidgetServiceUrl) {
            this.errorWidgetServiceUrl = errorWidgetServiceUrl;
        }

        public List<WidgetCatalogEntry> getWidgetCatalog() {
            return widgetCatalog;
        }

        public void setWidgetCatalog(List<WidgetCatalogEntry> widgetCatalog) {
            this.widgetCatalog = widgetCatalog;
        }
    }
}
