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
package org.talend.mdm.webapp.welcomeportal.client;

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
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface WelcomePortalServiceAsync {

    void isHiddenLicense(AsyncCallback<Boolean> callback);

    void isHiddenWorkFlowTask(AsyncCallback<Boolean> callback);
    
    void isHiddenDSCTask(AsyncCallback<Boolean> callback);

    void getAlertMsg(String language, AsyncCallback<String> callback);

    void getWorkflowTaskMsg(AsyncCallback<Integer> callback);
    
    void getDSCTaskMsg(AsyncCallback<Map<String, Integer>> callback);

    void getStandaloneProcess(String language, AsyncCallback<List<String>> callback);

    void runProcess(String transformerPK, AsyncCallback<String> callback);

    void isExpired(String language, AsyncCallback<Boolean> callback);

    void isEnterpriseVersion(AsyncCallback<Boolean> callback);

    void getMenuLabel(String language, String id, AsyncCallback<String> callback);

    void getLicenseWarning(String language, AsyncCallback<String> callback);

    void getCurrentDataContainer(AsyncCallback<String> callback);

    void getWelcomePortletConfig(AsyncCallback<Map<Boolean, Integer>> callback);

}
