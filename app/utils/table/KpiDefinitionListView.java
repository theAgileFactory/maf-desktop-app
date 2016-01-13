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
import framework.services.kpi.IKpiService;
import framework.services.kpi.Kpi;
import framework.services.kpi.Kpi.DataType;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import models.framework_models.kpi.KpiDefinition;

/**
 * A kpi definition list view is used to display a kpi definition row in a
 * table.
 * 
 * @author Johann Kohler
 */
public class KpiDefinitionListView {

    public static Table<KpiDefinitionListView> templateTable = new Table<KpiDefinitionListView>() {
        {
            setIdFieldName("id");

            addColumn("changeOrder", "id", "", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("changeOrder", new IColumnFormatter<KpiDefinitionListView>() {
                @Override
                public String apply(KpiDefinitionListView kpiDefinitionListView, Object value) {
                    if (kpiDefinitionListView.isDisplayed) {
                        return "<a href=\"" + controllers.admin.routes.KpiManagerController.changeOrder(kpiDefinitionListView.id, false).url()
                                + "\"><span class=\"fa fa-arrow-down\"></span></a>&nbsp;" + "<a href=\""
                                + controllers.admin.routes.KpiManagerController.changeOrder(kpiDefinitionListView.id, true).url()
                                + "\"><span class=\"fa fa-arrow-up\"></span></a>";
                    } else {
                        return "";
                    }
                }
            });
            setColumnCssClass("changeOrder", IMafConstants.BOOTSTRAP_COLUMN_1);
            setColumnValueCssClass("changeOrder", "rowlink-skip");

            addColumn("mainValueName", "mainValueName", "object.kpi_definition.name.label", Table.ColumnDef.SorterType.NONE);
            setColumnCssClass("mainValueName", IMafConstants.BOOTSTRAP_COLUMN_5);

            addColumn("isActive", "isActive", "object.kpi_definition.is_active.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("isActive", new BooleanFormatter<KpiDefinitionListView>());
            setColumnCssClass("isActive", IMafConstants.BOOTSTRAP_COLUMN_2);

            addColumn("hasTrend", "hasTrend", "object.kpi_definition.has_trend.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("hasTrend", new BooleanFormatter<KpiDefinitionListView>());
            setColumnCssClass("hasTrend", IMafConstants.BOOTSTRAP_COLUMN_2);

            addColumn("hasBoxDisplay", "hasBoxDisplay", "object.kpi_definition.has_box_display.label", Table.ColumnDef.SorterType.NONE);
            setJavaColumnFormatter("hasBoxDisplay", new BooleanFormatter<KpiDefinitionListView>());
            setColumnCssClass("hasBoxDisplay", IMafConstants.BOOTSTRAP_COLUMN_2);

            this.setLineAction(new IColumnFormatter<KpiDefinitionListView>() {
                @Override
                public String apply(KpiDefinitionListView kpiDefinitionListView, Object value) {
                    return controllers.admin.routes.KpiManagerController.view(kpiDefinitionListView.id).url();
                }
            });

        }
    };

    /**
     * Default constructor.
     */
    public KpiDefinitionListView() {
    }

    public Long id;
    public String mainValueName;
    public boolean isActive;
    public boolean isDisplayed;
    public boolean hasTrend;
    public boolean hasBoxDisplay;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param kpiDefinition
     *            the KPI definition in the DB
     * @param kpiService
     *            the KPI service
     */
    public KpiDefinitionListView(KpiDefinition kpiDefinition, IKpiService kpiService) {

        Kpi kpi = new Kpi(kpiDefinition, kpiService);

        this.id = kpiDefinition.id;
        this.mainValueName = Msg.get(kpi.getValueName(DataType.MAIN));
        this.isActive = kpiDefinition.isActive;
        this.isDisplayed = kpiDefinition.isDisplayed;
        this.hasTrend = kpi.hasTrend();
        this.hasBoxDisplay = kpi.hasBoxDisplay();

    }
}
