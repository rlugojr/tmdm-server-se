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
package org.talend.mdm.webapp.browserecords.client.widget.treedetail;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.junit.client.GWTTestCase;

public class MultiOccurrenceChangeItemGWTTest extends GWTTestCase {

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        UserSession session = new UserSession();
        session.put(UserSession.APP_HEADER, new AppHeader());
        Registry.register(BrowseRecords.USER_SESSION, session);
    }

    public void testMultiOccurrenceFieldLabel() {

        ItemNodeModel itemNodeModel = new ItemNodeModel();
        itemNodeModel.setTypePath("T/sub"); //$NON-NLS-1$
        itemNodeModel.setLabel("sub"); //$NON-NLS-1$

        ViewBean viewBean = new ViewBean();
        EntityModel entityModel = new EntityModel();
        LinkedHashMap<String, TypeModel> metaDataTypes = new LinkedHashMap<String, TypeModel>();
        TypeModel typeModel = new SimpleTypeModel("sub", DataTypeConstants.STRING); //$NON-NLS-1$
        Map<String, String> labelMap = new HashMap<String, String>();
        // labelMap.put("en", "sub");
        typeModel.setLabelMap(labelMap);
        typeModel.setMinOccurs(1);
        typeModel.setMaxOccurs(-1);
        typeModel.getType();
        metaDataTypes.put("T/sub", typeModel); //$NON-NLS-1$
        entityModel.setMetaDataTypes(metaDataTypes);
        viewBean.setBindingEntityModel(entityModel);
        Map<String, Field<?>> fieldMap = new HashMap<String, Field<?>>();

        MultiOccurrenceChangeItem multiOccurrenceChangeItem = new MultiOccurrenceChangeItem(itemNodeModel, viewBean, fieldMap,
                null, null);
        assertEquals("<div class=\"gwt-HTML\">sub<span style=\"color:red\"> *</span></div>", //$NON-NLS-1$
                multiOccurrenceChangeItem.getWidget(0).toString());

        typeModel.setMinOccurs(0);
        multiOccurrenceChangeItem = new MultiOccurrenceChangeItem(itemNodeModel, viewBean, fieldMap, null, null);
        assertEquals("<div class=\"gwt-HTML\">sub</div>", //$NON-NLS-1$
                multiOccurrenceChangeItem.getWidget(0).toString());

        typeModel.setMinOccurs(1);
        typeModel.setMaxOccurs(1);
        multiOccurrenceChangeItem = new MultiOccurrenceChangeItem(itemNodeModel, viewBean, fieldMap, null, null);
        assertEquals("<div class=\"gwt-HTML\">sub<span style=\"color:red\"> *</span></div>", //$NON-NLS-1$
                multiOccurrenceChangeItem.getWidget(0).toString());
    }

    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords"; //$NON-NLS-1$
    }
}
