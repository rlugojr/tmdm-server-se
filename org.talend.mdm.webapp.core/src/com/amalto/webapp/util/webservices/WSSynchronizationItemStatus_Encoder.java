// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation ��1.1.2_01������� R40��
// Generated source version: 1.1.2

package com.amalto.webapp.util.webservices;


import com.sun.xml.rpc.encoding.simpletype.*;
import javax.xml.namespace.QName;
import com.sun.xml.rpc.streaming.*;

public class WSSynchronizationItemStatus_Encoder extends SimpleTypeEncoderBase {
    
    private static final SimpleTypeEncoder encoder = XSDStringEncoder.getInstance();
    private static final WSSynchronizationItemStatus_Encoder instance = new WSSynchronizationItemStatus_Encoder();
    
    private WSSynchronizationItemStatus_Encoder() {
    }
    
    public static SimpleTypeEncoder getInstance() {
        return instance;
    }
    
    public String objectToString(Object obj, XMLWriter writer) throws Exception {
        java.lang.String value = ((WSSynchronizationItemStatus)obj).getValue();
        return encoder.objectToString(value, writer);
    }
    
    public Object stringToObject(String str, XMLReader reader) throws Exception {
        return WSSynchronizationItemStatus.fromValue((java.lang.String)encoder.stringToObject(str, reader));
    }
    
}
