package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.List;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectedObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
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
        this.status=STATUS.UP;
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

    //returns the tracked objects that has a cloudpoints list at a time
    public TrackedObject getTrackedObjects(DetectedObject detectedObject)
    {
        LiDarDataBase dataBase = LiDarDataBase.getInstance("path");
        for(StampedCloudPoints point: dataBase.getCloudPoints())
        {
            LinkedList<CloudPoint> cloudPoints = new LinkedList<CloudPoint>();
            for(int i=0; i<point.getCloudPoints().size();i++) // for each cloud point in the list
            {
                cloudPoints.add(new CloudPoint (point.getCloudPoints().get(i).get(0),point.getCloudPoints().get(i).get(1)));
            }
            if(point.getID().equals(detectedObject.getID())) // if the deteced object exists in the LiDarDataBase 
               { 
                // creating the object
                TrackedObject trackedObject = new TrackedObject(point.getID(), point.getTime(), detectedObject.getDescription(), cloudPoints); 
                 //adding the object found to the tracked objects final list
                
                 this.trackedObjects.add(trackedObject);
                return trackedObject;
               }
        }
         return null;
    }
}
