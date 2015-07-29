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

import models.finance.BudgetBucket;
import models.pmo.Actor;
import framework.utils.IColumnFormatter;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.ObjectFormatter;

/**
 * A budget bucket list view is used to display a budget bucket row in a table.
 * 
 * @author Johann Kohler
 */
public class BudgetBucketListView {

    public static Table<BudgetBucketListView> templateTable = getTable();

    /**
     * Get the table.
     */
    public static Table<BudgetBucketListView> getTable() {
        return new Table<BudgetBucketListView>() {
            {
                setIdFieldName("id");

                addColumn("refId", "refId", "object.budget_bucket.ref_id.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("refId", new ObjectFormatter<BudgetBucketListView>());

                addColumn("name", "name", "object.budget_bucket.name.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("name", new ObjectFormatter<BudgetBucketListView>());

                addColumn("owner", "owner", "object.budget_bucket.owner.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("owner", new IColumnFormatter<BudgetBucketListView>() {
                    @Override
                    public String apply(BudgetBucketListView budgetBucketListView, Object value) {
                        return views.html.modelsparts.display_actor.render(budgetBucketListView.owner).body();
                    }
                });
                this.setColumnValueCssClass("owner", "rowlink-skip");

                addColumn("isApproved", "isApproved", "object.budget_bucket.is_approved.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("isApproved", new BooleanFormatter<BudgetBucketListView>());

                addColumn("isActive", "isActive", "object.budget_bucket.is_active.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("isActive", new BooleanFormatter<BudgetBucketListView>());

                addCustomAttributeColumns(BudgetBucket.class);

                this.setLineAction(new IColumnFormatter<BudgetBucketListView>() {
                    @Override
                    public String apply(BudgetBucketListView budgetBucketListView, Object value) {
                        return controllers.core.routes.BudgetBucketController.view(budgetBucketListView.id, 0, 0).url();
                    }
                });

                setEmptyMessageKey("object.budget_bucket.table.empty");

            }
        };
    }

    /**
     * Default constructor.
     */
    public BudgetBucketListView() {
    }

    public Long id;
    public String refId;
    public String name;
    public Actor owner;
    public boolean isApproved;
    public boolean isActive;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param budgetBucket
     *            the budget bucket in the DB
     */
    public BudgetBucketListView(BudgetBucket budgetBucket) {
        this.id = budgetBucket.id;
        this.refId = budgetBucket.refId;
        this.name = budgetBucket.getName();
        this.owner = budgetBucket.owner;
        this.isApproved = budgetBucket.isApproved;
        this.isActive = budgetBucket.isActive;
    }
}
