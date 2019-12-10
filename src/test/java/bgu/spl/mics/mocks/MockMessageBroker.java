package bgu.spl.mics.mocks;

import bgu.spl.mics.*;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MockMessageBroker implements MessageBroker {
    private LinkedList<Subscriber> subscribers;
    private Map<Subscriber, BlockingQueue<Message>> subscriberMessageQueues;
    private Map<Subscriber, Set<Class<? extends Message>>> subscriberSubscribedMessages;
    private Map<Event<?>, Future<?>> futureMap;

    public MockMessageBroker() {
        subscribers = new LinkedList<>();
        subscriberMessageQueues = new HashMap<>();
        subscriberSubscribedMessages = new HashMap<>();
        futureMap = new HashMap<>();
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, Subscriber s) {
        if (!subscriberSubscribedMessages.containsKey(s)) {
            return;
        }

        subscriberSubscribedMessages.get(s).add(type);
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, Subscriber s) {
        if (!subscriberSubscribedMessages.containsKey(s)) {
            return;
        }

        subscriberSubscribedMessages.get(s).add(type);
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        if (!futureMap.containsKey(e)) {
            return;
        }

        ((Future<T>)futureMap.get(e)).resolve(result);
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        for (Subscriber s : subscribers) {
            if (subscriberSubscribedMessages.get(s).contains(b.getClass())) {
                subscriberMessageQueues.get(s).add(b);
            }
        }
    }

    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        for (Subscriber s : subscribers) {
            if (subscriberSubscribedMessages.get(s).contains(e.getClass())) {
                subscriberMessageQueues.get(s).add(e);
                Future<T> future = new Future<>();
                futureMap.put(e, future);
                return future;
            }
        }

        return null;
    }

    @Override
    public void register(Subscriber s) {
        if (!subscriberMessageQueues.containsKey(s)) {
            subscribers.add(s);
            subscriberMessageQueues.put(s, new LinkedBlockingQueue<>());
            subscriberSubscribedMessages.put(s, new HashSet<>());
        }
    }

    @Override
    public void unregister(Subscriber s) {
        if (subscriberMessageQueues.containsKey(s)) {
            subscribers.remove(s);
            subscriberMessageQueues.remove(s);
            subscriberSubscribedMessages.remove(s);
        }
    }

    @Override
    public Message awaitMessage(Subscriber s) throws InterruptedException {
        if (!subscriberMessageQueues.containsKey(s)) {
            return null;
        }

        return subscriberMessageQueues.get(s).take();
    }
}
