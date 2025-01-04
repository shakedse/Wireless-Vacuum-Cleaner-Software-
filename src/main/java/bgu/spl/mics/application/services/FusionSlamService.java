package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.*;
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
            /*
             * @pre: The tick broadcast is received.    
             * @post: The FusionSLAM service checks if there are any new objects to add to the map.
             *       If there are, the service adds them to the map and removes them from the waiting list.
             *      The service then checks if the current tick matches the time of any waiting objects.
             *     If it does, the service adds the object to the map and removes it from the waiting list.
             * @inv: The FusionSLAM service is subscribed to the tick broadcast.
             * @post: The FusionSLAM service has updated the map with new objects and poses.
             *@param: tick - The tick broadcast.
             @return: None.

             */
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
            /*
             * @pre: The TrackedObjectsEvent is received.
             * @post: The FusionSLAM service checks if the detected objects are new or previously detected.
             * @inv: The FusionSLAM service is subscribed to the TrackedObjectsEvent.
             * @param: event - The TrackedObjectsEvent.
             * @return: None.
             */
            //checking if the object is new or previously detected
            for(TrackedObject trackedObject : event.getTrackedObjectsList())
            {                
                fusionSlam.ChecksIfExist(trackedObject);
            }
        });
        
    //PoseEvent
        subscribeEvent(PoseEvent.class ,(PoseEvent pose) ->{
            /*
             * @pre: The PoseEvent is received.
             * @post: The FusionSLAM service adds the current pose to the map if it is not already present.
             * @inv: The FusionSLAM service is subscribed to the PoseEvent.
             * @param: pose - The PoseEvent.
             * @return: None.
             */
            fusionSlam.addPose((Pose)pose.getPose());//adding the current pose if it is not present in the poses list
        });
        
    //TerminatedBroadcast
        subscribeBroadcast(TerminatedBroadcast.class ,(TerminatedBroadcast terminate) ->{
            /*
             * @pre: The TerminatedBroadcast is received.
             * @post: The FusionSLAM service checks if the terminated microservice is the TimeService.
             *      If it is, the service terminates itself and broadcasts a TerminatedBroadcast.
             *     If the message queue is empty, the service terminates itself and broadcasts a TerminatedBroadcast.
             * @inv: The FusionSLAM service is subscribed to the TerminatedBroadcast.
             * @param: terminate - The TerminatedBroadcast.
             * @return: None.
             */
            if(terminate.getTerminatedID().equals("TimeService") )//if the terminated MS is timeService - terminate me too
            {
                StatisticalFolder.getInstance().setEarlyFinish();
                sendBroadcast(new TerminatedBroadcast("fusionSlam"));//tell everyone that the camera terminated itself
                terminate();
                fusionSlam.buildOutput();
            }
            else{
                System.out.println("the size of the message queue is: " + MessageBusImpl.getInstance().getMessageQueue().size());
                if(MessageBusImpl.getInstance().getMessageQueue().size() <= 2)
                {
                    StatisticalFolder.getInstance().setEarlyFinish();
                    System.out.println("fusionSlam is down");
                    sendBroadcast(new TerminatedBroadcast("fusionSlam"));//tell everyone that the camera terminated itself
                    terminate();
                    fusionSlam.buildOutput();
                }
            }
        });

    //CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class ,(CrashedBroadcast crashed) ->{
            /*
             * @pre: The CrashedBroadcast is received.
             * @post: The FusionSLAM service broadcasts a CrashedBroadcast and terminates itself.
             * @inv: The FusionSLAM service is subscribed to the CrashedBroadcast.
             * @param: crashed - The CrashedBroadcast.
             * @return: None.
             */
            StatisticalFolder.getInstance().setEarlyFinish();
            fusionSlam.errorOutPut(crashed);
            terminate();
        });
    }
}
