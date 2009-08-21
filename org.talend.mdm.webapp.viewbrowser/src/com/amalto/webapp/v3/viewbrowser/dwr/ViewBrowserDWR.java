package com.amalto.webapp.v3.viewbrowser.dwr;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amalto.webapp.core.bean.Configuration;
import com.amalto.webapp.core.dwr.CommonDWR;
import com.amalto.webapp.core.util.Util;
import com.amalto.webapp.core.util.XtentisWebappException;
import com.amalto.webapp.util.webservices.WSGetView;
import com.amalto.webapp.util.webservices.WSGetViewPKs;
import com.amalto.webapp.util.webservices.WSView;
import com.amalto.webapp.util.webservices.WSViewPK;
import com.amalto.webapp.v3.viewbrowser.bean.TreeNode;
import com.amalto.webapp.v3.viewbrowser.bean.View;


public class ViewBrowserDWR {

	public ViewBrowserDWR() {
		super();
	}
		
	public TreeMap<String,String> getViewsList(String language) throws RemoteException, Exception{
		if(language==null) {
			language = "EN";
		}
		WSViewPK[] wsViewsPK = Util.getPort().getViewPKs(new WSGetViewPKs("[^Browse_items].*")).getWsViewPK();
		String[] names = new String[wsViewsPK.length];
		TreeMap<String,String> views = new TreeMap<String,String>();
		Pattern p = Pattern.compile(".*\\["+language.toUpperCase()+":(.*?)\\].*",Pattern.DOTALL);
		for (int i = 0; i < wsViewsPK.length; i++) {
			WSView wsview = Util.getPort().getView(new WSGetView(wsViewsPK[i]));
			names[i] = wsViewsPK[i].getPk();
			if(!"MDM Reporting View".equals( wsViewsPK[i].getPk()) 
					&& !"XTENTIS PORTAL USER VIEW".equals( wsViewsPK[i].getPk()))
				views.put(wsview.getName(),p.matcher(wsview.getDescription()).replaceAll("$1"));
		}

/*		class NameComparator implements Comparator {
			
			public int compare(Object item1, Object item2) {
				String var1 = (String)item1;
				String var2 = (String)item2;
				return var1.compareTo(var2);
			}
		}

		Arrays.sort(names, new NameComparator());;
		*/
		return views;
	}

	public View getView(String viewPK, String language){
		try {
			return new View(viewPK, language);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String[] getViewables(String viewPK, String language){		
		WebContext ctx = WebContextFactory.get();
		ctx.getSession().setAttribute("view",null);
		try {
			Configuration config = Configuration.getInstance();
			String[] viewables = new View(viewPK,language).getViewablesXpath();
			String[] labelViewables = new String[viewables.length];
			HashMap<String,String> xpathToLabel = CommonDWR.getFieldsByDataModel(
					config.getModel(),
					CommonDWR.getConceptFromBrowseItemView(viewPK),
					language, true);
			for (int i = 0; i < viewables.length; i++) {
				labelViewables[i] = xpathToLabel.get(viewables[i]);
			}
			return labelViewables;
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	public void setTree(int id, String nodeElId){
        WebContext ctx = WebContextFactory.get();	
        String[] resultsXML = (String[])ctx.getSession().getAttribute("resultsXML");
        String xml = resultsXML[id];
        System.out.println(xml);
		try {
			Document d = Util.parse(xml);
			Element root = d.getDocumentElement();
			//Node root = d.getDocumentElement();
			HashMap<String,Element> idToNode = new HashMap<String,Element>();
			idToNode.put(nodeElId,root);
			//System.out.println("HASHMAP 1 "+idToNode);
			ctx.getSession().setAttribute("idToNode",idToNode);
			/*NodeList list = root.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				if(list.item(i).getNodeType()==Node.TEXT_NODE)
					System.out.println("Node value list1 "+i+" "+ list.item(i).getNodeValue());
				if(list.item(i).getNodeType()==Node.ELEMENT_NODE)
					System.out.println("Node name list1 "+i+" "+ list.item(i).getNodeName());
				NodeList list2 = list.item(i).getChildNodes();
				for (int j = 0; j < list2.getLength(); j++) {
					if(list2.item(j).getNodeType()==Node.TEXT_NODE)
						System.out.println("Node value list2 "+j+" "+ list2.item(j).getNodeValue());
					if(list2.item(j).getNodeType()==Node.ELEMENT_NODE)
						System.out.println("Node name list2 "+j+" "+ list2.item(j).getNodeName());
					if(list2.item(j).getNodeType()==Node.ELEMENT_NODE){
						if(list2.item(j).getChildNodes().getLength()>0){
							System.out.println("any sons");
						}
						else{
							System.out.println("no son");
						}
					}
				}				
			}
			*/
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public TreeNode[] getNode(String id, int nodeCount){
		WebContext ctx = WebContextFactory.get();
		HashMap<String,Element> idToNode = (HashMap<String,Element>) ctx.getSession().getAttribute("idToNode");
		Element node = idToNode.get(id);		
		NodeList nl = node.getChildNodes();
		//String[] data = new String[nl.getLength()];
		ArrayList<TreeNode> list = new ArrayList<TreeNode>();
		for (int i = 0; i < nl.getLength(); i++) {
			try {
				if(Util.getFirstTextNode(nl.item(i),"./")!=null){			
					TreeNode treeNode = new TreeNode();
					//data[i] = nl.item(i).getNodeName()+" "+Util.getFirstTextNode(nl.item(i),"./");
					treeNode.setName(nl.item(i).getNodeName());
					treeNode.setValue(Util.getFirstTextNode(nl.item(i),"./"));
					if(nl.item(i).getChildNodes().getLength()>1) 
						treeNode.setExpandable(true);//data[i]+="expandable";
					else
						treeNode.setExpandable(false);//data[i]+="expandable";
					if(nl.item(i).getNodeType()==Node.ELEMENT_NODE){
						//data[i]= nl.item(i).getNodeName();
						idToNode.put("ygtv"+nodeCount,(Element)nl.item(i));					
					}
					list.add(treeNode);
					nodeCount++;
				}
				
			} catch (XtentisWebappException e1) {
				e1.printStackTrace();
			}			
		}
		//return nodes;
		return list.toArray(new TreeNode[list.size()]); 
	}
	
	
	
	public Object [] getChildren(Object parent) {
		if (parent instanceof Document) 
			return new Object[] {((Document)parent).getDocumentElement()};
		
		if (parent instanceof Element) {
			Element e = (Element) parent;
			ArrayList<Node> list = new ArrayList<Node>();
			//Attributes
			NamedNodeMap map = e.getAttributes();
			for (int i = 0; i < map.getLength(); i++) {
				list.add(map.item(i));
			}
			//Sub-Elements
			NodeList nl = ((Element)parent).getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				if (nl.item(i) instanceof Element)
					list.add(nl.item(i));
			}
			if (list.size() == 0)
				return null;
			else
				return list.toArray(new Node[list.size()]);
		}
		
		return null;
	}
	

	
	public String getXML(int id){
        WebContext ctx = WebContextFactory.get();	
        String[] resultsXML = (String[])ctx.getSession().getAttribute("resultsXML");
        String xml = StringEscapeUtils.escapeXml(resultsXML[id]);
        xml = xml.replaceAll("\n","<br/>");
        return xml;
	}
	
}
