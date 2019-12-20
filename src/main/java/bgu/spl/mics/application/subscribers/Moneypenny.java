package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.AgentsAvailableEvent;;
import bgu.spl.mics.application.messages.LastTickBroadcast;
import bgu.spl.mics.application.messages.ReleaseAgentsEvent;
import bgu.spl.mics.application.messages.SendAgentsEvent;
import bgu.spl.mics.application.messages.eventsInfo.AgentsAvailableResult;
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
	// TODO: how should moneypenny know to terminate herself? she might be acquiring, releasing or sending agents and the last tick message might be a long way into the message queue

	public enum SubscribeTO {AgentsAvailable, SendAndRelease}

	private Squad squad;
	private int id;
	private SubscribeTO subscribeTo;

	public Moneypenny(int id, SubscribeTO subscribeTo) {
		super("Moneypenny: "+id);
		this.id = id;
		this.subscribeTo = subscribeTo;
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
	}

	/**
	 * CallBack function to handle the AgentAvaibleEvent
	 * @param aAE AgentsAvailableEvent
	 */
	private void agentsAvailableCallback(AgentsAvailableEvent aAE){
		List<String> agents = aAE.getArgs().agentsSerialNumbers();
		boolean agentsExist = squad.getAgents(agents);
		// TODO: get the names of the agents, not their serial number
		AgentsAvailableResult agentsAvailableResult = new AgentsAvailableResult(agentsExist, agents, id);
		complete(aAE, agentsAvailableResult);
	}

	private void sendAgentsCallback(SendAgentsEvent sendAgentsEvent){
		//TODO implement
	}

	private void releaseAgentsCallback(ReleaseAgentsEvent releaseAgentsEvent){
		//TODO implement
	}

	private void lastTickBroadcastCallback(LastTickBroadcast lastTickBroadcast){
		terminate();
	}

}

