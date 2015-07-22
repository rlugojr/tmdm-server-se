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
package org.talend.mdm.webapp.browserecords.client.widget.foreignkey;

import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.util.ClientResourceData;
import org.talend.mdm.webapp.browserecords.client.util.CommonUtilTestData;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.FKRelRecordWindow;

import com.extjs.gxt.ui.client.Registry;
import com.google.gwt.junit.client.GWTTestCase;

@SuppressWarnings("nls")
public class FKRelRecordWindowGWTTest extends GWTTestCase {

    private String foreignKey = "ProductFamily";

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        UserSession session = new UserSession();
        session.put(UserSession.CURRENT_ENTITY_MODEL, getCurrentEntityModel());
        Registry.register(BrowseRecords.USER_SESSION, session);
    }

    public void testGetEntityModel() {
        FKRelRecordWindow fkWindow = new FKRelRecordWindow();
        fkWindow.setEntityModel(getProductFamilyModel());
        fkWindow.setFkKey(foreignKey);
        assertNotNull(fkWindow.getEntityModel());
        assertTrue(fkWindow.getEntityModel().getConceptName().equals("ProductFamily"));
        assertNotNull(fkWindow.getParentEntityModel().getConceptName().equals("ProductFamily"));
    }

    public void testParseForeignKeyFilter() {
        FKRelRecordWindow fkWindow = new FKRelRecordWindow();
        // test Product datamodel
        String foreignKeyFilter = "ProductFamily/Name$$=$$Product/Name$$#";
        ItemNodeModel product = new ItemNodeModel();
        product.setTypeName("Product");
        product.setTypePath("Product");
        product.setObjectValue("Product");
        ItemNodeModel id = new ItemNodeModel();
        id.setTypeName("Id");
        id.setTypePath("Product/Id");
        id.setObjectValue("1");
        id.setParent(product);
        product.add(id);
        ItemNodeModel name = new ItemNodeModel();
        name.setTypeName("Name");
        name.setTypePath("Product/Name");
        name.setObjectValue("talend");
        name.setParent(product);
        product.add(name);
        ItemNodeModel price = new ItemNodeModel();
        price.setTypeName("Price");
        price.setTypePath("Product/Price");
        price.setObjectValue("9999");
        price.setParent(product);
        product.add(price);
        ItemNodeModel family = new ItemNodeModel();
        family.setTypeName("Family");
        family.setTypePath("Product/Family");
        family.setObjectValue("[123]");
        family.setParent(product);
        product.add(family);
        assertEquals("ProductFamily/Name$$=$$talend$$#",
                fkWindow.parseForeignKeyFilter(family, foreignKeyFilter, "Product/Family"));

        foreignKeyFilter = "ProductFamily/Name$$=$$../Name$$#";
        assertEquals("ProductFamily/Name$$=$$talend$$#",
                fkWindow.parseForeignKeyFilter(family, foreignKeyFilter, "Product/Family"));

        foreignKeyFilter = "ProductFamily/Name$$=$$\"talend\"$$#";
        assertEquals("ProductFamily/Name$$=$$talend$$#",
                fkWindow.parseForeignKeyFilter(family, foreignKeyFilter, "Product/Family"));

        foreignKeyFilter = "ProductFamily/Name$$=$$\'talend\'$$#";
        assertEquals("ProductFamily/Name$$=$$talend$$#",
                fkWindow.parseForeignKeyFilter(family, foreignKeyFilter, "Product/Family"));

        // test Person datamodel
        ItemNodeModel person = new ItemNodeModel();
        person.setTypeName("Person");
        person.setTypePath("Person");
        person.setObjectValue("Person");
        id = new ItemNodeModel();
        id.setTypeName("Id");
        id.setTypePath("Person/Id");
        id.setObjectValue("1");
        id.setParent(person);
        person.add(id);
        ItemNodeModel pname = new ItemNodeModel();
        pname.setTypeName("pname");
        pname.setTypePath("Person/pname");
        pname.setObjectValue("talend");
        pname.setParent(person);
        person.add(pname);
        ItemNodeModel addrs = new ItemNodeModel();
        addrs.setTypeName("addrs");
        addrs.setTypePath("Person/addrs");
        addrs.setParent(person);
        person.add(addrs);
        ItemNodeModel add_cod = new ItemNodeModel();
        add_cod.setTypeName("add_code");
        add_cod.setTypePath("Person/addrs/add_code");
        add_cod.setObjectValue("[123]");
        add_cod.setParent(addrs);
        addrs.add(add_cod);
        name = new ItemNodeModel();
        name.setTypeName("name");
        name.setTypePath("Person/addrs/name");
        name.setObjectValue("name");
        name.setParent(addrs);
        addrs.add(name);
        ItemNodeModel abc = new ItemNodeModel();
        abc.setTypeName("abc");
        abc.setTypePath("Person/addrs/abc");
        abc.setObjectValue("abc");
        abc.setParent(addrs);
        addrs.add(abc);

        foreignKeyFilter = "Addr/AddrId$$=$$Person/addrs/add_code$$#";
        assertEquals("Addr/AddrId$$=$$123$$#", fkWindow.parseForeignKeyFilter(abc, foreignKeyFilter, "Person/addrs/abc"));

        foreignKeyFilter = "Addr/AddrId$$=$$../add_code$$#";
        assertEquals("Addr/AddrId$$=$$123$$#", fkWindow.parseForeignKeyFilter(abc, foreignKeyFilter, "Person/addrs/abc"));

        foreignKeyFilter = "Addr/AddrId$$=$$\"123\"$$#";
        assertEquals("Addr/AddrId$$=$$123$$#", fkWindow.parseForeignKeyFilter(abc, foreignKeyFilter, "Person/addrs/abc"));

        foreignKeyFilter = "Addr/AddrId$$=$$\'123\'$$#";
        assertEquals("Addr/AddrId$$=$$123$$#", fkWindow.parseForeignKeyFilter(abc, foreignKeyFilter, "Person/addrs/abc"));
    }

    private EntityModel getCurrentEntityModel() {
        EntityModel productEntityModel = CommonUtilTestData.getEntityModel(ClientResourceData.getModelProduct());
        return productEntityModel;
    }

    private EntityModel getProductFamilyModel() {
        EntityModel familyEntityModel = CommonUtilTestData.getEntityModel(ClientResourceData.getModelProductFamily());
        return familyEntityModel;
    }

    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords";
    }
}