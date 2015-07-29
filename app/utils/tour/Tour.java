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
package utils.tour;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

/**
 * Define a tour.
 * 
 * @author Johann Kohler
 * 
 */
public class Tour {

    private String uid;
    private List<Step> steps;

    /**
     * Construct a tour.
     * 
     * @param uid
     *            the tour uid, should correspond to the preference value.
     */
    public Tour(String uid) {
        this.uid = uid;
        this.steps = new ArrayList<>();
    }

    /**
     * Add a step.
     * 
     * @param step
     *            the step to add
     */
    public void addStep(Step step) {
        this.steps.add(step);
    }

    /**
     * Get the uid.
     */
    public String getUid() {
        return this.uid;
    }

    /**
     * Get the steps as a json array.
     */
    public String renderSteps() {

        JSONArray json = new JSONArray();

        Integer index = 0;
        for (Step step : steps) {
            Step previous = index != 0 ? steps.get(index - 1) : null;
            Step next = index != steps.size() - 1 ? steps.get(index + 1) : null;
            json.put(step.render(this.uid, index, previous, next));
            index++;
        }

        // remove the quotes for anonymous functions
        String r = json.toString();
        r = r.replaceAll("\"onNext\":\"(.*?)\"", "\"onNext\":$1");
        r = r.replaceAll("\"onPrev\":\"(.*?)\"", "\"onPrev\":$1");

        return r;

    }

    /**
     * Get the number of steps.
     */
    public int getNumberOfSteps() {
        return this.steps.size();
    }
}
