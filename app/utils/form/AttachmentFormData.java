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
package utils.form;

import play.data.validation.Constraints.Required;
import play.data.validation.Constraints.ValidateWith;
import framework.utils.FileField;
import framework.utils.FileFieldValidator;

/**
 * An attachment form data is used to manage the fields when adding a new
 * attachment.
 * 
 * @author Johann Kohler
 */
public class AttachmentFormData {

    /**
     * The context id, could also represented to related object (in the case the
     * attribute objectId is not settled).
     */
    public Long id;

    /**
     * The related object for the attachment.
     */
    public Long objectId;

    @Required
    @ValidateWith(value = FileFieldValidator.class, message = "form.input.file_field.error")
    public FileField document;

    /**
     * Default constructor.
     */
    public AttachmentFormData() {
    }

    /**
     * Construct an attachment form data with the attachment id in the DB.
     * 
     * @param id
     *            the attachment id in the DB
     */
    public AttachmentFormData(Long id) {
        this.id = id;
    }

}
