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
package dao.finance;

import java.util.List;

import models.finance.Currency;
import com.avaje.ebean.Model.Finder;

import com.avaje.ebean.ExpressionList;

import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;

/**
 * DAO for the {@link Currency} object.
 * 
 * @author Pierre-Yves Cloux
 */
public abstract class CurrencyDAO {

    /**
     * Default finder for the entity class.
     */
    public static Finder<Long, Currency> find = new Finder<>(Currency.class);

    /**
     * Default constructor.
     */
    public CurrencyDAO() {
    }

    /**
     * Get a currency by id.
     * 
     * @param id
     *            the currency id
     */
    public static Currency getCurrencyById(Long id) {
        return CurrencyDAO.find.where().eq("deleted", false).eq("id", id).findUnique();
    }

    /**
     * Get the default currency.
     */
    public static Currency getCurrencyDefault() {
        return CurrencyDAO.find.where().eq("deleted", false).eq("isDefault", true).findUnique();
    }

    /**
     * Get the default currency code.
     */
    public static String getCurrencyDefaultAsCode() {
        return getCurrencyDefault().code;
    }

    /**
     * Get all active currencies.
     */
    public static List<Currency> getCurrencyActiveAsList() {
        return CurrencyDAO.find.where().eq("deleted", false).eq("isActive", true).findList();
    }

    /**
     * Get all active currencies as value holder collection.
     */
    public static ISelectableValueHolderCollection<String> getCurrencySelectableAsVH() {
        return new DefaultSelectableValueHolderCollection<>(getCurrencyActiveAsList());
    }

    /**
     * Get the currencies list with filter.
     * 
     * @param isActive
     *            the flag isActive
     **/
    public static List<Currency> getCurrencyAsListByActive(Boolean isActive) {

        ExpressionList<Currency> e = CurrencyDAO.find.where().eq("deleted", false);

        if (isActive != null) {
            e = e.eq("isActive", isActive);
        }

        return e.findList();
    }

    /**
     * @param code
     *            the code of the currency
     * 
     * @return currency by code
     **/
    public static Currency getCurrencyByCode(String code) {
        return CurrencyDAO.find.where().eq("deleted", false).eq("code", code).findUnique();
    }

}
