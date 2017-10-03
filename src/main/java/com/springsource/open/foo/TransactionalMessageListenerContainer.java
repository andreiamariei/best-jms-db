package com.springsource.open.foo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.JmsUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;

/**
 * @author Andrei Amariei
 */
public class TransactionalMessageListenerContainer extends DefaultMessageListenerContainer {

    private PlatformTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;

    public TransactionalMessageListenerContainer() {
        setSessionTransacted(true);
    }

    @Override
    public void initialize() {
        super.initialize();
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setName(getBeanName() + "-invocation");
    }

    @Autowired
    @Required
    public void setInvocationTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    protected void doInvokeListener(SessionAwareMessageListener listener, Session session, Message message) throws JMSException {
        Connection conToClose = null;
        Session sessionToClose = null;

        try {
            Session sessionToUse = session;
            if (!this.isExposeListenerSession()) {
                conToClose = this.createConnection();
                sessionToClose = this.createSession(conToClose);
                sessionToUse = sessionToClose;
            }

            invokeInTransaction(listener, message, sessionToUse);
            if (sessionToUse != session && sessionToUse.getTransacted() && this.isSessionLocallyTransacted(sessionToUse)) {
                JmsUtils.commitIfNecessary(sessionToUse);
            }
        } finally {
            JmsUtils.closeSession(sessionToClose);
            JmsUtils.closeConnection(conToClose);
        }
    }

    private void invokeInTransaction(final SessionAwareMessageListener listener, final Message message, final Session sessionToUse) throws JMSException {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                try {
                    listener.onMessage(message, sessionToUse);
                } catch (JMSException e) {
                    throw new RuntimeException("Exception while invoking listener in transaction", e);
                }
            }
        });
    }

    @Override
    protected void doInvokeListener(MessageListener listener, Message message) throws JMSException {
        invokeInTransaction(listener, message);
    }

    private void invokeInTransaction(final MessageListener listener, final Message message) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                listener.onMessage(message);
            }
        });
    }
}
