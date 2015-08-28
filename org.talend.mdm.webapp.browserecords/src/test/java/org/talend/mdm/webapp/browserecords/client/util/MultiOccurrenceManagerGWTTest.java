/*
 * Copyright (C) 2006-2015 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetail;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetail.DynamicTreeItem;

import com.google.gwt.junit.client.GWTTestCase;

public class MultiOccurrenceManagerGWTTest extends GWTTestCase {

    Map<String, TypeModel> metaDataTypes = new HashMap<String, TypeModel>();

    TreeDetail treeDetail = null;

    List<DynamicTreeItem> multiNodes = null;

    public void testAddMultiOccurrenceNode() {
        SimpleTypeModel simpleTypeModel = new SimpleTypeModel();
        simpleTypeModel.setAutoExpand(false);
        simpleTypeModel.setMaxOccurs(5);
        metaDataTypes.put("ThirdEntity/optionalDetails/optionalUbounded3", simpleTypeModel); //$NON-NLS-1$

        ItemNodeModel itemNodeModel = new ItemNodeModel();
        itemNodeModel.setName("optionalUbounded3"); //$NON-NLS-1$
        itemNodeModel.setTypePath("ThirdEntity/optionalDetails/optionalUbounded3"); //$NON-NLS-1$

        ItemNodeModel parent = new ItemNodeModel();
        parent.setName("optionalDetails"); //$NON-NLS-1$
        parent.setTypePath("ThirdEntity/optionalDetails"); //$NON-NLS-1$
        parent.add(itemNodeModel);
        itemNodeModel.setParent(parent);

        DynamicTreeItem item = new DynamicTreeItem();
        item.setItemNodeModel(itemNodeModel);

        DynamicTreeItem parentItem = new DynamicTreeItem();
        parentItem.addItem(item);

        MultiOccurrenceManager manager = new MultiOccurrenceManager(metaDataTypes, treeDetail, false);
        manager.addMultiOccurrenceNode(item);

        multiNodes = _getMultiOccurrence(manager, "optionalDetails/optionalUbounded3"); //$NON-NLS-1$
        assertEquals(1, multiNodes.size());
        assertEquals("optionalUbounded3", multiNodes.get(0).getItemNodeModel().getName()); //$NON-NLS-1$
        manager.addMultiOccurrenceNode(item);

        multiNodes = _getMultiOccurrence(manager, "optionalDetails/optionalUbounded3"); //$NON-NLS-1$
        assertEquals(2, multiNodes.size());
        assertEquals("optionalUbounded3", multiNodes.get(1).getItemNodeModel().getName()); //$NON-NLS-1$
    }

    private native List<DynamicTreeItem> _getMultiOccurrence(MultiOccurrenceManager manager, String xpath)/*-{
		return manager.@org.talend.mdm.webapp.browserecords.client.util.MultiOccurrenceManager::getBrothersGroup(Ljava/lang/String;)(xpath);
    }-*/;

    @Override
    public String getModuleName() {
        // GWTTestCase Required
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords"; //$NON-NLS-1$
    }
}
