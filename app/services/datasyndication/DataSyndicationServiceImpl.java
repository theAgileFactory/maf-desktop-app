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
import play.Configuration;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.F.Promise;
import services.datasyndication.models.DataSyndicationAgreement;
import services.datasyndication.models.DataSyndicationAgreementItem;
import services.datasyndication.models.DataSyndicationAgreementLink;
import services.datasyndication.models.DataSyndicationApiKey;
import services.datasyndication.models.DataSyndicationPartner;
import services.echannel.IEchannelService;

/**
 * The data syndication service.
 * 
 * @author Johann Kohler
 * 
 */
@Singleton
public class DataSyndicationServiceImpl implements IDataSyndicationService {

    private boolean isActive;

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
     */
    @Inject
    public DataSyndicationServiceImpl(ApplicationLifecycle lifecycle, Configuration configuration, IEchannelService echannelService,
            IApiSignatureService apiSignatureService, IPreferenceManagerPlugin preferenceManagerPlugin) {

        Logger.info("SERVICE>>> DataSyndicationServiceImpl starting...");

        this.isActive = configuration.getBoolean(Config.DATA_SYNDICATION_ACTIVE.getConfigurationKey());

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
    public List<DataSyndicationPartner> searchFromSlavePartners(String keywords) {
        return echannelService.findPartners(true, keywords);
    }

    @Override
    public DataSyndicationPartner getPartner(String domain) {
        return echannelService.getPartner(domain);
    }

    @Override
    public List<DataSyndicationAgreementItem> getDataAgreementItems() {
        return echannelService.getDataAgreementItems();
    }

    @Override
    public void submitAgreement(String refId, String name, Date startDate, Date endDate, List<Long> agreementItemIds, String slaveDomain) {
        echannelService.submitAgreement(refId, name, startDate, endDate, agreementItemIds, slaveDomain, IMafConstants.PARTNER_SYNDICATION_PERMISSION);
    }

    @Override
    public void acceptAgreement(DataSyndicationAgreement agreement) throws ApiSignatureException, DataSyndicationException {

        // create an application API key
        IApiApplicationConfiguration applicationConfiguration = apiSignatureService.setApplicationConfiguration(UUID.randomUUID().toString(),
                "Data syndication key for the instance " + agreement.masterPartner.domain, false, false, "GET (.*)\nPOST (.*)\nPUT (.*)\nDELETE (.*)");

        // Assign the key
        DataSyndicationApiKey apiKey = new DataSyndicationApiKey();
        apiKey.name = applicationConfiguration.getApplicationName();
        apiKey.secretKey = applicationConfiguration.getSignatureGenerator().getSharedSecret();
        apiKey.applicationKey = applicationConfiguration.getSignatureGenerator().getApplicationKey();

        echannelService.acceptAgreement(agreement.id, apiKey, IMafConstants.PARTNER_SYNDICATION_PERMISSION);

    }

    @Override
    public void rejectAgreement(DataSyndicationAgreement agreement) throws DataSyndicationException {
        echannelService.rejectAgreement(agreement.id, IMafConstants.PARTNER_SYNDICATION_PERMISSION);
    }

    @Override
    public void cancelAgreement(DataSyndicationAgreement agreement) throws ApiSignatureException, DataSyndicationException {
        echannelService.cancelAgreement(agreement.id, IMafConstants.PARTNER_SYNDICATION_PERMISSION);
    }

    @Override
    public void suspendAgreement(DataSyndicationAgreement agreement) throws DataSyndicationException {
        echannelService.suspendAgreement(agreement.id, IMafConstants.PARTNER_SYNDICATION_PERMISSION);
    }

    @Override
    public void restartAgreement(DataSyndicationAgreement agreement) throws DataSyndicationException {
        echannelService.restartAgreement(agreement.id, IMafConstants.PARTNER_SYNDICATION_PERMISSION);
    }

    @Override
    public DataSyndicationAgreement getAgreement(Long id) {
        return echannelService.getAgreement(id);
    }

    @Override
    public void deleteAgreement(Long id) {
        echannelService.deleteAgreement(id);
    }

    @Override
    public List<DataSyndicationAgreement> getMasterAgreements() {
        return echannelService.getMasterAgreements();
    }

    @Override
    public List<DataSyndicationAgreement> getSlaveAgreements() {
        return echannelService.getSlaveAgreements();
    }

    @Override
    public List<DataSyndicationAgreementLink> getLinksOfAgreement(Long id) {
        return echannelService.getLinksOfAgreement(id);
    }

    @Override
    public void submitAgreementLink(DataSyndicationAgreement agreement, String name, String description, List<Long> agreementItemIds, String dataType,
            Long masterObjectId) throws DataSyndicationException {
        echannelService.submitAgreementLink(agreement.id, name, description, agreementItemIds, dataType, masterObjectId,
                IMafConstants.PARTNER_SYNDICATION_PERMISSION);
    }

    @Override
    public void acceptAgreementLink(DataSyndicationAgreementLink agreementLink, Long slaveObjectId) throws DataSyndicationException {
        echannelService.acceptAgreementLink(agreementLink.id, slaveObjectId, IMafConstants.PARTNER_SYNDICATION_PERMISSION);
    }

    @Override
    public void rejectAgreementLink(DataSyndicationAgreementLink agreementLink) throws DataSyndicationException {
        echannelService.rejectAgreementLink(agreementLink.id, IMafConstants.PARTNER_SYNDICATION_PERMISSION);

    }

    @Override
    public void cancelAgreementLink(DataSyndicationAgreementLink agreementLink) throws DataSyndicationException {
        echannelService.cancelAgreement(agreementLink.id, IMafConstants.PARTNER_SYNDICATION_PERMISSION);
    }

    @Override
    public DataSyndicationAgreementLink getAgreementLink(Long agreementLinkId) {
        return echannelService.getAgreementLink(agreementLinkId);
    }

    @Override
    public List<DataSyndicationAgreementLink> getAgreementLinksToSynchronize() {
        return echannelService.getAgreementLinksToSynchronize();
    }

    @Override
    public List<DataSyndicationAgreementLink> getAgreementLinksOfMasterObject(String dataType, Long masterObjectId) {
        return echannelService.getAgreementLinksOfMasterObject(dataType, masterObjectId);
    }

    @Override
    public List<DataSyndicationAgreementLink> getAgreementLinksOfSlaveObject(String dataType, Long masterObjectId) {
        return echannelService.getAgreementLinksOfSlaveObject(dataType, masterObjectId);
    }
}