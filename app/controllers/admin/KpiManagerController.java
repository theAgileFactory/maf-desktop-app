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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.kpi.IKpiService;
import framework.services.kpi.Kpi;
import framework.services.kpi.Kpi.DataType;
import framework.services.system.ISysAdminUtils;
import framework.utils.Color;
import framework.utils.DefaultSelectableValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import models.framework_models.account.SystemPermission;
import models.framework_models.kpi.KpiColorRule;
import models.framework_models.kpi.KpiData;
import models.framework_models.kpi.KpiDefinition;
import models.framework_models.kpi.KpiValueDefinition;
import models.framework_models.kpi.KpiValueDefinition.RenderType;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import scala.concurrent.duration.Duration;
import services.tableprovider.ITableProvider;
import utils.form.*;
import utils.table.KpiColorRuleListView;
import utils.table.KpiDefinitionListView;
import utils.table.KpiValueDefinitionListView;

/**
 * The GUI for managing the KPIs.
 * 
 * @author Johann Kohler
 */
@Restrict({ @Group(IMafConstants.ADMIN_KPI_MANAGER_PERMISSION) })
public class KpiManagerController extends Controller {

    @Inject
    private IKpiService kpiService;
    @Inject
    private ISysAdminUtils sysAdminUtils;
    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;
    @Inject
    private ITableProvider tableProvider;

    public static Form<KpiDefinitionFormData> kpiDefinitionFormTemplate = Form.form(KpiDefinitionFormData.class);
    public static Form<KpiValueDefinitionFormData> standardKpiValueDefinitionFormTemplate = Form.form(KpiValueDefinitionFormData.class,
            KpiValueDefinitionFormData.StandardGroup.class);
    public static Form<KpiValueDefinitionFormData> customKpiValueDefinitionFormTemplate = Form.form(KpiValueDefinitionFormData.class,
            KpiValueDefinitionFormData.StandardGroup.class);
    public static Form<KpiSchedulerFormData> kpiSchedulerFormTemplate = Form.form(KpiSchedulerFormData.class);
    public static Form<KpiPermissionsFormData> kpiPermissionsFormTemplate = Form.form(KpiPermissionsFormData.class);
    public static Form<KpiColorRuleFormData> kpiColorRuleFormTemplate = Form.form(KpiColorRuleFormData.class);
    public static Form<CustomExternalKpiFormData> customExternalKpiFormTemplate = Form.form(CustomExternalKpiFormData.class);

    /**
     * List of all KPI definitions sorted by object type.
     */
    public Result index() {

        List<KpiDefinition> kpiDefinitions = KpiDefinition.getAll();

        Map<String, Table<KpiDefinitionListView>> tables = new HashMap<String, Table<KpiDefinitionListView>>();
        Map<String, List<KpiDefinitionListView>> kpiDefinitionListViews = new HashMap<String, List<KpiDefinitionListView>>();

        if (kpiDefinitions != null && kpiDefinitions.size() > 0) {

            for (KpiDefinition kpiDefinition : kpiDefinitions) {

                if (!kpiDefinitionListViews.containsKey(kpiDefinition.objectType)) {
                    kpiDefinitionListViews.put(kpiDefinition.objectType, new ArrayList<KpiDefinitionListView>());
                }

                kpiDefinitionListViews.get(kpiDefinition.objectType).add(new KpiDefinitionListView(kpiDefinition, getKpiService()));

            }

            for (Map.Entry<String, List<KpiDefinitionListView>> entry : kpiDefinitionListViews.entrySet()) {
                tables.put(entry.getKey(), this.getTableProvider().get().kpiDefinition.templateTable.fill(entry.getValue()));
            }

        }

        return ok(views.html.admin.kpi.index.render(tables));
    }

    /**
     * Display the details of a KPI definition.
     * 
     * @param kpiDefinitionId
     *            the KPI definition id
     */
    public Result view(Long kpiDefinitionId) {

        // get the KPI
        KpiDefinition kpiDefinition = KpiDefinition.getById(kpiDefinitionId);
        Kpi kpi = new Kpi(kpiDefinition, getKpiService());

        // create the table values
        List<KpiValueDefinitionListView> kpiValueDefinitionListView = new ArrayList<KpiValueDefinitionListView>();
        kpiValueDefinitionListView.add(new KpiValueDefinitionListView(kpiDefinition.mainKpiValueDefinition, DataType.MAIN));
        if (kpi.hasBoxDisplay()) {
            kpiValueDefinitionListView.add(new KpiValueDefinitionListView(kpiDefinition.additional1KpiValueDefinition, DataType.ADDITIONAL1));
            kpiValueDefinitionListView.add(new KpiValueDefinitionListView(kpiDefinition.additional2KpiValueDefinition, DataType.ADDITIONAL2));
        }
        Table<KpiValueDefinitionListView> valuesTable = this.getTableProvider().get().kpiValueDefinition.templateTable.fill(kpiValueDefinitionListView);

        // create the color rules table
        List<KpiColorRuleListView> kpiColorRuleListView = new ArrayList<KpiColorRuleListView>();
        for (KpiColorRule kpiColorRule : kpiDefinition.kpiColorRules) {
            kpiColorRuleListView.add(new KpiColorRuleListView(kpiColorRule, getI18nMessagesPlugin()));
        }
        Table<KpiColorRuleListView> rulesTable = this.getTableProvider().get().kpiColorRule.templateTable.fill(kpiColorRuleListView);

        return ok(views.html.admin.kpi.view.render(kpiDefinition, kpi, valuesTable, rulesTable));
    }

    /**
     * Change the order of a KPI definition comparing to the other KPIs with the
     * same object type.
     * 
     * @param kpiDefinitionId
     *            the KPI definition id
     * @param isDecrement
     *            if true then we decrement the order, else we increment it
     */
    public Result changeOrder(Long kpiDefinitionId, Boolean isDecrement) {

        KpiDefinition kpiDefinition = KpiDefinition.getById(kpiDefinitionId);

        KpiDefinition kpiDefinitionToReverse = null;
        if (isDecrement) {
            kpiDefinitionToReverse = KpiDefinition.getPrevious(kpiDefinition.objectType, kpiDefinition.order);
        } else {
            kpiDefinitionToReverse = KpiDefinition.getNext(kpiDefinition.objectType, kpiDefinition.order);
        }

        if (kpiDefinitionToReverse != null) {

            Integer newOrder = kpiDefinitionToReverse.order;

            kpiDefinitionToReverse.order = kpiDefinition.order;
            kpiDefinitionToReverse.save();

            kpiDefinition.order = newOrder;
            kpiDefinition.save();

        }

        return redirect(controllers.admin.routes.KpiManagerController.index());
    }

    /**
     * Form to edit the main data of a KPI definition.
     * 
     * @param kpiDefinitionId
     *            the KPI definition id
     */
    public Result edit(Long kpiDefinitionId) {

        // get the KPI
        KpiDefinition kpiDefinition = KpiDefinition.getById(kpiDefinitionId);
        Kpi kpi = new Kpi(kpiDefinition, getKpiService());

        Form<KpiDefinitionFormData> form = kpiDefinitionFormTemplate.fill(new KpiDefinitionFormData(kpiDefinition));

        return ok(views.html.admin.kpi.edit.render(kpiDefinition, kpi, form));
    }

    /**
     * Process the edit form of a KPI definition.
     * 
     * @return
     */
    public Result save() {

        // bind the form
        Form<KpiDefinitionFormData> boundForm = kpiDefinitionFormTemplate.bindFromRequest();

        // get the KPI
        Long kpiDefinitionId = Long.valueOf(boundForm.data().get("id"));
        KpiDefinition kpiDefinition = KpiDefinition.getById(kpiDefinitionId);
        Kpi kpi = new Kpi(kpiDefinition, getKpiService());

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.kpi.edit.render(kpiDefinition, kpi, boundForm));
        }

        KpiDefinitionFormData kpiDefinitionFormData = boundForm.get();

        kpiDefinitionFormData.fill(kpiDefinition);
        kpiDefinition.update();

        reloadKpiDefinition(kpiDefinition.uid);

        Utilities.sendSuccessFlashMessage(Msg.get("admin.kpi.edit.successful"));

        return redirect(controllers.admin.routes.KpiManagerController.view(kpiDefinition.id));
    }

    /**
     * Form to edit the main data of a KPI value definition.
     * 
     * @param kpiValueDefinitionId
     *            the KPI value definition
     * @param valueType
     *            the value type (main, additional1, additional2)
     */
    public Result editValue(Long kpiValueDefinitionId, String valueType) {

        // get the KPI value
        KpiValueDefinition kpiValueDefinition = KpiValueDefinition.getById(kpiValueDefinitionId);

        // get the KPI
        KpiDefinition kpiDefinition = KpiDefinition.getById(kpiValueDefinition.getKpiDefinition().id);
        Kpi kpi = new Kpi(kpiDefinition, getKpiService());

        Form<KpiValueDefinitionFormData> form = null;

        if (kpiDefinition.isStandard) {
            form = standardKpiValueDefinitionFormTemplate.fill(new KpiValueDefinitionFormData(kpiValueDefinition, getI18nMessagesPlugin()));
        } else {
            form = customKpiValueDefinitionFormTemplate.fill(new KpiValueDefinitionFormData(kpiValueDefinition, getI18nMessagesPlugin()));
        }

        return ok(views.html.admin.kpi.editValue.render(kpiDefinition, kpi, kpiValueDefinition, valueType, form, getRenderTypesAsValueHolderCollection()));
    }

    /**
     * Process the edit form of a KPI value definition.
     */
    public Result saveValue() {

        // get the KPI value
        Long kpiValueDefinitionId = Long.valueOf(request().body().asFormUrlEncoded().get("id")[0]);
        KpiValueDefinition kpiValueDefinition = KpiValueDefinition.getById(kpiValueDefinitionId);

        // get the KPI
        KpiDefinition kpiDefinition = KpiDefinition.getById(kpiValueDefinition.getKpiDefinition().id);
        Kpi kpi = new Kpi(kpiDefinition, getKpiService());

        // bind the form
        Form<KpiValueDefinitionFormData> boundForm = null;
        if (kpiDefinition.isStandard) {
            boundForm = standardKpiValueDefinitionFormTemplate.bindFromRequest();
        } else {
            boundForm = customKpiValueDefinitionFormTemplate.bindFromRequest();
        }

        // get the value type
        String valueType = boundForm.data().get("valueType");

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.kpi.editValue.render(kpiDefinition, kpi, kpiValueDefinition, valueType, boundForm,
                    getRenderTypesAsValueHolderCollection()));
        }

        KpiValueDefinitionFormData kpiValueDefinitionFormData = boundForm.get();

        if (kpiValueDefinitionFormData.renderType.equals(RenderType.PATTERN.name())
                && (kpiValueDefinitionFormData.renderPattern == null || kpiValueDefinitionFormData.renderPattern.equals(""))) {
            boundForm.reject("renderPattern", Msg.get("object.kpi_value_definition.render_pattern.invalid"));
            return ok(views.html.admin.kpi.editValue.render(kpiDefinition, kpi, kpiValueDefinition, valueType, boundForm,
                    getRenderTypesAsValueHolderCollection()));
        }

        kpiValueDefinitionFormData.fill(kpiValueDefinition, !kpiDefinition.isStandard);
        kpiValueDefinition.update();

        if (!kpiDefinition.isStandard) {
            kpiValueDefinitionFormData.name.persist(getI18nMessagesPlugin());
        }

        reloadKpiDefinition(kpiDefinition.uid);

        Utilities.sendSuccessFlashMessage(Msg.get("admin.kpi.value.edit.successful"));

        return redirect(controllers.admin.routes.KpiManagerController.view(kpiDefinition.id));
    }

    /**
     * Change the order of a KPI color rule comparing to the other rules of the
     * corresponding KPI.
     * 
     * @param kpiColorRuleId
     *            the KPI color rule id
     * @param isDecrement
     *            if true then we decrement the order, else we increment it
     */
    public Result changeRuleOrder(Long kpiColorRuleId, Boolean isDecrement) {

        KpiColorRule kpiColorRule = KpiColorRule.getById(kpiColorRuleId);

        KpiColorRule kpiColorRuleToReverse = null;
        if (isDecrement) {
            kpiColorRuleToReverse = KpiColorRule.getPrevious(kpiColorRule.kpiDefinition.id, kpiColorRule.order);
        } else {
            kpiColorRuleToReverse = KpiColorRule.getNext(kpiColorRule.kpiDefinition.id, kpiColorRule.order);
        }

        if (kpiColorRuleToReverse != null) {

            Integer newOrder = kpiColorRuleToReverse.order;

            kpiColorRuleToReverse.order = kpiColorRule.order;
            kpiColorRuleToReverse.save();

            kpiColorRule.order = newOrder;
            kpiColorRule.save();

            reloadKpiDefinition(kpiColorRule.kpiDefinition.uid);

        }

        return redirect(controllers.admin.routes.KpiManagerController.view(kpiColorRule.kpiDefinition.id));

    }

    /**
     * Form to manage (add or edit) a KPI color rule.
     * 
     * @param kpiDefinitionId
     *            the KPI definition id
     * @param kpiColorRuleId
     *            the KPI color rule id, set 0 for the add case
     */
    public Result manageRule(Long kpiDefinitionId, Long kpiColorRuleId) {

        // get the KPI
        KpiDefinition kpiDefinition = KpiDefinition.getById(kpiDefinitionId);
        Kpi kpi = new Kpi(kpiDefinition, getKpiService());

        // initiate the form with the template
        Form<KpiColorRuleFormData> form = kpiColorRuleFormTemplate;

        // edit case: inject values
        if (!kpiColorRuleId.equals(Long.valueOf(0))) {

            // get the KPI color rule
            KpiColorRule kpiColorRule = KpiColorRule.getById(kpiColorRuleId);

            form = kpiColorRuleFormTemplate.fill(new KpiColorRuleFormData(kpiColorRule, getI18nMessagesPlugin()));

        }

        return ok(views.html.admin.kpi.manageRule.render(kpiDefinition, kpi, form, Color.getColorsAsValueHolderCollection(getI18nMessagesPlugin())));
    }

    /**
     * Process the manage form of a KPI color rule.
     */
    public Result saveRule() {

        // bind the form
        Form<KpiColorRuleFormData> boundForm = kpiColorRuleFormTemplate.bindFromRequest();

        // get the KPI
        Long kpiDefinitionId = Long.valueOf(boundForm.data().get("kpiDefinitionId"));
        KpiDefinition kpiDefinition = KpiDefinition.getById(kpiDefinitionId);
        Kpi kpi = new Kpi(kpiDefinition, getKpiService());

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.kpi.manageRule.render(kpiDefinition, kpi, boundForm, Color.getColorsAsValueHolderCollection(getI18nMessagesPlugin())));
        }

        KpiColorRuleFormData kpiColorRuleFormData = boundForm.get();

        KpiColorRule kpiColorRule = null;

        if (kpiColorRuleFormData.kpiColorRuleId == null) { // create case

            kpiColorRule = new KpiColorRule();

            kpiColorRule.kpiDefinition = kpiDefinition;
            kpiColorRule.order = KpiColorRule.getLastOrder(kpiDefinition.id) + 1;

            kpiColorRuleFormData.fill(kpiColorRule);

            kpiColorRule.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.kpi.rule.add.successful"));

        } else { // edit case

            kpiColorRule = KpiColorRule.getById(kpiColorRuleFormData.kpiColorRuleId);

            kpiColorRuleFormData.fill(kpiColorRule);

            kpiColorRule.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.kpi.rule.edit.successful"));
        }

        kpiColorRuleFormData.renderLabel.persist(getI18nMessagesPlugin());

        reloadKpiDefinition(kpiDefinition.uid);

        return redirect(controllers.admin.routes.KpiManagerController.view(kpiDefinition.id));
    }

    /**
     * Delete a KPI color rule.
     * 
     * @param kpiColorRuleId
     *            the KPI color rule id
     */
    public Result deleteRule(Long kpiColorRuleId) {

        KpiColorRule kpiColorRule = KpiColorRule.getById(kpiColorRuleId);

        kpiColorRule.doDelete();

        reloadKpiDefinition(kpiColorRule.kpiDefinition.uid);

        Utilities.sendSuccessFlashMessage(Msg.get("admin.kpi.rule.delete"));

        return redirect(controllers.admin.routes.KpiManagerController.view(kpiColorRule.kpiDefinition.id));
    }

    /**
     * Delete the scheduler of a KPI definition.
     * 
     * @param kpiDefinitionId
     *            the KPI definition id
     */
    public Result deleteScheduler(Long kpiDefinitionId) {

        KpiDefinition kpiDefinition = KpiDefinition.getById(kpiDefinitionId);

        kpiDefinition.schedulerFrequency = null;
        kpiDefinition.schedulerRealTime = null;
        kpiDefinition.schedulerStartTime = null;
        kpiDefinition.save();

        reloadKpiDefinition(kpiDefinition.uid);

        Utilities.sendSuccessFlashMessage(Msg.get("admin.kpi.scheduler.delete"));

        return redirect(controllers.admin.routes.KpiManagerController.view(kpiDefinitionId));
    }

    /**
     * Form to edit the parameters of the scheduler of a KPI definition.
     * 
     * Possible only if the scheduler is active.
     * 
     * @param kpiDefinitionId
     *            the KPI definition id
     */
    public Result editScheduler(Long kpiDefinitionId) {

        // get the KPI
        KpiDefinition kpiDefinition = KpiDefinition.getById(kpiDefinitionId);
        Kpi kpi = new Kpi(kpiDefinition, getKpiService());

        Form<KpiSchedulerFormData> form = kpiSchedulerFormTemplate.fill(new KpiSchedulerFormData(kpiDefinition));

        return ok(views.html.admin.kpi.editScheduler.render(kpiDefinition, kpi, form));
    }

    /**
     * Process the edit form of the scheduler of a KPI definition.
     */
    public Result saveScheduler() {

        // bind the form
        Form<KpiSchedulerFormData> boundForm = kpiSchedulerFormTemplate.bindFromRequest();

        // get the KPI
        Long kpiDefinitionId = Long.valueOf(boundForm.data().get("id"));
        KpiDefinition kpiDefinition = KpiDefinition.getById(kpiDefinitionId);
        Kpi kpi = new Kpi(kpiDefinition, getKpiService());

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.kpi.editScheduler.render(kpiDefinition, kpi, boundForm));
        }

        KpiSchedulerFormData kpiSchedulerFormData = boundForm.get();

        kpiSchedulerFormData.fill(kpiDefinition);
        kpiDefinition.update();

        reloadKpiDefinition(kpiDefinition.uid);

        Utilities.sendSuccessFlashMessage(Msg.get("admin.kpi.editscheduler.successful"));

        return redirect(controllers.admin.routes.KpiManagerController.view(kpiDefinition.id));

    }

    /**
     * Trigger the scheduler of a KPI definition.
     * 
     * @param kpiDefinitionId
     *            the KPI definition id
     */
    public Result triggerScheduler(final Long kpiDefinitionId) {

        getSysAdminUtils().scheduleOnce(false, "KpiManager", Duration.create(0, TimeUnit.MILLISECONDS), new Runnable() {
            @Override
            public void run() {

                KpiDefinition kpiDefinition = KpiDefinition.getById(kpiDefinitionId);

                Kpi kpi = getKpiService().getKpi(kpiDefinition.uid);

                if (kpi != null && kpi.hasScheduler()) {
                    kpi.storeValues();
                }

            }
        });

        Utilities.sendSuccessFlashMessage(Msg.get("admin.kpi.scheduler.trigger"));

        return redirect(controllers.admin.routes.KpiManagerController.view(kpiDefinitionId));
    }

    public Result editPermissions(Long kpiDefinitionId) {

        // get the KPI
        KpiDefinition kpiDefinition = KpiDefinition.getById(kpiDefinitionId);
        Kpi kpi = new Kpi(kpiDefinition, getKpiService());

        Form<KpiPermissionsFormData> form = kpiPermissionsFormTemplate.fill(new KpiPermissionsFormData(kpiDefinition));

        return ok(views.html.admin.kpi.editPermissions.render(kpiDefinition, kpi, form, SystemPermission.getAllSelectableSystemPermissions()));
    }

    public Result savePermissions() {

        // bind the form
        Form<KpiPermissionsFormData> boundForm = kpiPermissionsFormTemplate.bindFromRequest();

        // get the KPI
        Long kpiDefinitionId = Long.valueOf(boundForm.data().get("id"));
        KpiDefinition kpiDefinition = KpiDefinition.getById(kpiDefinitionId);
        Kpi kpi = new Kpi(kpiDefinition, getKpiService());

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.kpi.editPermissions.render(kpiDefinition, kpi, boundForm, SystemPermission.getAllSelectableSystemPermissions()));
        }

        KpiPermissionsFormData kpiPermissionsFormData = boundForm.get();

        kpiPermissionsFormData.fill(kpiDefinition);
        kpiDefinition.update();

        reloadKpiDefinition(kpiDefinition.uid);

        Utilities.sendSuccessFlashMessage(Msg.get("admin.kpi.edit_permissions.successful"));

        return redirect(controllers.admin.routes.KpiManagerController.view(kpiDefinition.id));
    }

    /**
     * Form to create a new custom and external KPI.
     * 
     * @param objectType
     *            the object type
     */
    public Result create(String objectType) {
        return ok(views.html.admin.kpi.create.render(objectType, customExternalKpiFormTemplate));
    }

    /**
     * Process the form to create a new custom and external KPI.
     */
    public Result processCreate() {

        // bind the form
        Form<CustomExternalKpiFormData> boundForm = customExternalKpiFormTemplate.bindFromRequest();

        // get the object type
        String objectType = boundForm.data().get("objectType");

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.kpi.create.render(objectType, boundForm));
        }

        CustomExternalKpiFormData customExternalKpiFormData = boundForm.get();

        KpiDefinition kpiDefinition = customExternalKpiFormData.constructKpiDefinition();

        kpiDefinition.mainKpiValueDefinition.save();
        kpiDefinition.additional1KpiValueDefinition.save();
        kpiDefinition.additional2KpiValueDefinition.save();
        kpiDefinition.save();

        customExternalKpiFormData.mainName.persist(getI18nMessagesPlugin());
        customExternalKpiFormData.additional1Name.persist(getI18nMessagesPlugin());
        customExternalKpiFormData.additional2Name.persist(getI18nMessagesPlugin());

        reloadKpiDefinition(kpiDefinition.uid);

        Utilities.sendSuccessFlashMessage(Msg.get("admin.kpi.create.successful"));

        return redirect(controllers.admin.routes.KpiManagerController.view(kpiDefinition.id));
    }

    /**
     * Delete a custom KPI.
     * 
     * @param kpiDefinitionId
     *            the KPI definition id
     */
    public Result delete(Long kpiDefinitionId) {

        // get the KPI
        KpiDefinition kpiDefinition = KpiDefinition.getById(kpiDefinitionId);
        Kpi kpi = new Kpi(kpiDefinition, getKpiService());

        if (!kpiDefinition.isStandard) {

            // cancel the scheduler
            kpi.cancel();

            // delete the values and data
            deleteKpiValueDefinition(kpiDefinition.mainKpiValueDefinition);
            deleteKpiValueDefinition(kpiDefinition.additional1KpiValueDefinition);
            deleteKpiValueDefinition(kpiDefinition.additional2KpiValueDefinition);

            // delete the colors
            if (kpiDefinition.kpiColorRules != null) {
                for (KpiColorRule kpiColorRule : kpiDefinition.kpiColorRules) {
                    kpiColorRule.doDelete();
                }
            }

            kpiDefinition.deleted = true;
            kpiDefinition.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.kpi.delete.successful"));

        } else {
            Utilities.sendSuccessFlashMessage(Msg.get("admin.kpi.delete.error"));
        }

        return redirect(controllers.admin.routes.KpiManagerController.index());
    }

    /**
     * Delete a KPI value definition and its data.
     * 
     * @param kpiValueDefinition
     *            the KPI value definition to delete
     */
    private static void deleteKpiValueDefinition(KpiValueDefinition kpiValueDefinition) {
        if (kpiValueDefinition != null) {
            if (kpiValueDefinition.kpiDatas != null) {
                for (KpiData kpiData : kpiValueDefinition.kpiDatas) {
                    kpiData.delete();
                }
            }
            kpiValueDefinition.deleted = true;
            kpiValueDefinition.save();
        }
    }

    /**
     * Reload the KPI definition.
     * 
     * @param uid
     *            the KPI definition uid
     */
    private void reloadKpiDefinition(String uid) {
        this.getKpiService().reloadKpi(uid);
        this.getTableProvider().flushFilterConfig();
        this.getTableProvider().flushTables();
    }

    /**
     * Get the render types as a value holder collection.
     */
    private static ISelectableValueHolderCollection<String> getRenderTypesAsValueHolderCollection() {
        ISelectableValueHolderCollection<String> renderTypes = new DefaultSelectableValueHolderCollection<String>();
        for (RenderType renderType : RenderType.values()) {
            renderTypes.add(new DefaultSelectableValueHolder<String>(renderType.name(),
                    Msg.get("object.kpi_value_definition.render_type." + renderType.name() + ".label")));
        }
        return renderTypes;
    }

    /**
     * Get the KPI service.
     */
    private IKpiService getKpiService() {
        return kpiService;
    }

    /**
     * Get the system utils.
     */
    private ISysAdminUtils getSysAdminUtils() {
        return sysAdminUtils;
    }

    /**
     * Get the i18n messages service.
     */
    private II18nMessagesPlugin getI18nMessagesPlugin() {
        return i18nMessagesPlugin;
    }

    /**
     * Get the table provider.
     */
    private ITableProvider getTableProvider() {
        return this.tableProvider;
    }

}
