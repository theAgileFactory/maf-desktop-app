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
package models.sql;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;

import play.Logger;
import play.cache.Cache;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.annotation.Sql;

import constants.IMafConstants;

/**
 * Entity that provide the subordinates of an actor (until the 7th levels).
 * 
 * @author Johann Kohler
 */
@Entity
@Sql
public class ActorHierarchy {

    private static final String ACTOR_HIERARCHY_CACHE_PREFIX = IMafConstants.MAF_CACHE_PREFIX + "actorhierarchy.";
    private static final Integer CACHE_TTL = 300;

    public Long level1;
    public Long level2;
    public Long level3;
    public Long level4;
    public Long level5;
    public Long level6;
    public Long level7;

    /**
     * Get the list of actor hierarchies for an actor.
     * 
     * @param actorId
     *            the actor id
     */
    private static List<ActorHierarchy> getSubordinatesAsActorHierarchy(Long actorId) {

        String sql = "SELECT t2.id, t3.id, t4.id, t5.id, t6.id, t7.id FROM actor AS t1 " + "LEFT JOIN actor AS t2 ON t2.manager_id = t1.id "
                + "LEFT JOIN actor AS t3 ON t3.manager_id = t2.id " + "LEFT JOIN actor AS t4 ON t4.manager_id = t3.id "
                + "LEFT JOIN actor AS t5 ON t5.manager_id = t4.id " + "LEFT JOIN actor AS t6 ON t6.manager_id = t5.id "
                + "LEFT JOIN actor AS t7 ON t7.manager_id = t6.id " + "WHERE t1.id = " + actorId;

        RawSql rawSql = RawSqlBuilder.parse(sql).columnMapping("t2.id", "level2").columnMapping("t3.id", "level3").columnMapping("t4.id", "level4")
                .columnMapping("t5.id", "level5").columnMapping("t6.id", "level6").columnMapping("t7.id", "level7").create();

        Query<ActorHierarchy> query = Ebean.find(ActorHierarchy.class).setRawSql(rawSql);

        return query.findList();
    }

    /**
     * Get the list of subordinates id of an actor.
     * 
     * note: this method uses cache
     * 
     * @param actorId
     *            the actor id
     */
    public static Set<Long> getSubordinatesAsId(Long actorId) {

        String cacheKey = ACTOR_HIERARCHY_CACHE_PREFIX + "subordinates.asid." + actorId;
        @SuppressWarnings("unchecked")
        Set<Long> subordinatesId = (Set<Long>) Cache.get(cacheKey);
        if (subordinatesId != null) {
            Logger.debug("actor hierarchy: '" + cacheKey + "' read from cache");
            return subordinatesId;
        }

        subordinatesId = new HashSet<>();
        for (ActorHierarchy actorHierarchy : getSubordinatesAsActorHierarchy(actorId)) {
            subordinatesId.add(actorHierarchy.level2);
            subordinatesId.add(actorHierarchy.level3);
            subordinatesId.add(actorHierarchy.level4);
            subordinatesId.add(actorHierarchy.level5);
            subordinatesId.add(actorHierarchy.level6);
            subordinatesId.add(actorHierarchy.level7);
        }

        subordinatesId.remove(null);

        // set the result in the cache
        Cache.set(cacheKey, subordinatesId, CACHE_TTL);

        return subordinatesId;
    }

    /**
     * Get the list of subordinates of an actor as a string.
     * 
     * @param actorId
     *            the actor id
     * @param separator
     *            the separator (data aggregate)
     */
    public static String getSubordinatesAsString(Long actorId, String separator) {

        String cacheKey = ACTOR_HIERARCHY_CACHE_PREFIX + "subordinates.asstring." + actorId + "." + separator;
        String subordinatesString = (String) Cache.get(cacheKey);
        if (subordinatesString != null) {
            Logger.debug("actor hierarchy: '" + cacheKey + "' read from cache");
            return subordinatesString;
        }

        Set<Long> subordinatesId = getSubordinatesAsId(actorId);

        StringBuilder builder = new StringBuilder();

        boolean firstLoop = true;

        for (Long id : subordinatesId) {
            if (!firstLoop) {
                builder.append(separator);
            }
            builder.append(id.toString());
            firstLoop = false;
        }

        subordinatesString = builder.toString();

        // set the result in the cache
        Cache.set(cacheKey, subordinatesString, CACHE_TTL);

        return subordinatesString;

    }
}
