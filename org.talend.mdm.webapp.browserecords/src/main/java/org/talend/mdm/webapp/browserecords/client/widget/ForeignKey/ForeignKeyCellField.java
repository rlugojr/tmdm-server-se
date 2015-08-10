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
package org.talend.mdm.webapp.browserecords.client.widget.ForeignKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.form.Field;

public class ForeignKeyCellField extends ForeignKeyField {

    private String foreignKeyFilter;

    private Map<Integer, Field<?>> targetFields;

    public ForeignKeyCellField(ForeignKeyField foreignKeyField, String foreignKeyFilter) {
        super(foreignKeyField.getForeignKeyPath(), foreignKeyField.getForeignKeyInfo(), foreignKeyField.getCurrentPath());
        this.foreignKeyFilter = foreignKeyFilter;
    }

    public void setTargetField(Map<Integer, Field<?>> targetFields) {
        this.targetFields = targetFields;
    }

    @Override
    public String parseForeignKeyFilter() {
        if (foreignKeyFilter != null) {
            String[] criterias = org.talend.mdm.webapp.base.shared.util.CommonUtil
                    .getCriteriasByForeignKeyFilter(foreignKeyFilter);
            List<Map<String, String>> conditions = new ArrayList<Map<String, String>>();
            for (int i = 0; i < criterias.length; i++) {
                String criteria = criterias[i];
                Map<String, String> conditionMap = org.talend.mdm.webapp.base.shared.util.CommonUtil
                        .buildConditionByCriteria(criteria);
                String filterValue = conditionMap.get("Value"); //$NON-NLS-1$
                if (filterValue == null) {
                    continue;
                }

                filterValue = org.talend.mdm.webapp.base.shared.util.CommonUtil.unescapeXml(filterValue);
                if (org.talend.mdm.webapp.base.shared.util.CommonUtil.isFilterValue(filterValue)) {
                    filterValue = filterValue.substring(1, filterValue.length() - 1);
                } else {
                    if (targetFields != null && targetFields.get(i) != null) {
                        Field<?> targetField = targetFields.get(i);
                        if (targetField.getValue() != null) {
                            filterValue = org.talend.mdm.webapp.base.shared.util.CommonUtil.unwrapFkValue(targetField.getValue()
                                    .toString());
                        }
                    }
                }
                conditionMap.put("Value", filterValue); //$NON-NLS-1$
                conditions.add(conditionMap);
            }
            return org.talend.mdm.webapp.base.shared.util.CommonUtil.buildForeignKeyFilterByConditions(conditions);
        } else {
            return ""; //$NON-NLS-1$
        }
    }
}
