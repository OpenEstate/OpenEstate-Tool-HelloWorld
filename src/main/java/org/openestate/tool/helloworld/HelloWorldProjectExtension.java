/*
 * Copyright 2012-2016 OpenEstate.org.
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

import com.openindex.openestate.tool.ImmoToolException;
import com.openindex.openestate.tool.ImmoToolProject;
import com.openindex.openestate.tool.extensions.ProjectAdapter;
import org.openestate.tool.helloworld.extensions.DbHelloWorldExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integrate HelloWorld addon into the project loading sequence.
 * <p>
 * This extensions adds functions, that are called when a project is closed /
 * opened / cleaned / repaired.
 *
 * @author Andreas Rudolph <andy@openindex.de>
 */
public class HelloWorldProjectExtension extends ProjectAdapter
{
  private final static Logger LOGGER = LoggerFactory.getLogger( HelloWorldProjectExtension.class );

  @Override
  public void clean( ImmoToolProject project ) throws ImmoToolException
  {
    LOGGER.debug( "clean project..." );
  }

  @Override
  public void close( ImmoToolProject project ) throws ImmoToolException
  {
    LOGGER.debug( "close project..." );

    // unregister the database extension, when the project is closed
    HelloWorldPlugin.setDbHelloWorldExtension( null );
  }

  @Override
  public void open( ImmoToolProject project ) throws ImmoToolException
  {
    LOGGER.debug( "open project..." );

    // register the database extension, when the project is loaded
    DbHelloWorldExtension dbHelloWorldExtension = HelloWorldPluginUtils.getDbHelloWorldExtension( project );
    if (dbHelloWorldExtension==null) throw new ImmoToolException( "Can't find a usable DbHelloWorldExtension!" );
    HelloWorldPlugin.setDbHelloWorldExtension( dbHelloWorldExtension );
  }

  @Override
  public void repair( ImmoToolProject project ) throws ImmoToolException
  {
    LOGGER.debug( "repair project..." );
  }
}