-- noinspection SqlNoDataSourceInspectionForFile

-- -----------------------------------------------------
-- HelloWorld-Addon for OpenEstate-ImmoTool
-- update 2 for HSQLDB
-- Copyright 2012-2019 OpenEstate.org
-- -----------------------------------------------------
-- Rename field 'helloworld_text' to 'helloworld_notes' in table 'immotool_helloworld'
-- Rebuild 'view_immotool_helloworld'
-- Rebuild 'save_immotool_helloworld'
-- -----------------------------------------------------

DROP PROCEDURE IF EXISTS save_immotool_helloworld RESTRICT;
DROP VIEW IF EXISTS view_immotool_helloworld RESTRICT;

ALTER TABLE immotool_helloworld
  ALTER COLUMN helloworld_text RENAME TO helloworld_notes;

-- -----------------------------------------------------
-- View view_immotool_helloworld
-- -----------------------------------------------------
CREATE VIEW view_immotool_helloworld AS
  SELECT * FROM immotool_helloworld
  WHERE
  (
    ('DBA' IN (SELECT authorization_name FROM information_schema.authorizations WHERE authorization_type = 'ROLE'))
    OR
    (BITAND(access_permissions, 64) = 64)
    OR
    (BITAND(access_permissions, 8) = 8 AND access_group_id IN (SELECT group_id FROM view_immotool_users_groups WHERE user_login=USER()))
    OR
    (BITAND(access_permissions, 1) = 1 AND access_owner_id IN (SELECT user_id FROM view_immotool_users WHERE user_login=USER()))
  ) WITH CASCADED CHECK OPTION;

GRANT SELECT
  ON view_immotool_helloworld
  TO "IMMOTOOL";


\.
-- -----------------------------------------------------
-- Procedure save_immotool_helloworld
-- -----------------------------------------------------
CREATE PROCEDURE save_immotool_helloworld(
  INOUT val_helloworld_id BIGINT,
  val_helloworld_name VARCHAR(100),
  val_helloworld_notes LONGVARCHAR,
  val_access_owner_id BIGINT,
  val_access_group_id BIGINT,
  val_access_permissions INT )
  MODIFIES SQL DATA
  BEGIN ATOMIC
    DECLARE allowed BOOLEAN;
    DECLARE permissions INT;
    DECLARE owner_uid BIGINT;
    DECLARE owner_gid BIGINT;
    DECLARE current_uid BIGINT;
    DECLARE dba BOOLEAN;
    SET dba = is_admin();

    SELECT user_id
      INTO current_uid
      FROM view_immotool_users
      WHERE user_login=USER();

    -- create new entry
    IF val_helloworld_id < 1 THEN
      -- set owner
      IF (val_access_owner_id IS NULL OR val_access_owner_id<1 OR dba = FALSE) THEN
        SET owner_uid = current_uid;
      ELSE
        SET owner_uid = val_access_owner_id;
      END IF;

      -- set group
      IF (val_access_group_id IS NULL OR val_access_group_id<1) THEN
        SELECT group_id
          INTO owner_gid
          FROM view_immotool_groups
          WHERE group_name='IMMOTOOL';
      ELSE
        SET owner_gid = val_access_group_id;
      END IF;

      -- set permissions
      IF (val_access_permissions IS NULL OR val_access_permissions<0) THEN
        SET permissions = 63;
      ELSE
        SET permissions = val_access_permissions;
      END IF;

      -- insert entry
      INSERT INTO immotool_helloworld (
        helloworld_name,
        helloworld_notes,
        access_owner_id,
        access_group_id,
        access_permissions )
      VALUES (
        val_helloworld_name,
        val_helloworld_notes,
        owner_uid,
        owner_gid,
        permissions);

      -- return entry id
      SELECT helloworld_id
        INTO val_helloworld_id
        FROM immotool_helloworld
        WHERE helloworld_id = CURRENT VALUE FOR seq_immotool_helloworld;

    -- update existing entry
    ELSE
      -- make sure, that the user has permission for update
      SELECT access_permissions, access_owner_id, access_group_id
        INTO permissions, owner_uid, owner_gid
        FROM immotool_helloworld
        WHERE helloworld_id = val_helloworld_id;

      IF dba = FALSE THEN
        SET allowed = can_write(permissions, owner_uid, owner_gid);
        IF NOT allowed = TRUE THEN
          SIGNAL SQLSTATE '45000';
        END IF;
      END IF;

      -- only administrators may change owner
      IF dba = TRUE AND val_access_owner_id IS NOT NULL AND val_access_owner_id>1 THEN
        SET owner_uid = val_access_owner_id;
      END IF;

      IF dba = TRUE OR owner_uid = current_uid THEN

        -- only administrators and owners may change group
        IF val_access_group_id IS NOT NULL AND val_access_group_id>1 THEN
          SET owner_gid = val_access_group_id;
        END IF;

        -- only administrators and owners may permissions
        IF val_access_permissions IS NOT NULL AND val_access_permissions>1 THEN
          SET permissions = val_access_permissions;
        END IF;
      END IF;

      -- update entry
      UPDATE immotool_helloworld
        SET
          helloworld_name = val_helloworld_name,
          helloworld_notes = val_helloworld_notes,
          access_owner_id = owner_uid,
          access_group_id = owner_gid,
          access_permissions = permissions,
          modified_at = NOW()
        WHERE
          helloworld_id = val_helloworld_id;

    END IF;

  END;
.;

GRANT EXECUTE
  ON PROCEDURE save_immotool_helloworld
  TO "IMMOTOOL";
