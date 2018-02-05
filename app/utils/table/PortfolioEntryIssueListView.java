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
import controllers.core.routes;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.pmo.Actor;
import models.pmo.PortfolioEntryIssue;
import models.pmo.PortfolioEntryIssueType;

import java.text.MessageFormat;
import java.util.Date;

/**
 * A portfolio entry issue list view is used to display an portfolioEntry issue
 * row in a table.
 * 
 * @author Guillaume Petit
 */
public class PortfolioEntryIssueListView {

    /**
     * The definition of the table.
     *
     * @author Guillaume Petit
     */
    public static class TableDefinition {

        public Table<PortfolioEntryIssueListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition(II18nMessagesPlugin i18nMessagesPlugin) {
            this.templateTable = getTable(i18nMessagesPlugin);
        }

        /**
         * Get the table.
         */
        public Table<PortfolioEntryIssueListView> getTable(II18nMessagesPlugin i18nMessagesPlugin) {
            return new Table<PortfolioEntryIssueListView>() {
                {
                    setIdFieldName("id");

                    addColumn("name", "name", "object.portfolio_entry_issue.name.label", ColumnDef.SorterType.NONE);

                    addColumn("dueDate", "dueDate", "object.portfolio_entry_issue.due_date.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("dueDate", (portfolioEntryIssueListView, value) -> {
                        DateFormatter<PortfolioEntryIssueListView> df = new DateFormatter<>();
                        if (portfolioEntryIssueListView.dueDate != null) {
                            df.setAlert(portfolioEntryIssueListView.dueDate.before(new Date()));
                        }
                        return df.apply(portfolioEntryIssueListView, value);
                    });

                    addColumn("type", "type", "object.portfolio_entry_issue.type.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("type", (portfolioEntryIssueListView, value) -> views.html.framework_views.parts.formats.display_value_holder.render(portfolioEntryIssueListView.type, true).body());

                    addColumn("owner", "owner", "object.portfolio_entry_issue.owner.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("owner", (portfolioEntryIssueListView, value) -> views.html.modelsparts.display_actor.render(portfolioEntryIssueListView.owner).body());
                    this.setColumnValueCssClass("owner", "rowlink-skip");

                    addColumn("isActive", "isActive", "object.portfolio_entry_issue.is_active.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isActive", new BooleanFormatter<>());

                    addCustomAttributeColumns(i18nMessagesPlugin, PortfolioEntryIssue.class);

                    addColumn("editActionLink", "id", "", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<>(IMafConstants.EDIT_URL_FORMAT,
                            (StringFormatFormatter.Hook<PortfolioEntryIssueListView>) portfolioEntryIssueListView -> routes.PortfolioEntryStatusReportingController
                                    .manageIssue(portfolioEntryIssueListView.portfolioEntryId, portfolioEntryIssueListView.id).url()));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    this.setLineAction((portfolioEntryIssueListView, value) -> routes.PortfolioEntryStatusReportingController
                            .viewIssue(portfolioEntryIssueListView.portfolioEntryId, portfolioEntryIssueListView.id).url());
                    addColumn("deleteActionLink", "id", "", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", (portfolioEntryIssueListView, value) -> {
                        String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION, Msg.get("default.delete.confirmation.message"));
                        String url = routes.PortfolioEntryStatusReportingController.deleteIssue(portfolioEntryIssueListView.portfolioEntryId, portfolioEntryIssueListView.id).url();
                        return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    setEmptyMessageKey("object.portfolio_entry_issue.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public PortfolioEntryIssueListView() {
    }

    public Long id;

    public Long portfolioEntryId;

    public String name;

    public Date dueDate;

    public PortfolioEntryIssueType type;

    public Actor owner;

    public boolean isActive;

    /**
     * Construct a list view with a DB entry.
     *
     * @param portfolioEntryIssue
     *            the portfolio entry issue in the DB
     */
    public PortfolioEntryIssueListView(PortfolioEntryIssue portfolioEntryIssue) {

        this.id = portfolioEntryIssue.id;
        this.portfolioEntryId = portfolioEntryIssue.portfolioEntry.id;
        this.name = portfolioEntryIssue.name;
        this.dueDate = portfolioEntryIssue.dueDate;
        this.type = portfolioEntryIssue.portfolioEntryIssueType != null ? portfolioEntryIssue.portfolioEntryIssueType : null;
        this.owner = portfolioEntryIssue.owner;
        this.isActive = portfolioEntryIssue.isActive;

    }

}
