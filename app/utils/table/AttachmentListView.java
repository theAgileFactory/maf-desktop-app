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

import models.framework_models.common.Attachment;
import constants.IMafConstants;
import framework.utils.IColumnFormatter;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import framework.utils.formats.DateFormatter;
import framework.utils.formats.ObjectFormatter;
import framework.utils.formats.StringFormatFormatter;

/**
 * An attachment list view is used to display an attachment row in a table.
 * 
 * @author Johann Kohler
 */
public class AttachmentListView {

    public static Table<AttachmentListView> templateTable = getTable();

    /**
     * Get the table.
     */
    public static Table<AttachmentListView> getTable() {
        return new Table<AttachmentListView>() {
            {
                setIdFieldName("id");

                addColumn("name", "name", "object.attachment.name.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("name", new ObjectFormatter<AttachmentListView>());

                addColumn("lastUpdate", "lastUpdate", "object.attachment.last_update.label", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("lastUpdate", new DateFormatter<AttachmentListView>());

                addCustomAttributeColumns(Attachment.class);

                addColumn("downloadActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("downloadActionLink", new StringFormatFormatter<AttachmentListView>(IMafConstants.DOWNLOAD_URL_FORMAT,
                        new StringFormatFormatter.Hook<AttachmentListView>() {
                            @Override
                            public String convert(AttachmentListView attachmentListView) {
                                return Utilities.getAttachmentDownloadUrl(attachmentListView.id);
                            }
                        }));
                setColumnCssClass("downloadActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                setColumnValueCssClass("downloadActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                addColumn("removeActionLink", "id", "", Table.ColumnDef.SorterType.NONE);
                setJavaColumnFormatter("removeActionLink", new IColumnFormatter<AttachmentListView>() {
                    @Override
                    public String apply(AttachmentListView attachmentListView, Object value) {
                        String deleteConfirmationMessage =
                                MessageFormat.format(IMafConstants.DELETE_URL_FORMAT_WITH_CONFIRMATION, Msg.get("default.delete.confirmation.message"));
                        return views.html.framework_views.parts.formats.display_with_format.render(attachmentListView.deleteUrl, deleteConfirmationMessage)
                                .body();
                    }
                });
                setColumnCssClass("removeActionLink", IMafConstants.BOOTSTRAP_COLUMN_1);
                setColumnValueCssClass("removeActionLink", IMafConstants.BOOTSTRAP_TEXT_ALIGN_RIGHT);

                setEmptyMessageKey("object.attachment.empty");

            }
        };
    }

    /**
     * Default constructor.
     */
    public AttachmentListView() {
    }

    public Long id;

    public String objectType;
    public Long objectId;

    public String name;
    public Date lastUpdate;

    public String deleteUrl;

    /**
     * Construct a list view with a DB entry.
     * 
     * @param attachment
     *            the attachment in the DB
     * @param deleteUrl
     *            the delete url (according to the corresponding object)
     */
    public AttachmentListView(Attachment attachment, String deleteUrl) {

        this.id = attachment.id;

        this.objectId = attachment.objectId;
        this.objectType = attachment.objectType;

        this.name = attachment.name;
        this.lastUpdate = attachment.lastUpdate;

        this.deleteUrl = deleteUrl;
    }

}
