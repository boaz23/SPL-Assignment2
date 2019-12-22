package bgu.spl.mics.application.publishers;

import bgu.spl.mics.Publisher;
import bgu.spl.mics.application.messages.LastTickBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * TimeService is the global system timer There is only one instance of this Publisher.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other subscribers about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends Publisher {
	private static final int TICK_MS = 100;
	private int duration;
	private int tick;
	private final Object notify;

	public TimeService(int duration, String name) {
		super(name);
		this.duration = duration;
		notify = new Object();
	}

	@Override
	protected void initialize() {
		// This method will never be called...
	}

	@Override
	public void run() {
		tick = 1;
		Timer timer = new Timer(getName() + "-Timer");
		TimerTask timerTask = new TimeTickSchedule();
		timer.schedule(timerTask, TICK_MS, TICK_MS);

		//Wait for the timer to complete its tasks
		try {
			synchronized (notify) {
				notify.wait();
				timer.cancel();
				timer.purge();
			}
		} catch (InterruptedException ignored) {}
	}

	/**
	 * Return the time tick duration
	 * @return time tick duration
	 */
	public static int getTimeTickDuration(){
		return TICK_MS;
	}

	private class TimeTickSchedule extends TimerTask{

		@Override
		public void run() {
			if(tick != duration){
				TickBroadcast tickBroadcast = new TickBroadcast(tick);
				sendBroadcast(tickBroadcast);
				tick = tick +1;

			} else {
				LastTickBroadcast lastTickBroadcast = new LastTickBroadcast(tick);
				sendBroadcast(lastTickBroadcast);
				synchronized (notify){
					notify.notifyAll();
				}
			}
		}
	}

}
