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

import com.openindex.openestate.impl.db.JdbcUtils;
import com.openindex.openestate.tool.ImmoToolEnvironment;
import com.openindex.openestate.tool.ImmoToolProject;
import com.openindex.openestate.tool.ImmoToolUtils;
import com.openindex.openestate.tool.extensions.FrameSidebarAdapter;
import com.openindex.openestate.tool.gui.AbstractRenderer;
import com.openindex.openestate.tool.gui.DefaultListCellRenderer;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.openestate.tool.helloworld.db.DbHelloWorldHandler;
import org.openestate.tool.helloworld.db.DbHelloWorldObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * Integrate HelloWorld addon into the applications sidebar.
 * <p>
 * This extensions integrates a separate sidebar for the addon into the
 * application.
 *
 * @author Andreas Rudolph
 */
@SuppressWarnings("WeakerAccess")
public class HelloWorldFrameSidebarExtension extends FrameSidebarAdapter {
    private final static Logger LOGGER = LoggerFactory.getLogger(HelloWorldFrameSidebarExtension.class);
    private final static I18n I18N = I18nFactory.getI18n(HelloWorldFrameSidebarExtension.class);
    private static AbstractButton currentSidebarButton = null;
    private static HelloWorldList currentSidebarList = null;

    private JPopupMenu createActionMenu(final DbHelloWorldObject object) {
        // create a popup menu for the Hello World sidebar
        final JPopupMenu popup = new JPopupMenu();

        // add action into the popup menu for sidebar refresh
        popup.add(new JMenuItem(new HelloWorldPlugin.SidebarRefreshAction()));

        // add action into the popup menu for a new object
        popup.add(new JMenuItem(new HelloWorldPlugin.ObjectFormAction()));

        // no object is selected in the sidebar
        //noinspection StatementWithEmptyBody
        if (object == null) {
        }

        // add further actions for the selected sidebar entry
        else {
            String title = StringUtils.abbreviate(StringUtils.trimToEmpty(object.name), 30);
            popup.add(ImmoToolUtils.createMenuSeparator(
                    "<html>" + StringEscapeUtils.escapeHtml4(title) + "</html>"));

            // add action into the popup menu for editing the selected object
            popup.add(new HelloWorldPlugin.ObjectFormAction(object.id));

            // add action into the popup menu for removing the selected object
            popup.add(new HelloWorldPlugin.ObjectRemoveAction(object.id));
        }

        // return the created popup menu
        return popup;
    }

    @Override
    public JComponent createComponent() {
        // create the component, that is shown in the sidebar
        final HelloWorldList list = new HelloWorldList();
        list.setModel(ObjectUtils.defaultIfNull(createListModel(), new DefaultListModel<>()));
        list.setCellRenderer(new HelloWorldListRenderer());

        // register keyboard events
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!list.isEnabled()) return;

                // ENTER was pressed
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    DbHelloWorldObject object = list.getSelectedValue();
                    if (object != null)
                        new HelloWorldPlugin.ObjectFormAction(object.id).actionPerformed(null);
                }
            }
        });

        // register mouse events
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!list.isEnabled()) return;

                // single click with the right mouse button
                if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
                    // fetch the clicked element
                    int index = list.locationToIndex(e.getPoint());
                    if (index >= 0) list.setSelectedIndex(index);

                    // show popup menu with further actions
                    DbHelloWorldObject object = list.getSelectedValue();
                    JPopupMenu popup = createActionMenu(object);
                    if (popup != null) popup.show(list, e.getPoint().x, e.getPoint().y);
                }

                // double click with the left mouse button
                else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    DbHelloWorldObject object = list.getSelectedValue();
                    if (object != null)
                        new HelloWorldPlugin.ObjectFormAction(object.id).actionPerformed(null);
                }
            }
        });

        return list;
    }

    public static DefaultListModel<DbHelloWorldObject> createListModel() {
        final ImmoToolProject project = ImmoToolProject.getAppInstance();
        final DbHelloWorldHandler dbHelloWorldHandler = HelloWorldPlugin.getDbHelloWorldExtension().getHelloWorldHandler();
        Connection c = null;
        try {
            c = project.getDbConnection();
            return createListModel(c, dbHelloWorldHandler);
        } catch (Exception ex) {
            LOGGER.error("Can't load objects into sidebar!");
            LOGGER.error("> " + ex.getLocalizedMessage(), ex);
            return null;
        } finally {
            JdbcUtils.closeQuietly(c);
        }
    }

    public static DefaultListModel<DbHelloWorldObject> createListModel(Connection c, DbHelloWorldHandler dbHelloWorldHandler) throws SQLException {
        DefaultListModel<DbHelloWorldObject> model = new DefaultListModel<>();
        for (DbHelloWorldObject object : dbHelloWorldHandler.getObjects(c)) {
            model.addElement(object);
        }
        return model;
    }

    @SuppressWarnings("unused")
    public static AbstractButton getCurrentSidebarButton() {
        return currentSidebarButton;
    }

    public static HelloWorldList getCurrentSidebarList() {
        return currentSidebarList;
    }

    @Override
    public Icon getIcon() {
        return HelloWorldPlugin.getResourceIcon("helloworld.png", 32);
    }

    @Override
    public JPopupMenu getMenu() {
        return createActionMenu(((currentSidebarList != null) ?
                currentSidebarList.getSelectedValue() : null));
    }

    @Override
    public String getTitle() {
        return I18N.tr("Hello World!");
    }

    @Override
    public String getTooltipText() {
        return I18N.tr("Show details about the Hello World addon.");
    }

    @Override
    public synchronized void register(JComponent component, AbstractButton button) {
        if (component instanceof HelloWorldList) {
            setCurrentSidebar((HelloWorldList) component, button);
        } else {
            LOGGER.warn("An invalid sidebar component was registered!");
            setCurrentSidebar(null, null);
        }
    }

    private static synchronized void setCurrentSidebar(HelloWorldList list, AbstractButton button) {
        currentSidebarList = list;
        currentSidebarButton = button;
    }

    @Override
    public synchronized void unregister() {
        setCurrentSidebar(null, null);
    }

    public static class HelloWorldList extends JList<DbHelloWorldObject> {
        @Override
        public String getToolTipText(MouseEvent event) {
            return super.getToolTipText(event);
        }

        @SuppressWarnings("unused")
        public void putObject(DbHelloWorldObject object) {
            if (object == null) throw new IllegalArgumentException("No object was provided!");
            if (object.id < 1) throw new IllegalArgumentException("An unsaved object was provided!");
            DefaultListModel<DbHelloWorldObject> model = (DefaultListModel<DbHelloWorldObject>) getModel();

            // update existing list entry with the same object id
            for (int i = 0; i < model.getSize(); i++) {
                DbHelloWorldObject obj = model.get(i);
                if (obj != null && obj.id == object.id) {
                    model.setElementAt(object, i);
                    return;
                }
            }

            // add a new object to the list
            model.addElement(object);
        }

        @SuppressWarnings("unused")
        public boolean removeObject(long objectId) {
            if (objectId < 1) throw new IllegalArgumentException("An invalid object id was provided!");
            DefaultListModel model = (DefaultListModel) getModel();
            for (int i = 0; i < model.getSize(); i++) {
                DbHelloWorldObject obj = (DbHelloWorldObject) model.get(i);
                if (obj != null && obj.id == objectId) {
                    model.removeElementAt(i);
                    model.trimToSize();
                    return true;
                }
            }
            return false;
        }
    }

    private static class HelloWorldRenderer extends AbstractRenderer<DbHelloWorldObject> {
        private final Icon icon = ImmoToolEnvironment.getResourceIcon("file_new.png", 16);

        @Override
        public void render(DbHelloWorldObject value, JLabel label) {
            if (value != null) {
                label.setIcon(HelloWorldRenderer.this.icon);
                label.setText(value.name);
            } else {
                label.setIcon(null);
                label.setText(StringUtils.EMPTY);
            }
        }
    }

    private static class HelloWorldListRenderer extends DefaultListCellRenderer<DbHelloWorldObject> {
        public HelloWorldListRenderer() {
            super(new HelloWorldRenderer());
        }

        @Override
        protected void init() {
            super.init();
            HelloWorldListRenderer.this.setBorder(
                    BorderFactory.createEmptyBorder(3, 3, 3, 3));
        }
    }
}