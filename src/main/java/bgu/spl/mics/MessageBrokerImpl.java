package bgu.spl.mics;

import bgu.spl.mics.application.Loggers;

import java.util.*;
import java.util.concurrent.*;

/**
 * The {@link MessageBrokerImpl class is the implementation of the MessageBroker interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBrokerImpl implements MessageBroker {
	private ConcurrentMap<Subscriber, BlockingQueue<Message>> subscriberQueues;

	/**
	 * Lock for reading/writing to the map itself, not for the inner containers (queues).
	 * The inner containers should be synchronized independently.
	 */
	private ReadWriteLock eventsLock;
	private Map<Class<? extends Message>, Queue<Subscriber>> subscribedEvents;

	private ConcurrentMap<Event<?>, Future<?>> futures;

	/**
	 * Initializes this message broker instance
	 */
	public MessageBrokerImpl() {
		subscriberQueues = new ConcurrentHashMap<>();

		// We should use the read/write lock seen in class rather than the java's library ReentrantReadWriteLock
		// because the one seen in class favor writers while the other does not,
		// so it seems we should use the variant seen in class
		eventsLock = new WriterFavoredReadWriteLock();

		// We should use a non-concurrent map, since the access to it is protected by a lock.
		// We should use a concurrent version because we need to synchronizes
		// access to each one in the round robin.
		// Concurrent queue seems like a good idea since it's non-blocking, thread safe
		// and allows easy implementation of the round robin mechanism
		// (while not hurting sending broadcasts).
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

	@SuppressWarnings("unchecked")
	@Override
	public <T> void complete(Event<T> e, T result) {
		// There shouldn't be a problem if context switch happens mid-execution of this method, since:
		// 1. Only one subscriber is handed the event, therefore only one will complete it.
		// 2. If a future stays in the map for no reason, no harm can come out of it
		Future<T> future = (Future<T>)futures.get(e);
		future.resolve(result);
		futures.remove(e, future);
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		eventsLock.acquireReadLock();
		try {
			Loggers.DefaultLogger.appendLine(Thread.currentThread().getName() + " sending " + b);

			Queue<Subscriber> subscribers = getMessageSubscribers(b);
			if (subscribers != null) {
				addBroadcastToSubscriberQueues(b, subscribers);
			}
			else {
				Loggers.DefaultLogger.appendLine("No one is subbed to '" + b.getClass().getName());
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
			Loggers.DefaultLogger.appendLine(Thread.currentThread().getName() + " sending " + e);

			Queue<Subscriber> subscribers = getMessageSubscribers(e);
			if (subscribers != null) {
				return roundRobinEvent(e, subscribers);
			}
			else {
				Loggers.DefaultLogger.appendLine("No one is subbed to '" + e.getClass().getName());
			}

			return null;
		}
		finally {
			eventsLock.releaseReadLock();
		}
	}

	@Override
	public void register(Subscriber m) {
		subscriberQueues.computeIfAbsent(m, this::createSubscriberMessageQueue);
	}

	@Override
	public void unregister(Subscriber m) {
		BlockingQueue<Message> subscriberMsgQueue = removeSubscriber(m);
		completeFutures(subscriberMsgQueue);
	}

	@Override
	public Message awaitMessage(Subscriber m) throws InterruptedException {
		BlockingQueue<Message> queue = getSubscriberQueue(m);
		return queue.take();
	}

	private BlockingQueue<Message> createSubscriberMessageQueue(Subscriber s) {
		return new LinkedBlockingQueue<>();
	}

	private Queue<Subscriber> createContainerForMessageType(Class<? extends Message> c) {
		return new ConcurrentLinkedQueue<>();
	}

	private BlockingQueue<Message> getSubscriberQueue(Subscriber m) {
		return subscriberQueues.get(m);
	}

	private Queue<Subscriber> getMessageSubscribers(Message msg) {
		return subscribedEvents.getOrDefault(msg.getClass(), null);
	}

	private <T> void subscribeMessage(Class<? extends Message> type, Subscriber m) {
		eventsLock.acquireWriteLock();
		try {
			Queue<Subscriber> subscribers = subscribedEvents.computeIfAbsent(type, this::createContainerForMessageType);
			subscribers.add(m);
		}
		finally {
			eventsLock.releaseWriteLock();
		}
	}

	private void addMessageToSubscriberQueue(Message msg, Subscriber subscriber) {
		BlockingQueue<Message> queue = getSubscriberQueue(subscriber);
		putToBlockingQueue(queue, msg);
	}

	private void addBroadcastToSubscriberQueues(Broadcast b, Collection<Subscriber> subscribers) {
		// No (further) synchronization (beyond the lock for the queues map) is needed,
		// since after registration, the container doesn't change (for a broadcast message type)
		for (Subscriber subscriber : subscribers) {
			Loggers.DefaultLogger.appendLine(subscriber.getName() + " assigned " + b);
			addMessageToSubscriberQueue(b, subscriber);
		}
	}

	private <T> Future<T> roundRobinEvent(Event<T> e, Queue<Subscriber> subscribers) {
		// Many threads may try to send an event of this type,
		// we need to make sure the queue (for this type of message) stays valid
		synchronized (subscribedEvents.get(e.getClass())) {
			Subscriber subscriber = subscribers.poll();
			if (subscriber == null) {
				// No one is subscribed
				Loggers.DefaultLogger.appendLine("No one is subbed to '" + e.getClass().getName());
				return null;
			}

			Loggers.DefaultLogger.appendLine(subscriber.getName() + " assigned " + e);
			Future<T> future = handEventToSubscriber(e, subscriber);
			subscribers.add(subscriber);
			return future;
		}
	}

	private <T> Future<T> handEventToSubscriber(Event<T> e, Subscriber subscriber) {
		Future<T> future = new Future<>();
		futures.put(e, future);
		addMessageToSubscriberQueue(e, subscriber);
		return future;
	}

	private void unsubscribeFromMessages(Subscriber m) {
		Set<Class<? extends Message>> messageTypes = subscribedEvents.keySet();
		Iterator<Class<? extends Message>> iterator = messageTypes.iterator();
		unsubscribeFromMessages(m, iterator);
	}

	private void unsubscribeFromMessages(Subscriber m, Iterator<Class<? extends Message>> iterator) {
		while (iterator.hasNext()) {
			Class<? extends Message> msgType = iterator.next();
			Queue<Subscriber> subscribers = subscribedEvents.get(msgType);
			subscribers.remove(m);

			// Removes the message queue for the type if no one is subscribed to the message
			if (subscribers.isEmpty()) {
				iterator.remove();
			}
		}
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
			} catch (InterruptedException ignored) {
			}
		}
	}

	private BlockingQueue<Message> removeSubscriber(Subscriber m) {
		eventsLock.acquireWriteLock();
		try {
			BlockingQueue<Message> subscriberMsgQueue = subscriberQueues.remove(m);
			unsubscribeFromMessages(m);
			return subscriberMsgQueue;
		}
		finally {
			eventsLock.releaseWriteLock();
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void completeFutures(BlockingQueue<Message> subscriberMsgQueue) {
		for (Message msg : subscriberMsgQueue) {
			if (msg instanceof Event) {
				Event event = (Event)msg;
				complete(event, null);
			}
		}
		subscriberMsgQueue.clear();
	}

	private static class InstanceHolder {
		public static final MessageBrokerImpl instance = new MessageBrokerImpl();
	}
}
