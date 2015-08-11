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
package controllers.api;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;

import services.licensesmanagement.ILicensesManagementService;
import framework.services.api.server.IApiAuthenticationAdditionalCheck;

/**
 * The BizDock additional check.
 * 
 * @author Johann Kohler
 * 
 */
public class ApiAuthenticationBizdockCheck implements IApiAuthenticationAdditionalCheck {
    @Inject
    private static ILicensesManagementService licensesManagementService;
    
    @Override
    public Pair<Boolean, String> before() {
        if (!getLicensesManagementService().isInstanceAccessible()) {
            return Pair.of(false, "the instance is not accessible");
        }
        return Pair.of(true, null);
    }

    @Override
    public Pair<Boolean, String> after() {
        return Pair.of(true, null);
    }

    private static ILicensesManagementService getLicensesManagementService() {
        return licensesManagementService;
    }

}
