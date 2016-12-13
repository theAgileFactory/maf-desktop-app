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

import java.util.Date;

import constants.IMafConstants;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.IColumnFormatter;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.StringFormatFormatter;
import java.text.MessageFormat;
import framework.utils.Msg;
import framework.utils.formats.ObjectFormatter;
import models.pmo.Actor;
import models.pmo.PortfolioEntryRisk;
import models.pmo.PortfolioEntryRiskType;

/**
 * A portfolio entry risk list view is used to display an portfolioEntry risk
 * row in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryRiskListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<PortfolioEntryRiskListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition(II18nMessagesPlugin i18nMessagesPlugin) {
            this.templateTable = getTable(i18nMessagesPlugin);
        }

        /**
         * Get the table.
         */
        public Table<PortfolioEntryRiskListView> getTable(II18nMessagesPlugin i18nMessagesPlugin) {
            return new Table<PortfolioEntryRiskListView>() {
                {
                    setIdFieldName("id");

                    addColumn("name", "name", "object.portfolio_entry_risk.name.label", Table.ColumnDef.SorterType.NONE);

                    addColumn("targetDate", "targetDate", "object.portfolio_entry_risk.target_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("targetDate", new IColumnFormatter<PortfolioEntryRiskListView>() {
                        @Override
                        public String apply(PortfolioEntryRiskListView portfolioEntryRiskListView, Object value) {
                            DateFormatter<PortfolioEntryRiskListView> df = new DateFormatter<PortfolioEntryRiskListView>();
                            if (portfolioEntryRiskListView.targetDate != null) {
                                df.setAlert(portfolioEntryRiskListView.isActive && portfolioEntryRiskListView.targetDate.before(new Date()));
                            }
                            return df.apply(portfolioEntryRiskListView, value);
                        }
                    });

                    addColumn("dueDate", "dueDate", "object.portfolio_entry_risk.due_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("dueDate", new IColumnFormatter<PortfolioEntryRiskListView>() {
                        @Override
                        public String apply(PortfolioEntryRiskListView portfolioEntryRiskListView, Object value) {
                            DateFormatter<PortfolioEntryRiskListView> df = new DateFormatter<PortfolioEntryRiskListView>();
                            if (portfolioEntryRiskListView.dueDate != null) {
                                df.setAlert(portfolioEntryRiskListView.dueDate.before(new Date()));
                            }
                            return df.apply(portfolioEntryRiskListView, value);
                        }
                    });

                    addColumn("type", "type", "object.portfolio_entry_risk.type.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("type", new IColumnFormatter<PortfolioEntryRiskListView>() {
                        @Override
                        public String apply(PortfolioEntryRiskListView portfolioEntryRiskListView, Object value) {
                            return views.html.framework_views.parts.formats.display_value_holder.render(portfolioEntryRiskListView.type, true).body();
                        }
                    });

                    addColumn("isMitigated", "isMitigated", "object.portfolio_entry_risk.is_mitigated.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isMitigated", new BooleanFormatter<PortfolioEntryRiskListView>());

                    addColumn("owner", "owner", "object.portfolio_entry_risk.owner.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("owner", new IColumnFormatter<PortfolioEntryRiskListView>() {
                        @Override
                        public String apply(PortfolioEntryRiskListView portfolioEntryRiskListView, Object value) {
                            return views.html.modelsparts.display_actor.render(portfolioEntryRiskListView.owner).body();
                        }
                    });
                    this.setColumnValueCssClass("owner", "rowlink-skip");

                    addColumn("isActive", "isActive", "object.portfolio_entry_risk.is_active.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isActive", new BooleanFormatter<PortfolioEntryRiskListView>());

                    addCustomAttributeColumns(i18nMessagesPlugin, PortfolioEntryRisk.class);

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<PortfolioEntryRiskListView>(IMafConstants.EDIT_URL_FORMAT,
                            new StringFormatFormatter.Hook<PortfolioEntryRiskListView>() {
                        @Override
                        public String convert(PortfolioEntryRiskListView portfolioEntryRiskListView) {
                            return controllers.core.routes.PortfolioEntryStatusReportingController
                                    .manageRisk(portfolioEntryRiskListView.portfolioEntryId, portfolioEntryRiskListView.id, true).url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    this.setLineAction(new IColumnFormatter<PortfolioEntryRiskListView>() {
                        @Override
                        public String apply(PortfolioEntryRiskListView portfolioEntryRiskListView, Object value) {
                            return controllers.core.routes.PortfolioEntryStatusReportingController
                                    .viewRisk(portfolioEntryRiskListView.portfolioEntryId, portfolioEntryRiskListView.id).url();
                        }
                    });
                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<PortfolioEntryRiskListView>() 
                    {
                        @Override
                        public String apply(PortfolioEntryRiskListView portfolioEntryRiskListView, Object value) {
                            String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION, Msg.get("default.delete.confirmation.message"));
                            String url = controllers.core.routes.PortfolioEntryStatusReportingController.deleteRisk(portfolioEntryRiskListView.portfolioEntryId, portfolioEntryRiskListView.id).url();
                            return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                        }
                    });                   
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    setEmptyMessageKey("object.portfolio_entry_risk.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public PortfolioEntryRiskListView() {
    }

    public Long id;

    public Long portfolioEntryId;

    public String name;

    public Date targetDate;

    public Date dueDate;

    public PortfolioEntryRiskType type;

    public Boolean isMitigated;

    public Actor owner;

    public boolean isActive;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param portfolioEntryRisk
     *            the portfolio entry risk in the DB
     */
    public PortfolioEntryRiskListView(PortfolioEntryRisk portfolioEntryRisk) {

        this.id = portfolioEntryRisk.id;
        this.portfolioEntryId = portfolioEntryRisk.portfolioEntry.id;
        this.name = portfolioEntryRisk.name;
        this.targetDate = portfolioEntryRisk.targetDate;
        this.dueDate = portfolioEntryRisk.targetDate;
        this.type = portfolioEntryRisk.portfolioEntryRiskType != null ? portfolioEntryRisk.portfolioEntryRiskType : null;
        this.isMitigated = portfolioEntryRisk.isMitigated;
        this.owner = portfolioEntryRisk.owner;
        this.isActive = portfolioEntryRisk.isActive;

    }

}
