// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import org.springframework.util.StringUtils;

public class Messages {

    private final String baseName;

    private final ClassLoader loader;

    Messages(String baseName, ClassLoader loader) {
        this.baseName = baseName;
        this.loader = loader;
    }

    public String getMessage(String key, Object... args) {
        return getMessage(null, key, args);
    }

    public String getMessage(Locale locale, String key, Object... args) {
        try {
            StringBuffer pattern = new StringBuffer(MessagesFactory.getLocator().getBundle(baseName, locale, loader).getString(key));
            ModifyPatternAccordingToArgs(pattern, args);
            MessageFormat formatter = new MessageFormat(pattern.toString());
            return formatter.format(args);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public void ModifyPatternAccordingToArgs(StringBuffer pattern, Object... args) {
        if(args != null && args.length > 0){
            int index = 0;
            for(Object a : args){
                if(a != null && !StringUtils.isEmpty(a)){
                    pattern.append("{"+index+"}");  //$NON-NLS-1$//$NON-NLS-2$
                    if(index != args.length -1){
                        pattern.append(","); //$NON-NLS-1$
                    }
                }
                index++;
            }
        }
    }
    
}
