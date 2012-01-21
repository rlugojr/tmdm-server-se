package com.amalto.core.delegator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.servlet.http.HttpServletRequest;

import org.talend.mdm.commmon.util.core.MDMConfiguration;

import com.amalto.core.ejb.ItemPOJO;
import com.amalto.core.ejb.ItemPOJOPK;
import com.amalto.core.objects.universe.ejb.UniversePOJO;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.XtentisException;

public abstract class ILocalUser implements IBeanDelegator{
    
    protected static LinkedHashMap<String,String> onlineUsers = new LinkedHashMap<String,String>();
    
	public Subject getICurrentSubject() throws XtentisException {
		String SUBJECT_CONTEXT_KEY = "javax.security.auth.Subject.container";       		
		Subject subject;
		try {
			subject = (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
		} catch (PolicyContextException e1) {
			String err = "Unable find the local user: the JACC Policy Context cannot be accessed: "+e1.getMessage();
			org.apache.log4j.Logger.getLogger(LocalUser.class).error(err,e1);
			throw new XtentisException(err);
		}
		return subject;
	}
	
	public ILocalUser getILocalUser() throws XtentisException {
		return null;
	}
	
    public static LinkedHashMap<String, String> getOnlineUsers() {
        return onlineUsers;
    }

    public HashSet<String> getRoles() {
		// TODO Auto-generated method stub
		HashSet<String> set=new HashSet<String>();
		set.add("administration");
		set.add("authenticated");
		return set;
	}

	public UniversePOJO getUniverse() {
		HashMap<String, String>map=new HashMap<String, String>();
		for(String name:UniversePOJO.getXtentisObjectName()){
			map.put(name, null);
		}
		return new UniversePOJO("[HEAD]","",map,new LinkedHashMap<String, String>());
	}

	public String getUserXML() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUsername()  {
		String username=null;
		try {
			username=LocalUser.getPrincipalMember("Username");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        return username == null ? MDMConfiguration.getAdminUser() : username;
	}

	public String getPassword(){
		String passwd=null;
		try {
			passwd=LocalUser.getPrincipalMember("Password");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return passwd;
	}
	public boolean isAdmin(Class<?> objectTypeClass) throws XtentisException {
		// TODO Auto-generated method stub
		return true;
	}

	public void logout() throws XtentisException {
		String SERVLET_CONTEXT_KEY = "javax.servlet.http.HttpServletRequest";
		HttpServletRequest request;
		try {
			request = (HttpServletRequest) PolicyContext.getContext(SERVLET_CONTEXT_KEY);
		} catch (PolicyContextException e1) {
			String err = "Unable find the local servlet request: the JACC Policy Context cannot be accessed: "+e1.getMessage();
			org.apache.log4j.Logger.getLogger(LocalUser.class).error(err,e1);
			throw new XtentisException(err);
		}
		if (request != null) request.getSession().invalidate();
	}

	public void resetILocalUsers() throws XtentisException {
		// TODO Auto-generated method stub
		
	}

	public void setRoles(HashSet<String> roles) {
		// TODO Auto-generated method stub
		
	}

	public void setUniverse(UniversePOJO universe) {
		// TODO Auto-generated method stub
		
	}

	public void setUserXML(String userXML) {
		// TODO Auto-generated method stub
		
	}

	public void setUsername(String username) {
		// TODO Auto-generated method stub
		
	}

	public boolean userCanRead(Class<?> objectTypeClass, String instanceId)
			throws XtentisException {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean userCanWrite(Class<?> objectTypeClass, String instanceId)
			throws XtentisException {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean userItemCanRead(ItemPOJO item) throws XtentisException {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean userItemCanRead(ItemPOJOPK item) throws XtentisException {
		// TODO Auto-generated method stub
		return true;
	}
	public boolean userItemCanWrite(ItemPOJO item, String datacluster,
			String concept) throws XtentisException {
		// TODO Auto-generated method stub
		return true;
	}
	public boolean userItemCanWrite(ItemPOJOPK item, String datacluster,
			String concept) throws XtentisException {
		// TODO Auto-generated method stub
		return true;
	}	
}
