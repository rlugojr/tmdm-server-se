// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.webapp.welcomeportal.client.widget;

import java.util.HashMap;
import java.util.Map;

import org.talend.mdm.webapp.base.client.util.Cookies;
import org.talend.mdm.webapp.welcomeportal.client.MainFramePanel;
import org.talend.mdm.webapp.welcomeportal.client.WelcomePortal;
import org.talend.mdm.webapp.welcomeportal.client.WelcomePortalServiceAsync;
import org.talend.mdm.webapp.welcomeportal.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.welcomeportal.client.resources.icon.Icons;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.custom.Portal;
import com.extjs.gxt.ui.client.widget.custom.Portlet;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public abstract class BasePortlet extends Portlet {

    private static Map<String, String> titles;

    private static Map<String, AbstractImagePrototype> icons;

    static {
        titles = new HashMap<String, String>(8);
        titles.put(WelcomePortal.START, MessagesFactory.getMessages().start_title());
        titles.put(WelcomePortal.ALERT, MessagesFactory.getMessages().alerts_title());
        titles.put(WelcomePortal.TASKS, MessagesFactory.getMessages().tasks_title());
        titles.put(WelcomePortal.PROCESS, MessagesFactory.getMessages().process_title());
        titles.put(WelcomePortal.SEARCH, MessagesFactory.getMessages().search_title());
        titles.put(WelcomePortal.CHART_DATA, MessagesFactory.getMessages().chart_data_title());
        titles.put(WelcomePortal.CHART_JOURNAL, MessagesFactory.getMessages().chart_journal_title());
        titles.put(WelcomePortal.CHART_ROUTING_EVENT, MessagesFactory.getMessages().chart_routing_event_title());
        titles.put(WelcomePortal.CHART_MATCHING, MessagesFactory.getMessages().chart_mathcing_title());

        icons = new HashMap<String, AbstractImagePrototype>(5);
        icons.put(WelcomePortal.START, AbstractImagePrototype.create(Icons.INSTANCE.start()));
        icons.put(WelcomePortal.ALERT, AbstractImagePrototype.create(Icons.INSTANCE.alert()));
        icons.put(WelcomePortal.TASKS, AbstractImagePrototype.create(Icons.INSTANCE.task()));
        icons.put(WelcomePortal.PROCESS, AbstractImagePrototype.create(Icons.INSTANCE.transformer()));
        icons.put(WelcomePortal.SEARCH, AbstractImagePrototype.create(Icons.INSTANCE.find()));

    }

    protected WelcomePortalServiceAsync service = (WelcomePortalServiceAsync) Registry.get(WelcomePortal.WELCOMEPORTAL_SERVICE);

    protected Portal portal;

    protected String portletName;

    protected Label label;

    protected FieldSet set;

    protected Timer autoRefresher;

    protected AutoRefreshButton autoRefreshBtn;

    protected ToolButton refreshBtn;

    private boolean isAuto;

    private boolean startedAsOn;

    private int interval;

    private String cookieskey;

    public BasePortlet() {
        super();
        this.setLayout(new FitLayout());
        this.setStyleAttribute("padding", "5px"); //$NON-NLS-1$ //$NON-NLS-2$
        this.setCollapsible(true);
        this.setAnimCollapse(false);
        this.setAutoHeight(true);

        this.getHeader().addTool(new ToolButton("x-tool-close", new SelectionListener<IconButtonEvent>() { //$NON-NLS-1$

                    @Override
                    public void componentSelected(IconButtonEvent ce) {
                        BasePortlet.this.hide();
                        ((MainFramePanel) BasePortlet.this.portal).updateVisibilities();
                        removePortlet(portletName);
                    }

                }));

        // will be replaced only in StartPortlet, all other inherit it
        refreshBtn = new ToolButton("x-tool-refresh", new SelectionListener<IconButtonEvent>() { //$NON-NLS-1$

                    @Override
                    public void componentSelected(IconButtonEvent ce) {
                        refresh();
                    }
                });

        this.getHeader().addTool(refreshBtn);
    }

    public BasePortlet(String name, Portal portal) {

        this();

        this.portletName = name;

        this.portal = portal;

        this.setItemId(name + "Portlet"); //$NON-NLS-1$

        label = new Label();
        label.setItemId(name + "Label"); //$NON-NLS-1$
        label.setStyleAttribute("font-weight", "bold"); //$NON-NLS-1$ //$NON-NLS-2$
        label.setAutoHeight(true);
        this.add(label);

        set = new FieldSet();
        set.setItemId(name + "Set"); //$NON-NLS-1$
        set.setStyleAttribute("padding", "5px"); //$NON-NLS-1$ //$NON-NLS-2$
        set.setStyleAttribute("margin-left", "10px");//$NON-NLS-1$ //$NON-NLS-2$
        set.setStyleAttribute("margin-right", "10px");//$NON-NLS-1$ //$NON-NLS-2$
        set.setBorders(false);

        this.add(set);

        this.setHeading();
        this.setIcon();

        cookieskey = portletName + ".autoOnOff"; //$NON-NLS-1$

        if (Cookies.getValue(cookieskey) == null) {
            startedAsOn = ((MainFramePanel) portal).getStartedAsOn();
        } else {
            startedAsOn = (Boolean) Cookies.getValue(cookieskey);
        }
        interval = ((MainFramePanel) portal).getInterval();
    }

    protected void initAutoRefresher() {

        autoRefresher = new Timer() {

            @Override
            public void run() {
                if (isAuto) {
                    refresh();
                    schedule(interval);
                } else {
                    cancel();
                }
            }

        };

        // TODO: use gear image temporarily, need find a suitbale image icon for auto-refresh
        autoRefreshBtn = new AutoRefreshButton(startedAsOn, "x-tool-gear"); //$NON-NLS-1$
        autoRefreshBtn.setTitle(MessagesFactory.getMessages().autorefresh());

        autoRefreshBtn.addSelectionListener(new SelectionListener<IconButtonEvent>() {

            @Override
            public void componentSelected(IconButtonEvent ce) {
                autoRefreshBtn.flip();
                autoRefreshBtn.setTitle(autoRefreshBtn.isOn() ? MessagesFactory.getMessages().autorefresh_on() : MessagesFactory
                        .getMessages().autorefresh_off());
                BasePortlet.this.autoRefresh(autoRefreshBtn.isOn());
                Cookies.setValue(cookieskey, autoRefreshBtn.isOn());
                return;
            }

        });
        BasePortlet.this.getHeader().addTool(autoRefreshBtn);

    }

    protected void setHeading() {
        this.setHeading(titles.get(portletName));
    }

    protected void setIcon() {
        this.setIcon(icons.get(portletName));
    }

    public void autoRefresh(boolean auto) {
        if (isAuto != auto) {
            isAuto = auto;

            if (isAuto) {
                autoRefresher.run();
            }
        }
    }

    abstract public void refresh();

    protected class AutoRefreshButton extends ToolButton {

        private boolean on;

        public AutoRefreshButton(boolean on, String style) {
            super(style);
            this.on = on;
        }

        public AutoRefreshButton(String style, SelectionListener<IconButtonEvent> listener) {
            super(style, listener);
        }

        public AutoRefreshButton(boolean on, String style, SelectionListener<IconButtonEvent> listener) {
            this(style, listener);
            this.on = on;
        }

        public boolean isOn() {
            return this.on;
        }

        public void flip() {
            this.on = !on;
        }
    }

    public String getPortletName() {
        return this.portletName;
    }

    private native void removePortlet(String name)/*-{
		$wnd.amalto.core.unmarkPortlet(name);
    }-*/;
}
