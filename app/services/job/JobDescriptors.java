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
package services.job;

import framework.services.ServiceManager;
import framework.services.job.IJobDescriptor;
import services.licensesmanagement.LicensesManagementServiceImpl;

/**
 * All available job descriptors.
 * 
 * @author Johann Kohler
 */
public interface JobDescriptors {

    /**
     * The UpdateConsumedLicenses job descriptor.
     * 
     * @author Johann Kohler
     * 
     */
    class UpdateConsumedLicensesJobDescriptor implements IJobDescriptor {

        @Override
        public String getId() {
            return "UpdateConsumedLicenses";
        }

        @Override
        public String getName(String languageCode) {
            return "Update the consumed licenses";
        }

        @Override
        public String getDescription(String languageCode) {
            return "Update the consumed licenses (active portfolio entries, users and storage) in eChannel.";
        }

        @Override
        public Frequency getFrequency() {
            return Frequency.DAILY;
        }

        @Override
        public int getStartHour() {
            return 2;
        }

        @Override
        public int getStartMinute() {
            return 0;
        }

        @Override
        public void trigger() {

            ServiceManager.getService(LicensesManagementServiceImpl.NAME, LicensesManagementServiceImpl.class).updateConsumedPortfolioEntries();
            ServiceManager.getService(LicensesManagementServiceImpl.NAME, LicensesManagementServiceImpl.class).updateConsumedUsers();
            ServiceManager.getService(LicensesManagementServiceImpl.NAME, LicensesManagementServiceImpl.class).updateConsumedStorage();

        }

        @Override
        public String getTriggerUrl() {
            return null;
        }

    }

}
