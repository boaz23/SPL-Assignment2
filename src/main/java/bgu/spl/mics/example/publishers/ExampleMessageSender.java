package bgu.spl.mics.example.publishers;

import java.util.concurrent.TimeUnit;

import bgu.spl.mics.Future;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;

public class ExampleMessageSender extends Subscriber {

    private boolean broadcast;

    public ExampleMessageSender(String name, String[] args) {
        super(name);

        if (args.length != 1 || !args[0].matches("broadcast|event")) {
            throw new IllegalArgumentException("expecting a single argument: broadcast/event");
        }

        this.broadcast = args[0].equals("broadcast");
    }

    @Override
    protected void initialize() {
        System.out.println("Sender " + getName() + " started");
        if (broadcast) {
            try {
                getSimplePublisher().sendBroadcast(new ExampleBroadcast(getName()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Sender " + getName() + " publish an event and terminate");
            terminate();
        } else {
            Future<String> futureObject = null;
            try {
                futureObject = getSimplePublisher().sendEvent(new ExampleEvent(getName()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (futureObject != null) {
                String resolved = null;
                try {
                    resolved = futureObject.get(100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (resolved != null) {
            		System.out.println("Completed processing the event, its result is \"" + resolved + "\" - success");
            	}
            	else {
                	System.out.println("Time has elapsed, no subscriber has resolved the event - terminating");
                }
            }
            else {
            	System.out.println("No Subscriber has registered to handle ExampleEvent events! The event cannot be processed");
            }
            terminate();
        }
    }

}
