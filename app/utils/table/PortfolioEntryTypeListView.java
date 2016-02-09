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
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.pmo.PortfolioEntryType;

/**
 * A portfolio entry type list view is used to display a portfolio entry type
 * row in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryTypeListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<PortfolioEntryTypeListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition() {
            this.templateTable = getTable();
        }

        /**
         * Get the table.
         */
        public Table<PortfolioEntryTypeListView> getTable() {
            return new Table<PortfolioEntryTypeListView>() {
                {

                    setIdFieldName("id");

                    addColumn("name", "name", "object.portfolio_entry_type.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<PortfolioEntryTypeListView>());

                    addColumn("description", "description", "object.portfolio_entry_type.description.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("description", new ObjectFormatter<PortfolioEntryTypeListView>());

                    addColumn("selectable", "selectable", "object.portfolio_entry_type.selectable.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("selectable", new BooleanFormatter<PortfolioEntryTypeListView>());

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<PortfolioEntryTypeListView>(IMafConstants.EDIT_URL_FORMAT,
                            new StringFormatFormatter.Hook<PortfolioEntryTypeListView>() {
                        @Override
                        public String convert(PortfolioEntryTypeListView portfolioEntryTypeListView) {
                            return controllers.admin.routes.ConfigurationPortfolioController
                                    .managePortfolioEntryType(portfolioEntryTypeListView.isRelease, portfolioEntryTypeListView.id).url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<PortfolioEntryTypeListView>() {
                        @Override
                        public String apply(PortfolioEntryTypeListView portfolioEntryTypeListView, Object value) {
                            String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                    Msg.get("default.delete.confirmation.message"));
                            String url = controllers.admin.routes.ConfigurationPortfolioController.deletePortfolioEntryType(portfolioEntryTypeListView.id)
                                    .url();
                            return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                        }
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    setEmptyMessageKey("object.portfolio_entry_type.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public PortfolioEntryTypeListView() {
    }

    public Long id;

    public String name;

    public String description;

    public boolean selectable;

    public boolean isRelease;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param portfolioEntryType
     *            the portfolio entry type in the DB
     */
    public PortfolioEntryTypeListView(PortfolioEntryType portfolioEntryType) {

        this.id = portfolioEntryType.id;
        this.name = portfolioEntryType.name;
        this.description = portfolioEntryType.description;
        this.selectable = portfolioEntryType.selectable;
        this.isRelease = portfolioEntryType.isRelease;

    }
}
