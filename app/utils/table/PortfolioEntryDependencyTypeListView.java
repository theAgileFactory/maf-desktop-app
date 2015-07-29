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

import models.pmo.PortfolioEntryDependencyType;
import constants.IMafConstants;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;

/**
 * A portfolio entry dependency type list view is used to display a portfolio
 * entry dependency type row in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryDependencyTypeListView {

    public static Table<PortfolioEntryDependencyTypeListView> templateTable = new Table<PortfolioEntryDependencyTypeListView>() {
        {

            setIdFieldName("id");

            addColumn("name", "name", "object.portfolio_entry_dependency_type.name.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("name", new ObjectFormatter<PortfolioEntryDependencyTypeListView>());

            addColumn("contrary", "contrary", "object.portfolio_entry_dependency_type.contrary.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("contrary", new ObjectFormatter<PortfolioEntryDependencyTypeListView>());

            addColumn("description", "description", "object.portfolio_entry_dependency_type.description.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("description", new ObjectFormatter<PortfolioEntryDependencyTypeListView>());

            addColumn("isActive", "isActive", "object.portfolio_entry_dependency_type.is_active.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("isActive", new BooleanFormatter<PortfolioEntryDependencyTypeListView>());

            addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("editActionLink", new StringFormatFormatter<PortfolioEntryDependencyTypeListView>(IMafConstants.EDIT_URL_FORMAT,
                    new StringFormatFormatter.Hook<PortfolioEntryDependencyTypeListView>() {
                        @Override
                        public String convert(PortfolioEntryDependencyTypeListView portfolioEntryDependencyTypeListView) {
                            return controllers.admin.routes.ConfigurationPortfolioController.managePortfolioEntryDependencyType(
                                    portfolioEntryDependencyTypeListView.id).url();
                        }
                    }));
            setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

            addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<PortfolioEntryDependencyTypeListView>() {
                @Override
                public String apply(PortfolioEntryDependencyTypeListView portfolioEntryDependencyTypeListView, Object value) {
                    String deleteConfirmationMessage =
                            MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION, Msg.get("default.delete.confirmation.message"));
                    String url =
                            controllers.admin.routes.ConfigurationPortfolioController.deletePortfolioEntryDependencyType(
                                    portfolioEntryDependencyTypeListView.id).url();
                    return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                }
            });
            setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

            setEmptyMessageKey("object.portfolio_entry_dependency_type.table.empty");

        }
    };

    /**
     * Default constructor.
     */
    public PortfolioEntryDependencyTypeListView() {
    }

    public Long id;

    public String name;

    public String contrary;

    public String description;

    public boolean isActive;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param portfolioEntryDependencyType
     *            the portfolio entry dependency type in the DB
     */
    public PortfolioEntryDependencyTypeListView(PortfolioEntryDependencyType portfolioEntryDependencyType) {

        this.id = portfolioEntryDependencyType.id;
        this.name = portfolioEntryDependencyType.name;
        this.contrary = portfolioEntryDependencyType.contrary;
        this.description = portfolioEntryDependencyType.description;
        this.isActive = portfolioEntryDependencyType.isActive;

    }
}
