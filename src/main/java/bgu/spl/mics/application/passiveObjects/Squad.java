package bgu.spl.mics.application.passiveObjects;
import bgu.spl.mics.Utils;
import bgu.spl.mics.application.publishers.TimeService;
import bgu.spl.mics.loggers.Loggers;

import java.util.*;


/**
 * Passive data-object representing a information about an agent in MI6.
 * You must not alter any of the given public methods of this class. 
 * <p>
 * You may add ONLY private fields and methods to this class.
 */
public class Squad {

	private Map<String, Agent> agents;

	// NOTE: should be private, but it's said not to change signatures of public methods
	public Squad() {
		this.agents = new HashMap<>();
	}

	/**
	 * Retrieves the single instance of this class.
	 */
	public static Squad getInstance() {
		return Squad.InstanceHolder.instance;
	}

	/**
	 * Initializes the squad. This method adds all the agents to the squad.
	 * <p>
	 * @param agents 	Data structure containing all data necessary for initialization
	 * 						of the squad.
	 */
	public void load (Agent[] agents) {
		for(Agent agent: agents){
			this.agents.put(agent.getSerialNumber(), agent);
		}
	}

	/**
	 * Releases agents.
	 * Sort it, and then release from end to start
	 */
	public void releaseAgents(List<String> serials){
		serials.sort(String.CASE_INSENSITIVE_ORDER);
		for(int i=serials.size()-1; i > -1; i=i-1){
			Agent agent = agents.getOrDefault(serials.get(i), null);
			if(agent != null){
				agent.release();
			}
		}
	}

	/**
	 * simulates executing a mission by calling sleep.
	 * @param time   milliseconds to sleep
	 */
	public void sendAgents(List<String> serials, int time) throws InterruptedException {
		int timeTickDuration = TimeService.getTimeTickDuration();
		Thread.sleep(time*timeTickDuration);
		releaseAgents(serials);
	}

	/**
	 * acquires an agent, i.e. holds the agent until the caller is done with it
	 * @param serials   the serial numbers of the agents
	 * @return ‘false’ if an agent of serialNumber ‘serial’ is missing, and ‘true’ otherwise
	 */
	public boolean getAgents(List<String> serials) throws InterruptedException {
		boolean allExist = checkAllExist(serials);
		if(allExist) {
			serials.sort(String.CASE_INSENSITIVE_ORDER);
			Loggers.MnMPLogger.appendLine(Thread.currentThread().getName() + " getting agents " + Utils.listToString(serials));
			for (String serial : serials) {
				Loggers.MnMPLogger.appendLine(Thread.currentThread().getName() + " trying to acquire " + serial);
				Agent agent = agents.get(serial);
				agent.acquire();
				Loggers.MnMPLogger.appendLine(Thread.currentThread().getName() + " acquired " + serial);
			}
		}
		else {
			Loggers.MnMPLogger.appendLine(Thread.currentThread().getName() + " got agents that do not exist " + Utils.listToString(serials));
		}

		return allExist;
	}

    /**
     * gets the agents names
     * @param serials the serial numbers of the agents
     * @return a list of the names of the agents with the specified serials.
     */
    public List<String> getAgentsNames(List<String> serials){
		List<String> names = new LinkedList<>();
		for(String serial: serials){
			names.add(agents.get(serial).getName());
		}

		return names;
    }

	/**
	 * @return The serial numbers of all agents
	 */
	public Map<String, Agent> getAgentsMap() {
    	return agents;
	}

	private static class InstanceHolder {
		public static final Squad instance = new Squad();
	}

	/**
	 * Check if all agents are exist according to the serials
	 * @param serials list of serials for the agents
	 * @return true if all serials are exist
	 */
	private boolean checkAllExist(List<String> serials){
		boolean allExist = true;

		for(String serial: serials){
			Agent agent = agents.getOrDefault(serial, null);
			if(agent == null){
				allExist = false;
				break;
			}
		}

		return allExist;
	}

}
