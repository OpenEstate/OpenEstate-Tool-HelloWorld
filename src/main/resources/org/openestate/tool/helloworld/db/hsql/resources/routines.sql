-- -----------------------------------------------------
-- HelloWorld-Addon for OpenEstate-ImmoTool
-- routines for HSQLDB
-- Copyright (C) 2012-2017 OpenEstate.org
-- -----------------------------------------------------
\.
-- -----------------------------------------------------
-- Function can_delete_immotool_helloworld
-- -----------------------------------------------------
CREATE FUNCTION can_delete_immotool_helloworld( val_helloworld_id BIGINT )
  RETURNS BOOLEAN
  READS SQL DATA
  BEGIN ATOMIC
    DECLARE permissions INT;
    DECLARE owner_uid BIGINT;
    DECLARE owner_gid BIGINT;

    -- auf Administrator-Rechte prüfen
    IF NOT is_admin() = TRUE THEN
      RETURN TRUE;
    END IF;

    -- auf Benutzer-Rechte prüfen
    SELECT access_permissions, access_owner_id, access_group_id
      INTO permissions, owner_uid, owner_gid
      FROM immotool_helloworld
      WHERE helloworld_id = val_helloworld_id;

    RETURN can_delete( permissions, owner_uid, owner_gid );
  END;
.;
\.
-- -----------------------------------------------------
-- Function can_write_immotool_helloworld
-- -----------------------------------------------------
CREATE FUNCTION can_write_immotool_helloworld( val_helloworld_id BIGINT )
  RETURNS BOOLEAN
  READS SQL DATA
  BEGIN ATOMIC
    DECLARE permissions INT;
    DECLARE owner_uid BIGINT;
    DECLARE owner_gid BIGINT;

    -- auf Administrator-Rechte prüfen
    IF NOT is_admin() = TRUE THEN
      RETURN TRUE;
    END IF;

    -- auf Benutzer-Rechte prüfen
    SELECT access_permissions, access_owner_id, access_group_id
      INTO permissions, owner_uid, owner_gid
      FROM immotool_helloworld
      WHERE helloworld_id = val_helloworld_id;

    RETURN can_write( permissions, owner_uid, owner_gid );
  END;
.;
\.
-- -----------------------------------------------------
-- Procedure remove_immotool_helloworld
-- -----------------------------------------------------
CREATE PROCEDURE remove_immotool_helloworld( val_helloworld_id BIGINT )
  MODIFIES SQL DATA
  BEGIN ATOMIC
    DECLARE allowed BOOLEAN;

    IF val_helloworld_id < 1 THEN
      SIGNAL SQLSTATE '45000';
    END IF;

    -- sicherstellen, dass der Benutzer Lösch-Rechte besitzt
    SET allowed = can_delete_immotool_helloworld( val_helloworld_id );
    IF NOT allowed = TRUE THEN
      SIGNAL SQLSTATE '45000';
    END IF;

    -- Eintrag entfernen
    DELETE
      FROM immotool_helloworld
      WHERE helloworld_id = val_helloworld_id;

  END;
.;

GRANT EXECUTE
  ON PROCEDURE remove_immotool_helloworld
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

    -- Neuen Eintrag erzeugen
    IF val_helloworld_id < 1 THEN
      -- Inhaber-Benutzer ggf. automatisch setzen
      IF (val_access_owner_id IS NULL OR val_access_owner_id<1 OR dba = FALSE) THEN
        SET owner_uid = current_uid;
      ELSE
        SET owner_uid = val_access_owner_id;
      END IF;

      -- Inhaber-Gruppe ggf. automatisch setzen
      IF (val_access_group_id IS NULL OR val_access_group_id<1) THEN
        SELECT group_id
          INTO owner_gid
          FROM view_immotool_groups
          WHERE group_name='IMMOTOOL';
      ELSE
        SET owner_gid = val_access_group_id;
      END IF;

      -- Berechtigungen ggf. automatisch setzen
      IF (val_access_permissions IS NULL OR val_access_permissions<0) THEN
        SET permissions = 63;
      ELSE
        SET permissions = val_access_permissions;
      END IF;

      -- Eintrag speichern
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

      -- ID zurückliefern
      SELECT helloworld_id
        INTO val_helloworld_id
        FROM immotool_helloworld
        WHERE helloworld_id = CURRENT VALUE FOR seq_immotool_helloworld;

    -- Bestehenden Eintrag bearbeiten
    ELSE
      -- Prüfen ob Schreib-Rechte vorliegen
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

      -- Wechsel des Inhaber-Benutzers darf nur der Administrator durchführen
      IF dba = TRUE AND val_access_owner_id IS NOT NULL AND val_access_owner_id>1 THEN
        SET owner_uid = val_access_owner_id;
      END IF;

      IF dba = TRUE OR owner_uid = current_uid THEN

        -- Wechsel der Inhaber-Gruppe darf nur der Administrator oder Inhaber durchführen
        IF val_access_group_id IS NOT NULL AND val_access_group_id>1 THEN
          SET owner_gid = val_access_group_id;
        END IF;

        -- Wechsel der Berechtigungen darf nur der Administrator oder Inhaber durchführen
        IF val_access_permissions IS NOT NULL AND val_access_permissions>1 THEN
          SET permissions = val_access_permissions;
        END IF;
      END IF;

      -- Eintrag bearbeiten
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
