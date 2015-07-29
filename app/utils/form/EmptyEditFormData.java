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

/**
 * An empty edit form data is used to store the id of an object to id but
 * without direct data field. It's useful when we want to edit only the custom
 * attributes of an entity.
 * 
 * @author Johann Kohler
 */
public class EmptyEditFormData {

    public Long id;

    /**
     * Default constructor.
     */
    public EmptyEditFormData() {
    }

    /**
     * Construct with an id.
     * 
     * @param id
     *            the id
     */
    public EmptyEditFormData(Long id) {

        this.id = id;

    }

}
