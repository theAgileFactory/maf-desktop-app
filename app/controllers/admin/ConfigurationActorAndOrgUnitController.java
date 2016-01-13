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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import controllers.api.core.RootApiController;
import dao.pmo.ActorDao;
import dao.pmo.OrgUnitDao;
import dao.pmo.StakeholderDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import models.pmo.ActorType;
import models.pmo.Competency;
import models.pmo.OrgUnitType;
import models.pmo.StakeholderType;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import utils.form.ActorTypeFormData;
import utils.form.CompetencyFormData;
import utils.form.OrgUnitTypeFormData;
import utils.form.StakeholderTypeFormData;
import utils.table.ActorTypeListView;
import utils.table.CompetencyListView;
import utils.table.OrgUnitTypeListView;
import utils.table.StakeholderTypeListView;

/**
 * Manage the actors and org units reference data.
 * 
 * @author Johann Kohler
 * 
 */
@Restrict({ @Group(IMafConstants.ADMIN_CONFIGURATION_PERMISSION) })
public class ConfigurationActorAndOrgUnitController extends Controller {

    private static Form<ActorTypeFormData> actorTypeFormTemplate = Form.form(ActorTypeFormData.class);

    private static Form<OrgUnitTypeFormData> orgUnitTypeFormTemplate = Form.form(OrgUnitTypeFormData.class);

    private static Form<CompetencyFormData> competencyFormTemplate = Form.form(CompetencyFormData.class);

    private static Form<StakeholderTypeFormData> stakeholderTypeFormTemplate = Form.form(StakeholderTypeFormData.class);

    @Inject
    private II18nMessagesPlugin i18nMessagesPlugin;

    /**
     * Display the lists of data.
     */
    public Result list() {

        // actor types
        List<ActorType> actorTypes = ActorDao.getActorTypeAsList();

        List<ActorTypeListView> actorTypesListView = new ArrayList<ActorTypeListView>();
        for (ActorType actorType : actorTypes) {
            actorTypesListView.add(new ActorTypeListView(actorType));
        }

        Table<ActorTypeListView> actorTypesFilledTable = ActorTypeListView.templateTable.fill(actorTypesListView);

        // org unit type
        List<OrgUnitType> orgUnitTypes = OrgUnitDao.getOrgUnitTypeAsList();

        List<OrgUnitTypeListView> orgUnitTypesListView = new ArrayList<OrgUnitTypeListView>();
        for (OrgUnitType orgUnit : orgUnitTypes) {
            orgUnitTypesListView.add(new OrgUnitTypeListView(orgUnit));
        }

        Table<OrgUnitTypeListView> orgUnitTypesFilledTable = OrgUnitTypeListView.templateTable.fill(orgUnitTypesListView);

        // competency
        List<CompetencyListView> competenciesListView = new ArrayList<CompetencyListView>();
        for (Competency competency : ActorDao.getCompetencyAsList()) {
            competenciesListView.add(new CompetencyListView(competency));
        }
        Set<String> columnsForCompetencyToHide = new HashSet<>();
        columnsForCompetencyToHide.add("isDefault");
        Table<CompetencyListView> competenciesFilledTable = CompetencyListView.templateTable.fill(competenciesListView, columnsForCompetencyToHide);

        // stakeholder type
        List<StakeholderType> stakeholderTypes = StakeholderDao.getStakeholderTypeAsList();

        List<StakeholderTypeListView> stakeholderTypesListView = new ArrayList<StakeholderTypeListView>();
        for (StakeholderType stakeholderType : stakeholderTypes) {
            stakeholderTypesListView.add(new StakeholderTypeListView(stakeholderType));
        }

        Table<StakeholderTypeListView> stakeholderTypesFilledTable = StakeholderTypeListView.templateTable.fill(stakeholderTypesListView);

        return ok(views.html.admin.config.datareference.actorandorgunit.list.render(actorTypesFilledTable, orgUnitTypesFilledTable, competenciesFilledTable,
                stakeholderTypesFilledTable));
    }

    /**
     * Edit or create an actor type.
     * 
     * @param actorTypeId
     *            the actor type id (set 0 for create case)
     */
    public Result manageActorType(Long actorTypeId) {

        // initiate the form with the template
        Form<ActorTypeFormData> actorTypeForm = actorTypeFormTemplate;

        // edit case: inject values
        if (!actorTypeId.equals(Long.valueOf(0))) {

            ActorType actorType = ActorDao.getActorTypeById(actorTypeId);

            actorTypeForm = actorTypeFormTemplate.fill(new ActorTypeFormData(actorType, getI18nMessagesPlugin()));

        }

        return ok(views.html.admin.config.datareference.actorandorgunit.actor_type_manage.render(actorTypeForm));

    }

    /**
     * Process the edit/create form of an actor type.
     */
    public Result saveActorType() {

        // bind the form
        Form<ActorTypeFormData> boundForm = actorTypeFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.actorandorgunit.actor_type_manage.render(boundForm));
        }

        ActorTypeFormData actorTypeFormData = boundForm.get();

        // ref id should be unique
        ActorType testActorType = ActorDao.getActorTypeByRefId(actorTypeFormData.refId);
        if (testActorType != null) {
            if (actorTypeFormData.id != null) { // edit case
                if (!testActorType.id.equals(actorTypeFormData.id)) {
                    boundForm.reject("refId", Msg.get("object.actor_type.ref_id.invalid"));
                    return ok(views.html.admin.config.datareference.actorandorgunit.actor_type_manage.render(boundForm));
                }
            } else { // new case
                boundForm.reject("refId", Msg.get("object.actor_type.ref_id.invalid"));
                return ok(views.html.admin.config.datareference.actorandorgunit.actor_type_manage.render(boundForm));
            }
        }

        ActorType actorType = null;

        if (actorTypeFormData.id == null) { // create case

            actorType = new ActorType();

            actorTypeFormData.fill(actorType);
            actorType.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.actortype.add.successful"));

        } else { // edit case

            actorType = ActorDao.getActorTypeById(actorTypeFormData.id);

            actorTypeFormData.fill(actorType);
            actorType.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.actortype.edit.successful"));
        }

        actorTypeFormData.description.persist(getI18nMessagesPlugin());
        actorTypeFormData.name.persist(getI18nMessagesPlugin());

        RootApiController.flushFilters();

        return redirect(controllers.admin.routes.ConfigurationActorAndOrgUnitController.list());
    }

    /**
     * Delete an actor type.
     * 
     * @param actorTypeId
     *            the actor type id
     */
    public Result deleteActorType(Long actorTypeId) {
        ActorType actorType = ActorDao.getActorTypeById(actorTypeId);

        actorType.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.actortype.delete.successful"));

        RootApiController.flushFilters();

        return redirect(controllers.admin.routes.ConfigurationActorAndOrgUnitController.list());
    }

    /**
     * Edit or create an org unit type.
     * 
     * @param orgUnitTypeId
     *            the org unit type id (set 0 for create case)
     */
    public Result manageOrgUnitType(Long orgUnitTypeId) {

        // initiate the form with the template
        Form<OrgUnitTypeFormData> orgUnitTypeForm = orgUnitTypeFormTemplate;

        // edit case: inject values
        if (!orgUnitTypeId.equals(Long.valueOf(0))) {

            OrgUnitType orgUnitType = OrgUnitDao.getOrgUnitTypeById(orgUnitTypeId);

            orgUnitTypeForm = orgUnitTypeFormTemplate.fill(new OrgUnitTypeFormData(orgUnitType, getI18nMessagesPlugin()));

        }

        return ok(views.html.admin.config.datareference.actorandorgunit.org_unit_type_manage.render(orgUnitTypeForm));
    }

    /**
     * Process the edit/create form of an org unit type.
     */
    public Result saveOrgUnitType() {

        // bind the form
        Form<OrgUnitTypeFormData> boundForm = orgUnitTypeFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.actorandorgunit.org_unit_type_manage.render(boundForm));
        }

        OrgUnitTypeFormData orgUnitTypeFormData = boundForm.get();

        OrgUnitType orgUnitType = null;

        if (orgUnitTypeFormData.id == null) { // create case

            orgUnitType = new OrgUnitType();

            orgUnitTypeFormData.fill(orgUnitType);
            orgUnitType.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.orgunittype.add.successful"));

        } else { // edit case

            orgUnitType = OrgUnitDao.getOrgUnitTypeById(orgUnitTypeFormData.id);

            orgUnitTypeFormData.fill(orgUnitType);
            orgUnitType.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.orgunittype.edit.successful"));
        }

        orgUnitTypeFormData.description.persist(getI18nMessagesPlugin());
        orgUnitTypeFormData.name.persist(getI18nMessagesPlugin());

        RootApiController.flushFilters();

        return redirect(controllers.admin.routes.ConfigurationActorAndOrgUnitController.list());
    }

    /**
     * Delete an org unit type.
     * 
     * @param orgUnitTypeId
     *            the org unit type id
     */
    public Result deleteOrgUnitType(Long orgUnitTypeId) {
        OrgUnitType orgUnitType = OrgUnitDao.getOrgUnitTypeById(orgUnitTypeId);

        orgUnitType.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.orgunittype.delete.successful"));

        RootApiController.flushFilters();

        return redirect(controllers.admin.routes.ConfigurationActorAndOrgUnitController.list());
    }

    /**
     * Edit or create a competency.
     * 
     * @param competencyId
     *            the competency id (set 0 for create case)
     */
    public Result manageCompetency(Long competencyId) {

        // initiate the form with the template
        Form<CompetencyFormData> competencyForm = competencyFormTemplate;

        // edit case: inject values
        if (!competencyId.equals(Long.valueOf(0))) {

            Competency competency = ActorDao.getCompetencyById(competencyId);

            competencyForm = competencyFormTemplate.fill(new CompetencyFormData(competency, getI18nMessagesPlugin()));

        }

        return ok(views.html.admin.config.datareference.actorandorgunit.competency_manage.render(competencyForm));
    }

    /**
     * Process the edit/create form of a competency.
     */
    public Result processManageCompetency() {

        // bind the form
        Form<CompetencyFormData> boundForm = competencyFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.actorandorgunit.competency_manage.render(boundForm));
        }

        CompetencyFormData competencyFormData = boundForm.get();

        Competency competency = null;

        if (competencyFormData.id == null) { // create case

            competency = new Competency();

            competencyFormData.fill(competency);
            competency.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.competency.add.successful"));

        } else { // edit case

            competency = ActorDao.getCompetencyById(competencyFormData.id);

            competencyFormData.fill(competency);
            competency.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.competency.edit.successful"));
        }

        competencyFormData.description.persist(getI18nMessagesPlugin());
        competencyFormData.name.persist(getI18nMessagesPlugin());

        RootApiController.flushFilters();

        return redirect(controllers.admin.routes.ConfigurationActorAndOrgUnitController.list());
    }

    /**
     * Delete a competency.
     * 
     * @param competencyId
     *            the competency id
     */
    public Result deleteCompetency(Long competencyId) {

        Competency competency = ActorDao.getCompetencyById(competencyId);

        competency.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.competency.delete.successful"));

        RootApiController.flushFilters();

        return redirect(controllers.admin.routes.ConfigurationActorAndOrgUnitController.list());
    }

    /**
     * Edit or create a stakeholder type.
     * 
     * @param stakeholderTypeId
     *            the stakeholder type id (set 0 for create case)
     */
    public Result manageStakeholderType(Long stakeholderTypeId) {

        // initiate the form with the template
        Form<StakeholderTypeFormData> stakeholderTypeForm = stakeholderTypeFormTemplate;

        // edit case: inject values
        if (!stakeholderTypeId.equals(Long.valueOf(0))) {

            StakeholderType stakeholderType = StakeholderDao.getStakeholderTypeById(stakeholderTypeId);

            stakeholderTypeForm = stakeholderTypeFormTemplate.fill(new StakeholderTypeFormData(stakeholderType, getI18nMessagesPlugin()));

        }

        return ok(views.html.admin.config.datareference.actorandorgunit.stakeholder_type_manage.render(stakeholderTypeForm));
    }

    /**
     * Process the edit/create form of a stakeholder type.
     */
    public Result saveStakeholderType() {

        // bind the form
        Form<StakeholderTypeFormData> boundForm = stakeholderTypeFormTemplate.bindFromRequest();

        if (boundForm.hasErrors()) {
            return ok(views.html.admin.config.datareference.actorandorgunit.stakeholder_type_manage.render(boundForm));
        }

        StakeholderTypeFormData stakeholderTypeFormData = boundForm.get();

        StakeholderType stakeholderType = null;

        if (stakeholderTypeFormData.id == null) { // create case

            stakeholderType = new StakeholderType();

            stakeholderTypeFormData.fill(stakeholderType);
            stakeholderType.save();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.stakeholdertype.add.successful"));

        } else { // edit case

            stakeholderType = StakeholderDao.getStakeholderTypeById(stakeholderTypeFormData.id);

            stakeholderTypeFormData.fill(stakeholderType);
            stakeholderType.update();

            Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.stakeholdertype.edit.successful"));
        }

        stakeholderTypeFormData.description.persist(getI18nMessagesPlugin());
        stakeholderTypeFormData.name.persist(getI18nMessagesPlugin());
        stakeholderType.save();

        RootApiController.flushFilters();

        return redirect(controllers.admin.routes.ConfigurationActorAndOrgUnitController.list());
    }

    /**
     * Delete a stakeholder type.
     * 
     * @param stakeholderTypeId
     *            the stakeholder type id
     */
    public Result deleteStakeholderType(Long stakeholderTypeId) {

        StakeholderType stakeholderType = StakeholderDao.getStakeholderTypeById(stakeholderTypeId);

        stakeholderType.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("admin.configuration.reference_data.stakeholdertype.delete.successful"));

        RootApiController.flushFilters();

        return redirect(controllers.admin.routes.ConfigurationActorAndOrgUnitController.list());
    }

    /**
     * Get the i18n messages service.
     */
    private II18nMessagesPlugin getI18nMessagesPlugin() {
        return i18nMessagesPlugin;
    }

}
