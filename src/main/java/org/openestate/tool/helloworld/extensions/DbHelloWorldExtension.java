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
import com.openindex.openestate.tool.extensions.BasicExtension;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.openestate.tool.helloworld.db.DbHelloWorldHandler;

/**
 * An extension point, that provides database access for HelloWorld addon.
 *
 * @author Andreas Rudolph <andy@openindex.de>
 */
public interface DbHelloWorldExtension extends BasicExtension
{
  public final static String ID = "DbHelloWorldExtension";

  public DbHelloWorldHandler getHelloWorldHandler();

  public String[] getRequiredProcedures();

  public String[] getRequiredViews();

  public String[] getSupportedDrivers();

  public String getUninstallQuery() throws IOException;

  public DbUpdateHandler getUpdateHandler();

  public void install( Connection c ) throws IOException, SQLException;

  public boolean isSupportedDriver( String driverName );

  public void repair( Connection c, AbstractDbDriver driver ) throws SQLException;
}