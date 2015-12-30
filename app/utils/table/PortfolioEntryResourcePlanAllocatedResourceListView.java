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
package utils.table;

import java.math.BigDecimal;
import java.text.MessageFormat;

import constants.IMafConstants;
import dao.pmo.PortfolioEntryDao;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.NumberFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.finance.PortfolioEntryResourcePlanAllocatedActor;
import models.finance.PortfolioEntryResourcePlanAllocatedCompetency;
import models.finance.PortfolioEntryResourcePlanAllocatedOrgUnit;
import models.pmo.Actor;
import models.pmo.Competency;
import models.pmo.OrgUnit;
import models.pmo.PortfolioEntryPlanningPackage;

/**
 * A portfolio entry resource plan allocated resource list view is used to
 * display an allocated resource (actor, org unit or competency) in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryResourcePlanAllocatedResourceListView {

    public static Table<PortfolioEntryResourcePlanAllocatedResourceListView> templateTable = getTable();

    /**
     * Get the table.
     */
    public static Table<PortfolioEntryResourcePlanAllocatedResourceListView> getTable() {
        return new Table<PortfolioEntryResourcePlanAllocatedResourceListView>() {
            {
                setIdFieldName("id");

                addColumn("type", "type", "object.allocated_resource.type.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("type", new IColumnFormatter<PortfolioEntryResourcePlanAllocatedResourceListView>() {
                    @Override
                    public String apply(PortfolioEntryResourcePlanAllocatedResourceListView allocatedResourceListView, Object value) {
                        switch (allocatedResourceListView.type) {
                        case ACTOR:
                            return "<span class=\"fa fa-user\"></span>";
                        case COMPETENCY:
                            return "<span class=\"fa fa-graduation-cap\"></span>";
                        case ORG_UNIT:
                            return "<span class=\"fa fa-building\"></span>";
                        default:
                            return IMafConstants.DEFAULT_VALUE_EMPTY_DATA;
                        }
                    }
                });

                addColumn("resource", "id", "object.allocated_resource.resource.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("resource", new IColumnFormatter<PortfolioEntryResourcePlanAllocatedResourceListView>() {
                    @Override
                    public String apply(PortfolioEntryResourcePlanAllocatedResourceListView allocatedResourceListView, Object value) {
                        switch (allocatedResourceListView.type) {
                        case ACTOR:
                            return views.html.modelsparts.display_actor.render(allocatedResourceListView.actor).body();
                        case COMPETENCY:
                            return views.html.framework_views.parts.formats.display_value_holder.render(allocatedResourceListView.competency, true).body();
                        case ORG_UNIT:
                            return views.html.modelsparts.display_org_unit.render(allocatedResourceListView.orgUnit).body();
                        default:
                            return IMafConstants.DEFAULT_VALUE_EMPTY_DATA;
                        }
                    }
                });
                setColumnValueCssClass("resource", "rowlink-skip");

                addColumn("portfolioEntryName", "portfolioEntryName", "object.allocated_resource.portfolio_entry.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("portfolioEntryName", new ObjectFormatter<PortfolioEntryResourcePlanAllocatedResourceListView>());

                addColumn("days", "days", "object.allocated_resource.days.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("days", new NumberFormatter<PortfolioEntryResourcePlanAllocatedResourceListView>());

                addColumn("forecastDays", "forecastDays", "object.allocated_resource.forecast_days.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("forecastDays", new NumberFormatter<PortfolioEntryResourcePlanAllocatedResourceListView>());

                addColumn("dailyRate", "dailyRate", "object.allocated_resource.daily_rate.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("dailyRate", new NumberFormatter<PortfolioEntryResourcePlanAllocatedResourceListView>());

                addColumn("planningPackage", "planningPackage", "object.allocated_resource.package.label", Table.ColumnDef.SorterType.NONE);
                setColumnCssClass("planningPackage", IMafConstants.BOOTSTRAP_COLUMN_2);
                setJavaColumnFormatter("planningPackage", new IColumnFormatter<PortfolioEntryResourcePlanAllocatedResourceListView>() {
                    @Override
                    public String apply(PortfolioEntryResourcePlanAllocatedResourceListView allocatedResourceListView, Object value) {
                        return views.html.modelsparts.display_portfolio_entry_planning_package.render(allocatedResourceListView.planningPackage).body();
                    }
                });
                setColumnValueCssClass("planningPackage", "rowlink-skip");

                addColumn("date", "date", "object.allocated_resource.date.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("date", new ObjectFormatter<PortfolioEntryResourcePlanAllocatedResourceListView>());

                addColumn("isConfirmed", "isConfirmed", "object.allocated_resource.is_confirmed.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("isConfirmed", new BooleanFormatter<PortfolioEntryResourcePlanAllocatedResourceListView>());

                addColumn("reallocate", "id", "", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("reallocate",
                        new StringFormatFormatter<PortfolioEntryResourcePlanAllocatedResourceListView>(
                                "<a href=\"%s\"><span class=\"fa fa-user\"></span></a>",
                                new StringFormatFormatter.Hook<PortfolioEntryResourcePlanAllocatedResourceListView>() {
                    @Override
                    public String convert(PortfolioEntryResourcePlanAllocatedResourceListView allocatedResourceListView) {
                        switch (allocatedResourceListView.type) {
                        case COMPETENCY:
                            return controllers.core.routes.PortfolioEntryPlanningController
                                    .reallocateCompetency(allocatedResourceListView.portfolioEntryId, allocatedResourceListView.id).url();
                        case ORG_UNIT:
                            return controllers.core.routes.PortfolioEntryPlanningController
                                    .reallocateOrgUnit(allocatedResourceListView.portfolioEntryId, allocatedResourceListView.id).url();
                        default:
                            return IMafConstants.DEFAULT_VALUE_EMPTY_DATA;
                        }

                    }
                }));
                setColumnCssClass("reallocate", IMafConstants.BOOTSTRAP_COLUMN_1);
                setColumnValueCssClass("reallocate", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("editActionLink", new StringFormatFormatter<PortfolioEntryResourcePlanAllocatedResourceListView>(
                        IMafConstants.EDIT_URL_FORMAT, new StringFormatFormatter.Hook<PortfolioEntryResourcePlanAllocatedResourceListView>() {
                    @Override
                    public String convert(PortfolioEntryResourcePlanAllocatedResourceListView allocatedResourceListView) {
                        switch (allocatedResourceListView.type) {
                        case ACTOR:
                            return controllers.core.routes.PortfolioEntryPlanningController
                                    .manageAllocatedActor(allocatedResourceListView.portfolioEntryId, allocatedResourceListView.id).url();
                        case COMPETENCY:
                            return controllers.core.routes.PortfolioEntryPlanningController
                                    .manageAllocatedCompetency(allocatedResourceListView.portfolioEntryId, allocatedResourceListView.id).url();
                        case ORG_UNIT:
                            return controllers.core.routes.PortfolioEntryPlanningController
                                    .manageAllocatedOrgUnit(allocatedResourceListView.portfolioEntryId, allocatedResourceListView.id).url();
                        default:
                            return IMafConstants.DEFAULT_VALUE_EMPTY_DATA;
                        }
                    }
                }));
                setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                addColumn("removeActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("removeActionLink", new IColumnFormatter<PortfolioEntryResourcePlanAllocatedResourceListView>() {
                    @Override
                    public String apply(PortfolioEntryResourcePlanAllocatedResourceListView allocatedResourceListView, Object value) {
                        String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                Msg.get("default.delete.confirmation.message"));
                        String url;
                        switch (allocatedResourceListView.type) {
                        case ACTOR:
                            url = controllers.core.routes.PortfolioEntryPlanningController
                                    .deleteAllocatedActor(allocatedResourceListView.portfolioEntryId, allocatedResourceListView.id).url();
                            return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                        case COMPETENCY:
                            url = controllers.core.routes.PortfolioEntryPlanningController
                                    .deleteAllocatedCompetency(allocatedResourceListView.portfolioEntryId, allocatedResourceListView.id).url();
                            return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                        case ORG_UNIT:
                            url = controllers.core.routes.PortfolioEntryPlanningController
                                    .deleteAllocatedOrgUnit(allocatedResourceListView.portfolioEntryId, allocatedResourceListView.id).url();
                            return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                        default:
                            return IMafConstants.DEFAULT_VALUE_EMPTY_DATA;
                        }
                    }
                });
                setColumnCssClass("removeActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                setColumnValueCssClass("removeActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                setEmptyMessageKey("object.allocated_resource.table.empty");

            }
        };

    }

    /**
     * Default constructor.
     */
    public PortfolioEntryResourcePlanAllocatedResourceListView() {
    }

    public Type type;

    public Long id;

    public Long portfolioEntryId;

    public String portfolioEntryName;

    // the resource
    public Actor actor;
    public OrgUnit orgUnit;
    public Competency competency;

    public BigDecimal days;

    public BigDecimal forecastDays;

    public BigDecimal dailyRate;

    public String date;

    public PortfolioEntryPlanningPackage planningPackage;

    public boolean isConfirmed;

    public Boolean followPackageDates;

    /**
     * Construct a list view with an allocated actor entry.
     * 
     * @param allocatedActor
     *            the allocated actor in the DB
     */
    public PortfolioEntryResourcePlanAllocatedResourceListView(PortfolioEntryResourcePlanAllocatedActor allocatedActor) {
        this.type = Type.ACTOR;
        this.id = allocatedActor.id;
        this.portfolioEntryId = allocatedActor.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id;
        this.portfolioEntryName = PortfolioEntryDao.getPEById(this.portfolioEntryId).getName();
        this.actor = allocatedActor.actor;
        this.days = allocatedActor.days;
        this.date = allocatedActor.getDisplayDate();
        this.planningPackage = allocatedActor.portfolioEntryPlanningPackage;
        this.isConfirmed = allocatedActor.isConfirmed;
        this.followPackageDates = allocatedActor.followPackageDates;
        this.forecastDays = allocatedActor.forecastDays;
        this.dailyRate = allocatedActor.dailyRate;
    }

    /**
     * Construct a list view with an allocated org unit entry.
     * 
     * @param allocatedOrgUnit
     *            the allocated org unit in the DB
     */
    public PortfolioEntryResourcePlanAllocatedResourceListView(PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit) {
        this.type = Type.ORG_UNIT;
        this.id = allocatedOrgUnit.id;
        this.portfolioEntryId = allocatedOrgUnit.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id;
        this.portfolioEntryName = PortfolioEntryDao.getPEById(this.portfolioEntryId).getName();
        this.orgUnit = allocatedOrgUnit.orgUnit;
        this.days = allocatedOrgUnit.days;
        this.date = allocatedOrgUnit.getDisplayDate();
        this.planningPackage = allocatedOrgUnit.portfolioEntryPlanningPackage;
        this.isConfirmed = allocatedOrgUnit.isConfirmed;
        this.followPackageDates = allocatedOrgUnit.followPackageDates;
        this.forecastDays = allocatedOrgUnit.forecastDays;
        this.dailyRate = allocatedOrgUnit.dailyRate;
    }

    /**
     * Construct a list view with an allocated competency entry.
     * 
     * @param allocatedCompetency
     *            the allocated competency in the DB
     */
    public PortfolioEntryResourcePlanAllocatedResourceListView(PortfolioEntryResourcePlanAllocatedCompetency allocatedCompetency) {
        this.type = Type.COMPETENCY;
        this.id = allocatedCompetency.id;
        this.portfolioEntryId = allocatedCompetency.portfolioEntryResourcePlan.lifeCycleInstancePlannings.get(0).lifeCycleInstance.portfolioEntry.id;
        this.portfolioEntryName = PortfolioEntryDao.getPEById(this.portfolioEntryId).getName();
        this.competency = allocatedCompetency.competency;
        this.days = allocatedCompetency.days;
        this.date = allocatedCompetency.getDisplayDate();
        this.planningPackage = allocatedCompetency.portfolioEntryPlanningPackage;
        this.isConfirmed = allocatedCompetency.isConfirmed;
        this.followPackageDates = allocatedCompetency.followPackageDates;
        this.forecastDays = allocatedCompetency.forecastDays;
        this.dailyRate = allocatedCompetency.dailyRate;
    }

    /**
     * The type of the resource (actor, org unit or competency).
     * 
     * @author Johann Kohler
     * 
     */
    public static enum Type {
        ACTOR, ORG_UNIT, COMPETENCY;
    }

}
