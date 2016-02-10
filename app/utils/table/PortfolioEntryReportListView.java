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

import java.text.MessageFormat;
import java.util.Date;

import constants.IMafConstants;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.pmo.Actor;
import models.pmo.PortfolioEntryReport;
import models.pmo.PortfolioEntryReportStatusType;

/**
 * A portfolio entry report list view is used to display an portfolio entry
 * report row in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryReportListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<PortfolioEntryReportListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition(II18nMessagesPlugin i18nMessagesPlugin) {
            this.templateTable = getTable(i18nMessagesPlugin);
        }

        /**
         * Get the table.
         */
        public Table<PortfolioEntryReportListView> getTable(II18nMessagesPlugin i18nMessagesPlugin) {
            return new Table<PortfolioEntryReportListView>() {
                {
                    setIdFieldName("id");

                    addColumn("reportDate", "reportDate", "object.portfolio_entry_report.report_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("reportDate", new DateFormatter<PortfolioEntryReportListView>());

                    addColumn("author", "author", "object.portfolio_entry_report.author.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("author", new IColumnFormatter<PortfolioEntryReportListView>() {
                        @Override
                        public String apply(PortfolioEntryReportListView portfolioEntryReportListView, Object value) {
                            return views.html.modelsparts.display_actor.render(portfolioEntryReportListView.author).body();
                        }
                    });
                    this.setColumnValueCssClass("author", "rowlink-skip");

                    addColumn("status", "status", "object.portfolio_entry_report.status.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("status", new IColumnFormatter<PortfolioEntryReportListView>() {
                        @Override
                        public String apply(PortfolioEntryReportListView portfolioEntryReportListView, Object value) {
                            return views.html.modelsparts.display_portfolio_entry_report_status_type.render(portfolioEntryReportListView.status).body();
                        }
                    });

                    addColumn("comments", "comments", "object.portfolio_entry_report.comments.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("comments", new ObjectFormatter<PortfolioEntryReportListView>());

                    addCustomAttributeColumns(i18nMessagesPlugin, PortfolioEntryReport.class);

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<PortfolioEntryReportListView>(IMafConstants.EDIT_URL_FORMAT,
                            new StringFormatFormatter.Hook<PortfolioEntryReportListView>() {
                        @Override
                        public String convert(PortfolioEntryReportListView portfolioEntryReportListView) {
                            return controllers.core.routes.PortfolioEntryStatusReportingController
                                    .manageReport(portfolioEntryReportListView.portfolioEntryId, portfolioEntryReportListView.id).url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<PortfolioEntryReportListView>() {
                        @Override
                        public String apply(PortfolioEntryReportListView portfolioEntryReportListView, Object value) {
                            String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                    Msg.get("default.delete.confirmation.message"));
                            String url = controllers.core.routes.PortfolioEntryStatusReportingController
                                    .deleteReport(portfolioEntryReportListView.portfolioEntryId, portfolioEntryReportListView.id).url();
                            return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                        }
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    this.setLineAction(new IColumnFormatter<PortfolioEntryReportListView>() {
                        @Override
                        public String apply(PortfolioEntryReportListView portfolioEntryReportListView, Object value) {
                            return controllers.core.routes.PortfolioEntryStatusReportingController
                                    .viewReport(portfolioEntryReportListView.portfolioEntryId, portfolioEntryReportListView.id).url();
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
    public PortfolioEntryReportListView() {
    }

    public Long id;

    public Long portfolioEntryId;

    public Date reportDate;

    public Actor author;

    public String comments;

    public PortfolioEntryReportStatusType status;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param portfolioEntryReport
     *            the portfolio entry report in the DB
     */
    public PortfolioEntryReportListView(PortfolioEntryReport portfolioEntryReport) {

        this.id = portfolioEntryReport.id;
        this.portfolioEntryId = portfolioEntryReport.portfolioEntry.id;
        this.reportDate = portfolioEntryReport.creationDate;
        this.author = portfolioEntryReport.author;
        this.status = portfolioEntryReport.portfolioEntryReportStatusType;
        this.comments = portfolioEntryReport.comments;

    }

}
