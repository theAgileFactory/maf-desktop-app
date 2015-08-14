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

import java.util.List;

import constants.IMafConstants;
import dao.pmo.PortfolioEntryDao;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.ListOfValuesFormatter;
import models.pmo.PortfolioEntry;
import services.datasyndication.models.DataSyndicationAgreementItem;
import services.datasyndication.models.DataSyndicationAgreementLink;

/**
 * A data syndication agreement link list view is used to display an data
 * syndication agreement link row in a table.
 * 
 * @author Johann Kohler
 */
public class DataSyndicationAgreementLinkListView {

    public static Table<DataSyndicationAgreementLinkListView> templateTable = getTable();

    /**
     * Get the table.
     */
    public static Table<DataSyndicationAgreementLinkListView> getTable() {
        return new Table<DataSyndicationAgreementLinkListView>() {
            {
                setIdFieldName("id");

                addColumn("dataType", "dataType", "object.data_syndication_agreement_link.data_type.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("dataType", new IColumnFormatter<DataSyndicationAgreementLinkListView>() {
                    @Override
                    public String apply(DataSyndicationAgreementLinkListView dataSyndicationAgreementLinkListView, Object value) {
                        return Msg.get("object.data_syndication_agreement_item." + dataSyndicationAgreementLinkListView.dataType + ".label");
                    }
                });

                addColumn("objectId", "objectId", "object.data_syndication_agreement_link.object.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("objectId", new IColumnFormatter<DataSyndicationAgreementLinkListView>() {
                    @Override
                    public String apply(DataSyndicationAgreementLinkListView dataSyndicationAgreementLinkListView, Object value) {

                        // manage here specifically each data type

                        if (dataSyndicationAgreementLinkListView.dataType.equals(PortfolioEntry.class.getName())) {
                            return views.html.modelsparts.display_portfolio_entry
                                    .render(PortfolioEntryDao.getPEById(dataSyndicationAgreementLinkListView.objectId), true).body();
                        }

                        return IMafConstants.DEFAULT_VALUE_EMPTY_DATA;

                    }
                });
                this.setColumnValueCssClass("objectId", "rowlink-skip");

                addColumn("items", "items", "object.data_syndication_agreement_link.items.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("items", new ListOfValuesFormatter<>());

                addColumn("status", "status", "object.data_syndication_agreement_link.status.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("status", new IColumnFormatter<DataSyndicationAgreementLinkListView>() {
                    @Override
                    public String apply(DataSyndicationAgreementLinkListView dataSyndicationAgreementLinkListView, Object value) {
                        return dataSyndicationAgreementLinkListView.status.render();
                    }
                });

                // only for slave
                addColumn("processActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("processActionLink", new IColumnFormatter<DataSyndicationAgreementLinkListView>() {
                    @Override
                    public String apply(DataSyndicationAgreementLinkListView dataSyndicationAgreementLinkListView, Object value) {

                        if (dataSyndicationAgreementLinkListView.status.equals(DataSyndicationAgreementLink.Status.PENDING)) {

                            String content = "<a title=\"" + Msg.get("object.data_syndication_agreement_link.process.label")
                                    + "\" href=\"%s\"><span class=\"glyphicons glyphicons-thumbs-up\"></span></a>";

                            String url = controllers.admin.routes.DataSyndicationController.processAgreementLink(dataSyndicationAgreementLinkListView.id)
                                    .url();

                            return views.html.framework_views.parts.formats.display_with_format.render(url, content).body();

                        } else {
                            return "";
                        }
                    }
                });
                setColumnCssClass("processActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                setColumnValueCssClass("processActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                setEmptyMessageKey("object.data_syndication_agreement_link.table.empty");

            }
        };

    }

    /**
     * Default constructor.
     */
    public DataSyndicationAgreementLinkListView() {
    }

    public Long id;

    public String dataType;
    public Long objectId;

    public List<DataSyndicationAgreementItem> items;

    public DataSyndicationAgreementLink.Status status;

    /**
     * Construct a list view with an agreement link.
     * 
     * @param dataSyndicationAgreementLink
     *            the agreement link
     * @param currentDomain
     *            the domain of the current instance
     */
    public DataSyndicationAgreementLinkListView(DataSyndicationAgreementLink dataSyndicationAgreementLink, String currentDomain) {

        this.id = dataSyndicationAgreementLink.id;

        this.dataType = dataSyndicationAgreementLink.dataType;
        if (dataSyndicationAgreementLink.agreement.masterPartner.domain.equals(currentDomain)) {
            this.objectId = dataSyndicationAgreementLink.masterObjectId;
        } else {
            this.objectId = dataSyndicationAgreementLink.slaveObjectId;
        }

        this.items = dataSyndicationAgreementLink.items;

        this.status = dataSyndicationAgreementLink.status;

    }

}
