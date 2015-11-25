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
package controllers;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import play.mvc.Controller;
import play.mvc.Result;
import utils.picker.ActorPicker;
import utils.picker.BudgetBucketPicker;
import utils.picker.CostCenterPicker;
import utils.picker.DeliverablePicker;
import utils.picker.OrgUnitPicker;
import utils.picker.PlanningPackagePicker;
import utils.picker.PortfolioEntryTypePicker;
import utils.picker.PortfolioPicker;
import utils.picker.PortfolioTypePicker;
import utils.picker.PrincipalPicker;

/**
 * The controller that provides the pickers.
 * 
 * @author Johann Kohler
 */
@SubjectPresent
public class PickerController extends Controller {

    /**
     * The manager picker.
     */
    public Result manager() {
        return ActorPicker.pickerTemplate.handle(request());
    }

    /**
     * The owner picker.
     */
    public Result owner() {
        return ActorPicker.pickerTemplate.handle(request());
    }

    /**
     * The actor picker.
     */
    public Result actor() {
        return ActorPicker.pickerTemplate.handle(request());
    }

    /**
     * The actors without uid (null or empty).
     */
    public Result actorWithoutUid() {
        return ActorPicker.pickerWithoutUidTemplate.handle(request());
    }

    /**
     * The actors that are direct stakeholders of a portfolio entry.
     * 
     * The portfolio entry id is given as a picker parameter.
     */
    public Result actorOfPortfolioEntry() {
        return ActorPicker.pickerForPortfolioEntryTemplate.handle(request());
    }

    /**
     * The actors of an org unit.
     * 
     * The org unit id is given as a picker parameter.
     */
    public Result actorOfOrgUnit() {
        return ActorPicker.pickerForOrgUnitTemplate.handle(request());
    }

    /**
     * The actors with a competency.
     * 
     * The competency id is given as a picker parameter.
     */
    public Result actorWithCompetency() {
        return ActorPicker.pickerWithCompetencyTemplate.handle(request());
    }

    /**
     * The org unit picker.
     */
    public Result orgUnit() {
        return OrgUnitPicker.pickerTemplate.handle(request());
    }

    /**
     * The sponsoring unit picker.
     */
    public Result sponsoringUnit() {
        return OrgUnitPicker.canSponsorPickerTemplate.handle(request());
    }

    /**
     * The delivery units picker.
     */
    public Result deliveryUnits() {
        return OrgUnitPicker.canDeliverPickerTemplate.handle(request());
    }

    /**
     * The delivery units that are delivery units of a portfolio entry.
     * 
     * The portfolio entry id is given as a picker parameter.
     */
    public Result deliveryUnitsOfPortfolioEntry() {
        return OrgUnitPicker.canDeliverPickerForPortfolioEntryTemplate.handle(request());
    }

    /**
     * The cost center picker.
     */
    public Result costCenter() {
        return CostCenterPicker.pickerTemplate.handle(request());
    }

    /**
     * The portfolio picker.
     */
    public Result portfolio() {
        return PortfolioPicker.portfolioPickerHandler.handle(request());
    }

    /**
     * The budget bucket picker.
     */
    public Result budgetBucket() {
        return BudgetBucketPicker.pickerTemplate.handle(request());
    }

    /**
     * The principals picker (multi and by uid).
     */
    public Result principalsUid() {
        return PrincipalPicker.pickerTemplateUid.handle(request());
    }

    /**
     * The portfolio entry planning package of a portfolio entry.
     * 
     * The portfolio entry id is given as a picker parameter.
     */
    public Result planningPackageOfPortfolioEntry() {
        return PlanningPackagePicker.pickerTemplate.handle(request());
    }

    /**
     * The deliverables of a portfolio entry.
     * 
     * The portfolio entry id is given as a picker parameter.
     */
    public Result deliverableOfPortfolioEntry() {
        return DeliverablePicker.pickerTemplate.handle(request());
    }

    /**
     * The portfolio type picker.
     */
    public Result portfolioType() {
        return PortfolioTypePicker.pickerTemplate.handle(request());
    }

    /**
     * The portfolio entry type picker.
     */
    public Result portfolioEntryType() {
        return PortfolioEntryTypePicker.pickerTemplate.handle(request());
    }

}
