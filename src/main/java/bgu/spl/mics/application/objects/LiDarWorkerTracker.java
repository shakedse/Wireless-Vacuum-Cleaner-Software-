package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

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
    private LinkedBlockingQueue<TrackedObject> trackedObjects;
     
    public LiDarWorkerTracker (int id, int frequency)
    {
        this.id=id;
        this.frequency=frequency;
        this.status=STATUS.DOWN;
        this.trackedObjects = new LinkedBlockingQueue <TrackedObject>();//?????
    }
    public int getID()
    {
        return id;
    }

    public int getFrequency()
    {
        return frequency;
    }

    public LinkedBlockingQueue<TrackedObject> getTrackedObjects()
    {
        return trackedObjects;
    }
}
