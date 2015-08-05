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
package dao.architecture;

import java.util.ArrayList;
import java.util.List;

import com.avaje.ebean.ExpressionList;
import com.google.common.collect.Lists;

import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import models.architecture.ApplicationBlock;
import com.avaje.ebean.Model.Finder;

/**
 * DAO for the {@link ApplicationBlock} object.
 * 
 * @author Pierre-Yves Cloux
 */
public abstract class ArchitectureDao {

    public static Finder<Long, ApplicationBlock> findApplicationBlock = new Finder<>(Long.class, ApplicationBlock.class);

    /**
     * Default constructor.
     */
    public ArchitectureDao() {
    }

    /**
     * Get an application block by id.
     * 
     * @param id
     *            the application block id
     */
    public static ApplicationBlock getApplicationBlockById(Long id) {
        return findApplicationBlock.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get the list of ancestors of an application block.
     * 
     * The first element of the list is the root node, and the last is the
     * concerned node.
     * 
     * @param id
     *            the application block id
     */
    public static List<ApplicationBlock> getApplicationBlockAncestorsFromRoot(Long id) {
        ApplicationBlock leaf = getApplicationBlockById(id);
        List<ApplicationBlock> ancestors = new ArrayList<>();
        findApplicationBlockAncestors(ancestors, leaf);
        return Lists.reverse(ancestors);
    }

    /**
     * Find recursively the ancestors of an application block.
     * 
     * @param ancestors
     *            the list of ancestors (modified by the method)
     * @param applicationBlock
     *            the application block leaf
     */
    private static void findApplicationBlockAncestors(List<ApplicationBlock> ancestors, ApplicationBlock applicationBlock) {
        ancestors.add(applicationBlock);
        if (applicationBlock.getParent() != null) {
            findApplicationBlockAncestors(ancestors, applicationBlock.parent);
        }
    }

    /**
     * Get the application blocks as an expression list.
     */
    public static ExpressionList<ApplicationBlock> getApplicationBlockAsExpr() {
        return findApplicationBlock.where().eq("deleted", false);
    }

    /**
     * Get the active root application block (meaning without parent).
     */
    public static List<ApplicationBlock> getApplicationBlockActiveRootsAsList() {
        return findApplicationBlock.orderBy("order").where().eq("deleted", false).eq("archived", false).isNull("parent").findList();
    }

    /**
     * Get the active children of an application block.
     * 
     * @param parentId
     *            the application block id for which we need the children
     */
    public static List<ApplicationBlock> getApplicationBlockActiveAsListByParent(Long parentId) {
        return findApplicationBlock.orderBy("order").where().eq("deleted", false).eq("archived", false).eq("parent.id", parentId).findList();
    }

    /**
     * Search from all application blocks for which the criteria matches with
     * the ref id or the name.
     * 
     * @param key
     *            the search criteria (use % for wild cards)
     */
    public static List<ApplicationBlock> getApplicationBlockAsListByKeywords(String key) {
        return findApplicationBlock.where().eq("deleted", false).disjunction().ilike("refId", "%" + key + "%").ilike("name", "%" + key + "%").findList();
    }

    /**
     * Search from all application blocks with the search process defined by the
     * method "getApplicationBlockAsListByKeywords" and return a value holder
     * collection.
     * 
     * @param key
     *            the search criteria (use % for wild cards)
     */
    public static ISelectableValueHolderCollection<Long> getApplicationBlockAsVHByKeywords(String key) {
        return new DefaultSelectableValueHolderCollection<>(getApplicationBlockAsListByKeywords(key));
    }

}
