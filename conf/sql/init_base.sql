SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET SQL_SAFE_UPDATES = 0;

#---------------------------------------------------------
# Generated configuration
#---------------------------------------------------------

#Default system level roles types
INSERT INTO `system_level_role_type` (`name`,`description`,`deleted`,`selectable`,`last_update`)
VALUES ('SUPER_USER_ROLE','role.super_user_role.description',0,1,NOW());
INSERT INTO `system_level_role_type` (`name`,`description`,`deleted`,`selectable`,`last_update`)
VALUES ('PMO_ROLE','role.pmo_role.description',0,1,NOW());
INSERT INTO `system_level_role_type` (`name`,`description`,`deleted`,`selectable`,`last_update`)
VALUES ('PORTFOLIO_MANAGER_ROLE','role.portfolio_manager_role.description',0,1,NOW());
INSERT INTO `system_level_role_type` (`name`,`description`,`deleted`,`selectable`,`last_update`)
VALUES ('DELIVERY_MANAGER_ROLE','role.delivery_manager_role.description',0,1,NOW());
INSERT INTO `system_level_role_type` (`name`,`description`,`deleted`,`selectable`,`last_update`)
VALUES ('APPROVER_ROLE','role.approver_role.description',0,1,NOW());
INSERT INTO `system_level_role_type` (`name`,`description`,`deleted`,`selectable`,`last_update`)
VALUES ('ARCHITECTURE_ROLE','role.architecture_role.description',0,1,NOW());
INSERT INTO `system_level_role_type` (`name`,`description`,`deleted`,`selectable`,`last_update`)
VALUES ('DEVELOPER_ROLE','role.developer_role.description',0,1,NOW());
INSERT INTO `system_level_role_type` (`name`,`description`,`deleted`,`selectable`,`last_update`)
VALUES ('PROJECT_MANAGER_ROLE','role.project_manager_role.description',0,1,NOW());
INSERT INTO `system_level_role_type` (`name`,`description`,`deleted`,`selectable`,`last_update`)
VALUES ('FINANCIAL_OFFICER_ROLE','role.financial_officer_role.description',0,1,NOW());
INSERT INTO `system_level_role_type` (`name`,`description`,`deleted`,`selectable`,`last_update`,`account_type_defaults`)
VALUES ('VIEWER_ROLE','role.viewer_role.description',0,0,NOW(),'VIEWER');

#Associations role to permission
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='VIEWER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ROADMAP_DISPLAY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='VIEWER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='COCKPIT_DISPLAY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='VIEWER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PERSONAL_SPACE_READ_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='VIEWER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_VIEW_DETAILS_AS_STAKEHOLDER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='VIEWER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_VIEW_DETAILS_AS_STAKEHOLDER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='VIEWER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='SEARCH_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ROADMAP_SIMULATOR_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='API_MANAGER_PERMISSION ')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='API_TESTER_PERMISSION  ')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ROADMAP_DISPLAY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='COCKPIT_DISPLAY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_EDIT_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_VIEW_DETAILS_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_EDIT_FINANCIAL_INFO_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_DELETE_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PURCHASE_ORDER_VIEW_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ADMIN_USER_ADMINISTRATION_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ADMIN_HUB_MONITORING_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ADMIN_AUDIT_LOG_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PERSONAL_SPACE_READ_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ADMIN_PROVISIONING_MANAGER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='SCM_DEVELOPER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='SCM_ADMIN_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='JENKINS_DEPLOY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_SUBMISSION_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ADMIN_CONFIGURATION_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ADMIN_CUSTOM_ATTRIBUTE_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ADMIN_PLUGIN_MANAGER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ADMIN_KPI_MANAGER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='MILESTONE_APPROVAL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='MILESTONE_DECIDE_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='MILESTONE_OVERVIEW_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_REVIEW_REQUEST_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='SEARCH_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ACTOR_VIEW_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ORG_UNIT_VIEW_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_VIEW_DETAILS_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_VIEW_FINANCIAL_INFO_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ACTOR_EDIT_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ORG_UNIT_EDIT_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_EDIT_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='BUDGET_BUCKET_VIEW_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='BUDGET_BUCKET_EDIT_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='REPORTING_VIEW_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='REPORTING_ADMINISTRATION_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='TIMESHEET_ENTRY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='TIMESHEET_APPROVAL_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='TIMESHEET_LOCK_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='RELEASE_VIEW_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='RELEASE_EDIT_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PARTNER_SYNDICATION_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ROADMAP_SIMULATOR_PERMISSION')
);
		INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ROADMAP_DISPLAY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='COCKPIT_DISPLAY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_EDIT_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_VIEW_DETAILS_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_EDIT_FINANCIAL_INFO_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_DELETE_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PURCHASE_ORDER_VIEW_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PERSONAL_SPACE_READ_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_SUBMISSION_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='MILESTONE_APPROVAL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='MILESTONE_DECIDE_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='MILESTONE_OVERVIEW_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_REVIEW_REQUEST_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='SEARCH_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ACTOR_VIEW_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ORG_UNIT_VIEW_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_VIEW_DETAILS_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_VIEW_FINANCIAL_INFO_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ACTOR_EDIT_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ORG_UNIT_EDIT_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_EDIT_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='BUDGET_BUCKET_VIEW_AS_OWNER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='BUDGET_BUCKET_EDIT_AS_OWNER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='REPORTING_VIEW_AS_VIEWER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='TIMESHEET_ENTRY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='TIMESHEET_APPROVAL_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='TIMESHEET_LOCK_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='RELEASE_VIEW_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PMO_ROLE'),
(select system_permission.id from system_permission where system_permission.name='RELEASE_EDIT_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ROADMAP_SIMULATOR_PERMISSION')
);
		INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ROADMAP_DISPLAY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='COCKPIT_DISPLAY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_EDIT_AS_PORTFOLIO_MANAGER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_VIEW_DETAILS_AS_PORTFOLIO_MANAGER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_AS_PORTFOLIO_MANAGER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PERSONAL_SPACE_READ_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_SUBMISSION_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='MILESTONE_APPROVAL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='MILESTONE_OVERVIEW_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_REVIEW_REQUEST_AS_PORTFOLIO_MANAGER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='SEARCH_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ACTOR_VIEW_AS_SUPERIOR_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ORG_UNIT_VIEW_AS_RESPONSIBLE_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_VIEW_DETAILS_AS_MANAGER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_VIEW_FINANCIAL_INFO_AS_MANAGER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_EDIT_AS_PORTFOLIO_MANAGER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='BUDGET_BUCKET_VIEW_AS_OWNER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='BUDGET_BUCKET_EDIT_AS_OWNER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='REPORTING_VIEW_AS_VIEWER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='TIMESHEET_ENTRY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='TIMESHEET_APPROVAL_AS_MANAGER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PORTFOLIO_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='RELEASE_VIEW_ALL_PERMISSION')
);
		INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DELIVERY_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ROADMAP_DISPLAY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DELIVERY_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='COCKPIT_DISPLAY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DELIVERY_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_VIEW_PUBLIC_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DELIVERY_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_EDIT_AS_MANAGER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DELIVERY_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_VIEW_DETAILS_AS_MANAGER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DELIVERY_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_VIEW_DETAILS_AS_STAKEHOLDER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DELIVERY_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_AS_MANAGER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DELIVERY_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_EDIT_FINANCIAL_INFO_AS_MANAGER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DELIVERY_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PERSONAL_SPACE_READ_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DELIVERY_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='SCM_DEVELOPER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DELIVERY_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='SCM_ADMIN_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DELIVERY_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='JENKINS_DEPLOY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DELIVERY_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='SEARCH_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DELIVERY_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ACTOR_VIEW_AS_SUPERIOR_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DELIVERY_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ORG_UNIT_VIEW_AS_RESPONSIBLE_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DELIVERY_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_VIEW_DETAILS_AS_STAKEHOLDER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DELIVERY_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='REPORTING_VIEW_AS_VIEWER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DELIVERY_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='TIMESHEET_ENTRY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DELIVERY_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='RELEASE_VIEW_AS_MANAGER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DELIVERY_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='RELEASE_EDIT_AS_MANAGER_PERMISSION')
);
		INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='ARCHITECTURE_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ROADMAP_DISPLAY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='ARCHITECTURE_ROLE'),
(select system_permission.id from system_permission where system_permission.name='COCKPIT_DISPLAY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='ARCHITECTURE_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_VIEW_PUBLIC_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='ARCHITECTURE_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_VIEW_DETAILS_AS_STAKEHOLDER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='ARCHITECTURE_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_VIEW_DETAILS_AS_MANAGER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='ARCHITECTURE_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_AS_STAKEHOLDER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='ARCHITECTURE_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PERSONAL_SPACE_READ_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='ARCHITECTURE_ROLE'),
(select system_permission.id from system_permission where system_permission.name='SEARCH_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='ARCHITECTURE_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ACTOR_VIEW_AS_SUPERIOR_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='ARCHITECTURE_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ORG_UNIT_VIEW_AS_RESPONSIBLE_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='ARCHITECTURE_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_VIEW_DETAILS_AS_STAKEHOLDER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='ARCHITECTURE_ROLE'),
(select system_permission.id from system_permission where system_permission.name='REPORTING_VIEW_AS_VIEWER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='ARCHITECTURE_ROLE'),
(select system_permission.id from system_permission where system_permission.name='TIMESHEET_ENTRY_PERMISSION')
);
		INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DEVELOPER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='COCKPIT_DISPLAY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DEVELOPER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_VIEW_PUBLIC_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DEVELOPER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_VIEW_DETAILS_AS_STAKEHOLDER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DEVELOPER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PERSONAL_SPACE_READ_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DEVELOPER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='SCM_DEVELOPER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DEVELOPER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='JENKINS_VIEWER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DEVELOPER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='SEARCH_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DEVELOPER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_VIEW_DETAILS_AS_STAKEHOLDER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='DEVELOPER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='TIMESHEET_ENTRY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PROJECT_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ROADMAP_SIMULATOR_PERMISSION')
);
		INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PROJECT_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ROADMAP_DISPLAY_PERMISSION')
);
		INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PROJECT_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_VIEW_DETAILS_ALL_PERMISSION')
);
		INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PROJECT_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_VIEW_FINANCIAL_INFO_ALL_PERMISSION ')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PROJECT_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='COCKPIT_DISPLAY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PROJECT_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_EDIT_AS_MANAGER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PROJECT_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_VIEW_DETAILS_AS_MANAGER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PROJECT_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_AS_MANAGER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PROJECT_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_EDIT_FINANCIAL_INFO_AS_MANAGER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PROJECT_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PERSONAL_SPACE_READ_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PROJECT_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_SUBMISSION_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PROJECT_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='MILESTONE_APPROVAL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PROJECT_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='MILESTONE_OVERVIEW_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PROJECT_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='SEARCH_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PROJECT_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ACTOR_VIEW_AS_SUPERIOR_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PROJECT_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ORG_UNIT_VIEW_AS_RESPONSIBLE_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PROJECT_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_VIEW_DETAILS_AS_STAKEHOLDER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PROJECT_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='BUDGET_BUCKET_VIEW_AS_OWNER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PROJECT_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='REPORTING_VIEW_AS_VIEWER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PROJECT_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='TIMESHEET_ENTRY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PROJECT_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='TIMESHEET_APPROVAL_AS_MANAGER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='PROJECT_MANAGER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='RELEASE_VIEW_ALL_PERMISSION')
);
		INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='FINANCIAL_OFFICER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='COCKPIT_DISPLAY_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='FINANCIAL_OFFICER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_VIEW_PUBLIC_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='FINANCIAL_OFFICER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_VIEW_FINANCIAL_INFO_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='FINANCIAL_OFFICER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_ENTRY_EDIT_FINANCIAL_INFO_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='FINANCIAL_OFFICER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PURCHASE_ORDER_VIEW_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='FINANCIAL_OFFICER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PERSONAL_SPACE_READ_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='FINANCIAL_OFFICER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='SEARCH_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='FINANCIAL_OFFICER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ACTOR_VIEW_AS_SUPERIOR_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='FINANCIAL_OFFICER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ORG_UNIT_VIEW_AS_RESPONSIBLE_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='FINANCIAL_OFFICER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_VIEW_DETAILS_AS_STAKEHOLDER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='FINANCIAL_OFFICER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='PORTFOLIO_VIEW_FINANCIAL_INFO_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='FINANCIAL_OFFICER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='BUDGET_BUCKET_VIEW_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='FINANCIAL_OFFICER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='BUDGET_BUCKET_EDIT_ALL_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='FINANCIAL_OFFICER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='REPORTING_VIEW_AS_VIEWER_PERMISSION')
);
	INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='FINANCIAL_OFFICER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='TIMESHEET_ENTRY_PERMISSION')
);

INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='ARCHITECTURE_PERMISSION')
);

INSERT INTO `system_level_role_type_has_system_permission`
(`system_level_role_type_id`,
`system_permission_id`)
VALUES
(
(select role_type.id from system_level_role_type as role_type where role_type.name='SUPER_USER_ROLE'),
(select system_permission.id from system_permission where system_permission.name='APPLICATION_BLOCK_EDIT_ALL_PERMISSION')
);

#---------------------------------------------------------
#
# End of generated configuration
#
#---------------------------------------------------------

#---------------------------------------------------------
#
# Start of role descriptions
#
#---------------------------------------------------------

INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.super_user_role.description', 'en', 'The actor is a super user of the system');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.pmo_role.description', 'en', 'The actor is a member of the program management office');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.portfolio_manager_role.description', 'en', 'The actor is a portfolio manager');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.delivery_manager_role.description', 'en', 'The actor is a delivery manager');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.approver_role.description', 'en', 'The actor can contribute to a governance approval process');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.architecture_role.description', 'en', 'The actor is a member of the enterprise architecture team');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.developer_role.description', 'en', 'The actor is a developer');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.project_manager_role.description', 'en', 'The actor is a project manager');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.financial_officer_role.description', 'en', 'The actor is a financial officer');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.viewer_role.description', 'en', 'Read only');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.super_user_role.description', 'fr', 'L\'acteur est un administrateur du système');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.pmo_role.description', 'fr', 'L\'acteur est membre du bureau de gestion des projets');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.portfolio_manager_role.description', 'fr', 'L\'acteur est un gestionnaire de portefeuille');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.delivery_manager_role.description', 'fr', 'L\'acteur est un responsable des livraisons');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.approver_role.description', 'fr', 'L\'acteur participe au processus de gouvernance des projets');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.architecture_role.description', 'fr', 'L\'acteur est un membre de l\'équipe d\'urbanisation ');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.developer_role.description', 'fr', 'L\'acteur est un développeur');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.project_manager_role.description', 'fr', 'L\'acteur est un chef de projet');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.financial_officer_role.description', 'fr', 'L\'acteur est un responsable financier');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.viewer_role.description', 'fr', 'Lecture seule');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.super_user_role.description', 'de', 'Rolleninhaber ist Poweruser des Systems');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.pmo_role.description', 'de', 'Rolleninhaber kontrolliert den Lieferprozess ein Teil des Project Management Offices.');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.portfolio_manager_role.description', 'de', 'Rolleninhaber kontrolliert ein Portfolio von Aktivitäten');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.delivery_manager_role.description', 'de', 'Rolleninhaber ist verantwortlich für die Implementierung in Produktion');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.approver_role.description', 'de', 'Rolleninhaber ist Teil des Genehmigungsprozesses');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.architecture_role.description', 'de', 'Rolleninhaber repräsentiert das Architektenteam');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.developer_role.description', 'de', 'Rolleninhaber ist ein Entwickler');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.project_manager_role.description', 'de', 'Rolleninhaber kann Aktivitäten anlegen');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.financial_officer_role.description', 'de', 'Rolleninhaber kontrolliert den Finanzstatus');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('role.viewer_role.description', 'de', 'Lesezugriff');

#---------------------------------------------------------
#
# End of role descriptions
#
#---------------------------------------------------------

#Admin users
INSERT INTO `principal` (`uid`,`is_active`,`is_pending`,`validation_key`,`is_displayed`) VALUES ('admin',1,0,NULL,0);
#Admin user for TAF (needed mainly for Jenkins configuration)
INSERT INTO `principal` (`uid`,`is_active`,`is_pending`,`validation_key`,`is_displayed`) VALUES ('admin_taf',1,0,NULL,0);

INSERT INTO `system_level_role`
(`is_enabled`,
`system_level_role_type_id`,
`principal_id`)
VALUES
(
1,
(SELECT role_type.id FROM system_level_role_type as role_type WHERE role_type.name='SUPER_USER_ROLE'),
(SELECT principal.id FROM principal WHERE principal.uid='admin')
);

INSERT INTO `system_level_role`
(`is_enabled`,
`system_level_role_type_id`,
`principal_id`)
VALUES
(
1,
(SELECT role_type.id FROM system_level_role_type as role_type WHERE role_type.name='SUPER_USER_ROLE'),
(SELECT principal.id FROM principal WHERE principal.uid='admin_taf')
);

INSERT INTO `credential` (`id`,`uid`,`first_name`,`last_name`,`full_name`,`mail`,`password`,`failed_login`,`is_active`,`last_update`) VALUES (3,'admin','Thomas','Garlot','Thomas Garlot','thomas.garlot@the-agile-factory.com','e1NTSEF9NG5UR3pDdGVMV2xqT1QzWmpFV2JNYkR4NWgyanM2TC91dUtoVHc9PQ==',0,1,'2014-11-09 11:03:06');
INSERT INTO `credential` (`id`,`uid`,`first_name`,`last_name`,`full_name`,`mail`,`password`,`failed_login`,`is_active`,`last_update`) VALUES (4,'admin_taf','Patrick','Moiroux','Patrick Moiroux','patrick.moiroux@the-agile-factory.com','e1NTSEF9NG5UR3pDdGVMV2xqT1QzWmpFV2JNYkR4NWgyanM2TC91dUtoVHc9PQ==',0,1,'2014-11-09 11:03:06');

#---------------------------------------------------------
# Start of reference data
#---------------------------------------------------------

-- Table actor_type
insert into `actor_type`(id, deleted, name, description, ref_id, selectable, last_update) values (1, 0, 'actor_type.name.regular', null, 'REGULAR', 1, CURRENT_TIMESTAMP);
insert into `actor_type`(id, deleted, name, description, ref_id, selectable, last_update) values (3, 0, 'actor_type.name.external', null, 'EXTERNAL', 1, CURRENT_TIMESTAMP);
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('actor_type.name.regular', 'en', 'Regular');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('actor_type.name.regular', 'fr', 'Interne');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('actor_type.name.regular', 'de', 'Intern');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('actor_type.name.external', 'en', 'External');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('actor_type.name.external', 'fr', 'Externe');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('actor_type.name.external', 'de', 'Extern');

-- Table `org_unit_type`
insert into org_unit_type(id, name, description, deleted, selectable, last_update) values (1, 'org_unit_type.name.country', 'org_unit_type.description.country', 0, 1, CURRENT_TIMESTAMP);
insert into org_unit_type(id, name, description, deleted, selectable, last_update) values (2, 'org_unit_type.name.division', 'org_unit_type.description.division', 0, 1, CURRENT_TIMESTAMP);
insert into org_unit_type(id, name, description, deleted, selectable, last_update) values (3, 'org_unit_type.name.department', 'org_unit_type.description.department', 0, 1, CURRENT_TIMESTAMP);
insert into org_unit_type(id, name, description, deleted, selectable, last_update) values (4, 'org_unit_type.name.team', 'org_unit_type.description.team', 0, 1, CURRENT_TIMESTAMP);
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.name.country','en','Country');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.name.country','fr','Pays');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.name.country','de','Land');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.name.division','en','Division');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.name.division','fr','Division');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.name.division','de','Geschäftsbereich');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.name.department','en','Department');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.name.department','fr','Département');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.name.department','de','Abteilung');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.name.team','en','Team');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.name.team','fr','Equipe');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.name.team','de','Team');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.description.country','en','A country branch');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.description.country','fr','Une filiale d\'un pays');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.description.country','de','Landesniederlassung');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.description.division','en','The top level organizatonal unit which is headed by a VP attending the executive committee.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.description.division','fr','Une division gérée par un VP qui participe au commité éxécutif.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.description.division','de','Geschäftsbereich, Leiter ist Mitglied der Konzernleitung.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.description.department','en','Organizational unit child of the division level.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.description.department','fr','Unité organisationelle enfant d\'une division');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.description.department','de','Untergruppe des Geschäftsbreich');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.description.team','en','Organizational unit child of the department level or the team level');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.description.team','fr','Unité organisationelle enfant d\'un département');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('org_unit_type.description.team','de','Untergruppe der Abteilung');

-- Table portfolio_entry_type
insert into portfolio_entry_type(id, deleted, name,  description, selectable, last_update) values (1, 0, 'portfolio_entry_type.name.project', 'portfolio_entry_type.description.project', 1, CURRENT_TIMESTAMP);
insert into portfolio_entry_type(id, deleted, name,  description, selectable, last_update) values (2, 0, 'portfolio_entry_type.name.enhancement', 'portfolio_entry_type.description.enhancement', 1, CURRENT_TIMESTAMP);
insert into portfolio_entry_type(id, deleted, name,  description, selectable, last_update) values (3, 0, 'portfolio_entry_type.name.upgrade', 'portfolio_entry_type.description.upgrade', 1, CURRENT_TIMESTAMP);
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_type.name.project', 'en', 'Project');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_type.name.project', 'fr', 'Projet');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_type.name.project', 'de', 'Projekt');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_type.name.enhancement', 'en', 'Enhancement');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_type.name.enhancement', 'fr', 'Evolution');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_type.name.enhancement', 'de', 'Verbesserung');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_type.name.upgrade', 'en', 'Upgrade');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_type.name.upgrade', 'fr', 'Mise à jour');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_type.name.upgrade', 'de', 'Aktualisieren');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_type.description.project', 'en', 'A company project');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_type.description.project', 'fr', 'Un projet d''entreprise');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_type.description.project', 'de', 'Ein Unternehmensprojekt');	
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_type.description.enhancement', 'en', 'Enhancement of an existing IT platform');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_type.description.enhancement', 'fr', 'Evolution d''une plateforme existante');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_type.description.enhancement', 'de', 'Verbesserung einer IT-Plattform');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_type.description.upgrade', 'en', 'Upgrade of an IT system');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_type.description.upgrade', 'fr', 'Mise à jour d''un système informatique');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_type.description.upgrade', 'de', 'Aktualisieren eines IT-Systems');

-- Table portfolio_entry_dependency_type

INSERT INTO `portfolio_entry_dependency_type` (`deleted`, `last_update`, `name`, `contrary`, `description`, `is_active`) VALUES ('0', NOW(), 'portfolio_entry.dependency_type.precedes.name', 'portfolio_entry.dependency_type.precedes.contrary', 'portfolio_entry.dependency_type.precedes.description', '1');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry.dependency_type.precedes.name', 'en', 'Precedes');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry.dependency_type.precedes.name', 'fr', 'Précède');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry.dependency_type.precedes.name', 'de', 'Vorausgeht');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry.dependency_type.precedes.contrary', 'en', 'Follows');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry.dependency_type.precedes.contrary', 'fr', 'Suit');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry.dependency_type.precedes.contrary', 'de', 'Folgt');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry.dependency_type.precedes.description', 'en', 'Define the precedence of initiatives.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry.dependency_type.precedes.description', 'fr', 'Définit la précédence des initiatives.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry.dependency_type.precedes.description', 'de', 'Definieren Sie den Vorrang von Initiativen.');

-- Table portfolio_type
insert into portfolio_type(id, name, description, last_update, selectable, deleted) values (1, 'portfolio_type.name.program', 'portfolio_type.description.program', CURRENT_TIMESTAMP, 1, 0);
insert into portfolio_type(id, name, description, last_update, selectable, deleted) values (2, 'portfolio_type.name.portfolio', 'portfolio_type.description.portfolio', CURRENT_TIMESTAMP, 1, 0);
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_type.name.program', 'en', 'Program');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_type.name.program', 'fr', 'Programme');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_type.name.program', 'de', 'Program');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_type.name.portfolio', 'en', 'Portfolio');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_type.name.portfolio', 'fr', 'Portefeuille');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_type.name.portfolio', 'de', 'Portfolio');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_type.description.program', 'en', 'A group of projects which aims at a common goal.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_type.description.program', 'fr', 'Ensemble de projets qui ont un objectif commun.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_type.description.program', 'de', 'Eine Gruppe von Projekten mit dem gleichen Ziel.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_type.description.portfolio', 'en', 'A group of projects');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_type.description.portfolio', 'fr', 'Ensemble de projets');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_type.description.portfolio', 'de', 'Eine Gruppe von Projekten ');

-- Table portfolio_entry_report_status_type
insert into portfolio_entry_report_status_type(id, name, description, selectable, deleted, css_class, last_update) values(1, 'portfolio_entry_report_status_type.name.red', 'portfolio_entry_report_status_type.description.red', 1, 0, 'danger', CURRENT_TIMESTAMP);
insert into portfolio_entry_report_status_type(id, name, description, selectable, deleted, css_class, last_update) values(2, 'portfolio_entry_report_status_type.name.amber', 'portfolio_entry_report_status_type.description.amber', 1, 0, 'warning', CURRENT_TIMESTAMP);
insert into portfolio_entry_report_status_type(id, name, description, selectable, deleted, css_class, last_update) values(3, 'portfolio_entry_report_status_type.name.green', 'portfolio_entry_report_status_type.description.green', 1, 0, 'success', CURRENT_TIMESTAMP);
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_report_status_type.name.red', 'en', 'Red');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_report_status_type.name.red', 'fr', 'Rouge');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_report_status_type.name.red', 'de', 'Rot');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_report_status_type.name.green', 'en', 'Green');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_report_status_type.name.green', 'fr', 'Vert');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_report_status_type.name.green', 'de', 'Grün');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_report_status_type.name.amber', 'en', 'Amber');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_report_status_type.name.amber', 'fr', 'Orange');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_report_status_type.name.amber', 'de', 'Orange');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_report_status_type.description.red', 'en', 'The project is associated with issues or risks which are not solved nor mitigated.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_report_status_type.description.red', 'fr', 'Le projet est associé à des incidents ou risques qui ne sont pas résolus ou modérés.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_report_status_type.description.red', 'de', 'Risiken und Probleme des Projektes werden weder gelöst noch berücksichtigt.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_report_status_type.description.green', 'en', 'The project is not associated with any risks or issues.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_report_status_type.description.green', 'fr', 'Le projet n\'est pas associé à des incidents ou risques.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_report_status_type.description.green', 'de', 'Projekt hat weder Risiken noch Probleme.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_report_status_type.description.amber', 'en', 'The project is associated with issues or risks which are known and mitigated.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_report_status_type.description.amber', 'fr', 'Le projet est associé à des incidents ou risques qui sont connus et modérés.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_report_status_type.description.amber', 'de', 'Projekt hat bekannte Risiken und/oder Probleme welche berücksichtig werden.');

-- Table portfolio_entry_risk_type
insert into portfolio_entry_risk_type(id, name, description, deleted, selectable, last_update) values(1, 'portfolio_entry_risk_type.name.budget', 'portfolio_entry_risk_type.description.budget', 0, 1, CURRENT_TIMESTAMP);
insert into portfolio_entry_risk_type(id, name, description, deleted, selectable, last_update) values(2, 'portfolio_entry_risk_type.name.resource', 'portfolio_entry_risk_type.description.resource', 0, 1, CURRENT_TIMESTAMP);
insert into portfolio_entry_risk_type(id, name, description, deleted, selectable, last_update) values(3, 'portfolio_entry_risk_type.name.planning', 'portfolio_entry_risk_type.description.planning', 0, 1, CURRENT_TIMESTAMP);
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_risk_type.name.resource', 'en', 'Resource');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_risk_type.name.resource', 'fr', 'Ressource');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_risk_type.name.resource', 'de', 'Ressource');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_risk_type.name.planning', 'en', 'Planning');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_risk_type.name.planning', 'fr', 'Planning');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_risk_type.name.planning', 'de', 'Planung');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_risk_type.name.budget', 'en', 'Budget');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_risk_type.name.budget', 'fr', 'Budget');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_risk_type.name.budget', 'de', 'Budget');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_risk_type.description.resource', 'en', 'The risk is associated with a resource availability.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_risk_type.description.resource', 'fr', 'Le risque est lié à la disponibilité d\'une ressource.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_risk_type.description.resource', 'de', 'Risiko - Ressourcenverfügbarkeit.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_risk_type.description.planning', 'en', 'The risk is associated with the planning.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_risk_type.description.planning', 'fr', 'Le risque est lié au planning.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_risk_type.description.planning', 'de', 'Risiko - Planung.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_risk_type.description.budget', 'en', 'The risk is associated with the budget.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_risk_type.description.budget', 'fr', 'Le risque est lié au budget.');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('portfolio_entry_risk_type.description.budget', 'de', 'Risiko - Budget.');

-- Table life_cycle_process
insert into life_cycle_process(id, deleted, short_name, name, description, is_active, last_update) values (5, 0, 'life_cycle_process.short_name.default', 'life_cycle_process.short_name.default', 'life_cycle_process.description.default', 1, CURRENT_TIMESTAMP);
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_process.short_name.default', 'en', 'Default');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_process.short_name.default', 'fr', 'Standard');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_process.short_name.default', 'de', 'Grundeinstellung');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_process.name.default', 'en', 'Default');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_process.name.default', 'fr', 'Standard');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_process.name.default', 'de', 'Grundeinstellung');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_process.description.default', 'en', 'Default life cycle process when you do not know what to choose');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_process.description.default', 'fr', 'Processus de gouvernance standard');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_process.description.default', 'de', 'Grundeinstellung Lieferzyklus');

-- Table life_cycle_milestone_instance_status_type
insert into life_cycle_milestone_instance_status_type(id, deleted, name, description, is_approved, selectable, last_update) values (1, 0, 'life_cycle_milestone_instance_status_type.name.approved', 'life_cycle_milestone_instance_status_type.description.approved', 1, 1, CURRENT_TIMESTAMP);
insert into life_cycle_milestone_instance_status_type(id, deleted, name, description, is_approved, selectable, last_update) values (2, 0, 'life_cycle_milestone_instance_status_type.name.rejected', 'life_cycle_milestone_instance_status_type.description.rejected', 0, 1, CURRENT_TIMESTAMP);
insert into life_cycle_milestone_instance_status_type(id, deleted, name, description, is_approved, selectable, last_update) values (3, 0, 'life_cycle_milestone_instance_status_type.name.approved_remark', 'life_cycle_milestone_instance_status_type.description.approved_remark', 1, 1, CURRENT_TIMESTAMP);
insert into life_cycle_milestone_instance_status_type(id, deleted, name, description, is_approved, selectable, last_update) values (4, 0, 'life_cycle_milestone_instance_status_type.name.delayed', 'life_cycle_milestone_instance_status_type.description.delayed', 0, 1, CURRENT_TIMESTAMP);
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.name.approved', 'en', 'Approved');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.name.approved', 'fr', 'Approuvé');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.name.approved', 'de', 'Genehmigt');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.name.rejected', 'en', 'Rejected');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.name.rejected', 'fr', 'Rejeté');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.name.rejected', 'de', 'Abgelehnt');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.name.approved_remark', 'en', 'Approved with remarks');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.name.approved_remark', 'fr', 'Approuvé avec commentaires');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.name.approved_remark', 'de', 'Genehmigt mit Kommentaren');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.name.delayed', 'en', 'Delayed');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.name.delayed', 'fr', 'Retardé');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.name.delayed', 'de', 'Verschoben');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.description.approved', 'en', 'The review committe has given its approval');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.description.approved', 'fr', 'Le commité de pilotage a donné son approbation');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.description.approved', 'de', 'Der Prüfungsausschuss hat das Projekt genehmigt');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.description.rejected', 'en', 'The  review committe has rejected the milestone');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.description.rejected', 'fr', 'Le commité de pilotage a rejeté le passage du jalon');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.description.rejected', 'de', 'Der Prüfungsausschuss den Meilenstein abgelehnt');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.description.approved_remark', 'en', 'The  review committe has approved but with some comments');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.description.approved_remark', 'fr', 'Le commité de pilotage a approuvé le passage du jalon avec certains commentaires');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.description.approved_remark', 'de', 'Der Prüfungsausschuss hat das Projekt mit Kommentaren genehmigt');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.description.delayed', 'en', 'The review committe was not able to evaluate the project');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.description.delayed', 'fr', 'Le commité de pilotage n\'a pas été capable d\'évaluer l\'approbation du jalon');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone_instance_status_type.description.delayed', 'de', 'Der Prüfungsausschuss war nicht in der Lage das Projekt zu evaluieren');

-- Table life_cycle_milestone
insert into life_cycle_milestone(id, deleted, short_name, name, description, `order`, life_cycle_process_id, last_update, is_review_required, default_life_cycle_milestone_instance_status_type_id) values (20, 0, 'life_cycle_milestone.short_name.init', 'life_cycle_milestone.name.init', null, 1, 5, CURRENT_TIMESTAMP, 1, 1);
insert into life_cycle_milestone(id, deleted, short_name, name, description, `order`, life_cycle_process_id, last_update, is_review_required, default_life_cycle_milestone_instance_status_type_id) values (21, 0, 'life_cycle_milestone.short_name.start', 'life_cycle_milestone.name.start', null, 2, 5, CURRENT_TIMESTAMP, 1,  1);
insert into life_cycle_milestone(id, deleted, short_name, name, description, `order`, life_cycle_process_id, last_update, is_review_required, default_life_cycle_milestone_instance_status_type_id) values (22, 0, 'life_cycle_milestone.short_name.launch', 'life_cycle_milestone.name.launch', null, 3, 5, CURRENT_TIMESTAMP, 1, 1);
insert into life_cycle_milestone(id, deleted, short_name, name, description, `order`, life_cycle_process_id, last_update, is_review_required, default_life_cycle_milestone_instance_status_type_id) values (23, 0, 'life_cycle_milestone.short_name.closure', 'life_cycle_milestone.name.closure', null, 4, 5, CURRENT_TIMESTAMP, 0, 1);
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.short_name.init', 'en', 'Initialisation');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.short_name.init', 'fr', 'Initialisation');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.short_name.init', 'de', 'Initialisierung');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.short_name.start', 'en', 'Start');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.short_name.start', 'fr', 'Démarrage');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.short_name.start', 'de', 'Start');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.short_name.launch', 'en', 'Launch');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.short_name.launch', 'fr', 'Lancement');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.short_name.launch', 'de', 'Neu');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.short_name.closure', 'en', 'Closure');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.short_name.closure', 'fr', 'Terminé');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.short_name.closure', 'de', 'Schließung');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.name.init', 'en', 'Initialisation');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.name.init', 'fr', 'Initialisation');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.name.init', 'de', 'Initialisierung');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.name.start', 'en', 'Start');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.name.start', 'fr', 'Démarrage');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.name.start', 'de', 'Start');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.name.launch', 'en', 'Launch');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.name.launch', 'fr', 'Lancement');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.name.launch', 'de', 'Lancierung');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.name.closure', 'en', 'Closure');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.name.closure', 'fr', 'Terminé');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('life_cycle_milestone.name.closure', 'de', 'Abgeschlossen');

-- Table stakeholder_type
insert into stakeholder_type(id, deleted, name, description, selectable, last_update) values (2, 0, 'stakeholder_type.name.product_manager', null, 1, CURRENT_TIMESTAMP);
insert into stakeholder_type(id, deleted, name, description, selectable, last_update) values (5, 0, 'stakeholder_type.name.business_analyst', null, 1, CURRENT_TIMESTAMP);
insert into stakeholder_type(id, deleted, name, description, selectable, last_update) values (7, 0, 'stakeholder_type.name.developer', null, 1, CURRENT_TIMESTAMP);
insert into stakeholder_type(id, deleted, name, description, selectable, last_update) values (8, 0, 'stakeholder_type.name.architect', null, 1, CURRENT_TIMESTAMP);
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('stakeholder_type.name.product_manager', 'en', 'Product manager');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('stakeholder_type.name.product_manager', 'fr', 'Responsable de produit');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('stakeholder_type.name.product_manager', 'de', 'Produktmanager');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('stakeholder_type.name.business_analyst', 'en', 'Business analyst');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('stakeholder_type.name.business_analyst', 'fr', 'Analyste Business');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('stakeholder_type.name.business_analyst', 'de', 'Business Analyst');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('stakeholder_type.name.developer', 'en', 'Developer');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('stakeholder_type.name.developer', 'fr', 'Développeur');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('stakeholder_type.name.developer', 'de', 'Entwickler');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('stakeholder_type.name.architect', 'en', 'Architect');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('stakeholder_type.name.architect', 'fr', 'Architecte');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('stakeholder_type.name.architect', 'de', 'Architekt');

-- Table portfolio_type_has_stakeholder_type
INSERT INTO portfolio_type_has_stakeholder_type (`portfolio_type_id`, `stakeholder_type_id`) VALUES ('1', '2');
INSERT INTO portfolio_type_has_stakeholder_type (`portfolio_type_id`, `stakeholder_type_id`) VALUES ('1', '5');
INSERT INTO portfolio_type_has_stakeholder_type (`portfolio_type_id`, `stakeholder_type_id`) VALUES ('1', '7');
INSERT INTO portfolio_type_has_stakeholder_type (`portfolio_type_id`, `stakeholder_type_id`) VALUES ('1', '8');
INSERT INTO portfolio_type_has_stakeholder_type (`portfolio_type_id`, `stakeholder_type_id`) VALUES ('2', '2');
INSERT INTO portfolio_type_has_stakeholder_type (`portfolio_type_id`, `stakeholder_type_id`) VALUES ('2', '5');
INSERT INTO portfolio_type_has_stakeholder_type (`portfolio_type_id`, `stakeholder_type_id`) VALUES ('2', '7');
INSERT INTO portfolio_type_has_stakeholder_type (`portfolio_type_id`, `stakeholder_type_id`) VALUES ('2', '8');

-- Table portfolio_entry_type_has_stakeholder_type
INSERT INTO portfolio_entry_type_has_stakeholder_type (`portfolio_entry_type_id`, `stakeholder_type_id`) VALUES ('1', '2');
INSERT INTO portfolio_entry_type_has_stakeholder_type (`portfolio_entry_type_id`, `stakeholder_type_id`) VALUES ('1', '5');
INSERT INTO portfolio_entry_type_has_stakeholder_type (`portfolio_entry_type_id`, `stakeholder_type_id`) VALUES ('1', '7');
INSERT INTO portfolio_entry_type_has_stakeholder_type (`portfolio_entry_type_id`, `stakeholder_type_id`) VALUES ('1', '8');
INSERT INTO portfolio_entry_type_has_stakeholder_type (`portfolio_entry_type_id`, `stakeholder_type_id`) VALUES ('2', '2');
INSERT INTO portfolio_entry_type_has_stakeholder_type (`portfolio_entry_type_id`, `stakeholder_type_id`) VALUES ('2', '5');
INSERT INTO portfolio_entry_type_has_stakeholder_type (`portfolio_entry_type_id`, `stakeholder_type_id`) VALUES ('2', '7');
INSERT INTO portfolio_entry_type_has_stakeholder_type (`portfolio_entry_type_id`, `stakeholder_type_id`) VALUES ('2', '8');
INSERT INTO portfolio_entry_type_has_stakeholder_type (`portfolio_entry_type_id`, `stakeholder_type_id`) VALUES ('3', '2');
INSERT INTO portfolio_entry_type_has_stakeholder_type (`portfolio_entry_type_id`, `stakeholder_type_id`) VALUES ('3', '5');
INSERT INTO portfolio_entry_type_has_stakeholder_type (`portfolio_entry_type_id`, `stakeholder_type_id`) VALUES ('3', '7');
INSERT INTO portfolio_entry_type_has_stakeholder_type (`portfolio_entry_type_id`, `stakeholder_type_id`) VALUES ('3', '8');

-- Table life_cycle_phase
INSERT INTO life_cycle_phase (`name`,`life_cycle_process_id`,`order`,`start_life_cycle_milestone_id`,`end_life_cycle_milestone_id`,`last_update`,`gap_days_start`,`gap_days_end`) VALUES ('phase.initiation', 5, 1,  20, 21, CURRENT_TIMESTAMP, 0, 0);
INSERT INTO life_cycle_phase (`name`,`life_cycle_process_id`,`order`,`start_life_cycle_milestone_id`,`end_life_cycle_milestone_id`,`last_update`,`gap_days_start`,`gap_days_end`) VALUES ('phase.execution', 5, 2, 21, 22, CURRENT_TIMESTAMP, 1, 0);
INSERT INTO life_cycle_phase (`name`,`life_cycle_process_id`,`order`,`start_life_cycle_milestone_id`,`end_life_cycle_milestone_id`,`last_update`,`gap_days_start`,`gap_days_end`) VALUES ('phase.closure', 5, 3, 22, 23, CURRENT_TIMESTAMP, 1, 0);
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('phase.initiation', 'en', 'Initiation');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('phase.initiation', 'fr', 'Initiation');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('phase.initiation', 'de', 'Initiierung');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('phase.execution', 'en', 'Execution');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('phase.execution', 'fr', 'Exécution');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('phase.execution', 'de', 'Ausführung');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('phase.closure', 'en', 'Closure');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('phase.closure', 'fr', 'Fermeture');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('phase.closure', 'de', 'Abschluss');

-- Table requirement_status
INSERT INTO `requirement_status`(`id`,`deleted`,`last_update`,`type`,`name`,`description`) VALUES (1, 0, CURRENT_TIMESTAMP, 'NEW', 'requirement.status.name.new','');
INSERT INTO `requirement_status`(`id`,`deleted`,`last_update`,`type`,`name`,`description`) VALUES (2, 0, CURRENT_TIMESTAMP, 'OPEN', 'requirement.status.name.in_progress','');
INSERT INTO `requirement_status`(`id`,`deleted`,`last_update`,`type`,`name`,`description`) VALUES (3, 0, CURRENT_TIMESTAMP, 'OPEN', 'requirement.status.name.ready_to_test','');
INSERT INTO `requirement_status`(`id`,`deleted`,`last_update`,`type`,`name`,`description`) VALUES (4, 0, CURRENT_TIMESTAMP, 'CLOSED', 'requirement.status.name.closed','');
INSERT INTO `requirement_status`(`id`,`deleted`,`last_update`,`type`,`name`,`description`) VALUES (5, 0, CURRENT_TIMESTAMP, 'CLOSED', 'requirement.status.name.rejected','');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.status.name.new', 'en', 'New');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.status.name.new', 'fr', 'Nouveau');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.status.name.new', 'de', 'Neu');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.status.name.in_progress', 'en', 'In progress');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.status.name.in_progress', 'fr', 'En cours');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.status.name.in_progress', 'de', 'in Arbeit');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.status.name.ready_to_test', 'en', 'Ready to test');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.status.name.ready_to_test', 'fr', 'Prêt à tester');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.status.name.ready_to_test', 'de', 'Bereit zum Testen');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.status.name.closed', 'en', 'Closed');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.status.name.closed', 'fr', 'Fermé');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.status.name.closed', 'de', 'Geschlossen');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.status.name.rejected', 'en', 'Rejected');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.status.name.rejected', 'fr', 'Rejeté');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.status.name.rejected', 'de', 'Abgelehnt');

-- Table requirement_priority
INSERT INTO `requirement_priority`(`id`,`deleted`,`last_update`,`name`,`description`,`is_must`) VALUES (1, 0, CURRENT_TIMESTAMP, 'requirement.priority.could','', 0);
INSERT INTO `requirement_priority`(`id`,`deleted`,`last_update`,`name`,`description`,`is_must`) VALUES (2, 0, CURRENT_TIMESTAMP, 'requirement.priority.should','', 0);
INSERT INTO `requirement_priority`(`id`,`deleted`,`last_update`,`name`,`description`,`is_must`) VALUES (3, 0, CURRENT_TIMESTAMP, 'requirement.priority.must','', 1);
INSERT INTO `requirement_priority`(`id`,`deleted`,`last_update`,`name`,`description`,`is_must`) VALUES (4, 0, CURRENT_TIMESTAMP, 'requirement.priority.wont','', 0);
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.priority.could', 'en', 'Could');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.priority.could', 'fr', 'Pourrait');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.priority.could', 'de', 'Kann');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.priority.should', 'en', 'Should');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.priority.should', 'fr', 'Devrait');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.priority.should', 'de', 'Soll');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.priority.must', 'en', 'Must');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.priority.must', 'fr', 'Doit');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.priority.must', 'de', 'Muss');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.priority.wont', 'en', 'Won\'t');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.priority.wont', 'fr', 'Ne sera pas');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.priority.wont', 'de', 'Geht nicht');

-- Table requirement_severity
INSERT INTO `requirement_severity`(`id`,`deleted`,`last_update`,`name`,`description`,`is_blocker`) VALUES (1, 0, CURRENT_TIMESTAMP, 'requirement.severity.trivial','',0);
INSERT INTO `requirement_severity`(`id`,`deleted`,`last_update`,`name`,`description`,`is_blocker`) VALUES (2, 0, CURRENT_TIMESTAMP, 'requirement.severity.minor','',0);
INSERT INTO `requirement_severity`(`id`,`deleted`,`last_update`,`name`,`description`,`is_blocker`) VALUES (3, 0, CURRENT_TIMESTAMP, 'requirement.severity.major','',0);
INSERT INTO `requirement_severity`(`id`,`deleted`,`last_update`,`name`,`description`,`is_blocker`) VALUES (4, 0, CURRENT_TIMESTAMP, 'requirement.severity.critical','',1);
INSERT INTO `requirement_severity`(`id`,`deleted`,`last_update`,`name`,`description`,`is_blocker`) VALUES (5, 0, CURRENT_TIMESTAMP, 'requirement.severity.blocker','',1);
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.severity.trivial', 'en', 'Trivial');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.severity.trivial', 'fr', 'Trivial');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.severity.trivial', 'de', 'Trivial');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.severity.minor', 'en', 'Minor');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.severity.minor', 'fr', 'Mineur');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.severity.minor', 'de', 'Gering');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.severity.major', 'en', 'Major');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.severity.major', 'fr', 'Majeur');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.severity.major', 'de', 'Bedeutend');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.severity.critical', 'en', 'Critical');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.severity.critical', 'fr', 'Critique');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.severity.critical', 'de', 'Kritisch');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.severity.blocker', 'en', 'Blocker');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.severity.blocker', 'fr', 'Bloquant');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('requirement.severity.blocker', 'de', 'Blocker');

-- Table reporting_category
INSERT INTO `reporting_category` (`deleted`, `last_update`, `name`, `manageable`, `order`) VALUES ('0', CURRENT_TIMESTAMP, 'reporting_category.allocation.name', '1', '4');
INSERT INTO `reporting_category` (`deleted`, `last_update`, `name`, `manageable`, `order`) VALUES ('0', CURRENT_TIMESTAMP, 'reporting_category.project.name', '1', '1');
INSERT INTO `reporting_category` (`deleted`, `last_update`, `name`, `manageable`, `order`) VALUES ('0', CURRENT_TIMESTAMP, 'reporting_category.finance.name', '1', '7');
INSERT INTO `reporting_category` (`deleted`, `last_update`, `name`, `manageable`, `order`) VALUES ('0', CURRENT_TIMESTAMP, 'reporting_category.release.name', '1', '9');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('reporting_category.allocation.name', 'en', 'Allocation');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('reporting_category.allocation.name', 'fr', 'Ressources');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('reporting_category.allocation.name', 'de', 'Zuteilung');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('reporting_category.project.name', 'en', 'Project');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('reporting_category.project.name', 'fr', 'Projet');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('reporting_category.project.name', 'de', 'Projekt');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('reporting_category.finance.name', 'en', 'Finance');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('reporting_category.finance.name', 'fr', 'Finance');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('reporting_category.finance.name', 'de', 'Finanzwesen');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('reporting_category.release.name', 'en', 'Release');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('reporting_category.release.name', 'fr', 'Release');
INSERT INTO `i18n_messages` (`key`, `language`, `value`) VALUES ('reporting_category.release.name', 'de', 'Release');

-- Update standard report category
UPDATE `reporting` SET `reporting_category_id`=(SELECT `id` FROM `reporting_category` WHERE `name`='reporting_category.project.name') WHERE `name`='reporting.roadmap.name';
UPDATE `reporting` SET `reporting_category_id`=(SELECT `id` FROM `reporting_category` WHERE `name`='reporting_category.project.name') WHERE `name`='reporting.portfolios.name';
UPDATE `reporting` SET `reporting_category_id`=(SELECT `id` FROM `reporting_category` WHERE `name`='reporting_category.project.name') WHERE `name`='reporting.data_quality.name';
UPDATE `reporting` SET `reporting_category_id`=(SELECT `id` FROM `reporting_category` WHERE `name`='reporting_category.project.name') WHERE `name`='report.status_report.name';
UPDATE `reporting` SET `reporting_category_id`=(SELECT `id` FROM `reporting_category` WHERE `name`='reporting_category.finance.name') WHERE `name`='reporting.financial.name';
UPDATE `reporting` SET `reporting_category_id`=(SELECT `id` FROM `reporting_category` WHERE `name`='reporting_category.allocation.name') WHERE `name`='reporting.actor_allocation.name';
UPDATE `reporting` SET `reporting_category_id`=(SELECT `id` FROM `reporting_category` WHERE `name`='reporting_category.allocation.name') WHERE `name`='reporting.org_unit_allocation.name';
UPDATE `reporting` SET `reporting_category_id`=(SELECT `id` FROM `reporting_category` WHERE `name`='reporting_category.allocation.name') WHERE `name`='reporting.org_unit_actors_allocation.name';
UPDATE `reporting` SET `reporting_category_id`=(SELECT `id` FROM `reporting_category` WHERE `name`='reporting_category.allocation.name') WHERE `name`='report.competency.name';
UPDATE `reporting` SET `reporting_category_id`=(SELECT `id` FROM `reporting_category` WHERE `name`='reporting_category.release.name') WHERE `name`='report.release_requirements.name';

-- Table plugin_definition

INSERT INTO `plugin_definition` (`identifier`, `clazz`, `is_available`) VALUES ('jira1', 'services.plugins.atlassian.jira.jira1.JiraPluginRunner', '1');
INSERT INTO `plugin_definition` (`identifier`, `clazz`, `is_available`) VALUES ('jenk1', 'services.plugins.jenkins.jenk1.JenkinsPluginRunner', '1');
INSERT INTO `plugin_definition` (`identifier`, `clazz`, `is_available`) VALUES ('sharp1', 'services.plugins.microsoft.sharepoint.sharp1.SharepointPluginRunner', '0');
INSERT INTO `plugin_definition` (`identifier`, `clazz`, `is_available`) VALUES ('nex1', 'services.plugins.nexus.nex1.NexusPluginRunner', '1');
INSERT INTO `plugin_definition` (`identifier`, `clazz`, `is_available`) VALUES ('redm1', 'services.plugins.redmine.redm1.RedminePluginRunner', '1');
INSERT INTO `plugin_definition` (`identifier`, `clazz`, `is_available`) VALUES ('redm2', 'services.plugins.redmine.redm2.RedminePluginRunner', '1');
INSERT INTO `plugin_definition` (`identifier`, `clazz`, `is_available`) VALUES ('subv1', 'services.plugins.subversion.subv1.SubversionPluginRunner', '1');
INSERT INTO `plugin_definition` (`identifier`, `clazz`, `is_available`) VALUES ('actorsload1', 'services.plugins.system.actorsload1.ActorLoaderPluginRunner', '1');
INSERT INTO `plugin_definition` (`identifier`, `clazz`, `is_available`) VALUES ('orgunitsload1', 'services.plugins.system.orgunitsload1.OrgUnitLoaderPluginRunner', '1');
INSERT INTO `plugin_definition` (`identifier`, `clazz`, `is_available`) VALUES ('finance1', 'services.plugins.system.finance1.FinanceErpIntegrationPluginRunner', '1');
INSERT INTO `plugin_definition` (`identifier`, `clazz`, `is_available`) VALUES ('notification1', 'services.plugins.system.notification1.EventNotificationPluginRunner', '0');

-- API root key

INSERT INTO `api_registration` (`name`, `application_key`, `shared_secret`, `api_authorization`, `deleted`, `last_update`, `description`, `testable`) VALUES ('_root', '5p-1x5npqJbumZToiqrtiqTkv4_wnYqW5bOW5JqQ54mO7KWU7JmI6aO165yM762r7qWk4pym4pGx5ZiS6IWp5IGy54uJ6ICn67mR4LyO4q6I64C42Zruj7Xhh6PhhrLkrKbqg6HsuIbljK7voofpubDvg6rgr6Pii6PosZrbieyYreOst-6nluC4uem1g-K7hO2QlOSnm-evv-ehjOCjnMq24quP5KO_4a2O54Ss56uC6Lu_76K76oqQ', '6qme6r-I54u_6pKo57aI7Zei4Lat5q2W7oq865207ZeS7K6Y4YKV5IKN4a2Y6Kmq45K266mt5auV4LGI6r6X6reI65Sn5IKB5L2x7LKv5KSv8KSQuOqolei2uOqvk-Sdg-qTs-6lqem5ueW6nOC0ke6breWWoeKlsuKdquiMrOOqreSQoOWgs-OOquSLtu2fuuWigOaMheaeheWgmO-2i-SGlOq1puK1h-uem-6go--So-m8kui6he6rkuaPkw', 'GET (.*)\nPOST (.*)\nPUT (.*)\nDELETE (.*)', '0', NOW(), 'The root key', '1');

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = 1;
