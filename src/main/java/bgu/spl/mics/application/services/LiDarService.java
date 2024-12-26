package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.*;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */

    private final LiDarWorkerTracker myWorkerTracker;
    private int time;
    private BlockingQueue<DetectObjectsEvent<DetectedObject>> detectedObjects;

    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) 
    {
        super("LidarWorker" + LiDarWorkerTracker.getID());
        this.myWorkerTracker = LiDarWorkerTracker;
        this.detectedObjects = new LinkedBlockingQueue<DetectObjectsEvent<DetectedObject>>();
        time = 0;
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() 
    {
        // TODO Implement this
        subscribeBroadcast(TickBroadcast.class ,(TickBroadcast tick) ->{
            time = tick.getTick(); // why time exiets
            if(time == detectedObjects.peek().getTime() - myWorkerTracker.getFrequency())
            {
                DetectObjectsEvent<DetectedObject> detectionEvent = detectedObjects.poll();
                LinkedList<TrackedObject> TrackedObjectsList = myWorkerTracker.getTrackedObjects(detectionEvent.getDetectedObjects(),time);
                if(!TrackedObjectsList.isEmpty())
                {
                    TrackedObjectsEvent<TrackedObject> event = new TrackedObjectsEvent<TrackedObject>(TrackedObjectsList);
                    sendEvent(event); //returns Future???
                    System.out.println("TickBroadcast" + event);
                }
                MessageBusImpl.getInstance().complete(detectionEvent, TrackedObjectsList);
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class ,(TerminatedBroadcast terminate) ->{
            if(terminate.getTerminated().getClass() == TimeService.class)
            {
                terminate();
            }
        });

        subscribeBroadcast(CrashedBroadcast.class ,Call ->{
       
        });

        subscribeEvent(DetectObjectsEvent.class ,(DetectObjectsEvent event) ->{
            detectedObjects.add(event);
        });
    }
}

