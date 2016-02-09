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
import framework.commons.IFrameworkConstants.Syntax;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.Color;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.framework_models.kpi.KpiColorRule;

/**
 * A kpi color rule list view is used to display a kpi color rule row in a
 * table.
 * 
 * @author Johann Kohler
 */
public class KpiColorRuleListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<KpiColorRuleListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition() {
            this.templateTable = getTable();
        }

        /**
         * Get the table.
         */
        public Table<KpiColorRuleListView> getTable() {
            return new Table<KpiColorRuleListView>() {
                {
                    setIdFieldName("id");

                    addColumn("changeOrder", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("changeOrder", new IColumnFormatter<KpiColorRuleListView>() {
                        @Override
                        public String apply(KpiColorRuleListView kpiColorRuleListView, Object value) {
                            return "<a href=\"" + controllers.admin.routes.KpiManagerController.changeRuleOrder(kpiColorRuleListView.id, false).url()
                                    + "\"><span class=\"fa fa-arrow-down\"></span></a>&nbsp;" + "<a href=\""
                                    + controllers.admin.routes.KpiManagerController.changeRuleOrder(kpiColorRuleListView.id, true).url()
                                    + "\"><span class=\"fa fa-arrow-up\"></span></a>";
                        }
                    });
                    setColumnCssClass("changeOrder", IMafConstants.BOOTSTRAP_COLUMN_1);

                    addColumn("rule", "rule", "object.kpi_color_rule.rule.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("rule", new IColumnFormatter<KpiColorRuleListView>() {
                        @Override
                        public String apply(KpiColorRuleListView kpiColorRuleListView, Object value) {
                            return views.html.framework_views.parts.code_display.render(kpiColorRuleListView.rule, Syntax.JAVASCRIPT).body();
                        }
                    });
                    this.setColumnValueCssClass("rule", "rowlink-skip");

                    addColumn("color", "color", "object.kpi_color_rule.color.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("color", new ObjectFormatter<KpiColorRuleListView>());

                    addColumn("renderLabel", "renderLabel", "object.kpi_color_rule.render_label.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("renderLabel", new ObjectFormatter<KpiColorRuleListView>());

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink", new StringFormatFormatter<KpiColorRuleListView>(IMafConstants.EDIT_URL_FORMAT,
                            new StringFormatFormatter.Hook<KpiColorRuleListView>() {
                        @Override
                        public String convert(KpiColorRuleListView kpiColorRuleListView) {
                            return controllers.admin.routes.KpiManagerController.manageRule(kpiColorRuleListView.kpiDefinitionId, kpiColorRuleListView.id)
                                    .url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<KpiColorRuleListView>() {
                        @Override
                        public String apply(KpiColorRuleListView kpiColorRuleListView, Object value) {
                            String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                    Msg.get("default.delete.confirmation.message"));
                            String url = controllers.admin.routes.KpiManagerController.deleteRule(kpiColorRuleListView.id).url();
                            return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                        }
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public KpiColorRuleListView() {
    }

    public Long id;
    public Long kpiDefinitionId;
    public String rule;
    public String color;
    public String renderLabel;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param kpiColorRule
     *            the KPI color rule in the DB
     * @param messagesPlugin
     *            the i18n service
     */
    public KpiColorRuleListView(KpiColorRule kpiColorRule, II18nMessagesPlugin messagesPlugin) {

        this.id = kpiColorRule.id;
        this.kpiDefinitionId = kpiColorRule.kpiDefinition.id;
        this.rule = kpiColorRule.rule;
        this.color = Color.getLabel(kpiColorRule.cssColor, messagesPlugin);
        this.renderLabel = kpiColorRule.renderLabel;

    }
}
