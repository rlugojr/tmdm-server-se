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
package org.talend.mdm.webapp.welcomeportal.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.util.Cookies;
import org.talend.mdm.webapp.base.client.util.UrlUtil;
import org.talend.mdm.webapp.welcomeportal.client.widget.AlertPortlet;
import org.talend.mdm.webapp.welcomeportal.client.widget.BasePortlet;
import org.talend.mdm.webapp.welcomeportal.client.widget.ChartPortlet;
import org.talend.mdm.webapp.welcomeportal.client.widget.DataChart;
import org.talend.mdm.webapp.welcomeportal.client.widget.JournalChart;
import org.talend.mdm.webapp.welcomeportal.client.widget.MatchingChart;
import org.talend.mdm.webapp.welcomeportal.client.widget.ProcessPortlet;
import org.talend.mdm.webapp.welcomeportal.client.widget.RoutingChart;
import org.talend.mdm.webapp.welcomeportal.client.widget.SearchPortlet;
import org.talend.mdm.webapp.welcomeportal.client.widget.StartPortlet;
import org.talend.mdm.webapp.welcomeportal.client.widget.TaskPortlet;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.ContainerEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.PortalEvent;
import com.extjs.gxt.ui.client.widget.custom.Portal;
import com.extjs.gxt.ui.client.widget.custom.Portlet;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class MainFramePanel extends Portal {

    private static final int DEFAULT_REFRESH_INTERVAL = 10000;

    private static final boolean DEFAULT_REFRESH_STARTASON = false;

    private static final List<String> DEFAULT_ORDERING_INDEX = Arrays.asList(WelcomePortal.START, WelcomePortal.PROCESS,
            WelcomePortal.ALERT, WelcomePortal.SEARCH, WelcomePortal.TASKS, WelcomePortal.CHART_DATA,
            WelcomePortal.CHART_ROUTING_EVENT, WelcomePortal.CHART_JOURNAL, WelcomePortal.CHART_MATCHING);

    private static final String NAME_START = "start", NAME_PROCESS = "process", NAME_ALERT = "alert", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            NAME_SEARCH = "search", NAME_TASKS = "tasks", NAME_CHART_DATA = "chart_data", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            NAME_CHART_ROUTING_EVENT = "chart_routing_event", NAME_CHART_JOURNAL = "chart_journal", NAME_CHART_MATCHING = "chart_matching"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private static final Set<String> DEFAULT_CHART_NAMES = new HashSet<String>(Arrays.asList(NAME_CHART_DATA,
            NAME_CHART_ROUTING_EVENT, NAME_CHART_JOURNAL, NAME_CHART_MATCHING));

    private static final String COOKIES_PORTLET_LOCATIONS = "portletLocations"; //$NON-NLS-1$

    private static final String COOKIES_PORTLET_VISIBILITIES = "portletVisibilities"; //$NON-NLS-1$

    private static final String COOKIES_PORTLET_COLUMN = "columnNum"; //$NON-NLS-1$

    private static final String COOKIES_CHARTS_ENABLED = "chartsOn"; //$NON-NLS-1$

    private static final String COOKIES_CHARTS = "allCharts"; //$NON-NLS-1$

    private static final String USING_DEFAULT_COLUMN_NUM = "defaultColNum"; //$NON-NLS-1$
    
    private static final int DEFAULT_COLUMN_NUM = 3;
    
    private static final int ALTERNATIVE_COLUMN_NUM = 2;

    private static final String CHARTS_ENABLED = "chartsOn"; //$NON-NLS-1$

    private List<String> allCharts;

    private boolean chartsOn;

    private boolean startedAsOn;

    private int interval;

    private boolean hiddenWorkFlowTask;

    private boolean hiddenDSCTask;

    private List<BasePortlet> portlets;

    private int pos;

    private int numColumns;

    private Map<String, List<Integer>> portletToLocations;

    private Map<String, Boolean> portletToVisibilities;

    private WelcomePortalServiceAsync service = (WelcomePortalServiceAsync) Registry.get(WelcomePortal.WELCOMEPORTAL_SERVICE);

    public MainFramePanel(int numColumns) {
        this(numColumns, null);
    }

    public MainFramePanel(int numColumns, Map<String, Boolean> config) {
        super(numColumns);

        this.numColumns = numColumns;

        if (config != null) {
            portletToVisibilities = config;
            chartsOn = config.get(CHARTS_ENABLED);
        } else {
            Object chartsOnObj = Cookies.getValue(COOKIES_CHARTS_ENABLED);
            chartsOn = (chartsOnObj == null) ? true : (Boolean) chartsOnObj;
        }

        setBorders(true);
        setStyleAttribute("backgroundColor", "white"); //$NON-NLS-1$ //$NON-NLS-2$
        if (numColumns == DEFAULT_COLUMN_NUM) {
            setColumnWidth(0, .33);
            setColumnWidth(1, .33);
            setColumnWidth(2, .33);
        } else {
            setColumnWidth(0, .50);
            setColumnWidth(1, .50);
        }

        this.addListener(Events.Add, new Listener<ContainerEvent>() {

            @Override
            public void handleEvent(ContainerEvent be) {

                BasePortlet portlet = (BasePortlet) be.getItem();
                String portletName = portlet.getPortletName();
                int column = MainFramePanel.this.getPortletColumn(portlet);
                int row = MainFramePanel.this.getPortletIndex(portlet);

                portletToLocations.put(portletName, Arrays.asList(column, row));
                int index = DEFAULT_ORDERING_INDEX.indexOf(portletName);
                if (index < DEFAULT_ORDERING_INDEX.size() - 1) {
                    initializePortlet(DEFAULT_ORDERING_INDEX.get(index + 1));
                } else {
                    // init cookies value after all portlet initialized
                    portletToVisibilities = new HashMap<String, Boolean>();
                    for (String name : portletToLocations.keySet()) {
                        portletToVisibilities.put(name, true);
                    }
                    // now we have all available portlets, need to record them in Actions panel in General project
                    Map<String, Boolean> portletConfigs = new HashMap<String, Boolean>(portletToLocations.size());
                    for (String name : portletToLocations.keySet()) {
                        portletConfigs.put(name, portletToVisibilities.get(name));
                    }

                    allCharts = new ArrayList<String>(portletToLocations.keySet());
                    allCharts.retainAll(DEFAULT_CHART_NAMES);

                    Cookies.setValue(COOKIES_PORTLET_LOCATIONS, portletToLocations);
                    Cookies.setValue(COOKIES_PORTLET_VISIBILITIES, portletToVisibilities);
                    Cookies.setValue(COOKIES_PORTLET_COLUMN, MainFramePanel.this.numColumns);
                    Cookies.setValue(COOKIES_CHARTS, allCharts);
                    Cookies.setValue(COOKIES_CHARTS_ENABLED, chartsOn);
                    
                    portletConfigs.put(USING_DEFAULT_COLUMN_NUM, MainFramePanel.this.numColumns == DEFAULT_COLUMN_NUM);
                    portletConfigs.put(CHARTS_ENABLED, chartsOn);
                    recordPortlets(portletConfigs);
                }
            }
        });

        this.addListener(Events.Drop, new Listener<PortalEvent>() {

            @Override
            public void handleEvent(PortalEvent pe) {
                updateLocations();
                Cookies.setValue(COOKIES_PORTLET_LOCATIONS, portletToLocations);
            }
        });

        service.getWelcomePortletConfig(new SessionAwareAsyncCallback<Map<Boolean, Integer>>() {

            @Override
            public void onSuccess(Map<Boolean, Integer> config) {
                if (!config.containsKey(startedAsOn)) {
                    startedAsOn = !startedAsOn;
                }

                interval = config.get(startedAsOn);
                initializePortlets();
            }

            @Override
            public void doOnFailure(Throwable e) {
                startedAsOn = DEFAULT_REFRESH_STARTASON;
                interval = DEFAULT_REFRESH_INTERVAL;
                initializePortlets();
            }
        });

    }

    private void initializePortlets() {
        portlets = new ArrayList<BasePortlet>();
        if (Cookies.getValue(COOKIES_PORTLET_LOCATIONS) == null) {
            portletToLocations = new LinkedHashMap<String, List<Integer>>();
            BasePortlet portlet;
            // start portlets initialization, see ContainerEvent listener
            portlet = new StartPortlet(this);
            portlets.add(portlet);
            this.add(portlet);
        } else if (portletToVisibilities == null) {
            portletToLocations = (Map<String, List<Integer>>) Cookies.getValue(COOKIES_PORTLET_LOCATIONS);
            portletToVisibilities = (Map<String, Boolean>) Cookies.getValue(COOKIES_PORTLET_VISIBILITIES);
            allCharts = (List<String>) Cookies.getValue(COOKIES_CHARTS);
            initializePortlets(portletToLocations, portletToVisibilities);
            Map<String, Boolean> portletConfigs = new HashMap<String, Boolean>(portletToLocations.size());
            for (String name : portletToLocations.keySet()) {
                portletConfigs.put(name, portletToVisibilities.get(name));
            }

            portletConfigs.put(USING_DEFAULT_COLUMN_NUM, numColumns == DEFAULT_COLUMN_NUM);
            chartsOn = (Boolean) Cookies.getValue(COOKIES_CHARTS_ENABLED);
            portletConfigs.put(CHARTS_ENABLED, chartsOn);
            recordPortlets(portletConfigs);
        } else {// switch column config/chartsSwitcher status updated, get current ordering from cookies
            portletToLocations = (Map<String, List<Integer>>) Cookies.getValue(COOKIES_PORTLET_LOCATIONS);
            SortedMap<List<Integer>, String> locationToPortlets = new TreeMap<List<Integer>, String>(getLocationComparator());
            for (String portletName : portletToLocations.keySet()) {
                locationToPortlets.put(portletToLocations.get(portletName), portletName);
            }

            List<String> orderedPortletNames = new ArrayList<String>(portletToLocations.size());
            orderedPortletNames.addAll(locationToPortlets.values());

            allCharts = (List<String>) Cookies.getValue(COOKIES_CHARTS);
            if (chartsOn) {
                if (!orderedPortletNames.containsAll(allCharts)) {
                    orderedPortletNames.addAll(allCharts);
                }
            } else {
                orderedPortletNames.removeAll(allCharts);
            }
            
            List<List<Integer>> defaultLocations = getDefaultLocations(numColumns);
            int index = 0;
            List<Integer> loc;
            for (String name : orderedPortletNames) {
                loc = defaultLocations.get(index);
                initializePortlet(name, loc, portletToVisibilities.get(name));
                index++;
            }
            
            updateLocations();

            Cookies.setValue(COOKIES_PORTLET_LOCATIONS, portletToLocations);
            Cookies.setValue(COOKIES_PORTLET_VISIBILITIES, portletToVisibilities);
            Cookies.setValue(COOKIES_PORTLET_COLUMN, numColumns);
            Cookies.setValue(COOKIES_CHARTS_ENABLED, chartsOn);
            
            Map<String, Boolean> portletConfigs = new HashMap<String, Boolean>(portletToLocations.size());
            for (String name : portletToLocations.keySet()) {
                portletConfigs.put(name, portletToVisibilities.get(name));
            }
            portletConfigs.put(USING_DEFAULT_COLUMN_NUM, numColumns == DEFAULT_COLUMN_NUM);
            portletConfigs.put(CHARTS_ENABLED, chartsOn);
            recordPortlets(portletConfigs);
        }

    }

    private Comparator<List<Integer>> getLocationComparator() {
        return new Comparator<List<Integer>>() {

            @Override
            public int compare(List<Integer> loc1, List<Integer> loc2) {
                assert loc1.size() == 2;
                assert loc2.size() == 2;

                int row1 = loc1.get(1);
                int row2 = loc2.get(1);

                int diff = row1 - row2;
                if (diff != 0) {
                    return diff;
                } else {
                    return loc1.get(0) - loc2.get(0);
                }
            }

        };
    }

    private List<List<Integer>> getDefaultLocations(int colNum) {

        List<List<Integer>> locs = new ArrayList<List<Integer>>(9);
        if (colNum == ALTERNATIVE_COLUMN_NUM) {
            locs.add(Arrays.asList(0, 0));
            locs.add(Arrays.asList(1, 0));
            locs.add(Arrays.asList(0, 1));
            locs.add(Arrays.asList(1, 1));
            locs.add(Arrays.asList(0, 2));
            locs.add(Arrays.asList(1, 2));
            locs.add(Arrays.asList(0, 3));
            locs.add(Arrays.asList(1, 3));
            locs.add(Arrays.asList(0, 4));
        } else {
            locs.add(Arrays.asList(0, 0));
            locs.add(Arrays.asList(1, 0));
            locs.add(Arrays.asList(2, 0));
            locs.add(Arrays.asList(0, 1));
            locs.add(Arrays.asList(1, 1));
            locs.add(Arrays.asList(2, 1));
            locs.add(Arrays.asList(0, 2));
            locs.add(Arrays.asList(1, 2));
            locs.add(Arrays.asList(2, 2));
        }

        return locs;
    }

    // called when portlet locations are restored from cookies -- back in synchronous mode
    private void initializePortlets(Map<String, List<Integer>> locs, Map<String, Boolean> visibles) {

        SortedMap<List<Integer>, String> locationToPortlets = new TreeMap<List<Integer>, String>(getLocationComparator());
        for (String portletName : locs.keySet()) {
            locationToPortlets.put(locs.get(portletName), portletName);
        }
        String portletName;
        for (List<Integer> loc : locationToPortlets.keySet()) {
            portletName = locationToPortlets.get(loc);
            initializePortlet(portletName, loc, visibles.get(portletName));
        }
    }

    private void initializePortlet(String portletName) {
        BasePortlet portlet = null;

        if (WelcomePortal.PROCESS.equals(portletName)) {
            portlet = new ProcessPortlet(this);
        } else if (WelcomePortal.ALERT.equals(portletName)) {
            initAlertPortlet();
        } else if (WelcomePortal.SEARCH.equals(portletName)) {
            initSearchPortlet();
        } else if (WelcomePortal.TASKS.equals(portletName)) {
            initTaskPortlet();
        } else if (WelcomePortal.CHART_DATA.equals(portletName)) {
            portlet = new DataChart(this);
        } else if (WelcomePortal.CHART_ROUTING_EVENT.equals(portletName)) {
            portlet = new RoutingChart(this);
        } else if (WelcomePortal.CHART_JOURNAL.equals(portletName)) {
            portlet = new JournalChart(this);
        } else if (WelcomePortal.CHART_MATCHING.equals(portletName)) {
            initMatchingChart();
        }

        if (portlet != null) {
            portlets.add(portlet);
            this.add(portlet);
        }
    }

    private void initializePortlet(String portletName, List<Integer> loc, boolean visible) {
        BasePortlet portlet = null;
        if (WelcomePortal.START.equals(portletName)) {
            portlet = new StartPortlet(this);
        } else if (WelcomePortal.PROCESS.equals(portletName)) {
            portlet = new ProcessPortlet(this);
        } else if (WelcomePortal.ALERT.equals(portletName)) {
            portlet = new AlertPortlet(this);
        } else if (WelcomePortal.SEARCH.equals(portletName)) {
            portlet = new SearchPortlet(this);
        } else if (WelcomePortal.TASKS.equals(portletName)) {
            portlet = new TaskPortlet(this);
        } else if (WelcomePortal.CHART_DATA.equals(portletName)) {
            portlet = new DataChart(this);
        } else if (WelcomePortal.CHART_ROUTING_EVENT.equals(portletName)) {
            portlet = new RoutingChart(this);
        } else if (WelcomePortal.CHART_JOURNAL.equals(portletName)) {
            portlet = new JournalChart(this);
        } else if (WelcomePortal.CHART_MATCHING.equals(portletName)) {
            portlet = new MatchingChart(this);
        } else {
            assert false;
        }

        if (portlet != null) {
            portlets.add(portlet);
            if (!visible) {
                portlet.hide();
            }
            this.add(portlet, loc);
        }
    }

    private void add(Portlet portlet) {
        this.add(portlet, pos % numColumns);
        pos++;
        fireEvent(Events.Add, new ContainerEvent<Portal, Portlet>(this, portlet));
    }

    private void add(Portlet portlet, List<Integer> loc) {
        insert(portlet, loc.get(1), loc.get(0));
    }

    private List<String> getPortletNames() {
        List<String> names = new ArrayList<String>(portlets.size());
        for (BasePortlet portlet : portlets) {
            names.add(portlet.getPortletName());
        }
        return names;
    }

    private void updateLocations() {
        int column;
        int row;
        for (BasePortlet portlet : portlets) {
            column = this.getPortletColumn(portlet);
            row = this.getPortletIndex(portlet);

            portletToLocations.put(portlet.getPortletName(), Arrays.asList(column, row));
        }
        
        if ((!chartsOn) && (portlets.size() < portletToLocations.size())) {
            for (String name : allCharts) {
                portletToLocations.remove(name);
            }
        }
    }

    public void updateVisibilities() {
        for (BasePortlet portlet : portlets) {
            portletToVisibilities.put(portlet.getPortletName(), portlet.isVisible());
        }
    }

    public void refreshPortlets() {
        for (BasePortlet portlet : portlets) {
            portlet.refresh();
        }
    }

    public void stopChartsAutoRefresh() {
        for (BasePortlet portlet : portlets) {
            if (allCharts.contains(portlet.getPortletName())) {
                portlet.autoRefresh(false);
            }
        }
    }
    public void refresh(Map<String, Boolean> config) {
        chartsOn = config.get(CHARTS_ENABLED);

        for (BasePortlet portlet : portlets) {

            if (config.get(portlet.getPortletName())) {
                portlet.show();
            } else {
                portlet.hide();
            }
        }

        this.layout(true);
        updateVisibilities();
        Cookies.setValue(COOKIES_PORTLET_VISIBILITIES, portletToVisibilities);
    }

    public void itemClick(final String context, final String application) {
        service.isExpired(UrlUtil.getLanguage(), new SessionAwareAsyncCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean result) {
                initUI(context, application);
            }
        });
    }

    public boolean isHiddenWorkFlowTask() {
        return this.hiddenWorkFlowTask;
    }

    public void setHiddenWorkFlowTask(boolean hiddenWorkFlowTask) {
        this.hiddenWorkFlowTask = hiddenWorkFlowTask;
    }

    public boolean isHiddenDSCTask() {
        return this.hiddenDSCTask;
    }

    public void setHiddenDSCTask(boolean hiddenDSCTask) {
        this.hiddenDSCTask = hiddenDSCTask;
    }

    public boolean getStartedAsOn() {
        return this.startedAsOn;
    }

    public int getInterval() {
        return this.interval;
    }

    private void initAlertPortlet() {

        service.isHiddenLicense(new SessionAwareAsyncCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean hideMe) {
                if (!hideMe) {
                    BasePortlet portlet = new AlertPortlet(MainFramePanel.this);
                    portlets.add(portlet);
                    MainFramePanel.this.add(portlet);
                } else {
                    int index = DEFAULT_ORDERING_INDEX.indexOf(WelcomePortal.ALERT);
                    initializePortlet(DEFAULT_ORDERING_INDEX.get(index + 1));
                }
            }

        });

    }

    private void initSearchPortlet() {

        service.isEnterpriseVersion(new SessionAwareAsyncCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean isEnterprise) {
                if (isEnterprise) {
                    BasePortlet portlet = new SearchPortlet(MainFramePanel.this);
                    portlets.add(portlet);
                    MainFramePanel.this.add(portlet);
                } else {
                    int index = DEFAULT_ORDERING_INDEX.indexOf(WelcomePortal.SEARCH);
                    initializePortlet(DEFAULT_ORDERING_INDEX.get(index + 1));
                }
            }
        });

    }

    private void initTaskPortlet() {

        service.isHiddenWorkFlowTask(new SessionAwareAsyncCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean hideMe) {
                setHiddenWorkFlowTask(hideMe);

                service.isHiddenDSCTask(new SessionAwareAsyncCallback<Boolean>() {

                    @Override
                    public void onSuccess(Boolean hideMeToo) {
                        setHiddenDSCTask(hideMeToo);

                        if (!isHiddenWorkFlowTask() || !isHiddenDSCTask()) {
                            BasePortlet portlet = new TaskPortlet(MainFramePanel.this);
                            portlets.add(portlet);
                            MainFramePanel.this.add(portlet);
                        } else {
                            int index = DEFAULT_ORDERING_INDEX.indexOf(WelcomePortal.TASKS);
                            initializePortlet(DEFAULT_ORDERING_INDEX.get(index + 1));
                        }

                    }
                });
            }
        });

    }

    private void initMatchingChart() {

        service.isEnterpriseVersion(new SessionAwareAsyncCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean isEnterprise) {
                if (isEnterprise) {
                    BasePortlet portlet = new MatchingChart(MainFramePanel.this);
                    portlets.add(portlet);
                    MainFramePanel.this.add(portlet);
                }
            }
        });

    }

    public native void openWindow(String url)/*-{
        window.open(url);
    }-*/;

    public native void initUI(String context, String application)/*-{
        $wnd.setTimeout(function() {
            $wnd.amalto[context][application].init();
        }, 50);
    }-*/;

    // record available portlets in Actions panel
    private native void recordPortlets(Map<String, Boolean> configs)/*-{
        $wnd.amalto.core.markPortlets(configs);
    }-*/;
}