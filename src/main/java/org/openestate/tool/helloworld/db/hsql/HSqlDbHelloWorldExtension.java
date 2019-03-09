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
import com.openindex.openestate.tool.db.AbstractDbDriver;
import com.openindex.openestate.tool.db.AbstractDbUpdateListener;
import com.openindex.openestate.tool.db.DbUpdateHandler;
import com.openindex.openestate.tool.db.hsql.HSqlDbUpdateHandler;
import com.openindex.openestate.tool.db.hsql.HSqlLocalDriver;
import com.openindex.openestate.tool.db.hsql.HSqlRemoteDriver;
import com.openindex.openestate.tool.db.hsql.HSqlUtils;
import com.openindex.openestate.tool.extensions.DbExtension;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hsqldb.cmdline.SqlFile;
import org.hsqldb.cmdline.SqlToolError;
import org.openestate.tool.helloworld.HelloWorldPlugin;
import org.openestate.tool.helloworld.db.DbHelloWorldHandler;
import org.openestate.tool.helloworld.db.DbHelloWorldUpdateListener;
import org.openestate.tool.helloworld.extensions.DbHelloWorldAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * Implementation of database access on a HSQL database.
 *
 * @author Andreas Rudolph <andy@openindex.de>
 */
public class HSqlDbHelloWorldExtension extends DbHelloWorldAdapter
{
  private final static Logger LOGGER = LoggerFactory.getLogger( HSqlDbHelloWorldExtension.class );
  private final static I18n I18N = I18nFactory.getI18n( HSqlDbHelloWorldExtension.class );
  public final static String RESOURCE_PATH = "/org/openestate/tool/helloworld/db/hsql/resources/";
  private final static DbHelloWorldHandler HELLO_WORLD_HANDLER = new HSqlDbHelloWorldHandler();

  @Override
  public DbHelloWorldHandler getHelloWorldHandler()
  {
    return HELLO_WORLD_HANDLER;
  }

  @Override
  public final String[] getRequiredProcedures()
  {
    return new String[]{
      HSqlDbHelloWorldHandler.PROC_REMOVE_HELLOWORLD,
      HSqlDbHelloWorldHandler.PROC_SAVE_HELLOWORLD,
    };
  }

  @Override
  public final String[] getRequiredViews()
  {
    return new String[]{
      HSqlDbHelloWorldHandler.VIEW_HELLOWORLD,
    };
  }

  @Override
  public String[] getSupportedDrivers()
  {
    return new String[]{ HSqlLocalDriver.NAME, HSqlRemoteDriver.NAME };
  }

  @Override
  public String getUninstallQuery() throws IOException
  {
    return readHsqlQuery( "uninstall.sql" );
  }

  @Override
  public DbUpdateHandler getUpdateHandler()
  {
    String uninstallQuery = null;
    try
    {
      uninstallQuery = getUninstallQuery();
    }
    catch (IOException ex)
    {
      LOGGER.warn( "Can't load uninstall query!" );
      LOGGER.warn( "> " + ex.getLocalizedMessage(), ex );
    }
    return new HelloWorldUpdateHandler(
      uninstallQuery, new DbHelloWorldUpdateListener() );
  }

  @Override
  public void install( Connection c ) throws IOException, SQLException
  {
    LOGGER.info( "Installing HelloWorld plugin." );
    Statement s = null;
    SqlFile f = null;
    try
    {
      s = c.createStatement();

      // import database schema
      f = readHsqlFile( "schema.sql" );
      f.setConnection( c );
      f.execute();
      f.closeReader();

      // import database routines
      f = readHsqlFile( "routines.sql" );
      f.setConnection( c );
      f.execute();
      f.closeReader();

      // write database structures
      s.execute( "CHECKPOINT;" );
    }
    catch (SqlToolError ex)
    {
      if (f!=null) f.closeReader();
      c.rollback();
      LOGGER.error( "Can't execute schema!" );
      LOGGER.error( "> " + ex.getLocalizedMessage(), ex );
      throw new SQLException( "Can't execute schema!" );
    }
    catch (SQLException ex)
    {
      if (f!=null) f.closeReader();
      c.rollback();
      throw ex;
    }
    finally
    {
      JdbcUtils.closeQuietly( s );
    }
  }

  private static SqlFile readHsqlFile( String file ) throws IOException
  {
    InputStream input = null;
    try
    {
      // read query from a resource file
      input = HSqlDbHelloWorldExtension.class.getResourceAsStream( RESOURCE_PATH + file );
      if (input==null) return null;
      List<String> lines = IOUtils.readLines( input, "UTF-8" );

      // write query into a temporary file
      File tempFile = File.createTempFile( "helloworld.", ".sql" );
      tempFile.deleteOnExit();
      FileUtils.writeLines( tempFile, lines );

      // create a SQL file
      return new SqlFile( tempFile, "UTF-8" );
    }
    finally
    {
      IOUtils.closeQuietly( input );
    }
  }

  private static String readHsqlQuery( String file ) throws IOException
  {
    InputStream input = null;
    try
    {
      // read query from a resource file
      input = HSqlDbHelloWorldExtension.class.getResourceAsStream( RESOURCE_PATH + file );
      if (input==null) return null;
      List<String> lines = IOUtils.readLines( input, "UTF-8" );

      // return query
      return StringUtils.join( lines, System.lineSeparator() );
    }
    finally
    {
      IOUtils.closeQuietly( input );
    }
  }

  @Override
  public void repair( Connection c, AbstractDbDriver driver ) throws SQLException
  {
    super.repair( c, driver );

    // recreate foreign keys
    repairForeignKeys( c, driver );
  }

  private static void repairForeignKeys( Connection c, AbstractDbDriver driver ) throws SQLException
  {
    // recreate foreign keys for access_owner_id fields
    HSqlUtils.updateAccessOwnerForeignKey(
      c, HSqlDbHelloWorldHandler.TABLE_HELLOWORLD );

    // recreate foreign keys for access_group_id fields
    HSqlUtils.updateAccessGroupForeignKey(
      c, HSqlDbHelloWorldHandler.TABLE_HELLOWORLD );
  }

  private final static class HelloWorldUpdateHandler extends HSqlDbUpdateHandler
  {
    public HelloWorldUpdateHandler( String uninstallQuery, AbstractDbUpdateListener listener )
    {
      super(
        HelloWorldPlugin.ID,
        HSqlDbUpdateHandler.Type.PLUGIN,
        HelloWorldPlugin.getInstance().getApiVersion(),
        uninstallQuery,
        HSqlDbHelloWorldExtension.RESOURCE_PATH,
        HelloWorldPlugin.class.getClassLoader(),
        listener );
    }

    /**
     * Structural changes on a HSQL database occured for the HelloWorld addon.
     */
    @Override
    public void updateFinished( Connection c, AbstractDbDriver dbDriver, DbExtension dbExtension, long oldDbVersion, long newDbVersion ) throws SQLException, IOException
    {
      super.updateFinished( c, dbDriver, dbExtension, oldDbVersion, newDbVersion );

      // recreate foreign keys
      HSqlDbHelloWorldExtension.repairForeignKeys( c, dbDriver );
    }
  }
}