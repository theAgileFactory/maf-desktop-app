package framework.services.plugins;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import models.framework_models.plugin.DashboardRowTemplate;

/**
 * A service to manage the user dashboard
 * @author Pierre-Yves Cloux
 */
public interface IDashboardService {
    public static final Long NO_WIDGET_ID=-1l;
    
    /**
     * Return the ordered list of dashboard pages for a named user.<br/>
     * This is a list of tuples:
     * <ul>
     * <li>[1] the page title/name</li>
     * <li>[2] the dashboard page id</li>
     * </ul>
     * @param uid the UID of a user or null (if null the current user is used)
     * @return 
     */
    public List<Pair<String, Long>> getDashboardPages(String uid) throws DashboardException;
    
    /**
     * Return the DashboardPage configuration for the specified id and the specified user.<br/>
     * The code must check if the specified dashboard page belongs to the specified user.
     * This is a tuple:
     * <ul>
     * <li>[1] the page title/name</li>
     * <li>[2] true if the page is the user home page (default displayed)</li>
     * <li>[3] the page content configuration</li>
     * </ul>
     * @param id a unique dashboard page Id
     * @param uid the UID of a user or null (if null the current user is used)
     * @return 
     */
    public Triple<String, Boolean, List<DashboardRowConfiguration>> getDashboardPageConfiguration(Long id, String uid) throws DashboardException;
    
    /**
     * Update the configuration of the dashboard page.<br/>
     * The code must check if the specified dashboard page belongs to the specified user.
     * @param id the id of the dashboard page
     * @param uid the UID of a user or null (if null the current user is used)
     * @param config the page configuration
     * @throws DashboardException
     */
    public void updateDashboardPageConfiguration(Long id, String uid, List<DashboardRowConfiguration> config) throws DashboardException;
    
    /**
     * Update the name of a dashboard page.<br/>
     * The code must check if the specified dashboard page belongs to the specified user.
     * @param id the id of the dashboard page
     * @param uid the UID of a user or null (if null the current user is used)
     * @param name the name of the page to be updated
     * @throws DashboardException
     */
    public void updateDashboardPageName(Long id, String uid, String name) throws DashboardException;
    
    
    /**
     * Set the specified page as home.<br/>
     * The code must check if the specified dashboard page belongs to the specified user.
     * @param id the id of the dashboard page
     * @param uid the UID of a user or null (if null the current user is used)
     * @throws DashboardException
     */
    public void setDashboardPageAsHome(Long id, String uid) throws DashboardException;
    
    
    /**
     * Create the configuration of the dashboard page.<br/>
     * @param uid the UID of a user or null (if null the current user is used)
     * @param name the name/title of the page
     * @param isHome true if the page is the home page of the dashboard
     * @param config the page configuration
     * @throws DashboardException
     */
    public void createDashboardPage(String uid, String name, Boolean isHome, List<DashboardRowConfiguration> config) throws DashboardException;
    
    /**
     * Delete the configuration of the dashboard page.<br/>
     * The code must check if the specified dashboard page belongs to the specified user.
     * @param id the id of the dashboard page
     * @param uid the UID of a user or null (if null the current user is used)
     * @throws DashboardException
     */
    public void deleteDashboardPage(Long id, String uid) throws DashboardException;
    
    
    /**
     * Creates a new widget from the widget catalog entry
     * and return its unique id.<br/>
     * This throws an exception of this widget entry is not consistent.
     */
    public Long createNewWidget(WidgetCatalogEntry widgetCatalogEntry) throws DashboardException;
    
    /**
     * A data structure which represents a dashboard row configuration.<br/>
     * It consists in :
     * <ul>
     * <li>a layout : instance of {@link DashboardRowTemplate}</li>
     * <li>widgetIds : an ordered list of widgets ids</li>
     * </ul>
     * @author Pierre-Yves Cloux
     */
    public static class DashboardRowConfiguration{        
        private DashboardRowTemplate layout;
        private List<Long> widgetIds;
        
        public DashboardRowConfiguration() {
            super();
        }

        public DashboardRowTemplate getLayout() {
            return layout;
        }
        public void setLayout(DashboardRowTemplate layout) {
            this.layout = layout;
        }
        public List<Long> getWidgetIds() {
            return widgetIds;
        }
        public void setWidgetIds(List<Long> widgetIds) {
            this.widgetIds = widgetIds;
        }
    }
    
    /**
     * A data structure which is to be used to manage a widget catalog entry.<br/>
     * This one contains:
     * <ul>
     * <li>pluginConfigurationName : the name of the plugin configuration</li>
     * <li>pluginConfigurationId : the plugin configuration id</li>
     * <li>identifier : the widget identifier</li>
     * <li>name : the name of the widget</li>
     * <li>description : the description of the widget</li>
     * </ul>
     * @author Pierre-Yves Cloux
     */
    public static class WidgetCatalogEntry{
        private String pluginConfigurationName;
        private Long pluginConfigurationId;
        private String identifier;
        private String name;
        private String description;
        public String getPluginConfigurationName() {
            return pluginConfigurationName;
        }
        public void setPluginConfigurationName(String pluginConfigurationName) {
            this.pluginConfigurationName = pluginConfigurationName;
        }
        public Long getPluginConfigurationId() {
            return pluginConfigurationId;
        }
        public void setPluginConfigurationId(Long pluginConfigurationId) {
            this.pluginConfigurationId = pluginConfigurationId;
        }
        public String getIdentifier() {
            return identifier;
        }
        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }
    }
}
