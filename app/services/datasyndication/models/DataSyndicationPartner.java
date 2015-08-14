package services.datasyndication.models;

/**
 * Define a partner for data syndication.
 * 
 * @author Johann Kohler
 *
 */
public class DataSyndicationPartner {

    // from instance
    public String domain;

    // from customer
    public Byte[] customerLogo;
    public String customerName;
    public String customerDescription;
    public String customerWebsite;

    // from contact
    public String contactName;
    public String contactEmail;

}
