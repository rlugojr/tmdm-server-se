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
package org.talend.mdm.webapp.journal.client.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.ItemBaseModel;
import org.talend.mdm.webapp.base.client.util.PostDataUtil;
import org.talend.mdm.webapp.base.client.util.UrlUtil;
import org.talend.mdm.webapp.journal.client.Journal;
import org.talend.mdm.webapp.journal.client.JournalEvents;
import org.talend.mdm.webapp.journal.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.journal.client.util.KeyUtil;
import org.talend.mdm.webapp.journal.shared.JournalSearchCriteria;

import com.amalto.core.objects.UpdateReportPOJO;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.DateTimePropertyEditor;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;

/**
 * DOC Administrator  class global comment. Detailled comment
 */
public class JournalSearchPanel extends FormPanel {

    private static JournalSearchPanel formPanel;
        
    private TextField<String> entityField;
    
    private TextField<String> keyField;
    
    private ComboBox<ItemBaseModel> sourceCombo;
    
    private ComboBox<ItemBaseModel> operationTypeCombo;
    
    private DateField startDateField;
    
    private DateField endDateField;
    
    private CheckBox strictCheckBox;
        
    private Button resetButton;
    
    private Button searchButton;
    
    private Button exportButton;
    
    public static JournalSearchPanel getInstance() {
        if (formPanel == null)
            formPanel = new JournalSearchPanel();
        return formPanel;
    }
    
    private JournalSearchPanel() {
        FormData formData = new FormData();
        this.setFrame(true);
        this.setHeight(-1);
        this.setPadding(5);
        this.setHeading(MessagesFactory.getMessages().search_panel_title());
        this.setButtonAlign(HorizontalAlignment.RIGHT);

        LayoutContainer main = new LayoutContainer();
        main.setLayout(new ColumnLayout());

        LayoutContainer left = new LayoutContainer();
        left.setStyleAttribute("paddingRight", "10px"); //$NON-NLS-1$ //$NON-NLS-2$
        FormLayout layout = new FormLayout();
        layout.setLabelAlign(LabelAlign.LEFT);
        left.setWidth(350);
        left.setLayout(layout);

        entityField = new TextField<String>();
        entityField.setFieldLabel(MessagesFactory.getMessages().entity_label());
        entityField.addListener(Events.KeyDown, new Listener<FieldEvent>() {

            public void handleEvent(FieldEvent be) {
                if (be.getKeyCode() == KeyCodes.KEY_ENTER) {
                    if (searchButton != null) {
                        searchButton.fireEvent(Events.Select);
                    }
                }
            }
        });
        left.add(entityField, formData);

        List<String> list = new ArrayList<String>();
        list.add(UpdateReportPOJO.GENERIC_UI_SOURCE); //$NON-NLS-1$
        list.add("adminWorkbench"); //$NON-NLS-1$
        list.add("dataSynchronization"); //$NON-NLS-1$
        list.add("workflow"); //$NON-NLS-1$

        sourceCombo = new ComboBox<ItemBaseModel>();
        sourceCombo.setId("source");//$NON-NLS-1$
        sourceCombo.setName("source");//$NON-NLS-1$
        sourceCombo.setFieldLabel(MessagesFactory.getMessages().source_label());
        sourceCombo.setDisplayField("label"); //$NON-NLS-1$
        sourceCombo.setValueField("key"); //$NON-NLS-1$
        sourceCombo.setStore(this.getListStore(list));
        sourceCombo.setTriggerAction(TriggerAction.ALL);
        sourceCombo.addListener(Events.KeyDown, new Listener<FieldEvent>() {

            public void handleEvent(FieldEvent be) {
                if (be.getKeyCode() == KeyCodes.KEY_ENTER) {
                    if (searchButton != null) {
                        searchButton.fireEvent(Events.Select);
                    }
                }
            }
        });
        left.add(sourceCombo, formData);

        startDateField = new DateField();
        startDateField.setFieldLabel(MessagesFactory.getMessages().start_date_label());
        startDateField.setPropertyEditor(new DateTimePropertyEditor("yyyy-MM-dd HH:mm:ss")); //$NON-NLS-1$
        startDateField.addListener(Events.KeyDown, new Listener<FieldEvent>() {

            public void handleEvent(FieldEvent be) {
                if (be.getKeyCode() == KeyCodes.KEY_ENTER) {
                    if (searchButton != null) {
                        searchButton.fireEvent(Events.Select);
                    }
                }
            }
        });
        left.add(startDateField, formData);

        LayoutContainer right = new LayoutContainer();
        right.setStyleAttribute("paddingLeft", "10px"); //$NON-NLS-1$ //$NON-NLS-2$
        right.setWidth(350);
        layout = new FormLayout();
        layout.setLabelAlign(LabelAlign.LEFT);
        right.setLayout(layout);

        keyField = new TextField<String>();
        keyField.setFieldLabel(MessagesFactory.getMessages().key_label());
        keyField.addListener(Events.KeyDown, new Listener<FieldEvent>() {

            public void handleEvent(FieldEvent be) {
                if (be.getKeyCode() == KeyCodes.KEY_ENTER) {
                    if (searchButton != null) {
                        searchButton.fireEvent(Events.Select);
                    }
                }
            }
        });
        right.add(keyField, formData);

        list.clear();
        list.add("ALL"); //$NON-NLS-1$
        list.add(UpdateReportPOJO.OPERATION_TYPE_CREATE);
        list.add(UpdateReportPOJO.OPERATION_TYPE_UPDATE);
        list.add(UpdateReportPOJO.OPERATION_TYPE_PHYSICAL_DELETE);
        list.add(UpdateReportPOJO.OPERATION_TYPE_LOGICAL_DELETE);
        list.add(UpdateReportPOJO.OPERATION_TYPE_RESTORED);
        list.add(UpdateReportPOJO.OPERATION_TYPE_ACTION);

        operationTypeCombo = new ComboBox<ItemBaseModel>();
        operationTypeCombo.setId("operationType");//$NON-NLS-1$
        operationTypeCombo.setName("operationType");//$NON-NLS-1$
        operationTypeCombo.setFieldLabel(MessagesFactory.getMessages().operation_type_label());
        operationTypeCombo.setDisplayField("label"); //$NON-NLS-1$
        operationTypeCombo.setValueField("key"); //$NON-NLS-1$
        operationTypeCombo.setStore(this.getListStore(list));
        operationTypeCombo.setTriggerAction(TriggerAction.ALL);
        operationTypeCombo.setEditable(false);
        operationTypeCombo.addListener(Events.KeyDown, new Listener<FieldEvent>() {

            public void handleEvent(FieldEvent be) {
                if (be.getKeyCode() == KeyCodes.KEY_ENTER && !operationTypeCombo.isExpanded()) {
                    if (searchButton != null) {
                        searchButton.fireEvent(Events.Select);
                    }
                } else if (KeyUtil.isCharacter(be.getKeyCode())) {
                    operationTypeCombo.doQuery(KeyUtil.getKeyValueByKeyCode(be.getKeyCode()), true);
                }
            }
        });
        right.add(operationTypeCombo, formData);

        endDateField = new DateField();
        endDateField.setFieldLabel(MessagesFactory.getMessages().end_date_label());
        endDateField.setPropertyEditor(new DateTimePropertyEditor("yyyy-MM-dd HH:mm:ss")); //$NON-NLS-1$
        endDateField.addListener(Events.KeyDown, new Listener<FieldEvent>() {

            public void handleEvent(FieldEvent be) {
                if (be.getKeyCode() == KeyCodes.KEY_ENTER) {
                    if (searchButton != null) {
                        searchButton.fireEvent(Events.Select);
                    }
                }
            }
        });
        right.add(endDateField, formData);

        main.add(left, new ColumnData(.5));
        main.add(right, new ColumnData(.5));
        this.add(main, new FormData("100%")); //$NON-NLS-1$

        strictCheckBox = new CheckBox();
        strictCheckBox.setEnabled(true);
        strictCheckBox.setValue(true);
        strictCheckBox.setBoxLabel(MessagesFactory.getMessages().strict_search_checkbox());
        this.getButtonBar().add(strictCheckBox);

        searchButton = new Button(MessagesFactory.getMessages().search_button());
        searchButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                if (entityField.isValid() && sourceCombo.isValid() && startDateField.isValid() && keyField.isValid() && operationTypeCombo.isValid() && endDateField.isValid()) {
                    if (startDateField.getValue() != null && endDateField.getValue() != null &&  startDateField.getValue().after(endDateField.getValue())) {
                        MessageBox.alert(MessagesFactory.getMessages().warning_title(), MessagesFactory.getMessages().search_date_error_message(),null);
                    } else {
                        bundleCriteria();
                        Dispatcher dispatcher = Dispatcher.get();
                        dispatcher.dispatch(JournalEvents.DoSearch);
                    }
                }
            }
        });
        this.addButton(searchButton);
        
        resetButton = new Button(MessagesFactory.getMessages().reset_button());
        resetButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                entityField.clear();
                keyField.clear();
                sourceCombo.clear();
                operationTypeCombo.clear();
                startDateField.clear();
                endDateField.clear();
                strictCheckBox.setValue(true);
            }
        });
        this.addButton(resetButton);

        exportButton = new Button(MessagesFactory.getMessages().exprot_excel_button());
        exportButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                PostDataUtil.postData(GWT.getHostPageBaseURL() + "journal/journalExport", getCriteriaMap()); //$NON-NLS-1$
            }
        });
        this.addButton(exportButton);
    }
    
    private ListStore<ItemBaseModel> getListStore(List<String> list) {
        List<ItemBaseModel> modelList = new ArrayList<ItemBaseModel>();
        for (String str : list) {
            ItemBaseModel model = new ItemBaseModel();
            model.set("label", str); //$NON-NLS-1$
            model.set("key", str); //$NON-NLS-1$
            modelList.add(model);
        }
        ListStore<ItemBaseModel> store = new ListStore<ItemBaseModel>();
        store.add(modelList);
        return store;
    }
    
    private void bundleCriteria() {
        JournalSearchCriteria criteria = Registry.get(Journal.SEARCH_CRITERIA);
        criteria.setEntity(entityField.getValue());
        criteria.setKey(keyField.getValue());
        if (sourceCombo.getValue() != null) {
            criteria.setSource(sourceCombo.getValue().get("key").toString()); //$NON-NLS-1$
        } else if (sourceCombo.getRawValue() != null && !"".equals(sourceCombo.getRawValue())) { //$NON-NLS-1$
            criteria.setSource(sourceCombo.getRawValue());
        } else {
            criteria.setSource(null);
        }

        if (operationTypeCombo.getValue() != null && !"ALL".equals(operationTypeCombo.getValue().get("key").toString())) //$NON-NLS-1$ //$NON-NLS-2$
            criteria.setOperationType(operationTypeCombo.getValue().get("key").toString()); //$NON-NLS-1$
        else
            criteria.setOperationType(null);

        criteria.setStartDate(startDateField.getValue());
        criteria.setEndDate(endDateField.getValue());
        criteria.setStrict(strictCheckBox.getValue());
    }

    private Map<String, String> getCriteriaMap() {
        Map<String, String> map = new HashMap<String, String>();
        if (entityField.getValue() != null)
            map.put("entity", entityField.getValue()); //$NON-NLS-1$
        if (keyField.getValue() != null)
            map.put("key", keyField.getValue()); //$NON-NLS-1$
        if (sourceCombo.getValue() != null)
            map.put("source", sourceCombo.getValue().get("key").toString()); //$NON-NLS-1$ //$NON-NLS-2$
        if (operationTypeCombo.getValue() != null && ! operationTypeCombo.getValue().get("key").equals("ALL")) //$NON-NLS-1$ //$NON-NLS-2$
            map.put("operationType", operationTypeCombo.getValue().get("key").toString()); //$NON-NLS-1$ //$NON-NLS-2$
        if (startDateField.getValue() != null)
            map.put("startDate", String.valueOf(startDateField.getValue().getTime())); //$NON-NLS-1$
        if (endDateField.getValue() != null)
            map.put("endDate", String.valueOf(endDateField.getValue().getTime())); //$NON-NLS-1$
        map.put("isStrict", String.valueOf(strictCheckBox.getValue())); //$NON-NLS-1$
        map.put("language", UrlUtil.getLanguage()); //$NON-NLS-1$

        return map;
    }
    
    public void initPanel() {
        resetButton.fireEvent(Events.Select);
    }
    
    public void setEntityFieldValue(String value) {
        this.entityField.setValue(value);
    }
    
    public void setKeyFieldValue(String value) {
        this.keyField.setValue(value);
    }
}