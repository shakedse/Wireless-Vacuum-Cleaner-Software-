package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private List<TrackedObject> trackedObjects;
     
    public LiDarWorkerTracker (int id, int frequency)
    {
        this.id=id;
        this.frequency=frequency;
        this.status=STATUS.DOWN;
        this.trackedObjects = new CopyOnWriteArrayList <TrackedObject>();//?????
    }
}
