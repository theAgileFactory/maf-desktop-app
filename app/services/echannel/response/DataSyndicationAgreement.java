package services.echannel.response;

import java.util.Date;
import java.util.List;

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
    public String masterDomain;
    public String slaveDomain;
    public ApiRegistration apiKey;

    /**
     * The possible status.
     * 
     * @author Johann Kohler
     */
    public static enum Status {
        PENDING, ONGOING, REJECTED, FINISHED, SUSPENDED, CANCELLED;
    }

}
