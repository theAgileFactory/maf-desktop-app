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
import models.pmo.PortfolioEntryPlanningPackageType;

/**
 * A portfolio entry planning package type list view is used to display a
 * portfolio entry planning package type row in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryPlanningPackageTypeListView {

    public static Table<PortfolioEntryPlanningPackageTypeListView> templateTable = new Table<PortfolioEntryPlanningPackageTypeListView>() {
        {

            setIdFieldName("id");

            addColumn("name", "name", "object.portfolio_entry_planning_package_type.name.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("name", new ObjectFormatter<PortfolioEntryPlanningPackageTypeListView>());

            addColumn("isActive", "isActive", "object.portfolio_entry_planning_package_type.is_active.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("isActive", new BooleanFormatter<PortfolioEntryPlanningPackageTypeListView>());

            addColumn("color", "color", "object.portfolio_entry_planning_package_type.css_class.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("color", new ObjectFormatter<PortfolioEntryPlanningPackageTypeListView>());

            addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("editActionLink", new StringFormatFormatter<PortfolioEntryPlanningPackageTypeListView>(IMafConstants.EDIT_URL_FORMAT,
                    new StringFormatFormatter.Hook<PortfolioEntryPlanningPackageTypeListView>() {
                @Override
                public String convert(PortfolioEntryPlanningPackageTypeListView portfolioEntryPlanningPackageTypeListView) {
                    return controllers.admin.routes.ConfigurationPlanningPackageController
                            .managePlanningPackageType(portfolioEntryPlanningPackageTypeListView.id).url();
                }
            }));
            setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

            addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<PortfolioEntryPlanningPackageTypeListView>() {
                @Override
                public String apply(PortfolioEntryPlanningPackageTypeListView portfolioEntryPlanningPackageTypeListView, Object value) {
                    String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                            Msg.get("default.delete.confirmation.message"));
                    String url = controllers.admin.routes.ConfigurationPlanningPackageController
                            .deletePlanningPackageType(portfolioEntryPlanningPackageTypeListView.id).url();
                    return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                }
            });
            setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

            setEmptyMessageKey("object.portfolio_entry_planning_package_type.table.empty");

        }
    };

    /**
     * Default constructor.
     */
    public PortfolioEntryPlanningPackageTypeListView() {
    }

    public Long id;

    public String name;

    public boolean isActive;

    public String color;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param planningPackageType
     *            the planning package type in the DB
     * @param messagesPlugin
     *            the i18n service
     */
    public PortfolioEntryPlanningPackageTypeListView(PortfolioEntryPlanningPackageType planningPackageType, II18nMessagesPlugin messagesPlugin) {

        this.id = planningPackageType.id;
        this.name = planningPackageType.name;
        this.isActive = planningPackageType.isActive;
        this.color = Color.getLabel(planningPackageType.cssClass, messagesPlugin);

    }
}
