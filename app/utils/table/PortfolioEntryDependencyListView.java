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
import framework.utils.formats.ObjectFormatter;
import models.pmo.PortfolioEntry;
import models.pmo.PortfolioEntryDependency;

/**
 * A portfolio entry dependency list view is used to display a dependency row in
 * a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryDependencyListView {

    public static Table<PortfolioEntryDependencyListView> templateTable = getTable();

    /**
     * Get the table.
     */
    public static Table<PortfolioEntryDependencyListView> getTable() {
        return new Table<PortfolioEntryDependencyListView>() {
            {
                setIdFieldName("portfolioEntryId");

                addColumn("dependencyType", "dependencyType", "object.portfolio_entry.dependency_type.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("dependencyType", new ObjectFormatter<PortfolioEntryDependencyListView>());

                addColumn("name", "dependingPortfolioEntry", "object.portfolio_entry.name.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("name", new IColumnFormatter<PortfolioEntryDependencyListView>() {
                    @Override
                    public String apply(PortfolioEntryDependencyListView portfolioEntryDependencyListView, Object value) {
                        return views.html.modelsparts.display_portfolio_entry.render(portfolioEntryDependencyListView.dependingPortfolioEntry, false).body();
                    }
                });

                addColumn("deleteActionLink", "portfolioEntryId", "", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<PortfolioEntryDependencyListView>() {
                    @Override
                    public String apply(PortfolioEntryDependencyListView portfolioEntryDependencyListView, Object value) {
                        String unassignConfirmationMessage = MessageFormat.format(
                                "<a onclick=\"return maf_confirmAction(''{0}'');\" href=\"%s\">" + "<span class=\"fa fa-times\"></span></a>",
                                Msg.get("object.portfolio_entry.dependency.delete.confirmation"));
                        String url = controllers.core.routes.PortfolioEntryController.deleteDependency(portfolioEntryDependencyListView.portfolioEntryId,
                                portfolioEntryDependencyListView.portfolioEntryDependency.id.sourcePortfolioEntryId,
                                portfolioEntryDependencyListView.portfolioEntryDependency.id.destinationPortfolioEntryId,
                                portfolioEntryDependencyListView.portfolioEntryDependency.id.portfolioEntryDependencyTypeId).url();
                        return views.html.framework_views.parts.formats.display_with_format.render(url, unassignConfirmationMessage).body();
                    }
                });
                setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                this.setLineAction(new IColumnFormatter<PortfolioEntryDependencyListView>() {
                    @Override
                    public String apply(PortfolioEntryDependencyListView portfolioEntryDependencyListView, Object value) {
                        return controllers.core.routes.PortfolioEntryController.overview(portfolioEntryDependencyListView.dependingPortfolioEntry.id).url();
                    }
                });

                setEmptyMessageKey("object.portfolio_entry.table.empty");

            }
        };

    }

    /**
     * Default constructor.
     */
    public PortfolioEntryDependencyListView() {
    }

    public Long portfolioEntryId;

    public PortfolioEntryDependency portfolioEntryDependency;

    public PortfolioEntry dependingPortfolioEntry;

    public String dependencyType;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param portfolioEntryDependency
     *            the portfolio entry dependency in the DB
     */
    public PortfolioEntryDependencyListView(Long portfolioEntryId, PortfolioEntryDependency portfolioEntryDependency) {

        this.portfolioEntryId = portfolioEntryId;
        this.portfolioEntryDependency = portfolioEntryDependency;

        if (this.portfolioEntryDependency.getSourcePortfolioEntry().id.equals(portfolioEntryId)) {
            this.dependingPortfolioEntry = this.portfolioEntryDependency.getDestinationPortfolioEntry();
            this.dependencyType = this.portfolioEntryDependency.getPortfolioEntryDependencyType().getNameKey();
        } else {
            this.dependingPortfolioEntry = this.portfolioEntryDependency.getSourcePortfolioEntry();
            this.dependencyType = this.portfolioEntryDependency.getPortfolioEntryDependencyType().contrary;
        }

    }

}
