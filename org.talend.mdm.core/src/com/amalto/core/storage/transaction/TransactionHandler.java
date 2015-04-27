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

package com.amalto.core.storage.transaction;

import com.amalto.core.schema.validation.SkipAttributeDocumentBuilder;
import com.amalto.core.server.ServerContext;
import org.apache.log4j.Logger;

import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.handler.Handler;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.*;
import java.util.Iterator;

public class TransactionHandler implements Handler {

    private static final Logger LOGGER = Logger.getLogger(TransactionHandler.class);

    private static final String TRANSACTION_ID = "transaction-id"; //$NON-NLS-1$

    private static final String LOGOUT_OPERATION_NAME = "WSLogout"; //$NON-NLS-1$

    private TransactionState state;

    private static TransactionState getState(MessageContext messageContext) {
        try {
            if (messageContext instanceof SOAPMessageContext) {
                SOAPMessage message = ((SOAPMessageContext) messageContext).getMessage();
                if (message != null) {
                    SOAPBody body = message.getSOAPBody();
                    String messageOperationName = null;
                    if (body.getFirstChild() != null) {
                        messageOperationName = body.getFirstChild().getLocalName();
                    }
                    if (LOGOUT_OPERATION_NAME.equals(messageOperationName)) {
                        return NoOpTransactionState.INSTANCE;
                    }
                    SOAPHeader soapHeader = message.getSOAPHeader();
                    if (soapHeader != null) {
                        Iterator iterator = soapHeader.extractAllHeaderElements();
                        while (iterator.hasNext()) {
                            SOAPHeaderElement element = (SOAPHeaderElement) iterator.next();
                            Name name = element.getElementName();
                            if (name != null
                                    && SkipAttributeDocumentBuilder.TALEND_NAMESPACE.equals(name.getURI())
                                    && TRANSACTION_ID.equals(name.getLocalName())) {
                                String transactionID = element.getValue();
                                return new ExplicitTransaction(transactionID);
                            }
                        }
                    }
                }
            }
            return NoOpTransactionState.INSTANCE;
        } catch (SOAPException e) {
            LOGGER.error("Unexpected SOAP handler exception.", e);
            return NoOpTransactionState.INSTANCE;
        }
    }

    @Override
    public QName[] getHeaders() {
        return new QName[] {new QName(SkipAttributeDocumentBuilder.TALEND_NAMESPACE, TRANSACTION_ID)};
    }

    @Override
    public void init(HandlerInfo handlerInfo) throws JAXRPCException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public boolean handleRequest(MessageContext messageContext) throws JAXRPCException, SOAPFaultException {
        state = getState(messageContext);
        state.preRequest();
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        state.postRequest();
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext) {
        state.cancelRequest();
        return true;
    }

    private static interface TransactionState {
        void preRequest();

        void postRequest();

        void cancelRequest();
    }

    private static class ExplicitTransaction implements TransactionState {

        private final String transactionID;

        public ExplicitTransaction(String transactionID) {
            this.transactionID = transactionID;
        }

        @Override
        public void preRequest() {
            TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
            Transaction transaction = transactionManager.get(transactionID);
            if (transaction == null) {
                transaction = transactionManager.create(Transaction.Lifetime.LONG, transactionID);
                transaction.begin();
            }
            transactionManager.associate(transaction);
        }

        @Override
        public void postRequest() {
            TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
            Transaction transaction = transactionManager.get(transactionID);
            if (transaction == null) {
                throw new IllegalStateException("Transaction '" + transactionID + "' no longer exists.");
            }
            transactionManager.dissociate(transaction);
        }

        @Override
        public void cancelRequest() {
            TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
            Transaction transaction = transactionManager.get(transactionID);
            if (transaction == null) {
                throw new IllegalStateException("Transaction '" + transactionID + "' no longer exists.");
            }
            transactionManager.dissociate(transaction);
        }
    }

    private static class NoOpTransactionState implements TransactionState {

        private static final TransactionState INSTANCE = new NoOpTransactionState();

        @Override
        public void preRequest() {
        }

        @Override
        public void postRequest() {
            TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
            if (transactionManager.hasTransaction()) {
                throw new IllegalStateException("A non-transactional (auto-commit) operation has an active " +
                        "transaction after operation completion.");
            }
        }

        @Override
        public void cancelRequest() {
            TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
            if (transactionManager.hasTransaction()) {
                throw new IllegalStateException("A non-transactional (auto-commit) operation has an active " +
                        "transaction after operation completion.");
            }
        }
    }
}
