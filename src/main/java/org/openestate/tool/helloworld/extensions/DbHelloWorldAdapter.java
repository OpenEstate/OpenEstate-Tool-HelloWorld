/*
 * Copyright 2012-2015 OpenEstate.org.
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
package org.openestate.tool.helloworld.extensions;

import com.openindex.openestate.tool.db.AbstractDbDriver;
import com.openindex.openestate.tool.db.DbUpdateHandler;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.lang.ArrayUtils;
import org.openestate.tool.helloworld.db.DbHelloWorldHandler;

/**
 * DbHelloWorldAdapter.
 *
 * @author Andreas Rudolph <andy@openindex.de>
 */
public abstract class DbHelloWorldAdapter implements DbHelloWorldExtension
{
  @Override
  public abstract DbHelloWorldHandler getHelloWorldHandler();

  @Override
  public String[] getRequiredPluginIds()
  {
    return null;
  }

  @Override
  public abstract String[] getRequiredProcedures();

  @Override
  public abstract String[] getRequiredViews();

  @Override
  public abstract String[] getSupportedDrivers();

  @Override
  public abstract String getUninstallQuery() throws IOException;

  @Override
  public abstract DbUpdateHandler getUpdateHandler();

  @Override
  public abstract void install( Connection c ) throws IOException, SQLException;

  @Override
  public final boolean isSupportedDriver( String driverName )
  {
    return driverName!=null && ArrayUtils.contains( getSupportedDrivers(), driverName );
  }

  @Override
  public void repair( Connection c, AbstractDbDriver driver ) throws SQLException
  {
  }
}