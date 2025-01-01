package bgu.spl.mics.application.messages;

import java.util.LinkedList;
import java.util.List;


import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.*;

public class DetectedObjectsEvent implements Event<Boolean> {
    private LinkedList<DetectedObject> detectedObjects;
    private int detectedTime;
    private int cameraID;
    private boolean isRemoved;

    public DetectedObjectsEvent(int detectedTime, LinkedList<DetectedObject> detectedObjects,int cameraID)
    {
        this.detectedTime = detectedTime;
        this.detectedObjects = detectedObjects;
        this.cameraID=cameraID;
        this.isRemoved = false;
    }
    public boolean isRemoved()
    {
        return isRemoved;
    }
    
    public void remove()
    {
        isRemoved = true;
    }

    public int getdetectedTime()
    {
        return detectedTime;
    }
    public int getCameraID()
    {
        return cameraID;
    }
    public LinkedList<DetectedObject> getDetectedObjects()
    {
        return detectedObjects;
    }
}
