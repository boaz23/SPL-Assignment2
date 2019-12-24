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

import java.util.List;

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
	private SubscribeTO subscribeTo;

	private List<String> lastAcquiredAgents;

	public Moneypenny(int id, SubscribeTO subscribeTo) {
		super("Moneypenny"+ id);
		this.id = id;
		this.subscribeTo = subscribeTo;
		squad = Squad.getInstance();
		lastAcquiredAgents = null;
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
	}

	/**
	 * CallBack function to handle the AgentAvaibleEvent
	 * @param aAE AgentsAvailableEvent
	 */
	private void agentsAvailableCallback(AgentsAvailableEvent aAE){
//		Loggers.MnMPLogger.appendLine(getName() + " handling " + aAE);

		List<String> agents = aAE.getArgs().agentsSerialNumbers();
		boolean agentsExist = squad.getAgents(agents);

		if (agentsExist) {
			lastAcquiredAgents = agents;
		}
		if (Thread.currentThread().isInterrupted()) {
			Loggers.DefaultLogger.appendLine(getName() + " interrupted");
			releaseAndTerminate();
		}
		else {
			Loggers.DefaultLogger.appendLine(getName() + " completing " + aAE);
			AgentsAvailableResult agentsAvailableResult = new AgentsAvailableResult(agentsExist,
					agentsExist ? squad.getAgentsNames(agents) : null, agents, id);
			complete(aAE, agentsAvailableResult);
		}
	}

	private void sendAgentsCallback(SendAgentsEvent sendAgentsEvent){
		Loggers.MnMPLogger.appendLine(getName() + " handling " + sendAgentsEvent);

		SendAgentsEventArgs sendAgentsEventArgs = sendAgentsEvent.getArgs();
		Loggers.DefaultLogger.appendLine(getName() + " executing mission: '" + sendAgentsEvent.getArgs().getMissionName() + "'");
		squad.sendAgents(sendAgentsEventArgs.serialAgentsNumbers(),
				sendAgentsEventArgs.duration());

		if (Thread.currentThread().isInterrupted()) {
			Loggers.MnMPLogger.appendLine(Thread.currentThread().getName() + " interrupted while in mission " + sendAgentsEvent.getArgs().getMissionName());
			terminate();
		} else {
			Loggers.DefaultLogger.appendLine("Mission ended: '" + sendAgentsEvent.getArgs().getMissionName() + "'");
			complete(sendAgentsEvent, null);
		}
	}

	private void releaseAgentsCallback(ReleaseAgentsEvent releaseAgentsEvent){
		Loggers.DefaultLogger.appendLine(getName() + " handling " + releaseAgentsEvent);
		ReleaseAgentsEventArgs releaseAgentsEventArgs = releaseAgentsEvent.getArgs();
		squad.releaseAgents(releaseAgentsEventArgs.serialAgentsNumbers());
		Loggers.DefaultLogger.appendLine(getName() + " completing " + releaseAgentsEvent);
		complete(releaseAgentsEvent, null);
	}

	private void lastTickBroadcastCallback(LastTickBroadcast lastTickBroadcast){
		releaseAndTerminate();
	}

	private void releaseAndTerminate() {
		if (lastAcquiredAgents != null) {
			squad.releaseAgents(lastAcquiredAgents);
		}

		terminate();
	}
}

