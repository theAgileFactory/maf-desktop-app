package services.datasyndication.models;

import java.util.Date;
import java.util.List;

import framework.utils.Msg;
import models.framework_models.api.ApiRegistration;

/**
 * The API key of a slave instance.
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
    public ApiRegistration apiKey;

    /**
     * The possible status.
     * 
     * @author Johann Kohler
     */
    public static enum Status {

        PENDING("warning"), ONGOING("success"), REJECTED("danger"), FINISHED("default"), SUSPENDED("warning"), CANCELLED("default");

        private String bootstrapClass;

        /**
         * Construct with the bootstrap label class.
         * 
         * @param bootstrapClass
         *            the bootstrap label class
         */
        Status(String bootstrapClass) {
            this.bootstrapClass = bootstrapClass;
        }

        /**
         * Render the status.
         */
        public String render() {
            return "<span class=\"label label-" + bootstrapClass + "\">" + Msg.get("object.data_syndication_agreement.status." + name() + ".label")
                    + "</span>";
        }

    }

}
