/*
 * Generated by XDoclet - Do not edit!
 */
package com.amalto.core.ejb.local;

/**
 * BMP layer for Service.
 * @xdoclet-generated at 12-10-09
 * @copyright The XDoclet Team
 * @author XDoclet
 * @version ${version}
 */
public class ServiceBMP
   extends com.amalto.core.ejb.ServiceBean
   implements javax.ejb.EntityBean
{

   public java.lang.String serviceName;
   public java.lang.String configuration;
   public java.lang.String serviceData;

   public java.lang.String getServiceName() 
   {
      return this.serviceName;
   }

   public void setServiceName( java.lang.String serviceName ) 
   {
      this.serviceName = serviceName;
      makeDirty();
   }
   public java.lang.String getConfiguration() 
   {
      return this.configuration;
   }

   public void setConfiguration( java.lang.String configuration ) 
   {
      this.configuration = configuration;
      makeDirty();
   }
   public java.lang.String getServiceData() 
   {
      return this.serviceData;
   }

   public void setServiceData( java.lang.String serviceData ) 
   {
      this.serviceData = serviceData;
      makeDirty();
   }

   public boolean isModified()
   {
      return dirty;
   }

   protected void makeDirty()
   {
      dirty = true;
   }

   protected void makeClean()
   {
      dirty = false;
   }

   private boolean dirty = false;

   public com.amalto.core.ejb.dao.ServiceData getData()
   {
      com.amalto.core.ejb.dao.ServiceData dataHolder = null;
      try
      {
         dataHolder = new com.amalto.core.ejb.dao.ServiceData();

         dataHolder.setServiceName( getServiceName() );
         dataHolder.setConfiguration( getConfiguration() );
         dataHolder.setServiceData( getServiceData() );

      }
      catch (RuntimeException e)
      {
         throw new javax.ejb.EJBException(e);
      }

      return dataHolder;
   }

   public void ejbLoad() throws javax.ejb.EJBException, java.rmi.RemoteException
   {
      super.ejbLoad();
      makeClean();
   }

   public void ejbStore() throws javax.ejb.EJBException, java.rmi.RemoteException
   {
      if (isModified())
      {
         super.ejbStore();
         makeClean();
      }
   }

   public void ejbActivate() throws javax.ejb.EJBException, java.rmi.RemoteException
   {
      super.ejbActivate();
   }

   public void ejbPassivate() throws javax.ejb.EJBException, java.rmi.RemoteException
   {
      super.ejbPassivate();

      ServiceValue = null;
   }

   public void setEntityContext(javax.ejb.EntityContext ctx) throws javax.ejb.EJBException, java.rmi.RemoteException
   {
      super.setEntityContext(ctx);
   }

   public void unsetEntityContext() throws javax.ejb.EJBException, java.rmi.RemoteException
   {
      super.unsetEntityContext();
   }

   public void ejbRemove() throws javax.ejb.EJBException, java.rmi.RemoteException, javax.ejb.RemoveException
   {
      super.ejbRemove();

   }

   /* Value Objects BEGIN */

   private com.amalto.core.ejb.remote.ServiceValue ServiceValue = null;

   public com.amalto.core.ejb.remote.ServiceValue getServiceValue()
   {
      ServiceValue = new com.amalto.core.ejb.remote.ServiceValue();
      try
         {
            ServiceValue.setServiceName( getServiceName() );
            ServiceValue.setConfiguration( getConfiguration() );
            ServiceValue.setServiceData( getServiceData() );

         }
         catch (Exception e)
         {
            throw new javax.ejb.EJBException(e);
         }

	  return ServiceValue;
   }

/* Value Objects END */

}
