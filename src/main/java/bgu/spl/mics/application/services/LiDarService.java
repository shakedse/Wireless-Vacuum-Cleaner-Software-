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
 * This service interacts with the LiDarWorkerTracker object to retrieve and
 * process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService 
{

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service
     *                           will use to process data.
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
    protected void initialize() {
        // TODO Implement this
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            time = tick.getTick();

            myWorkerTracker.checkForErrors(time); // getting the current ticks event to check if the LiDar crashed

            if(myWorkerTracker.getStatus() == STATUS.ERROR) //if the camera crashed - terminate
            {
                myWorkerTracker.statusDown();
                sendBroadcast(new CrashedBroadcast("LiDar"+ myWorkerTracker.getID()));// send a broadcast that the LiDar crashed
                terminate();
            }

            else if (this.myWorkerTracker.getStatus() == STATUS.UP)
            {
                for (DetectedObjectsEvent decEvent : this.detectedObjects) 
                {
                    if ((time >= decEvent.getdetectedTime() - myWorkerTracker.getFrequency()) && (decEvent.isRemoved()==false))
                    {
                        // going over the detected objects at time currTime
                        TrackedObjectsEvent currEvent = myWorkerTracker.convertDetectedToTracked(decEvent);
                        decEvent.remove();
                        sendEvent(currEvent);
                        myWorkerTracker.setLastFrame(currEvent);
                    }
                    MessageBusImpl.getInstance().complete(decEvent, true); 
                }
            }

            int last = LiDarDataBase.getInstance().getCloudPoints().size() - 1;
            if (LiDarDataBase.getInstance().getCloudPoints().get(last).getTime() <= time - myWorkerTracker.getFrequency())
            {
                myWorkerTracker.statusDown();
                sendBroadcast(new TerminatedBroadcast(((Integer)myWorkerTracker.getID()).toString()));
                terminate();
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminate) -> {
            if (terminate.getTerminatedID().equals("TimeService"))// if the terminated MS is timeService
            {
                this.myWorkerTracker.statusDown();
                sendBroadcast(new TerminatedBroadcast(((Integer) myWorkerTracker.getID()).toString()));// tell everyone that the LiDar terminated itself
                terminate();
            }
            else if (this.myWorkerTracker.getStatus() == STATUS.DOWN)// if the LiDar status is down
            {
                sendBroadcast(new TerminatedBroadcast(((Integer) myWorkerTracker.getID()).toString()));// tell everyone that the LiDar terminated it
                terminate();
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crash) -> {
            terminate();
        });

        // checking if we got a detected objects event
        subscribeEvent(DetectedObjectsEvent.class, (DetectedObjectsEvent event) -> {
            
            if (time >= event.getdetectedTime() - myWorkerTracker.getFrequency()) 
            {
                TrackedObjectsEvent toSend = myWorkerTracker.convertDetectedToTracked(event);
                sendEvent(toSend);
                myWorkerTracker.setLastFrame(toSend);
            }
            else
            {
                detectedObjects.add(event);
            }
        });
        latch.countDown();
    }
}
