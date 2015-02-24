/*
 * Copyright (C) 2006-2014 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.task.staging;

import java.io.InputStream;
import java.io.StringReader;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.amalto.core.storage.task.ConfigurableFilter;
import com.amalto.core.storage.task.DefaultFilter;
import com.amalto.core.storage.task.Filter;
import com.amalto.core.util.Util;

@RestController
@RequestMapping(StagingTaskService.TASKS)
public class StagingTaskService {

    public static final String TASKS = "/tasks/staging"; //$NON-NLS-1$

    private static final Logger LOGGER = Logger.getLogger(StagingTaskService.class);

    private final StagingTaskServiceDelegate delegate = new DefaultStagingTaskService();

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML)
    public StagingContainerSummary getContainerSummary() {
        return delegate.getContainerSummary();
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String startValidation() {
        return delegate.startValidation();
    }

    @RequestMapping(value = "{container}/", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML)
    public StagingContainerSummary getContainerSummary(@PathVariable("container") String dataContainer,
            @RequestParam("model") String dataModel) {
        return delegate.getContainerSummary(dataContainer, dataModel);
    }

    @RequestMapping(value = "{container}/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_XML)
    public String startValidation(@PathVariable("container") String dataContainer, @RequestParam("model") String dataModel,
            InputStream body) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Filter filter;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            String content = IOUtils.toString(body);
            if (!content.isEmpty()) {
                Document doc = builder.parse(new InputSource(new StringReader(content)));
                filter = new ConfigurableFilter(doc);
            } else {
                filter = DefaultFilter.INSTANCE;
            }
        } catch (Exception e) {
            filter = DefaultFilter.INSTANCE;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Ignored parse error for staging filter: ", e);
            }
        }
        return delegate.startValidation(dataContainer, dataModel, filter);
    }

    @RequestMapping(value = "{container}/execs", method = RequestMethod.GET, produces = { MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON })
    public List<String> listCompletedTaskExecutions(@PathVariable("container") String dataContainer,
            @RequestParam("before") String beforeDate, @RequestParam(value = "start", defaultValue = "1") int start,
            @RequestParam(value = "size", defaultValue = "-1") int size) {
        return SerializableList.create(delegate.listCompletedExecutions(dataContainer, beforeDate, start, size), "executions",
                "execution");
    }

    @RequestMapping(value = "{container}/execs/count", method = RequestMethod.GET, produces = { MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON })
    public int countCompletedTaskExecutions(@PathVariable("container") String dataContainer,
            @RequestParam("before") String beforeDate) {
        return delegate.listCompletedExecutions(dataContainer, beforeDate, 1, -1).size();
    }

    @RequestMapping(value = "{container}/execs/current/", method = RequestMethod.GET, produces = { MediaType.APPLICATION_XML,
            MediaType.APPLICATION_JSON })
    public ExecutionStatistics getCurrentExecutionStats(@PathVariable("container") String dataContainer,
            @RequestParam("model") String dataModel) {
        return delegate.getCurrentExecutionStats(dataContainer, dataModel);
    }

    @RequestMapping(value = "{container}/execs/current/", method = RequestMethod.DELETE)
    public void cancelCurrentExecution(@PathVariable("container") String dataContainer, @RequestParam("model") String dataModel) {
        delegate.cancelCurrentExecution(dataContainer, dataModel);
    }

    @RequestMapping(value = "{container}/execs/{executionId}/", method = RequestMethod.GET)
    public ExecutionStatistics getExecutionStats(@PathVariable("container") String dataContainer,
            @RequestParam("model") String dataModel, @PathVariable("executionId") String executionId) {
        return delegate.getExecutionStats(dataContainer, dataModel, executionId);
    }

    @RequestMapping(value = "{container}/hasStaging", method = RequestMethod.GET)
    public String isSupportStaging(@PathVariable("container") String dataContainer) {
        try {
            return String.valueOf(Util.getXmlServerCtrlLocal().supportStaging(dataContainer));
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Could not confirm staging support.", e); //$NON-NLS-1$
            }
            throw new RuntimeException("Could not confirm staging support.", e); //$NON-NLS-1$
        }
    }
}
