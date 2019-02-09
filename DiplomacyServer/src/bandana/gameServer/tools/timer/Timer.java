package bandana.gameServer.tools.timer;

import java.util.ArrayList;
import java.util.List;

/**
 * A Timer thread that generates 'timer ticks' at given intervals.
 * <p>
 * Generating a 'tick' simply means that it calls a specified method on every object in the timerTickHandlers list. These objects can then use this to schedule tasks at regular intervals.
 *
 * @author Dave de Jonge, Western Sydney University
 */
public class Timer extends Thread {


	//STATIC FIELDS

	//STATIC METHODS

	//FIELDS
	/**
	 * The time in milliseconds between two consecutive 'ticks'.
	 */
	private int interval;

	/**
	 * List of objects that will be notified whenever a 'tick' occurs. These can be objects of any class, as long as it implements the TimerTickHandler interface.
	 */
	private List<TimerTickHandler> timerTickHandlers = new ArrayList<>();


	private boolean ticking = false;

	//CONSTRUCTORS
	public Timer(int interval) {
		this.interval = interval;
	}

	//METHODS
	@Override
	public void run() {

		ticking = true;

		while (ticking) {
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
			}

			for (TimerTickHandler timerTickHandler : timerTickHandlers) {
				timerTickHandler.handleTimerTick();
			}

		}
	}

	public void cancel() {
		this.ticking = false;
	}

	//GETTERS AND SETTERS
	public List<TimerTickHandler> getTimerTickHandlers() {
		return this.timerTickHandlers;
	}
}
