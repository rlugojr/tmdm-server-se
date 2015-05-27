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
package org.talend.mdm.webapp.journal.server.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import junit.framework.TestCase;

import org.dom4j.tree.DefaultElement;
import org.talend.mdm.webapp.journal.shared.JournalGridModel;
import org.talend.mdm.webapp.journal.shared.JournalSearchCriteria;
import org.talend.mdm.webapp.journal.shared.JournalTreeModel;

import com.amalto.core.objects.UpdateReportPOJO;
import com.extjs.gxt.ui.client.data.ModelData;

/**
 * DOC talend2  class global comment. Detailled comment
 */

public class JournalDBServiceTest extends TestCase{
    
    private WebServiceMock mock = new WebServiceMock();

    JournalDBService journalDBService = new JournalDBService(mock);
    
    @SuppressWarnings("unchecked")
    public void testGetResultListByCriteria () throws Exception {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  //$NON-NLS-1$
        JournalSearchCriteria criteria = new JournalSearchCriteria();
        criteria.setEntity("TestModel"); //$NON-NLS-1$
        criteria.setStartDate(dateFormat.parse("2012-07-01")); //$NON-NLS-1$
        criteria.setEndDate(dateFormat.parse("2012-09-30")); //$NON-NLS-1$
        criteria.setKey("1"); //$NON-NLS-1$
        criteria.setOperationType(UpdateReportPOJO.OPERATION_TYPE_CREATE);
        criteria.setSource(UpdateReportPOJO.GENERIC_UI_SOURCE); //$NON-NLS-1$
        
        Object[] result = null;
        List<JournalGridModel> journalGridModelList = null;
        JournalGridModel journalGridModel = null;
        
        mock.setEnterpriseVersion(true);
        result = journalDBService.getResultListByCriteria(criteria, 0, 20, "ASC", "key"); //$NON-NLS-1$ //$NON-NLS-2$
        journalGridModelList = (List<JournalGridModel>)result[1];
        assertEquals(20, journalGridModelList.size());

        mock.setForbiddenDataModelName("Product"); //$NON-NLS-1$
        result = journalDBService.getResultListByCriteria(criteria, 0, 20, "ASC", "key"); //$NON-NLS-1$ //$NON-NLS-2$        
        journalGridModelList = (List<JournalGridModel>)result[1];
        assertEquals(6, journalGridModelList.size());
        journalGridModel = journalGridModelList.get(0);
        assertEquals("T", journalGridModel.getDataContainer()); //$NON-NLS-1$
        assertEquals("T", journalGridModel.getDataModel()); //$NON-NLS-1$
        
        journalGridModel = journalGridModelList.get(2);
        assertEquals("T", journalGridModel.getDataContainer()); //$NON-NLS-1$
        assertEquals("T", journalGridModel.getDataModel()); //$NON-NLS-1$

        mock.setForbiddenDataModelName("T"); //$NON-NLS-1$
        result = journalDBService.getResultListByCriteria(criteria, 0, 20, "ASC", "key"); //$NON-NLS-1$ //$NON-NLS-2$   
        journalGridModelList = (List<JournalGridModel>)result[1];
        assertEquals(14, journalGridModelList.size());
        journalGridModel = journalGridModelList.get(0);
        assertEquals("Product", journalGridModel.getDataContainer()); //$NON-NLS-1$
        assertEquals("Product", journalGridModel.getDataModel()); //$NON-NLS-1$
        
        journalGridModel = journalGridModelList.get(2);
        assertEquals("Product", journalGridModel.getDataContainer()); //$NON-NLS-1$
        assertEquals("Product", journalGridModel.getDataModel()); //$NON-NLS-1$

        mock.setForbiddenconceptName("Product"); //$NON-NLS-1$
        result = journalDBService.getResultListByCriteria(criteria, 0, 20, "ASC", "key"); //$NON-NLS-1$ //$NON-NLS-2$   
        journalGridModelList = (List<JournalGridModel>)result[1];
        assertEquals(2, journalGridModelList.size());
        journalGridModel = journalGridModelList.get(0);
        assertEquals("Product", journalGridModel.getDataContainer()); //$NON-NLS-1$
        assertEquals("Product", journalGridModel.getDataModel()); //$NON-NLS-1$
        assertEquals("ProductFamily", journalGridModel.getEntity()); //$NON-NLS-1$

        journalGridModel = journalGridModelList.get(1);
        assertEquals("Product", journalGridModel.getDataContainer()); //$NON-NLS-1$
        assertEquals("Product", journalGridModel.getDataModel()); //$NON-NLS-1$
        assertEquals("ProductFamily", journalGridModel.getEntity()); //$NON-NLS-1$        

        mock.setForbiddenconceptName("ProductFamily"); //$NON-NLS-1$
        result = journalDBService.getResultListByCriteria(criteria, 0, 20, "ASC", "key"); //$NON-NLS-1$ //$NON-NLS-2$   
        journalGridModelList = (List<JournalGridModel>)result[1];
        assertEquals(12, journalGridModelList.size());
        journalGridModel = journalGridModelList.get(0);
        assertEquals("Product", journalGridModel.getDataContainer()); //$NON-NLS-1$
        assertEquals("Product", journalGridModel.getDataModel()); //$NON-NLS-1$
        assertEquals("Product", journalGridModel.getEntity()); //$NON-NLS-1$

        journalGridModel = journalGridModelList.get(2);
        assertEquals("Product", journalGridModel.getDataContainer()); //$NON-NLS-1$
        assertEquals("Product", journalGridModel.getDataModel()); //$NON-NLS-1$
        assertEquals("Product", journalGridModel.getEntity()); //$NON-NLS-1$   

        mock.setForbiddenDataModelName(null);
        mock.setForbiddenconceptName(null);
        mock.setEnterpriseVersion(false);
        result = journalDBService.getResultListByCriteria(criteria, 0, 20, "ASC", "key"); //$NON-NLS-1$ //$NON-NLS-2$   
        assertEquals(20, result[0]);

        mock.setForbiddenDataModelName("Product"); //$NON-NLS-1$
        result = journalDBService.getResultListByCriteria(criteria, 0, 20, "ASC", "key"); //$NON-NLS-1$ //$NON-NLS-2$   
        assertEquals(20, result[0]);

        mock.setForbiddenDataModelName("T"); //$NON-NLS-1$
        result = journalDBService.getResultListByCriteria(criteria, 0, 20, "ASC", "key"); //$NON-NLS-1$ //$NON-NLS-2$   
        assertEquals(20, result[0]);

        mock.setForbiddenconceptName("Product"); //$NON-NLS-1$
        result = journalDBService.getResultListByCriteria(criteria, 0, 20, "ASC", "key"); //$NON-NLS-1$ //$NON-NLS-2$   
        assertEquals(20, result[0]);

        mock.setForbiddenconceptName("ProductFamily"); //$NON-NLS-1$
        result = journalDBService.getResultListByCriteria(criteria, 0, 20, "ASC", "key"); //$NON-NLS-1$ //$NON-NLS-2$   
        assertEquals(20, result[0]);
    }
    
    public void testGetDetailTreeModel() throws Exception {
        String[] ids = {"genericUI","1360140140037"}; //$NON-NLS-1$ //$NON-NLS-2$
        JournalTreeModel journalTreeModel = journalDBService.getDetailTreeModel(ids);
        assertEquals("Update", journalTreeModel.getName()); //$NON-NLS-1$
        assertEquals(10, journalTreeModel.getChildCount());
        JournalTreeModel childModel = (JournalTreeModel)journalTreeModel.getChild(0);
        assertEquals("UserName:administrator", childModel.getName()); //$NON-NLS-1$
        childModel = (JournalTreeModel)journalTreeModel.getChild(1);
        assertEquals("Source:genericUI", childModel.getName()); //$NON-NLS-1$
        childModel = (JournalTreeModel)journalTreeModel.getChild(2);
        assertEquals("TimeInMillis:1361153957282", childModel.getName()); //$NON-NLS-1$
        childModel = (JournalTreeModel)journalTreeModel.getChild(3);
        assertEquals("OperationType:UPDATE", childModel.getName()); //$NON-NLS-1$
        childModel = (JournalTreeModel)journalTreeModel.getChild(4);
        assertEquals("Concept:Product", childModel.getName()); //$NON-NLS-1$
        childModel = (JournalTreeModel)journalTreeModel.getChild(5);
        assertEquals("RevisionID:", childModel.getName()); //$NON-NLS-1$
        childModel = (JournalTreeModel)journalTreeModel.getChild(6);
        assertEquals("DataCluster:Product", childModel.getName()); //$NON-NLS-1$
        childModel = (JournalTreeModel)journalTreeModel.getChild(7);
        assertEquals("DataModel:Product", childModel.getName()); //$NON-NLS-1$
        childModel = (JournalTreeModel)journalTreeModel.getChild(8);
        assertEquals("Key:1",childModel.getName()); //$NON-NLS-1$
        childModel = (JournalTreeModel)journalTreeModel.getChild(9);        
        assertEquals("path:Name",((JournalTreeModel)childModel.getChild(0)).getName()); //$NON-NLS-1$
        assertEquals("oldValue:1",((JournalTreeModel)childModel.getChild(1)).getName()); //$NON-NLS-1$
        assertEquals("newValue:123",((JournalTreeModel)childModel.getChild(2)).getName()); //$NON-NLS-1$
    }
    
    public void testGetComparisionTreeModel() throws NoSuchMethodException,InvocationTargetException,IllegalArgumentException,IllegalAccessException {
        String xmlString = "<result><Update><UserName>Jennifer</UserName><Source>genericUI</Source><TimeInMillis>1360032633336</TimeInMillis><OperationType>UPDATE</OperationType><RevisionID>null</RevisionID><DataCluster>DStar</DataCluster><DataModel>DStar</DataModel><Concept>Agency</Concept><Key>2</Key><Item><path>Name</path><oldValue>23456</oldValue><newValue>34567</newValue></Item></Update></result>"; //$NON-NLS-1$
        JournalTreeModel returnValue = journalDBService.getComparisionTreeModel(xmlString);
        assertEquals("root", returnValue.getId()); //$NON-NLS-1$
        assertEquals("Document", returnValue.getName()); //$NON-NLS-1$
        JournalTreeModel journalTreeModel = (JournalTreeModel) returnValue.getChild(0);
        assertEquals("result",journalTreeModel.getName()); //$NON-NLS-1$
        journalTreeModel = (JournalTreeModel) journalTreeModel.getChild(0);
        assertEquals("Update",journalTreeModel.getName()); //$NON-NLS-1$
        List<ModelData> journalTreeModelList = journalTreeModel.getChildren();
        assertEquals("UserName:Jennifer",journalTreeModelList.get(0).get("name")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("Source:genericUI",journalTreeModelList.get(1).get("name")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("TimeInMillis:1360032633336",journalTreeModelList.get(2).get("name")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("OperationType:UPDATE",journalTreeModelList.get(3).get("name")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("RevisionID:null",journalTreeModelList.get(4).get("name")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("DataCluster:DStar",journalTreeModelList.get(5).get("name")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("DataModel:DStar",journalTreeModelList.get(6).get("name")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("Concept:Agency",journalTreeModelList.get(7).get("name")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("Key:2",journalTreeModelList.get(8).get("name")); //$NON-NLS-1$ //$NON-NLS-2$
        journalTreeModel = (JournalTreeModel) journalTreeModelList.get(9);
        journalTreeModelList = journalTreeModel.getChildren();
        assertEquals("oldValue:23456",journalTreeModelList.get(1).get("name")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("newValue:34567",journalTreeModelList.get(2).get("name")); //$NON-NLS-1$ //$NON-NLS-2$
        
        returnValue = journalDBService.getComparisionTreeModel(""); //$NON-NLS-1$
        assertEquals("root", returnValue.getId()); //$NON-NLS-1$
        assertEquals("Document", returnValue.getName()); //$NON-NLS-1$
        assertEquals("root", returnValue.getPath()); //$NON-NLS-1$
        assertEquals(0, returnValue.getChildCount());
    }
    
    public void testGetModelByElement() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method method = journalDBService.getClass().getDeclaredMethod("getModelByElement", org.dom4j.Element.class); //$NON-NLS-1$
        method.setAccessible(true);
        
        org.dom4j.Element element = new DefaultElement("Product"); //$NON-NLS-1$
        element.addAttribute("id", "18-Product-1"); //$NON-NLS-1$ //$NON-NLS-2$
        element.addAttribute("cls", "tree-node-update"); //$NON-NLS-1$ //$NON-NLS-2$
        element.setText("text_value"); //$NON-NLS-1$
        
        Object returnValue = method.invoke(journalDBService, new Object[] { element });
        method.setAccessible(false);        
        JournalTreeModel journalTreeModel = (JournalTreeModel)returnValue;
        assertEquals("18-Product-1", journalTreeModel.getId()); //$NON-NLS-1$      
        assertEquals("Product:text_value", journalTreeModel.getName()); //$NON-NLS-1$
        assertEquals("/Product", journalTreeModel.getPath()); //$NON-NLS-1$
        assertEquals("tree-node-update", journalTreeModel.getCls()); //$NON-NLS-1$ 
    }
    
    public void testParseString2Model() throws NoSuchMethodException,InvocationTargetException,IllegalArgumentException,IllegalAccessException {
        String xmlString = "<result><Update><UserName>Jennifer</UserName><Source>genericUI</Source><TimeInMillis>1360032633336</TimeInMillis><OperationType>UPDATE</OperationType><RevisionID>null</RevisionID><DataCluster>DStar</DataCluster><DataModel>DStar</DataModel><Concept>Agency</Concept><Key>2</Key><Item><path>Name</path><oldValue>23456</oldValue><newValue>34567</newValue><path>Feautres/Sizes/Size[3]</path><oldValue>ccc</oldValue><newValue>333</newValue><path>Feautres/Sizes/Size[2]</path><oldValue>bbb</oldValue><newValue>222</newValue><path>Feautres/Sizes/Size[1]</path><oldValue>aaa</oldValue><newValue>111</newValue></Item></Update></result>"; //$NON-NLS-1$        
        Method method = journalDBService.getClass().getDeclaredMethod("parseString2Model", String.class); //$NON-NLS-1$
        method.setAccessible(true);
        JournalGridModel returnValue = (JournalGridModel)method.invoke(journalDBService, new Object[] { xmlString });
        method.setAccessible(false);
        assertEquals("DStar", returnValue.getDataContainer()); //$NON-NLS-1$
        assertEquals("DStar", returnValue.getDataModel()); //$NON-NLS-1$   
        assertEquals("Agency", returnValue.getEntity()); //$NON-NLS-1$   
        assertEquals("genericUI.1360032633336", returnValue.getIds()); //$NON-NLS-1$   
        assertEquals("2", returnValue.getKey()); //$NON-NLS-1$   
        assertEquals("", returnValue.getRevisionId()); //$NON-NLS-1$   
        assertEquals(UpdateReportPOJO.OPERATION_TYPE_UPDATE, returnValue.getOperationType());
        assertEquals("genericUI", returnValue.getSource()); //$NON-NLS-1$   
        assertEquals("1360032633336", returnValue.getOperationTime()); //$NON-NLS-1$
        assertEquals("Jennifer", returnValue.getUserName()); //$NON-NLS-1$
        assertEquals("/Agency/Name", returnValue.getChangeNodeList().get(0)); //$NON-NLS-1$
        assertEquals("/Agency/Feautres/Sizes/Size[1]", returnValue.getChangeNodeList().get(1)); //$NON-NLS-1$
        assertEquals("/Agency/Feautres/Sizes/Size[2]", returnValue.getChangeNodeList().get(2)); //$NON-NLS-1$    
        assertEquals("/Agency/Feautres/Sizes/Size[3]", returnValue.getChangeNodeList().get(3)); //$NON-NLS-1$    
    }
            
    public void testCheckNull() throws NoSuchMethodException,InvocationTargetException,IllegalArgumentException,IllegalAccessException {        
        Method method = journalDBService.getClass().getDeclaredMethod("checkNull", String.class); //$NON-NLS-1$
        method.setAccessible(true);
        Object returnValue = method.invoke(journalDBService, new Object[] { "genericUI" }); //$NON-NLS-1$            
        assertEquals("genericUI", returnValue); //$NON-NLS-1$
        returnValue = method.invoke(journalDBService, new Object[] { "null" }); //$NON-NLS-1$            
        assertEquals("", returnValue); //$NON-NLS-1$
        returnValue = method.invoke(journalDBService, new Object[] { null });        
        assertEquals("", returnValue); //$NON-NLS-1$
        method.setAccessible(false);
    }
}
