package bgu.spl.mics.application.messages;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.*;

public class PoseEvent<Pose> implements Event<Pose>{
    private Pose currentPose;

    public PoseEvent(Pose currentPose)
    {
        this.currentPose = currentPose;
    }

    public Pose getPose()
    {
        return currentPose;
    }
}
