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

import models.architecture.ApplicationBlock;
import models.framework_models.parent.IModelConstants;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import dao.architecture.ArchitectureDao;

/**
 * An application block form data is used to manage the fields when
 * adding/editing an application block.
 * 
 * @author Johann Kohler
 */
public class ApplicationBlockFormData {

    public Long id;

    public Integer order;

    public Long parentId;

    public boolean archived;

    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    public String refId;

    @Required
    @MaxLength(value = IModelConstants.LARGE_STRING)
    public String name;

    @MaxLength(value = IModelConstants.XLARGE_STRING)
    public String description;

    /**
     * Default constructor.
     */
    public ApplicationBlockFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param applicationBlock
     *            the application block in the DB
     */
    public ApplicationBlockFormData(ApplicationBlock applicationBlock) {
        this.id = applicationBlock.id;
        this.order = applicationBlock.order;
        this.parentId = applicationBlock.parent != null ? applicationBlock.parent.id : null;
        this.archived = applicationBlock.archived;
        this.refId = applicationBlock.refId;
        this.name = applicationBlock.name;
        this.description = applicationBlock.description;
    }

    /**
     * Construct the form data with initial values.
     * 
     * @param parentId
     *            the parent id
     * @param order
     *            the order
     */
    public ApplicationBlockFormData(Long parentId, Integer order) {
        this.archived = false;
        this.parentId = parentId;
        this.order = order;
    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param applicationBlock
     *            the application block in the DB
     */
    public void fill(ApplicationBlock applicationBlock) {
        applicationBlock.order = this.order;
        applicationBlock.parent = this.parentId != null ? ArchitectureDao.getApplicationBlockById(this.parentId) : null;
        applicationBlock.archived = this.archived;
        applicationBlock.refId = this.refId;
        applicationBlock.setName(this.name);
        applicationBlock.description = this.description;
    }
}
