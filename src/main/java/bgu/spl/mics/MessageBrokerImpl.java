package bgu.spl.mics;

import java.util.*;
import java.util.concurrent.*;

/**
 * The {@link MessageBrokerImpl class is the implementation of the MessageBroker interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBrokerImpl implements MessageBroker {
	private ConcurrentMap<Subscriber, BlockingQueue<Message>> subscriberQueues;

	private ReadWriteLock eventsLock;
	private Map<Class<? extends Message>, Queue<Subscriber>> subscribedEvents;

	private ConcurrentMap<Event<?>, Future<?>> futures;

	/**
	 * Initializes this message broker instance
	 */
	public MessageBrokerImpl() {
		subscriberQueues = new ConcurrentHashMap<>();

		// TODO: should we use the standard java's library ReentrantReadWriteLock or the one seen in class
		// the one seen in class favor writers while the other does not
		eventsLock = new WriterFavoredReadWriteLock();

		// TODO: should we use a concurrent version of hash map?
		// TODO: should we use a list or a set?
		// TODO: should we use a concurrent version of the inner subscribers container?
		subscribedEvents = new HashMap<>();

		futures = new ConcurrentHashMap<>();
	}

	/**
	 * Retrieves the single instance of this class.
	 */
	public static MessageBroker getInstance() {
		return InstanceHolder.instance;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, Subscriber m) {
		subscribeMessage(type, m);
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, Subscriber m) {
		subscribeMessage(type, m);
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		Future<T> future = (Future<T>)futures.get(e);
		future.resolve(result);
		futures.remove(e, future);
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		eventsLock.acquireReadLock();
		try {
			Queue<Subscriber> subscribers = getMessageSubscribers(b);
			if (subscribers != null) {
				addBroadcastToSubscriberQueues(b, subscribers);
			}
		}
		finally {
			eventsLock.releaseReadLock();
		}
	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		eventsLock.acquireReadLock();
		try {
			Queue<Subscriber> subscribers = getMessageSubscribers(e);
			if (subscribers != null) {
				return roundRobinEvent(e, subscribers);
			}

			return null;
		}
		finally {
			eventsLock.releaseReadLock();
		}
	}

	@Override
	public void register(Subscriber m) {
		subscriberQueues.computeIfAbsent(m, s -> new LinkedBlockingQueue<>());
	}

	@Override
	public void unregister(Subscriber m) {
		eventsLock.acquireWriteLock();
		subscriberQueues.remove(m);
		try {
			Set<Class<? extends Message>> keys = subscribedEvents.keySet();
			// TODO: maybe not remove the subscriber from the queue, but skip it when we encounter in while sending events
			for (Class<? extends Message> key : keys) {
				Queue<Subscriber> subscribers = subscribedEvents.get(key);
				subscribers.remove(m);
			}
		}
		finally {
			eventsLock.releaseWriteLock();
		}
	}

	@Override
	public Message awaitMessage(Subscriber m) throws InterruptedException {
		// Return null if the subscriber doesn't exists
		BlockingQueue<Message> queue = getSubscriberQueue(m);
		if (queue != null) {
			return queue.take();
		}

		// TODO: ??? should it even be possible?
		return null;
	}

	private BlockingQueue<Message> getSubscriberQueue(Subscriber m) {
		return subscriberQueues.getOrDefault(m, null);
	}

	private Queue<Subscriber> getMessageSubscribers(Message msg) {
		return subscribedEvents.getOrDefault(msg.getClass(), null);
	}

	private <T> void subscribeMessage(Class<? extends Message> type, Subscriber m) {
		eventsLock.acquireWriteLock();
		try {
			Queue<Subscriber> subscribers = subscribedEvents.computeIfAbsent(type, c -> new ConcurrentLinkedQueue<>());
			subscribers.add(m);
		}
		finally {
			eventsLock.releaseWriteLock();
		}
	}

	private void addMessageToSubscriberQueue(Message msg, Subscriber subscriber) {
		BlockingQueue<Message> queue = getSubscriberQueue(subscriber);
		if (queue != null) {
			putToBlockingQueue(queue, msg);
		}
	}

	private void addBroadcastToSubscriberQueues(Broadcast b, Collection<Subscriber> subscribers) {
		for (Subscriber subscriber : subscribers) {
			addMessageToSubscriberQueue(b, subscriber);
		}
	}

	private <T> Future<T> roundRobinEvent(Event<T> e, Queue<Subscriber> subscribers) {
		synchronized (subscribedEvents.get(e.getClass())) {
			Subscriber subscriber = subscribers.poll();
			if (subscriber == null) {
				return null;
			}

			return handEventToSubscriber(e, subscribers, subscriber);
		}
	}

	private <T> Future<T> handEventToSubscriber(Event<T> e, Queue<Subscriber> subscribers, Subscriber subscriber) {
		Future<T> future = new Future<>();
		futures.put(e, future);
		addMessageToSubscriberQueue(e, subscriber);
		subscribers.add(subscriber);
		return future;
	}

	/**
	 * Puts an item to a blocking queue. The put is tried until it fully completes with interruptions.
	 * @param queue The queue to put into
	 * @param e The item to put into the queue
	 * @param <E> The item's type
	 */
	private static <E> void putToBlockingQueue(BlockingQueue<E> queue, E e) {
		boolean successfulAdd = false;
		while (!successfulAdd) {
			try {
				queue.put(e);
				successfulAdd = true;
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	private static class InstanceHolder {
		public static final MessageBrokerImpl instance = new MessageBrokerImpl();
	}
}
