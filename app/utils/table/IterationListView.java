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

import java.util.Date;

import constants.IMafConstants;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.FilterConfig;
import framework.utils.FilterConfig.SortStatusType;
import framework.utils.IColumnFormatter;
import framework.utils.Table;
import framework.utils.Utilities;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.NumberFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.delivery.Iteration;

/**
 * An iteration list view is used to display an iteration row in a table.
 * 
 * @author Johann Kohler
 */
public class IterationListView {

    public static class TableDefinition {

        public FilterConfig<IterationListView> filterConfig;

        public TableDefinition(II18nMessagesPlugin i18nMessagesPlugin) {
            this.filterConfig = getFilterConfig();
            this.templateTable = getTable(i18nMessagesPlugin);
        }

        /**
         * Get the filter config.
         */
        public FilterConfig<IterationListView> getFilterConfig() {
            return new FilterConfig<IterationListView>() {
                {

                    addColumnConfiguration("name", "name", "object.iteration.name.label", new TextFieldFilterComponent("*"), true, false,
                            SortStatusType.UNSORTED);

                    addColumnConfiguration("storyPoints", "storyPoints", "object.iteration.story_points.label", new NumericFieldFilterComponent("0", "="),
                            true, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("isClosed", "isClosed", "object.iteration.is_closed.label", new CheckboxFilterComponent(false), true, false,
                            SortStatusType.NONE);

                    addColumnConfiguration("startDate", "startDate", "object.iteration.start_date.label",
                            new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), false, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("endDate", "endDate", "object.iteration.end_date.label",
                            new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.ASC);

                    addColumnConfiguration("source", "source", "object.iteration.source.label", new TextFieldFilterComponent("*"), false, false,
                            SortStatusType.UNSORTED);

                    addCustomAttributesColumns("id", Iteration.class);
                }
            };
        }

        public Table<IterationListView> templateTable;

        /**
         * Get the table.
         */
        public Table<IterationListView> getTable(II18nMessagesPlugin i18nMessagesPlugin) {
            return new Table<IterationListView>() {
                {

                    setIdFieldName("id");

                    addColumn("name", "name", "object.iteration.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<IterationListView>());

                    addColumn("storyPoints", "storyPoints", "object.iteration.story_points.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("storyPoints", new NumberFormatter<IterationListView>());

                    addColumn("isClosed", "isClosed", "object.iteration.is_closed.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isClosed", new BooleanFormatter<IterationListView>());

                    addColumn("startDate", "startDate", "object.iteration.start_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("startDate", new DateFormatter<IterationListView>());

                    addColumn("endDate", "endDate", "object.iteration.end_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("endDate", new DateFormatter<IterationListView>());

                    addColumn("source", "source", "object.iteration.source.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("source", new ObjectFormatter<IterationListView>());

                    addCustomAttributeColumns(i18nMessagesPlugin, Iteration.class);

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink",
                            new StringFormatFormatter<IterationListView>(IMafConstants.EDIT_URL_FORMAT, new StringFormatFormatter.Hook<IterationListView>() {
                        @Override
                        public String convert(IterationListView iterationListView) {
                            return controllers.core.routes.PortfolioEntryDeliveryController
                                    .editIteration(iterationListView.portfolioEntryId, iterationListView.id).url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    this.setLineAction(new IColumnFormatter<IterationListView>() {
                        @Override
                        public String apply(IterationListView iterationListView, Object value) {
                            return controllers.core.routes.PortfolioEntryDeliveryController
                                    .viewIteration(iterationListView.portfolioEntryId, iterationListView.id).url();
                        }
                    });

                    setEmptyMessageKey("object.iteration.table.empty");
                }
            };
        }

    }

    /**
     * Default constructor.
     */
    public IterationListView() {
    }

    public Long id;
    public Long portfolioEntryId;
    public String name;
    public Boolean isClosed;
    public Integer storyPoints;
    public Date startDate;
    public Date endDate;
    public String source;

    /**
     * Construct an iteration list view with a DB entry.
     * 
     * @param iteration
     *            the iteration in the DB
     */
    public IterationListView(Iteration iteration) {

        this.id = iteration.id;
        this.portfolioEntryId = iteration.portfolioEntry.id;

        this.name = iteration.name;
        this.isClosed = iteration.isClosed;
        this.storyPoints = iteration.storyPoints;
        this.startDate = iteration.startDate;
        this.endDate = iteration.endDate;
        this.source = iteration.source;
    }
}
