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
    private int cameraNum;//we want to know how many cameras are there in the system

    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker, CountDownLatch latch, int cameraNum) 
    {
        super("LidarWorker" + LiDarWorkerTracker.getID());
        this.myWorkerTracker = LiDarWorkerTracker;
        this.detectedObjects = new LinkedList<DetectedObjectsEvent>();
        time = 0;
        this.latch = latch;
        this.cameraNum = cameraNum;
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
            int currTime = tick.getTick();
            if (this.myWorkerTracker.getStatus() == STATUS.UP){
            for (DetectedObjectsEvent decEvent : this.detectedObjects) 
            {
                if ((currTime >= decEvent.getdetectedTime() + myWorkerTracker.getFrequency()) && (decEvent.isRemoved()==false))
                {
                    // going over the detected objects at time currTime
                        TrackedObjectsEvent currEvent = myWorkerTracker.convertDetectedToTracked(decEvent);
                        if (currEvent.getTrackedObjectsList().get(0).getDescription().equals("ERROR"))// if the detected object id is ERROR
                        {
                            sendBroadcast(new CrashedBroadcast("LiDar"+ myWorkerTracker.getID()));// send a broadcast that the LiDar crashed
                            terminate();
                        }
                        else
                        {
                            decEvent.remove();
                            sendEvent(currEvent);
                        }
                    }
                    if(this.myWorkerTracker.getStatus() == STATUS.DOWN)
                    {
                        sendBroadcast(new TerminatedBroadcast("LiDar"+ myWorkerTracker.getID()));// send a broadcast that the LiDar terminate itself
                        terminate();
                    }
                    MessageBusImpl.getInstance().complete(decEvent, true); 

                }
        }
        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminate) -> {
            boolean isFinish = false;
            if (terminate.getTerminatedID().equals("TimeService"))// if the terminated MS is timeService
            {
                this.myWorkerTracker.statusDown();
                sendBroadcast(new TerminatedBroadcast(((Integer) myWorkerTracker.getID()).toString()));// tell everyone that the LiDar terminated itself
                terminate();
            }
            else if(terminate.getTerminatedID().equals("Camera"))//if the terminated MS is camera - terminate me too
            {
                cameraNum--;
                if(cameraNum==0)//if no cameras are left
                {
                    for(DetectedObjectsEvent d : detectedObjects)
                    {
                        if (d.isRemoved()==false)
                        {
                            break;
                        }
                    }
                    isFinish = true;//all the detected objects were sent
                }
                if(isFinish)//no more cameras and no more detected objects so we can terminate
                {
                    this.myWorkerTracker.statusDown();
                    sendBroadcast(new TerminatedBroadcast(((Integer) myWorkerTracker.getID()).toString()));// tell everyone that the LiDar terminated itself
                    terminate();
                }             
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
            if ((myWorkerTracker.getStatus() == STATUS.UP)
                    && (time >= event.getdetectedTime() + myWorkerTracker.getFrequency())) 
            {
                TrackedObjectsEvent toSend = myWorkerTracker.convertDetectedToTracked(event);      
                if (((TrackedObject)toSend.getTrackedObjectsList().get(0)).getId().equals("ERROR"))// if the detected                                                           // object id is ERROR
                {
                    sendBroadcast(new CrashedBroadcast("LiDar"));// send a broadcast that the lidar crashed
                    terminate();
                } 
                else 
                {
                     sendEvent(toSend);
                }
            }
            else
            {
            detectedObjects.add(event);
            }
        });
        latch.countDown();
    }
}
