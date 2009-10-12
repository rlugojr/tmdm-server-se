 /*
 * Generated by XDoclet - Do not edit!
 * this class was prodiuced by xdoclet automagically...
 */
package com.amalto.core.objects.storedprocedure.ejb.remote;

import java.util.*;

/**
 * This class is remote adapter to StoredProcedureCtrl. It provides convenient way to access
 * facade session bean. Inverit from this class to provide reasonable caching and event handling capabilities.
 *
 * Remote facade for StoredProcedureCtrl.
 * @xdoclet-generated at 12-10-09
 * @copyright The XDoclet Team
 * @author XDoclet
 * @version ${version}
 */

public class StoredProcedureCtrlRemote extends Observable
{
    static StoredProcedureCtrlRemote _instance = null;
    public static StoredProcedureCtrlRemote getInstance() {
        if(_instance == null) {
	   _instance = new StoredProcedureCtrlRemote();
	}
	return _instance;
    }

  /**
   * cached remote session interface
   */
  com.amalto.core.objects.storedprocedure.ejb.remote.StoredProcedureCtrl _session = null;
  /**
   * return session bean remote interface
   */
   protected com.amalto.core.objects.storedprocedure.ejb.remote.StoredProcedureCtrl getSession() {
      try {
   	if(_session == null) {
	   _session = com.amalto.core.objects.storedprocedure.ejb.local.StoredProcedureCtrlUtil.getHome().create();
	}
	return _session;
      } catch(Exception ex) {
        // just catch it here and return null.
        // somebody can provide better solution
	ex.printStackTrace();
	return null;
      }
   }

   public com.amalto.core.objects.storedprocedure.ejb.StoredProcedurePOJOPK putStoredProcedure ( com.amalto.core.objects.storedprocedure.ejb.StoredProcedurePOJO storedProcedure )
	  throws com.amalto.core.util.XtentisException, java.rmi.RemoteException
   {
        com.amalto.core.objects.storedprocedure.ejb.StoredProcedurePOJOPK retval;
       retval =  getSession().putStoredProcedure( storedProcedure );

      return retval;

   }

   public com.amalto.core.objects.storedprocedure.ejb.StoredProcedurePOJO getStoredProcedure ( com.amalto.core.objects.storedprocedure.ejb.StoredProcedurePOJOPK pk )
	  throws com.amalto.core.util.XtentisException, java.rmi.RemoteException
   {
        com.amalto.core.objects.storedprocedure.ejb.StoredProcedurePOJO retval;
       retval =  getSession().getStoredProcedure( pk );

      return retval;

   }

   public com.amalto.core.objects.storedprocedure.ejb.StoredProcedurePOJO existsStoredProcedure ( com.amalto.core.objects.storedprocedure.ejb.StoredProcedurePOJOPK pk )
	  throws com.amalto.core.util.XtentisException, java.rmi.RemoteException
   {
        com.amalto.core.objects.storedprocedure.ejb.StoredProcedurePOJO retval;
       retval =  getSession().existsStoredProcedure( pk );

      return retval;

   }

   public com.amalto.core.objects.storedprocedure.ejb.StoredProcedurePOJOPK removeStoredProcedure ( com.amalto.core.objects.storedprocedure.ejb.StoredProcedurePOJOPK pk )
	  throws com.amalto.core.util.XtentisException, java.rmi.RemoteException
   {
        com.amalto.core.objects.storedprocedure.ejb.StoredProcedurePOJOPK retval;
       retval =  getSession().removeStoredProcedure( pk );

      return retval;

   }

   public java.util.Collection execute ( com.amalto.core.objects.storedprocedure.ejb.StoredProcedurePOJOPK sppk,java.lang.String revisionID,com.amalto.core.objects.datacluster.ejb.DataClusterPOJOPK dcpk,java.lang.String[] parameters )
	  throws com.amalto.core.util.XtentisException, java.rmi.RemoteException
   {
        java.util.Collection retval;
       retval =  getSession().execute( sppk,revisionID,dcpk,parameters );

      return retval;

   }

   public java.util.Collection getStoredProcedurePKs ( java.lang.String regex )
	  throws com.amalto.core.util.XtentisException, java.rmi.RemoteException
   {
        java.util.Collection retval;
       retval =  getSession().getStoredProcedurePKs( regex );

      return retval;

   }

  /**
   * override this method to provide feedback to interested objects
   * in case collections were changed.
   */
  public void invalidate() {

  	setChanged();
	notifyObservers();
  }
}
