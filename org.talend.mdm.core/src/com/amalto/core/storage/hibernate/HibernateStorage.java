/*
 * Copyright (C) 2006-2014 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import com.amalto.core.integrity.ForeignKeyIntegrity;
import com.amalto.core.metadata.MetadataUtils;
import com.amalto.core.query.optimization.*;
import com.amalto.core.query.user.*;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.transaction.StorageTransaction;
import com.amalto.core.storage.transaction.TransactionManager;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Level;
import org.hibernate.Session;
import org.hibernate.cfg.Environment;
import org.hibernate.search.FullTextSession;
import org.talend.mdm.commmon.metadata.*;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.RDBMSDataSource;
import com.amalto.core.storage.prepare.*;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordConverter;
import com.amalto.core.storage.record.metadata.DataRecordMetadata;
import net.sf.ehcache.CacheManager;
import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.search.MassIndexer;
import org.hibernate.search.Search;
import org.hibernate.search.event.ContextHolder;
import org.hibernate.search.impl.SearchFactoryImpl;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.hbm2ddl.SchemaValidator;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;

public class HibernateStorage implements Storage {

    public static final HibernateStorage.LocalEntityResolver ENTITY_RESOLVER = new HibernateStorage.LocalEntityResolver();

    private static final String CLASS_LOADER = "com.amalto.core.storage.hibernate.DefaultStorageClassLoader"; //$NON-NLS-1$

    private static final String ALTERNATE_CLASS_LOADER = "com.amalto.core.storage.hibernate.FullStorageClassLoader"; //$NON-NLS-1$

    private static final Logger LOGGER = Logger.getLogger(HibernateStorage.class);

    private static final Optimizer[] OPTIMIZERS = new Optimizer[]{
            new RangeOptimizer(), // Transforms (value > n AND value < p) into (RANGE(n,p)).
            new ContainsOptimizer(), // Transforms all '*' in CONTAINS into '%'.
            new UpdateReportOptimizer() // Adds queries on super types if update report query a concept name with super types.
    };

    private static final String FORBIDDEN_PREFIX = "x_talend_"; //$NON-NLS-1$

    private static final MetadataChecker METADATA_CHECKER = new MetadataChecker();

    // Default value is "true" (meaning the storage will try to create database if it doesn't exist).
    private static final boolean autoPrepare = Boolean.valueOf(MDMConfiguration.getConfiguration().getProperty(
            "db.autoPrepare", "true")); //$NON-NLS-1$ //$NON-NLS-2$

    private static final Boolean FLUSH_ON_LOAD = Boolean.valueOf(MDMConfiguration.getConfiguration().getProperty("db.flush.on.load", "false")); //$NON-NLS-1$ //$NON-NLS-2$

    private final String storageName;

    private final StorageType storageType;

    private MappingRepository mappingRepository;

    private InternalRepository typeMappingRepository;

    private ClassCreator hibernateClassCreator;

    private StorageClassLoader storageClassLoader;

    private boolean isPrepared = false;

    private SessionFactory factory;

    private Configuration configuration;

    private RDBMSDataSource dataSource;

    private MetadataRepository userMetadataRepository;

    private TableResolver tableResolver;

    private int batchSize;

    /**
     * Create a {@link StorageType#MASTER} storage.
     *
     * @param storageName Name for this storage. <b>by convention</b>, this is the MDM container name.
     * @see StorageType#MASTER
     */
    public HibernateStorage(String storageName) {
        this(storageName, StorageType.MASTER);
    }

    /**
     * @param storageName Name for this storage. <b>By convention</b>, this is the MDM container name.
     * @param type        Tells whether this storage is a staging area or not.
     * @see StorageType
     */
    public HibernateStorage(String storageName, StorageType type) {
        this.storageName = storageName;
        this.storageType = type;
    }

    @Override
    public Storage asInternal() {
        return this;
    }

    @Override
    public int getCapabilities() {
        int capabilities = CAP_TRANSACTION | CAP_INTEGRITY;
        if (dataSource.supportFullText()) {
            capabilities |= CAP_FULL_TEXT;
        }
        return capabilities;
    }

    @Override
    public synchronized StorageTransaction newStorageTransaction() {
        assertPrepared();
        org.hibernate.classic.Session session = factory.openSession();
        session.setFlushMode(FlushMode.MANUAL);
        return new HibernateStorageTransaction(this, session);
    }

    @Override
    public void init(DataSource dataSource) {
        // Stateless components
        if (dataSource == null) {
            throw new IllegalArgumentException("Data source can not be null.");
        }
        if (!(dataSource instanceof RDBMSDataSource)) {
            throw new IllegalArgumentException("Data source is expected to be a RDBMS data source.");
        }
        if (dataSource.isShared()) {
            LOGGER.warn("Datasource '" + dataSource.getName() + "' (for storage type: " + storageType + ") is shared " +
                    "with at least another one other storage type, please review datasource configuration.");
        }
        this.dataSource = (RDBMSDataSource) dataSource;
        internalInit();
    }

    private void internalInit() {
        if (!dataSource.supportFullText()) {
            LOGGER.warn("Storage '" + storageName + "' (" + storageType + ") is not configured to support full text queries.");
        }
        configuration = new Configuration();
        // Setting our own entity resolver allows to ensure the DTD found/used are what we expect (and not potentially
        // one provided by the application server).
        configuration.setEntityResolver(ENTITY_RESOLVER);
    }

    @Override
    public synchronized void prepare(MetadataRepository repository,
                                     Set<Expression> optimizedExpressions,
                                     boolean force,
                                     boolean dropExistingData) {
        if (!force && isPrepared) {
            return; // No op operation
        }
        if (isPrepared) {
            close();
            internalInit();
        }
        if (dataSource == null) {
            throw new IllegalArgumentException("Datasource is not set.");
        }
        // No support for data models including inheritance AND for g* XSD simple types AND fields that start with
        // X_TALEND_
        try {
            MetadataUtils.sortTypes(repository); // Do a "sort" to ensure there's no cyclic dependency.
            repository.accept(METADATA_CHECKER);
            userMetadataRepository = repository;
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred during unsupported features check.", e);
        }
        // Create class loader for storage's dynamically created classes.
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Class<? extends StorageClassLoader> clazz;
        try {
            try {
                clazz = (Class<? extends StorageClassLoader>) Class.forName(ALTERNATE_CLASS_LOADER);
            } catch (ClassNotFoundException e) {
                clazz = (Class<? extends StorageClassLoader>) Class.forName(CLASS_LOADER);
            }
            Constructor<? extends StorageClassLoader> constructor = clazz.getConstructor(ClassLoader.class,
                    String.class,
                    RDBMSDataSource.DataSourceDialect.class,
                    StorageType.class);
            storageClassLoader = constructor.newInstance(contextClassLoader,
                    storageName,
                    dataSource.getDialectName(),
                    storageType);
            storageClassLoader.setDataSourceConfiguration(dataSource);
            storageClassLoader.generateHibernateConfig(); // Checks if configuration can be generated.
        } catch (Exception e) {
            throw new RuntimeException("Could not create storage class loader", e);
        }
        if (dropExistingData) {
            LOGGER.info("Cleaning existing database content.");
            StorageCleaner cleaner = new JDBCStorageCleaner(new FullTextIndexCleaner());
            cleaner.clean(this);
        } else {
            LOGGER.info("*NOT* cleaning existing database content.");
        }
        if (autoPrepare) {
            LOGGER.info("Preparing database before schema generation.");
            StorageInitializer initializer = new JDBCStorageInitializer();
            if (initializer.supportInitialization(this)) {
                if (!initializer.isInitialized(this)) {
                    initializer.initialize(this);
                } else {
                    LOGGER.info("Database is already prepared.");
                }
            } else {
                LOGGER.info("Datasource is not configured for automatic initialization.");
            }
        } else {
            LOGGER.info("*NOT* preparing database before schema generation.");
        }
        try {
            Thread.currentThread().setContextClassLoader(storageClassLoader);
            // Mapping of data model types to RDBMS (i.e. 'flatten' representation of types).
            MetadataRepository internalRepository;
            try {
                InternalRepository typeEnhancer = getTypeEnhancer();
                internalRepository = userMetadataRepository.accept(typeEnhancer);
                mappingRepository = typeEnhancer.getMappings();
            } catch (Exception e) {
                throw new RuntimeException("Exception occurred during type mapping creation.", e);
            }
            // Set fields to be indexed in database.
            Set<FieldMetadata> databaseIndexedFields = new HashSet<FieldMetadata>();
            switch (storageType) {
                case MASTER:
                    // Adds indexes on user defined fields
                    for (Expression optimizedExpression : optimizedExpressions) {
                        Collection<FieldMetadata> indexedFields = RecommendedIndexes.get(optimizedExpression);
                        for (FieldMetadata indexedField : indexedFields) {
                            // TMDM-5896: Don't index Composite Key fields
                            if (indexedField instanceof CompoundFieldMetadata) {
                                continue;
                            }
                            // TMDM-5311: Don't index TEXT fields
                            TypeMetadata indexedFieldType = indexedField.getType();
                            if (!isIndexable(indexedFieldType)) {
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("Ignore index on field '" + indexedField.getName() + "' because value is stored in TEXT.");
                                }
                                continue;
                            }
                            // Go up the containment tree in case containing type is anonymous.
                            ComplexTypeMetadata containingType = indexedField.getContainingType().getEntity();
                            TypeMapping mapping = mappingRepository.getMappingFromUser(containingType);
                            FieldMetadata databaseField = mapping.getDatabase(indexedField);
                            if (!isIndexable(databaseField.getType())) {
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("Ignore index on field '" + indexedField.getName() + "' because value (in database mapping) is stored in TEXT.");
                                }
                                continue; // Don't take into indexed fields long text fields
                            }
                            databaseIndexedFields.add(databaseField);
                            if (!databaseField.getContainingType().isInstantiable()) {
                                Collection<ComplexTypeMetadata> roots = RecommendedIndexes.getRoots(optimizedExpression);
                                for (ComplexTypeMetadata root : roots) {
                                    List<FieldMetadata> path = MetadataUtils.path(mappingRepository.getMappingFromUser(root).getDatabase(), databaseField);
                                    if (path.size() > 1) {
                                        databaseIndexedFields.addAll(path.subList(0, path.size() - 1));
                                    } else {
                                        LOGGER.warn("Failed to properly index field '" + databaseField + "'.");
                                    }
                                }
                            }
                        }
                    }
                    break;
                case STAGING:
                     // Adds "staging status" as indexed field
                    if (!optimizedExpressions.isEmpty()) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Ignoring " + optimizedExpressions.size() + " to optimize (disabled on staging area).");
                        }
                    }
                    for (TypeMapping typeMapping : mappingRepository.getAllTypeMappings()) {
                        ComplexTypeMetadata database = typeMapping.getDatabase();
                        if (database.hasField(METADATA_STAGING_STATUS)) {
                            databaseIndexedFields.add(database.getField(METADATA_STAGING_STATUS));
                        }
                        if (database.hasField(METADATA_STAGING_BLOCK_KEY)) {
                            databaseIndexedFields.add(database.getField(METADATA_STAGING_BLOCK_KEY));
                        }
                    }
                    break;
                case SYSTEM: // Nothing to index on SYSTEM
                    break;
            }
            // Don't add FK in indexes if using H2
            if (dataSource.getDialectName() == RDBMSDataSource.DataSourceDialect.H2) {
                Iterator<FieldMetadata> indexedFields = databaseIndexedFields.iterator();
                while (indexedFields.hasNext()) {
                    FieldMetadata field = indexedFields.next();
                    if (field instanceof ReferenceFieldMetadata || field.isKey()) {
                        indexedFields.remove(); // H2 doesn't like indexes on PKs or FKs.
                    }
                }
            }
            // Set table/column name length limitation
            switch (dataSource.getDialectName()) {
                case ORACLE_10G:
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Oracle database is being used. Limit table name length to 30.");
                    }
                    tableResolver = new OracleStorageTableResolver(databaseIndexedFields, 30);
                    break;
                case MYSQL:
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("MySQL database is being used. Limit table name length to 64.");
                    }
                    tableResolver = new StorageTableResolver(databaseIndexedFields, 64);
                    break;
                case SQL_SERVER:
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("SQL Server database is being used. Limit table name length to 128.");
                    }
                    tableResolver = new StorageTableResolver(databaseIndexedFields, 128);
                    break;
                case POSTGRES:
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Postgres database is being used. Limit table name length to 63.");
                    }
                    tableResolver = new StorageTableResolver(databaseIndexedFields, 63);
                    break;
                case H2:
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("No limitation for table name length.");
                    }
                    tableResolver = new StorageTableResolver(databaseIndexedFields);
                    break;
            }
            storageClassLoader.setTableResolver(tableResolver);
            // Master, Staging and System share same class creator.
            switch (storageType) {
                case MASTER:
                case STAGING:
                case SYSTEM:
                    hibernateClassCreator = new ClassCreator(storageClassLoader);
                    break;
            }
            // Create Hibernate classes (after some modifications to the types).
            try {
                internalRepository.accept(hibernateClassCreator);
            } catch (Exception e) {
                throw new RuntimeException("Exception occurred during dynamic classes creation.", e);
            }
            // Last step: configuration of Hibernate
            try {
                // Hibernate needs to have dynamic classes in context class loader during configuration.
                InputStream ehCacheConfig = storageClassLoader.getResourceAsStream(StorageClassLoader.EHCACHE_XML_CONFIG);
                if (ehCacheConfig != null) {
                    CacheManager.create(ehCacheConfig);
                }
                configuration.configure(StorageClassLoader.HIBERNATE_CONFIG);
                batchSize = Integer.parseInt(configuration.getProperty(Environment.STATEMENT_BATCH_SIZE));
                Properties properties = configuration.getProperties();
                if (dataSource.getDialectName() == RDBMSDataSource.DataSourceDialect.ORACLE_10G) {
                    properties.setProperty(Environment.DEFAULT_SCHEMA, dataSource.getUserName());
                }
                // Logs DDL *before* initialization in case initialization fails (useful for debugging).
                if (LOGGER.isTraceEnabled()) {
                    traceDDL();
                }
                // Customize schema generation according to datasource content.
                RDBMSDataSource.SchemaGeneration schemaGeneration = dataSource.getSchemaGeneration();
                List exceptions = Collections.emptyList();
                switch (schemaGeneration) {
                    case CREATE:
                        SchemaExport schemaExport = new SchemaExport(configuration);
                        schemaExport.create(false, true);
                        // Exception may happen during recreation (hibernate may perform statements on tables that does
                        // not exist): these exceptions are supposed to be harmless (but log them to DEBUG just in case).
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Exception(s) occurred during schema creation:");
                            for (Object exceptionObject : schemaExport.getExceptions()) {
                                LOGGER.debug(((Exception) exceptionObject).getMessage());
                            }
                        }
                        break;
                    case VALIDATE:
                        SchemaValidator schemaValidator = new SchemaValidator(configuration);
                        schemaValidator.validate(); // This is supposed to throw exception on validation issue.
                        break;
                    case UPDATE:
                        SchemaUpdate schemaUpdate = new SchemaUpdate(configuration);
                        schemaUpdate.execute(false, true);
                        exceptions = schemaUpdate.getExceptions();
                        break;
                }
                // Throw an exception if schema update met issue(s).
                if (!exceptions.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Could not prepare database schema: ");
                    Iterator iterator = exceptions.iterator();
                    while (iterator.hasNext()) {
                        Exception exception = (Exception) iterator.next();
                        if (exception instanceof SQLException) {
                            SQLException currentSQLException = (SQLException) exception;
                            while (currentSQLException != null) {
                                sb.append(currentSQLException.getMessage());
                                sb.append('\n');
                                currentSQLException = currentSQLException.getNextException();
                            }
                        } else if (exception != null) {
                            sb.append(exception.getMessage());
                        }
                        if (iterator.hasNext()) {
                            sb.append('\n');
                        }
                    }
                    throw new IllegalStateException(sb.toString());
                }
                // This method is deprecated but using a 4.1+ hibernate initialization, Hibernate Search can't be
                // started
                // (wait for Hibernate Search 4.1 to be ready before considering changing this).
                factory = configuration.buildSessionFactory();
                MDMTransactionSessionContext.declareStorage(this, factory);
            } catch (Exception e) {
                throw new RuntimeException("Exception occurred during Hibernate initialization.", e);
            }
            // All set: set prepared flag to true.
            isPrepared = true;
            LOGGER.info("Storage '" + storageName + "' (" + storageType + ") is ready.");
        } catch (Throwable t) {
            try {
                // This prevent PermGen OOME in case of multiple failures to start.
                close();
            } catch (Exception e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Error occurred during clean up following failed prepare", e);
                }
            }
            throw new RuntimeException("Could not prepare '" + storageName + "'.", t);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private static boolean isIndexable(TypeMetadata fieldType) {
        if (Types.MULTI_LINGUAL.equals(fieldType.getName())) {
            return false;
        }
        if (fieldType.getData(MetadataRepository.DATA_MAX_LENGTH) != null) {
            Object maxLength = fieldType.getData(MetadataRepository.DATA_MAX_LENGTH);
            if (maxLength != null && Integer.parseInt(String.valueOf(maxLength)) > MappingGenerator.MAX_VARCHAR_TEXT_LIMIT) {
                return false; // Don't take into indexed fields long text fields
            }
        }
        return true;
    }

    private void traceDDL() {
        try {
            if (configuration == null) {
                throw new IllegalStateException("Expect a Hibernate configuration to be set.");
            }
            String jbossServerTempDir = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
            RDBMSDataSource.DataSourceDialect dialectType = dataSource.getDialectName();
            SchemaExport export = new SchemaExport(configuration);
            export.setFormat(false);
            export.setOutputFile(jbossServerTempDir + File.separator + storageName
                    + "_" + storageType + "_" + dialectType + ".ddl"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            export.setDelimiter(";"); //$NON-NLS-1$
            export.execute(false, false, false, true);
            if (export.getExceptions().size() > 0) {
                for (int i = 0; i < export.getExceptions().size(); i++) {
                    LOGGER.error("Error occurred while producing ddl.",//$NON-NLS-1$
                            (Exception) export.getExceptions().get(i));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error occurred while producing ddl.", e); //$NON-NLS-1$
        }
    }


    protected TypeMappingStrategy getMappingStrategy() {
        switch (storageType) {
            case SYSTEM:
                switch (dataSource.getDialectName()) {
                    case ORACLE_10G: // Oracle needs to store long string values to CLOBs.
                        return TypeMappingStrategy.SCATTERED_CLOB;
                    default:
                        return TypeMappingStrategy.SCATTERED;
                }

            case MASTER:
            case STAGING:
                return TypeMappingStrategy.AUTO;
            default:
                throw new IllegalArgumentException("Storage type '" + storageType + "' is not supported.");
        }
    }

    public InternalRepository getTypeEnhancer() {
        if (typeMappingRepository == null) {
            TypeMappingStrategy mappingStrategy = getMappingStrategy();
            mappingStrategy.setUseTechnicalFK(dataSource.generateTechnicalFK());
            // TODO Not nice to setUseTechnicalFK, change this
            switch (storageType) {
                case SYSTEM:
                    typeMappingRepository = new SystemTypeMappingRepository(mappingStrategy);
                    break;
                case MASTER:
                    typeMappingRepository = new UserTypeMappingRepository(mappingStrategy);
                    break;
                case STAGING:
                    typeMappingRepository = new StagingTypeMappingRepository(mappingStrategy);
                    break;
                default:
                    throw new IllegalArgumentException("Storage type '" + storageType + "' is not supported.");
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Selected type mapping strategy: " + mappingStrategy);
            }
        }
        return typeMappingRepository;
    }

    @Override
    public synchronized void prepare(MetadataRepository repository, boolean dropExistingData) {
        if (!isPrepared) {
            prepare(repository, Collections.<Expression>emptySet(), false, dropExistingData);
        }
    }

    @Override
    public MetadataRepository getMetadataRepository() {
        if (!isPrepared) {
            throw new IllegalStateException("Storage '" + storageName + "' has not been prepared.");
        }
        return userMetadataRepository;
    }

    @Override
    public StorageResults fetch(Expression userQuery) {
        assertPrepared();
        try {
            storageClassLoader.bind(Thread.currentThread());
            if (!ServerContext.INSTANCE.get().getTransactionManager().hasTransaction()) {
                throw new IllegalStateException("Transaction must be active during fetch operation.");
            }
            Session session = factory.getCurrentSession();
            // Call back closes session once calling code has consumed all results.
            Set<ResultsCallback> callbacks = Collections.<ResultsCallback>singleton(new ResultsCallback() {
                @Override
                public void onBeginOfResults() {
                    storageClassLoader.bind(Thread.currentThread());
                }

                @Override
                public void onEndOfResults() {
                    storageClassLoader.unbind(Thread.currentThread());
                }
            });
            return internalFetch(session, userQuery, callbacks);
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred during fetch operation", e);
        } finally {
            storageClassLoader.unbind(Thread.currentThread());
        }
    }

    @Override
    public void update(DataRecord record) {
        update(Collections.singleton(record));
    }

    @Override
    public void update(Iterable<DataRecord> records) {
        assertPrepared();
        try {
            storageClassLoader.bind(Thread.currentThread());
            Session session = factory.getCurrentSession();
            DataRecordConverter<Object> converter = new ObjectDataRecordConverter(storageClassLoader, session);
            for (DataRecord currentDataRecord : records) {
                TypeMapping mapping = mappingRepository.getMappingFromUser(currentDataRecord.getType());
                Wrapper o = (Wrapper) currentDataRecord.convert(converter, mapping);
                if (session.contains(o) && session.isReadOnly(o)) { // A read only instance for an update?
                    // Session#setReadOnly(...) does not always work as expected (especially in case of compound keys
                    // see TMDM-7014).
                    session.evict(o);
                    o = (Wrapper) currentDataRecord.convert(converter, mapping);
                }
                o.timestamp(System.currentTimeMillis());
                DataRecordMetadata recordMetadata = currentDataRecord.getRecordMetadata();
                Map<String, String> recordProperties = recordMetadata.getRecordProperties();
                if (!ObjectUtils.equals(recordMetadata.getTaskId(), o.taskId())) {
                    o.taskId(recordMetadata.getTaskId());
                }
                for (Map.Entry<String, String> currentProperty : recordProperties.entrySet()) {
                    String key = currentProperty.getKey();
                    String value = currentProperty.getValue();
                    ComplexTypeMetadata database = mapping.getDatabase();
                    if (database.hasField(key)) {
                        Object convertedValue = MetadataUtils.convert(value, database.getField(key));
                        if (!ObjectUtils.equals(convertedValue, o.get(key))) {
                            o.set(key, convertedValue);
                        }
                    } else {
                        throw new IllegalArgumentException("Can not store value '" + key
                                + "' because there is no database field '" + key + "' in type '" + mapping.getName()
                                + "' (storage is '" + toString() + "')");
                    }
                }
                session.saveOrUpdate(o);
                if (FLUSH_ON_LOAD && session.getStatistics().getEntityCount() % batchSize == 0) {
                    // Periodically flush objects to avoid using too much memory.
                    session.flush();
                }
            }
        } catch (ConstraintViolationException e) {
            throw new com.amalto.core.storage.exception.ConstraintViolationException(e);
        } catch (PropertyValueException e) {
            throw new RuntimeException("Invalid value in record to update.", e);
        } catch (NonUniqueObjectException e) {
            throw new RuntimeException("Attempted to update multiple times same record within same transaction.", e);
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred during update.", e);
        } finally {
            storageClassLoader.unbind(Thread.currentThread());
        }
    }

    @Override
    public void begin() {
        assertPrepared();
        TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
        transactionManager.currentTransaction().include(this).begin();
    }

    @Override
    public void commit() {
        assertPrepared();
        TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
        transactionManager.currentTransaction().include(this).commit();
    }

    @Override
    public void rollback() {
        assertPrepared();
        TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
        transactionManager.currentTransaction().include(this).rollback();
    }

    @Override
    public synchronized void end() {
        // TODO Remove
    }

    @Override
    public void reindex() {
        if (!dataSource.supportFullText()) {
            LOGGER.error("Can not reindex storage '" + storageName + "': datasource '" + dataSource.getName() + "' does not support full text.");
            return;
        }
        LOGGER.info("Reindexing full-text for " + storageName + "...");
        Session session = factory.getCurrentSession();
        MassIndexer indexer = Search.getFullTextSession(session).createIndexer();
        indexer.optimizeOnFinish(true);
        indexer.optimizeAfterPurge(true);
        try {
            indexer.threadsForSubsequentFetching(2);
            indexer.threadsToLoadObjects(2);
            indexer.batchSizeToLoadObjects(batchSize);
            indexer.startAndWait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            LOGGER.info("Reindexing done.");
        }
    }

    @Override
    public Set<String> getFullTextSuggestion(String keyword, FullTextSuggestion mode, int suggestionSize) {
        // TODO Need Lucene 3.0+ to implement this.
        /*
         * FullTextSession fullTextSession = Search.getFullTextSession(factory.getCurrentSession()); SearchFactory
         * searchFactory = fullTextSession.getSearchFactory();
         * 
         * Collection<ComplexTypeMetadata> complexTypes = internalRepository.getUserComplexTypes(); Set<String> fields =
         * new HashSet<String>(); List<DirectoryProvider> directoryProviders = new LinkedList<DirectoryProvider>(); for
         * (ComplexTypeMetadata complexType : complexTypes) { for (FieldMetadata fieldMetadata :
         * complexType.getFields()) { fields.add(fieldMetadata.getName()); } Class<?> generatedClass =
         * storageClassLoader.getClassFromType(complexType); DirectoryProvider[] providers =
         * searchFactory.getDirectoryProviders(generatedClass); Collections.addAll(directoryProviders, providers); }
         * 
         * DirectoryProvider[] providers = directoryProviders.toArray(new DirectoryProvider[directoryProviders.size()]);
         * IndexReader reader = searchFactory.getReaderProvider().openReader(providers);
         * 
         * try { switch (mode) { case START: try { IndexSearcher searcher = new IndexSearcher(reader);
         * 
         * String[] fieldsAsArray = fields.toArray(new String[fields.size()]); MultiFieldQueryParser parser = new
         * MultiFieldQueryParser(Version.LUCENE_29, fieldsAsArray, new KeywordAnalyzer()); StringBuilder queryBuffer =
         * new StringBuilder(); Iterator<String> fieldsIterator = fields.iterator(); while (fieldsIterator.hasNext()) {
         * queryBuffer.append(fieldsIterator.next()).append(':').append(keyword).append("*"); if
         * (fieldsIterator.hasNext()) { queryBuffer.append(" OR "); } } org.apache.lucene.search.Query query =
         * parser.parse(queryBuffer.toString());
         * 
         * MatchedWordsCollector collector = new MatchedWordsCollector(reader); searcher.search(query, collector);
         * return collector.getMatchedWords(); } catch (Exception e) { throw new RuntimeException(e); } case ALTERNATE:
         * try { IndexSearcher searcher = new IndexSearcher(reader);
         * 
         * String[] fieldsAsArray = fields.toArray(new String[fields.size()]); BooleanQuery query = new BooleanQuery();
         * for (String field : fieldsAsArray) { FuzzyQuery fieldQuery = new FuzzyQuery(new Term(field, '~' + keyword));
         * query.add(fieldQuery, BooleanClause.Occur.SHOULD); }
         * 
         * MatchedWordsCollector collector = new MatchedWordsCollector(reader); searcher.search(query, collector);
         * return collector.getMatchedWords(); } catch (Exception e) { throw new RuntimeException(e); } default: throw
         * new NotImplementedException("No support for suggestion mode '" + mode + "'"); } } finally { try {
         * reader.close(); } catch (IOException e) {
         * LOGGER.error("Exception occurred during full text suggestion searches.", e); } }
         */
        throw new UnsupportedOperationException("No support due to version of Lucene in use.");
    }

    @Override
    public String getName() {
        return storageName;
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public StorageType getType() {
        return storageType;
    }

    @Override
    public void delete(Expression userQuery) {
        try {
            storageClassLoader.bind(Thread.currentThread());
            Session session = factory.getCurrentSession();
            userQuery = userQuery.normalize(); // First do a normalize for correct optimization detection.
            // Check if optimized delete for one type (and no filter) is applicable
            if (userQuery instanceof Select) {
                Select select = (Select) userQuery;
                List<ComplexTypeMetadata> types = select.getTypes();
                if (types.size() == 1 && select.getCondition() == null) {
                    FlushMode previousFlushMode = session.getFlushMode();
                    try {
                        session.setFlushMode(FlushMode.ALWAYS); // Force Hibernate to actually send SQL query to database during delete.
                        ComplexTypeMetadata mainType = types.get(0);
                        TypeMapping mapping = mappingRepository.getMappingFromUser(mainType);
                        // Compute (and eventually sort) types to delete
                        List<ComplexTypeMetadata> typesToDelete;
                        MetadataRepository internalRepository = typeMappingRepository.getInternalRepository();
                        if (mapping instanceof ScatteredTypeMapping) {
                            MetadataVisitor<List<ComplexTypeMetadata>> transitiveClosure = new TypeTransitiveClosure();
                            typesToDelete = MetadataUtils.sortTypes(internalRepository, mapping.getDatabase().accept(transitiveClosure));
                        } else {
                            Collection<ComplexTypeMetadata> subTypes = mapping.getDatabase().getSubTypes();
                            if (subTypes.isEmpty()) {
                                typesToDelete = Collections.singletonList(mapping.getDatabase());
                            } else {
                                typesToDelete = new ArrayList<ComplexTypeMetadata>(subTypes.size() + 1);
                                typesToDelete.add(mapping.getDatabase());
                                typesToDelete.addAll(subTypes);
                            }
                        }
                        for (ComplexTypeMetadata typeToDelete : typesToDelete) {
                            Set<ReferenceFieldMetadata> references = internalRepository.accept(new ForeignKeyIntegrity(typeToDelete));
                            // Empty values from intermediate tables to this non instantiable type and unset inbound references
                            for (ReferenceFieldMetadata reference : references) {
                                if (!reference.isMandatory()) {
                                    if (reference.isMany()) {
                                        String collectionTableName = reference.getContainingType().getName() + '_' + reference.getName() + '_' + reference.getReferencedType().getName();
                                        String formattedTableName = MappingGenerator.formatSQLName(collectionTableName.toUpperCase(), tableResolver.getNameMaxLength());
                                        session.createSQLQuery("delete from " + formattedTableName).executeUpdate(); //$NON-NLS-1$
                                    } else {
                                        String referenceTableName = MappingGenerator.formatSQLName(reference.getContainingType().getName().toUpperCase(), tableResolver.getNameMaxLength());
                                        List<String> fkColumnNames;
                                        if (reference.getReferencedField() instanceof CompoundFieldMetadata) {
                                            FieldMetadata[] fields = ((CompoundFieldMetadata) reference.getReferencedField()).getFields();
                                            fkColumnNames = new ArrayList<String>(fields.length);
                                            for (FieldMetadata field : fields) {
                                                String columnName = MappingGenerator.formatSQLName(reference.getName() + "_" + field.getName(), tableResolver.getNameMaxLength()); //$NON-NLS-1
                                                fkColumnNames.add(columnName);
                                            }
                                        } else {
                                            String columnName = MappingGenerator.formatSQLName(reference.getName() + "_" + reference.getReferencedField().getName(), tableResolver.getNameMaxLength()); //$NON-NLS-1
                                            fkColumnNames = Collections.singletonList(columnName);
                                        }
                                        for (String fkColumnName : fkColumnNames) {
                                            session.createSQLQuery("update " + referenceTableName + " set " + fkColumnName + " = NULL").executeUpdate(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                        }
                                    }
                                }
                            }
                            // Empty values in type isMany=true reference
                            for (FieldMetadata field  : typeToDelete.getFields()) {
                                if (field.isMany()) {
                                    if (field instanceof ReferenceFieldMetadata) {
                                        String collectionTableName = typeToDelete.getName() + '_' + field.getName() + '_' + ((ReferenceFieldMetadata) field).getReferencedType().getName();
                                        String formattedTableName = MappingGenerator.formatSQLName(collectionTableName.toUpperCase(), tableResolver.getNameMaxLength());
                                        session.createSQLQuery("delete from " + formattedTableName).executeUpdate(); //$NON-NLS-1$
                                    } else if(field instanceof SimpleTypeFieldMetadata) {
                                        String collectionTableName = typeToDelete.getName() + '_' + field.getName();
                                        String formattedTableName = MappingGenerator.formatSQLName(collectionTableName.toUpperCase(), tableResolver.getNameMaxLength());
                                        session.createSQLQuery("delete from " + formattedTableName).executeUpdate(); //$NON-NLS-1$
                                    }
                                }
                            }
                            // Delete the type instances
                            String className = storageClassLoader.getClassFromType(typeToDelete).getName();
                            String tableName = MappingGenerator.formatSQLName(className, tableResolver.getNameMaxLength());
                            session.createQuery("delete from " + tableName).executeUpdate(); //$NON-NLS-1$
                            // Clean up full text indexes
                            if (dataSource.supportFullText()) {
                                FullTextSession fullTextSession = Search.getFullTextSession(session);
                                fullTextSession.purgeAll(storageClassLoader.getClassFromType(mapping.getDatabase()));
                            }
                        }
                    } finally {
                        session.setFlushMode(previousFlushMode);
                    }
                    return;
                }
            }
            // Generic fall back for deletions (filter)
            if (userQuery instanceof Select) {
                ((Select) userQuery).setForUpdate(true);
            }
            Iterable<DataRecord> records = internalFetch(session, userQuery, Collections.<ResultsCallback>emptySet());
            for (DataRecord currentDataRecord : records) {
                ComplexTypeMetadata currentType = currentDataRecord.getType();
                TypeMapping mapping = mappingRepository.getMappingFromUser(currentType);
                if (mapping == null) {
                    throw new IllegalArgumentException("Type '" + currentType.getName() + "' does not have a database mapping.");
                }
                Class<?> clazz = storageClassLoader.getClassFromType(mapping.getDatabase());

                Serializable idValue;
                Collection<FieldMetadata> keyFields = currentType.getKeyFields();
                if (keyFields.size() == 1) {
                    idValue = (Serializable) currentDataRecord.get(keyFields.iterator().next());
                } else {
                    List<Object> compositeIdValues = new LinkedList<Object>();
                    for (FieldMetadata keyField : keyFields) {
                        compositeIdValues.add(currentDataRecord.get(keyField));
                    }
                    idValue = ObjectDataRecordConverter.createCompositeId(storageClassLoader, clazz, compositeIdValues);
                }

                Wrapper object = (Wrapper) session.get(clazz, idValue, LockOptions.READ);
                if (object != null) {
                    session.delete(object);
                } else {
                    LOGGER.warn("Instance of type '" + currentType.getName() + "' and ID '" + idValue.toString()
                            + "' has already been deleted within same transaction.");
                }
            }
        } catch (ConstraintViolationException e) {
            throw new com.amalto.core.storage.exception.ConstraintViolationException(e);
        } catch (HibernateException e) {
            throw new RuntimeException(e);
        } finally {
            storageClassLoader.unbind(Thread.currentThread());
        }
    }

    @Override
    public void delete(DataRecord record) {
        try {
            storageClassLoader.bind(Thread.currentThread());
            Session session = factory.getCurrentSession();
            ComplexTypeMetadata currentType = record.getType();
            TypeMapping mapping = mappingRepository.getMappingFromUser(currentType);
            if (mapping == null) {
                throw new IllegalArgumentException("Type '" + currentType.getName() + "' does not have a database mapping.");
            }
            Class<?> clazz = storageClassLoader.getClassFromType(mapping.getDatabase());

            Serializable idValue;
            Collection<FieldMetadata> keyFields = currentType.getKeyFields();
            if (keyFields.size() == 1) {
                idValue = (Serializable) record.get(keyFields.iterator().next());
            } else {
                List<Object> compositeIdValues = new LinkedList<Object>();
                for (FieldMetadata keyField : keyFields) {
                    compositeIdValues.add(record.get(keyField));
                }
                idValue = ObjectDataRecordConverter.createCompositeId(storageClassLoader, clazz, compositeIdValues);
            }

            Wrapper object = (Wrapper) session.get(clazz, idValue, LockOptions.READ);
            if (object != null) {
                session.delete(object);
            } else {
                LOGGER.warn("Instance of type '" + currentType.getName() + "' and ID '" + idValue.toString()
                        + "' has already been deleted within same transaction.");
            }
        } catch (ConstraintViolationException e) {
            throw new com.amalto.core.storage.exception.ConstraintViolationException(e);
        } catch (HibernateException e) {
            throw new RuntimeException(e);
        } finally {
            storageClassLoader.unbind(Thread.currentThread());
        }
    }

    @Override
    public synchronized void close() {
        LOGGER.info("Closing storage '" + storageName + "' (" + storageType + ").");
        try {
            if (storageClassLoader != null) {
                storageClassLoader.bind(Thread.currentThread());
            }
            // Hack to prevent Hibernate Search to cause ConcurrentModificationException
            try {
                Field contexts = ContextHolder.class.getDeclaredField("contexts"); //$NON-NLS-1$
                contexts.setAccessible(true); // 'contexts' field is private.
                ThreadLocal<WeakHashMap<Configuration, SearchFactoryImpl>> contextsPerThread = (ThreadLocal<WeakHashMap<Configuration, SearchFactoryImpl>>) contexts
                        .get(null);
                WeakHashMap<Configuration, SearchFactoryImpl> contextMap = contextsPerThread.get();
                if (contextMap != null) {
                    contextMap.remove(configuration);
                }
            } catch (Exception e) {
                LOGGER.error("Exception occurred during Hibernate Search clean up.", e);
            }
            if (factory != null) {
                factory.close();
                factory = null; // SessionFactory#close() documentation advises to remove all references to SessionFactory.
            }
        } finally {
            if (storageClassLoader != null) {
                storageClassLoader.unbind(Thread.currentThread()); // TMDM-5934: Prevent restoring a closed classloader.
                storageClassLoader.close();
                storageClassLoader = null;
            }
        }
        // Reset caches
        ListIterator.resetTypeReaders();
        ScrollableIterator.resetTypeReaders();
        LOGGER.info("Storage '" + storageName + "' (" + storageType + ") closed.");
    }

    @Override
    public void close(boolean dropExistingData) {
        // Close hibernate so all connections get released before drop schema.
        close();
        if (dropExistingData) { // Drop schema if asked for...
            LOGGER.info("Deleting data and schema of storage '" + storageName + "' (" + storageType + ").");
            JDBCStorageCleaner cleaner = new JDBCStorageCleaner(new FullTextIndexCleaner());
            cleaner.clean(this);
            LOGGER.info("Data and schema of storage '" + storageName + "' (" + storageType + ") deleted.");
        }
    }

    private StorageResults internalFetch(Session session, Expression userQuery, Set<ResultsCallback> callbacks) {
        // Always normalize the query to ensure query has expected format.
        Expression expression = userQuery.normalize();
        if (expression instanceof Select) {
            Select select = (Select) expression;
            // Contains optimizations (use of full text, disable it...)
            ConfigurableContainsOptimizer containsOptimizer = new ConfigurableContainsOptimizer(dataSource);
            containsOptimizer.optimize(select);
            // Other optimizations
            for (Optimizer optimizer : OPTIMIZERS) {
                optimizer.optimize(select);
            }
        }
        expression = userQuery.normalize();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Query after optimizations:");
            userQuery.accept(new UserQueryDumpConsole(LOGGER));
        }
        // Analyze query
        SelectAnalyzer selectAnalysis = new SelectAnalyzer(mappingRepository, storageClassLoader, session, callbacks, this, tableResolver);
        Visitor<StorageResults> queryHandler = userQuery.accept(selectAnalysis);
        // Transform query using mappings
        Expression internalExpression = expression;
        if (expression instanceof Select) {
            List<ComplexTypeMetadata> types = ((Select) expression).getTypes();
            boolean isInternal = true;
            for (ComplexTypeMetadata type : types) {
                TypeMapping mapping = mappingRepository.getMappingFromUser(type);
                if (mapping != null) {
                    isInternal &= mapping.getDatabase() == type;
                }
            }
            if (!isInternal) {
                MappingExpressionTransformer transformer = new MappingExpressionTransformer(mappingRepository);
                // Normalize should not be needed, but adds it as safety
                internalExpression = expression.accept(transformer).normalize();
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Internal query after mappings:");
                userQuery.accept(new UserQueryDumpConsole(LOGGER, Level.TRACE));
            }
        }
        // Evaluate query
        return internalExpression.accept(queryHandler);
    }

    private void assertPrepared() {
        if (!isPrepared) {
            throw new IllegalStateException("Storage has not been prepared.");
        }
        if (storageClassLoader == null || storageClassLoader.isClosed()) {
            throw new IllegalStateException("Storage has been closed.");
        }
    }

    public StorageClassLoader getClassLoader() {
        return storageClassLoader;
    }

    private static class MetadataChecker extends DefaultMetadataVisitor<Object> {

        final Set<TypeMetadata> processedTypes = new HashSet<TypeMetadata>();

        @Override
        public Object visit(SimpleTypeFieldMetadata simpleField) {
            String simpleFieldTypeName = simpleField.getType().getName();
            if (NoSupportTypes.getType(simpleFieldTypeName) != null) {
                throw new IllegalArgumentException("No support for field type '" + simpleFieldTypeName + "' (field '"
                        + simpleField.getName() + "' of type '" + simpleField.getContainingType().getName() + "').");                
            }
            assertField(simpleField);
            return super.visit(simpleField);
        }

        @Override
        public Object visit(ReferenceFieldMetadata referenceField) {
            assertField(referenceField);
            return super.visit(referenceField);
        }

        @Override
        public Object visit(ContainedTypeFieldMetadata containedField) {
            assertField(containedField);
            if (processedTypes.contains(containedField.getContainedType())) {
                return null;
            } else {
                processedTypes.add(containedField.getContainedType());
            }
            return super.visit(containedField);
        }

        @Override
        public Object visit(EnumerationFieldMetadata enumField) {
            assertField(enumField);
            return super.visit(enumField);
        }

        private static void assertField(FieldMetadata field) {
            if (field.getName().toLowerCase().startsWith(FORBIDDEN_PREFIX)) {
                throw new IllegalArgumentException("Field '" + field.getName() + "' of type '"
                        + field.getContainingType().getName() + "' is not allowed to start with " + FORBIDDEN_PREFIX);
            }
        }
    }

    private static class LocalEntityResolver implements EntityResolver {

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            if (StorageClassLoader.CONFIGURATION_PUBLIC_ID.equals(publicId)) {
                InputStream resourceAsStream = HibernateStorage.class.getResourceAsStream("hibernate.cfg.dtd"); //$NON-NLS-1$
                if (resourceAsStream == null) {
                    throw new IllegalStateException("Expected class path to contain Hibernate configuration DTD.");
                }
                return new InputSource(resourceAsStream);
            } else if (StorageClassLoader.MAPPING_PUBLIC_ID.equals(publicId)) {
                InputStream resourceAsStream = HibernateStorage.class.getResourceAsStream("hibernate.hbm.dtd"); //$NON-NLS-1$
                if (resourceAsStream == null) {
                    throw new IllegalStateException("Expected class path to contain Hibernate mapping DTD.");
                }
                return new InputSource(resourceAsStream);
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return storageName + '(' + storageType + ')';
    }

    private static class TypeTransitiveClosure extends DefaultMetadataVisitor<List<ComplexTypeMetadata>> {

        private final List<ComplexTypeMetadata> types = new LinkedList<ComplexTypeMetadata>();

        @Override
        public List<ComplexTypeMetadata> visit(ComplexTypeMetadata complexType) {
            if (types.isEmpty() || !complexType.isInstantiable()) {
                types.add(complexType);
                types.addAll(complexType.getSubTypes());
                super.visit(complexType);
            }
            return types;
        }

        @Override
        public List<ComplexTypeMetadata> visit(ReferenceFieldMetadata referenceField) {
            ComplexTypeMetadata referencedType = referenceField.getReferencedType();
            referencedType.accept(this);
            return types;
        }
    }

}
