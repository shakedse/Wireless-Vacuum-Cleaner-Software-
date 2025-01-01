package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.*;

import java.util.concurrent.CountDownLatch;

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
    private int cameras;
    private int lidars;
    private boolean pose;
    private boolean timeService;
    private final CountDownLatch latch;

    public FusionSlamService(FusionSlam fusionSlam, int cam, int lid, CountDownLatch latch) {
        super("FusionSlam");
        this.fusionSlam = fusionSlam;
        // TODO Implement this
        this.cameras = cam;
        this.lidars = lid;
        this.pose = true;
        this.timeService = true;
        this.latch = latch;
    }
    public void setCameras() {
        this.cameras--;
    }
    public void setLidars() {
        this.lidars--;
    }
    public void setPose() {
        this.pose = false;
    }
    public void setTimeService() {
        this.timeService = false;
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
                setTimeService();
                fusionSlam.setEarlyFinish();
                sendBroadcast(new TerminatedBroadcast("fusionSlam"));//tell everyone that the camera terminated itself
                terminate();
            }
            if(terminate.getTerminatedID().equals("Camera"))//if the terminated MS is camera - decrease the number of cameras
            {
                setCameras();
            }
            if(terminate.getTerminatedID().equals("LiDar"))//if the terminated MS is LiDar - decrease the number of LiDars
            {
                setLidars();
            }
            if(terminate.getTerminatedID().equals("PoseService"))//if the terminated MS is PoseService - set the pose to false
            {
                setPose();
            }
            if(cameras == 0 && lidars == 0 && !pose)//if all the services are terminated
            {
                fusionSlam.setEarlyFinish();
                sendBroadcast(new TerminatedBroadcast("fusionSlam"));//tell everyone that the fusionSlam terminated itself
                terminate();
            }
        });

    //CrashedBroadcast
        subscribeBroadcast(CrashedBroadcast.class ,(CrashedBroadcast crashed) ->{
            terminate();
        });
    }
}
