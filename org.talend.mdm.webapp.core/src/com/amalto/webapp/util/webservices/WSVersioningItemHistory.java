// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation ��1.1.2_01������� R40��
// Generated source version: 1.1.2

package com.amalto.webapp.util.webservices;


public class WSVersioningItemHistory {
    protected com.amalto.webapp.util.webservices.WSItemPK wsItemPK;
    protected com.amalto.webapp.util.webservices.WSVersioningHistoryEntry[] wsHistoryEntries;
    
    public WSVersioningItemHistory() {
    }
    
    public WSVersioningItemHistory(com.amalto.webapp.util.webservices.WSItemPK wsItemPK, com.amalto.webapp.util.webservices.WSVersioningHistoryEntry[] wsHistoryEntries) {
        this.wsItemPK = wsItemPK;
        this.wsHistoryEntries = wsHistoryEntries;
    }
    
    public com.amalto.webapp.util.webservices.WSItemPK getWsItemPK() {
        return wsItemPK;
    }
    
    public void setWsItemPK(com.amalto.webapp.util.webservices.WSItemPK wsItemPK) {
        this.wsItemPK = wsItemPK;
    }
    
    public com.amalto.webapp.util.webservices.WSVersioningHistoryEntry[] getWsHistoryEntries() {
        return wsHistoryEntries;
    }
    
    public void setWsHistoryEntries(com.amalto.webapp.util.webservices.WSVersioningHistoryEntry[] wsHistoryEntries) {
        this.wsHistoryEntries = wsHistoryEntries;
    }
}
