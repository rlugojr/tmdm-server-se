// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation ��1.1.2_01������� R40��
// Generated source version: 1.1.2

package com.amalto.webapp.util.webservices;


public class WSTransformerPluginV2VariableDescriptor {
    protected java.lang.String variableName;
    protected boolean mandatory;
    protected java.lang.String description;
    protected java.lang.String[] contentTypesRegex;
    protected java.lang.String[] possibleValuesRegex;
    
    public WSTransformerPluginV2VariableDescriptor() {
    }
    
    public WSTransformerPluginV2VariableDescriptor(java.lang.String variableName, boolean mandatory, java.lang.String description, java.lang.String[] contentTypesRegex, java.lang.String[] possibleValuesRegex) {
        this.variableName = variableName;
        this.mandatory = mandatory;
        this.description = description;
        this.contentTypesRegex = contentTypesRegex;
        this.possibleValuesRegex = possibleValuesRegex;
    }
    
    public java.lang.String getVariableName() {
        return variableName;
    }
    
    public void setVariableName(java.lang.String variableName) {
        this.variableName = variableName;
    }
    
    public boolean isMandatory() {
        return mandatory;
    }
    
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }
    
    public java.lang.String getDescription() {
        return description;
    }
    
    public void setDescription(java.lang.String description) {
        this.description = description;
    }
    
    public java.lang.String[] getContentTypesRegex() {
        return contentTypesRegex;
    }
    
    public void setContentTypesRegex(java.lang.String[] contentTypesRegex) {
        this.contentTypesRegex = contentTypesRegex;
    }
    
    public java.lang.String[] getPossibleValuesRegex() {
        return possibleValuesRegex;
    }
    
    public void setPossibleValuesRegex(java.lang.String[] possibleValuesRegex) {
        this.possibleValuesRegex = possibleValuesRegex;
    }
}
