package bgu.spl.mics;

import bgu.spl.mics.application.passiveObjects.Agent;
import bgu.spl.mics.application.passiveObjects.Squad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SquadTest {
    private Squad squad;
    private Agent[] agents;
    List<String> agentSerialNums;

    @BeforeEach
    /*
    Loads some agents to work with in every test
     */
    public void setUp() {
        squad = Squad.getInstance();
        agents = new Agent[] {
                new Agent(),
                new Agent(),
                new Agent()
        };
        agents[0].setName("Tracer");
        agents[1].setName("Soldier 76");
        agents[2].setName("Winston");

        for (int i = 0; i < agents.length; i++) {
            agents[i].setSerialNumber("00" + i);
        }
        squad.load(agents);

        agentSerialNums = null;
    }

    @Test
    /*
    Tests that the squads load correctly and that it returns the names of the requested agents (and only of them)
     */
    public void testLoadAndGetNames() {
        agents[0].setName("Jessie");
        agents[1].setName("James");
        agents[2].setName("Meow, that's right!");

        List<String> agentSerialNums;
        List<String> agentNames;

        agentSerialNums = new ArrayList<>();
        for (int i = 0; i < agents.length; i++) {
            agentSerialNums.add(agents[i].getSerialNumber());
        }

        agentNames = squad.getAgentsNames(agentSerialNums);
        for (int i = 0; i < agents.length; i++) {
            assertEquals(agents[i].getName(), agentNames.get(i), "Different name");
        }

        agentSerialNums = new ArrayList<String>() {{
           add("000");
           add("002");
        }};
        agentNames = squad.getAgentsNames(agentSerialNums);
        assertEquals(agents[0].getName(), agentNames.get(0), "Different name");
        assertEquals(agents[2].getName(), agentNames.get(1), "Different name");
    }

    @Test
    /*
    Tests that getting agents which do no exists result in returning false (as defined)
     */
    public void testGetAgents_invalidSerialNumber() throws InterruptedException {
        agentSerialNums = new ArrayList<String>() {{
            add("007");
        }};

        assertFalse(squad.getAgents(agentSerialNums), "Agent does not exists but it went smooth");
    }

    @Test
    /*
    Tests that the availability of every (existing) agent passed to @see getAgents does not change
    if an invalid agent was passed to it (even if some were unavailable)
     */
    public void testGetAgents_availableDoesntChangeOnInvalidSerialNumber() throws InterruptedException {
        agents[0].acquire();
        agentSerialNums = new ArrayList<String>() {{
            add("001");
            add("007");
            add("000");
        }};

        assertFalse(squad.getAgents(agentSerialNums), "Agent does not exists but it went smooth");
        assertFalse(agents[0].isAvailable(), "Agent should be available");
        assertTrue(agents[1].isAvailable(), "Agent should be available");
    }

    @Test
    /*
    Tests that the getAgents exits correctly and that the availability of the agents
     passed it stays unchanged when every agent passed to is available beforehand
     */
    public void testGetAgents_allRelased() throws InterruptedException {
        agentSerialNums = new ArrayList<String>() {{
            add("000");
            add("001");
        }};

        assertTrue(squad.getAgents(agentSerialNums), "All agents should exist in the squad");
        assertTrue(agents[0].isAvailable(), "Agent should be available");
        assertTrue(agents[1].isAvailable(), "Agent should be available");
    }

    @Test
    /*
    Tests that getAgents waits for the agents to become available if they're not beforehand
     */
    public void testGetAgents_waitsForAvailability() throws InterruptedException {
        agentSerialNums = new ArrayList<String>() {{
            add("000");
            add("001");
            add("002");
        }};

        agents[1].acquire();
        Thread releaser = new Thread(() -> {
            try {
                Thread.sleep(50);
                agents[1].release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        releaser.start();

        long start = System.currentTimeMillis();
        boolean result = squad.getAgents(agentSerialNums);
        long end = System.currentTimeMillis();
        long duration = end - start;

        assertTrue(result, "All agents should exist in the squad");
        // error margin of 2ms
        assertTrue(50 <= duration && duration <= 52, "Didn't wait or waited too much");

        try {
            releaser.interrupt();
            releaser.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    /*
    Tests that releaseAgents release all and only the agents it gets passed as arguments
     */
    public void testReleaseAgents() throws InterruptedException {
        agentSerialNums = new ArrayList<String>() {{
            add("002");
            add("001");
        }};

        agents[0].acquire();
        agents[1].acquire();
        squad.releaseAgents(agentSerialNums);

        assertTrue(agents[2].isAvailable(), "Agent should be available");
        assertTrue(agents[1].isAvailable(), "Agent should be available");
        assertFalse(agents[0].isAvailable(), "Agent should be unavailable");
    }

    @Test
    /*
    Tests that sendAgents releases all and only the agents it gets passed as arguments
    and that it blocks for exactly the amount of time specified in the time argument.
     */
    public void testSendAgents_allReleased() throws InterruptedException {
        agentSerialNums = new ArrayList<String>() {{
            add("000");
            add("002");
        }};

        agents[0].acquire();
        agents[1].acquire();

        long start = System.currentTimeMillis();
        squad.sendAgents(agentSerialNums, 50);
        long end = System.currentTimeMillis();
        long duration = end - start;

        assertTrue(agents[0].isAvailable(), "Agent should be available because it was sent");
        assertFalse(agents[1].isAvailable(), "Agent should unavailable as it was not sent");
        assertTrue(agents[2].isAvailable(), "Agent sgould be available because it was sent");

        // error margin of 2ms
        assertTrue(50 <= duration && duration <= 52, "Didn't sleep or slept too much");
    }
}
