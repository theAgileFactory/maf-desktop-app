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
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.pmo.PortfolioEntryPlanningPackagePattern;
import models.pmo.PortfolioEntryPlanningPackageType;

/**
 * A portfolio entry planning package pattern list view is used to display a
 * package pattern row in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryPlanningPackagePatternListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<PortfolioEntryPlanningPackagePatternListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition() {
            this.templateTable = getTable();
        }

        /**
         * Get the table.
         */
        public Table<PortfolioEntryPlanningPackagePatternListView> getTable() {
            return new Table<PortfolioEntryPlanningPackagePatternListView>() {
                {

                    setIdFieldName("id");

                    addColumn("changeOrder", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("changeOrder", new IColumnFormatter<PortfolioEntryPlanningPackagePatternListView>() {
                        @Override
                        public String apply(PortfolioEntryPlanningPackagePatternListView packagePatternListView, Object value) {
                            return "<a href=\""
                                    + controllers.admin.routes.ConfigurationPlanningPackageController
                                            .changePackagePatternOrder(packagePatternListView.id, false).url()
                                    + "\"><span class=\"fa fa-arrow-down\"></span></a>&nbsp;" + "<a href=\""
                                    + controllers.admin.routes.ConfigurationPlanningPackageController
                                            .changePackagePatternOrder(packagePatternListView.id, true).url()
                                    + "\"><span class=\"fa fa-arrow-up\"></span></a>";
                        }
                    });
                    setColumnCssClass("changeOrder", IMafConstants.BOOTSTRAP_COLUMN_1);

                    addColumn("name", "name", "object.portfolio_entry_planning_package.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<PortfolioEntryPlanningPackagePatternListView>());

                    addColumn("description", "description", "object.portfolio_entry_planning_package.description.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("description", new ObjectFormatter<PortfolioEntryPlanningPackagePatternListView>());

                    addColumn("isOpex", "isOpex", "object.portfolio_entry_planning_package.expenditure_type.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isOpex", new IColumnFormatter<PortfolioEntryPlanningPackagePatternListView>() {
                        @Override
                        public String apply(PortfolioEntryPlanningPackagePatternListView portfolioEntryPlanningPackagePatternListView, Object value) {
                            return views.html.modelsparts.display_is_opex.render(portfolioEntryPlanningPackagePatternListView.isOpex).body();
                        }
                    });

                    addColumn("type", "type", "object.portfolio_entry_planning_package.type.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("type", new IColumnFormatter<PortfolioEntryPlanningPackagePatternListView>() {
                        @Override
                        public String apply(PortfolioEntryPlanningPackagePatternListView portfolioEntryPlanningPackagePatternListView, Object value) {
                            return views.html.modelsparts.display_portfolio_entry_planning_package_type
                                    .render(portfolioEntryPlanningPackagePatternListView.type).body();
                        }
                    });

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<PortfolioEntryPlanningPackagePatternListView>(
                            IMafConstants.EDIT_URL_FORMAT, new StringFormatFormatter.Hook<PortfolioEntryPlanningPackagePatternListView>() {
                        @Override
                        public String convert(PortfolioEntryPlanningPackagePatternListView packagePatternListView) {
                            return controllers.admin.routes.ConfigurationPlanningPackageController
                                    .managePackagePattern(packagePatternListView.packageGroupId, packagePatternListView.id).url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<PortfolioEntryPlanningPackagePatternListView>() {
                        @Override
                        public String apply(PortfolioEntryPlanningPackagePatternListView packagePatternListView, Object value) {
                            String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                    Msg.get("default.delete.confirmation.message"));
                            String url = controllers.admin.routes.ConfigurationPlanningPackageController.deletePackagePattern(packagePatternListView.id)
                                    .url();
                            return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                        }
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    setEmptyMessageKey("object.portfolio_entry_planning_package.pattern.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public PortfolioEntryPlanningPackagePatternListView() {
    }

    public Long packageGroupId;
    public Long id;

    public String name;

    public String description;

    public PortfolioEntryPlanningPackageType type;

    public boolean isOpex;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param packagePattern
     *            the package pattern in the DB
     * @param messagesPlugin
     *            the i18n service
     */
    public PortfolioEntryPlanningPackagePatternListView(PortfolioEntryPlanningPackagePattern packagePattern, II18nMessagesPlugin messagesPlugin) {

        this.id = packagePattern.id;
        this.packageGroupId = packagePattern.portfolioEntryPlanningPackageGroup.id;

        this.name = packagePattern.name;
        this.description = packagePattern.description;
        this.type = packagePattern.portfolioEntryPlanningPackageType;
        this.isOpex = packagePattern.isOpex;

    }
}
