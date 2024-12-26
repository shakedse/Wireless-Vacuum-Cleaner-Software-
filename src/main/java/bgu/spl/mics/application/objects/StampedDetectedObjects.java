package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects 
{
    // TODO: Define fields and methods.
    //fields:
    private int time;
    private LinkedList<DetectedObject> DetectedObjects;
    
    public StampedDetectedObjects (int time)
    {
        this.time=time; // ????
        this.DetectedObjects = new LinkedList<DetectedObject>();//?????
    }

    public int getTime()
    {
        return time;
    }

    public LinkedList<DetectedObject> getList()
    {
        return DetectedObjects;
    }
}
