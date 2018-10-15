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
import controllers.core.routes;
import dao.pmo.PortfolioEntryEventDao;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.*;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.ObjectFormatter;
import models.pmo.Actor;
import models.pmo.PortfolioEntryEvent;
import models.pmo.PortfolioEntryEventType;

import java.text.MessageFormat;
import java.util.Date;

/**
 * A portfolio entry event list view is used to display an portfolio entry event
 * row in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryEventListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public FilterConfig<PortfolioEntryEventListView> filterConfig;

        /**
         * Default constructor.
         */
        public TableDefinition(II18nMessagesPlugin i18nMessagesPlugin) {
            this.filterConfig = getFilterConfig();
            this.templateTable = getTable(i18nMessagesPlugin);
        }

        /**
         * Get the filter config.
         */
        public FilterConfig<PortfolioEntryEventListView> getFilterConfig() {
            return new FilterConfig<PortfolioEntryEventListView>() {
                {

                    ISelectableValueHolderCollection<Long> eventTypes = PortfolioEntryEventDao.getPEEventTypeActiveAsVH(true);
                    addColumnConfiguration("portfolioEntryEventType", "portfolioEntryEventType.id", "object.portfolio_entry_event.type.label",
                            new SelectFilterComponent(eventTypes.getValues().iterator().next().getValue(), eventTypes, new String[]{"portfolioEntryEventType.name"}), true, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("message", "message", "object.portfolio_entry_event.message.label", new TextFieldFilterComponent("*"), true, false,
                            SortStatusType.UNSORTED);

                    addColumnConfiguration("actor", "actor.id", "object.portfolio_entry_event.actor.label",
                            new AutocompleteFilterComponent(controllers.routes.JsonController.manager().url()), true, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("creationDate", "creationDate", "object.portfolio_entry_event.creation_date.label",
                            new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.DESC);

                    addCustomAttributesColumns("id", PortfolioEntryEvent.class);

                }
            };
        }

        public Table<PortfolioEntryEventListView> templateTable;

        /**
         * Get the table.
         */
        public Table<PortfolioEntryEventListView> getTable(II18nMessagesPlugin i18nMessagesPlugin) {
            return new Table<PortfolioEntryEventListView>() {
                {
                    setIdFieldName("id");

                    addColumn("portfolioEntryEventType", "portfolioEntryEventType", "object.portfolio_entry_event.type.label",
                            Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("portfolioEntryEventType", (portfolioEntryEventListView, value) -> views.html.modelsparts.display_portfolio_entry_event_type.render(portfolioEntryEventListView.portfolioEntryEventType)
                            .body());

                    addColumn("creationDate", "creationDate", "object.portfolio_entry_event.creation_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("creationDate", new DateFormatter<>());

                    addColumn("actor", "actor", "object.portfolio_entry_event.actor.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("actor", (portfolioEntryEventListView, value) -> views.html.modelsparts.display_actor.render(portfolioEntryEventListView.actor).body());
                    this.setColumnValueCssClass("actor", "rowlink-skip");

                    addCustomAttributeColumns(i18nMessagesPlugin, PortfolioEntryEvent.class);

                    addColumn("message", "message", "object.portfolio_entry_event.message.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("message", new ObjectFormatter<>());

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", (portfolioEntryEventListView, value) -> {
                        String actionLink = null;
                        if (!portfolioEntryEventListView.readOnly) {
                            String url = routes.PortfolioEntryStatusReportingController
                                    .manageEvent(portfolioEntryEventListView.portfolioEntryId, portfolioEntryEventListView.id).url();

                            actionLink = views.html.framework_views.parts.formats.display_with_format.render(url, IMafConstants.EDIT_URL_FORMAT).body();
                        }
                        return actionLink;
                    });
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", (portfolioEntryEventListView, value) -> {
                        String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                Msg.get("default.delete.confirmation.message"));
                        String url = controllers.core.routes.PortfolioEntryStatusReportingController
                                .deleteEvent(portfolioEntryEventListView.portfolioEntryId, portfolioEntryEventListView.id).url();
                        return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    setEmptyMessageKey("object.portfolio_entry_event.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public PortfolioEntryEventListView() {
    }

    public Long id;

    public Long portfolioEntryId;

    public Date creationDate;

    public PortfolioEntryEventType portfolioEntryEventType;

    public Actor actor;

    public String message;

    public boolean readOnly;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param portfolioEntryEvent
     *            the portfolio entry event in the DB
     */
    public PortfolioEntryEventListView(PortfolioEntryEvent portfolioEntryEvent) {

        this.id = portfolioEntryEvent.id;
        this.portfolioEntryId = portfolioEntryEvent.portfolioEntry.id;

        this.creationDate = portfolioEntryEvent.creationDate;
        this.portfolioEntryEventType = portfolioEntryEvent.portfolioEntryEventType;
        this.actor = portfolioEntryEvent.actor;
        this.message = portfolioEntryEvent.message;
        this.readOnly = portfolioEntryEvent.portfolioEntryEventType.readOnly;
    }

}
