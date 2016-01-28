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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import framework.utils.IColumnFormatter;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.ListOfValuesFormatter;
import framework.utils.formats.ObjectFormatter;
import models.pmo.Actor;
import models.pmo.Portfolio;
import models.pmo.PortfolioType;
import models.pmo.Stakeholder;

/**
 * A portfolio list view is used to display an portfolio row in a table.
 * 
 * @author Johann Kohler
 */
public class PortfolioListView {

    public static Table<PortfolioListView> templateTable = getTable();

    /**
     * Get the table.
     */
    public static Table<PortfolioListView> getTable() {
        return new Table<PortfolioListView>() {
            {

                setIdFieldName("id");

                addColumn("refId", "refId", "object.portfolio.ref_id.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("refId", new ObjectFormatter<PortfolioListView>());

                addColumn("name", "name", "object.portfolio.name.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("name", new ObjectFormatter<PortfolioListView>());

                addColumn("type", "type", "object.portfolio.type.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("type", new IColumnFormatter<PortfolioListView>() {
                    @Override
                    public String apply(PortfolioListView portfolioListView, Object value) {
                        return views.html.framework_views.parts.formats.display_value_holder.render(portfolioListView.type, true).body();
                    }
                });

                addColumn("isActive", "isActive", "object.portfolio.is_active.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("isActive", new BooleanFormatter<PortfolioListView>());

                addColumn("manager", "manager", "object.portfolio.manager.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("manager", new IColumnFormatter<PortfolioListView>() {
                    @Override
                    public String apply(PortfolioListView portfolioListView, Object value) {
                        return views.html.modelsparts.display_actor.render(portfolioListView.manager).body();
                    }
                });
                this.setColumnValueCssClass("manager", "rowlink-skip");

                addColumn("stakeholderTypes", "stakeholderTypes", "object.portfolio.stakeholder_types.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("stakeholderTypes", new ListOfValuesFormatter<PortfolioListView>());

                addCustomAttributeColumns(Portfolio.class);

                this.setLineAction(new IColumnFormatter<PortfolioListView>() {
                    @Override
                    public String apply(PortfolioListView portfolioListView, Object value) {
                        return controllers.core.routes.PortfolioController.overview(portfolioListView.id).url();
                    }
                });

                setEmptyMessageKey("object.portfolio.table.empty");
            }
        };

    }

    public static Set<String> hideStakeholderTypeColumn = new HashSet<String>() {
        private static final long serialVersionUID = 1L;

        {
            add("stakeholderTypes");
        }
    };

    /**
     * Default constructor.
     */
    public PortfolioListView() {
    }

    public Long id;
    public String refId;
    public String name;
    public PortfolioType type;
    public Boolean isActive;
    public Actor manager;
    public List<String> stakeholderTypes = new ArrayList<String>();

    /**
     * Construct a list view with a DB entry.
     * 
     * @param portfolio
     *            the portfolio in the DB
     */
    public PortfolioListView(Portfolio portfolio) {

        this.id = portfolio.id;
        this.refId = portfolio.refId;
        this.name = portfolio.name;
        this.type = portfolio.portfolioType;
        this.manager = portfolio.manager;
        this.isActive = portfolio.isActive;

    }

    /**
     * Constructor used in the case of the stakeholderTypes column must be
     * displayed.
     * 
     * note: this constructor is called when displaying the portfolios for which
     * the current user is a stakeholder, in order to display his roles on the
     * portfolio
     * 
     * @param portfolio
     *            the portfolio
     * @param stakeholders
     *            the stakeholders
     */
    public PortfolioListView(Portfolio portfolio, List<Stakeholder> stakeholders) {

        this(portfolio);
        for (Stakeholder stakeholder : stakeholders) {
            this.stakeholderTypes.add(stakeholder.stakeholderType.getName());
        }

    }
}
