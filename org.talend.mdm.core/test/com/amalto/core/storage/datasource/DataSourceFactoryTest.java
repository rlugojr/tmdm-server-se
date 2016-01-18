package com.amalto.core.storage.datasource;

import java.io.File;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.Test;

import junit.framework.TestCase;

@SuppressWarnings({ "static-access", "nls" })
public class DataSourceFactoryTest extends TestCase {

    DataSourceFactory factory = new DataSourceFactory();

    String dataSourcesLocation = getClass().getResource("datasources_testEncrypt.xml").getFile();

    @Test
    public void testEncyptDataSource() throws Exception {
        factory.encyptDataSource(dataSourcesLocation, "PostgreSQL-Default");
        
        File file = new File(dataSourcesLocation);
        XMLConfiguration config = new XMLConfiguration();
        config.setDelimiterParsingDisabled(true);
        config.load(file);

        HierarchicalConfiguration sub = config.configurationAt("datasource(0)");
        String password = sub.getString("master.rdbms-configuration.connection-password");
        assertEquals("sa", password);
        password = sub.getString("master.rdbms-configuration.init.connection-password");
        assertNull(password);

        sub = config.configurationAt("datasource(1)");
        password = sub.getString("master.rdbms-configuration.connection-password");
        assertEquals("+WNho+eyvY2IdYENFaoKIA==,Encrypt", password);
        password = sub.getString("master.rdbms-configuration.init.connection-password");
        assertEquals("+WNho+eyvY2IdYENFaoKIA==,Encrypt", password);
    }
}
