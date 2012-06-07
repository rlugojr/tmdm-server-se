// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.webapp.browserecords.client.widget.treedetail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.model.ColumnTreeLayoutModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatTextField;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetail.CountMapItem;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetail.DynamicTreeItem;
import org.talend.mdm.webapp.browserecords.shared.ComplexTypeModel;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public class IncrementalBuildTree implements IncrementalCommand {

	private TreeDetail treeDetail;
	
	private ItemNodeModel itemNode;
	
	private List<ModelData> itemNodeChildren;
	
	private ViewBean viewBean;
	
	private Map<String, TypeModel> metaDataTypes;
	
	private boolean withDefaultValue;
	
	private Map<TypeModel, List<ItemNodeModel>> foreighKeyMap = new HashMap<TypeModel, List<ItemNodeModel>>();
	
	private String operation;
	
	private DynamicTreeItem item;
	
	private HashMap<CountMapItem, Integer> occurMap;
	
	int level = 0;
	
	private int index = 0;
	
	private int itemWidth = 0;
	private int offset = 0;
	
	public static final int GROUP_SIZE = 30;
	
	public int getChildCount(){
		return itemNode.getChildCount();
	}
	
	public IncrementalBuildTree(TreeDetail treeDetail, ItemNodeModel itemNode, ViewBean viewBean, boolean withDefaultValue, 
			String operation, DynamicTreeItem item, HashMap<CountMapItem, Integer> occurMap){
		this.treeDetail = treeDetail;
		this.itemNode = itemNode;
		this.withDefaultValue = withDefaultValue;
		this.operation = operation;
		this.item = item;
		this.occurMap = occurMap;
		itemNodeChildren = itemNode.getChildren();
		this.viewBean = viewBean;
		metaDataTypes = viewBean.getBindingEntityModel().getMetaDataTypes();
		level = getLevel();
		
		ColumnTreeLayoutModel columnLayoutModel = viewBean.getColumnLayoutModel();
        if (columnLayoutModel != null) {
        	int columnWidth = treeDetail.getWidth() / columnLayoutModel.getColumnTreeModels().size(); 
    		itemWidth = columnWidth;
    		offset = 300;
        } else {
        	itemWidth = treeDetail.getWidth();
        	offset = 400;
        }
		
	}
	
	private int getLevel(){
		TreeItem current = item;
		int leval = 0;
		while (current != null){
			leval++;
			current = current.getParentItem();
		}
		return leval;
	}
	
	private void initItemWidth(TreeItem childItem, int leval){
		if (childItem.getWidget() instanceof HorizontalPanel) {
			HorizontalPanel hp = (HorizontalPanel) childItem.getWidget();
			if(hp.getWidgetCount() > 1) {
				Widget field = hp.getWidget(1);
				int size = itemWidth - (offset + 19 * leval);
				if (field instanceof FormatTextField)
					((FormatTextField)field).setWidth(size > 200 ? size : 200);
				else if (field instanceof SimpleComboBox)
                    ((SimpleComboBox) field).setWidth(size > 200 ? size : 200);
			}
		}
	}

	
	private void tryRenderFks(){
		if (foreighKeyMap.size() > 0) {
			for (TypeModel model : foreighKeyMap.keySet()) {
				treeDetail.getFkRender().RenderForeignKey(itemNode, foreighKeyMap.get(model), model, treeDetail.getToolBar(), viewBean, treeDetail, treeDetail.getItemsDetailPanel());
			}
			foreighKeyMap.clear();

			TypeModel itemType = metaDataTypes.get(itemNode.getTypePath());
			if (itemType instanceof ComplexTypeModel){
				List<TypeModel> subTypes = ((ComplexTypeModel) itemType).getSubTypes();
				if (subTypes != null && subTypes.size() == 1){
					// All went to FK Tab, in that case return null so the tree item is hide
					item.getElement().setPropertyBoolean("EmptyFkContainer", true); //$NON-NLS-1$
				}
			}
        }
	}
	
	private void executeGroup(){
		while (index < itemNodeChildren.size()){
	        ItemNodeModel node = (ItemNodeModel) itemNodeChildren.get(index++);
	        Serializable nodeObjectValue = node.getObjectValue();
	        String nodeBindingPath = node.getBindingPath();
	        String typePath = node.getTypePath();

	        TypeModel typeModel = metaDataTypes.get(typePath);
	        String typeModelDefaultValue = typeModel.getDefaultValue();
	        if (withDefaultValue && typeModelDefaultValue != null && (nodeObjectValue == null || nodeObjectValue.equals(""))) //$NON-NLS-1$
	            node.setObjectValue(typeModelDefaultValue);

	        boolean isFKDisplayedIntoTab = TreeDetail.isFKDisplayedIntoTab(node, typeModel, metaDataTypes);

	        if (isFKDisplayedIntoTab) {
	            if (!foreighKeyMap.containsKey(typeModel)) {
	                foreighKeyMap.put(typeModel, new ArrayList<ItemNodeModel>());
	            }
	            foreighKeyMap.get(typeModel).add(node);
	            
	        } else {
	            TreeItem childItem = treeDetail.buildGWTTree(node, null, withDefaultValue, operation);
	            if (childItem != null && !childItem.getElement().getPropertyBoolean("EmptyFkContainer")) { //$NON-NLS-1$
	        		initItemWidth(childItem, level);
	                item.addItem(childItem);
	                int count = 0;
	                CountMapItem countMapItem = treeDetail.new CountMapItem(nodeBindingPath, itemNode);
	                if (occurMap.containsKey(countMapItem))
	                    count = occurMap.get(countMapItem);
	                occurMap.put(countMapItem, count + 1);
	            }
	        }

	        if (index % GROUP_SIZE == 0){
	        	return;
	        }
		}
	}
	
	public boolean execute() {
		executeGroup();

        if (index < itemNodeChildren.size()){
        	return true;
        } else { 
          tryRenderFks();
          return false;
        }
	}

}
