package bgu.spl.mics;

abstract class RunnableSubPub implements Runnable {
    private final String name;
    private final SimplePublisher simplePublisher;

    /**
     * this method is called once when the event loop starts.
     */
    protected abstract void initialize();

    /**
     * @param name the Publisher/Subscriber name (used mainly for debugging purposes -
     *             does not have to be unique)
     */
    RunnableSubPub(String name) {
        this.name = name;
        simplePublisher = new SimplePublisher();
    }

    /**
     * @return the name of the Publisher/Subscriber - the Publisher/Subscriber name is given to it in the
     *         construction time and is used mainly for debugging purposes.
     */
    public final String getName() {
        return name;
    }

    /**
     * The entry point of the publisher/subscriber.
     * otherwise you will end up in an infinite loop.
     */
    @Override
    public abstract void run();

    /**
     * @return the simple publisher
     */
    public SimplePublisher getSimplePublisher() {
        return simplePublisher;
    }

    /**
     * Sends an event
     * @param e The event to send
     * @param <T> The event's result type
     * @return A future for the result
     */
    protected <T> Future<T> sendEvent(Event<T> e) throws InterruptedException {
        return simplePublisher.sendEvent(e);
    }

    /**
     * Sends a broadcast
     * @param b The broadcast to send
     */
    protected void sendBroadcast(Broadcast b) throws InterruptedException {
        simplePublisher.sendBroadcast(b);
    }
}
