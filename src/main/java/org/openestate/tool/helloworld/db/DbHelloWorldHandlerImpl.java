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
package org.openestate.tool.helloworld.db;

import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * General implementation of database operations for HelloWorld addon.
 * <p>
 * This class may be extended for database specific implementations.
 *
 * @author Andreas Rudolph
 */
public abstract class DbHelloWorldHandlerImpl implements DbHelloWorldHandler {
    @SuppressWarnings("unused")
    private final static Logger LOGGER = LoggerFactory.getLogger(DbHelloWorldHandlerImpl.class);
    @SuppressWarnings("unused")
    private final static I18n I18N = I18nFactory.getI18n(DbHelloWorldHandlerImpl.class);

    protected DbHelloWorldObject createObject() {
        return new DbHelloWorldObject();
    }

    @Override
    public final DbHelloWorldObject getObject(Connection c, long id) throws SQLException {
        DbHelloWorldObject[] result = getObjects(c, new long[]{id});
        return (result != null && result.length > 0) ? result[0] : null;
    }

    @Override
    public final DbHelloWorldObject[] getObjects(Connection c) throws SQLException {
        return getObjects(c, null);
    }

    @Override
    public abstract DbHelloWorldObject[] getObjects(Connection c, long[] ids) throws SQLException;

    @Override
    public abstract long[] getObjectIds(Connection c) throws SQLException;

    @Override
    public final void removeObject(Connection c, long id) throws SQLException {
        removeObjects(c, new long[]{id});
    }

    @Override
    public abstract void removeObjects(Connection c, long[] ids) throws SQLException;

    @Override
    public abstract void saveObject(Connection c, DbHelloWorldObject feed) throws SQLException;
}