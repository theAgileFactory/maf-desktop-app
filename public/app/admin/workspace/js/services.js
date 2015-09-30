'use strict';

/*
 * Workspace services
 */
var workspaceServices = angular.module('workspaceServices', ['ngResource']);

phonecatServices.factory('Msg', ['$resource',
  function($resource){
    return $resource(_maf_app_paths.i18nRoute, {}, {
      query: {method:'GET', isArray:true}
    });
  }]);
