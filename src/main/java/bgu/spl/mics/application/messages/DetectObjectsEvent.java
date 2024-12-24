package bgu.spl.mics.application.messages;

import java.util.List;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.*;

public class DetectObjectsEvent implements Event {
    private List<DetectedObject> detectedObjects;
    private int time;

    public DetectObjectsEvent(int time, List<DetectedObject> detectedObjects)
    {
        this.time = time;
        this.detectedObjects = detectedObjects;
    }
    
    public int getTime()
    {
        return time;
    }

    public List<DetectedObject> getDetectedObjects()
    {
        return detectedObjects;
    }
}
