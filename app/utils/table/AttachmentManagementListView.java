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
import constants.MafDataType;
import controllers.admin.routes;
import dao.governance.LifeCycleMilestoneDao;
import dao.pmo.PortfolioEntryDao;
import dao.pmo.PortfolioEntryPlanningPackageDao;
import dao.pmo.PortfolioEntryReportDao;
import framework.commons.DataType;
import framework.services.configuration.II18nMessagesPlugin;
import framework.services.storage.IAttachmentManagerPlugin;
import framework.utils.*;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.StringFormatFormatter;
import models.framework_models.common.Attachment;
import models.pmo.PortfolioEntry;
import play.Logger;
import views.html.modelsparts.display_portfolio_entry;

import java.text.MessageFormat;
import java.util.Date;

/**
 * An attachment management list view is used to display the attachments for the admin section.
 * 
 * @author Guillaume Petit
 */
public class AttachmentManagementListView {
	private static Logger.ALogger log = Logger.of(AttachmentManagementListView.class);
	
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
                    addColumnConfiguration("portfolioEntryId", "portfolioEntryId","object.attachment.link_portfolioentry_id.label", new NoDbFilterComponentWrapper(new AutocompleteFilterComponent(controllers.routes.JsonController.portfolioEntry().url())), true, false, SortStatusType.NONE);
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
                    setJavaColumnFormatter("objectType", (column, value) -> {
                        String objectTypeClassNameAsString=String.valueOf(value);
                        DataType dt=DataType.getDataTypeFromClassName(objectTypeClassNameAsString);
                        return dt == null ? objectTypeClassNameAsString : i18nMessagesPlugin.get(dt.getLabel());
                    });
                    
                    addColumn("portfolioEntryId", "portfolioEntryId", "object.attachment.link_portfolioentry_id.label", ColumnDef.SorterType.STRING_SORTER);
                    setJavaColumnFormatter("portfolioEntryId", (attachmentManagementListView, value) ->
                    	display_portfolio_entry.render(PortfolioEntryDao.getPEById(attachmentManagementListView.portfolioEntryId), true).body()
                    );

                    addColumn("lastUpdate", "lastUpdate", "object.attachment.last_update.label", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("lastUpdate", new DateFormatter<>());

                    addColumn("viewActionLink", "id", "", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("viewActionLink", new StringFormatFormatter<>(IMafConstants.DISPLAY_URL_FORMAT, view -> {
                        try {
                            if (view.objectType.equals(MafDataType.getPortfolioEntry().getDataTypeClassName())) {
                                return controllers.core.routes.PortfolioEntryController.view(view.objectId, 0).url();
                            }
                            if (view.objectType.equals(MafDataType.getLifeCycleMilestoneInstance().getDataTypeClassName())) {
                                return controllers.core.routes.PortfolioEntryGovernanceController.viewMilestone(view.portfolioEntryId, view.objectId).url();
                            }
                            if (view.objectType.equals(MafDataType.getPortfolioEntryPlanningPackage().getDataTypeClassName())) {
                                return controllers.core.routes.PortfolioEntryPlanningController.viewPackage(view.portfolioEntryId, view.objectId).url();
                            }
                            if (view.objectType.equals(MafDataType.getPortfolioEntryReport().getDataTypeClassName())) {
                                return controllers.core.routes.PortfolioEntryStatusReportingController.viewReport(view.portfolioEntryId, view.objectId).url();
                            }
                        } catch (NullPointerException e) {
                            if (log.isDebugEnabled()) {
                                log.debug("Error while looking for the objectid : " + view.objectId);
                            }
                        }
                        return "";
                    }));
                    setColumnCssClass("viewActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("viewActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    addColumn("downloadActionLink", "id", "", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("downloadActionLink", new StringFormatFormatter<>(
                            IMafConstants.DOWNLOAD_URL_FORMAT,
                            (StringFormatFormatter.Hook<AttachmentManagementListView>) attachmentManagementListView -> routes.AttachmentsController.downloadAttachment(attachmentManagementListView.id).url())
                    );
                    setColumnCssClass("downloadActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                    setColumnValueCssClass("downloadActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                    addColumn("removeActionLink", "id", "", ColumnDef.SorterType.NONE);
                    setJavaColumnFormatter("removeActionLink", (attachmentManagementListView, value) -> {
                        String deleteConfirmationMessage = MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION,
                                Msg.get("default.delete.confirmation.message"));
                        return views.html.framework_views.parts.formats.display_with_format
                                .render(routes.AttachmentsController.deleteAttachment(attachmentManagementListView.id).url(), deleteConfirmationMessage).body();
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
    public Long portfolioEntryId;

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
        this.portfolioEntryId=getLinkedPortfolioEntry(objectType, objectId);
        
        this.name = attachment.name;
        this.mimeType = attachment.mimeType;
        this.lastUpdate = attachment.lastUpdate;

    }

    /**
     * Return the id of the linked {@link PortfolioEntry} when one is available
     * @param objectType an object type
     * @param objectId an object id
     * @return a {@link PortfolioEntry} id
     */
    private static Long getLinkedPortfolioEntry(String objectType, Long objectId){
    	if (objectType.equals(MafDataType.getPortfolioEntry().getDataTypeClassName())) {
            PortfolioEntry pe = PortfolioEntryDao.getPEAllById(objectId); 
    		return pe == null ? -1l : pe.id;
        }
        if (objectType.equals(MafDataType.getLifeCycleMilestoneInstance().getDataTypeClassName())) {
        	try{
        		return LifeCycleMilestoneDao.getLCMilestoneInstanceById(objectId).getLifeCycleInstance().portfolioEntry.id;
        	}catch(NullPointerException e){
        		log.warn("Error while looking for the objectid "+objectId+" with object type "+objectType);
        		return -1l;
        	}
        }
        if (objectType.equals(MafDataType.getPortfolioEntryPlanningPackage().getDataTypeClassName())) {
        	try{
        		return PortfolioEntryPlanningPackageDao.getPEPlanningPackageById(objectId).portfolioEntry.id;
	    	}catch(NullPointerException e){
	    		log.warn("Error while looking for the objectid "+objectId+" with object type "+objectType);
	    		return -1l;
	    	}
        }
        if (objectType.equals(MafDataType.getPortfolioEntryReport().getDataTypeClassName())) {
        	try{
            	return PortfolioEntryReportDao.getPEReportById(objectId).portfolioEntry.id;
	    	}catch(NullPointerException e){
	    		log.warn("Error while looking for the objectid "+objectId+" with object type "+objectType);
	    		return -1l;
	    	}
        }
        return -1l;
    }

}
