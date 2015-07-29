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
import java.util.List;

import models.pmo.PortfolioEntryType;
import models.pmo.PortfolioType;
import models.pmo.StakeholderType;
import constants.IMafConstants;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.ListOfValuesFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;

/**
 * A stakeholder type list view is used to display a stakeholder type row in a
 * table.
 * 
 * @author Johann Kohler
 */
public class StakeholderTypeListView {

    public static Table<StakeholderTypeListView> templateTable = new Table<StakeholderTypeListView>() {
        {

            setIdFieldName("id");

            addColumn("name", "name", "object.stakeholder_type.name.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("name", new ObjectFormatter<StakeholderTypeListView>());

            addColumn("description", "description", "object.stakeholder_type.description.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("description", new ObjectFormatter<StakeholderTypeListView>());

            addColumn("selectable", "selectable", "object.stakeholder_type.selectable.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("selectable", new BooleanFormatter<StakeholderTypeListView>());

            addColumn("portfolioTypes", "portfolioTypes", "object.stakeholder_type.portolio_types.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("portfolioTypes", new ListOfValuesFormatter<StakeholderTypeListView>());

            addColumn("portfolioEntryTypes", "portfolioEntryTypes", "object.stakeholder_type.portolio_entry_types.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("portfolioEntryTypes", new ListOfValuesFormatter<StakeholderTypeListView>());

            addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("editActionLink", new StringFormatFormatter<StakeholderTypeListView>(IMafConstants.EDIT_URL_FORMAT,
                    new StringFormatFormatter.Hook<StakeholderTypeListView>() {
                        @Override
                        public String convert(StakeholderTypeListView stakeholderTypeListView) {
                            return controllers.admin.routes.ConfigurationActorAndOrgUnitController.manageStakeholderType(stakeholderTypeListView.id).url();
                        }
                    }));
            setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

            addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<StakeholderTypeListView>() {
                @Override
                public String apply(StakeholderTypeListView stakeholderTypeListView, Object value) {
                    String deleteConfirmationMessage =
                            MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION, Msg.get("default.delete.confirmation.message"));
                    String url = controllers.admin.routes.ConfigurationActorAndOrgUnitController.deleteStakeholderType(stakeholderTypeListView.id).url();
                    return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                }
            });
            setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

            setEmptyMessageKey("object.stakeholder_type.table.empty");

        }
    };

    /**
     * Default constructor.
     */
    public StakeholderTypeListView() {
    }

    public Long id;

    public String name;

    public String description;

    public boolean selectable;

    public List<PortfolioType> portfolioTypes;

    public List<PortfolioEntryType> portfolioEntryTypes;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param stakeholderType
     *            the stakeholder type in the DB
     */
    public StakeholderTypeListView(StakeholderType stakeholderType) {

        this.id = stakeholderType.id;
        this.name = stakeholderType.name;
        this.description = stakeholderType.description;
        this.selectable = stakeholderType.selectable;
        this.portfolioTypes = stakeholderType.portfolioTypes;
        this.portfolioEntryTypes = stakeholderType.portfolioEntryTypes;

    }
}
