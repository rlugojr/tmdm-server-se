/*
 * Copyright (C) 2006-2012 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.datasource;

import java.io.InputStream;
import java.util.Map;

import junit.framework.TestCase;

public class DataSourceParsingTest extends TestCase {

    public void testInvalidParameters() {
        try {
            DataSourceFactory.getInstance().getDataSource(null, null, null);
            fail();
        } catch (Exception e) {
            // Expected
        }

        try {
            DataSourceFactory.getInstance().getDataSource("Test-1", null, null);
            fail();
        } catch (Exception e) {
            // Expected
        }

        try {
            DataSourceFactory.getInstance().getDataSource(null, "MDM", null);
            fail();
        } catch (Exception e) {
            // Expected
        }
    }

    public void testParsing() throws Exception {
        InputStream stream = DataSourceParsingTest.class.getResourceAsStream("datasources1.xml");
        DataSourceDefinition dataSourceDefinition = DataSourceFactory.getInstance().getDataSource(stream, "Test-0", "MDM", null);
        DataSource dataSource = dataSourceDefinition.getMaster();
        assertNotNull(dataSource);
        assertTrue(dataSource instanceof RDBMSDataSource);

        RDBMSDataSource rdbmsDataSource = (RDBMSDataSource) dataSource;
        assertTrue(rdbmsDataSource.hasInit());
        assertEquals("jdbc:mysql://10.42.150.15:3306/Test3", rdbmsDataSource.getConnectionURL());
        assertEquals("mdm_dev2", rdbmsDataSource.getDatabaseName());
        assertEquals(RDBMSDataSource.DataSourceDialect.MYSQL, rdbmsDataSource.getDialectName());
        assertEquals("com.mysql.jdbc.Driver", rdbmsDataSource.getDriverClassName());
        assertEquals("/var/lucene/indexes/DS2", rdbmsDataSource.getIndexDirectory());
        assertEquals("/var/cache/DS2", rdbmsDataSource.getCacheDirectory());
        assertEquals("Test-0", rdbmsDataSource.getName());
        assertEquals("toor", rdbmsDataSource.getPassword());
        assertEquals("root", rdbmsDataSource.getUserName());
        assertEquals(5, rdbmsDataSource.getConnectionPoolMinSize());
        assertEquals(50, rdbmsDataSource.getConnectionPoolMaxSize());
        assertEquals(RDBMSDataSource.SchemaGeneration.UPDATE, rdbmsDataSource.getSchemaGeneration());
        assertEquals("jdbc:mysql://10.42.150.15:3306/", rdbmsDataSource.getInitConnectionURL());
        assertEquals("root", rdbmsDataSource.getInitUserName());
        assertEquals("toor", rdbmsDataSource.getInitPassword());
    }

    public void testContainerChange1() throws Exception {
        InputStream stream = DataSourceParsingTest.class.getResourceAsStream("datasources1.xml");
        DataSourceDefinition dataSourceDefinition = DataSourceFactory.getInstance().getDataSource(stream, "Test-1", "MDM", null);
        DataSource dataSource = dataSourceDefinition.getMaster();
        assertNotNull(dataSource);
        assertTrue(dataSource instanceof RDBMSDataSource);

        RDBMSDataSource rdbmsDataSource = (RDBMSDataSource) dataSource;
        assertTrue(rdbmsDataSource.hasInit());
        assertEquals("jdbc:mysql://10.42.150.15:3306/MDM", rdbmsDataSource.getConnectionURL());
        assertEquals("MDM", rdbmsDataSource.getDatabaseName());
        assertEquals(0, rdbmsDataSource.getConnectionPoolMinSize());
        assertEquals(50, rdbmsDataSource.getConnectionPoolMaxSize());
        assertEquals(RDBMSDataSource.SchemaGeneration.UPDATE, rdbmsDataSource.getSchemaGeneration());
    }

    public void testContainerChange2() throws Exception {
        InputStream stream = DataSourceParsingTest.class.getResourceAsStream("datasources1.xml");
        DataSourceDefinition dataSourceDefinition = DataSourceFactory.getInstance().getDataSource(stream, "Test-2", "MDM", null);
        DataSource dataSource = dataSourceDefinition.getMaster();
        assertNotNull(dataSource);
        assertTrue(dataSource instanceof RDBMSDataSource);

        RDBMSDataSource rdbmsDataSource = (RDBMSDataSource) dataSource;
        assertTrue(rdbmsDataSource.hasInit());
        assertEquals("jdbc:mysql://10.42.150.15:3306/mdm_dev2", rdbmsDataSource.getConnectionURL());
        assertEquals("mdm_dev2", rdbmsDataSource.getDatabaseName());
        assertEquals(0, rdbmsDataSource.getConnectionPoolMinSize());
        assertEquals(0, rdbmsDataSource.getConnectionPoolMaxSize());
        assertEquals(RDBMSDataSource.SchemaGeneration.UPDATE, rdbmsDataSource.getSchemaGeneration());
    }

    public void testSchemaGeneration() throws Exception {
        InputStream stream = DataSourceParsingTest.class.getResourceAsStream("datasources1.xml");
        DataSourceDefinition dataSourceDefinition = DataSourceFactory.getInstance().getDataSource(stream, "Test-3", "MDM", null);
        DataSource dataSource = dataSourceDefinition.getMaster();
        assertNotNull(dataSource);
        assertTrue(dataSource instanceof RDBMSDataSource);

        RDBMSDataSource rdbmsDataSource = (RDBMSDataSource) dataSource;
        assertEquals(RDBMSDataSource.SchemaGeneration.VALIDATE, rdbmsDataSource.getSchemaGeneration());
    }

    public void testAdvancedProperties() throws Exception {
        InputStream stream = DataSourceParsingTest.class.getResourceAsStream("datasources1.xml");
        DataSourceDefinition dataSourceDefinition = DataSourceFactory.getInstance().getDataSource(stream, "Test-3", "MDM", null);
        DataSource dataSource = dataSourceDefinition.getMaster();
        assertNotNull(dataSource);
        assertTrue(dataSource instanceof RDBMSDataSource);

        RDBMSDataSource rdbmsDataSource = (RDBMSDataSource) dataSource;
        Map<String,String> advancedProperties = rdbmsDataSource.getAdvancedProperties();
        assertEquals(3, advancedProperties.size());
        assertEquals("value1", advancedProperties.get("property1"));
        assertEquals("value2", advancedProperties.get("property2"));
        assertEquals("value3", advancedProperties.get("property3"));
    }

    public void testCaseSensitiveConfiguration() throws Exception {
        InputStream stream = DataSourceParsingTest.class.getResourceAsStream("datasources1.xml");
        DataSourceDefinition dataSourceDefinition = DataSourceFactory.getInstance().getDataSource(stream, "Test-3", "MDM", null);
        DataSource dataSource = dataSourceDefinition.getMaster();
        assertNotNull(dataSource);
        assertTrue(dataSource instanceof RDBMSDataSource);

        RDBMSDataSource rdbmsDataSource = (RDBMSDataSource) dataSource;
        assertTrue(rdbmsDataSource.isCaseSensitiveSearch()); // Default is case sensitive search

        stream = DataSourceParsingTest.class.getResourceAsStream("datasources1.xml");
        dataSourceDefinition = DataSourceFactory.getInstance().getDataSource(stream, "Test-4", "MDM", null);
        dataSource = dataSourceDefinition.getMaster();
        assertNotNull(dataSource);
        assertTrue(dataSource instanceof RDBMSDataSource);

        rdbmsDataSource = (RDBMSDataSource) dataSource;
        assertFalse(rdbmsDataSource.isCaseSensitiveSearch());
    }
}
