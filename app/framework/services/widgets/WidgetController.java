package framework.services.widgets;

import framework.services.ext.ILinkGenerationService;
import framework.services.ext.api.AbstractExtensionController;
import framework.services.ext.api.WebCommandPath;
import framework.services.ext.api.WebParameter;
import play.libs.F.Promise;
import play.mvc.Result;

public abstract class WidgetController extends AbstractExtensionController {

    public WidgetController(ILinkGenerationService linkGenerationService) {
        super(linkGenerationService);
    }

    @WebCommandPath(id = WebCommandPath.DEFAULT_COMMAND_ID, path = WebCommandPath.DEFAULT_COMMAND_PATH + "/:template" +"/:id")
    public Promise<Result> index(@WebParameter(name = "template") String template, @WebParameter(name = "id") Long widgetId) {
        return display(widgetId);
    }
    
    /**
     * This method is called by the default method for the registration
     * controller.<br/>
     * <b>No need to mark it with</b>:
     * 
     * <pre>
     * {@code
     * &#64;WebCommandPath
     * }
     * 
     * </pre>
     * 
     * @param widgetId
     *            the widget id which is referencing the configuration to be used by the controller
     * @return a result
     */
    public abstract Promise<Result> display(Long widgetId);
}
