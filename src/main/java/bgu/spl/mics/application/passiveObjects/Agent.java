package bgu.spl.mics.application.passiveObjects;

import bgu.spl.mics.loggers.Loggers;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Passive data-object representing a information about an agent in MI6.
 * You must not alter any of the given public methods of this class. 
 * <p>
 * You may add ONLY private fields and methods to this class.
 */
public class Agent {

	private String serialNumber;
	private String name;

	private boolean available;

	public Agent(){
		available = true;
	}
	/**
	 * Sets the serial number of an agent.
	 */
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	/**
     * Retrieves the serial number of an agent.
     * <p>
     * @return The serial number of an agent.
     */
	public String getSerialNumber() {
		return  serialNumber;
	}

	/**
	 * Sets the name of the agent.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
     * Retrieves the name of the agent.
     * <p>
     * @return the name of the agent.
     */
	public String getName() {
		return name;
	}

	/**
     * Retrieves if the agent is available.
     * <p>
     * @return if the agent is available.
     */
	public boolean isAvailable() {
		return available;
	}

	/**
	 * Acquires an agent.
	 */
	public synchronized void acquire() throws InterruptedException {
		try {
			while (!available) {
				wait();
			}
			available = false;
		} catch (InterruptedException e) {
			Loggers.MnMPLogger.appendLine(Thread.currentThread().getName() + " interrupted while waiting to acquire agent " + serialNumber);
			throw e;
		}
	}

	/**
	 * Releases an agent.
	 */
	public synchronized void release(){
		available = true;
		notifyAll();
	}
}
