/*
 * Copyright (C) 2006-2015 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import java.io.StringWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.StaleStateException;
import org.hibernate.Transaction;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.internal.SessionImpl;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;

import com.amalto.core.load.io.ResettableStringWriter;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.exception.ConstraintViolationException;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordXmlWriter;
import com.amalto.core.storage.record.ObjectDataRecordReader;
import com.amalto.core.storage.transaction.StorageTransaction;

class HibernateStorageTransaction extends StorageTransaction {

    private static final Logger LOGGER = Logger.getLogger(HibernateStorageTransaction.class);

    private static final int TRANSACTION_DUMP_MAX = 10;
    
    private static final int LOCK_TIMEOUT_SECONDS = 30;

    private final HibernateStorage storage;

    private final Session session;

    private final Thread initiatorThread;
    
    private final Lock lock = new ReentrantLock();

    private boolean hasFailed;

    // a stack keeping track of all begin calls on this StorageTransaction
    private Stack<BeginEvent> beginStack = new Stack<BeginEvent>();
    
    private final String id = UUID.randomUUID().toString();

    public HibernateStorageTransaction(HibernateStorage storage, Session session) {
        super();
        this.storage = storage;
        this.session = session;
        this.initiatorThread = Thread.currentThread();
    }

    public Thread getInitiatorThread() {
        return initiatorThread;
    }

    @Override
    public Storage getStorage() {
        return storage;
    }

    @Override
    public void begin() {
        this.acquireLock();
        try {
            if (!session.isOpen()) {
                throw new IllegalStateException("Could not start transaction: provided session is not ready for use (session is closed)."); //$NON-NLS-1$
            }
            Transaction transaction = session.getTransaction();
            if (LOGGER.isDebugEnabled()) {
                debug("Transaction begin"); //$NON-NLS-1$
            }
            if (!transaction.isActive()) {
                session.beginTransaction();
            }
            // push a new event to the begin stack
            BeginEvent event = new BeginEvent();
            this.beginStack.push(event);
            if(LOGGER.isDebugEnabled()){
                debug("Begin event pushed: " + event.toString()); //$NON-NLS-1$
            }
        }
        finally {
            this.releaseLock();
        }
    }
    
    public void acquireLock() {
        if(LOGGER.isTraceEnabled()){
            debug("Trying to acquire lock for " + this + " on thread " + Thread.currentThread().getName()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        try {
            if(!this.lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)){
                LOGGER.error("Failed to acquire lock within "+ LOCK_TIMEOUT_SECONDS + " seconds"); //$NON-NLS-1$ //$NON-NLS-2$
                throw new RuntimeException("Failed to acquire lock within " + LOCK_TIMEOUT_SECONDS + " seconds"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted while trying to acquire lock on " + this); //$NON-NLS-1$
        }
        if(LOGGER.isTraceEnabled()){
            debug("Lock acquired for " + this + " on thread " + Thread.currentThread().getName()); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    public void releaseLock(){
        if(LOGGER.isTraceEnabled()){
            debug("Trying to release for " + this + " on thread " + Thread.currentThread().getName()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        lock.unlock();
        if(LOGGER.isTraceEnabled()){
            debug("Lock released for " + this + " on thread " + Thread.currentThread().getName()); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public void commit() {
        this.acquireLock();
        try {
            if (!mustProceed()) {
                return;
            }
            if (isAutonomous) {
                Transaction transaction = session.getTransaction();
                if (LOGGER.isDebugEnabled()) {
                    debug("Transaction #" + transaction.hashCode() + " -> Commit includes "
                            + session.getStatistics().getEntityCount() + " not-flushed record(s)...");
                }
                if (!transaction.isActive()) {
                    throw new IllegalStateException("Can not commit transaction, no transaction is active."); //$NON-NLS-1$
                }
                try {
                    if (!transaction.wasCommitted()) {
                        session.flush();
                        transaction.commit();
                        if (LOGGER.isDebugEnabled()) {
                            debug("Transaction #" + transaction.hashCode() + " -> Commit done.");
                        }
                    } else {
                        LOGGER.warn("Transaction was already committed."); //$NON-NLS-1$
                    }
                    if (session.isOpen()) {
                        /*
                         * Eviction is not <b>needed</b> (the session will not be reused), but evicts cache in case the session
                         * is reused.
                         */
                        if (session.getStatistics().getEntityKeys().size() > 0) {
                            session.clear();
                        }
                        session.close();
                    }
                } catch (Exception e) {
                    try {
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("Transaction failed, dumps transaction content for diagnostic."); //$NON-NLS-1$
                            dumpTransactionContent(session, storage); // Dumps all the faulty session information.
                        }
                        processCommitException(e);
                    } finally {
                        hasFailed = true; // Mark this storage transaction as "failed".
                    }
                }
            } else {
                try {
                    if (session.isDirty()) {
                        session.flush();
                    }
                } catch (Exception e) {
                    hasFailed = true; // Mark this storage transaction as "failed".
                    processCommitException(e);
                }
            }
            super.commit();
            storage.getClassLoader().reset(Thread.currentThread());
        }
        finally {
            this.releaseLock();
        }
    }

    private static void processCommitException(Exception e) {
        if (e instanceof org.hibernate.exception.ConstraintViolationException //
                || e instanceof ObjectNotFoundException //
                || e instanceof StaleStateException) {
            throw new ConstraintViolationException(e);
        } else {
            throw new RuntimeException(e);
        }
    }

    /**
     * Dumps all current entities in <code>session</code> using data model information from <code>storage</code>.
     *
     * @param session The Hibernate session that failed to be committed.
     * @param storage A {@link com.amalto.core.storage.hibernate.HibernateStorage} that can be used to retrieve metadata information for all objects in
     *                <code>session</code>.
     */
    private static void dumpTransactionContent(Session session, HibernateStorage storage) {
        Level currentLevel = Level.INFO;
        if (LOGGER.isEnabledFor(currentLevel)) {
            Set<EntityKey> failedKeys = new HashSet<>(session.getStatistics().getEntityKeys()); // Copy content to avoid concurrent modification issues.
            int i = 1;
            ObjectDataRecordReader reader = new ObjectDataRecordReader();
            MappingRepository mappingRepository = storage.getTypeEnhancer().getMappings();
            StorageClassLoader classLoader = storage.getClassLoader();
            DataRecordXmlWriter writer = new DataRecordXmlWriter();
            ResettableStringWriter xmlContent = new ResettableStringWriter();
            for (EntityKey failedKey : failedKeys) {
                String entityTypeName = StringUtils.substringAfterLast(failedKey.getEntityName(), "."); //$NON-NLS-1$
                LOGGER.log(currentLevel, "Entity #" + i++ + " (type=" + entityTypeName + ", id=" + failedKey.getIdentifier() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                try {
                    storage.getClassLoader().bind(Thread.currentThread());
                    Wrapper o = (Wrapper) ((SessionImpl) session).getPersistenceContext().getEntity(failedKey);
                    if (!session.isReadOnly(o)) {
                        if (o != null) {
                            ComplexTypeMetadata type = classLoader.getTypeFromClass(classLoader.loadClass(failedKey.getEntityName()));
                            if (type != null) {
                                DataRecord record = reader.read(mappingRepository.getMappingFromDatabase(type), o);
                                writer.write(record, xmlContent);
                                LOGGER.log(currentLevel, xmlContent + "\n(taskId='" + o.taskId() + "', timestamp='" + o.timestamp() + "')"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            } else {
                                LOGGER.warn("Could not find data model type for object " + o); //$NON-NLS-1$
                            }
                        } else {
                            LOGGER.warn("Could not find an object for entity " + failedKey); //$NON-NLS-1$
                        }
                    }
                } catch (ObjectNotFoundException missingRefException) {
                    LOGGER.log(currentLevel, "Can not log entity: contains a unresolved reference to '" //$NON-NLS-1$
                            + missingRefException.getEntityName() + "' with id '" //$NON-NLS-1$
                            + missingRefException.getIdentifier() + "'"); //$NON-NLS-1$
                } catch (Exception serializationException) {
                    LOGGER.log(currentLevel, "Failed to log entity content for type " + entityTypeName + " (enable DEBUG for exception details)."); //$NON-NLS-1$ //$NON-NLS-2$
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Serialization exception occurred.", serializationException); //$NON-NLS-1$
                    }
                } finally {
                    xmlContent.reset();
                    storage.getClassLoader().unbind(Thread.currentThread());
                }
                if (i > TRANSACTION_DUMP_MAX) {
                    if (!LOGGER.isDebugEnabled()) {
                        int more = failedKeys.size() - i;
                        if (more > 0) {
                            LOGGER.log(currentLevel, "and " + more + " more... (enable DEBUG for full dump)"); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        return;
                    } else {
                        currentLevel = Level.DEBUG; // Continue the dump but with a DEBUG level
                    }
                }
            }
        }
    }

    @Override
    public void rollback() {
        this.acquireLock();
        try {
            if(isAutonomous && mustProceed()){
                try {
                    Transaction transaction = session.getTransaction();
                    if (!transaction.isActive()) {
                        LOGGER.warn("Can not rollback transaction, no transaction is active."); //$NON-NLS-1$
                        return;
                    } else {
                        boolean dirty;
                        try {
                            dirty = session.isDirty();
                        } catch (HibernateException e) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("Is dirty check during rollback threw exception.", e); //$NON-NLS-1$
                            }
                            dirty = true; // Consider session is dirty (exception might occur when there's an integrity issue).
                        }
                        if (LOGGER.isInfoEnabled() && dirty) {
                            LOGGER.info("Transaction is being rollbacked. Transaction content:"); //$NON-NLS-1$
                            dumpTransactionContent(session, storage); // Dumps all content in the current transaction.
                        }
                    }
                    if (!transaction.wasRolledBack()) {
                        transaction.rollback();
                    } else {
                        LOGGER.warn("Transaction was already rollbacked."); //$NON-NLS-1$
                    }
                } finally {
                    try {
                        /*
                         * Eviction is not <b>needed</b> (the session will not be reused), but evicts cache in case the
                         * session is reused.
                         */
                        if (session.isOpen() && session.getStatistics().getEntityKeys().size() > 0) {
                            session.clear();
                            session.close();
                        }
                        hasFailed = false;
                    } catch (HibernateException e) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Could not clean up session.", e); //$NON-NLS-1$
                        }
                    } finally {
                        // It is *very* important to ensure super.rollback() gets called (even if session close did not succeed).
                        super.rollback();
                        if (!storage.isClosed()) {
                            storage.getClassLoader().reset(Thread.currentThread());
                        }
                    }
                }
            }
        }
        finally {
            this.releaseLock();
        }
    }
    
    /**
     * @return true if the current state of this HibernateStorageTransaction authorizes a commit or 
     * a rollback to be really performed. This is true only if the internal begin events stack 
     * contains one element.
     */
    private boolean mustProceed(){
        // pop the first begin element from the stack
        if(!beginStack.isEmpty()){
            BeginEvent event = beginStack.pop();
            if(LOGGER.isDebugEnabled()){
                debug("Popped begin event: " + event.toString()); //$NON-NLS-1$
            }
        }
        // the stack is already empty. A begin is missing ... keep things happen anyway 
        else {
            LOGGER.warn("mustProceed was called on HibernateStorageTransaction but the begin stack is empty. A begin is missing", new IllegalStateException()); //$NON-NLS-1$
        }
        // if the stack is still not empty, it means we should not perform neither commit nor rollback.
        if(!beginStack.isEmpty()){
            if(LOGGER.isDebugEnabled()){
                debug("Nested transactions: A begin event prevents to proceed:" + beginStack.peek().toString()); //$NON-NLS-1$
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean hasFailed() {
        return hasFailed;
    }

    public synchronized Session getSession() {
        com.amalto.core.storage.transaction.Transaction.LockStrategy lockStrategy = getLockStrategy();
        switch (lockStrategy) {
            case NO_LOCK:
                return session;
            case LOCK_FOR_UPDATE:
                return new LockUpdateSession(session);
            default:
                throw new NotImplementedException("No support for lock '" + lockStrategy + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public boolean isClean() {
        return this.beginStack.isEmpty();
    }
    
    @Override
    public String toString() {
        return "HibernateStorageTransaction {" + //$NON-NLS-1$
                "storage=" + storage + //$NON-NLS-1$
                ", session=" + session + //$NON-NLS-1$
                ", initiatorThread=" + initiatorThread + //$NON-NLS-1$
                '}';
    }
    
    /**
     * debug utility method
     * @param msg
     */
    private void debug(String msg){
        LOGGER.debug(id + "[" + storage + "] (" + (session != null ? session.hashCode() : "null") + ") " + msg); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
    
    /**
     * A simple pojo representing a call to {@link HibernateStorageTransaction#begin()}.
     */
    private class BeginEvent {
        
        private final String id;
        
        private final StackTraceElement[] stackTrace;
        
        private final long timestamp;
        
        public BeginEvent() {
            this.id = UUID.randomUUID().toString();
            this.stackTrace = Thread.currentThread().getStackTrace();
            this.timestamp = System.currentTimeMillis();
        }
        
        public String toString(){
            String eol = System.getProperty("line.separator"); //$NON-NLS-1$
            StringWriter writer = new StringWriter();
            writer.append("ID = " + id); //$NON-NLS-1$
            writer.append(", CreationDate: " + new Date(this.timestamp)); //$NON-NLS-1$
            if(this.stackTrace != null){
                writer.append(eol);
                for(StackTraceElement s : this.stackTrace){
                    writer.append(s.toString());
                    writer.append(eol);
                }
            }
            return writer.toString();
        }
    }
}
