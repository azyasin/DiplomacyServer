package bandana.gameServer.tools.timer;

/**
 * This class is just for testing purposes.
 *
 * @author Dave de Jonge, Western Sydney University
 */
public class TimerTester implements TimerTickHandler {

	//STATIC FIELDS

	//STATIC METHODS
	public static void main(String[] args) {

		TimerTester tester1 = new TimerTester(1);
		TimerTester tester2 = new TimerTester(2);

		Timer timer = new Timer(1000);
		timer.getTimerTickHandlers().add(tester1);
		timer.getTimerTickHandlers().add(tester2);

		timer.start();

		while (true) {

		}
	}

	//FIELDS
	int id;

	//CONSTRUCTORS
	TimerTester(int id) {
		this.id = id;
	}

	//METHODS
	@Override
	public void handleTimerTick() {
		System.out.println("Tick! id: " + id);
	}

	//GETTERS AND SETTERS
}
