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

import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import dao.pmo.ActorDao;
import framework.services.ServiceStaticAccessor;
import framework.services.job.IJobDescriptor;
import framework.utils.Msg;
import models.framework_models.account.NotificationCategory;
import models.framework_models.account.NotificationCategory.Code;
import modules.StaticAccessor;
import play.Logger;
import services.datasyndication.IDataSyndicationService;
import services.datasyndication.IDataSyndicationService.DataSyndicationPostDataException;
import services.datasyndication.models.DataSyndicationAgreement;
import services.datasyndication.models.DataSyndicationAgreementLink;
import services.echannel.IEchannelService;
import services.echannel.models.NotificationEvent;
import services.licensesmanagement.ILicensesManagementService;

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
            ILicensesManagementService licensesManagementService = StaticAccessor.getLicensesManagementService();
            licensesManagementService.updateConsumedPortfolioEntries();
            licensesManagementService.updateConsumedUsers();
            licensesManagementService.updateConsumedStorage();

        }

        @Override
        public String getTriggerUrl() {
            return null;
        }

    }

    /**
     * Send notification events.
     * 
     * @author Johann Kohler
     * 
     */
    class SendNotificationEventsJobDescriptor implements IJobDescriptor {

        @Inject
        private IEchannelService echannelService;

        @Inject
        private ILicensesManagementService licensesManagementService;

        @Override
        public String getId() {
            return "SendNotificationEvents";
        }

        @Override
        public String getName(String languageCode) {
            return "Send the notification events";
        }

        @Override
        public String getDescription(String languageCode) {
            return "Send the notification eventy to notify provided by eChannel.";
        }

        @Override
        public Frequency getFrequency() {
            return Frequency.HOURLY;
        }

        @Override
        public int getStartHour() {
            return 0;
        }

        @Override
        public int getStartMinute() {
            return 5;
        }

        @Override
        public void trigger() {

            if (licensesManagementService.isActive()) {

                Logger.info("start trigger " + this.getId());

                try {

                    List<NotificationEvent> notificationEvents = echannelService.getNotificationEventsToNotify();
                    for (NotificationEvent notificationEvent : notificationEvents) {

                        switch (notificationEvent.recipientsDescriptor.type) {
                        case ACTORS:
                            for (Long actorId : notificationEvent.recipientsDescriptor.actors) {
                                ActorDao.sendNotificationWithContent(ActorDao.getActorById(actorId), NotificationCategory.getByCode(Code.INFORMATION),
                                        notificationEvent.actionLink, notificationEvent.title, notificationEvent.message);
                            }
                            break;
                        case PERMISSIONS:
                            for (String permission : notificationEvent.recipientsDescriptor.permissions) {
                                ServiceStaticAccessor.getNotificationManagerPlugin().sendNotificationWithPermission(permission,
                                        NotificationCategory.getByCode(Code.INFORMATION), notificationEvent.title, notificationEvent.message,
                                        notificationEvent.actionLink);
                            }
                            break;
                        case PRINCIPALS:
                            for (String uid : notificationEvent.recipientsDescriptor.principals) {
                                ActorDao.sendNotificationWithContent(uid, NotificationCategory.getByCode(Code.INFORMATION), notificationEvent.actionLink,
                                        notificationEvent.title, notificationEvent.message);
                            }
                            break;
                        default:
                            break;

                        }
                    }

                } catch (Exception e) {
                    Logger.error(this.getId() + " unexpected error", e);
                }

                Logger.info("end trigger " + this.getId());

            }

        }

        @Override
        public String getTriggerUrl() {
            return null;
        }

    }

    /**
     * Activate PENDING_INSTANCE agreement.
     * 
     * This job is executed ones, 5 minutes after application is started.
     * 
     * @author Johann Kohler
     * 
     */
    class ActivatePendingInstanceAgreementJobDescriptor implements IJobDescriptor {

        @Inject
        private IDataSyndicationService dataSyndicationService;

        @Override
        public String getId() {
            return "ActivatePendingInstanceAgreement";
        }

        @Override
        public String getName(String languageCode) {
            return "Activate PENDING_INSTANCE agreement";
        }

        @Override
        public String getDescription(String languageCode) {
            return "Activate the PENDING_INSTANCE agreement if it exist.";
        }

        @Override
        public Frequency getFrequency() {
            return Frequency.ONE_TIME;
        }

        @Override
        public int getStartHour() {
            return 0;
        }

        @Override
        public int getStartMinute() {
            return 0;
        }

        @Override
        public void trigger() {

            if (dataSyndicationService.isActive()) {

                Logger.info("start trigger " + this.getId());

                try {

                    List<DataSyndicationAgreement> agreements = dataSyndicationService.getAgreementsAsSlave();
                    for (DataSyndicationAgreement agreement : agreements) {
                        if (agreement.status.equals(DataSyndicationAgreement.Status.PENDING_INSTANCE)) {
                            dataSyndicationService.acceptAgreement(agreement);
                        }
                    }

                } catch (Exception e) {
                    Logger.error(this.getId() + " unexpected error", e);
                }

                Logger.info("end trigger " + this.getId());

            }

        }

        @Override
        public String getTriggerUrl() {
            return null;
        }

    }

    /**
     * Synchronize data syndication.
     * 
     * @author Johann Kohler
     * 
     */
    class SynchronizeDataSyndicationJobDescriptor implements IJobDescriptor {

        @Inject
        private IDataSyndicationService dataSyndicationService;

        @Override
        public String getId() {
            return "SynchronizeDataSyndication";
        }

        @Override
        public String getName(String languageCode) {
            return "Synchronize data syndication.";
        }

        @Override
        public String getDescription(String languageCode) {
            return "Push to all slave partner instance the data to synchronize.";
        }

        @Override
        public Frequency getFrequency() {
            return Frequency.DAILY;
        }

        @Override
        public int getStartHour() {
            Random rand = new Random();
            int randomNum = rand.nextInt(3) + 2; // [2,4]
            return randomNum;
        }

        @Override
        public int getStartMinute() {
            Random rand = new Random();
            int randomNum = rand.nextInt(60); // [0,59]
            return randomNum;
        }

        @Override
        public void trigger() {

            Logger.info("start trigger " + this.getId());

            List<DataSyndicationAgreementLink> agreementLinks = null;
            try {
                agreementLinks = dataSyndicationService.getAgreementLinksToSynchronize();
            } catch (Exception e) {
                Logger.error("error with dataSyndicationService.getAgreementLinksToSynchronize", e);
            }

            if (agreementLinks != null) {

                for (DataSyndicationAgreementLink agreementLink : agreementLinks) {
                    try {
                        dataSyndicationService.postData(agreementLink);
                    } catch (DataSyndicationPostDataException e) {
                        Logger.warn("postData for agreement link [id=" + agreementLink.id + ", agreementId=" + agreementLink.agreement.id + ", dataType="
                                + agreementLink.dataType + ", masterObjectId=" + agreementLink.masterObjectId + ", slaveObjectId="
                                + agreementLink.slaveObjectId + "]");
                        Logger.error(Msg.get(e.getCode().getMessageKey()));
                    }
                }

            }

            Logger.info("end trigger " + this.getId());

        }

        @Override
        public String getTriggerUrl() {
            return null;
        }

    }

}
