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
package org.talend.mdm.webapp.browserecords.client.widget.ForeignKey;

import java.util.List;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.resources.icon.Icons;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.SuggestComboBoxField;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Image;

public class ForeignKeyField extends TextField<ForeignKeyBean> {

    private BrowseRecordsServiceAsync service = (BrowseRecordsServiceAsync) Registry.get(BrowseRecords.BROWSERECORDS_SERVICE);

    protected List<String> foreignKeyInfo;

    protected SuggestComboBoxField suggestBox;

    protected String currentPath;

    protected String foreignKeyPath;

    private String usageField;

    protected boolean isStaging;

    protected Image selectButton;

    private ForeignKeyListWindow foreignKeyListWindow;

    protected String foreignConceptName;

    protected boolean showInput;

    protected boolean showSelectButton;

    public ForeignKeyField(String foreignKeyPath, List<String> foreignKeyInfo, String currentPath) {
        this.foreignKeyPath = foreignKeyPath;
        this.foreignKeyInfo = foreignKeyInfo;
        this.currentPath = currentPath;
        selectButton = new Image(Icons.INSTANCE.link());
        generateForeignKeyListWindow();
        suggestBox = new SuggestComboBoxField(this);
        showInput = true;
        showSelectButton = true;
    }

    @Override
    protected void onRender(Element target, int index) {
        El wrap = renderField();
        addButtonListener();
        setAutoWidth(true);
        setElement(wrap.dom, target, index);
        // updateCtrlButton();
        super.onRender(target, index);
    }

    @Override
    protected void onResize(int width, int height) {
        if ("SearchFieldCreator".equals(usageField)) { //$NON-NLS-1$
            suggestBox.setWidth(width - selectButton.getWidth() - 20);
        } else {
            suggestBox.setWidth(width - selectButton.getWidth());
        }
    }

    @Override
    protected void doAttachChildren() {
        super.doAttachChildren();
        ComponentHelper.doAttach(selectButton);
        if (showInput) {
            ComponentHelper.doAttach(suggestBox);
        }
    }

    @Override
    protected void doDetachChildren() {
        super.doDetachChildren();
        ComponentHelper.doDetach(selectButton);
        if (showInput) {
            ComponentHelper.doDetach(suggestBox);
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        selectButton.setVisible(!readOnly);
    }

    protected El renderField() {
        El wrap = new El(DOM.createTable());
        wrap.setElementAttribute("cellSpacing", "0"); //$NON-NLS-1$ //$NON-NLS-2$
        wrap.addStyleName("x-form-field-wrap"); //$NON-NLS-1$
        wrap.addStyleName("x-form-file-wrap"); //$NON-NLS-1$
        Element tbody = DOM.createTBody();
        Element foreignKeyTR = DOM.createTR();
        tbody.appendChild(foreignKeyTR);
        if (showInput) {
            Element inputTD = DOM.createTD();
            foreignKeyTR.appendChild(inputTD);
            suggestBox.render(inputTD);
        }
        Element iconTD = DOM.createTD();
        foreignKeyTR.appendChild(iconTD);
        Element iconBody = DOM.createTBody();
        Element iconTR = DOM.createTR();
        if (showSelectButton) {
            Element selectTD = DOM.createTD();
            iconTR.appendChild(selectTD);
            selectTD.appendChild(selectButton.getElement());
        }
        iconBody.appendChild(iconTR);
        iconTD.setAttribute("align", "right"); //$NON-NLS-1$//$NON-NLS-2$
        iconTD.appendChild(iconBody);
        wrap.appendChild(tbody);
        return wrap;
    }

    private void generateForeignKeyListWindow() {
        String[] foreignKeyPathArray = foreignKeyPath.split("/"); //$NON-NLS-1$
        if (foreignKeyPathArray.length > 0) {
            foreignConceptName = foreignKeyPathArray[0];
            service.getEntityModel(foreignConceptName, Locale.getLanguage(), new SessionAwareAsyncCallback<EntityModel>() {

                @Override
                public void onSuccess(EntityModel entityModel) {
                    foreignKeyListWindow = new ForeignKeyListWindow(foreignKeyPath, foreignKeyInfo, getDataCluster(),
                            entityModel, ForeignKeyField.this);
                    foreignKeyListWindow.setSize(550, 350);
                    foreignKeyListWindow.setResizable(false);
                    foreignKeyListWindow.setModal(true);
                    foreignKeyListWindow.setBlinkModal(true);
                    foreignKeyListWindow.setHeading(MessagesFactory.getMessages().fk_RelatedRecord());
                }
            });
        }
    }

    protected void showForeignKeyListWindow() {
        foreignKeyListWindow.show();
    }

    public void setUsageField(String usageField) {
        this.usageField = usageField;
    }

    public void setStaging(boolean isStaging) {
        this.isStaging = isStaging;
    }

    public List<String> getForeignKeyInfo() {
        return this.foreignKeyInfo;
    }

    public String getCurrentPath() {
        return this.currentPath;
    }

    public String getForeignKeyFilter() {
        return ""; //$NON-NLS-1$
    }

    public String getForeignKeyPath() {
        return this.foreignKeyPath;
    }

    public String getDataCluster() {
        if (isStaging) {
            return BrowseRecords.getSession().getAppHeader().getStagingDataCluster();
        } else {
            return BrowseRecords.getSession().getAppHeader().getMasterDataCluster();
        }
    }

    @Override
    public void setValue(ForeignKeyBean foreignKeyBean) {
        if (foreignKeyBean != null) {
            foreignKeyBean.setShowInfo(foreignKeyInfo.size() > 0);
        }
        if (suggestBox != null) {
            suggestBox.setValue(foreignKeyBean);
        }
        super.setValue(foreignKeyBean);
    }

    @Override
    public ForeignKeyBean getValue() {
        if (suggestBox.getValue() != null) {
            return suggestBox.getValue();
        }
        return value;
    }

    @Override
    protected void onFocus(ComponentEvent be) {
        if (suggestBox != null) {
            suggestBox.focus();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        selectButton.setVisible(enabled);
    }

    protected void addButtonListener() {
        selectButton.setTitle(MessagesFactory.getMessages().fk_select_title());
        selectButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent ce) {
                if (foreignKeyListWindow != null) {
                    foreignKeyListWindow.setForeignKeyFilter(getForeignKeyFilter());
                    showForeignKeyListWindow();
                }
            }
        });
    }

    public void setShowInput(boolean showInput) {
        this.showInput = showInput;
    }

    public void setShowSelectButton(boolean showSelectButton) {
        this.showSelectButton = showSelectButton;
    }
}
