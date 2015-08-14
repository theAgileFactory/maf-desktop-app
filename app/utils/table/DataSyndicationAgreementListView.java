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

import constants.IMafConstants;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.MailFormatter;
import framework.utils.formats.ObjectFormatter;
import services.datasyndication.models.DataSyndicationAgreement;

/**
 * A data syndication agreement list view is used to display an data syndication
 * agreement row in a table.
 * 
 * @author Johann Kohler
 */
public class DataSyndicationAgreementListView {

    public static Table<DataSyndicationAgreementListView> templateTable = getTable();

    /**
     * Get the table.
     */
    public static Table<DataSyndicationAgreementListView> getTable() {
        return new Table<DataSyndicationAgreementListView>() {
            {
                setIdFieldName("id");

                addColumn("name", "name", "object.data_syndication_agreement.name.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("name", new ObjectFormatter<DataSyndicationAgreementListView>());

                addColumn("refId", "refId", "object.data_syndication_agreement.ref_id.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("refId", new ObjectFormatter<DataSyndicationAgreementListView>());

                addColumn("masterCustomerName", "masterCustomerName", "object.data_syndication_agreement.partner.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("masterCustomerName", new ObjectFormatter<DataSyndicationAgreementListView>());
                addColumn("masterContactName", "masterContactName", "object.data_syndication_agreement.contact_name.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("masterContactName", new ObjectFormatter<DataSyndicationAgreementListView>());
                addColumn("masterContactEmail", "masterContactEmail", "object.data_syndication_agreement.contact_email.label",
                        Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("masterContactEmail", new MailFormatter<DataSyndicationAgreementListView>());

                addColumn("slaveCustomerName", "slaveCustomerName", "object.data_syndication_agreement.partner.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("slaveCustomerName", new ObjectFormatter<DataSyndicationAgreementListView>());
                addColumn("slaveContactName", "slaveContactName", "object.data_syndication_agreement.contact_name.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("slaveContactName", new ObjectFormatter<DataSyndicationAgreementListView>());
                addColumn("slaveContactEmail", "slaveContactEmail", "object.data_syndication_agreement.contact_email.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("slaveContactEmail", new MailFormatter<DataSyndicationAgreementListView>());

                addColumn("startDate", "startDate", "object.data_syndication_agreement.start_date.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("startDate", new DateFormatter<DataSyndicationAgreementListView>());

                addColumn("endDate", "endDate", "object.data_syndication_agreement.end_date.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("endDate", new DateFormatter<DataSyndicationAgreementListView>());

                addColumn("status", "status", "object.data_syndication_agreement.status.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("status", new IColumnFormatter<DataSyndicationAgreementListView>() {
                    @Override
                    public String apply(DataSyndicationAgreementListView dataSyndicationAgreementListView, Object value) {
                        return dataSyndicationAgreementListView.status.render();
                    }
                });

                // only for slave
                addColumn("processActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("processActionLink", new IColumnFormatter<DataSyndicationAgreementListView>() {
                    @Override
                    public String apply(DataSyndicationAgreementListView dataSyndicationAgreementListView, Object value) {

                        if (dataSyndicationAgreementListView.status.equals(DataSyndicationAgreement.Status.PENDING)) {

                            String content = "<a title=\"" + Msg.get("object.data_syndication_agreement.process.label")
                                    + "\" href=\"%s\"><span class=\"glyphicons glyphicons-thumbs-up\"></span></a>";

                            String url = controllers.admin.routes.DataSyndicationController.processAgreement(dataSyndicationAgreementListView.id).url();

                            return views.html.framework_views.parts.formats.display_with_format.render(url, content).body();

                        } else {
                            return "";
                        }
                    }
                });
                setColumnCssClass("processActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                setColumnValueCssClass("processActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                // only for master
                addColumn("stateActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("stateActionLink", new IColumnFormatter<DataSyndicationAgreementListView>() {
                    @Override
                    public String apply(DataSyndicationAgreementListView dataSyndicationAgreementListView, Object value) {

                        String content = null;
                        String url = null;

                        switch (dataSyndicationAgreementListView.status) {

                        case ONGOING:
                            content = MessageFormat.format(
                                    "<a onclick=\"return maf_confirmAction(''{0}'');\" href=\"%s\"><span class=\"glyphicons glyphicons-pause\"></span></a>",
                                    Msg.get("object.data_syndication_agreement.suspend.confirm"));

                            url = controllers.admin.routes.DataSyndicationController.suspendAgreement(dataSyndicationAgreementListView.id).url();

                            return views.html.framework_views.parts.formats.display_with_format.render(url, content).body();

                        case SUSPENDED:
                            content = "<a href=\"%s\"><span class=\"glyphicons glyphicons-play\"></span></a>";

                            url = controllers.admin.routes.DataSyndicationController.restartAgreement(dataSyndicationAgreementListView.id).url();

                            return views.html.framework_views.parts.formats.display_with_format.render(url, content).body();

                        default:
                            return "";

                        }

                    }
                });
                setColumnCssClass("stateActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                setColumnValueCssClass("stateActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                addColumn("cancelActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("cancelActionLink", new IColumnFormatter<DataSyndicationAgreementListView>() {
                    @Override
                    public String apply(DataSyndicationAgreementListView dataSyndicationAgreementListView, Object value) {

                        if (dataSyndicationAgreementListView.status.equals(DataSyndicationAgreement.Status.ONGOING)
                                || dataSyndicationAgreementListView.status.equals(DataSyndicationAgreement.Status.PENDING)
                                || dataSyndicationAgreementListView.status.equals(DataSyndicationAgreement.Status.SUSPENDED)) {

                            String content = MessageFormat.format(
                                    "<a onclick=\"return maf_confirmAction(''{0}'');\" href=\"%s\"><span class=\"glyphicons glyphicons-ban\"></span></a>",
                                    Msg.get("object.data_syndication_agreement.cancel.confirm"));

                            String url = controllers.admin.routes.DataSyndicationController.cancelAgreement(dataSyndicationAgreementListView.id).url();

                            return views.html.framework_views.parts.formats.display_with_format.render(url, content).body();

                        } else {

                            return "";
                        }
                    }
                });
                setColumnCssClass("cancelActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                setColumnValueCssClass("cancelActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                this.setLineAction(new IColumnFormatter<DataSyndicationAgreementListView>() {
                    @Override
                    public String apply(DataSyndicationAgreementListView dataSyndicationAgreementListView, Object value) {
                        return controllers.admin.routes.DataSyndicationController.viewAgreement(dataSyndicationAgreementListView.id, false).url();
                    }
                });

                setEmptyMessageKey("object.data_syndication_agreement.table.empty");

            }
        };

    }

    /**
     * Default constructor.
     */
    public DataSyndicationAgreementListView() {
    }

    public Long id;

    public String masterCustomerName;
    public String masterContactName;
    public String masterContactEmail;

    public String slaveCustomerName;
    public String slaveContactName;
    public String slaveContactEmail;

    public String name;
    public String refId;
    public Date startDate;
    public Date endDate;
    public DataSyndicationAgreement.Status status;

    /**
     * Construct a list view with an agreement.
     * 
     * @param dataSyndicationAgreement
     *            the agreement
     */
    public DataSyndicationAgreementListView(DataSyndicationAgreement dataSyndicationAgreement) {

        this.id = dataSyndicationAgreement.id;

        this.masterCustomerName = dataSyndicationAgreement.masterPartner.customerName;
        this.masterContactName = dataSyndicationAgreement.masterPartner.contactName;
        this.masterContactEmail = dataSyndicationAgreement.masterPartner.contactName;

        this.slaveCustomerName = dataSyndicationAgreement.slavePartner.customerName;
        this.slaveContactName = dataSyndicationAgreement.slavePartner.contactName;
        this.slaveContactEmail = dataSyndicationAgreement.slavePartner.contactEmail;

        this.name = dataSyndicationAgreement.name;
        this.refId = dataSyndicationAgreement.refId;
        this.startDate = dataSyndicationAgreement.startDate;
        this.endDate = dataSyndicationAgreement.endDate;
        this.status = dataSyndicationAgreement.status;
    }

}
