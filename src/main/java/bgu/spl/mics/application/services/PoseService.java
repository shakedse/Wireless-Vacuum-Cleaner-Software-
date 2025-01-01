package bgu.spl.mics.application.services;

import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.MicroService;

/**
 * PoseService is responsible for maintaining the robot's current pose (position
 * and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {
    private GPSIMU myPose;
    private int camersNum;
    private int lidarsNum;

    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu, int camersNum, int lidarsNum) {
        super("PoseService");
        this.myPose = gpsimu;
        this.camersNum = camersNum;
        this.lidarsNum = lidarsNum;
        // TODO Implement this
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the
     * current pose.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
            myPose.setTick(tick.getTick());
            // returns future??
            sendEvent(new PoseEvent(myPose.getPoseAtTick(tick.getTick())));// creating an event at a certin tick
        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminate) -> {
            if (terminate.getTerminatedID().equals("Camera"))// if the terminated MS is camera
            {
                camersNum--;// decrease the number of cameras
            }
            if (terminate.getTerminatedID().equals("LiDar"))// if the terminated MS is liDar
            {
                lidarsNum--;// decrease the number of lidars
            }
            if (camersNum == 0 && lidarsNum == 0)// if there are no more cameras and lidars
            {
                sendBroadcast(new TerminatedBroadcast("PoseService"));// tell everyone that the poseService is terminated
                terminate();
            }
            if (terminate.getTerminatedID().equals("TimeService"))// if the terminated MS is timeService
            {
                this.myPose.statusDown();// set the status of the pose to down
                sendBroadcast(new TerminatedBroadcast("PoseService"));// tell everyone tha
                terminate();
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crash) -> {
            terminate();
        });
    }
}
