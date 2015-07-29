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

import models.delivery.Release;
import models.pmo.Actor;
import framework.utils.FilterConfig;
import framework.utils.IColumnFormatter;
import framework.utils.Table;
import framework.utils.Utilities;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.NumberFormatter;
import framework.utils.formats.ObjectFormatter;

/**
 * A release list view is used to display a release row in a table.
 * 
 * @author Johann Kohler
 */
public class ReleaseListView {

    public static FilterConfig<ReleaseListView> filterConfig = getFilterConfig();

    /**
     * Get the filter config.
     */
    public static FilterConfig<ReleaseListView> getFilterConfig() {
        return new FilterConfig<ReleaseListView>() {
            {

                addColumnConfiguration("isActive", "isActive", "object.release.is_active.label", new CheckboxFilterComponent(true), false, false,
                        SortStatusType.UNSORTED);

                addColumnConfiguration("name", "name", "object.release.name.label", new TextFieldFilterComponent("*"), true, false, SortStatusType.UNSORTED);

                addColumnConfiguration("capacity", "capacity", "object.release.capacity.label", new NumericFieldFilterComponent("0", "="), false, false,
                        SortStatusType.UNSORTED);

                addColumnConfiguration("cutOffDate", "cutOffDate", "object.release.cut_off_date.label", new DateRangeFilterComponent(new Date(), new Date(),
                        Utilities.getDefaultDatePattern()), false, false, SortStatusType.UNSORTED);

                addColumnConfiguration("endTestsDate", "endTestsDate", "object.release.end_tests_date.label", new DateRangeFilterComponent(new Date(),
                        new Date(), Utilities.getDefaultDatePattern()), false, false, SortStatusType.UNSORTED);

                addColumnConfiguration("deploymentDate", "deploymentDate", "object.release.deployment_date.label", new DateRangeFilterComponent(new Date(),
                        new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.ASC);

                addColumnConfiguration("manager", "manager.id", "object.release.manager.label", new AutocompleteFilterComponent(
                        controllers.routes.JsonController.manager().url()), true, false, SortStatusType.UNSORTED);

                addCustomAttributesColumns("id", Release.class);

            }
        };
    }

    public static Table<ReleaseListView> templateTable = getTable();

    /**
     * Get the table.
     */
    public static Table<ReleaseListView> getTable() {
        return new Table<ReleaseListView>() {
            {
                setIdFieldName("id");

                addColumn("isActive", "isActive", "object.release.is_active.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("isActive", new BooleanFormatter<ReleaseListView>());

                addColumn("name", "name", "object.release.name.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("name", new ObjectFormatter<ReleaseListView>());

                addColumn("capacity", "capacity", "object.release.capacity.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("capacity", new NumberFormatter<ReleaseListView>());

                addColumn("cutOffDate", "cutOffDate", "object.release.cut_off_date.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("cutOffDate", new DateFormatter<ReleaseListView>());

                addColumn("endTestsDate", "endTestsDate", "object.release.end_tests_date.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("endTestsDate", new DateFormatter<ReleaseListView>());

                addColumn("deploymentDate", "deploymentDate", "object.release.deployment_date.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("deploymentDate", new DateFormatter<ReleaseListView>());

                addColumn("manager", "manager", "object.release.manager.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("manager", new IColumnFormatter<ReleaseListView>() {
                    @Override
                    public String apply(ReleaseListView releaseListView, Object value) {
                        return views.html.modelsparts.display_actor.render(releaseListView.manager).body();
                    }
                });
                this.setColumnValueCssClass("manager", "rowlink-skip");

                addCustomAttributeColumns(Release.class);

                this.setLineAction(new IColumnFormatter<ReleaseListView>() {
                    @Override
                    public String apply(ReleaseListView releaseListView, Object value) {
                        return controllers.core.routes.ReleaseController.view(releaseListView.id, 0).url();
                    }
                });

                setEmptyMessageKey("object.release.table.empty");

            }
        };

    }

    /**
     * Default constructor.
     */
    public ReleaseListView() {
    }

    public Long id;

    public boolean isActive;

    public String name;

    public Integer capacity;

    public Date cutOffDate;

    public Date endTestsDate;

    public Date deploymentDate;

    public Actor manager;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param release
     *            the release in the DB
     */
    public ReleaseListView(Release release) {
        this.id = release.id;
        this.isActive = release.isActive;
        this.name = release.name;
        this.capacity = release.capacity;
        this.cutOffDate = release.cutOffDate;
        this.endTestsDate = release.endTestsDate;
        this.deploymentDate = release.deploymentDate;
        this.manager = release.manager;
    }

}
