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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.OrderBy;

import be.objectify.deadbolt.java.actions.Dynamic;
import constants.IMafConstants;
import controllers.ControllersUtils;
import dao.delivery.IterationDAO;
import dao.delivery.ReleaseDAO;
import dao.delivery.RequirementDAO;
import dao.pmo.PortfolioEntryDao;
import framework.highcharts.pattern.BasicBar;
import framework.highcharts.pattern.DistributedDonut;
import framework.highcharts.pattern.RangeLine;
import framework.security.ISecurityService;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.utils.CustomAttributeFormAndDisplayHandler;
import framework.utils.DefaultSelectableValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.FilterConfig;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Msg;
import framework.utils.Pagination;
import framework.utils.Table;
import framework.utils.Utilities;
import models.delivery.Iteration;
import models.delivery.Release;
import models.delivery.ReleasePortfolioEntry;
import models.delivery.Requirement;
import models.delivery.RequirementPriority;
import models.delivery.RequirementSeverity;
import models.delivery.RequirementStatus.Type;
import models.pmo.PortfolioEntry;
import play.Configuration;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.CheckPortfolioEntryExists;
import utils.form.IterationFormData;
import utils.form.ReleasePortfolioEntryFormData;
import utils.form.RequirementFormData;
import utils.table.IterationListView;
import utils.table.ReleasePortfolioEntryListView;
import utils.table.RequirementListView;

/**
 * The controller which allows to manage the delivery part of a portfolio entry.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryDeliveryController extends Controller {

    @Inject
    private II18nMessagesPlugin messagesPlugin;
    @Inject
    private ISecurityService securityService;
    @Inject
    private Configuration configuration;
    @Inject
    private IUserSessionManagerPlugin userSessionManagerPlugin;


    private static Logger.ALogger log = Logger.of(PortfolioEntryDeliveryController.class);

    public static Form<RequirementFormData> formTemplate = Form.form(RequirementFormData.class);
    public static Form<IterationFormData> iterationFormTemplate = Form.form(IterationFormData.class);
    public static Form<ReleasePortfolioEntryFormData> assignReleaseFormTemplate = Form.form(ReleasePortfolioEntryFormData.class);

    /**
     * Display the list of the requirements of a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION)
    public Result requirements(Long id) {

        // get the portfolio entry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        try {

            // get the filter config
            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            FilterConfig<RequirementListView> filterConfig = RequirementListView.filterConfig.getCurrent(uid, request());

            ExpressionList<Requirement> expressionList = filterConfig.updateWithSearchExpression(RequirementDAO.getRequirementAsExprByPE(id));
            filterConfig.updateWithSortExpression(expressionList);

            Pagination<Requirement> pagination = new Pagination<Requirement>(expressionList);
            pagination.setCurrentPage(filterConfig.getCurrentPage());

            List<RequirementListView> requirementListView = new ArrayList<RequirementListView>();
            for (Requirement requirement : pagination.getListOfObjects()) {
                requirementListView.add(new RequirementListView(requirement));
            }

            Set<String> hideColumns = filterConfig.getColumnsToHide();
            if (!getSecurityService().dynamic("PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION", "")) {
                hideColumns.add("editActionLink");
            }

            Table<RequirementListView> filledTable = RequirementListView.templateTable.fillForFilterConfig(requirementListView, hideColumns);

            return ok(views.html.core.portfolioentrydelivery.requirements.render(portfolioEntry, filledTable, pagination, filterConfig));

        } catch (Exception e) {

               return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getMessagesPlugin());

        }

    }

    /**
     * Filter the requirements.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION)
    public Result requirementsFilter(Long id) {

        try {

            // get the filter config
            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            FilterConfig<RequirementListView> filterConfig = RequirementListView.filterConfig.persistCurrentInDefault(uid, request());

            if (filterConfig == null) {
                return ok(views.html.framework_views.parts.table.dynamic_tableview_no_more_compatible.render());
            } else {

                ExpressionList<Requirement> expressionList = filterConfig.updateWithSearchExpression(RequirementDAO.getRequirementAsExprByPE(id));
                filterConfig.updateWithSortExpression(expressionList);

                Pagination<Requirement> pagination = new Pagination<Requirement>(expressionList);
                pagination.setCurrentPage(filterConfig.getCurrentPage());

                List<RequirementListView> requirementListView = new ArrayList<RequirementListView>();
                for (Requirement requirement : pagination.getListOfObjects()) {
                    requirementListView.add(new RequirementListView(requirement));
                }

                Set<String> hideColumns = filterConfig.getColumnsToHide();
                if (!getSecurityService().dynamic("PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION", "")) {
                    hideColumns.add("editActionLink");
                }

                Table<RequirementListView> filledTable = RequirementListView.templateTable.fillForFilterConfig(requirementListView, hideColumns);

                return ok(views.html.framework_views.parts.table.dynamic_tableview.render(filledTable, pagination));

            }

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getMessagesPlugin());
        }
    }

    /**
     * Display the status (charts) of the requirements of a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION)
    public Result requirementsStatus(Long id) {

        // get the portfolio entry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // priority pie chart (for the needs)

        DistributedDonut distributedDonutPriority = new DistributedDonut(getMessagesPlugin());

        DistributedDonut.Elem mustElem = new DistributedDonut.Elem(Msg.get("core.portfolio_entry_delivery.requirement.status.priority.must.true.label"));
        double mustTotal = 0;
        for (Type type : Type.values()) {
            Double count = Double.valueOf(RequirementDAO.getRequirementNeedsAsExprByPEAndStatusType(id, type, true).findRowCount());
            if (count != null && count.doubleValue() != 0) {
                mustElem.addSubValue(Msg.get("object.requirement_status.type." + type.name() + ".label"), count);
                mustTotal += count.doubleValue();
            }
        }
        if (mustTotal != 0) {
            mustElem.setValue(mustTotal);
            distributedDonutPriority.addElem(mustElem);
        }

        DistributedDonut.Elem notMustElem = new DistributedDonut.Elem(Msg.get("core.portfolio_entry_delivery.requirement.status.priority.must.false.label"));
        double notMustTotal = 0;
        for (Type type : Type.values()) {
            Double count = Double.valueOf(RequirementDAO.getRequirementNeedsAsExprByPEAndStatusType(id, type, false).findRowCount());
            if (count != null && count.doubleValue() != 0) {
                notMustElem.addSubValue(Msg.get("object.requirement_status.type." + type.name() + ".label"), count);
                notMustTotal += count.doubleValue();
            }
        }
        if (notMustTotal != 0) {
            notMustElem.setValue(notMustTotal);
            distributedDonutPriority.addElem(notMustElem);
        }

        if (distributedDonutPriority.isEmpty()) {
            distributedDonutPriority = null;
        }

        // priority bar chart (for the needs)

        BasicBar basicBarPriority = null;

        if (distributedDonutPriority != null) {

            basicBarPriority = new BasicBar();

            for (Type type : Type.values()) {
                basicBarPriority.addCategory(Msg.get("object.requirement_status.type." + type.name() + ".label"));
            }

            for (RequirementPriority priority : RequirementDAO.getRequirementPriorityAsList()) {
                BasicBar.Elem elem = new BasicBar.Elem(priority.getName());
                for (Type type : Type.values()) {
                    elem.addValue(Double.valueOf(RequirementDAO.getRequirementNeedsAsExprByPEAndStatusTypeAndPriority(id, type, priority.id).findRowCount()));
                }
                basicBarPriority.addElem(elem);
            }

        }

        // severity pie chart (for the defects)

        DistributedDonut distributedDonutSeverity = new DistributedDonut(getMessagesPlugin());

        DistributedDonut.Elem blockerElem = new DistributedDonut.Elem(
                Msg.get("core.portfolio_entry_delivery.requirement.status.severity.blocker.true.label"));
        double blockerTotal = 0;
        for (Type type : Type.values()) {
            Double count = Double.valueOf(RequirementDAO.getRequirementDefectsAsExprByPEAndStatusType(id, type, true).findRowCount());
            if (count != null && count.doubleValue() != 0) {
                blockerElem.addSubValue(Msg.get("object.requirement_status.type." + type.name() + ".label"), count);
                blockerTotal += count.doubleValue();
            }
        }
        if (blockerTotal != 0) {
            blockerElem.setValue(blockerTotal);
            distributedDonutSeverity.addElem(blockerElem);
        }

        DistributedDonut.Elem nonBlockerElem = new DistributedDonut.Elem(
                Msg.get("core.portfolio_entry_delivery.requirement.status.severity.blocker.false.label"));
        double nonBlockerTotal = 0;
        for (Type type : Type.values()) {
            Double count = Double.valueOf(RequirementDAO.getRequirementDefectsAsExprByPEAndStatusType(id, type, false).findRowCount());
            if (count != null && count.doubleValue() != 0) {
                nonBlockerElem.addSubValue(Msg.get("object.requirement_status.type." + type.name() + ".label"), count);
                nonBlockerTotal += count.doubleValue();
            }
        }
        if (nonBlockerTotal != 0) {
            nonBlockerElem.setValue(nonBlockerTotal);
            distributedDonutSeverity.addElem(nonBlockerElem);
        }

        if (distributedDonutSeverity.isEmpty()) {
            distributedDonutSeverity = null;
        }

        // severity bar chart (for the defects)

        BasicBar basicBarSeverity = null;

        if (distributedDonutSeverity != null) {

            basicBarSeverity = new BasicBar();

            for (Type type : Type.values()) {
                basicBarSeverity.addCategory(Msg.get("object.requirement_status.type." + type.name() + ".label"));
            }

            for (RequirementSeverity requirementSeverity : RequirementDAO.getRequirementSeverityAsList()) {
                BasicBar.Elem elem = new BasicBar.Elem(requirementSeverity.getName());
                for (Type type : Type.values()) {
                    elem.addValue(Double.valueOf(
                            RequirementDAO.getRequirementDefectsAsExprByPEAndStatusTypeAndSeverity(id, type, requirementSeverity.id).findRowCount()));
                }
                basicBarSeverity.addElem(elem);
            }

        }

        return ok(views.html.core.portfolioentrydelivery.requirements_status.render(portfolioEntry, distributedDonutPriority, basicBarPriority,
                distributedDonutSeverity, basicBarSeverity));
    }

    /**
     * View all details of a requirement.
     * 
     * @param id
     *            the portfolio entry id
     * @param requirementId
     *            the requirement id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION)
    public Result viewRequirement(Long id, Long requirementId) {

        // get the portfolio entry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the requirement
        Requirement requirement = RequirementDAO.getRequirementById(requirementId);

        // security: the portfolioEntry must be related to the object
        if (!requirement.portfolioEntry.id.equals(id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        return ok(views.html.core.portfolioentrydelivery.requirement_view.render(portfolioEntry, requirement));
    }

    /**
     * Form to edit some attributes of a requirement.
     * 
     * @param id
     *            the portfolio entry id
     * @param requirementId
     *            the requirement id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result editRequirement(Long id, Long requirementId) {

        // get the portfolio entry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the requirement
        Requirement requirement = RequirementDAO.getRequirementById(requirementId);

        // security: the portfolio entry must be related to the object
        if (!requirement.portfolioEntry.id.equals(id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        Form<RequirementFormData> requirementForm = formTemplate.fill(new RequirementFormData(requirement));

        // add the custom attributes values
        CustomAttributeFormAndDisplayHandler.fillWithValues(requirementForm, Requirement.class, requirementId);

        return ok(views.html.core.portfolioentrydelivery.requirement_edit.render(portfolioEntry, requirement, requirementForm,
                ReleaseDAO.getReleaseAsVHByPEAndType(id, models.delivery.ReleasePortfolioEntry.Type.BY_REQUIREMENT)));

    }

    /**
     * Process the edition of a requirement.
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processEditRequirement() {

        // bind the form
        Form<RequirementFormData> boundForm = formTemplate.bindFromRequest();

        // get the portfolioEntry
        Long id = Long.valueOf(request().body().asFormUrlEncoded().get("id")[0]);
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the requirement
        Long requirementId = Long.valueOf(request().body().asFormUrlEncoded().get("requirementId")[0]);
        Requirement requirement = RequirementDAO.getRequirementById(requirementId);

        // security: the portfolio entry must be related to the object
        if (!requirement.portfolioEntry.id.equals(id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        if (boundForm.hasErrors() || CustomAttributeFormAndDisplayHandler.validateValues(boundForm, Requirement.class)) {
            return ok(views.html.core.portfolioentrydelivery.requirement_edit.render(portfolioEntry, requirement, boundForm,
                    ReleaseDAO.getReleaseAsVHByPEAndType(id, models.delivery.ReleasePortfolioEntry.Type.BY_REQUIREMENT)));
        }

        RequirementFormData requirementFormData = boundForm.get();

        requirementFormData.fill(requirement);
        requirement.update();

        // save the custom attributes
        CustomAttributeFormAndDisplayHandler.validateAndSaveValues(boundForm, Requirement.class, requirement.id);

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_delivery.requirement.edit.successful"));

        return redirect(controllers.core.routes.PortfolioEntryDeliveryController.requirements(portfolioEntry.id));
    }

    /**
     * Display the iterations of a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION)
    public Result iterations(Long id) {

        // get the portfolio entry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        try {

            // get the filter config
            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            FilterConfig<IterationListView> filterConfig = IterationListView.filterConfig.getCurrent(uid, request());

            // get the table
            Pair<Table<IterationListView>, Pagination<Iteration>> t = getIterationsTable(id, filterConfig);

            // burndown chart
            ExpressionList<Iteration> expressionList = filterConfig.updateWithSearchExpression(IterationDAO.getIterationAllAsExprByPE(id));
            OrderBy<Iteration> orderBy = expressionList.orderBy();
            orderBy.asc("endDate");
            List<Iteration> iterations = expressionList.findList();

            RangeLine rangeLine = null;

            if (iterations != null && iterations.size() > 0) {

                rangeLine = new RangeLine();

                // the first value name is start
                rangeLine.addValueName(Msg.get("core.portfolio_entry_delivery.iterations.burndown.start.label"));

                // compute the total story points, add the ranges and the value
                // names
                Integer totalStoryPoints = 0;
                for (Iteration iteration : iterations) {
                    if (iteration.storyPoints != null && iteration.endDate != null) {
                        rangeLine.addRange(iteration.getName());
                        rangeLine.addValueName(Msg.get("core.portfolio_entry_delivery.iterations.burndown.end_of_iteration.label", iteration.getName()));
                        totalStoryPoints += iteration.storyPoints;
                    }
                }

                // add the elems
                RangeLine.Elem plannedElem = new RangeLine.Elem(Msg.get("core.portfolio_entry_delivery.iterations.burndown.planned.label"));
                RangeLine.Elem realElem = new RangeLine.Elem(Msg.get("core.portfolio_entry_delivery.iterations.burndown.real.label"));
                rangeLine.addElem(plannedElem);
                rangeLine.addElem(realElem);

                plannedElem.addValue(totalStoryPoints.doubleValue());
                realElem.addValue(totalStoryPoints.doubleValue());

                Integer currentPlannedStoryPoints = totalStoryPoints;
                Integer currentRealStoryPoints = totalStoryPoints;
                for (Iteration iteration : iterations) {
                    if (iteration.storyPoints != null && iteration.endDate != null) {

                        currentPlannedStoryPoints -= iteration.storyPoints;

                        plannedElem.addValue(currentPlannedStoryPoints.doubleValue());

                        if (iteration.isClosed) {
                            // sum of closed story points of requirements
                            currentRealStoryPoints -= RequirementDAO.getStoryPointClosedAsCountByIteration(iteration.id);
                            realElem.addValue(currentRealStoryPoints.doubleValue());
                        } else {
                            currentRealStoryPoints -= iteration.storyPoints;
                            realElem.addValue(null);
                        }

                    }
                }

            }

            return ok(views.html.core.portfolioentrydelivery.iterations.render(portfolioEntry, t.getLeft(), t.getRight(), filterConfig, rangeLine));

        } catch (Exception e) {

            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getMessagesPlugin());

        }

    }

    /**
     * Filter the iterations.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION)
    public Result iterationsFilter(Long id) {

        try {

            // get the filter config
            String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
            FilterConfig<IterationListView> filterConfig = IterationListView.filterConfig.persistCurrentInDefault(uid, request());

            if (filterConfig == null) {
                return ok(views.html.framework_views.parts.table.dynamic_tableview_no_more_compatible.render());
            } else {

                // get the table
                Pair<Table<IterationListView>, Pagination<Iteration>> t = getIterationsTable(id, filterConfig);

                return ok(views.html.framework_views.parts.table.dynamic_tableview.render(t.getLeft(), t.getRight()));

            }

        } catch (Exception e) {
            return ControllersUtils.logAndReturnUnexpectedError(e, log, getConfiguration(), getMessagesPlugin());
        }

    }

    /**
     * Get the iterations table for a portfolio entry and a filter config.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param filterConfig
     *            the filter config.
     */
    private Pair<Table<IterationListView>, Pagination<Iteration>> getIterationsTable(Long portfolioEntryId, FilterConfig<IterationListView> filterConfig) {

        ExpressionList<Iteration> expressionList = filterConfig.updateWithSearchExpression(IterationDAO.getIterationAllAsExprByPE(portfolioEntryId));
        filterConfig.updateWithSortExpression(expressionList);

        Pagination<Iteration> pagination = new Pagination<Iteration>(expressionList);
        pagination.setCurrentPage(filterConfig.getCurrentPage());

        List<IterationListView> iterationListView = new ArrayList<IterationListView>();
        for (Iteration iteration : pagination.getListOfObjects()) {
            iterationListView.add(new IterationListView(iteration));
        }

        Set<String> hideColumns = filterConfig.getColumnsToHide();
        if (!getSecurityService().dynamic("PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION", "")) {
            hideColumns.add("editActionLink");
        }

        Table<IterationListView> table = IterationListView.templateTable.fillForFilterConfig(iterationListView, hideColumns);

        return Pair.of(table, pagination);

    }

    /**
     * Display the details of an iteration.
     * 
     * @param id
     *            the portfolio entry id
     * @param iterationId
     *            the iteration id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION)
    public Result viewIteration(Long id, Long iterationId) {

        // get the portfolio entry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the requirement
        Iteration iteration = IterationDAO.getIterationById(iterationId);

        // security: the portfolioEntry must be related to the object
        if (!iteration.portfolioEntry.id.equals(id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        return ok(views.html.core.portfolioentrydelivery.iteration_view.render(portfolioEntry, iteration));
    }

    /**
     * Edit an iteration.
     * 
     * @param id
     *            the portfolio entry id
     * @param iterationId
     *            the iteration id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result editIteration(Long id, Long iterationId) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        Iteration iteration = IterationDAO.getIterationById(iterationId);

        // security: the portfolioEntry must be related to the object
        if (!iteration.portfolioEntry.id.equals(id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        Form<IterationFormData> iterationForm = iterationFormTemplate.fill(new IterationFormData(iteration));

        // add the custom attributes values
        CustomAttributeFormAndDisplayHandler.fillWithValues(iterationForm, Iteration.class, iterationId);

        return ok(views.html.core.portfolioentrydelivery.iteration_edit.render(portfolioEntry, iterationForm,
                ReleaseDAO.getReleaseAsVHByPEAndType(id, models.delivery.ReleasePortfolioEntry.Type.BY_ITERATION)));

    }

    /**
     * Save an iteration.
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processEditIteration() {

        // bind the form
        Form<IterationFormData> boundForm = iterationFormTemplate.bindFromRequest();

        // get the portfolioEntry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        if (boundForm.hasErrors() || CustomAttributeFormAndDisplayHandler.validateValues(boundForm, Iteration.class)) {
            return ok(views.html.core.portfolioentrydelivery.iteration_edit.render(portfolioEntry, boundForm,
                    ReleaseDAO.getReleaseAsVHByPEAndType(id, models.delivery.ReleasePortfolioEntry.Type.BY_ITERATION)));
        }

        IterationFormData iterationFormData = boundForm.get();

        Iteration iteration = IterationDAO.getIterationById(iterationFormData.iterationId);

        // security: the portfolioEntry must be related to the object
        if (!iteration.portfolioEntry.id.equals(id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        iterationFormData.fill(iteration);

        iteration.update();

        // save the custom attributes
        CustomAttributeFormAndDisplayHandler.validateAndSaveValues(boundForm, Iteration.class, iteration.id);

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_delivery.iteration.edit.successful"));

        return redirect(controllers.core.routes.PortfolioEntryDeliveryController.iterations(id));

    }

    /**
     * List of assigned (to the portfolio entry) releases.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_DETAILS_DYNAMIC_PERMISSION)
    public Result releases(Long id) {

        // get the portfolio entry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        List<ReleasePortfolioEntryListView> releasePortfolioEntryListView = new ArrayList<ReleasePortfolioEntryListView>();
        for (ReleasePortfolioEntry releasePortfolioEntry : portfolioEntry.releasesPortfolioEntries) {
            Release release = releasePortfolioEntry.getRelease();
            if (release != null) {
                releasePortfolioEntryListView.add(new ReleasePortfolioEntryListView(release, id, releasePortfolioEntry.type));
            }
        }

        Set<String> columnsToHide = new HashSet<>();
        if (!getSecurityService().dynamic("PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION", "")) {
            columnsToHide.add("unassignActionLink");
        }

        Table<ReleasePortfolioEntryListView> releasesTable = ReleasePortfolioEntryListView.templateTable.fill(releasePortfolioEntryListView, columnsToHide);

        return ok(views.html.core.portfolioentrydelivery.releases.render(portfolioEntry, releasesTable));
    }

    /**
     * Display the form to assign a new release to the portfolio entry.
     * 
     * @param id
     *            the portfolio entry
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result assignRelease(Long id) {

        // get the portfolio entry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        Form<ReleasePortfolioEntryFormData> assignReleaseForm = assignReleaseFormTemplate.fill(new ReleasePortfolioEntryFormData());

        return ok(views.html.core.portfolioentrydelivery.release_assign.render(portfolioEntry, assignReleaseForm,
                getRequirementsRelationTypesAsValueHolderCollection()));
    }

    /**
     * Process the form to assign a release.
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processAssignRelease() {

        // bind the form
        Form<ReleasePortfolioEntryFormData> boundForm = assignReleaseFormTemplate.bindFromRequest();

        // get the portfolioEntry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        if (boundForm.hasErrors()) {
            return ok(views.html.core.portfolioentrydelivery.release_assign.render(portfolioEntry, boundForm,
                    getRequirementsRelationTypesAsValueHolderCollection()));
        }

        ReleasePortfolioEntryFormData releasePortfolioEntryFormData = boundForm.get();

        // check the release is not already assigned.
        if (PortfolioEntryDao.hasPEAssignedRelease(id, releasePortfolioEntryFormData.release)) {
            boundForm.reject("release", Msg.get("object.release.portfolio_entry.already_assigned"));
            return ok(views.html.core.portfolioentrydelivery.release_assign.render(portfolioEntry, boundForm,
                    getRequirementsRelationTypesAsValueHolderCollection()));
        }

        ReleasePortfolioEntry releasePortfolioEntry = releasePortfolioEntryFormData.get();
        releasePortfolioEntry.save();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_delivery.release_assign.successful"));

        return redirect(controllers.core.routes.PortfolioEntryDeliveryController.releases(portfolioEntry.id));

    }

    /**
     * Unassign a release.
     * 
     * @param id
     *            the portfolio entry id
     * @param releaseId
     *            the release id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result unassignRelease(Long id, Long releaseId) {

        ReleasePortfolioEntry association = ReleaseDAO.getReleaseByIdAndPE(releaseId, id);

        // remove existing relation
        switch (association.type) {
        case BY_ITERATION:
            for (Iteration iteration : IterationDAO.getIterationAllAsListByPEAndRelease(releaseId, id)) {
                iteration.release = null;
                iteration.save();
            }
            break;
        case BY_REQUIREMENT:
            for (Requirement requirement : RequirementDAO.getRequirementAsListByPEAndRelease(releaseId, id)) {
                requirement.release = null;
                requirement.save();
            }
            break;
        default:
            break;
        }

        association.delete();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_delivery.release_unassign.successful"));

        return redirect(controllers.core.routes.PortfolioEntryDeliveryController.releases(id));

    }

    /**
     * Get the requirements' relation types as a value holder collection.
     */
    private static ISelectableValueHolderCollection<String> getRequirementsRelationTypesAsValueHolderCollection() {
        ISelectableValueHolderCollection<String> types = new DefaultSelectableValueHolderCollection<String>();
        for (ReleasePortfolioEntry.Type type : ReleasePortfolioEntry.Type.values()) {
            types.add(new DefaultSelectableValueHolder<String>(type.name(), type.getLabel()));
        }
        return types;
    }

    /**
     * Get the messages service.
     */
    private II18nMessagesPlugin getMessagesPlugin() {
        return messagesPlugin;
    }

    /**
     * Get the security service.
     */
    private ISecurityService getSecurityService() {
        return securityService;
    }

    private Configuration getConfiguration() {
        return configuration;
    }

     /**
     * Get the user session manager service.
     */
    private IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return userSessionManagerPlugin;
    }

}
