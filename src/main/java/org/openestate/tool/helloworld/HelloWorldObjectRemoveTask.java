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
package org.openestate.tool.helloworld;

import com.openindex.openestate.impl.db.JdbcUtils;
import com.openindex.openestate.tool.ImmoToolEnvironment;
import com.openindex.openestate.tool.ImmoToolTask;
import com.openindex.openestate.tool.ImmoToolUtils;
import com.openindex.openestate.tool.db.AbstractDbDriver;
import java.sql.Connection;
import org.openestate.tool.helloworld.db.DbHelloWorldHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * Remove an object of the HelloWorld addon.
 *
 * @author Andreas Rudolph
 */
public class HelloWorldObjectRemoveTask extends ImmoToolTask<Boolean, Void> {
    @SuppressWarnings("unused")
    private final static Logger LOGGER = LoggerFactory.getLogger(HelloWorldObjectRemoveTask.class);
    private final static I18n I18N = I18nFactory.getI18n(HelloWorldObjectRemoveTask.class);
    private final long objectId;
    private final AbstractDbDriver dbDriver;

    public HelloWorldObjectRemoveTask(AbstractDbDriver dbDriver, long objectId) {
        super();
        this.dbDriver = dbDriver;
        this.objectId = objectId;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        final DbHelloWorldHandler dbHandler = HelloWorldPlugin.getDbHelloWorldExtension().getHelloWorldHandler();
        Connection c = null;
        try {
            c = dbDriver.getConnection();
            dbHandler.removeObject(c, objectId);
            return true;
        } finally {
            JdbcUtils.closeQuietly(c);
        }
    }

    @Override
    protected void failed(Throwable t) {
        super.failed(t);
        ImmoToolUtils.showMessageErrorDialog(
                I18N.tr("Can't remove object!"), t, ImmoToolEnvironment.getFrame());
    }

    @Override
    protected void succeeded(Boolean result) {
        super.succeeded(result);

        // refresh sidebar after an object was removed
        if (Boolean.TRUE.equals(result)) {
            HelloWorldPlugin.refreshSidebar();
        }
    }
}