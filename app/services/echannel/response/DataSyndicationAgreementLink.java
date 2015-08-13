package services.echannel.response;

import java.util.List;

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
    public DataSyndicationAgreement agreement;
    public List<DataSyndicationAgreementItem> items;
    public String dataType;
    public Long masterObjectId;
    public Long slaveMasterId;
    public Status status;

    /**
     * The possible status.
     * 
     * @author Johann Kohler
     */
    public static enum Status {
        PENDING, ONGOING, REJECTED, CANCELLED;
    }

}
