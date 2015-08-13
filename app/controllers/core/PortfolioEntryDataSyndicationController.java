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
package controllers.core;

import be.objectify.deadbolt.java.actions.Dynamic;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.CheckPortfolioEntryExists;
import security.DefaultDynamicResourceHandler;

/**
 * The controller which allows to manage the data syndication for a portfolio
 * entry.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryDataSyndicationController extends Controller {

    /**
     * Display the data syndication agreements.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(DefaultDynamicResourceHandler.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result index(Long id) {
        return TODO;
    }

}
