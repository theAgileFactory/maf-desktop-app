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

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import constants.IMafConstants;
import dao.pmo.PortfolioDao;
import dao.pmo.StakeholderDao;
import framework.utils.Msg;
import framework.utils.Utilities;
import models.pmo.Portfolio;
import models.pmo.Stakeholder;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import utils.form.StakeholderFormData;

/**
 * The controller which allows to manage the stakeholders of a portfolio.
 * 
 * @author Johann Kohler
 */
@SubjectPresent(forceBeforeAuthCheck = true)
public class PortfolioStakeholderController extends Controller {

    public static Form<StakeholderFormData> formTemplate = Form.form(StakeholderFormData.class);

    /**
     * Form to create/edit a direct stakeholder of a portfolio.
     * 
     * @param id
     *            the portfolio id
     * @param stakeholderId
     *            the stakeholder id (set to 0 for create case)
     */
    @Dynamic(IMafConstants.PORTFOLIO_EDIT_DYNAMIC_PERMISSION)
    public Result manage(Long id, Long stakeholderId) {

        // get the portfolio
        Portfolio portfolio = PortfolioDao.getPortfolioById(id);

        // initiate the form with the template
        Form<StakeholderFormData> stakeholderForm = formTemplate;

        // edit case: inject values
        if (!stakeholderId.equals(Long.valueOf(0))) {
            Stakeholder stakeholder = StakeholderDao.getStakeholderById(stakeholderId);

            // security: the portfolio must be related to the object
            if (!stakeholder.portfolio.id.equals(id)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            stakeholderForm = formTemplate.fill(new StakeholderFormData(stakeholder));
        }

        return ok(views.html.core.portfoliostakeholder.portfolio_stakeholder_manage.render(portfolio, stakeholderForm));
    }

    /**
     * Perform the save for a new/update portfolio stakeholder.
     */
    @Dynamic(IMafConstants.PORTFOLIO_EDIT_DYNAMIC_PERMISSION)
    public Result save() {

        // bind the form
        Form<StakeholderFormData> boundForm = formTemplate.bindFromRequest();

        // get the portfolio
        Long id = Long.valueOf(boundForm.data().get("id"));
        Portfolio portfolio = PortfolioDao.getPortfolioById(id);

        if (boundForm.hasErrors()) {
            return ok(views.html.core.portfoliostakeholder.portfolio_stakeholder_manage.render(portfolio, boundForm));
        }

        StakeholderFormData stakeholderFormData = boundForm.get();

        // check the unicity
        Stakeholder stakeholderForUnicity = StakeholderDao.getStakeholderByActorAndTypeAndPortfolio(stakeholderFormData.actor,
                stakeholderFormData.stakeholderType, id);
        if (stakeholderForUnicity != null && stakeholderForUnicity.id != stakeholderFormData.stakeholderId) {
            boundForm.reject("stakeholderType", Msg.get("object.stakeholder.role.invalid"));
            return ok(views.html.core.portfoliostakeholder.portfolio_stakeholder_manage.render(portfolio, boundForm));
        }

        // create case
        if (stakeholderFormData.stakeholderId == null) {

            Stakeholder stakeholder = new Stakeholder();
            stakeholder.portfolio = portfolio;
            stakeholderFormData.fill(stakeholder);
            stakeholder.save();

            Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_stakeholder.add.successful"));

        } else { // edit case

            Stakeholder updStakeholder = StakeholderDao.getStakeholderById(stakeholderFormData.stakeholderId);

            // security: the portfolio must be related to the object
            if (!updStakeholder.portfolio.id.equals(id)) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            stakeholderFormData.fill(updStakeholder);
            updStakeholder.update();

            Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_stakeholder.edit.successful"));
        }

        return redirect(controllers.core.routes.PortfolioController.view(stakeholderFormData.id, 0, 0, false));
    }

    /**
     * Delete a stakeholder of a portfolio.
     * 
     * @param id
     *            the portfolio id
     * @param stakeholderId
     *            the stakeholder id
     */
    @Dynamic(IMafConstants.PORTFOLIO_EDIT_DYNAMIC_PERMISSION)
    public Result delete(Long id, Long stakeholderId) {

        // get the stakeholder
        Stakeholder stakeholder = StakeholderDao.getStakeholderById(stakeholderId);

        // security: the portfolio must be related to the object
        if (!stakeholder.portfolio.id.equals(id)) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        // set the delete flag to true
        stakeholder.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_stakeholder.delete"));

        return redirect(controllers.core.routes.PortfolioController.view(id, 0, 0, false));
    }
}
