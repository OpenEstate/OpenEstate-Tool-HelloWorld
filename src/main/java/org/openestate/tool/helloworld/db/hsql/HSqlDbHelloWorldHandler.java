/*
 * Copyright 2012-2019 OpenEstate.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openestate.tool.helloworld.db.hsql;

import com.openindex.openestate.impl.db.JdbcUtils;
import com.openindex.openestate.impl.db.NamedCallableStatement;
import com.openindex.openestate.tool.utils.Permission;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.openestate.tool.helloworld.db.DbHelloWorldHandlerImpl;
import org.openestate.tool.helloworld.db.DbHelloWorldObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * Implementation of database operations on a HSQL database.
 *
 * @author Andreas Rudolph <andy@openindex.de>
 */
public class HSqlDbHelloWorldHandler extends DbHelloWorldHandlerImpl
{
  private final static Logger LOGGER = LoggerFactory.getLogger( HSqlDbHelloWorldHandler.class );
  private final static I18n I18N = I18nFactory.getI18n( HSqlDbHelloWorldHandler.class );
  public final static String PROC_REMOVE_HELLOWORLD = "remove_immotool_helloworld";
  public final static String PROC_SAVE_HELLOWORLD = "save_immotool_helloworld";
  public final static String VIEW_HELLOWORLD = "view_immotool_helloworld";
  public final static String TABLE_HELLOWORLD = "immotool_helloworld";
  public final static String FIELD_HELLOWORLD_ID = "helloworld_id";
  public final static String FIELD_HELLOWORLD_NAME = "helloworld_name";
  public final static String FIELD_HELLOWORLD_NOTES = "helloworld_notes";
  public final static String FIELD_CREATED_AT = "created_at";
  public final static String FIELD_MODIFIED_AT = "modified_at";
  public final static String FIELD_ACCESS_OWNER_ID = "access_owner_id";
  public final static String FIELD_ACCESS_GROUP_ID =   "access_group_id";
  public final static String FIELD_ACCESS_PERMISSIONS = "access_permissions";

  protected DbHelloWorldObject buildObject( ResultSet result ) throws SQLException
  {
    DbHelloWorldObject object = createObject();
    object.id = result.getLong( FIELD_HELLOWORLD_ID );
    object.name = result.getString( FIELD_HELLOWORLD_NAME );
    object.notes = result.getString( FIELD_HELLOWORLD_NOTES );
    object.createdAt = result.getDate( FIELD_CREATED_AT );
    object.modifiedAt = result.getDate( FIELD_MODIFIED_AT );
    object.ownerGroupId = result.getLong( FIELD_ACCESS_GROUP_ID );
    object.ownerUserId = result.getLong( FIELD_ACCESS_OWNER_ID );
    object.permission = new Permission( result.getInt( FIELD_ACCESS_PERMISSIONS ) );
    return object;
  }

  @Override
  public DbHelloWorldObject[] getObjects( Connection c, long[] ids ) throws SQLException
  {
    if (ids!=null && ids.length<1) return new DbHelloWorldObject[]{};
    PreparedStatement statement = null;
    ResultSet result = null;
    try
    {
      if (ids==null)
      {
        statement = c.prepareStatement( "SELECT * "
          + "FROM " + VIEW_HELLOWORLD + " "
          + "ORDER BY " + FIELD_HELLOWORLD_NAME + " ASC;" );
      }
      else
      {
        statement = c.prepareStatement( "SELECT * "
          + "FROM " + VIEW_HELLOWORLD + " "
          + "WHERE " + FIELD_HELLOWORLD_ID + " IN (" + JdbcUtils.writeQuestionMarkList( ids.length ) + ") "
          + "ORDER BY " + FIELD_HELLOWORLD_NAME + " ASC "
          + "LIMIT " + ids.length + ";" );
        for (int i=0; i<ids.length; i++)
        {
          statement.setLong( i+1, ids[i] );
        }
      }
      result = statement.executeQuery();
      List<DbHelloWorldObject> objects = new ArrayList<>();
      while (result.next())
      {
        objects.add( buildObject( result ) );
      }
      return objects.toArray( new DbHelloWorldObject[objects.size()] );
    }
    finally
    {
      JdbcUtils.closeQuietly( result );
      JdbcUtils.closeQuietly( statement );
    }
  }

  @Override
  public long[] getObjectIds( Connection c ) throws SQLException
  {
    PreparedStatement statement = null;
    ResultSet result = null;
    try
    {
      statement = c.prepareStatement( "SELECT " + FIELD_HELLOWORLD_ID + " "
        + "FROM " + VIEW_HELLOWORLD + " "
        + "ORDER BY " + FIELD_HELLOWORLD_ID + " ASC;" );
      result = statement.executeQuery();
      List<Long> ids = new ArrayList<>();
      while (result.next())
      {
        ids.add( result.getLong( FIELD_HELLOWORLD_ID ) );
      }
      return ArrayUtils.toPrimitive( ids.toArray( new Long[ids.size()] ) );
    }
    finally
    {
      JdbcUtils.closeQuietly( result );
      JdbcUtils.closeQuietly( statement );
    }
  }

  @Override
  public void removeObjects( Connection c, long[] ids ) throws SQLException
  {
    if (ids.length<1) return;
    final boolean oldAutoCommit = c.getAutoCommit();
    CallableStatement removeStatement = null;
    try
    {
      c.setAutoCommit( false );
      removeStatement = c.prepareCall( "CALL " + PROC_REMOVE_HELLOWORLD + "(?);" );
      for (long id : ids)
      {
        removeStatement.clearParameters();
        removeStatement.setLong( 1, id );
        removeStatement.execute();
      }
      c.commit();
    }
    catch (SQLException ex )
    {
      c.rollback();
      throw ex;
    }
    finally
    {
      JdbcUtils.closeQuietly( removeStatement );
      c.setAutoCommit( oldAutoCommit );
    }
  }

  @Override
  public void saveObject( Connection c, DbHelloWorldObject feed ) throws SQLException
  {
    final boolean oldAutoCommit = c.getAutoCommit();
    NamedCallableStatement saveStatement = null;
    try
    {
      c.setAutoCommit( false );
      saveStatement = new NamedCallableStatement( c, "CALL " + PROC_SAVE_HELLOWORLD + "("
        + ":" + FIELD_HELLOWORLD_ID + ", "
        + ":" + FIELD_HELLOWORLD_NAME + ", "
        + ":" + FIELD_HELLOWORLD_NOTES + ", "
        + ":" + FIELD_ACCESS_OWNER_ID + ", "
        + ":" + FIELD_ACCESS_GROUP_ID + ", "
        + ":" + FIELD_ACCESS_PERMISSIONS + ");" );
      saveStatement.setLong( FIELD_HELLOWORLD_ID, feed.id );
      saveStatement.setString( FIELD_HELLOWORLD_NOTES, feed.notes );
      saveStatement.setString( FIELD_HELLOWORLD_NAME, StringUtils.abbreviate( feed.name, 100 ) );
      saveStatement.setLong( FIELD_ACCESS_OWNER_ID, feed.ownerUserId );
      saveStatement.setLong( FIELD_ACCESS_GROUP_ID, feed.ownerGroupId );
      saveStatement.setInt( FIELD_ACCESS_PERMISSIONS, (feed.permission!=null)? feed.permission.getValue(): -1 );
      saveStatement.execute();

      // ID des erzeugten / bearbeiteten Datensatzes ermitteln
      final long referencedId = (feed.id<1)? saveStatement.getLong( FIELD_HELLOWORLD_ID ): feed.id;
      if (referencedId<1) throw new SQLException( "Can't determine ID of the saved object!" );

      c.commit();

      // Nach einem INSERT die ID des erzeugten Datensatzes übernehmen
      if (feed.id<=0)
      {
        feed.id = referencedId;
      }
    }
    catch (SQLException ex)
    {
      c.rollback();
      throw ex;
    }
    finally
    {
      JdbcUtils.closeQuietly( saveStatement );
      c.setAutoCommit( oldAutoCommit );
    }
  }
}