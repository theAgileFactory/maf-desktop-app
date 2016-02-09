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

import constants.IMafConstants;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.Color;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.pmo.PortfolioEntryReportStatusType;

/**
 * A portfolio entry report status type list view is used to display a portfolio
 * entry report status row in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryReportStatusTypeListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<PortfolioEntryReportStatusTypeListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition() {
            this.templateTable = getTable();
        }

        /**
         * Get the table.
         */
        public Table<PortfolioEntryReportStatusTypeListView> getTable() {
            return new Table<PortfolioEntryReportStatusTypeListView>() {
                {

                    setIdFieldName("id");

                    addColumn("name", "name", "object.portfolio_entry_report_status_type.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<PortfolioEntryReportStatusTypeListView>());

                    addColumn("description", "description", "object.portfolio_entry_report_status_type.description.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("description", new ObjectFormatter<PortfolioEntryReportStatusTypeListView>());

                    addColumn("selectable", "selectable", "object.portfolio_entry_report_status_type.selectable.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("selectable", new BooleanFormatter<PortfolioEntryReportStatusTypeListView>());

                    addColumn("color", "color", "object.portfolio_entry_report_status_type.css_class.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("color", new ObjectFormatter<PortfolioEntryReportStatusTypeListView>());

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<PortfolioEntryReportStatusTypeListView>(IMafConstants.EDIT_URL_FORMAT,
                            new StringFormatFormatter.Hook<PortfolioEntryReportStatusTypeListView>() {
                        @Override
                        public String convert(PortfolioEntryReportStatusTypeListView portfolioEntryReportStatusTypeListView) {
                            return controllers.admin.routes.ConfigurationRegisterController.manageReportStatusType(portfolioEntryReportStatusTypeListView.id)
                                    .url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<PortfolioEntryReportStatusTypeListView>() {
                        @Override
                        public String apply(PortfolioEntryReportStatusTypeListView portfolioEntryReportStatusTypeListView, Object value) {
                            String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                    Msg.get("default.delete.confirmation.message"));
                            String url = controllers.admin.routes.ConfigurationRegisterController
                                    .deleteReportStatusType(portfolioEntryReportStatusTypeListView.id).url();
                            return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                        }
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    setEmptyMessageKey("object.portfolio_entry_report_status_type.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public PortfolioEntryReportStatusTypeListView() {
    }

    public Long id;

    public String name;

    public String description;

    public boolean selectable;

    public String color;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param reportStatusType
     *            the portfolio entry report status type in the DB
     * @param messagesPlugin
     *            the i18n service
     */
    public PortfolioEntryReportStatusTypeListView(PortfolioEntryReportStatusType reportStatusType, II18nMessagesPlugin messagesPlugin) {

        this.id = reportStatusType.id;
        this.name = reportStatusType.name;
        this.description = reportStatusType.description;
        this.selectable = reportStatusType.selectable;
        this.color = Color.getLabel(reportStatusType.cssClass, messagesPlugin);

    }
}
