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
     */
    @Inject
    public LicensesManagementServiceImpl(ApplicationLifecycle lifecycle, Configuration configuration, IEchannelService echannelService) {

        Logger.info("SERVICE>>> LicensesManagementServiceImpl starting...");

        this.isActive = configuration.getBoolean(Config.LICENSE_MANAGEMENT_ACTIVE.getConfigurationKey());

        this.echannelService = echannelService;

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
            return echannelService.canCreateUser();
        } else {
            return true;
        }
    }

    @Override
    public boolean canCreatePortfolioEntry() {
        if (this.isActive) {
            return echannelService.canCreatePortfolioEntry();
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
            echannelService.updateConsumedUsers();
        }
    }

    @Override
    public void updateConsumedPortfolioEntries() {
        if (this.isActive) {
            echannelService.updateConsumedPortfolioEntries();
        }
    }

    @Override
    public void updateConsumedStorage() {
        if (this.isActive) {
            echannelService.updateConsumedStorage();
        }
    }

    @Override
    public void addLoginEvent(String uid, Boolean result, ErrorCode errorCode, String errorMessage) {
        if (this.isActive) {
            echannelService.addLoginEvent(uid, result, errorCode, errorMessage);
        }
    }

}
