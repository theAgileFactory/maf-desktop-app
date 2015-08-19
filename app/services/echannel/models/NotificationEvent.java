package services.echannel.models;

/**
 * A notification event to notify.
 * 
 * @author Johann Kohler
 *
 */
public class NotificationEvent {

    public RecipientsDescriptor recipientsDescriptor;

    public String title;

    public String message;

    public String actionLink;

    /**
     * Default constructor.
     */
    public NotificationEvent() {
    }

}
