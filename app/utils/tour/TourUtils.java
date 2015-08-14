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

import constants.IMafConstants;
import framework.security.SecurityUtils;
import framework.services.ServiceStaticAccessor;
import framework.services.account.IPreferenceManagerPlugin;
import framework.utils.Msg;
import play.twirl.api.Html;

/**
 * Provide the methods to manage the help tour.
 * 
 * @author Johann Kohler
 * 
 */
public class TourUtils {

    /**
     * The existing tour.
     * 
     * To create a new tour<br/>
     * - add its uid in the enum<br/>
     * - add a switch case with the needed steps
     * 
     * @author Johann Kohler
     * 
     */
    private enum TourUid {
        TOP_MENU_BAR_TOUR, BREADCRUMB_TOUR;
    }

    public static final String TOUR_ALL_PAGES = "tour.all_pages";
    public static final String TOUR_START_PAGE = "tour.start_page";

    /**
     * Get the correct tour for the given route.
     * 
     * @param route
     *            the current route
     * @param tourUidString
     *            if a specific tour is required then we give it's uid
     * @param tourStepString
     *            if a specific tour is required then we give the wished step
     */
    public static Html getTour(String route, String tourUidString, String tourStepString) {

        if (route.equals("")) {
            route = "/";
        }

        Integer step = 0;
        if (tourStepString != null && !tourStepString.equals("")) {
            step = Integer.valueOf(tourStepString);
        }

        TourUid uid = null;

        if (tourUidString != null && !tourUidString.equals("")) {

            // this case occurs when a tour has been started on another page and
            // forwarded to the current
            uid = TourUid.valueOf(tourUidString);

        } else {

            IPreferenceManagerPlugin preferenceService = ServiceStaticAccessor.getPreferenceManagerPlugin();
            boolean isAdmin = SecurityUtils.isAllowed(IMafConstants.ADMIN_CONFIGURATION_PERMISSION);

            for (TourUid tourUid : TourUid.values()) {
                if (preferenceService.getPreferenceValueAsBoolean(tourUid.name())) {

                    // we get the tour if both conditions are true:
                    // 1. the user is admin or the tour is not SYSTEM
                    // 2. the tour is for all pages or for the current
                    if ((isAdmin || !preferenceService.isPreferenceSystem(tourUid.name()))
                            && (preferenceService.getPropertyAsBoolean(tourUid.name(), TOUR_ALL_PAGES) || preferenceService.getPropertyAsString(
                                    tourUid.name(), TOUR_START_PAGE).equals(route))) {
                        uid = tourUid;
                        break;
                    }

                }
            }

        }

        if (uid != null) {

            Tour tour = new Tour(uid.name());

            switch (uid) {

            case TOP_MENU_BAR_TOUR:

                tour.addStep(new Step(Msg.get("tour.top_menu_bar.step0.title"), Msg.get("tour.top_menu_bar.step0.content"), null, null, null));
                tour.addStep(new Step(Msg.get("tour.top_menu_bar.step1.title"), Msg.get("tour.top_menu_bar.step1.content"), "#lang-selector", "left", null));
                tour.addStep(new Step(Msg.get("tour.top_menu_bar.step2.title"), Msg.get("tour.top_menu_bar.step2.content"), "#menu-my", "left", null));
                tour.addStep(new Step(Msg.get("tour.top_menu_bar.step3.title"), Msg.get("tour.top_menu_bar.step3.content"), "#topmenubar-item-0", "bottom",
                        null));
                tour.addStep(new Step(Msg.get("tour.top_menu_bar.step4.title"), Msg.get("tour.top_menu_bar.step4.content"), "#topmenubar-item-1", "bottom",
                        null));
                tour.addStep(new Step(Msg.get("tour.top_menu_bar.step5.title"), Msg.get("tour.top_menu_bar.step5.content"), "#topmenubar-item-2", "bottom",
                        null));
                tour.addStep(new Step(Msg.get("tour.top_menu_bar.step6.title"), Msg.get("tour.top_menu_bar.step6.content"), "#topmenubar-item-3", "bottom",
                        null));
                tour.addStep(new Step(Msg.get("tour.top_menu_bar.step7.title"), Msg.get("tour.top_menu_bar.step7.content"), "#topmenubar-item-4", "bottom",
                        null));
                tour.addStep(new Step(Msg.get("tour.top_menu_bar.step8.title"), Msg.get("tour.top_menu_bar.step8.content"), "#topmenubar-item-5", "bottom",
                        null));
                tour.addStep(new Step(Msg.get("tour.top_menu_bar.step9.title"), Msg.get("tour.top_menu_bar.step9.content"), "#topmenubar-item-6", "bottom",
                        null));
                tour.addStep(new Step(Msg.get("tour.top_menu_bar.step10.title"), Msg.get("tour.top_menu_bar.step10.content"), "#topmenubar-item-7", "bottom",
                        null));
                tour.addStep(new Step(Msg.get("tour.top_menu_bar.step11.title"), Msg.get("tour.top_menu_bar.step11.content"), "#menu-messages", "bottom",
                        null));
                tour.addStep(new Step(Msg.get("tour.top_menu_bar.step12.title"), Msg.get("tour.top_menu_bar.step12.content"), "#menu-notifications",
                        "bottom", null));
                tour.addStep(new Step(Msg.get("tour.top_menu_bar.step13.title"), Msg.get("tour.top_menu_bar.step13.content"), "#menu-my", "left", null));

                return views.html.tour.tour.render(tour, step);

            case BREADCRUMB_TOUR:

                tour.addStep(new Step(Msg.get("tour.breadcrumb.step0.title"), Msg.get("tour.breadcrumb.step0.content"), "#pinThisPage", "left", null));
                tour.addStep(new Step(Msg.get("tour.breadcrumb.step1.title"), Msg.get("tour.breadcrumb.step1.content"), "#helpTarget", "left", null));
                tour.renderSteps();

                return views.html.tour.tour.render(tour, step);
            }

        }

        return new Html("");

    }

    /**
     * Mark a tour as read.
     * 
     * @param tourUidString
     *            the tour uid
     */
    public static void markTourAsRead(String tourUidString) {
        IPreferenceManagerPlugin preferenceService = ServiceStaticAccessor.getPreferenceManagerPlugin();
        preferenceService.updatePreferenceValue(tourUidString, false);
    }
}
