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
package org.openestate.tool.helloworld;

import com.openindex.openestate.tool.ImmoToolEnvironment;
import com.openindex.openestate.tool.ImmoToolProject;
import com.openindex.openestate.tool.db.AbstractDbDriver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;
import org.openestate.tool.helloworld.extensions.DbHelloWorldExtension;
import org.openestate.tool.helloworld.extensions.ObjectViewExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HelloWorldPluginUtils.
 *
 * @author Andreas Rudolph <andy@openindex.de>
 */
public class HelloWorldPluginUtils
{
  private final static Logger LOGGER = LoggerFactory.getLogger( HelloWorldPluginUtils.class );

  public static DbHelloWorldExtension getDbHelloWorldExtension( ImmoToolProject project )
  {
    return (project!=null)? getDbHelloWorldExtension(  project.getDbDriver() ): null;
  }

  public static DbHelloWorldExtension getDbHelloWorldExtension( AbstractDbDriver driver )
  {
    return (driver!=null)? getDbHelloWorldExtension( driver.getName() ): null;
  }

  public static DbHelloWorldExtension getDbHelloWorldExtension( String driverName )
  {
    for (Object ext : getExtensionHandlers( DbHelloWorldExtension.ID, DbHelloWorldExtension.class, null, null ))
    {
      DbHelloWorldExtension dbExtension = (DbHelloWorldExtension) ext;
      if (dbExtension.isSupportedDriver( driverName )) return dbExtension;
    }
    return null;
  }

  public static Collection<DbHelloWorldExtension> getDbHelloWorldExtensions()
  {
    List<DbHelloWorldExtension> extensions = new ArrayList<DbHelloWorldExtension>();
    for (Object ext : getExtensionHandlers( DbHelloWorldExtension.ID, DbHelloWorldExtension.class, null, null ))
    {
      extensions.add( (DbHelloWorldExtension) ext );
    }
    return extensions;
  }

  private static Collection<Object> getExtensionHandlers( String extensionName, Class handlerClass, ImmoToolProject project, String[] pluginIds )
  {
    List<Object> handlers = new ArrayList<Object>();
    ExtensionPoint point = getExtensionPoint( extensionName );
    if (point==null)
    {
      LOGGER.warn( "Can't find extension-point!" );
      LOGGER.warn( "> " + extensionName );
      return handlers;
    }
    //LOGGER.debug( "Lookup extension handlers '" + extensionName + "'..." );
    List<String> pluginIdList = (pluginIds!=null && pluginIds.length>0)? Arrays.asList( pluginIds ): null;
    for (Extension ext : point.getConnectedExtensions())
    {
      final PluginDescriptor plugin = ext.getDeclaringPluginDescriptor();

      // ggf. nur die Extensions eines bestimmten Plugins ermitteln
      if (pluginIdList!=null && !pluginIdList.contains( plugin.getId() )) continue;

      // ggf. nur die Extensions von Plugins ermitteln, die im aktuellen Projekt aktiviert sind
      if (project!=null && !ImmoToolEnvironment.APP_PLUGIN.equals( plugin.getId() ) && !project.isPluginUsable( plugin.getId() )) continue;

      // Objekte erzeugen, die im Parameter 'handler' übermittelt wurden
      for (Extension.Parameter p : ext.getParameters( "handler" ))
      {
        try
        {
          String clazz = p.valueAsString();
          //LOGGER.debug( "> class: " + handlerClass );
          if (clazz==null || clazz.trim().length()==0) continue;
          ClassLoader cl = ImmoToolEnvironment.getPluginManager().getPluginClassLoader( plugin );
          Object handler = cl.loadClass( clazz.trim() ).newInstance();
          if (handler==null)
            LOGGER.warn( "Can't create handler-class: " + clazz );
          else if (!handlerClass.isInstance( handler ))
            LOGGER.warn( "The provided handler-class '" + clazz + "' is not an instance of '" + handlerClass.getName() + "'!" );
          else
            handlers.add( handler );
        }
        catch (Exception ex)
        {
          LOGGER.warn( "Can't create extension-handler!" );
          LOGGER.warn( "> " + ex.getLocalizedMessage(), ex );
        }
      }
    }
    return handlers;
  }

  private static ExtensionPoint getExtensionPoint( String pointId )
  {
    return ImmoToolEnvironment.getExtensionPoint( HelloWorldPlugin.ID, pointId );
  }

  public static Collection<ObjectViewExtension> getObjectViewExtensions()
  {
    final ImmoToolProject project = ImmoToolProject.getAppInstance();
    List<ObjectViewExtension> extensions = new ArrayList<ObjectViewExtension>();
    for (Object ext : getExtensionHandlers( ObjectViewExtension.ID, ObjectViewExtension.class, project, null ))
    {
      extensions.add( (ObjectViewExtension) ext );
    }
    return extensions;
  }
}