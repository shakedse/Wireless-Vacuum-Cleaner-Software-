package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects 
{
    // TODO: Define fields and methods.
    //fields:
    private int time;
    private CopyOnWriteArrayList<DetectedObject> DetectedObjects;
    
    public StampedDetectedObjects (int time)
    {
        this.time=time; // ????
        this.DetectedObjects = new CopyOnWriteArrayList <DetectedObject>();//?????
    }

    public int getTime()
    {
        return time;
    }

    public CopyOnWriteArrayList<DetectedObject> getList()
    {
        return DetectedObjects;
    }
}
