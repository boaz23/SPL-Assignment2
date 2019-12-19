package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Future;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.messages.eventsInfo.*;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.MissionInfo;
import bgu.spl.mics.application.passiveObjects.Report;

import java.util.List;

/**
 * M handles ReadyEvent - fills a report and sends agents to mission.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class M extends Subscriber {
	private final int serialNumber;
	private int lastTick;
	private final Diary diary;

	public M(int serialNumber) {
		super("M" + serialNumber);
		this.serialNumber = serialNumber;
		diary = Diary.getInstance();
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, this::onTimeTick);
		subscribeEvent(MissionReceivedEvent.class, this::onMissionReceived);
	}

	// TODO: the last tick we got may not be updated when we're handling events
	private void onTimeTick(TickBroadcast b) {
		lastTick = b.getTick();
	}

	private void onMissionReceived(MissionReceivedEvent e) {
		MissionInfo missionInfo = e.getMissionInfo();
		diary.incrementTotal();

		MissionPreparation missionPreparation = checkValidity(missionInfo);
		if (missionPreparation.shouldExecute()) {
			sendAgents(missionInfo.getSerialAgentsNumbers(), missionInfo.getDuration());
			reportMission(missionInfo, missionPreparation);
		} else {
			if (missionPreparation.shouldReleaseAgents()) {
				releaseAgents(missionInfo.getSerialAgentsNumbers());
			}
		}
	}

	private MissionPreparation checkValidity(MissionInfo missionInfo) {
		MissionPreparation missionPreparation = new MissionPreparation();

		List<String> agentsSerialNumbers = missionInfo.getSerialAgentsNumbers();
		AgentsAvailableResult agentsAvailableResult = areAgentsValid(agentsSerialNumbers);
		missionPreparation.setAgentsAvailableResult(agentsAvailableResult);
		if (!agentsAvailableResult.areSerialAgentsNumbersValid()) {
			// TODO: what are we supposed to do?
			// Abort the mission because it's invalid?
			return missionPreparation;
		}

		missionPreparation.setShouldReleaseAgents(true);
		GadgetAvailableResult gadgetAvailableResult = isGadgetAvailable(missionInfo.getGadget());
		missionPreparation.setGadgetAvailableResult(gadgetAvailableResult);
		if (!gadgetAvailableResult.isAvailable()) {
			// TODO: what are we supposed to do?
			// Abort the mission because it's invalid?
			return missionPreparation;
		}

		if (lastTick > missionInfo.getTimeExpired()) {
			return missionPreparation;
		}

		missionPreparation.setShouldReleaseAgents(false);
		missionPreparation.setShouldExecute(true);
		return missionPreparation;
	}

	private AgentsAvailableResult areAgentsValid(List<String> agentsSerialNumbers) {
		return sendAgentsAvailableEvent(agentsSerialNumbers).get();
	}

	private Future<AgentsAvailableResult> sendAgentsAvailableEvent(List<String> agentsSerialNumbers) {
		return sendEvent(new AgentsAvailableEvent(new AgentsAvailableEventArgs(agentsSerialNumbers)));
	}

	private GadgetAvailableResult isGadgetAvailable(String gadget) {
		return sendGadgetAvailableEvent(gadget).get();
	}

	private Future<GadgetAvailableResult> sendGadgetAvailableEvent(String gadget) {
		return sendEvent(new GadgetAvailableEvent(new GadgetAvailableEventArgs(gadget)));
	}

	private void releaseAgents(List<String> agentsSerialNumbers) {
		sendEvent(new ReleaseAgentsEvent(new ReleaseAgentsEventArgs(agentsSerialNumbers)));
	}

	private void sendAgents(List<String> agentsSerialNumbers, int duration) {
		sendEvent(new SendAgentsEvent(new SendAgentsEventArgs(agentsSerialNumbers, duration)));
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

	private static class MissionPreparation {
		private AgentsAvailableResult agentsAvailableResult;
		private GadgetAvailableResult gadgetAvailableResult;
		private boolean shouldReleaseAgents;
		private boolean shouldExecute;

		/**
		 * Initializes a new mission preparation instance
		 */
		public MissionPreparation() {
			this.agentsAvailableResult = null;
			this.gadgetAvailableResult = null;
			shouldReleaseAgents = false;
			shouldExecute = false;
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
		 * @return The serial number of the Q instance who handled the gadget availability request
		 */
		public int q() {
			return gadgetAvailableResult.qSerialNumber();
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
		 * @return Whether the mission should be executed
		 */
		public boolean shouldExecute() {
			return shouldExecute;
		}

		/**
		 * Sets whether the mission should be executed
		 * @param shouldExecute The value to set to
		 */
		public void setShouldExecute(boolean shouldExecute) {
			this.shouldExecute = shouldExecute;
		}
	}
}
