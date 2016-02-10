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

import java.util.List;

import constants.IMafConstants;
import dao.delivery.DeliverableDAO;
import dao.delivery.RequirementDAO;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.FilterConfig;
import framework.utils.FilterConfig.SortStatusType;
import framework.utils.IColumnFormatter;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.HoursFormatter;
import framework.utils.formats.ListOfValuesFormatter;
import framework.utils.formats.NumberFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.delivery.Deliverable;
import models.delivery.Iteration;
import models.delivery.Requirement;
import models.delivery.RequirementPriority;
import models.delivery.RequirementSeverity;
import models.delivery.RequirementStatus;
import models.pmo.Actor;

/**
 * A requirement list view is used to display an portfolio entry row in a table.
 * 
 * @author Johann Kohler
 */
public class RequirementListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public FilterConfig<RequirementListView> filterConfig;
        public Table<RequirementListView> templateTable;

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
        public FilterConfig<RequirementListView> getFilterConfig() {
            return new FilterConfig<RequirementListView>() {
                {

                    addColumnConfiguration("isDefect", "isDefect", "object.requirement.is_defect.label", new CheckboxFilterComponent(true), true, false,
                            SortStatusType.NONE);

                    addColumnConfiguration("externalRefId", "externalRefId", "object.requirement.external_ref_id.label", new TextFieldFilterComponent("*"),
                            true, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("name", "name", "object.requirement.name.label", new TextFieldFilterComponent("*"), true, false,
                            SortStatusType.UNSORTED);

                    addColumnConfiguration("deliverables", "deliverables.name", "object.requirement.deliverables.label", new TextFieldFilterComponent("*"),
                            false, false, SortStatusType.NONE);

                    addColumnConfiguration("iteration", "iteration", "object.requirement.iteration.label", new NoneFilterComponent(), false, false,
                            SortStatusType.NONE);

                    addColumnConfiguration("category", "category", "object.requirement.category.label", new TextFieldFilterComponent("*"), false, false,
                            SortStatusType.UNSORTED);

                    ISelectableValueHolderCollection<Long> status = RequirementDAO.getRequirementStatusAsVH();
                    if (status != null && status.getValues().size() > 0) {
                        addColumnConfiguration("requirementStatus", "requirementStatus.id", "object.requirement.status.label",
                                new SelectFilterComponent(status.getValues().iterator().next().getValue(), status), true, false, SortStatusType.NONE);
                    } else {
                        addColumnConfiguration("requirementStatus", "requirementStatus.id", "object.requirement.status.label", new NoneFilterComponent(),
                                true, false, SortStatusType.NONE);
                    }

                    ISelectableValueHolderCollection<Long> priorities = RequirementDAO.getRequirementPriorityAsVH();
                    if (priorities != null && priorities.getValues().size() > 0) {
                        addColumnConfiguration("requirementPriority", "requirementPriority.id", "object.requirement.priority.label",
                                new SelectFilterComponent(priorities.getValues().iterator().next().getValue(), priorities), false, false,
                                SortStatusType.NONE);
                    } else {
                        addColumnConfiguration("requirementPriority", "requirementPriority.id", "object.requirement.priority.label",
                                new NoneFilterComponent(), false, false, SortStatusType.NONE);
                    }

                    ISelectableValueHolderCollection<Long> severities = RequirementDAO.getRequirementSeverityAsVH();
                    if (severities != null && severities.getValues().size() > 0) {
                        addColumnConfiguration("requirementSeverity", "requirementSeverity.id", "object.requirement.severity.label",
                                new SelectFilterComponent(severities.getValues().iterator().next().getValue(), severities), false, false,
                                SortStatusType.NONE);
                    } else {
                        addColumnConfiguration("requirementSeverity", "requirementSeverity.id", "object.requirement.severity.label",
                                new NoneFilterComponent(), false, false, SortStatusType.NONE);
                    }

                    addColumnConfiguration("isScoped", "isScoped", "object.requirement.is_scoped.label", new CheckboxFilterComponent(true), false, false,
                            SortStatusType.NONE);

                    addColumnConfiguration("author", "author.id", "object.requirement.author.label",
                            new AutocompleteFilterComponent(controllers.routes.JsonController.manager().url()), false, false, SortStatusType.NONE);

                    addColumnConfiguration("storyPoints", "storyPoints", "object.requirement.story_points.label", new NumericFieldFilterComponent("0", "="),
                            false, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("initialEstimation", "initialEstimation", "object.requirement.initial_estimation.label",
                            new NumericFieldFilterComponent("0", "="), false, false, SortStatusType.UNSORTED);

                    addColumnConfiguration("effort", "effort", "object.requirement.effort.label", new NumericFieldFilterComponent("0", "="), false, false,
                            SortStatusType.UNSORTED);

                    addColumnConfiguration("remainingEffort", "remainingEffort", "object.requirement.remaining_effort.label",
                            new NumericFieldFilterComponent("0", "="), false, false, SortStatusType.UNSORTED);

                    addCustomAttributesColumns("id", Requirement.class);

                }
            };
        }

        /**
         * Get the table.
         */
        public Table<RequirementListView> getTable(II18nMessagesPlugin i18nMessagesPlugin) {
            return new Table<RequirementListView>() {
                {

                    setIdFieldName("id");

                    addColumn("isDefect", "isDefect", "object.requirement.is_defect.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isDefect", new BooleanFormatter<RequirementListView>());

                    addColumn("externalRefId", "externalRefId", "object.requirement.external_ref_id.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("externalRefId", new ObjectFormatter<RequirementListView>());

                    addColumn("name", "name", "object.requirement.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<RequirementListView>());

                    addColumn("deliverables", "deliverables", "object.requirement.deliverables.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deliverables", new ListOfValuesFormatter<RequirementListView>());
                    this.setColumnValueCssClass("deliverables", "rowlink-skip");

                    addColumn("iteration", "iteration", "object.requirement.iteration.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("iteration", new IColumnFormatter<RequirementListView>() {
                        @Override
                        public String apply(RequirementListView requirementListView, Object value) {
                            return views.html.modelsparts.display_iteration.render(requirementListView.iteration).body();
                        }
                    });
                    this.setColumnValueCssClass("iteration", "rowlink-skip");

                    addColumn("category", "category", "object.requirement.category.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("category", new ObjectFormatter<RequirementListView>());

                    addColumn("requirementStatus", "requirementStatus", "object.requirement.status.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("requirementStatus", new IColumnFormatter<RequirementListView>() {
                        @Override
                        public String apply(RequirementListView requirementListView, Object value) {
                            return views.html.framework_views.parts.formats.display_value_holder.render(requirementListView.requirementStatus, true).body();
                        }
                    });

                    addColumn("requirementPriority", "requirementPriority", "object.requirement.priority.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("requirementPriority", new IColumnFormatter<RequirementListView>() {
                        @Override
                        public String apply(RequirementListView requirementListView, Object value) {
                            return views.html.framework_views.parts.formats.display_value_holder.render(requirementListView.requirementPriority, true).body();
                        }
                    });

                    addColumn("requirementSeverity", "requirementSeverity", "object.requirement.severity.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("requirementSeverity", new IColumnFormatter<RequirementListView>() {
                        @Override
                        public String apply(RequirementListView requirementListView, Object value) {
                            return views.html.framework_views.parts.formats.display_value_holder.render(requirementListView.requirementSeverity, true).body();
                        }
                    });

                    addColumn("isScoped", "isScoped", "object.requirement.is_scoped.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isScoped", new BooleanFormatter<RequirementListView>());

                    addColumn("author", "author", "object.requirement.author.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("author", new IColumnFormatter<RequirementListView>() {
                        @Override
                        public String apply(RequirementListView requirementListView, Object value) {
                            return views.html.modelsparts.display_actor.render(requirementListView.author).body();
                        }
                    });
                    this.setColumnValueCssClass("author", "rowlink-skip");

                    addColumn("storyPoints", "storyPoints", "object.requirement.story_points.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("storyPoints", new NumberFormatter<RequirementListView>());

                    addColumn("initialEstimation", "initialEstimation", "object.requirement.initial_estimation.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("initialEstimation", new HoursFormatter<RequirementListView>());

                    addColumn("effort", "effort", "object.requirement.effort.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("effort", new HoursFormatter<RequirementListView>());

                    addColumn("remainingEffort", "remainingEffort", "object.requirement.remaining_effort.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("remainingEffort", new HoursFormatter<RequirementListView>());

                    addCustomAttributeColumns(i18nMessagesPlugin, Requirement.class);

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<RequirementListView>(IMafConstants.EDIT_URL_FORMAT,
                            new StringFormatFormatter.Hook<RequirementListView>() {
                        @Override
                        public String convert(RequirementListView requirementListView) {
                            return controllers.core.routes.PortfolioEntryDeliveryController
                                    .manageRequirement(requirementListView.portfolioEntryId, requirementListView.id).url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    this.setLineAction(new IColumnFormatter<RequirementListView>() {
                        @Override
                        public String apply(RequirementListView requirementListView, Object value) {
                            return controllers.core.routes.PortfolioEntryDeliveryController
                                    .viewRequirement(requirementListView.portfolioEntryId, requirementListView.id).url();
                        }
                    });

                    setEmptyMessageKey("object.requirement.table.empty");
                }
            };
        }

    }

    /**
     * Default constructor.
     */
    public RequirementListView() {
    }

    public Long id;
    public Long portfolioEntryId;

    public boolean isDefect;
    public String externalRefId;
    public String name;
    public String category;
    public RequirementStatus requirementStatus;
    public RequirementPriority requirementPriority;
    public RequirementSeverity requirementSeverity;
    public Actor author;
    public Integer storyPoints;
    public Double initialEstimation;
    public Double effort;
    public Double remainingEffort;
    public Boolean isScoped;
    public Iteration iteration;
    public List<Deliverable> deliverables;

    /**
     * Construct a requirement list view with a DB entry.
     * 
     * @param requirement
     *            the requirement in the DB
     */
    public RequirementListView(Requirement requirement) {

        this.id = requirement.id;
        this.portfolioEntryId = requirement.portfolioEntry.id;

        this.isDefect = requirement.isDefect;
        this.externalRefId = requirement.externalRefId;
        this.name = requirement.name;
        this.category = requirement.category;
        this.requirementStatus = requirement.requirementStatus;
        this.requirementPriority = requirement.requirementPriority;
        this.requirementSeverity = requirement.requirementSeverity;
        this.author = requirement.author;
        this.storyPoints = requirement.storyPoints;
        this.initialEstimation = requirement.initialEstimation;
        this.effort = requirement.effort;
        this.remainingEffort = requirement.remainingEffort;
        this.isScoped = requirement.isScoped;
        this.iteration = requirement.iteration;
        this.deliverables = DeliverableDAO.getDeliverableAsListByRequirement(requirement.id);
    }

}
