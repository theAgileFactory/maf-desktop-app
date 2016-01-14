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

import javax.inject.Inject;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import play.mvc.Controller;
import play.mvc.Result;
import services.picker.IPickerService;

/**
 * The controller that provides the pickers.
 * 
 * @author Johann Kohler
 */
@SubjectPresent
public class PickerController extends Controller {

    @Inject
    private IPickerService pickerService;

    /**
     * The manager picker.
     */
    public Result manager() {
        return this.getPickerService().getActor().handle(request());
    }

    /**
     * The owner picker.
     */
    public Result owner() {
        return this.getPickerService().getActor().handle(request());
    }

    /**
     * The actor picker.
     */
    public Result actor() {
        return this.getPickerService().getActor().handle(request());
    }

    /**
     * The actors without uid (null or empty).
     */
    public Result actorWithoutUid() {
        return this.getPickerService().getActorWithoutUid().handle(request());
    }

    /**
     * The actors of an org unit.
     * 
     * The org unit id is given as a picker parameter.
     */
    public Result actorOfOrgUnit() {
        return this.getPickerService().getActorByOrgUnit().handle(request());
    }

    /**
     * The actors with a competency.
     * 
     * The competency id is given as a picker parameter.
     */
    public Result actorWithCompetency() {
        return this.getPickerService().getActorByCompetency().handle(request());
    }

    /**
     * The org unit picker.
     */
    public Result orgUnit() {
        return this.getPickerService().getOrgUnit().handle(request());
    }

    /**
     * The sponsoring unit picker.
     */
    public Result sponsoringUnit() {
        return this.getPickerService().getSponsoringUnit().handle(request());
    }

    /**
     * The delivery units picker.
     */
    public Result deliveryUnits() {
        return this.getPickerService().getDeliveryUnit().handle(request());
    }

    /**
     * The cost center picker.
     */
    public Result costCenter() {
        return this.getPickerService().getCostCenter().handle(request());
    }

    /**
     * The portfolio picker.
     */
    public Result portfolio() {
        return this.getPickerService().getPortfolio().handle(request());
    }

    /**
     * The budget bucket picker.
     */
    public Result budgetBucket() {
        return this.getPickerService().getBudgetBucket().handle(request());
    }

    /**
     * The principals picker (multi and by uid).
     */
    public Result principalsUid() {
        return this.getPickerService().getPrincipal().handle(request());
    }

    /**
     * The portfolio entry planning package of a portfolio entry.
     * 
     * The portfolio entry id is given as a picker parameter.
     */
    public Result planningPackageOfPortfolioEntry() {
        return this.getPickerService().getPlanningPackage().handle(request());
    }

    /**
     * The deliverables of a portfolio entry.
     * 
     * The portfolio entry id is given as a picker parameter.
     */
    public Result deliverableOfPortfolioEntry() {
        return this.getPickerService().getDeliverableByPortfolioEntry().handle(request());
    }

    /**
     * The portfolio type picker.
     */
    public Result portfolioType() {
        return this.getPickerService().getPortfolioType().handle(request());
    }

    /**
     * The portfolio entry type picker.
     */
    public Result portfolioEntryType() {
        return this.getPickerService().getPortfolioEntryType().handle(request());
    }

    /**
     * Get the picker service.
     */
    private IPickerService getPickerService() {
        return this.pickerService;
    }

}
