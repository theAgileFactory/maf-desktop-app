package services.echannel.models;

import java.util.List;

/**
 * A recipients descriptor for a notification event.
 * 
 * @author Johann Kohler
 *
 */
public class RecipientsDescriptor {

    public Type type;

    public List<String> permissions;

    public List<Long> actors;

    public List<String> principals;

    /**
     * Default constructor.
     */
    public RecipientsDescriptor() {
    }

    /**
     * The possible types.
     * 
     * @author Johann Kohler
     */
    public static enum Type {
        PERMISSIONS, ACTORS, PRINCIPALS;
    }

}
