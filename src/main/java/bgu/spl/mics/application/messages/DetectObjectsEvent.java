package bgu.spl.mics.application.messages;

import java.util.LinkedList;
import java.util.List;


import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.*;

public class DetectObjectsEvent<DetectedObject> implements Event {
    private LinkedList<DetectedObject> detectedObjects;
    private int time;

    public DetectObjectsEvent(int time, LinkedList<DetectedObject> detectedObjects)
    {
        this.time = time;
        this.detectedObjects = detectedObjects;
    }
    
    public int getTime()
    {
        return time;
    }

    public LinkedList<DetectedObject> getDetectedObjects()
    {
        return detectedObjects;
    }
}
