package framework.services.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import framework.security.ISecurityService;
import framework.services.account.IUserAccount;
import models.framework_models.account.Principal;
import models.framework_models.plugin.DashboardPage;
import models.framework_models.plugin.DashboardWidget;
import play.Configuration;
import play.Logger;
import play.cache.CacheApi;
import play.inject.ApplicationLifecycle;
import play.libs.F.Promise;

/**
 * The service which is managing the dashboards
 * @author Pierre-Yves Cloux
 */
public class DashboardServiceImpl implements IDashboardService {
    private static Logger.ALogger log = Logger.of(DashboardServiceImpl.class);
    private IPluginManagerService pluginManagerService;
    private CacheApi cacheApi;
    private ISecurityService securityService;
    private ObjectMapper mapper;
    
    @Inject
    public DashboardServiceImpl(ApplicationLifecycle lifecycle, Configuration configuration, IPluginManagerService pluginManagerService, CacheApi cacheApi, ISecurityService securityService) {
        log.info("SERVICE>>> DashboardServiceImpl starting...");
        this.pluginManagerService=pluginManagerService;
        this.cacheApi=cacheApi;
        this.securityService=securityService;
        this.mapper=new ObjectMapper();
        lifecycle.addStopHook(() -> {
            log.info("SERVICE>>> DashboardServiceImpl stopping...");
            log.info("SERVICE>>> DashboardServiceImpl stopped");
            return Promise.pure(null);
        });
        log.info("SERVICE>>> DashboardServiceImpl started");
    }

    @Override
    public Long createNewWidget(WidgetCatalogEntry widgetCatalogEntry) throws DashboardException {
        return null;
    }

    @Override
    public List<Pair<String, Long>> getDashboardPages(String uid) throws DashboardException {
        if(log.isDebugEnabled()){
            log.debug("Request for dashboard pages for user "+(uid==null?"current":uid));
        }
        IUserAccount userAccount = getUserAccount(uid);
        
        List<Pair<String, Long>> dashboardConfig=null;
        Ebean.beginTransaction();
        try {
            List<DashboardPage> dashboardPages=DashboardPage.find.where().eq("principal.id", userAccount.getMafUid()).findList();
            if(log.isDebugEnabled()){
                log.debug("Found "+dashboardPages.size()+" pages for user "+userAccount.getUid());
            }
            if(dashboardPages!=null){
                dashboardConfig=new ArrayList<>();
                for(DashboardPage dashboardPage : dashboardPages){
                    if(log.isDebugEnabled()){
                        log.debug("Found dashboard page "+dashboardPage.name+" with id "+dashboardPage.id);
                    }
                    dashboardConfig.add(Pair.of(dashboardPage.name, dashboardPage.id));
                }
            }
            Ebean.commitTransaction();
        } catch (Exception e) {
            Ebean.rollbackTransaction();
            String message = String.format("Error while getting the dashboard configuration for account uid=%s", userAccount.getUid());
            log.error(message, e);
            throw new DashboardException(message, e);
        } finally {
            Ebean.endTransaction();
        }
        return dashboardConfig;
    }

    @Override
    public Triple<String, Boolean, List<DashboardRowConfiguration>> getDashboardPageConfiguration(Long id, String uid) throws DashboardException {
        if(log.isDebugEnabled()){
            log.debug("Request for dashboard page "+id+" configuration for user "+(uid==null?"current":uid));
        }
        IUserAccount userAccount = getUserAccount(uid);
        String pageName=null;
        Boolean isHome=null;
        List<DashboardRowConfiguration> rows=null;
        Ebean.beginTransaction();
        try {
            DashboardPage dashboardPage=DashboardPage.find.where().eq("id", id).findUnique();
            if(dashboardPage==null || !userAccount.getUid().equals(dashboardPage.getPrincipal().uid)){
                if(log.isDebugEnabled()){
                    log.debug("No page found for id "+id+" and user "+userAccount.getUid());
                }
                return null;
            }
            //Extract the data stored into the database
            try{
                rows=getMapper().readValue(dashboardPage.layout, new TypeReference<List<DashboardRowConfiguration>>() {});
                pageName=dashboardPage.name;
                isHome=dashboardPage.isHome;
            }catch(Exception exp){
                log.error("Unable to read the content of the dashboard configuration from the database ",exp);
                throw exp;
            }
            Ebean.commitTransaction();
        } catch (Exception e) {
            Ebean.rollbackTransaction();
            String message = String.format("No dashboard configuration page for ", id);
            log.error(message, e);
            throw new DashboardException(message, e);
        } finally {
            Ebean.endTransaction();
        }
        return Triple.of(pageName, isHome, rows);
    }

    @Override
    public void createDashboardPage(String uid, String name, Boolean isHome, List<DashboardRowConfiguration> config)
            throws DashboardException {
        if(log.isDebugEnabled()){
            log.debug("Creating dashboard page for user "+(uid==null?"current":uid));
        }
        IUserAccount userAccount = getUserAccount(uid);
        Ebean.beginTransaction();
        try {
            if(isHome){
                //Check if a home page is already defined and then change it
                DashboardPage existingHomePage=DashboardPage.find.where().eq("principal.id", userAccount.getMafUid()).eq("isHome", true).findUnique();
                if(existingHomePage!=null){
                    existingHomePage.isHome=false;
                    existingHomePage.save();
                }
                //Create a new dashboard page
                DashboardPage dashboardPage=new DashboardPage();
                dashboardPage.name=name;
                dashboardPage.isHome=isHome;
                dashboardPage.layout=getMapper().writeValueAsBytes(config);
                dashboardPage.principal=Principal.getPrincipalFromId(userAccount.getMafUid());
                dashboardPage.save();
            }
            Ebean.commitTransaction();
        }catch (Exception e) {
            Ebean.rollbackTransaction();
            String message = String.format("Cannot update dashboard page for user ");
            log.error(message, e);
            throw new DashboardException(message, e);
        } finally {
            Ebean.endTransaction();
        }
    }

    @Override
    public void updateDashboardPageConfiguration(Long id, String uid, List<DashboardRowConfiguration> config) throws DashboardException {
        if(log.isDebugEnabled()){
            log.debug("Updating dashboard page "+id+" for user "+(uid==null?"current":uid));
        }
        IUserAccount userAccount = getUserAccount(uid);
        Ebean.beginTransaction();
        try {
            DashboardPage dashboardPage=DashboardPage.find.where().eq("id", id).findUnique();
            //Check the user is the right one
            if(dashboardPage==null || !userAccount.getUid().equals(dashboardPage.getPrincipal().uid)){
                if(log.isDebugEnabled()){
                    log.debug("No page found for id "+id+" and user "+userAccount.getUid());
                }
                return;
            }
            //Listing all the widgets in the current dashboard to identify some which must be deleted
            Set<Long> newWidgetIds=new HashSet<>();
            for(DashboardRowConfiguration newRow : config){
                newWidgetIds.addAll(newRow.getWidgetIds());
            }
            //Identify deleted widgets (check for changes)
            for(DashboardWidget dashboardWidget : dashboardPage.dashboardWidgets){
                if(!newWidgetIds.contains(dashboardWidget.id)){
                    if(log.isDebugEnabled()){
                        log.debug("The widget "+dashboardWidget.id+" hash been removed from the dashboard "+id);
                    }
                    //This is a widget which has been deleted (delete it from the database)
                    dashboardWidget.delete();
                }
                newWidgetIds.remove(dashboardWidget.id);
            }
            //Check if there are some unknown widget ids
            if(newWidgetIds.size()>0){
                throw new DashboardException("Unknown widgets in the dashboard update set "+newWidgetIds);
            }
            //Update the layout
            dashboardPage.layout=getMapper().writeValueAsBytes(config);
            dashboardPage.save();
            Ebean.commitTransaction();
        }catch (Exception e) {
            Ebean.rollbackTransaction();
            String message = String.format("Cannot update dashboard page ", id);
            log.error(message, e);
            throw new DashboardException(message, e);
        } finally {
            Ebean.endTransaction();
        }
    }

    @Override
    public void updateDashboardPageName(Long id, String uid, String name) throws DashboardException {
        if(log.isDebugEnabled()){
            log.debug("Updating dashboard page "+id+" for user "+(uid==null?"current":uid));
        }
        IUserAccount userAccount = getUserAccount(uid);
        Ebean.beginTransaction();
        try {
            DashboardPage dashboardPage=DashboardPage.find.where().eq("id", id).findUnique();
            //Check the user is the right one
            if(dashboardPage==null || !userAccount.getUid().equals(dashboardPage.getPrincipal().uid)){
                if(log.isDebugEnabled()){
                    log.debug("No page found for id "+id+" and user "+userAccount.getUid());
                }
                return;
            }
            dashboardPage.name=name;
            dashboardPage.save();
            Ebean.commitTransaction();
        }catch (Exception e) {
            Ebean.rollbackTransaction();
            String message = String.format("Cannot update dashboard page ", id);
            log.error(message, e);
            throw new DashboardException(message, e);
        } finally {
            Ebean.endTransaction();
        }
    }

    @Override
    public void setDashboardPageAsHome(Long id, String uid) throws DashboardException {
        if(log.isDebugEnabled()){
            log.debug("Updating dashboard page "+id+" for user "+(uid==null?"current":uid));
        }
        IUserAccount userAccount = getUserAccount(uid);
        Ebean.beginTransaction();
        try {            
            DashboardPage dashboardPage=DashboardPage.find.where().eq("id", id).findUnique();
            //Check the user is the right one
            if(dashboardPage==null || !userAccount.getUid().equals(dashboardPage.getPrincipal().uid)){
                if(log.isDebugEnabled()){
                    log.debug("No page found for id "+id+" and user "+userAccount.getUid());
                }
                return;
            }
            dashboardPage.isHome=true;
            dashboardPage.save();
            
            //Check if a home page is already defined and then change it
            DashboardPage existingHomePage=DashboardPage.find.where().eq("principal.id", userAccount.getMafUid()).eq("isHome", true).findUnique();
            if(existingHomePage!=null){
                existingHomePage.isHome=false;
                existingHomePage.save();
            }
            
            Ebean.commitTransaction();
        }catch (Exception e) {
            Ebean.rollbackTransaction();
            String message = String.format("Cannot update dashboard page ", id);
            log.error(message, e);
            throw new DashboardException(message, e);
        } finally {
            Ebean.endTransaction();
        }
    }

    @Override
    public void deleteDashboardPage(Long id, String uid) throws DashboardException {
        if(log.isDebugEnabled()){
            log.debug("Deleting dashboard page "+id+" for user "+(uid==null?"current":uid));
        }
        IUserAccount userAccount = getUserAccount(uid);
        Ebean.beginTransaction();
        try {
            DashboardPage dashboardPage=DashboardPage.find.where().eq("id", id).findUnique();
            //Check the user is the right one
            if(dashboardPage==null || !userAccount.getUid().equals(dashboardPage.getPrincipal().uid)){
                if(log.isDebugEnabled()){
                    log.debug("No page found for id "+id+" and user "+userAccount.getUid());
                }
                return;
            }
            dashboardPage.delete();
        }catch (Exception e) {
            Ebean.rollbackTransaction();
            String message = String.format("Cannot delete dashboard page ", id);
            log.error(message, e);
            throw new DashboardException(message, e);
        } finally {
            Ebean.endTransaction();
        }
    }

    private IUserAccount getUserAccount(String uid) throws DashboardException {
        IUserAccount userAccount=null;
            try{
                if(uid==null){
                    userAccount=getSecurityService().getCurrentUser();
                }else{
                    userAccount=getSecurityService().getUserFromUid(uid);
                }
            }catch(Exception e){
                throw new DashboardException("Unable to retreive the pages of the specified user",e);
            }
        return userAccount;
    }

    private IPluginManagerService getPluginManagerService() {
        return pluginManagerService;
    }

    private CacheApi getCacheApi() {
        return cacheApi;
    }

    private ISecurityService getSecurityService() {
        return securityService;
    }

    private ObjectMapper getMapper() {
        return mapper;
    }

}
