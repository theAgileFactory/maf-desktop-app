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
package controllers.admin;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import controllers.api.core.RootApiController;
import dao.pmo.PortfolioEntryPlanningPackageDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.Color;
import framework.utils.CssValueForValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import models.pmo.PortfolioEntryPlanningPackageGroup;
import models.pmo.PortfolioEntryPlanningPackagePattern;
import models.pmo.PortfolioEntryPlanningPackageType;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import utils.form.PortfolioEntryPlanningPackageGroupFormData;
import utils.form.PortfolioEntryPlanningPackagePatternFormData;
import utils.form.PortfolioEntryPlanningPackageTypeFormData;
import utils.table.PortfolioEntryPlanningPackageGroupListView;
import utils.table.PortfolioEntryPlanningPackagePatternListView;
import utils.table.PortfolioEntryPlanningPackageTypeListView;

/**
 * Manage the planning packages reference data.
 * 
 * @author Johann Kohler
 * 
 */
@Restrict({ @Group(IMafConstants.ADMIN_CONFIGURATION_PERMISSION) })
public class ConfigurationPlanningPackageController extends Controller {

    private static Form<PortfolioEntryPlanningPackageGroupFormData> packageGroupFormTemplate = Form.form(PortfolioEntryPlanningPackageGroupFormData.class);
    private static Form<PortfolioEntryPlanningPackagePatternFormData> packagePatternFormTemplate = Form
            .form(PortfolioEntryPlanningPackagePatternFormData.class);
    private static Form<PortfolioEntryPlanningPackageTypeFormData> packageTypeFormTemplate = Form.form(PortfolioEntryPlanningPackageTypeFormData.class);

    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;

    /**
     * Display the list of package groups.
     */
    public Result list() {

        // groups

        List<PortfolioEntryPlanningPackageGroup> packageGroups = PortfolioEntryPlanningPackageDao.getPEPlanningPackageGroupAsList();

        List<PortfolioEntryPlanningPackageGroupListView> packageGroupListView = new ArrayList<PortfolioEntryPlanningPackageGroupListView>();
        for (PortfolioEntryPlanningPackageGroup packageGroup : packageGroups) {
            packageGroupListView.add(new PortfolioEntryPlanningPackageGroupListView(packageGroup));
        }

        Table<PortfolioEntryPlanningPackageGroupListView> packageGroupsTable = PortfolioEntryPlanningPackageGroupListView.templateTable
                .fill(packageGroupListView);

        // types
        List<PortfolioEntryPlanningPackageType> packagesTypes = PortfolioEntryPlanningPackageDao.getPEPlanningPackageTypeAsList();

        List<PortfolioEntryPlanningPackageTypeListView> packagesTypesListView = new ArrayList<PortfolioEntryPlanningPackageTypeListView>();
        for (PortfolioEntryPlanningPackageType packagesType : packagesTypes) {
            packagesTypesListView.add(new PortfolioEntryPlanningPackageTypeListView(packagesType, getI18nMessagesPlugin()));
        }

        Table<PortfolioEntryPlanningPackageTypeListView> packagesTypesTable = PortfolioEntryPlanningPackageTypeListView.templateTable
                .fill(packagesTypesListView);

        return ok(views.html.admin.config.datareference.planning_package.list.render(packageGroupsTable, packagesTypesTable));
    }

    /**
     * Display the patterns associated to a group.
     * 
     * @param packageGroupId
     *            the package group id
     */
    public Result viewPackageGroup(Long packageGroupId) {

        PortfolioEntryPlanningPackageGroup packageGroup = PortfolioEntryPlanningPackageDao.getPEPlanningPackageGroupById(packageGroupId);

        // patterns
        List<PortfolioEntryPlanningPackagePattern> packagePatterns = packageGroup.portfolioEntryPlanningPackagePatterns;

        List<PortfolioEntryPlanningPackagePatternListView> packagePatternListView = new ArrayList<PortfolioEntryPlanningPackagePatternListView>();
        for (PortfolioEntryPlanningPackagePattern packagePattern : packagePatterns) {
            packagePatternListView.add(new PortfolioEntryPlanningPackagePatternListView(packagePattern, getI18nMessagesPlugin()));
        }

        Table<PortfolioEntryPlanningPackagePatternListView> packagePatternsTable = PortfolioEntryPlanningPackagePatternListView.templateTable
                .fill(packagePatternListView);

        return ok(views.html.admin.config.datareference.planning_package.package_group_view.render(packageGroup, packagePatternsTable));

    }

    /**
     * Form to create/edit a package group.
     * 
     * @param packageGroupId
     *            the package group id (0 for create case)
     */
    public Result managePackageGroup(Long packageGroupId) {

        // initiate the form with the template
        Form<PortfolioEntryPlanningPackageGroupFormData> packageGroupForm = packageGroupFormTemplate;

        // edit case: inject values
        if (!packageGroupId.equals(Long.valueOf(0))) {

            PortfolioEntryPlanningPackageGroup packageGroup = PortfolioEntryPlanningPackageDao.getPEPlanningPackageGroupById(packageGroupId);

            packageGroupForm = packageGroupFormTemplate.fill(new PortfolioEntryPlanningPackageGroupFormData(packageGroup, getI18nMessagesPlugin()));

        }

        return ok(views.html.admin.config.datareference.planning_package.package_group_manage.render(packageGroupForm));
    }

    /**
     * Process the form to create/edit a package group.
     */
    public Result processManagePackageGroup() {

        // bind the form
        Form<PortfolioEntryPlanningPackageGroupFormData> boundForm = packageGroupFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.planning_package.package_group_manage.render(boundForm));
        }

        PortfolioEntryPlanningPackageGroupFormData packageGroupFormData = boundForm.get();

        PortfolioEntryPlanningPackageGroup packageGroup = null;

        if (packageGroupFormData.id == null) { // create case

            packageGroup = new PortfolioEntryPlanningPackageGroup();

            packageGroupFormData.fill(packageGroup);
            packageGroup.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.package_group.add.successful"));

        } else { // edit case

            packageGroup = PortfolioEntryPlanningPackageDao.getPEPlanningPackageGroupById(packageGroupFormData.id);

            packageGroupFormData.fill(packageGroup);
            packageGroup.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.package_group.edit.successful"));
        }

        packageGroupFormData.description.persist(getI18nMessagesPlugin());
        packageGroupFormData.name.persist(getI18nMessagesPlugin());

        RootApiController.flushFilters();

        return redirect(controllers.admin.routes.ConfigurationPlanningPackageController.list());

    }

    /**
     * Delete a package group.
     * 
     * @param packageGroupId
     *            the package group id
     */
    public Result deletePackageGroup(Long packageGroupId) {

        PortfolioEntryPlanningPackageGroup packageGroup = PortfolioEntryPlanningPackageDao.getPEPlanningPackageGroupById(packageGroupId);

        packageGroup.doDelete();
        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.package_group.delete.successful"));
        RootApiController.flushFilters();

        return redirect(controllers.admin.routes.ConfigurationPlanningPackageController.list());
    }

    /**
     * Edit or create a planning package type.
     * 
     * @param planningPackageTypeId
     *            the planning package type id (set 0 for create case)
     */
    public Result managePlanningPackageType(Long planningPackageTypeId) {

        Form<PortfolioEntryPlanningPackageTypeFormData> packageTypeForm = packageTypeFormTemplate;

        // edit case: inject values
        if (!planningPackageTypeId.equals(Long.valueOf(0))) {

            PortfolioEntryPlanningPackageType planningPackageType = PortfolioEntryPlanningPackageDao.getPEPlanningPackageTypeById(planningPackageTypeId);

            packageTypeForm = packageTypeFormTemplate.fill(new PortfolioEntryPlanningPackageTypeFormData(planningPackageType, getI18nMessagesPlugin()));

        }

        return ok(views.html.admin.config.datareference.planning_package.package_type_manage.render(packageTypeForm,
                Color.getColorsAsValueHolderCollection(getI18nMessagesPlugin())));

    }

    /**
     * Process the edit/create form of a planning package type.
     */
    public Result processManagePlanningPackageType() {

        // bind the form
        Form<PortfolioEntryPlanningPackageTypeFormData> boundForm = packageTypeFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.planning_package.package_type_manage.render(boundForm,
                    Color.getColorsAsValueHolderCollection(getI18nMessagesPlugin())));
        }

        PortfolioEntryPlanningPackageTypeFormData planningPackageTypeFormData = boundForm.get();

        PortfolioEntryPlanningPackageType packageType = null;

        if (planningPackageTypeFormData.id == null) { // create case

            packageType = new PortfolioEntryPlanningPackageType();

            planningPackageTypeFormData.fill(packageType);
            packageType.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.package_type.add.successful"));

        } else { // edit case

            packageType = PortfolioEntryPlanningPackageDao.getPEPlanningPackageTypeById(planningPackageTypeFormData.id);

            planningPackageTypeFormData.fill(packageType);
            packageType.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.package_type.edit.successful"));
        }

        planningPackageTypeFormData.name.persist(getI18nMessagesPlugin());

        RootApiController.flushFilters();

        return redirect(controllers.admin.routes.ConfigurationPlanningPackageController.list());
    }

    /**
     * Delete a planning package type.
     * 
     * @param planningPackageTypeId
     *            the planning package type id
     */
    public Result deletePlanningPackageType(Long planningPackageTypeId) {

        PortfolioEntryPlanningPackageType packageType = PortfolioEntryPlanningPackageDao.getPEPlanningPackageTypeById(planningPackageTypeId);

        packageType.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.package_type.delete.successful"));

        RootApiController.flushFilters();

        return redirect(controllers.admin.routes.ConfigurationPlanningPackageController.list());
    }

    /**
     * Change the order of a package pattern.
     * 
     * @param packagePatternId
     *            the package pattern id
     * @param isDecrement
     *            set to true to decrease the order, to false to increase it
     */
    public Result changePackagePatternOrder(Long packagePatternId, Boolean isDecrement) {

        PortfolioEntryPlanningPackagePattern packagePattern = PortfolioEntryPlanningPackageDao.getPEPlanningPackagePatternById(packagePatternId);

        PortfolioEntryPlanningPackagePattern packagePatternToReverse = null;
        if (isDecrement) {
            packagePatternToReverse = PortfolioEntryPlanningPackageDao
                    .getPEPlanningPackagePatternPreviousByGroup(packagePattern.portfolioEntryPlanningPackageGroup.id, packagePattern.order);
        } else {
            packagePatternToReverse = PortfolioEntryPlanningPackageDao
                    .getPEPlanningPackagePatternNextByGroup(packagePattern.portfolioEntryPlanningPackageGroup.id, packagePattern.order);
        }

        if (packagePatternToReverse != null) {

            Integer newOrder = packagePatternToReverse.order;

            packagePatternToReverse.order = packagePattern.order;
            packagePatternToReverse.save();

            packagePattern.order = newOrder;
            packagePattern.save();

        }

        return redirect(
                controllers.admin.routes.ConfigurationPlanningPackageController.viewPackageGroup(packagePattern.portfolioEntryPlanningPackageGroup.id));

    }

    /**
     * Form to create/edit a package pattern.
     * 
     * @param packageGroupId
     *            the package group id
     * @param packagePatternId
     *            the package pattern id
     */
    public Result managePackagePattern(Long packageGroupId, Long packagePatternId) {

        // get the package group
        PortfolioEntryPlanningPackageGroup packageGroup = PortfolioEntryPlanningPackageDao.getPEPlanningPackageGroupById(packageGroupId);

        // initiate the form with the template
        Form<PortfolioEntryPlanningPackagePatternFormData> packagePatternForm = packagePatternFormTemplate;

        // edit case: inject values
        if (!packagePatternId.equals(Long.valueOf(0))) {

            PortfolioEntryPlanningPackagePattern packagePattern = PortfolioEntryPlanningPackageDao.getPEPlanningPackagePatternById(packagePatternId);

            packagePatternForm = packagePatternFormTemplate.fill(new PortfolioEntryPlanningPackagePatternFormData(packagePattern));

        }

        DefaultSelectableValueHolderCollection<CssValueForValueHolder> selectablePortfolioEntryPlanningPackageTypes = PortfolioEntryPlanningPackageDao
                .getPEPlanningPackageTypeActiveAsCssVH();

        return ok(views.html.admin.config.datareference.planning_package.package_pattern_manage.render(packageGroup, packagePatternForm,
                selectablePortfolioEntryPlanningPackageTypes));
    }

    /**
     * Process the form to create/edit a package pattern.
     */
    public Result processManagePackagePattern() {

        // bind the form
        Form<PortfolioEntryPlanningPackagePatternFormData> boundForm = packagePatternFormTemplate.bindFromRequest();

        // get the package group
        Long packageGroupId = Long.valueOf(request().body().asFormUrlEncoded().get("packageGroupId")[0]);
        PortfolioEntryPlanningPackageGroup packageGroup = PortfolioEntryPlanningPackageDao.getPEPlanningPackageGroupById(packageGroupId);

        if (boundForm.hasErrors()) {

            DefaultSelectableValueHolderCollection<CssValueForValueHolder> selectablePortfolioEntryPlanningPackageTypes = PortfolioEntryPlanningPackageDao
                    .getPEPlanningPackageTypeActiveAsCssVH();

            return ok(views.html.admin.config.datareference.planning_package.package_pattern_manage.render(packageGroup, boundForm,
                    selectablePortfolioEntryPlanningPackageTypes));
        }

        PortfolioEntryPlanningPackagePatternFormData packagePatternFormData = boundForm.get();

        PortfolioEntryPlanningPackagePattern packagePattern = null;

        if (packagePatternFormData.id == null) { // create case

            packagePattern = new PortfolioEntryPlanningPackagePattern();
            packagePattern.portfolioEntryPlanningPackageGroup = packageGroup;
            packagePattern.order = PortfolioEntryPlanningPackageDao.getPEPlanningPackagePatternAsLastOrderByGroup(packageGroup.id) + 1;

            packagePatternFormData.fill(packagePattern);
            packagePattern.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.package_pattern.add.successful"));

        } else { // edit case

            packagePattern = PortfolioEntryPlanningPackageDao.getPEPlanningPackagePatternById(packagePatternFormData.id);

            packagePatternFormData.fill(packagePattern);
            packagePattern.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.package_pattern.edit.successful"));
        }

        RootApiController.flushFilters();

        return redirect(controllers.admin.routes.ConfigurationPlanningPackageController.viewPackageGroup(packageGroup.id));
    }

    /**
     * Delete a package pattern.
     * 
     * @param packagePatternId
     *            the package pattern id
     */
    public Result deletePackagePattern(Long packagePatternId) {

        PortfolioEntryPlanningPackagePattern packagePattern = PortfolioEntryPlanningPackageDao.getPEPlanningPackagePatternById(packagePatternId);

        packagePattern.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.package_pattern.delete.successful"));

        RootApiController.flushFilters();

        return redirect(
                controllers.admin.routes.ConfigurationPlanningPackageController.viewPackageGroup(packagePattern.portfolioEntryPlanningPackageGroup.id));
    }

    private II18nMessagesPlugin getI18nMessagesPlugin() {
        return i18nMessagesPlugin;
    }

}
