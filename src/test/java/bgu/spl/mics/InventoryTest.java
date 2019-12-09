package bgu.spl.mics;

import bgu.spl.mics.application.passiveObjects.Inventory;
import com.google.gson.Gson;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;

public class InventoryTest {

    private Inventory inventory;

    @BeforeEach
    public void setUp(){
        inventory = Inventory.getInstance();
    }

    /**
     * Since in order to check load() we need getItem() and in order the check
     * getItem() we need load(), we are testing them together.
     * Test 2 options in getItem, not exist item in the inventory and existing one.
     * To check the load(), we test for each gadget if its exist with getItem()
     */
    @Test
    public void loadAndGetItem() {

        String[] gadgets = {"Car", "Phone"};
        inventory.load(gadgets);

        assertFalse(inventory.getItem("Car2"),"Inventory getItem(), Car2 is not a gadget in inventory class");
        assertFalse(inventory.getItem("PhOnE"),"Inventory getItem(), PhOnE is not a gadget in inventory class");

        assertFalse(inventory.getItem("Phone"),"Inventory getItem(), Phone is a gadget in inventory class");
        assertFalse(inventory.getItem("Car"),"Inventory getItem(), Car is a gadget in inventory class");

    }
}
