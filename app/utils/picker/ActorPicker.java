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

import dao.pmo.ActorDao;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.PickerHandler;
import framework.utils.PickerHandler.Parameters;

/**
 * The pickers for the actors.
 * 
 * @author Johann Kohler
 */
public class ActorPicker {

    public static PickerHandler<Long> pickerTemplate = new PickerHandler<Long>(Long.class, new PickerHandler.Handle<Long>() {

        @Override
        public Map<Parameters, String> config(Map<Parameters, String> defaultParameters) {
            defaultParameters.put(Parameters.SEARCH_ENABLED, "true");
            return defaultParameters;
        }

        @Override
        public ISelectableValueHolderCollection<Long> getInitialValueHolders(List<Long> values, Map<String, String> context) {
            return ActorDao.getActorActiveAsVH();
        }

        @Override
        public ISelectableValueHolderCollection<Long> getFoundValueHolders(String searchString, Map<String, String> context) {
            searchString = searchString.replaceAll("\\*", "%");
            return ActorDao.getActorActiveAsVHByKeywords(searchString);
        }

    });

    public static PickerHandler<Long> pickerWithoutUidTemplate = new PickerHandler<Long>(Long.class, new PickerHandler.Handle<Long>() {

        @Override
        public Map<Parameters, String> config(Map<Parameters, String> defaultParameters) {
            defaultParameters.put(Parameters.SEARCH_ENABLED, "true");
            return defaultParameters;
        }

        @Override
        public ISelectableValueHolderCollection<Long> getInitialValueHolders(List<Long> values, Map<String, String> context) {
            return ActorDao.getActorActiveWithoutUidAsVH();
        }

        @Override
        public ISelectableValueHolderCollection<Long> getFoundValueHolders(String searchString, Map<String, String> context) {
            searchString = searchString.replaceAll("\\*", "%");
            return ActorDao.getActorActiveWithoutUidAsVHByKeywords(searchString);
        }

    });

    public static PickerHandler<Long> pickerForOrgUnitTemplate = new PickerHandler<Long>(Long.class, new PickerHandler.Handle<Long>() {

        @Override
        public Map<Parameters, String> config(Map<Parameters, String> defaultParameters) {
            defaultParameters.put(Parameters.SEARCH_ENABLED, "false");
            return defaultParameters;
        }

        @Override
        public ISelectableValueHolderCollection<Long> getInitialValueHolders(List<Long> values, Map<String, String> context) {
            Long orgUnitId = Long.valueOf(context.get("orgUnitId"));
            return ActorDao.getActorActiveAsVHByOrgUnit(orgUnitId);
        }

    });

    public static PickerHandler<Long> pickerWithCompetencyTemplate = new PickerHandler<Long>(Long.class, new PickerHandler.Handle<Long>() {

        @Override
        public Map<Parameters, String> config(Map<Parameters, String> defaultParameters) {
            defaultParameters.put(Parameters.SEARCH_ENABLED, "false");
            return defaultParameters;
        }

        @Override
        public ISelectableValueHolderCollection<Long> getInitialValueHolders(List<Long> values, Map<String, String> context) {
            Long competencyId = Long.valueOf(context.get("competencyId"));
            return ActorDao.getActorActiveAsVHByCompetency(competencyId);
        }

    });

}
