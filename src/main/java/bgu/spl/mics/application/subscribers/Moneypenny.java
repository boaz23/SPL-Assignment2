package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.AgentsAvailableEvent;;
import bgu.spl.mics.application.passiveObjects.Squad;

import java.util.List;

/**
 * Only this type of Subscriber can access the squad.
 * Three are several Moneypenny-instances - each of them holds a unique serial number that will later be printed on the report.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Moneypenny extends Subscriber {
	// TODO: have moneypenny available to send agents at all times to execute missions. see https://www.cs.bgu.ac.il/~spl201/index.php?page=Assignments.Assignment_2Forum&action=show-thread&id=80cb06ba55a76335ff907450a401c197

	private Squad squad;
	//TODO check the serial
	// TODO: add serial number as field
	public Moneypenny(int id) {
		super(""+id);
		squad = Squad.getInstance();
	}

	@Override
	protected void initialize() {
		// TODO: handle last tick
		subscribeEvent(AgentsAvailableEvent.class, this::callBack);
	}

	private void callBack(AgentsAvailableEvent aAE){
		// TODO: call getAgents once and then complete (since we will wait for eveyone to be acquired)
		List<String> agents = aAE.getArgs().agentsSerialNumbers();
		while(! squad.getAgents(agents)) {}
	}

}

