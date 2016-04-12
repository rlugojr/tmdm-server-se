/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.services;

import static com.amalto.core.query.user.UserQueryBuilder.eq;
import static com.amalto.core.query.user.UserQueryBuilder.from;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Variant;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;
import org.talend.mdm.query.QueryParser;
import org.talend.utils.json.JSONArray;

import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.history.DocumentHistoryFactory;
import com.amalto.core.history.StorageActionFactory;
import com.amalto.core.history.StorageDocumentFactory;
import com.amalto.core.history.action.ActionFactory;
import com.amalto.core.load.io.XMLStreamUnwrapper;
import com.amalto.core.objects.UpdateReportItemPOJO;
import com.amalto.core.objects.UpdateReportPOJO;
import com.amalto.core.query.user.Expression;
import com.amalto.core.query.user.Select;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.save.DocumentSaverContext;
import com.amalto.core.save.RecordValidationCommitter;
import com.amalto.core.save.SaverSession;
import com.amalto.core.save.SaverSession.Committer;
import com.amalto.core.save.UserAction;
import com.amalto.core.save.context.DocumentSaver;
import com.amalto.core.save.context.SaverContextFactory;
import com.amalto.core.server.Server;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.SQLWrapper;
import com.amalto.core.storage.SecuredStorage;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.history.HistoryStorage;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordJSONWriter;
import com.amalto.core.storage.record.DataRecordWriter;
import com.amalto.core.storage.record.DataRecordXmlWriter;
import com.amalto.core.storage.transaction.Transaction;
import com.amalto.core.storage.transaction.Transaction.Lifetime;
import com.amalto.core.storage.transaction.TransactionManager;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.XtentisException;
import com.amalto.xmlserver.interfaces.XmlServerException;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("/data")
@Api("User data management")
public class DataService {

    private static final Logger LOGGER = Logger.getLogger(DataService.class);
    
    private static final List<Variant> queryVariants = new ArrayList<>();
    
    static {
        synchronized (queryVariants) {
            if (queryVariants.isEmpty()) {
                // Query evaluation service serves both XML and JSON
                Variant.VariantListBuilder builder = Variant.VariantListBuilder.newInstance();
                builder.mediaTypes(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE);
                builder.add();
                queryVariants.addAll(builder.build());
            }
        }
    }

    public DataService() {
    }

    /**
     * Enrich {@link com.amalto.core.query.user.UserQueryBuilder builder} to add a condition on type's id (using values
     * in <code>id</code>) based on metadata information.
     * 
     * @param type The type containing the id information.
     * @param id The id as string (for composite ids, use "." as separator).
     * @param qb The {@link com.amalto.core.query.user.UserQueryBuilder builder} to use for condition build.
     */
    private static void buildGetById(ComplexTypeMetadata type, String id, UserQueryBuilder qb) {
        Collection<FieldMetadata> keyFields = type.getKeyFields();
        Iterator<FieldMetadata> keyFieldIterator = keyFields.iterator();
        if (keyFields.size() == 1) {
            qb.where(eq(keyFieldIterator.next(), id));
        } else {
            StringTokenizer idValues = new StringTokenizer(id, "."); //$NON-NLS-1$
            int tokenCount = idValues.countTokens();
            if (tokenCount < keyFields.size()) {
                String message = "Expected " + keyFields.size() + " values for id but got only " + tokenCount + " in '" + id + "'."; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                LOGGER.error(message);
                throw new IllegalArgumentException(message);
            }
            while (keyFieldIterator.hasNext()) {
                if (!keyFieldIterator.hasNext()) {
                    // Handle left overs
                    StringBuilder builder = new StringBuilder();
                    while (idValues.hasMoreTokens()) {
                        builder.append(idValues.nextToken());
                        if (idValues.hasMoreTokens()) {
                            builder.append('.');
                        }
                    }
                    qb.where(eq(keyFieldIterator.next(), builder.toString()));
                } else {
                    qb.where(eq(keyFieldIterator.next(), idValues.nextToken()));
                }
            }
        }
    }

    /**
     * Ensures the storage name is correct according to <code>storageType</code>. If storage type is
     * {@link StorageType#STAGING} but storage name does not end with {@link StorageAdmin#STAGING_SUFFIX}, this method
     * adds it.
     * 
     * @param storageName A non-null storage name.
     * @param storageType A {@link StorageType storage type}.
     * @return The name of the storage with proper naming.
     */
    private static String getStorageName(String storageName, String storageType) {
        if (storageName == null) {
            LOGGER.error("Storage name cannot be null."); //$NON-NLS-1$
            throw new IllegalArgumentException("Storage name cannot be null."); //$NON-NLS-1$
        }
        if (getStorageType(storageType) == StorageType.STAGING) {
            if (!storageName.endsWith(StorageAdmin.STAGING_SUFFIX)) {
                storageName += StorageAdmin.STAGING_SUFFIX;
            } else {
                // Keep as is (already ends with StorageAdmin.STAGING_SUFFIX).
                return storageName;
            }
        }
        return storageName;
    }

    /**
     * @param type A string representation of {@link StorageType storage type}.
     * @return The {@link StorageType type} for <code>type</code>.
     * @throws IllegalArgumentException If <code>type</code> is not a member of {@link StorageType}.
     */
    protected static StorageType getStorageType(String type) {
        // Select right storage type following query param
        StorageType storageType;
        try {
            storageType = StorageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            String message = "Container '" + type + "' does not exist (please specify one of [MASTER, STAGING])."; //$NON-NLS-1$ //$NON-NLS-2$
            LOGGER.error(message, e);
            throw new IllegalArgumentException(message); 
        }
        return storageType;
    }

    private static Response handleReadQuery(Request request, final Storage storage, Select expression,
            SecuredStorage.UserDelegator delegator) {
        storage.begin();
        try {
            final StorageResults records = storage.fetch(expression);
            // Select correct writer
            final DataRecordWriter writer;
            Variant variant = request.selectVariant(queryVariants);
            final MediaType mediaType;
            if (variant != null && MediaType.APPLICATION_JSON_TYPE.equals(variant.getMediaType())) {
                mediaType = MediaType.APPLICATION_JSON_TYPE;
            } else {
                mediaType = MediaType.APPLICATION_XML_TYPE;
            }        
            // Select correct writer based on requested media type
            if (MediaType.APPLICATION_XML_TYPE.equals(mediaType)) {
                if (expression.isProjection()) {
                    writer = new DataRecordXmlWriter("result"); //$NON-NLS-1$
                } else {
                    writer = new DataRecordXmlWriter();
                }
            } else if (MediaType.APPLICATION_JSON_TYPE.equals(mediaType)) {
                writer = new DataRecordJSONWriter();
            } else {
                String message = "Unable to select correct writer class."; //$NON-NLS-1$
                LOGGER.error(message);
                throw new IllegalStateException(message);
            }
            writer.setSecurityDelegator(delegator);
            StreamingOutput output = new StreamingOutput() {

                @Override
                public void write(OutputStream output) throws IOException, WebApplicationException {
                    try {
                        // Write header
                        if (MediaType.APPLICATION_XML_TYPE.equals(mediaType)) {
                            output.write("<results>".getBytes()); //$NON-NLS-1$
                        } else if (MediaType.APPLICATION_JSON_TYPE.equals(mediaType)) {
                            output.write("[".getBytes()); //$NON-NLS-1$
                        }
                        // Write results
                        {
                            Iterator<DataRecord> iterator = records.iterator();
                            while (iterator.hasNext()) {
                                DataRecord record = iterator.next();
                                writer.write(record, output);
                                if (iterator.hasNext() && MediaType.APPLICATION_JSON_TYPE.equals(mediaType)) {
                                    output.write(",".getBytes()); //$NON-NLS-1$
                                }
                            }
                        }
                        // Write footer
                        if (MediaType.APPLICATION_XML_TYPE.equals(mediaType)) {
                            output.write("</results>".getBytes()); //$NON-NLS-1$
                        } else if (MediaType.APPLICATION_JSON_TYPE.equals(mediaType)) {
                            output.write("]".getBytes()); //$NON-NLS-1$
                        }
                        output.flush();
                        storage.commit();
                    } catch (Exception e) {
                        storage.rollback();
                        String message = "Error occurred during result writing."; //$NON-NLS-1$
                        LOGGER.error(message, e);
                        throw new RuntimeException(message, e);
                    }
                }
            };
            return Response.ok(output).type(mediaType).build();
        } catch (Exception e) {
            storage.rollback();
            String message = "Could not execute query."; //$NON-NLS-1$
            LOGGER.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    private static void handleDeleteQuery(Storage storage, Select select) {
        // Delete the record
        storage.begin();
        try {
            storage.delete(select);
            storage.commit();
        } catch (Exception e) {
            storage.rollback();
            String message = "Unable to delete record."; //$NON-NLS-1$
            LOGGER.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    private static Throwable getRootException(Throwable e) {
        Throwable root = e;
        while(root != null && root.getCause() != null && root.getCause().getMessage() != null) {
            root = root.getCause();
        }
        return root;
    }

    private Map<String, Object> doValidation(String storageName, String storageType, boolean invokeBeforeSaving, InputStream content) {
        DataRecord.ValidateRecord.set(true);
        boolean isValid = true;
        String message = StringUtils.EMPTY;

        SaverSession session = SaverSession.newSession();
        Committer committer = new RecordValidationCommitter();
        DocumentSaverContext context = session.getContextFactory().createValidation(getStorageName(storageName, storageType),
                storageName, invokeBeforeSaving, content);
        DocumentSaver saver = context.createSaver();
        try {
            session.begin(storageName, committer);
            saver.save(session, context);
            session.end(committer);
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Record validataion failed.", e); //$NON-NLS-1$
            }
            isValid = false;
            message = getRootException(e).getMessage();
        } finally {
            session.abort(committer);
            DataRecord.ValidateRecord.remove();
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("isValid", isValid); //$NON-NLS-1$
        result.put("message", message); //$NON-NLS-1$
        return result;
    }

    @GET
    @Path("{containerName}/{type}")
    @ApiOperation("Lists all primary keys for container and type")
    public Response listIds(@Context Request request, 
            @ApiParam(value="Container type", allowableValues="MASTER, STAGING, SYSTEM") @QueryParam("container") @DefaultValue("MASTER") String storageType, 
            @ApiParam("Container name") @PathParam("containerName") String storageName, 
            @ApiParam("Data type") @PathParam("type") String typeName) {
        // Get storage for records
        Server server = ServerContext.INSTANCE.get();
        StorageAdmin admin = server.getStorageAdmin();
        Storage storage = admin.get(storageName, getStorageType(storageType));
        // Create query to retrieve the record
        MetadataRepository metadataRepository = storage.getMetadataRepository();
        ComplexTypeMetadata type = metadataRepository.getComplexType(typeName);
        UserQueryBuilder qb = from(type).selectId(type);
        final SecuredStorage.UserDelegator delegator = SecuredStorage.getDelegator();
        return handleReadQuery(request, storage, qb.getSelect(), delegator);
    }

    @POST
    @Path("{containerName}")
    @ApiOperation("Creates a new record in the container. The record will be provided in the request content as XML")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public void createRecord(
            @ApiParam("Container name") @PathParam("containerName") String storageName,
            @ApiParam(value="Container type", allowableValues="MASTER, STAGING, SYSTEM") @QueryParam("container") @DefaultValue("MASTER") String storageType, 
            InputStream content) {
        SaverSession session = SaverSession.newSession();
        DocumentSaverContext context = session.getContextFactory().create(getStorageName(storageName, storageType), storageName,
                UpdateReportPOJO.SERVICE_SOURCE, content, true, true, true, false, false);
        context.setUserAction(UserAction.CREATE);
        DocumentSaver saver = context.createSaver();
        try {
            session.begin(storageName);
            saver.save(session, context);
            session.end();
        } catch (Exception e) {
            session.abort();
            String message = "Unable to create record."; //$NON-NLS-1$
            LOGGER.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    /**
     * Create a set of records wrapped in a top level XML element. All records are created using
     * {@link UserAction#AUTO_STRICT} mode.
     * 
     * @param storageName The container to write records to.
     * @param storageType A value of {@link StorageType}.
     * @param content All the records wrapped in a top level element.
     */
    @POST
    @Path("{containerName}/batch")
    @ApiOperation("Creates or updates records in the container in batch mode. Records will be provided in the request content as XML")
    public void createBatch(
            @ApiParam("Container name") @PathParam("containerName") String storageName, 
            @ApiParam(value="Container type", allowableValues="MASTER, STAGING, SYSTEM") @QueryParam("container") @DefaultValue("MASTER") String storageType, 
            InputStream content) {
        List<String> allDocs = new ArrayList<String>();
        XMLStreamUnwrapper tokenizer = new XMLStreamUnwrapper(content);
        while (tokenizer.hasMoreElements()) {
            allDocs.add(tokenizer.nextElement());
        }
        
        SaverSession session = SaverSession.newSession();
        final TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
        boolean createOwnTransaction = true;
        if(transactionManager.hasTransaction()){
            Transaction currentTransaction = transactionManager.currentTransaction();
            if(currentTransaction.getLifetime() == Lifetime.LONG){
                createOwnTransaction = false;
            }
        }
        Transaction ownTransaction = null;
        if(createOwnTransaction){
            ownTransaction = transactionManager.create(Transaction.Lifetime.LONG);
        }
        try {
            if(ownTransaction != null){
                ownTransaction.begin();
            }
            session.begin(storageName);
            {
                for (String doc : allDocs) {
                    DocumentSaverContext context = session.getContextFactory().create(getStorageName(storageName, storageType),
                            storageName, false, new ByteArrayInputStream(doc.getBytes("UTF-8"))); //$NON-NLS-1$
                    context.setUserAction(UserAction.AUTO_STRICT);
                    DocumentSaver saver = context.createSaver();
                    saver.save(session, context);
                }
            }
            session.end();
            if(ownTransaction != null){
                ownTransaction.commit();
            }
            
        } catch (Exception e) {
            session.abort();
            if(ownTransaction != null){
                ownTransaction.rollback();
            }
            LOGGER.warn("Unable to commit batch, trying to create one by one.", e); //$NON-NLS-1$
            List<String> errorDocs = new ArrayList<String>();
            for (String doc : allDocs) {
                try {
                    byte[] docBytes = doc.getBytes("UTF-8"); //$NON-NLS-1$
                    createRecord(storageName, storageType, new ByteArrayInputStream(docBytes));
                } catch (Exception e1) {
                    errorDocs.add(doc);
                    if (e1 instanceof UnsupportedEncodingException) {
                        LOGGER.error("Unable to create record.", e1); //$NON-NLS-1$
                    }
                }
            }       
            if (errorDocs.size() > 0) {
                LOGGER.error("Unable to commit the whole batch, " + errorDocs.size() + " records failed.\n"); //$NON-NLS-1$
                for (String doc : errorDocs) {
                    LOGGER.error(doc + "\n"); //$NON-NLS-1$
                }
                throw new RuntimeException("Unable to commit the whole batch, " + errorDocs.size() + " records failed."); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    @PUT
    @Path("{containerName}")
    @ApiOperation("Updates a record in the container. The record will be provided in the request content as XML")
    public void updateRecord(
            @ApiParam("Container name") @PathParam("containerName") String storageName, 
            @ApiParam(value="Container type", allowableValues="MASTER, STAGING, SYSTEM") @QueryParam("container") @DefaultValue("MASTER") String storageType, 
            InputStream content) {
        SaverSession session = SaverSession.newSession();
        DocumentSaverContext context = session.getContextFactory().create(getStorageName(storageName, storageType), 
                storageName, 
                UpdateReportPOJO.SERVICE_SOURCE, content, true, true, true, false, false);
        context.setUserAction(UserAction.UPDATE);
        DocumentSaver saver = context.createSaver();
        try {
            session.begin(storageName);
            saver.save(session, context);
            session.end();
        } catch (Exception e) {
            session.abort();
            String message = "Unable to update record."; //$NON-NLS-1$
            LOGGER.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    @PATCH
    @Path("{containerName}")
    @ApiOperation("Partially updates a record in the container. The record will be provided in the request content as XML")
    public void partialUpdateRecord(
            @ApiParam("Container name") @PathParam("containerName") String storageName, 
            @ApiParam(value="Container type", allowableValues="MASTER, STAGING, SYSTEM") @QueryParam("container") @DefaultValue("MASTER") String storageType, 
            InputStream content) {
        SaverSession session = SaverSession.newSession();
        DocumentSaverContext context = session.getContextFactory().createPartialUpdate(getStorageName(storageName, storageType), 
                storageName, 
                UpdateReportPOJO.SERVICE_SOURCE, content, true, true, StringUtils.EMPTY, StringUtils.EMPTY, 0, false);
        DocumentSaver saver = context.createSaver();
        try {
            session.begin(storageName);
            saver.save(session, context);
            session.end();
        } catch (Exception e) {
            session.abort();
            String message = "Unable to partial update record.";//$NON-NLS-1$
            LOGGER.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    @POST
    @Path("{containerName}/validate")
    @ApiOperation("Validate records in the container. The records will be provided in the request content as XML")
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public Response validateRecord(
            @ApiParam("Container name") @PathParam("containerName") String storageName,
            @ApiParam(value="Container type", allowableValues="MASTER, STAGING") @QueryParam("container") @DefaultValue("MASTER") String storageType,
            @ApiParam(value="Invoke beforeSaving") @QueryParam("beforeSaving") @DefaultValue("true") boolean invokeBeforeSaving,
            InputStream content) {
        try {
            if (!"MASTER".equalsIgnoreCase(storageType) && !"STAGING".equalsIgnoreCase(storageType)) { //$NON-NLS-1$ //$NON-NLS-2$
                throw new IllegalArgumentException("Container '" + storageType + "' is not valid (please specify one of [MASTER, STAGING])."); //$NON-NLS-1$ //$NON-NLS-2$
            }
            List<String> allDocs = new ArrayList<String>();
            XMLStreamUnwrapper tokenizer = new XMLStreamUnwrapper(content);
            while (tokenizer.hasMoreElements()) {
                allDocs.add(tokenizer.nextElement());
            }
            JSONArray results = new JSONArray();
            Map<String, Object> result;
            for (String doc : allDocs) {
                result = doValidation(storageName, storageType, invokeBeforeSaving, new ByteArrayInputStream(doc.getBytes("UTF-8"))); //$NON-NLS-1$
                results.put(result);
            }
            return Response.ok(results.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (Exception e) {
            Throwable root = getRootException(e);
            LOGGER.error(root.getMessage(), e);
            if(root instanceof IllegalArgumentException || root instanceof XMLStreamException) {
                return Response.status(Status.BAD_REQUEST).entity(root.getMessage()).build();
            } else {
                return Response.status(Status.INTERNAL_SERVER_ERROR).entity(root.getMessage()).build();
            }
        }
    }

    @DELETE
    @Path("{containerName}")
    @ApiOperation("Deletes a container")
    public void deleteContainer(
            @ApiParam("Container name") @PathParam("containerName") String storageName, 
            @ApiParam("If true all data will be deleted") @QueryParam("drop") boolean dropData) {
        Server server = ServerContext.INSTANCE.get();
        
        //closing deleting storage (master, staging, master)
        StorageAdmin admin = server.getStorageAdmin();
        admin.delete(storageName, dropData);
    }

    /**
     * Delete record(s) by query.
     *
     * @param storageName The storage (container) containing the record to delete.
     * @param queryText A query using JSON-based query language.
     */
    @PUT
    @Path("{containerName}/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation("Deletes data records by query. Query is provided in the request content as JSON")
    public void deleteByQuery(
            @ApiParam("Container name") @PathParam("containerName") String storageName, 
            @ApiParam(value="Container type", allowableValues="MASTER, STAGING, SYSTEM") @QueryParam("container") @DefaultValue("MASTER") String storageType, 
            InputStream queryText) {
        // Get storage for records
        Server server = ServerContext.INSTANCE.get();
        StorageAdmin admin = server.getStorageAdmin();
        Storage storage = admin.get(storageName, getStorageType(storageType));
        // Create query to retrieve the record
        QueryParser parser = QueryParser.newParser(storage.getMetadataRepository());
        Expression expression = parser.parse(queryText);
        // Delete the record
        handleDeleteQuery(storage, (Select) expression);
    }

    /**
     * Delete a record by id.
     *
     * @param storageName The storage (container) containing the record to delete.
     * @param typeName The type of the record to delete.
     * @param id The id of the record to delete. In case of composite key, expects values to be separated using '.'.
     */
    @DELETE
    @Path("{containerName}/{type}/{id}")
    @ApiOperation("Deletes a record by its id.")
    public void deleteById(
            @ApiParam("Container name") @PathParam("containerName") String storageName, 
            @ApiParam(value="Container type", allowableValues="MASTER, STAGING, SYSTEM") @QueryParam("container") @DefaultValue("MASTER") String storageType, 
            @ApiParam(value="Data type") @PathParam("type") String typeName,
            @ApiParam(value="Primary key of the record to delete") @PathParam("id") String id, 
            @ApiParam(value="if true generate update report", allowableValues="true, false") @QueryParam("updateReport") @DefaultValue("true") boolean updateReport ) {
        // Get storage for records
        Server server = ServerContext.INSTANCE.get();
        StorageAdmin admin = server.getStorageAdmin();
        Storage storage = admin.get(storageName, getStorageType(storageType));
        // Create query to retrieve the record
        MetadataRepository metadataRepository = storage.getMetadataRepository();
        ComplexTypeMetadata type = metadataRepository.getComplexType(typeName);
        UserQueryBuilder qb = from(type);
        buildGetById(type, id, qb);
        Select select = qb.getSelect();
        handleDeleteQuery(storage, select);
        if("MASTER".equals(storageType) && updateReport) { //$NON-NLS-1$
            SaverSession session = SaverSession.newSession();
            try {
                ILocalUser user = LocalUser.getLocalUser();
                Map<String, UpdateReportItemPOJO> updateReportItemsMap = new HashMap<String, UpdateReportItemPOJO>();
                String userName = user.getUsername();
                UpdateReportPOJO updateReportPOJO = new UpdateReportPOJO(typeName, id, UpdateReportPOJO.OPERATION_TYPE_PHYSICAL_DELETE, 
                        UpdateReportPOJO.SERVICE_SOURCE, System.currentTimeMillis(), storageName, storageName, userName, updateReportItemsMap);
                String xmlString = updateReportPOJO.serialize();                
                SaverContextFactory contextFactory = session.getContextFactory();
                DocumentSaverContext context = contextFactory.create(UpdateReportPOJO.DATA_CLUSTER,
                        UpdateReportPOJO.DATA_MODEL,
                        true,
                        new ByteArrayInputStream(xmlString.getBytes("UTF-8"))); //$NON-NLS-1$
                DocumentSaver saver = context.createSaver();
                session.begin(UpdateReportPOJO.DATA_CLUSTER);
                saver.save(session, context);
                session.end();
            } catch (Exception e) {
                session.abort();
                String message = "Unable to create update report."; //$NON-NLS-1$
                LOGGER.error(message, e);
                throw new RuntimeException(message, e);
            }
        }
    }

    /**
     * Executes a query against a storage, query is defined using a JSON-based string.
     * 
     * @param request The current request (used to determine best output format).
     * @param storageName The name of the storage (container) to query.
     * @param queryText A query using JSON-based query language.
     * @return A streamed {@link javax.ws.rs.core.Response response} containing query results, or empty if no result to
     * be found.
     * @see org.talend.mdm.query.QueryParser
     */
    @PUT
    @Path("{containerName}/query")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation("Get data records by query. Query is provided in the request content as JSON")
    public Response executeQuery(@Context Request request, 
            @ApiParam("Container name") @PathParam("containerName") String storageName, 
            @ApiParam(value="Container type", allowableValues="MASTER, STAGING, SYSTEM") @QueryParam("container") @DefaultValue("MASTER") String type, 
            InputStream queryText) {
        // Find storage
        final StorageType storageType = getStorageType(type);
        StorageAdmin admin = ServerContext.INSTANCE.get().getStorageAdmin();
        Storage innerStorage = admin.get(storageName, storageType);
        if (innerStorage == null) {
            String message = "Container '" + storageName + "' does not exist."; //$NON-NLS-1$ //$NON-NLS-2$
            LOGGER.error(message);
            throw new IllegalArgumentException(message);
        }
        // Adds history browsing extensions...
        Storage updateReportStorage = admin.get(XSystemObjects.DC_UPDATE_PREPORT.getName(), StorageType.MASTER);
        ActionFactory actionFactory = new StorageActionFactory(updateReportStorage);
        StorageDocumentFactory documentFactory = new StorageDocumentFactory();
        innerStorage = new HistoryStorage(innerStorage, DocumentHistoryFactory.getInstance().create(actionFactory,
                documentFactory));
        // ... then adds security over it
        final SecuredStorage.UserDelegator delegator = SecuredStorage.getDelegator();
        final Storage storage = new SecuredStorage(innerStorage, delegator);
        // Parse query
        QueryParser parser = QueryParser.newParser(storage.getMetadataRepository());
        Expression expression = parser.parse(queryText);
        // Check expression is compatible with storage type
        Set<Expression> incompatibleExpressions = expression.accept(new IncompatibleExpressions(storageType));
        if (incompatibleExpressions.isEmpty()) {
            // No incompatible expressions, proceed to query execution
            return handleReadQuery(request, storage, (Select) expression, delegator);
        } else {
            StringBuilder builder = new StringBuilder();
            for (Expression incompatibleExpression : incompatibleExpressions) {
                builder.append(incompatibleExpression.toString()).append(' ');
            }
            String message = "Unable to execute query due to incompatible expressions (" + builder + ")."; //$NON-NLS-1$ //$NON-NLS-2$
            LOGGER.error(message);
            throw new RuntimeException(message);
        }
    }

    /**
     * Returns a record by id ("get by id" query). Service supports history browsing (with "swing" and "dateTime"
     * parameter).
     * 
     * @param request The current request (used to determine best output format).
     * @param storageName The name of the storage (container) to query.
     * @param typeName The type name to query.
     * @param id The id of the record to return. In case of composite key, expects values to be separated using '.'.
     * @param dateTime An optional date time for history browsing. This parameter is used as a "go to date" parameter
     * and may either contains a time as milliseconds (since EPOCH) or a XML formatted date.
     * @param swing An optional parameter to use to move back and forth the history.
     * @return A streamed {@link javax.ws.rs.core.Response response} containing query results, or empty if no result to
     * be found.
     * @see com.amalto.core.query.user.At.Swing
     * @see com.amalto.core.query.user.UserQueryBuilder#at(java.lang.String)
     */
    @GET
    @Path("{containerName}/{type}/{id}")
    @ApiOperation("Get a data record by id.")
    public Response getRecord(@Context Request request, 
            @ApiParam("Container name") @PathParam("containerName") String storageName, 
            @ApiParam(value="Container type", allowableValues="MASTER, STAGING, SYSTEM") @QueryParam("container") @DefaultValue("MASTER") String storageType, 
            @ApiParam("Data type") @PathParam("type") String typeName, 
            @ApiParam("Record id") @PathParam("id") String id, 
            @ApiParam("Show record as it was at provided date. Date can be provided as number of milliseconds since EPOCH or a XML formatted date") @QueryParam("datetime") String dateTime, 
            @ApiParam(value="Controls how datetime is interpreted", allowableValues="CLOSEST, BEFORE, AFTER") @QueryParam("swing") String swing) {
        // Get storage for records
        Server server = ServerContext.INSTANCE.get();
        StorageAdmin admin = server.getStorageAdmin();
        Storage storage = admin.get(storageName, getStorageType(storageType));
        // Create query to retrieve the record
        MetadataRepository metadataRepository = storage.getMetadataRepository();
        ComplexTypeMetadata type = metadataRepository.getComplexType(typeName);
        UserQueryBuilder qb = from(type);
        buildGetById(type, id, qb);
        // Adds history browsing related information
        if (dateTime != null) {
            qb.at(dateTime).swing(swing);
            Storage updateReportStorage = admin.get(XSystemObjects.DC_UPDATE_PREPORT.getName(), StorageType.MASTER);
            ActionFactory actionFactory = new StorageActionFactory(updateReportStorage);
            StorageDocumentFactory documentFactory = new StorageDocumentFactory();
            storage = new HistoryStorage(storage, DocumentHistoryFactory.getInstance().create(actionFactory, documentFactory));
        }
        final SecuredStorage.UserDelegator delegator = SecuredStorage.getDelegator();
        return handleReadQuery(request, storage, qb.getSelect(), delegator);
    }
    
    /**
     * Get the array of documents uniqueIDs in a container (of specific type)
     * 
     * @param containerName The name of the storage (container) to query.
     * @param typeName The type name to query.(if null, return all types' documents ids, else return this type's)
     * @return
     */
    @GET
    @Path("{containerName}/documents/uniqueIds")
    @ApiOperation("Get all documents unique ids as array")
    public Response getAllDocumentsUniqueID(
            @ApiParam(value = "Container name") @PathParam("containerName") String containerName, 
            @ApiParam(value = "Data type") @QueryParam("type") String typeName) {
        String clusterName = StringUtils.isBlank(typeName) ? containerName : containerName + '/' + typeName;
        try {
            String[] output = new SQLWrapper().getAllDocumentsUniqueID(clusterName);
            return Response.ok(output).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (XmlServerException e) {
            String message = "Unable to get documents unique ids."; //$NON-NLS-1$
            LOGGER.error(message, e);
            throw new RuntimeException(message, e);
        }
    }
    
    /**
     * Gets an XML document from the DB
     * 
     * @param containerName The name of the storage (container) to query.
     * @param uniqueId The unique ID of the document
     * @param encoding The encoding of the XML instruction (e.g. UTF-16, UTF-8, etc...).
     * @return
     */
    @GET
    @Path("{containerName}/documents/{uniqueId}")
    @ApiOperation("Gets an XML document from the DB")
    public Response getDocumentAsString(
            @ApiParam(value = "Container name") @PathParam("containerName") String containerName, 
            @ApiParam(value = "Unique ID") @PathParam("uniqueId") String uniqueId,
            @ApiParam(value = "Encoding") @QueryParam("encoding") @DefaultValue("UTF-8") String encoding) {
        try {
            String output = new SQLWrapper().getDocumentAsString(containerName, uniqueId, encoding);
            return Response.ok(output).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (XmlServerException e) {
            String message = "Unable to get document by unique id."; //$NON-NLS-1$
            LOGGER.error(message, e);
            throw new RuntimeException(message, e);
        }
    }
    
    /**
     * Gets many XML document from the DB
     * 
     * @param containerName The name of the storage (container) to query.
     * @param uniqueIds The unique IDs of the documents (it may contains "/" like "Product/Product.Product.1", so use QueryParam not PathParam)
     * @param encoding The encoding of the XML instruction (e.g. UTF-16, UTF-8, etc...).
     * @return
     */
    @POST
    @Path("{containerName}/documents")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets many XML documents from the DB")
    public Response getDocumentsAsString(
            @ApiParam(value = "Container name") @PathParam("containerName") String containerName, 
            @ApiParam(value = "Unique IDs") @Multipart(value = "uniqueIds") String[] uniqueIds,
            @ApiParam(value = "Encoding") @QueryParam("encoding") @DefaultValue("UTF-8") String encoding) {
        try {
            String[] output = new SQLWrapper().getDocumentsAsString(containerName, uniqueIds, encoding);
            return Response.ok(output).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (XmlServerException e) {
            String message = "Unable to get document by unique ids."; //$NON-NLS-1$
            LOGGER.error(message, e);
            throw new RuntimeException(message, e);
        }
    }
}