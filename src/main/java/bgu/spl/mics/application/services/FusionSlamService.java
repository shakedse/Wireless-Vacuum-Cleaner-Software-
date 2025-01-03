package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectedObjectsEvent;
import bgu.spl.mics.application.messages.LastCameraFrameEvent;
import bgu.spl.mics.application.messages.LastLiDarFrameEvent;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.*;

import com.google.gson.Gson;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */

    private FusionSlam fusionSlam;
    private final CountDownLatch latch;

    public FusionSlamService(FusionSlam fusionSlam, CountDownLatch latch) 
    {
        super("FusionSlam");
        this.fusionSlam = fusionSlam;
        // TODO Implement this
        this.latch = latch;
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() 
    {
    //TickBroadcast
        subscribeBroadcast(TickBroadcast.class ,(TickBroadcast tick) ->
        {
            List<TrackedObject> toRemove = new LinkedList<>();
            for (TrackedObject waitingObject : fusionSlam.getWaitingObjects()) { // For each object
                if (waitingObject.getTime() == tick.getTick()) { // If we found a new pose
                    fusionSlam.ChecksIfExist(waitingObject);
                    toRemove.add(waitingObject); // Mark it for removal
                }
            }
            // Remove all marked objects after the iteration
            fusionSlam.getWaitingObjects().removeAll(toRemove);
        });
        
    //TrackedObjectsEvent        
        subscribeEvent(TrackedObjectsEvent.class ,(TrackedObjectsEvent event) ->{
            //checking if the object is new or previously detected
            for(TrackedObject trackedObject : event.getTrackedObjectsList())
            {                
                fusionSlam.ChecksIfExist(trackedObject);
            }
        });
        
    //PoseEvent
        subscribeEvent(PoseEvent.class ,(PoseEvent pose) ->{
            fusionSlam.getInstance().addPose((Pose)pose.getPose());//adding the current pose if it is not present in the poses list
        });
        
    //TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class ,(TerminatedBroadcast terminate) ->{
            if(terminate.getTerminatedID().equals("TimeService") )//if the terminated MS is timeService - terminate me too
            {
                fusionSlam.setEarlyFinish();
                sendBroadcast(new TerminatedBroadcast("fusionSlam"));//tell everyone that the camera terminated itself
                terminate();
                fusionSlam.buildOutput();
            }
            else{
                System.out.println("the size of the message queue is: " + MessageBusImpl.getInstance().getMessageQueue().size());
                if(MessageBusImpl.getInstance().getMessageQueue().size() <= 2)
                {
                    fusionSlam.setEarlyFinish();
                    System.out.println("fusionSlam is down");
                    sendBroadcast(new TerminatedBroadcast("fusionSlam"));//tell everyone that the camera terminated itself
                    terminate();
                    fusionSlam.buildOutput();
                }
            }
        });

    //CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class ,(CrashedBroadcast crashed) ->{
            fusionSlam.setEarlyFinish();
            fusionSlam.errorOutPut(crashed);
            terminate();
        });

        subscribeEvent(LastCameraFrameEvent.class ,(LastCameraFrameEvent event) ->{
            fusionSlam.addLastFrameCamera(event.getName(), (DetectedObjectsEvent)event.getLastFrame());
        });

        subscribeEvent(LastLiDarFrameEvent.class ,(LastLiDarFrameEvent event) ->{
            fusionSlam.addLastFrameLidar(event.getName(), (TrackedObjectsEvent)event.getLastFrame());
        });
    }
}
