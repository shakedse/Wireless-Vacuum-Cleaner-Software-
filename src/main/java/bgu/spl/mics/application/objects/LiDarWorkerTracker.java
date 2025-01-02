package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.List;

import bgu.spl.mics.application.messages.DetectedObjectsEvent;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;

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
    private LinkedList<TrackedObject> trackedObjects = new LinkedList<TrackedObject>();
     
    public LiDarWorkerTracker (int id, int frequency)
    {
        this.id=id;
        this.frequency=frequency;
        this.status=STATUS.UP;
    }
    public int getID()
    {
        return id;
    }

    public int getFrequency()
    {
        return frequency;
    }

    public STATUS getStatus()
    {
        return status;
    }
    public void statusDown()   
    {
        this.status = STATUS.DOWN;
    }

    //returns the tracked objects that has a cloudpoints list at a time
    public TrackedObject getTrackedObjects(DetectedObject detectedObject, int time)
    {
        LiDarDataBase dataBase = LiDarDataBase.getInstance();
        for(StampedCloudPoints point: dataBase.getCloudPoints())
        {
            LinkedList<CloudPoint> cloudPoints = new LinkedList<CloudPoint>();
            for(int i=0; i<point.getCloudPoints().size();i++) // for each cloud point in the list
            {
                cloudPoints.add(new CloudPoint (point.getCloudPoints().get(i).get(0),point.getCloudPoints().get(i).get(1)));
            }
            if(point.getID().equals(detectedObject.getID())) // if the deteced object exists in the LiDarDataBase 
               {
                if (time == point.getTime()){
                // creating the object
                    TrackedObject trackedObject = new TrackedObject(point.getID(), point.getTime(), detectedObject.getDescription(), cloudPoints); 
                    //adding the object found to the tracked objects final list
                    if(trackedObjects == null)
                        trackedObjects = new LinkedList<TrackedObject>();
                    trackedObjects.add(trackedObject);
                    return trackedObject;
               }
            }
        }
        return null;
    }

    public TrackedObjectsEvent convertDetectedToTracked(DetectedObjectsEvent detectedObjectsEvent)
    {
        TrackedObjectsEvent trackedObjectsEvent = new TrackedObjectsEvent(((Integer)this.getID()).toString(), detectedObjectsEvent.getdetectedTime());
        for(DetectedObject detectedObject: (LinkedList<DetectedObject>)detectedObjectsEvent.getDetectedObjects())
        {
            TrackedObject trackedObject = getTrackedObjects(detectedObject, detectedObjectsEvent.getdetectedTime());
            if(trackedObject != null)
            {
                trackedObjectsEvent.addTrackedObject(trackedObject);
                // adding a tracked object to the statistical folder each time we track an object
                StatisticalFolder.getInstance().incrementNumTrackedObjects(); 
            }
            else {
                if (this.getStatus() == STATUS.ERROR)// if the detected object id is ERROR
                {
                    return new TrackedObjectsEvent(((Integer)this.getID()).toString(), detectedObjectsEvent.getdetectedTime(),
                    new TrackedObject(((Integer)this.getID()).toString(), 0, "ERROR", new LinkedList<CloudPoint>()));
                }
            }
        }
        return trackedObjectsEvent;
    }

    public void checkForErrors(int time)
    {
        LiDarDataBase dataBase = LiDarDataBase.getInstance();
        for(StampedCloudPoints point: dataBase.getCloudPoints())
        {
            if (point.getTime() == time)
            {
                if (point.getID().equals("ERROR"))
                {
                    this.status = STATUS.ERROR;
                }
            }
        }
    }
}
