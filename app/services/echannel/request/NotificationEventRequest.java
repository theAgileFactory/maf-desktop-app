package services.echannel.request;

import services.echannel.models.RecipientsDescriptor;

/**
 * The content for the createNotificationEvent request.
 * 
 * @author Johann Kohler
 *
 */
public class NotificationEventRequest {

    public String domain;

    public RecipientsDescriptor recipientsDescriptor;

    public String title;

    public String message;

    public String actionLink;

    /**
     * Default constructor.
     */
    public NotificationEventRequest() {
    }

}
