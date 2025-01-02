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

    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) 
    {
        super("PoseService");
        this.myPose = gpsimu;
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
            if(GPSIMU.getInstance().getStatus() == STATUS.DOWN)
            {
                System.out.println("PoseService is down at time:" + (tick.getTick()));
                sendBroadcast(new TerminatedBroadcast("PoseService"));
                terminate();
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminate) -> {
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
