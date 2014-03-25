/*
 * Copyright (C) 2006-2014 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.server;

import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.transaction.StorageTransaction;
import com.amalto.core.storage.transaction.Transaction;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import java.util.*;

class MDMTransaction implements Transaction {

    private static final Logger LOGGER = Logger.getLogger(MDMTransaction.class);

    private final MultiKeyMap storageTransactions = new MultiKeyMap();

    private final String id;

    private final Lifetime lifetime;

    MDMTransaction(Lifetime lifetime, String id) {
        this.lifetime = lifetime;
        this.id = id;
    }

    private void transactionComplete() {
        synchronized (storageTransactions) {
            MapIterator iterator = storageTransactions.mapIterator();
            while (iterator.hasNext()) {
                iterator.next();
                StorageTransaction storageTransaction = (StorageTransaction) iterator.getValue();
                if (!storageTransaction.hasFailed()) {
                    iterator.remove();
                }
            }
            if (storageTransactions.isEmpty()) {
                storageTransactions.clear();
                ServerContext.INSTANCE.get().getTransactionManager().remove(this);
            }
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void begin() {
        synchronized (storageTransactions) {
            Collection<StorageTransaction> values = new ArrayList<StorageTransaction>(storageTransactions.values());
            for (StorageTransaction storageTransaction : values) {
                storageTransaction.autonomous().begin();
            }
        }
    }

    @Override
    public void commit() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[" + this + "] Transaction #" + this.hashCode() + " -> Commit.");
        }
        synchronized (storageTransactions) {
            try {
                Collection<StorageTransaction> values = new ArrayList<StorageTransaction>(storageTransactions.values());
                for (StorageTransaction storageTransaction : values) {
                    storageTransaction.autonomous().commit();
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[" + this + "] Transaction #" + this.hashCode() + " -> Commit done.");
                }
            } catch (Throwable t) {
                LOGGER.warn("Commit failed for transaction " + getId() + ". Perform automatic rollback.");
                rollback();
            } finally {
                transactionComplete();
            }
        }
    }

    @Override
    public void rollback() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[" + this + "] Transaction #" + this.hashCode() + " -> Rollback. ");
        }
        synchronized (storageTransactions) {
            try {
                Collection<StorageTransaction> values = new ArrayList<StorageTransaction>(storageTransactions.values());
                for (StorageTransaction storageTransaction : values) {
                    storageTransaction.autonomous().rollback();
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[" + this + "] Transaction #" + this.hashCode() + " -> Rollback done.");
                }
            } finally {
                transactionComplete();
            }
        }
    }

    @Override
    public StorageTransaction exclude(Storage storage) {
        StorageTransaction transaction = (StorageTransaction) storageTransactions.remove(storage, Thread.currentThread());
        synchronized (storageTransactions) {
            if (storageTransactions.isEmpty()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Transaction '" + getId() + "' has no longer storage transactions. Removing it.");
                }
                transactionComplete();
            }
        }
        return transaction;
    }

    @Override
    public boolean hasFailed() {
        synchronized (storageTransactions) {
            for (Object storageTransactionAsObject : storageTransactions.values()) {
                StorageTransaction storageTransaction = (StorageTransaction) storageTransactionAsObject;
                if (storageTransaction.hasFailed()) {
                    return true;
                }
            }
        }
        return false;
    }

    public StorageTransaction include(Storage storage) {
        if ((storage.getCapabilities() & Storage.CAP_TRANSACTION) != Storage.CAP_TRANSACTION) {
            throw new IllegalArgumentException("Storage '" + storage.getName() + "' does not support transactions.");
        }
        StorageTransaction storageTransaction;
        synchronized (storageTransactions) {
            storageTransaction = getStorageTransaction(storage, Thread.currentThread());
            if (storageTransaction == null) {
                storageTransaction = storage.newStorageTransaction();
                storageTransactions.put(storage, Thread.currentThread(), storageTransaction);
            }
        }
        switch (lifetime) {
            case AD_HOC:
                return storageTransaction.autonomous();
            case LONG:
                return storageTransaction.dependent();
            default:
                throw new NotImplementedException("No support for life time '" + lifetime + "'");
        }
    }

    private StorageTransaction getStorageTransaction(Storage storage, Thread thread) {
        synchronized (storageTransactions) {
            if (storage.getType() == StorageType.SYSTEM) {
                for (Object storageTransactionAsObject : storageTransactions.values()) {
                    StorageTransaction storageTransaction = (StorageTransaction) storageTransactionAsObject;
                    Storage associatedStorage = storageTransaction.getStorage();
                    if (associatedStorage.getType() == StorageType.SYSTEM) {
                        return storageTransaction;
                    }
                }
            }
            return (StorageTransaction) storageTransactions.get(storage, thread);
        }
    }

    @Override
    public String toString() {
        return "MDMTransaction{" +
                "id='" + id + '\'' +
                ", storageTransactions=" + storageTransactions +
                ", lifetime=" + lifetime +
                '}';
    }
}
