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
package utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.governance.LifeCycleMilestone;
import models.governance.LifeCycleMilestoneInstance;
import models.governance.PlannedLifeCycleMilestoneInstance;
import security.DefaultDynamicResourceHandler;
import dao.finance.CurrencyDAO;
import dao.finance.PortfolioEntryBudgetDAO;
import dao.finance.PortfolioEntryResourcePlanDAO;
import dao.governance.LifeCycleMilestoneDao;
import dao.governance.LifeCyclePlanningDao;
import framework.utils.Msg;

/**
 * Compute and display the milestones trend table.
 * 
 * @author Johann Kohler
 * 
 */
public class MilestonesTrend {

    List<LifeCycleMilestone> lifeCycleMilestones;
    List<Row> rows;
    Long portfolioEntryId;

    /**
     * Construct the trend milestones.
     * 
     * @param lifeCycleMilestones
     *            the milestones of the process
     * @param milestoneInstances
     *            the approved milestone instances of the process instance
     */
    public MilestonesTrend(List<LifeCycleMilestone> lifeCycleMilestones, List<LifeCycleMilestoneInstance> milestoneInstances) {
        this.lifeCycleMilestones = lifeCycleMilestones;
        this.rows = new ArrayList<>();
        Row lastRow = null;
        for (LifeCycleMilestoneInstance milestoneInstance : milestoneInstances) {
            Row row = new Row(lastRow, lifeCycleMilestones, milestoneInstance);
            this.rows.add(row);
            lastRow = row;
            this.portfolioEntryId = milestoneInstance.lifeCycleInstance.portfolioEntry.id;
        }
    }

    /**
     * Render the trend milestones table.
     */
    public String renderTable() {

        if (this.rows.size() > 0) {

            String table =
                    "<div style=\"overflow-x: auto; overflow-y: hidden;\"><table class=\"table table-condensed table-hover\" id=\"milestones-trend-table\">";

            // colgroup
            table += "<colgroup>";
            table += "<col class=\"col-md-1\">";
            if (DefaultDynamicResourceHandler.isAllowed(DefaultDynamicResourceHandler.PORTFOLIO_ENTRY_FINANCIAL_VIEW_DYNAMIC_PERMISSION, "")) {
                table += "<col class=\"col-md-1\">";
            }
            table += "<col class=\"col-md-1\">";
            for (int i = 0; i < lifeCycleMilestones.size(); i++) {
                table += "<col class=\"col-md-1\">";
            }
            table += "</colgroup>";

            // header
            table += "<thead><tr>";
            table += "<th style=\"white-space: nowrap\">" + Msg.get("core.portfolio_entry.overview.milestones_trend.panel.milestone.label") + "</th>";
            if (DefaultDynamicResourceHandler.isAllowed(DefaultDynamicResourceHandler.PORTFOLIO_ENTRY_FINANCIAL_VIEW_DYNAMIC_PERMISSION, "")) {
                table +=
                        "<th style=\"white-space: nowrap\">" + Msg.get("core.portfolio_entry.overview.milestones_trend.panel.budget.label") + " ("
                                + CurrencyDAO.getCurrencyDefaultAsCode() + ")" + "</th>";
            }
            table += "<th style=\"white-space: nowrap\">" + Msg.get("core.portfolio_entry.overview.milestones_trend.panel.resources.label") + "</th>";
            for (LifeCycleMilestone milestone : lifeCycleMilestones) {
                table += "<th style=\"white-space: nowrap\">" + milestone.getShortName() + "</th>";
            }
            table += "</tr></thead>";

            // body
            table += "<tbody class=\"rowlink\" data-link=\"row\">";
            for (Row row : rows) {

                table += "<tr>";

                table +=
                        "<td style=\"white-space: nowrap\"><a class=\"hidden\" href=\""
                                + controllers.core.routes.PortfolioEntryGovernanceController.viewMilestone(this.portfolioEntryId, row.lifeCycleMilestoneId)
                                        .url() + "\"></a>" + row.milestoneInstance.lifeCycleMilestone.getShortName() + "</td>";

                if (DefaultDynamicResourceHandler.isAllowed(DefaultDynamicResourceHandler.PORTFOLIO_ENTRY_FINANCIAL_VIEW_DYNAMIC_PERMISSION, "")) {
                    table += "<td style=\"white-space: nowrap\">" + row.renderBugdteContent() + "</td>";
                }

                table +=
                        "<td style=\"white-space: nowrap\">"
                                + row.renderResourcesContent("core.portfolio_entry.overview.milestones_trend.panel.resources.value.label") + "</td>";

                for (DateCell dateCell : row.dates) {
                    table += dateCell.render();
                }

                table += "</tr>";
            }
            table += "</tbody>";

            table += "</table></div>";

            table += "<script>$('#milestones-trend-table tbody').rowlink()</script>";

            return table;

        } else {
            return Msg.get("core.portfolio_entry.overview.milestones_trend.panel.empty");
        }
    }

    /**
     * A row of the trend milestones table.
     * 
     * @author Johann Kohler
     * 
     */
    public static class Row {

        Long lifeCycleMilestoneId;
        LifeCycleMilestoneInstance milestoneInstance;
        DecimalCell opexAmount;
        DecimalCell capexAmount;
        DecimalCell resources;
        DateCell[] dates;

        /**
         * Construct a row thanks a milestone instance.
         * 
         * @param lastRow
         *            the last row, useful to draw correctly up and down arrows
         * @param lifeCycleMilestones
         *            the milestones of the process
         * @param milestoneInstance
         *            the milestone instance
         */
        public Row(Row lastRow, List<LifeCycleMilestone> lifeCycleMilestones, LifeCycleMilestoneInstance milestoneInstance) {

            this.milestoneInstance = milestoneInstance;
            this.lifeCycleMilestoneId = milestoneInstance.lifeCycleMilestone.id;

            BigDecimal lastResourcesValue = lastRow != null ? lastRow.resources.value : null;
            BigDecimal lastOpexAmountValue = lastRow != null ? lastRow.opexAmount.value : null;
            BigDecimal lastCapexAmountValue = lastRow != null ? lastRow.capexAmount.value : null;

            this.resources =
                    new DecimalCell(lastResourcesValue,
                            PortfolioEntryResourcePlanDAO.getPEResourcePlanAsDaysById(milestoneInstance.portfolioEntryResourcePlan.id));
            this.opexAmount =
                    new DecimalCell(lastOpexAmountValue, new BigDecimal(PortfolioEntryBudgetDAO.getPEBudgetAsAmountById(
                            milestoneInstance.portfolioEntryBudget.id, true)));
            this.capexAmount =
                    new DecimalCell(lastCapexAmountValue, new BigDecimal(PortfolioEntryBudgetDAO.getPEBudgetAsAmountById(
                            milestoneInstance.portfolioEntryBudget.id, false)));

            // dates
            this.dates = new DateCell[lifeCycleMilestones.size()];
            int i = 0;
            for (LifeCycleMilestone milestone : lifeCycleMilestones) {

                Date lastDateCellValue = null;
                if (lastRow != null) {
                    lastDateCellValue = lastRow.dates[i].value;
                }

                if (milestone.id.equals(milestoneInstance.lifeCycleMilestone.id)) {
                    this.dates[i] = new DateCell(true, lastDateCellValue, milestoneInstance.passedDate);
                } else {
                    if (!LifeCycleMilestoneDao.hasLCMilestoneInstanceApprovedByPEAndLCMilestone(milestoneInstance.lifeCycleInstance.portfolioEntry.id,
                            milestone.id, milestoneInstance.passedDate)) {
                        PlannedLifeCycleMilestoneInstance plannedDate =
                                LifeCyclePlanningDao.getPlannedLCMilestoneInstanceByLCInstancePlanningAndLCMilestone(milestoneInstance.getPlanning().id,
                                        milestone.id);
                        Date date = plannedDate != null ? plannedDate.plannedDate : null;
                        this.dates[i] = new DateCell(true, lastDateCellValue, date);
                    } else {
                        this.dates[i] = new DateCell(false, null, null);
                    }
                }

                i++;
            }

        }

        /**
         * Render the content of the resources cell.
         * 
         * @param i18nKey
         *            the i18n key
         */
        public String renderResourcesContent(String i18nKey) {
            return Msg.get(i18nKey, resources.value) + resources.renderArrow();
        }

        /**
         * Render the content of the budget cell.
         */
        public String renderBugdteContent() {
            String opexContent =
                    "OPEX: " + views.html.framework_views.parts.formats.display_number.render(opexAmount.value, null, false).body()
                            + opexAmount.renderArrow();
            String capexContent =
                    "CAPEX: " + views.html.framework_views.parts.formats.display_number.render(capexAmount.value, null, false).body()
                            + capexAmount.renderArrow();
            return opexContent + "<br/>" + capexContent;
        }

    }

    /**
     * A decimal cell (resources, budget).
     * 
     * @author Johann Kohler
     * 
     */
    public static class DecimalCell {

        BigDecimal previousValue;
        BigDecimal value;

        /**
         * Construct the decimal cell.
         * 
         * @param previousValue
         *            the value of the previous row
         * @param value
         *            the value
         */
        public DecimalCell(BigDecimal previousValue, BigDecimal value) {
            this.previousValue = previousValue;
            this.value = value;
        }

        /**
         * Render the arrow of the cell.
         */
        public String renderArrow() {
            String arrow = "";
            if (this.previousValue != null && this.value != null) {
                if (this.value.compareTo(this.previousValue) < 0) {
                    // the value is lesser than the previousValue
                    arrow = " <span class=\"glyphicons glyphicons-down-arrow\" style=\"color: green;\"></span>";
                } else if (this.value.compareTo(this.previousValue) > 0) {
                    // the value is higher than the previousValue
                    arrow = " <span class=\"glyphicons glyphicons-up-arrow\" style=\"color: red;\"></span>";
                }
            }
            return arrow;
        }

    }

    /**
     * A date cell.
     * 
     * @author Johann Kohler
     * 
     */
    public static class DateCell {

        boolean isApplicable;
        Date previousValue;
        Date value;

        /**
         * Construct the date cell.
         * 
         * @param isApplicable
         *            set to true if the date should be displayed, else an empty
         *            cell with special background is displayed.
         * @param previousValue
         *            the value of the previous row
         * @param value
         *            the value
         */
        public DateCell(boolean isApplicable, Date previousValue, Date value) {
            this.isApplicable = isApplicable;
            this.previousValue = previousValue;
            this.value = value;
        }

        /**
         * Render a date cell.
         */
        public String render() {
            if (isApplicable) {

                String arrow = "";
                if (this.previousValue != null && this.value != null) {
                    if (this.value.before(this.previousValue)) {
                        // the value is before than the previousValue
                        arrow = " <span class=\"glyphicons glyphicons-down-arrow\" style=\"color: green;\"></span>";
                    } else if (this.value.after(this.previousValue)) {
                        // the value is after than the previousValue
                        arrow = " <span class=\"glyphicons glyphicons-up-arrow\" style=\"color: red;\"></span>";
                    }
                }

                return "<td style=\"white-space: nowrap\">" + views.html.framework_views.parts.formats.display_date.render(value, null, false).body() + arrow
                        + "</td>";
            } else {
                return "<td class=\"bg-warning\">&nbsp;</td>";
            }
        }
    }
}
