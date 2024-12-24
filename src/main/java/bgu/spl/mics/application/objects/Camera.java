package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private List<StampedDetectedObjects> stampDetectedObjects;

    public Camera (int id, int frequency)
    {
        this.id=id;
        this.frequency=frequency;
        this.status=STATUS.DOWN;
        this.stampDetectedObjects = new CopyOnWriteArrayList <StampedDetectedObjects>();//?????
    }

    public int getFrequency()
    {
        return frequency;
    }

    public CopyOnWriteArrayList<DetectedObject> getDetectedObjectsAtTick(int tick)
    {
        CopyOnWriteArrayList<DetectedObject> DetectedObjectsAtTick = new CopyOnWriteArrayList<DetectedObject>();
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
