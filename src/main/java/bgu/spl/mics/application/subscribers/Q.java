package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.GadgetAvailableEvent;
import bgu.spl.mics.application.messages.LastTickBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.eventsInfo.GadgetAvailableEventArgs;
import bgu.spl.mics.application.messages.eventsInfo.GadgetAvailableResult;
import bgu.spl.mics.application.passiveObjects.Inventory;

/**
 * Q is the only Subscriber\Publisher that has access to the {@link bgu.spl.mics.application.passiveObjects.Inventory}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Q extends Subscriber {
	private int lastTick;
	private final Inventory inventory;

	public Q(Inventory inventory) {
		super("Q");
		this.inventory = inventory;
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(LastTickBroadcast.class, this::onLastTimeTick);
		subscribeBroadcast(TickBroadcast.class, this::onTimeTick);
		subscribeEvent(GadgetAvailableEvent.class, this::onGadgetAvailableEvent);
	}

	private void onLastTimeTick(LastTickBroadcast b) {
		onTimeTick(b);
		terminate();
	}

	private void onTimeTick(TickBroadcast b) {
		lastTick = b.getTick();
	}

	/*
	That may cause some missions not to get executed, for example:
	One Q retrieves gadget 'A'. The M who handles the mission who needed that gadget might not be executed because of time constraints.
	Then another Q wants to retrieve that gadget but it's unavailable, so the other mission fails because it doesn't have the gadget.
	It is possible that if the second Q got the gadget, the second mission would be executed.
	 */
	private void onGadgetAvailableEvent(GadgetAvailableEvent gadgetAvailableEvent) {
		GadgetAvailableEventArgs args = gadgetAvailableEvent.getArgs();
		boolean isAvailable = inventory.getItem(args.gadget());
		GadgetAvailableResult result = new GadgetAvailableResult(isAvailable, lastTick);
		complete(gadgetAvailableEvent, result);
	}
}
