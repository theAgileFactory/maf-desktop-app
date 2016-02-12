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

import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.IColumnFormatter;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.ObjectFormatter;
import models.pmo.Actor;
import models.pmo.OrgUnit;

/**
 * An actor list view is used to display an actor row in a table.
 * 
 * @author Johann Kohler
 */
public class ActorListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<ActorListView> templateTable;

        public TableDefinition(II18nMessagesPlugin i18nMessagesPlugin) {
            this.templateTable = getTable(i18nMessagesPlugin);
        }

        /**
         * Get the table.
         */
        public Table<ActorListView> getTable(II18nMessagesPlugin i18nMessagesPlugin) {
            return new Table<ActorListView>() {
                {
                    setIdFieldName("id");

                    addColumn("employeeId", "employeeId", "object.actor.employee_id.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("employeeId", new ObjectFormatter<ActorListView>());

                    addColumn("name", "name", "object.actor.name.label", Table.ColumnDef.SorterType.NONE);

                    addColumn("title", "title", "object.actor.title.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("title", new ObjectFormatter<ActorListView>());

                    addColumn("isActive", "isActive", "object.actor.is_active.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isActive", new BooleanFormatter<ActorListView>());

                    addColumn("orgUnit", "orgUnit", "object.actor.org_unit.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("orgUnit", new IColumnFormatter<ActorListView>() {
                        @Override
                        public String apply(ActorListView actorListView, Object value) {
                            return views.html.modelsparts.display_org_unit.render(actorListView.orgUnit).body();
                        }
                    });
                    this.setColumnValueCssClass("orgUnit", "rowlink-skip");

                    addColumn("manager", "manager", "object.actor.manager.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("manager", new IColumnFormatter<ActorListView>() {
                        @Override
                        public String apply(ActorListView actorListView, Object value) {
                            return views.html.modelsparts.display_actor.render(actorListView.manager).body();
                        }
                    });
                    this.setColumnValueCssClass("manager", "rowlink-skip");

                    addCustomAttributeColumns(i18nMessagesPlugin, Actor.class);

                    this.setLineAction(new IColumnFormatter<ActorListView>() {
                        @Override
                        public String apply(ActorListView actorListView, Object value) {
                            return controllers.core.routes.ActorController.view(actorListView.id).url();
                        }
                    });

                    setEmptyMessageKey("object.actor.table.empty");

                }
            };
        }

    }

    /**
     * Default constructor.
     */
    public ActorListView() {
    }

    public Long id;
    public String employeeId;
    public String name;
    public String title;
    public Boolean isActive;
    public OrgUnit orgUnit;
    public Actor manager;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param actor
     *            the actor in the DB
     */
    public ActorListView(Actor actor) {
        this.id = actor.id;
        this.employeeId = actor.employeeId;
        this.name = actor.getNameHumanReadable();
        this.title = actor.title;
        this.isActive = actor.isActive;
        this.orgUnit = actor.orgUnit;
        this.manager = actor.manager;
    }
}
