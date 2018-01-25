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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import models.pmo.PortfolioEntry;
import utils.datatable.common.*;

import java.util.Date;
import java.util.List;

/**
 * @author Guillaume Petit
 */
@JsonAutoDetect()
@JsonRootName("data")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class PortfolioEntryDTO extends AbstractBizDockDTO {

    public PortfolioEntryDTO() {
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

    //TODO: Attention aux null
    public PortfolioEntryDTO(PortfolioEntry portfolioEntry) {
        this.governanceId = portfolioEntry.governanceId;
        this.creationDate = portfolioEntry.creationDate;
        this.isPublic = portfolioEntry.isPublic;
        this.name = portfolioEntry.name;
        this.portfolioEntryType = portfolioEntry.portfolioEntryType.getName();
        this.manager = new ActorLink(portfolioEntry.manager);
        this.sponsoringUnit = new OrgUnitLink(portfolioEntry.sponsoringUnit);
        portfolioEntry.deliveryUnits.forEach(deliveryUnit -> this.deliveryUnits.add(new OrgUnitLink(deliveryUnit)));
        portfolioEntry.portfolios.forEach(portfolio -> this.portfolios.add(new PortfolioLink(portfolio)));
        this.lifeCycleProcess = portfolioEntry.activeLifeCycleInstance.lifeCycleProcess.name;
        this.portfolioEntryStatus = new PortfolioEntryReportLink(portfolioEntry.lastPortfolioEntryReport);
        this.lastPEReportDate = portfolioEntry.lastPortfolioEntryReport.creationDate;
        this.lastMilestone = new LifeCycleMilestoneInstanceLink(portfolioEntry.lastApprovedLifeCycleMilestoneInstance);
        this.nextMilestone = new PlannedLifeCycleMilestoneInstanceLink(portfolioEntry.nextPlannedLifeCycleMilestoneInstance);
        this.lastMilestoneDate = portfolioEntry.lastApprovedLifeCycleMilestoneInstance.passedDate;
        this.nextMilestoneDate = portfolioEntry.nextPlannedLifeCycleMilestoneInstance.plannedDate;
        this.isConcept = portfolioEntry.activeLifeCycleInstance.isConcept;
        this.archived = portfolioEntry.archived;
        portfolioEntry.stakeholders.forEach(stakeholder -> this.stakeholders.add(new ActorLink(stakeholder.actor)));
        portfolioEntry.destinationDependencies.forEach(dependency -> this.dependencies.add(new PortfolioEntryLink(dependency.getSourcePortfolioEntry())));
        this.startDate = portfolioEntry.startDate;
        this.endDate = portfolioEntry.endDate;
    }
}