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

import play.mvc.Controller;
import play.mvc.Result;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import dao.architecture.ArchitectureDao;
import dao.finance.PurchaseOrderDAO;
import dao.pmo.ActorDao;
import dao.pmo.OrgUnitDao;
import dao.pmo.PortfolioDao;
import framework.utils.Utilities;

/**
 * The controller that provides the json data (API).
 * 
 * @author Johann Kohler
 */
@SubjectPresent
public class JsonController extends Controller {

    /**
     * Search from actors.
     */
    public Result manager() {
        return ok(Utilities.marshallAsJson(ActorDao.getActorAsVHByKeywords(request().getQueryString("query")).getValues()));
    }

    /**
     * Search from sponsoring units.
     */
    public Result sponsoringUnit() {
        return ok(Utilities.marshallAsJson(OrgUnitDao.getOrgUnitActiveCanSponsorAsVHByKeywords(request().getQueryString("query")).getValues()));
    }

    /**
     * Search from delivery units.
     */
    public Result deliveryUnit() {
        return ok(Utilities.marshallAsJson(OrgUnitDao.getOrgUnitActiveCanDeliverAsVHByKeywords(request().getQueryString("query")).getValues()));
    }

    /**
     * Search from portfolios.
     */
    public Result portfolio() {
        return ok(Utilities.marshallAsJson(PortfolioDao.getPortfolioAsVHByKeywords(request().getQueryString("query")).getValues()));
    }

    /**
     * Search from purchase orders.
     */
    public Result purchaseOrder() {
        return ok(Utilities.marshallAsJson(PurchaseOrderDAO.getPurchaseOrderActiveAsVHByRefIdLike(request().getQueryString("query")).getValues()));
    }

    /**
     * Search from application blocks.
     */
    public Result applicationBlock() {
        return ok(Utilities.marshallAsJson(ArchitectureDao.getApplicationBlockAsVHByKeywords(request().getQueryString("query")).getValues()));
    }
}
