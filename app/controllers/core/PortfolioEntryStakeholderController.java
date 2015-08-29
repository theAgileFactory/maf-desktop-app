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

import be.objectify.deadbolt.java.actions.Dynamic;
import constants.IMafConstants;
import dao.pmo.PortfolioEntryDao;
import dao.pmo.StakeholderDao;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import models.pmo.PortfolioEntry;
import models.pmo.Stakeholder;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.CheckPortfolioEntryExists;
import security.ISecurityService;
import utils.form.StakeholderFormData;
import utils.table.PortfolioEntryStakeholderListView;
import utils.table.PortfolioStakeholderListView;

/**
 * The controller which allows to manage a stakeholder of a portfolio entry.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryStakeholderController extends Controller {
    @Inject
    private ISecurityService securityService;

    public static Form<StakeholderFormData> formTemplate = Form.form(StakeholderFormData.class);

    /**
     * Display the list of direct stakeholders of a portfolio entry and the list
     * stakeholder with their role of the portfolios of the portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_VIEW_DYNAMIC_PERMISSION)
    public Result index(Long id) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get stakeholders of portfolios
        List<Stakeholder> portfolioStakeholders = StakeholderDao.getStakeholderAsListByPortfolioOfPE(id);

        List<PortfolioStakeholderListView> stakeholdersForPortfolioListView = new ArrayList<PortfolioStakeholderListView>();
        for (Stakeholder stakeholder : portfolioStakeholders) {
            stakeholdersForPortfolioListView.add(new PortfolioStakeholderListView(stakeholder));
        }

        Set<String> hideColumnsForPortfolioStakeholders = new HashSet<String>();
        hideColumnsForPortfolioStakeholders.add("editActionLink");
        hideColumnsForPortfolioStakeholders.add("removeActionLink");

        Table<PortfolioStakeholderListView> filledPortfolioTable =
                PortfolioStakeholderListView.templateTable.fill(stakeholdersForPortfolioListView, hideColumnsForPortfolioStakeholders);

        // get the stakeholders
        List<Stakeholder> stakeholders = StakeholderDao.getStakeholderAsListByPE(id);

        List<PortfolioEntryStakeholderListView> stakeholdersListView = new ArrayList<PortfolioEntryStakeholderListView>();
        for (Stakeholder stakeholder : stakeholders) {
            stakeholdersListView.add(new PortfolioEntryStakeholderListView(stakeholder));
        }

        Set<String> hideColumnsForStakeholder = new HashSet<String>();
        if (!getSecurityService().dynamic("PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION", "")) {
            hideColumnsForStakeholder.add("editActionLink");
            hideColumnsForStakeholder.add("removeActionLink");
        }

        Table<PortfolioEntryStakeholderListView> filledTable =
                PortfolioEntryStakeholderListView.templateTable.fill(stakeholdersListView, hideColumnsForStakeholder);

        return ok(views.html.core.portfolioentrystakeholder.stakeholder_index.render(portfolioEntry, filledTable, filledPortfolioTable));
    }

    /**
     * Form to create/edit a direct stakeholder of a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     * @param stakeholderId
     *            the stakeholder id (set to 0 for create case)
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result manage(Long id, Long stakeholderId) {

        // get the portfolioEntry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // initiate the form with the template
        Form<StakeholderFormData> stakeholderForm = formTemplate;

        // edit case: inject values
        if (!stakeholderId.equals(Long.valueOf(0))) {
            Stakeholder stakeholder = StakeholderDao.getStakeholderById(stakeholderId);

            // security: the portfolioEntry must be related to the object
            if (!stakeholder.portfolioEntry.id.equals(id)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            stakeholderForm = formTemplate.fill(new StakeholderFormData(stakeholder));
        }

        return ok(views.html.core.portfolioentrystakeholder.stakeholder_manage.render(portfolioEntry, stakeholderForm));
    }

    /**
     * Process the creation/update of a stakeholder.
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processManage() {

        // bind the form
        Form<StakeholderFormData> boundForm = formTemplate.bindFromRequest();

        // get the portfolioEntry
        Long id = Long.valueOf(request().body().asFormUrlEncoded().get("id")[0]);
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        if (boundForm.hasErrors()) {
            return ok(views.html.core.portfolioentrystakeholder.stakeholder_manage.render(portfolioEntry, boundForm));
        }

        StakeholderFormData stakeholderFormData = boundForm.get();

        // check the unicity
        Stakeholder stakeholderForUnicity =
                StakeholderDao.getStakeholderByActorAndTypeAndPE(stakeholderFormData.actor, stakeholderFormData.stakeholderType, id);
        if (stakeholderForUnicity != null && stakeholderForUnicity.id != stakeholderFormData.stakeholderId) {
            boundForm.reject("stakeholderType", Msg.get("object.stakeholder.role.invalid"));
            return ok(views.html.core.portfolioentrystakeholder.stakeholder_manage.render(portfolioEntry, boundForm));
        }

        // create case
        if (stakeholderFormData.stakeholderId == null) {

            Stakeholder stakeholder = new Stakeholder();
            stakeholder.portfolioEntry = portfolioEntry;
            stakeholderFormData.fill(stakeholder);
            stakeholder.save();

            Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_stakeholder.add.successful"));

        } else { // edit case

            Stakeholder updStakeholder = StakeholderDao.getStakeholderById(stakeholderFormData.stakeholderId);

            // security: the portfolioEntry must be related to the object
            if (!updStakeholder.portfolioEntry.id.equals(id)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            stakeholderFormData.fill(updStakeholder);
            updStakeholder.update();

            Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_stakeholder.edit.successful"));
        }

        return redirect(controllers.core.routes.PortfolioEntryStakeholderController.index(stakeholderFormData.id));
    }

    /**
     * Delete a direct stakeholder of a portfolio entry.
     * 
     * @param id
     *            the portfolio entry id
     * @param stakeholderId
     *            the stakeholder id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(IMafConstants.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result delete(Long id, Long stakeholderId) {

        // get the stakeholder
        Stakeholder stakeholder = StakeholderDao.getStakeholderById(stakeholderId);

        // security: the portfolioEntry must be related to the object
        if (!stakeholder.portfolioEntry.id.equals(id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        // set the delete flag to true
        stakeholder.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_stakeholder.delete"));

        return redirect(controllers.core.routes.PortfolioEntryStakeholderController.index(id));
    }

    private ISecurityService getSecurityService() {
        return securityService;
    }

}
