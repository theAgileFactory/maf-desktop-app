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

import constants.IMafConstants;
import controllers.admin.routes;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.pmo.PortfolioEntryIssueType;

import java.text.MessageFormat;

/**
 * A portfolio entry issue type list view is used to display a portfolio entry
 * issue type row in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryIssueTypeListView {

    /**
     * The definition of the table.
     *
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<PortfolioEntryIssueTypeListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition() {
            this.templateTable = getTable();
        }

        /**
         * Get the table.
         */
        public Table<PortfolioEntryIssueTypeListView> getTable() {
            return new Table<PortfolioEntryIssueTypeListView>() {
                {

                    setIdFieldName("id");

                    addColumn("name", "name", "object.portfolio_entry_issue_type.name.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<>());

                    addColumn("description", "description", "object.portfolio_entry_issue_type.description.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("description", new ObjectFormatter<>());

                    addColumn("selectable", "selectable", "object.portfolio_entry_issue_type.selectable.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("selectable", new BooleanFormatter<>());

                    addColumn("editActionLink", "id", "", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<>(IMafConstants.EDIT_URL_FORMAT,
                            (StringFormatFormatter.Hook<PortfolioEntryIssueTypeListView>) portfolioEntryIssueTypeListView -> routes.ConfigurationRegisterController.manageIssueType(portfolioEntryIssueTypeListView.id).url()));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("deleteActionLink", "id", "", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", (portfolioEntryIssueTypeListView, value) -> {
                        String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                Msg.get("default.delete.confirmation.message"));
                        String url = routes.ConfigurationRegisterController.deleteIssueType(portfolioEntryIssueTypeListView.id).url();
                        return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    setEmptyMessageKey("object.portfolio_entry_issue_type.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public PortfolioEntryIssueTypeListView() {
    }

    public Long id;

    public String name;

    public String description;

    public boolean selectable;

    /**
     * Construct a list view with a DB entry.
     *
     * @param issueType
     *            the portfolio entry issue type in the DB
     */
    public PortfolioEntryIssueTypeListView(PortfolioEntryIssueType issueType) {

        this.id = issueType.id;
        this.name = issueType.name;
        this.description = issueType.description;
        this.selectable = issueType.selectable;

    }
}
