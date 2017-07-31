/*
 * Copyright 2012-2017 OpenEstate.org.
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

import javax.swing.JMenuItem;
import org.openestate.tool.helloworld.HelloWorldObjectViewPanel;
import org.openestate.tool.helloworld.db.DbHelloWorldObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * A general extension, that adds custom features into
 * {@link HelloWorldObjectViewPanel}.
 * <p>
 * This class may be extended for custom tabs or actions in the
 * {@link HelloWorldObjectViewPanel}.
 *
 * @author Andreas Rudolph <andy@openindex.de>
 */
public abstract class ObjectViewAdapter implements ObjectViewExtension
{
  private final static Logger LOGGER = LoggerFactory.getLogger( ObjectViewAdapter.class );
  private final static I18n I18N = I18nFactory.getI18n( ObjectViewAdapter.class );

  @Override
  public JMenuItem[] createActionMenuItems( DbHelloWorldObject object )
  {
    return null;
  }

  @Override
  public HelloWorldObjectViewPanel.AbstractTab[] createTabs()
  {
    return null;
  }

  @Override
  public String[] getRequiredPluginIds()
  {
    return null;
  }
}