/*
 * Copyright (C) 2006-2014 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.transaction;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.task.staging.SerializableList;

@RestController
@RequestMapping("/transactions")
public class TransactionService {

    /**
     * Lists all actives transactions ({@link Transaction.Lifetime#LONG} and {@link Transaction.Lifetime#AD_HOC}).
     * 
     * @return A space-separated list of transaction ids (as UUID).
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public List<String> list() {
        TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
        List<String> list = transactionManager.list();
        return SerializableList.create(list, "transactions", "transaction_id"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Starts a new transaction and returns the id of the newly created transaction.
     * 
     * @return A transaction id (as UUID).
     */
    @RequestMapping(value = "/", method = RequestMethod.PUT)
    public String begin() {
        TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
        Transaction transaction = transactionManager.create(Transaction.Lifetime.LONG);
        return transaction.getId();
    }

    /**
     * Associate calling thread with transaction <code>transactionId</code>.
     * 
     * @param transactionId A transaction id.
     */
    @RequestMapping(value = "{id}/", method = RequestMethod.GET)
    public void resume(@PathVariable("id") String transactionId) { //$NON-NLS-1$
        TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
        Transaction transaction = transactionManager.get(transactionId);
        if (transaction != null) {
            transactionManager.associate(transaction);
        }
    }

    /**
     * Commit the changes in transaction <code>transactionId</code>.
     * 
     * @param transactionId A valid transaction id.
     */
    @RequestMapping(value = "{id}/", method = RequestMethod.POST)
    public void commit(@PathVariable("id") String transactionId) { //$NON-NLS-1$
        TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
        Transaction transaction = transactionManager.get(transactionId);
        if (transaction != null) {
            transaction.commit();
        }
    }

    /**
     * Cancels (rollback) all changes done in <code>transactionId</code>.
     * 
     * @param transactionId A transaction id.
     */
    @RequestMapping(value = "{id}/", method = RequestMethod.DELETE)
    public void rollback(@PathVariable("id") String transactionId) { //$NON-NLS-1$
        TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
        Transaction transaction = transactionManager.get(transactionId);
        if (transaction != null) {
            transaction.rollback();
        }
    }
}
