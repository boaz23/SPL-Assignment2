package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.GadgetAvailableEvent;
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
	private final int serialNumber;
	private int lastTick;
	private final Inventory inventory;

	public Q(int serialNumber) {
		super("Q" + serialNumber);
		this.serialNumber = serialNumber;
		inventory = Inventory.getInstance();
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, this::onTimeTick);
		subscribeEvent(GadgetAvailableEvent.class, this::onGadgetAvailableEvent);
	}

	// TODO: the last tick we got may not be updated when we're handling events
	private void onTimeTick(TickBroadcast b) {
		lastTick = b.getTick();
	}

	private void onGadgetAvailableEvent(GadgetAvailableEvent gadgetAvailableEvent) {
		GadgetAvailableEventArgs args = gadgetAvailableEvent.getArgs();
		boolean isAvailable = inventory.getItem(args.gadget());
		GadgetAvailableResult result = new GadgetAvailableResult(isAvailable, lastTick, serialNumber);
		complete(gadgetAvailableEvent, result);
	}
}
