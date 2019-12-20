package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.MissionReceivedEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.MissionInfo;

import javax.print.attribute.IntegerSyntax;
import java.util.*;

/**
 * A Publisher\Subscriber.
 * Holds a list of Info objects and sends them
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Intelligence extends Subscriber {

	private Map<Integer, LinkedList<MissionInfo>> missionInfos;
	//TODO init the list
	public Intelligence(String name, MissionInfo[] missions) {
		super(name);
		missionInfos = new HashMap<>();
		for(MissionInfo mission : missions){
			// TODO: initialize list
			LinkedList<MissionInfo> list =  missionInfos.get(mission.getTimeIssued());
			list.add(mission);
		}
	}

	@Override
	protected void initialize() {
		// TODO: handle last tick
		subscribeBroadcast(TickBroadcast.class, this::callBack);
	}

	//TODO check if any other subscriber can alter the mission, if so edit the code to be tread safe
	private void callBack(TickBroadcast tick){
		int tickTime = tick.getTick();
		LinkedList<MissionInfo> list =  missionInfos.getOrDefault(tickTime, null);
		if(list != null){
			for (MissionInfo mission: list) {
				sendEvent(new MissionReceivedEvent(mission));
			}
		}

	}
}
