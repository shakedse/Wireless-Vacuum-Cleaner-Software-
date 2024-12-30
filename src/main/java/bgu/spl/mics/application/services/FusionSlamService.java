package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.*;
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

    public FusionSlamService(FusionSlam fusionSlam) {
        super("FusionSlam");
        this.fusionSlam = fusionSlam;
        // TODO Implement this
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
            
        });
        
    //TrackedObjectsEvent        
        subscribeEvent(TrackedObjectsEvent.class ,(TrackedObjectsEvent event) ->{
            //checking if the object is new or previously detected
            for(Object obj : event.getTrackedObjectsList())
            {
                TrackedObject trackedObject = (TrackedObject) obj;
                if(!fusionSlam.getInstance().getTrackedObjects().contains(trackedObject))//if the object is new
                {
                    fusionSlam.getTrackedObjects().add(trackedObject);//add it to the tracked objects list
                    fusionSlam.addNewLandMark(trackedObject);//add it to the map
                }
                else//if the object is previously detected
                {
                    //updates measurements by averaging with previous
                    fusionSlam.updateOldLandMark(trackedObject);  
                }
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
                sendBroadcast(new TerminatedBroadcast("fusionSlam"));//tell everyone that the camera terminated itself
                terminate();
            }
        });

    //CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class ,(CrashedBroadcast crashed) ->{
            terminate();
        });
    }
}
