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
import dao.pmo.PortfolioDao;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.StringFormatFormatter;
import models.pmo.Actor;
import models.pmo.Stakeholder;
import models.pmo.StakeholderType;

/**
 * A stakeholder list view is used to display a stakeholder row for a portfolio
 * in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioStakeholderListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<PortfolioStakeholderListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition() {
            this.templateTable = getTable();
        }

        /**
         * Get the table.
         */
        public Table<PortfolioStakeholderListView> getTable() {
            return new Table<PortfolioStakeholderListView>() {
                {
                    setIdFieldName("id");

                    addColumn("name", "actor", "object.stakeholder.actor.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new IColumnFormatter<PortfolioStakeholderListView>() {
                        @Override
                        public String apply(PortfolioStakeholderListView stakeholderListView, Object value) {
                            return views.html.modelsparts.display_actor.render(stakeholderListView.actor).body();
                        }
                    });

                    addColumn("role", "type", "object.stakeholder.role.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("role", new IColumnFormatter<PortfolioStakeholderListView>() {
                        @Override
                        public String apply(PortfolioStakeholderListView stakeholderListView, Object value) {
                            return views.html.framework_views.parts.formats.display_value_holder.render(stakeholderListView.type, true).body();
                        }
                    });

                    addColumn("portfolio", "portfolioId", "object.stakeholder.portfolio.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("portfolio", new IColumnFormatter<PortfolioStakeholderListView>() {
                        @Override
                        public String apply(PortfolioStakeholderListView stakeholderListView, Object value) {
                            if (stakeholderListView.portfolioId != null) {
                                return views.html.modelsparts.display_portfolio.render(PortfolioDao.getPortfolioById(stakeholderListView.portfolioId)).body();
                            }
                            return IMafConstants.DEFAULT_VALUE_EMPTY_DATA;
                        }
                    });

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<PortfolioStakeholderListView>(IMafConstants.EDIT_URL_FORMAT,
                            new StringFormatFormatter.Hook<PortfolioStakeholderListView>() {
                        @Override
                        public String convert(PortfolioStakeholderListView stakeholderListView) {
                            return controllers.core.routes.PortfolioStakeholderController.manage(stakeholderListView.portfolioId, stakeholderListView.id)
                                    .url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    addColumn("removeActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("removeActionLink", new IColumnFormatter<PortfolioStakeholderListView>() {
                        @Override
                        public String apply(PortfolioStakeholderListView portfolioStakeholderListView, Object value) {
                            String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                    Msg.get("default.delete.confirmation.message"));
                            String url = controllers.core.routes.PortfolioStakeholderController
                                    .delete(portfolioStakeholderListView.portfolioId, portfolioStakeholderListView.id).url();
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
    public PortfolioStakeholderListView() {
    }

    public Long id;
    public Long portfolioId;
    public Actor actor;
    public StakeholderType type;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param stakeholder
     *            the stakeholder in the DB
     */
    public PortfolioStakeholderListView(Stakeholder stakeholder) {

        this.id = stakeholder.id;
        this.portfolioId = stakeholder.portfolio != null ? stakeholder.portfolio.id : null;
        this.actor = stakeholder.actor;
        this.type = stakeholder.stakeholderType;

    }

}
