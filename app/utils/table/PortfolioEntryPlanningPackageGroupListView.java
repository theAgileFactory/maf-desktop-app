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

import models.pmo.PortfolioEntryPlanningPackageGroup;
import constants.IMafConstants;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;

/**
 * A portfolio entry planning package group list view is used to display a
 * package group row in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryPlanningPackageGroupListView {

    public static Table<PortfolioEntryPlanningPackageGroupListView> templateTable = new Table<PortfolioEntryPlanningPackageGroupListView>() {
        {

            setIdFieldName("id");

            addColumn("name", "name", "object.portfolio_entry_planning_package_group.name.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("name", new ObjectFormatter<PortfolioEntryPlanningPackageGroupListView>());

            addColumn("description", "description", "object.portfolio_entry_planning_package_group.description.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("description", new ObjectFormatter<PortfolioEntryPlanningPackageGroupListView>());

            addColumn("isActive", "isActive", "object.portfolio_entry_planning_package_group.is_active.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("isActive", new BooleanFormatter<PortfolioEntryPlanningPackageGroupListView>());

            addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("editActionLink", new StringFormatFormatter<PortfolioEntryPlanningPackageGroupListView>(IMafConstants.EDIT_URL_FORMAT,
                    new StringFormatFormatter.Hook<PortfolioEntryPlanningPackageGroupListView>() {
                        @Override
                        public String convert(PortfolioEntryPlanningPackageGroupListView packageGroupListView) {
                            return controllers.admin.routes.ConfigurationPlanningPackageController.managePackageGroup(packageGroupListView.id).url();
                        }
                    }));
            setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

            addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<PortfolioEntryPlanningPackageGroupListView>() {
                @Override
                public String apply(PortfolioEntryPlanningPackageGroupListView packageGroupListView, Object value) {
                    String deleteConfirmationMessage =
                            MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION, Msg.get("default.delete.confirmation.message"));
                    String url = controllers.admin.routes.ConfigurationPlanningPackageController.deletePackageGroup(packageGroupListView.id).url();
                    return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                }
            });
            setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

            this.setLineAction(new IColumnFormatter<PortfolioEntryPlanningPackageGroupListView>() {
                @Override
                public String apply(PortfolioEntryPlanningPackageGroupListView packageGroupListView, Object value) {
                    return controllers.admin.routes.ConfigurationPlanningPackageController.viewPackageGroup(packageGroupListView.id).url();
                }
            });

            setEmptyMessageKey("object.portfolio_entry_planning_package_group.table.empty");

        }
    };

    /**
     * Default constructor.
     */
    public PortfolioEntryPlanningPackageGroupListView() {
    }

    public Long id;

    public String name;

    public String description;

    public boolean isActive;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param packageGroup
     *            the package group in the DB
     */
    public PortfolioEntryPlanningPackageGroupListView(PortfolioEntryPlanningPackageGroup packageGroup) {

        this.id = packageGroup.id;
        this.name = packageGroup.name;
        this.description = packageGroup.description;
        this.isActive = packageGroup.isActive;

    }
}
