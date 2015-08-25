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
    public String refId;
    public String name;
    public Date startDate;
    public Date endDate;
    public List<DataSyndicationAgreementItem> items;
    public Status status;
    public DataSyndicationPartner masterPartner;
    public DataSyndicationPartner slavePartner;
    public DataSyndicationApiKey apiKey;

    /**
     * The possible status.
     * 
     * @author Johann Kohler
     */
    public static enum Status {

        PENDING("primary"), ONGOING("success"), REJECTED("danger"), FINISHED("info"), SUSPENDED("warning"), CANCELLED("default");

        public String bootstrapClass;

        /**
         * Construct with the bootstrap label class.
         * 
         * @param bootstrapClass
         *            the bootstrap label class
         */
        Status(String bootstrapClass) {
            this.bootstrapClass = bootstrapClass;
        }

    }

}
