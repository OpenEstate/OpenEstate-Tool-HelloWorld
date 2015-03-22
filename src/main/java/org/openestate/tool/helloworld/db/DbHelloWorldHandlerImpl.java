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
package org.openestate.tool.helloworld.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * DbHelloWorldHandlerImpl.
 *
 * @author Andreas Rudolph <andy@openindex.de>
 */
public abstract class DbHelloWorldHandlerImpl implements DbHelloWorldHandler
{
  protected DbHelloWorldObject createObject()
  {
    return new DbHelloWorldObject();
  }

  @Override
  public final DbHelloWorldObject getObject( Connection c, long id ) throws SQLException
  {
    DbHelloWorldObject[] result = getObjects( c, new long[]{id} );
    return (result!=null && result.length>0)? result[0]: null;
  }

  @Override
  public final DbHelloWorldObject[] getObjects( Connection c ) throws SQLException
  {
    return getObjects( c, null );
  }

  @Override
  public abstract DbHelloWorldObject[] getObjects( Connection c, long[] ids ) throws SQLException;

  @Override
  public abstract long[] getObjectIds( Connection c ) throws SQLException;

  @Override
  public final void removeObject( Connection c, long id ) throws SQLException
  {
    removeObjects( c, new long[]{id} );
  }

  @Override
  public abstract void removeObjects( Connection c, long[] ids ) throws SQLException;

  @Override
  public abstract void saveObject( Connection c, DbHelloWorldObject feed ) throws SQLException;
}