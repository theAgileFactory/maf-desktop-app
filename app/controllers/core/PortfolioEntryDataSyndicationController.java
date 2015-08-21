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
package controllers.core;

import java.util.List;

import javax.inject.Inject;

import be.objectify.deadbolt.java.actions.Dynamic;
import dao.pmo.PortfolioEntryDao;
import framework.services.session.IUserSessionManagerPlugin;
import framework.utils.DefaultSelectableValueHolder;
import framework.utils.DefaultSelectableValueHolderCollection;
import framework.utils.ISelectableValueHolderCollection;
import framework.utils.Msg;
import framework.utils.Utilities;
import models.pmo.PortfolioEntry;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.CheckPortfolioEntryExists;
import security.DefaultDynamicResourceHandler;
import services.datasyndication.IDataSyndicationService;
import services.datasyndication.models.DataSyndicationAgreement;
import services.datasyndication.models.DataSyndicationAgreementItem;
import services.datasyndication.models.DataSyndicationAgreementLink;
import utils.form.DataSyndicationAgreementLinkSubmitFormData;

/**
 * The controller which allows to manage the data syndication for a portfolio
 * entry.
 * 
 * @author Johann Kohler
 */
public class PortfolioEntryDataSyndicationController extends Controller {

    private static Form<DataSyndicationAgreementLinkSubmitFormData> agreementLinkSubmitFormTemplate = Form
            .form(DataSyndicationAgreementLinkSubmitFormData.class);

    @Inject
    private IDataSyndicationService dataSyndicationService;

    @Inject
    private IUserSessionManagerPlugin userSessionManagerPlugin;

    /**
     * Display the data syndication agreements.
     * 
     * @param id
     *            the portfolio entry id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(DefaultDynamicResourceHandler.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result index(Long id) {

        // get the portfolio entry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the agreement links as slave (consumer)
        List<DataSyndicationAgreementLink> slaveAgreementLinks = null;
        try {
            slaveAgreementLinks = dataSyndicationService.getAgreementLinksOfSlaveObject(PortfolioEntry.class.getName(), id);
        } catch (Exception e) {
            return ok(views.html.core.portfolioentrydatasyndication.communication_error.render(portfolioEntry));
        }

        // get the agreement links as master
        List<DataSyndicationAgreementLink> masterAgreementLinks = null;
        try {
            masterAgreementLinks = dataSyndicationService.getAgreementLinksOfMasterObject(PortfolioEntry.class.getName(), id);
        } catch (Exception e) {
            return ok(views.html.core.portfolioentrydatasyndication.communication_error.render(portfolioEntry));
        }

        // get the master agreements
        List<DataSyndicationAgreement> masterAgreements = null;
        try {
            masterAgreements = dataSyndicationService.getAgreementsAsMaster();
        } catch (Exception e) {
            return ok(views.html.core.portfolioentrydatasyndication.communication_error.render(portfolioEntry));
        }

        // remove agreements that is not ONGOING or hasn't PE item
        for (DataSyndicationAgreement masterAgreement : masterAgreements) {
            boolean hasPEItem = false;
            for (DataSyndicationAgreementItem item : masterAgreement.items) {
                if (item.dataType.equals(PortfolioEntry.class.getName())) {
                    hasPEItem = true;
                    break;
                }
            }
            if (!masterAgreement.status.equals(DataSyndicationAgreement.Status.ONGOING) || !hasPEItem) {
                masterAgreements.remove(masterAgreement);
            }
        }

        return ok(views.html.core.portfolioentrydatasyndication.index.render(portfolioEntry, slaveAgreementLinks, masterAgreementLinks, masterAgreements));
    }

    /**
     * Display the details of an agreement link with management capabilities.
     * 
     * @param id
     *            the portfolio entry id
     * @param agreementLinkId
     *            the agreement link id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(DefaultDynamicResourceHandler.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result viewAgreementLink(Long id, Long agreementLinkId) {

        // get the portfolio entry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the agreement link
        DataSyndicationAgreementLink agreementLink = null;
        try {
            agreementLink = dataSyndicationService.getAgreementLink(agreementLinkId);
            if (agreementLink == null) {
                return notFound(views.html.error.not_found.render(""));
            }
        } catch (Exception e) {
            return ok(views.html.core.portfolioentrydatasyndication.communication_error.render(portfolioEntry));
        }

        // define if the instance is the master or slave of the agreement link
        Boolean isMasterAgreementLink = agreementLink.agreement.masterPartner.domain.equals(dataSyndicationService.getCurrentDomain());

        return ok(views.html.core.portfolioentrydatasyndication.agreement_link_view.render(portfolioEntry, isMasterAgreementLink, agreementLink));

    }

    /**
     * Cancel an agreement link.
     * 
     * @param id
     *            the portfolio entry id
     * @param agreementLinkId
     *            the agreement link id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(DefaultDynamicResourceHandler.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result cancelAgreementLink(Long id, Long agreementLinkId) {

        // get the portfolio entry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the agreement link
        DataSyndicationAgreementLink agreementLink = null;
        try {
            agreementLink = dataSyndicationService.getAgreementLink(agreementLinkId);
            if (agreementLink == null) {
                return notFound(views.html.error.not_found.render(""));
            }
        } catch (Exception e) {
            return ok(views.html.core.portfolioentrydatasyndication.communication_error.render(portfolioEntry));
        }

        // cancel the agreement
        try {
            dataSyndicationService.cancelAgreementLink(agreementLink);
        } catch (Exception e) {
            return ok(views.html.core.portfolioentrydatasyndication.communication_error.render(portfolioEntry));
        }

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_data_syndication.link.cancel.success"));

        return redirect(controllers.core.routes.PortfolioEntryDataSyndicationController.index(portfolioEntry.id));
    }

    /**
     * Form to submit an agreement link.
     * 
     * @param id
     *            the portfolio entry id
     * @param agreementId
     *            the agreement id
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(DefaultDynamicResourceHandler.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result submitAgreementLink(Long id, Long agreementId) {

        // get the portfolio entry
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the agreement
        DataSyndicationAgreement agreement = null;
        try {
            agreement = dataSyndicationService.getAgreement(agreementId);
            if (agreement == null) {
                return notFound(views.html.error.not_found.render(""));
            }
        } catch (Exception e) {
            return ok(views.html.core.portfolioentrydatasyndication.communication_error.render(portfolioEntry));
        }

        if (!agreement.status.equals(DataSyndicationAgreement.Status.ONGOING)
                || !agreement.masterPartner.domain.equals(dataSyndicationService.getCurrentDomain())) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        // get possible items
        ISelectableValueHolderCollection<Long> itemsAsVH = new DefaultSelectableValueHolderCollection<Long>();
        for (DataSyndicationAgreementItem item : agreement.items) {
            if (item.dataType.equals(PortfolioEntry.class.getName())) {
                itemsAsVH.add(new DefaultSelectableValueHolder<Long>(item.id, item.getLabel()));
            }
        }

        // initialize the form
        Form<DataSyndicationAgreementLinkSubmitFormData> form = agreementLinkSubmitFormTemplate
                .fill(new DataSyndicationAgreementLinkSubmitFormData(agreementId, portfolioEntry.getName(), portfolioEntry.getDescription()));

        return ok(views.html.core.portfolioentrydatasyndication.agreement_link_submit.render(portfolioEntry, agreement, itemsAsVH, form));
    }

    /**
     * Process the form to submit an agreement link.
     */
    @With(CheckPortfolioEntryExists.class)
    @Dynamic(DefaultDynamicResourceHandler.PORTFOLIO_ENTRY_EDIT_DYNAMIC_PERMISSION)
    public Result processSubmitAgreementLink() {

        // bind the form
        Form<DataSyndicationAgreementLinkSubmitFormData> boundForm = agreementLinkSubmitFormTemplate.bindFromRequest();

        // get the portfolio entry
        Long id = Long.valueOf(boundForm.data().get("id"));
        PortfolioEntry portfolioEntry = PortfolioEntryDao.getPEById(id);

        // get the agreement
        Long agreementId = Long.valueOf(boundForm.data().get("agreementId"));
        DataSyndicationAgreement agreement = null;
        try {
            agreement = dataSyndicationService.getAgreement(agreementId);
            if (agreement == null) {
                return notFound(views.html.error.not_found.render(""));
            }
        } catch (Exception e) {
            return ok(views.html.core.portfolioentrydatasyndication.communication_error.render(portfolioEntry));
        }

        if (!agreement.status.equals(DataSyndicationAgreement.Status.ONGOING)
                || !agreement.masterPartner.domain.equals(dataSyndicationService.getCurrentDomain())) {
            return forbidden(views.html.error.access_forbidden.render(""));
        }

        if (boundForm.hasErrors()) {

            // get possible items
            ISelectableValueHolderCollection<Long> itemsAsVH = new DefaultSelectableValueHolderCollection<Long>();
            for (DataSyndicationAgreementItem item : agreement.items) {
                if (item.dataType.equals(PortfolioEntry.class.getName())) {
                    itemsAsVH.add(new DefaultSelectableValueHolder<Long>(item.id, item.getLabel()));
                }
            }

            return ok(views.html.core.portfolioentrydatasyndication.agreement_link_submit.render(portfolioEntry, agreement, itemsAsVH, boundForm));
        }

        DataSyndicationAgreementLinkSubmitFormData formData = boundForm.get();

        try {
            dataSyndicationService.submitAgreementLink(userSessionManagerPlugin.getUserSessionId(ctx()), agreement, formData.name, formData.description,
                    formData.itemIds, PortfolioEntry.class.getName(), id);
        } catch (Exception e) {
            return ok(views.html.core.portfolioentrydatasyndication.communication_error.render(portfolioEntry));
        }

        Utilities.sendSuccessFlashMessage(Msg.get("core.portfolio_entry_data_syndication.link.submit.success"));

        return redirect(controllers.core.routes.PortfolioEntryDataSyndicationController.index(portfolioEntry.id));
    }

}
