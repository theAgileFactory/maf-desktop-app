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
import framework.services.kpi.Kpi.DataType;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.framework_models.kpi.KpiValueDefinition;

/**
 * A kpi value definition list view is used to display a kpi value definition
 * row in a table.
 * 
 * @author Johann Kohler
 */
public class KpiValueDefinitionListView {

    public static class TableDefinition {

        public Table<KpiValueDefinitionListView> templateTable;

        public TableDefinition() {
            this.templateTable = getTable();
        }

        public Table<KpiValueDefinitionListView> getTable() {
            return new Table<KpiValueDefinitionListView>() {
                {
                    setIdFieldName("id");

                    addColumn("name", "name", "object.kpi_value_definition.name.label", Table.ColumnDef.SorterType.NONE);

                    addColumn("type", "type", "object.kpi_value_definition.type.label", Table.ColumnDef.SorterType.NONE);

                    addColumn("isTrendDisplayed", "isTrendDisplayed", "object.kpi_value_definition.is_trend_displayed.label",
                            Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isTrendDisplayed", new BooleanFormatter<KpiValueDefinitionListView>());

                    addColumn("renderType", "renderType", "object.kpi_value_definition.render_type.label", Table.ColumnDef.SorterType.NONE);

                    addColumn("renderPattern", "renderPattern", "object.kpi_value_definition.render_pattern.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("renderPattern", new ObjectFormatter<KpiValueDefinitionListView>());

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<KpiValueDefinitionListView>(IMafConstants.EDIT_URL_FORMAT,
                            new StringFormatFormatter.Hook<KpiValueDefinitionListView>() {
                        @Override
                        public String convert(KpiValueDefinitionListView kpiValueDefinitionListView) {
                            return controllers.admin.routes.KpiManagerController.editValue(kpiValueDefinitionListView.id, kpiValueDefinitionListView.type)
                                    .url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public KpiValueDefinitionListView() {
    }

    public Long id;
    public String name;
    public String type;
    public String renderType;
    public String renderPattern;
    public boolean isTrendDisplayed;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param kpiValueDefinition
     *            the KPI value definition in the DB
     * @param dataType
     *            the data type (main, additional1, additional2)
     */
    public KpiValueDefinitionListView(KpiValueDefinition kpiValueDefinition, DataType dataType) {

        this.id = kpiValueDefinition.id;
        this.name = Msg.get(kpiValueDefinition.name);
        this.type = dataType.name().toLowerCase();
        this.renderType = Msg.get("object.kpi_value_definition.render_type." + kpiValueDefinition.renderType.name() + ".label");
        this.renderPattern = kpiValueDefinition.renderPattern;
        this.isTrendDisplayed = kpiValueDefinition.isTrendDisplayed;

    }
}
