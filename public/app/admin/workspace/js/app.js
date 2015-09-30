'use strict';

/*
 * Workspace application module
 */
var workspaceApp = angular.module('workspaceApp', [
	'ngRoute',
	'workspaceControllers'
]);
workspaceApp.constant("Msg", _maf_Msg);
workspaceApp.constant("Routes", _maf_app_paths);

workspaceApp.config(['$routeProvider','Routes',
function($routeProvider, Routes) {
  $routeProvider.
    when('/home', {
      templateUrl: Routes.basePath+'partials/home.html',
      controller: 'HomeCtrl'
    }).
    otherwise({
      redirectTo: '/home'
    });
}]);