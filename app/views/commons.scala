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
    val _echannelService=play.mvc.Http.Context.current().args.get(classOf[services.echannel.IEchannelService].getName).asInstanceOf[services.echannel.IEchannelService]
    val _securityService=play.mvc.Http.Context.current().args.get(classOf[framework.security.ISecurityService].getName).asInstanceOf[framework.security.ISecurityService]
    val _notificationService=play.mvc.Http.Context.current().args.get(classOf[framework.services.notification.INotificationManagerPlugin].getName).asInstanceOf[framework.services.notification.INotificationManagerPlugin]
}