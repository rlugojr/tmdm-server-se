// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation 
// Generated source version: 1.1.2

package com.amalto.webapp.util.webservices;


public class WSItemPKsByCriteriaResponseResults {
    protected long date;
    protected com.amalto.webapp.util.webservices.WSItemPK wsItemPK;
    protected java.lang.String taskId;
    
    public WSItemPKsByCriteriaResponseResults() {
    }
    
    public WSItemPKsByCriteriaResponseResults(long date, com.amalto.webapp.util.webservices.WSItemPK wsItemPK, java.lang.String taskId) {
        this.date = date;
        this.wsItemPK = wsItemPK;
        this.taskId = taskId;
    }
    
    public long getDate() {
        return date;
    }
    
    public void setDate(long date) {
        this.date = date;
    }
    
    public com.amalto.webapp.util.webservices.WSItemPK getWsItemPK() {
        return wsItemPK;
    }
    
    public void setWsItemPK(com.amalto.webapp.util.webservices.WSItemPK wsItemPK) {
        this.wsItemPK = wsItemPK;
    }
    
    public java.lang.String getTaskId() {
        return taskId;
    }
    
    public void setTaskId(java.lang.String taskId) {
        this.taskId = taskId;
    }
}