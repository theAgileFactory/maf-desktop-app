package services.datasyndication.models;

import java.util.List;

import services.datasyndication.models.DataSyndicationAgreement.Status;

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
    private Status status;

    /**
     * Get the status.
     */
    public Status getStatus() {
        if (this.status.equals(DataSyndicationAgreement.Status.CANCELLED) || this.status.equals(DataSyndicationAgreement.Status.REJECTED)
                || this.agreement.status.equals(DataSyndicationAgreement.Status.ONGOING)) {
            return this.status;
        } else {
            return this.agreement.status;
        }

    }

}
