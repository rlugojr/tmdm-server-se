/*
 * Copyright (C) 2006-2015 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.bulkload.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.apache.commons.lang.math.RandomUtils;

/**
 *
 */
public class BulkloadClientTest extends TestCase {

    public void testClient() throws Exception {
        String serverURL = "http://localhost:8080/datamanager/loadServlet";

        boolean isServerRunning = isServerRunning(serverURL);
        if (isServerRunning) {
            BulkloadClient client = new BulkloadClient(serverURL, "admin", "talend", null, "Order", "Country", "Order");
            client.setOptions(new BulkloadOptions());
            InputStream bin = BulkloadClientTest.class.getResourceAsStream("test.xml");
            client.load(bin);
        }
    }

    private static boolean isServerRunning(String serverURL) {
        boolean isServerRunning = false;
        try {
            URL testURL = new URL(serverURL);
            URLConnection urlConnection = testURL.openConnection();
            urlConnection.connect();
            isServerRunning = true;
        } catch (IOException e) {
            System.out.println("Server is not running on '" + serverURL + "', skip this test.");
        }
        return isServerRunning;
    }

    public void testPerformance() throws Exception {

        String serverURL = "http://localhost:8080/datamanager/loadServlet";
        boolean isServerRunning = isServerRunning(serverURL);
        if (isServerRunning) {
            BulkloadClient client = new BulkloadClient(serverURL, "admin", "talend", null, "Order", "Country", "Order");
            client.setOptions(new BulkloadOptions());

            String xml = "<Country><isoCode>zh1</isoCode><label>china</label><Continent>Asia</Continent></Country>";
            int num = 1000;
            int gap_num = 200;
            int gap = num / gap_num;
            for (int i = 0; i < gap; i++) {
                StringBuffer sb = new StringBuffer();
                for (int j = 0; j < gap_num; j++) {
                    int n = gap_num * i + j;
                    xml = "<Country><isoCode>zh" + n + "</isoCode><label>china</label><Continent>Asia</Continent></Country>\n";
                    sb.append(xml);
                }
                InputStreamMerger manager = client.load();

                // InputStream bin = BulkloadClientTest.class.getResourceAsStream("test.xml");
                InputStream bin = new ByteArrayInputStream(sb.toString().getBytes("utf-8"));
                manager.push(bin);
                manager.close();
                // client.load(bin);
            }
        }
    }

    public void testInterruptedBulkLoad() throws Exception {
        String serverURL = "http://localhost:8080/datamanager/loadServlet";
        boolean isServerRunning = isServerRunning(serverURL);
        if (isServerRunning) {
            final InterruptedTestResult result = new InterruptedTestResult();
            BulkloadClient client = new BulkloadClient(serverURL, "admin", "talend", null, "Product", "Product", "Product");
            client.setOptions(new BulkloadOptions());

            int count = 10;
            InputStream bin = new InputStream() {

                @Override
                public synchronized int read() throws IOException {
                    try {
                        Thread.sleep(RandomUtils.nextInt(5000));
                        synchronized (result) {
                            result.setSuccess(true);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return -1;
                }
            };
            for (int i = 0; i < count; i++) {
                InputStreamMerger manager = client.load();
                manager.push(bin);
                manager.close();
            }

            // client.waitForEndOfQueue();
            synchronized (result) {
                assertEquals(count, result.getSuccessCount());
                assertTrue(result.isSuccess());
            }
        }
    }

    public void testInterruptedBulkLoadOrder() throws Exception {
        final long waitTime = 500;
        String serverURL = "http://localhost:8180/service/bulkload";
        boolean isServerRunning = isServerRunning(serverURL);
        if (isServerRunning) {
            BulkloadClient client = new BulkloadClient(serverURL, "admin", "talend", null, "Product", "Product", "Product");
            client.setOptions(new BulkloadOptions());
            int count = 5;
            for (int i = 0; i < count; i++) {
                long startTime = System.currentTimeMillis();
                InputStreamMerger manager = client.load();
                synchronized (manager) {
                    InputStream bin = new InputStream() {

                        @Override
                        public synchronized int read() throws IOException {
                            try {
                                Thread.sleep(waitTime);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            return -1;
                        }
                    };
                    manager.push(bin);
                    manager.close();
                    while (!manager.isAlreadyProcessed()) {
                        manager.wait(100);
                    }
                    long endTime = System.currentTimeMillis();
                    assertEquals(true, (endTime - startTime) > waitTime);
                    assertEquals(true, manager.isAlreadyProcessed());
                    manager.notify();
                }
            }
        }
    }

    class InterruptedTestResult {

        boolean success = false;

        AtomicInteger successCount = new AtomicInteger();

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            successCount.incrementAndGet();
            this.success = success;
        }

        public int getSuccessCount() {
            return successCount.get();
        }
    }
}
