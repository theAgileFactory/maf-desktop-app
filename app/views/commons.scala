package views.html

/**
 * This class provides a few scala utilities to be reused in various
 * parts
 */
object commons extends framework.handlers.ViewsInjector{
	//-----------------------------------------------------------------------------------
	// Injection of common services into the scala templates
	// WARNING: this assumes that these services have been injected into the Http.Context
	// otherwise the values will be null.
	// Please see framework.handlers.AbstractRequestHandler
	//-----------------------------------------------------------------------------------
	val _dataSyndication=play.mvc.Http.Context.current().args.get(classOf[services.datasyndication.IDataSyndicationService].getName).asInstanceOf[services.datasyndication.IDataSyndicationService]
}