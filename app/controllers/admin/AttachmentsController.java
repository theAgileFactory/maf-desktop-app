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
package controllers.admin;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import com.avaje.ebean.ExpressionList;
import constants.IMafConstants;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.session.IUserSessionManagerPlugin;
import framework.services.storage.IAttachmentManagerPlugin;
import framework.utils.*;
import models.framework_models.common.Attachment;
import org.apache.commons.lang3.tuple.Pair;
import play.mvc.Controller;
import play.mvc.Result;
import services.tableprovider.ITableProvider;
import utils.table.AttachmentManagementListView;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The administration page used to view, download and delete attached documents throughout the system.
 *
 * @author Guillaume Petit
 */
@Restrict({ @Group(IMafConstants.ADMIN_ATTACHMENTS_MANAGEMENT_PERMISSION) })
public class AttachmentsController extends Controller {

    @Inject
    private IAttachmentManagerPlugin attachmentManagerPlugin;

    @Inject
    private IUserSessionManagerPlugin userSessionManagerPlugin;

    @Inject
    private IPreferenceManagerPlugin preferenceManagerPlugin;

    @Inject
    private ITableProvider tableProvider;

    /**
     * The attachments management main page. Displays a table with the list of all attachments across the application.
     */
    public Result index() {
        String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
        FilterConfig<AttachmentManagementListView> filterConfig = this.getTableProvider().get().attachmentManagement.getResetedFilterConfig().getCurrent(uid, request());

        Pair<Table<AttachmentManagementListView>, Pagination<Attachment>> t = getTable(filterConfig);

        return ok(views.html.admin.attachments.attachments_index.render(t.getLeft(), t.getRight(), filterConfig));
    }

    /**
     * Filters the attachment management table.
     */
    public Result attachmentsFilter() {
        String uid = getUserSessionManagerPlugin().getUserSessionId(ctx());
        FilterConfig<AttachmentManagementListView> filterConfig = this.getTableProvider().get().attachmentManagement.filterConfig.persistCurrentInDefault(uid, request());

        if (filterConfig == null) {
            return ok(views.html.framework_views.parts.table.dynamic_tableview_no_more_compatible.render());
        } else {
            Pair<Table<AttachmentManagementListView>, Pagination<Attachment>> t = getTable(filterConfig);

            return ok(views.html.framework_views.parts.table.dynamic_tableview.render(t.getLeft(), t.getRight()));
        }

    }

    /**
     * Downloads all attachments as an archive
     */
    public Result downloadAttachments() {
        return ok();
    }

    /**
     * Delete an attachment
     *
     * @param id the attachment id
     */
    public Result deleteAttachment(Long id) {
        Attachment attachment = Attachment.getAttachmentFromId(id);

        FileAttachmentHelper.deleteFileAttachment(id, getAttachmentManagerPlugin(), getUserSessionManagerPlugin());

        attachment.doDelete();

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry.attachment.delete"));

        return redirect(controllers.admin.routes.AttachmentsController.index());
    }

    /**
     * Get the attachment management table.
     *
     * @param filterConfig the table filter configuration
     */
    private Pair<Table<AttachmentManagementListView>, Pagination<Attachment>> getTable(FilterConfig<AttachmentManagementListView> filterConfig) {

        ExpressionList<Attachment> expressionList = filterConfig.updateWithSearchExpression(FileAttachmentHelper.getAllAttachmentsForDisplay(getUserSessionManagerPlugin()));
        filterConfig.updateWithSortExpression(expressionList);

        Pagination<Attachment> pagination = new Pagination<>(getPreferenceManagerPlugin(), expressionList);
        pagination.setCurrentPage(filterConfig.getCurrentPage());

        List<AttachmentManagementListView> attachmentManagementListViews = pagination.getListOfObjects().stream().map(AttachmentManagementListView::new).collect(Collectors.toList());

        Table<AttachmentManagementListView> table = getTableProvider().get().attachmentManagement.templateTable.fillForFilterConfig(attachmentManagementListViews, filterConfig.getColumnsToHide());

        return Pair.of(table, pagination);
    }

    public IAttachmentManagerPlugin getAttachmentManagerPlugin() {
        return attachmentManagerPlugin;
    }

    public IUserSessionManagerPlugin getUserSessionManagerPlugin() {
        return userSessionManagerPlugin;
    }

    public ITableProvider getTableProvider() {
        return tableProvider;
    }

    public IPreferenceManagerPlugin getPreferenceManagerPlugin() {
        return preferenceManagerPlugin;
    }

    public enum MenuItemType {
        NONE, SEARCH, DOWNLOAD
    }
}
