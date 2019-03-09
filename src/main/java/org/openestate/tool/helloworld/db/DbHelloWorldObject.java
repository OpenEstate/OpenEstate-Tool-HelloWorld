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

import com.openindex.openestate.tool.utils.Permission;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * An object, that is stored into the database for HelloWorld addon.
 *
 * @author Andreas Rudolph <andy@openindex.de>
 */
@SuppressFBWarnings(
        value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD",
        justification = "Public fields are kept for compatibility with the database model.")
public class DbHelloWorldObject implements Serializable {
    private final static Logger LOGGER = LoggerFactory.getLogger(DbHelloWorldObject.class);
    private final static I18n I18N = I18nFactory.getI18n(DbHelloWorldObject.class);
    public long id = 0;
    public String name = null;
    public String notes = null;
    public Date createdAt = null;
    public Date modifiedAt = null;
    public long ownerUserId = 0;
    public long ownerGroupId = 0;
    public Permission permission = null;
}