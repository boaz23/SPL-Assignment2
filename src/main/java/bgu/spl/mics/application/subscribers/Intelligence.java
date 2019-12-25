package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.LastTickBroadcast;
import bgu.spl.mics.application.messages.MissionReceivedEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.MissionInfo;

import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * A Publisher\Subscriber.
 * Holds a list of Info objects and sends them
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Intelligence extends Subscriber {

	private final CountDownLatch subRegisterAwaiter;
	private Map<Integer, LinkedList<MissionInfo>> missionInfos;

	public Intelligence(String name, MissionInfo[] missions, CountDownLatch subRegisterAwaiter) {
		super(name);
		this.subRegisterAwaiter = subRegisterAwaiter;
		missionInfos = new HashMap<>();
		for(MissionInfo mission : missions){
			List<MissionInfo> list =  missionInfos.computeIfAbsent(mission.getTimeIssued(), t -> new LinkedList<>());
			list.add(mission);
		}
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, this::TickBroadcastCallBack);
		subscribeBroadcast(LastTickBroadcast.class, this::lastTickBroadcast);
		subRegisterAwaiter.countDown();
	}

	private void TickBroadcastCallBack(TickBroadcast tick) throws InterruptedException {
		int tickTime = tick.getTick();
		LinkedList<MissionInfo> list =  missionInfos.getOrDefault(tickTime, null);
		if(list != null){
			for (MissionInfo mission: list) {
				sendEvent(new MissionReceivedEvent(mission));
			}
		}

	}

	private void lastTickBroadcast(LastTickBroadcast lastTickBroadcast){
		terminate();
	}
}
