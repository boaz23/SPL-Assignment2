package bgu.spl.mics.mocks;

import bgu.spl.mics.Subscriber;

public class MockSubscriber extends Subscriber {
    /**
     * Initializes a mock subscriber with a default name
     */
    public MockSubscriber() {
        this("Mock Subscriber");
    }

    /**
     * @param name the Subscriber name (used mainly for debugging purposes -
     *             does not have to be unique)
     */
    public MockSubscriber(String name) {
        super(name);
    }

    @Override
    protected void initialize() {

    }
}
