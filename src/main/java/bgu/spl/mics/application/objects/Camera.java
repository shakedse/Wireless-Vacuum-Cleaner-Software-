package bgu.spl.mics.application.objects;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.application.messages.DetectedObjectsEvent;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera 
{
    // TODO: Define fields and methods.
    //fields:
    private int id;
    private int frequency;
    private STATUS status;
    private LinkedList<StampedDetectedObjects> stampDetectedObjects;

    public Camera (int id, int frequency)
    {
        this.id=id;
        this.frequency=frequency;
        this.status=STATUS.UP;

        Gson gson = new Gson();
        try (FileReader reader = new FileReader("./camera_data.json")) 
        {
            // Convert JSON File to Java Object
            Type camerasDataBaseType = new TypeToken<LinkedList<LinkedList<StampedDetectedObjects>>>(){}.getType();
            LinkedList<LinkedList<StampedDetectedObjects>> DataBases = gson.fromJson(reader, camerasDataBaseType);
            this.stampDetectedObjects = DataBases.get(id);
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public int getFrequency()
    {
        return frequency;
    }
    public int getID()
    {
        return id;
    }
//returns the objects captured list at a certin time tick
    public LinkedList<DetectedObject> getDetectedObjectsAtTick(int tick)
    {
        LinkedList<DetectedObject> DetectedObjectsAtTick = new LinkedList<DetectedObject>();
        for(StampedDetectedObjects obj: stampDetectedObjects)
        {
            if(obj.getTime() == tick)
            {
                DetectedObjectsAtTick = obj.getList();
                break;
            }
        }
        return DetectedObjectsAtTick;
    }
    
    //returns the event that matches the certin tick
    public DetectedObjectsEvent activateTick(int tick)
    {
        LinkedList<DetectedObject> detectedObjects = this.getDetectedObjectsAtTick(tick);
        if(!detectedObjects.isEmpty())
        {
    //id error??
            DetectedObjectsEvent event = new DetectedObjectsEvent(tick, detectedObjects, id);
            for(int i=0; i<detectedObjects.size(); i++)
            {
                StatisticalFolder.getInstance().incrementNumDetectedObjects();
            }
            return event;
        }
        return null;
    }
}
