package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

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
        this.status=STATUS.DOWN;
        this.stampDetectedObjects = new LinkedList<StampedDetectedObjects>();//?????
    }

    public int getFrequency()
    {
        return frequency;
    }
    public int getID()
    {
        return id;
    }

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

}
