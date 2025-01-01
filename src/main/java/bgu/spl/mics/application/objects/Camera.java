package bgu.spl.mics.application.objects;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import bgu.spl.mics.application.messages.DetectedObjectsEvent;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    // TODO: Define fields and methods.
    // fields:
    private int id;
    private int frequency;
    private STATUS status;
    private LinkedList<StampedDetectedObjects> stampDetectedObjects = new LinkedList<>();
    private String camera_key;

    public Camera(int id, int frequency, String camera_key) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.camera_key = camera_key;

        /*
         * Gson gson = new Gson();
         * try (FileReader reader = new FileReader("./camera_data.json"))
         * {
         * // Convert JSON File to Java Object
         * Type camerasDataBaseType = new
         * TypeToken<LinkedList<LinkedList<StampedDetectedObjects>>>(){}.getType();
         * LinkedList<LinkedList<StampedDetectedObjects>> DataBases =
         * gson.fromJson(reader, camerasDataBaseType);
         * this.stampDetectedObjects = DataBases.get(id);
         * }
         * catch (IOException e) {
         * e.printStackTrace();
         * }
         */
    }

    public int getFrequency() {
        return frequency;
    }

    public int getID() {
        return id;
    }

    public STATUS getStatus() {
        return status;
    }

    public void statusDown() {
        this.status = STATUS.DOWN;
    }

    public LinkedList<StampedDetectedObjects> getList()
    {
        return stampDetectedObjects;
    }

    // returns the objects captured list at a certin time tick
    public LinkedList<DetectedObject> getDetectedObjectsAtTick(int tick) {
        LinkedList<DetectedObject> DetectedObjectsAtTick = new LinkedList<DetectedObject>();
        for (StampedDetectedObjects obj : stampDetectedObjects) {
            if (obj.getTime() == tick) {
                DetectedObjectsAtTick = obj.getList();
                break;
            }
        }
        return DetectedObjectsAtTick;
    }

    // returns the event that matches the certin tick
    public DetectedObjectsEvent activateTick(int tick) {
        LinkedList<DetectedObject> detectedObjects = this.getDetectedObjectsAtTick(tick);
        if (!detectedObjects.isEmpty()) {
            // checking if we got a detected objects event that is error
            for (int i = 0; i < detectedObjects.size(); i++) {
                if (detectedObjects.get(i).getID().equals("ERROR")) {
                    status = STATUS.ERROR;
                    LinkedList<DetectedObject> toReturn = new LinkedList<DetectedObject>();
                    toReturn.add(detectedObjects.get(i));
                    return new DetectedObjectsEvent(tick, toReturn, id);
                }
            }
            DetectedObjectsEvent event = new DetectedObjectsEvent(tick, detectedObjects, id);
            for (int i = 0; i < detectedObjects.size(); i++) {
                StatisticalFolder.getInstance().incrementNumDetectedObjects();
            }
            return event;
        }
        return null;
    }

    public void buildData(String dataPath) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(dataPath)) {
            // Parse the JSON as a map
            Type dataType = new TypeToken<Map<String, LinkedList<StampedDetectedObjects>>>() {
            }.getType();
            Map<String, LinkedList<StampedDetectedObjects>> data = gson.fromJson(reader, dataType);

            // Match the camera key and assign the corresponding data
            if (data.containsKey(camera_key)) {
                this.stampDetectedObjects = data.get(camera_key);
            } else {
                System.err.println("No data found for camera key: " + camera_key);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //מתי משתמשים?
    public class CameraDataType {
        LinkedList<StampedDetectedObjects> camerasDataBase;
        String camera_key;

        public LinkedList<StampedDetectedObjects> getCamerasDataBase() {
            return camerasDataBase;
        }

        public String getCameraKey() {
            return camera_key;
        }
    }

    /* 
    public static void main(String[] args) {
        
        Camera camera = new Camera(1, 1, "camera2");
        camera.buildData("C:\\Users\\einav\\.vscode\\Assignment2\\example_input_2\\camera_data.json");
        LinkedList<StampedDetectedObjects> stampDetectedObjects = camera.getList();
        for (StampedDetectedObjects obj : stampDetectedObjects) {
            System.out.println("Time: " + obj.getTime());
            for (DetectedObject d : obj.getList()) {
                System.out.println("ID: " + d.getID());
                System.out.println("des: " + d.getDescription());

            }
            System.out.println("");

        }

    }
        */


}
