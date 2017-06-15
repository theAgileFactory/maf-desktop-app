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

import models.framework_models.account.SystemPermission;
import models.framework_models.kpi.KpiDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * KPI permissions form data
 *
 * @author Guillaume Petit
 */
public class KpiPermissionsFormData {

    public Long id;

    public List<Long> permissions = new ArrayList<>();

    public KpiPermissionsFormData() {}

    public KpiPermissionsFormData(KpiDefinition kpiDefinition) {

        this.id = kpiDefinition.id;

        kpiDefinition.systemPermissions.stream()
                .forEach(
                        systemPermission -> this.permissions.add(systemPermission.id)
                );
    }

    public void fill(KpiDefinition kpiDefinition) {

        kpiDefinition.systemPermissions = new ArrayList<>();

        this.permissions.stream()
                .forEach(
                        permissionId -> {
                            if (permissionId != null) {
                                kpiDefinition.systemPermissions.add(SystemPermission.getById(permissionId));
                            }
                        }
                );
    }

}
