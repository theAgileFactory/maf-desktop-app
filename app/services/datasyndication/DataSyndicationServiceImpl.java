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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import constants.IMafConstants;
import dao.pmo.PortfolioEntryDao;
import dao.pmo.PortfolioEntryPlanningPackageDao;
import dao.pmo.PortfolioEntryReportDao;
import dao.timesheet.TimesheetDao;
import framework.services.account.IPreferenceManagerPlugin;
import framework.services.api.AbstractApiController;
import framework.services.api.commons.ApiMethod;
import framework.services.api.commons.ApiSignatureException;
import framework.services.api.server.IApiApplicationConfiguration;
import framework.services.api.server.IApiSignatureService;
import framework.services.configuration.II18nMessagesPlugin;
import framework.utils.Msg;
import models.pmo.PortfolioEntry;
import models.pmo.PortfolioEntryPlanningPackage;
import models.pmo.PortfolioEntryReport;
import models.timesheet.TimesheetLog;
import play.Configuration;
import play.Logger;
import play.i18n.Lang;
import play.inject.ApplicationLifecycle;
import play.libs.F.Promise;
import services.bizdockapi.IBizdockApiClient;
import services.bizdockapi.IBizdockApiClient.BizdockApiException;
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
    private IBizdockApiClient bizdockApiClient;

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
     * @param bizdockApiClient
     *            the BizDock API client (for the slave instance)
     * @param apiSignatureService
     *            the API signature service
     * @param preferenceManagerPlugin
     *            the preference service
     * @param i18nMessagesPlugin
     *            the i18n service
     */
    @Inject
    public DataSyndicationServiceImpl(ApplicationLifecycle lifecycle, Configuration configuration, IEchannelService echannelService,
            IBizdockApiClient bizdockApiClient, IApiSignatureService apiSignatureService, IPreferenceManagerPlugin preferenceManagerPlugin,
            II18nMessagesPlugin i18nMessagesPlugin) {

        Logger.info("SERVICE>>> DataSyndicationServiceImpl starting...");

        this.isActive = configuration.getBoolean(Config.DATA_SYNDICATION_ACTIVE.getConfigurationKey());

        this.lang = i18nMessagesPlugin.getLanguageByCode(i18nMessagesPlugin.getDefaultLanguageCode()).getLang();

        this.echannelService = echannelService;
        this.apiSignatureService = apiSignatureService;
        this.preferenceManagerPlugin = preferenceManagerPlugin;
        this.bizdockApiClient = bizdockApiClient;

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
    public Date getStringDate(String stringDate) {
        SimpleDateFormat df = new SimpleDateFormat(AbstractApiController.DATE_FORMAT);
        try {
            return df.parse(stringDate);
        } catch (Exception e) {
            return null;
        }
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
    public DataSyndicationAgreementItem getAgreementItemByDataTypeAndDescriptor(String dataType, String descriptor) throws EchannelException {
        return echannelService.getAgreementItemByDataTypeAndDescriptor(dataType, descriptor);
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
    public void submitAgreementNoSlave(String refId, String name, Date startDate, Date endDate, List<Long> agreementItemIds, String partnerEmail)
            throws EchannelException {
        echannelService.submitAgreementNoSlave(refId, name, startDate, endDate, agreementItemIds, partnerEmail);
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
                if (agreement.slavePartner != null) {
                    domain = agreement.slavePartner.domain;
                    baseUrl = agreement.slavePartner.baseUrl;
                }
            } else {
                domain = agreement.masterPartner.domain;
                baseUrl = agreement.masterPartner.baseUrl;
            }

            if (domain != null && baseUrl != null) {

                String actionLink = baseUrl + controllers.admin.routes.DataSyndicationController.viewAgreement(agreement.id, false).url();
                String title = Msg.get(this.lang, "data_syndication.cancel_agreement.notification.title");
                String message = Msg.get(this.lang, "data_syndication.cancel_agreement.notification.message", actionLink);

                echannelService.createNotificationEvent(domain, getRecipientsDescriptorAsPartnerAdmin(), title, message, actionLink);
            }

        } catch (Exception e) {
            Logger.error("Error when creating notification event for cancelAgreement action", e);
        }
    }

    @Override
    public void suspendAgreement(DataSyndicationAgreement agreement) throws EchannelException {

        echannelService.suspendAgreement(agreement.id);

        if (agreement.slavePartner != null) {
            try {

                String actionLink = agreement.slavePartner.baseUrl
                        + controllers.admin.routes.DataSyndicationController.viewAgreement(agreement.id, false).url();
                String title = Msg.get(this.lang, "data_syndication.suspend_agreement.notification.title");
                String message = Msg.get(this.lang, "data_syndication.suspend_agreement.notification.message", actionLink);

                echannelService.createNotificationEvent(agreement.slavePartner.domain, getRecipientsDescriptorAsPartnerAdmin(), title, message, actionLink);
            } catch (Exception e) {
                Logger.error("Error when creating notification event for suspendAgreement action", e);
            }
        }
    }

    @Override
    public void restartAgreement(DataSyndicationAgreement agreement) throws EchannelException {

        echannelService.restartAgreement(agreement.id);

        if (agreement.slavePartner != null) {
            try {
                String actionLink = agreement.slavePartner.baseUrl
                        + controllers.admin.routes.DataSyndicationController.viewAgreement(agreement.id, false).url();
                String title = Msg.get(this.lang, "data_syndication.restart_agreement.notification.title");
                String message = Msg.get(this.lang, "data_syndication.restart_agreement.notification.message", actionLink);

                echannelService.createNotificationEvent(agreement.slavePartner.domain, getRecipientsDescriptorAsPartnerAdmin(), title, message, actionLink);
            } catch (Exception e) {
                Logger.error("Error when creating notification event for restartAgreement action", e);
            }
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

        echannelService.cancelAgreementLink(agreementLink.id);

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
    public List<DataSyndicationAgreementLink> getAgreementLinksOfSlaveObject(String dataType, Long slaveObjectId) throws EchannelException {
        return echannelService.getAgreementLinksOfSlaveObject(dataType, slaveObjectId);
    }

    @Override
    public List<DataSyndicationAgreementLink> getAgreementLinksOfItemAndSlaveObject(DataSyndicationAgreementItem item, String dataType, Long slaveObjectId)
            throws EchannelException {
        List<DataSyndicationAgreementLink> agreementLinks = new ArrayList<>();
        for (DataSyndicationAgreementLink agreementLink : this.getAgreementLinksOfSlaveObject(PortfolioEntry.class.getName(), slaveObjectId)) {
            if (agreementLink.items.contains(item)) {
                agreementLinks.add(agreementLink);
            }
        }
        return agreementLinks;
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

    @Override
    public void postData(DataSyndicationAgreementLink agreementLink) throws DataSyndicationPostDataException {

        // Call the getSystemCurrentTime method in order to now if the slave
        // instance is accessible
        try {
            String getSystemCurrentTimeUrl = agreementLink.agreement.slavePartner.baseUrl
                    + controllers.api.system.routes.SystemApiController.getSystemCurrentTime().url();
            bizdockApiClient.call(agreementLink.agreement.apiKey.applicationKey, agreementLink.agreement.apiKey.secretKey, ApiMethod.GET,
                    getSystemCurrentTimeUrl, null);
        } catch (Exception e) {
            Logger.error("dataSyndicationService.postData: error with bizdockApiClient.getSystemCurrentTime", e);
            throw new DataSyndicationPostDataException(DataSyndicationPostDataException.ErrorCode.E1001);
        }

        // PortfolioEntry case
        if (agreementLink.dataType.equals(PortfolioEntry.class.getName())) {

            // verify the master object exists
            PortfolioEntry masterPE = PortfolioEntryDao.getPEById(agreementLink.masterObjectId);
            if (masterPE == null) {
                try {
                    this.deleteAgreementLink(agreementLink);
                    throw new DataSyndicationPostDataException(DataSyndicationPostDataException.ErrorCode.E2002);
                } catch (EchannelException e) {
                    Logger.error("dataSyndicationService.postData: error with dataSyndicationService.deleteAgreementLink", e);
                    throw new DataSyndicationPostDataException(DataSyndicationPostDataException.ErrorCode.E1003);
                }
            }

            // verify the slave object exists
            JsonNode reponse = null;
            try {
                String getPortfolioEntryUrl = agreementLink.agreement.slavePartner.baseUrl
                        + controllers.api.core.routes.PortfolioEntryApiController.getPortfolioEntryById(agreementLink.slaveObjectId).url();
                reponse = bizdockApiClient.call(agreementLink.agreement.apiKey.applicationKey, agreementLink.agreement.apiKey.secretKey, ApiMethod.GET,
                        getPortfolioEntryUrl, null);
            } catch (BizdockApiException e) {
                if (e.getHttpStatusCode().equals(404)) {
                    try {
                        this.deleteAgreementLink(agreementLink);
                        throw new DataSyndicationPostDataException(DataSyndicationPostDataException.ErrorCode.E2003);
                    } catch (EchannelException e2) {
                        Logger.error("dataSyndicationService.postData: error with dataSyndicationService.deleteAgreementLink", e2);
                        throw new DataSyndicationPostDataException(DataSyndicationPostDataException.ErrorCode.E1003);
                    }
                } else {
                    Logger.error("dataSyndicationService.postData: error with bizdockApiClient.getPortfolioEntryById", e);
                    throw new DataSyndicationPostDataException(DataSyndicationPostDataException.ErrorCode.E1002);
                }
            }
            PortfolioEntry slavePE = null;
            try {
                slavePE = bizdockApiClient.getMapper().treeToValue(reponse, PortfolioEntry.class);
            } catch (JsonProcessingException e) {
                Logger.error("dataSyndicationService.postData: error with JSON parsing", e);
                throw new DataSyndicationPostDataException(DataSyndicationPostDataException.ErrorCode.E1002);
            }

            // if the slave or the master object is not active, then the sync is
            // not done
            if (masterPE.archived || slavePE.archived) {
                throw new DataSyndicationPostDataException(DataSyndicationPostDataException.ErrorCode.E2004);
            }

            // for each item
            for (DataSyndicationAgreementItem agreementItem : agreementLink.items) {

                // construct the data
                List<List<Object>> data = new ArrayList<>();
                if (agreementItem.descriptor.equals("PLANNING_PACKAGE")) {
                    data.add(Arrays.asList("object.portfolio_entry_planning_package.is_important.label", "object.portfolio_entry_planning_package.name.label",
                            "object.portfolio_entry_planning_package.description.label", "object.portfolio_entry_planning_package.start_date.label",
                            "object.portfolio_entry_planning_package.end_date.label", "object.portfolio_entry_planning_package.group.label",
                            "object.portfolio_entry_planning_package.status.label"));
                    for (PortfolioEntryPlanningPackage planningPackage : PortfolioEntryPlanningPackageDao
                            .getPEPlanningPackageAsListByPE(agreementLink.masterObjectId)) {
                        String group = planningPackage.portfolioEntryPlanningPackageGroup != null
                                ? Msg.get(planningPackage.portfolioEntryPlanningPackageGroup.getName()) : null;
                        data.add(Arrays.asList(planningPackage.isImportant, planningPackage.name, planningPackage.description, planningPackage.startDate,
                                planningPackage.endDate, group,
                                "object.portfolio_entry_planning_package.status." + planningPackage.status.name() + ".label"));
                    }

                } else if (agreementItem.descriptor.equals("REPORT")) {
                    data.add(Arrays.asList("object.portfolio_entry_report.report_date.label", "object.portfolio_entry_report.author.label",
                            "object.portfolio_entry_report.status.label", "object.portfolio_entry_report.comments.label"));
                    for (PortfolioEntryReport report : PortfolioEntryReportDao.getPEReportAsListByPE(agreementLink.masterObjectId)) {
                        String status = views.html.modelsparts.display_portfolio_entry_report_status_type.render(report.portfolioEntryReportStatusType)
                                .body();
                        data.add(Arrays.asList(report.creationDate, report.author.getName(), status, report.comments));
                    }
                } else if (agreementItem.descriptor.equals("TIMESHEET")) {
                    data.add(Arrays.asList("object.timesheet_report.actor.label", "object.timesheet_log.log_date.label", "object.timesheet_log.hours.label",
                            "object.timesheet_report.status.label", "object.timesheet_entry.planning_package.label"));
                    for (TimesheetLog timesheetLog : TimesheetDao.getTimesheetLogAsExprByPortfolioEntry(agreementLink.masterObjectId).findList()) {
                        String planningPackageName = timesheetLog.timesheetEntry.portfolioEntryPlanningPackage != null
                                ? timesheetLog.timesheetEntry.portfolioEntryPlanningPackage.name : null;
                        String status = "<span class=\"label label-" + timesheetLog.timesheetEntry.timesheetReport.getStatusCssClass() + "\">"
                                + Msg.get("object.timesheet_report.status." + timesheetLog.timesheetEntry.timesheetReport.status.name() + ".label")
                                + "</span>";
                        data.add(Arrays.asList(timesheetLog.timesheetEntry.timesheetReport.actor.getName(), timesheetLog.logDate, timesheetLog.hours, status,
                                planningPackageName));
                    }
                }
                JsonNode jsonData = bizdockApiClient.getMapper().valueToTree(data);

                // post the data
                try {
                    String postDataUrl = agreementLink.agreement.slavePartner.baseUrl
                            + controllers.api.core.routes.DataSyndicationApiController.postData(agreementLink.id, agreementItem.id).url();
                    bizdockApiClient.call(agreementLink.agreement.apiKey.applicationKey, agreementLink.agreement.apiKey.secretKey, ApiMethod.POST,
                            postDataUrl, jsonData);
                } catch (BizdockApiException e) {
                    Logger.error("dataSyndicationService.postData: error with bizdockApiClient.postData", e);
                    throw new DataSyndicationPostDataException(DataSyndicationPostDataException.ErrorCode.E1004);
                }

            }

        } else {
            throw new DataSyndicationPostDataException(DataSyndicationPostDataException.ErrorCode.E2001);
        }

    }

}