package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectedObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;

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
    private final CountDownLatch latch; 
    private int time;
    private LinkedList<DetectedObjectsEvent> detectedObjects;

    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker, CountDownLatch latch) 
    {
        super("LidarWorker" + LiDarWorkerTracker.getID());
        this.myWorkerTracker = LiDarWorkerTracker;
        this.detectedObjects = new LinkedList<DetectedObjectsEvent>();
        time = 0;
        this.latch = latch;
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectedObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() 
    {
        // TODO Implement this
        subscribeBroadcast(TickBroadcast.class ,(TickBroadcast tick) ->{
           int curTime = tick.getTick();
           TrackedObjectsEvent currEvent = new TrackedObjectsEvent<>();
           for(DetectedObjectsEvent decEvent : detectedObjects)
            {
                if(curTime >= decEvent.getdetectedTime() + myWorkerTracker.getFrequency())
                {
                //going over the detected objects at time currTime
                for(DetectedObject d : (List<DetectedObject>)decEvent.getDetectedObjects())
                {
                    if(d.getID().equals("ERROR"))//if the detected object id is ERROR
                    {
                        sendBroadcast(new CrashedBroadcast("LiDar"));//send a broadcast that the camera crashed
                        terminate();
                    }
                    TrackedObject trackedObject = myWorkerTracker.getTrackedObjects(d);//crating the tracked object with the cloud points from the data base
                    if(trackedObject!=null)
                    {
                        StatisticalFolder.getInstance().incrementNumTrackedObjects(); //adding a tracked object to the statistical folder each time we track an object
                        currEvent.addTrackedObject(trackedObject);
                    }
                }
                sendEvent(currEvent); //returns Future???
                MessageBusImpl.getInstance().complete(decEvent,true);
                }      
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class ,(TerminatedBroadcast terminate) -> {
            if(terminate.getTerminatedID().equals("TimeService") )//if the terminated MS is timeService
            {
                sendBroadcast(new TerminatedBroadcast(((Integer)myWorkerTracker.getID()).toString()));//tell everyone tha
                terminate();
            }
        });

        subscribeBroadcast(CrashedBroadcast.class ,Call ->{
       
        });
        //checking if we got a detected objects event 
        subscribeEvent(DetectedObjectsEvent.class ,(DetectedObjectsEvent event) ->{
            
            detectedObjects.add(event);
        });
    }
}

