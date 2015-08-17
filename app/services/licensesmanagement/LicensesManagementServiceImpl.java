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
package services.licensesmanagement;

import javax.inject.Inject;
import javax.inject.Singleton;

import dao.pmo.PortfolioEntryDao;
import framework.services.ext.IExtensionManagerService;
import framework.services.storage.IAttachmentManagerPlugin;
import framework.services.storage.IPersonalStoragePlugin;
import framework.services.storage.ISharedStorageService;
import models.framework_models.account.Principal;
import play.Configuration;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.F.Promise;
import services.echannel.IEchannelService;
import services.echannel.request.LoginEventRequest.ErrorCode;

/**
 * The licenses management plugin.
 * 
 * @author Johann Kohler
 * 
 */
@Singleton
public class LicensesManagementServiceImpl implements ILicensesManagementService {

    private boolean isActive;

    private IEchannelService echannelService;
    private ISharedStorageService sharedStorageService;
    private IAttachmentManagerPlugin attachmentManagerPlugin;
    private IExtensionManagerService extensionManagerService;
    private IPersonalStoragePlugin personalStoragePlugin;

    /**
     * Configurations of the the service.
     * 
     * @author Johann Kohler
     *
     */
    public enum Config {

        LICENSE_MANAGEMENT_ACTIVE("maf.licenses_management.is_active");

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
     * @param sharedStorageService
     *            the shared storage service
     * @param attachmentManagerPlugin
     *            the attachment manager service
     * @param extensionManagerService
     *            the extension manager service
     * @param personalStoragePlugin
     *            the personal storage service
     */
    @Inject
    public LicensesManagementServiceImpl(ApplicationLifecycle lifecycle, Configuration configuration, IEchannelService echannelService,
            ISharedStorageService sharedStorageService, IAttachmentManagerPlugin attachmentManagerPlugin, IExtensionManagerService extensionManagerService,
            IPersonalStoragePlugin personalStoragePlugin) {

        Logger.info("SERVICE>>> LicensesManagementServiceImpl starting...");

        this.isActive = configuration.getBoolean(Config.LICENSE_MANAGEMENT_ACTIVE.getConfigurationKey());

        this.echannelService = echannelService;
        this.sharedStorageService = sharedStorageService;
        this.attachmentManagerPlugin = attachmentManagerPlugin;
        this.extensionManagerService = extensionManagerService;
        this.personalStoragePlugin = personalStoragePlugin;

        lifecycle.addStopHook(() -> {
            Logger.info("SERVICE>>> LicensesManagementServiceImpl stopping...");
            Logger.info("SERVICE>>> LicensesManagementServiceImpl stopped");
            return Promise.pure(null);
        });

        Logger.info("SERVICE>>> LicensesManagementServiceImpl started");
    }

    @Override
    public boolean canCreateUser() {
        if (this.isActive) {
            return echannelService.canCreateUser(Principal.getConsumedUsers());
        } else {
            return true;
        }
    }

    @Override
    public boolean canCreatePortfolioEntry() {
        if (this.isActive) {
            return echannelService.canCreatePortfolioEntry(PortfolioEntryDao.getPEAsNumberConsumedLicenses());
        } else {
            return true;
        }
    }

    @Override
    public boolean isInstanceAccessible() {
        if (this.isActive) {
            return echannelService.isInstanceAccessible();
        } else {
            return true;
        }
    }

    @Override
    public void updateConsumedUsers() {
        if (this.isActive) {
            echannelService.updateConsumedUsers(Principal.getConsumedUsers());
        }
    }

    @Override
    public void updateConsumedPortfolioEntries() {
        if (this.isActive) {
            echannelService.updateConsumedPortfolioEntries(PortfolioEntryDao.getPEAsNumberConsumedLicenses());
        }
    }

    @Override
    public void updateConsumedStorage() {

        if (this.isActive) {

            try {

                // shared storage
                long sharedStorage = getSharedStorageService().getSize();
                Logger.debug("sharedStorage (B): " + sharedStorage);

                // personal storage
                long personalStorage = getPersonalStoragePlugin().getSize();
                Logger.debug("personalStorage (B): " + personalStorage);

                // attachments
                long attachments = getAttachmentManagerPlugin().getSize();
                Logger.debug("attachments (B): " + attachments);

                // extensions
                long extensions = getExtensionManagerService().getSize();
                Logger.debug("extensions (B): " + extensions);

                int storage = (int) (sharedStorage + personalStorage + attachments + extensions) / (1024 * 1024 * 1024);
                Logger.debug("storage (GB): " + storage);

                echannelService.updateConsumedStorage(storage);

            } catch (Exception e) {
                Logger.error("License management service unexpected error / updateConsumedStorage", e);
            }

        }
    }

    @Override
    public void addLoginEvent(String uid, Boolean result, ErrorCode errorCode, String errorMessage) {
        if (this.isActive) {
            echannelService.addLoginEvent(uid, result, errorCode, errorMessage);
        }
    }

    /**
     * Get the shared storage service.
     */
    private ISharedStorageService getSharedStorageService() {
        return sharedStorageService;
    }

    /**
     * Get the attachment manager service.
     */
    private IAttachmentManagerPlugin getAttachmentManagerPlugin() {
        return attachmentManagerPlugin;
    }

    /**
     * Get the extension manager service.
     */
    private IExtensionManagerService getExtensionManagerService() {
        return extensionManagerService;
    }

    /**
     * Get the personal storage service.
     */
    private IPersonalStoragePlugin getPersonalStoragePlugin() {
        return personalStoragePlugin;
    }

}
