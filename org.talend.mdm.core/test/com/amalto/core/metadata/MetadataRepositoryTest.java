/*
 * Copyright (C) 2006-2014 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.metadata;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ConsoleDumpMetadataVisitor;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.MetadataUtils;
import org.talend.mdm.commmon.metadata.TypeMetadata;

/**
 * Schema parsing <br>
 * <li>add Chinese and Japanese language label testCase
 */
@SuppressWarnings("nls")
public class MetadataRepositoryTest extends TestCase {

    ConsoleDumpMetadataVisitor visitor = new ConsoleDumpMetadataVisitor();

    public void test1() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("schema.xsd");
        repository.load(stream);
        // repository.accept(visitor);

        ComplexTypeMetadata product = repository.getComplexType("Product");
        assertNotNull(product);
        assertEquals("Product", product.getName(new Locale("en")));
        assertEquals("Product", product.getName(new Locale("zn")));
        assertEquals("Produit", product.getName(new Locale("fr")));
        FieldMetadata field = product.getField("Features/Sizes/Size");
        assertNotNull(field);
        assertEquals("Features/Sizes/Size", field.getPath());
        assertEquals("Product", field.getEntityTypeName());
        FieldMetadata name = product.getField("Name");
        assertNotNull(name);
        assertEquals("Name", name.getName(new Locale("en")));
        assertEquals("Name", name.getName(new Locale("zn")));
        assertEquals("Nom", name.getName(new Locale("fr")));
    }

    public void test2() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("schema2.xsd");
        repository.load(stream);
        // repository.accept(visitor);
    }

    public void test3() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("schema3.xsd");
        repository.load(stream);
        // repository.accept(visitor);
    }

    public void test4() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("schema4.xsd");
        repository.load(stream);
        // repository.accept(visitor);
    }

    public void test5() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("schema5.xsd");
        repository.load(stream);
        // repository.accept(visitor);
    }

    public void test6() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("schema6.xsd");
        repository.load(stream);
        // repository.accept(visitor);
    }

    public void test7() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("schema7.xsd");
        try {
            repository.load(stream); // Model has many errors to be fixed.
        } catch (Exception e) {
            // Expected
        }
        // repository.accept(visitor);
    }

    public void test8() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("schema8.xsd");
        repository.load(stream);
        // repository.accept(visitor);
    }

    public void test9() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("schema9.xsd");
        repository.load(stream);
        // repository.accept(visitor);
    }

    public void test10() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("schema10.xsd");
        repository.load(stream);
        // repository.accept(visitor);
    }

    public void test11() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("schema11.xsd");
        repository.load(stream);
        // repository.accept(visitor);
    }

    public void test12() {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema12.xsd");
        repository.load(stream);
        assertTrue(repository.getTypes().size() > 0);
    }

    public void test13() {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema13.xsd");
        repository.load(stream);
        assertTrue(repository.getTypes().size() > 0);
        // repository.accept(visitor);
    }

    public void test14() {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema14.xsd");
        try {
            repository.load(stream);
            fail("Expected exception due to invalid key definition in inheritance tree.");
        } catch (Exception e) {
            // Expected.
        }
    }

    public void test15() {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema15.xsd");
        repository.load(stream);

        ComplexTypeMetadata company = repository.getComplexType("Company");
        assertNotNull(company);
        assertEquals(1, company.getKeyFields().size());
    }

    public void test16() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema16.xsd");
        repository.load(stream);

        ComplexTypeMetadata person = repository.getComplexType("Person");
        assertNotNull(person);
        assertEquals(2, person.getKeyFields().size());
        String[] expectedOrder = { "lastname", "firstname" };
        int i = 0;
        for (FieldMetadata keyField : person.getKeyFields()) {
            assertEquals(expectedOrder[i++], keyField.getName());
        }
    }

    public void test17() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema17.xsd");
        repository.load(stream);

        ComplexTypeMetadata entityType = repository.getComplexType("A");
        assertNotNull(entityType);
        TypeMetadata simpleType = repository.getNonInstantiableType("", "A");
        assertNotNull(simpleType);
        assertNotSame(entityType, simpleType);
    }

    public void test18() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema18.xsd");
        repository.load(stream);

        ComplexTypeMetadata entityType = repository.getComplexType("A");
        assertNotNull(entityType);
        assertTrue(entityType.hasField("string20"));
        assertTrue(entityType.hasField("string30"));
        assertTrue(entityType.hasField("string40"));

        TypeMetadata string20Field = entityType.getField("string20").getType();
        assertEquals("20", string20Field.getData(MetadataRepository.DATA_MAX_LENGTH));
        TypeMetadata string30Field = entityType.getField("string30").getType();
        assertEquals("30", string30Field.getData(MetadataRepository.DATA_MAX_LENGTH));
        TypeMetadata string40Field = entityType.getField("string40").getType();
        assertEquals("40", string40Field.getData(MetadataRepository.DATA_MAX_LENGTH));
    }

    public void test19() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema19.xsd");
        try {
            repository.load(stream);
            fail("Expected exception (cycles in model).");
        } catch (Exception e) {
            // Expected
        }
    }

    public void test20() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema20.xsd");
        repository.load(stream);
        ComplexTypeMetadata test = repository.getComplexType("Test");
        assertNotNull(test);
        assertTrue(test.hasField("text"));
        assertEquals("888", test.getField("text").getType().getData(MetadataRepository.DATA_MAX_LENGTH));
    }

    public void test21() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema21.xsd");
        repository.load(stream);
        ComplexTypeMetadata test = repository.getComplexType("shiporder");
        assertNotNull(test);
        assertTrue(test.hasField("shipto/name"));
    }

    public void testCopy() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema21.xsd");
        repository.load(stream);
        ComplexTypeMetadata test1 = repository.getComplexType("shiporder");

        MetadataRepository copy = repository.copy();
        ComplexTypeMetadata test2 = copy.getComplexType("shiporder");

        assertEquals(test1.getName(), test2.getName());
        assertTrue(test1 != test2);
    }

    public void test22() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema22.xsd");
        repository.load(stream);
        // repository.accept(visitor);
        ComplexTypeMetadata box = repository.getComplexType("Box");
        assertNotNull(box);
        FieldMetadata field = box.getField("FieldCTFK/Name");
        assertNotNull(field);
        assertEquals("Box/FieldCTFK/Name", field.getEntityTypeName() + "/" + field.getPath());
    }

    public void test23() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema23.xsd");
        try {
            repository.load(stream);
            fail("Expected a cycle in model.");
        } catch (Exception e) {
            // Expected
        }
        // repository.accept(visitor);
    }

    public void test24() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema24.xsd");
        try {
            repository.load(stream);
            fail("Expected a cycle in model.");
        } catch (Exception e) {
            // Expected
        }
        // repository.accept(visitor);
    }

    public void test25() throws Exception { // See TMDM-7235
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema25.xsd");
        repository.load(stream);
        ComplexTypeMetadata test = repository.getComplexType("Test");
        ComplexTypeMetadata containedType = (ComplexTypeMetadata) test.getField("documents").getType();
        List<ComplexTypeMetadata> sort = MetadataUtils.sortTypes(repository, Arrays.asList(test, containedType));
        assertEquals("Test", sort.get(0).getName());
    }

    public void test26() throws Exception { // See TMDM-7235
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema26.xsd");
        repository.load(stream);
        List<ComplexTypeMetadata> sortTypes = MetadataUtils.sortTypes(repository, MetadataUtils.SortType.LENIENT);
        boolean hasMetHexavia = false;
        boolean hasMetAdresse = false;
        for (ComplexTypeMetadata sortType : sortTypes) {
            if ("Hexavia".equals(sortType.getName())) {
                hasMetHexavia = true;
            }
            if ("Adresse".equals(sortType.getName())) {
                assertTrue(hasMetHexavia);
                hasMetAdresse = true;
            }
        }
        assertTrue(hasMetAdresse);
    }

    public void test27() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema27.xsd");
        repository.load(stream);

        ComplexTypeMetadata entityType = repository.getComplexType("A");
        assertNotNull(entityType);
        assertTrue(entityType.hasField("string20"));
        assertTrue(entityType.hasField("id"));

        TypeMetadata string20Field = entityType.getField("string20").getType();
        assertEquals("20", string20Field.getData(MetadataRepository.DATA_MAX_LENGTH));
        TypeMetadata string30Field = entityType.getField("id").getType();
        assertEquals("20", string30Field.getData(MetadataRepository.DATA_MAX_LENGTH));
    }

    public void test28() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema28.xsd");
        repository.load(stream);
        // Assert on composite key
        ComplexTypeMetadata entityType = repository.getComplexType("qq");
        assertNotNull(entityType);
        assertEquals(3, entityType.getKeyFields().size());
        String[] names = new String[3];
        int i = 0;
        for (FieldMetadata keyField : entityType.getKeyFields()) {
            names[i++] = keyField.getName();
        }
        // Assert on composite key (using copy).
        MetadataRepository copy = repository.copy();
        ComplexTypeMetadata entityTypeCopy = copy.getComplexType("qq");
        assertNotNull(entityTypeCopy);
        assertEquals(3, entityTypeCopy.getKeyFields().size());
        String[] copyNames = new String[3];
        i = 0;
        for (FieldMetadata keyField : entityTypeCopy.getKeyFields()) {
            copyNames[i++] = keyField.getName();
        }
        // Assert order is same
        for (int j = 0; j < names.length; j++) {
            assertEquals(names[j], copyNames[j]);
        }
    }

    public void test30() throws Exception {
        String[] keyNames = { "NumeroBdd", "BddSource", "NomApplication", "IdMDM" };
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema30.xsd");
        repository.load(stream);
        ComplexTypeMetadata entityType = repository.getComplexType("XrefAgence");
        assertNotNull(entityType);
        assertEquals(4, entityType.getKeyFields().size());
        int i = 0;
        for (FieldMetadata keyField : entityType.getKeyFields()) {
            assertEquals(keyNames[i++], keyField.getName());
        }
    }
}
