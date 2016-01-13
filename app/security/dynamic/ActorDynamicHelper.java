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
package security.dynamic;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.OrderBy;

import constants.IMafConstants;
import dao.pmo.ActorDao;
import framework.security.ISecurityService;
import framework.services.account.AccountManagementException;
import framework.services.account.IUserAccount;
import framework.utils.Utilities;
import models.pmo.Actor;
import models.sql.ActorHierarchy;
import play.Logger;

/**
 * Provides all method to compute the dynamic permissions for an actor.
 * 
 * @author Johann Kohler
 */
public class ActorDynamicHelper {

    /**
     * Get the ebean expression list for all authorized actors of the sign-in
     * user. It's possible to filter and sort the list.
     * 
     * @param expression
     *            the ebean filter expression
     * @param orderBy
     *            the ebean order by
     * @param securityService
     *            the security service
     */
    public static ExpressionList<Actor> getActorsViewAllowedAsQuery(Expression expression, OrderBy<Actor> orderBy, ISecurityService securityService)
            throws AccountManagementException {
        IUserAccount currentUserAccount = securityService.getCurrentUser();
        String raw = "(";

        // user has permission ACTOR_VIEW_ALL_PERMISSION
        // OR
        if (securityService.restrict(IMafConstants.ACTOR_VIEW_ALL_PERMISSION, currentUserAccount)) {
            raw += "1 = '1' OR ";
        }

        Actor actor = ActorDao.getActorByUid(currentUserAccount.getIdentifier());
        if (actor != null) {

            // the actor "is" the user
            raw += "id = " + actor.id + " OR ";

            // user has permission
            // ACTOR_VIEW_AS_SUPERIOR_PERMISSION AND
            // user or his subordinates is manager of the actor OR
            if (securityService.restrict(IMafConstants.ACTOR_VIEW_AS_SUPERIOR_PERMISSION, currentUserAccount)) {
                raw += "manager.id = " + actor.id + " OR ";

                String subordinatesString = ActorHierarchy.getSubordinatesAsString(actor.id, ",");
                if (subordinatesString != null && !subordinatesString.trim().isEmpty()) {
                    raw += "manager.id IN (" + subordinatesString + ") OR ";
                }
            }

        }

        raw += "1 = '0')";

        ExpressionList<Actor> expressionList;

        if (orderBy != null) {
            expressionList = ActorDao.findActor.where();
            Utilities.updateExpressionListWithOrderBy(orderBy, expressionList);

        } else {
            expressionList = ActorDao.findActor.where();
        }

        expressionList = expressionList.eq("deleted", false);

        if (expression != null) {
            return expressionList.add(expression).raw(raw);
        } else {
            return expressionList.raw(raw);
        }

    }

    /**
     * Define if a user can view the details of an actor.
     * 
     * @param actorId
     *            the actor id
     * @param securityService
     *            the security service
     */
    public static boolean isActorViewAllowed(Long actorId, ISecurityService securityService) {
        try {
            return getActorsViewAllowedAsQuery(Expr.eq("id", actorId), null, securityService).findRowCount() > 0 ? true : false;
        } catch (AccountManagementException e) {
            Logger.error("ActorDynamicHelper.isActorViewAllowed: impossible to get the user account");
            Logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * Define if a user can edit the details of an actor.
     * 
     * @param actor
     *            the actor to edit
     * @param securityService
     *            the security service
     */
    public static boolean isActorEditAllowed(Actor actor, ISecurityService securityService) {

        try {
            IUserAccount currentUserAccount = securityService.getCurrentUser();
            // user has permission ACTOR_EDIT_ALL_PERMISSION OR
            if (securityService.restrict(IMafConstants.ACTOR_EDIT_ALL_PERMISSION, currentUserAccount)) {
                return true;
            }

            // the actor "is" the user
            Actor currenctActor = ActorDao.getActorByUid(currentUserAccount.getIdentifier());
            if (currenctActor != null && currenctActor.id.equals(actor.id)) {
                return true;
            }

        } catch (Exception e) {

            Logger.error("impossible to get the user account", e);

        }

        return false;

    }

    /**
     * Define if a user can delete the details of an actor.
     * 
     * @param actor
     *            the actor to delete
     * @param securityService
     *            the security service
     */
    public static boolean isActorDeleteAllowed(Actor actor, ISecurityService securityService) {

        try {
            IUserAccount currentUserAccount = securityService.getCurrentUser();
            // user has permission ACTOR_EDIT_ALL_PERMISSION OR
            if (securityService.restrict(IMafConstants.ACTOR_EDIT_ALL_PERMISSION, currentUserAccount)) {
                return true;
            }

        } catch (Exception e) {

            Logger.error("impossible to get the user account", e);

        }

        return false;

    }

}
