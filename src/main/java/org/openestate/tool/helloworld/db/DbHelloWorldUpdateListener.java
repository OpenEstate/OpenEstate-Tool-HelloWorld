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

import com.openindex.openestate.tool.db.AbstractDbDriver;
import com.openindex.openestate.tool.db.AbstractDbUpdateListener;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * A listener on the update process for HelloWorld addon.
 * <p>
 * This listener may execute specific code on database updates by the HelloWorld
 * addon.
 *
 * @author Andreas Rudolph <andy@openindex.de>
 */
public class DbHelloWorldUpdateListener extends AbstractDbUpdateListener {
    @SuppressWarnings("unused")
    private final static Logger LOGGER = LoggerFactory.getLogger(DbHelloWorldUpdateListener.class);
    @SuppressWarnings("unused")
    private final static I18n I18N = I18nFactory.getI18n(DbHelloWorldUpdateListener.class);

    @Override
    public void updateFinished(Connection c, AbstractDbDriver dbDriver, long oldDbVersion, long newDbVersion) {
        // launch some Java code, after the database was updated
    }
}