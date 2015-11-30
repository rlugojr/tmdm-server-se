package org.talend.mdm.webapp.browserecords.client.widget.inputfield;

import java.util.HashMap;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.widget.PortletConstants;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.model.FormatModel;
import org.talend.mdm.webapp.browserecords.client.util.Locale;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

public class FormatNumberField extends NumberField {

    BrowseRecordsServiceAsync service = (BrowseRecordsServiceAsync) Registry.get(BrowseRecords.BROWSERECORDS_SERVICE);

    private String formatPattern;

    private Number ojbectValue;

    private String diplayValue;

    private boolean validateFlag = true;

    private HashMap<String, String> userProperties;

    public FormatNumberField() {
        super();
        validateFlag = BrowseRecords.getSession().getAppHeader().isAutoValidate();
        setUserProperties(BrowseRecords.getSession().getAppHeader().getUserProperties());
    }

    public String getFormatPattern() {
        return formatPattern;
    }

    public void setFormatPattern(String formatPattern) {
        this.formatPattern = formatPattern;
    }

    public Number getOjbectValue() {
        return ojbectValue;
    }

    public void setOjbectValue(Number ojbectValue) {
        this.ojbectValue = ojbectValue;
    }

    public String getDiplayValue() {
        return diplayValue;
    }

    public void setDiplayValue(String diplayValue) {
        this.diplayValue = diplayValue;
    }

    @Override
    public boolean validateValue(String value) {
        if (value.equals(this.getDiplayValue())) {
            return true;
        } else {
            if (!validateFlag) {
                return true;
            }
            // validator should run after super rules
            Validator tv = validator;
            validator = null;
            if (!super.validateValue(value)) {
                validator = tv;
                return false;
            }
            validator = tv;
            if (value.length() < 1) { // if it's blank and textfield didn't flag it then
                // its valid it's valid
                this.setOjbectValue(null);
                return true;
            }

            String v = value;

            Number d = null;
            try {
                d = getPropertyEditor().convertStringValue(v);
            } catch (Exception e) {
                String error = ""; //$NON-NLS-1$
                if (getMessages().getNanText() == null) {
                    error = GXT.MESSAGES.numberField_nanText(v);
                } else {
                    error = Format.substitute(getMessages().getNanText(), v);
                }
                markInvalid(error);
                return false;
            }
            if (d.doubleValue() < super.getMinValue().doubleValue()) {
                String error = ""; //$NON-NLS-1$
                if (getMessages().getMinText() == null) {
                    error = GXT.MESSAGES.numberField_minText(super.getMinValue().doubleValue());
                } else {
                    error = Format.substitute(getMessages().getMinText(), super.getMinValue());
                }
                markInvalid(error);
                return false;
            }

            if (d.doubleValue() > super.getMaxValue().doubleValue()) {
                String error = ""; //$NON-NLS-1$
                if (getMessages().getMaxText() == null) {
                    error = GXT.MESSAGES.numberField_maxText(super.getMaxValue().doubleValue());
                } else {
                    error = Format.substitute(getMessages().getMaxText(), super.getMaxValue());
                }
                markInvalid(error);
                return false;
            }

            if (!super.getAllowNegative() && d.doubleValue() < 0) {
                markInvalid(getMessages().getNegativeText());
                return false;
            }

            this.setOjbectValue(d);

            if (validator != null) {
                String msg = validator.validate(this, value);
                if (msg != null) {
                    markInvalid(msg);
                    return false;
                }
            }

            if (formatPattern != null) {
                FormatModel model = new FormatModel(formatPattern, d, Locale.getLanguage());
                service.formatValue(model, new SessionAwareAsyncCallback<String>() {

                    @Override
                    public void onSuccess(String result) {
                        setDiplayValue(result);
                        setRawValue(result);
                    }
                });

            }

            return true;
        }
    }

    public void setValidateFlag(boolean validateFlag) {
        this.validateFlag = validateFlag;
    }

    @Override
    public void setRawValue(String value) {
        if (rendered) {
            if (isEditable()) {
                getInputEl().setValue(value == null ? "" : value); //$NON-NLS-1$
            } else {
                getInputEl().dom.setInnerText(value == null ? "" : value); //$NON-NLS-1$
            }
        }
    }

    @Override
    public String getRawValue() {
        String v = rendered ? getInputEl().getValue() : ""; //$NON-NLS-1$
        if (!isEditable()) {
            v = rendered ? getInputEl().dom.getInnerText() : ""; //$NON-NLS-1$
        }
        if (v == null || v.equals(emptyText)) {
            return ""; //$NON-NLS-1$
        }
        return v;
    }

    @Override
    protected void onRender(Element target, int index) {
        super.onRender(target, index);
        if (!isEditable()) {
            if (el() != null) {
                el().removeChildren();
                getElement().setAttribute("role", "presentation"); //$NON-NLS-1$//$NON-NLS-2$
                input = new El(DOM.createDiv());
                input.dom.setId(XDOM.getUniqueId());
                if (name != null && name.length() > 0) {
                    DOM.setElementAttribute(input.dom, "key", name); //$NON-NLS-1$
                }
                DOM.setElementAttribute(input.dom, "type", "text"); //$NON-NLS-1$//$NON-NLS-2$
                DOM.setElementAttribute(input.dom, "contenteditable", "false"); //$NON-NLS-1$//$NON-NLS-2$
                String elementStyle = "overflow: hidden; whiteSpace: nowrap;"; //$NON-NLS-1$
                if (getUserProperties() != null && getUserProperties().size() > 0) {
                    if (getUserProperties().containsKey(PortletConstants.KEY_MDM_READ_ONLY_FIELD_STYLE)) {
                        elementStyle = elementStyle + getUserProperties().get(PortletConstants.KEY_MDM_READ_ONLY_FIELD_STYLE);
                    }
                }
                DOM.setElementAttribute(input.dom, "style", elementStyle); //$NON-NLS-1$
                input.addStyleName(fieldStyle);
                input.addStyleName("x-form-text"); //$NON-NLS-1$
                el().appendChild(input.dom);
            }
        }
    }

    @Override
    public void disable() {
        if (GXT.isIE) {
            addStyleName(disabledStyle);
        } else {
            super.disable();
        }
        setEditable(false);
        if (input != null) {
            input.dom.setAttribute("contenteditable", "false"); //$NON-NLS-1$//$NON-NLS-2$
        }
    }

    @Override
    public void enable() {
        if (GXT.isIE) {
            removeStyleName(disabledStyle);
        } else {
            super.enable();
        }
        setEditable(true);
        if (input != null) {
            input.dom.setAttribute("contenteditable", "true"); //$NON-NLS-1$//$NON-NLS-2$
        }
    }

    public HashMap<String, String> getUserProperties() {
        return this.userProperties;
    }

    public void setUserProperties(HashMap<String, String> userProperties) {
        this.userProperties = userProperties;
    }
}
