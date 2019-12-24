package bgu.spl.mics;

import bgu.spl.mics.mocks.MockBroadcast;
import bgu.spl.mics.mocks.MockEvent;
import bgu.spl.mics.mocks.MockMessageBroker;
import bgu.spl.mics.mocks.MockSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MessageBrokerTest {
    private MessageBroker messageBroker;

    @BeforeEach
    public void setUp(){
        messageBroker = new MockMessageBroker();
    }

    /**
     * Tests registering, completion and some of the very basic functionality of
     * subscribing to event, sending an event and awaiting for a message.
     */
    @Test
    public void testReceiveAndCompleteEvent() {
        Subscriber subscriber = new MockSubscriber();
        Event<Integer> event = new MockEvent();

        messageBroker.register(subscriber);
        messageBroker.subscribeEvent(MockEvent.class, subscriber);
        Future<Integer> future = messageBroker.sendEvent(event);

        assertNotNull(future, "No subscriber to handle the message");
        // start a thread to interrupted this one in case it gets blocked for too long
        Thread interrupter = startInterrupter(5);

        try {
            Message msg = messageBroker.awaitMessage(subscriber);
            assertSame(msg, event, "Got a different event.");
            messageBroker.complete(event, 9);
            assertEquals(9, future.get());
        } catch (InterruptedException e) {
            fail("Waited too long");
        }

        TestUtils.closeThread(interrupter);
    }

    /**
     * Tests the unregistering works. e.g. that the subscriber stops receiving events
     */
    @Test
    public void testUnregister() {
        Subscriber subscriber = new MockSubscriber();
        Event<Integer> event = new MockEvent();

        messageBroker.register(subscriber);
        messageBroker.subscribeEvent(MockEvent.class, subscriber);
        messageBroker.unregister(subscriber);
        Future<Integer> future = messageBroker.sendEvent(event);

        assertNull(future, "A subscriber was assigned to handle the message");
    }

    /**
     * Tests that sendBroadcast send a broadcast to every subscriber
     * that is subscribed to that broadcast type (and only them)
     */
    @Test
    public void testSendBroadcast() {
        Subscriber s1 = new MockSubscriber("s1");
        Subscriber s2 = new MockSubscriber("s2");
        Subscriber s3 = new MockSubscriber("s3");
        Broadcast broadcast = new MockBroadcast();

        testSendBroadcast_registerAndSend(s1, s2, s3, broadcast);
        testSendBroadcast_assertReceived(s1, s2, broadcast);
        testSendBroadcast_assertWaitingTilInterrupt(s3);
    }


    /**
     * Tests that awaitMessage blocks when there are no messages in queue
     */
    @Test
    public void testAwaitMessageBlocking() {
        Subscriber subscriber = new MockSubscriber();

        messageBroker.register(subscriber);
        messageBroker.subscribeEvent(MockEvent.class, subscriber);

        // start a thread to interrupted this one in case it gets blocked for too long
        Thread interrupter = startInterrupter(20);
        try {
            messageBroker.awaitMessage(subscriber);
            fail("The subscriber got a message");
        } catch (InterruptedException ignored) {

        }

        TestUtils.closeThread(interrupter);
    }

    private void testSendBroadcast_assertWaitingTilInterrupt(Subscriber s3) {
        // start a thread to interrupted this one in case it gets blocked for too long
        Thread interrupter = startInterrupter(5);
        try {
            messageBroker.awaitMessage(s3);
            fail("Subscriber 3 got a message even though he is not subscribed to it");
        } catch (InterruptedException ignored) {
        }

        TestUtils.closeThread(interrupter);
    }

    private void testSendBroadcast_assertReceived(Subscriber s1, Subscriber s2, Broadcast broadcast) {
        Message receivedBroadcast;
        try {
            receivedBroadcast = messageBroker.awaitMessage(s2);
            assertSame(receivedBroadcast, broadcast, "Subscriber 2 didn't get the broadcast");
            receivedBroadcast = messageBroker.awaitMessage(s1);
            assertSame(receivedBroadcast, broadcast, "Subscriber 1 didn't get the broadcast");
        } catch (InterruptedException e) {
            fail("Should not get here, no one should interrupt");
        }
    }

    private void testSendBroadcast_registerAndSend(Subscriber s1, Subscriber s2,
                                                   Subscriber s3, Broadcast broadcast) {
        messageBroker.register(s1);
        messageBroker.register(s2);
        messageBroker.register(s3);
        messageBroker.subscribeBroadcast(MockBroadcast.class, s1);
        messageBroker.subscribeBroadcast(MockBroadcast.class, s2);
        messageBroker.sendBroadcast(broadcast);
    }

    private Thread startInterrupter(int sleepTime) {
        return startInterrupter(sleepTime, Thread.currentThread());
    }

    private Thread startInterrupter(final int sleepTime, final Thread testThread) {
        Thread interrupter = new Thread(() -> {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ignored) {
            }
            testThread.interrupt();
        });
        interrupter.start();
        return interrupter;
    }
}
