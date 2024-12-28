package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.List;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker
{

    // TODO: Define fields and methods.
    //FIELDS:
    private int id;
    private int frequency;
    private STATUS status;
    private LinkedList<TrackedObject> trackedObjects;
     
    public LiDarWorkerTracker (int id, int frequency)
    {
        this.id=id;
        this.frequency=frequency;
        this.status=STATUS.DOWN;
        this.trackedObjects = new LinkedList<TrackedObject>();//?????
    }
    public int getID()
    {
        return id;
    }

    public int getFrequency()
    {
        return frequency;
    }

    public LinkedList<TrackedObject> getTrackedObjects(LinkedList<DetectedObject> detectedObjects, int time)
    {
        trackedObjects = new LinkedList<TrackedObject>(); 
        LiDarDataBase dataBase = LiDarDataBase.getInstance("path");
        for(StampedCloudPoints cp: dataBase.getCloudPoints())
        {
            for(DetectedObject detected: detectedObjects)
                if(cp.getID().equals(detected.getID()) && cp.getTime() <= time) // if the deteced objects exists in the LiDarDataBase and the time detected is right
                    trackedObjects.add(new TrackedObject(detected.getID(), time, detected.getDescription(), cp.getCloudPoints())); // adding the object to the tracked list
        }
        return trackedObjects;
    }
}
