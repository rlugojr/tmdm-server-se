// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.webapp.browserecords.client.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BoxComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.Widget;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class ItemsDetailPanel extends ContentPanel {

    public final static String SINGLETON = "SINGLETON"; //$NON-NLS-1$

    public final static String MULTIPLE = "MULTIPLE"; //$NON-NLS-1$

    // Panel within gray outer border in which all content is contained
    private LayoutContainer mainPanel = new LayoutContainer();

    // The north gray border in which to place the breadcrumb
    private LayoutContainer ncBorder = new LayoutContainer();

    // The tab panel widget.
    private ItemsDetailTabPanel itemsDetailTabPanel = new ItemsDetailTabPanel();

    private SimplePanel breadCrumb = new SimplePanel();

    private LayoutContainer banner = new LayoutContainer();

    private Text textTitle = new Text();

    private List<Text> subTitleList = new ArrayList<Text>();

    public ItemsDetailPanel() {
        super();
        this.setBodyBorder(false);
        this.setHeaderVisible(false);
        this.initPanel();
    }

    private void initPanel() {
        this.setId("ItemsDetailPanel"); //$NON-NLS-1$

        // This has border layout in order to draw gray borders
        this.setLayout(new BorderLayout());

        // Configure the mainPanel
        this.mainPanel.setId("ItemsDetailPanel-mainPanel"); //$NON-NLS-1$
        this.mainPanel.setLayout(new RowLayout(Orientation.VERTICAL));

        // Initialize the outer gray border panels.
        this.initBorderPanels();

        // Add breadcrumb to north gray border
        ncBorder.add(breadCrumb);

        // Configure the textTitle within the banner
        textTitle.setId("ItemsDetailPanel-textTitle"); //$NON-NLS-1$

        banner.setHeight(BANNER_HEIGHT);
        banner.setScrollMode(Scroll.AUTO);
        final LayoutContainer bannerWrapper = newBannerWrapper(banner);

        // Resize banner when parent resizes
        // For some reason this is not automatic
        mainPanel.addListener(Events.Resize, new Listener<BoxComponentEvent>() {

            public void handleEvent(final BoxComponentEvent event) {
                bannerWrapper.setWidth(mainPanel.getWidth());
            }
        });
        this.mainPanel.add(bannerWrapper);

        // Initialize and add the tabPanel
        // Resize tab panel explicitly or it will vertically overflow the parent container of fixed height
        mainPanel.addListener(Events.Resize, new Listener<BoxComponentEvent>() {

            public void handleEvent(final BoxComponentEvent event) {
                int newHeight = mainPanel.getHeight() - BANNER_HEIGHT;
                if (newHeight < 0) {
                    newHeight = 0;
                }
                itemsDetailTabPanel.setHeight(newHeight);
                itemsDetailTabPanel.setWidth(mainPanel.getWidth());
            }
        });
        mainPanel.add(itemsDetailTabPanel);
    }

    public void initBreadCrumb(BreadCrumb breadCrumb) {
        breadCrumb.getElement().setId("ItemsDetailPanel-breadCrumb"); //$NON-NLS-1$
        this.breadCrumb.setWidget(breadCrumb);
    }

    public void clearBreadCrumb() {
        this.breadCrumb.clear();
    }

    public void appendBreadCrumb(String concept, String ids) {
        if (this.breadCrumb.getWidget() instanceof BreadCrumb) {
            BreadCrumb curBC = (BreadCrumb) this.breadCrumb.getWidget();
            curBC.appendBreadCrumb(concept, ids);
        }
    }

    public void initBanner(List<String> xpathList, String desc) {
        clearBanner();
        String toolTipString = ""; //$NON-NLS-1$
        if (desc != null && !desc.equals("")) //$NON-NLS-1$
        {
            toolTipString = "<img style='margin-left:16px;' " + //$NON-NLS-1$
                    "src='/talendmdm/secure/img/genericUI/information_icon.gif' title='" + desc + "'/>"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (xpathList != null && xpathList.size() > 0) {
            int i = 1;
            for (String str : xpathList) {
                if (i == 1) {
                    textTitle.setText(str + toolTipString);
                    banner.add(textTitle);
                    i++;
                    continue;
                }
                Text subTitle = new Text();
                subTitle.setStyleName("ItemsDetailPanel-subTitle"); //$NON-NLS-1$
                subTitle.setText(str);
                subTitleList.add(subTitle);
                banner.add(subTitle);
            }
            banner.layout(true);
        }

    }

    public void clearBanner() {
        textTitle.setText(null);
        for (Text text : subTitleList) {
            banner.remove(text);
        }
        subTitleList.clear();
        banner.layout(true);
    }

    public void clearAll() {
        clearBanner();
        clearBreadCrumb();
        clearContent();
    }

    public ItemDetailTabPanelContentHandle addTabItem(String title, ContentPanel panel, String pattern, String id) {
        return itemsDetailTabPanel.addTabItem(title, panel, pattern, id);
    }

    public ItemPanel getCurrentItemPanel() {
        return itemsDetailTabPanel.getCurrentlySelectedTabItemPanel();
    }

    public void clearContent() {
        itemsDetailTabPanel.clear();
    }

    public void clearChildrenContent() {
        itemsDetailTabPanel.clearChildrenContent();
    }

    public void closeCurrentTab() {
        itemsDetailTabPanel.closeCurrentTab();
    }

    public Widget getFirstTabWidget() {

        return itemsDetailTabPanel.getFirstTabWidget();
    }

    public Widget getCurrentlySelectedTabWidget() {

        return itemsDetailTabPanel.getCurrentlySelectedTabWidget();
    }

    public void selectTabAtIndex(int index) {
        this.itemsDetailTabPanel.selectTabAtIndex(index);
    }

    public int getTabCount() {
        return itemsDetailTabPanel.getTabCount();
    }

    public Widget getTabWidgetAtIndex(int index) {
        return this.itemsDetailTabPanel.getTabWidgetAtIndex(index);
    }

    public void closeTabPanelWithId(String tabItemId) {

        this.itemsDetailTabPanel.closeTabPanelWithId(tabItemId);
    }

    /**
     * Handle returned by addTabItem used to delete the tab item added.
     */
    public class ItemDetailTabPanelContentHandle {

        private String id = ""; //$NON-NLS-1$

        public ItemDetailTabPanelContentHandle(String id) {
            this.id = id;
        }

        public void deleteContent() {
            ItemsDetailPanel.this.closeTabPanelWithId(id);
        }
    }

    /**
     * Custom tab panel within ItemsDetailPanel that supports the desired custom look and feel.
     */
    private class ItemsDetailTabPanel extends LayoutContainer {

        public static final int TAB_BAR_HEIGHT = 28;

        public static final int TAB_BAR_CONTENT_DIVIDER_HEIGHT = 5;

        // Internal state, saves id's and panels corresponding to each tab
        private Vector<String> tabIds = new Vector<String>();

        private Vector<ContentPanel> tabPanels = new Vector<ContentPanel>();

        // Visual elements, the tab bar, a horizontal spacer, and the content panel
        private TabBar tabBar = new TabBar();

        private LayoutContainer tabBarContentDivider = new LayoutContainer();

        private LayoutContainer tabContent = new LayoutContainer();

        // The desired width/height of tabContent and what is within tabContent
        // What is within tabContent must be smaller because of the borders of tabContent
        private int curTabContentWidth = 0;

        private int curTabContentHeight = 0;

        private int curTabContentInnerWidth = 0;

        private int curTabContentInnerHeight = 0;

        /**
         * Get the number of tabs.
         * 
         * @return The number of tabs.
         */
        public int getTabCount() {
            return this.tabBar.getTabCount();
        }

        /**
         * Called by resize listener to recalculate what tabContent and the within it should be in size. What is within
         * must be smaller due to borders of tabContent.
         */
        private void calcTabContentSize() {
            this.curTabContentHeight = this.getHeight() - TAB_BAR_HEIGHT - TAB_BAR_CONTENT_DIVIDER_HEIGHT;
            if (this.curTabContentHeight < 0)
                this.curTabContentHeight = 0;

            this.curTabContentWidth = this.getWidth();

            this.curTabContentInnerHeight = this.curTabContentHeight - 1;
            if (this.curTabContentInnerHeight < 0)
                this.curTabContentInnerHeight = 0;

            this.curTabContentInnerWidth = this.curTabContentWidth - 2;
            if (this.curTabContentInnerWidth < 0)
                this.curTabContentInnerWidth = 0;
        }

        /**
         * ItemsDetailTabPanel constructor.
         */
        public ItemsDetailTabPanel() {
            this.setLayout(new RowLayout(Orientation.VERTICAL));

            // Resize listener for the tab panel, recalculate sizes needed and resize everything within
            this.addListener(Events.Resize, new Listener<BoxComponentEvent>() {

                public void handleEvent(final BoxComponentEvent event) {
                    // Recalculate tabContent size
                    ItemsDetailTabPanel.this.calcTabContentSize();

                    // Resize tabContent
                    ItemsDetailTabPanel.this.tabContent.setHeight(ItemsDetailTabPanel.this.curTabContentHeight);
                    ItemsDetailTabPanel.this.tabContent.setWidth(ItemsDetailTabPanel.this.curTabContentWidth);

                    // Resize what is in tabContent, if anything
                    Widget contentWidget = ItemsDetailTabPanel.this.tabContent.getWidget(0);
                    if (contentWidget != null) {
                        contentWidget.setHeight(ItemsDetailTabPanel.this.curTabContentInnerHeight + "px");  //$NON-NLS-1$
                        contentWidget.setWidth(ItemsDetailTabPanel.this.curTabContentInnerWidth + "px"); //$NON-NLS-1$
                    }
                }
            });

            // Tab selection listener for tab panel, change the content displayed and size it appropriately
            this.tabBar.addSelectionHandler(new SelectionHandler<Integer>() {

                public void onSelection(SelectionEvent<Integer> arg0) {

                    ItemsDetailTabPanel.this.tabContent.removeAll();

                    int selectedItemIndex = arg0.getSelectedItem();
                    ContentPanel newPanel = ItemsDetailTabPanel.this.tabPanels.get(selectedItemIndex);
                    ItemsDetailTabPanel.this.tabContent.add(newPanel);

                    ItemsDetailTabPanel.this.layout(true);

                    newPanel.setHeight(ItemsDetailTabPanel.this.curTabContentInnerHeight);
                    newPanel.setWidth(ItemsDetailTabPanel.this.curTabContentInnerWidth);
                }
            });
            this.add(this.tabBar);

            this.tabBarContentDivider.setId("ItemsDetailPanel-tabBarContentDivider");  //$NON-NLS-1$
            this.add(this.tabBarContentDivider);

            this.tabContent.setId("ItemsDetailPanel-tabContent"); //$NON-NLS-1$
            this.add(this.tabContent);
        }

        /**
         * Add a new tab.
         * 
         * @param title
         * @param panel
         * @param pattern
         * @param id
         */
        public ItemDetailTabPanelContentHandle addTabItem(String title, ContentPanel panel, String pattern, String id) {
            if (pattern.equalsIgnoreCase(ItemsDetailPanel.MULTIPLE)) {
                this.closeTabPanelWithId(id);

                if (this.getTabCount() == 0) {
                    // Adding a first tab

                    // Create the tab, which must be special because it is the first
                    // It must have the Home icon in it
                    Text firstTabText = new Text(title);
                    firstTabText.addStyleName("gwt-Label"); //$NON-NLS-1$
                    SimplePanel firstTabPanel = new SimplePanel();
                    firstTabPanel.add(firstTabText);
                    firstTabPanel.addStyleName("gwt-TabBarItem-first"); //$NON-NLS-1$
                    this.tabBar.addTab(firstTabPanel);

                    // Save the ID
                    this.tabIds.add(id);

                    // Save the panel
                    this.tabPanels.add(panel);

                    // Select the tab
                    this.tabBar.selectTab(0);
                } else {
                    // Create the tab
                    this.tabBar.addTab(title);

                    // Save the ID
                    this.tabIds.add(id);

                    // Save the panel
                    this.tabPanels.add(panel);
                }

            } else {
                int itemIndex = this.tabIds.indexOf(id);
                if (itemIndex == -1) {
                    if (this.getTabCount() == 0) {
                        // Create the first tab, which must be special because it is
                        // the first. It must have the Home icon in it
                        Text firstTabText = new Text(title);
                        firstTabText.addStyleName("gwt-Label"); //$NON-NLS-1$
                        SimplePanel firstTabPanel = new SimplePanel();
                        firstTabPanel.add(firstTabText);
                        firstTabPanel.addStyleName("gwt-TabBarItem-first"); //$NON-NLS-1$
                        this.tabBar.addTab(firstTabPanel);
                    } else {
                        // Create the tab
                        this.tabBar.addTab(title);
                    }

                    // Save the ID
                    this.tabIds.add(id);

                    // Save the panel
                    this.tabPanels.add(panel);

                    // Select the tab
                    this.tabBar.selectTab(this.getTabCount() - 1);
                } else {
                    this.tabPanels.set(itemIndex, panel);
                    if (this.tabBar.getSelectedTab() == itemIndex) {
                        // Replacing the currently selected tab

                        this.tabContent.removeAll();
                        this.tabContent.add(panel);

                        this.layout(true);

                        panel.setHeight(this.curTabContentInnerHeight);
                        panel.setWidth(this.curTabContentInnerWidth);
                    }
                }
            }

            return new ItemDetailTabPanelContentHandle(id);
        }

        /**
         * This does nothing if index is not valid. If tab is selected, tries to select next one if there is one, and if
         * not, select the previous one.
         * 
         * @param index Index of the tab to remove. If out of bounds, method does nothing.
         */
        public void closeTabAtIndex(int index) {
            int tabCount = this.getTabCount();
            if (index >= 0 && index < tabCount) {
                // Attempting to close a valid tab

                if (tabCount == 1) {// Closing the last tab

                    this.tabContent.removeAll();
                } else if (index == this.tabBar.getSelectedTab()) {// Attempting to close selected tab and at least one
                                                                   // more remains

                    if (index < tabCount - 1) {
                        // Selected tab being removed is not the last tab
                        this.tabBar.selectTab(index + 1);
                    } else {
                        // Selected tab being removed is the last tab
                        this.tabBar.selectTab(tabCount - 2);
                    }
                }

                this.tabBar.removeTab(index);
                this.tabIds.remove(index);
                this.tabPanels.remove(index);
            }
        }

        /**
         * Clear all but the first tab.
         */
        public void clearChildrenContent() {
            while (this.getTabCount() > 1) {
                this.closeTabAtIndex(1);
            }
        }

        /**
         * Close tab with specified id. This does nothing if no tab has id.
         * 
         * @param tabItemId
         */
        public void closeTabPanelWithId(String tabItemId) {
            int index = tabIds.indexOf(tabItemId);
            if (index >= 0) {
                this.closeTabAtIndex(index);
            }
        }

        /**
         * Close the current tab. This does nothing if no tab currently selected.
         */
        public void closeCurrentTab() {
            int index = this.tabBar.getSelectedTab();
            if (index >= 0) {
                this.closeTabAtIndex(index);
            }
        }

        /**
         * Select the tab at index. This does nothing if index is not valid.
         * 
         * @param index Index of tab to select, starts from 0.
         */
        public void selectTabAtIndex(int index) {
            if (0 <= index && index < this.getTabCount()) {
                this.tabBar.selectTab(index);
            }
        }

        /**
         * Clear both display and internal state.
         */
        public void clear() {
            while (this.getTabCount() > 0) {
                this.closeTabAtIndex(0);
            }
        }

        /**
         * Returns widget contained in first tab. Returns null if no tabs.
         * 
         * @return
         */
        public Widget getFirstTabWidget() {
            if (this.getTabCount() > 0) {
                return this.tabPanels.get(0);
            }
            return null;
        }

        /**
         * Returns widget contained in currently selected tab. Returns null if no currently selected tab.
         * 
         * @return Widget in currently selected tab.
         */
        public Widget getCurrentlySelectedTabWidget() {
            if (this.tabBar.getSelectedTab() >= 0) {
                Widget w = this.tabContent.getWidget(0);
                return w;
            }
            return null;
        }

        /**
         * Returns ItemPanel in currently selected tab. If no currently selected tab or widget not ItemPanel, null is
         * returned.
         * 
         * @return ItemPanel in currently selected tab.
         */
        public ItemPanel getCurrentlySelectedTabItemPanel() {
            if (this.tabBar.getSelectedTab() >= 0) {
                Widget w = this.tabContent.getWidget(0);
                if (w != null && w instanceof ItemPanel) {
                    return (ItemPanel) w;
                }
            }
            return null;
        }

        /**
         * Get widget in tab at specified index. Returns null if index is not valid.
         * 
         * @param index
         * @return
         */
        public Widget getTabWidgetAtIndex(int index) {
            if (0 <= index && index < this.getTabCount()) {
                return this.tabPanels.get(index);
            }

            return null;
        }
    }

    private static final int BANNER_WRAPPER_LEFT_BORDER_WIDTH = 10;

    private static final int BANNER_WRAPPER_RIGHT_BORDER_WIDTH = 9;

    private static final int BANNER_HEIGHT = 70;

    /**
     * Wraps the banner in a panel that gives it side borders.
     * 
     * @param banner
     * @return
     */
    private LayoutContainer newBannerWrapper(LayoutContainer banner) {
        LayoutContainer bannerWrapper = new LayoutContainer(new BorderLayout());
        bannerWrapper.setId("ItemsDetailPanel-bannerWrapper");  //$NON-NLS-1$
        bannerWrapper.setHeight(BANNER_HEIGHT);

        LayoutContainer bannerWrapperLeft = new LayoutContainer();
        bannerWrapperLeft.setWidth(BANNER_WRAPPER_LEFT_BORDER_WIDTH);
        bannerWrapperLeft.setId("ItemsDetailPanel-bannerWrapperLeft");  //$NON-NLS-1$
        bannerWrapper.add(bannerWrapperLeft, newBorderData(LayoutRegion.WEST, BANNER_WRAPPER_LEFT_BORDER_WIDTH));

        LayoutContainer bannerWrapperRight = new LayoutContainer();
        bannerWrapperRight.setWidth(BANNER_WRAPPER_RIGHT_BORDER_WIDTH);
        bannerWrapperRight.setId("ItemsDetailPanel-bannerWrapperRight");  //$NON-NLS-1$
        bannerWrapper.add(bannerWrapperRight, newBorderData(LayoutRegion.EAST, BANNER_WRAPPER_RIGHT_BORDER_WIDTH));

        banner.setId("ItemsDetailPanel-bannerWrapperCenter");  //$NON-NLS-1$
        bannerWrapper.add(banner, newBorderCenterData());

        return bannerWrapper;
    }

    private static final int SIDE_BORDER_WIDTH = 14;

    private static final int NORTH_BORDER_HEIGHT = 25;

    private static final int SOUTH_BORDER_HEIGHT = 11;

    /**
     * Initialize the gray outer borders of the ItemsDetailPanel using a border layout to emulate the HTML technique of
     * using an image for each corner and side.
     */
    private void initBorderPanels() {
        // North strip of the background
        LayoutContainer nBorder = new LayoutContainer(new BorderLayout());
        nBorder.setId("ItemsDetailPanel-nBorder");  //$NON-NLS-1$

        LayoutContainer nwCorner = new LayoutContainer();
        nwCorner.setId("ItemsDetailPanel-nwCorner");  //$NON-NLS-1$
        nBorder.add(nwCorner, newBorderData(LayoutRegion.WEST, SIDE_BORDER_WIDTH));

        LayoutContainer neCorner = new LayoutContainer();
        neCorner.setId("ItemsDetailPanel-neCorner");  //$NON-NLS-1$
        nBorder.add(neCorner, newBorderData(LayoutRegion.EAST, SIDE_BORDER_WIDTH));

        // ncBorder is an instance variable because we put the breadcrumb in it
        ncBorder.setId("ItemsDetailPanel-ncBorder");  //$NON-NLS-1$
        nBorder.add(this.ncBorder, newBorderCenterData());

        this.add(nBorder, newBorderData(LayoutRegion.NORTH, NORTH_BORDER_HEIGHT));

        // South strip of the background
        LayoutContainer sBorder = new LayoutContainer(new BorderLayout());
        sBorder.setId("ItemsDetail-sBorder");  //$NON-NLS-1$

        LayoutContainer swCorner = new LayoutContainer();
        swCorner.setId("ItemsDetailPanel-swCorner");  //$NON-NLS-1$
        sBorder.add(swCorner, newBorderData(LayoutRegion.WEST, SIDE_BORDER_WIDTH));

        LayoutContainer seCorner = new LayoutContainer();
        seCorner.setId("ItemsDetailPanel-seCorner");  //$NON-NLS-1$
        sBorder.add(seCorner, newBorderData(LayoutRegion.EAST, SIDE_BORDER_WIDTH));

        LayoutContainer scBorder = new LayoutContainer();
        scBorder.setId("ItemsDetailPanel-scBorder");  //$NON-NLS-1$
        sBorder.add(scBorder, newBorderCenterData());

        this.add(sBorder, newBorderData(LayoutRegion.SOUTH, SOUTH_BORDER_HEIGHT));

        // West strip of the background
        LayoutContainer wBorder = new LayoutContainer();
        wBorder.setId("ItemsDetailPanel-wBorder");  //$NON-NLS-1$
        this.add(wBorder, newBorderData(LayoutRegion.WEST, SIDE_BORDER_WIDTH));

        // East strip of the background
        LayoutContainer eBorder = new LayoutContainer();
        eBorder.setId("ItemsDetailPanel-eBorder");  //$NON-NLS-1$
        this.add(eBorder, newBorderData(LayoutRegion.EAST, SIDE_BORDER_WIDTH));

        // Main panel containing all the content
        this.add(this.mainPanel, newBorderCenterData());
    }

    /**
     * Utility method for generating BorderLayoutData for the borders.
     * 
     * @param region
     * @param width
     * @return
     */
    private BorderLayoutData newBorderData(LayoutRegion region, int width) {
        BorderLayoutData result = new BorderLayoutData(region, width, width, width);
        result.setSplit(false);
        result.setCollapsible(false);
        return result;
    }

    /**
     * Utility method for generating BorderLayoutData for the borders.
     * 
     * @return
     */
    private BorderLayoutData newBorderCenterData() {
        BorderLayoutData ncData = new BorderLayoutData(LayoutRegion.CENTER);
        ncData.setSplit(false);
        ncData.setCollapsible(false);
        return ncData;
    }
}
