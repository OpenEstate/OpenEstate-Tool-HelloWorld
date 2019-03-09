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

import com.openindex.openestate.tool.extensions.FrameMenuAdapter;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * Integrate HelloWorld addon into the applications main menu.
 * <p>
 * This extensions integrates a separate menu for the addon into the
 * applications main menu.
 *
 * @author Andreas Rudolph <andy@openindex.de>
 */
public class HelloWorldFrameMenuExtension extends FrameMenuAdapter {
    @SuppressWarnings("unused")
    private final static Logger LOGGER = LoggerFactory.getLogger(HelloWorldFrameMenuExtension.class);
    @SuppressWarnings("unused")
    private final static I18n I18N = I18nFactory.getI18n(HelloWorldFrameMenuExtension.class);

    @Override
    public JMenu createMainMenu() {
        // create a main menu entry for the Hello World Plugin
        //noinspection ConstantConditions
        JMenu menu = new JMenu(HelloWorldPlugin.getInstance().getTitle());

        // add action into the main menu for sidebar view
        menu.add(new JMenuItem(new HelloWorldPlugin.SidebarSelectAction()));

        // add action into the main menu for a new object
        menu.add(new JMenuItem(new HelloWorldPlugin.ObjectFormAction()));

        // return the created main menu
        return menu;
    }
}