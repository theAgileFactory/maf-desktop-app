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

import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;

/**
 * The step of a tour.
 * 
 * @author Johann Kohler
 * 
 */
public class Step {

    private String title;
    private String content;
    private String element;
    private String placement;
    private String route;

    /**
     * Construct a step.
     * 
     * @param title
     *            the step title (mandatory)
     * @param content
     *            the step content (mandatory)
     * @param element
     *            the element selector, for example #my-id (if null the step is
     *            centered and without arrow)
     * @param placement
     *            the step placement, could be: bottom, top, left, right
     *            (default is bottom)
     * @param route
     *            the step route (only and mandatory for non "allPages" tour)
     */
    public Step(String title, String content, String element, String placement, String route) {
        this.title = title;
        this.content = content;
        this.element = element;
        this.placement = placement != null ? placement : "bottom";
        this.route = route;
    }

    /**
     * Get the route.
     */
    public String getRoute() {
        return this.route;
    }

    /**
     * Get the step as a json object.
     * 
     * @param uid
     *            the tour uid
     * @param index
     *            the index of the step
     * @param previous
     *            the previous step
     * @param next
     *            the next step
     */
    public JSONObject render(String uid, Integer index, Step previous, Step next) {

        JSONObject json = new JSONObject();

        try {
            json.put("title", this.title);
            json.put("content", this.content);
            if (this.element != null) {
                json.put("element", this.element);
                json.put("placement", this.placement);
            } else {
                json.put("orphan", true);
            }
            if (this.route != null) {
                json.put("path", this.route);
            }
            if (previous != null && previous.getRoute() != null && this.route != null && !previous.getRoute().equals(this.route)) {
                json.put("onPrev", "function(){ document.location.href = '" + previous.getRoute() + "?tourStep=" + (index - 1) + "&tourUid=" + uid
                        + "'; return (new jQuery.Deferred()).promise(); }");
            }
            if (next != null && next.getRoute() != null && this.route != null && !next.getRoute().equals(this.route)) {
                json.put("onNext", "function(){ document.location.href = '" + next.getRoute() + "?tourStep=" + (index + 1) + "&tourUid=" + uid
                        + "'; return (new jQuery.Deferred()).promise(); }");
            }
            return json;
        } catch (JSONException e) {
            Logger.error("error when render a step tour", e);
        }

        return null;
    }
}
