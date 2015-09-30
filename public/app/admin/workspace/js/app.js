'use strict';

/*
 * Workspace application module
 */
var workspaceApp = angular.module('workspaceApp', [
	'ngRoute',
	'workspaceControllers'
]);

workspaceApp.config(['$routeProvider',
function($routeProvider) {
  $routeProvider.
    when('/home', {
      templateUrl: _maf_app_rootpath+'partials/home.html',
      controller: 'HomeCtrl'
    }).
    otherwise({
      redirectTo: '/home'
    });
}]);