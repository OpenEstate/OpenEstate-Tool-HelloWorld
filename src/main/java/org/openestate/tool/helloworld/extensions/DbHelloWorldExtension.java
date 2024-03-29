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
package org.openestate.tool.helloworld.extensions;

import com.openindex.openestate.tool.ImmoToolEnvironment;
import com.openindex.openestate.tool.ImmoToolProject;
import com.openindex.openestate.tool.db.AbstractDbDriver;
import com.openindex.openestate.tool.db.DbUpdateHandler;
import com.openindex.openestate.tool.extensions.BasicExtension;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import org.openestate.tool.helloworld.db.DbHelloWorldHandler;

/**
 * An extension point, that provides database access for HelloWorld addon.
 *
 * @author Andreas Rudolph
 */
public interface DbHelloWorldExtension extends BasicExtension {
    DbHelloWorldHandler getHelloWorldHandler();

    String[] getRequiredProcedures();

    String[] getRequiredViews();

    String[] getSupportedDrivers();

    String getUninstallQuery() throws IOException;

    DbUpdateHandler getUpdateHandler();

    void install(Connection c) throws IOException, SQLException;

    boolean isSupportedDriver(String driverName);

    void repair(Connection c, AbstractDbDriver driver) throws SQLException;

    /**
     * Load available implementations of {@link DbHelloWorldExtension}.
     *
     * @return available implementations of {@link DbHelloWorldExtension}
     */
    static Collection<DbHelloWorldExtension> load() {
        return ImmoToolEnvironment.getExtensions(DbHelloWorldExtension.class);
    }

    /**
     * Load a {@link DbHelloWorldExtension} for a specific database driver.
     *
     * @param driver database driver
     * @return implementation of {@link DbHelloWorldExtension} for the specified database driver
     */
    static DbHelloWorldExtension loadByDriver(AbstractDbDriver driver) {
        return (driver != null) ? loadByDriver(driver.getName()) : null;
    }

    /**
     * Load a {@link DbHelloWorldExtension} for a specific database driver.
     *
     * @param driverName internal name of the database driver
     * @return implementation of {@link DbHelloWorldExtension} for the specified database driver
     */
    static DbHelloWorldExtension loadByDriver(String driverName) {
        for (DbHelloWorldExtension ext : ImmoToolEnvironment.getExtensions(DbHelloWorldExtension.class)) {
            if (ext.isSupportedDriver(driverName)) return ext;
        }
        return null;
    }

    /**
     * Load a {@link DbHelloWorldExtension} for a project.
     *
     * @param project project
     * @return implementation of {@link DbHelloWorldExtension} for the specified project
     */
    static DbHelloWorldExtension loadByProject(ImmoToolProject project) {
        return (project != null) ? loadByDriver(project.getDbDriver()) : null;
    }
}