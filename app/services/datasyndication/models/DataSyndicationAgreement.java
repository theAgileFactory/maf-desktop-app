package services.datasyndication.models;

import java.util.Date;
import java.util.List;

/**
 * An agreement for data syndication.
 * 
 * @author Johann Kohler
 *
 */
public class DataSyndicationAgreement {

    public Long id;
    public Date creationDate;
    public String refId;
    public String name;
    public Date startDate;
    public Date endDate;
    public List<DataSyndicationAgreementItem> items;
    public Status status;
    public DataSyndicationPartner masterPartner;
    public DataSyndicationPartner slavePartner;
    public String slaveEmail;
    public DataSyndicationApiKey apiKey;

    /**
     * The possible status.
     * 
     * @author Johann Kohler
     */
    public static enum Status {

        PENDING(true, "primary"), PENDING_INSTANCE(false, "primary"), ONGOING(true, "success"), REJECTED(true, "danger"), FINISHED(true,
                "info"), SUSPENDED(true, "warning"), CANCELLED(true, "default");

        public boolean displayAsLegend;
        public String bootstrapClass;

        /**
         * Construct with the bootstrap label class.
         * 
         * @param displayAsLegend
         *            set to true if the status should be displayed in the
         *            legends list
         * @param bootstrapClass
         *            the bootstrap label class
         */
        Status(boolean displayAsLegend, String bootstrapClass) {
            this.displayAsLegend = displayAsLegend;
            this.bootstrapClass = bootstrapClass;
        }

    }

}
