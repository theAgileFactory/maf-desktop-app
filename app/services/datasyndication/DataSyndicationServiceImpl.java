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

import javax.inject.Inject;
import javax.inject.Singleton;

import play.Configuration;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.F.Promise;
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

    private IEchannelService echannelService;

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
     */
    @Inject
    public DataSyndicationServiceImpl(ApplicationLifecycle lifecycle, Configuration configuration, IEchannelService echannelService) {

        Logger.info("SERVICE>>> DataSyndicationServiceImpl starting...");

        this.isActive = configuration.getBoolean(Config.DATA_SYNDICATION_ACTIVE.getConfigurationKey());

        this.echannelService = echannelService;

        lifecycle.addStopHook(() -> {
            Logger.info("SERVICE>>> DataSyndicationServiceImpl stopping...");
            Logger.info("SERVICE>>> DataSyndicationServiceImpl stopped");
            return Promise.pure(null);
        });

        Logger.info("SERVICE>>> DataSyndicationServiceImpl started");
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

}
