package bgu.spl.mics;

import bgu.spl.mics.application.passiveObjects.Inventory;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.Arrays;

public class InventoryTest {

    @BeforeEach
    public void setUp(){
    }

    /**
     * Test if the class creates 1 instance.
     */
    @Test
    public void singleInstance(){
        Thread[] tInstances = new Thread[5];
        Inventory[] inventories = new Inventory[5];

        // Create the threads
        for(int i=0; i < 5; i=i+1 ){
            final int index = i;
            tInstances[i] = new Thread(() ->
                    inventories[index] = Inventory.getInstance());
        }

        // Run the threads
        for(int i=0; i < 5; i=i+1 ){
            tInstances[i].start();
        }

        // wait for them to finish
        for(int i=0; i < 5; i=i+1 ){
            try {
                tInstances[i].join();
            } catch (Exception e) {}
        }

        for(int i=0; i < 4; i=i+1 ){
            assertNotSame(inventories[i], inventories[i + 1], "singleInstance: The class Inventory is not thread safe");
        }


    }

    /**
     * Since the {@code Inventory.getItem} function is internal,
     * the only way to check load and printTofile, is to combine them to the
     * same test.
     */
    @Test
    public void loadAndPrintToFileJson(){
        Inventory inv = Inventory.getInstance();
        Gson gson = new Gson();

        String[] inventory = {"Car", "Phone"};
        Arrays.sort(inventory);

        String funcName = "loadAndPrintToFileJson ";
        String fileName = "src/test/TestFiles/inv.json";

        inv.load(inventory);
        inv.printToFile(fileName);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String[] json2;
            json2 = gson.fromJson(reader, String[].class);
            Arrays.sort(json2);

            if(inventory.length != json2.length){
                fail(funcName + ": json inventory items are not in the same length");
            }else {
                for (int i = 0; i < json2.length; i = i + 1) {
                    assertFalse(inventory[i].compareTo(json2[i]) != 0,
                            funcName + ": load and then print are not identical output");
                }
            }
            reader.close();
        }
        catch (Exception e) {
            fail(funcName + e.getMessage());
            System.out.println(e.getMessage());
        }
    }
}
