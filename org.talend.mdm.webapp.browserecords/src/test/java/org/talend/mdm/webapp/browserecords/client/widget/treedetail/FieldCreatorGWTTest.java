// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.widget.MultiLanguageField;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.FacetModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.creator.DataTypeCreator;
import org.talend.mdm.webapp.browserecords.client.model.ComboBoxModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.widget.ItemDetailToolBar;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.ComboBoxField;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.junit.client.GWTTestCase;

public class FieldCreatorGWTTest extends GWTTestCase {

    ItemsDetailPanel itemsDetailPanel;

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        UserSession session = new UserSession();
        session.put(UserSession.APP_HEADER, new AppHeader());
        Registry.register(BrowseRecords.USER_SESSION, session);
        itemsDetailPanel = ItemsDetailPanel.newInstance();
        itemsDetailPanel.setStaging(false);
    }

    public void testFieldReadOnly() {

        ComplexTypeModel product = new ComplexTypeModel();
        product.setTypePath("Product"); //$NON-NLS-1$

        SimpleTypeModel pictureType = new SimpleTypeModel("Picture", DataTypeCreator.getDataType("Picture", "string")); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
        pictureType.setTypePath("Product/Picture"); //$NON-NLS-1$

        SimpleTypeModel idType = new SimpleTypeModel("Id", DataTypeCreator.getDataType("string", "string")); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
        idType.setTypePath("Product/Id"); //$NON-NLS-1$
        idType.setReadOnly(true);

        SimpleTypeModel nameType = new SimpleTypeModel("Name", DataTypeCreator.getDataType("string", "string")); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
        nameType.setTypePath("Product/Name"); //$NON-NLS-1$
        nameType.setReadOnly(true);

        SimpleTypeModel descriptionType = new SimpleTypeModel("Description", DataTypeConstants.MLS); //$NON-NLS-1$
        descriptionType.setTypePath("Product/Description"); //$NON-NLS-1$
        descriptionType.setReadOnly(true);

        ComplexTypeModel featuresType = new ComplexTypeModel();
        featuresType.setTypePath("Product/Features"); //$NON-NLS-1$

        ComplexTypeModel sizesType = new ComplexTypeModel();
        sizesType.setTypePath("Product/Features/Sizes"); //$NON-NLS-1$

        SimpleTypeModel sizeType = new SimpleTypeModel("Size", DataTypeCreator.getDataType("string", "string")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        sizeType.setTypePath("Product/Features/Sizes/Size"); //$NON-NLS-1$

        ComplexTypeModel colorsType = new ComplexTypeModel();
        colorsType.setTypePath("Product/Features/Colors"); //$NON-NLS-1$

        SimpleTypeModel colorType = new SimpleTypeModel("Color", DataTypeCreator.getDataType("string", "string")); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
        colorType.setTypePath("Product/Features/Colors/Color"); //$NON-NLS-1$

        SimpleTypeModel priceType = new SimpleTypeModel("Price", DataTypeCreator.getDataType("decimal", "decimal")); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
        priceType.setTypePath("Product/Price"); //$NON-NLS-1$

        ItemNodeModel productNode = new ItemNodeModel("Product"); //$NON-NLS-1$

        ItemNodeModel pictureNode = new ItemNodeModel("Picture"); //$NON-NLS-1$
        ItemNodeModel idNode = new ItemNodeModel("Id"); //$NON-NLS-1$
        idNode.setKey(true);
        ItemNodeModel nameNode = new ItemNodeModel("Name"); //$NON-NLS-1$
        ItemNodeModel descriptionNode = new ItemNodeModel("Description"); //$NON-NLS-1$
        ItemNodeModel featuresNode = new ItemNodeModel("Features"); //$NON-NLS-1$
        ItemNodeModel priceNode = new ItemNodeModel("Price"); //$NON-NLS-1$

        productNode.add(pictureNode);
        productNode.add(idNode);
        productNode.add(nameNode);
        productNode.add(descriptionNode);
        productNode.add(featuresNode);
        productNode.add(priceNode);

        ItemNodeModel sizesNode = new ItemNodeModel("Sizes"); //$NON-NLS-1$
        ItemNodeModel colorsNode = new ItemNodeModel("Colors"); //$NON-NLS-1$
        featuresNode.add(sizesNode);
        featuresNode.add(colorsNode);

        ItemNodeModel size1Node = new ItemNodeModel("Size"); //$NON-NLS-1$
        ItemNodeModel size2Node = new ItemNodeModel("Size"); //$NON-NLS-1$
        sizesNode.add(size1Node);
        sizesNode.add(size2Node);

        ItemNodeModel color1Node = new ItemNodeModel("Color"); //$NON-NLS-1$
        ItemNodeModel color2Node = new ItemNodeModel("Color"); //$NON-NLS-1$
        colorsNode.add(color1Node);
        colorsNode.add(color2Node);

        Map<String, Field<?>> fieldMap = new HashMap<String, Field<?>>();

        Field<?> pictureField = TreeDetailGridFieldCreator.createField(pictureNode, pictureType, "en", fieldMap, //$NON-NLS-1$
                ItemDetailToolBar.CREATE_OPERATION, itemsDetailPanel);
        assertNotNull(pictureField);
        assertEquals(false, pictureField.isReadOnly());

        Field<?> idField = TreeDetailGridFieldCreator.createField(idNode, idType, "en", fieldMap, //$NON-NLS-1$
                ItemDetailToolBar.CREATE_OPERATION, itemsDetailPanel);
        assertNotNull(idField);
        assertEquals(false, idField.isReadOnly());

        idField = TreeDetailGridFieldCreator.createField(idNode, idType, "en", fieldMap, //$NON-NLS-1$
                ItemDetailToolBar.VIEW_OPERATION, itemsDetailPanel);
        assertNotNull(idField);
        assertEquals(true, idField.isReadOnly());

        Field<?> nameField = TreeDetailGridFieldCreator.createField(nameNode, nameType, "en", fieldMap, //$NON-NLS-1$
                ItemDetailToolBar.CREATE_OPERATION, itemsDetailPanel);
        assertNotNull(nameField);
        assertEquals(true, nameField.isReadOnly());

        Field<?> descriptionField = TreeDetailGridFieldCreator.createField(descriptionNode, descriptionType, "en", fieldMap, //$NON-NLS-1$
                ItemDetailToolBar.CREATE_OPERATION, itemsDetailPanel);
        assertNotNull(descriptionField);
        assertTrue(descriptionField instanceof MultiLanguageField);
        assertEquals(true, descriptionField.isReadOnly());

        Field<?> size1Field = TreeDetailGridFieldCreator.createField(size1Node, sizeType, "en", fieldMap, //$NON-NLS-1$
                ItemDetailToolBar.CREATE_OPERATION, itemsDetailPanel);
        assertNotNull(size1Field);
        assertEquals(false, size1Field.isReadOnly());

        Field<?> size2Field = TreeDetailGridFieldCreator.createField(size2Node, sizeType, "en", fieldMap, //$NON-NLS-1$
                ItemDetailToolBar.CREATE_OPERATION, itemsDetailPanel);
        assertNotNull(size2Field);
        assertEquals(false, size2Field.isReadOnly());

        Field<?> color1Field = TreeDetailGridFieldCreator.createField(color1Node, colorType, "en", fieldMap, //$NON-NLS-1$
                ItemDetailToolBar.CREATE_OPERATION, itemsDetailPanel);
        assertNotNull(color1Field);
        assertEquals(false, color1Field.isReadOnly());

        Field<?> color2Field = TreeDetailGridFieldCreator.createField(color2Node, colorType, "en", fieldMap, //$NON-NLS-1$
                ItemDetailToolBar.CREATE_OPERATION, itemsDetailPanel);
        assertNotNull(color2Field);
        assertEquals(false, color2Field.isReadOnly());

        Field<?> priceField = TreeDetailGridFieldCreator.createField(priceNode, priceType, "en", fieldMap, //$NON-NLS-1$
                ItemDetailToolBar.CREATE_OPERATION, itemsDetailPanel);
        assertNotNull(priceField);
        assertEquals(false, priceField.isReadOnly());

    }

    public void testFieldValid() {
        FacetModel facetModel = new FacetModel();
        facetModel.setName("length"); //$NON-NLS-1$
        facetModel.setValue("1"); //$NON-NLS-1$
        List<FacetModel> facets = new ArrayList<FacetModel>();
        facets.add(facetModel);
        ComplexTypeModel TestModel = new ComplexTypeModel();
        TestModel.setTypePath("TestModel"); //$NON-NLS-1$

        SimpleTypeModel idType = new SimpleTypeModel("Id", DataTypeCreator.getDataType("string", "string")); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
        idType.setFacets(facets);
        idType.setTypePath("Product/Id"); //$NON-NLS-1$
        idType.setReadOnly(true);

        SimpleTypeModel nameType = new SimpleTypeModel("Name", DataTypeCreator.getDataType("string", "string")); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
        nameType.setFacets(facets);
        nameType.setTypePath("Product/Name"); //$NON-NLS-1$
        nameType.setReadOnly(true);

        SimpleTypeModel descriptionType = new SimpleTypeModel("Description", DataTypeConstants.MLS); //$NON-NLS-1$
        descriptionType.setFacets(facets);
        descriptionType.setTypePath("Product/Description"); //$NON-NLS-1$
        descriptionType.setReadOnly(true);

        ItemNodeModel TestNode = new ItemNodeModel("Test"); //$NON-NLS-1$
        ItemNodeModel idNode = new ItemNodeModel("Id"); //$NON-NLS-1$
        idNode.setMandatory(true);
        idNode.setKey(true);
        ItemNodeModel nameNode = new ItemNodeModel("Name"); //$NON-NLS-1$
        nameNode.setMandatory(true);
        ItemNodeModel descriptionNode = new ItemNodeModel("Description"); //$NON-NLS-1$
        descriptionNode.setMandatory(true);

        TestNode.add(idNode);
        TestNode.add(nameNode);
        TestNode.add(descriptionNode);

        Map<String, Field<?>> fieldMap = new HashMap<String, Field<?>>();
        Field<?> idField = TreeDetailGridFieldCreator.createField(idNode, idType, "en", fieldMap, //$NON-NLS-1$
                ItemDetailToolBar.CREATE_OPERATION, itemsDetailPanel);
        assertNotNull(idField);
        assertEquals(false, idField.isReadOnly());
        assertEquals(false, ((TextField) idField).getAllowBlank());
        assertEquals("1", ((TextField) idField).getData("length")); //$NON-NLS-1$ //$NON-NLS-2$

        Field<?> nameField = TreeDetailGridFieldCreator.createField(nameNode, nameType, "en", fieldMap, //$NON-NLS-1$
                ItemDetailToolBar.CREATE_OPERATION, itemsDetailPanel);
        assertNotNull(nameField);
        assertEquals(true, nameField.isReadOnly());
        assertEquals(false, ((TextField) nameField).getAllowBlank());
        assertEquals("1", ((TextField) nameField).getData("length")); //$NON-NLS-1$ //$NON-NLS-2$

        Field<?> descriptionField = TreeDetailGridFieldCreator.createField(descriptionNode, descriptionType, "en", fieldMap, //$NON-NLS-1$
                ItemDetailToolBar.CREATE_OPERATION, itemsDetailPanel);
        assertNotNull(descriptionField);
        assertTrue(descriptionField instanceof MultiLanguageField);
        assertEquals(true, descriptionField.isReadOnly());
        assertEquals(false, ((TextField) descriptionField).getAllowBlank());
        assertEquals("1", ((TextField) descriptionField).getData("length")); //$NON-NLS-1$ //$NON-NLS-2$

        itemsDetailPanel.setStaging(true);
        idField = TreeDetailGridFieldCreator.createField(idNode, idType, "en", fieldMap, //$NON-NLS-1$
                ItemDetailToolBar.CREATE_OPERATION, itemsDetailPanel);
        assertNotNull(idField);
        assertEquals(false, idField.isReadOnly());
        assertEquals(true, ((TextField) idField).getAllowBlank());
        assertEquals(null, ((TextField) idField).getData("length")); //$NON-NLS-1$ 

        nameField = TreeDetailGridFieldCreator.createField(nameNode, nameType, "en", fieldMap, //$NON-NLS-1$
                ItemDetailToolBar.CREATE_OPERATION, itemsDetailPanel);
        assertNotNull(nameField);
        assertEquals(true, nameField.isReadOnly());
        assertEquals(true, ((TextField) nameField).getAllowBlank());
        assertEquals(null, ((TextField) nameField).getData("length")); //$NON-NLS-1$ 

        descriptionField = TreeDetailGridFieldCreator.createField(descriptionNode, descriptionType, "en", fieldMap, //$NON-NLS-1$
                ItemDetailToolBar.CREATE_OPERATION, itemsDetailPanel);
        assertNotNull(descriptionField);
        assertTrue(descriptionField instanceof MultiLanguageField);
        assertEquals(true, descriptionField.isReadOnly());
        assertEquals(true, ((TextField) descriptionField).getAllowBlank());
        assertEquals(null, ((TextField) descriptionField).getData("length")); //$NON-NLS-1$

    }

    @SuppressWarnings("nls")
    public void testPolymorphismTypeFieldCreate() {
        ComplexTypeModel addressType = new ComplexTypeModel("AddressType", DataTypeCreator.getDataType("AddressType", "string"));
        addressType.setTypePath("AddressType");
        addressType.setLabelMap(new HashMap<String, String>());
        ComplexTypeModel abstractAddressType = new ComplexTypeModel("AddressType", null);
        abstractAddressType.setAbstract(true);
        ComplexTypeModel defaultBlankValue = new ComplexTypeModel("", null);
        addressType.addComplexReusableTypes(defaultBlankValue);
        addressType.addComplexReusableTypes(abstractAddressType);
        ComplexTypeModel cnAddressType = new ComplexTypeModel("CNAddressType", null);
        addressType.addComplexReusableTypes(cnAddressType);
        ComplexTypeModel usAddressType = new ComplexTypeModel("USAddressType", null);
        addressType.addComplexReusableTypes(usAddressType);
        Map<String, Field<?>> fieldMap = new HashMap<String, Field<?>>();
        ItemNodeModel addressTypeNodeModel = new ItemNodeModel("AddressType");
        Field<?> addressTypeField = TreeDetailGridFieldCreator.createField(addressTypeNodeModel, addressType, "en", fieldMap,
                ItemDetailToolBar.CREATE_OPERATION, itemsDetailPanel);
        assertTrue(addressType.getReusableComplexTypes().size() == 4);
        assertTrue(addressTypeField instanceof ComboBoxField);
        assertTrue(((ComboBoxField<?>) addressTypeField).getStore().getCount() == 3);
        ComboBoxModel defaultModel = ((ComboBoxField<ComboBoxModel>) addressTypeField).getStore().getAt(0);
        assertEquals(defaultBlankValue.getName(), defaultModel.getValue());
        ComboBoxModel cnModel = ((ComboBoxField<ComboBoxModel>) addressTypeField).getStore().getAt(1);
        assertEquals(cnAddressType.getName(), cnModel.getValue());
        ComboBoxModel usModel = ((ComboBoxField<ComboBoxModel>) addressTypeField).getStore().getAt(2);
        assertEquals(usAddressType.getName(), usModel.getValue());
    }

    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords"; //$NON-NLS-1$
    }
}
