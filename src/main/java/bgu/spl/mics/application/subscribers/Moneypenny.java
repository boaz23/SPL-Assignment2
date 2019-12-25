package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Subscriber;
import bgu.spl.mics.loggers.Loggers;
import bgu.spl.mics.application.messages.AgentsAvailableEvent;;
import bgu.spl.mics.application.messages.LastTickBroadcast;
import bgu.spl.mics.application.messages.ReleaseAgentsEvent;
import bgu.spl.mics.application.messages.SendAgentsEvent;
import bgu.spl.mics.application.messages.eventsInfo.AgentsAvailableResult;
import bgu.spl.mics.application.messages.eventsInfo.ReleaseAgentsEventArgs;
import bgu.spl.mics.application.messages.eventsInfo.SendAgentsEventArgs;
import bgu.spl.mics.application.passiveObjects.Squad;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Only this type of Subscriber can access the squad.
 * Three are several Moneypenny-instances - each of them holds a unique serial number that will later be printed on the report.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Moneypenny extends Subscriber {
	public enum SubscribeTO {AgentsAvailable, SendAndRelease}

	private Squad squad;
	private int id;
	private final Releaser releaser;
	private SubscribeTO subscribeTo;
	private final CountDownLatch subRegisterAwaiter;
	private List<String> allAgentsSerialNumbers;

	public Moneypenny(int id, Releaser releaser, SubscribeTO subscribeTo, CountDownLatch subRegisterAwaiter) {
		super("Moneypenny"+ id);
		this.id = id;
		this.releaser = releaser;
		this.subscribeTo = subscribeTo;
		this.subRegisterAwaiter = subRegisterAwaiter;
		squad = Squad.getInstance();
	}

	/**
	 * Initialize the class , use the {@link SubscribeTO} which event
	 * to listen.
	 */
	@Override
	protected void initialize() {
		subscribeBroadcast(LastTickBroadcast.class, this::lastTickBroadcastCallback);
		if(subscribeTo == SubscribeTO.AgentsAvailable) {
			subscribeEvent(AgentsAvailableEvent.class, this::agentsAvailableCallback);
		} else if(subscribeTo == SubscribeTO.SendAndRelease) {
			subscribeEvent(SendAgentsEvent.class, this::sendAgentsCallback);
			subscribeEvent(ReleaseAgentsEvent.class, this::releaseAgentsCallback);
		}
		subRegisterAwaiter.countDown();
	}

	/**
	 * CallBack function to handle the AgentAvaibleEvent
	 * @param aAE AgentsAvailableEvent
	 */
	private void agentsAvailableCallback(AgentsAvailableEvent aAE) throws InterruptedException {
		List<String> agents = aAE.getArgs().agentsSerialNumbers();
		boolean agentsExist = squad.getAgents(agents);
		releaser.notifyHelpers();

		Loggers.DefaultLogger.appendLine(getName() + " completing " + aAE);
		AgentsAvailableResult agentsAvailableResult = new AgentsAvailableResult(agentsExist,
				agentsExist ? squad.getAgentsNames(agents) : null, agents, id);
		complete(aAE, agentsAvailableResult);
	}

	private void sendAgentsCallback(SendAgentsEvent sendAgentsEvent) throws InterruptedException {
		SendAgentsEventArgs sendAgentsEventArgs = sendAgentsEvent.getArgs();
		Loggers.DefaultLogger.appendLine(getName() + " executing mission: '" + sendAgentsEvent.getArgs().getMissionName() + "'");
		squad.sendAgents(sendAgentsEventArgs.serialAgentsNumbers(),
				sendAgentsEventArgs.duration());
		complete(sendAgentsEvent, null);
		Loggers.DefaultLogger.appendLine("Mission ended: '" + sendAgentsEvent.getArgs().getMissionName() + "'");
	}

	private void releaseAgentsCallback(ReleaseAgentsEvent releaseAgentsEvent){
		Loggers.DefaultLogger.appendLine(getName() + " handling " + releaseAgentsEvent);
		ReleaseAgentsEventArgs releaseAgentsEventArgs = releaseAgentsEvent.getArgs();
		releaseAgents(releaseAgentsEventArgs.serialAgentsNumbers());
		Loggers.DefaultLogger.appendLine(getName() + " completing " + releaseAgentsEvent);
		complete(releaseAgentsEvent, null);
	}

	private void lastTickBroadcastCallback(LastTickBroadcast lastTickBroadcast) throws InterruptedException {
		cleanupAndTerminate();
	}

	private void cleanupAndTerminate() throws InterruptedException {
		if (subscribeTo == SubscribeTO.AgentsAvailable) {
			releaser.decrement();
		}
		else {
			helpReleaseAgents();
		}

		terminate();
	}

	private void helpReleaseAgents() throws InterruptedException {
		synchronized (releaser) {
			while (releaser.count() > 0) {
				releaseAllAgents();
				releaser.awaitRelease();
			}
		}
	}

	private void releaseAllAgents() {
		releaseAgents(getAllAgentsSerialNumbers());
	}

	private void releaseAgents(List<String> allAgentsSerialNumbers) {
		squad.releaseAgents(allAgentsSerialNumbers);
	}

	private List<String> getAllAgentsSerialNumbers() {
		if (allAgentsSerialNumbers == null) {
			Set<String> allAgentsSerialNumbers = squad.getAgentsMap().keySet();
			this.allAgentsSerialNumbers = new ArrayList<>(allAgentsSerialNumbers);
		}
		return allAgentsSerialNumbers;
	}

	/**
	 * An object responsible for releasing agents when the program is terminating to
	 * allow moneypennies who are stuck on agents acquisition to terminate gracefully
	 */
	public static class Releaser {
		private AtomicInteger count;

		/**
		 * Initializes a new releaser with the given count
		 * @param count The count of moneypennies who perform get agents who'll need
		 * to be terminated before terminating the rest
		 */
		public Releaser(int count) {
			this.count = new AtomicInteger(count);
		}

		/**
		 * @return The amount of remaining moneypennies to wait for
		 */
		public int count() {
			return count.get();
		}

		/**
		 * Notifies that there's one less moneypenny to wait for termination
		 */
		public void decrement() {
			count.decrementAndGet();
			notifyHelpers();
		}

		/**
		 * Notify all moneypennies who release to act if necessary
		 */
		public void notifyHelpers() {
			synchronized (this) {
				notifyAll();
			}
		}

		/**
		 * Waits for the notification of a moneypenny who get agents and terminates or about to acquire them
		 * @throws InterruptedException see Object.wait() exception
		 */
		public void awaitRelease() throws InterruptedException {
			synchronized (this) {
				wait();
			}
		}
	}
}

