/*
 * Copyright (C) 2006-2014 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.task.staging;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@XmlRootElement(name = "staging")
@ApiModel(value="staging", description="Overview of a ...")
public class StagingContainerSummary {

    private int totalRecord = 0;

    private int waitingForValidation = 0;

    private int validRecords = 0;

    private int invalidRecords = 0;

    private String dataContainer;

    private String dataModel;

    public StagingContainerSummary() {
    }

    @ApiModelProperty(name="total_records")
    @XmlElement(name = "total_records")
    public int getTotalRecord() {
        return totalRecord;
    }

    public void setTotalRecord(int totalRecord) {
        this.totalRecord = totalRecord;
    }

    @XmlElement(name = "waiting_validation_records")
    @ApiModelProperty(name="waiting_validation_records")
    public int getWaitingForValidation() {
        return waitingForValidation;
    }

    public void setWaitingForValidation(int waitingForValidation) {
        this.waitingForValidation = waitingForValidation;
    }

    @XmlElement(name = "valid_records")
    public int getValidRecords() {
        return validRecords;
    }

    public void setValidRecords(int validRecords) {
        this.validRecords = validRecords;
    }

    @XmlElement(name = "invalid_records")
    @ApiModelProperty(name="invalid_records")
    public int getInvalidRecords() {
        return invalidRecords;
    }

    public void setInvalidRecords(int invalidRecords) {
        this.invalidRecords = invalidRecords;
    }

    public void setDataContainer(String dataContainer) {
        this.dataContainer = dataContainer;
    }

    @XmlElement(name = "data_container")
    @ApiModelProperty(name="data_container")
    public String getDataContainer() {
        return dataContainer;
    }

    public void setDataModel(String dataModel) {
        this.dataModel = dataModel;
    }

    @XmlElement(name = "data_model")
    @ApiModelProperty(name="data_container")
    public String getDataModel() {
        return dataModel;
    }
}
