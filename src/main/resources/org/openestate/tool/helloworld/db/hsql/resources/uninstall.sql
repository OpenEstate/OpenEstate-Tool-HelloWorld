-- noinspection SqlNoDataSourceInspectionForFile

-- -----------------------------------------------------
-- HelloWorld-Addon for OpenEstate-ImmoTool
-- uninstallation for HSQLDB
-- Copyright 2012-2019 OpenEstate.org
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Procedures
-- -----------------------------------------------------
DROP FUNCTION IF EXISTS can_delete_immotool_helloworld CASCADE;
DROP FUNCTION IF EXISTS can_write_immotool_helloworld CASCADE;
DROP PROCEDURE IF EXISTS remove_immotool_helloworld CASCADE;
DROP PROCEDURE IF EXISTS save_immotool_helloworld CASCADE;


-- -----------------------------------------------------
-- Views
-- -----------------------------------------------------
DROP VIEW IF EXISTS view_immotool_helloworld CASCADE;


-- -----------------------------------------------------
-- Tables
-- -----------------------------------------------------
DROP TABLE IF EXISTS immotool_helloworld CASCADE;


-- -----------------------------------------------------
-- Sequences
-- -----------------------------------------------------
DROP SEQUENCE IF EXISTS seq_immotool_helloworld CASCADE;
