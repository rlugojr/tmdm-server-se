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
package org.talend.mdm.webapp.browserecords.client.widget;

import java.util.ArrayList;
import java.util.List;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.BreadCrumbModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetailUtil;
import org.talend.mdm.webapp.browserecords.shared.Constants;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.SimplePanel;

public class NavigatorPanel extends ContentPanel {

    private BrowseRecordsServiceAsync service = (BrowseRecordsServiceAsync) Registry.get(BrowseRecords.BROWSERECORDS_SERVICE);

    private ContentPanel navigatorPanel;

    private ContentPanel detailPanel;

    private boolean isStaging = false;

    private boolean isFkToolBar = false;

    private boolean isHierarchyCall = false;

    String operation = ItemDetailToolBar.VIEW_OPERATION;

    public NavigatorPanel() {
        setId(MessagesFactory.getMessages().navigator_panel_label());
        initPanel();
        registerNavigatorService();
    }

    private void initPanel() {
        setHeaderVisible(false);
        setLayout(new BorderLayout());
        setStyleAttribute("height", "100%"); //$NON-NLS-1$ //$NON-NLS-2$  
        BorderLayoutData eastData = new BorderLayoutData(LayoutRegion.EAST, 400);
        eastData.setMargins(new Margins(0, 5, 0, 0));
        eastData.setMinSize(0);
        eastData.setMaxSize(7000);
        eastData.setSplit(true);
        eastData.setFloatable(false);
        eastData.setCollapsible(true);
        initDetailPanel();
        add(detailPanel, eastData);
        BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
        centerData.setMargins(new Margins(0));
        initNavigatorPanel();
        add(navigatorPanel, centerData);
    }

    private void initNavigatorPanel() {
        navigatorPanel = new ContentPanel();
        navigatorPanel.setLayout(new FitLayout());
        navigatorPanel.setHeaderVisible(false);
        SimplePanel navigator = new SimplePanel();
        navigator.getElement().setId("navigator"); //$NON-NLS-1$
        navigator.getElement().getStyle().setProperty("height", 800, Unit.PX); //$NON-NLS-1$
        navigator.getElement().getStyle().setProperty("width", 800, Unit.PX); //$NON-NLS-1$
        navigatorPanel.add(navigator);
    }

    private void initDetailPanel() {
        detailPanel = new ContentPanel();
        detailPanel.setLayout(new FitLayout());
        detailPanel.setHeading(MessagesFactory.getMessages().navigator_detailPanel_label());

    }

    public void updateDetailPanel(final String ids, final String concept) {
        detailPanel.removeAll();
        service.getItemBeanById(concept, ids, Locale.getLanguage(), new SessionAwareAsyncCallback<ItemBean>() {

            @Override
            public void onSuccess(ItemBean itemBean) {
                String[] idArr = TreeDetailUtil.parseKey(ids);
                final ItemsDetailPanel panel = ItemsDetailPanel.newInstance();
                panel.setStaging(isStaging);
                final BrowseRecordsServiceAsync brService = (BrowseRecordsServiceAsync) Registry
                        .get(BrowseRecords.BROWSERECORDS_SERVICE);
                brService.getItemBeanById(concept, idArr, Locale.getLanguage(), new SessionAwareAsyncCallback<ItemBean>() {

                    @Override
                    public void onSuccess(final ItemBean item) {
                        brService.getExsitedViewName(concept, new SessionAwareAsyncCallback<String>() {

                            @Override
                            public void onSuccess(String viewName) {
                                brService.getView(viewName, Locale.getLanguage(), new SessionAwareAsyncCallback<ViewBean>() {

                                    @Override
                                    public void onSuccess(ViewBean viewBean) {
                                        ItemPanel itemPanel = new ItemPanel(isStaging, viewBean, item, operation, panel);
                                        itemPanel.getToolBar().setOutMost(true);
                                        itemPanel.getToolBar().setFkToolBar(isFkToolBar);
                                        itemPanel.getToolBar().setHierarchyCall(isHierarchyCall);

                                        List<BreadCrumbModel> breads = new ArrayList<BreadCrumbModel>();
                                        if (item != null) {
                                            breads.add(new BreadCrumbModel("", BreadCrumb.DEFAULTNAME, null, null, false)); //$NON-NLS-1$
                                            breads.add(new BreadCrumbModel(item.getConcept(), item.getLabel(), item.getIds(),
                                                    item.getDisplayPKInfo().equals(item.getLabel()) ? null : item
                                                            .getDisplayPKInfo(), true));
                                        }

                                        panel.setOutMost(true);
                                        panel.setId(item.getIds());
                                        panel.initBanner(item.getPkInfoList(), item.getDescription());
                                        panel.addTabItem(item.getLabel(), itemPanel, ItemsDetailPanel.SINGLETON, item.getIds());
                                        panel.initBreadCrumb(new BreadCrumb(breads, panel));

                                        TypeModel typeModel = viewBean.getBindingEntityModel().getMetaDataTypes()
                                                .get(item.getConcept());

                                        String tabItemId = "Navigator" + typeModel.getLabel(Locale.getLanguage()) //$NON-NLS-1$
                                                + " " + panel.getItemId(); //$NON-NLS-1$
                                        panel.setHeading(tabItemId);
                                        panel.setItemId(tabItemId
                                                + (isStaging ? Constants.BROWSE_STAGING_SUFFIX_MARK
                                                        : Constants.BROWSE_MASTER_SUFFIX_MARK));
                                        detailPanel.add(panel);
                                        layout();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    public static String getBaseUrl() {
        return GWT.getHostPageBaseURL() + "services/rest"; //$NON-NLS-1$
    }

    public static void renderPanel(String baseUrl, String ids, String concept, String cluster, ContentPanel contentPanel) {
        if (GWT.isScript()) {
            String itemId = concept + "_" + ids; //$NON-NLS-1$
            renderGwtPanel(itemId, contentPanel);
        } else {
            renderDebugPanel(contentPanel);
        }
        String restServiceUrl = baseUrl + "services/rest"; //$NON-NLS-1$
        paintNavigator(restServiceUrl, ids, concept, cluster, Locale.getLanguage());
    }

    private static void renderDebugPanel(ContentPanel contentPanel) {
        Window window = new Window();
        window.setLayout(new FitLayout());
        window.add(contentPanel);
        window.setSize(1100, 700);
        window.setMaximizable(true);
        window.setModal(false);
        window.show();
    }

    private native void registerNavigatorService()/*-{
		var instance = this;
		$wnd.amalto = $wnd.amalto || {};
        $wnd.amalto.navigator = $wnd.amalto.navigator || {};
        $wnd.amalto.navigator.Navigator = $wnd.amalto.navigator.Navigator || {};
		$wnd.amalto.navigator.Navigator.openRecord = function(ids, concept) {
			instance
					.@org.talend.mdm.webapp.browserecords.client.widget.NavigatorPanel::updateDetailPanel(Ljava/lang/String;Ljava/lang/String;)(
							ids, concept);
		}
		
		$wnd.amalto.navigator.Navigator.getBaseUrl = function() {
            @org.talend.mdm.webapp.browserecords.client.widget.NavigatorPanel::getBaseUrl()();
        }
    }-*/;

    public native static void paintNavigator(String restServiceUrl, String ids, String concept, String cluster, String language)/*-{
		$wnd.amalto.itemsbrowser.NavigatorPanel(restServiceUrl, ids, concept,
				cluster, language);
    }-*/;

    public native static void renderGwtPanel(String itemId, ContentPanel contentPanel)/*-{
		var tabPanel = $wnd.amalto.core.getTabPanel();
		var panel = @org.talend.mdm.webapp.browserecords.client.widget.NavigatorPanel::transferPanel(Lcom/extjs/gxt/ui/client/widget/ContentPanel;)(contentPanel);
		tabPanel.add(panel);
		tabPanel.setSelection(itemId);
    }-*/;

    private native static JavaScriptObject transferPanel(ContentPanel contentPanel)/*-{
		var panel = {
			// imitate extjs's render method, really call gxt code.
			render : function(el) {
				var rootPanel = @com.google.gwt.user.client.ui.RootPanel::get(Ljava/lang/String;)(el.id);
				rootPanel.@com.google.gwt.user.client.ui.RootPanel::add(Lcom/google/gwt/user/client/ui/Widget;)(contentPanel);
			},
			// imitate extjs's setSize method, really call gxt code.
			setSize : function(width, height) {
				contentPanel.@com.extjs.gxt.ui.client.widget.ContentPanel::setSize(II)(width, height);
			},
			// imitate extjs's getItemId, really return itemId of ContentPanel of GXT.
			getItemId : function() {
				return contentPanel.@com.extjs.gxt.ui.client.widget.ContentPanel::getItemId()();
			},
			// imitate El object of extjs
			getEl : function() {
				var el = contentPanel.@com.extjs.gxt.ui.client.widget.ContentPanel::getElement()();
				return {
					dom : el
				};
			},
			// imitate extjs's doLayout method, really call gxt code.
			doLayout : function() {
				return contentPanel.@com.extjs.gxt.ui.client.widget.ContentPanel::doLayout()();
			},
			title : function() {
				return contentPanel.@com.extjs.gxt.ui.client.widget.ContentPanel::getHeading()();
			}
		};
		return panel;
    }-*/;
}
