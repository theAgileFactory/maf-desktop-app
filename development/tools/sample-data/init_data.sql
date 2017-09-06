#This file loads some default data into the ST environment

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET SQL_SAFE_UPDATES = 0;

#---------------------------------------------------------
# End of DB cleanup
#---------------------------------------------------------

-- Remove init_base data conflicting with sample data
DELETE FROM actor;
DELETE FROM credential;

-- Table actor
INSERT INTO `actor`(`id`, `deleted`, `ref_id`, `erp_ref_id`, `first_name`, `last_name`, `title`, `mail`, `mobile_phone`, `fix_phone`, `employee_id`, `uid`, `actor_type_id`, `manager_id`, `last_update`, `org_unit_id`, `is_active`) VALUES (9, 0, '009', 'REG09', 'Steven', 'All', 'Key User', 'test_sall@the-agile-factory.com', null, null, null, 'test_sall', 1, null, CURRENT_TIMESTAMP, 2, 1);
INSERT INTO `actor`(`id`, `deleted`, `ref_id`, `erp_ref_id`, `first_name`, `last_name`, `title`, `mail`, `mobile_phone`, `fix_phone`, `employee_id`, `uid`, `actor_type_id`, `manager_id`, `last_update`, `org_unit_id`, `is_active`) VALUES (4, 0, '004', 'REG04', 'Suzan', 'Pmo', null, 'test_spmo@the-agile-factory.com', '0041433456659', null, null, 'test_spmo', 1, 9, CURRENT_TIMESTAMP, 2, 1);
INSERT INTO `actor`(`id`, `deleted`, `ref_id`, `erp_ref_id`, `first_name`, `last_name`, `title`, `mail`, `mobile_phone`, `fix_phone`, `employee_id`, `uid`, `actor_type_id`, `manager_id`, `last_update`, `org_unit_id`, `is_active`) VALUES (1, 0, '001', 'REG01', 'Alfred', 'Exco', 'IT / Portfolio Manager', 'test_aexco@the-agile-factory.com', '0041123456483', '0041123456483', null, 'test_aexco', 1, null, CURRENT_TIMESTAMP, 9, 1);
INSERT INTO `actor`(`id`, `deleted`, `ref_id`, `erp_ref_id`, `first_name`, `last_name`, `title`, `mail`, `mobile_phone`, `fix_phone`, `employee_id`, `uid`, `actor_type_id`, `manager_id`, `last_update`, `org_unit_id`, `is_active`) VALUES (2, 0, '002', null, 'John', 'Dm', 'Delivery manager', 'test_jdm@the-agile-factory.com', '0041123456789', null, null, 'test_jdm', 1, null, CURRENT_TIMESTAMP, 8, 1);
INSERT INTO `actor`(`id`, `deleted`, `ref_id`, `erp_ref_id`, `first_name`, `last_name`, `title`, `mail`, `mobile_phone`, `fix_phone`, `employee_id`, `uid`, `actor_type_id`, `manager_id`, `last_update`, `org_unit_id`, `is_active`) VALUES (8, 0, '008', 'REG08', 'Gustav', 'Archi', 'Enterprise Architect', 'test_garchi@the-agile-factory.com', null, null, null, 'test_garchi', 1, 1, CURRENT_TIMESTAMP, 9, 1);
INSERT INTO `actor`(`id`, `deleted`, `ref_id`, `erp_ref_id`, `first_name`, `last_name`, `title`, `mail`, `mobile_phone`, `fix_phone`, `employee_id`, `uid`, `actor_type_id`, `manager_id`, `last_update`, `org_unit_id`, `is_active`) VALUES (7, 0, '007', 'REG07', 'James', 'Dev', 'Developper', 'test_jdev@the-agile-factory.com', '00415333236679', null, null, 'test_jdev', 1, 1, CURRENT_TIMESTAMP, 9, 1);
INSERT INTO `actor`(`id`, `deleted`, `ref_id`, `erp_ref_id`, `first_name`, `last_name`, `title`, `mail`, `mobile_phone`, `fix_phone`, `employee_id`, `uid`, `actor_type_id`, `manager_id`, `last_update`, `org_unit_id`, `is_active`) VALUES (3, 0, '003', 'REG03', 'Robert', 'Pm', 'Project manager', 'test_rpm@the-agile-factory.com', '0041433456789', null, null, 'test_rpm', 3, 1, CURRENT_TIMESTAMP, 9, 1);
INSERT INTO `actor`(`id`, `deleted`, `ref_id`, `erp_ref_id`, `first_name`, `last_name`, `title`, `mail`, `mobile_phone`, `fix_phone`, `employee_id`, `uid`, `actor_type_id`, `manager_id`, `last_update`, `org_unit_id`, `is_active`) VALUES (6, 0, '006', 'REG06', 'Bill', 'Finance', 'VP Finance', 'test_bfin@the-agile-factory.com', '00415333236659', null, null, 'test_bfin', 1, null, CURRENT_TIMESTAMP, 1, 1);
INSERT INTO `actor`(`id`, `deleted`, `ref_id`, `erp_ref_id`, `first_name`, `last_name`, `title`, `mail`, `mobile_phone`, `fix_phone`, `employee_id`, `uid`, `actor_type_id`, `manager_id`, `last_update`, `org_unit_id`, `is_active`) VALUES (5, 0, '005', 'REG05', 'Mary', 'Marketing', 'Product Manager', 'test_mmark@the-agile-factory.com', '00414333236659', null, null, 'test_mmark', 1, null, CURRENT_TIMESTAMP, 7, 1);

-- Table actor_capacity
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (1,CURRENT_TIMESTAMP,0,3,2015,1,21.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (2,CURRENT_TIMESTAMP,0,3,2015,2,19.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (3,CURRENT_TIMESTAMP,0,3,2015,3,15.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (4,CURRENT_TIMESTAMP,0,3,2015,4,20.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (5,CURRENT_TIMESTAMP,0,3,2015,5,21.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (6,CURRENT_TIMESTAMP,0,3,2015,6,18.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (7,CURRENT_TIMESTAMP,0,3,2015,7,10.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (8,CURRENT_TIMESTAMP,0,3,2015,8,15.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (9,CURRENT_TIMESTAMP,0,3,2015,9,20.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (10,CURRENT_TIMESTAMP,0,3,2015,10,21.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (11,CURRENT_TIMESTAMP,0,3,2015,11,20.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (12,CURRENT_TIMESTAMP,0,3,2015,12,21.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (13,CURRENT_TIMESTAMP,0,3,2016,1,19.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (14,CURRENT_TIMESTAMP,0,3,2016,2,19.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (15,CURRENT_TIMESTAMP,0,3,2016,3,19.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (16,CURRENT_TIMESTAMP,0,3,2016,4,19.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (17,CURRENT_TIMESTAMP,0,3,2016,5,0.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (18,CURRENT_TIMESTAMP,0,3,2016,6,0.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (19,CURRENT_TIMESTAMP,0,3,2016,7,19.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (20,CURRENT_TIMESTAMP,0,3,2016,8,19.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (21,CURRENT_TIMESTAMP,0,3,2016,9,19.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (22,CURRENT_TIMESTAMP,0,3,2016,10,19.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (23,CURRENT_TIMESTAMP,0,3,2016,11,0.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (24,CURRENT_TIMESTAMP,0,3,2016,12,19.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (25,CURRENT_TIMESTAMP,0,7,2015,1,21.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (26,CURRENT_TIMESTAMP,0,7,2015,2,19.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (27,CURRENT_TIMESTAMP,0,7,2015,3,15.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (28,CURRENT_TIMESTAMP,0,7,2015,4,20.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (29,CURRENT_TIMESTAMP,0,7,2015,5,21.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (30,CURRENT_TIMESTAMP,0,7,2015,6,18.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (31,CURRENT_TIMESTAMP,0,7,2015,7,10.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (32,CURRENT_TIMESTAMP,0,7,2015,8,15.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (33,CURRENT_TIMESTAMP,0,7,2015,9,20.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (34,CURRENT_TIMESTAMP,0,7,2015,10,21.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (35,CURRENT_TIMESTAMP,0,7,2015,11,20.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (36,CURRENT_TIMESTAMP,0,7,2015,12,21.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (37,CURRENT_TIMESTAMP,0,7,2016,1,19.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (38,CURRENT_TIMESTAMP,0,7,2016,2,19.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (39,CURRENT_TIMESTAMP,0,7,2016,3,19.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (40,CURRENT_TIMESTAMP,0,7,2016,4,19.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (41,CURRENT_TIMESTAMP,0,7,2016,5,0.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (42,CURRENT_TIMESTAMP,0,7,2016,6,0.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (43,CURRENT_TIMESTAMP,0,7,2016,7,19.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (44,CURRENT_TIMESTAMP,0,7,2016,8,19.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (45,CURRENT_TIMESTAMP,0,7,2016,9,19.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (46,CURRENT_TIMESTAMP,0,7,2016,10,19.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (47,CURRENT_TIMESTAMP,0,7,2016,11,0.00);
INSERT INTO `actor_capacity` (`id`,`last_update`,`deleted`,`actor_id`,`year`,`month`,`value`) VALUES (48,CURRENT_TIMESTAMP,0,7,2016,12,19.00);

-- Table actor_type
INSERT INTO `actor_type`(`id`, `deleted`, `name`, `description`, `ref_id`, `selectable`, `last_update`) VALUES (4, 1, 'DELETED ACTORY TYPE', 'DELETED ACTOR TYPE', 'DELETED ACTOR TYPE', 1, CURRENT_TIMESTAMP);
INSERT INTO `actor_type`(`id`, `deleted`, `name`, `description`, `ref_id`, `selectable`, `last_update`) VALUES (5, 0, 'NOT SELECTABLE ACTOR TYPE', 'NOT SELECTABLE ACTOR TYPE', 'NOT SELECTABLE ACTOR TYPE', 0, CURRENT_TIMESTAMP);
INSERT INTO `actor_type`(`id`, `deleted`, `name`, `description`, `ref_id`, `selectable`, `last_update`) VALUES (6, 1, 'DELETED NOT SELECTABLE TYPE', 'DELETED AND NOT SELECTABLE ACTOR TYPE', 'DELETED AND NOT SELECTABLE ACTOR TYPE', 0, CURRENT_TIMESTAMP);

-- Table actor_has_competencies
INSERT INTO `actor_has_competency`(`actor_id`,`competency_id`) VALUES (7,1);
INSERT INTO `actor_has_competency`(`actor_id`,`competency_id`) VALUES (7,2);
INSERT INTO `actor_has_competency`(`actor_id`,`competency_id`) VALUES (7,3);
INSERT INTO `actor_has_competency`(`actor_id`,`competency_id`) VALUES (8,1);
INSERT INTO `actor_has_competency`(`actor_id`,`competency_id`) VALUES (8,2);

-- Table budget_bucket
INSERT INTO `budget_bucket`(`id`,`deleted`,`ref_id`,`name`,`owner_id`,`is_approved`,`last_update`,`is_active`) VALUES (1, 0, 'IT_DIV_2015_OPEX', 'IT OPEX Budget for 2015', 1, 1, CURRENT_TIMESTAMP, 1);
INSERT INTO `budget_bucket`(`id`,`deleted`,`ref_id`,`name`,`owner_id`,`is_approved`,`last_update`,`is_active`) VALUES (2, 0, 'IT_DIV_2015_CAPEX', 'IT CAPEX Budget for 2015', 1, 1, CURRENT_TIMESTAMP, 1);
INSERT INTO `budget_bucket`(`id`,`deleted`,`ref_id`,`name`,`owner_id`,`is_approved`,`last_update`,`is_active`) VALUES (3, 0, 'DEV_CRM_2015', 'CRM Budget for 2015', 9, 0, CURRENT_TIMESTAMP, 1);
INSERT INTO `budget_bucket`(`id`,`deleted`,`ref_id`,`name`,`owner_id`,`is_approved`,`last_update`,`is_active`) VALUES (4, 0, 'DEV_ERP_2015', 'ERP Budget for 2015', 9, 0, CURRENT_TIMESTAMP, 1);
INSERT INTO `budget_bucket`(`id`,`deleted`,`ref_id`,`name`,`owner_id`,`is_approved`,`last_update`,`is_active`) VALUES (5, 0, 'DEV_BILL_2015', 'Billing Budget for 2015', 9, 0, CURRENT_TIMESTAMP, 0);

-- Table budget_bucket_line
INSERT INTO `budget_bucket_line`(`id`,`deleted`,`last_update`,`name`,`ref_id`,`is_opex`,`amount`,`currency_code`,`budget_bucket_id`) VALUES (1, 0, CURRENT_TIMESTAMP, 'Maintenance IT systems', 'BL_IT_OPEX', 1, 200000, 'CHF', 1);
INSERT INTO `budget_bucket_line`(`id`,`deleted`,`last_update`,`name`,`ref_id`,`is_opex`,`amount`,`currency_code`,`budget_bucket_id`) VALUES (2, 0, CURRENT_TIMESTAMP, 'Enhancement IT systems', 'BL_IT_CAPEX', 0, 100000, 'CHF', 2);
INSERT INTO `budget_bucket_line`(`id`,`deleted`,`last_update`,`name`,`ref_id`,`is_opex`,`amount`,`currency_code`,`budget_bucket_id`) VALUES (3, 0, CURRENT_TIMESTAMP, 'Envelop CRM development', 'BL_CRM_DEV', 0, 30000, 'CHF', 3);
INSERT INTO `budget_bucket_line`(`id`,`deleted`,`last_update`,`name`,`ref_id`,`is_opex`,`amount`,`currency_code`,`budget_bucket_id`) VALUES (4, 0, CURRENT_TIMESTAMP, 'Envelop CRM configuration', 'BL_CRM_CONF', 0, 20000, 'CHF', 3);
INSERT INTO `budget_bucket_line`(`id`,`deleted`,`last_update`,`name`,`ref_id`,`is_opex`,`amount`,`currency_code`,`budget_bucket_id`) VALUES (5, 0, CURRENT_TIMESTAMP, 'Envelop ERP configuration', 'BL_ERP_CONF', 1, 50000, 'CHF', 4);
INSERT INTO `budget_bucket_line`(`id`,`deleted`,`last_update`,`name`,`ref_id`,`is_opex`,`amount`,`currency_code`,`budget_bucket_id`) VALUES (6, 0, CURRENT_TIMESTAMP, 'Envelop Billing development', 'BL_BILL_DEV', 1, 200000, 'CHF', 5);

-- Table cost_center
INSERT INTO cost_center (`id`,`deleted`,`ref_id`,`name`,`owner_id`,`last_update`) VALUES(1, 0, '503100', 'Servers', 3, NOW());
INSERT INTO cost_center (`id`,`deleted`,`ref_id`,`name`,`owner_id`,`last_update`) VALUES(2, 0, '503500', 'Operations', 3, NOW());
INSERT INTO cost_center (`id`,`deleted`,`ref_id`,`name`,`owner_id`,`last_update`) VALUES(3, 0, '2000', 'OS', 3, NOW());
INSERT INTO cost_center (`id`,`deleted`,`ref_id`,`name`,`owner_id`,`last_update`) VALUES(4, 0, '3000', 'CRM', 4, NOW());
INSERT INTO cost_center (`id`,`deleted`,`ref_id`,`name`,`owner_id`,`last_update`) VALUES(5, 1, '4000', 'DELETED CC', 3, NOW());

-- Table competency
INSERT INTO `competency`(`id`,`deleted`,`last_update`,`is_active`,`name`,`description`) VALUES (1,0,CURRENT_TIMESTAMP,1,'java.name','java.desc');
INSERT INTO `competency`(`id`,`deleted`,`last_update`,`is_active`,`name`,`description`) VALUES (2,0,CURRENT_TIMESTAMP,1,'business.analysis.name','business.analysis.desc');
INSERT INTO `competency`(`id`,`deleted`,`last_update`,`is_active`,`name`,`description`) VALUES (3,0,CURRENT_TIMESTAMP,1,'test.name','test.desc');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('java.name', 'en', 'Java');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('java.name', 'fr', 'Java');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('java.name', 'de', 'Java');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('business.analysis.name', 'en', 'Business Analysis');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('business.analysis.name', 'fr', 'Business Analysis');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('business.analysis.name', 'de', 'Business Analysis');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('test.name', 'en', 'Test');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('test.name', 'fr', 'Test');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('test.name', 'de', 'Test');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('java.desc', 'en', 'Java');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('java.desc', 'fr', 'Java');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('java.desc', 'de', 'Java');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('business.analysis.desc', 'en', 'Business Analysis');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('business.analysis.desc', 'fr', 'Business Analysis');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('business.analysis.desc', 'de', 'Business Analysis');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('test.desc', 'en', 'Test');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('test.desc', 'fr', 'Test');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('test.desc', 'de', 'Test');

-- Table credential
INSERT INTO `credential` (`id`,`uid`,`first_name`,`last_name`,`full_name`,`mail`,`password`,`failed_login`,`is_active`,`last_update`) VALUES (1,'test_sall','Steven','All','Steven All','test_sall@the-agile-factory.com','e1NTSEF9YVgzMXZsbEswb2JkVElPazRGWlZ5dHpmbm85cjF0YThSZ2xKOHc9PQ==',0,1,CURRENT_TIMESTAMP);
INSERT INTO `credential` (`id`,`uid`,`first_name`,`last_name`,`full_name`,`mail`,`password`,`failed_login`,`is_active`,`last_update`) VALUES (5,'test_spmo','Suzan','Pmo','Suzan Pmo','test_spmo@the-agile-factory.com','e1NTSEF9YVgzMXZsbEswb2JkVElPazRGWlZ5dHpmbm85cjF0YThSZ2xKOHc9PQ==',0,1,CURRENT_TIMESTAMP);
INSERT INTO `credential` (`id`,`uid`,`first_name`,`last_name`,`full_name`,`mail`,`password`,`failed_login`,`is_active`,`last_update`) VALUES (6,'test_aexco','Alfred','Exco','Alfred Exco','test_aexco@the-agile-factory.com','e1NTSEF9YVgzMXZsbEswb2JkVElPazRGWlZ5dHpmbm85cjF0YThSZ2xKOHc9PQ==',0,1,CURRENT_TIMESTAMP);
INSERT INTO `credential` (`id`,`uid`,`first_name`,`last_name`,`full_name`,`mail`,`password`,`failed_login`,`is_active`,`last_update`) VALUES (7,'test_jdm','John','Dm','John Dm','test_jdm@the-agile-factory.com','e1NTSEF9YVgzMXZsbEswb2JkVElPazRGWlZ5dHpmbm85cjF0YThSZ2xKOHc9PQ==',0,1,CURRENT_TIMESTAMP);
INSERT INTO `credential` (`id`,`uid`,`first_name`,`last_name`,`full_name`,`mail`,`password`,`failed_login`,`is_active`,`last_update`) VALUES (8,'test_garchi','Gustav','Architecture','Gustav Architecture', 'test_garchi@the-agile-factory.com', 'e1NTSEF9YVgzMXZsbEswb2JkVElPazRGWlZ5dHpmbm85cjF0YThSZ2xKOHc9PQ==',0,1,CURRENT_TIMESTAMP);
INSERT INTO `credential` (`id`,`uid`,`first_name`,`last_name`,`full_name`,`mail`,`password`,`failed_login`,`is_active`,`last_update`) VALUES (9,'test_jdev','James','Developer','James Developer','test_jdev@the-agile-factory.com','e1NTSEF9YVgzMXZsbEswb2JkVElPazRGWlZ5dHpmbm85cjF0YThSZ2xKOHc9PQ==',0,1,CURRENT_TIMESTAMP);
INSERT INTO `credential` (`id`,`uid`,`first_name`,`last_name`,`full_name`,`mail`,`password`,`failed_login`,`is_active`,`last_update`) VALUES (2,'test_rpm','Robert','Pm','Robert Pm','test_rpm@the-agile-factory.com','e1NTSEF9YVgzMXZsbEswb2JkVElPazRGWlZ5dHpmbm85cjF0YThSZ2xKOHc9PQ==',0,1,CURRENT_TIMESTAMP);
INSERT INTO `credential` (`id`,`uid`,`first_name`,`last_name`,`full_name`,`mail`,`password`,`failed_login`,`is_active`,`last_update`) VALUES (10,'test_bfin','Bill','Finance','Bill Finance','test_bfin@the-agile-factory.com','e1NTSEF9YVgzMXZsbEswb2JkVElPazRGWlZ5dHpmbm85cjF0YThSZ2xKOHc9PQ==',0,1,CURRENT_TIMESTAMP);
INSERT INTO `credential` (`id`,`uid`,`first_name`,`last_name`,`full_name`,`mail`,`password`,`failed_login`,`is_active`,`last_update`) VALUES (11,'test_mmark','Mary','Marketing','Mary Marketing','test_mmark@the-agile-factory.com','e1NTSEF9YVgzMXZsbEswb2JkVElPazRGWlZ5dHpmbm85cjF0YThSZ2xKOHc9PQ==',0,1,CURRENT_TIMESTAMP);

-- Table custom_attribute_definition
INSERT INTO `custom_attribute_definition`(`object_type`,`order`,`attribute_type`,`uuid`,`name`,`description`,`last_update`)
VALUES('models.pmo.PortfolioEntry', 1,'BOOLEAN','CUSTOM_ATTRIBUTE_IS_IMPORTANT','custom_attribute.isImportant','Boolean if an intiative is important', NOW());
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('custom_attribute.isImportant','en','Is important');
INSERT INTO `custom_attribute_definition`(`object_type`,`order`,`attribute_type`,`uuid`,`name`,`description`,`last_update`)
VALUES('models.pmo.PortfolioEntry', 4,'INTEGER','CUSTOM_ATTRIBUTE_SCORE','custom_attribute.score','Foreseen ROI', NOW());
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('custom_attribute.score','en','ROI');
INSERT INTO `custom_attribute_definition`(`object_type`,`order`,`attribute_type`,`uuid`,`name`,`description`,`last_update`)
VALUES('models.pmo.PortfolioEntry', 5,'TEXT','CUSTOM_ATTRIBUTE_COMMENT','custom_attribute.comment','Comment', NOW());
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('custom_attribute.comment','en','Comment');
INSERT INTO `custom_attribute_definition`(`object_type`,`order`,`attribute_type`,`uuid`,`name`,`description`,`last_update`)
VALUES('models.finance.WorkOrder', 1,'STRING','CUSTOM_ATTRIBUTE_WORK_ORDER_SUPPLIER','custom_attribute.WorkOrderSupplier','The supplier for a work order', NOW());
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('custom_attribute.WorkOrderSupplier','en','Supplier');
INSERT INTO `custom_attribute_definition`(`object_type`,`order`,`attribute_type`,`uuid`,`name`,`description`,`last_update`)
VALUES('models.finance.PortfolioEntryBudgetLine', 1,'STRING','CUSTOM_ATTRIBUTE_BUDGET_LI_SUPPLIER','custom_attribute.BudgetLineItemSupplier','The supplier for a budget line item', NOW());
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('custom_attribute.BudgetLineItemSupplier','en','Supplier');
INSERT INTO `custom_attribute_definition`(`object_type`,`order`,`attribute_type`,`uuid`,`name`,`description`,`last_update`)
VALUES('models.pmo.PortfolioEntry', 2,'DATE','CUSTOM_ATTRIBUTE_START_DATE','custom_attribute.StartDate','The wished start date of the initiative', NOW());
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('custom_attribute.StartDate','en','Wished start date');
INSERT INTO `custom_attribute_definition`(`object_type`,`order`,`attribute_type`,`uuid`,`name`,`description`,`last_update`)
VALUES('models.pmo.PortfolioEntry',3,'SINGLE_ITEM','CUSTOM_ATTRIBUTE_LEVEL_OF_COMPLEXITY','custom_attribute.levelOfComplexity','The level of complexity of the initiative', NOW());
INSERT INTO `custom_attribute_item_option`(`name`,`custom_attribute_definition_id`,`last_update`,`order`)
VALUES('levelOfComplexity.VeryLow', (SELECT id FROM custom_attribute_definition WHERE uuid = 'CUSTOM_ATTRIBUTE_LEVEL_OF_COMPLEXITY'), NOW(), 1);
INSERT INTO `custom_attribute_item_option`(`name`,`custom_attribute_definition_id`,`last_update`,`order`)
VALUES('levelOfComplexity.Low', (SELECT id FROM custom_attribute_definition WHERE uuid = 'CUSTOM_ATTRIBUTE_LEVEL_OF_COMPLEXITY'), NOW(), 2);
INSERT INTO `custom_attribute_item_option`(`name`,`custom_attribute_definition_id`,`last_update`,`order`)
VALUES('levelOfComplexity.Medium', (SELECT id FROM custom_attribute_definition WHERE uuid = 'CUSTOM_ATTRIBUTE_LEVEL_OF_COMPLEXITY'), NOW(), 3);
INSERT INTO `custom_attribute_item_option`(`name`,`custom_attribute_definition_id`,`last_update`,`order`)
VALUES('levelOfComplexity.High', (SELECT id FROM custom_attribute_definition WHERE uuid = 'CUSTOM_ATTRIBUTE_LEVEL_OF_COMPLEXITY'), NOW(), 4);
INSERT INTO `custom_attribute_item_option`(`name`,`custom_attribute_definition_id`,`last_update`,`order`)
VALUES('levelOfComplexity.VeryHigh', (SELECT id FROM custom_attribute_definition WHERE uuid = 'CUSTOM_ATTRIBUTE_LEVEL_OF_COMPLEXITY'), NOW(), 5);
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('custom_attribute.levelOfComplexity','en','Complexity');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('levelOfComplexity.VeryLow','en','Very low');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('levelOfComplexity.Low','en','Low');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('levelOfComplexity.Medium','en','Medium');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('levelOfComplexity.High','en','High');
INSERT INTO `i18n_messages`(`key`,`language`,`value`) VALUES ('levelOfComplexity.VeryHigh','en','Very High');

-- Table good_receipt
INSERT INTO `goods_receipt`(`deleted`,`ref_id`,`purchase_order_line_item_id`,`amount_received`,`last_update`,`currency_code`) VALUES (0, '123', 1, 10000, CURRENT_TIMESTAMP, 'CHF');
INSERT INTO `goods_receipt`(`deleted`,`ref_id`,`purchase_order_line_item_id`,`amount_received`,`last_update`,`currency_code`) VALUES (0, '456', 2, 50000, CURRENT_TIMESTAMP, 'CHF');
INSERT INTO `goods_receipt`(`deleted`,`ref_id`,`purchase_order_line_item_id`,`amount_received`,`last_update`,`currency_code`) VALUES (0, '789', 3, 100000, CURRENT_TIMESTAMP, 'CHF');
INSERT INTO `goods_receipt`(`deleted`,`ref_id`,`purchase_order_line_item_id`,`amount_received`,`last_update`,`currency_code`) VALUES (0, '012', 4, 50000, CURRENT_TIMESTAMP, 'CHF');
INSERT INTO `goods_receipt`(`deleted`,`ref_id`,`purchase_order_line_item_id`,`amount_received`,`last_update`,`currency_code`) VALUES (0, '345', 5, 50000, CURRENT_TIMESTAMP, 'CHF');

-- Table life_cycle_instance
INSERT INTO life_cycle_instance(`id`, `portfolio_entry_id`, `is_active`, `deleted`, `last_update`, `life_cycle_process_id`, `is_concept`) VALUES (1, 1, 1, 0, CURRENT_TIMESTAMP, 1, 0);
INSERT INTO life_cycle_instance(`id`, `portfolio_entry_id`, `is_active`, `deleted`, `last_update`, `life_cycle_process_id`, `is_concept`) VALUES (2, 2, 1, 0, CURRENT_TIMESTAMP, 1, 1);
INSERT INTO life_cycle_instance(`id`, `portfolio_entry_id`, `is_active`, `deleted`, `last_update`, `life_cycle_process_id`, `is_concept`) VALUES (3, 3, 1, 0, CURRENT_TIMESTAMP, 1, 1);
INSERT INTO life_cycle_instance(`id`, `portfolio_entry_id`, `is_active`, `deleted`, `last_update`, `life_cycle_process_id`, `is_concept`) VALUES (4, 4, 1, 0, CURRENT_TIMESTAMP, 1, 1);
INSERT INTO life_cycle_instance(`id`, `portfolio_entry_id`, `is_active`, `deleted`, `last_update`, `life_cycle_process_id`, `is_concept`) VALUES (5, 5, 1, 0, CURRENT_TIMESTAMP, 1, 1);
INSERT INTO life_cycle_instance(`id`, `portfolio_entry_id`, `is_active`, `deleted`, `last_update`, `life_cycle_process_id`, `is_concept`) VALUES (6, 6, 1, 0, CURRENT_TIMESTAMP, 1, 1);
INSERT INTO life_cycle_instance(`id`, `portfolio_entry_id`, `is_active`, `deleted`, `last_update`, `life_cycle_process_id`, `is_concept`) VALUES (7, 7, 1, 0, CURRENT_TIMESTAMP, 1, 1);
INSERT INTO life_cycle_instance(`id`, `portfolio_entry_id`, `is_active`, `deleted`, `last_update`, `life_cycle_process_id`, `is_concept`) VALUES (8, 8, 1, 0, CURRENT_TIMESTAMP, 1, 1);
INSERT INTO life_cycle_instance(`id`, `portfolio_entry_id`, `is_active`, `deleted`, `last_update`, `life_cycle_process_id`, `is_concept`) VALUES (9, 9, 1, 0, CURRENT_TIMESTAMP, 1, 1);
INSERT INTO life_cycle_instance(`id`, `portfolio_entry_id`, `is_active`, `deleted`, `last_update`, `life_cycle_process_id`, `is_concept`) VALUES (10, 10, 1, 0, CURRENT_TIMESTAMP, 1, 1);
INSERT INTO life_cycle_instance(`id`, `portfolio_entry_id`, `is_active`, `deleted`, `last_update`, `life_cycle_process_id`, `is_concept`) VALUES (11, 11, 1, 0, CURRENT_TIMESTAMP, 1, 1);
INSERT INTO life_cycle_instance(`id`, `portfolio_entry_id`, `is_active`, `deleted`, `last_update`, `life_cycle_process_id`, `is_concept`) VALUES (12, 12, 1, 0, CURRENT_TIMESTAMP, 1, 1);
INSERT INTO life_cycle_instance(`id`, `portfolio_entry_id`, `is_active`, `deleted`, `last_update`, `life_cycle_process_id`, `is_concept`) VALUES (13, 13, 1, 0, CURRENT_TIMESTAMP, 1, 1);
INSERT INTO life_cycle_instance(`id`, `portfolio_entry_id`, `is_active`, `deleted`, `last_update`, `life_cycle_process_id`, `is_concept`) VALUES (14, 14, 1, 0, CURRENT_TIMESTAMP, 1, 1);
INSERT INTO life_cycle_instance(`id`, `portfolio_entry_id`, `is_active`, `deleted`, `last_update`, `life_cycle_process_id`, `is_concept`) VALUES (15, 15, 1, 0, CURRENT_TIMESTAMP, 1, 1);
INSERT INTO life_cycle_instance(`id`, `portfolio_entry_id`, `is_active`, `deleted`, `last_update`, `life_cycle_process_id`, `is_concept`) VALUES (16, 16, 1, 0, CURRENT_TIMESTAMP, 1, 1);

-- Table life_cycle_instance_planning
INSERT INTO life_cycle_instance_planning(`id`, `life_cycle_instance_id`, `creation_date`, `version`, `is_frozen`, `deleted`, `name`, `comments`, `last_update`, `portfolio_entry_resource_plan_id`, `portfolio_entry_budget_id`) VALUES (1, 1, CURRENT_TIMESTAMP - INTERVAL 5 DAY, 1, 1, 0, 'Active Planning instance', null, CURRENT_TIMESTAMP, 1, 1);
INSERT INTO life_cycle_instance_planning(`id`, `life_cycle_instance_id`, `creation_date`, `version`, `is_frozen`, `deleted`, `name`, `comments`, `last_update`, `portfolio_entry_resource_plan_id`, `portfolio_entry_budget_id`) VALUES (2, 2, CURRENT_TIMESTAMP, 1, 0, 0, 'Active Planning instance', null, CURRENT_TIMESTAMP, 2, 2);
INSERT INTO life_cycle_instance_planning(`id`, `life_cycle_instance_id`, `creation_date`, `version`, `is_frozen`, `deleted`, `name`, `comments`, `last_update`, `portfolio_entry_resource_plan_id`, `portfolio_entry_budget_id`) VALUES (3, 3, CURRENT_TIMESTAMP, 1, 0, 0, 'Active Planning instance', null, CURRENT_TIMESTAMP, 3, 3);
INSERT INTO life_cycle_instance_planning(`id`, `life_cycle_instance_id`, `creation_date`, `version`, `is_frozen`, `deleted`, `name`, `comments`, `last_update`, `portfolio_entry_resource_plan_id`, `portfolio_entry_budget_id`) VALUES (4, 4, CURRENT_TIMESTAMP, 1, 0, 0, 'Active Planning instance', null, CURRENT_TIMESTAMP, 4, 4);
INSERT INTO life_cycle_instance_planning(`id`, `life_cycle_instance_id`, `creation_date`, `version`, `is_frozen`, `deleted`, `name`, `comments`, `last_update`, `portfolio_entry_resource_plan_id`, `portfolio_entry_budget_id`) VALUES (5, 5, CURRENT_TIMESTAMP, 1, 0, 0, 'Active Planning instance', null, CURRENT_TIMESTAMP, 5, 5);
INSERT INTO life_cycle_instance_planning(`id`, `life_cycle_instance_id`, `creation_date`, `version`, `is_frozen`, `deleted`, `name`, `comments`, `last_update`, `portfolio_entry_resource_plan_id`, `portfolio_entry_budget_id`) VALUES (6, 6, CURRENT_TIMESTAMP, 1, 0, 0, 'Active Planning instance', null, CURRENT_TIMESTAMP, 6, 6);
INSERT INTO life_cycle_instance_planning(`id`, `life_cycle_instance_id`, `creation_date`, `version`, `is_frozen`, `deleted`, `name`, `comments`, `last_update`, `portfolio_entry_resource_plan_id`, `portfolio_entry_budget_id`) VALUES (7, 7, CURRENT_TIMESTAMP, 1, 0, 0, 'Active Planning instance', null, CURRENT_TIMESTAMP, 7, 7);
INSERT INTO life_cycle_instance_planning(`id`, `life_cycle_instance_id`, `creation_date`, `version`, `is_frozen`, `deleted`, `name`, `comments`, `last_update`, `portfolio_entry_resource_plan_id`, `portfolio_entry_budget_id`) VALUES (8, 8, CURRENT_TIMESTAMP, 1, 0, 0, 'Active Planning instance', null, CURRENT_TIMESTAMP, 8, 8);
INSERT INTO life_cycle_instance_planning(`id`, `life_cycle_instance_id`, `creation_date`, `version`, `is_frozen`, `deleted`, `name`, `comments`, `last_update`, `portfolio_entry_resource_plan_id`, `portfolio_entry_budget_id`) VALUES (9, 9, CURRENT_TIMESTAMP, 1, 0, 0, 'Active Planning instance', null, CURRENT_TIMESTAMP, 9, 9);
INSERT INTO life_cycle_instance_planning(`id`, `life_cycle_instance_id`, `creation_date`, `version`, `is_frozen`, `deleted`, `name`, `comments`, `last_update`, `portfolio_entry_resource_plan_id`, `portfolio_entry_budget_id`) VALUES (10, 10, CURRENT_TIMESTAMP, 1, 0, 0, 'Active Planning instance', null, CURRENT_TIMESTAMP, 10, 10);
INSERT INTO life_cycle_instance_planning(`id`, `life_cycle_instance_id`, `creation_date`, `version`, `is_frozen`, `deleted`, `name`, `comments`, `last_update`, `portfolio_entry_resource_plan_id`, `portfolio_entry_budget_id`) VALUES (11, 11, CURRENT_TIMESTAMP, 1, 0, 0, 'Active Planning instance', null, CURRENT_TIMESTAMP, 11, 11);
INSERT INTO life_cycle_instance_planning(`id`, `life_cycle_instance_id`, `creation_date`, `version`, `is_frozen`, `deleted`, `name`, `comments`, `last_update`, `portfolio_entry_resource_plan_id`, `portfolio_entry_budget_id`) VALUES (12, 12, CURRENT_TIMESTAMP, 1, 0, 0, 'Active Planning instance', null, CURRENT_TIMESTAMP, 12, 12);
INSERT INTO life_cycle_instance_planning(`id`, `life_cycle_instance_id`, `creation_date`, `version`, `is_frozen`, `deleted`, `name`, `comments`, `last_update`, `portfolio_entry_resource_plan_id`, `portfolio_entry_budget_id`) VALUES (13, 13, CURRENT_TIMESTAMP, 1, 0, 0, 'Active Planning instance', null, CURRENT_TIMESTAMP, 13, 13);
INSERT INTO life_cycle_instance_planning(`id`, `life_cycle_instance_id`, `creation_date`, `version`, `is_frozen`, `deleted`, `name`, `comments`, `last_update`, `portfolio_entry_resource_plan_id`, `portfolio_entry_budget_id`) VALUES (14, 14, CURRENT_TIMESTAMP, 1, 0, 0, 'Active Planning instance', null, CURRENT_TIMESTAMP, 14, 14);
INSERT INTO life_cycle_instance_planning(`id`, `life_cycle_instance_id`, `creation_date`, `version`, `is_frozen`, `deleted`, `name`, `comments`, `last_update`, `portfolio_entry_resource_plan_id`, `portfolio_entry_budget_id`) VALUES (15, 15, CURRENT_TIMESTAMP, 1, 0, 0, 'Active Planning instance', null, CURRENT_TIMESTAMP, 15, 15);
INSERT INTO life_cycle_instance_planning(`id`, `life_cycle_instance_id`, `creation_date`, `version`, `is_frozen`, `deleted`, `name`, `comments`, `last_update`, `portfolio_entry_resource_plan_id`, `portfolio_entry_budget_id`) VALUES (16, 16, CURRENT_TIMESTAMP, 1, 0, 0, 'Active Planning instance', null, CURRENT_TIMESTAMP, 16, 16);
INSERT INTO life_cycle_instance_planning (`id`,`life_cycle_instance_id`,`creation_date`,`version`,`is_frozen`,`deleted`,`name`,`comments`,`last_update`,
`portfolio_entry_resource_plan_id`,`portfolio_entry_budget_id`) VALUES (17,1,CURRENT_TIMESTAMP - INTERVAL 4 DAY,2,1,0,'',NULL,CURRENT_TIMESTAMP,17,17);
INSERT INTO life_cycle_instance_planning (`id`,`life_cycle_instance_id`,`creation_date`,`version`,`is_frozen`,`deleted`,`name`,`comments`,`last_update`, `portfolio_entry_resource_plan_id`,`portfolio_entry_budget_id`) VALUES (18,1,CURRENT_TIMESTAMP - INTERVAL 3 DAY,3,1,0,'',NULL,CURRENT_TIMESTAMP,18,18);
INSERT INTO life_cycle_instance_planning (`id`,`life_cycle_instance_id`,`creation_date`,`version`,`is_frozen`,`deleted`,`name`,`comments`,`last_update`, `portfolio_entry_resource_plan_id`,`portfolio_entry_budget_id`) VALUES (19,1,CURRENT_TIMESTAMP - INTERVAL 2 DAY,4,1,0,'',NULL,CURRENT_TIMESTAMP,19,19);
INSERT INTO life_cycle_instance_planning (`id`,`life_cycle_instance_id`,`creation_date`,`version`,`is_frozen`,`deleted`,`name`,`comments`,`last_update`, `portfolio_entry_resource_plan_id`,`portfolio_entry_budget_id`) VALUES (20,1,CURRENT_TIMESTAMP - INTERVAL 1 DAY,5,1,0,'',NULL,CURRENT_TIMESTAMP,20,20);
INSERT INTO life_cycle_instance_planning (`id`,`life_cycle_instance_id`,`creation_date`,`version`,`is_frozen`,`deleted`,`name`,`comments`,`last_update`, `portfolio_entry_resource_plan_id`,`portfolio_entry_budget_id`) VALUES (21,1,CURRENT_TIMESTAMP,6,0,0,'',NULL,CURRENT_TIMESTAMP,21,21);


-- Table life_cycle_milestone
INSERT INTO life_cycle_milestone(`id`, `deleted`, `short_name`, `name`, `description`, `order`, `life_cycle_process_id`, `last_update`, `is_review_required`, `default_life_cycle_milestone_instance_status_type_id`) VALUES (1, 0, 'New', 'New', 'New', 1, 1, CURRENT_TIMESTAMP, 1, 1);
INSERT INTO life_cycle_milestone(`id`, `deleted`, `short_name`, `name`, `description`, `order`, `life_cycle_process_id`, `last_update`, `is_review_required`, `default_life_cycle_milestone_instance_status_type_id`) VALUES (2, 0, 'T-1', 'T-1', 'Initiation', 2, 1, CURRENT_TIMESTAMP, 0,  1);
INSERT INTO life_cycle_milestone(`id`, `deleted`, `short_name`, `name`, `description`, `order`, `life_cycle_process_id`, `last_update`, `is_review_required`, `default_life_cycle_milestone_instance_status_type_id`) VALUES (3, 0, 'T0', 'T0', null, 3, 1, CURRENT_TIMESTAMP, 1,  1);
INSERT INTO life_cycle_milestone(`id`, `deleted`, `short_name`, `name`, `description`, `order`, `life_cycle_process_id`, `last_update`, `is_review_required`, `default_life_cycle_milestone_instance_status_type_id`) VALUES (4, 0, 'T1', 'T1', null, 4, 1, CURRENT_TIMESTAMP, 1,  1);
INSERT INTO life_cycle_milestone(`id`, `deleted`, `short_name`, `name`, `description`, `order`, `life_cycle_process_id`, `last_update`, `is_review_required`, `default_life_cycle_milestone_instance_status_type_id`) VALUES (5, 0, 'T2', 'T2', null, 5, 1, CURRENT_TIMESTAMP, 1,  1);
INSERT INTO life_cycle_milestone(`id`, `deleted`, `short_name`, `name`, `description`, `order`, `life_cycle_process_id`, `last_update`, `is_review_required`, `default_life_cycle_milestone_instance_status_type_id`) VALUES (6, 0, 'T3', 'T3', null, 6, 1, CURRENT_TIMESTAMP, 0,  1);
INSERT INTO life_cycle_milestone(`id`, `deleted`, `short_name`, `name`, `description`, `order`, `life_cycle_process_id`, `last_update`, `is_review_required`, `default_life_cycle_milestone_instance_status_type_id`) VALUES (7, 0, 'T4', 'T4', null, 7, 1, CURRENT_TIMESTAMP, 1,  1);
INSERT INTO life_cycle_milestone(`id`, `deleted`, `short_name`, `name`, `description`, `order`, `life_cycle_process_id`, `last_update`, `is_review_required`, `default_life_cycle_milestone_instance_status_type_id`) VALUES (8, 0, 'Live', 'Live', 'Project is live', 8, 1, CURRENT_TIMESTAMP, 1,  1);
INSERT INTO life_cycle_milestone(`id`, `deleted`, `short_name`, `name`, `description`, `order`, `life_cycle_process_id`, `last_update`, `is_review_required`, `default_life_cycle_milestone_instance_status_type_id`) VALUES (9, 0, 'New', 'New', 'New project created (no milestone passed yet)', 1, 2, CURRENT_TIMESTAMP, 1,  1);
INSERT INTO life_cycle_milestone(`id`, `deleted`, `short_name`, `name`, `description`, `order`, `life_cycle_process_id`, `last_update`, `is_review_required`, `default_life_cycle_milestone_instance_status_type_id`) VALUES (10, 1, 'T-1', 'T-1', 'DELETED GATE', 2, 2, CURRENT_TIMESTAMP,1,  1);
INSERT INTO life_cycle_milestone(`id`, `deleted`, `short_name`, `name`, `description`, `order`, `life_cycle_process_id`, `last_update`, `is_review_required`, `default_life_cycle_milestone_instance_status_type_id`) VALUES (11, 0, 'T0', 'T0', null, 3, 2, CURRENT_TIMESTAMP, 1,  1);
INSERT INTO life_cycle_milestone(`id`, `deleted`, `short_name`, `name`, `description`, `order`, `life_cycle_process_id`, `last_update`, `is_review_required`, `default_life_cycle_milestone_instance_status_type_id`) VALUES (12, 0, 'Live', 'Live', 'Project is live', 6, 2, CURRENT_TIMESTAMP, 1,  1);
INSERT INTO life_cycle_milestone(`id`, `deleted`, `short_name`, `name`, `description`, `order`, `life_cycle_process_id`, `last_update`, `is_review_required`, `default_life_cycle_milestone_instance_status_type_id`) VALUES (13, 0, 'Live', 'Live', 'Project is live', 1, 4, CURRENT_TIMESTAMP, 1,  1);

-- Table life_cycle_milestone_approver
INSERT INTO life_cycle_milestone_approver(`life_cycle_milestone_id`, `actor_id`) VALUES (1,2);
INSERT INTO life_cycle_milestone_approver(`life_cycle_milestone_id`, `actor_id`) VALUES (1,3);
INSERT INTO life_cycle_milestone_approver(`life_cycle_milestone_id`, `actor_id`) VALUES (1,4);
INSERT INTO life_cycle_milestone_approver(`life_cycle_milestone_id`, `actor_id`) VALUES (1,5);
INSERT INTO life_cycle_milestone_approver(`life_cycle_milestone_id`, `actor_id`) VALUES (1,6);
INSERT INTO life_cycle_milestone_approver(`life_cycle_milestone_id`, `actor_id`) VALUES (1,7);
INSERT INTO life_cycle_milestone_approver(`life_cycle_milestone_id`, `actor_id`) VALUES (1,8);
INSERT INTO life_cycle_milestone_approver(`life_cycle_milestone_id`, `actor_id`) VALUES (6,1);
INSERT INTO life_cycle_milestone_approver(`life_cycle_milestone_id`, `actor_id`) VALUES (6,2);
INSERT INTO life_cycle_milestone_approver(`life_cycle_milestone_id`, `actor_id`) VALUES (6,3);
INSERT INTO life_cycle_milestone_approver(`life_cycle_milestone_id`, `actor_id`) VALUES (6,4);
INSERT INTO life_cycle_milestone_approver(`life_cycle_milestone_id`, `actor_id`) VALUES (6,5);
INSERT INTO life_cycle_milestone_approver(`life_cycle_milestone_id`, `actor_id`) VALUES (6,6);
INSERT INTO life_cycle_milestone_approver(`life_cycle_milestone_id`, `actor_id`) VALUES (6,7);
INSERT INTO life_cycle_milestone_approver(`life_cycle_milestone_id`, `actor_id`) VALUES (6,8);
INSERT INTO life_cycle_milestone_approver(`life_cycle_milestone_id`, `actor_id`) VALUES (6,9);
INSERT INTO life_cycle_milestone_approver(`life_cycle_milestone_id`, `actor_id`) VALUES (2, 1);
INSERT INTO life_cycle_milestone_approver(`life_cycle_milestone_id`, `actor_id`) VALUES (2, 2);
INSERT INTO life_cycle_milestone_approver(`life_cycle_milestone_id`, `actor_id`) VALUES (2, 6);

-- Table life_cycle_milestone_instance
INSERT INTO `life_cycle_milestone_instance` (`id`,`life_cycle_instance_id`,`life_cycle_milestone_id`,`passed_date`,`gate_comments`,`deleted`,`has_attachments`,`last_update`,`portfolio_entry_budget_id`,`life_cycle_milestone_instance_status_type_id`,`is_passed`,`portfolio_entry_resource_plan_id`) VALUES (1,1,1,CONCAT(CURRENT_DATE, " 00:00:00"),'',0,0,CONCAT(CURRENT_DATE, " 00:00:00"),1,1,1,1);
INSERT INTO `life_cycle_milestone_instance` (`id`,`life_cycle_instance_id`,`life_cycle_milestone_id`,`passed_date`,`gate_comments`,`deleted`,`has_attachments`,`last_update`,`portfolio_entry_budget_id`,`life_cycle_milestone_instance_status_type_id`,`is_passed`,`portfolio_entry_resource_plan_id`) VALUES (2,1,2,CONCAT(CURRENT_DATE + INTERVAL 30 DAY, " 00:00:00"),'',0,0,CONCAT(CURRENT_DATE + INTERVAL 30 DAY, " 00:00:00"),17,1,1,17);
INSERT INTO `life_cycle_milestone_instance` (`id`,`life_cycle_instance_id`,`life_cycle_milestone_id`,`passed_date`,`gate_comments`,`deleted`,`has_attachments`,`last_update`,`portfolio_entry_budget_id`,`life_cycle_milestone_instance_status_type_id`,`is_passed`,`portfolio_entry_resource_plan_id`) VALUES (3,1,3,CONCAT(CURRENT_DATE + INTERVAL 65 DAY, " 00:00:00"),'',0,0,CONCAT(CURRENT_DATE + INTERVAL 65 DAY, " 00:00:00"),18,1,1,18);
INSERT INTO `life_cycle_milestone_instance` (`id`,`life_cycle_instance_id`,`life_cycle_milestone_id`,`passed_date`,`gate_comments`,`deleted`,`has_attachments`,`last_update`,`portfolio_entry_budget_id`,`life_cycle_milestone_instance_status_type_id`,`is_passed`,`portfolio_entry_resource_plan_id`) VALUES (4,1,4,CONCAT(CURRENT_DATE + INTERVAL 90 DAY, " 00:00:00"),'',0,0,CONCAT(CURRENT_DATE + INTERVAL 90 DAY, " 00:00:00"),19,1,1,19);
INSERT INTO `life_cycle_milestone_instance` (`id`,`life_cycle_instance_id`,`life_cycle_milestone_id`,`passed_date`,`gate_comments`,`deleted`,`has_attachments`,`last_update`,`portfolio_entry_budget_id`,`life_cycle_milestone_instance_status_type_id`,`is_passed`,`portfolio_entry_resource_plan_id`) VALUES (5,1,5,CONCAT(CURRENT_DATE + INTERVAL 100 DAY, " 00:00:00"),'',0,0,CONCAT(CURRENT_DATE + INTERVAL 100 DAY, " 00:00:00"),20,1,1,20);

-- Table life_cycle_milestone_instance_approver
INSERT INTO `life_cycle_milestone_instance_approver` (`actor_id`,`life_cycle_milestone_instance_id`,`has_approved`,`comments`,`approval_date`,`id`,`deleted`,`last_update`) VALUES (1,2,NULL,NULL,NULL,1,0,CURRENT_TIMESTAMP);
INSERT INTO `life_cycle_milestone_instance_approver` (`actor_id`,`life_cycle_milestone_instance_id`,`has_approved`,`comments`,`approval_date`,`id`,`deleted`,`last_update`) VALUES (2,2,NULL,NULL,NULL,2,0,CURRENT_TIMESTAMP);
INSERT INTO `life_cycle_milestone_instance_approver` (`actor_id`,`life_cycle_milestone_instance_id`,`has_approved`,`comments`,`approval_date`,`id`,`deleted`,`last_update`) VALUES (6,2,NULL,NULL,NULL,3,0,CURRENT_TIMESTAMP);

-- Table life_cycle_milestone_instance_status_type
INSERT INTO life_cycle_milestone_instance_status_type(`id`, `deleted`, `name`, `description`, `is_approved`, `selectable`, `last_update`) VALUES (5, 1, 'DELETED STATUS', 'DELETED MILESTONE STATUS', 0, 1, CURRENT_TIMESTAMP);
INSERT INTO life_cycle_milestone_instance_status_type(`id`, `deleted`, `name`, `description`, `is_approved`, `selectable`, `last_update`) VALUES (6, 0, 'NO SELECTABLE STATUS', 'NOT SELECTABLE MILESTONE STATUS', 0, 0, CURRENT_TIMESTAMP);

-- Table life_cycle_phase
INSERT INTO life_cycle_phase (`name`,`life_cycle_process_id`,`order`,`start_life_cycle_milestone_id`,`end_life_cycle_milestone_id`,`last_update`,`gap_days_start`,`gap_days_end`) VALUES ('Initiation', 1, 1, 1, 3, NOW(), 0, 0);
INSERT INTO life_cycle_phase (`name`,`life_cycle_process_id`,`order`,`start_life_cycle_milestone_id`,`end_life_cycle_milestone_id`,`last_update`,`gap_days_start`,`gap_days_end`) VALUES ('Execution', 1, 2, 3, 6, NOW(), 1, 0);
INSERT INTO life_cycle_phase (`name`,`life_cycle_process_id`,`order`,`start_life_cycle_milestone_id`,`end_life_cycle_milestone_id`,`last_update`,`gap_days_start`,`gap_days_end`) VALUES ('Closure', 1, 2, 6, 8, NOW(), 1, 0);
INSERT INTO life_cycle_phase (`name`,`life_cycle_process_id`,`order`,`start_life_cycle_milestone_id`,`end_life_cycle_milestone_id`,`last_update`,`gap_days_start`,`gap_days_end`) VALUES ('iTTM.execution', 2, 1, 9, 12, NOW(), 0, 0);
INSERT INTO i18n_messages(`key`,`language`,`value`) VALUES ('iTTM.execution', 'en', 'Execution');

-- Table life_cycle_process
INSERT INTO life_cycle_process(`id`, `deleted`, `short_name`, `name`, `description`, `is_active`, `last_update`) VALUES (1, 0, 'TTM', 'Marketing board', 'Marketing board Governance life cycle', 1, CURRENT_TIMESTAMP);
INSERT INTO life_cycle_process(`id`, `deleted`, `short_name`, `name`, `description`, `is_active`, `last_update`) VALUES (2, 0, 'iTTM', 'Technical board', 'Technical board Governance life cycle ', 1, CURRENT_TIMESTAMP);

-- Table 'org_unit'
INSERT INTO org_unit(`id`, `name`, `deleted`, `is_active`, `ref_id`, `can_sponsor`, `org_unit_type_id`, `parent_id`, `last_update`, `manager_id`) VALUES (1, 'Marketing', 0, 1, 'MARKETING', 1,  2, null, CURRENT_TIMESTAMP, 6);
INSERT INTO org_unit(`id`, `name`, `deleted`, `is_active`, `ref_id`, `can_sponsor`, `org_unit_type_id`, `parent_id`, `last_update`, `manager_id`) VALUES (2, 'IT', 0, 1, 'IT', 0, 2, null, CURRENT_TIMESTAMP, 9);
INSERT INTO org_unit(`id`, `name`, `deleted`, `is_active`, `ref_id`, `can_sponsor`, `org_unit_type_id`, `parent_id`, `last_update`, `manager_id`) VALUES (3, 'Sales', 0, 1, 'SALES', 1, 2, null, CURRENT_TIMESTAMP, null);
INSERT INTO org_unit(`id`, `name`, `deleted`, `is_active`, `ref_id`, `can_sponsor`, `org_unit_type_id`, `parent_id`, `last_update`, `manager_id`) VALUES (4, 'Consumer Marketing', 0, 1, 'MARKETING_CONSUMER', 1, 3, 1, CURRENT_TIMESTAMP, null);
INSERT INTO org_unit(`id`, `name`, `deleted`, `is_active`, `ref_id`, `can_sponsor`, `org_unit_type_id`, `parent_id`, `last_update`, `manager_id`) VALUES (5, 'Communications', 0, 1, 'MARKETING_COMMUNICATIONS', 1, 3, 1, CURRENT_TIMESTAMP, null);
INSERT INTO org_unit(`id`, `name`, `deleted`, `is_active`, `ref_id`, `can_sponsor`, `org_unit_type_id`, `parent_id`, `last_update`, `manager_id`) VALUES (6, 'Products', 0, 1, 'PRODUCTS', 1, 4, 4, CURRENT_TIMESTAMP, null);
INSERT INTO org_unit(`id`, `name`, `deleted`, `is_active`, `ref_id`, `can_sponsor`, `org_unit_type_id`, `parent_id`, `last_update`, `manager_id`) VALUES (7, 'Offers', 0, 1, 'OFFERS', 1, 4, 1, CURRENT_TIMESTAMP, null);
INSERT INTO org_unit(`id`, `name`, `deleted`, `is_active`, `ref_id`, `can_sponsor`, `org_unit_type_id`, `parent_id`, `last_update`, `manager_id`) VALUES (8, 'Operations', 0, 1, 'OPS', 0, 3, 2, CURRENT_TIMESTAMP, 2);
INSERT INTO org_unit(`id`, `name`, `deleted`, `is_active`, `ref_id`, `can_sponsor`, `org_unit_type_id`, `parent_id`, `last_update`, `manager_id`) VALUES (9, 'Development', 0, 1, 'DEV', 0, 3, 2, CURRENT_TIMESTAMP, 1);
INSERT INTO org_unit(`id`, `name`, `deleted`, `is_active`, `ref_id`, `can_sponsor`, `org_unit_type_id`, `parent_id`, `last_update`, `manager_id`) VALUES (10, 'DELETED ORG UNIT', 1, 1, 'MARKETING_DELETED', 1, 2, NULL, CURRENT_TIMESTAMP, null);
INSERT INTO org_unit(`id`, `name`, `deleted`, `is_active`, `ref_id`, `can_sponsor`, `org_unit_type_id`, `parent_id`, `last_update`, `manager_id`) VALUES (11, 'NOT ACTIVE ORG UNIT', 0, 0, 'MARKETING_DISBANDED', 0, 2, NULL, CURRENT_TIMESTAMP, null);
INSERT INTO org_unit(`id`, `name`, `deleted`, `is_active`, `ref_id`, `can_sponsor`, `org_unit_type_id`, `parent_id`, `last_update`, `manager_id`) VALUES (12, 'DELETED AND NOT ACTIVE ORG UNIT', 1, 0, 'MARKETING_DEL_DISBANDED', 1, 2, NULL, CURRENT_TIMESTAMP, null);

-- Table `org_unit_type`
INSERT INTO org_unit_type(`id`, `name`, `description`, `deleted`, `selectable`, `last_update`) VALUES (5, 'DELETED ORG UNIT TYPE', 'DELETED ORG UNIT TYPE', 1, 1, CURRENT_TIMESTAMP);
INSERT INTO org_unit_type(`id`, `name`, `description`, `deleted`, `selectable`, `last_update`) VALUES (6, 'ACTIVE NOT SELECTABLE TYPE', 'ACTIVE BUT NOT SELECTABLE ORG UNIT TYPE', 0, 0, CURRENT_TIMESTAMP);
INSERT INTO org_unit_type(`id`, `name`, `description`, `deleted`, `selectable`, `last_update`) VALUES (7, 'DELETED NOT SELECTABLE TYPE', 'DELETED AND NOT SELECTABLE ORG UNIT TYPE', 1, 0, CURRENT_TIMESTAMP);

-- Table life_cycle_milestone_instance
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE - INTERVAL 10 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP - INTERVAL 10 DAY, 1, 1);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 30 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP - INTERVAL 10 DAY, 1, 2);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 70 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP - INTERVAL 10 DAY, 1, 3);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 90 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP - INTERVAL 10 DAY, 1, 4);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 100 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP - INTERVAL 10 DAY, 1, 5);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 120 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP - INTERVAL 10 DAY, 1, 6);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 140 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP - INTERVAL 10 DAY, 1, 7);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 190 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP - INTERVAL 10 DAY, 1, 8);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE - INTERVAL 10 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 2, 1);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 30 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 2, 2);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 70 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 2, 3);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 90 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 2, 4);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 100 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 2, 5);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 120 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 2, 6);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 140 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 2, 7);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 190 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 2, 8);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE - INTERVAL 10 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 3, 1);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 30 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 3, 2);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 70 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 3, 3);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 90 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 3, 4);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 100 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 3, 5);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 120 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 3, 6);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 140 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 3, 7);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 190 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 3, 8);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE - INTERVAL 10 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 4, 1);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 30 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 4, 2);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 70 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 4, 3);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 90 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 4, 4);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 100 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 4, 5);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 120 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 4, 6);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 140 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 4, 7);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 190 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 4, 8);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE - INTERVAL 10 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 5, 1);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 30 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 5, 2);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 70 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 5, 3);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 90 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 5, 4);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 100 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 5, 5);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 120 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 5, 6);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 140 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 5, 7);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 190 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 5, 8);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE - INTERVAL 10 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 6, 1);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 30 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 6, 2);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 70 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 6, 3);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 90 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 6, 4);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 100 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 6, 5);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 120 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 6, 6);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 140 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 6, 7);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 190 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 6, 8);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE - INTERVAL 10 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 7, 1);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 30 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 7, 2);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 70 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 7, 3);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 90 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 7, 4);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 100 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 7, 5);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 120 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 7, 6);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 140 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 7, 7);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 190 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 7, 8);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE - INTERVAL 10 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 8, 1);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 30 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 8, 2);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 70 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 8, 3);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 90 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 8, 4);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 100 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 8, 5);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 120 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 8, 6);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 140 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 8, 7);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 190 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 8, 8);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE - INTERVAL 10 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 9, 1);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 30 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 9, 2);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 70 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 9, 3);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 90 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 9, 4);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 100 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 9, 5);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 120 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 9, 6);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 140 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 9, 7);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 190 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 9, 8);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE - INTERVAL 10 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 10, 1);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 30 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 10, 2);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 70 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 10, 3);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 90 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 10, 4);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 100 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 10, 5);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 120 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 10, 6);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 140 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 10, 7);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 190 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 10, 8);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE - INTERVAL 10 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 11, 1);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 30 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 11, 2);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 70 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 11, 3);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 90 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 11, 4);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 100 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 11, 5);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 120 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 11, 6);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 140 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 11, 7);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 190 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 11, 8);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE - INTERVAL 10 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 12, 1);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 30 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 12, 2);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 70 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 12, 3);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 90 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 12, 4);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 100 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 12, 5);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 120 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 12, 6);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 140 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 12, 7);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 190 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 12, 8);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE - INTERVAL 10 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 13, 1);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 30 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 13, 2);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 70 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 13, 3);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 90 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 13, 4);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 100 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 13, 5);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 120 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 13, 6);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 140 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 13, 7);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 190 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 13, 8);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE - INTERVAL 10 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 14, 1);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 30 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 14, 2);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 70 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 14, 3);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 90 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 14, 4);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 100 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 14, 5);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 120 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 14, 6);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 140 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 14, 7);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 190 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 14, 8);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE - INTERVAL 10 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 15, 1);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 30 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 15, 2);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 70 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 15, 3);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 90 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 15, 4);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 100 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 15, 5);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 120 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 15, 6);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 140 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 15, 7);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 190 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 15, 8);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE - INTERVAL 10 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 16, 1);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 30 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 16, 2);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 70 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 16, 3);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 90 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 16, 4);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 100 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 16, 5);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 120 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 16, 6);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 140 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 16, 7);
INSERT INTO planned_life_cycle_milestone_instance(`planned_date`, `deleted`, `last_update`, `life_cycle_instance_planning_id`, `life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 190 DAY, " 00:00:00"), 0, CURRENT_TIMESTAMP, 16, 8);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 35 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 5 DAY,17,2);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 75 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 5 DAY,17,3);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 95 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 5 DAY,17,4);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 105 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 5 DAY,17,5);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 125 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 5 DAY,17,6);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 145 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 5 DAY,17,7);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 195 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 5 DAY,17,8);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 75 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 4 DAY,18,3);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 95 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 4 DAY,18,4);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 105 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 4 DAY,18,5);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 125 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 4 DAY,18,6);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 145 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 4 DAY,18,7);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 195 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 4 DAY,18,8);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 90 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 2 DAY,19,4);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 100 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 2 DAY,19,5);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 120 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 2 DAY,19,6);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 140 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 2 DAY,19,7);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 190 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 2 DAY,19,8);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 100 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 1 DAY,20,5);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 125 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 1 DAY,20,6);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 140 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 1 DAY,20,7);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 190 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 1 DAY,20,8);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 125 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 1 DAY,21,6);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 140 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 1 DAY,21,7);
INSERT INTO planned_life_cycle_milestone_instance (`planned_date`,`deleted`,`last_update`,`life_cycle_instance_planning_id`,`life_cycle_milestone_id`) VALUES (CONCAT(CURRENT_DATE + INTERVAL 190 DAY, " 00:00:00"),0,CURRENT_TIMESTAMP - INTERVAL 1 DAY,21,8);

-- Table portfolio
INSERT INTO portfolio(`id`, `deleted`, `is_active`, `ref_id`, `name`, `last_update`, `portfolio_type_id`, `manager_id`) VALUES (1, 0, 1, 'TRANS_PROG', 'Transformation Program', CURRENT_TIMESTAMP, 1, (SELECT id FROM actor WHERE uid='test_aexco'));
INSERT INTO portfolio(`id`, `deleted`, `is_active`, `ref_id`, `name`, `last_update`, `portfolio_type_id`, `manager_id`) VALUES (2, 0, 1, 'MARKETING_PORTFOLIO', 'Marketing Portfolio', CURRENT_TIMESTAMP, 2, (SELECT id FROM actor WHERE uid='test_aexco'));
INSERT INTO portfolio(`id`, `deleted`, `is_active`, `ref_id`, `name`, `last_update`, `portfolio_type_id`, `manager_id`) VALUES (3, 0, 1, 'CONFIDENTIAL_PROGRAM', 'Confidential Program', CURRENT_TIMESTAMP, 1, (SELECT id FROM actor WHERE uid='test_sall'));
INSERT INTO portfolio(`id`, `deleted`, `is_active`, `ref_id`, `name`, `last_update`, `portfolio_type_id`, `manager_id`) VALUES (4, 1, 1, 'DELETED_BUT_ACTIVE_PROGRAM', 'DELETED BUT ACTIVE PROGRAM', CURRENT_TIMESTAMP, 1, (SELECT id FROM actor WHERE uid='test_aexco'));
INSERT INTO portfolio(`id`, `deleted`, `is_active`, `ref_id`, `name`, `last_update`, `portfolio_type_id`, `manager_id`) VALUES (5, 0, 0, 'INACTIVE_PROGRAM', 'INACTIVE PROGRAM', CURRENT_TIMESTAMP, 1, (SELECT id FROM actor WHERE uid='test_aexco'));

-- Table portfolio_entry
INSERT INTO portfolio_entry(`id`, `deleted`, `ref_id`, `governance_id`, `erp_ref_id`, `name`, `description`, `creation_date`, `manager_id`, `sponsoring_unit_id`, `is_public`, `archived`, `last_update`, `portfolio_entry_type_id`, `last_portfolio_entry_report_id`, `active_life_cycle_instance_id`, `last_approved_life_cycle_milestone_instance_id`) VALUES (1, 0, 'PROJ001', 'PROJ1', '123', 'CRM upgrade', 'Upgrade of Company CRM system', CURRENT_TIMESTAMP, 3, 7, 1, 0, CURRENT_TIMESTAMP, 1, 4, 1, 5);
INSERT INTO portfolio_entry(`id`, `deleted`, `ref_id`, `governance_id`, `erp_ref_id`, `name`, `description`, `creation_date`, `manager_id`, `sponsoring_unit_id`, `is_public`, `archived`, `last_update`, `portfolio_entry_type_id`, `last_portfolio_entry_report_id`, `active_life_cycle_instance_id`, `last_approved_life_cycle_milestone_instance_id`) VALUES (2, 0, 'PROJ002', null, 'GOV02', 'Improve cross-up selling in Call Center', 'Increase revenues generated by CSR', CURRENT_TIMESTAMP, 3, 6, 1, 0, CURRENT_TIMESTAMP, 1, 16, 2, null);
INSERT INTO portfolio_entry(`id`, `deleted`, `ref_id`, `governance_id`, `erp_ref_id`, `name`, `description`, `creation_date`, `manager_id`, `sponsoring_unit_id`, `is_public`, `archived`, `last_update`, `portfolio_entry_type_id`, `last_portfolio_entry_report_id`, `active_life_cycle_instance_id`, `last_approved_life_cycle_milestone_instance_id`) VALUES (3, 0, null, 'ER1', 'GOV03', 'Add new field to registration form', 'We are missing the customer birthdate. This need to be added', CURRENT_TIMESTAMP, 2, 6, 1, 0, CURRENT_TIMESTAMP, 2, 17, 3, null);
INSERT INTO portfolio_entry(`id`, `deleted`, `ref_id`, `governance_id`, `erp_ref_id`, `name`, `description`, `creation_date`, `manager_id`, `sponsoring_unit_id`, `is_public`, `archived`, `last_update`, `portfolio_entry_type_id`, `last_portfolio_entry_report_id`, `active_life_cycle_instance_id`, `last_approved_life_cycle_milestone_instance_id`) VALUES (4, 1, 'PROJ003', 'PRO3', null, 'DELETED PROJECT', 'DELETED PROJECT', CURRENT_TIMESTAMP, 5, 6, 1, 0, CURRENT_TIMESTAMP, 1, null, 4, null);
INSERT INTO portfolio_entry(`id`, `deleted`, `ref_id`, `governance_id`, `erp_ref_id`, `name`, `description`, `creation_date`, `manager_id`, `sponsoring_unit_id`, `is_public`, `archived`, `last_update`, `portfolio_entry_type_id`, `last_portfolio_entry_report_id`, `active_life_cycle_instance_id`, `last_approved_life_cycle_milestone_instance_id`) VALUES (5, 0, 'PROJ004', 'PRO4', null, 'Archived project', 'Archived project', CURRENT_TIMESTAMP, 3, 6, 1, 1, CURRENT_TIMESTAMP, 1, null, 5, null);
INSERT INTO portfolio_entry(`id`, `deleted`, `ref_id`, `governance_id`, `erp_ref_id`, `name`, `description`, `creation_date`, `manager_id`, `sponsoring_unit_id`, `is_public`, `archived`, `last_update`, `portfolio_entry_type_id`, `last_portfolio_entry_report_id`, `active_life_cycle_instance_id`, `last_approved_life_cycle_milestone_instance_id`) VALUES (6, 0, 'PROJ006', 'PRO6', null, 'Confidential Project', 'Confidential project', CURRENT_TIMESTAMP, 3, 3, 0, 0, CURRENT_TIMESTAMP, 1, null, 6, null);
INSERT INTO portfolio_entry(`id`, `deleted`, `ref_id`, `governance_id`, `erp_ref_id`, `name`, `description`, `creation_date`, `manager_id`, `sponsoring_unit_id`, `is_public`, `archived`, `last_update`, `portfolio_entry_type_id`, `last_portfolio_entry_report_id`, `active_life_cycle_instance_id`, `last_approved_life_cycle_milestone_instance_id`) VALUES (7, 0, 'MIG001', 'Migration', null, 'Migration to new Active directory', 'Migration to new Active directory as the existing one is out of support', CURRENT_TIMESTAMP, 2, 6, 1, 0, CURRENT_TIMESTAMP, 3, 18, 7, null);
INSERT INTO portfolio_entry(`id`, `deleted`, `ref_id`, `governance_id`, `erp_ref_id`, `name`, `description`, `creation_date`, `manager_id`, `sponsoring_unit_id`, `is_public`, `archived`, `last_update`, `portfolio_entry_type_id`, `last_portfolio_entry_report_id`, `active_life_cycle_instance_id`, `last_approved_life_cycle_milestone_instance_id`) VALUES (8, 0, '04324432543', 'PRO7', null, 'ERP V2.3', 'Upgrade of the ERP system', CURRENT_TIMESTAMP, 2, 6, 1, 0, CURRENT_TIMESTAMP, 1, null, 8, null);
INSERT INTO portfolio_entry(`id`, `deleted`, `ref_id`, `governance_id`, `erp_ref_id`, `name`, `description`, `creation_date`, `manager_id`, `sponsoring_unit_id`, `is_public`, `archived`, `last_update`, `portfolio_entry_type_id`, `last_portfolio_entry_report_id`, `active_life_cycle_instance_id`, `last_approved_life_cycle_milestone_instance_id`) VALUES (9, 0, 'PROJ007', '9485439853543', null, 'New product - Twitter++', 'Build up a concurrent to Twitter', CURRENT_TIMESTAMP, 2, 6, 1, 0, CURRENT_TIMESTAMP, 1, null, 9, null);
INSERT INTO portfolio_entry(`id`, `deleted`, `ref_id`, `governance_id`, `erp_ref_id`, `name`, `description`, `creation_date`, `manager_id`, `sponsoring_unit_id`, `is_public`, `archived`, `last_update`, `portfolio_entry_type_id`, `last_portfolio_entry_report_id`, `active_life_cycle_instance_id`, `last_approved_life_cycle_milestone_instance_id`) VALUES (10, 0, 'PROJ008', 'PRO8', 'h665464364564366546765', 'Black Star', 'Black Star project', CURRENT_TIMESTAMP, 3, 6, 1, 0, CURRENT_TIMESTAMP, 1, 19, 10, null);
INSERT INTO portfolio_entry(`id`, `deleted`, `ref_id`, `governance_id`, `erp_ref_id`, `name`, `description`, `creation_date`, `manager_id`, `sponsoring_unit_id`, `is_public`, `archived`, `last_update`, `portfolio_entry_type_id`, `last_portfolio_entry_report_id`, `active_life_cycle_instance_id`, `last_approved_life_cycle_milestone_instance_id`) VALUES (11, 0, 'PROJ009', 'PRO9', null, 'Shop efficiency improvements', 'Improve the sales', CURRENT_TIMESTAMP, 3, 6, 1, 0, CURRENT_TIMESTAMP, 1, null, 11, null);
INSERT INTO portfolio_entry(`id`, `deleted`, `ref_id`, `governance_id`, `erp_ref_id`, `name`, `description`, `creation_date`, `manager_id`, `sponsoring_unit_id`, `is_public`, `archived`, `last_update`, `portfolio_entry_type_id`, `last_portfolio_entry_report_id`, `active_life_cycle_instance_id`, `last_approved_life_cycle_milestone_instance_id`) VALUES (12, 0, 'PROJ010', 'PRO10', null, 'Call Center Telephony', 'Call Center Telephony upgrade project', CURRENT_TIMESTAMP, 2, 6, 1, 0, CURRENT_TIMESTAMP, 1, null, 12, null);
INSERT INTO portfolio_entry(`id`, `deleted`, `ref_id`, `governance_id`, `erp_ref_id`, `name`, `description`, `creation_date`, `manager_id`, `sponsoring_unit_id`, `is_public`, `archived`, `last_update`, `portfolio_entry_type_id`, `last_portfolio_entry_report_id`, `active_life_cycle_instance_id`, `last_approved_life_cycle_milestone_instance_id`) VALUES (13, 0, 'PROJ011', 'PRO11', null, 'Replace TV in shops', 'Replace TV in all shops', CURRENT_TIMESTAMP, 2, 6, 1, 0, CURRENT_TIMESTAMP, 1, null, 13, null);
INSERT INTO portfolio_entry(`id`, `deleted`, `ref_id`, `governance_id`, `erp_ref_id`, `name`, `description`, `creation_date`, `manager_id`, `sponsoring_unit_id`, `is_public`, `archived`, `last_update`, `portfolio_entry_type_id`, `last_portfolio_entry_report_id`, `active_life_cycle_instance_id`, `last_approved_life_cycle_milestone_instance_id`) VALUES (14, 0, 'PROJ012', 'PRO12', null, 'OverSales', 'Sell more than the competition', CURRENT_TIMESTAMP, 2, 6, 1, 0, CURRENT_TIMESTAMP, 1, null, 14, null);
INSERT INTO portfolio_entry(`id`, `deleted`, `ref_id`, `governance_id`, `erp_ref_id`, `name`, `description`, `creation_date`, `manager_id`, `sponsoring_unit_id`, `is_public`, `archived`, `last_update`, `portfolio_entry_type_id`, `last_portfolio_entry_report_id`, `active_life_cycle_instance_id`, `last_approved_life_cycle_milestone_instance_id`) VALUES (15, 0, 'PROJ013', 'PRO13', null, 'Incentives management', 'Optimise the incentive amount to dealers', CURRENT_TIMESTAMP, 3, 6, 1, 0, CURRENT_TIMESTAMP, 1, null, 15, null);
INSERT INTO portfolio_entry(`id`, `deleted`, `ref_id`, `governance_id`, `erp_ref_id`, `name`, `description`, `creation_date`, `manager_id`, `sponsoring_unit_id`, `is_public`, `archived`, `last_update`, `portfolio_entry_type_id`, `last_portfolio_entry_report_id`, `active_life_cycle_instance_id`, `last_approved_life_cycle_milestone_instance_id`) VALUES (16, 0, 'PROJ014', 'PRO814', null, 'GLO v3.4', 'Upgrade of GLO platform to 3.4', CURRENT_TIMESTAMP, 2, 6, 1, 0, CURRENT_TIMESTAMP, 3, null, 16, null);

-- Table portfolio_entry_budget
INSERT INTO portfolio_entry_budget(`id`,`last_update`) VALUES (1, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_budget(`id`,`last_update`) VALUES (2, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_budget(`id`,`last_update`) VALUES (3, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_budget(`id`,`last_update`) VALUES (4, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_budget(`id`,`last_update`) VALUES (5, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_budget(`id`,`last_update`) VALUES (6, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_budget(`id`,`last_update`) VALUES (7, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_budget(`id`,`last_update`) VALUES (8, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_budget(`id`,`last_update`) VALUES (9, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_budget(`id`,`last_update`) VALUES (10, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_budget(`id`,`last_update`) VALUES (11, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_budget(`id`,`last_update`) VALUES (12, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_budget(`id`,`last_update`) VALUES (13, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_budget(`id`,`last_update`) VALUES (14, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_budget(`id`,`last_update`) VALUES (15, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_budget(`id`,`last_update`) VALUES (16, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_budget(`id`,`last_update`) VALUES (17, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_budget(`id`,`last_update`) VALUES (18, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_budget(`id`,`last_update`) VALUES (19, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_budget(`id`,`last_update`) VALUES (20, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_budget(`id`,`last_update`) VALUES (21, CURRENT_TIMESTAMP);

-- Table portfolio_entry_budget_line
INSERT INTO portfolio_entry_budget_line(`id`, `name`, `ref_id`, `is_opex`, `amount`, `currency_code`, `last_update`, `portfolio_entry_budget_id`, `budget_bucket_id`) VALUES (1, 'Development UI', 'DEVUI', 0, 20000, 'CHF', CURRENT_TIMESTAMP,1,3);
INSERT INTO portfolio_entry_budget_line(`id`, `name`, `ref_id`, `is_opex`, `amount`, `currency_code`, `last_update`, `portfolio_entry_budget_id`, `budget_bucket_id`) VALUES (2, 'Development backend', 'DEVB', 0, 50000, 'CHF', CURRENT_TIMESTAMP,1,3);
INSERT INTO portfolio_entry_budget_line(`id`, `name`, `ref_id`, `is_opex`, `amount`, `currency_code`, `last_update`, `portfolio_entry_budget_id`, `budget_bucket_id`) VALUES (3, 'Serers', 'SERV', 0, 400000, 'CHF', CURRENT_TIMESTAMP,1,3);
INSERT INTO portfolio_entry_budget_line(`id`, `name`, `ref_id`, `is_opex`, `amount`, `currency_code`, `last_update`, `portfolio_entry_budget_id`, `budget_bucket_id`) VALUES (4, 'Service Support', 'SERV', 1, 20000, 'CHF', CURRENT_TIMESTAMP,1,1);
INSERT INTO portfolio_entry_budget_line(`id`, `name`, `ref_id`, `is_opex`, `amount`, `currency_code`, `last_update`, `portfolio_entry_budget_id`, `budget_bucket_id`) VALUES (5, 'Development UI', 'UI', 0, 200000, 'CHF', CURRENT_TIMESTAMP,2,3);
INSERT INTO portfolio_entry_budget_line(`id`, `name`, `ref_id`, `is_opex`, `amount`, `currency_code`, `last_update`, `portfolio_entry_budget_id`, `budget_bucket_id`) VALUES (6, 'Development backend', 'BACK', 0, 40000, 'CHF', CURRENT_TIMESTAMP,2,3);
INSERT INTO portfolio_entry_budget_line(`id`, `name`, `ref_id`, `is_opex`, `amount`, `currency_code`, `last_update`, `portfolio_entry_budget_id`, `budget_bucket_id`) VALUES (7, 'Licence OS', 'LIC', 1, 5000, 'CHF', CURRENT_TIMESTAMP,2,3);
INSERT INTO portfolio_entry_budget_line(`id`, `name`, `ref_id`, `is_opex`, `amount`, `currency_code`, `last_update`, `portfolio_entry_budget_id`, `budget_bucket_id`) VALUES (8, 'Service', 'SERV', 1, 20000, 'CHF', CURRENT_TIMESTAMP,3,1);
INSERT INTO `portfolio_entry_budget_line` (`id`,`deleted`,`name`,`ref_id`,`is_opex`,`amount`,`gl_account`,`currency_code`,`budget_bucket_id`,`last_update`,`portfolio_entry_budget_id`) VALUES (9,0,'Development UI','DEVUI',0,20000.00,NULL,'CHF',3,CURRENT_TIMESTAMP,17);
INSERT INTO `portfolio_entry_budget_line` (`id`,`deleted`,`name`,`ref_id`,`is_opex`,`amount`,`gl_account`,`currency_code`,`budget_bucket_id`,`last_update`,`portfolio_entry_budget_id`) VALUES (10,0,'Development backend','DEVB',0,50000.00,NULL,'CHF',3,CURRENT_TIMESTAMP,17);
INSERT INTO `portfolio_entry_budget_line` (`id`,`deleted`,`name`,`ref_id`,`is_opex`,`amount`,`gl_account`,`currency_code`,`budget_bucket_id`,`last_update`,`portfolio_entry_budget_id`) VALUES (11,0,'Serers','SERV',0,400000.00,NULL,'CHF',3,CURRENT_TIMESTAMP,17);
INSERT INTO `portfolio_entry_budget_line` (`id`,`deleted`,`name`,`ref_id`,`is_opex`,`amount`,`gl_account`,`currency_code`,`budget_bucket_id`,`last_update`,`portfolio_entry_budget_id`) VALUES (12,0,'Service Support','SERV',1,20000.00,NULL,'CHF',1,CURRENT_TIMESTAMP,17);
INSERT INTO `portfolio_entry_budget_line` (`id`,`deleted`,`name`,`ref_id`,`is_opex`,`amount`,`gl_account`,`currency_code`,`budget_bucket_id`,`last_update`,`portfolio_entry_budget_id`) VALUES (13,0,'Development UI','DEVUI',0,10000.00,NULL,'CHF',3,CURRENT_TIMESTAMP,18);
INSERT INTO `portfolio_entry_budget_line` (`id`,`deleted`,`name`,`ref_id`,`is_opex`,`amount`,`gl_account`,`currency_code`,`budget_bucket_id`,`last_update`,`portfolio_entry_budget_id`) VALUES (14,0,'Development backend','DEVB',0,50000.00,NULL,'CHF',3,CURRENT_TIMESTAMP,18);
INSERT INTO `portfolio_entry_budget_line` (`id`,`deleted`,`name`,`ref_id`,`is_opex`,`amount`,`gl_account`,`currency_code`,`budget_bucket_id`,`last_update`,`portfolio_entry_budget_id`) VALUES (15,0,'Serers','SERV',0,400000.00,NULL,'CHF',3,CURRENT_TIMESTAMP,18);
INSERT INTO `portfolio_entry_budget_line` (`id`,`deleted`,`name`,`ref_id`,`is_opex`,`amount`,`gl_account`,`currency_code`,`budget_bucket_id`,`last_update`,`portfolio_entry_budget_id`) VALUES (16,0,'Service Support','SERV',1,30000.00,NULL,'CHF',1,CURRENT_TIMESTAMP,18);
INSERT INTO `portfolio_entry_budget_line` (`id`,`deleted`,`name`,`ref_id`,`is_opex`,`amount`,`gl_account`,`currency_code`,`budget_bucket_id`,`last_update`,`portfolio_entry_budget_id`) VALUES (17,0,'Development UI','DEVUI',0,10000.00,NULL,'CHF',3,CURRENT_TIMESTAMP,19);
INSERT INTO `portfolio_entry_budget_line` (`id`,`deleted`,`name`,`ref_id`,`is_opex`,`amount`,`gl_account`,`currency_code`,`budget_bucket_id`,`last_update`,`portfolio_entry_budget_id`) VALUES (18,0,'Development backend','DEVB',0,50000.00,NULL,'CHF',3,CURRENT_TIMESTAMP,19);
INSERT INTO `portfolio_entry_budget_line` (`id`,`deleted`,`name`,`ref_id`,`is_opex`,`amount`,`gl_account`,`currency_code`,`budget_bucket_id`,`last_update`,`portfolio_entry_budget_id`) VALUES (19,0,'Serers','SERV',0,400000.00,NULL,'CHF',3,CURRENT_TIMESTAMP,19);
INSERT INTO `portfolio_entry_budget_line` (`id`,`deleted`,`name`,`ref_id`,`is_opex`,`amount`,`gl_account`,`currency_code`,`budget_bucket_id`,`last_update`,`portfolio_entry_budget_id`) VALUES (20,0,'Service Support','SERV',1,30000.00,NULL,'CHF',1,CURRENT_TIMESTAMP,19);
INSERT INTO `portfolio_entry_budget_line` (`id`,`deleted`,`name`,`ref_id`,`is_opex`,`amount`,`gl_account`,`currency_code`,`budget_bucket_id`,`last_update`,`portfolio_entry_budget_id`) VALUES (21,0,'Development UI','DEVUI',0,10000.00,NULL,'CHF',3,CURRENT_TIMESTAMP,20);
INSERT INTO `portfolio_entry_budget_line` (`id`,`deleted`,`name`,`ref_id`,`is_opex`,`amount`,`gl_account`,`currency_code`,`budget_bucket_id`,`last_update`,`portfolio_entry_budget_id`) VALUES (22,0,'Development backend','DEVB',0,50000.00,NULL,'CHF',3,CURRENT_TIMESTAMP,20);
INSERT INTO `portfolio_entry_budget_line` (`id`,`deleted`,`name`,`ref_id`,`is_opex`,`amount`,`gl_account`,`currency_code`,`budget_bucket_id`,`last_update`,`portfolio_entry_budget_id`) VALUES (23,0,'Serers','SERV',0,400000.00,NULL,'CHF',3,CURRENT_TIMESTAMP,20);
INSERT INTO `portfolio_entry_budget_line` (`id`,`deleted`,`name`,`ref_id`,`is_opex`,`amount`,`gl_account`,`currency_code`,`budget_bucket_id`,`last_update`,`portfolio_entry_budget_id`) VALUES (24,0,'Service Support','SERV',1,30000.00,NULL,'CHF',1,CURRENT_TIMESTAMP,20);
INSERT INTO `portfolio_entry_budget_line` (`id`,`deleted`,`name`,`ref_id`,`is_opex`,`amount`,`gl_account`,`currency_code`,`budget_bucket_id`,`last_update`,`portfolio_entry_budget_id`) VALUES (25,0,'Development UI','DEVUI',0,10000.00,NULL,'CHF',3,CURRENT_TIMESTAMP,21);
INSERT INTO `portfolio_entry_budget_line` (`id`,`deleted`,`name`,`ref_id`,`is_opex`,`amount`,`gl_account`,`currency_code`,`budget_bucket_id`,`last_update`,`portfolio_entry_budget_id`) VALUES (26,0,'Development backend','DEVB',0,50000.00,NULL,'CHF',3,CURRENT_TIMESTAMP,21);
INSERT INTO `portfolio_entry_budget_line` (`id`,`deleted`,`name`,`ref_id`,`is_opex`,`amount`,`gl_account`,`currency_code`,`budget_bucket_id`,`last_update`,`portfolio_entry_budget_id`) VALUES (27,0,'Serers','SERV',0,400000.00,NULL,'CHF',3,CURRENT_TIMESTAMP,21);
INSERT INTO `portfolio_entry_budget_line` (`id`,`deleted`,`name`,`ref_id`,`is_opex`,`amount`,`gl_account`,`currency_code`,`budget_bucket_id`,`last_update`,`portfolio_entry_budget_id`) VALUES (28,0,'Service Support','SERV',1,30000.00,NULL,'CHF',1,CURRENT_TIMESTAMP,21);


-- Table portfolio_entry_event
INSERT INTO `portfolio_entry_event` (`last_update`, `creation_date`, `portfolio_entry_event_type_id`,`message`,`portfolio_entry_id`,`actor_id`)
VALUES(CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2, 'Following last Steering Committee, the project is delayed by 3 months',1,3);
INSERT INTO `portfolio_entry_event` (`last_update`, `creation_date`, `portfolio_entry_event_type_id`,`message`,`portfolio_entry_id`,`actor_id`)
VALUES(CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'Feasibilit study not at the expected quality level, need some rework before sending it for approval',1,3);

-- Table portfolio_entry_has_delivery_unit
INSERT INTO portfolio_entry_has_delivery_unit(portfolio_entry_id, org_unit_id) VALUES (1, 8);
INSERT INTO portfolio_entry_has_delivery_unit(portfolio_entry_id, org_unit_id) VALUES (1, 9);
INSERT INTO portfolio_entry_has_delivery_unit(portfolio_entry_id, org_unit_id) VALUES (1, 4);
INSERT INTO portfolio_entry_has_delivery_unit(portfolio_entry_id, org_unit_id) VALUES (2, 9);
INSERT INTO portfolio_entry_has_delivery_unit(portfolio_entry_id, org_unit_id) VALUES (2, 7);
INSERT INTO portfolio_entry_has_delivery_unit(portfolio_entry_id, org_unit_id) VALUES (2, 5);

-- Table portfolio_entry_planning_package
INSERT INTO `portfolio_entry_planning_package` (`id`,`last_update`,`name`,`start_date`,`end_date`,`portfolio_entry_planning_package_type_id`,`portfolio_entry_id`,`portfolio_entry_planning_package_group_id`,`status`) VALUES (1, CURRENT_TIMESTAMP, 'Business Requirements', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 20 DAY, 1, 1, NULL, 'ON_GOING');
INSERT INTO `portfolio_entry_planning_package` (`id`,`last_update`,`name`,`start_date`,`end_date`,`portfolio_entry_planning_package_type_id`,`portfolio_entry_id`,`portfolio_entry_planning_package_group_id`,`status`) VALUES (2, CURRENT_TIMESTAMP, 'Business Requirements sign-off', NULL , CURRENT_TIMESTAMP + INTERVAL 21 DAY, 1, 1, NULL, 'NOT_STARTED');
INSERT INTO `portfolio_entry_planning_package` (`id`,`last_update`,`name`,`start_date`,`end_date`,`portfolio_entry_planning_package_type_id`,`portfolio_entry_id`,`portfolio_entry_planning_package_group_id`,`status`) VALUES (3, CURRENT_TIMESTAMP, 'Feasibility study', CURRENT_TIMESTAMP + INTERVAL 22 DAY, CURRENT_TIMESTAMP + INTERVAL 40 DAY, 1, 1, NULL, 'NOT_STARTED');
INSERT INTO `portfolio_entry_planning_package` (`id`,`last_update`,`name`,`start_date`,`end_date`,`portfolio_entry_planning_package_type_id`,`portfolio_entry_id`,`portfolio_entry_planning_package_group_id`,`status`) VALUES (4, CURRENT_TIMESTAMP, 'Feasibility study sign-off', NULL, CURRENT_TIMESTAMP + INTERVAL 41 DAY, 1, 1, NULL, 'NOT_STARTED');
INSERT INTO `portfolio_entry_planning_package` (`id`,`last_update`,`name`,`start_date`,`end_date`,`portfolio_entry_planning_package_type_id`,`portfolio_entry_id`,`portfolio_entry_planning_package_group_id`,`status`) VALUES (5, CURRENT_TIMESTAMP, 'Development', CURRENT_TIMESTAMP + INTERVAL 42 DAY, CURRENT_TIMESTAMP + INTERVAL 80 DAY, 1, 1, NULL, 'NOT_STARTED');
INSERT INTO `portfolio_entry_planning_package` (`id`,`last_update`,`name`,`start_date`,`end_date`,`portfolio_entry_planning_package_type_id`,`portfolio_entry_id`,`portfolio_entry_planning_package_group_id`,`status`) VALUES (6, CURRENT_TIMESTAMP, 'System Tests', CURRENT_TIMESTAMP + INTERVAL 81 DAY, CURRENT_TIMESTAMP + INTERVAL 100 DAY, 1, 1, NULL, 'NOT_STARTED');
INSERT INTO `portfolio_entry_planning_package` (`id`,`last_update`,`name`,`start_date`,`end_date`,`portfolio_entry_planning_package_type_id`,`portfolio_entry_id`,`portfolio_entry_planning_package_group_id`,`status`) VALUES (7, CURRENT_TIMESTAMP, 'Sprint 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 14 DAY, 1, 2, NULL, 'ON_GOING');
INSERT INTO `portfolio_entry_planning_package` (`id`,`last_update`,`name`,`start_date`,`end_date`,`portfolio_entry_planning_package_type_id`,`portfolio_entry_id`,`portfolio_entry_planning_package_group_id`,`status`) VALUES (8, CURRENT_TIMESTAMP, 'Sprint 2', CURRENT_TIMESTAMP + INTERVAL 14 DAY, CURRENT_TIMESTAMP + INTERVAL 28 DAY, 1, 2, NULL, 'NOT_STARTED');
INSERT INTO `portfolio_entry_planning_package` (`id`,`last_update`,`name`,`start_date`,`end_date`,`portfolio_entry_planning_package_type_id`,`portfolio_entry_id`,`portfolio_entry_planning_package_group_id`,`status`) VALUES (9, CURRENT_TIMESTAMP, 'Sprint 3', CURRENT_TIMESTAMP + INTERVAL 28 DAY, CURRENT_TIMESTAMP + INTERVAL 42 DAY, 1, 2, NULL, 'NOT_STARTED');

-- Table portfolio_entry_planning_package_group
INSERT INTO `portfolio_entry_planning_package_group`(`id`,`last_update`,`name`,`description`) VALUES (15, CURRENT_TIMESTAMP, 'Project Management', 'PM related planning packages like project plan.');
INSERT INTO `portfolio_entry_planning_package_group`(`id`,`last_update`,`name`,`description`) VALUES (16, CURRENT_TIMESTAMP, 'Initiation', 'Initiation related planning packages like business requirements.');
INSERT INTO `portfolio_entry_planning_package_group`(`id`,`last_update`,`name`,`description`) VALUES (17, CURRENT_TIMESTAMP, 'Implementation', 'Implementation related planning packages like software code and test cases.');

-- Table  portfolio_entry_planning_package_pattern
INSERT INTO `portfolio_entry_planning_package_pattern`(`id`,`last_update`,`name`,`description`,`order`,`portfolio_entry_planning_package_type_id`,`portfolio_entry_planning_package_group_id`) VALUES (69, CURRENT_TIMESTAMP, 'Project Plan', 'The project plan of the initiative', 1, 1, 15);
INSERT INTO `portfolio_entry_planning_package_pattern`(`id`,`last_update`,`name`,`description`,`order`,`portfolio_entry_planning_package_type_id`,`portfolio_entry_planning_package_group_id`) VALUES (70, CURRENT_TIMESTAMP, 'Stakeholders analysis', 'Lsit of the stakeholders of the initiative', 1, 1, 15);
INSERT INTO `portfolio_entry_planning_package_pattern`(`id`,`last_update`,`name`,`description`,`order`,`portfolio_entry_planning_package_type_id`,`portfolio_entry_planning_package_group_id`) VALUES (71, CURRENT_TIMESTAMP, 'Business requirements', 'The business requirements document coming from the initiative sponsor', 1, 1, 16);
INSERT INTO `portfolio_entry_planning_package_pattern`(`id`,`last_update`,`name`,`description`,`order`,`portfolio_entry_planning_package_type_id`,`portfolio_entry_planning_package_group_id`) VALUES (72, CURRENT_TIMESTAMP, 'Feasibility study', 'The feasibility study, including detailed costs of the initiative', 1, 1, 16);
INSERT INTO `portfolio_entry_planning_package_pattern`(`id`,`last_update`,`name`,`description`,`order`,`portfolio_entry_planning_package_type_id`,`portfolio_entry_planning_package_group_id`) VALUES (73, CURRENT_TIMESTAMP, 'Software code', NULL, 1, 1, 17);
INSERT INTO `portfolio_entry_planning_package_pattern`(`id`,`last_update`,`name`,`description`,`order`,`portfolio_entry_planning_package_type_id`,`portfolio_entry_planning_package_group_id`) VALUES (74, CURRENT_TIMESTAMP, 'Integration API', NULL, 1, 1, 17);
INSERT INTO `portfolio_entry_planning_package_pattern`(`id`,`last_update`,`name`,`description`,`order`,`portfolio_entry_planning_package_type_id`,`portfolio_entry_planning_package_group_id`) VALUES (75, CURRENT_TIMESTAMP, 'System test cases', NULL, 1, 1, 17);

-- Table portfolio_entry_report
INSERT INTO portfolio_entry_report(`id`, `creation_date`, `publication_date`, `comments`, `author_id`, `is_published`, `deleted`, `last_update`, `portfolio_entry_report_status_type_id`, `portfolio_entry_id`) VALUES(1, CURRENT_TIMESTAMP - INTERVAL 50 DAY, CURRENT_TIMESTAMP - INTERVAL 50 DAY, 'Project started. Resources assigned. Writting of the business requirements on-going.', 3, 1, 0, CURRENT_TIMESTAMP - INTERVAL 50 DAY, 3, 1);
INSERT INTO portfolio_entry_report(`id`, `creation_date`, `publication_date`, `comments`, `author_id`, `is_published`, `deleted`, `last_update`, `portfolio_entry_report_status_type_id`, `portfolio_entry_id`) VALUES(2, CURRENT_TIMESTAMP - INTERVAL 40 DAY, CURRENT_TIMESTAMP - INTERVAL 40 DAY, 'Good progress in the specification of the requirements. To be continued. First discussion with our main supplier for implementation', 3, 1, 0, CURRENT_TIMESTAMP - INTERVAL 40 DAY, 3, 1);
INSERT INTO portfolio_entry_report(`id`, `creation_date`, `publication_date`, `comments`, `author_id`, `is_published`, `deleted`, `last_update`, `portfolio_entry_report_status_type_id`, `portfolio_entry_id`) VALUES(3, CURRENT_TIMESTAMP - INTERVAL 30 DAY, CURRENT_TIMESTAMP - INTERVAL 30 DAY, 'DELETED REPORT', 3, 1, 1, CURRENT_TIMESTAMP - INTERVAL 30 DAY, 3, 1);
INSERT INTO portfolio_entry_report(`id`, `creation_date`, `publication_date`, `comments`, `author_id`, `is_published`, `deleted`, `last_update`, `portfolio_entry_report_status_type_id`, `portfolio_entry_id`) VALUES(4, CURRENT_TIMESTAMP - INTERVAL 10 DAY, CURRENT_TIMESTAMP - INTERVAL 10 DAY, 'We have some troubles with our main supplier. Need to be escalated as no time to work on the project before Xmas.', 3, 1, 0, CURRENT_TIMESTAMP - INTERVAL 10 DAY, 1, 1);
INSERT INTO portfolio_entry_report(`id`, `creation_date`, `publication_date`, `comments`, `author_id`, `is_published`, `deleted`, `last_update`, `portfolio_entry_report_status_type_id`, `portfolio_entry_id`) VALUES(6, CURRENT_TIMESTAMP - INTERVAL 100 DAY, CURRENT_TIMESTAMP - INTERVAL 100 DAY, '', 3, 1, 0, CURRENT_TIMESTAMP - INTERVAL 100 DAY, 3, 2);
INSERT INTO portfolio_entry_report(`id`, `creation_date`, `publication_date`, `comments`, `author_id`, `is_published`, `deleted`, `last_update`, `portfolio_entry_report_status_type_id`, `portfolio_entry_id`) VALUES(7, CURRENT_TIMESTAMP - INTERVAL 90 DAY, CURRENT_TIMESTAMP - INTERVAL 90 DAY, '', 3, 1, 0, CURRENT_TIMESTAMP - INTERVAL 90 DAY, 3, 2);
INSERT INTO portfolio_entry_report(`id`, `creation_date`, `publication_date`, `comments`, `author_id`, `is_published`, `deleted`, `last_update`, `portfolio_entry_report_status_type_id`, `portfolio_entry_id`) VALUES(8, CURRENT_TIMESTAMP - INTERVAL 80 DAY, CURRENT_TIMESTAMP - INTERVAL 80 DAY, '', 3, 1, 0, CURRENT_TIMESTAMP - INTERVAL 80 DAY, 3, 2);
INSERT INTO portfolio_entry_report(`id`, `creation_date`, `publication_date`, `comments`, `author_id`, `is_published`, `deleted`, `last_update`, `portfolio_entry_report_status_type_id`, `portfolio_entry_id`) VALUES(9, CURRENT_TIMESTAMP - INTERVAL 70 DAY, CURRENT_TIMESTAMP - INTERVAL 70 DAY, '', 3, 1, 0, CURRENT_TIMESTAMP - INTERVAL 70 DAY, 3, 2);
INSERT INTO portfolio_entry_report(`id`, `creation_date`, `publication_date`, `comments`, `author_id`, `is_published`, `deleted`, `last_update`, `portfolio_entry_report_status_type_id`, `portfolio_entry_id`) VALUES(10, CURRENT_TIMESTAMP - INTERVAL 60 DAY, CURRENT_TIMESTAMP - INTERVAL 60 DAY, '', 3, 1, 0, CURRENT_TIMESTAMP - INTERVAL 60 DAY, 3, 2);
INSERT INTO portfolio_entry_report(`id`, `creation_date`, `publication_date`, `comments`, `author_id`, `is_published`, `deleted`, `last_update`, `portfolio_entry_report_status_type_id`, `portfolio_entry_id`) VALUES(11, CURRENT_TIMESTAMP - INTERVAL 50 DAY, CURRENT_TIMESTAMP - INTERVAL 50 DAY, 'Good progress so far. Development is going well as planned.', 3, 1, 0, CURRENT_TIMESTAMP - INTERVAL 50 DAY, 3, 2);
INSERT INTO portfolio_entry_report(`id`, `creation_date`, `publication_date`, `comments`, `author_id`, `is_published`, `deleted`, `last_update`, `portfolio_entry_report_status_type_id`, `portfolio_entry_id`) VALUES(12, CURRENT_TIMESTAMP - INTERVAL 40 DAY, CURRENT_TIMESTAMP - INTERVAL 40 DAY, 'Servers have been delivered. Installation planned for next week.', 3, 1, 0, CURRENT_TIMESTAMP - INTERVAL 40 DAY, 3, 2);
INSERT INTO portfolio_entry_report(`id`, `creation_date`, `publication_date`, `comments`, `author_id`, `is_published`, `deleted`, `last_update`, `portfolio_entry_report_status_type_id`, `portfolio_entry_id`) VALUES(13, CURRENT_TIMESTAMP - INTERVAL 30 DAY, CURRENT_TIMESTAMP - INTERVAL 30 DAY, 'Infra up and running. First migration dry run done successfully.', 3, 1, 0, CURRENT_TIMESTAMP - INTERVAL 30 DAY, 3, 2);
INSERT INTO portfolio_entry_report(`id`, `creation_date`, `publication_date`, `comments`, `author_id`, `is_published`, `deleted`, `last_update`, `portfolio_entry_report_status_type_id`, `portfolio_entry_id`) VALUES(14, CURRENT_TIMESTAMP - INTERVAL 20 DAY, CURRENT_TIMESTAMP - INTERVAL 20 DAY, '2nd migration dry run ok too. Fine tunning on going.', 3, 1, 0, CURRENT_TIMESTAMP - INTERVAL 20 DAY, 3, 2);
INSERT INTO portfolio_entry_report(`id`, `creation_date`, `publication_date`, `comments`, `author_id`, `is_published`, `deleted`, `last_update`, `portfolio_entry_report_status_type_id`, `portfolio_entry_id`) VALUES(15, CURRENT_TIMESTAMP - INTERVAL 10 DAY, CURRENT_TIMESTAMP - INTERVAL 10 DAY, 'Help desk training done. Operational procedures valided by Operations.', 3, 1, 0, CURRENT_TIMESTAMP - INTERVAL 10 DAY, 3, 2);
INSERT INTO portfolio_entry_report(`id`, `creation_date`, `publication_date`, `comments`, `author_id`, `is_published`, `deleted`, `last_update`, `portfolio_entry_report_status_type_id`, `portfolio_entry_id`) VALUES(16, CURRENT_TIMESTAMP - INTERVAL 5 DAY, CURRENT_TIMESTAMP - INTERVAL 5 DAY, 'Ready to go to production. Need GO from Steering Committee', 3, 1, 0, CURRENT_TIMESTAMP - INTERVAL 5 DAY, 3, 2);
INSERT INTO portfolio_entry_report(`id`, `creation_date`, `publication_date`, `comments`, `author_id`, `is_published`, `deleted`, `last_update`, `portfolio_entry_report_status_type_id`, `portfolio_entry_id`) VALUES(17, CURRENT_TIMESTAMP - INTERVAL 5 DAY, CURRENT_TIMESTAMP - INTERVAL 5 DAY, 'Project initiation on going. Resources allocations to be completed by end of week', 3, 1, 0, CURRENT_TIMESTAMP - INTERVAL 5 DAY, 2, 3);
INSERT INTO portfolio_entry_report(`id`, `creation_date`, `publication_date`, `comments`, `author_id`, `is_published`, `deleted`, `last_update`, `portfolio_entry_report_status_type_id`, `portfolio_entry_id`) VALUES(18, CURRENT_TIMESTAMP - INTERVAL 5 DAY, CURRENT_TIMESTAMP - INTERVAL 5 DAY, 'Project on going as planned', 3, 1, 0, CURRENT_TIMESTAMP - INTERVAL 5 DAY, 3, 7);
INSERT INTO portfolio_entry_report(`id`, `creation_date`, `publication_date`, `comments`, `author_id`, `is_published`, `deleted`, `last_update`, `portfolio_entry_report_status_type_id`, `portfolio_entry_id`) VALUES(19, CURRENT_TIMESTAMP - INTERVAL 5 DAY, CURRENT_TIMESTAMP - INTERVAL 5 DAY, 'Project on hold following last steerco', 3, 1, 0, CURRENT_TIMESTAMP - INTERVAL 5 DAY, 1, 10);

-- Table portfolio_entry_report_status_type
INSERT INTO portfolio_entry_report_status_type(`id`, `name`, `description`, `selectable`, `deleted`, `css_class`, `last_update`) VALUES(4, 'NOT SELECTABLE', 'NO SELECTABLE STATUS TYPE', 0, 0, 'active', CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_report_status_type(`id`, `name`, `description`, `selectable`, `deleted`, `css_class`, `last_update`) VALUES(5, 'DELETED', 'DELETED STATUS TYPE', 0, 1, 'info', CURRENT_TIMESTAMP);

-- Table portfolio_entry_resource_plan
INSERT INTO `portfolio_entry_resource_plan` (`id`, `last_update`) VALUES ('1', CURRENT_TIMESTAMP);
INSERT INTO `portfolio_entry_resource_plan` (`id`, `last_update`) VALUES ('2', CURRENT_TIMESTAMP);
INSERT INTO `portfolio_entry_resource_plan` (`id`, `last_update`) VALUES ('3', CURRENT_TIMESTAMP);
INSERT INTO `portfolio_entry_resource_plan` (`id`, `last_update`) VALUES ('4', CURRENT_TIMESTAMP);
INSERT INTO `portfolio_entry_resource_plan` (`id`, `last_update`) VALUES ('5', CURRENT_TIMESTAMP);
INSERT INTO `portfolio_entry_resource_plan` (`id`, `last_update`) VALUES ('6', CURRENT_TIMESTAMP);
INSERT INTO `portfolio_entry_resource_plan` (`id`, `last_update`) VALUES ('7', CURRENT_TIMESTAMP);
INSERT INTO `portfolio_entry_resource_plan` (`id`, `last_update`) VALUES ('8', CURRENT_TIMESTAMP);
INSERT INTO `portfolio_entry_resource_plan` (`id`, `last_update`) VALUES ('9', CURRENT_TIMESTAMP);
INSERT INTO `portfolio_entry_resource_plan` (`id`, `last_update`) VALUES ('10', CURRENT_TIMESTAMP);
INSERT INTO `portfolio_entry_resource_plan` (`id`, `last_update`) VALUES ('11', CURRENT_TIMESTAMP);
INSERT INTO `portfolio_entry_resource_plan` (`id`, `last_update`) VALUES ('12', CURRENT_TIMESTAMP);
INSERT INTO `portfolio_entry_resource_plan` (`id`, `last_update`) VALUES ('13', CURRENT_TIMESTAMP);
INSERT INTO `portfolio_entry_resource_plan` (`id`, `last_update`) VALUES ('14', CURRENT_TIMESTAMP);
INSERT INTO `portfolio_entry_resource_plan` (`id`, `last_update`) VALUES ('15', CURRENT_TIMESTAMP);
INSERT INTO `portfolio_entry_resource_plan` (`id`, `last_update`) VALUES ('16', CURRENT_TIMESTAMP);
INSERT INTO `portfolio_entry_resource_plan` (`id`, `last_update`) VALUES ('17', CURRENT_TIMESTAMP);
INSERT INTO `portfolio_entry_resource_plan` (`id`, `last_update`) VALUES ('18', CURRENT_TIMESTAMP);
INSERT INTO `portfolio_entry_resource_plan` (`id`, `last_update`) VALUES ('19', CURRENT_TIMESTAMP);
INSERT INTO `portfolio_entry_resource_plan` (`id`, `last_update`) VALUES ('20', CURRENT_TIMESTAMP);
INSERT INTO `portfolio_entry_resource_plan` (`id`, `last_update`) VALUES ('21', CURRENT_TIMESTAMP);

-- Table portfolio_entry_resource_plan_allocated_actor
INSERT INTO `portfolio_entry_resource_plan_allocated_actor` (`id`,`portfolio_entry_resource_plan_id`,`actor_id`,`days`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`, `currency_code`) VALUES (1,1,8,5.00,0,CURRENT_TIMESTAMP + INTERVAL 22 DAY,CURRENT_TIMESTAMP + INTERVAL 40 DAY,CURRENT_TIMESTAMP,3,2,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_actor` (`id`,`portfolio_entry_resource_plan_id`,`actor_id`,`days`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`, `currency_code`) VALUES (2,1,7,30.00,0,CURRENT_TIMESTAMP + INTERVAL 42 DAY,CURRENT_TIMESTAMP + INTERVAL 80 DAY,CURRENT_TIMESTAMP,5,2,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_actor` (`id`,`portfolio_entry_resource_plan_id`,`actor_id`,`days`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`, `currency_code`) VALUES (3,1,6,15.00,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP + INTERVAL 30 DAY,CURRENT_TIMESTAMP,NULL,2,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_actor` (`id`,`portfolio_entry_resource_plan_id`,`actor_id`,`days`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`, `currency_code`) VALUES (4,3,7,20.00,0,CURRENT_TIMESTAMP + INTERVAL 10 DAY,CURRENT_TIMESTAMP + INTERVAL 40 DAY,CURRENT_TIMESTAMP,NULL,2,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_actor` (`id`,`portfolio_entry_resource_plan_id`,`actor_id`,`days`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`, `currency_code`) VALUES (5,3,3,30.00,0,CURRENT_TIMESTAMP + INTERVAL 50 DAY,CURRENT_TIMESTAMP + INTERVAL 100 DAY,CURRENT_TIMESTAMP,NULL,2,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_actor` (`id`,`portfolio_entry_resource_plan_id`,`actor_id`,`days`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`, `currency_code`) VALUES (6,2,8,5.00,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP + INTERVAL 14 DAY,CURRENT_TIMESTAMP,7,2,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_actor` (`id`,`portfolio_entry_resource_plan_id`,`actor_id`,`days`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (7,17,6,15.00,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP + INTERVAL 30 DAY,CURRENT_TIMESTAMP,NULL,0,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_actor` (`id`,`portfolio_entry_resource_plan_id`,`actor_id`,`days`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (8,17,8,5.00,0,CURRENT_TIMESTAMP + INTERVAL 22 DAY,CURRENT_TIMESTAMP + INTERVAL 40 DAY,CURRENT_TIMESTAMP,3,1,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_actor` (`id`,`portfolio_entry_resource_plan_id`,`actor_id`,`days`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (9,17,7,30.00,0,CURRENT_TIMESTAMP + INTERVAL 42 DAY,CURRENT_TIMESTAMP + INTERVAL 80 DAY,CURRENT_TIMESTAMP,5,0,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_actor` (`id`,`portfolio_entry_resource_plan_id`,`actor_id`,`days`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (10,18,6,15.00,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP + INTERVAL 30 DAY,CURRENT_TIMESTAMP,NULL,0,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_actor` (`id`,`portfolio_entry_resource_plan_id`,`actor_id`,`days`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (11,18,8,5.00,0,CURRENT_TIMESTAMP + INTERVAL 22 DAY,CURRENT_TIMESTAMP + INTERVAL 40 DAY,CURRENT_TIMESTAMP,3,1,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_actor` (`id`,`portfolio_entry_resource_plan_id`,`actor_id`,`days`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (12,18,7,30.00,0,CURRENT_TIMESTAMP + INTERVAL 42 DAY,CURRENT_TIMESTAMP + INTERVAL 80 DAY,CURRENT_TIMESTAMP,5,0,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_actor` (`id`,`portfolio_entry_resource_plan_id`,`actor_id`,`days`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (13,19,6,15.00,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP + INTERVAL 30 DAY,CURRENT_TIMESTAMP,NULL,0,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_actor` (`id`,`portfolio_entry_resource_plan_id`,`actor_id`,`days`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (14,19,8,5.00,0,CURRENT_TIMESTAMP + INTERVAL 22 DAY,CURRENT_TIMESTAMP + INTERVAL 40 DAY,CURRENT_TIMESTAMP,3,1,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_actor` (`id`,`portfolio_entry_resource_plan_id`,`actor_id`,`days`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (15,19,7,30.00,0,CURRENT_TIMESTAMP + INTERVAL 42 DAY,CURRENT_TIMESTAMP + INTERVAL 80 DAY,CURRENT_TIMESTAMP,5,0,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_actor` (`id`,`portfolio_entry_resource_plan_id`,`actor_id`,`days`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (16,20,6,15.00,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP + INTERVAL 30 DAY,CURRENT_TIMESTAMP,NULL,0,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_actor` (`id`,`portfolio_entry_resource_plan_id`,`actor_id`,`days`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (17,20,8,5.00,0,CURRENT_TIMESTAMP + INTERVAL 22 DAY,CURRENT_TIMESTAMP + INTERVAL 40 DAY,CURRENT_TIMESTAMP,3,1,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_actor` (`id`,`portfolio_entry_resource_plan_id`,`actor_id`,`days`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (18,20,7,30.00,0,CURRENT_TIMESTAMP + INTERVAL 42 DAY,CURRENT_TIMESTAMP + INTERVAL 80 DAY,CURRENT_TIMESTAMP,5,0,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_actor` (`id`,`portfolio_entry_resource_plan_id`,`actor_id`,`days`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (19,21,6,15.00,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP + INTERVAL 30 DAY,CURRENT_TIMESTAMP,NULL,0,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_actor` (`id`,`portfolio_entry_resource_plan_id`,`actor_id`,`days`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (20,21,8,5.00,0,CURRENT_TIMESTAMP + INTERVAL 22 DAY,CURRENT_TIMESTAMP + INTERVAL 40 DAY,CURRENT_TIMESTAMP,3,1,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_actor` (`id`,`portfolio_entry_resource_plan_id`,`actor_id`,`days`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (21,21,7,30.00,0,CURRENT_TIMESTAMP + INTERVAL 42 DAY,CURRENT_TIMESTAMP + INTERVAL 80 DAY,CURRENT_TIMESTAMP,5,0,NULL,'CHF');

-- Table portfolio_entry_resource_plan_allocated_org_unit
INSERT INTO `portfolio_entry_resource_plan_allocated_org_unit` (`id`,`days`,`portfolio_entry_resource_plan_id`,`org_unit_id`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`, `currency_code`) VALUES (1,5.00,1,8,0,CURRENT_TIMESTAMP + INTERVAL 81 DAY,CURRENT_TIMESTAMP + INTERVAL 100 DAY,CURRENT_TIMESTAMP,6,2,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_org_unit` (`id`,`days`,`portfolio_entry_resource_plan_id`,`org_unit_id`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`, `currency_code`) VALUES (2,10.00,1,4,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP + INTERVAL 20 DAY,CURRENT_TIMESTAMP,1,2,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_org_unit` (`id`,`days`,`portfolio_entry_resource_plan_id`,`org_unit_id`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`, `currency_code`) VALUES (3,20.00,2,9,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP + INTERVAL 14 DAY,CURRENT_TIMESTAMP,7,2,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_org_unit` (`id`,`days`,`portfolio_entry_resource_plan_id`,`org_unit_id`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`, `currency_code`) VALUES (4,20.00,2,9,0,CURRENT_TIMESTAMP + INTERVAL 14 DAY,CURRENT_TIMESTAMP + INTERVAL 28 DAY,CURRENT_TIMESTAMP,8,2,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_org_unit` (`id`,`days`,`portfolio_entry_resource_plan_id`,`org_unit_id`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (5,15.00,17,4,0,CURRENT_TIMESTAMP + INTERVAL 81 DAY,CURRENT_TIMESTAMP + INTERVAL 100 DAY,CURRENT_TIMESTAMP,1,1,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_org_unit` (`id`,`days`,`portfolio_entry_resource_plan_id`,`org_unit_id`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (6,5.00,17,8,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP + INTERVAL 20 DAY,CURRENT_TIMESTAMP,6,0,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_org_unit` (`id`,`days`,`portfolio_entry_resource_plan_id`,`org_unit_id`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (7,15.00,18,4,0,CURRENT_TIMESTAMP + INTERVAL 81 DAY,CURRENT_TIMESTAMP + INTERVAL 100 DAY,CURRENT_TIMESTAMP,1,1,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_org_unit` (`id`,`days`,`portfolio_entry_resource_plan_id`,`org_unit_id`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (8,5.00,18,8,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP + INTERVAL 20 DAY,CURRENT_TIMESTAMP,6,0,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_org_unit` (`id`,`days`,`portfolio_entry_resource_plan_id`,`org_unit_id`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (9,20.00,19,4,0,CURRENT_TIMESTAMP + INTERVAL 81 DAY,CURRENT_TIMESTAMP + INTERVAL 100 DAY,CURRENT_TIMESTAMP,1,1,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_org_unit` (`id`,`days`,`portfolio_entry_resource_plan_id`,`org_unit_id`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (10,5.00,19,8,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP + INTERVAL 20 DAY,CURRENT_TIMESTAMP,6,0,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_org_unit` (`id`,`days`,`portfolio_entry_resource_plan_id`,`org_unit_id`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (11,20.00,20,4,0,CURRENT_TIMESTAMP + INTERVAL 81 DAY,CURRENT_TIMESTAMP + INTERVAL 100 DAY,CURRENT_TIMESTAMP,1,1,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_org_unit` (`id`,`days`,`portfolio_entry_resource_plan_id`,`org_unit_id`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (12,5.00,20,8,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP + INTERVAL 20 DAY,CURRENT_TIMESTAMP,6,0,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_org_unit` (`id`,`days`,`portfolio_entry_resource_plan_id`,`org_unit_id`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (13,20.00,21,4,0,CURRENT_TIMESTAMP + INTERVAL 81 DAY,CURRENT_TIMESTAMP + INTERVAL 100 DAY,CURRENT_TIMESTAMP,1,1,NULL,'CHF');
INSERT INTO `portfolio_entry_resource_plan_allocated_org_unit` (`id`,`days`,`portfolio_entry_resource_plan_id`,`org_unit_id`,`deleted`,`start_date`,`end_date`,`last_update`,`portfolio_entry_planning_package_id`,`portfolio_entry_resource_plan_allocation_status_type_id`,`follow_package_dates`, `currency_code`) VALUES (14,5.00,21,8,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP + INTERVAL 20 DAY,CURRENT_TIMESTAMP,6,0,NULL,'CHF');

-- Table portfolio_entry_risk
INSERT INTO portfolio_entry_risk(`id`, `creation_date`, `target_date`, `name`, `description`, `portfolio_entry_id`, `has_occured`, `is_mitigated`, `closure_date`, `mitigation_comment`, `is_active`, `deleted`, `last_update`, `portfolio_entry_risk_type_id`, `owner_id`) VALUES(1, CURRENT_TIMESTAMP - INTERVAL 50 DAY, CURRENT_TIMESTAMP + INTERVAL 50 DAY, 'No more ressource', 'Key ressource may leave', 1, 0, 0, null, null, 1, 0, CURRENT_TIMESTAMP - INTERVAL 50 DAY, 2, NULL);
INSERT INTO portfolio_entry_risk(`id`, `creation_date`, `target_date`, `name`, `description`, `portfolio_entry_id`, `has_occured`, `is_mitigated`, `closure_date`, `mitigation_comment`, `is_active`, `deleted`, `last_update`, `portfolio_entry_risk_type_id`, `owner_id`) VALUES(2, CURRENT_TIMESTAMP - INTERVAL 40 DAY, CURRENT_TIMESTAMP + INTERVAL 10 DAY, 'Delay in delivery of servers', 'Servers would arrive after the planned launch date', 1, 0, 1, null, 'Take some old ones', 1, 0, CURRENT_TIMESTAMP - INTERVAL 50 DAY, 3, NULL);
INSERT INTO portfolio_entry_risk(`id`, `creation_date`, `target_date`, `name`, `description`, `portfolio_entry_id`, `has_occured`, `is_mitigated`, `closure_date`, `mitigation_comment`, `is_active`, `deleted`, `last_update`, `portfolio_entry_risk_type_id`, `owner_id`) VALUES(3, CURRENT_TIMESTAMP - INTERVAL 50 DAY, null, 'Bug during testing', 'Big performance issue has been detected', 1, 1, 0, null, null, 1, 0, CURRENT_TIMESTAMP - INTERVAL 10 DAY, 3, 2);
INSERT INTO portfolio_entry_risk(`id`, `creation_date`, `target_date`, `name`, `description`, `portfolio_entry_id`, `has_occured`, `is_mitigated`, `closure_date`, `mitigation_comment`, `is_active`, `deleted`, `last_update`, `portfolio_entry_risk_type_id`, `owner_id`) VALUES(4, CURRENT_TIMESTAMP - INTERVAL 50 DAY, CURRENT_TIMESTAMP + INTERVAL 50 DAY, 'NO MOVE ACTIVE RISK', 'NO MOVE ACTIVE RISK', 1, 0, 0, CURRENT_TIMESTAMP - INTERVAL 30 DAY, 'not valid anymore', 0, 0, CURRENT_TIMESTAMP - INTERVAL 30 DAY, 1, NULL);


-- Table portfolio_entry_risk_type
INSERT INTO portfolio_entry_risk_type(`id`, `name`, `description`, `deleted`, `selectable`, `last_update`) VALUES(4, 'NOT SELECTABLE RISK TYPE', 'NOT SELECTABLE RISK TYPE', 0, 0, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_risk_type(`id`, `name`, `description`, `deleted`, `selectable`, `last_update`) VALUES(5, 'DELETED RISK TYPE', 'DELETED RISK TYPE', 1, 1, CURRENT_TIMESTAMP);

-- Table portfolio_entry_type
INSERT INTO portfolio_entry_type(`id`, `deleted`, `name`,  description, `selectable`, `last_update`) VALUES (4, 1, 'DELETED PORTFOLIO ENTRY TYPE', 'DELETED PORTFOLIO ENTRY TYPE', 1, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_type(`id`, `deleted`, `name`,  description, `selectable`, `last_update`) VALUES (5, 0, 'NOT SELECTABLE PORTFOLIO ENTRY TYPE', 'NOT SELECTABLE PORTFOLIO ENTRY TYPE', 0, CURRENT_TIMESTAMP);
INSERT INTO portfolio_entry_type(`id`, `deleted`, `name`,  description, `selectable`, `last_update`) VALUES (6, 1, 'DELETED AND NOT SELECTABLE PORTFOLIO ENTRY TYPE', 'DELETED AND NOT SELECTABLE PORTFOLIO ENTRY TYPE', 0, CURRENT_TIMESTAMP);

-- Table portfolio_has_portfolio_entry
INSERT INTO portfolio_has_portfolio_entry(`portfolio_entry_id`, `portfolio_id`) VALUES (1,1);
INSERT INTO portfolio_has_portfolio_entry(`portfolio_entry_id`, `portfolio_id`) VALUES (2,2);
INSERT INTO portfolio_has_portfolio_entry(`portfolio_entry_id`, `portfolio_id`) VALUES (3,1);
INSERT INTO portfolio_has_portfolio_entry(`portfolio_entry_id`, `portfolio_id`) VALUES (4,1);
INSERT INTO portfolio_has_portfolio_entry(`portfolio_entry_id`, `portfolio_id`) VALUES (5,1);
INSERT INTO portfolio_has_portfolio_entry(`portfolio_entry_id`, `portfolio_id`) VALUES (6,3);
INSERT INTO portfolio_has_portfolio_entry(`portfolio_entry_id`, `portfolio_id`) VALUES (6,1);
INSERT INTO portfolio_has_portfolio_entry(`portfolio_entry_id`, `portfolio_id`) VALUES (7,1);
INSERT INTO portfolio_has_portfolio_entry(`portfolio_entry_id`, `portfolio_id`) VALUES (8,1);
INSERT INTO portfolio_has_portfolio_entry(`portfolio_entry_id`, `portfolio_id`) VALUES (9,1);
INSERT INTO portfolio_has_portfolio_entry(`portfolio_entry_id`, `portfolio_id`) VALUES (10,1);
INSERT INTO portfolio_has_portfolio_entry(`portfolio_entry_id`, `portfolio_id`) VALUES (11,1);

-- Table portfolio_type
INSERT INTO portfolio_type(`id`, `name`, `description`, `last_update`, `selectable`, `deleted`) VALUES (4, 'DELETED PORTFOLIO TYPE', 'DELETED PORTFOLIO TYPE', CURRENT_TIMESTAMP, 1, 1);
INSERT INTO portfolio_type(`id`, `name`, `description`, `last_update`, `selectable`, `deleted`) VALUES (5, 'NON SELECTABLE PORTFOLIO TYPE', 'NON SELECTABLE PORTFOLIO TYPE', CURRENT_TIMESTAMP, 0, 0);

-- Table principal
INSERT INTO `principal` (`uid`,`is_active`,`is_pending`,`validation_key`) VALUES ('test_sall',1,0,NULL);
INSERT INTO `principal` (`uid`,`is_active`,`is_pending`,`validation_key`) VALUES ('test_spmo',1,0,NULL);
INSERT INTO `principal` (`uid`,`is_active`,`is_pending`,`validation_key`) VALUES ('test_aexco',1,0,NULL);
INSERT INTO `principal` (`uid`,`is_active`,`is_pending`,`validation_key`) VALUES ('test_jdm',1,0,NULL);
INSERT INTO `principal` (`uid`,`is_active`,`is_pending`,`validation_key`) VALUES ('test_garchi',1,0,NULL);
INSERT INTO `principal` (`uid`,`is_active`,`is_pending`,`validation_key`) VALUES ('test_jdev',1,0,NULL);
INSERT INTO `principal` (`uid`,`is_active`,`is_pending`,`validation_key`) VALUES ('test_rpm',1,0,NULL);
INSERT INTO `principal` (`uid`,`is_active`,`is_pending`,`validation_key`) VALUES ('test_bfin',1,0,NULL);
INSERT INTO `principal` (`uid`,`is_active`,`is_pending`,`validation_key`) VALUES ('test_mmark',1,0,NULL);

-- Table boolean_custom_attribute_value - to hide the tour when reset of the environment
INSERT INTO `boolean_custom_attribute_value` (`object_type`,`object_id`,`value`,`deleted`,`last_update`,`custom_attribute_definition_id`) VALUES ('models.framework_models.account.Principal',(SELECT id FROM principal WHERE `uid`='test_sall'),0,0,CURRENT_TIMESTAMP,(SELECT id FROM custom_attribute_definition WHERE `uuid`='TOP_MENU_BAR_TOUR'));
INSERT INTO `boolean_custom_attribute_value` (`object_type`,`object_id`,`value`,`deleted`,`last_update`,`custom_attribute_definition_id`) VALUES ('models.framework_models.account.Principal',(SELECT id FROM principal WHERE `uid`='test_sall'),0,0,CURRENT_TIMESTAMP,(SELECT id FROM custom_attribute_definition WHERE `uuid`='BREADCRUMB_TOUR'));
INSERT INTO `boolean_custom_attribute_value` (`object_type`,`object_id`,`value`,`deleted`,`last_update`,`custom_attribute_definition_id`) VALUES ('models.framework_models.account.Principal',(SELECT id FROM principal WHERE `uid`='test_spmo'),0,0,CURRENT_TIMESTAMP,(SELECT id FROM custom_attribute_definition WHERE `uuid`='TOP_MENU_BAR_TOUR'));
INSERT INTO `boolean_custom_attribute_value` (`object_type`,`object_id`,`value`,`deleted`,`last_update`,`custom_attribute_definition_id`) VALUES ('models.framework_models.account.Principal',(SELECT id FROM principal WHERE `uid`='test_spmo'),0,0,CURRENT_TIMESTAMP,(SELECT id FROM custom_attribute_definition WHERE `uuid`='BREADCRUMB_TOUR'));
INSERT INTO `boolean_custom_attribute_value` (`object_type`,`object_id`,`value`,`deleted`,`last_update`,`custom_attribute_definition_id`) VALUES ('models.framework_models.account.Principal',(SELECT id FROM principal WHERE `uid`='test_aexco'),0,0,CURRENT_TIMESTAMP,(SELECT id FROM custom_attribute_definition WHERE `uuid`='TOP_MENU_BAR_TOUR'));
INSERT INTO `boolean_custom_attribute_value` (`object_type`,`object_id`,`value`,`deleted`,`last_update`,`custom_attribute_definition_id`) VALUES ('models.framework_models.account.Principal',(SELECT id FROM principal WHERE `uid`='test_aexco'),0,0,CURRENT_TIMESTAMP,(SELECT id FROM custom_attribute_definition WHERE `uuid`='BREADCRUMB_TOUR'));
INSERT INTO `boolean_custom_attribute_value` (`object_type`,`object_id`,`value`,`deleted`,`last_update`,`custom_attribute_definition_id`) VALUES ('models.framework_models.account.Principal',(SELECT id FROM principal WHERE `uid`='test_jdm'),0,0,CURRENT_TIMESTAMP,(SELECT id FROM custom_attribute_definition WHERE `uuid`='TOP_MENU_BAR_TOUR'));
INSERT INTO `boolean_custom_attribute_value` (`object_type`,`object_id`,`value`,`deleted`,`last_update`,`custom_attribute_definition_id`) VALUES ('models.framework_models.account.Principal',(SELECT id FROM principal WHERE `uid`='test_jdm'),0,0,CURRENT_TIMESTAMP,(SELECT id FROM custom_attribute_definition WHERE `uuid`='BREADCRUMB_TOUR'));
INSERT INTO `boolean_custom_attribute_value` (`object_type`,`object_id`,`value`,`deleted`,`last_update`,`custom_attribute_definition_id`) VALUES ('models.framework_models.account.Principal',(SELECT id FROM principal WHERE `uid`='test_garchi'),0,0,CURRENT_TIMESTAMP,(SELECT id FROM custom_attribute_definition WHERE `uuid`='TOP_MENU_BAR_TOUR'));
INSERT INTO `boolean_custom_attribute_value` (`object_type`,`object_id`,`value`,`deleted`,`last_update`,`custom_attribute_definition_id`) VALUES ('models.framework_models.account.Principal',(SELECT id FROM principal WHERE `uid`='test_garchi'),0,0,CURRENT_TIMESTAMP,(SELECT id FROM custom_attribute_definition WHERE `uuid`='BREADCRUMB_TOUR'));
INSERT INTO `boolean_custom_attribute_value` (`object_type`,`object_id`,`value`,`deleted`,`last_update`,`custom_attribute_definition_id`) VALUES ('models.framework_models.account.Principal',(SELECT id FROM principal WHERE `uid`='test_jdev'),0,0,CURRENT_TIMESTAMP,(SELECT id FROM custom_attribute_definition WHERE `uuid`='TOP_MENU_BAR_TOUR'));
INSERT INTO `boolean_custom_attribute_value` (`object_type`,`object_id`,`value`,`deleted`,`last_update`,`custom_attribute_definition_id`) VALUES ('models.framework_models.account.Principal',(SELECT id FROM principal WHERE `uid`='test_jdev'),0,0,CURRENT_TIMESTAMP,(SELECT id FROM custom_attribute_definition WHERE `uuid`='BREADCRUMB_TOUR'));
INSERT INTO `boolean_custom_attribute_value` (`object_type`,`object_id`,`value`,`deleted`,`last_update`,`custom_attribute_definition_id`) VALUES ('models.framework_models.account.Principal',(SELECT id FROM principal WHERE `uid`='test_rpm'),0,0,CURRENT_TIMESTAMP,(SELECT id FROM custom_attribute_definition WHERE `uuid`='TOP_MENU_BAR_TOUR'));
INSERT INTO `boolean_custom_attribute_value` (`object_type`,`object_id`,`value`,`deleted`,`last_update`,`custom_attribute_definition_id`) VALUES ('models.framework_models.account.Principal',(SELECT id FROM principal WHERE `uid`='test_rpm'),0,0,CURRENT_TIMESTAMP,(SELECT id FROM custom_attribute_definition WHERE `uuid`='BREADCRUMB_TOUR'));
INSERT INTO `boolean_custom_attribute_value` (`object_type`,`object_id`,`value`,`deleted`,`last_update`,`custom_attribute_definition_id`) VALUES ('models.framework_models.account.Principal',(SELECT id FROM principal WHERE `uid`='test_bfin'),0,0,CURRENT_TIMESTAMP,(SELECT id FROM custom_attribute_definition WHERE `uuid`='TOP_MENU_BAR_TOUR'));
INSERT INTO `boolean_custom_attribute_value` (`object_type`,`object_id`,`value`,`deleted`,`last_update`,`custom_attribute_definition_id`) VALUES ('models.framework_models.account.Principal',(SELECT id FROM principal WHERE `uid`='test_bfin'),0,0,CURRENT_TIMESTAMP,(SELECT id FROM custom_attribute_definition WHERE `uuid`='BREADCRUMB_TOUR'));
INSERT INTO `boolean_custom_attribute_value` (`object_type`,`object_id`,`value`,`deleted`,`last_update`,`custom_attribute_definition_id`) VALUES ('models.framework_models.account.Principal',(SELECT id FROM principal WHERE `uid`='test_mmark'),0,0,CURRENT_TIMESTAMP,(SELECT id FROM custom_attribute_definition WHERE `uuid`='TOP_MENU_BAR_TOUR'));
INSERT INTO `boolean_custom_attribute_value` (`object_type`,`object_id`,`value`,`deleted`,`last_update`,`custom_attribute_definition_id`) VALUES ('models.framework_models.account.Principal',(SELECT id FROM principal WHERE `uid`='test_mmark'),0,0,CURRENT_TIMESTAMP,(SELECT id FROM custom_attribute_definition WHERE `uuid`='BREADCRUMB_TOUR'));

-- Table purchase_order
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (2, 'OP200','PO OP200 - OPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (3, 'OP300','PO OP300 - OPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (4, 'OP400','PO OP400 - OPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (5, 'OP500','PO OP500 - OPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (6, 'OP600','PO OP600 - OPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (7, 'OP700','PO OP700 - OPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (8, 'OP800','PO OP800 - OPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (9, 'OP900','PO OP900 - OPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (10, 'OP1000','PO OP1000 - OPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (11, 'OP2000','PO OP2000 - OPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (12, 'OP3000','PO OP3000 - OPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (13, 'OP4000','PO OP4000 - OPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (14, 'OP5000','PO OP5000 - OPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (15, 'OP6000','PO OP6000 - OPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (16, 'OP7000','PO OP7000 - OPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (17, 'OP8000','PO OP8000 - OPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (18, 'OP9000','PO OP9000 - OPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (19, 'CA100','PO CA100 - CAPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (20, 'CA200','PO CA200 - CAPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (21, 'CA300','PO CA300 - CAPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (22, 'CA400','PO CA400 - CAPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (23, 'CA500','PO CA500 - CAPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (24, 'CA600','PO CA600 - CAPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (25, 'CA700','PO CA700 - CAPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (26, 'CA800','PO CA800 - CAPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (27, 'CA900','PO CA900 - CAPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (28, 'CA1000','PO CA1000 - CAPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (29, 'CA2000','PO CA2000 - CAPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (30, 'CA3000','PO CA3000 - CAPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (31, 'CA4000','PO CA4000 - CAPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (32, 'CA5000','PO CA5000 - CAPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (33, 'CA6000','PO CA6000 - CAPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (34, 'CA7000','PO CA7000 - CAPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (35, 'CA8000','PO CA8000 - CAPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (36, 'CA9000','PO CA9000 - CAPEX', CURRENT_TIMESTAMP, null, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (37, 'PE1-10000','PO 10000 FROM ERP attached to PE 1', NOW(), 1, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (38, 'PE1-20000','PO 20000 FROM ERP attached to PE 1', NOW(), 1, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (39, 'PE1-30000','PO 30000 FROM ERP attached to PE 1', NOW(), 1, 0);
INSERT INTO purchase_order (`id`,`ref_id`,`description`,`last_update`,`portfolio_entry_id`,`is_cancelled`)VALUES (40, 'PE1-40000','PO 40000 FROM ERP attached to PE 1 and cancelled', NOW(), 1, 1);

-- Table purchase_order_line_item
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (3,'PO LI OP200', 'LI OP200', null, 200000, 100000, 100000, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 90 DAY, 1, CURRENT_TIMESTAMP, 2, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (4,'PO LI OP300-1', 'LI OP300-1', null, 100000, 50000, 50000, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 60 DAY, 1, CURRENT_TIMESTAMP, 3, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (5,'PO LI OP300-2', 'LI OP300-2', null, 100000, 50000, 50000, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 60 DAY, 1, CURRENT_TIMESTAMP, 3, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (6,'PO LI OP400', 'LI OP400', null, 100000, 100000, 0, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 100 DAY, 1, CURRENT_TIMESTAMP, 4, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (7,'PO LI OP500-1', 'LI OP500-1', null, 300000, 300000, 0, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 120 DAY, 1, CURRENT_TIMESTAMP, 5, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (8,'PO LI OP500-2', 'LI OP500-2', null, 300000, 300000, 0, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 120 DAY, 1, CURRENT_TIMESTAMP, 5, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (9,'PO LI OP600', 'LI OP600', null, 100000, 100000, 0, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 100 DAY, 1, CURRENT_TIMESTAMP, 6, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (10,'PO LI OP700-1', 'LI OP700-1', null, 40000, 30000, 10000, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 180 DAY, 1, CURRENT_TIMESTAMP, 7, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (11,'PO LI OP700-2', 'LI OP700-2', null, 40000, 30000, 10000, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 180 DAY, 1, CURRENT_TIMESTAMP, 7, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (12,'PO LI OP800', 'LI OP800', null, 80000, 80000, 0, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 10 DAY, 1, CURRENT_TIMESTAMP, 8, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (13,'PO LI OP900-1', 'LI OP900-1', null, 50000, 50000, 0, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 30 DAY, 1, CURRENT_TIMESTAMP, 9, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (14,'PO LI OP900-2', 'LI OP900-2', null, 50000, 50000, 0, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 30 DAY, 1, CURRENT_TIMESTAMP, 9, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (15,'PO LI OP1000-1', 'LI OP1000-1', null, 100000, 90000, 10000, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 90 DAY, 1, CURRENT_TIMESTAMP, 10, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (17,'PO LI OP2000', 'LI OP2000', null, 200000, 200000, 0, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 90 DAY, 1, CURRENT_TIMESTAMP, 11, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (18,'PO LI OP3000-1', 'LI OP3000-1', null, 100000, 100000, 0, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 60 DAY, 1, CURRENT_TIMESTAMP, 12, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (19,'PO LI OP3000-2', 'LI OP3000-2', null, 100000, 100000, 0, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 60 DAY, 1, CURRENT_TIMESTAMP, 12, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (20,'PO LI OP4000', 'LI OP4000', null, 100000, 100000, 0, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 100 DAY, 1, CURRENT_TIMESTAMP, 13, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (21,'PO LI OP5000-1', 'LI OP5000-1', null, 300000, 300000, 0, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 120 DAY, 1, CURRENT_TIMESTAMP, 14, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (22,'PO LI OP5000-2', 'LI OP5000-2', null, 300000, 300000, 0, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 120 DAY, 1, CURRENT_TIMESTAMP, 14, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (23,'PO LI OP6000', 'LI OP6000', null, 100000, 100000, 0, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 100 DAY, 1, CURRENT_TIMESTAMP, 15, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (24,'PO LI OP7000-1', 'LI OP7000-1', null, 40000, 30000, 10000, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 180 DAY, 1, CURRENT_TIMESTAMP, 16, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (25,'PO LI OP7000-2', 'LI OP7000-2', null, 40000, 30000, 10000, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 180 DAY, 1, CURRENT_TIMESTAMP, 16, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (26,'PO LI OP8000', 'LI OP8000', null, 80000, 80000, 0, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 10 DAY, 1, CURRENT_TIMESTAMP, 17, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (27,'PO LI OP9000-1', 'LI OP9000-1', null, 50000, 50000, 0, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 30 DAY, 1, CURRENT_TIMESTAMP, 18, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (28,'PO LI OP9000-2', 'LI OP9000-2', null, 50000, 50000, 0, '500001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 30 DAY, 1, CURRENT_TIMESTAMP, 18, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (29,'PO LI CA100-1', 'LI CA100-1', null, 30000, 20000, 10000, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 90 DAY, 1, CURRENT_TIMESTAMP, 19, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (30,'PO LI CA100-2', 'LI CA100-2', null, 10000, 10000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 90 DAY, 1, CURRENT_TIMESTAMP, 19, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (31,'PO LI CA200', 'LI CA200', null, 200000, 200000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 90 DAY, 1, CURRENT_TIMESTAMP, 20, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (32,'PO LI CA300-1', 'LI CA300-1', null, 100000, 100000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 60 DAY, 1, CURRENT_TIMESTAMP, 21, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (33,'PO LI CA300-2', 'LI CA300-2', null, 100000, 100000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 60 DAY, 1, CURRENT_TIMESTAMP, 21, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (34,'PO LI CA400', 'LI CA400', null, 100000, 100000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 100 DAY, 1, CURRENT_TIMESTAMP, 22, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (35,'PO LI CA500-1', 'LI CA500-1', null, 300000, 300000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 120 DAY, 1, CURRENT_TIMESTAMP, 23, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (36,'PO LI CA500-2', 'LI CA500-2', null, 300000, 300000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 120 DAY, 1, CURRENT_TIMESTAMP,23, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (37,'PO LI CA600', 'LI CA600', null, 100000, 100000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 100 DAY, 1, CURRENT_TIMESTAMP, 24, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (38,'PO LI CA700-1', 'LI CA700-1', null, 40000, 30000, 10000, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 180 DAY, 1, CURRENT_TIMESTAMP, 25, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (39,'PO LI CA700-2', 'LI CA700-2', null, 40000, 30000, 10000, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 180 DAY, 1, CURRENT_TIMESTAMP, 25, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (40,'PO LI CA800', 'LI CA800', null, 80000, 80000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 10 DAY, 1, CURRENT_TIMESTAMP, 26, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (41,'PO LI CA900-1', 'LI CA900-1', null, 50000, 50000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 30 DAY, 1, CURRENT_TIMESTAMP, 27, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (42,'PO LI CA900-2', 'LI CA900-2', null, 50000, 50000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 30 DAY, 1, CURRENT_TIMESTAMP, 27, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (43,'PO LI CA1000-1', 'LI CA1000-1', null, 100000, 90000, 10000, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 90 DAY, 1, CURRENT_TIMESTAMP, 28, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (44,'PO LI CA1000-2', 'LI CA1000-2', null, 100000, 100000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 90 DAY, 1, CURRENT_TIMESTAMP, 28, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (45,'PO LI CA2000', 'LI CA2000', null, 200000, 200000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 90 DAY, 1, CURRENT_TIMESTAMP, 29, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (46,'PO LI CA3000-1', 'LI CA3000-1', null, 100000, 100000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 60 DAY, 1, CURRENT_TIMESTAMP, 30, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (47,'PO LI CA3000-2', 'LI CA3000-2', null, 100000, 100000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 60 DAY, 1, CURRENT_TIMESTAMP, 30, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (48,'PO LI CA4000', 'LI CA4000', null, 100000, 100000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 100 DAY, 1, CURRENT_TIMESTAMP, 31, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (49,'PO LI CA5000-1', 'LI CA5000-1', null, 300000, 300000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 120 DAY, 1, CURRENT_TIMESTAMP, 32, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (50,'PO LI CA5000-2', 'LI CA5000-2', null, 300000, 300000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 120 DAY, 1, CURRENT_TIMESTAMP, 32, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (51,'PO LI CA6000', 'LI CA6000', null, 100000, 100000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 100 DAY, 1, CURRENT_TIMESTAMP, 33, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (52,'PO LI CA7000-1', 'LI CA7000-1', null, 40000, 30000, 10000, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 180 DAY, 1, CURRENT_TIMESTAMP, 34, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (53,'PO LI CA7000-2', 'LI CA7000-2', null, 40000, 30000, 10000, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 180 DAY, 1, CURRENT_TIMESTAMP, 34, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (54,'PO LI CA8000', 'LI CA8000', null, 80000, 80000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 10 DAY, 1, CURRENT_TIMESTAMP, 35, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (55,'PO LI CA9000-1', 'LI CA9000-1', null, 50000, 50000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 30 DAY, 1, CURRENT_TIMESTAMP, 36, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (56,'PO LI CA9000-2', 'LI CA9000-2', null, 50000, 50000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 30 DAY, 1, CURRENT_TIMESTAMP, 36, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (57,'PO LI PE1-100-1', 'PO LI PE1-100-1 desc', null, 20000, 5000, 2000, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 90 DAY, 1, NOW(), 37, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (58,'PO LI PE1-200-1', 'PO LI PE1-200-1 desc', null, 100000, 10000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 90 DAY, 1, NOW(), 38, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (59,'PO LI PE1-200-2', 'PO LI PE1-200-2 desc', null, 100000, 10000, 1, '500006', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 90 DAY, 1, NOW(), 38, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (60,'PE1-300-1', 'PE1-300-1 desc', null, 100000, 10000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 90 DAY, 1, NOW(), 39, 'CHF', 0);
INSERT INTO purchase_order_line_item (`id`, `ref_id`,`description`,`supplier_id`,`amount`,`amount_received`,`amount_billed`,`gl_account`,`is_opex`,`creation_date`,`due_date`,`cost_center_id`,`last_update`,`purchase_order_id`,`currency_code`,`is_cancelled`) VALUES (61,'PE1-400 CAN', 'PE1-300 CANCELLED', null, 100000, 10000, 0, '500001', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL 90 DAY, 1, NOW(), 40, 'CHF', 1);

-- Table requirement
INSERT INTO `requirement` (`id`,`deleted`,`last_update`,`is_defect`,`portfolio_entry_id`,`external_ref_id`,`external_link`,`name`,`description`,`category`,`requirement_status_id`,`requirement_priority_id`,`requirement_severity_id`,`author_id`,`story_points`,`initial_estimation`) VALUES (1,0,'2014-10-24 11:57:55',0,1,'774','https://prod.the-agile-factory.net/redmine/issues/774','List of requirements','Menu: Requirements (between Governance and Planning - glyphicon: log_book)\n\nTable with filter capability (as the roadmap) that display the requirement FROM the requirement table\n- Default columns: external_`ref_id`, subject, requirement_type, requirement_status\nThe filter and column SELECTion must be kept as an user preference (as for the roadmap)\n\nClick on the line show the requirement with all its fields: external_`ref_id`, subject, `description`, priority, type, category, status, story point, initial estimate, planning package, author (first and last name, clickable as always for an actor) + link to external system\nBack button to come back to the requirements page','DevDock Governance',3,2,2,1,13,NULL);
INSERT INTO `requirement` (`id`,`deleted`,`last_update`,`is_defect`,`portfolio_entry_id`,`external_ref_id`,`external_link`,`name`,`description`,`category`,`requirement_status_id`,`requirement_priority_id`,`requirement_severity_id`,`author_id`,`story_points`,`initial_estimation`) VALUES (2,0,'2014-10-24 11:57:56',1,1,'782','https://prod.the-agile-factory.net/redmine/issues/782','KPI improvement',NULL,'DevDock Governance',5,3,2,1,10,2);
INSERT INTO `requirement` (`id`,`deleted`,`last_update`,`is_defect`,`portfolio_entry_id`,`external_ref_id`,`external_link`,`name`,`description`,`category`,`requirement_status_id`,`requirement_priority_id`,`requirement_severity_id`,`author_id`,`story_points`,`initial_estimation`) VALUES (3,0,'2014-10-24 11:57:56',0,1,'703','https://prod.the-agile-factory.net/redmine/issues/703','KPI - Burndown déviation','Current thinking status:\n- A plugin will go regularly (scheduler to be configurable as the load of the actor) into redmine to get the following data:\n* it will first find the current version (lookup into version table WHERE he can get the version id, the start date and the effective (end date)). It needs to take the next open versions\n* with the version, he can then query the issues table and get all the issues (tracker types to be configured n the plugin) and get the sum of the estimated_hours and remaining_hours for all these issues\n- he can then store the following KPI in the kpi_history table:\n* the remaing_time: it\'s directly the sum of the remaing_hours of all the SELECTed issues:\n* target_remaing_time: it\'s the sum of the initial_estimates * (sys date - start date of the sprint) / number of days of the sprint. it should be possible to configure in the plugin the day to NOT take into account (typically saturday and sunday)\n* the main KPI: deviation that is (remaing_time - target_remaining_time) / target_ remaining_time\n\nThen the KPI must be configured (as any other) to display the main and 2 sub-KPIs. Color rules are: green if =<0, Orange if between 0 and 10% and Red if above 10%','DevDock Governance',1,1,NULL,2,10,4);
INSERT INTO `requirement` (`id`,`deleted`,`last_update`,`is_defect`,`portfolio_entry_id`,`external_ref_id`,`external_link`,`name`,`description`,`category`,`requirement_status_id`,`requirement_priority_id`,`requirement_severity_id`,`author_id`,`story_points`,`initial_estimation`) VALUES (4,0,'2014-10-24 11:57:56',0,6,'738','https://prod.the-agile-factory.net/redmine/issues/738','Chart for repartition of the defect by severity','\nShow 2 gaphs:\n- pie chart of number of NOT CLOSED defect by severity + other donut around with the split showstopper/not showstopper\n- A bar chart with number of NOT CLOSED defect by severity, group by status','DevDock Governance',2,1,1,5,4,3);
INSERT INTO `requirement` (`id`,`deleted`,`last_update`,`is_defect`,`portfolio_entry_id`,`external_ref_id`,`external_link`,`name`,`description`,`category`,`requirement_status_id`,`requirement_priority_id`,`requirement_severity_id`,`author_id`,`story_points`,`initial_estimation`) VALUES (5,0,'2014-10-24 11:57:57',1,6,'755',NULL,'date column too thin',NULL,NULL,NULL,NULL,NULL,2,NULL,1);
INSERT INTO `requirement` (`id`,`deleted`,`last_update`,`is_defect`,`portfolio_entry_id`,`external_ref_id`,`external_link`,`name`,`description`,`category`,`requirement_status_id`,`requirement_priority_id`,`requirement_severity_id`,`author_id`,`story_points`,`initial_estimation`) VALUES (6,0,'2014-11-01 09:34:41',0,1,'823',NULL,'System crashes all the time','Nothing more to add - FIXE IT!',NULL,2,2,3,1,NULL,NULL);
INSERT INTO `requirement` (`id`,`deleted`,`last_update`,`is_defect`,`portfolio_entry_id`,`external_ref_id`,`external_link`,`name`,`description`,`category`,`requirement_status_id`,`requirement_priority_id`,`requirement_severity_id`,`author_id`,`story_points`,`initial_estimation`) VALUES (8,0,'2014-11-01 09:41:31',0,1,'231',NULL,'User interface to be enhanced','change color of the buttons','UI',4,2,2,1,NULL,NULL);
INSERT INTO `requirement` (`id`,`deleted`,`last_update`,`is_defect`,`portfolio_entry_id`,`external_ref_id`,`external_link`,`name`,`description`,`category`,`requirement_status_id`,`requirement_priority_id`,`requirement_severity_id`,`author_id`,`story_points`,`initial_estimation`) VALUES (9,0,'2014-11-01 09:54:52',1,1,'345',NULL,'Exception in job',NULL,NULL,1,1,1,1,NULL,NULL);
INSERT INTO `requirement` (`id`,`deleted`,`last_update`,`is_defect`,`portfolio_entry_id`,`external_ref_id`,`external_link`,`name`,`description`,`category`,`requirement_status_id`,`requirement_priority_id`,`requirement_severity_id`,`author_id`,`story_points`,`initial_estimation`) VALUES (10,0,'2014-11-01 09:54:52',1,1,'123',NULL,'Another bug',NULL,'UI',3,3,3,1,NULL,NULL);
INSERT INTO `requirement` (`id`,`deleted`,`last_update`,`is_defect`,`portfolio_entry_id`,`external_ref_id`,`external_link`,`name`,`description`,`category`,`requirement_status_id`,`requirement_priority_id`,`requirement_severity_id`,`author_id`,`story_points`,`initial_estimation`) VALUES (11,0,'2014-11-01 09:54:52',1,1,'346',NULL,'Display issue with activation screen',NULL,'UI',3,3,3,1,NULL,NULL);

-- Table reporting
UPDATE `reporting` SET `is_public`='1' WHERE `name`='reporting.portfolios.name';

-- Table stakeholder
INSERT INTO `stakeholder` (`id`,`deleted`,`stakeholder_type_id`,`actor_id`,`portfolio_entry_id`,`portfolio_id`,`last_update`) VALUES (30,0,8,8,1,NULL,CURRENT_TIMESTAMP);
INSERT INTO `stakeholder` (`id`,`deleted`,`stakeholder_type_id`,`actor_id`,`portfolio_entry_id`,`portfolio_id`,`last_update`) VALUES (31,0,7,7,1,NULL,CURRENT_TIMESTAMP);
INSERT INTO `stakeholder` (`id`,`deleted`,`stakeholder_type_id`,`actor_id`,`portfolio_entry_id`,`portfolio_id`,`last_update`) VALUES (32,0,2,6,1,NULL,CURRENT_TIMESTAMP);
INSERT INTO `stakeholder` (`id`,`deleted`,`stakeholder_type_id`,`actor_id`,`portfolio_entry_id`,`portfolio_id`,`last_update`) VALUES (33,0,8,8,2,NULL,CURRENT_TIMESTAMP);
INSERT INTO `stakeholder` (`id`,`deleted`,`stakeholder_type_id`,`actor_id`,`portfolio_entry_id`,`portfolio_id`,`last_update`) VALUES (34,0,7,7,2,NULL,CURRENT_TIMESTAMP);
INSERT INTO `stakeholder` (`id`,`deleted`,`stakeholder_type_id`,`actor_id`,`portfolio_entry_id`,`portfolio_id`,`last_update`) VALUES (35,0,5,2,2,NULL,CURRENT_TIMESTAMP);
INSERT INTO `stakeholder` (`id`,`deleted`,`stakeholder_type_id`,`actor_id`,`portfolio_entry_id`,`portfolio_id`,`last_update`) VALUES (36,0,2,3,3,NULL,CURRENT_TIMESTAMP);
INSERT INTO `stakeholder` (`id`,`deleted`,`stakeholder_type_id`,`actor_id`,`portfolio_entry_id`,`portfolio_id`,`last_update`) VALUES (37,0,7,7,3,NULL,CURRENT_TIMESTAMP);
INSERT INTO `stakeholder` (`id`,`deleted`,`stakeholder_type_id`,`actor_id`,`portfolio_entry_id`,`portfolio_id`,`last_update`) VALUES (38,0,2,6,NULL,1,CURRENT_TIMESTAMP);
INSERT INTO `stakeholder` (`id`,`deleted`,`stakeholder_type_id`,`actor_id`,`portfolio_entry_id`,`portfolio_id`,`last_update`) VALUES (39,0,5,4,NULL,1,CURRENT_TIMESTAMP);
INSERT INTO `stakeholder` (`id`,`deleted`,`stakeholder_type_id`,`actor_id`,`portfolio_entry_id`,`portfolio_id`,`last_update`) VALUES (40,0,8,8,NULL,2,CURRENT_TIMESTAMP);
INSERT INTO `stakeholder` (`id`,`deleted`,`stakeholder_type_id`,`actor_id`,`portfolio_entry_id`,`portfolio_id`,`last_update`) VALUES (41,0,2,9,NULL,2,CURRENT_TIMESTAMP);

-- Table stakeholder_type
INSERT INTO stakeholder_type(`id`, `deleted`, `name`, `description`, `selectable`, `last_update`) VALUES (1, 0, 'Head of program', 'Head of a program', 1, CURRENT_TIMESTAMP);
INSERT INTO stakeholder_type(`id`, `deleted`, `name`, `description`, `selectable`, `last_update`) VALUES (3, 0, 'Not `selectable` head of', 'IT  responsible of the program', 0, CURRENT_TIMESTAMP);
INSERT INTO stakeholder_type(`id`, `deleted`, `name`, `description`, `selectable`, `last_update`) VALUES (4, 1, 'DELETED STAKEHOLDER TYPE', 'DELETED STAKEHOLDER TYPE', 1, CURRENT_TIMESTAMP);
INSERT INTO stakeholder_type(`id`, `deleted`, `name`, `description`, `selectable`, `last_update`) VALUES (6, 0, 'Delivery Manager', 'Delivery manager', 1, CURRENT_TIMESTAMP);

-- Table supplier
INSERT INTO `supplier` (`deleted`, `ref_id`, `name`, `last_update`) VALUES (0, 'SWORD', 'Sword', CURRENT_TIMESTAMP);
INSERT INTO `supplier` (`deleted`, `ref_id`, `name`, `last_update`) VALUES (0, 'ORACLE', 'Oracle Corporation', CURRENT_TIMESTAMP);
INSERT INTO `supplier` (`deleted`, `ref_id`, `name`, `last_update`) VALUES (0, 'MICROSOFT', 'Microsoft', CURRENT_TIMESTAMP);

#Default roles for principals
INSERT INTO `system_level_role`
(`is_enabled`,
`system_level_role_type_id`,
`principal_id`)
VALUES
(
1,
(SELECT role_type.id FROM system_level_role_type as role_type WHERE role_type.name='SUPER_USER_ROLE'),
(SELECT principal.id FROM principal WHERE principal.uid='test_sall')
);
INSERT INTO `system_level_role`
(`is_enabled`,
`system_level_role_type_id`,
`principal_id`)
VALUES
(
1,
(SELECT role_type.id FROM system_level_role_type as role_type WHERE role_type.name='PMO_ROLE'),
(SELECT principal.id FROM principal WHERE principal.uid='test_spmo')
);
INSERT INTO `system_level_role`
(`is_enabled`,
`system_level_role_type_id`,
`principal_id`)
VALUES
(
1,
(SELECT role_type.id FROM system_level_role_type as role_type WHERE role_type.name='PORTFOLIO_MANAGER_ROLE'),
(SELECT principal.id FROM principal WHERE principal.uid='test_aexco')
);
INSERT INTO `system_level_role`
(`is_enabled`,
`system_level_role_type_id`,
`principal_id`)
VALUES
(
1,
(SELECT role_type.id FROM system_level_role_type as role_type WHERE role_type.name='DELIVERY_MANAGER_ROLE'),
(SELECT principal.id FROM principal WHERE principal.uid='test_jdm')
);
INSERT INTO `system_level_role`
(`is_enabled`,
`system_level_role_type_id`,
`principal_id`)
VALUES
(
1,
(SELECT role_type.id FROM system_level_role_type as role_type WHERE role_type.name='ARCHITECTURE_ROLE'),
(SELECT principal.id FROM principal WHERE principal.uid='test_garchi')
);
INSERT INTO `system_level_role`
(`is_enabled`,
`system_level_role_type_id`,
`principal_id`)
VALUES
(
1,
(SELECT role_type.id FROM system_level_role_type as role_type WHERE role_type.name='DEVELOPER_ROLE'),
(SELECT principal.id FROM principal WHERE principal.uid='test_jdev')
);
INSERT INTO `system_level_role`
(`is_enabled`,
`system_level_role_type_id`,
`principal_id`)
VALUES
(
1,
(SELECT role_type.id FROM system_level_role_type as role_type WHERE role_type.name='PROJECT_MANAGER_ROLE'),
(SELECT principal.id FROM principal WHERE principal.uid='test_rpm')
);
INSERT INTO `system_level_role`
(`is_enabled`,
`system_level_role_type_id`,
`principal_id`)
VALUES
(
1,
(SELECT role_type.id FROM system_level_role_type as role_type WHERE role_type.name='FINANCIAL_OFFICER_ROLE'),
(SELECT principal.id FROM principal WHERE principal.uid='test_bfin')
);
INSERT INTO `system_level_role`
(`is_enabled`,
`system_level_role_type_id`,
`principal_id`)
VALUES
(
1,
(SELECT role_type.id FROM system_level_role_type as role_type WHERE role_type.name='APPROVER_ROLE'),
(SELECT principal.id FROM principal WHERE principal.uid='test_mmark')
);


-- Table timesheet_activity
INSERT INTO `timesheet_activity` (`id`,`deleted`,`last_update`,`name`,`description`,`timesheet_activity_type_id`) VALUES (4,0,CURRENT_TIMESTAMP,'Holidays','',5);
INSERT INTO `timesheet_activity` (`id`,`deleted`,`last_update`,`name`,`description`,`timesheet_activity_type_id`) VALUES (5,0,CURRENT_TIMESTAMP,'Illness','',5);
INSERT INTO `timesheet_activity` (`id`,`deleted`,`last_update`,`name`,`description`,`timesheet_activity_type_id`) VALUES (6,0,CURRENT_TIMESTAMP,'Paternity Leave','',5);
INSERT INTO `timesheet_activity` (`id`,`deleted`,`last_update`,`name`,`description`,`timesheet_activity_type_id`) VALUES (7,0,CURRENT_TIMESTAMP,'Database administration','',6);
INSERT INTO `timesheet_activity` (`id`,`deleted`,`last_update`,`name`,`description`,`timesheet_activity_type_id`) VALUES (8,0,CURRENT_TIMESTAMP,'OS Support','',6);
INSERT INTO `timesheet_activity` (`id`,`deleted`,`last_update`,`name`,`description`,`timesheet_activity_type_id`) VALUES (9,0,CURRENT_TIMESTAMP,'Network','',6);

-- Table timesheet_activity_type
INSERT INTO `timesheet_activity_type` (`id`,`deleted`,`last_update`,`name`,`description`) VALUES (5,0,CURRENT_TIMESTAMP,'Absences','Absences (illness, Military services...)');
INSERT INTO `timesheet_activity_type` (`id`,`deleted`,`last_update`,`name`,`description`) VALUES (6,0,CURRENT_TIMESTAMP,'Operational Support','');

-- Table work_order
INSERT INTO `work_order` (`id`,`deleted`,`last_update`,`creation_date`,`due_date`,`name`,`description`,`amount`,`amount_received`,`is_opex`,`currency_code`,`shared`,`portfolio_entry_id`,`purchase_order_line_item_id`,`is_engaged`) VALUES (12,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,NULL,'Development UI','',30000.00,NULL,0,'CHF',0,1,NULL,0);
INSERT INTO `work_order` (`id`,`deleted`,`last_update`,`creation_date`,`due_date`,`name`,`description`,`amount`,`amount_received`,`is_opex`,`currency_code`,`shared`,`portfolio_entry_id`,`purchase_order_line_item_id`,`is_engaged`) VALUES (13,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,NULL,'Development Backend','',30000.00,NULL,0,'CHF',0,1,NULL,0);
INSERT INTO `work_order` (`id`,`deleted`,`last_update`,`creation_date`,`due_date`,`name`,`description`,`amount`,`amount_received`,`is_opex`,`currency_code`,`shared`,`portfolio_entry_id`,`purchase_order_line_item_id`,`is_engaged`) VALUES (14,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,NULL,'Licence','',30000.00,NULL,1,'CHF',0,1,NULL,0);
INSERT INTO `work_order` (`id`,`deleted`,`last_update`,`creation_date`,`due_date`,`name`,`description`,`amount`,`amount_received`,`is_opex`,`currency_code`,`shared`,`portfolio_entry_id`,`purchase_order_line_item_id`,`is_engaged`) VALUES (15,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,NULL,'Service','',50000.00,NULL,0,'CHF',0,2,NULL,0);



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = 1;
