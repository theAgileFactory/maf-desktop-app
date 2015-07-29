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

import models.pmo.Actor;
import models.pmo.OrgUnit;
import models.pmo.OrgUnitType;
import framework.utils.IColumnFormatter;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.ObjectFormatter;

/**
 * A org unit list view is used to display an org unit row in a table.
 * 
 * @author Johann Kohler
 */
public class OrgUnitListView {

    public static Table<OrgUnitListView> templateTable = getTable();

    /**
     * Get the table.
     */
    public static Table<OrgUnitListView> getTable() {
        return new Table<OrgUnitListView>() {
            {
                setIdFieldName("id");

                addColumn("refId", "refId", "object.org_unit.ref_id.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("refId", new ObjectFormatter<OrgUnitListView>());

                addColumn("name", "name", "object.org_unit.name.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("name", new ObjectFormatter<OrgUnitListView>());

                addColumn("type", "type", "object.org_unit.type.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("type", new IColumnFormatter<OrgUnitListView>() {
                    @Override
                    public String apply(OrgUnitListView orgUnitListView, Object value) {
                        return views.html.framework_views.parts.formats.display_value_holder.render(orgUnitListView.type, true).body();
                    }
                });

                addColumn("isActive", "isActive", "object.org_unit.is_active.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("isActive", new BooleanFormatter<OrgUnitListView>());

                addColumn("manager", "manager", "object.org_unit.manager.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("manager", new IColumnFormatter<OrgUnitListView>() {
                    @Override
                    public String apply(OrgUnitListView orgUnitListView, Object value) {
                        return views.html.modelsparts.display_actor.render(orgUnitListView.manager).body();
                    }
                });
                this.setColumnValueCssClass("manager", "rowlink-skip");

                addCustomAttributeColumns(OrgUnit.class);

                this.setLineAction(new IColumnFormatter<OrgUnitListView>() {
                    @Override
                    public String apply(OrgUnitListView orgUnitListView, Object value) {
                        return controllers.core.routes.OrgUnitController.view(orgUnitListView.id, 0).url();
                    }
                });

                setEmptyMessageKey("object.org_unit.table.empty");

            }
        };
    }

    /**
     * Default constructor.
     */
    public OrgUnitListView() {
    }

    public Long id;
    public String refId;
    public String name;
    public OrgUnitType type;
    public Boolean isActive;
    public Actor manager;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param orgUnit
     *            the org unit in the DB
     */
    public OrgUnitListView(OrgUnit orgUnit) {
        this.id = orgUnit.id;
        this.refId = orgUnit.refId;
        this.name = orgUnit.name;
        this.type = orgUnit.orgUnitType;
        this.isActive = orgUnit.isActive;
        this.manager = orgUnit.manager;
    }
}
