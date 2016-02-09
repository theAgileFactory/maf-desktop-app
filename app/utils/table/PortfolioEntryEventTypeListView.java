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
import framework.utils.Icon;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.pmo.PortfolioEntryEventType;

/**
 * A portfolio entry event type list view is used to display a portfolio entry
 * event type row in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryEventTypeListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<PortfolioEntryEventTypeListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition() {
            this.templateTable = getTable();
        }

        /**
         * Get the table.
         */
        public Table<PortfolioEntryEventTypeListView> getTable() {
            return new Table<PortfolioEntryEventTypeListView>() {
                {

                    setIdFieldName("id");

                    addColumn("bootstrapGlyphicon", "bootstrapGlyphicon", "object.portfolio_entry_event_type.bootstrap_glyphicon.label",
                            Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("bootstrapGlyphicon", new IColumnFormatter<PortfolioEntryEventTypeListView>() {
                        @Override
                        public String apply(PortfolioEntryEventTypeListView portfolioEntryEventTypeListView, Object value) {
                            return Icon.getLabel(portfolioEntryEventTypeListView.bootstrapGlyphicon);
                        }
                    });

                    addColumn("name", "name", "object.portfolio_entry_event_type.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<PortfolioEntryEventTypeListView>());

                    addColumn("selectable", "selectable", "object.portfolio_entry_event_type.selectable.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("selectable", new BooleanFormatter<PortfolioEntryEventTypeListView>());

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<PortfolioEntryEventTypeListView>(IMafConstants.EDIT_URL_FORMAT,
                            new StringFormatFormatter.Hook<PortfolioEntryEventTypeListView>() {
                        @Override
                        public String convert(PortfolioEntryEventTypeListView portfolioEntryEventTypeListView) {
                            return controllers.admin.routes.ConfigurationRegisterController.manageEventType(portfolioEntryEventTypeListView.id).url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<PortfolioEntryEventTypeListView>() {
                        @Override
                        public String apply(PortfolioEntryEventTypeListView portfolioEntryEventTypeListView, Object value) {
                            String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                    Msg.get("default.delete.confirmation.message"));
                            String url = controllers.admin.routes.ConfigurationRegisterController.deleteEventType(portfolioEntryEventTypeListView.id).url();
                            return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                        }
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    setEmptyMessageKey("object.portfolio_entry_event_type.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public PortfolioEntryEventTypeListView() {
    }

    public Long id;

    public String name;

    public boolean selectable;

    public String bootstrapGlyphicon;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param eventType
     *            the portfolio entry event type in the DB
     */
    public PortfolioEntryEventTypeListView(PortfolioEntryEventType eventType) {

        this.id = eventType.id;
        this.name = eventType.name;
        this.selectable = eventType.selectable;
        this.bootstrapGlyphicon = eventType.bootstrapGlyphicon;

    }
}
