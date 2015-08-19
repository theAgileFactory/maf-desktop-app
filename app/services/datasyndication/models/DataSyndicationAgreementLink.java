package services.datasyndication.models;

import java.util.List;

import framework.utils.Msg;

/**
 * Define an agreement link for data syndication.
 * 
 * A link represents the relation between master and slave objects.
 * 
 * @author Johann Kohler
 *
 */
public class DataSyndicationAgreementLink {

    public Long id;
    public String masterPrincipalUid;
    public String name;
    public String description;
    public DataSyndicationAgreement agreement;
    public List<DataSyndicationAgreementItem> items;
    public String dataType;
    public Long masterObjectId;
    public Long slaveObjectId;
    public Status status;

    /**
     * The possible status.
     * 
     * @author Johann Kohler
     */
    public static enum Status {
        PENDING("warning"), ONGOING("success"), REJECTED("danger"), CANCELLED("default");

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
         * Get the bootstrap class.
         */
        public String getBootstrapClass() {
            return this.bootstrapClass;
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
