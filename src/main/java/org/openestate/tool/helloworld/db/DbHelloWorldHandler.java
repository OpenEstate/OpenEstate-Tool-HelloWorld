/*
 * Copyright 2012-2017 OpenEstate.org.
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
 * Specification of database operations for HelloWorld addon.
 *
 * @author Andreas Rudolph <andy@openindex.de>
 */
public interface DbHelloWorldHandler
{
  public DbHelloWorldObject getObject( Connection c, long id ) throws SQLException;

  public DbHelloWorldObject[] getObjects( Connection c ) throws SQLException;

  public DbHelloWorldObject[] getObjects( Connection c, long[] ids ) throws SQLException;

  public long[] getObjectIds( Connection c ) throws SQLException;

  public void removeObject( Connection c, long id ) throws SQLException;

  public void removeObjects( Connection c, long[] ids ) throws SQLException;

  public void saveObject( Connection c, DbHelloWorldObject feed ) throws SQLException;
}