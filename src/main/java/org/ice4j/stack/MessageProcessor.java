/* See LICENSE.md for license information */
package org.ice4j.stack;

import java.util.concurrent.*;

import org.ice4j.*;
import org.ice4j.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class is used to parse and dispatch incoming messages in a multi-thread
 * manner.
 *
 * @author Emil Ivov
 */
class MessageProcessor implements Runnable {
 
    private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

    /**
     * The listener that will be collecting error notifications.
     */
    private final ErrorHandler errorHandler;

    /**
     * The queue where we store incoming messages until they are collected.
     */
    private final BlockingQueue<RawMessage> messageQueue;

    /**
     * The listener that will be retrieving MessageEvents
     */
    private final MessageEventHandler messageEventHandler;

    /**
     * The NetAccessManager which has created this instance and which is its owner.
     */
    private final NetAccessManager netAccessManager;

    /**
     * A reference to the future that we use to execute ourselves.
     */
    private Future<?> future;

    /**
     * Creates a Message processor.
     *
     * @param netAccessManager the NetAccessManager which is creating
     * the new instance, is going to be its owner, specifies the
     * BlockingQueue which is to store incoming messages, specifies the
     * MessageEventHandler and represents the ErrorHandler to
     * handle exceptions in the new instance
     * @throws IllegalArgumentException if any of the mentioned properties of
     * netAccessManager are null
     */
    MessageProcessor(NetAccessManager netAccessManager) throws IllegalArgumentException {
        if (netAccessManager == null)
            throw new NullPointerException("netAccessManager");

        BlockingQueue<RawMessage> messageQueue = netAccessManager.getMessageQueue();

        if (messageQueue == null) {
            throw new IllegalArgumentException("The message queue may not be null");
        }

        MessageEventHandler messageEventHandler = netAccessManager.getMessageEventHandler();

        if (messageEventHandler == null) {
            throw new IllegalArgumentException("The message event handler may not be null");
        }

        this.netAccessManager = netAccessManager;
        this.messageQueue = messageQueue;
        this.messageEventHandler = messageEventHandler;
        this.errorHandler = netAccessManager;
    }

    /**
     * Does the message parsing.
     */
    public void run() {
        Thread.currentThread().setName("MessageProcessor@" + System.currentTimeMillis());
        //add an extra try/catch block that handles uncaught errors and helps
        //avoid having dead threads in our pools.
        try {
            StunStack stunStack = netAccessManager.getStunStack();
            while (true) {
                RawMessage rawMessage;
                try {
                    rawMessage = messageQueue.take();
                } catch (InterruptedException ex) {
                    if (logger.isDebugEnabled()) {
                        logger.warn("A net access point has gone useless", ex);
                    }
                    //nothing to do here since we test whether we are running just beneath ...
                    rawMessage = null;
                }
                //anything to parse?
                if (rawMessage == null) {
                    continue;
                }
                Message stunMessage = null;
                try {
                    stunMessage = Message.decode(rawMessage.getBytes(), 0, rawMessage.getMessageLength());
                } catch (StunException ex) {
                    errorHandler.handleError("Failed to decode a stun message!", ex);
                    continue; //let this one go and for better luck next time.
                }
                logger.trace("Dispatching a StunMessageEvent");
                StunMessageEvent stunMessageEvent = new StunMessageEvent(stunStack, rawMessage, stunMessage);
                messageEventHandler.handleMessageEvent(stunMessageEvent);
            }
        } catch (Throwable err) {
            //notify and bail
            errorHandler.handleFatalError(this, "Unexpected Error!", err);
        }
    }

    /**
     * Shut down the message processor.
     */
    void stop() {
        future.cancel(true);
    }

    /**
     * Sets a local reference to the future which owns this instance.
     * 
     * @param future
     */
    public void setFutureRef(Future<?> future) {
        this.future = future;
    }

}
