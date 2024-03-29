package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Future;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.messages.eventsInfo.*;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.MissionInfo;
import bgu.spl.mics.application.passiveObjects.Report;
import bgu.spl.mics.loggers.Loggers;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * M handles ReadyEvent - fills a report and sends agents to mission.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class M extends Subscriber {
	private static final int MAX_MISSION_NEED_TRIES = 3;

	private final int serialNumber;
	private int lastTick;
	private final Diary diary;
	private final CountDownLatch subRegisterAwaiter;

	public M(int serialNumber, Diary diary, CountDownLatch subRegisterAwaiter) {
		super("M" + serialNumber);
		this.serialNumber = serialNumber;
		this.diary = diary;
		this.subRegisterAwaiter = subRegisterAwaiter;
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(LastTickBroadcast.class, this::onLastTimeTick);
		subscribeBroadcast(TickBroadcast.class, this::onTimeTick);
		subscribeEvent(MissionReceivedEvent.class, this::onMissionReceived);
		subRegisterAwaiter.countDown();
	}

	private void onLastTimeTick(LastTickBroadcast b) {
		onTimeTick(b);
		terminate();
	}

	private void onTimeTick(TickBroadcast b) {
		lastTick = b.getTick();
	}

	private void onMissionReceived(MissionReceivedEvent e) throws InterruptedException {
		Loggers.MnMPLogger.appendLine(getName() + " handling on tick " + lastTick + ": " + e);

		MissionInfo missionInfo = e.getMissionInfo();
		diary.incrementTotal();

		MissionPreparation missionPreparation = checkValidity(missionInfo);
		switch (missionPreparation.getStatus()) {
			case Terminate:
				terminate();
				break;
			case Execute:
				Loggers.MnMPLogger.appendLine(getName() + " executing mission " + missionInfo.getMissionName());
				sendAgents(missionInfo);
				reportMission(missionInfo, missionPreparation);
				break;
			case Abort:
				Loggers.MnMPLogger.appendLine(getName() + " aborting mission " + missionInfo.getMissionName());
				if (missionPreparation.shouldReleaseAgents()) {
					Loggers.MnMPLogger.appendLine(getName() + " releasing agents for mission " + missionInfo.getMissionName());
					releaseAgents(missionInfo);
				}
				break;
		}
	}

	private MissionPreparation checkValidity(MissionInfo missionInfo) throws InterruptedException {
		MissionPreparation missionPreparation = new MissionPreparation();
		MissionPreparationNeedProvider<?>[] missionNeeds = new MissionPreparationNeedProvider<?>[] {
			new AgentsNeedProvider(missionInfo, missionPreparation),
			new GadgetNeedProvider(missionInfo, missionPreparation),
		};

		for (MissionPreparationNeedProvider<?> missionNeed : missionNeeds) {
			Loggers.MnMPLogger.appendLine(getName() + " trying to fulfill need " + missionNeed.getName());
			if (!missionNeed.tryFulfillNeed()) {
				Loggers.MnMPLogger.appendLine(getName() + ": Need " + missionNeed.getName() + " failed to be fulfilled");
				// Abort the mission because one of the mission prerequisite failed to be fulfilled
				return missionPreparation;
			}
		}

		if (lastTick >= missionInfo.getTimeExpired()) {
			Loggers.MnMPLogger.appendLine(getName() + ": Time expired for mission " + missionInfo.getMissionName());
			// Mission time's expired
			return missionPreparation;
		}

		missionPreparation.setShouldReleaseAgents(false);
		missionPreparation.setStatus(ActionStatus.Execute);
		return missionPreparation;
	}

	private void releaseAgents(MissionInfo missionInfo) throws InterruptedException {
		List<String> agentsSerialNumbers = missionInfo.getSerialAgentsNumbers();
		sendEvent(new ReleaseAgentsEvent(new ReleaseAgentsEventArgs(agentsSerialNumbers)));
	}

	private void sendAgents(MissionInfo missionInfo) throws InterruptedException {
		List<String> agentsSerialNumbers = missionInfo.getSerialAgentsNumbers();
		int duration = missionInfo.getDuration();
		String missionName = missionInfo.getMissionName();
		sendEvent(new SendAgentsEvent(new SendAgentsEventArgs(agentsSerialNumbers, duration, missionName)));
	}

	private void reportMission(MissionInfo missionInfo, MissionPreparation missionPreparation) {
		Report missionReport = new Report();
		missionReport.setMissionName(missionInfo.getMissionName());
		missionReport.setM(serialNumber);
		missionReport.setMoneypenny(missionPreparation.moneypenny());
		missionReport.setAgentsSerialNumbersNumber(missionInfo.getSerialAgentsNumbers());
		missionReport.setAgentsNames(missionPreparation.agentNames());
		missionReport.setGadgetName(missionInfo.getGadget());
		missionReport.setTimeIssued(missionInfo.getTimeIssued());
		missionReport.setQTime(missionPreparation.qTime());
		missionReport.setTimeCreated(lastTick);

		diary.addReport(missionReport);
	}

	private class AgentsNeedProvider extends MissionPreparationNeedProvider<AgentsAvailableResult> {
		protected AgentsNeedProvider(MissionInfo missionInfo, MissionPreparation missionPreparation) {
			super(missionInfo, missionPreparation);
		}

		@Override
		protected Future<AgentsAvailableResult> seekNeedInformation() throws InterruptedException {
			List<String> agentsSerialNumbers = missionInfo.getSerialAgentsNumbers();
			return sendEvent(new AgentsAvailableEvent(new AgentsAvailableEventArgs(agentsSerialNumbers)));
		}

		@Override
		protected void setInfo(AgentsAvailableResult result) {
			missionPreparation.setAgentsAvailableResult(result);
		}

		@Override
		protected boolean hasBeenFulfilled() {
			boolean valid = missionPreparation.areSerialAgentsNumbersValid();
			if (valid) {
				missionPreparation.setShouldReleaseAgents(true);
			}

			return valid;
		}

		@Override
		public String getName() {
			return "AgentsRequired";
		}
	}

	private class GadgetNeedProvider extends MissionPreparationNeedProvider<GadgetAvailableResult> {
		protected GadgetNeedProvider(MissionInfo missionInfo, MissionPreparation missionPreparation) {
			super(missionInfo, missionPreparation);
		}

		@Override
		protected Future<GadgetAvailableResult> seekNeedInformation() throws InterruptedException {
			String gadget = missionInfo.getGadget();
			return sendEvent(new GadgetAvailableEvent(new GadgetAvailableEventArgs(gadget)));
		}

		@Override
		protected void setInfo(GadgetAvailableResult result) {
			missionPreparation.setGadgetAvailableResult(result);
		}

		@Override
		protected boolean hasBeenFulfilled() {
			return missionPreparation.isGadgetAvailable();
		}

		@Override
		public String getName() {
			return "GadgetRequired";
		}
	}

	private abstract static class MissionPreparationNeedProvider<T> {
		/**
		 * Provides data for sending events
		 */
		protected final MissionInfo missionInfo;

		/**
		 * The mission preparation instance this provider fills with information
		 */
		protected final MissionPreparation missionPreparation;

		/**
		 * Initialize a new instance with the given mission information and preparation instances
		 * @param missionInfo The mission information
		 * @param missionPreparation The mission preparation
		 */
		protected MissionPreparationNeedProvider(MissionInfo missionInfo, MissionPreparation missionPreparation) {
			this.missionInfo = missionInfo;
			this.missionPreparation = missionPreparation;
		}

		/**
		 * Tries to send events to check a prerequisite of the mission
		 * @return Whether the need has been fulfilled and M can continue fulfilling the rest of the prerequisites
		 */
		public boolean tryFulfillNeed() throws InterruptedException {
			T result = sendNeedFulfillRequest();
			if (result == null) {
				missionPreparation.setStatus(ActionStatus.Terminate);
				return false;
			}
			setInfo(result);
			return hasBeenFulfilled();
		}

		/**
		 * @return A name for the need (used for debugging)
		 */
		public abstract String getName();

		/**
		 * Send event to get the information from other objects
		 * @return The future for this information
		 */
		protected abstract Future<T> seekNeedInformation() throws InterruptedException;

		/**
		 * Update the mission preparation object with newly received information
		 * @param result The information
		 */
		protected abstract void setInfo(T result);

		/**
		 * @return Whether the prerequisite of this instance has been fulfilled
		 */
		protected abstract boolean hasBeenFulfilled();

		private T sendNeedFulfillRequest() throws InterruptedException {
			T result = null;
			for (int i = 0; i < MAX_MISSION_NEED_TRIES; ++i) {
				Future<T> future = seekNeedInformation();
				if (future != null) {
					result = future.get();
					break;
				}
			}

			return result;
		}
	}

	private enum ActionStatus {
		Abort,
		Execute,
		Terminate
	}

	private static class MissionPreparation {
		private AgentsAvailableResult agentsAvailableResult;
		private GadgetAvailableResult gadgetAvailableResult;
		private boolean shouldReleaseAgents;
		private ActionStatus status;

		/**
		 * Initializes a new mission preparation instance
		 */
		public MissionPreparation() {
			this.agentsAvailableResult = null;
			this.gadgetAvailableResult = null;
			shouldReleaseAgents = false;
			status = ActionStatus.Abort;
		}

		/**
		 * Sets the result of the agents availability request
		 * @param agentsAvailableResult The result
		 */
		public void setAgentsAvailableResult(AgentsAvailableResult agentsAvailableResult) {
			this.agentsAvailableResult = agentsAvailableResult;
		}

		/**
		 * Sets the result of the gadget availability request
		 * @param gadgetAvailableResult The result
		 */
		public void setGadgetAvailableResult(GadgetAvailableResult gadgetAvailableResult) {
			this.gadgetAvailableResult = gadgetAvailableResult;
		}

		/**
		 * @return Whether acquiring the agents succeeded
		 */
		public boolean areSerialAgentsNumbersValid() {
			return agentsAvailableResult.areSerialAgentsNumbersValid();
		}

		/**
		 * @return The name of the agents that are needed for the mission
		 */
		public List<String> agentNames() {
			return agentsAvailableResult.getAgentNames();
		}

		/**
		 * @return The serial number of the Moneypenny instance who handled the agents availability request
		 */
		public int moneypenny() {
			return agentsAvailableResult.moneyPennySerialNumber();
		}

		/**
		 * @return Whether the requested gadget is available
		 */
		public boolean isGadgetAvailable() {
			return gadgetAvailableResult.isAvailable();
		}

		/**
		 * @return The time tick in which the Q instance who handled the gadget availability request received it
		 */
		public int qTime() {
			return gadgetAvailableResult.receiveTime();
		}

		/**
		 * @return Whether the agents should be released
		 */
		public boolean shouldReleaseAgents() {
			return shouldReleaseAgents;
		}

		/**
		 * Sets whether the agents should be released
		 * @param shouldReleaseAgents The value to set to
		 */
		public void setShouldReleaseAgents(boolean shouldReleaseAgents) {
			this.shouldReleaseAgents = shouldReleaseAgents;
		}

		/**
		 * @return The status of the mission
		 */
		public ActionStatus getStatus() {
			return status;
		}

		/**
		 * Sets the status of the mission
		 * @param status The value to set to
		 */
		public void setStatus(ActionStatus status) {
			this.status = status;
		}
	}
}
