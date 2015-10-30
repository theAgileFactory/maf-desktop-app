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

import java.util.ArrayList;
import java.util.List;

import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.CustomConstraints.MultiLanguagesStringMaxLength;
import framework.utils.MultiLanguagesString;
import models.framework_models.account.SystemLevelRoleType;
import models.framework_models.account.SystemPermission;
import models.framework_models.parent.IModelConstants;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;

/**
 * A role form data is used to manage the fields when adding/editing a role.
 * 
 * @author Johann Kohler
 */
public class RoleFormData {

    public Long id;

    @Required
    @MaxLength(value = IModelConstants.SMALL_STRING)
    public String name;

    @MultiLanguagesStringMaxLength(value = IModelConstants.VLARGE_STRING)
    public MultiLanguagesString description;

    public List<Long> permissions = new ArrayList<Long>();

    /**
     * Default constructor.
     */
    public RoleFormData() {
    }

    /**
     * Construct the form data with a DB entry.
     * 
     * @param systemLevelRoleType
     *            the role in the DB
     * @param i18nMessagesPlugin
     *            the i18n manager
     */
    public RoleFormData(SystemLevelRoleType systemLevelRoleType, II18nMessagesPlugin i18nMessagesPlugin) {

        this.id = systemLevelRoleType.id;
        this.name = systemLevelRoleType.name;
        this.description = MultiLanguagesString.getByKey(systemLevelRoleType.description, i18nMessagesPlugin);

        if (systemLevelRoleType.systemPermissions != null) {
            for (SystemPermission permission : systemLevelRoleType.systemPermissions) {
                this.permissions.add(permission.id);
            }
        }

    }

    /**
     * Fill the DB entry with the form values.
     * 
     * @param systemLevelRoleType
     *            the role in the DB
     */
    public void fill(SystemLevelRoleType systemLevelRoleType) {

        systemLevelRoleType.name = this.name.toUpperCase();
        systemLevelRoleType.description = this.description.getKeyIfValue();

        systemLevelRoleType.systemPermissions = new ArrayList<SystemPermission>();
        for (Long permission : this.permissions) {
            if (permission != null) {
                systemLevelRoleType.systemPermissions.add(SystemPermission.getById(permission));
            }
        }

    }
}
