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
import constants.MafDataType;
import dao.governance.LifeCycleMilestoneDao;
import dao.pmo.PortfolioEntryDao;
import dao.pmo.PortfolioEntryPlanningPackageDao;
import dao.pmo.PortfolioEntryReportDao;
import framework.commons.DataType;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.storage.IAttachmentManagerPlugin;
import framework.utils.FilterConfig;
import framework.utils.IColumnFormatter;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.framework_models.common.Attachment;

/**
 * An attachment management list view is used to display the attachments for the admin section.
 * 
 * @author Guillaume Petit
 */
public class AttachmentManagementListView {

    /**
     * The definition of the table.
     *
     * @author Guillaume Petit
     */
    public static class TableDefinition {

        public Table<AttachmentManagementListView> templateTable;
        public FilterConfig<AttachmentManagementListView> filterConfig;

        public TableDefinition(II18nMessagesPlugin i18nMessagesPlugin, IAttachmentManagerPlugin attachmentManagerPlugin) {
            this.templateTable = getTable(i18nMessagesPlugin, attachmentManagerPlugin);
            this.filterConfig = getFilterConfig();
        }
        
        /**
         * This method is to be used to force the reload of the filter config.
         */
        public FilterConfig<AttachmentManagementListView>  getResetedFilterConfig(){
        	this.filterConfig = getFilterConfig();
        	return this.filterConfig;
        }

        /**
         * The attachment management table filter config
         */
        public FilterConfig<AttachmentManagementListView> getFilterConfig() {
            return new FilterConfig<AttachmentManagementListView>() {
                {
                    addColumnConfiguration("name", "name", "object.attachment.name.label", new TextFieldFilterComponent("*"), true, false, SortStatusType.UNSORTED);
                    addColumnConfiguration("mimeType", "mimeType", "object.attachment.mime_type.label", new TextFieldFilterComponent("*"), false, false, SortStatusType.UNSORTED);
                    ISelectableValueHolderCollection<String> objectTypes = Attachment.getDistinctAttachmentsObjectTypes();
                    String defaultObjectType="";
                    if(objectTypes.getValues().size()>0){
                    	defaultObjectType=objectTypes.getValues().iterator().next().getValue();
                    }
                    addColumnConfiguration("objectType", "objectType", "object.attachment.object_type.label", new SelectFilterComponent(defaultObjectType, objectTypes), true, false, SortStatusType.UNSORTED);                   
                    addColumnConfiguration("objectId", "objectId", "object.attachment.object_id.label", new NoneFilterComponent(), true, false, SortStatusType.NONE);
                    addColumnConfiguration("lastUpdate", "lastUpdate", "object.attachment.last_update.label", new DateRangeFilterComponent(new Date(), new Date(), Utilities.getDefaultDatePattern()), true, false, SortStatusType.UNSORTED);
                }
            };
        }

        /**
         * The attachment management table definition
         */
        public Table<AttachmentManagementListView> getTable(II18nMessagesPlugin i18nMessagesPlugin, IAttachmentManagerPlugin attachmentManagerPlugin) {
            return new Table<AttachmentManagementListView>() {
                {
                    setIdFieldName("id");

                    addColumn("name", "name", "object.attachment.name.label", ColumnDef.SorterType.NONE);
                    addColumn("mimeType", "mimeType", "object.attachment.mime_type.label", ColumnDef.SorterType.NONE);
                    addColumn("objectType", "objectType", "object.attachment.object_type.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("objectType", new IColumnFormatter<AttachmentManagementListView>() {
						@Override
						public String apply(AttachmentManagementListView column, Object value) {
							String objectTypeClassNameAsString=String.valueOf(value);
							DataType dt=DataType.getDataTypeFromClassName(objectTypeClassNameAsString);
							return i18nMessagesPlugin.get(dt.getLabel());
						}
					});

                    // Depending on the object type, the object reference link will look different and point to different locations across the application
                    addColumn("objectId", "objectId", "object.attachment.object_id.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("objectId", (attachmentManagementListView, value) -> {
                        if (attachmentManagementListView.objectType.equals(MafDataType.getPortfolioEntry().getDataTypeClassName())) {
                            return views.html.modelsparts.display_portfolio_entry.render(PortfolioEntryDao.getPEById(attachmentManagementListView.objectId), true).body();
                        }
                        if (attachmentManagementListView.objectType.equals(MafDataType.getLifeCycleMilestoneInstance().getDataTypeClassName())) {
                            return views.html.modelsparts.display_milestone_instance.render(LifeCycleMilestoneDao.getLCMilestoneInstanceById(attachmentManagementListView.objectId)).body();
                        }
                        if (attachmentManagementListView.objectType.equals(MafDataType.getPortfolioEntryPlanningPackage().getDataTypeClassName())) {
                            return views.html.modelsparts.display_portfolio_entry_planning_package.render(PortfolioEntryPlanningPackageDao.getPEPlanningPackageById(attachmentManagementListView.objectId)).body();
                        }
                        if (attachmentManagementListView.objectType.equals(MafDataType.getPortfolioEntryReport().getDataTypeClassName())) {
                            return views.html.modelsparts.display_portfolio_entry_report.render(PortfolioEntryReportDao.getPEReportById(attachmentManagementListView.objectId)).body();
                        }
                        return "";
                    });
                    setColumnValueCssClass("objectId", "rowlink-skip");

                    addColumn("lastUpdate", "lastUpdate", "object.attachment.last_update.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("lastUpdate", new DateFormatter<>());

                    addColumn("downloadActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("downloadActionLink", new StringFormatFormatter<>(
                            IMafConstants.DOWNLOAD_URL_FORMAT,
                            (StringFormatFormatter.Hook<AttachmentManagementListView>) attachmentManagementListView -> attachmentManagerPlugin.getAttachmentDownloadUrl(attachmentManagementListView.id))
                    );
                    setColumnCssClass("downloadActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("downloadActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    addColumn("removeActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("removeActionLink", (attachmentManagementListView, value) -> {
                        String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                Msg.get("default.delete.confirmation.message"));
                        return views.html.framework_views.parts.formats.display_with_format
                                .render(controllers.admin.routes.AttachmentsController.deleteAttachment(attachmentManagementListView.id).url(), deleteConfirmationMessage).body();
                    });
                    setColumnCssClass("removeActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("removeActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);
                }
            };
        }

    }

    /**
     * Default constructor.
     */
    public AttachmentManagementListView() {
    }

    public Long id;

    public String objectType;
    public Long objectId;

    public String name;
    public String mimeType;
    public Date lastUpdate;

    /**
     * Construct a list view with a DB entry.
     *
     * @param attachment
     *            the attachment in the DB
     */
    public AttachmentManagementListView(Attachment attachment) {

        this.id = attachment.id;

        this.objectId = attachment.objectId;
        this.objectType = attachment.objectType;

        this.name = attachment.name;
        this.mimeType = attachment.mimeType;
        this.lastUpdate = attachment.lastUpdate;

    }

}
