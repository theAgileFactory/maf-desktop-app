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
package utils.picker;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import framework.services.ServiceManager;
import framework.services.account.AccountManagementException;
import framework.services.account.IAccountManagerPlugin;
import framework.services.account.IUserAccount;
import framework.utils.DefaultSelectableValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.PickerHandler;
import framework.utils.PickerHandler.Handle;
import framework.utils.PickerHandler.Parameters;

/**
 * The pickers for the principals.
 * 
 * @author Johann Kohler
 */
public class PrincipalPicker {

    public static PickerHandler<String> pickerTemplateUid = new PickerHandler<String>(String.class, new Handle<String>() {

        @Override
        public Map<Parameters, String> config(Map<Parameters, String> defaultParameters) {
            defaultParameters.put(Parameters.SEARCH_ENABLED, "true");
            return defaultParameters;
        }

        @Override
        public ISelectableValueHolderCollection<String> getFoundValueHolders(String searchString, Map<String, String> context) {
            return getSelectableValuesList(searchString);
        }

        @Override
        public ISelectableValueHolderCollection<String> getInitialValueHolders(List<String> values, Map<String, String> context) {
            return getSelectableValuesList(null);
        }
    });

    /**
     * Return a list of selectable values.
     * 
     * @param searchString
     *            a search string (selection of principal names)
     * @return a list of selectable values
     */
    private static ISelectableValueHolderCollection<String> getSelectableValuesList(String searchString) {
        if (searchString == null) {
            searchString = "*";
        }
        IAccountManagerPlugin accountManagerPlugin = ServiceManager.getService(IAccountManagerPlugin.NAME, IAccountManagerPlugin.class);
        ISelectableValueHolderCollection<String> selectableValues = new DefaultSelectableValueHolderCollection<String>();
        if (StringUtils.isNotBlank(searchString)) {
            if (!searchString.equals("*")) {
                searchString = "*" + searchString + "*";
            }
            try {
                List<IUserAccount> userAccounts = accountManagerPlugin.getUserAccountsFromName(searchString);
                if (userAccounts != null) {
                    for (IUserAccount userAccount : userAccounts) {
                        if (userAccount.isActive() && userAccount.isDisplayed()) {
                            selectableValues.add(new DefaultSelectableValueHolder<String>(userAccount.getUid(), String.format("%s %s",
                                    userAccount.getFirstName(), userAccount.getLastName())));
                        }
                    }
                }
            } catch (AccountManagementException e) {
                Logger.error("Unable to get a list of users using the specified searchString", e);
            }
        }
        return selectableValues;
    }

}
