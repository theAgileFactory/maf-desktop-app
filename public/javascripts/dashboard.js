/**
 * The service which is managing the dashboard pages view and edition
 */


/**
 * Display an error message in the message area of the widget
 * - widgetId : the unique id of the widget
 * - message : the success message
 */
function bizdock_widget_displaySuccessMessage(widgetId, message){
	var msg='<div class="alert alert-success alert-dismissible" role="alert"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>';
	msg=msg+message;
	msg=msg+'</div>';
	$('#maf_widget_widget_id_'+widgetId).find('._maf_widget_widget_message').html(msg);
}

/**
 * Display an error message in the message area of the widget
 * - widgetId : the unique id of the widget
 * - message : the error message
 */
function bizdock_widget_displayErrorMessage(widgetId, message){
	var msg='<div class="alert alert-danger alert-dismissible" role="alert"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>';
	msg=msg+message;
	msg=msg+'</div>';
	$('#maf_widget_widget_id_'+widgetId).find('._maf_widget_widget_message').html(msg);
}

/**
 * Register an event listener for a widget.<br/>
 * This method is to be called by the widget developer.
 */
function bizdock_widget_addEventListener(widgetId, eventListener){
	$('#maf_widget_widget_id_'+widgetId).ready(function (){
		$('#maf_widget_widget_id_'+widgetId).on('maf.event.widget.'+widgetId, eventListener);
		$('#maf_widget_widget_id_'+widgetId+' ._maf_widget_widget_link').click(function(event){
			event.preventDefault();
			var sourceElement=$(this).get();
			_maf_widget_sendLinkEvent(widgetId, sourceElement);
		});
	});
}

/**
 * A method to send an edition event (when the Edit button is pressed)
 * @param widgetId a widget id
 */
function _maf_widget_sendEditEvent(widgetId){
	var e = jQuery.Event('maf.event.widget.'+widgetId, { nature: "EDIT"} );
	$('#maf_widget_widget_id_'+widgetId).trigger(e);
}

/**
 * A method to send a link event (when a link of class _maf_widget_widget_link is pressed)
 * @param widgetId a widget id
 * @param sourceElement the DOM element which triggered the event
 */
function _maf_widget_sendLinkEvent(widgetId, sourceElement){
	var e = jQuery.Event('maf.event.widget.'+widgetId, { nature: "ACTION", source:  sourceElement} );
	$('#maf_widget_widget_id_'+widgetId).trigger(e);
}

/**
 * The main object which manages the dashboard system
 */
function _maf_widget_dashboardService(dashboardPageId, configurationUrl, errorUrl){
	//The URL to be redirected to if something harmfull and unexpected happen
	this.errorUrl=errorUrl;
	//The URL to get the configuration of the dashboard page
	this.configurationUrl=configurationUrl;
	//The dashbord page id
	this.dashboardPageId=dashboardPageId;
	//Is the page in edition mode
	this.editionMode=false;
	//The maximum number of rows which can be added to a dashboard page
	this.maxNumberOfRows=0;
	//The image to be displayed when waiting for a content
	this.ajaxWaitImage="";
	//An error message to be displayed when a widget cannot be loaded
	this.unableToLoadWidgetErrorMessage="";
	//The message to be displayed to confirm the removal of a widget
	this.confirmWidgetRemoveMessage="";
	//The message to be displayed to confirm the removal of a dashboard row
	this.confirmDashboardRowRemoveMessage="";
	//The message to be displayed when the widget catalog cannot be loaded
	this.unableToLoadWidgetCatalogErrorMessage="";
	//The message to be displayed when the user attempt to remove the last row
	this.cannotDeleteTheLastRowMessage="";
	//The title of the WARNING message boxes
	this.warningMessageBoxTitleMessage="";
	//The maximum number of rows for the current page has been reached
	this.maxNumberOfRowReachedMessage="";
	//Unexpected error message
	this.unexpectedErrorMessage="";
	//Message displayed to confirm that the current page must be deleted
	this.confirmCurrentPageRemoveMessage="";
	//The array which contains the dashboard page configuration
	this.dashboardData=[];
	//The first part of the message to be displayed in "empty" widget areas
	this.dragWidgetMessage="";
	//The second part of the message to be displayed in "empty" widget areas
	this.addANewWidgetMessage="";
	//The URL to request the creation of a new empty dashboard row (passing the requested number of columns)
	this.createNewRowAjaxServiceUrl="";
	//The URL to ask the creation of a new widget from a widget identifier
	this.createNewWidgetAjaxServiceUrl="";
	//The URL to ask updating the dashboard page
	this.updateDashboardPageAjaxServiceUrl="";
	//The URL to get the widget catalog
	this.widgetCatalogServiceUrl="";
	//The URL to remove the current dashboard page
	this.removeCurrentDashboardPageServiceUrl="";
	//The URL to add a new dashboard page
	this.addNewDashboardPageServiceUrl="";
	//The URL to display a dashboard page
	this.displayDashboardPageServiceUrl="";
	//The URL to change the home page status of the current page
	this.setAsHomePageServiceUrl="";
	//The URL to rename a page
	this.renamePageServiceUrl="";
	//The URL to retrieve an error widget
	this.errorWidgetServiceUrl="";
	//An object which stores the widget catalog (indexed by widget identifier)
	this.loadedWidgetCatalog=[];
	
	/**
	 * Load the dashboard configuration from the server and call the provided callback
	 */
	this.refresh=function(callback){
		var currentObject=this;
		var jqxhr = $.get(currentObject.configurationUrl, function(data) {
			currentObject.maxNumberOfRows=data.maxNumberOfRows;
			currentObject.createNewRowAjaxServiceUrl=data.createNewRowAjaxServiceUrl;
			currentObject.createNewWidgetAjaxServiceUrl=data.createNewWidgetAjaxServiceUrl;
			currentObject.updateDashboardPageAjaxServiceUrl=data.updateDashboardPageAjaxServiceUrl;
			currentObject.widgetCatalogServiceUrl=data.widgetCatalogServiceUrl;
			currentObject.removeCurrentDashboardPageServiceUrl=data.removeCurrentDashboardPageServiceUrl;
			currentObject.addNewDashboardPageServiceUrl=data.addNewDashboardPageServiceUrl;
			currentObject.displayDashboardPageServiceUrl=data.displayDashboardPageServiceUrl;
			currentObject.setAsHomePageServiceUrl=data.setAsHomePageServiceUrl;
			currentObject.renamePageServiceUrl=data.renamePageServiceUrl;
			currentObject.ajaxWaitImage=data.ajaxWaitImage;
			currentObject.unableToLoadWidgetErrorMessage=data.unableToLoadWidgetErrorMessage;
			currentObject.confirmWidgetRemoveMessage=data.confirmWidgetRemoveMessage;
			currentObject.confirmDashboardRowRemoveMessage=data.confirmDashboardRowRemoveMessage;
			currentObject.dragWidgetMessage=data.dragWidgetMessage;
			currentObject.addANewWidgetMessage=data.addANewWidgetMessage;
			currentObject.cannotDeleteTheLastRowMessage=data.cannotDeleteTheLastRowMessage;
			currentObject.warningMessageBoxTitleMessage=data.warningMessageBoxTitleMessage;
			currentObject.maxNumberOfRowReachedMessage=data.maxNumberOfRowReachedMessage;
			currentObject.unexpectedErrorMessage=data.unexpectedErrorMessage;
			currentObject.confirmCurrentPageRemoveMessage=data.confirmCurrentPageRemoveMessage;
			currentObject.dashboardData=data.dashboardData;
			currentObject.errorWidgetServiceUrl=data.errorWidgetServiceUrl;
			
			for(var widgetCatalogIndex=0; widgetCatalogIndex < data.widgetCatalog.length; widgetCatalogIndex++){
				var widgetCatalogEntry=data.widgetCatalog[widgetCatalogIndex];
				currentObject.loadedWidgetCatalog[widgetCatalogEntry.identifier]=widgetCatalogEntry;
			}

			callback();
		}).fail(function() {
			window.location.replace(currentObject.errorUrl+"REFRESH");
		});	
	};

	
	/**
	 * Return the widget URL associated with the specified id
	 */
	this.getWidgetUrlFromId=function(widgetId){
		for(var rowCount=0; rowCount<this.dashboardData.length; rowCount++){
			var widgets=this.dashboardData[rowCount].widgets;
			for(var widgetCount=0; widgetCount<widgets.length; widgetCount++){
				var widget=widgets[widgetCount];
				if(widget.id==widgetId){
					return widget.url;
				}
			}
		}
		return "#";
	}
	
	/**
	 * Set the place holder message into the specified widget container
	 * widgetAreaElement : the widget area in which the placeholder (empty) will be set
	 */
	this.setPlaceHolderMessage=function(widgetAreaElement){
		var template='<div class="_maf_widget_widget_placeholder"><br/><br/><h4 class="text-center">'+this.dragWidgetMessage+' <a class="_maf_widget_add" href="#">'+this.addANewWidgetMessage+'</a></h4><br/><br/></div>';
		widgetAreaElement.html(template);
		$('._maf_widget_add').off('click');
		$('._maf_widget_add').click(function(event){
			event.preventDefault();
			var target = $( event.target );
			var widgetAreaElementWhenClicked=target.closest("._maf_widget_widget_area");
			_maf_widget_openWidgetSelector(widgetAreaElementWhenClicked);
		});
	};
	
	/**
	 * Switch from edition mode to display mode
	 */
	this.toggleEditonMode=function(){
		
		if(this.editionMode){
			_maf_widget_disableDashboardEdition();
			$("#_maf_widget_edition_mode").removeClass('btn btn-warning').addClass('btn btn-primary').html('<i class="fa fa-cog"></i>&nbsp;Edit page');
			this.editionMode=false;
		}else{
			_maf_widget_activateDashboardEdition();
			$("#_maf_widget_edition_mode").removeClass('btn btn-primary').addClass('btn btn-warning').html('<i class="fa fa-play"></i>&nbsp;Display page');
			this.editionMode=true;
		}
		
		var editionMode = this.editionMode;
		var rowIndex=0;
		$("._maf_widget_dashboard_row").each(function(){
			var widgetIndex=0;
			var rowConfig=_dashboardServiceInstance.dashboardData[rowIndex];
			$(this).children("._maf_widget_widget_area").each(function(){
				var widget=rowConfig.widgets[widgetIndex];
				if(widget.id!=-1){
					_maf_widget_toggleEdition(editionMode, $(this), widget.identifier);
				}
				widgetIndex++;
			});
			rowIndex++;
		});

	}
	
	/**
	 * Update the dashboard data when swaping a widget with another one (or an empty area)
	 * widgetElementA : the moved widget
	 * widgetElementB : another widget or an empty area (placeholder)
	 */
	this.swapWidget=function(widgetElementA, widgetElementB){
		var widgetLocationA=this.findWidgetLocation(widgetElementA);
		var selectedRowIndexA=widgetLocationA.selectedRowIndex;
		var selectedWidgetIndexA=widgetLocationA.selectedWidgetIndex;
		
		var widgetLocationB=this.findWidgetLocation(widgetElementB);
		var selectedRowIndexB=widgetLocationB.selectedRowIndex;
		var selectedWidgetIndexB=widgetLocationB.selectedWidgetIndex;
		
		var widgetIdA=this.dashboardData[selectedRowIndexA].widgets[selectedWidgetIndexA];
		var widgetIdB=this.dashboardData[selectedRowIndexB].widgets[selectedWidgetIndexB];
		
		this.dashboardData[selectedRowIndexA].widgets[selectedWidgetIndexA]=widgetIdB;
		this.dashboardData[selectedRowIndexB].widgets[selectedWidgetIndexB]=widgetIdA;
		
		this.updateDashboard();
	};
	
	/**
	 * Update the dashboard data when adding a widget
	 * widgetElement : the widget content element
	 * widgetId : the newly created widget id
	 * widgetIdentifier : the identifier of the widget catalog entry
	 */
	this.addWidget=function(widgetElement, widgetId, widgetUrl, widgetIdentifier){
		var widgetLocationA=this.findWidgetLocation(widgetElement);
		var selectedRowIndex=widgetLocationA.selectedRowIndex;
		var selectedWidgetIndex=widgetLocationA.selectedWidgetIndex;
		if(selectedWidgetIndex!=-1){
			this.dashboardData[selectedRowIndex].widgets[selectedWidgetIndex]={id: widgetId, url: widgetUrl, identifier: widgetIdentifier};
		}
		this.updateDashboard();
	};
	
	/**
	 * Update the dashboard data when removing a widget
	 * widgetElement : the widget content element
	 */
	this.removeWidget=function(widgetElement){
		var widgetLocation=this.findWidgetLocation(widgetElement);
		var selectedRowIndex=widgetLocation.selectedRowIndex;
		var selectedWidgetIndex=widgetLocation.selectedWidgetIndex;
		if(selectedWidgetIndex!=-1){
			this.dashboardData[selectedRowIndex].widgets[selectedWidgetIndex]={id : -1};
		}
		this.updateDashboard();
	};
	
	/**
	 * Update the dashboard data when adding a row
	 */
	this.addRow=function(dashboardRowElement, templateIdentifier){
		var numberOfColumns=templateIdentifier.substring(templateIdentifier.indexOf('_')+1);
		var selectedRowIndex=this.findRowIndex(dashboardRowElement);
		if(selectedRowIndex!=-1){
			var widgetArray=[];
			for(var widgetCount=0; widgetCount<numberOfColumns; widgetCount++){
				widgetArray.push({id : -1, url : null});
			}
			var newRowData={layout : templateIdentifier, widgets : widgetArray};
			this.dashboardData.splice(selectedRowIndex+1, 0, newRowData);
		}
		this.updateDashboard();
	};
	
	/**
	 * Update the dashboard data when removing a row
	 */
	this.removeRow=function(dashboardRowElement){
		var selectedRowIndex=this.findRowIndex(dashboardRowElement);
		if(selectedRowIndex!=-1){
			this.dashboardData.splice(selectedRowIndex, 1);
		}
		this.updateDashboard();
	};
	
	/**
	 * Call an AJAX service to "post" the update of the dasboard page configuration
	 */
	this.updateDashboard=function(){
		maf_performPostJsonReceiveJson(
				this.updateDashboardPageAjaxServiceUrl,
				JSON.stringify(this.dashboardData),
				function(){},
				function(){});
	};
	
	/**
	 * Find the index of the specified row in the dashboard page
	 */
	this.findRowIndex=function(dashboardRowElement){
		var rowIndex=0;
		var selectedRowIndex=-1;
		$("._maf_widget_dashboard_row").each(function(){
			if(dashboardRowElement.is($(this))){
				selectedRowIndex=rowIndex;
			}
			rowIndex++;
		});
		return selectedRowIndex;
	};
	
	/**
	 * Find the specified widget element in the dashboard page
	 */
	this.findWidgetLocation=function(widgetElement){
		var selectedRowIndex=-1;
		var selectedWidgetIndex=-1;
		var rowIndex=0;
		$("._maf_widget_dashboard_row").each(function(){
			var widgetIndex=0;
			$(this).find("._maf_widget_widget_area").each(function(){
				var localWidgetContent=$(this).children(":first");
				if(widgetElement.is(localWidgetContent)){
					selectedRowIndex=rowIndex;
					selectedWidgetIndex=widgetIndex;
				}
				widgetIndex++;
			});
			rowIndex++;
		});
		return  {"selectedRowIndex" : selectedRowIndex, "selectedWidgetIndex" : selectedWidgetIndex};
	};
}

/**
 * Remove the current dashboard page
 */
function _maf_widget_removeCurrentDashboardPage(){
	_maf_widget_ConfirmationBox(
		_dashboardServiceInstance.warningMessageBoxTitleMessage,
		_dashboardServiceInstance.confirmCurrentPageRemoveMessage,
		function(){
			var jqxhr = $.get(_dashboardServiceInstance.removeCurrentDashboardPageServiceUrl, function(data) {
				window.location.replace(_dashboardServiceInstance.displayDashboardPageServiceUrl);//Redirect to the default page
			}).fail(function(){
				_maf_widget_unexpectedErrorRefreshPage();
			});
		});
}

/**
 * Set the current page as home page
 */
function _maf_widget_setCurrentPageAsHomePage(){
	var jqxhr = $.get(_dashboardServiceInstance.setAsHomePageServiceUrl, function(data) {
		window.location.replace(_dashboardServiceInstance.displayDashboardPageServiceUrl);
	}).fail(function(){
		_maf_widget_unexpectedErrorRefreshPage();
	});
}

/**
 * Rename the current page according to the specified name
 */
function _maf_widget_openRenameDashboardPageForm(newName){	
	$("#_maf_widget_RenamePage_Name").val('');
	$("#_maf_widget_RenamePageOKButton").prop('disabled', false);
	$("#_maf_widget_RenamePageCloseButton").prop('disabled', false);
	$("#_maf_widget_RenamePageForm").modal('show');
	$("#_maf_widget_RenamePageOKButton").off('click');
	$("#_maf_widget_RenamePageOKButton").click(function(){
		var newName=$("#_maf_widget_RenamePage_Name").val();
		if(newName.length>0){
			$("#_maf_widget_RenamePage_Name").closest(".form-group").removeClass('has-error');
			$("#_maf_widget_RenamePageOKButton").prop('disabled', true);
			$("#_maf_widget_RenamePageCloseButton").prop('disabled', true);
			
			var jqxhr = $.get(_dashboardServiceInstance.renamePageServiceUrl+newName, function(data) {
				$("#_maf_widget_RenamePageForm").modal('hide');
				window.location.replace(_dashboardServiceInstance.displayDashboardPageServiceUrl);
			}).fail(function(){
				_maf_widget_unexpectedErrorRefreshPage();
			});
		}else{
			$("#_maf_widget_RenamePage_Name").closest(".form-group").addClass('has-error');
		}
	});
}

/**
 * Add a new page
 */
function _maf_widget_openNewDashboardPageForm(){
	$("#_maf_widget_AddNewPage_Name").val('');
	$("#_maf_widget_AddNewPage_isHome").attr('checked', false);
	$("#_maf_widget_AddNewPageOKButton").prop('disabled', false);
	$("#_maf_widget_AddNewPageCloseButton").prop('disabled', false);
	$("#_maf_widget_AddNewPageForm").modal('show');
	$("#_maf_widget_AddNewPageOKButton").off('click');
	$("#_maf_widget_AddNewPageOKButton").click(function(){
		var newPageConfig={};
		newPageConfig.name=$("#_maf_widget_AddNewPage_Name").val();
		newPageConfig.isHome=$("#_maf_widget_AddNewPage_isHome").is(':checked');
		if(newPageConfig.name.length>0){
			$("#_maf_widget_AddNewPage_Name").closest(".form-group").removeClass('has-error');
			$("#_maf_widget_AddNewPageOKButton").prop('disabled', true);
			$("#_maf_widget_AddNewPageCloseButton").prop('disabled', true);
			maf_performPostJsonReceiveJson(
				_dashboardServiceInstance.addNewDashboardPageServiceUrl,
				JSON.stringify(newPageConfig),
				function(data){
					$("#_maf_widget_AddNewPageForm").modal('hide');
					window.location.replace(_dashboardServiceInstance.displayDashboardPageServiceUrl+"?id="+data.id);
				},
				function(){
					_maf_widget_unexpectedErrorRefreshPage();
				});
		}else{
			$("#_maf_widget_AddNewPage_Name").closest(".form-group").addClass('has-error');
		}
	});
}

/**
 * Change the widget state to edition mode or display mode
 * isEditionMode : true if the widget is to be set in "edition mode" (the configuration button is visible)
 * widgetAreaElement: the dom container of the widget (jQuery form)
 * widgetCatalogEntryIdentifer: the identifier of the widget catalog entry (for example WG3)
 */
function _maf_widget_toggleEdition(isEditionMode, widgetAreaElement, widgetCatalogEntryIdentifer){

	/**
	 * true if the widget has an edit mode (meaning a configuration mode).
	 * 
	 * Note: do not be confused with the isEditionMode which means that the current 
	 * window is in the edit mode (to add/move/delete the widgets in the panels)
	 */
	var hasEditMode = true;
	if (widgetCatalogEntryIdentifer != null) {
		hasEditMode = _dashboardServiceInstance.loadedWidgetCatalog[widgetCatalogEntryIdentifer].hasEditMode;
	}

	if(isEditionMode){
		widgetAreaElement.find("._maf_widget_widget_commands").html('<i class="_maf_widget_widget_command_trash fa fa-trash fa-lg"></i>');
		widgetAreaElement.find("._maf_widget_widget_command_trash").click(function(event){
			event.preventDefault();
			var widgetElement=$(this).closest("._maf_widget_widget");
			_maf_widget_ConfirmationBox(
					_dashboardServiceInstance.warningMessageBoxTitleMessage,
					_dashboardServiceInstance.confirmWidgetRemoveMessage,
					function(){
						_maf_widget_removeWidgetFromDashboard(widgetElement);
					});
		});
	}else if(hasEditMode){
		widgetAreaElement.find("._maf_widget_widget_commands").html('<i class="_maf_widget_widget_command_display fa fa-square-o fa-lg"></i>&nbsp;&nbsp;<i class="_maf_widget_widget_command_configure fa fa-cog fa-lg"></i>');
		widgetAreaElement.find("._maf_widget_widget_command_configure").click(function(event){
			event.preventDefault();
			var widgetElement=$(this).closest("._maf_widget_widget");
			//Format of the element id is maf_widget_widget_id_@id
			var widgetId=widgetElement.attr('id').substring("maf_widget_widget_id_".length);
			_maf_widget_sendEditEvent(widgetId);
		});
		widgetAreaElement.find("._maf_widget_widget_command_display").click(function(event){
			event.preventDefault();
			var widgetElement=$(this).closest("._maf_widget_widget");
			//Format of the element id is maf_widget_widget_id_@id
			var widgetId=widgetElement.attr('id').substring("maf_widget_widget_id_".length);
			//Find the corresponding url
			var widgetUrl=_dashboardServiceInstance.getWidgetUrlFromId(widgetId);
			var widgetAreaElement=$(this).closest("._maf_widget_widget_area");
			_maf_widget_loadWidgetContent(widgetAreaElement, widgetUrl, widgetId, function(){
				_maf_widget_toggleEdition(false, widgetAreaElement, widgetCatalogEntryIdentifer);
			});
		});
	} else {
		widgetAreaElement.find("._maf_widget_widget_commands").html('');
	}
}

/**
 * Set the dashboard page in display mode
 */
function _maf_widget_disableDashboardEdition(){
	$("#_maf_widget_addNewPage").show();
	$("#_maf_widget_removePage").hide();
	$("#_maf_widget_renamePage").hide();
	$("#_maf_widget_currentPageIsHomeContainer").hide();
	$("._maf_widget_widget_placeholder").detach();
	$("._maf_widget_dashboard_row_trash").detach();
	$("._maf_widget_dashboard_row_add").detach();
	interact('._maf_widget_draggable').draggable({enabled: false});
	interact('._maf_widget_widget_area').dropzone({enabled:false});
	$("._maf_widget_dashboard_row").toggleClass("_maf_widget_dashboard_row_activated");
}

/**
 * Set the dashboard page in edition mode
 */
function _maf_widget_activateDashboardEdition(){
	_maf_widget_addPlaceHolderToEmptyCells();
	$("#_maf_widget_addNewPage").hide();
	$("#_maf_widget_removePage").show();
	$("#_maf_widget_renamePage").show();
	$("#_maf_widget_currentPageIsHomeContainer").show();
	$("._maf_widget_dashboard_row").toggleClass("_maf_widget_dashboard_row_activated");
	$("._maf_widget_dashboard_row").prepend('<div class="_maf_widget_dashboard_row_trash"><a class="_maf_widget_dashboard_row_trash_button" href="#"><i class="fa fa-trash text-primary"></i></a></div>');
	$("._maf_widget_dashboard_row").append('<div class="_maf_widget_dashboard_row_add"><a class="_maf_widget_dashboard_row_add_button" href="#"><i class="fa fa-plus text-primary"></i></a></div>');
	$("._maf_widget_dashboard_row_trash_button").click(function(event){
		event.preventDefault();
		var rowElement=$(this).closest("._maf_widget_dashboard_row");
		_maf_widget_ConfirmationBox(
				_dashboardServiceInstance.warningMessageBoxTitleMessage,
				_dashboardServiceInstance.confirmDashboardRowRemoveMessage,
				function(){
					_maf_widget_removeDashboardRow(rowElement);
				});
	});
	$("._maf_widget_dashboard_row_add_button").click(function(event){
		event.preventDefault();
		var rowElement=$(this).closest("._maf_widget_dashboard_row");
		_maf_widget_openDashboardRowTemplateSelector(rowElement);
	});
	
	//Activate the draggability
	interact('._maf_widget_draggable').draggable({
		accept: '.widget',
		autoScroll: true,
		onmove: _maf_widget_dragMoveListener});
	//Activate the management of the drag and drop
	interact('._maf_widget_widget_area').dropzone({
		ondropactivate : function(event) {
			event.target.classList.add('_maf_widget_drop-active');
		},
		ondragenter : function(event) {
			var draggableElement = event.relatedTarget;
	        var dropzoneElement = event.target;
			dropzoneElement.classList.add('_maf_widget_drop-selected');
		},
		ondragleave : function(event) {
			var draggableElement = event.relatedTarget,
	        dropzoneElement = event.target;
			dropzoneElement.classList.remove('_maf_widget_drop-selected');
		},
		ondrop : function(event) {
			var draggableElement = event.relatedTarget,
	        dropzoneElement = event.target;
			var dropzoneJQueryElement=$(dropzoneElement);
			var draggableJQueryElement=$(draggableElement);
			var parentDraggableJQueryElement=draggableJQueryElement.parent();
			var childDropzoneJQueryElement=dropzoneJQueryElement.children(":first");
			
			//Swap both widgets
			_dashboardServiceInstance.swapWidget(draggableJQueryElement, childDropzoneJQueryElement);
			
			draggableJQueryElement.detach();
			childDropzoneJQueryElement.detach();
			draggableJQueryElement.appendTo(dropzoneJQueryElement);
			draggableJQueryElement.removeAttr('data-x');
			draggableJQueryElement.removeAttr('data-y');
			draggableJQueryElement.removeAttr('style');
			childDropzoneJQueryElement.appendTo(parentDraggableJQueryElement);
		    dropzoneElement.classList.remove('_maf_widget_drop-selected');
		},
		ondropdeactivate : function(event) {
			var draggableElement = event.relatedTarget,
	        dropzoneElement = event.target;
			var draggableJQueryElement=$(draggableElement);
			draggableJQueryElement.removeAttr('data-x');
			draggableJQueryElement.removeAttr('data-y');
			draggableJQueryElement.removeAttr('style');
			dropzoneElement.classList.remove('_maf_widget_drop-active');
		}
	});
}

/**
 * Listener used by _maf_widget_activateDashboardEdition() managing the 
 * drag movements
 */
function _maf_widget_dragMoveListener(event){
    var target = event.target,
    x = (parseFloat(target.getAttribute('data-x')) || 0) + event.dx,
    y = (parseFloat(target.getAttribute('data-y')) || 0) + event.dy;
    target.style.webkitTransform =
    target.style.transform = 'translate(' + x + 'px, ' + y + 'px)';
    target.setAttribute('data-x', x);
    target.setAttribute('data-y', y);
}

/**
 * Add a widget placeholder to any empty row cell
 */
function _maf_widget_addPlaceHolderToEmptyCells(){
	$("._maf_widget_widget_area").each(function() {
		//Add a place holder for visualizing the "empty" areas
		if($(this).html().trim()==""){
			var widgetAreaElement=$(this);
			_dashboardServiceInstance.setPlaceHolderMessage(widgetAreaElement);
		}
	});
}

/**
 * Remove a dashboard row
 * dashboardRowElement : the jQuery element for a dashboard row
 */
function _maf_widget_removeDashboardRow(dashboardRowElement){
	//Count the number of row (prevent removing the last row)
	var rowCount=$("._maf_widget_dashboard_row").length;
	if(rowCount > 1){
		_dashboardServiceInstance.removeRow(dashboardRowElement);
		dashboardRowElement.detach();
	}else{
		_maf_widget_MessageBox(_dashboardServiceInstance.warningMessageBoxTitleMessage,_dashboardServiceInstance.cannotDeleteTheLastRowMessage);
	}
}

/**
 * Open a popup to select the dashboard row template (1, 2 or 3 columns)
 * dashboardRowElement : the jQuery element for a dashboard row
 */
function _maf_widget_openDashboardRowTemplateSelector(dashboardRowElement){
	//Check if the number of row is not exceeded
	var rowCount = $('._maf_widget_dashboard_row').length;
	if(rowCount >= _dashboardServiceInstance.maxNumberOfRows){
		_maf_widget_MessageBox(_dashboardServiceInstance.warningMessageBoxTitleMessage,_dashboardServiceInstance.maxNumberOfRowReachedMessage+_dashboardServiceInstance.maxNumberOfRows);
	}else{
		//Open the row template selector
		$('#_maf_widget_dashboardRowTemplateSelector').modal('show');
		$('#_maf_widget_dashboardRowTemplateSelector').data("mafSourceDashboardRowElement",dashboardRowElement);
	}
}

/**
 * Remove a widget from the dashboard
 * widgetElement : the jQuery element for the widget
 */
function _maf_widget_removeWidgetFromDashboard(widgetElement){
	_dashboardServiceInstance.removeWidget(widgetElement);
	var widgetAreaElement=widgetElement.parent();
	widgetElement.detach();
	_dashboardServiceInstance.setPlaceHolderMessage(widgetAreaElement);
}

/**
 * Open the widget catalog for selecting a new widget to be set in the specified area
 * widgetAreaElement : the area which in which the new widget is to be set
 */
function _maf_widget_openWidgetSelector(widgetAreaElement){
	$('#_maf_widget_CatalogSelect').html("");
	$('#_maf_widget_CatalogEntryName').html("");
	$('#_maf_widget_CatalogEntryDescription').html("");
	$('#_maf_widget_pluginConfigurationName').html("");
	$('#_maf_widget_Catalog').modal('show');
	$('#_maf_widget_Catalog').data("mafSourceWidgetAreaElement",widgetAreaElement);
	$('#_maf_widget_CatalogAddButton').prop('disabled', true);
	
	var catalog = _dashboardServiceInstance.loadedWidgetCatalog;
	var widgetCatalogSelect=$('#_maf_widget_CatalogSelect');
	for(widgetIdentifier in catalog){
		var widgetCatalogEntry=catalog[widgetIdentifier];
		$('<option>').val(widgetCatalogEntry.identifier).text(widgetCatalogEntry.name).appendTo(widgetCatalogSelect);
	}
	$('#_maf_widget_CatalogAddButton').prop('disabled', false);

}

/**
 * Add an empty row to the dashboard
 * dashboardRowElement : the jQuery element of the row under which the new row must be added
 * templateIdentifier : the number of columns of the new row to be added
 */
function _maf_widget_addDashboardRow(dashboardRowElement, templateIdentifier){
	var jqxhr = $.get(_dashboardServiceInstance.createNewRowAjaxServiceUrl+templateIdentifier, function(data) {
		_dashboardServiceInstance.toggleEditonMode();
		dashboardRowElement.after(data);
		_dashboardServiceInstance.toggleEditonMode();
		_dashboardServiceInstance.addRow(dashboardRowElement, templateIdentifier);
	}).fail(function(){
		_maf_widget_unexpectedErrorRefreshPage();
	});
}

/**
 * Add a new widget to the dashboard created from the specified widget identifier
 * widgetAreaElement : a jQuery element, the place holder to welcome the widget content
 * widgetCatalogEntry : an entry from the widget catalog
 */
function _maf_widget_addNewWidget(widgetAreaElement, widgetCatalogEntry){
	widgetAreaElement.html('<div><img src="'+_dashboardServiceInstance.ajaxWaitImage+'"/></div>');
	maf_performPostJsonReceiveJson(
		_dashboardServiceInstance.createNewWidgetAjaxServiceUrl,
		JSON.stringify(widgetCatalogEntry),
		function(data){
			_maf_widget_loadWidgetContent(widgetAreaElement, data.url, data.id, function(){
				var widgetElement=widgetAreaElement.children(":first");
				_dashboardServiceInstance.addWidget(widgetElement, data.id, data.url, widgetCatalogEntry.identifier);
				_maf_widget_toggleEdition(true, widgetAreaElement, widgetCatalogEntry.identifier);
			});
		},
		function(){
			_maf_widget_MessageBox(_dashboardServiceInstance.warningMessageBoxTitleMessage,_dashboardServiceInstance.unableToLoadWidgetErrorMessage);
			_dashboardServiceInstance.setPlaceHolderMessage(widgetAreaElement);
		});
}


/**
 * Load a some widget content based from the specified URL
 * widgetAreaElement : a jQuery element, the place holder to welcome the widget content
 * url : an AJAX URL to be called
 * widgetId : the unique id of the widget
 * callback : a callback method to be used once the widget is loaded
 */
function _maf_widget_loadWidgetContent(widgetAreaElement, url, widgetId, callback) {
	widgetAreaElement.html('<div><img src="'+_dashboardServiceInstance.ajaxWaitImage+'"/></div>');
	$.get(url, function(data) {
		widgetAreaElement.html(data);
		if(callback){
			callback();
		}
	}).fail(function() {
		$.get(_dashboardServiceInstance.errorWidgetServiceUrl+"?id="+widgetId, function(data) {
			widgetAreaElement.html(data);
			if(callback){
				callback();
			}
		}).fail(function() {
			widgetAreaElement.html('<div class="bg-danger _maf_widget_error"><i class="fa fa-exclamation-triangle"></i>&nbsp;UNEXPECTED ERROR</div>');
		});
	});
}

/**
 * An unexpected error occurred, refresh the page
 */
function _maf_widget_unexpectedErrorRefreshPage(){
	_maf_widget_MessageBox(
			_dashboardServiceInstance.warningMessageBoxTitleMessage,
			_dashboardServiceInstance.unexpectedErrorMessage,
			function(){
				location.reload();
			});
}

/**
 * Display a modal with a message for the end user
 */
function _maf_widget_MessageBox(title, content, callback){
	$("#_maf_widget_MessageOKButton").hide();
	$("#_maf_widget_MessageCloseButton").show();
	$("#_maf_widget_MessageOKButton").off('click');
	$("#_maf_widget_MessageBox").modal('show');
	$("#_maf_widget_MessageBox .modal-title").html(title);
	$("#_maf_widget_MessageBox .modal-body").html('<div class="alert alert-warning" role="alert">'+content+'</div>');
	if(callback){
		callback();
	}
}

/**
 * Display a modal with a message for the end user
 */
function _maf_widget_ConfirmationBox(title, content, callback){
	$("#_maf_widget_MessageOKButton").show();
	$("#_maf_widget_MessageCloseButton").show();
	$("#_maf_widget_MessageOKButton").off('click');
	$("#_maf_widget_MessageOKButton").click(function(){
		$("#_maf_widget_MessageBox").modal('hide');
		callback();
	});
	$("#_maf_widget_MessageBox").modal('show');
	$("#_maf_widget_MessageBox .modal-title").html(title);
	$("#_maf_widget_MessageBox .modal-body").html('<div class="alert alert-warning" role="alert">'+content+'</div>');
}
