package org.talend.mdm.webapp.browserecords.client.widget.inputfield.celleditor;

import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsListPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.ForeignKeyField;

import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.Grid;

public class FKCellEditor extends CellEditor {

    public FKCellEditor(ForeignKeyField field) {
        super(field);
    }

    @Override
    public Object preProcessValue(Object value) {
        if (value == null) {
            return null;
        }
        Grid<ItemBean> grid = ItemsListPanel.getInstance().getGrid();

        ItemBean itemBean = grid.getSelectionModel().getSelectedItem();
        ForeignKeyBean fkBean = itemBean.getForeignkeyDesc((String) value);
        return fkBean;
    }

    @Override
    public Object postProcessValue(Object value) {
        if (value == null) {
            return value;
        }
        Grid<ItemBean> grid = ItemsListPanel.getInstance().getGrid();

        ItemBean itemBean = grid.getSelectionModel().getSelectedItem();
        ForeignKeyBean fkBean = (ForeignKeyBean) value;
        if (itemBean.getForeignkeyDesc(fkBean.getFullString()) == null) {
            itemBean.setForeignkeyDesc(fkBean.getFullString(), fkBean);
        }
        return fkBean.getFullString();
    }
}
