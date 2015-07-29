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

import models.pmo.PortfolioEntryRiskType;
import constants.IMafConstants;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;

/**
 * A portfolio entry risk type list view is used to display a portfolio entry
 * risk type row in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryRiskTypeListView {

    public static Table<PortfolioEntryRiskTypeListView> templateTable = new Table<PortfolioEntryRiskTypeListView>() {
        {

            setIdFieldName("id");

            addColumn("name", "name", "object.portfolio_entry_risk_type.name.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("name", new ObjectFormatter<PortfolioEntryRiskTypeListView>());

            addColumn("description", "description", "object.portfolio_entry_risk_type.description.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("description", new ObjectFormatter<PortfolioEntryRiskTypeListView>());

            addColumn("selectable", "selectable", "object.portfolio_entry_risk_type.selectable.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("selectable", new BooleanFormatter<PortfolioEntryRiskTypeListView>());

            addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("editActionLink", new StringFormatFormatter<PortfolioEntryRiskTypeListView>(IMafConstants.EDIT_URL_FORMAT,
                    new StringFormatFormatter.Hook<PortfolioEntryRiskTypeListView>() {
                        @Override
                        public String convert(PortfolioEntryRiskTypeListView portfolioEntryRiskTypeListView) {
                            return controllers.admin.routes.ConfigurationRegisterController.manageRiskType(portfolioEntryRiskTypeListView.id).url();
                        }
                    }));
            setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

            addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<PortfolioEntryRiskTypeListView>() {
                @Override
                public String apply(PortfolioEntryRiskTypeListView portfolioEntryRiskTypeListView, Object value) {
                    String deleteConfirmationMessage =
                            MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION, Msg.get("default.delete.confirmation.message"));
                    String url = controllers.admin.routes.ConfigurationRegisterController.deleteRiskType(portfolioEntryRiskTypeListView.id).url();
                    return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                }
            });
            setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

            setEmptyMessageKey("object.portfolio_entry_risk_type.table.empty");

        }
    };

    /**
     * Default constructor.
     */
    public PortfolioEntryRiskTypeListView() {
    }

    public Long id;

    public String name;

    public String description;

    public boolean selectable;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param riskType
     *            the portfolio entry risk type in the DB
     */
    public PortfolioEntryRiskTypeListView(PortfolioEntryRiskType riskType) {

        this.id = riskType.id;
        this.name = riskType.name;
        this.description = riskType.description;
        this.selectable = riskType.selectable;

    }
}
