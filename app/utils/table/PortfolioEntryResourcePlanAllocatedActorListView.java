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
import controllers.routes;
import dao.pmo.OrgUnitDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.FilterConfig;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.ListOfValuesFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.finance.PortfolioEntryResourcePlanAllocatedActor;

import java.text.MessageFormat;

/**
 * A portfolio entry resource plan allocated actor list view is used to display
 * a portfolio entry allocated actor in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryResourcePlanAllocatedActorListView extends AllocatedActorListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class PortfolioEntryResourcePlanAllocatedActorTableDefinition extends TableDefinition {

        public FilterConfig<PortfolioEntryResourcePlanAllocatedActorListView> filterConfig;
        public Table<PortfolioEntryResourcePlanAllocatedActorListView> templateTable;

        /**
         * Default constructor.
         * 
         * @param i18nMessagesPlugin
         *            the i18n messages service
         */
        public PortfolioEntryResourcePlanAllocatedActorTableDefinition(II18nMessagesPlugin i18nMessagesPlugin) {
            super();
            this.filterConfig = getFilterConfig();
            this.templateTable = getTable(i18nMessagesPlugin);
        }

        /**
         * Get the filter config.
         */
        public FilterConfig<PortfolioEntryResourcePlanAllocatedActorListView> getFilterConfig() {
            return new FilterConfig<PortfolioEntryResourcePlanAllocatedActorListView>() {
                {
                    String[] actorFieldsSort = { "actor.lastName", "actor.firstName" };
                    addColumnConfiguration("actor", "actor.id", "object.allocated_resource.actor.label",
                            new AutocompleteFilterComponent(routes.JsonController.manager().url(), actorFieldsSort), true, false,
                            SortStatusType.UNSORTED);

                    ISelectableValueHolderCollection<Long> orgUnits = OrgUnitDao.getOrgUnitActiveAsVH();
                    if (orgUnits.getValues().size() > 0) {
                        addColumnConfiguration("orgUnit", "actor.orgUnit.id", "object.allocated_resource.org_unit.label",
                                new SelectFilterComponent(orgUnits.getValues().iterator().next().getValue(), orgUnits), false, false,
                                SortStatusType.NONE);
                    } else {
                        addColumnConfiguration("orgUnit", "actor.orgUnit.id", "object.allocated_resource.org_unit.label",
                                new NoneFilterComponent(), false, false, SortStatusType.NONE);
                    }

                    initFilterConfig(this);

                }
            };
        }

        /**
         * Get the table.
         * 
         * @param i18nMessagesPlugin
         *            the i18n messages service
         */
        public Table<PortfolioEntryResourcePlanAllocatedActorListView> getTable(II18nMessagesPlugin i18nMessagesPlugin) {
            return new Table<PortfolioEntryResourcePlanAllocatedActorListView>() {
                {

                    addColumn("actor", "actor", "object.allocated_resource.actor.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("actor", (portfolioEntryResourcePlanAllocatedActorListView, value) -> views.html.modelsparts.display_actor.render(portfolioEntryResourcePlanAllocatedActorListView.actor).body());
                    setColumnValueCssClass("actor", "rowlink-skip");

                    addColumn("orgUnit", "orgUnit", "object.allocated_resource.org_unit.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("orgUnit", new ListOfValuesFormatter<>());
                    this.setColumnValueCssClass("orgUnit", "rowlink-skip");

                    initTable(this, i18nMessagesPlugin);

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<>(
                            IMafConstants.EDIT_URL_FORMAT, portfolioEntryResourcePlanAllocatedActorListView -> controllers.core.routes.PortfolioEntryPlanningController
                            .manageAllocatedActor(portfolioEntryResourcePlanAllocatedActorListView.portfolioEntry.id,
                                    portfolioEntryResourcePlanAllocatedActorListView.id)
                            .url()));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("removeActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("removeActionLink", (portfolioEntryResourcePlanAllocatedActorListView, value) -> {
                        String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                Msg.get("default.delete.confirmation.message"));
                        String url = controllers.core.routes.PortfolioEntryPlanningController
                                .deleteAllocatedActor(portfolioEntryResourcePlanAllocatedActorListView.portfolioEntry.id,
                                        portfolioEntryResourcePlanAllocatedActorListView.id)
                                .url();
                        return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                    });
                    setColumnCssClass("removeActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("removeActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                }
            };
        }

    }

    /**
     * Default constructor.
     */
    public PortfolioEntryResourcePlanAllocatedActorListView() {
    }

    /**
     * Construct a list view with a DB entry.
     * 
     * @param allocatedActor
     *            the allocated actor in the DB
     */
    public PortfolioEntryResourcePlanAllocatedActorListView(PortfolioEntryResourcePlanAllocatedActor allocatedActor) {
        super(allocatedActor);
    }

}
