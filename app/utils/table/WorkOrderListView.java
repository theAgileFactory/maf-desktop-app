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

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;

import constants.IMafConstants;
import dao.finance.PurchaseOrderDAO;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.NumberFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.finance.Currency;
import models.finance.WorkOrder;
import models.pmo.PortfolioEntryPlanningPackage;

/**
 * A work order list view is used to display a work order row in a table.
 * 
 * @author Johann Kohler
 */
public class WorkOrderListView {

    /**
     * The definition of the table.
     * 
     * @author Johann Kohler
     */
    public static class TableDefinition {

        public Table<WorkOrderListView> templateTable;

        /**
         * Default constructor.
         */
        public TableDefinition(II18nMessagesPlugin i18nMessagesPlugin) {
            this.templateTable = getTable(i18nMessagesPlugin);
        }

        /**
         * Get the table.
         */
        public Table<WorkOrderListView> getTable(II18nMessagesPlugin i18nMessagesPlugin) {
            return new Table<WorkOrderListView>() {
                {
                    setIdFieldName("id");

                    addColumn("name", "name", "object.work_order.name.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("name", new ObjectFormatter<WorkOrderListView>());

                    addColumn("shared", "shared", "object.work_order.shared.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("shared", new BooleanFormatter<WorkOrderListView>());

                    addColumn("isOpex", "isOpex", "object.work_order.expenditure_type.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("isOpex", new IColumnFormatter<WorkOrderListView>() {
                        @Override
                        public String apply(WorkOrderListView workOrderListView, Object value) {
                            return views.html.modelsparts.display_is_opex.render(workOrderListView.isOpex).body();
                        }
                    });

                    addColumn("currency", "currency", "object.work_order.currency.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("currency", new ObjectFormatter<WorkOrderListView>());

                    addColumn("amount", "amount", "object.work_order.amount.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("amount", new NumberFormatter<WorkOrderListView>());

                    addColumn("amountReceived", "amountReceived", "object.work_order.amount_received.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("amountReceived", new NumberFormatter<WorkOrderListView>());

                    addColumn("planningPackage", "planningPackage", "object.work_order.portfolio_entry_planning_package.label",
                            Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("planningPackage", new IColumnFormatter<WorkOrderListView>() {
                        @Override
                        public String apply(WorkOrderListView workOrderListView, Object value) {
                            return views.html.modelsparts.display_portfolio_entry_planning_package.render(workOrderListView.planningPackage).body();
                        }
                    });
                    setColumnValueCssClass("planningPackage", "rowlink-skip");

                    addColumn("startDate", "startDate", "object.work_order.start_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("startDate", new DateFormatter<WorkOrderListView>());

                    addColumn("dueDate", "dueDate", "object.work_order.due_date.label", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("dueDate", new DateFormatter<WorkOrderListView>());

                    addCustomAttributeColumns(i18nMessagesPlugin, WorkOrder.class);

                    addColumn("selectLineItemActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("selectLineItemActionLink", new IColumnFormatter<WorkOrderListView>() {
                        @Override
                        public String apply(WorkOrderListView workOrderListView, Object value) {
                            if (!workOrderListView.fromResource) {
                                String url = controllers.core.routes.PortfolioEntryFinancialController
                                        .selectWorkOrderLineItemStep1(workOrderListView.portfolioEntryId, workOrderListView.id).url();
                                return views.html.framework_views.parts.formats.display_with_format
                                        .render(url, "<a href=\"%s\"><span class=\"fa fa-lock\"></span></a>").body();
                            } else {
                                return null;
                            }
                        }
                    });
                    setColumnCssClass("selectLineItemActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("selectLineItemActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("engageWorkOrder", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("engageWorkOrder", new IColumnFormatter<WorkOrderListView>() {
                        @Override
                        public String apply(WorkOrderListView workOrderListView, Object value) {
                            if (!workOrderListView.fromResource) {
                                String url = controllers.core.routes.PortfolioEntryFinancialController
                                        .engageWorkOrderStep1(workOrderListView.portfolioEntryId, workOrderListView.id).url();
                                return views.html.framework_views.parts.formats.display_with_format
                                        .render(url, "<a href=\"%s\"><span class=\"fa fa-lock\"></span></a>").body();
                            } else {
                                return null;
                            }
                        }
                    });
                    setColumnCssClass("engageWorkOrder", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("engageWorkOrder", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("editActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("editActionLink",
                            new StringFormatFormatter<WorkOrderListView>(IMafConstants.EDIT_URL_FORMAT, new StringFormatFormatter.Hook<WorkOrderListView>() {
                        @Override
                        public String convert(WorkOrderListView workOrderListView) {
                            return controllers.core.routes.PortfolioEntryFinancialController
                                    .manageWorkOrder(workOrderListView.portfolioEntryId, workOrderListView.id).url();
                        }
                    }));
                    setColumnCssClass("editActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("editActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    addColumn("deleteActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("deleteActionLink", new IColumnFormatter<WorkOrderListView>() {
                        @Override
                        public String apply(WorkOrderListView workOrderListView, Object value) {
                            if (!workOrderListView.fromResource) {
                                String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                        Msg.get("default.delete.confirmation.message"));
                                String url = controllers.core.routes.PortfolioEntryFinancialController
                                        .deleteWorkOrder(workOrderListView.portfolioEntryId, workOrderListView.id).url();
                                return views.html.framework_views.parts.formats.display_with_format.render(url, deleteConfirmationMessage).body();
                            } else {
                                return null;
                            }
                        }
                    });
                    setColumnCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("deleteActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                    this.setLineAction(new IColumnFormatter<WorkOrderListView>() {
                        @Override
                        public String apply(WorkOrderListView workOrderListView, Object value) {
                            return controllers.core.routes.PortfolioEntryFinancialController
                                    .viewWorkOrder(workOrderListView.portfolioEntryId, workOrderListView.id).url();
                        }
                    });

                    setEmptyMessageKey("object.work_order.table.empty");

                }
            };

        }

    }

    /**
     * Default constructor.
     */
    public WorkOrderListView() {
    }

    public Long id;

    public Long portfolioEntryId;

    public String name;

    public Boolean shared;

    public Currency currency;

    public BigDecimal amount;

    public Boolean isOpex;

    public Date dueDate;

    public Date startDate;

    public PortfolioEntryPlanningPackage planningPackage;

    public boolean fromResource;

    /*
     * if engaged
     */

    public BigDecimal amountReceived;

    /**
     * Construct a row with a DB entry.
     * 
     * @param preferenceManagerPlugin
     *            the preference manager service
     * @param workOrder
     *            the work order in the DB.
     */
    public WorkOrderListView(IPreferenceManagerPlugin preferenceManagerPlugin, WorkOrder workOrder) {

        this.id = workOrder.id;
        this.portfolioEntryId = workOrder.portfolioEntry.id;
        this.name = workOrder.name;
        this.shared = workOrder.shared;
        this.currency = workOrder.currency;
        this.amount = workOrder.getComputedAmount(PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder(preferenceManagerPlugin));
        this.isOpex = workOrder.isOpex;
        this.dueDate = workOrder.dueDate;
        this.startDate = workOrder.startDate;
        this.planningPackage = workOrder.portfolioEntryPlanningPackage;
        this.fromResource = workOrder.resourceObjectType != null;
        this.amountReceived = workOrder.getComputedAmountReceived(PurchaseOrderDAO.isSystemPreferenceUsePurchaseOrder(preferenceManagerPlugin));
    }
}
