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
package dao.pmo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.PersistenceException;

import models.framework_models.account.NotificationCategory;
import models.framework_models.account.Principal;
import models.pmo.Actor;
import models.pmo.ActorCapacity;
import models.pmo.ActorType;
import models.pmo.Competency;

import org.apache.commons.lang3.StringUtils;

import play.Logger;

import com.avaje.ebean.Model.Finder;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.SqlUpdate;

import framework.services.ServiceStaticAccessor;
import framework.services.account.AccountManagementException;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IUserAccount;
import framework.services.configuration.Language;
import framework.services.notification.INotificationManagerPlugin;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Msg;

/**
 * DAO for the {@link Actor} and {@link ActorCapacity} and {@link ActorType} and
 * {@link Competency} objects.
 * 
 * @author Johann Kohler
 */
public abstract class ActorDao {

    public static Finder<Long, Actor> findActor = new Finder<>(Actor.class);

    public static Finder<Long, ActorCapacity> findActorCapacity = new Finder<>(ActorCapacity.class);

    public static Finder<Long, ActorType> findActorType = new Finder<>(ActorType.class);

    public static Finder<Long, Competency> findCompetency = new Finder<>(Competency.class);

    /**
     * Default constructor.
     */
    public ActorDao() {
    }

    /**
     * Get an actor by id.
     * 
     * @param id
     *            the actor id
     */
    public static Actor getActorById(Long id) {
        return findActor.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get an actor by uid.
     * 
     * @param uid
     *            the uid
     */
    public static Actor getActorByUid(String uid) {
        return findActor.where().eq("deleted", false).eq("uid", uid).findUnique();
    }

    /**
     * Get an actor by email.
     * 
     * If many actors match the email, then the method returns the first.
     * 
     * @param email
     *            the email
     */
    public static Actor getActorByEmail(String email) {
        List<Actor> actors = findActor.where().eq("deleted", false).eq("mail", email).findList();
        if (actors != null && actors.size() > 0) {
            return actors.get(0);
        }
        return null;
    }

    /**
     * Get an actor by refId (a not deleted actor).
     * 
     * @param refId
     *            the refId
     */
    public static Actor getActorByRefId(String refId) {
        try {
            return findActor.where().eq("deleted", false).eq("refId", refId).findUnique();
        } catch (PersistenceException e) {
            return findActor.where().eq("deleted", false).eq("refId", refId).findList().get(0);
        }
    }

    /**
     * Get a deleted actor by refId.
     * 
     * @param refId
     *            the refId
     */
    public static Actor getActorDeletedByRefId(String refId) {
        try {
            return findActor.where().eq("deleted", true).eq("refId", refId).findUnique();
        } catch (PersistenceException e) {
            return findActor.where().eq("deleted", true).eq("refId", refId).findList().get(0);
        }
    }

    /**
     * Get an actor (deleted or not deleted) using its refId.
     * 
     * @param refId
     *            the refId
     */
    public static Actor getActorAnyByRefId(String refId) {
        try {
            return findActor.where().eq("refId", refId).findUnique();
        } catch (PersistenceException e) {
            return findActor.where().eq("refId", refId).findList().get(0);
        }
    }

    /**
     * Get an actor by erp ref id (a not deleted actor).
     * 
     * @param erpRefId
     *            the erp ref id
     */
    public static Actor getActorByErpRefId(String erpRefId) {
        try {
            return findActor.where().eq("deleted", false).eq("erpRefId", erpRefId).findUnique();
        } catch (PersistenceException e) {
            return findActor.where().eq("deleted", false).eq("erpRefId", erpRefId).findList().get(0);
        }
    }

    /**
     * Get a deleted actor by uid, useful to undelete an actor instead to create
     * a new one.
     * 
     * @param uid
     *            the uid
     */
    public static Actor getActorDeletedByUid(String uid) {
        try {
            return findActor.where().eq("deleted", true).eq("uid", uid).findUnique();
        } catch (PersistenceException e) {
            return findActor.where().eq("deleted", true).eq("uid", uid).findList().get(0);
        }

    }

    /**
     * WARNING: user carefully this method since it unactivate ALL THE ACTORS in
     * the database.
     * 
     * @param whereClause
     *            a where clause (without the where statement)
     */
    public static int unactivateActors(String whereClause) {
        String sql = "update actor set is_active=0 " + (StringUtils.isBlank(whereClause) ? "" : "where " + whereClause);
        SqlUpdate update = Ebean.createSqlUpdate(sql);
        return Ebean.execute(update);
    }

    /**
     * Get all active actors.
     */
    public static List<Actor> getActorActiveAsList() {
        return findActor.where().eq("deleted", false).eq("isActive", true).findList();
    }

    /**
     * Get all active actors without uid.
     */
    public static List<Actor> getActorActiveWithoutUidAsList() {
        return findActor.where().eq("deleted", false).eq("isActive", true).disjunction().isNull("uid").eq("uid", "").findList();
    }

    /**
     * Get all active actors as a value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getActorActiveAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getActorActiveAsList());
    }

    /**
     * Get all active actors as without uid a value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getActorActiveWithoutUidAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getActorActiveWithoutUidAsList());
    }

    /**
     * Get the active actor of an org unit.
     * 
     * @param orgUnitId
     *            the org unit id
     */
    public static List<Actor> getActorActiveAsListByOrgUnit(Long orgUnitId) {
        return findActor.where().eq("deleted", false).eq("isActive", true).eq("orgUnit.id", orgUnitId).findList();
    }

    /**
     * Get the active actor with a competency.
     * 
     * @param competencyId
     *            the competency id
     */
    public static List<Actor> getActorActiveAsListByCompetency(Long competencyId) {
        return findActor.where().eq("deleted", false).eq("isActive", true).eq("competencies.id", competencyId).findList();
    }

    /**
     * Get the active actor of an org unit as a value holder collection.
     * 
     * @param orgUnitId
     *            the org unit id
     */
    public static ISelectableValueHolderCollection<Long> getActorActiveAsVHByOrgUnit(Long orgUnitId) {
        return new DefaultSelectableValueHolderCollection<>(getActorActiveAsListByOrgUnit(orgUnitId));
    }

    /**
     * Get the active actors with a competency as a value holder collection.
     * 
     * @param competencyId
     *            the competency id
     */
    public static ISelectableValueHolderCollection<Long> getActorActiveAsVHByCompetency(Long competencyId) {
        return new DefaultSelectableValueHolderCollection<>(getActorActiveAsListByCompetency(competencyId));
    }

    /**
     * Get all actors of a manager.
     * 
     * @param manageId
     *            the actor id of the manager
     */
    public static List<Actor> getActorAsListByManager(Long manageId) {
        return findActor.where().eq("deleted", false).eq("manager.id", manageId).findList();
    }

    /**
     * Get the active actors that are direct stakeholders of a portfolio entry.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static List<Actor> getActorActiveAsListByPE(Long portfolioEntryId) {
        return findActor.where().eq("deleted", false).eq("isActive", true).eq("stakeholders.deleted", false)
                .eq("stakeholders.portfolioEntry.id", portfolioEntryId).findList();
    }

    /**
     * Get the active actors that are direct stakeholders of a portfolio entry
     * as a value holder collection.
     * 
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static ISelectableValueHolderCollection<Long> getActorActiveAsVHByPE(Long portfolioEntryId) {
        return new DefaultSelectableValueHolderCollection<>(getActorActiveAsListByPE(portfolioEntryId));
    }

    /**
     * Search from all actors for which the criteria matches with
     * "firstName lastName" or "lastName firstName".
     * 
     * @param key
     *            the search criteria (use % for wild cards)
     */
    public static List<Actor> getActorAsListByKeywords(String key) {
        key = key.replace("\"", "\\\"");
        String sql =
                "SELECT a.id FROM actor a WHERE " + "a.deleted = false AND (a.ref_id LIKE \"" + key
                        + "%\" OR CONCAT_WS(\" \", a.first_name, a.last_name) LIKE \"" + key + "%\" OR "
                        + "CONCAT_WS(\" \", a.last_name, a.first_name) LIKE \"" + key + "%\")";
        RawSql rawSql = RawSqlBuilder.parse(sql).columnMapping("a.id", "id").create();
        return findActor.query().setRawSql(rawSql).findList();
    }

    /**
     * Search from all active actors for which the criteria matches with
     * "firstName lastName" or "lastName firstName".
     * 
     * @param key
     *            the search criteria (use % for wild cards)
     */
    public static List<Actor> getActorActiveAsListByKeywords(String key) {
        key = key.replace("\"", "\\\"");
        String sql =
                "SELECT a.id FROM actor a WHERE " + "a.deleted = false AND a.is_active = true AND (a.ref_id LIKE \"" + key
                        + "%\" OR CONCAT_WS(\" \", a.first_name, a.last_name) LIKE \"" + key + "%\" OR "
                        + "CONCAT_WS(\" \", a.last_name, a.first_name) LIKE \"" + key + "%\")";
        RawSql rawSql = RawSqlBuilder.parse(sql).columnMapping("a.id", "id").create();
        return findActor.query().setRawSql(rawSql).findList();
    }

    /**
     * Search from all active actors without uid for which the criteria matches
     * with "firstName lastName" or "lastName firstName".
     * 
     * @param key
     *            the search criteria (use % for wild cards)
     */
    public static List<Actor> getActorActiveWithoutUidAsListByKeywords(String key) {
        key = key.replace("\"", "\\\"");
        String sql =
                "SELECT a.id FROM actor a WHERE " + "a.deleted = false AND a.is_active = true AND (a.uid IS NULL OR a.uid = '') AND (a.ref_id LIKE \"" + key
                        + "%\" OR CONCAT_WS(\" \", a.first_name, a.last_name) LIKE \"" + key + "%\" OR "
                        + "CONCAT_WS(\" \", a.last_name, a.first_name) LIKE \"" + key + "%\")";
        RawSql rawSql = RawSqlBuilder.parse(sql).columnMapping("a.id", "id").create();
        return findActor.query().setRawSql(rawSql).findList();
    }

    /**
     * Search from all actors with the search process defined by the method
     * "getActorAsListByKeywords" and return a value holder collection.
     * 
     * @param key
     *            the search criteria (use % for wild cards)
     */
    public static ISelectableValueHolderCollection<Long> getActorAsVHByKeywords(String key) {
        return new DefaultSelectableValueHolderCollection<>(getActorAsListByKeywords(key));
    }

    /**
     * Search from all active actors with the search process defined by the
     * method "getActorActiveAsListByKeywords" and return a value holder
     * collection.
     * 
     * @param key
     *            the search criteria (use % for wild cards)
     */
    public static ISelectableValueHolderCollection<Long> getActorActiveAsVHByKeywords(String key) {
        return new DefaultSelectableValueHolderCollection<>(getActorActiveAsListByKeywords(key));
    }

    /**
     * Search from all active actors without uid and return a value holder
     * collection.
     * 
     * @param key
     *            the search criteria (use % for wild cards)
     */
    public static ISelectableValueHolderCollection<Long> getActorActiveWithoutUidAsVHByKeywords(String key) {
        return new DefaultSelectableValueHolderCollection<>(getActorActiveWithoutUidAsListByKeywords(key));
    }

    /**
     * Search from the active actors that are direct stakholders of a portfolio
     * entry and return a value holder collection.
     * 
     * @param key
     *            the search criteria (use % for wild cards)
     * @param portfolioEntryId
     *            the portfolio entry id
     */
    public static ISelectableValueHolderCollection<Long> getActorActiveAsVHByKeywordsAndPE(String key, Long portfolioEntryId) {
        key = key.replace("\"", "\\\"");
        String sql =
                "SELECT a.id FROM actor a JOIN stakeholder s ON a.id = s.actor_id WHERE "
                        + "a.deleted = false AND a.is_active = true AND s.deleted = false AND s.portfolio_entry_id = '" + portfolioEntryId
                        + "' AND (CONCAT_WS(\" \", a.first_name, a.last_name) like \"" + key + "%\" OR "
                        + "CONCAT_WS(\" \", a.last_name, a.first_name) like \"" + key + "%\")";
        RawSql rawSql = RawSqlBuilder.parse(sql).columnMapping("a.id", "id").create();
        List<Actor> actors = findActor.query().setRawSql(rawSql).findList();

        return new DefaultSelectableValueHolderCollection<>(actors);
    }

    /**
     * Return the actor associated with the specified uid. If not found, look
     * for a {@link Principal} with the same id to create a new actor.
     * 
     * @param uid
     *            the uid
     * 
     * @return an actor or null if the actor is not found (nor any Principal)
     */
    public static Actor getActorByUidOrCreateDefaultActor(String uid) {
        Actor actor = getActorByUid(uid);
        if (actor == null) {
            actor = getActorDeletedByUid(uid);
            if (actor != null) {
                actor.deleted = false;
                actor.save();
            } else {
                IAccountManagerPlugin accountManagerPlugin = ServiceStaticAccessor.getAccountManagerPlugin();
                try {
                    IUserAccount userAccount = accountManagerPlugin.getUserAccountFromUid(uid);
                    if (userAccount != null) {
                        actor = new Actor();
                        actor.firstName = userAccount.getFirstName();
                        actor.lastName = userAccount.getLastName();
                        actor.mail = userAccount.getMail();
                        actor.uid = userAccount.getUid();
                        actor.save();
                    }
                } catch (AccountManagementException e) {
                    Logger.error("Error while creating a default actor from a Principal uid=" + uid, e);
                }
            }
        }
        return actor;
    }

    /**
     * Send a notification to a user (principal).
     * 
     * @param recipient
     *            the uid of the user (principal)
     * @param category
     *            the notification category
     * @param url
     *            the attached url for the notification
     * @param titleKey
     *            the i18n key of the notification title
     * @param messageKey
     *            the i18n key of the message to send
     * @param args
     *            the arguments for the message
     */
    public static void sendNotification(String recipient, NotificationCategory category, String url, String titleKey, String messageKey, Object... args) {

        // get the principal
        Principal principal = Principal.getPrincipalFromUid(recipient);

        if (principal != null) {

            // get the language
            Language language = new Language(principal.preferredLanguage);

            // construct the title and the message
            String message = null;
            String title = null;
            if (ServiceStaticAccessor.getMessagesPlugin().isLanguageValid(language.getCode())) {
                message = Msg.get(language.getLang(), messageKey, args);
                title = Msg.get(language.getLang(), titleKey);
            } else {
                message = Msg.get(messageKey, args);
                title = Msg.get(titleKey);
            }

            ServiceStaticAccessor.getNotificationManagerPlugin().sendNotification(recipient, category, title, message, url);
        }
    }

    /**
     * Send a notification to the user (principal) associated to an actor. If
     * the actor is not linked to a principal then the notification is not sent.
     * 
     * @param actor
     *            the actor for which the notification should be sent
     * @param category
     *            the notification category
     * @param url
     *            the attached url for the notification (can be null)
     * @param titleKey
     *            the i18n key of the notification title
     * @param messageKey
     *            the i18n key of the message to send
     * @param args
     *            the arguments for the message
     */
    public static void sendNotification(Actor actor, NotificationCategory category, String url, String titleKey, String messageKey, Object... args) {
        if (actor != null && actor.uid != null && !actor.uid.equals("")) {
            sendNotification(actor.uid, category, url, titleKey, messageKey, args);
        }
    }

    /**
     * Send a notification to a list of actors. For each actor we take the
     * principal (only if it exists).
     * 
     * @param actors
     *            the list of actors for which the notification should be sent
     * @param category
     *            the notification category
     * @param url
     *            the attached url for the notification (can be null)
     * @param titleKey
     *            the i18n key of the notification title
     * @param messageKey
     *            the i18n key of the message to send
     * @param args
     *            the arguments for the message
     */
    public static void sendNotification(List<Actor> actors, NotificationCategory category, String url, String titleKey, String messageKey, Object... args) {
        Set<String> recipients = new HashSet<>();
        for (Actor actor : actors) {
            if (actor != null && actor.uid != null && !actor.uid.equals("")) {
                recipients.add(actor.uid);
            }
        }
        for (String recipient : recipients) {
            sendNotification(recipient, category, url, titleKey, messageKey, args);
        }
    }

    /**
     * Get the actors list with filters.
     * 
     * @param isActive
     *            true to return only active actors, false only non-active, null
     *            all.
     * @param managerId
     *            if not null then return only actors with the given manager.
     * @param actorTypeId
     *            if not null then return only actors with the given type.
     * @param competencyId
     *            if not null then return only actors with the given competency.
     * @param orgUnitId
     *            if not null then return only actors with the given org unit.
     */
    public static List<Actor> getActorAsListByFilter(Boolean isActive, Long managerId, Long actorTypeId, Long competencyId, Long orgUnitId) {
        ExpressionList<Actor> e = findActor.where().eq("deleted", false);
        if (isActive != null) {
            e = e.eq("isActive", isActive);
        }
        if (managerId != null) {
            e = e.eq("manager.id", managerId);
        }
        if (actorTypeId != null) {
            e = e.eq("actorType.id", actorTypeId);
        }
        if (competencyId != null) {
            e = e.eq("competencies.id", competencyId);
        }
        if (orgUnitId != null) {
            e = e.eq("orgUnit.id", orgUnitId);
        }

        return e.findList();
    }

    /**
     * Get a capacity by id.
     * 
     * @param id
     *            the capacity id
     */
    public static ActorCapacity getActorCapacityById(Long id) {
        return findActorCapacity.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get the capacities of an actor for a year.
     * 
     * This method returns always an array with 12 entries. If some capacities
     * are not yet in the DB then they are created.
     * 
     * The capacities are returned with a month order: the first entry (index =
     * 0) is for January and the last (index = 11) for December.
     * 
     * @param actor
     *            the actor
     * @param year
     *            the year
     */
    public static ActorCapacity[] getActorCapacityAsArrayByActorAndYear(Actor actor, Integer year) {

        ActorCapacity[] r = new ActorCapacity[12];

        for (ActorCapacity actorCapacity : findActorCapacity.where().eq("deleted", false).eq("actor.id", actor.id).eq("year", year).findList()) {
            if (actorCapacity.month >= 1 && actorCapacity.month <= 12) {
                r[actorCapacity.month - 1] = actorCapacity;
            }
        }

        for (int i = 0; i < r.length; i++) {
            if (r[i] == null) {
                ActorCapacity newActorCapacity = new ActorCapacity();
                newActorCapacity.actor = actor;
                newActorCapacity.month = i + 1;
                newActorCapacity.value = 0.0;
                newActorCapacity.year = year;
                newActorCapacity.save();
                r[i] = newActorCapacity;
            }
        }

        return r;

    }

    /**
     * Get the actor capacities for an org unit.
     * 
     * @param orgUnitId
     *            the org unit
     * @param year
     *            the year
     */
    public static List<ActorCapacity> getActorCapacityAsListByOrgUnitAndYear(Long orgUnitId, Integer year) {
        return findActorCapacity.where().eq("deleted", false).eq("actor.deleted", false).eq("actor.orgUnit.id", orgUnitId).eq("year", year).findList();
    }

    /**
     * Get the actor capacities for a competency.
     * 
     * @param competencyId
     *            the competency id
     * @param year
     *            the year
     */
    public static List<ActorCapacity> getActorCapacityAsListByCompetencyAndYear(Long competencyId, Integer year) {
        return findActorCapacity.where().eq("deleted", false).eq("actor.deleted", false).eq("actor.defaultCompetency.id", competencyId).eq("year", year)
                .findList();
    }

    /**
     * Get the capacity of an actor for a exact month of a year.
     * 
     * @param actorId
     *            the actor id
     * @param year
     *            the year
     * @param month
     *            the month
     */
    public static ActorCapacity getActorCapacityByActorAndPeriod(Long actorId, Integer year, Integer month) {
        return findActorCapacity.where().eq("deleted", false).eq("actor.id", actorId).eq("year", year).eq("month", month).findUnique();
    }

    /**
     * Get the actor capacities list with filters.
     * 
     * @param actorId
     *            if not null then return only actor capacities for the given
     *            actor.
     * @param year
     *            if not null then return only actor capacities with the given
     *            year.
     */
    public static List<ActorCapacity> getActorCapacityAsListByFilter(Long actorId, Integer year) {
        ExpressionList<ActorCapacity> e = findActorCapacity.where().eq("deleted", false);

        if (actorId != null) {
            e = e.eq("actor.id", actorId);
        }
        if (year != null) {
            e = e.eq("year", year);
        }

        return e.findList();
    }

    /**
     * Get an actor type by id.
     * 
     * @param id
     *            the actor type id
     */
    public static ActorType getActorTypeById(Long id) {
        return findActorType.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get the actor type associated with the specified id.
     * 
     * @param refId
     *            an actor type ref Id.
     */
    public static ActorType getActorTypeByRefId(String refId) {
        try {
            return findActorType.where().eq("deleted", false).eq("refId", refId).findUnique();
        } catch (PersistenceException e) {
            return findActorType.where().eq("deleted", false).eq("refId", refId).findList().get(0);
        }
    }

    /**
     * Get all actor types.
     */
    public static List<ActorType> getActorTypeAsList() {
        return findActorType.where().eq("deleted", false).findList();
    }

    /**
     * Get all active actor types.
     */
    public static List<ActorType> getActorTypeActiveAsList() {
        return findActorType.where().eq("deleted", false).eq("selectable", true).findList();
    }

    /**
     * Get all active actor types as value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getActorTypeActiveAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getActorTypeActiveAsList());
    }

    /**
     * Get the actor types list with filters.
     * 
     * @param selectable
     *            true to return only active type, false only non-active, null
     *            all.
     */
    public static List<ActorType> getActorTypeAsListByFilter(Boolean selectable) {
        if (selectable != null) {
            return findActorType.where().eq("deleted", false).eq("selectable", selectable).findList();
        } else {
            return getActorTypeAsList();
        }
    }

    /**
     * Get an competency by id.
     * 
     * @param id
     *            the competency id
     */
    public static Competency getCompetencyById(Long id) {
        return findCompetency.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get all competencies.
     */
    public static List<Competency> getCompetencyAsList() {
        return findCompetency.where().eq("deleted", false).findList();
    }

    /**
     * Get active competencies.
     */
    public static List<Competency> getCompetencyActiveAsList() {
        return findCompetency.where().eq("deleted", false).eq("isActive", true).findList();
    }

    /**
     * Get active competencies as a value holder collection.
     */
    public static ISelectableValueHolderCollection<Long> getCompetencyActiveAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getCompetencyActiveAsList());
    }

    /**
     * Get the competencies list with filters.
     * 
     * @param isActive
     *            true to return only active type, false only non-active, null
     *            all.
     * @param actorId
     *            if not null then return only competencies for the given actor.
     */
    public static List<Competency> getCompetencyAsListByFilter(Boolean isActive, Long actorId) {

        ExpressionList<Competency> e = findCompetency.where().eq("deleted", false);

        if (isActive != null) {
            e = e.eq("isActive", isActive);
        }
        if (actorId != null) {
            e = e.eq("actors.id", actorId);
        }

        return e.findList();
    }

}
