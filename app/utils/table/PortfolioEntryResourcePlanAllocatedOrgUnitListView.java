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

import constants.IMafConstants;
import dao.pmo.OrgUnitDao;
import dao.pmo.PortfolioEntryDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.FilterConfig;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.StringFormatFormatter;
import models.finance.Currency;
import models.finance.PortfolioEntryResourcePlanAllocatedOrgUnit;
import models.finance.PortfolioEntryResourcePlanAllocationStatusType;
import models.pmo.Actor;
import models.pmo.OrgUnit;
import models.pmo.PortfolioEntryPlanningPackage;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;

/**
 * A portfolio entry resource plan allocated org unit list view is used to
 * display a portfolio entry allocated org unit in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryResourcePlanAllocatedOrgUnitListView extends AllocatedOrgUnitListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class PortfolioEntryResourcePlanTableDefinition extends TableDefinition {

        public FilterConfig<PortfolioEntryResourcePlanAllocatedOrgUnitListView> filterConfig;
        public Table<PortfolioEntryResourcePlanAllocatedOrgUnitListView> templateTable;

        public PortfolioEntryResourcePlanTableDefinition(II18nMessagesPlugin i18nMessagesPlugin) {
            super(i18nMessagesPlugin);
            this.templateTable = getTable(i18nMessagesPlugin);
            this.filterConfig = getFilterConfig();
        }

        public FilterConfig<PortfolioEntryResourcePlanAllocatedOrgUnitListView> getFilterConfig() {

            return new FilterConfig<PortfolioEntryResourcePlanAllocatedOrgUnitListView>() {
                {
                    initFilterConfig(this);

                    ISelectableValueHolderCollection<Long> orgUnits = OrgUnitDao.getOrgUnitActiveAsVH();
                    addColumnConfiguration("orgUnit", "orgUnit.id", "object.allocated_resource.org_unit.label",
                            new FilterConfig.SelectFilterComponent(orgUnits.getValues().isEmpty() ? null : orgUnits.getValues().iterator().next().getValue(), orgUnits, new String[]{"name"}), true, false, FilterConfig.SortStatusType.UNSORTED);
                }
            };
        }


        /**
         * Get the table.
         *
         * @param i18nMessagesPlugin
         *            the i18n messages service
         */
        public Table<PortfolioEntryResourcePlanAllocatedOrgUnitListView> getTable(II18nMessagesPlugin i18nMessagesPlugin) {
            return new Table<PortfolioEntryResourcePlanAllocatedOrgUnitListView>() {
                {
                    addColumn("orgUnit", "orgUnit", "object.allocated_resource.org_unit.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter(
                            "orgUnit",
                            (portfolioEntryResourcePlanAllocatedOrgUnitListView, value) ->
                                    views.html.modelsparts.display_org_unit.render(portfolioEntryResourcePlanAllocatedOrgUnitListView.orgUnit).body()
                    );
                    setColumnValueCssClass("orgUnit", "rowlink-skip");

                    initTable(this, i18nMessagesPlugin);

                    addColumn("reallocate", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("reallocate",
                            new StringFormatFormatter<>(
                                    "<a href=\"%s\"><span class=\"fa fa-user\"></span></a>",
                                    portfolioEntryResourcePlanAllocatedOrgUnitListView -> controllers.core.routes.PortfolioEntryPlanningController
                                            .reallocateOrgUnit(
                                                    portfolioEntryResourcePlanAllocatedOrgUnitListView.portfolioEntryId,
                                                    portfolioEntryResourcePlanAllocatedOrgUnitListView.id
                                            ).url()
                            )
                    );
                    setColumnCssClass("reallocate", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("reallocate", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter(
                            "editActionLink",
                            new StringFormatFormatter<>(
                                    IMafConstants.EDIT_URL_FORMAT,
                                    (StringFormatFormatter.Hook<PortfolioEntryResourcePlanAllocatedOrgUnitListView>) portfolioEntryResourcePlanAllocatedOrgUnitListView ->
                                            controllers.core.routes.PortfolioEntryPlanningController
                                                    .manageAllocatedOrgUnit(portfolioEntryResourcePlanAllocatedOrgUnitListView.portfolioEntryId, portfolioEntryResourcePlanAllocatedOrgUnitListView.id)
                                                    .url()
                            )
                    );
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("removeActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("removeActionLink", (portfolioEntryResourcePlanAllocatedOrgUnitListView, value) -> {
                        String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                Msg.get("default.delete.confirmation.message"));
                        String url = controllers.core.routes.PortfolioEntryPlanningController
                                .deleteAllocatedOrgUnit(portfolioEntryResourcePlanAllocatedOrgUnitListView.portfolioEntryId,
                                        portfolioEntryResourcePlanAllocatedOrgUnitListView.id)
                                .url();
                        return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                    });
                    setColumnCssClass("removeActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("removeActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");
                }
            };

        }


    }

    public OrgUnit orgUnit;

    /**
     * Default constructor.
     */
    public PortfolioEntryResourcePlanAllocatedOrgUnitListView() {
        super();
    }

    /**
     * Construct a list view with a DB entry.
     * 
     * @param allocatedOrgUnit
     *            the allocated org unit in the DB
     */
    public PortfolioEntryResourcePlanAllocatedOrgUnitListView(PortfolioEntryResourcePlanAllocatedOrgUnit allocatedOrgUnit) {
        super(allocatedOrgUnit);
        this.orgUnit = allocatedOrgUnit.orgUnit;
    }

}
