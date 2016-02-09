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

import framework.utils.IColumnFormatter;
import framework.utils.Table;
import framework.utils.formats.ObjectFormatter;
import models.pmo.PortfolioEntryReport;

/**
 * A portfolio report list view is used to display a portfolio entry report row
 * in a portfolio context.
 * 
 * @author Johann Kohler
 */
public class PortfolioReportListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<PortfolioReportListView> templateTable;

        /**
         * Default constructor.
         * 
         */
        public TableDefinition() {
            this.templateTable = getTable();
        }

        /**
         * Get the table.
         */
        public Table<PortfolioReportListView> getTable() {
            return new Table<PortfolioReportListView>() {
                {
                    setIdFieldName("id");

                    addColumn("portfolioEntryName", "portfolioEntryName", "object.portfolio_entry_report.portfolio_entry.label",
                            Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("portfolioEntryName", new ObjectFormatter<PortfolioReportListView>());

                    addColumn("comments", "comments", "object.portfolio_entry_report.comments.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("comments", new ObjectFormatter<PortfolioReportListView>());

                    this.setLineAction(new IColumnFormatter<PortfolioReportListView>() {
                        @Override
                        public String apply(PortfolioReportListView portfolioReportListView, Object value) {
                            return controllers.core.routes.PortfolioEntryStatusReportingController
                                    .registers(portfolioReportListView.portfolioEntryId, 0, 0, 0, false, false).url();
                        }
                    });

                    setEmptyMessageKey("object.portfolio_entry_report.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public PortfolioReportListView() {
    }

    public Long id;

    public Long portfolioEntryId;

    public String portfolioEntryName;

    public String comments;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param portfolioEntryReport
     *            the portfolio entry report in the DB
     */
    public PortfolioReportListView(PortfolioEntryReport portfolioEntryReport) {

        this.id = portfolioEntryReport.id;
        this.portfolioEntryId = portfolioEntryReport.portfolioEntry.id;
        this.portfolioEntryName = portfolioEntryReport.portfolioEntry.getName();
        this.comments = portfolioEntryReport.comments;

    }

}
