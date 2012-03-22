/*
 * Copyright (C) 2006-2012 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.save;

import com.amalto.core.ejb.ItemPOJO;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.metadata.MetadataRepository;
import com.amalto.core.save.context.DocumentSaver;
import com.amalto.core.save.context.SaverSource;
import com.amalto.core.util.OutputReport;
import com.amalto.core.util.XtentisException;
import junit.framework.TestCase;

import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

public class DocumentSaveTest extends TestCase {

    public void testCreate() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));

        SaverSource source = new TestSaverSource(repository, false, "test1_original.xml");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        session.end(new MockCommitter());

    }

    public void testUpdate() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));

        SaverSource source = new TestSaverSource(repository, true, "test1_original.xml");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        session.end(new MockCommitter());

    }

    public void testSystemUpdate() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));

        SaverSource source = new TestSaverSource(repository, true, "test1_original.xml");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
        DocumentSaverContext context = session.getContextFactory().create("UpdateReport", "DStar", "Source", recordXml, true, false);
        DocumentSaver saver = context.createSaver();
        try {
            saver.save(session, context);
            fail("Expected fail due to TMDM-3606");
        } catch (Exception e) {
            // Expected
        }
        session.end(new MockCommitter());
    }

    public void testProductUpdate() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));

        SaverSource source = new TestSaverSource(repository, true, "test2_original.xml");

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test2.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, false);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        session.end(new MockCommitter());
    }

    public void testBeforeSavingWithAlterRecord() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));

        //
        boolean isOK = true;
        boolean newOutput = true;
        SaverSource source = new AlterRecordTestSaverSource(repository, isOK, newOutput);

        SaverSession session = SaverSession.newSession(source);
        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true);
        DocumentSaver saver = context.createSaver();
        saver.save(session, context);
        session.end(new MockCommitter());

        //
        isOK = false;
        newOutput = true;
        source = new AlterRecordTestSaverSource(repository, isOK, newOutput);

        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
        context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true);
        saver = context.createSaver();
        try {
            saver.save(session, context);
            fail("Expected an exception.");
        } catch (Exception e) {
            // Expected
        }
        session.end(new MockCommitter());

        //
        isOK = false;
        newOutput = false;
        source = new AlterRecordTestSaverSource(repository, isOK, newOutput);

        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
        context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true);
        saver = context.createSaver();
        try {
            saver.save(session, context);
            fail("Expected an exception.");
        } catch (Exception e) {
            // Expected
        }
        session.end(new MockCommitter());

        //
        isOK = true;
        newOutput = false;
        source = new AlterRecordTestSaverSource(repository, isOK, newOutput);

        session = SaverSession.newSession(source);
        recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
        context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, true);
        saver = context.createSaver();
        saver.save(session, context);
        session.end(new MockCommitter());
    }

    public void testCreatePerformance() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));

        SaverSource source = new TestSaverSource(repository, false, "test1_original.xml");
        {
            SaverSession session = SaverSession.newSession(source);
            {
                for (int i = 0; i < 10; i++) {
                    InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
                    DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, false);
                    DocumentSaver saver = context.createSaver();
                    saver.save(session, context);
                }
            }
            session.end(new MockCommitter());
        }

        long saveTime = System.currentTimeMillis();
        long max = 0;
        long min = Long.MAX_VALUE;
        {
            SaverSession session = SaverSession.newSession(source);
            {
                for (int i = 0; i < 200; i++) {
                    long singleExecTime = System.currentTimeMillis();
                    {
                        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
                        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, false);
                        DocumentSaver saver = context.createSaver();
                        saver.save(session, context);
                    }
                    singleExecTime = (System.currentTimeMillis() - singleExecTime);
                    if (singleExecTime > max) {
                        max = singleExecTime;
                    }
                    if (singleExecTime < min) {
                        min = singleExecTime;
                    }
                }
            }
            session.end(new MockCommitter());
        }
        System.out.println("Time (mean): " + (System.currentTimeMillis() - saveTime) / 200f + " ms.");
        System.out.println("Time (min): " + min);
        System.out.println("Time (max): " + max);
    }


    public void testUpdatePerformance() throws Exception {
        final MetadataRepository repository = new MetadataRepository();
        repository.load(DocumentSaveTest.class.getResourceAsStream("metadata1.xsd"));

        SaverSource source = new TestSaverSource(repository, true, "test1_original.xml");
        {
            SaverSession session = SaverSession.newSession(source);
            {
                for (int i = 0; i < 10; i++) {
                    InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
                    DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, false);
                    DocumentSaver saver = context.createSaver();
                    saver.save(session, context);
                }
            }
            session.end(new MockCommitter());
        }

        long saveTime = System.currentTimeMillis();
        long max = 0;
        long min = Long.MAX_VALUE;
        {
            SaverSession session = SaverSession.newSession(source);
            {
                for (int i = 0; i < 200; i++) {
                    long singleExecTime = System.currentTimeMillis();
                    {
                        InputStream recordXml = DocumentSaveTest.class.getResourceAsStream("test1.xml");
                        DocumentSaverContext context = session.getContextFactory().create("MDM", "DStar", "Source", recordXml, true, false);
                        DocumentSaver saver = context.createSaver();
                        saver.save(session, context);
                    }
                    singleExecTime = (System.currentTimeMillis() - singleExecTime);
                    if (singleExecTime > max) {
                        max = singleExecTime;
                    }
                    if (singleExecTime < min) {
                        min = singleExecTime;
                    }
                }
            }
            session.end(new MockCommitter());
        }
        System.out.println("Time (mean): " + (System.currentTimeMillis() - saveTime) / 200f + " ms.");
        System.out.println("Time (min): " + min);
        System.out.println("Time (max): " + max);
    }

    private static class MockCommitter implements SaverSession.Committer {

        private static final boolean DEBUG = false;

        public void begin(String dataCluster) {
            if (DEBUG) {
                System.out.println("Start on '" + dataCluster + "'");
            }
        }

        public void commit(String dataCluster) {
            if (DEBUG) {
                System.out.println("Commit on '" + dataCluster + "'");
            }
        }

        public void save(ItemPOJO item, String revisionId) {
            if (DEBUG) {
                try {
                    System.out.println(item.getProjectionAsString());
                } catch (XtentisException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static class TestSaverSource implements SaverSource {

        private final MetadataRepository repository;

        private final boolean exist;

        private final String originalDocumentFileName;

        private MetadataRepository updateReportRepository;

        public TestSaverSource(MetadataRepository repository, boolean exist, String originalDocumentFileName) {
            this.repository = repository;
            this.exist = exist;
            this.originalDocumentFileName = originalDocumentFileName;
        }

        public InputStream get(String dataClusterName, String typeName, String revisionId, String[] key) {
            return DocumentSaveTest.class.getResourceAsStream(originalDocumentFileName);
        }

        public boolean exist(String dataCluster, String typeName, String revisionId, String[] key) {
            return exist;
        }

        public MetadataRepository getMetadataRepository(String dataModelName) {
            if ("UpdateReport".equals(dataModelName)) {
                if (updateReportRepository == null) {
                    updateReportRepository = new MetadataRepository();
                    updateReportRepository.load(DocumentSaveTest.class.getResourceAsStream("updateReport.xsd"));
                }
                return updateReportRepository;
            }
            return repository;
        }

        public InputStream getSchema(String dataModelName) {
            return DocumentSaveTest.class.getResourceAsStream("metadata1.xsd");
        }

        public String getUniverse() {
            return "Universe";
        }

        public OutputReport invokeBeforeSaving(DocumentSaverContext context, MutableDocument updateReportDocument) {
            // Nothing to do
            return null;
        }

        public Set<String> getCurrentUserRoles() {
            return Collections.emptySet();
        }

        public String getUserName() {
            return "User";
        }

        public boolean existCluster(String revisionID, String dataClusterName) {
            return true;
        }

        public String getConceptRevisionID(String typeName) {
            return "HEAD";
        }

        public void resetLocalUsers() {
        }

        public void initAutoIncrement() {
        }
    }

    private static class AlterRecordTestSaverSource extends DocumentSaveTest.TestSaverSource {

        private final boolean OK;

        private final boolean newOutput;

        public AlterRecordTestSaverSource(MetadataRepository repository, boolean OK, boolean newOutput) {
            super(repository, false, "test1_original.xml");
            this.OK = OK;
            this.newOutput = newOutput;
        }

        public OutputReport invokeBeforeSaving(DocumentSaverContext context, MutableDocument updateReportDocument) {
            String message = "<report><message type=\"info\">change the value successfully!</message></report>";
            if (!OK) {
                message = "<report><message type=\"error\">change the value failed!</message></report>";
            }
            String item = null;
            OutputReport report = new OutputReport(message, item);

            if (newOutput) {
                item = "<exchange><item>" + "<Agency><Id>1</Id><Name>beforeSaving_Agency</Name></Agency></item></exchange>";
                report.setItem(item);
            }
            return report;
        }
    }
}
