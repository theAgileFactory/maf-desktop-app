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
import java.util.Date;

import models.delivery.Release;
import models.delivery.ReleasePortfolioEntry.Type;
import models.pmo.Actor;
import constants.IMafConstants;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.BooleanFormatter;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.ObjectFormatter;

/**
 * A release "portfolio entry" list view is used to display a release row in a
 * table in a portfolio entry context.
 * 
 * @author Johann Kohler
 */
public class ReleasePortfolioEntryListView {

    public static Table<ReleasePortfolioEntryListView> templateTable = getTable();

    /**
     * Get the table.
     */
    public static Table<ReleasePortfolioEntryListView> getTable() {
        return new Table<ReleasePortfolioEntryListView>() {
            {
                setIdFieldName("id");

                addColumn("isActive", "isActive", "object.release.is_active.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("isActive", new BooleanFormatter<ReleasePortfolioEntryListView>());

                addColumn("name", "name", "object.release.name.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("name", new ObjectFormatter<ReleasePortfolioEntryListView>());

                addColumn("deploymentDate", "deploymentDate", "object.release.deployment_date.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("deploymentDate", new DateFormatter<ReleasePortfolioEntryListView>());

                addColumn("manager", "manager", "object.release.manager.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("manager", new IColumnFormatter<ReleasePortfolioEntryListView>() {
                    @Override
                    public String apply(ReleasePortfolioEntryListView releasePortfolioEntryListView, Object value) {
                        return views.html.modelsparts.display_actor.render(releasePortfolioEntryListView.manager).body();
                    }
                });
                this.setColumnValueCssClass("manager", "rowlink-skip");

                addColumn("typeLabel", "typeLabel", "object.release.portfolio_entry.type.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("typeLabel", new ObjectFormatter<ReleasePortfolioEntryListView>());

                addCustomAttributeColumns(Release.class);

                addColumn("unassignActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("unassignActionLink", new IColumnFormatter<ReleasePortfolioEntryListView>() {
                    @Override
                    public String apply(ReleasePortfolioEntryListView releasePortfolioEntryListView, Object value) {
                        String unassignConfirmationMessage =
                                MessageFormat.format("<a onclick=\"return maf_confirmAction(''{0}'');\" href=\"%s\">"
                                        + "<span class=\"glyphicons glyphicons-remove-2\"></span></a>",
                                        Msg.get("object.release.portfolio_entry.unassign.confirmation"));
                        String url =
                                controllers.core.routes.PortfolioEntryDeliveryController.unassignRelease(releasePortfolioEntryListView.portfolioEntryId,
                                        releasePortfolioEntryListView.id).url();
                        return views.html.framework_views.parts.formats.display_with_format.render(url, unassignConfirmationMessage).body();
                    }
                });
                setColumnCssClass("unassignActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                setColumnValueCssClass("unassignActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                this.setLineAction(new IColumnFormatter<ReleasePortfolioEntryListView>() {
                    @Override
                    public String apply(ReleasePortfolioEntryListView releasePortfolioEntryListView, Object value) {
                        return controllers.core.routes.ReleaseController.view(releasePortfolioEntryListView.id, 0).url();
                    }
                });

                setEmptyMessageKey("object.release.table.empty");

            }
        };

    }

    /**
     * Default constructor.
     */
    public ReleasePortfolioEntryListView() {
    }

    public Long portfolioEntryId;
    public Long id;

    public boolean isActive;

    public String name;

    public Date deploymentDate;

    public Actor manager;

    public String typeLabel;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param release
     *            the release in the DB
     * @param portfolioEntryId
     *            the portfolio entry id
     * @param type
     *            the requirements' relation type
     */
    public ReleasePortfolioEntryListView(Release release, Long portfolioEntryId, Type type) {
        this.portfolioEntryId = portfolioEntryId;
        this.typeLabel = type.getLabel();
        this.id = release.id;
        this.isActive = release.isActive;
        this.name = release.name;
        this.deploymentDate = release.deploymentDate;
        this.manager = release.manager;
    }

}
