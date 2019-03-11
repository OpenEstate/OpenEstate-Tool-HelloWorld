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
import com.openindex.openestate.tool.ImmoToolAppUtils;
import com.openindex.openestate.tool.ImmoToolEnvironment;
import com.openindex.openestate.tool.ImmoToolProject;
import com.openindex.openestate.tool.ImmoToolTask;
import com.openindex.openestate.tool.ImmoToolUtils;
import com.openindex.openestate.tool.db.AbstractDbDriver;
import com.openindex.openestate.tool.gui.AbstractMainTab;
import java.sql.Connection;
import org.openestate.tool.helloworld.db.DbHelloWorldHandler;
import org.openestate.tool.helloworld.db.DbHelloWorldObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * Show form to create or edit an object of the HelloWorld addon.
 *
 * @author Andreas Rudolph
 */
public class HelloWorldObjectViewTask extends ImmoToolTask<HelloWorldObjectViewPanel, Void> {
    @SuppressWarnings("unused")
    private final static Logger LOGGER = LoggerFactory.getLogger(HelloWorldObjectViewTask.class);
    private final static I18n I18N = I18nFactory.getI18n(HelloWorldObjectViewTask.class);
    private final long objectId;
    private final boolean selectCreatedTab;
    private final HelloWorldObjectViewPanel viewTab;
    private final AbstractDbDriver dbDriver;

    public HelloWorldObjectViewTask(AbstractDbDriver dbDriver, long objectId) {
        this(dbDriver, objectId, true);
    }

    public HelloWorldObjectViewTask(AbstractDbDriver dbDriver, long objectId, boolean selectCreatedTab) {
        super(I18N.tr("Loading object {0}.", "#" + objectId));
        this.dbDriver = dbDriver;
        this.objectId = objectId;
        this.selectCreatedTab = selectCreatedTab;
        this.viewTab = null;
    }

    public HelloWorldObjectViewTask(AbstractDbDriver dbDriver, long objectId, HelloWorldObjectViewPanel viewTab) {
        super(I18N.tr("Loading object {0}.", "#" + objectId));
        this.dbDriver = dbDriver;
        this.objectId = objectId;
        this.selectCreatedTab = true;
        this.viewTab = viewTab;
    }

    @Override
    protected HelloWorldObjectViewPanel doInBackground() throws Exception {
        final DbHelloWorldHandler dbHandler = HelloWorldPlugin.getDbHelloWorldExtension().getHelloWorldHandler();

        // check, if a tab for the selected object is already opened
        if (viewTab == null) {
            AbstractMainTab[] tabs = ImmoToolAppUtils.getTabs(HelloWorldObjectViewPanel.class);
            for (AbstractMainTab tab : tabs) {
                HelloWorldObjectViewPanel form = (HelloWorldObjectViewPanel) tab;
                if (objectId < 1 && form.getCurrentObjectId() < 1) {
                    if (selectCreatedTab) ImmoToolAppUtils.selectTab(form);
                    return null;
                } else if (objectId > 0 && form.getCurrentObjectId() == objectId) {
                    if (selectCreatedTab) ImmoToolAppUtils.selectTab(form);
                    return null;
                }
            }
        }

        // create a new tab for the object
        final HelloWorldObjectViewPanel form;
        if (objectId < 1) {
            form = (viewTab != null) ? viewTab : HelloWorldObjectViewPanel.createTab();
        } else {
            Connection c = null;
            try {
                c = dbDriver.getConnection();
                DbHelloWorldObject object = dbHandler.getObject(c, objectId);
                if (object == null) throw new Exception("Can't find object #" + objectId + "!");
                if (viewTab == null) {
                    form = HelloWorldObjectViewPanel.createTab(object);
                } else {
                    form = viewTab;
                    form.setObject(object);
                }
            } finally {
                JdbcUtils.closeQuietly(c);
            }
        }
        return form;
    }

    @Override
    protected void failed(Throwable ex) {
        super.failed(ex);
        ImmoToolUtils.showMessageErrorDialog(
                I18N.tr("Can't load object!"), ex, ImmoToolEnvironment.getFrame());
    }

    @Override
    protected void succeeded(HelloWorldObjectViewPanel form) {
        super.succeeded(form);
        if (form != null) {
            ImmoToolAppUtils.showTab(form, selectCreatedTab);
            form.loadInBackground(ImmoToolProject.getAppInstance().getDbDriver());
        }
    }
}