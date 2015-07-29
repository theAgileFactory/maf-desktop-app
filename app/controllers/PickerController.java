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

import play.mvc.Controller;
import play.mvc.Result;
import utils.picker.ActorPicker;
import utils.picker.BudgetBucketPicker;
import utils.picker.CostCenterPicker;
import utils.picker.OrgUnitPicker;
import utils.picker.PlanningPackagePicker;
import utils.picker.PortfolioEntryTypePicker;
import utils.picker.PortfolioPicker;
import utils.picker.PortfolioTypePicker;
import utils.picker.PrincipalPicker;
import utils.picker.ReleasePicker;
import be.objectify.deadbolt.java.actions.SubjectPresent;

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
    public static Result manager() {
        return ActorPicker.pickerTemplate.handle(request());
    }

    /**
     * The owner picker.
     */
    public static Result owner() {
        return ActorPicker.pickerTemplate.handle(request());
    }

    /**
     * The actor picker.
     */
    public static Result actor() {
        return ActorPicker.pickerTemplate.handle(request());
    }

    /**
     * The actors without uid (null or empty).
     */
    public static Result actorWithoutUid() {
        return ActorPicker.pickerWithoutUidTemplate.handle(request());
    }

    /**
     * The actors that are direct stakeholders of a portfolio entry.
     * 
     * The portfolio entry id is given as a picker parameter.
     */
    public static Result actorOfPortfolioEntry() {
        return ActorPicker.pickerForPortfolioEntryTemplate.handle(request());
    }

    /**
     * The actors of an org unit.
     * 
     * The org unit id is given as a picker parameter.
     */
    public static Result actorOfOrgUnit() {
        return ActorPicker.pickerForOrgUnitTemplate.handle(request());
    }

    /**
     * The actors with a competency.
     * 
     * The competency id is given as a picker parameter.
     */
    public static Result actorWithCompetency() {
        return ActorPicker.pickerWithCompetencyTemplate.handle(request());
    }

    /**
     * The org unit picker.
     */
    public static Result orgUnit() {
        return OrgUnitPicker.pickerTemplate.handle(request());
    }

    /**
     * The sponsoring unit picker.
     */
    public static Result sponsoringUnit() {
        return OrgUnitPicker.canSponsorPickerTemplate.handle(request());
    }

    /**
     * The delivery units picker.
     */
    public static Result deliveryUnits() {
        return OrgUnitPicker.canDeliverPickerTemplate.handle(request());
    }

    /**
     * The delivery units that are delivery units of a portfolio entry.
     * 
     * The portfolio entry id is given as a picker parameter.
     */
    public static Result deliveryUnitsOfPortfolioEntry() {
        return OrgUnitPicker.canDeliverPickerForPortfolioEntryTemplate.handle(request());
    }

    /**
     * The cost center picker.
     */
    public static Result costCenter() {
        return CostCenterPicker.pickerTemplate.handle(request());
    }

    /**
     * The portfolio picker.
     */
    public static Result portfolio() {
        return PortfolioPicker.portfolioPickerHandler.handle(request());
    }

    /**
     * The budget bucket picker.
     */
    public static Result budgetBucket() {
        return BudgetBucketPicker.pickerTemplate.handle(request());
    }

    /**
     * The principals picker (multi and by uid).
     */
    public static Result principalsUid() {
        return PrincipalPicker.pickerTemplateUid.handle(request());
    }

    /**
     * The portfolio entry planning package of a portfolio entry.
     * 
     * The portfolio entry id is given as a picker parameter.
     */
    public static Result planningPackageOfPortfolioEntry() {
        return PlanningPackagePicker.pickerTemplate.handle(request());
    }

    /**
     * The portfolio type picker.
     */
    public static Result portfolioType() {
        return PortfolioTypePicker.pickerTemplate.handle(request());
    }

    /**
     * The portfolio entry type picker.
     */
    public static Result portfolioEntryType() {
        return PortfolioEntryTypePicker.pickerTemplate.handle(request());
    }

    /**
     * The active release picker.
     */
    public static Result activeRelease() {
        return ReleasePicker.allActiveTemplate.handle(request());
    }

}
