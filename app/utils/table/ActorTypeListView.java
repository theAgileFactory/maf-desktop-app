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

import constants.IMafConstants;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.pmo.ActorType;

/**
 * An actor type list view is used to display an actor type row in a table.
 * 
 * @author Johann Kohler
 */
public class ActorTypeListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<ActorTypeListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition() {
            this.templateTable = getTable();
        }

        /**
         * Get the table.
         */
        public Table<ActorTypeListView> getTable() {
            return new Table<ActorTypeListView>() {
                {

                    setIdFieldName("id");

                    addColumn("refId", "refId", "object.actor_type.ref_id.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("refId", new ObjectFormatter<ActorTypeListView>());

                    addColumn("name", "name", "object.actor_type.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<ActorTypeListView>());

                    addColumn("description", "description", "object.actor_type.description.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("description", new ObjectFormatter<ActorTypeListView>());

                    addColumn("selectable", "selectable", "object.actor_type.selectable.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("selectable", new BooleanFormatter<ActorTypeListView>());

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink",
                            new StringFormatFormatter<ActorTypeListView>(IMafConstants.EDIT_URL_FORMAT, new StringFormatFormatter.Hook<ActorTypeListView>() {
                        @Override
                        public String convert(ActorTypeListView actorTypeListView) {
                            return controllers.admin.routes.ConfigurationActorAndOrgUnitController.manageActorType(actorTypeListView.id).url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<ActorTypeListView>() {
                        @Override
                        public String apply(ActorTypeListView actorTypeListView, Object value) {
                            String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                    Msg.get("default.delete.confirmation.message"));
                            String url = controllers.admin.routes.ConfigurationActorAndOrgUnitController.deleteActorType(actorTypeListView.id).url();
                            return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                        }
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    setEmptyMessageKey("object.actor_type.table.empty");

                }
            };
        }

    }

    /**
     * Default constructor.
     */
    public ActorTypeListView() {
    }

    public Long id;

    public String name;

    public String description;

    public String refId;

    public boolean selectable;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param actorType
     *            the actor type in the DB
     */
    public ActorTypeListView(ActorType actorType) {

        this.id = actorType.id;
        this.name = actorType.name;
        this.description = actorType.description;
        this.refId = actorType.refId;
        this.selectable = actorType.selectable;

    }
}
