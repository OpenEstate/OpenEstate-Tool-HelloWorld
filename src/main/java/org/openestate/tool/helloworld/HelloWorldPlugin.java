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

import com.openindex.openestate.impl.db.JdbcUtils;
import com.openindex.openestate.tool.ImmoToolApp;
import com.openindex.openestate.tool.ImmoToolAppUtils;
import com.openindex.openestate.tool.ImmoToolEnvironment;
import com.openindex.openestate.tool.ImmoToolFrame;
import com.openindex.openestate.tool.ImmoToolProject;
import com.openindex.openestate.tool.ImmoToolProjectPlugin;
import com.openindex.openestate.tool.ImmoToolTask;
import com.openindex.openestate.tool.ImmoToolUtils;
import com.openindex.openestate.tool.db.AbstractDbDriver;
import com.openindex.openestate.tool.db.DbUpdateHandler;
import com.openindex.openestate.tool.db.DbUser;
import com.openindex.openestate.tool.extensions.DbExtension;
import com.openindex.openestate.tool.gui.AbstractI18nAction;
import com.openindex.openestate.tool.utils.ProjectPermission;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.apache.commons.lang3.StringUtils;
import org.openestate.tool.helloworld.db.DbHelloWorldHandler;
import org.openestate.tool.helloworld.db.DbHelloWorldObject;
import org.openestate.tool.helloworld.extensions.DbHelloWorldExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * Base class of the HelloWorld addon.
 *
 * @author Andreas Rudolph <andy@openindex.de>
 */
public class HelloWorldPlugin extends ImmoToolProjectPlugin
{
  private final static Logger LOGGER = LoggerFactory.getLogger( HelloWorldPlugin.class );
  private final static I18n I18N = I18nFactory.getI18n( HelloWorldPlugin.class );
  public final static String ID = "OpenEstate-Tool-HelloWorld";
  public final static String RESOURCE_PATH = "/org/openestate/tool/helloworld/resources/";
  private static DbHelloWorldExtension dbHelloWorldExtension = null;

  @Override
  protected void doStart() throws Exception
  {
    LOGGER.debug( "doStart" );
  }

  @Override
  protected void doStop() throws Exception
  {
    LOGGER.debug( "doStop" );
  }

  @Override
  public String getDescription()
  {
    return I18N.tr( "This plugin does not provide any features." );
  }

  public static DbHelloWorldExtension getDbHelloWorldExtension()
  {
    return dbHelloWorldExtension;
  }

  @Override
  public ImageIcon getIcon()
  {
    return new ImageIcon( HelloWorldPlugin.getResourceImage( "helloworld.png", 32 ) );
  }

  @Override
  public String getId()
  {
    return ID;
  }

  public static HelloWorldPlugin getInstance()
  {
    return ( HelloWorldPlugin ) ImmoToolEnvironment.getPlugin( ID );
  }

  @Override
  public String getLicense()
  {
    return "Apache Software License 2.0";
  }

  @Override
  public ProjectPermission[] getPermissions()
  {
    return HelloWorldPermission.values();
  }

  @Override
  public String[] getRequiredProcedures()
  {
    return (dbHelloWorldExtension!=null)?
      dbHelloWorldExtension.getRequiredProcedures(): null;
  }

  @Override
  public String[] getRequiredViews()
  {
    return (dbHelloWorldExtension!=null)?
      dbHelloWorldExtension.getRequiredViews(): null;
  }

  public static Icon getResourceIcon( String name, int size )
  {
    return ImmoToolUtils.getResourceIcon(
      RESOURCE_PATH, size, name, HelloWorldPlugin.class.getClassLoader() );
  }

  public static Image getResourceImage( String name, int size )
  {
    return ImmoToolUtils.getResourceImage(
      RESOURCE_PATH, size, name, HelloWorldPlugin.class.getClassLoader() );
  }

  public static URL getResourceImageURL( String name, int size )
  {
    return ImmoToolUtils.getResourceImageURL(
      RESOURCE_PATH, size, name, HelloWorldPlugin.class.getClassLoader() );
  }

  @Override
  public String getTitle()
  {
    return I18N.tr( "Hello World!" );
  }

  @Override
  public String getUninstallQuery( String driverName ) throws IOException
  {
    DbHelloWorldExtension pluginDbExtension = HelloWorldPluginUtils.getDbHelloWorldExtension( driverName );
    if (pluginDbExtension==null) throw new IOException( "Can't find a DbHelloWorldExtension for driver '" + driverName + "'!" );
    return pluginDbExtension.getUninstallQuery();
  }

  @Override
  public DbUpdateHandler getUpdateHandler( String driverName )
  {
    DbHelloWorldExtension pluginDbExtension = HelloWorldPluginUtils.getDbHelloWorldExtension( driverName );
    if (pluginDbExtension==null)
    {
      LOGGER.warn( "Can't find a DbHelloWorldExtension for driver '" + driverName + "'!" );
      return null;
    }
    return pluginDbExtension.getUpdateHandler();
  }

  @Override
  public void install( Connection c, DbExtension dbExtension, String driverName, boolean importDefaultData ) throws SQLException, IOException
  {
    DbHelloWorldExtension pluginDbExtension = HelloWorldPluginUtils.getDbHelloWorldExtension( driverName );
    if (pluginDbExtension==null) throw new SQLException( "Can't find a DbHelloWorldExtension for driver '" + driverName + "'!" );

    // Datenstrukturen des Plugins erzeugen
    pluginDbExtension.install( c );
    super.install( c, dbExtension, driverName, importDefaultData );

    // Standard-Daten importieren
    if (importDefaultData)
    {
      for (int i=1; i<4; i++)
      {
        DbHelloWorldObject obj = new DbHelloWorldObject();
        obj.name = "test_" + i;
        pluginDbExtension.getHelloWorldHandler().saveObject( c, obj );
      }
    }
  }

  @Override
  public boolean isAllowed( ImmoToolProject project )
  {
    try
    {
      return isUserAllowedTo( HelloWorldPermission.USE_PLUGIN, project );
    }
    catch (Exception ex)
    {
      LOGGER.warn( "Can't check addon for allowance!" );
      LOGGER.warn( "> " + ex.getLocalizedMessage(), ex );
      return false;
    }
  }

  @Override
  public boolean isUsable( ImmoToolProject project )
  {
    try
    {
      return super.isUsable( project ) && isAllowed( project );
    }
    catch (Exception ex)
    {
      LOGGER.warn( "Can't check addon for usability!" );
      LOGGER.warn( "> " + ex.getLocalizedMessage(), ex );
      return false;
    }
  }

  public static boolean isUserAllowedTo( HelloWorldPermission permission )
  {
    return isUserAllowedTo( permission, ImmoToolProject.getAppInstance() );
  }

  public static boolean isUserAllowedTo( HelloWorldPermission permission, ImmoToolProject project )
  {
    if (project==null) return false;
    DbUser user = project.getUser();
    return (user!=null)? user.isAllowedTo( ID, permission ): false;
  }

  public static void refreshSidebar()
  {
    final HelloWorldFrameSidebarExtension.HelloWorldList list = HelloWorldFrameSidebarExtension.getCurrentSidebarList();
    if (list!=null) ImmoToolUtils.executeTask( new SidebarRefreshTask( list ) );
  }

  @Override
  public void repair( Connection c, AbstractDbDriver driver ) throws SQLException
  {
    getDbHelloWorldExtension().repair( c, driver );
  }

  public static void setDbHelloWorldExtension( DbHelloWorldExtension dbHelloWorldExtension )
  {
    HelloWorldPlugin.dbHelloWorldExtension = dbHelloWorldExtension;
  }

  /**
   * Show tab for an object.
   */
  public final static class ObjectFormAction extends AbstractI18nAction
  {
    private final long objectId;

    public ObjectFormAction()
    {
      this( 0 );
    }

    public ObjectFormAction( long objectId )
    {
      super();
      this.objectId = objectId;
      if (objectId<1)
      {
        ObjectFormAction.this.setSmallIcon(
          ImmoToolEnvironment.getResourceIcon( "edit_add.png", 16 ) );
        ObjectFormAction.this.setEnabled(
          isUserAllowedTo( HelloWorldPermission.OBJECTS_EDIT ) );
      }
      else
      {
        ObjectFormAction.this.setSmallIcon(
          ImmoToolEnvironment.getResourceIcon( "edit.png", 16 ) );
        ObjectFormAction.this.setEnabled(
          isUserAllowedTo( HelloWorldPermission.OBJECTS ) );
      }
      ObjectFormAction.this.updateLocalization();
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {
      // create form for a new object
      if (ObjectFormAction.this.objectId<1)
      {
        HelloWorldObjectViewPanel panel = HelloWorldObjectViewPanel.createTab();
        ImmoToolAppUtils.showTab( panel );
        panel.loadInBackground( ImmoToolProject.getAppInstance().getDbDriver() );
      }

      // create form for an existing object
      else
      {
        ImmoToolUtils.executeTask( new HelloWorldObjectViewTask(
          ImmoToolProject.getAppInstance().getDbDriver(), ObjectFormAction.this.objectId ) );
      }
    }

    @Override
    protected final void updateLocalization()
    {
      if (ObjectFormAction.this.objectId<1)
      {
        ObjectFormAction.this.setName( StringUtils.capitalize(
          I18N.tr( "new object" ) ) );
        ObjectFormAction.this.setShortDescription(
          I18N.tr( "Add a new object." ) );
      }
      else
      {
        ObjectFormAction.this.setName( StringUtils.capitalize(
          I18N.tr( "show object" ) ) );
        ObjectFormAction.this.setShortDescription(
          I18N.tr( "Show details about the object." ) );
      }
    }
  }

  /**
   * Remove a certain object.
   */
  public final static class ObjectRemoveAction extends AbstractI18nAction
  {
    private final long objectId;

    public ObjectRemoveAction( long objectId )
    {
      super();
      this.objectId = objectId;
      ObjectRemoveAction.this.setSmallIcon(
        ImmoToolEnvironment.getResourceIcon( "edit_remove.png", 16 ) );
      ObjectRemoveAction.this.setEnabled(
        isUserAllowedTo( HelloWorldPermission.OBJECTS_REMOVE ) );
      ObjectRemoveAction.this.updateLocalization();
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {
      ImmoToolUtils.executeTask( new HelloWorldObjectRemoveTask(
        ImmoToolProject.getAppInstance().getDbDriver(), objectId ) );
    }

    @Override
    protected final void updateLocalization()
    {
      ObjectRemoveAction.this.setName( StringUtils.capitalize(
        I18N.tr( "remove object" ) ) );
      ObjectRemoveAction.this.setShortDescription(
        I18N.tr( "Remove object from the project." ) );
    }
  }

  /**
   * Refresh view in the sidebar.
   */
  public final static class SidebarRefreshAction extends AbstractI18nAction
  {
    public SidebarRefreshAction()
    {
      super();
      SidebarRefreshAction.this.setSmallIcon(
        ImmoToolEnvironment.getResourceIcon( "reload.png", 16 ) );
      SidebarRefreshAction.this.setEnabled(
        isUserAllowedTo( HelloWorldPermission.USE_PLUGIN ) );
      SidebarRefreshAction.this.updateLocalization();
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {
      HelloWorldPlugin.refreshSidebar();
    }

    @Override
    protected final void updateLocalization()
    {
      SidebarRefreshAction.this.setName(
        StringUtils.capitalize( I18N.tr( "refresh" ) ) );
      SidebarRefreshAction.this.setShortDescription(
        I18N.tr( "Refresh view in the sidebar." ) );
    }
  }

  private final static class SidebarRefreshTask extends ImmoToolTask<DefaultListModel, Void>
  {
    private final HelloWorldFrameSidebarExtension.HelloWorldList list;

    public SidebarRefreshTask( HelloWorldFrameSidebarExtension.HelloWorldList list )
    {
      super();
      this.list = list;
    }

    @Override
    protected DefaultListModel doInBackground() throws Exception
    {
      final ImmoToolProject project = ImmoToolProject.getAppInstance();
      final DbHelloWorldHandler dbHandler = HelloWorldPlugin.getDbHelloWorldExtension().getHelloWorldHandler();
      Connection c = null;
      try
      {
        c = project.getDbConnection();
        return HelloWorldFrameSidebarExtension.createListModel( c, dbHandler );
      }
      finally
      {
        JdbcUtils.closeQuietly( c );
      }
    }

    @Override
    protected void failed( Throwable t )
    {
      super.failed( t );
      ImmoToolUtils.showMessageErrorDialog(
        "Can't refresh sidebar!", t, ImmoToolEnvironment.getFrame() );
    }

    @Override
    protected void succeeded( DefaultListModel result )
    {
      super.succeeded( result );
      if (result==null) return;

      // remember ID of the currently selected object
      DbHelloWorldObject selectedObject = (DbHelloWorldObject) list.getSelectedValue();
      long id = (selectedObject!=null)? selectedObject.id: 0;

      // load model into list
      SidebarRefreshTask.this.list.setModel( result );

      // select the previously selected object
      for (int i=0; i<result.getSize(); i++)
      {
        DbHelloWorldObject object = (DbHelloWorldObject) list.getModel().getElementAt( i );
        if (object.id == id)
        {
          SidebarRefreshTask.this.list.setSelectedIndex( i );
          break;
        }
      }
    }
  }

  /**
   * Show view in sidebar.
   */
  public final static class SidebarSelectAction extends AbstractI18nAction
  {
    public SidebarSelectAction()
    {
      super();
      SidebarSelectAction.this.setSmallIcon(
        HelloWorldPlugin.getResourceIcon( "helloworld.png", 16 ) );
      SidebarSelectAction.this.setEnabled(
        isUserAllowedTo( HelloWorldPermission.USE_PLUGIN ) );
      SidebarSelectAction.this.updateLocalization();
    }

    @Override
    public void actionPerformed( ActionEvent e )
    {
      final ImmoToolFrame view = ImmoToolApp.getInstance().getFrame();
      if (view!=null) view.selectSidebar( HelloWorldFrameSidebarExtension.class );
    }

    @Override
    protected final void updateLocalization()
    {
      SidebarSelectAction.this.setName(
        StringUtils.capitalize( I18N.tr( "show Hello World!" ) ) );
      SidebarSelectAction.this.setShortDescription(
        I18N.tr( "Show view for Hello World! in the sidebar." ) );
    }
  }
}