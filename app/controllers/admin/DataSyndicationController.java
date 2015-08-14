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
package controllers.admin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import constants.IMafConstants;
import framework.utils.Table;
import play.mvc.Controller;
import play.mvc.Result;
import services.datasyndication.IDataSyndicationService;
import services.datasyndication.models.DataSyndicationAgreement;
import services.datasyndication.models.DataSyndicationAgreementLink;
import utils.table.DataSyndicationAgreementLinkListView;
import utils.table.DataSyndicationAgreementListView;

/**
 * The constroller for the data syndication.
 * 
 * @author Johann Kohler
 */
@Restrict({ @Group(IMafConstants.PARTNER_SYNDICATION_PERMISSION) })
public class DataSyndicationController extends Controller {

    @Inject
    private IDataSyndicationService dataSyndicationService;

    /**
     * Display the list of master agreements.
     */
    public Result viewMasterAgreements() {

        // TODO(jkohler) add sort and filter capabilities

        if (dataSyndicationService.isActive()) {

            List<DataSyndicationAgreement> masterAgreements = null;
            try {
                masterAgreements = dataSyndicationService.getMasterAgreements();
            } catch (Exception e) {
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            Set<String> columnsToHide = new HashSet<>();
            columnsToHide.add("masterCustomerName");
            columnsToHide.add("masterContactName");
            columnsToHide.add("masterContactEmail");
            columnsToHide.add("processActionLink");

            List<DataSyndicationAgreementListView> dataSyndicationAgreementRows = new ArrayList<DataSyndicationAgreementListView>();
            for (DataSyndicationAgreement masterAgreement : masterAgreements) {
                dataSyndicationAgreementRows.add(new DataSyndicationAgreementListView(masterAgreement));
            }
            Table<DataSyndicationAgreementListView> dataSyndicationAgreementTable = DataSyndicationAgreementListView.templateTable
                    .fill(dataSyndicationAgreementRows, columnsToHide);

            return ok(views.html.admin.datasyndication.master_agreements.render(dataSyndicationAgreementTable));

        } else {
            return forbidden(views.html.error.access_forbidden.render(""));
        }
    }

    /**
     * Display the list of consumer agreements.
     */
    public Result viewConsumerAgreements() {

        // TODO(jkohler) add sort and filter capabilities

        if (dataSyndicationService.isActive()) {

            List<DataSyndicationAgreement> slaveAgreements = null;
            try {
                slaveAgreements = dataSyndicationService.getSlaveAgreements();
            } catch (Exception e) {
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            Set<String> columnsToHide = new HashSet<>();
            columnsToHide.add("slaveCustomerName");
            columnsToHide.add("slaveContactName");
            columnsToHide.add("slaveContactEmail");
            columnsToHide.add("stateActionLink");

            List<DataSyndicationAgreementListView> dataSyndicationAgreementRows = new ArrayList<DataSyndicationAgreementListView>();
            for (DataSyndicationAgreement slaveAgreement : slaveAgreements) {
                dataSyndicationAgreementRows.add(new DataSyndicationAgreementListView(slaveAgreement));
            }
            Table<DataSyndicationAgreementListView> dataSyndicationAgreementTable = DataSyndicationAgreementListView.templateTable
                    .fill(dataSyndicationAgreementRows, columnsToHide);

            return ok(views.html.admin.datasyndication.consumer_agreements.render(dataSyndicationAgreementTable));

        } else {
            return forbidden(views.html.error.access_forbidden.render(""));
        }
    }

    /**
     * Form to submit a new master agreement.
     */
    public Result submitAgreement() {
        return TODO;
    }

    /**
     * Process the form to submit a new master agreement.
     */
    public Result processSubmitAgreement() {
        return TODO;
    }

    /**
     * Display the details of an agreement.
     * 
     * @param agreementId
     *            the agreement id
     * @param viewAllLinks
     *            if true then display all links of the agreement, else display
     *            only PENDING and ONGOING links
     */
    public Result viewAgreement(Long agreementId, Boolean viewAllLinks) {

        if (dataSyndicationService.isActive()) {

            // get the agreement
            DataSyndicationAgreement agreement = null;
            try {
                agreement = dataSyndicationService.getAgreement(agreementId);
                if (agreement == null) {
                    return notFound(views.html.error.not_found.render(""));
                }
            } catch (Exception e) {
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            // define if the instance is the master or slave of the agreement
            Boolean isMasterAgreement = agreement.masterPartner.domain.equals(dataSyndicationService.getCurrentDomain());

            // get the links
            List<DataSyndicationAgreementLink> links = null;
            try {
                links = dataSyndicationService.getLinksOfAgreement(agreementId);
            } catch (Exception e) {
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            // construct the links table
            Set<String> columnsToHide = new HashSet<>();
            if (!isMasterAgreement) {
                columnsToHide.add("processActionLink");
            }
            List<DataSyndicationAgreementLinkListView> linkRows = new ArrayList<DataSyndicationAgreementLinkListView>();
            for (DataSyndicationAgreementLink link : links) {
                if (viewAllLinks || link.status.equals(DataSyndicationAgreementLink.Status.PENDING)
                        || link.status.equals(DataSyndicationAgreementLink.Status.ONGOING)) {
                    linkRows.add(new DataSyndicationAgreementLinkListView(link, dataSyndicationService.getCurrentDomain()));
                }
            }
            Table<DataSyndicationAgreementLinkListView> linksTable = DataSyndicationAgreementLinkListView.templateTable.fill(linkRows, columnsToHide);

            return ok(views.html.admin.datasyndication.view_agreement.render(isMasterAgreement, agreement, linksTable, viewAllLinks));

        } else {
            return forbidden(views.html.error.access_forbidden.render(""));
        }
    }

    /**
     * Page to accept or reject an agreement.
     * 
     * @param agreementId
     *            the agreement id
     */
    public Result processAgreement(Long agreementId) {

        // TODO check here the right

        return TODO;
    }

    /**
     * Accept an agreement.
     * 
     * @param agreementId
     *            the agreement id
     */
    public Result acceptAgreement(Long agreementId) {

        // TODO check here the right

        return TODO;
    }

    /**
     * Reject an agreement.
     * 
     * @param agreementId
     *            the agreement id
     */
    public Result rejectAgreement(Long agreementId) {

        // TODO check here the right

        return TODO;
    }

    /**
     * Suspend an agreement.
     * 
     * @param agreementId
     *            the agreement id
     */
    public Result suspendAgreement(Long agreementId) {

        // TODO check here the right

        return TODO;
    }

    /**
     * Restart an agreement.
     * 
     * @param agreementId
     *            the agreement id
     */
    public Result restartAgreement(Long agreementId) {

        // TODO check here the right

        return TODO;
    }

    /**
     * Cancel an agreement.
     * 
     * @param agreementId
     *            the agreement id
     */
    public Result cancelAgreement(Long agreementId) {

        // TODO check here the right

        return TODO;
    }

    /**
     * Page to accept or reject an agreement link.
     * 
     * @param agreementLinkId
     *            the agreement link id
     */
    public Result processAgreementLink(Long agreementLinkId) {

        // TODO check here the right

        return TODO;
    }

    /**
     * Accept an agreement link.
     * 
     * @param agreementLinkId
     *            the agreement link id
     */
    public Result acceptAgreementLink(Long agreementLinkId) {

        // TODO check here the right

        return TODO;
    }

    /**
     * Reject an agreement link.
     * 
     * @param agreementLinkId
     *            the agreement link id
     */
    public Result rejectAgreementLink(Long agreementLinkId) {

        // TODO check here the right

        return TODO;
    }
}
