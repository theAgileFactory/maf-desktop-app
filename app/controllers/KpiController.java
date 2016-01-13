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
package controllers;

import javax.inject.Inject;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import framework.services.kpi.IKpiService;
import play.mvc.Controller;
import play.mvc.Http.Context;
import play.mvc.Result;

/**
 * The controller that provides the KPI.
 * 
 * @author Johann Kohler
 */
@SubjectPresent
public class KpiController extends Controller {

    @Inject
    private IKpiService kpiService;

    /**
     * Display the trend of a KPI.
     */
    public Result trend() {
        return getKpiService().trend(Context.current());
    }

    /**
     * Get the KPI service.
     */
    private IKpiService getKpiService() {
        return kpiService;
    }
}
