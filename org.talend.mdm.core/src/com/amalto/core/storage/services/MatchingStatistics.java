/*
 * Copyright (C) 2006-2014 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.services;

import static com.amalto.core.query.user.UserQueryBuilder.*;
import static com.amalto.core.query.user.UserStagingQueryBuilder.groupSize;

import java.io.StringWriter;
import java.util.Locale;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.amalto.core.storage.StagingStorage;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONWriter;
import org.talend.mdm.commmon.metadata.*;

import com.amalto.core.query.user.Expression;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.record.DataRecord;

@Path("/system/stats/matching") //$NON-NLS-1$
public class MatchingStatistics {

    private static final Logger LOGGER = Logger.getLogger(MatchingStatistics.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{container}") //$NON-NLS-1$
    public Response getMatchingStatistics(@PathParam("container") String containerName, @QueryParam("lang") String language) { //$NON-NLS-1$ //$NON-NLS-2$
        StorageAdmin storageAdmin = ServerContext.INSTANCE.get().getStorageAdmin();
        Storage dataStorage = storageAdmin.get(containerName, StorageType.STAGING, null);
        if (dataStorage == null) {
            Storage systemStorage = storageAdmin.get(StorageAdmin.SYSTEM_STORAGE, StorageType.SYSTEM, null);
            if (systemStorage == null) { // is xmldb, not supported
                LOGGER.debug("Could not find system storage. Statistics is not supported for XMLDB"); //$NON-NLS-1$
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            throw new IllegalArgumentException("Container '" + containerName + "' does not exist.");
        }
        // Build statistics
        StringWriter stringWriter = new StringWriter();
        JSONWriter writer = new JSONWriter(stringWriter);
        MetadataRepository repository = dataStorage.getMetadataRepository();
        try {
            dataStorage.begin();
            writer.object().key("matching"); //$NON-NLS-1$
            {
                writer.array();
                {
                    for (ComplexTypeMetadata type : repository.getUserComplexTypes()) {
                        if (StagingStorage.EXECUTION_LOG_TYPE.equals(type.getName())) {
                            continue;
                        }
                        /*
                         * Eligible types for matching are:
                         *  - single key types.
                         *  - key field type must be xsd:string.
                         */
                        if (type.getKeyFields().size() > 1) {
                            continue; // not eligible
                        }
                        FieldMetadata keyField = type.getKeyFields().iterator().next();
                        if (!Types.STRING.equals(MetadataUtils.getSuperConcreteType(keyField.getType()).getName())) {
                            continue; // not eligible
                        }
                        writer.object();
                        {
                            Expression count = from(type).select(alias(count(), "count")).where(gt(groupSize(), "2")) //$NON-NLS-1$ //$NON-NLS-2$
                                    .where(eq(keyField, taskId())).cache().getExpression();
                            StorageResults typeCount = dataStorage.fetch(count);
                            long countValue = 0;
                            for (DataRecord record : typeCount) {
                                countValue = (Long) record.get("count"); //$NON-NLS-1$
                            }
                            // Starts stats for type
                            String name;
                            if (language != null) {
                                name = type.getName(new Locale(language));
                            } else {
                                name = type.getName();
                            }
                            writer.key(name).value(countValue);
                        }
                        writer.endObject();
                    }
                }
                writer.endArray();
            }
            writer.endObject();
            dataStorage.commit();
        } catch (JSONException e) {
            dataStorage.rollback();
            throw new RuntimeException("Could not provide statistics.", e);
        }
        return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(stringWriter.toString())
                .header("Access-Control-Allow-Origin", "*").build(); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
