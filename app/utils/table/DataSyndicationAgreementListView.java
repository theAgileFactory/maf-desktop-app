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
import framework.utils.FilterConfig;
import framework.utils.FilterConfig.SortStatusType;
import framework.utils.IColumnFormatter;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
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

    public static FilterConfig<DataSyndicationAgreementListView> filterConfig = getFilterConfig();

    /**
     * Get the filter config.
     */
    public static FilterConfig<DataSyndicationAgreementListView> getFilterConfig() {
        return new FilterConfig<DataSyndicationAgreementListView>() {
            {

                addColumnConfiguration("masterCustomerName", "masterCustomerName", "object.data_syndication_agreement.partner.label",
                        new TextFieldFilterComponent("*"), true, false, SortStatusType.UNSORTED);
                addColumnConfiguration("masterContactName", "masterContactName", "object.data_syndication_agreement.contact_name.label",
                        new TextFieldFilterComponent("*"), true, false, SortStatusType.UNSORTED);
                addColumnConfiguration("masterContactEmail", "masterContactEmail", "object.data_syndication_agreement.contact_email.label",
                        new TextFieldFilterComponent("*"), true, false, SortStatusType.UNSORTED);

                addColumnConfiguration("slaveCustomerName", "slaveCustomerName", "object.data_syndication_agreement.partner.label",
                        new TextFieldFilterComponent("*"), true, false, SortStatusType.UNSORTED);
                addColumnConfiguration("slaveContactName", "slaveContactName", "object.data_syndication_agreement.contact_name.label",
                        new TextFieldFilterComponent("*"), true, false, SortStatusType.UNSORTED);
                addColumnConfiguration("slaveContactEmail", "slaveContactEmail", "object.data_syndication_agreement.contact_email.label",
                        new TextFieldFilterComponent("*"), true, false, SortStatusType.UNSORTED);

                addColumnConfiguration("name", "name", "object.data_syndication_agreement.name.label", new TextFieldFilterComponent("*"), true, false,
                        SortStatusType.UNSORTED);

                addColumnConfiguration("refId", "refId", "object.data_syndication_agreement.ref_id.label", new TextFieldFilterComponent("*"), true, false,
                        SortStatusType.UNSORTED);

                addColumnConfiguration("startDate", "startDate", "object.data_syndication_agreement.start_date.label",
                        new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.UNSORTED);

                addColumnConfiguration("endDate", "endDate", "object.data_syndication_agreement.end_date.label",
                        new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.UNSORTED);

                ISelectableValueHolderCollection<String> statuses = DataSyndicationAgreement.Status.getAllAsVH();
                addColumnConfiguration("status", "status.id", "object.portfolio_entry.status.label",
                        new SelectFilterComponent(statuses.getValues().iterator().next().getValue(), statuses), true, false, SortStatusType.NONE);

            }
        };
    }

    public static Table<DataSyndicationAgreementListView> templateTable = getTable();

    /**
     * Get the table.
     */
    public static Table<DataSyndicationAgreementListView> getTable() {
        return new Table<DataSyndicationAgreementListView>() {
            {
                setIdFieldName("id");

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

                addColumn("name", "name", "object.data_syndication_agreement.name.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("name", new ObjectFormatter<DataSyndicationAgreementListView>());

                addColumn("refId", "refId", "object.data_syndication_agreement.ref_id.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("refId", new ObjectFormatter<DataSyndicationAgreementListView>());

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

                addColumn("processActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("processActionLink", new IColumnFormatter<DataSyndicationAgreementListView>() {
                    @Override
                    public String apply(DataSyndicationAgreementListView dataSyndicationAgreementListView, Object value) {

                        String content = "<a title=\"" + Msg.get("object.data_syndication_agreement.process.label")
                                + "\" href=\"%s\"><span class=\"glyphicons glyphicons-thumbs-up\"></span></a>";

                        String url = controllers.admin.routes.DataSyndicationController.processAgreement(dataSyndicationAgreementListView.id).url();

                        return views.html.framework_views.parts.formats.display_with_format.render(url, content).body();
                    }
                });
                setColumnCssClass("processActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                setColumnValueCssClass("processActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                addColumn("suspendActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("suspendActionLink", new IColumnFormatter<DataSyndicationAgreementListView>() {
                    @Override
                    public String apply(DataSyndicationAgreementListView dataSyndicationAgreementListView, Object value) {

                        String content = MessageFormat.format(
                                "<a onclick=\"return maf_confirmAction(''{0}'');\" href=\"%s\"><span class=\"glyphicons glyphicons-pause\"></span></a>",
                                Msg.get("object.data_syndication_agreement.suspend.confirm"));

                        String url = controllers.admin.routes.DataSyndicationController.suspendAgreement(dataSyndicationAgreementListView.id).url();

                        return views.html.framework_views.parts.formats.display_with_format.render(url, content).body();
                    }
                });
                setColumnCssClass("suspendActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                setColumnValueCssClass("suspendActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                addColumn("restartActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("restartActionLink", new IColumnFormatter<DataSyndicationAgreementListView>() {
                    @Override
                    public String apply(DataSyndicationAgreementListView dataSyndicationAgreementListView, Object value) {

                        String content = "<a href=\"%s\"><span class=\"glyphicons glyphicons-play\"></span></a>";

                        String url = controllers.admin.routes.DataSyndicationController.restartAgreement(dataSyndicationAgreementListView.id).url();

                        return views.html.framework_views.parts.formats.display_with_format.render(url, content).body();
                    }
                });
                setColumnCssClass("restartActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                setColumnValueCssClass("restartActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                addColumn("cancelActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("cancelActionLink", new IColumnFormatter<DataSyndicationAgreementListView>() {
                    @Override
                    public String apply(DataSyndicationAgreementListView dataSyndicationAgreementListView, Object value) {

                        String content = MessageFormat.format(
                                "<a onclick=\"return maf_confirmAction(''{0}'');\" href=\"%s\"><span class=\"glyphicons glyphicons-ban\"></span></a>",
                                Msg.get("object.data_syndication_agreement.cancel.confirm"));

                        String url = controllers.admin.routes.DataSyndicationController.cancelAgreement(dataSyndicationAgreementListView.id).url();

                        return views.html.framework_views.parts.formats.display_with_format.render(url, content).body();
                    }
                });
                setColumnCssClass("cancelActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                setColumnValueCssClass("cancelActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT + " rowlink-skip");

                this.setLineAction(new IColumnFormatter<DataSyndicationAgreementListView>() {
                    @Override
                    public String apply(DataSyndicationAgreementListView dataSyndicationAgreementListView, Object value) {
                        return controllers.admin.routes.DataSyndicationController.viewAgreement(dataSyndicationAgreementListView.id).url();
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
