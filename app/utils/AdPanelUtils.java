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
package utils;

import play.twirl.api.Html;
import framework.services.ServiceManager;
import framework.services.remote.IAdPanelManagerService;

/**
 * Provide the methods to manage the ad panel.
 * 
 * For each page it's possible to have a specific panel or the default one. Here
 * is the applied rules to determine which one is displayed on a page:<br/>
 * 1. A specific panel exists for the page => we display it<br/>
 * 2. The default panel exists => we display it<br/>
 * 3. No panel exists => we display nothing
 * 
 * @author Johann Kohler
 * 
 */
public class AdPanelUtils {
    /**
     * Display the ad panel of the given route.
     * 
     * @param route
     *            the current route without query parameters
     */
    public static Html display(String route) {
        IAdPanelManagerService adPanelManagerService = ServiceManager.getService(IAdPanelManagerService.NAME, IAdPanelManagerService.class);
        return adPanelManagerService.getPanel(route);
    }
}
