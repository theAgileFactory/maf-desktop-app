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
package utils.datatable;

import com.fasterxml.jackson.annotation.JsonInclude;
import controllers.core.routes;
import models.pmo.PortfolioEntry;
import utils.datatable.common.*;
import utils.datatable.common.Date;

import java.util.*;

/**
 * @author Guillaume Petit
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class PortfolioEntryDTO extends AbstractBizDockDTO {

    public PortfolioEntryDTO() {
        this.deliveryUnits = new ArrayList<>();
        this.dependencies = new ArrayList<>();
        this.portfolios = new ArrayList<>();
        this.stakeholders = new ArrayList<>();
    }

    public String governanceId;

    public Date creationDate;

    public boolean isPublic;

    public String name;

    public String portfolioEntryType;

    public ActorLink manager;

    public OrgUnitLink sponsoringUnit;

    public List<OrgUnitLink> deliveryUnits;

    public List<PortfolioLink> portfolios;

    public String lifeCycleProcess;

    public PortfolioEntryReportLink portfolioEntryStatus;

    public Date lastPEReportDate;

    public LifeCycleMilestoneInstanceLink lastMilestone;

    public PlannedLifeCycleMilestoneInstanceLink nextMilestone;

    public Date lastMilestoneDate;

    public Date nextMilestoneDate;

    public boolean isConcept;

    public boolean archived;

    public List<ActorLink> stakeholders;

    public List<PortfolioEntryLink> dependencies;

    public Date startDate;

    public Date endDate;

    public PortfolioEntryDTO(PortfolioEntry portfolioEntry) {
        this.deliveryUnits = new ArrayList<>();
        this.dependencies = new ArrayList<>();
        this.portfolios = new ArrayList<>();
        this.stakeholders = new ArrayList<>();

        this.id = portfolioEntry.id;
        this.link = routes.PortfolioEntryController.overview(this.id).url();
        this.governanceId = portfolioEntry.governanceId;
        this.creationDate = new Date(portfolioEntry.creationDate);
        this.isPublic = portfolioEntry.isPublic;
        this.name = portfolioEntry.name;
        this.portfolioEntryType = portfolioEntry.portfolioEntryType != null ? portfolioEntry.portfolioEntryType.getName() : "";
        this.manager = new ActorLink(portfolioEntry.manager);
        this.sponsoringUnit = new OrgUnitLink(portfolioEntry.sponsoringUnit);
        if (portfolioEntry.deliveryUnits != null) {
            portfolioEntry.deliveryUnits.forEach(deliveryUnit -> this.deliveryUnits.add(new OrgUnitLink(deliveryUnit)));
        }
        if (portfolioEntry.portfolios != null) {
            portfolioEntry.portfolios.forEach(portfolio -> this.portfolios.add(new PortfolioLink(portfolio)));
        }
        this.lifeCycleProcess = portfolioEntry.activeLifeCycleInstance != null ? portfolioEntry.activeLifeCycleInstance.lifeCycleProcess.name : "";
        this.portfolioEntryStatus = new PortfolioEntryReportLink(portfolioEntry.lastPortfolioEntryReport);
        if (portfolioEntry.lastPortfolioEntryReport != null) {
            this.lastPEReportDate = new Date(portfolioEntry.lastPortfolioEntryReport.creationDate);
        }
        this.lastMilestone = new LifeCycleMilestoneInstanceLink(portfolioEntry.lastApprovedLifeCycleMilestoneInstance);
        this.nextMilestone = new PlannedLifeCycleMilestoneInstanceLink(portfolioEntry.nextPlannedLifeCycleMilestoneInstance);
        if (portfolioEntry.lastApprovedLifeCycleMilestoneInstance != null) {
            this.lastMilestoneDate =  new Date(portfolioEntry.lastApprovedLifeCycleMilestoneInstance.passedDate);
        }
        if (portfolioEntry.nextPlannedLifeCycleMilestoneInstance != null) {
            this.nextMilestoneDate = new Date(portfolioEntry.nextPlannedLifeCycleMilestoneInstance.plannedDate);
        }
        this.isConcept = portfolioEntry.activeLifeCycleInstance.isConcept;
        this.archived = portfolioEntry.archived;
        if (portfolioEntry.stakeholders != null) {
            portfolioEntry.stakeholders.forEach(stakeholder -> this.stakeholders.add(new ActorLink(stakeholder.actor)));
        }
        if (portfolioEntry.destinationDependencies != null) {
            portfolioEntry.destinationDependencies.forEach(dependency -> this.dependencies.add(new PortfolioEntryLink(dependency.getSourcePortfolioEntry())));
        }
        this.startDate = new Date(portfolioEntry.startDate);
        this.endDate = new Date(portfolioEntry.endDate);
    }
}