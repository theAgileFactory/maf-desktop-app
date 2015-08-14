package utils;

import play.mvc.Http;
import services.datasyndication.IDataSyndicationService;

/**
 * Provide utilities for templates.
 * 
 * @author Johann Kohler
 *
 */
public class Tpl {

    /**
     * Get the data syndication service.
     * 
     * Note: it has been manually add in the MafHttpRequestHandler.
     */
    public static IDataSyndicationService getDataSyndicationService() {
        return (IDataSyndicationService) Http.Context.current().args.get(IDataSyndicationService.NAME);
    }

}
