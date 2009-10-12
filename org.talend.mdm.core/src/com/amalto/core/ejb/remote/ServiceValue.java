/*
 * Generated by XDoclet - Do not edit!
 */
package com.amalto.core.ejb.remote;

/**
 * Value object for Service.
 *
 * Notice, this object is used to represent the state of an 
 * Service object. This value object
 * Is not connected to the database in any way, it is just a normal object used 
 * as a container for data from an EJB. 
 *
 * @xdoclet-generated at 12-10-09
 * @copyright The XDoclet Team
 * @author XDoclet
 * @version ${version}
 */
public class ServiceValue
   extends java.lang.Object
   implements java.io.Serializable, java.lang.Cloneable 
{

   private java.lang.String serviceName;
   private boolean serviceNameHasBeenSet = false;

   private java.lang.String configuration;
   private boolean configurationHasBeenSet = false;

   private java.lang.String serviceData;
   private boolean serviceDataHasBeenSet = false;

   private com.amalto.core.ejb.remote.ServicePK primaryKey;

   public ServiceValue()
   {
	  primaryKey = new com.amalto.core.ejb.remote.ServicePK();
   }

   public ServiceValue( java.lang.String serviceName,java.lang.String configuration,java.lang.String serviceData )
   {
       setServiceName(serviceName);
       setConfiguration(configuration);
       setServiceData(serviceData);
       primaryKey = new com.amalto.core.ejb.remote.ServicePK(this.getServiceName());
   }

   /**
    * @deprecated use {@link #clone}
    */
   public ServiceValue( ServiceValue otherValue )
   {
	  this.serviceName = otherValue.serviceName;
	  serviceNameHasBeenSet = true;
	  this.configuration = otherValue.configuration;
	  configurationHasBeenSet = true;
	  this.serviceData = otherValue.serviceData;
	  serviceDataHasBeenSet = true;

	  primaryKey = new com.amalto.core.ejb.remote.ServicePK(this.getServiceName());
   }

   public com.amalto.core.ejb.remote.ServicePK getPrimaryKey()
   {
	  return primaryKey;
   }

   public void setPrimaryKey( com.amalto.core.ejb.remote.ServicePK primaryKey)
   {
      // it's also nice to update PK object - just in case
      // somebody would ask for it later...
      this.primaryKey = primaryKey;
	  setServiceName( primaryKey.serviceName );
   }

   public java.lang.String getServiceName()
   {
	  return this.serviceName;
   }

   public void setServiceName( java.lang.String serviceName )
   {
	  this.serviceName = serviceName;
	  serviceNameHasBeenSet = true;

                        if (primaryKey != null) {
                               primaryKey.setServiceName(serviceName);
                        }                        //Patch NullPointException
                        //primaryKey.setServiceName(serviceName);
   }

   public boolean serviceNameHasBeenSet(){
	  return serviceNameHasBeenSet;
   }
   public java.lang.String getConfiguration()
   {
	  return this.configuration;
   }

   public void setConfiguration( java.lang.String configuration )
   {
	  this.configuration = configuration;
	  configurationHasBeenSet = true;

   }

   public boolean configurationHasBeenSet(){
	  return configurationHasBeenSet;
   }
   public java.lang.String getServiceData()
   {
	  return this.serviceData;
   }

   public void setServiceData( java.lang.String serviceData )
   {
	  this.serviceData = serviceData;
	  serviceDataHasBeenSet = true;

   }

   public boolean serviceDataHasBeenSet(){
	  return serviceDataHasBeenSet;
   }

   public String toString()
   {
	  StringBuffer str = new StringBuffer("{");

	  str.append("serviceName=" + getServiceName() + " " + "configuration=" + getConfiguration() + " " + "serviceData=" + getServiceData());
	  str.append('}');

	  return(str.toString());
   }

   /**
    * A Value Object has an identity if the attributes making its Primary Key have all been set. An object without identity is never equal to any other object.
    *
    * @return true if this instance has an identity.
    */
   protected boolean hasIdentity()
   {
	  boolean ret = true;
	  ret = ret && serviceNameHasBeenSet;
	  return ret;
   }

   /**
    *
    * @deprecated use {@link #equals}
    */
   public boolean isIdentical(Object other)
   {
          if (other instanceof ServiceValue)
          {
                 ServiceValue that = (ServiceValue) other;
                 boolean lEquals = true;
                 if( this.configuration == null )
                 {
                        lEquals = lEquals && ( that.configuration == null );
                 }
                 else
                 {
                        lEquals = lEquals && this.configuration.equals( that.configuration );
                 }
                 if( this.serviceData == null )
                 {
                        lEquals = lEquals && ( that.serviceData == null );
                 }
                 else
                 {
                        lEquals = lEquals && this.serviceData.equals( that.serviceData );
                 }

                 return lEquals;
          }
          else
          {
                 return false;
          }
   }

    public boolean equals(Object other) {

        //If it's not the correct type, clearly it isn't equal to this.
        if (!(other instanceof ServiceValue)) { 
            return false;
        }

        return equals((ServiceValue) other);
    }

    /**
     * This class is not using strict ordering. This means that the object is not Comparable, and
     * each check for equality will test all members for equality. We do not check collections for
     * equality however, so you would be wise to not use this if you have collection typed EJB References.
     */
    public boolean equals(ServiceValue that) {

        //try to get lucky.
        if (this == that) {
            return true;
        }
        //this clearly isn't null.
        if(null == that) {
            return false;
        }

        if(this.serviceName != that.serviceName) {

            if( this.serviceName == null || that.serviceName == null ) {
                return false;
            }

            if(!this.serviceName.equals(that.serviceName)) {
                return false;
            }

        }

        if(this.configuration != that.configuration) {

            if( this.configuration == null || that.configuration == null ) {
                return false;
            }

            if(!this.configuration.equals(that.configuration)) {
                return false;
            }

        }

        if(this.serviceData != that.serviceData) {

            if( this.serviceData == null || that.serviceData == null ) {
                return false;
            }

            if(!this.serviceData.equals(that.serviceData)) {
                return false;
            }

        }

        return true;

    }

    public Object clone() throws java.lang.CloneNotSupportedException {
        ServiceValue other = (ServiceValue) super.clone();

        return other;
    }

    public ReadOnlyServiceValue getReadOnlyServiceValue() {
        return new ReadOnlyServiceValue();
    }

    public int hashCode(){
	  int result = 17;
      result = 37*result + ((this.serviceName != null) ? this.serviceName.hashCode() : 0);

      result = 37*result + ((this.configuration != null) ? this.configuration.hashCode() : 0);

      result = 37*result + ((this.serviceData != null) ? this.serviceData.hashCode() : 0);

	  return result;
    }

    /**
     * Covariant function so the compiler can choose the proper one at compile time,
     * eliminates the need for XDoclet to really understand compiletime typing.
     *
     * Read only collections need to be synchronized. Once we start giving out handles
     * to these collections, they'll be used in other threads sooner or later. 
     */
    private static java.util.Collection wrapCollection(java.util.Collection input) {
        return java.util.Collections.synchronizedCollection(input);
    }
    /**
     * Covariant function so the compiler can choose the proper one at compile time,
     * eliminates the need for XDoclet to really understand compiletime typing.
     *
     * Read only collections need to be synchronized. Once we start giving out handles
     * to these collections, they'll be used in other threads sooner or later. 
     */
    private static java.util.Set wrapCollection(java.util.Set input) {
        return java.util.Collections.synchronizedSet(input);
    }
    /**
     * Covariant function. This is used in covariant form so that the compiler
     * can do some of our conditional branches for us. If I made these functions
     * have different names, then XDoclet would have to choose between them based on 
     * compiletime types, that wouldn't be easy. 
     */
    private static java.util.Collection wrapReadOnly(java.util.Collection input) {
        return java.util.Collections.unmodifiableCollection(input);
    }
    /**
     * Covariant function. This is used in covariant form so that the compiler
     * can do some of our conditional branches for us. If I made these functions
     * have different names, then XDoclet would have to choose between them based on 
     * compiletime types, that wouldn't be easy. 
     */
    private static java.util.Set wrapReadOnly(java.util.Set input) {
        return java.util.Collections.unmodifiableSet(input);
    }

    private final class ReadOnlyServiceValue 
    implements java.lang.Cloneable, java.io.Serializable 
    {
        private ServiceValue underlying() {
            return ServiceValue.this;
        }

       public java.lang.String getServiceName() {
              return underlying().serviceName;
       }

       public java.lang.String getConfiguration() {
              return underlying().configuration;
       }

       public java.lang.String getServiceData() {
              return underlying().serviceData;
       }

        public int hashCode() {
            return 101 * underlying().hashCode();
        }

        public boolean equals(Object o) {
            if(o instanceof ReadOnlyServiceValue) {
                return this.equals((ReadOnlyServiceValue) o);
            }
            return false;
        }

        public boolean equals(ReadOnlyServiceValue that) {
            if(null == that) {
                return false;
            }

            return this.underlying().equals(that.underlying());
        }

    }

}
