package bgu.spl.mics.application.objects;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import bgu.spl.mics.application.messages.DetectedObjectsEvent;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;

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
    private DetectedObjectsEvent lastFrame;

    public Camera(int id, int frequency, String camera_key) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.camera_key = camera_key;
    }
    public Camera(int id, int frequency, String camera_key,  LinkedList<StampedDetectedObjects> stampDetectedObjects) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.camera_key = camera_key;
        this.stampDetectedObjects = stampDetectedObjects;
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

    public String getStringId()
    {
        return camera_key;
    }

    public void statusDown() {
        this.status = STATUS.DOWN;
    }

    public LinkedList<StampedDetectedObjects> getList()
    {
        return stampDetectedObjects;
    }

    public void setLastFrame(DetectedObjectsEvent lastFrame)
    {
        StatisticalFolder.getInstance().addLastFrameCamera(camera_key, lastFrame);
        this.lastFrame = lastFrame;
    }

    public DetectedObjectsEvent gerLastFrame()
    {
        return lastFrame;
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
            for (int i = 0; i < detectedObjects.size(); i++) 
            {
                if (detectedObjects.get(i).getID().equals("ERROR")) 
                {
                    status = STATUS.ERROR;
                    LinkedList<DetectedObject> toReturn = new LinkedList<DetectedObject>();
                    toReturn.add(detectedObjects.get(i));
                    return new DetectedObjectsEvent(tick, toReturn, id);
                }
            }
            DetectedObjectsEvent event = new DetectedObjectsEvent(tick, detectedObjects, id);
            
            for (int i = 0; i < detectedObjects.size(); i++) 
            {
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

    // if the current tick is crashing the camera - change the status to error
    public DetectedObject checkForErrors(int time) {
        for(StampedDetectedObjects obj : stampDetectedObjects)
        {
            if(obj.getTime() == time)
            {
                for(DetectedObject detectedObject : obj.getList())
                {
                    if(detectedObject.getID().equals("ERROR"))
                    {
                        status = STATUS.ERROR;
                        return detectedObject;
                    }
                }
            }
        }
        return null;
    }
}
