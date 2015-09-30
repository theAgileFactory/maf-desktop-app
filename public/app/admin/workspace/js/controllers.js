'use strict';

/*
 * Workspace controllers
 */
var workspaceControllers = angular.module('workspaceControllers', []);

workspaceControllers.controller('HomeCtrl', ['$scope',
  function($scope) {
    $scope.message = 'HellowWorld in BizDock';
 }]);
