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

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Paddings;
import com.openindex.openestate.impl.db.JdbcUtils;
import com.openindex.openestate.tool.ImmoToolAppUtils;
import com.openindex.openestate.tool.ImmoToolEnvironment;
import com.openindex.openestate.tool.ImmoToolPermissionPanel;
import com.openindex.openestate.tool.ImmoToolProject;
import com.openindex.openestate.tool.ImmoToolTask;
import com.openindex.openestate.tool.ImmoToolUtils;
import com.openindex.openestate.tool.db.DbGroup;
import com.openindex.openestate.tool.db.DbUser;
import com.openindex.openestate.tool.extensions.DbExtension;
import com.openindex.openestate.tool.gui.AbstractI18nAction;
import com.openindex.openestate.tool.gui.AbstractMainView;
import com.openindex.openestate.tool.gui.AbstractMainViewTab;
import com.openindex.openestate.tool.utils.StatusNotification;
import com.openindex.openestate.tool.utils.forms.ValidationHandler;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ContainerEvent;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jdesktop.swingx.JXTitledSeparator;
import org.openestate.tool.helloworld.db.DbHelloWorldHandler;
import org.openestate.tool.helloworld.db.DbHelloWorldObject;
import org.openestate.tool.helloworld.extensions.ObjectViewExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * Form to create or edit an object of the HelloWorld addon.
 *
 * @author Andreas Rudolph <andy@openindex.de>
 */
public class HelloWorldObjectViewPanel extends AbstractMainView
{
  private final static Logger LOGGER = LoggerFactory.getLogger( HelloWorldObjectViewPanel.class );
  private final static I18n I18N = I18nFactory.getI18n( HelloWorldObjectViewPanel.class );
  private DbHelloWorldObject currentObject = null;
  private long nextObjectId = 0;
  private long prevObjectId = 0;
  private boolean mayEditObject = false;
  private boolean mayRemoveObject = false;
  private List<ObjectViewExtension> addons;
  private FormTab formTab;
  private PermissionsTab permissionTab;
  private AbstractI18nAction submitAction;
  private AbstractI18nAction helpAction;
  private AbstractI18nAction closeAction;
  private AbstractI18nAction showActionsAction;
  private AbstractI18nAction viewNextAction;
  private AbstractI18nAction viewPrevAction;

  private HelloWorldObjectViewPanel()
  {
    super();
  }

  @Override
  protected void buildHeaderComponentButtons( ButtonBarBuilder builder )
  {
    submitAction = createDefaultSubmitAction(
      HelloWorldPlugin.isUserAllowedTo( HelloWorldPermission.OBJECTS_EDIT ) );
    helpAction = createDefaultHelpAction();
    closeAction = createDefaultCloseAction();
    viewNextAction = createDefaultViewNextAction();
    viewPrevAction = createDefaultViewPreviousAction();
    showActionsAction = createDefaultShowActionsAction(
      HelloWorldPlugin.getResourceIcon( "helloworld.png", 16 ) );

    builder.addRelatedGap();
    builder.addFixed( new JButton( viewPrevAction ) );
    builder.addRelatedGap();
    builder.addFixed( new JButton( viewNextAction ) );
    builder.addRelatedGap();
    builder.addFixed( new JButton( submitAction ) );
    builder.addRelatedGap();
    builder.addFixed( new JButton( showActionsAction ) );
    builder.addRelatedGap();
    builder.addFixed( new JButton( helpAction ) );
    builder.addRelatedGap();
    builder.addFixed( new JButton( closeAction ) );
  }

  @Override
  protected void buildMainComponentTabs( JTabbedPane tabbedPane )
  {
    final ImmoToolProject project = ImmoToolProject.getAppInstance();

    // create form tab
    formTab = (FormTab) ImmoToolUtils.addTab(
      new FormTab(), tabbedPane );

    // create addon tabs
    try
    {
      for (ObjectViewExtension addon : addons)
      {
        AbstractTab[] tabs = addon.createTabs();
        if (ArrayUtils.isEmpty( tabs )) continue;
        for (AbstractTab tab : tabs)
        {
          ImmoToolUtils.addTab( tab, tabbedPane );
        }
      }
    }
    catch (Exception ex)
    {
      LOGGER.warn( "Can't load addon tabs!" );
      LOGGER.warn( "> " + ex.getLocalizedMessage(), ex );
    }

    // create permission tab only for multi user projects
    if (project!=null && project.isRemoteProject())
    {
      permissionTab = (PermissionsTab) ImmoToolUtils.addTab(
        new PermissionsTab(), tabbedPane );
    }
  }

  public static HelloWorldObjectViewPanel createTab()
  {
    return createTab( null );
  }

  public static HelloWorldObjectViewPanel createTab( DbHelloWorldObject object )
  {
    HelloWorldObjectViewPanel tab = new HelloWorldObjectViewPanel();
    tab.setObject( object );
    return tab;
  }

  @Override
  public void doClose( boolean force )
  {
    super.doClose( force );
  }

  @Override
  protected void doCopy()
  {
    if (!mayEditObject)
    {
      ImmoToolUtils.showMessageWarningDialog( I18N.tr( "Access denied!" ), this );
      return;
    }

    // validate tabs
    if (!validateTabs()) return;

    // start task, that copies the current object
    setButtonsEnabled( false );
    ImmoToolUtils.executeTask( new SubmitTask( getTabs(), true ) );
  }

  @Override
  protected void doHelp()
  {
    ImmoToolUtils.showMessageErrorDialog(
      I18N.tr( "Help is not implemented yet!" ), ImmoToolEnvironment.getFrame() );
  }

  @Override
  protected void doLoadInBackground( Connection c ) throws Exception
  {
    final DbHelloWorldHandler dbHandler = HelloWorldPlugin.getDbHelloWorldExtension().getHelloWorldHandler();

    // load ID of the next / previous object
    nextObjectId = 0;
    prevObjectId = 0;
    try
    {
      long[] ids = dbHandler.getObjectIds( c );
      int pos = ArrayUtils.indexOf( ids, currentObject.id );
      if (pos>0) prevObjectId = ids[pos-1];
      if ((pos+1)<ids.length) nextObjectId = ids[pos+1];
    }
    catch (Exception ex)
    {
      LOGGER.warn( "Can't load previous & next dataset!" );
      LOGGER.warn( "> " + ex.getLocalizedMessage(), ex );
    }
  }

  @Override
  public void doRefresh()
  {
    if (currentObject!=null && currentObject.id>0)
    {
      setObject( currentObject );
      loadInBackground( ImmoToolProject.getAppInstance().getDbDriver() );
    }
  }

  @Override
  protected void doRemove()
  {
    if (currentObject==null || currentObject.id<1)
    {
      return;
    }
    if (!mayRemoveObject)
    {
      ImmoToolUtils.showMessageWarningDialog( I18N.tr( "Access denied!" ), this );
      return;
    }
    boolean canDelete = ImmoToolUtils.showQuestionDialog(
      I18N.tr( "Do you really want to remove object {0}?", "#" + currentObject.id ), this );
    if (!canDelete) return;

    // start task, that removes the current object
    setButtonsEnabled( false );
    ImmoToolUtils.executeTask( new RemoveTask( currentObject.id ) );
  }

  @Override
  protected void doShowActions( Component component, int x, int y )
  {
    JPopupMenu popup = new JPopupMenu();
    popup.add( createDefaultCopyAction(
      currentObject!=null && currentObject.id>0 &&
      HelloWorldPlugin.isUserAllowedTo( HelloWorldPermission.OBJECTS_EDIT ) ) );
    popup.add( createDefaultRemoveAction(
      currentObject!=null && currentObject.id>0 && mayRemoveObject ) );

    boolean addonItemAdded = false;
    for (ObjectViewExtension addon : addons)
    {
      JMenuItem[] items = addon.createActionMenuItems( currentObject );
      if (ArrayUtils.isEmpty( items )) continue;
      for (JMenuItem item : items)
      {
        if (!addonItemAdded)
        {
          addonItemAdded = true;
          popup.addSeparator();
        }
        popup.add( item );
      }
    }

    popup.show( component, x, y );
  }

  @Override
  protected void doSubmit()
  {
    if (!mayEditObject)
    {
      ImmoToolUtils.showMessageWarningDialog( I18N.tr( "Access denied!" ), this );
      return;
    }

    // validate tabs
    if (!validateTabs()) return;

    // start task, that saves the current object
    setButtonsEnabled( false );
    ImmoToolUtils.executeTask( new SubmitTask( getTabs(), false ) );
  }

  @Override
  protected void doViewNext()
  {
    // start task, that loads the next object into the current form
    if (nextObjectId>0)
    {
      setButtonsEnabled( false );
      ImmoToolUtils.executeTask( new HelloWorldObjectViewTask(
        ImmoToolProject.getAppInstance().getDbDriver(), nextObjectId, this ) );
    }
  }

  @Override
  protected void doViewPrevious()
  {
    // start task, that loads the previous object into the current form
    if (prevObjectId>0)
    {
      setButtonsEnabled( false );
      ImmoToolUtils.executeTask( new HelloWorldObjectViewTask(
        ImmoToolProject.getAppInstance().getDbDriver(), prevObjectId, this ) );
    }
  }

  public long getCurrentObjectId()
  {
    return (currentObject!=null)? currentObject.id: 0;
  }

  @Override
  protected Icon getHeaderIcon()
  {
    return HelloWorldPlugin.getResourceIcon( "helloworld.png", 32 );
  }

  @Override
  protected String getHeaderTitle()
  {
    if (currentObject==null || currentObject.id<1)
      return StringUtils.capitalize( I18N.tr( "new object" ) );
    else
      return StringUtils.capitalize( I18N.tr( "object" ) ) + " #" + currentObject.id;
  }

  @Override
  public String getTabTitle()
  {
    if (currentObject==null || currentObject.id<1)
      return StringUtils.capitalize( I18N.tr( "new object" ) );
    else
      return StringUtils.capitalize( I18N.tr( "object" ) ) + " #" + currentObject.id;
  }

  @Override
  public String getTabToolTipText()
  {
    if (currentObject==null || currentObject.id<1)
      return I18N.tr( "Create a new object." );
    else
      return I18N.tr( "Edit object {0}.", "#" + currentObject.id );
  }

  @Override
  protected void init()
  {
    super.init();

    // load addons
    addons = new ArrayList<ObjectViewExtension>();
    try
    {
      for (ObjectViewExtension addon : HelloWorldPluginUtils.getObjectViewExtensions())
      {
        addons.add( addon );
      }
    }
    catch (Exception ex)
    {
      LOGGER.warn( "Can't load addons!" );
      LOGGER.warn( "> " + ex.getLocalizedMessage(), ex );
    }
  }

  @Override
  protected boolean isLoadedInBackground()
  {
    // we only must load the form in background,
    // if the current object is not new
    return currentObject!=null && currentObject.id>0;
  }

  @Override
  protected void setButtonsEnabled( boolean enabled )
  {
    super.setButtonsEnabled( enabled );
    submitAction.setEnabled( enabled && mayEditObject );
    closeAction.setEnabled( enabled );
    helpAction.setEnabled( enabled );
    showActionsAction.setEnabled( enabled );
    viewNextAction.setEnabled( enabled && nextObjectId>0 );
    viewPrevAction.setEnabled( enabled && prevObjectId>0 );
  }

  public void setObject( DbHelloWorldObject object )
  {
    currentObject = (object!=null)? object: new DbHelloWorldObject();
    prevObjectId = 0;
    nextObjectId = 0;

    // load permissions
    mayEditObject = HelloWorldPlugin.isUserAllowedTo( HelloWorldPermission.OBJECTS_EDIT );
    mayRemoveObject = HelloWorldPlugin.isUserAllowedTo( HelloWorldPermission.OBJECTS_REMOVE );
    if (currentObject.permission!=null)
    {
      DbUser currentUser = ImmoToolProject.getAppInstance().getUser();
      mayEditObject = mayEditObject && currentObject.permission.canWrite(
        currentUser, currentObject.ownerUserId, currentObject.ownerGroupId );
      mayRemoveObject = mayRemoveObject && currentObject.permission.canDelete(
        currentUser, currentObject.ownerUserId, currentObject.ownerGroupId );
    }

    // load tabs
    for (AbstractMainViewTab tab : getTabs())
    {
      int index = indexOf( tab );
      if (index<0) continue;
      getTabbedPane().setEnabledAt( index, false );
      try
      {
        ((AbstractTab) tab).load( currentObject );
      }
      catch (Exception ex)
      {
        LOGGER.error( "Can't load tab '" + tab.getTabTitle() + "'!" );
        LOGGER.error( "> " + ex.getLocalizedMessage(), ex );
      }
    }

    // reload localization
    updateLocalization();
  }

  @Override
  protected void tabComponentAdded( ContainerEvent e )
  {
    super.tabComponentAdded( e );
    Component c = e.getChild();
    if (c instanceof AbstractTab)
    {
      ((AbstractTab) c).setViewPanel( this );
    }
  }

  @Override
  protected void tabComponentRemoved( ContainerEvent e )
  {
    super.tabComponentRemoved( e );
    Component c = e.getChild();
    if (c instanceof AbstractTab)
    {
      ((AbstractTab) c).setViewPanel( null );
    }
  }

  public static abstract class AbstractTab extends AbstractMainViewTab
  {
    private WeakReference<HelloWorldObjectViewPanel> viewPanel = null;
    private final List<String> saveWarnings = new ArrayList<String>();

    protected final void addSaveWarning( String msg )
    {
      saveWarnings.add( msg );
    }

    public HelloWorldObjectViewPanel getViewPanel()
    {
      return (AbstractTab.this.viewPanel!=null)?
        AbstractTab.this.viewPanel.get(): null;
    }

    public abstract void load( DbHelloWorldObject object ) throws Exception;

    public abstract void save( DbHelloWorldObject object ) throws Exception;

    public void saveFinished( Connection c, ImmoToolProject project, DbHelloWorldObject object ) throws SQLException, IOException
    {
    }

    public void setViewPanel( HelloWorldObjectViewPanel viewPanel )
    {
      AbstractTab.this.viewPanel = (viewPanel!=null)?
        new WeakReference<HelloWorldObjectViewPanel>( viewPanel ): null;
    }
  }

  private final static class FormTab extends AbstractTab
  {
    private ValidationHandler validationHandler;
    private JXTitledSeparator formTitle;
    private JLabel nameLabel;
    private JTextField nameField;
    private JXTitledSeparator notesTitle;
    private RTextArea notesField;

    private FormTab()
    {
      super();
      FormTab.this.build();
      FormTab.this.updateLocalization();
    }

    private void build()
    {
      FormTab.this.validationHandler = ImmoToolUtils.createValidationHandler();
      FormTab.this.formTitle = ImmoToolUtils.createHead2Separator( StringUtils.EMPTY );
      FormTab.this.notesTitle = ImmoToolUtils.createHead2Separator( StringUtils.EMPTY );

      // create name field
      FormTab.this.nameLabel = new JLabel();
      FormTab.this.nameField = new JTextField();
      FormTab.this.validationHandler.putRule(
        nameField, ValidationHandler.NOT_BLANK );

      // create notes field
      FormTab.this.notesField = new RTextArea();
      RTextScrollPane notesScroller = new RTextScrollPane( FormTab.this.notesField );

      // create form
      DefaultFormBuilder builder = ImmoToolUtils.createFormBuilder(
        "right:pref, 3dlu, pref:grow" );
      builder.append( FormTab.this.formTitle, 3 );
      builder.nextLine();
      builder.append( FormTab.this.nameLabel, FormTab.this.nameField );
      builder.nextLine();
      builder.append( FormTab.this.notesTitle, 3 );
      builder.nextLine();

      // build panel
      FormTab.this.setLayout( new BorderLayout( 10, 10 ) );
      FormTab.this.setBorder( Paddings.DIALOG );
      add( builder.getPanel(), BorderLayout.NORTH );
      add( notesScroller, BorderLayout.CENTER );
    }

    @Override
    public void doLoadInBackground( Connection c ) throws Exception
    {
      // Maybe we can load some form components within a background task. This
      // function is only called, when "isLoadedInBackground()" returns true.
    }

    @Override
    public String getTabTitle()
    {
      return StringUtils.capitalize( I18N.tr( "object" ) );
    }

    @Override
    public boolean isLoadedInBackground()
    {
      return false;
    }

    @Override
    public boolean isTabValid()
    {
      FormTab.this.validationHandler.validate();
      return FormTab.this.validationHandler.isValid();
    }

    @Override
    public void load( DbHelloWorldObject object ) throws Exception
    {
      FormTab.this.nameField.setText( (object!=null)?
        StringUtils.trimToEmpty( object.name ): StringUtils.EMPTY );
      FormTab.this.notesField.setText( (object!=null)?
        StringUtils.trimToEmpty( object.notes ): StringUtils.EMPTY );
      FormTab.this.notesField.setCaretPosition( 0 );

      // update validation
      if (object!=null && object.id>0)
        FormTab.this.validationHandler.validate();
      else
        FormTab.this.validationHandler.init();

      // update translations
      FormTab.this.updateLocalization();
    }

    @Override
    public void save( DbHelloWorldObject object ) throws Exception
    {
      object.name = FormTab.this.nameField.getText().trim();
      object.notes = FormTab.this.notesField.getText().trim();
    }

    @Override
    public void saveFinished( Connection c, ImmoToolProject project, DbHelloWorldObject object ) throws SQLException, IOException
    {
      // Maybe we can do some more operations after object was saved - e.g. set
      // some relations to the saved object.
    }

    @Override
    protected void updateLocalization()
    {
      super.updateLocalization();
      FormTab.this.formTitle.setTitle( StringUtils.capitalize(
        I18N.tr( "details about the object" ) ) );

      FormTab.this.nameLabel.setText( StringUtils.capitalize(
        I18N.tr( "name" ) ) + ":" );
      FormTab.this.nameField.setToolTipText(
        I18N.tr( "Enter the name of the object." ) );

      FormTab.this.notesTitle.setTitle( StringUtils.capitalize(
        I18N.tr( "notes about the object" ) ) );
      FormTab.this.notesField.setToolTipText(
        I18N.tr( "Enter some notes about the object." ) );
    }
  }

  private final class PermissionsTab extends AbstractTab
  {
    private ImmoToolPermissionPanel form;
    private boolean loaded = false;

    public PermissionsTab()
    {
      super();
      build();
    }

    private void build()
    {
      // create form
      PermissionsTab.this.form = new ImmoToolPermissionPanel();
      PermissionsTab.this.form.setBorder( Paddings.DIALOG );
      JScrollPane scroller = new JScrollPane( PermissionsTab.this.form );
      scroller.setBorder( Paddings.EMPTY );

      // attach form to the panel
      PermissionsTab.this.setLayout( new BorderLayout( 5, 5 ) );
      PermissionsTab.this.setBorder( Paddings.EMPTY );
      PermissionsTab.this.add( scroller, BorderLayout.CENTER );
    }

    @Override
    public void doLoadInBackground( Connection c ) throws Exception
    {
      PermissionsTab.this.loaded = false;
      try
      {
        final ImmoToolProject project = ImmoToolProject.getAppInstance();
        final DbExtension dbExtension = project.getDbExtension();
        final DbUser currentUser = project.getUser();

        if (HelloWorldObjectViewPanel.this.currentObject!=null && HelloWorldObjectViewPanel.this.currentObject.id>0)
        {
          PermissionsTab.this.form.setPermission(
            HelloWorldObjectViewPanel.this.currentObject.ownerUserId,
            HelloWorldObjectViewPanel.this.currentObject.ownerGroupId,
            HelloWorldObjectViewPanel.this.currentObject.permission );
        }
        else
        {
          PermissionsTab.this.form.setPermission(
            currentUser.id, 0, null );
        }

        // load permissions
        PermissionsTab.this.form.load(
          c, dbExtension.getUserHandler(), currentUser, dbExtension.getBaseGroupName() );

        // permissions were successfully loaded
        PermissionsTab.this.loaded = true;
      }
      catch (Exception ex)
      {
        LOGGER.warn( "Can't load permissions!" );
        LOGGER.warn( "> " + ex.getLocalizedMessage(), ex );
        PermissionsTab.this.loaded = false;
      }
    }

    @Override
    public String getTabTitle()
    {
      return PermissionsTab.this.form.getTitle();
    }

    @Override
    public boolean isLoadedInBackground()
    {
      return true;
    }

    @Override
    public boolean isTabEnabled()
    {
      return PermissionsTab.this.loaded;
    }

    @Override
    public void load( DbHelloWorldObject object )
    {
      PermissionsTab.this.loaded = false;
      PermissionsTab.this.form.setPermission( 0, 0, null );
    }

    @Override
    public void save( DbHelloWorldObject object )
    {
      object.permission = PermissionsTab.this.form.getSelectedPermission();
      DbUser user = PermissionsTab.this.form.getSelectedOwner();
      if (user!=null) object.ownerUserId = user.id;
      DbGroup group = PermissionsTab.this.form.getSelectedGroup();
      if (group!=null) object.ownerGroupId = group.id;
    }
  }

  private class RemoveTask extends HelloWorldObjectRemoveTask
  {
    public RemoveTask( long objectId )
    {
      super( ImmoToolProject.getAppInstance().getDbDriver(), objectId );
    }

    @Override
    protected void finished()
    {
      super.finished();
      HelloWorldObjectViewPanel.this.setButtonsEnabled( true );
    }

    @Override
    protected void succeeded( Boolean result )
    {
      super.succeeded( result );
      if (!Boolean.TRUE.equals( result )) return;
      ImmoToolAppUtils.removeTab( HelloWorldObjectViewPanel.this, true );
    }
  }

  private class SubmitTask extends ImmoToolTask<DbHelloWorldObject, Void>
  {
    private final AbstractMainViewTab[] tabs;
    private final boolean saveAsCopy;
    private final List<String> warnings = new ArrayList<String>();

    public SubmitTask( AbstractMainViewTab[] tabs, boolean saveAsCopy )
    {
      super();
      this.tabs = tabs;
      this.saveAsCopy = saveAsCopy;
    }

    @Override
    protected DbHelloWorldObject doInBackground() throws Exception
    {
      final ImmoToolProject project = ImmoToolProject.getAppInstance();
      final DbHelloWorldHandler dbHandler = HelloWorldPlugin.getDbHelloWorldExtension().getHelloWorldHandler();
      SubmitTask.this.warnings.clear();
      Connection c = null;
      try
      {
        c = project.getDbConnection();

        // load current object from database
        DbHelloWorldObject object = null;
        long currentObjectId = HelloWorldObjectViewPanel.this.getCurrentObjectId();
        if (currentObjectId<1 || SubmitTask.this.saveAsCopy)
        {
          object = new DbHelloWorldObject();
        }
        else
        {
          object = dbHandler.getObject( c, currentObjectId );
          if (object==null) throw new Exception( "Can't load object #" + currentObjectId + "!" );
        }

        // add changes into the object
        for (AbstractMainViewTab tab : SubmitTask.this.tabs)
        {
          if (tab instanceof AbstractTab)
          {
            ((AbstractTab) tab).saveWarnings.clear();
            ((AbstractTab) tab).save( object );
          }
        }

        // save the object
        dbHandler.saveObject( c, object );

        // do some further actions, after the object was initially saved
        for (AbstractMainViewTab tab : SubmitTask.this.tabs)
        {
          if (tab instanceof AbstractTab)
          {
            AbstractTab form = (AbstractTab) tab;
            form.saveFinished( c, project, object );
            if (!form.saveWarnings.isEmpty())
            {
              SubmitTask.this.warnings.addAll( form.saveWarnings );
            }
          }
        }

        return object;
      }
      finally
      {
        JdbcUtils.closeQuietly( c );
      }
    }

    @Override
    protected void failed( Throwable ex )
    {
      super.failed( ex );
      ImmoToolUtils.showMessageErrorDialog(
        I18N.tr( "Can't save object!" ), ex, ImmoToolEnvironment.getFrame() );
    }

    @Override
    protected void finished()
    {
      super.finished();
      HelloWorldObjectViewPanel.this.setButtonsEnabled( true );
    }

    @Override
    protected void succeeded( DbHelloWorldObject object )
    {
      super.succeeded( object );
      if (object==null) return;

      // show warnings as balloon tip, that occured during the process
      if (!SubmitTask.this.warnings.isEmpty())
      {
        for (String msg : warnings)
        {
          ImmoToolEnvironment.showStatusNotification(
            new StatusNotification.Warning( msg ) );
        }
      }

      // load saved object into the current form
      HelloWorldObjectViewPanel.this.setObject( object );
      HelloWorldObjectViewPanel.this.loadInBackground(
        ImmoToolProject.getAppInstance().getDbDriver() );

      // refresh sidebar after the object was saved
      HelloWorldPlugin.refreshSidebar();
    }
  }
}