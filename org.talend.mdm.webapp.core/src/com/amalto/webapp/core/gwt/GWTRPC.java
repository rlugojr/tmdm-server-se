package com.amalto.webapp.core.gwt;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.impl.AbstractSerializationStream;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RPCServletUtils;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;
import com.google.gwt.user.server.rpc.UnexpectedException;
import com.google.gwt.user.server.rpc.impl.LegacySerializationPolicy;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamReader;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter;
import com.google.gwt.user.server.rpc.impl.TypeNameObfuscator;

public final class GWTRPC {

    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS = new HashMap<Class<?>, Class<?>>();

    private static Map<Class<?>, Set<String>> serviceToImplementedInterfacesMap;

    private static final HashMap<String, Class<?>> TYPE_NAMES;

    static {
        PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Boolean.class, Boolean.TYPE);
        PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Byte.class, Byte.TYPE);
        PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Character.class, Character.TYPE);
        PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Double.class, Double.TYPE);
        PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Float.class, Float.TYPE);
        PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Integer.class, Integer.TYPE);
        PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Long.class, Long.TYPE);
        PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.put(Short.class, Short.TYPE);

        TYPE_NAMES = new HashMap<String, Class<?>>();
        TYPE_NAMES.put("Z", boolean.class); //$NON-NLS-1$
        TYPE_NAMES.put("B", byte.class); //$NON-NLS-1$
        TYPE_NAMES.put("C", char.class); //$NON-NLS-1$
        TYPE_NAMES.put("D", double.class); //$NON-NLS-1$
        TYPE_NAMES.put("F", float.class); //$NON-NLS-1$
        TYPE_NAMES.put("I", int.class); //$NON-NLS-1$
        TYPE_NAMES.put("J", long.class); //$NON-NLS-1$
        TYPE_NAMES.put("S", short.class); //$NON-NLS-1$

        serviceToImplementedInterfacesMap = new HashMap<Class<?>, Set<String>>();
    }


    public static GWTRPCRequest decodeRequest(String encodedRequest) {
        return decodeRequest(encodedRequest, null);
    }

    public static GWTRPCRequest decodeRequest(String encodedRequest,
            SerializationPolicyProvider serializationPolicyProvider) {
        if (encodedRequest == null) {
            throw new NullPointerException("encodedRequest cannot be null"); //$NON-NLS-1$
        }

        if (encodedRequest.length() == 0) {
            throw new IllegalArgumentException("encodedRequest cannot be empty"); //$NON-NLS-1$
        }

//        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            ServerSerializationStreamReader streamReader = new ServerSerializationStreamReader(classLoader,
                    serializationPolicyProvider);
            streamReader.prepareToRead(encodedRequest);

            // Read the name of the RemoteService interface
            String serviceIntfName = maybeDeobfuscate(streamReader, streamReader.readString());



            SerializationPolicy serializationPolicy = streamReader.getSerializationPolicy();
            Class<?> serviceIntf;
            try {
                serviceIntf = getClassFromSerializedName(serviceIntfName, classLoader);

                if (!Class
                        .forName("com.google.gwt.user.client.rpc.RemoteService", false, classLoader).isAssignableFrom(serviceIntf)) { //$NON-NLS-1$
                    // The requested interface is not a RemoteService interface
                    throw new IncompatibleRemoteServiceException("Blocked attempt to access interface '" //$NON-NLS-1$
                            + printTypeName(serviceIntf)
                            + "', which doesn't extend RemoteService; this is either misconfiguration or a hack attempt"); //$NON-NLS-1$
                }
            } catch (ClassNotFoundException e) {
                throw new IncompatibleRemoteServiceException("Could not locate requested interface '" + serviceIntfName //$NON-NLS-1$
                        + "' in default classloader", e); //$NON-NLS-1$
            }

            String serviceMethodName = streamReader.readString();

            int paramCount = streamReader.readInt();
            if (paramCount > streamReader.getNumberOfTokens()) {
                throw new IncompatibleRemoteServiceException("Invalid number of parameters"); //$NON-NLS-1$
            }
            Class<?>[] parameterTypes = new Class[paramCount];

            for (int i = 0; i < parameterTypes.length; i++) {
                String paramClassName = maybeDeobfuscate(streamReader, streamReader.readString());

                try {
                    parameterTypes[i] = getClassFromSerializedName(paramClassName, classLoader);
                } catch (ClassNotFoundException e) {
                    throw new IncompatibleRemoteServiceException("Parameter " + i + " of is of an unknown type '" //$NON-NLS-1$ //$NON-NLS-2$
                            + paramClassName + "'", e); //$NON-NLS-1$
                }
            }

            try {
                Method method = serviceIntf.getMethod(serviceMethodName, parameterTypes);

                Object[] parameterValues = new Object[parameterTypes.length];
                for (int i = 0; i < parameterValues.length; i++) {
                    parameterValues[i] = streamReader.deserializeValue(parameterTypes[i]);
                }

                return new GWTRPCRequest(serviceIntfName, new RPCRequest(method, parameterValues, serializationPolicy,
                        streamReader.getFlags()));

            } catch (NoSuchMethodException e) {
                throw new IncompatibleRemoteServiceException(formatMethodNotFoundErrorMessage(serviceIntf, serviceMethodName,
                        parameterTypes));
            }
        } catch (Exception ex) {
            throw new IncompatibleRemoteServiceException(ex.getMessage(), ex);
        }
    }


    public static String encodeResponseForFailure(Method serviceMethod, Throwable cause) throws SerializationException {
        return encodeResponseForFailure(serviceMethod, cause, getDefaultSerializationPolicy());
    }

    public static String encodeResponseForFailure(Method serviceMethod, Throwable cause, SerializationPolicy serializationPolicy)
            throws SerializationException {
        return encodeResponseForFailure(serviceMethod, cause, serializationPolicy, AbstractSerializationStream.DEFAULT_FLAGS);
    }

    public static String encodeResponseForFailure(Method serviceMethod, Throwable cause, SerializationPolicy serializationPolicy,
            int flags) throws SerializationException {
        if (cause == null) {
            throw new NullPointerException("cause cannot be null"); //$NON-NLS-1$
        }

        if (serializationPolicy == null) {
            throw new NullPointerException("serializationPolicy"); //$NON-NLS-1$
        }

        if (serviceMethod != null && !RPCServletUtils.isExpectedException(serviceMethod, cause)) {
            throw new UnexpectedException("Service method '" + getSourceRepresentation(serviceMethod) //$NON-NLS-1$
                    + "' threw an unexpected exception: " + cause.toString(), cause); //$NON-NLS-1$
        }

        return encodeResponse(cause.getClass(), cause, true, flags, serializationPolicy);
    }

    public static String encodeResponseForSuccess(Method serviceMethod, Object object) throws SerializationException {
        return encodeResponseForSuccess(serviceMethod, object, getDefaultSerializationPolicy());
    }

    public static String encodeResponseForSuccess(Method serviceMethod, Object object, SerializationPolicy serializationPolicy)
            throws SerializationException {
        return encodeResponseForSuccess(serviceMethod, object, serializationPolicy, AbstractSerializationStream.DEFAULT_FLAGS);
    }

    public static String encodeResponseForSuccess(Method serviceMethod, Object object, SerializationPolicy serializationPolicy,
            int flags) throws SerializationException {
        if (serviceMethod == null) {
            throw new NullPointerException("serviceMethod cannot be null"); //$NON-NLS-1$
        }

        if (serializationPolicy == null) {
            throw new NullPointerException("serializationPolicy"); //$NON-NLS-1$
        }

        Class<?> methodReturnType = serviceMethod.getReturnType();
        if (methodReturnType != void.class && object != null) {
            Class<?> actualReturnType;
            if (methodReturnType.isPrimitive()) {
                actualReturnType = getPrimitiveClassFromWrapper(object.getClass());
            } else {
                actualReturnType = object.getClass();
            }

            if (actualReturnType == null || !methodReturnType.isAssignableFrom(actualReturnType)) {
                throw new IllegalArgumentException("Type '" + printTypeName(object.getClass()) //$NON-NLS-1$
                        + "' does not match the return type in the method's signature: '" //$NON-NLS-1$
                        + getSourceRepresentation(serviceMethod) + "'"); //$NON-NLS-1$
            }
        }

        return encodeResponse(methodReturnType, object, false, flags, serializationPolicy);
    }

    public static SerializationPolicy getDefaultSerializationPolicy() {
        return LegacySerializationPolicy.getInstance();
    }

    public static String invokeAndEncodeResponse(Object target, Method serviceMethod, Object[] args)
            throws SerializationException {
        return invokeAndEncodeResponse(target, serviceMethod, args, getDefaultSerializationPolicy());
    }

    public static String invokeAndEncodeResponse(Object target, Method serviceMethod, Object[] args,
            SerializationPolicy serializationPolicy) throws SerializationException {
        return invokeAndEncodeResponse(target, serviceMethod, args, serializationPolicy,
                AbstractSerializationStream.DEFAULT_FLAGS);
    }

    public static String invokeAndEncodeResponse(Object target, Method serviceMethod, Object[] args,
            SerializationPolicy serializationPolicy, int flags) throws SerializationException {
        if (serviceMethod == null) {
            throw new NullPointerException("serviceMethod"); //$NON-NLS-1$
        }

        if (serializationPolicy == null) {
            throw new NullPointerException("serializationPolicy"); //$NON-NLS-1$
        }

        String responsePayload;
        try {
            Object result = serviceMethod.invoke(target, args);

            responsePayload = encodeResponseForSuccess(serviceMethod, result, serializationPolicy, flags);
        } catch (IllegalAccessException e) {
            SecurityException securityException = new SecurityException(formatIllegalAccessErrorMessage(target, serviceMethod));
            securityException.initCause(e);
            throw securityException;
        } catch (IllegalArgumentException e) {
            SecurityException securityException = new SecurityException(formatIllegalArgumentErrorMessage(target, serviceMethod,
                    args));
            securityException.initCause(e);
            throw securityException;
        } catch (InvocationTargetException e) {
            // Try to encode the caught exception
            //
            Throwable cause = e.getCause();

            responsePayload = encodeResponseForFailure(serviceMethod, cause, serializationPolicy, flags);
        }

        return responsePayload;
    }

    /**
     * Returns a string that encodes the results of an RPC call. Private overload that takes a flag signaling the
     * preamble of the response payload.
     * 
     * @param object the object that we wish to send back to the client
     * @param wasThrown if true, the object being returned was an exception thrown by the service method; if false, it
     * was the result of the service method's invocation
     * @return a string that encodes the response from a service method
     * @throws SerializationException if the object cannot be serialized
     */
    private static String encodeResponse(Class<?> responseClass, Object object, boolean wasThrown, int flags,
            SerializationPolicy serializationPolicy) throws SerializationException {

        ServerSerializationStreamWriter stream = new ServerSerializationStreamWriter(serializationPolicy);
        stream.setFlags(flags);

        stream.prepareToWrite();
        if (responseClass != void.class) {
            stream.serializeValue(object, responseClass);
        }

        String bufferStr = (wasThrown ? "//EX" : "//OK") + stream.toString(); //$NON-NLS-1$ //$NON-NLS-2$
        return bufferStr;
    }

    private static String formatIllegalAccessErrorMessage(Object target, Method serviceMethod) {
        StringBuffer sb = new StringBuffer();
        sb.append("Blocked attempt to access inaccessible method '"); //$NON-NLS-1$
        sb.append(getSourceRepresentation(serviceMethod));
        sb.append("'"); //$NON-NLS-1$

        if (target != null) {
            sb.append(" on target '"); //$NON-NLS-1$
            sb.append(printTypeName(target.getClass()));
            sb.append("'"); //$NON-NLS-1$
        }

        sb.append("; this is either misconfiguration or a hack attempt"); //$NON-NLS-1$

        return sb.toString();
    }

    private static String formatIllegalArgumentErrorMessage(Object target, Method serviceMethod, Object[] args) {
        StringBuffer sb = new StringBuffer();
        sb.append("Blocked attempt to invoke method '"); //$NON-NLS-1$
        sb.append(getSourceRepresentation(serviceMethod));
        sb.append("'"); //$NON-NLS-1$

        if (target != null) {
            sb.append(" on target '"); //$NON-NLS-1$
            sb.append(printTypeName(target.getClass()));
            sb.append("'"); //$NON-NLS-1$
        }

        sb.append(" with invalid arguments"); //$NON-NLS-1$

        if (args != null && args.length > 0) {
            sb.append(Arrays.asList(args));
        }

        return sb.toString();
    }

    private static String formatMethodNotFoundErrorMessage(Class<?> serviceIntf, String serviceMethodName,
            Class<?>[] parameterTypes) {
        StringBuffer sb = new StringBuffer();

        sb.append("Could not locate requested method '"); //$NON-NLS-1$
        sb.append(serviceMethodName);
        sb.append("("); //$NON-NLS-1$
        for (int i = 0; i < parameterTypes.length; ++i) {
            if (i > 0) {
                sb.append(", "); //$NON-NLS-1$
            }
            sb.append(printTypeName(parameterTypes[i]));
        }
        sb.append(")'"); //$NON-NLS-1$

        sb.append(" in interface '"); //$NON-NLS-1$
        sb.append(printTypeName(serviceIntf));
        sb.append("'"); //$NON-NLS-1$

        return sb.toString();
    }


    private static Class<?> getClassFromSerializedName(String serializedName, ClassLoader classLoader)
            throws ClassNotFoundException {
        Class<?> value = TYPE_NAMES.get(serializedName);
        if (value != null) {
            return value;
        }

        return Class.forName(serializedName, false, classLoader);
    }


    private static Class<?> getPrimitiveClassFromWrapper(Class<?> wrapperClass) {
        return PRIMITIVE_WRAPPER_CLASS_TO_PRIMITIVE_CLASS.get(wrapperClass);
    }

    private static String getSourceRepresentation(Method method) {
        return method.toString().replace('$', '.');
    }


    public static boolean implementsInterface(Class<?> service, String intfName) {
        synchronized (serviceToImplementedInterfacesMap) {
            // See if it's cached.
            //
            Set<String> interfaceSet = serviceToImplementedInterfacesMap.get(service);
            if (interfaceSet != null) {
                if (interfaceSet.contains(intfName)) {
                    return true;
                }
            } else {
                interfaceSet = new HashSet<String>();
                serviceToImplementedInterfacesMap.put(service, interfaceSet);
            }

            if (!service.isInterface()) {
                while ((service != null) && !RemoteServiceServlet.class.equals(service)) {
                    Class<?>[] intfs = service.getInterfaces();
                    for (Class<?> intf : intfs) {
                        if (implementsInterfaceRecursive(intf, intfName)) {
                            interfaceSet.add(intfName);
                            return true;
                        }
                    }

                    // did not find the interface in this class so we look in the
                    // superclass
                    //
                    service = service.getSuperclass();
                }
            } else {
                if (implementsInterfaceRecursive(service, intfName)) {
                    interfaceSet.add(intfName);
                    return true;
                }
            }

            return false;
        }
    }


    private static boolean implementsInterfaceRecursive(Class<?> clazz, String intfName) {
        assert (clazz.isInterface());

        if (clazz.getName().equals(intfName)) {
            return true;
        }

        // search implemented interfaces
        Class<?>[] intfs = clazz.getInterfaces();
        for (Class<?> intf : intfs) {
            if (implementsInterfaceRecursive(intf, intfName)) {
                return true;
            }
        }

        return false;
    }

    private static String maybeDeobfuscate(ServerSerializationStreamReader streamReader, String name)
            throws SerializationException {
        int index;
        if (streamReader.hasFlags(AbstractSerializationStream.FLAG_ELIDE_TYPE_NAMES)) {
            SerializationPolicy serializationPolicy = streamReader.getSerializationPolicy();
            if (!(serializationPolicy instanceof TypeNameObfuscator)) {
                throw new IncompatibleRemoteServiceException("RPC request was encoded with obfuscated type names, " //$NON-NLS-1$
                        + "but the SerializationPolicy in use does not implement " + TypeNameObfuscator.class.getName()); //$NON-NLS-1$
            }

            String maybe = ((TypeNameObfuscator) serializationPolicy).getClassNameForTypeId(name);
            if (maybe != null) {
                return maybe;
            }
        } else if ((index = name.indexOf('/')) != -1) {
            return name.substring(0, index);
        }
        return name;
    }

    public static String printTypeName(Class<?> type) {
        // Primitives
        //
        if (type.equals(Integer.TYPE)) {
            return "int"; //$NON-NLS-1$
        } else if (type.equals(Long.TYPE)) {
            return "long"; //$NON-NLS-1$
        } else if (type.equals(Short.TYPE)) {
            return "short"; //$NON-NLS-1$
        } else if (type.equals(Byte.TYPE)) {
            return "byte"; //$NON-NLS-1$
        } else if (type.equals(Character.TYPE)) {
            return "char"; //$NON-NLS-1$
        } else if (type.equals(Boolean.TYPE)) {
            return "boolean"; //$NON-NLS-1$
        } else if (type.equals(Float.TYPE)) {
            return "float"; //$NON-NLS-1$
        } else if (type.equals(Double.TYPE)) {
            return "double"; //$NON-NLS-1$
        }

        if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            return printTypeName(componentType) + "[]"; //$NON-NLS-1$
        }

        return type.getName().replace('$', '.');
    }

    private GWTRPC() {
        // Not instantiable
    }
}
