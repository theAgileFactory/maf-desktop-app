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
import framework.utils.formats.StringFormatFormatter;
import models.pmo.Actor;
import models.pmo.Stakeholder;
import models.pmo.StakeholderType;

/**
 * A portfolio entry stakeholder list view is used to display a stakeholder row
 * for a portfolio entry in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryStakeholderListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<PortfolioEntryStakeholderListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition() {
            this.templateTable = getTable();
        }

        /**
         * Get the table.
         */
        public Table<PortfolioEntryStakeholderListView> getTable() {
            return new Table<PortfolioEntryStakeholderListView>() {
                {
                    setIdFieldName("id");

                    addColumn("name", "actor", "object.stakeholder.actor.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new IColumnFormatter<PortfolioEntryStakeholderListView>() {
                        @Override
                        public String apply(PortfolioEntryStakeholderListView stakeholderListView, Object value) {
                            return views.html.modelsparts.display_actor.render(stakeholderListView.actor).body();
                        }
                    });

                    addColumn("role", "type", "object.stakeholder.role.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("role", new IColumnFormatter<PortfolioEntryStakeholderListView>() {
                        @Override
                        public String apply(PortfolioEntryStakeholderListView stakeholderListView, Object value) {
                            return views.html.framework_views.parts.formats.display_value_holder.render(stakeholderListView.type, true).body();
                        }
                    });

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<PortfolioEntryStakeholderListView>(IMafConstants.EDIT_URL_FORMAT,
                            new StringFormatFormatter.Hook<PortfolioEntryStakeholderListView>() {
                        @Override
                        public String convert(PortfolioEntryStakeholderListView stakeholderListView) {
                            return controllers.core.routes.PortfolioEntryStakeholderController
                                    .manage(stakeholderListView.portfolioEntryId, stakeholderListView.id).url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    addColumn("removeActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("removeActionLink", new IColumnFormatter<PortfolioEntryStakeholderListView>() {
                        @Override
                        public String apply(PortfolioEntryStakeholderListView portfolioEntryStakeholderListView, Object value) {
                            String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                    Msg.get("default.delete.confirmation.message"));
                            String url = controllers.core.routes.PortfolioEntryStakeholderController
                                    .delete(portfolioEntryStakeholderListView.portfolioEntryId, portfolioEntryStakeholderListView.id).url();
                            return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                        }
                    });
                    setColumnCssClass("removeActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("removeActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    setEmptyMessageKey("object.stakeholder.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public PortfolioEntryStakeholderListView() {
    }

    public Long id;
    public Long portfolioEntryId;
    public Actor actor;
    public StakeholderType type;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param stakeholder
     *            the stakeholder for a portfolio entry in the DB
     */
    public PortfolioEntryStakeholderListView(Stakeholder stakeholder) {

        this.id = stakeholder.id;
        this.portfolioEntryId = stakeholder.portfolioEntry != null ? stakeholder.portfolioEntry.id : null;
        this.actor = stakeholder.actor;
        this.type = stakeholder.stakeholderType;

    }

}
