package bgu.spl.mics;

import bgu.spl.mics.loggers.Loggers;

import java.util.HashMap;
import java.util.Map;

/**
 * The Subscriber is an abstract class that any subscriber in the system
 * must extend. The abstract Subscriber class is responsible to get and
 * manipulate the singleton {@link MessageBroker} instance.
 * <p>
 * Derived classes of Subscriber should never directly touch the MessageBroker.
 * the derived class also supplies a {@link Callback} that should be called when
 * a message of the subscribed type was taken from the Subscriber
 * message-queue (see {@link MessageBroker#register(Subscriber)}
 * method). The abstract Subscriber stores this callback together with the
 * type of the message is related to.
 * 
 * Only private fields and methods may be added to this class.
 * <p>
 */
public abstract class Subscriber extends RunnableSubPub {
    private boolean terminated = false;
    private MessageBroker messageBroker;
    private Map<Class<? extends Message>, Callback<? extends Message>> messageCallbacks;

    /**
     * The thread which this instance will run in
     */
    private Thread hostThread;

    /**
     * @param name the Subscriber name (used mainly for debugging purposes -
     *             does not have to be unique)
     */
    public Subscriber(String name) {
        super(name);
        messageBroker = MessageBrokerImpl.getInstance();
        messageCallbacks = new HashMap<>();
    }

    /**
     * Subscribes to events of type {@code type} with the callback
     * {@code callback}. This means two things:
     * 1. Subscribe to events in the singleton MessageBroker using the supplied
     * {@code type}
     * 2. Store the {@code callback} so that when events of type {@code type}
     * are received it will be called.
     * <p>
     * For a received message {@code m} of type {@code type = m.getClass()}
     * calling the callback {@code callback} means running the method
     * {@link Callback#call(java.lang.Object)} by calling
     * {@code callback.call(m)}.
     * <p>
     * @param <E>      The type of event to subscribe to.
     * @param <T>      The type of result expected for the subscribed event.
     * @param type     The {@link Class} representing the type of event to
     *                 subscribe to.
     * @param callback The callback that should be called when messages of type
     *                 {@code type} are taken from this Subscriber message
     *                 queue.
     */
    protected final <T, E extends Event<T>> void subscribeEvent(Class<E> type, Callback<E> callback) {
        if (!messageCallbacks.containsKey(type)) {
            messageBroker.subscribeEvent(type, this);
        }
        messageCallbacks.put(type, callback);
    }

    /**
     * Subscribes to broadcast message of type {@code type} with the callback
     * {@code callback}. This means two things:
     * 1. Subscribe to broadcast messages in the singleton MessageBroker using the
     * supplied {@code type}
     * 2. Store the {@code callback} so that when broadcast messages of type
     * {@code type} received it will be called.
     * <p>
     * For a received message {@code m} of type {@code type = m.getClass()}
     * calling the callback {@code callback} means running the method
     * {@link Callback#call(java.lang.Object)} by calling
     * {@code callback.call(m)}.
     * <p>
     * @param <B>      The type of broadcast message to subscribe to
     * @param type     The {@link Class} representing the type of broadcast
     *                 message to subscribe to.
     * @param callback The callback that should be called when messages of type
     *                 {@code type} are taken from this Subscriber message
     *                 queue.
     */
    protected final <B extends Broadcast> void subscribeBroadcast(Class<B> type, Callback<B> callback) {
        if (!messageCallbacks.containsKey(type)) {
            messageBroker.subscribeBroadcast(type, this);
        }
        messageCallbacks.put(type, callback);
    }

    /**
     * Completes the received request {@code e} with the result {@code result}
     * using the MessageBroker.
     * <p>
     * @param <T>    The type of the expected result of the processed event
     *               {@code e}.
     * @param e      The event to complete.
     * @param result The result to resolve the relevant Future object.
     *               {@code e}.
     */
    protected final <T> void complete(Event<T> e, T result) {
        messageBroker.complete(e, result);
    }

    /**
     * Signals the event loop that it must terminate after handling the current
     * message.
     */
    protected final void terminate() {
        this.terminated = true;
        if (hostThread != null) {
            hostThread.interrupt();
        }
    }

    /**
     * The entry point of the Subscriber.
     * otherwise you will end up in an infinite loop.
     */
    @Override
    public final void run() {
        hostThread = Thread.currentThread();
        messageBroker.register(this);

        initialize();
        try {
            while (!shouldTerminate()) {
                if (!awaitAndProcessMessage()) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            terminated = true;
        }

        Loggers.MnMPLogger.appendLine(getName() + " terminating...");
        messageBroker.unregister(this);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean awaitAndProcessMessage() throws InterruptedException {
        Message message = messageBroker.awaitMessage(this);

        // Check if we were requested to terminate between waiting and the
        // loop condition check before handling the message
        if (shouldTerminate()) {
            return false;
        }

        // !!! It should be type safe because we only add callbacks with their matching type
        // (hopefully no one actively 'tricks' the generic subscribe method) !!!
        Callback callback = messageCallbacks.get(message.getClass());
        Loggers.DefaultLogger.appendLine(getName() + " handling " + message);
        callback.call(message);
        return true;
    }

    private boolean shouldTerminate() {
        return terminated || hostThread.isInterrupted();
    }
}
