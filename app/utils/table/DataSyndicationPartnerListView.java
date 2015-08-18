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
import framework.utils.IColumnFormatter;
import framework.utils.Table;
import framework.utils.formats.ObjectFormatter;
import services.datasyndication.models.DataSyndicationPartner;

/**
 * A data syndication partner list view is used to display an data syndication
 * partner row in a table.
 * 
 * @author Johann Kohler
 */
public class DataSyndicationPartnerListView {

    public static Table<DataSyndicationPartnerListView> templateTable = getTable();

    /**
     * Get the table.
     */
    public static Table<DataSyndicationPartnerListView> getTable() {
        return new Table<DataSyndicationPartnerListView>() {
            {
                setIdFieldName("domain");

                addColumn("customerLogo", "customerLogo", "object.data_syndication_partner.customer_logo.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("customerLogo", new IColumnFormatter<DataSyndicationPartnerListView>() {
                    @Override
                    public String apply(DataSyndicationPartnerListView dataSyndicationPartnerListView, Object value) {
                        if (dataSyndicationPartnerListView.customerLogo != null) {
                            return "<img style=\"max-height: 60px;\" src='" + dataSyndicationPartnerListView.customerLogo + "' />";
                        } else {
                            return IMafConstants.DEFAULT_VALUE_EMPTY_DATA;
                        }
                    }
                });

                addColumn("customerName", "customerName", "object.data_syndication_partner.customer_name.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("customerName", new ObjectFormatter<DataSyndicationPartnerListView>());

                addColumn("customerDescription", "customerDescription", "object.data_syndication_partner.customer_description.label",
                        Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("customerDescription", new ObjectFormatter<DataSyndicationPartnerListView>());

                addColumn("customerWebsite", "customerWebsite", "object.data_syndication_partner.customer_website.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("customerWebsite", new IColumnFormatter<DataSyndicationPartnerListView>() {
                    @Override
                    public String apply(DataSyndicationPartnerListView dataSyndicationPartnerListView, Object value) {
                        return views.html.framework_views.parts.formats.display_url.render(dataSyndicationPartnerListView.customerWebsite, null, true).body();
                    }
                });
                setColumnValueCssClass("customerWebsite", "rowlink-skip");

                this.setLineAction(new IColumnFormatter<DataSyndicationPartnerListView>() {
                    @Override
                    public String apply(DataSyndicationPartnerListView dataSyndicationPartnerListView, Object value) {
                        return controllers.admin.routes.DataSyndicationController.submitAgreement(dataSyndicationPartnerListView.domain).url();
                    }
                });

                setEmptyMessageKey("table.empty");

            }
        };

    }

    /**
     * Default constructor.
     */
    public DataSyndicationPartnerListView() {
    }

    public String domain;

    public String customerLogo;
    public String customerName;
    public String customerDescription;
    public String customerWebsite;

    /**
     * Construct a list view with a partner.
     * 
     * @param dataSyndicationPartner
     *            the partner
     */
    public DataSyndicationPartnerListView(DataSyndicationPartner dataSyndicationPartner) {

        this.domain = dataSyndicationPartner.domain;

        this.customerLogo = dataSyndicationPartner.customerLogo;
        this.customerName = dataSyndicationPartner.customerName;
        this.customerDescription = dataSyndicationPartner.customerDescription;
        this.customerWebsite = dataSyndicationPartner.customerWebsite;
    }

}
