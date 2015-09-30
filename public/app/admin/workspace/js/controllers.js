'use strict';

/*
 * Workspace controllers
 */
var workspaceControllers = angular.module('workspaceControllers', []);

workspaceControllers.controller('HomeCtrl', ['$scope','Msg',
  function($scope, Msg) {
	$scope.workspaceAsAdminPanelTitle = Msg['admin.workspace.admin.panel.title'];
    $scope.workspaceAsMemberPanelTitle = Msg['admin.workspace.member.panel.title'];
 }]);
