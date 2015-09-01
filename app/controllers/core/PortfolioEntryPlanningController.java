/*! LICENSE
 *
 * Copyright (c) 2015, The Agile Factory SA and/or its affiliates. All rights
 * reserved.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package controllers.core;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import be.objectify.deadbolt.java.actions.Dynamic;
import constants.IMafConstants;
import controllers.ControllersUtils;
import dao.delivery.IterationDAO;
import dao.finance.PortfolioEntryResourcePlanDAO;
import dao.governance.LifeCycleMilestoneDao;
import dao.governance.LifeCyclePlanningDao;
import dao.pmo.ActorDao;
import dao.pmo.OrgUnitDao;
import dao.pmo.PortfolioEntryDao;
import dao.pmo.PortfolioEntryPlanningPackageDao;
import dao.pmo.StakeholderDao;
import dao.timesheet.TimesheetDao;
import framework.security.ISecurityService;
import framework.services.ServiceStaticAccessor;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.services.storage.IAttachmentManagerPlugin;
import framework.utils.Color;
import framework.utils.CustomAttributeFormAndDisplayHandler;
import framework.utils.DefaultSelectableValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.FileAttachmentHelper;
import framework.utils.FilterConfig;
import framework.utils.ISelectableValueHolder;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.JqueryGantt;
import framework.utils.Msg;
import framework.utils.Pagination;
import framework.utils.Table;
import framework.utils.Utilities;
import models.delivery.Iteration;
import models.delivery.Release;
import models.delivery.ReleasePortfolioEntry;
import models.finance.PortfolioEntryResourcePlan;
import models.finance.PortfolioEntryResourcePlanAllocatedActor;
import models.finance.PortfolioEntryResourcePlanAllocatedCompetency;
import models.finance.PortfolioEntryResourcePlanAllocatedOrgUnit;
import models.finance.WorkOrder;
import models.framework_models.account.NotificationCategory;
import models.framework_models.account.NotificationCategory.Code;
import models.framework_models.common.Attachment;
import models.governance.LifeCycleInstance;
import models.governance.LifeCycleInstancePlanning;
import models.governance.LifeCyclePhase;
import models.governance.PlannedLifeCycleMilestoneInstance;
import models.pmo.Actor;
import models.pmo.Competency;
import models.pmo.OrgUnit;
import models.pmo.PortfolioEntry;
import models.pmo.PortfolioEntryPlanningPackage;
import models.pmo.PortfolioEntryPlanningPackageGroup;
import models.pmo.PortfolioEntryPlanningPackagePattern;
import models.pmo.Stakeholder;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.CheckPortfolioEntryExists;
import services.datasyndication.IDataSyndicationService;
import services.datasyndication.models.DataSyndicationAgreementItem;
import services.datasyndication.models.DataSyndicationAgreementLink;
import utils.SortableCollection;
import utils.SortableCollection.ComplexSortableObject;
import utils.SortableCollection.DateSortableObject;
import utils.SortableCollection.ISortableObject;
import utils.form.AttachmentFormData;
import utils.form.PortfolioEntryPlanningPackageFormData;
import utils.form.PortfolioEntryPlanningPackageGroupsFormData;
import utils.form.PortfolioEntryResourcePlanAllocatedActorFormData;
import utils.form.PortfolioEntryResourcePlanAllocatedCompetencyFormData;
import utils.form.PortfolioEntryResourcePlanAllocatedOrgUnitFormData;
import utils.gantt.SourceDataValue;
import utils.gantt.SourceItem;
import utils.gantt.SourceValue;
import utils.table.AttachmentListView;
import utils.table.PortfolioEntryPlanningPackageListView;
import utils.table.PortfolioEntryResourcePlanAllocatedActorListView;
import utils.table.PortfolioEntryResourcePlanAllocatedResourceListView;

/**
 * The controller which allows to manage the planning of a portfolio entry.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryPlanningController extends Controller {

    @Inject
    private IPreferenceManagerPlugin preferenceManagerPlugin;
    @Inject
    private II18nMessagesPlugin messagesPlugin;
    @Inject
    private IAttachmentManagerPlugin attachmentManagerPlugin;
    @Inject
    private IUserSessionManagerPlugin userSessionManagerPlugin;
    @Inject
    private ISecurityService securityService;

    @Inject
    private IDataSyndicationService dataSyndicationService;

    private static Logger.ALogger log = Logger.of(PortfolioEntryPlanningController.class);

    public static Form<OverviewConfiguration> overviewConfigurationFormTemplate = Form.form(OverviewConfiguration.class);
    public static Form<PortfolioEntryPlanningPackageFormData> planningPackageFormTemplate = Form.form(PortfolioEntryPlanningPackageFormData.class);
    public static Form<PortfolioEntryPlanningPackageGroupsFormData> planningPackageGroupsFormTemplate = Form
            .form(PortfolioEntryPlanningPackageGroupsFormData.class);
    public static Form<PortfolioEntryResourcePlanAllocatedActorFormData> allocatedActorFormTemplate = Form
            .form(PortfolioEntryResourcePlanAllocatedActorFormData.class, PortfolioEntryResourcePlanAllocatedActorFormData.DefaultGroup.class);
    public static Form<PortfolioEntryResourcePlanAllocatedOrgUnitFormData> allocatedOrgUnitFormTemplate = Form
            .form(PortfolioEntryResourcePlanAllocatedOrgUnitFormData.class);
    public static Form<PortfolioEntryResourcePlanAllocatedCompetencyFormData> allocatedCompetencyFormTemplate = Form
            .form(PortfolioEntryResourcePlanAllocatedCompetencyFormData.class);
    public static Form<PortfolioEntryResourcePlanAllocatedActorFormData> reallocateOrgUnitFormTemplate = Form
            .form(PortfolioEntryResourcePlanAllocatedActorFormData.class, PortfolioEntryResourcePlanAllocatedActorFormData.ReallocateGroup.class);
    public static Form<PortfolioEntryResourcePlanAllocatedActorFormData> reallocateCompetencyFormTemplate = Form
            .form(PortfolioEntryResourcePlanAllocatedActorFormData.class, PortfolioEntryResourcePlanAllocatedActorFormData.ReallocateGroup.class);
    private static Form<AttachmentFormData> attachmentFormTemplate = Form.form(AttachmentFormData.class);

    /**
     * Display global gantt chart of the initiative.
     * 
     * Life cycle phases of the process (with is_roadmap_phase = false)<br/>
     * Life cycle milestones (diamond)<br/>
     * Planning packages<br/>
     * Releases<br/>
     * Iterations<br/>
     * 
     * Configuration:<br/>
     * -Display phases<br/>
     * -Display milestones<br/>
     * -Display packages<br/>
     * -Display releases<br/>
     * -Display iterations<br/>
     * 
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION)
    public Result overview(Long id) {

        // load the overview configuration from the user preference
        OverviewConfiguration conf = OverviewConfiguration.load();

        // prepare the overview configuration form
        Form<OverviewConfiguration> overviewConfigurationForm = overviewConfigurationFormTemplate.fill(conf);

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the active life cycle process instance
        LifeCycleInstance processInstance = portfolioEntry.activeLifeCycleInstance;

        // get the last planned milestone instances
        List<PlannedLifeCycleMilestoneInstance> lastPlannedMilestoneInstances = LifeCyclePlanningDao
                .getPlannedLCMilestoneInstanceLastAsListByPE(portfolioEntry.id);

        // initiate the sortable collection
        SortableCollection<ComplexSortableObject> sortableCollection = new SortableCollection<>();

        /** Life cycle phases of the process (with is_roadmap_phase = false) **/

        if (conf.phases) {

            // get the all phases of a process
            List<LifeCyclePhase> lifeCyclePhases = LifeCycleMilestoneDao.getLCPhaseAsListByLCProcess(processInstance.lifeCycleProcess.id);

            if (lifeCyclePhases != null && !lifeCyclePhases.isEmpty() && lastPlannedMilestoneInstances != null && lastPlannedMilestoneInstances.size() > 0) {

                // get the CSS class
                String cssClass = "success";

                // create the source data value
                SourceDataValue dataValue = new SourceDataValue(controllers.core.routes.PortfolioEntryGovernanceController.index(portfolioEntry.id).url(),
                        null, null, null, null);

                // transform the list of last planned milestone instances to a
                // map
                Map<Long, PlannedLifeCycleMilestoneInstance> lastPlannedMilestoneInstancesAsMap = new HashMap<Long, PlannedLifeCycleMilestoneInstance>();
                for (PlannedLifeCycleMilestoneInstance plannedMilestoneInstance : lastPlannedMilestoneInstances) {
                    lastPlannedMilestoneInstancesAsMap.put(plannedMilestoneInstance.lifeCycleMilestone.id, plannedMilestoneInstance);
                }

                for (LifeCyclePhase phase : lifeCyclePhases) {

                    if (lastPlannedMilestoneInstancesAsMap.containsKey(phase.startLifeCycleMilestone.id)
                            && lastPlannedMilestoneInstancesAsMap.containsKey(phase.endLifeCycleMilestone.id)) {

                        // get the from date
                        Date from = LifeCyclePlanningDao
                                .getPlannedLCMilestoneInstanceAsPassedDate(lastPlannedMilestoneInstancesAsMap.get(phase.startLifeCycleMilestone.id).id);

                        // get the to date
                        Date to = LifeCyclePlanningDao
                                .getPlannedLCMilestoneInstanceAsPassedDate(lastPlannedMilestoneInstancesAsMap.get(phase.endLifeCycleMilestone.id).id);

                        if (from != null && to != null) {

                            to = JqueryGantt.cleanToDate(from, to);

                            // add gap for the from date
                            if (phase.gapDaysStart != null && phase.gapDaysStart.intValue() > 0) {
                                Calendar c = Calendar.getInstance();
                                c.setTime(from);
                                c.add(Calendar.DATE, phase.gapDaysStart);
                                from = c.getTime();
                            }

                            // remove gap for the to date
                            if (phase.gapDaysEnd != null && phase.gapDaysEnd.intValue() > 0) {
                                Calendar c = Calendar.getInstance();
                                c.setTime(to);
                                c.add(Calendar.DATE, -1 * phase.gapDaysEnd);
                                to = c.getTime();
                            }

                            SourceItem item = new SourceItem(phase.getName(), null);

                            item.values.add(new SourceValue(from, to, "", phase.getName(), cssClass, dataValue));

                            sortableCollection.addObject(new ComplexSortableObject(from, 1, phase.order != null ? phase.order : 0, item));

                        }

                    }

                }

            }

        }

        /** Life cycle milestones (diamond) **/

        if (conf.milestones && lastPlannedMilestoneInstances != null && lastPlannedMilestoneInstances.size() > 0) {

            // get the CSS class
            String cssClass = "diamond diamond-danger";

            for (PlannedLifeCycleMilestoneInstance plannedMilestoneInstance : lastPlannedMilestoneInstances) {

                // create the source data value
                SourceDataValue dataValue = new SourceDataValue(controllers.core.routes.PortfolioEntryGovernanceController
                        .viewMilestone(portfolioEntry.id, plannedMilestoneInstance.lifeCycleMilestone.id).url(), null, null, null, null);

                // get the from date
                Date from = LifeCyclePlanningDao.getPlannedLCMilestoneInstanceAsPassedDate(plannedMilestoneInstance.id);

                // get the to date
                Date to = from;

                if (from != null) {

                    SourceItem item = new SourceItem(plannedMilestoneInstance.lifeCycleMilestone.getShortName(), null);

                    item.values.add(new SourceValue(from, to, "", "", cssClass, dataValue));

                    sortableCollection.addObject(new ComplexSortableObject(from, 2, 0, item));

                }

            }

        }

        /** Planning packages **/

        if (conf.packages) {

            for (PortfolioEntryPlanningPackage planningPackage : PortfolioEntryPlanningPackageDao.getPEPlanningPackageAsListByPE(portfolioEntry.id)) {

                // create the source data value
                SourceDataValue dataValue = new SourceDataValue(
                        controllers.core.routes.PortfolioEntryPlanningController.viewPackage(portfolioEntry.id, planningPackage.id).url(), null, null, null,
                        null);

                // get the from date
                Date from = planningPackage.startDate;

                // get the to date
                Date to = planningPackage.endDate;

                if (to != null) {

                    String cssClass = null;

                    if (from != null) {

                        to = JqueryGantt.cleanToDate(from, to);
                        cssClass = "warning";

                    } else {

                        from = to;
                        cssClass = "diamond diamond-warning";

                    }

                    SourceItem item = new SourceItem(planningPackage.name, null);

                    item.values.add(new SourceValue(from, to, "", planningPackage.name, cssClass, dataValue));

                    sortableCollection.addObject(new ComplexSortableObject(from, 5, 0, item));

                }

            }

        }

        /** Releases **/

        if (conf.releases) {

            for (ReleasePortfolioEntry releasePortfolioEntry : portfolioEntry.releasesPortfolioEntries) {

                Release release = releasePortfolioEntry.getRelease();

                if (release != null) {

                    SourceDataValue dataValue = new SourceDataValue(controllers.core.routes.ReleaseController.view(release.id, 0).url(), null, null, null,
                            null);

                    // get the dates
                    Date cutOffDate = release.cutOffDate;
                    Date endTestsDate = release.endTestsDate;
                    Date deploymentDate = release.deploymentDate;

                    SourceItem item = new SourceItem(release.getName(), null);

                    Date from = null;

                    if (cutOffDate == null && endTestsDate == null) {

                        item.values.add(new SourceValue(deploymentDate, deploymentDate, "", "", "diamond diamond-info", dataValue));
                        from = deploymentDate;

                    } else if (cutOffDate != null && endTestsDate == null) {

                        item.values.add(
                                new SourceValue(cutOffDate, JqueryGantt.cleanToDate(cutOffDate, deploymentDate), "", release.getName(), "info", dataValue));
                        from = cutOffDate;

                    } else if (cutOffDate == null && endTestsDate != null) {

                        item.values.add(new SourceValue(endTestsDate, JqueryGantt.cleanToDate(endTestsDate, deploymentDate), "", release.getName(), "info",
                                dataValue));
                        from = endTestsDate;

                    } else {

                        item.values.add(
                                new SourceValue(cutOffDate, JqueryGantt.cleanToDate(cutOffDate, deploymentDate), "", release.getName(), "info", dataValue));
                        from = cutOffDate;

                    }

                    sortableCollection.addObject(new ComplexSortableObject(from, 3, 0, item));

                }
            }

        }

        /** Iterations **/

        if (conf.iterations) {

            for (Iteration iteration : IterationDAO.getIterationAllAsListByPE(portfolioEntry.id)) {

                SourceDataValue dataValue = new SourceDataValue(
                        controllers.core.routes.PortfolioEntryDeliveryController.viewIteration(portfolioEntry.id, iteration.id).url(), null, null, null,
                        null);

                // get the from date
                Date from = iteration.startDate;

                // get the to date
                Date to = iteration.endDate;

                if (to != null) {

                    String cssClass = null;

                    if (from != null) {

                        to = JqueryGantt.cleanToDate(from, to);
                        cssClass = "primary";

                    } else {

                        from = to;
                        cssClass = "diamond diamond-primary";

                    }

                    SourceItem item = new SourceItem(iteration.name, null);

                    item.values.add(new SourceValue(from, to, "", iteration.name, cssClass, dataValue));

                    sortableCollection.addObject(new ComplexSortableObject(from, 4, 0, item));

                }

            }

        }

        /** construct the gantt **/

        List<SourceItem> items = new ArrayList<SourceItem>();
        for (ISortableObject sortableObject : sortableCollection.getSorted()) {
            Logger.debug(sortableObject.getSortableAttributesAsString());
            SourceItem item = (SourceItem) sortableObject.getObject();
            items.add(item);
        }

        String ganttSource = "";
        try {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            ganttSource = ow.writeValueAsString(items);
        } catch (JsonProcessingException e) {
            Logger.error(e.getMessage());
        }

        return ok(views.html.core.portfolioentryplanning.overview.render(portfolioEntry, ganttSource, overviewConfigurationForm));

    }

    /**
     * Change the configuration of the overview planning.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION)
    public Result overviewChangeConf(Long id) {

        Form<OverviewConfiguration> boundForm = overviewConfigurationFormTemplate.bindFromRequest();

        OverviewConfiguration conf = boundForm.get();

        OverviewConfiguration.store(conf);

        return redirect(controllers.core.routes.PortfolioEntryPlanningController.overview(id));
    }

    /**
     * Display the planning packages of a portfolio entry with a gantt view.
     * 
     * @param id
     *            the portfolio entry id
     * @param reset
     *            define if the filter should be reseted
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION)
    public Result packages(Long id, Boolean reset) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        try {

            FilterConfig<PortfolioEntryPlanningPackageListView> filterConfig = null;

            /*
             * we try to get the last filter configuration of the sign-in user,
             * if it exists we use it to filter the requirements (except if the
             * reset flag is to true)
             */
            String backedUpFilter = getFilterConfigurationFromPreferences(IMafConstants.PACKAGES_FILTER_STORAGE_PREFERENCE);
            if (!reset && !StringUtils.isBlank(backedUpFilter)) {

                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(backedUpFilter);
                filterConfig = PortfolioEntryPlanningPackageListView.filterConfig.parseResponse(json);

            } else {

                // get a copy of the default filter config
                filterConfig = PortfolioEntryPlanningPackageListView.filterConfig;
                storeFilterConfigFromPreferences(filterConfig.marshall(), IMafConstants.PACKAGES_FILTER_STORAGE_PREFERENCE);

            }

            // get the table
            Pair<Table<PortfolioEntryPlanningPackageListView>, Pagination<PortfolioEntryPlanningPackage>> t = getPackagesTable(id, filterConfig,
                    getMessagesPlugin());

            // get the available groups
            List<PortfolioEntryPlanningPackageGroup> groups = PortfolioEntryPlanningPackageDao.getPEPlanningPackageGroupActiveNonEmptyAsList();

            // get the syndicated reports
            List<DataSyndicationAgreementLink> agreementLinks = new ArrayList<>();
            DataSyndicationAgreementItem agreementItem = null;
            if (portfolioEntry.isSyndicated && getDataSyndicationService().isActive()) {
                try {
                    agreementItem = getDataSyndicationService().getAgreementItemByDataTypeAndDescriptor(PortfolioEntry.class.getName(), "PLANNING_PACKAGE");
                    if (agreementItem != null) {
                        agreementLinks = getDataSyndicationService().getAgreementLinksOfItemAndSlaveObject(agreementItem, PortfolioEntry.class.getName(), id);
                    }
                } catch (Exception e) {
                    Logger.error("impossible to get the syndicated planning package data", e);
                }
            }

            // construct the gantt

            ExpressionList<PortfolioEntryPlanningPackage> expressionList = filterConfig
                    .updateWithSearchExpression(PortfolioEntryPlanningPackageDao.getPEPlanningPackageAsExprByPE(id));
            filterConfig.updateWithSortExpression(expressionList);

            // initiate the sortable collection
            SortableCollection<DateSortableObject> sortableCollection = new SortableCollection<>();

            for (PortfolioEntryPlanningPackage planningPackage : expressionList.findList()) {

                // get the from date
                Date from = planningPackage.startDate;

                // get the to date
                Date to = planningPackage.endDate;

                if (to != null) {

                    SourceItem item = new SourceItem(planningPackage.name, "");

                    String cssClass = null;

                    if (from != null) {

                        to = JqueryGantt.cleanToDate(from, to);
                        cssClass = planningPackage.cssClass;

                    } else {

                        from = to;
                        cssClass = "diamond diamond-" + planningPackage.cssClass;

                    }

                    item.values.add(new SourceValue(from, to, "", "", cssClass, null));

                    sortableCollection.addObject(new DateSortableObject(from, item));

                }

            }

            List<SourceItem> items = new ArrayList<SourceItem>();
            for (ISortableObject sortableObject : sortableCollection.getSorted()) {
                SourceItem item = (SourceItem) sortableObject.getObject();
                items.add(item);
            }

            String ganttSource = "";
            try {
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                ganttSource = ow.writeValueAsString(items);
            } catch (JsonProcessingException e) {
                Logger.error(e.getMessage());
            }

            return ok(views.html.core.portfolioentryplanning.packages.render(portfolioEntry, t.getLeft(), t.getRight(), filterConfig, groups, ganttSource,
                    agreementLinks, agreementItem));

        } catch (Exception e) {

            if (reset.equals(false)) {
                ControllersUtils.logAndReturnUnexpectedError(e, log);
                return redirect(controllers.core.routes.PortfolioEntryPlanningController.packages(id, true));
            } else {
                return ControllersUtils.logAndReturnUnexpectedError(e, log);
            }

        }

    }

    /**
     * Filter the packages.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION)
    public Result packagesFilter(Long id) {

        try {

            // get the json
            JsonNode json = request().body().asJson();

            // store the filter config
            storeFilterConfigFromPreferences(json.toString(), IMafConstants.PACKAGES_FILTER_STORAGE_PREFERENCE);

            // fill the filter config
            FilterConfig<PortfolioEntryPlanningPackageListView> filterConfig = PortfolioEntryPlanningPackageListView.filterConfig.parseResponse(json);

            // get the table
            Pair<Table<PortfolioEntryPlanningPackageListView>, Pagination<PortfolioEntryPlanningPackage>> t = getPackagesTable(id, filterConfig,
                    getMessagesPlugin());

            return ok(views.html.framework_views.parts.table.dynamic_tableview.render(t.getLeft(), t.getRight()));

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }

    }

    /**
     * Get the packages table for a portfolio entry and a filter config.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param filterConfig
     *            the filter config.
     * @param messagesPlugin
     *            the message service
     */
    private Pair<Table<PortfolioEntryPlanningPackageListView>, Pagination<PortfolioEntryPlanningPackage>> getPackagesTable(Long portfolioEntryId,
            FilterConfig<PortfolioEntryPlanningPackageListView> filterConfig, II18nMessagesPlugin messagesPlugin) {

        ExpressionList<PortfolioEntryPlanningPackage> expressionList = filterConfig
                .updateWithSearchExpression(PortfolioEntryPlanningPackageDao.getPEPlanningPackageAsExprByPE(portfolioEntryId));
        filterConfig.updateWithSortExpression(expressionList);

        Pagination<PortfolioEntryPlanningPackage> pagination = new Pagination<PortfolioEntryPlanningPackage>(expressionList);
        pagination.setCurrentPage(filterConfig.getCurrentPage());

        List<PortfolioEntryPlanningPackageListView> portfolioEntryPlanningPackageListView = new ArrayList<PortfolioEntryPlanningPackageListView>();
        for (PortfolioEntryPlanningPackage portfolioEntryPlanningPackage : pagination.getListOfObjects()) {
            portfolioEntryPlanningPackageListView.add(new PortfolioEntryPlanningPackageListView(portfolioEntryPlanningPackage, messagesPlugin));
        }

        Set<String> hideColumnsForPackage = filterConfig.getColumnsToHide();
        if (!getSecurityService().dynamic("PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION", "")) {
            hideColumnsForPackage.add("editActionLink");
            hideColumnsForPackage.add("deleteActionLink");
        }

        Table<PortfolioEntryPlanningPackageListView> table = PortfolioEntryPlanningPackageListView.templateTable
                .fillForFilterConfig(portfolioEntryPlanningPackageListView, hideColumnsForPackage);

        return Pair.of(table, pagination);

    }

    /**
     * Display the details of a planning package.
     * 
     * @param id
     *            the portfolio entry id
     * @param planningPackageId
     *            the planning package id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION)
    public Result viewPackage(Long id, Long planningPackageId) {

        // get the portfolio entry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the planning package
        PortfolioEntryPlanningPackage planningPackage = PortfolioEntryPlanningPackageDao.getPEPlanningPackageById(planningPackageId);

        // security: the portfolioEntry must be related to the object
        if (!planningPackage.portfolioEntry.id.equals(id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        // get the allocated resources' days
        BigDecimal allocatedResourcesDays = PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorAsDaysByPlanningPackage(planningPackage)
                .add(PortfolioEntryResourcePlanDAO.getPEResourcePlanAllocatedOrgUnitAsDaysByPlanningPackage(planningPackage))
                .add(PortfolioEntryResourcePlanDAO.getPEResourcePlanAllocatedCompetencyAsDaysByPlanningPackage(planningPackage));

        // get the timesheets' days
        BigDecimal timesheetsDays = TimesheetDao.getTimesheetLogAsTotalHoursByPEPlanningPackage(planningPackage)
                .divide(TimesheetDao.getTimesheetReportHoursPerDay(), BigDecimal.ROUND_HALF_UP);

        /*
         * Get the attachments
         */

        // authorize the attachments
        FileAttachmentHelper.getFileAttachmentsForDisplay(PortfolioEntryPlanningPackage.class, planningPackageId, getAttachmentManagerPlugin(),
                getUserSessionManagerPlugin());

        // create the table
        List<Attachment> attachments = Attachment.getAttachmentsFromObjectTypeAndObjectId(PortfolioEntryPlanningPackage.class, planningPackageId);

        List<AttachmentListView> attachmentsListView = new ArrayList<AttachmentListView>();
        for (Attachment attachment : attachments) {
            attachmentsListView.add(new AttachmentListView(attachment,
                    controllers.core.routes.PortfolioEntryPlanningController.deletePackageAttachment(id, planningPackageId, attachment.id).url()));
        }

        Set<String> hideColumns = new HashSet<String>();
        if (!getSecurityService().dynamic("PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION", "")) {
            hideColumns.add("removeActionLink");
        }

        Table<AttachmentListView> attachmentsTable = AttachmentListView.templateTable.fill(attachmentsListView, hideColumns);

        return ok(views.html.core.portfolioentryplanning.package_view.render(portfolioEntry, planningPackage, allocatedResourcesDays, timesheetsDays,
                attachmentsTable));
    }

    /**
     * Create or edit a planning package.
     * 
     * @param id
     *            the portfolio entry id
     * @param planningPackageId
     *            the planning package id (0 for create case)
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result managePackage(Long id, Long planningPackageId) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // initiate the form with the template
        Form<PortfolioEntryPlanningPackageFormData> planningPackageForm = planningPackageFormTemplate;

        // edit case: inject values
        if (!planningPackageId.equals(Long.valueOf(0))) {

            PortfolioEntryPlanningPackage planningPackage = PortfolioEntryPlanningPackageDao.getPEPlanningPackageById(planningPackageId);

            // security: the portfolioEntry must be related to the object
            if (!planningPackage.portfolioEntry.id.equals(id)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            planningPackageForm = planningPackageFormTemplate.fill(new PortfolioEntryPlanningPackageFormData(planningPackage));

            // add the custom attributes values
            CustomAttributeFormAndDisplayHandler.fillWithValues(planningPackageForm, PortfolioEntryPlanningPackage.class, planningPackageId);
        } else {
            planningPackageForm = planningPackageFormTemplate.fill(new PortfolioEntryPlanningPackageFormData());

            // add the custom attributes default values
            CustomAttributeFormAndDisplayHandler.fillWithValues(planningPackageForm, PortfolioEntryPlanningPackage.class, null);
        }

        return ok(views.html.core.portfolioentryplanning.package_manage.render(portfolioEntry, Color.getColorsAsValueHolderCollection(getMessagesPlugin()),
                planningPackageForm, PortfolioEntryPlanningPackageDao.getPEPlanningPackageGroupActiveAsVH(), getPackageStatusAsValueHolderCollection()));
    }

    /**
     * Save a planning package.
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processManagePackage() {

        // bind the form
        Form<PortfolioEntryPlanningPackageFormData> boundForm = planningPackageFormTemplate.bindFromRequest();

        // get the portfolioEntry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        if (boundForm.hasErrors() || CustomAttributeFormAndDisplayHandler.validateValues(boundForm, PortfolioEntryPlanningPackage.class)) {
            return ok(
                    views.html.core.portfolioentryplanning.package_manage.render(portfolioEntry, Color.getColorsAsValueHolderCollection(getMessagesPlugin()),
                            boundForm, PortfolioEntryPlanningPackageDao.getPEPlanningPackageGroupActiveAsVH(), getPackageStatusAsValueHolderCollection()));
        }

        PortfolioEntryPlanningPackageFormData planningPackageFormData = boundForm.get();

        // check the dates
        if (!planningPackageFormData.startDate.equals("") && planningPackageFormData.endDate.equals("")) {
            // the start date cannot be filled alone
            boundForm.reject("startDate", Msg.get("object.portfolio_entry_planning_package.start_date.invalid"));
            return ok(
                    views.html.core.portfolioentryplanning.package_manage.render(portfolioEntry, Color.getColorsAsValueHolderCollection(getMessagesPlugin()),
                            boundForm, PortfolioEntryPlanningPackageDao.getPEPlanningPackageGroupActiveAsVH(), getPackageStatusAsValueHolderCollection()));
        }
        if (!planningPackageFormData.startDate.equals("") && !planningPackageFormData.endDate.equals("")) {
            // the end date should be after the start date
            try {
                if (Utilities.getDateFormat(null).parse(planningPackageFormData.startDate)
                        .after(Utilities.getDateFormat(null).parse(planningPackageFormData.endDate))) {
                    boundForm.reject("endDate", Msg.get("object.portfolio_entry_planning_package.end_date.invalid"));
                    return ok(views.html.core.portfolioentryplanning.package_manage.render(portfolioEntry,
                            Color.getColorsAsValueHolderCollection(getMessagesPlugin()), boundForm,
                            PortfolioEntryPlanningPackageDao.getPEPlanningPackageGroupActiveAsVH(), getPackageStatusAsValueHolderCollection()));
                }
            } catch (ParseException e) {
                return ControllersUtils.logAndReturnUnexpectedError(e, log);
            }
        }

        PortfolioEntryPlanningPackage planningPackage = null;

        if (planningPackageFormData.planningPackageId == null) { // create case

            planningPackage = new PortfolioEntryPlanningPackage();

            planningPackage.portfolioEntry = portfolioEntry;

            planningPackageFormData.fill(planningPackage);

            planningPackage.save();

            // if exists, add the document
            if (FileAttachmentHelper.hasFileField("document")) {
                Logger.debug("has document");
                try {
                    FileAttachmentHelper.saveAsAttachement("document", PortfolioEntryPlanningPackage.class, planningPackage.id, getAttachmentManagerPlugin());
                } catch (IOException e) {
                    Logger.error("impossible to add the document for the created package '" + planningPackage.id + "'. The package still created.", e);
                }
            }

            Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_planning.package.add.successful"));

        } else { // edit case

            planningPackage = PortfolioEntryPlanningPackageDao.getPEPlanningPackageById(planningPackageFormData.planningPackageId);

            Date oldStartDate = planningPackage.startDate;
            Date oldEndDate = planningPackage.endDate;

            // security: the portfolioEntry must be related to the object
            if (!planningPackage.portfolioEntry.id.equals(id)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            planningPackageFormData.fill(planningPackage);

            planningPackage.update();

            Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_planning.package.edit.successful"));

            // if the dates have been changed, then we send a notification to
            // the manager of each confirmed and "follow package dates"
            // allocated resource
            if ((oldStartDate == null && planningPackage.startDate != null) || (oldStartDate != null && !oldStartDate.equals(planningPackage.startDate))
                    || (oldEndDate == null && planningPackage.endDate != null) || (oldEndDate != null && !oldEndDate.equals(planningPackage.endDate))) {
                for (PortfolioEntryResourcePlanAllocatedActor allocatedActor : planningPackage.portfolioEntryResourcePlanAllocatedActors) {
                    if (allocatedActor.isConfirmed && allocatedActor.followPackageDates != null && allocatedActor.followPackageDates) {
                        ActorDao.sendNotification(allocatedActor.actor.manager, NotificationCategory.getByCode(Code.PORTFOLIO_ENTRY),
                                controllers.core.routes.ActorController.allocation(allocatedActor.actor.id).url(),
                                "core.portfolio_entry_planning.allocated_actor.edit_confirmed.notification.title",
                                "core.portfolio_entry_planning.allocated_actor.edit_confirmed.notification.message",
                                allocatedActor.actor.getNameHumanReadable());
                    }
                }
            }
        }

        // update the dates of the allocated resources that have the flag
        // "follow package dates" to true.
        for (PortfolioEntryResourcePlanAllocatedActor a : planningPackage.portfolioEntryResourcePlanAllocatedActors) {
            if (a.followPackageDates != null && a.followPackageDates) {
                a.startDate = planningPackage.startDate;
                a.endDate = planningPackage.endDate;
                a.save();
            }
        }
        for (PortfolioEntryResourcePlanAllocatedOrgUnit oo : planningPackage.portfolioEntryResourcePlanAllocatedOrgUnits) {
            if (oo.followPackageDates != null && oo.followPackageDates) {
                oo.startDate = planningPackage.startDate;
                oo.endDate = planningPackage.endDate;
                oo.save();
            }
        }
        for (PortfolioEntryResourcePlanAllocatedCompetency c : planningPackage.portfolioEntryResourcePlanAllocatedCompetencies) {
            if (c.followPackageDates != null && c.followPackageDates) {
                c.startDate = planningPackage.startDate;
                c.endDate = planningPackage.endDate;
                c.save();
            }
        }
        for (WorkOrder o : planningPackage.workOrders) {
            if (o.followPackageDates != null && o.followPackageDates) {
                o.startDate = planningPackage.startDate;
                o.dueDate = planningPackage.endDate;
                o.save();
            }
        }

        // save the custom attributes
        CustomAttributeFormAndDisplayHandler.validateAndSaveValues(boundForm, PortfolioEntryPlanningPackage.class, planningPackage.id);

        return redirect(controllers.core.routes.PortfolioEntryPlanningController.packages(id, false));

    }

    /**
     * Display the form to add one or many package groups.
     * 
     * The available package groups are the ones related to the portfolio entry
     * type of the portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     * @return
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result addPackageGroups(Long id) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        List<PortfolioEntryPlanningPackageGroup> groups = new ArrayList<>();
        for (ISelectableValueHolder<Long> value : PortfolioEntryPlanningPackageDao.getPEPlanningPackageGroupActiveNonEmptyAsVH().getSortedValues()) {
            groups.add((PortfolioEntryPlanningPackageGroup) value);
        }

        return ok(views.html.core.portfolioentryplanning.package_add_groups.render(portfolioEntry, groups, planningPackageGroupsFormTemplate));
    }

    /**
     * Process the package groups to add, meaning create the planning package
     * according to the patterns.
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processAddPackageGroups() {

        // bind the form
        Form<PortfolioEntryPlanningPackageGroupsFormData> boundForm = planningPackageGroupsFormTemplate.bindFromRequest();

        // get the portfolioEntry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the available groups
        List<PortfolioEntryPlanningPackageGroup> groups = PortfolioEntryPlanningPackageDao.getPEPlanningPackageGroupActiveNonEmptyAsList();

        if (boundForm.hasErrors()) {
            return ok(views.html.core.portfolioentryplanning.package_add_groups.render(portfolioEntry, groups, boundForm));
        }

        PortfolioEntryPlanningPackageGroupsFormData groupsFormData = boundForm.get();

        List<Long> groupsId = new ArrayList<Long>();
        for (Long groupId : groupsFormData.groups) {
            if (groupId != null) {
                groupsId.add(groupId);
            }
        }

        if (groupsId.size() == 0) {
            boundForm.reject("groups", Msg.get("core.portfolio_entry_planning.package.groups.selectagroup"));
            return ok(views.html.core.portfolioentryplanning.package_add_groups.render(portfolioEntry, groups, boundForm));
        }

        for (Long groupId : groupsId) {
            PortfolioEntryPlanningPackageGroup group = PortfolioEntryPlanningPackageDao.getPEPlanningPackageGroupById(groupId);
            for (PortfolioEntryPlanningPackagePattern pattern : group.portfolioEntryPlanningPackagePatterns) {
                PortfolioEntryPlanningPackage planningPackage = new PortfolioEntryPlanningPackage(portfolioEntry, pattern);
                planningPackage.save();
            }
        }

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_planning.package.groups.successful"));

        return redirect(controllers.core.routes.PortfolioEntryPlanningController.packages(portfolioEntry.id, false));
    }

    /**
     * Delete a planning package.
     * 
     * @param id
     *            the portfolio entry id
     * @param planningPackageId
     *            the planning package id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result deletePackage(Long id, Long planningPackageId) {

        // get the planning package
        PortfolioEntryPlanningPackage planningPackage = PortfolioEntryPlanningPackageDao.getPEPlanningPackageById(planningPackageId);

        // security: the portfolioEntry must be related to the object
        if (!planningPackage.portfolioEntry.id.equals(id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        // set the delete flag to true
        planningPackage.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_planning.package.delete.successful"));

        return redirect(controllers.core.routes.PortfolioEntryPlanningController.packages(id, false));

    }

    /**
     * Form to add an attachment to a planning package.
     * 
     * @param id
     *            the portfolio entry id
     * @param planningPackageId
     *            the planning package id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result createPackageAttachment(Long id, Long planningPackageId) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the package
        PortfolioEntryPlanningPackage planningPackage = PortfolioEntryPlanningPackageDao.getPEPlanningPackageById(planningPackageId);

        // construct the form
        Form<AttachmentFormData> attachmentForm = attachmentFormTemplate.fill(new AttachmentFormData());

        return ok(views.html.core.portfolioentryplanning.package_attachment_create.render(portfolioEntry, planningPackage, attachmentForm));

    }

    /**
     * Process the form to add an attachment to a planning package.
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processCreatePackageAttachment() {

        Form<AttachmentFormData> boundForm = attachmentFormTemplate.bindFromRequest();

        // get the portfolioEntry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the package
        Long planningPackageId = Long.valueOf(boundForm.data().get("objectId"));
        PortfolioEntryPlanningPackage planningPackage = PortfolioEntryPlanningPackageDao.getPEPlanningPackageById(planningPackageId);

        if (boundForm.hasErrors()) {
            return ok(views.html.core.portfolioentryplanning.package_attachment_create.render(portfolioEntry, planningPackage, boundForm));
        }

        // store the document
        try {
            FileAttachmentHelper.saveAsAttachement("document", PortfolioEntryPlanningPackage.class, planningPackage.id, getAttachmentManagerPlugin());
        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log);
        }

        // success message
        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry.attachment.new.successful"));

        return redirect(controllers.core.routes.PortfolioEntryPlanningController.viewPackage(portfolioEntry.id, planningPackage.id));
    }

    /**
     * Delete an attachment of a planning package.
     * 
     * @param id
     *            the portfolio entry id
     * @param planningPackageId
     *            the planning package id
     * @param attachmentId
     *            the attachment id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result deletePackageAttachment(Long id, Long planningPackageId, Long attachmentId) {

        // get the package
        PortfolioEntryPlanningPackage planningPackage = PortfolioEntryPlanningPackageDao.getPEPlanningPackageById(planningPackageId);

        // get the attachment
        Attachment attachment = Attachment.getAttachmentFromId(attachmentId);

        // security: the planning package must be related to the attachment, and
        // the portfolio entry to the planning package
        if (!attachment.objectId.equals(planningPackageId) || !planningPackage.portfolioEntry.id.equals(id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        // delete the attachment
        FileAttachmentHelper.deleteFileAttachment(attachmentId, getAttachmentManagerPlugin(), getUserSessionManagerPlugin());

        attachment.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry.attachment.delete"));

        return redirect(controllers.core.routes.PortfolioEntryPlanningController.viewPackage(id, planningPackageId));
    }

    /**
     * Display the resources of a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION)
    public Result resources(Long id) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get current planning
        LifeCycleInstancePlanning planning = portfolioEntry.activeLifeCycleInstance.getCurrentLifeCycleInstancePlanning();

        /*
         * create the resource plan allocated actors/org unit tables
         */

        // hide columns for resource plan tables
        Set<String> hideColumnsForResourcePlanTable = new HashSet<String>();
        hideColumnsForResourcePlanTable.add("portfolioEntryName");
        hideColumnsForResourcePlanTable.add("followPackageDates");
        if (!getSecurityService().dynamic("PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION", "")) {
            hideColumnsForResourcePlanTable.add("editActionLink");
            hideColumnsForResourcePlanTable.add("removeActionLink");
            hideColumnsForResourcePlanTable.add("reallocate");
        }

        // get or create the resource plan
        PortfolioEntryResourcePlan resourcePlan = planning.portfolioEntryResourcePlan;
        if (resourcePlan == null) {
            Logger.debug("a new resource plan has been automatically created for the portfolio entry " + portfolioEntry.getName());
            resourcePlan = new PortfolioEntryResourcePlan();
            planning.portfolioEntryResourcePlan = resourcePlan;
            planning.save();
        }

        // construct the allocated actors table
        List<PortfolioEntryResourcePlanAllocatedActor> allocatedActors = resourcePlan.portfolioEntryResourcePlanAllocatedActors;
        List<PortfolioEntryResourcePlanAllocatedActorListView> portfolioEntryResourcePlanAllocatedActorListView;
        portfolioEntryResourcePlanAllocatedActorListView = new ArrayList<PortfolioEntryResourcePlanAllocatedActorListView>();
        for (PortfolioEntryResourcePlanAllocatedActor allocatedActor : allocatedActors) {
            portfolioEntryResourcePlanAllocatedActorListView.add(new PortfolioEntryResourcePlanAllocatedActorListView(allocatedActor));
        }
        Table<PortfolioEntryResourcePlanAllocatedActorListView> allocatedActorsTable = PortfolioEntryResourcePlanAllocatedActorListView.templateTable
                .fill(portfolioEntryResourcePlanAllocatedActorListView, hideColumnsForResourcePlanTable);

        // construct the general allocation table
        List<PortfolioEntryResourcePlanAllocatedResourceListView> allocatedResourceListView = new ArrayList<>();

        SortableCollection<DateSortableObject> sortableCollection = new SortableCollection<>();
        for (PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit : resourcePlan.portfolioEntryResourcePlanAllocatedOrgUnits) {
            sortableCollection.addObject(new DateSortableObject(allocatedOrgUnit.endDate, allocatedOrgUnit));
        }
        for (PortfolioEntryResourcePlanAllocatedCompetency allocatedCompetency : resourcePlan.portfolioEntryResourcePlanAllocatedCompetencies) {
            sortableCollection.addObject(new DateSortableObject(allocatedCompetency.endDate, allocatedCompetency));
        }

        for (DateSortableObject dateSortableObject : sortableCollection.getSorted()) {
            if (dateSortableObject.getObject() instanceof PortfolioEntryResourcePlanAllocatedOrgUnit) {
                PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit = (PortfolioEntryResourcePlanAllocatedOrgUnit) dateSortableObject.getObject();
                allocatedResourceListView.add(new PortfolioEntryResourcePlanAllocatedResourceListView(allocatedOrgUnit));
            } else if (dateSortableObject.getObject() instanceof PortfolioEntryResourcePlanAllocatedCompetency) {
                PortfolioEntryResourcePlanAllocatedCompetency allocatedCompetency = (PortfolioEntryResourcePlanAllocatedCompetency) dateSortableObject
                        .getObject();
                allocatedResourceListView.add(new PortfolioEntryResourcePlanAllocatedResourceListView(allocatedCompetency));
            }
        }

        Table<PortfolioEntryResourcePlanAllocatedResourceListView> allocatedResourcesTable = PortfolioEntryResourcePlanAllocatedResourceListView.templateTable
                .fill(allocatedResourceListView, hideColumnsForResourcePlanTable);

        // check if there are active competencies
        boolean existCompetencies = ActorDao.getCompetencyActiveAsList().size() > 0 ? true : false;

        return ok(views.html.core.portfolioentryplanning.resources.render(portfolioEntry, allocatedActorsTable, allocatedResourcesTable, existCompetencies));
    }

    /**
     * allocated actors
     */

    /**
     * Create or edit an allocated actor (for a resource plan).
     * 
     * @param id
     *            the portfolio entry id
     * @param allocatedActorId
     *            the allocated actor id, set to 0 for create case
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result manageAllocatedActor(Long id, Long allocatedActorId) {

        // for create case, check there is at least one actor that can be
        // allocated (represented by the stakeholders of the portfolio entry)
        if (allocatedActorId.equals(Long.valueOf(0))) {
            List<Actor> actors = ActorDao.getActorActiveAsListByPE(id);
            if (actors == null || actors.size() == 0) {
                Utilities.sendInfoFlashMessage(Msg.get("core.portfolio_entry_planning.allocated_actor.manage.noactor"));
                return redirect(controllers.core.routes.PortfolioEntryStakeholderController.index(id));
            }
        }

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // initiate the form with the template
        Form<PortfolioEntryResourcePlanAllocatedActorFormData> allocatedActorForm = allocatedActorFormTemplate;

        // edit case: inject values
        if (!allocatedActorId.equals(Long.valueOf(0))) {

            PortfolioEntryResourcePlanAllocatedActor allocatedActor = PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorById(allocatedActorId);

            // security: the portfolioEntry must be related to the object
            if (!allocatedActor.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id.equals(id)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            allocatedActorForm = allocatedActorFormTemplate.fill(new PortfolioEntryResourcePlanAllocatedActorFormData(allocatedActor));

            // add the custom attributes values
            CustomAttributeFormAndDisplayHandler.fillWithValues(allocatedActorForm, PortfolioEntryResourcePlanAllocatedActor.class, allocatedActorId);
        } else {
            // add the custom attributes default values
            CustomAttributeFormAndDisplayHandler.fillWithValues(allocatedActorForm, PortfolioEntryResourcePlanAllocatedActor.class, null);
        }

        return ok(views.html.core.portfolioentryplanning.allocated_actor_manage.render(portfolioEntry, allocatedActorForm));

    }

    /**
     * Save an allocated actor.
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processManageAllocatedActor() {

        // bind the form
        Form<PortfolioEntryResourcePlanAllocatedActorFormData> boundForm = allocatedActorFormTemplate.bindFromRequest();

        // get the portfolioEntry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        if (boundForm.hasErrors() || CustomAttributeFormAndDisplayHandler.validateValues(boundForm, PortfolioEntryResourcePlanAllocatedActor.class)) {
            return ok(views.html.core.portfolioentryplanning.allocated_actor_manage.render(portfolioEntry, boundForm));
        }

        PortfolioEntryResourcePlanAllocatedActorFormData portfolioEntryResourcePlanAllocatedActorFormData = boundForm.get();

        PortfolioEntryResourcePlanAllocatedActor allocatedActor = null;

        if (portfolioEntryResourcePlanAllocatedActorFormData.allocatedActorId == null) { // create
                                                                                         // case

            // get current planning
            LifeCycleInstancePlanning planning = portfolioEntry.activeLifeCycleInstance.getCurrentLifeCycleInstancePlanning();

            // get the resource plan
            PortfolioEntryResourcePlan resourcePlan = planning.portfolioEntryResourcePlan;

            allocatedActor = new PortfolioEntryResourcePlanAllocatedActor();
            allocatedActor.portfolioEntryResourcePlan = resourcePlan;

            portfolioEntryResourcePlanAllocatedActorFormData.fill(allocatedActor);

            allocatedActor.save();

            Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_planning.allocated_actor.add.successful"));

        } else { // edit case

            allocatedActor = PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorById(portfolioEntryResourcePlanAllocatedActorFormData.allocatedActorId);

            boolean oldIsConfirmed = allocatedActor.isConfirmed;

            // security: the portfolioEntry must be related to the object
            if (!allocatedActor.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id.equals(id)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            portfolioEntryResourcePlanAllocatedActorFormData.fill(allocatedActor);
            allocatedActor.update();

            Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_planning.allocated_actor.edit.successful"));

            // if the allocation was previously confirmed, then we send a
            // notification to the manager of the concerned actor
            if (oldIsConfirmed) {
                ActorDao.sendNotification(allocatedActor.actor.manager, NotificationCategory.getByCode(Code.PORTFOLIO_ENTRY),
                        controllers.core.routes.ActorController.allocation(allocatedActor.actor.id).url(),
                        "core.portfolio_entry_planning.allocated_actor.edit_confirmed.notification.title",
                        "core.portfolio_entry_planning.allocated_actor.edit_confirmed.notification.message", allocatedActor.actor.getNameHumanReadable());
            }
        }

        // save the custom attributes
        CustomAttributeFormAndDisplayHandler.validateAndSaveValues(boundForm, PortfolioEntryResourcePlanAllocatedActor.class, allocatedActor.id);

        return redirect(controllers.core.routes.PortfolioEntryPlanningController.resources(id));

    }

    /**
     * Delete an allocated actor.
     * 
     * @param id
     *            the portfolio entry id
     * @param allocatedActorId
     *            the allocated actor id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result deleteAllocatedActor(Long id, Long allocatedActorId) {

        // get the allocated actor
        PortfolioEntryResourcePlanAllocatedActor allocatedActor = PortfolioEntryResourcePlanDAO.getPEPlanAllocatedActorById(allocatedActorId);

        // security: the portfolioEntry must be related to the object
        if (!allocatedActor.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id.equals(id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        // set the delete flag to true
        allocatedActor.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_planning.allocated_actor.delete.successful"));

        return redirect(controllers.core.routes.PortfolioEntryPlanningController.resources(id));
    }

    /**
     * allocated delivery units
     */

    /**
     * Create or edit an allocated delivery unit (for a resource plan).
     * 
     * @param id
     *            the porfolio entry id
     * @param allocatedOrgUnitId
     *            the allocated delivery unit id, set to 0 for create case
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result manageAllocatedOrgUnit(Long id, Long allocatedOrgUnitId) {

        // for create case, check there is at least one delivery unit that can
        // be allocated (represented by the delivery units of the portfolio
        // entry)
        if (allocatedOrgUnitId.equals(Long.valueOf(0))) {
            List<OrgUnit> orgUnits = OrgUnitDao.getOrgUnitActiveCanDeliverAsListByPE(id);
            if (orgUnits == null || orgUnits.size() == 0) {
                Utilities.sendInfoFlashMessage(Msg.get("core.portfolio_entry_planning.allocated_org_unit.manage.nodeliveryunit"));
                return redirect(controllers.core.routes.PortfolioEntryController.edit(id));
            }
        }

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // initiate the form with the template
        Form<PortfolioEntryResourcePlanAllocatedOrgUnitFormData> allocatedOrgUnitForm = allocatedOrgUnitFormTemplate;

        // edit case: inject values
        if (!allocatedOrgUnitId.equals(Long.valueOf(0))) {

            PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit = PortfolioEntryResourcePlanDAO
                    .getPEResourcePlanAllocatedOrgUnitById(allocatedOrgUnitId);

            // security: the portfolioEntry must be related to the object
            if (!allocatedOrgUnit.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id.equals(id)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            allocatedOrgUnitForm = allocatedOrgUnitFormTemplate.fill(new PortfolioEntryResourcePlanAllocatedOrgUnitFormData(allocatedOrgUnit));

            // add the custom attributes values
            CustomAttributeFormAndDisplayHandler.fillWithValues(allocatedOrgUnitForm, PortfolioEntryResourcePlanAllocatedOrgUnit.class, allocatedOrgUnitId);
        } else {
            // add the custom attributes default values
            CustomAttributeFormAndDisplayHandler.fillWithValues(allocatedOrgUnitForm, PortfolioEntryResourcePlanAllocatedOrgUnit.class, null);
        }

        return ok(views.html.core.portfolioentryplanning.allocated_org_unit_manage.render(portfolioEntry, allocatedOrgUnitForm));

    }

    /**
     * Save an allocated delivery unit.
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processManageAllocatedOrgUnit() {

        // bind the form
        Form<PortfolioEntryResourcePlanAllocatedOrgUnitFormData> boundForm = allocatedOrgUnitFormTemplate.bindFromRequest();

        // get the portfolioEntry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        if (boundForm.hasErrors() || CustomAttributeFormAndDisplayHandler.validateValues(boundForm, PortfolioEntryResourcePlanAllocatedOrgUnit.class)) {
            return ok(views.html.core.portfolioentryplanning.allocated_org_unit_manage.render(portfolioEntry, boundForm));
        }

        PortfolioEntryResourcePlanAllocatedOrgUnitFormData portfolioEntryResourcePlanAllocatedOrgUnitFormData = boundForm.get();

        PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit = null;

        if (portfolioEntryResourcePlanAllocatedOrgUnitFormData.allocatedOrgUnitId == null) { // create
                                                                                             // case

            // get current planning
            LifeCycleInstancePlanning planning = portfolioEntry.activeLifeCycleInstance.getCurrentLifeCycleInstancePlanning();

            // get the resource plan
            PortfolioEntryResourcePlan resourcePlan = planning.portfolioEntryResourcePlan;

            allocatedOrgUnit = new PortfolioEntryResourcePlanAllocatedOrgUnit();
            allocatedOrgUnit.portfolioEntryResourcePlan = resourcePlan;

            portfolioEntryResourcePlanAllocatedOrgUnitFormData.fill(allocatedOrgUnit);

            allocatedOrgUnit.save();

            Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_planning.allocated_org_unit.add.successful"));

        } else { // edit case

            allocatedOrgUnit = PortfolioEntryResourcePlanDAO
                    .getPEResourcePlanAllocatedOrgUnitById(portfolioEntryResourcePlanAllocatedOrgUnitFormData.allocatedOrgUnitId);

            // security: the portfolioEntry must be related to the object
            if (!allocatedOrgUnit.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id.equals(id)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            portfolioEntryResourcePlanAllocatedOrgUnitFormData.fill(allocatedOrgUnit);
            allocatedOrgUnit.update();

            Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_planning.allocated_org_unit.edit.successful"));
        }

        // save the custom attributes
        CustomAttributeFormAndDisplayHandler.validateAndSaveValues(boundForm, PortfolioEntryResourcePlanAllocatedOrgUnit.class, allocatedOrgUnit.id);

        return redirect(controllers.core.routes.PortfolioEntryPlanningController.resources(id));
    }

    /**
     * Delete an allocated delivery unit.
     * 
     * @param id
     *            the portfolio entry id
     * @param allocatedOrgUnitId
     *            the allocated delivery unit id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result deleteAllocatedOrgUnit(Long id, Long allocatedOrgUnitId) {

        // get the allocated org unit
        PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit = PortfolioEntryResourcePlanDAO.getPEResourcePlanAllocatedOrgUnitById(allocatedOrgUnitId);

        // security: the portfolioEntry must be related to the object
        if (!allocatedOrgUnit.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id.equals(id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        // set the delete flag to true
        allocatedOrgUnit.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_planning.allocated_org_unit.delete.successful"));

        return redirect(controllers.core.routes.PortfolioEntryPlanningController.resources(id));
    }

    /**
     * Create or edit an allocated competency (for a resource plan).
     * 
     * @param id
     *            the porfolio entry id
     * @param allocatedCompetencyId
     *            the allocated competency id, set to 0 for create case
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result manageAllocatedCompetency(Long id, Long allocatedCompetencyId) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // initiate the form with the template
        Form<PortfolioEntryResourcePlanAllocatedCompetencyFormData> allocatedCompetencyForm = allocatedCompetencyFormTemplate;

        // edit case: inject values
        if (!allocatedCompetencyId.equals(Long.valueOf(0))) {

            PortfolioEntryResourcePlanAllocatedCompetency allocatedCompetency = PortfolioEntryResourcePlanDAO
                    .getPEResourcePlanAllocatedCompetencyById(allocatedCompetencyId);

            // security: the portfolioEntry must be related to the object
            if (!allocatedCompetency.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id.equals(id)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            allocatedCompetencyForm = allocatedCompetencyFormTemplate.fill(new PortfolioEntryResourcePlanAllocatedCompetencyFormData(allocatedCompetency));

            // add the custom attributes values
            CustomAttributeFormAndDisplayHandler.fillWithValues(allocatedCompetencyForm, PortfolioEntryResourcePlanAllocatedCompetency.class,
                    allocatedCompetencyId);
        } else {
            // add the custom attributes default values
            CustomAttributeFormAndDisplayHandler.fillWithValues(allocatedCompetencyForm, PortfolioEntryResourcePlanAllocatedCompetency.class, null);
        }

        return ok(views.html.core.portfolioentryplanning.allocated_competency_manage.render(portfolioEntry, allocatedCompetencyForm));
    }

    /**
     * Save an allocated competency.
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processManageAllocatedCompetency() {

        // bind the form
        Form<PortfolioEntryResourcePlanAllocatedCompetencyFormData> boundForm = allocatedCompetencyFormTemplate.bindFromRequest();

        // get the portfolioEntry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        if (boundForm.hasErrors() || CustomAttributeFormAndDisplayHandler.validateValues(boundForm, PortfolioEntryResourcePlanAllocatedCompetency.class)) {
            return ok(views.html.core.portfolioentryplanning.allocated_competency_manage.render(portfolioEntry, boundForm));
        }

        PortfolioEntryResourcePlanAllocatedCompetencyFormData allocatedCompetencyFormData = boundForm.get();

        PortfolioEntryResourcePlanAllocatedCompetency allocatedCompetency = null;

        if (allocatedCompetencyFormData.allocatedCompetencyId == null) { // create
                                                                         // case

            // get current planning
            LifeCycleInstancePlanning planning = portfolioEntry.activeLifeCycleInstance.getCurrentLifeCycleInstancePlanning();

            // get the resource plan
            PortfolioEntryResourcePlan resourcePlan = planning.portfolioEntryResourcePlan;

            allocatedCompetency = new PortfolioEntryResourcePlanAllocatedCompetency();
            allocatedCompetency.portfolioEntryResourcePlan = resourcePlan;

            allocatedCompetencyFormData.fill(allocatedCompetency);

            allocatedCompetency.save();

            Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_planning.allocated_competency.add.successful"));

        } else { // edit case

            allocatedCompetency = PortfolioEntryResourcePlanDAO.getPEResourcePlanAllocatedCompetencyById(allocatedCompetencyFormData.allocatedCompetencyId);

            // security: the portfolioEntry must be related to the object
            if (!allocatedCompetency.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id.equals(id)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            allocatedCompetencyFormData.fill(allocatedCompetency);
            allocatedCompetency.update();

            Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_planning.allocated_competency.edit.successful"));
        }

        // save the custom attributes
        CustomAttributeFormAndDisplayHandler.validateAndSaveValues(boundForm, PortfolioEntryResourcePlanAllocatedCompetency.class, allocatedCompetency.id);

        return redirect(controllers.core.routes.PortfolioEntryPlanningController.resources(id));

    }

    /**
     * Delete an allocated competency.
     * 
     * @param id
     *            the portfolio entry id
     * @param allocatedCompetencyId
     *            the allocated competency id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result deleteAllocatedCompetency(Long id, Long allocatedCompetencyId) {

        // get the allocated competency
        PortfolioEntryResourcePlanAllocatedCompetency allocatedCompetency = PortfolioEntryResourcePlanDAO
                .getPEResourcePlanAllocatedCompetencyById(allocatedCompetencyId);

        // security: the portfolioEntry must be related to the object
        if (!allocatedCompetency.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id.equals(id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        // set the delete flag to true
        allocatedCompetency.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_planning.allocated_competency.delete.successful"));

        return redirect(controllers.core.routes.PortfolioEntryPlanningController.resources(id));

    }

    /**
     * Display the form to reallocate an org unit to an actor.
     * 
     * @param id
     *            the portfolio entry id
     * @param allocatedOrgUnitId
     *            the allocated org unit
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result reallocateOrgUnit(Long id, Long allocatedOrgUnitId) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the allocated org unit
        PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit = PortfolioEntryResourcePlanDAO.getPEResourcePlanAllocatedOrgUnitById(allocatedOrgUnitId);

        // get the org unit
        OrgUnit orgUnit = allocatedOrgUnit.orgUnit;

        // security: the portfolioEntry must be related to the object
        if (!allocatedOrgUnit.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id.equals(id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        Form<PortfolioEntryResourcePlanAllocatedActorFormData> allocatedActorForm = reallocateOrgUnitFormTemplate
                .fill(new PortfolioEntryResourcePlanAllocatedActorFormData(allocatedOrgUnit));

        return ok(views.html.core.portfolioentryplanning.allocated_org_unit_reallocate.render(portfolioEntry, orgUnit, allocatedActorForm));

    }

    /**
     * Process the reallocation of an org unit to an actor.
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processReallocateOrgUnit() {

        // bind the form
        Form<PortfolioEntryResourcePlanAllocatedActorFormData> boundForm = reallocateOrgUnitFormTemplate.bindFromRequest();

        // get the portfolio entry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the allocated org unit
        Long allocatedOrgUnitId = Long.valueOf(boundForm.data().get("allocatedOrgUnitId"));
        PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit = PortfolioEntryResourcePlanDAO.getPEResourcePlanAllocatedOrgUnitById(allocatedOrgUnitId);

        if (boundForm.hasErrors() || CustomAttributeFormAndDisplayHandler.validateValues(boundForm, PortfolioEntryResourcePlanAllocatedActor.class)) {
            return ok(views.html.core.portfolioentryplanning.allocated_org_unit_reallocate.render(portfolioEntry, allocatedOrgUnit.orgUnit, boundForm));
        }

        PortfolioEntryResourcePlanAllocatedActorFormData allocatedActorFormData = boundForm.get();

        // get current planning
        LifeCycleInstancePlanning planning = portfolioEntry.activeLifeCycleInstance.getCurrentLifeCycleInstancePlanning();

        // get the resource plan
        PortfolioEntryResourcePlan resourcePlan = planning.portfolioEntryResourcePlan;

        // create the allocated actor
        PortfolioEntryResourcePlanAllocatedActor allocatedActor = new PortfolioEntryResourcePlanAllocatedActor();
        allocatedActor.portfolioEntryResourcePlan = resourcePlan;
        allocatedActorFormData.fill(allocatedActor);
        allocatedActor.save();

        // save the custom attributes
        CustomAttributeFormAndDisplayHandler.validateAndSaveValues(boundForm, PortfolioEntryResourcePlanAllocatedActor.class, allocatedActor.id);

        // remove the allocated org unit
        allocatedOrgUnit.doDelete();

        // create the stakeholder if necessary
        if (StakeholderDao.getStakeholderByActorAndTypeAndPE(allocatedActor.actor.id, allocatedActorFormData.stakeholderType, portfolioEntry.id) == null) {
            Stakeholder stakeholder = new Stakeholder();
            stakeholder.actor = allocatedActor.actor;
            stakeholder.portfolioEntry = portfolioEntry;
            stakeholder.stakeholderType = StakeholderDao.getStakeholderTypeById(allocatedActorFormData.stakeholderType);
            stakeholder.save();
        }

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_planning.reallocate_resource.successful"));

        if (allocatedOrgUnit.days.compareTo(allocatedActor.days) > 0) {
            return redirect(controllers.core.routes.PortfolioEntryPlanningController.reallocateOrgUnitReportBalance(portfolioEntry.id, allocatedOrgUnit.id,
                    allocatedOrgUnit.days.subtract(allocatedActor.days).doubleValue()));
        } else {
            return redirect(controllers.core.routes.PortfolioEntryPlanningController.resources(id));
        }

    }

    /**
     * Report the days balance of an org unit reallocation.
     * 
     * @param id
     *            the portfolio entry id
     * @param allocatedOrgUnitId
     *            the allocated org unit id
     * @param days
     *            the balance to report
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result reallocateOrgUnitReportBalance(Long id, Long allocatedOrgUnitId, Double days) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the deleted allocated org unit
        PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit = PortfolioEntryResourcePlanDAO
                .getPEResourcePlanAllocatedOrgUnitDeletedById(allocatedOrgUnitId);

        return ok(views.html.core.portfolioentryplanning.allocated_org_unit_reallocate_report_balance.render(portfolioEntry, allocatedOrgUnit, days));
    }

    /**
     * Process the reporting of the days balance of an org unit reallocation.
     * 
     * @param id
     *            the portfolio entry id
     * @param allocatedOrgUnitId
     *            the allocated org unit id
     * @param days
     *            the balance to report
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processReallocateOrgUnitReportBalance(Long id, Long allocatedOrgUnitId, Double days) {

        // get the deleted allocated org unit
        PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit = PortfolioEntryResourcePlanDAO
                .getPEResourcePlanAllocatedOrgUnitDeletedById(allocatedOrgUnitId);

        // restore the initial allocated org unit with the days
        allocatedOrgUnit.deleted = false;
        allocatedOrgUnit.days = new BigDecimal(days);
        allocatedOrgUnit.save();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_planning.reallocate.report_balance.successful"));

        return redirect(controllers.core.routes.PortfolioEntryPlanningController.resources(id));
    }

    /**
     * Display the form to reallocate a competency to an actor.
     * 
     * @param id
     *            the portfolio entry id
     * @param allocatedCompetencyId
     *            the allocated competency
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result reallocateCompetency(Long id, Long allocatedCompetencyId) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the allocated org unit
        PortfolioEntryResourcePlanAllocatedCompetency allocatedCompetency = PortfolioEntryResourcePlanDAO
                .getPEResourcePlanAllocatedCompetencyById(allocatedCompetencyId);

        // get the competency
        Competency competency = allocatedCompetency.competency;

        // security: the portfolioEntry must be related to the object
        if (!allocatedCompetency.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id.equals(id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        Form<PortfolioEntryResourcePlanAllocatedActorFormData> allocatedActorForm = reallocateCompetencyFormTemplate
                .fill(new PortfolioEntryResourcePlanAllocatedActorFormData(allocatedCompetency));

        return ok(views.html.core.portfolioentryplanning.allocated_competency_reallocate.render(portfolioEntry, competency, allocatedActorForm));

    }

    /**
     * Process the reallocation of an competency to an actor.
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processReallocateCompetency() {

        // bind the form
        Form<PortfolioEntryResourcePlanAllocatedActorFormData> boundForm = reallocateCompetencyFormTemplate.bindFromRequest();

        // get the portfolio entry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the allocated competency
        Long allocatedCompetencyId = Long.valueOf(boundForm.data().get("allocatedCompetencyId"));
        PortfolioEntryResourcePlanAllocatedCompetency allocatedCompetency = PortfolioEntryResourcePlanDAO
                .getPEResourcePlanAllocatedCompetencyById(allocatedCompetencyId);

        if (boundForm.hasErrors() || CustomAttributeFormAndDisplayHandler.validateValues(boundForm, PortfolioEntryResourcePlanAllocatedActor.class)) {
            return ok(
                    views.html.core.portfolioentryplanning.allocated_competency_reallocate.render(portfolioEntry, allocatedCompetency.competency, boundForm));
        }

        PortfolioEntryResourcePlanAllocatedActorFormData allocatedActorFormData = boundForm.get();

        // get current planning
        LifeCycleInstancePlanning planning = portfolioEntry.activeLifeCycleInstance.getCurrentLifeCycleInstancePlanning();

        // get the resource plan
        PortfolioEntryResourcePlan resourcePlan = planning.portfolioEntryResourcePlan;

        // create the allocated actor
        PortfolioEntryResourcePlanAllocatedActor allocatedActor = new PortfolioEntryResourcePlanAllocatedActor();
        allocatedActor.portfolioEntryResourcePlan = resourcePlan;
        allocatedActorFormData.fill(allocatedActor);
        allocatedActor.save();

        // save the custom attributes
        CustomAttributeFormAndDisplayHandler.validateAndSaveValues(boundForm, PortfolioEntryResourcePlanAllocatedActor.class, allocatedActor.id);

        // remove the allocated competency
        allocatedCompetency.doDelete();

        // create the stakeholder if necessary
        if (StakeholderDao.getStakeholderByActorAndTypeAndPE(allocatedActor.actor.id, allocatedActorFormData.stakeholderType, portfolioEntry.id) == null) {
            Stakeholder stakeholder = new Stakeholder();
            stakeholder.actor = allocatedActor.actor;
            stakeholder.portfolioEntry = portfolioEntry;
            stakeholder.stakeholderType = StakeholderDao.getStakeholderTypeById(allocatedActorFormData.stakeholderType);
            stakeholder.save();
        }

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_planning.reallocate_resource.successful"));

        if (allocatedCompetency.days.compareTo(allocatedActor.days) > 0) {
            return redirect(controllers.core.routes.PortfolioEntryPlanningController.reallocateCompetencyReportBalance(portfolioEntry.id,
                    allocatedCompetency.id, allocatedCompetency.days.subtract(allocatedActor.days).doubleValue()));
        } else {
            return redirect(controllers.core.routes.PortfolioEntryPlanningController.resources(id));
        }

    }

    /**
     * Report the days balance of a competency reallocation.
     * 
     * @param id
     *            the portfolio entry id
     * @param allocatedCompetencyId
     *            the allocated competency id
     * @param days
     *            the balance to report
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result reallocateCompetencyReportBalance(Long id, Long allocatedCompetencyId, Double days) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the deleted allocated competency
        PortfolioEntryResourcePlanAllocatedCompetency allocatedCompetency = PortfolioEntryResourcePlanDAO
                .getPEResourcePlanAllocatedCompetencyDeletedById(allocatedCompetencyId);

        return ok(views.html.core.portfolioentryplanning.allocated_competency_reallocate_report_balance.render(portfolioEntry, allocatedCompetency, days));
    }

    /**
     * Process the reporting of the days balance of a competency reallocation.
     * 
     * @param id
     *            the portfolio entry id
     * @param allocatedCompetencyId
     *            the competency unit id
     * @param days
     *            the balance to report
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processReallocateCompetencyReportBalance(Long id, Long allocatedCompetencyId, Double days) {

        // get the deleted allocated competency
        PortfolioEntryResourcePlanAllocatedCompetency allocatedCompetency = PortfolioEntryResourcePlanDAO
                .getPEResourcePlanAllocatedCompetencyDeletedById(allocatedCompetencyId);

        // restore the initial allocated competency with the days
        allocatedCompetency.deleted = false;
        allocatedCompetency.days = new BigDecimal(days);
        allocatedCompetency.save();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_planning.reallocate.report_balance.successful"));

        return redirect(controllers.core.routes.PortfolioEntryPlanningController.resources(id));
    }

    /**
     * Get the package status as a value holder collection.
     */
    public static ISelectableValueHolderCollection<String> getPackageStatusAsValueHolderCollection() {
        ISelectableValueHolderCollection<String> allStatus = new DefaultSelectableValueHolderCollection<String>();
        for (PortfolioEntryPlanningPackage.Status status : PortfolioEntryPlanningPackage.Status.values()) {
            allStatus.add(new DefaultSelectableValueHolder<String>(status.name(),
                    Msg.get("object.portfolio_entry_planning_package.status." + status.name() + ".label")));
        }
        return allStatus;
    }

    /**
     * Store the filter configuration in the user preferences.
     * 
     * @param filterConfigAsJson
     *            the filter configuration as a json string
     * @param preferenceName
     *            the name of the preference
     */
    private void storeFilterConfigFromPreferences(String filterConfigAsJson, String preferenceName) {
        getPreferenceManagerPlugin().updatePreferenceValue(preferenceName, filterConfigAsJson);
    }

    /**
     * Retrieve the filter configuration from the user preferences.
     * 
     * @param preferenceName
     *            the name of the preference
     */
    private String getFilterConfigurationFromPreferences(String preferenceName) {
        return getPreferenceManagerPlugin().getPreferenceValueAsString(preferenceName);
    }

    /**
     * Get the preference manager service.
     */
    private IPreferenceManagerPlugin getPreferenceManagerPlugin() {
        return preferenceManagerPlugin;
    }

    /**
     * Get the message service.
     */
    private II18nMessagesPlugin getMessagesPlugin() {
        return messagesPlugin;
    }

    /**
     * Get the attachment service.
     */
    private IAttachmentManagerPlugin getAttachmentManagerPlugin() {
        return attachmentManagerPlugin;
    }

    /**
     * The configuration of the overview (gantt chart).
     * 
     * @author Johann Kohler
     * 
     */
    public static class OverviewConfiguration {

        public boolean phases;
        public boolean milestones;
        public boolean packages;
        public boolean releases;
        public boolean iterations;

        /**
         * Default constructor.
         */
        public OverviewConfiguration() {
        }

        /**
         * Construct an overview configuration with a default value.
         * 
         * @param defaultValue
         *            the default value (same for all attributes)
         */
        public OverviewConfiguration(boolean defaultValue) {
            this.phases = defaultValue;
            this.milestones = defaultValue;
            this.packages = defaultValue;
            this.releases = defaultValue;
            this.iterations = defaultValue;
        }

        /**
         * Load the configuration from a user preference.
         */
        public static OverviewConfiguration load() {
            String overviewPreferenceAsJson = ServiceStaticAccessor.getPreferenceManagerPlugin()
                    .getPreferenceValueAsString(IMafConstants.PORTFOLIO_ENTRY_PLANNING_OVERVIEW_PREFERENCE);
            if (overviewPreferenceAsJson == null || overviewPreferenceAsJson.equals("")) {
                OverviewConfiguration conf = new OverviewConfiguration(true);
                store(conf);
                return conf;
            } else {
                try {
                    return new ObjectMapper().readValue(overviewPreferenceAsJson, OverviewConfiguration.class);
                } catch (Exception e) {
                    Logger.error("impossible to parse the value of PORTFOLIO_ENTRY_PLANNING_OVERVIEW_PREFERENCE", e);
                    OverviewConfiguration conf = new OverviewConfiguration(true);
                    store(conf);
                    return conf;
                }
            }
        }

        /**
         * Store the configuration in a user preference.
         * 
         * @param conf
         *            the overview configuration
         */
        public static void store(OverviewConfiguration conf) {
            String json = "";
            try {
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                json = ow.writeValueAsString(conf);
            } catch (JsonProcessingException e) {
                Logger.error("impossible to jsonify the OverviewConfiguration", e);
            }
            ServiceStaticAccessor.getPreferenceManagerPlugin().updatePreferenceValue(IMafConstants.PORTFOLIO_ENTRY_PLANNING_OVERVIEW_PREFERENCE, json);
        }

    }

    /**
     * Get the user session manager service.
     */
    private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return userSessionManagerPlugin;
    }

    /**
     * Get the data syndication service.
     */
    private IDataSyndicationService getDataSyndicationService() {
        return dataSyndicationService;
    }

    private ISecurityService getSecurityService() {
        return securityService;
    }

}
