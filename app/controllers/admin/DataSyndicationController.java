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
import dao.pmo.ActorDao;
import dao.pmo.PortfolioEntryDao;
import framework.utils.DefaultSelectableValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Msg;
import framework.utils.Table;
import framework.utils.Utilities;
import models.framework_models.account.NotificationCategory;
import models.framework_models.account.NotificationCategory.Code;
import models.pmo.PortfolioEntry;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import services.datasyndication.IDataSyndicationService;
import services.datasyndication.models.DataSyndicationAgreement;
import services.datasyndication.models.DataSyndicationAgreementItem;
import services.datasyndication.models.DataSyndicationAgreementLink;
import services.datasyndication.models.DataSyndicationPartner;
import utils.form.DataSyndicationAgreementLinkAcceptExistingPEFormData;
import utils.form.DataSyndicationAgreementLinkAcceptNewPEFormData;
import utils.form.DataSyndicationAgreementNoSlaveSubmitFormData;
import utils.form.DataSyndicationAgreementSubmitFormData;
import utils.table.DataSyndicationAgreementLinkListView;
import utils.table.DataSyndicationAgreementListView;
import utils.table.DataSyndicationPartnerListView;

/**
 * The constroller for the data syndication.
 * 
 * @author Johann Kohler
 */
@Restrict({ @Group(IMafConstants.PARTNER_SYNDICATION_PERMISSION) })
public class DataSyndicationController extends Controller {

    private static Form<SearchPartnerForm> searchPartnerFormTemplate = Form.form(SearchPartnerForm.class);
    private static Form<DataSyndicationAgreementSubmitFormData> agreementSubmitFormTemplate = Form.form(DataSyndicationAgreementSubmitFormData.class);
    private static Form<DataSyndicationAgreementLinkAcceptNewPEFormData> agreementLinkAcceptNewPEFormTemplate = Form
            .form(DataSyndicationAgreementLinkAcceptNewPEFormData.class);
    private static Form<DataSyndicationAgreementLinkAcceptExistingPEFormData> agreementLinkAcceptExistingPEFormTemplate = Form
            .form(DataSyndicationAgreementLinkAcceptExistingPEFormData.class);
    private static Form<DataSyndicationAgreementNoSlaveSubmitFormData> agreementNoSlaveSubmitFormTemplate = Form
            .form(DataSyndicationAgreementNoSlaveSubmitFormData.class);

    @Inject
    private IDataSyndicationService dataSyndicationService;

    /**
     * Display the list of master agreements.
     */
    public Result viewMasterAgreements() {

        if (dataSyndicationService.isActive()) {

            List<DataSyndicationAgreement> masterAgreements = null;
            try {
                masterAgreements = dataSyndicationService.getAgreementsAsMaster();
            } catch (Exception e) {
                Logger.error("DataSyndication viewMasterAgreements unexpected error", e);
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

        if (dataSyndicationService.isActive()) {

            List<DataSyndicationAgreement> slaveAgreements = null;
            try {
                slaveAgreements = dataSyndicationService.getAgreementsAsSlave();
            } catch (Exception e) {
                Logger.error("DataSyndication viewConsumerAgreements unexpected error", e);
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
     * Form to search a partner.
     */
    public Result searchPartner() {
        if (dataSyndicationService.isActive()) {
            return ok(views.html.admin.datasyndication.search_partner.render(searchPartnerFormTemplate));
        } else {
            return forbidden(views.html.error.access_forbidden.render(""));
        }
    }

    /**
     * Process the form to search a partner.
     */
    public Result processSearchPartner() {

        if (dataSyndicationService.isActive()) {

            // bind the form
            Form<SearchPartnerForm> boundForm = searchPartnerFormTemplate.bindFromRequest();

            if (boundForm.hasErrors()) {
                return ok(views.html.admin.datasyndication.search_partner.render(boundForm));
            }

            SearchPartnerForm searchPartnerForm = boundForm.get();

            // clean the keywords
            String keywords = searchPartnerForm.keywords.replaceAll("\\*", "%").trim();

            // perform the search
            List<DataSyndicationPartner> partners = null;
            try {
                partners = dataSyndicationService.searchFromSlavePartners(keywords);
            } catch (Exception e) {
                Logger.error("DataSyndication processSearchPartner unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            // remove current domain
            if (partners != null) {
                for (DataSyndicationPartner partner : partners) {
                    if (partner.domain.equals(dataSyndicationService.getCurrentDomain())) {
                        partners.remove(partner);
                        break;
                    }
                }
            }

            if (partners == null || partners.size() == 0) {

                // get all possible items
                ISelectableValueHolderCollection<Long> itemsAsVH = new DefaultSelectableValueHolderCollection<Long>();
                try {
                    for (DataSyndicationAgreementItem item : dataSyndicationService.getAgreementItems()) {
                        itemsAsVH.add(new DefaultSelectableValueHolder<Long>(item.id, item.getFullLabel()));
                    }
                } catch (Exception e) {
                    Logger.error("DataSyndication submitAgreement unexpected error", e);
                    return ok(views.html.admin.datasyndication.communication_error.render());
                }

                return ok(views.html.admin.datasyndication.search_partner_no_result.render(itemsAsVH, agreementNoSlaveSubmitFormTemplate));
            } else {

                List<DataSyndicationPartnerListView> dataSyndicationPartnerRows = new ArrayList<DataSyndicationPartnerListView>();
                for (DataSyndicationPartner partner : partners) {
                    dataSyndicationPartnerRows.add(new DataSyndicationPartnerListView(partner));
                }
                Table<DataSyndicationPartnerListView> dataSyndicationPartnerTable = DataSyndicationPartnerListView.templateTable
                        .fill(dataSyndicationPartnerRows);

                return ok(views.html.admin.datasyndication.search_partner_result.render(dataSyndicationPartnerTable));
            }

        } else {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

    }

    /**
     * Form to submit a new master agreement.
     * 
     * @param domain
     *            the domain of the slave instance
     */
    public Result submitAgreement(String domain) {

        if (dataSyndicationService.isActive()) {

            // get the partner
            DataSyndicationPartner partner = null;
            try {
                partner = dataSyndicationService.getPartner(domain);
            } catch (Exception e) {
                Logger.error("DataSyndication submitAgreement unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            // get all possible items
            ISelectableValueHolderCollection<Long> itemsAsVH = new DefaultSelectableValueHolderCollection<Long>();
            try {
                for (DataSyndicationAgreementItem item : dataSyndicationService.getAgreementItems()) {
                    itemsAsVH.add(new DefaultSelectableValueHolder<Long>(item.id, item.getFullLabel()));
                }
            } catch (Exception e) {
                Logger.error("DataSyndication submitAgreement unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            // initialize the form
            Form<DataSyndicationAgreementSubmitFormData> form = agreementSubmitFormTemplate.fill(new DataSyndicationAgreementSubmitFormData(domain));

            return ok(views.html.admin.datasyndication.submit_agreement.render(partner, itemsAsVH, form));
        } else {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

    }

    /**
     * Process the form to submit a new master agreement.
     */
    public Result processSubmitAgreement() {

        if (dataSyndicationService.isActive()) {

            // bind the form
            Form<DataSyndicationAgreementSubmitFormData> boundForm = agreementSubmitFormTemplate.bindFromRequest();

            // get the slave domain
            String slaveDomain = boundForm.data().get("slaveDomain");

            // get the partner
            DataSyndicationPartner partner = null;
            try {
                partner = dataSyndicationService.getPartner(slaveDomain);
            } catch (Exception e) {
                Logger.error("DataSyndication processSubmitAgreement unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            if (boundForm.hasErrors()) {

                // get all possible items
                ISelectableValueHolderCollection<Long> itemsAsVH = new DefaultSelectableValueHolderCollection<Long>();
                try {
                    for (DataSyndicationAgreementItem item : dataSyndicationService.getAgreementItems()) {
                        itemsAsVH.add(new DefaultSelectableValueHolder<Long>(item.id, item.getFullLabel()));
                    }
                } catch (Exception e) {
                    Logger.error("DataSyndication processSubmitAgreement unexpected error", e);
                    return ok(views.html.admin.datasyndication.communication_error.render());
                }

                return ok(views.html.admin.datasyndication.submit_agreement.render(partner, itemsAsVH, boundForm));
            }

            DataSyndicationAgreementSubmitFormData formData = boundForm.get();

            try {
                dataSyndicationService.submitAgreement(formData.refId, formData.name, formData.getStartDateAsDate(), formData.getEndDateAsDate(),
                        formData.itemIds, slaveDomain, partner.baseUrl);
            } catch (Exception e) {
                Logger.error("DataSyndication processSubmitAgreement unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            Utilities.sendSuccessFlashMessage(Msg.get("admin.data_syndication.submit_agreement.success"));

            return redirect(controllers.admin.routes.DataSyndicationController.viewMasterAgreements());

        } else {
            return forbidden(views.html.error.access_forbidden.render(""));
        }
    }

    /**
     * Process the form to submit a new master agreement without slave.
     */
    public Result processSubmitAgreementNoSlave() {

        if (dataSyndicationService.isActive()) {

            // bind the form
            Form<DataSyndicationAgreementNoSlaveSubmitFormData> boundForm = agreementNoSlaveSubmitFormTemplate.bindFromRequest();

            if (boundForm.hasErrors()) {

                // get all possible items
                ISelectableValueHolderCollection<Long> itemsAsVH = new DefaultSelectableValueHolderCollection<Long>();
                try {
                    for (DataSyndicationAgreementItem item : dataSyndicationService.getAgreementItems()) {
                        itemsAsVH.add(new DefaultSelectableValueHolder<Long>(item.id, item.getFullLabel()));
                    }
                } catch (Exception e) {
                    Logger.error("DataSyndication processSubmitAgreementNoSlave unexpected error", e);
                    return ok(views.html.admin.datasyndication.communication_error.render());
                }

                return ok(views.html.admin.datasyndication.search_partner_no_result.render(itemsAsVH, boundForm));
            }

            DataSyndicationAgreementNoSlaveSubmitFormData formData = boundForm.get();

            try {
                dataSyndicationService.submitAgreementNoSlave(formData.refId, formData.name, formData.getStartDateAsDate(), formData.getEndDateAsDate(),
                        formData.itemIds, formData.partnerEmail);
            } catch (Exception e) {
                Logger.error("DataSyndication processSubmitAgreementNoSlave unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            Utilities.sendSuccessFlashMessage(Msg.get("admin.data_syndication.submit_agreement.no_slave.success"));

            return redirect(controllers.admin.routes.DataSyndicationController.viewMasterAgreements());

        } else {
            return forbidden(views.html.error.access_forbidden.render(""));
        }
    }

    /**
     * Display the details of an agreement.
     * 
     * @param agreementId
     *            the agreement id
     * @param viewAllLinks
     *            if true then display all links of the agreement, else display
     *            only PENDING, SUSPENDED, FINISHED and ONGOING links
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
                Logger.error("DataSyndication viewAgreement unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            // define if the instance is the master or slave of the agreement
            Boolean isMasterAgreement = agreement.masterPartner.domain.equals(dataSyndicationService.getCurrentDomain());

            // get the links
            List<DataSyndicationAgreementLink> links = null;
            try {
                links = dataSyndicationService.getLinksOfAgreement(agreementId);
            } catch (Exception e) {
                Logger.error("DataSyndication viewAgreement unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            // construct the links table
            Set<String> columnsToHide = new HashSet<>();
            if (isMasterAgreement) {
                columnsToHide.add("processActionLink");
            }
            List<DataSyndicationAgreementLinkListView> linkRows = new ArrayList<DataSyndicationAgreementLinkListView>();
            for (DataSyndicationAgreementLink link : links) {
                if (viewAllLinks || link.getStatus().equals(DataSyndicationAgreement.Status.PENDING)
                        || link.getStatus().equals(DataSyndicationAgreement.Status.ONGOING)
                        || link.getStatus().equals(DataSyndicationAgreement.Status.SUSPENDED)
                        || link.getStatus().equals(DataSyndicationAgreement.Status.FINISHED)) {
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

        if (dataSyndicationService.isActive()) {

            // get the agreement
            DataSyndicationAgreement agreement = null;
            try {
                agreement = dataSyndicationService.getAgreement(agreementId);
                if (agreement == null) {
                    return notFound(views.html.error.not_found.render(""));
                }
            } catch (Exception e) {
                Logger.error("DataSyndication processAgreement unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            // check the instance is the slave
            if (agreement.slavePartner == null || !agreement.slavePartner.domain.equals(dataSyndicationService.getCurrentDomain())) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            // check the agreement is pending
            if (!agreement.status.equals(DataSyndicationAgreement.Status.PENDING)) {
                Utilities.sendInfoFlashMessage(Msg.get("admin.data_syndication.process_agreement.already"));
                return redirect(controllers.admin.routes.DataSyndicationController.viewConsumerAgreements());
            }

            return ok(views.html.admin.datasyndication.process_agreement.render(agreement));

        } else {
            return forbidden(views.html.error.access_forbidden.render(""));
        }
    }

    /**
     * Accept an agreement.
     * 
     * @param agreementId
     *            the agreement id
     */
    public Result acceptAgreement(Long agreementId) {

        if (dataSyndicationService.isActive()) {

            // get the agreement
            DataSyndicationAgreement agreement = null;
            try {
                agreement = dataSyndicationService.getAgreement(agreementId);
                if (agreement == null) {
                    return notFound(views.html.error.not_found.render(""));
                }
            } catch (Exception e) {
                Logger.error("DataSyndication acceptAgreement unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            // check the instance is the slave
            if (agreement.slavePartner == null || !agreement.slavePartner.domain.equals(dataSyndicationService.getCurrentDomain())) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            // check the agreement is pending
            if (!agreement.status.equals(DataSyndicationAgreement.Status.PENDING)) {
                Utilities.sendInfoFlashMessage(Msg.get("admin.data_syndication.process_agreement.already"));
                return redirect(controllers.admin.routes.DataSyndicationController.viewConsumerAgreements());
            }

            // accept the agreement
            try {
                dataSyndicationService.acceptAgreement(agreement);
            } catch (Exception e) {
                Logger.error("DataSyndication acceptAgreement unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            Utilities.sendSuccessFlashMessage(Msg.get("admin.data_syndication.process_agreement.accept.success"));

            return redirect(controllers.admin.routes.DataSyndicationController.viewConsumerAgreements());

        } else {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

    }

    /**
     * Reject an agreement.
     * 
     * @param agreementId
     *            the agreement id
     */
    public Result rejectAgreement(Long agreementId) {

        if (dataSyndicationService.isActive()) {

            // get the agreement
            DataSyndicationAgreement agreement = null;
            try {
                agreement = dataSyndicationService.getAgreement(agreementId);
                if (agreement == null) {
                    return notFound(views.html.error.not_found.render(""));
                }
            } catch (Exception e) {
                Logger.error("DataSyndication rejectAgreement unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            // check the instance is the slave
            if (agreement.slavePartner == null || !agreement.slavePartner.domain.equals(dataSyndicationService.getCurrentDomain())) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            // check the agreement is pending
            if (!agreement.status.equals(DataSyndicationAgreement.Status.PENDING)) {
                Utilities.sendInfoFlashMessage(Msg.get("admin.data_syndication.process_agreement.already"));
                return redirect(controllers.admin.routes.DataSyndicationController.viewConsumerAgreements());
            }

            // reject the agreement
            try {
                dataSyndicationService.rejectAgreement(agreement);
            } catch (Exception e) {
                Logger.error("DataSyndication rejectAgreement unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            Utilities.sendSuccessFlashMessage(Msg.get("admin.data_syndication.process_agreement.reject.success"));

            return redirect(controllers.admin.routes.DataSyndicationController.viewConsumerAgreements());

        } else {
            return forbidden(views.html.error.access_forbidden.render(""));
        }
    }

    /**
     * Suspend an agreement.
     * 
     * @param agreementId
     *            the agreement id
     */
    public Result suspendAgreement(Long agreementId) {

        if (dataSyndicationService.isActive()) {

            // get the agreement
            DataSyndicationAgreement agreement = null;
            try {
                agreement = dataSyndicationService.getAgreement(agreementId);
                if (agreement == null) {
                    return notFound(views.html.error.not_found.render(""));
                }
            } catch (Exception e) {
                Logger.error("DataSyndication suspendAgreement unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            // check the agreement is ongoing and the instance is the master
            if (!agreement.status.equals(DataSyndicationAgreement.Status.ONGOING)
                    || !agreement.masterPartner.domain.equals(dataSyndicationService.getCurrentDomain())) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            // suspend the agreement
            try {
                dataSyndicationService.suspendAgreement(agreement);
            } catch (Exception e) {
                Logger.error("DataSyndication suspendAgreement unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            Utilities.sendSuccessFlashMessage(Msg.get("admin.data_syndication.suspend_agreement.success"));

            return redirect(controllers.admin.routes.DataSyndicationController.viewMasterAgreements());

        } else {
            return forbidden(views.html.error.access_forbidden.render(""));
        }
    }

    /**
     * Restart an agreement.
     * 
     * @param agreementId
     *            the agreement id
     */
    public Result restartAgreement(Long agreementId) {

        if (dataSyndicationService.isActive()) {

            // get the agreement
            DataSyndicationAgreement agreement = null;
            try {
                agreement = dataSyndicationService.getAgreement(agreementId);
                if (agreement == null) {
                    return notFound(views.html.error.not_found.render(""));
                }
            } catch (Exception e) {
                Logger.error("DataSyndication restartAgreement unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            // check the agreement is suspended and the instance is the master
            if (!agreement.status.equals(DataSyndicationAgreement.Status.SUSPENDED)
                    || !agreement.masterPartner.domain.equals(dataSyndicationService.getCurrentDomain())) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            // restart the agreement
            try {
                dataSyndicationService.restartAgreement(agreement);
            } catch (Exception e) {
                Logger.error("DataSyndication restartAgreement unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            Utilities.sendSuccessFlashMessage(Msg.get("admin.data_syndication.restart_agreement.success"));

            return redirect(controllers.admin.routes.DataSyndicationController.viewMasterAgreements());

        } else {
            return forbidden(views.html.error.access_forbidden.render(""));
        }
    }

    /**
     * Cancel an agreement.
     * 
     * @param agreementId
     *            the agreement id
     */
    public Result cancelAgreement(Long agreementId) {

        if (dataSyndicationService.isActive()) {

            // get the agreement
            DataSyndicationAgreement agreement = null;
            try {
                agreement = dataSyndicationService.getAgreement(agreementId);
                if (agreement == null) {
                    return notFound(views.html.error.not_found.render(""));
                }
            } catch (Exception e) {
                Logger.error("DataSyndication cancelAgreement unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            // check the agreement is "ongoing or pending or suspended" and the
            // instance is the master or the slave
            if ((!agreement.status.equals(DataSyndicationAgreement.Status.ONGOING) && !agreement.status.equals(DataSyndicationAgreement.Status.PENDING)
                    && !agreement.status.equals(DataSyndicationAgreement.Status.PENDING_INSTANCE)
                    && !agreement.status.equals(DataSyndicationAgreement.Status.SUSPENDED))
                    || (!agreement.masterPartner.domain.equals(dataSyndicationService.getCurrentDomain())
                            && (agreement.slavePartner == null || !agreement.slavePartner.domain.equals(dataSyndicationService.getCurrentDomain())))) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            // cancel the agreement
            try {
                dataSyndicationService.cancelAgreement(agreement);
            } catch (Exception e) {
                Logger.error("DataSyndication cancelAgreement unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            Utilities.sendSuccessFlashMessage(Msg.get("admin.data_syndication.cancel_agreement.success"));

            if (agreement.masterPartner.domain.equals(dataSyndicationService.getCurrentDomain())) {
                return redirect(controllers.admin.routes.DataSyndicationController.viewMasterAgreements());
            } else {
                return redirect(controllers.admin.routes.DataSyndicationController.viewConsumerAgreements());
            }

        } else {
            return forbidden(views.html.error.access_forbidden.render(""));
        }
    }

    /**
     * Page to accept or reject an agreement link.
     * 
     * @param agreementLinkId
     *            the agreement link id
     */
    public Result processAgreementLink(Long agreementLinkId) {

        if (dataSyndicationService.isActive()) {

            // get the agreement link
            DataSyndicationAgreementLink agreementLink = null;
            try {
                agreementLink = dataSyndicationService.getAgreementLink(agreementLinkId);
                if (agreementLink == null) {
                    return notFound(views.html.error.not_found.render(""));
                }
            } catch (Exception e) {
                Logger.error("DataSyndication processAgreementLink unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            // check the instance is the slave
            if (!agreementLink.agreement.slavePartner.domain.equals(dataSyndicationService.getCurrentDomain())) {
                return forbidden(views.html.error.access_forbidden.render(""));
            }

            // check the agreement is pending
            if (!agreementLink.getStatus().equals(DataSyndicationAgreement.Status.PENDING)) {
                Utilities.sendInfoFlashMessage(Msg.get("admin.data_syndication.process_agreement_link.already"));
                return redirect(controllers.admin.routes.DataSyndicationController.viewAgreement(agreementLink.agreement.id, false));
            }

            // initialize the new PE form
            Form<DataSyndicationAgreementLinkAcceptNewPEFormData> agreementLinkAcceptNewPEForm = agreementLinkAcceptNewPEFormTemplate
                    .fill(new DataSyndicationAgreementLinkAcceptNewPEFormData(agreementLink.name));

            return ok(views.html.admin.datasyndication.process_agreement_link.render(agreementLink, agreementLinkAcceptNewPEForm,
                    agreementLinkAcceptExistingPEFormTemplate));

        } else {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

    }

    /**
     * Process the form to accept a PE agreement link with creating a new PE.
     */
    public Result acceptAgreementLinkNewPE() {

        if (dataSyndicationService.isActive()) {

            // bind the form
            Form<DataSyndicationAgreementLinkAcceptNewPEFormData> boundForm = agreementLinkAcceptNewPEFormTemplate.bindFromRequest();

            // get the agreement link id
            Long agreementLinkId = Long.valueOf(boundForm.data().get("agreementLinkId"));

            // get the agreement link
            DataSyndicationAgreementLink agreementLink = null;
            try {
                agreementLink = dataSyndicationService.getAgreementLink(agreementLinkId);
                if (agreementLink == null) {
                    return notFound(views.html.error.not_found.render(""));
                }
            } catch (Exception e) {
                Logger.error("DataSyndication acceptAgreementLinkNewPE unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            if (boundForm.hasErrors()) {
                return ok(
                        views.html.admin.datasyndication.process_agreement_link.render(agreementLink, boundForm, agreementLinkAcceptExistingPEFormTemplate));
            }

            DataSyndicationAgreementLinkAcceptNewPEFormData formData = boundForm.get();

            PortfolioEntry portfolioEntry = formData.createPorfolioEntry(agreementLink);

            // accept the agreement link
            try {
                dataSyndicationService.acceptAgreementLink(agreementLink, portfolioEntry.id);
            } catch (Exception e) {
                Logger.error("DataSyndication acceptAgreementLinkNewPE unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            // send a notification to the initiative manager
            ActorDao.sendNotification(portfolioEntry.manager, NotificationCategory.getByCode(Code.PORTFOLIO_ENTRY),
                    controllers.core.routes.PortfolioEntryController.view(portfolioEntry.id, 0).url(),
                    "admin.data_syndication.process_agreement_link.pe.accept.new.notification.title",
                    "admin.data_syndication.process_agreement_link.pe.accept.new.notification.message", agreementLink.name);

            Utilities.sendSuccessFlashMessage(Msg.get("admin.data_syndication.process_agreement_link.pe.accept.new.success"));

            return redirect(controllers.admin.routes.DataSyndicationController.viewAgreement(agreementLink.agreement.id, false));

        } else {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

    }

    /**
     * Process the form to accept a PE agreement link with an existing PE.
     */
    public Result acceptAgreementLinkExistingPE() {

        if (dataSyndicationService.isActive()) {

            // bind the form
            Form<DataSyndicationAgreementLinkAcceptExistingPEFormData> boundForm = agreementLinkAcceptExistingPEFormTemplate.bindFromRequest();

            // get the agreement link id
            Long agreementLinkId = Long.valueOf(boundForm.data().get("agreementLinkId"));

            // get the agreement link
            DataSyndicationAgreementLink agreementLink = null;
            try {
                agreementLink = dataSyndicationService.getAgreementLink(agreementLinkId);
                if (agreementLink == null) {
                    return notFound(views.html.error.not_found.render(""));
                }
            } catch (Exception e) {
                Logger.error("DataSyndication acceptAgreementLinkExistingPE unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            if (boundForm.hasErrors()) {
                return ok(views.html.admin.datasyndication.process_agreement_link.render(agreementLink, agreementLinkAcceptNewPEFormTemplate, boundForm));
            }

            DataSyndicationAgreementLinkAcceptExistingPEFormData formData = boundForm.get();

            PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(formData.portfolioEntryId);
            portfolioEntry.isSyndicated = true;
            portfolioEntry.save();

            // accept the agreement link
            try {
                dataSyndicationService.acceptAgreementLink(agreementLink, portfolioEntry.id);
            } catch (Exception e) {
                Logger.error("DataSyndication acceptAgreementLinkExistingPE unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            // send a notification to the initiative manager
            ActorDao.sendNotification(portfolioEntry.manager, NotificationCategory.getByCode(Code.PORTFOLIO_ENTRY),
                    controllers.core.routes.PortfolioEntryController.view(portfolioEntry.id, 0).url(),
                    "admin.data_syndication.process_agreement_link.pe.accept.existing.notification.title",
                    "admin.data_syndication.process_agreement_link.pe.accept.existing.notification.message", portfolioEntry.getName(), agreementLink.name);

            Utilities.sendSuccessFlashMessage(Msg.get("admin.data_syndication.process_agreement_link.pe.accept.existing.success"));

            return redirect(controllers.admin.routes.DataSyndicationController.viewAgreement(agreementLink.agreement.id, false));

        } else {
            return forbidden(views.html.error.access_forbidden.render(""));
        }
    }

    /**
     * Reject an agreement link.
     * 
     * @param agreementLinkId
     *            the agreement link id
     */
    public Result rejectAgreementLink(Long agreementLinkId) {

        if (dataSyndicationService.isActive()) {

            // get the agreement link
            DataSyndicationAgreementLink agreementLink = null;
            try {
                agreementLink = dataSyndicationService.getAgreementLink(agreementLinkId);
                if (agreementLink == null) {
                    return notFound(views.html.error.not_found.render(""));
                }
            } catch (Exception e) {
                Logger.error("DataSyndication rejectAgreementLink unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            // reject the agreement
            try {
                dataSyndicationService.rejectAgreementLink(agreementLink);
            } catch (Exception e) {
                Logger.error("DataSyndication rejectAgreementLink unexpected error", e);
                return ok(views.html.admin.datasyndication.communication_error.render());
            }

            Utilities.sendSuccessFlashMessage(Msg.get("admin.data_syndication.process_agreement_link.reject.success"));

            return redirect(controllers.admin.routes.DataSyndicationController.viewAgreement(agreementLink.agreement.id, false));

        } else {
            return forbidden(views.html.error.access_forbidden.render(""));
        }
    }

    /**
     * The form to search a partner.
     * 
     * @author Johann Kohler
     */
    public static class SearchPartnerForm {

        /**
         * Default constructor.
         */
        public SearchPartnerForm() {

        }

        @Required
        public String keywords;

    }
}
