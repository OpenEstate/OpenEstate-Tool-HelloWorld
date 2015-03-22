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

import com.openindex.openestate.tool.extensions.FrameMenuAdapter;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * HelloWorldFrameMenuExtension.
 *
 * @author Andreas Rudolph <andy@openindex.de>
 */
public class HelloWorldFrameMenuExtension extends FrameMenuAdapter
{
  @Override
  public JMenu createMainMenu()
  {
    // create a main menu entry for the Hello World Plugin
    JMenu menu = new JMenu( HelloWorldPlugin.getInstance().getTitle() );

    // add action into the main menu for sidebar view
    menu.add( new JMenuItem( new HelloWorldPlugin.SidebarSelectAction() ) );

    // add action into the main menu for a new object
    menu.add( new JMenuItem( new HelloWorldPlugin.ObjectFormAction() ) );

    // return the created main menu
    return menu;
  }
}