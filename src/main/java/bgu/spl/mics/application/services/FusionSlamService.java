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
        super("The FusionSlam");
        this.fusionSlam = fusionSlam;
        // TODO Implement this
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class ,(TickBroadcast tick) ->{
            
        });

        subscribeEvent(TrackedObjectsEvent.class ,(TrackedObjectsEvent event) ->{
            
        });

        subscribeEvent(PoseEvent.class ,(PoseEvent pose) ->{
            fusionSlam.getInstance().addPose((Pose)pose.getPose());
        });

        subscribeBroadcast(TerminatedBroadcast.class ,(TerminatedBroadcast terminate) ->{
            if(terminate.getTerminated().getClass() == TimeService.class)
            {
                terminate();
            }
        });

        subscribeBroadcast(CrashedBroadcast.class ,Call ->{
       
        });
    }
}
