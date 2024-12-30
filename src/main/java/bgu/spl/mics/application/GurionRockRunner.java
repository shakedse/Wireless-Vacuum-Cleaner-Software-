package bgu.spl.mics.application;

import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.*;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import bgu.spl.mics.application.messages.*;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */

    public class CameraData {
       private String CameraID;
       private LinkedList<StampedDetectedObjects> DetectedObjects;

       public CameraData(String CameraID, LinkedList<StampedDetectedObjects> DetectedObjects)
       {
           this.CameraID = CameraID;
           this.DetectedObjects = DetectedObjects;
       }
    }
       

    public static void main(String[] args) 
    {        
        // TODO: Parse configuration file.

        Gson gson = new Gson();
        try (FileReader reader = new FileReader("C:\\Users\\einav\\.vscode\\Assignment2\\example_input_2\\camera_data.json")) 
        {
            // Convert JSON File to Java Object
            Type camerasDataBaseType = new TypeToken<LinkedList<CameraData>>(){}.getType();
            LinkedList<CameraData> DataBases = gson.fromJson(reader, camerasDataBaseType);
            DataBases.toString();
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    /*
        String configFilePath = "config.json";  // Example file path
        try (FileReader reader = new FileReader(configFilePath)) {
            // Create a Gson object
            
            Type cameraListType = new TypeToken<List<Camera>>(){}.getType();
            List<Camera> cameras = gson.fromJson(reader, cameraListType);

            Type LiDarListType = new TypeToken<List<LiDarWorkerTracker>>(){}.getType();
            List<Camera> LiDarWorkers = gson.fromJson(reader, cameraListType);

            
        }
        catch (IOException e) {
            e.printStackTrace();
        }*/
        // TODO: Initialize system components and services.
        // TODO: Start the simulation.
    }
}
