package com.amalto.webapp.core.util;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.amalto.core.jobox.util.JobNotFoundException;
import com.amalto.core.save.SaveException;
import com.amalto.core.util.*;
import com.amalto.core.util.RoutingException;

public class XtentisWebPortTest extends TestCase {

    private final List<RuntimeException> exceptionList = new ArrayList<RuntimeException>();

    private final List<String> titleList = new ArrayList<String>();

    private final List<String> messageList = new ArrayList<String>();

    @Override
    public void setUp() throws Exception {
        exceptionList.add(new RuntimeException(new SchematronValidateException("SchematronValidateException"))); //$NON-NLS-1$
        exceptionList.add(new RuntimeException(new ValidateException("ValidateException"))); //$NON-NLS-1$
        exceptionList.add(new RuntimeException(new CVCException("CVCException"))); //$NON-NLS-1$
        exceptionList.add(new RuntimeException(new JobNotFoundException("job", "0.1"))); //$NON-NLS-1$ //$NON-NLS-2$       
        exceptionList.add(new RuntimeException(new BeforeSavingFormatException("BeforeSavingFormatException"))); //$NON-NLS-1$ 
        exceptionList.add(new RuntimeException(new RoutingException("RoutingException"))); //$NON-NLS-1$
        exceptionList.add(new RuntimeException(new OutputReportMissingException("OutputReportMissingException"))); //$NON-NLS-1$
        exceptionList.add(new RuntimeException(new com.amalto.core.jobox.util.JoboxException("JoboxException"))); //$NON-NLS-1$
        BeforeSavingErrorException beforeSavingException = new BeforeSavingErrorException("Error beforesaving"); //$NON-NLS-1$
        SaveException saveException = new SaveException("beforesaing xml", beforeSavingException); //$NON-NLS-1$
        exceptionList.add(saveException);

        titleList.add(XtentisWebPort.VALIDATE_EXCEPTION_MESSAGE);
        titleList.add(XtentisWebPort.VALIDATE_EXCEPTION_MESSAGE);
        titleList.add(XtentisWebPort.CVC_EXCEPTION_MESSAGE);
        titleList.add(XtentisWebPort.JOB_NOT_FOUND_EXCEPTION_MESSAGE);
        titleList.add(XtentisWebPort.BEFORE_SAVING_FORMAT_ERROR_MESSAGE);
        titleList.add(XtentisWebPort.ROUTING_ERROR_MESSAGE);
        titleList.add(XtentisWebPort.OUTPUT_REPORT_MISSING_ERROR_MESSAGE);
        titleList.add(XtentisWebPort.JOBOX_EXCEPTION_MESSAGE);
        titleList.add(XtentisWebPort.SAVE_PROCESS_BEFORE_SAVING_FAILURE_MESSAGE);

        messageList.add("SchematronValidateException"); //$NON-NLS-1$
        messageList.add("ValidateException"); //$NON-NLS-1$
        messageList.add("CVCException"); //$NON-NLS-1$
        messageList.add("job 0.1"); //$NON-NLS-1$
        messageList.add("BeforeSavingFormatException"); //$NON-NLS-1$
        messageList.add("RoutingException"); //$NON-NLS-1$
        messageList.add("OutputReportMissingException"); //$NON-NLS-1$
        messageList.add("JoboxException"); //$NON-NLS-1$
        messageList.add("Error beforesaving"); //$NON-NLS-1$  
    }

    public void testHandleException() {
        // testing catch Exception
        for (int i = 0; i < exceptionList.size(); i++) {
            RemoteException processedException = XtentisWebPort.handleException(exceptionList.get(i),
                    XtentisWebPort.DEFAULT_REMOTE_ERROR_MESSAGE);
            assertTrue(processedException.getCause() instanceof CoreException);
            CoreException webCoreException = (CoreException) processedException.getCause();
            assertEquals(titleList.get(i), webCoreException.getTitle());
            if (i == 0) {
                assertEquals(messageList.get(i), webCoreException.getLocalizedMessage());
                assertEquals(CoreException.INFO, webCoreException.getLevel());
            } else {
                assertEquals(messageList.get(i), webCoreException.getCause().getLocalizedMessage());
                assertEquals(CoreException.Error, webCoreException.getLevel());
            }
        }
    }
}
