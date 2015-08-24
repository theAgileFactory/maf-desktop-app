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
package services.datasyndication;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import constants.IMafConstants;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.api.commons.ApiSignatureException;
import framework.services.api.server.IApiApplicationConfiguration;
import framework.services.api.server.IApiSignatureService;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.Msg;
import models.pmo.PortfolioEntry;
import play.Configuration;
import play.Logger;
import play.i18n.Lang;
import play.inject.ApplicationLifecycle;
import play.libs.F.Promise;
import services.datasyndication.models.DataSyndicationAgreement;
import services.datasyndication.models.DataSyndicationAgreementItem;
import services.datasyndication.models.DataSyndicationAgreementLink;
import services.datasyndication.models.DataSyndicationApiKey;
import services.datasyndication.models.DataSyndicationPartner;
import services.echannel.IEchannelService;
import services.echannel.IEchannelService.EchannelException;
import services.echannel.models.RecipientsDescriptor;

/**
 * The data syndication service.
 * 
 * @author Johann Kohler
 * 
 */
@Singleton
public class DataSyndicationServiceImpl implements IDataSyndicationService {

    private boolean isActive;

    private Lang lang;

    private IApiSignatureService apiSignatureService;
    private IEchannelService echannelService;
    private IPreferenceManagerPlugin preferenceManagerPlugin;

    /**
     * Configurations of the the service.
     * 
     * @author Johann Kohler
     *
     */
    public enum Config {

        DATA_SYNDICATION_ACTIVE("maf.data_syndication.is_active");

        private String configurationKey;

        /**
         * Construct a configuration with its key.
         * 
         * @param configurationKey
         *            the configuration key
         */
        private Config(String configurationKey) {
            this.configurationKey = configurationKey;
        }

        /**
         * Get the configuration key.
         */
        public String getConfigurationKey() {
            return configurationKey;
        }
    }

    /**
     * Initialize the service.
     * 
     * @param lifecycle
     *            the Play life cycle service
     * @param configuration
     *            the Play configuration service
     * @param echannelService
     *            the eChannel service
     * @param apiSignatureService
     *            the API signature service
     * @param preferenceManagerPlugin
     *            the preference service
     * @param i18nMessagesPlugin
     *            the i18n service
     */
    @Inject
    public DataSyndicationServiceImpl(ApplicationLifecycle lifecycle, Configuration configuration, IEchannelService echannelService,
            IApiSignatureService apiSignatureService, IPreferenceManagerPlugin preferenceManagerPlugin, II18nMessagesPlugin i18nMessagesPlugin) {

        Logger.info("SERVICE>>> DataSyndicationServiceImpl starting...");

        this.isActive = configuration.getBoolean(Config.DATA_SYNDICATION_ACTIVE.getConfigurationKey());

        this.lang = i18nMessagesPlugin.getLanguageByCode(i18nMessagesPlugin.getDefaultLanguageCode()).getLang();

        this.echannelService = echannelService;
        this.apiSignatureService = apiSignatureService;
        this.preferenceManagerPlugin = preferenceManagerPlugin;

        lifecycle.addStopHook(() -> {
            Logger.info("SERVICE>>> DataSyndicationServiceImpl stopping...");
            Logger.info("SERVICE>>> DataSyndicationServiceImpl stopped");
            return Promise.pure(null);
        });

        Logger.info("SERVICE>>> DataSyndicationServiceImpl started");
    }

    @Override
    public String getCurrentDomain() {
        return preferenceManagerPlugin.getPreferenceValueAsString(IMafConstants.LICENSE_INSTANCE_DOMAIN_PREFERENCE);
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public List<DataSyndicationPartner> searchFromSlavePartners(String keywords) throws EchannelException {
        return echannelService.findPartners(true, keywords);
    }

    @Override
    public DataSyndicationPartner getPartner(String domain) throws EchannelException {
        return echannelService.getPartner(domain);
    }

    @Override
    public List<DataSyndicationAgreementItem> getAgreementItems() throws EchannelException {
        return echannelService.getAgreementItems();
    }

    @Override
    public void submitAgreement(String refId, String name, Date startDate, Date endDate, List<Long> agreementItemIds, String slaveDomain, String slaveBaseUrl)
            throws EchannelException {

        DataSyndicationAgreement agreement = echannelService.submitAgreement(refId, name, startDate, endDate, agreementItemIds, slaveDomain);

        try {
            String actionLink = slaveBaseUrl + controllers.admin.routes.DataSyndicationController.processAgreement(agreement.id).url();
            String title = Msg.get(this.lang, "data_syndication.submit_agreement.notification.title");
            String message = Msg.get(this.lang, "data_syndication.submit_agreement.notification.message", actionLink);

            echannelService.createNotificationEvent(slaveDomain, getRecipientsDescriptorAsPartnerAdmin(), title, message, actionLink);
        } catch (Exception e) {
            Logger.error("Error when creating notification event for submitAgreement action", e);
        }
    }

    @Override
    public void acceptAgreement(DataSyndicationAgreement agreement) throws ApiSignatureException, EchannelException {

        // create an application API key
        IApiApplicationConfiguration applicationConfiguration = apiSignatureService.setApplicationConfiguration(UUID.randomUUID().toString(),
                "Data syndication key for the instance " + agreement.masterPartner.domain, false, false, "GET (.*)\nPOST (.*)\nPUT (.*)\nDELETE (.*)");

        // Assign the key
        DataSyndicationApiKey apiKey = new DataSyndicationApiKey();
        apiKey.name = applicationConfiguration.getApplicationName();
        apiKey.secretKey = applicationConfiguration.getSignatureGenerator().getSharedSecret();
        apiKey.applicationKey = applicationConfiguration.getSignatureGenerator().getApplicationKey();

        echannelService.acceptAgreement(agreement.id, apiKey);

        try {
            String actionLink = agreement.masterPartner.baseUrl + controllers.admin.routes.DataSyndicationController.viewAgreement(agreement.id, false).url();
            String title = Msg.get(this.lang, "data_syndication.accept_agreement.notification.title");
            String message = Msg.get(this.lang, "data_syndication.accept_agreement.notification.message", actionLink);

            echannelService.createNotificationEvent(agreement.masterPartner.domain, getRecipientsDescriptorAsPartnerAdmin(), title, message, actionLink);
        } catch (Exception e) {
            Logger.error("Error when creating notification event for acceptAgreement action", e);
        }

    }

    @Override
    public void rejectAgreement(DataSyndicationAgreement agreement) throws EchannelException {

        echannelService.rejectAgreement(agreement.id);

        try {
            String actionLink = agreement.masterPartner.baseUrl + controllers.admin.routes.DataSyndicationController.viewAgreement(agreement.id, false).url();
            String title = Msg.get(this.lang, "data_syndication.reject_agreement.notification.title");
            String message = Msg.get(this.lang, "data_syndication.reject_agreement.notification.message", actionLink);

            echannelService.createNotificationEvent(agreement.masterPartner.domain, getRecipientsDescriptorAsPartnerAdmin(), title, message, actionLink);
        } catch (Exception e) {
            Logger.error("Error when creating notification event for rejectAgreement action", e);
        }
    }

    @Override
    public void cancelAgreement(DataSyndicationAgreement agreement) throws ApiSignatureException, EchannelException {

        echannelService.cancelAgreement(agreement.id);

        try {

            String domain = null;
            String baseUrl = null;
            if (this.getCurrentDomain().equals(agreement.masterPartner.domain)) {
                domain = agreement.slavePartner.domain;
                baseUrl = agreement.slavePartner.baseUrl;
            } else {
                domain = agreement.masterPartner.domain;
                baseUrl = agreement.masterPartner.baseUrl;
            }

            String actionLink = baseUrl + controllers.admin.routes.DataSyndicationController.viewAgreement(agreement.id, false).url();
            String title = Msg.get(this.lang, "data_syndication.cancel_agreement.notification.title");
            String message = Msg.get(this.lang, "data_syndication.cancel_agreement.notification.message", actionLink);

            echannelService.createNotificationEvent(domain, getRecipientsDescriptorAsPartnerAdmin(), title, message, actionLink);
        } catch (Exception e) {
            Logger.error("Error when creating notification event for cancelAgreement action", e);
        }
    }

    @Override
    public void suspendAgreement(DataSyndicationAgreement agreement) throws EchannelException {

        echannelService.suspendAgreement(agreement.id);

        try {
            String actionLink = agreement.slavePartner.baseUrl + controllers.admin.routes.DataSyndicationController.viewAgreement(agreement.id, false).url();
            String title = Msg.get(this.lang, "data_syndication.suspend_agreement.notification.title");
            String message = Msg.get(this.lang, "data_syndication.suspend_agreement.notification.message", actionLink);

            echannelService.createNotificationEvent(agreement.slavePartner.domain, getRecipientsDescriptorAsPartnerAdmin(), title, message, actionLink);
        } catch (Exception e) {
            Logger.error("Error when creating notification event for suspendAgreement action", e);
        }
    }

    @Override
    public void restartAgreement(DataSyndicationAgreement agreement) throws EchannelException {

        echannelService.restartAgreement(agreement.id);

        try {
            String actionLink = agreement.slavePartner.baseUrl + controllers.admin.routes.DataSyndicationController.viewAgreement(agreement.id, false).url();
            String title = Msg.get(this.lang, "data_syndication.restart_agreement.notification.title");
            String message = Msg.get(this.lang, "data_syndication.restart_agreement.notification.message", actionLink);

            echannelService.createNotificationEvent(agreement.slavePartner.domain, getRecipientsDescriptorAsPartnerAdmin(), title, message, actionLink);
        } catch (Exception e) {
            Logger.error("Error when creating notification event for restartAgreement action", e);
        }
    }

    @Override
    public DataSyndicationAgreement getAgreement(Long id) throws EchannelException {
        return echannelService.getAgreement(id);
    }

    @Override
    public List<DataSyndicationAgreement> getAgreementsAsMaster() throws EchannelException {
        return echannelService.getAgreementsAsMaster();
    }

    @Override
    public List<DataSyndicationAgreement> getAgreementsAsSlave() throws EchannelException {
        return echannelService.getAgreementsAsSlave();
    }

    @Override
    public List<DataSyndicationAgreementLink> getLinksOfAgreement(Long id) throws EchannelException {
        return echannelService.getLinksOfAgreement(id);
    }

    @Override
    public void submitAgreementLink(String masterPrincipalUid, DataSyndicationAgreement agreement, String name, String description,
            List<Long> agreementItemIds, String dataType, Long masterObjectId) throws EchannelException {

        DataSyndicationAgreementLink agreementLink = echannelService.submitAgreementLink(masterPrincipalUid, agreement.id, name, description,
                agreementItemIds, dataType, masterObjectId);

        try {
            String actionLink = agreement.slavePartner.baseUrl
                    + controllers.admin.routes.DataSyndicationController.processAgreementLink(agreementLink.id).url();
            String title = Msg.get(this.lang, "data_syndication.submit_agreement_link.notification.title");
            String message = Msg.get(this.lang, "data_syndication.submit_agreement_link.notification.message", actionLink);

            echannelService.createNotificationEvent(agreement.slavePartner.domain, getRecipientsDescriptorAsPartnerAdmin(), title, message, actionLink);
        } catch (Exception e) {
            Logger.error("Error when creating notification event for submitAgreementLink action", e);
        }

    }

    @Override
    public void acceptAgreementLink(DataSyndicationAgreementLink agreementLink, Long slaveObjectId) throws EchannelException {

        echannelService.acceptAgreementLink(agreementLink.id, slaveObjectId);

        try {

            String actionLink = null;
            if (agreementLink.dataType.equals(PortfolioEntry.class.getName())) {
                actionLink = agreementLink.agreement.masterPartner.baseUrl + controllers.core.routes.PortfolioEntryDataSyndicationController
                        .viewAgreementLink(agreementLink.masterObjectId, agreementLink.id).url();
            }

            String title = Msg.get(this.lang, "data_syndication.accept_agreement_link.notification.title");
            String message = Msg.get(this.lang, "data_syndication.accept_agreement_link.notification.message", actionLink);

            echannelService.createNotificationEvent(agreementLink.agreement.masterPartner.domain,
                    getRecipientsDescriptorAsPrincipal(agreementLink.masterPrincipalUid), title, message, actionLink);

        } catch (Exception e) {
            Logger.error("Error when creating notification event for acceptAgreementLink action", e);
        }

    }

    @Override
    public void rejectAgreementLink(DataSyndicationAgreementLink agreementLink) throws EchannelException {

        echannelService.rejectAgreementLink(agreementLink.id);

        try {

            String actionLink = null;
            if (agreementLink.dataType.equals(PortfolioEntry.class.getName())) {
                actionLink = agreementLink.agreement.masterPartner.baseUrl + controllers.core.routes.PortfolioEntryDataSyndicationController
                        .viewAgreementLink(agreementLink.masterObjectId, agreementLink.id).url();
            }

            String title = Msg.get(this.lang, "data_syndication.reject_agreement_link.notification.title");
            String message = Msg.get(this.lang, "data_syndication.reject_agreement_link.notification.message", actionLink);

            echannelService.createNotificationEvent(agreementLink.agreement.masterPartner.domain,
                    getRecipientsDescriptorAsPrincipal(agreementLink.masterPrincipalUid), title, message, actionLink);

        } catch (Exception e) {
            Logger.error("Error when creating notification event for rejectAgreementLink action", e);
        }

    }

    @Override
    public void cancelAgreementLink(DataSyndicationAgreementLink agreementLink) throws EchannelException {

        echannelService.cancelAgreement(agreementLink.id);

        try {

            String domain = null;
            String baseUrl = null;
            if (this.getCurrentDomain().equals(agreementLink.agreement.masterPartner.domain)) {
                domain = agreementLink.agreement.slavePartner.domain;
                baseUrl = agreementLink.agreement.slavePartner.baseUrl;
            } else {
                domain = agreementLink.agreement.masterPartner.domain;
                baseUrl = agreementLink.agreement.masterPartner.baseUrl;
            }

            String actionLink = baseUrl + controllers.admin.routes.DataSyndicationController.viewAgreement(agreementLink.agreement.id, true).url();
            String title = Msg.get(this.lang, "data_syndication.cancel_agreement_link.notification.title");
            String message = Msg.get(this.lang, "data_syndication.cancel_agreement_link.notification.message", actionLink);

            echannelService.createNotificationEvent(domain, getRecipientsDescriptorAsPartnerAdmin(), title, message, actionLink);

        } catch (Exception e) {
            Logger.error("Error when creating notification event for cancelAgreementLink action", e);
        }

    }

    @Override
    public DataSyndicationAgreementLink getAgreementLink(Long agreementLinkId) throws EchannelException {
        return echannelService.getAgreementLink(agreementLinkId);
    }

    @Override
    public void deleteAgreementLink(DataSyndicationAgreementLink agreementLink) throws EchannelException {

        echannelService.deleteAgreementLink(agreementLink.id);

        try {
            String title = Msg.get(this.lang, "data_syndication.delete_agreement_link.notification.title");
            String message = Msg.get(this.lang, "data_syndication.delete_agreement_link.notification.message", agreementLink.agreement.name);

            String masterActionLink = agreementLink.agreement.masterPartner.baseUrl
                    + controllers.admin.routes.DataSyndicationController.viewMasterAgreements().url();
            echannelService.createNotificationEvent(agreementLink.agreement.masterPartner.domain, getRecipientsDescriptorAsPartnerAdmin(), title, message,
                    masterActionLink);

            String slaveActionLink = agreementLink.agreement.slavePartner.baseUrl
                    + controllers.admin.routes.DataSyndicationController.viewMasterAgreements().url();
            echannelService.createNotificationEvent(agreementLink.agreement.slavePartner.domain, getRecipientsDescriptorAsPartnerAdmin(), title, message,
                    slaveActionLink);

        } catch (Exception e) {
            Logger.error("Error when creating notification event for deleteAgreementLink action", e);
        }
    }

    @Override
    public List<DataSyndicationAgreementLink> getAgreementLinksToSynchronize() throws EchannelException {
        return echannelService.getAgreementLinksToSynchronize();
    }

    @Override
    public List<DataSyndicationAgreementLink> getAgreementLinksOfMasterObject(String dataType, Long masterObjectId) throws EchannelException {
        return echannelService.getAgreementLinksOfMasterObject(dataType, masterObjectId);
    }

    @Override
    public List<DataSyndicationAgreementLink> getAgreementLinksOfSlaveObject(String dataType, Long masterObjectId) throws EchannelException {
        return echannelService.getAgreementLinksOfSlaveObject(dataType, masterObjectId);
    }

    /**
     * Get the "partner admin" recipients descriptor for notification event.
     */
    private static RecipientsDescriptor getRecipientsDescriptorAsPartnerAdmin() {

        RecipientsDescriptor descriptor = new RecipientsDescriptor();

        descriptor.type = RecipientsDescriptor.Type.PERMISSIONS;
        descriptor.permissions = new ArrayList<>();
        descriptor.permissions.add(IMafConstants.PARTNER_SYNDICATION_PERMISSION);

        return descriptor;
    }

    /**
     * Get the "principal" recipients descriptor for notification event.
     * 
     * @param principalUid
     *            as principal uid
     */
    private static RecipientsDescriptor getRecipientsDescriptorAsPrincipal(String principalUid) {

        RecipientsDescriptor descriptor = new RecipientsDescriptor();

        descriptor.type = RecipientsDescriptor.Type.PRINCIPALS;
        descriptor.principals = new ArrayList<>();
        descriptor.principals.add(principalUid);

        return descriptor;
    }
}