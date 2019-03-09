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

import com.openindex.openestate.tool.ImmoToolUtils;
import com.openindex.openestate.tool.utils.ProjectPermission;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * Permissions of the HelloWorld addon.
 *
 * @author Andreas Rudolph <andy@openindex.de>
 */
public enum HelloWorldPermission implements ProjectPermission
{
  USE_PLUGIN( null, I18n.marktr( "use addon" ) ),

  OBJECTS( USE_PLUGIN, I18n.marktr( "access to objects" ) ),
  OBJECTS_EDIT( OBJECTS, I18n.marktr( "edit objects" ) ),
  OBJECTS_REMOVE( OBJECTS, I18n.marktr( "remove objects" ) );

  private final static Logger LOGGER = LoggerFactory.getLogger( HelloWorldPermission.class );
  private final static I18n I18N = I18nFactory.getI18n( HelloWorldPermission.class );
  private final String i18nKey;
  private final ProjectPermission parent;

  private HelloWorldPermission( ProjectPermission parent, String i18nKey )
  {
    this.parent = parent;
    this.i18nKey = i18nKey;
  }

  @Override
  public ProjectPermission[] getChildren()
  {
    List<ProjectPermission> perms = new ArrayList<>();
    for (HelloWorldPermission p : values())
    {
      if (this.equals( p.parent )) perms.add( p );
    }
    return perms.toArray( new ProjectPermission[perms.size()] );
  }

  @Override
  public String getKey()
  {
    return name();
  }

  @Override
  public ProjectPermission getParent()
  {
    return parent;
  }

  @Override
  public String getTranslation()
  {
    return ImmoToolUtils.getI18nString( HelloWorldPermission.class, i18nKey );
  }

  @Override
  public String getTranslation( Locale locale )
  {
    return ImmoToolUtils.getI18nString( HelloWorldPermission.class, i18nKey, locale );
  }
}