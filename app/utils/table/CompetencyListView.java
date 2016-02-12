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
import models.pmo.Actor;
import models.pmo.Competency;

/**
 * A competency list view is used to display a competency row in a table.
 * 
 * @author Johann Kohler
 */
public class CompetencyListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<CompetencyListView> templateTable;

        public TableDefinition() {
            this.templateTable = getTable();
        }

        public Table<CompetencyListView> getTable() {

            return new Table<CompetencyListView>() {
                {

                    setIdFieldName("id");

                    addColumn("isDefault", "isDefault", "object.competency.is_default.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isDefault", new BooleanFormatter<CompetencyListView>());

                    addColumn("name", "name", "object.competency.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<CompetencyListView>());

                    addColumn("description", "description", "object.competency.description.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("description", new ObjectFormatter<CompetencyListView>());

                    addColumn("isActive", "isActive", "object.competency.is_active.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isActive", new BooleanFormatter<CompetencyListView>());

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<CompetencyListView>(IMafConstants.EDIT_URL_FORMAT,
                            new StringFormatFormatter.Hook<CompetencyListView>() {
                        @Override
                        public String convert(CompetencyListView competencyListView) {
                            return controllers.admin.routes.ConfigurationActorAndOrgUnitController.manageCompetency(competencyListView.id).url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<CompetencyListView>() {
                        @Override
                        public String apply(CompetencyListView competencyListView, Object value) {
                            String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                    Msg.get("default.delete.confirmation.message"));
                            String url = controllers.admin.routes.ConfigurationActorAndOrgUnitController.deleteCompetency(competencyListView.id).url();
                            return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                        }
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    setEmptyMessageKey("object.competency.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public CompetencyListView() {
    }

    public Long id;

    public String name;

    public String description;

    public boolean isActive;

    public boolean isDefault;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param competency
     *            the competency in the DB
     */
    public CompetencyListView(Competency competency) {

        this.id = competency.id;
        this.name = competency.name;
        this.description = competency.description;
        this.isActive = competency.isActive;

    }

    /**
     * Construct a list view with a DB entry.
     * 
     * @param competency
     *            the competency in the DB
     * @param actor
     *            the concerned actor
     */
    public CompetencyListView(Competency competency, Actor actor) {

        this.id = competency.id;
        this.name = competency.name;
        this.description = competency.description;
        this.isActive = competency.isActive;

        if (actor.defaultCompetency != null && competency.id.equals(actor.defaultCompetency.id)) {
            this.isDefault = true;
        } else {
            this.isDefault = false;
        }

    }

}
